package com.caint.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.telegram.messenger.ApplicationLoader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Discovers and loads plugins from disk, using each plugin's manifest.json
 * to find its Entrypoint script.
 *
 * Expected layout, one subfolder per plugin under the plugins root:
 *
 *   <pluginsRoot>/<pluginFolder>/manifest.json
 *   <pluginsRoot>/<pluginFolder>/<Entrypoint from manifest, e.g. main.lua>
 *   <pluginsRoot>/<pluginFolder>/...any other files the entrypoint needs
 *
 * A malformed manifest, a missing entrypoint file, or a plugin script that
 * throws while loading only skips that one plugin -- it never stops the
 * rest of the scan or crashes the host app.
 *
 * Plugins can also be brought in from outside the plugins root via
 * importFromZip() / importFromFolder() -- see below.
 */
public final class PluginManager {

    private static final String TAG = "Caint.PluginManager";
    private static final String PLUGINS_DIR_NAME = "caint_plugins";
    private static final String MANIFEST_FILE_NAME = "manifest.json";
    private static final String VISIBILITY_PREFS_NAME = "caint_plugin_visibility";
    private static final String IMPORT_STAGING_PREFIX = "._import_";

    private static final List<CaintPlugin> loadedPlugins = new ArrayList<>();
    private static final List<PluginLoadFailure> loadFailures = new ArrayList<>();

    /** Records why a plugin failed to load, so the UI can show it (with full logs) to the user. */
    public static final class PluginLoadFailure {
        public final String id;
        public final String pluginName;
        public final String message;
        public final String stackTrace;

        PluginLoadFailure(String id, String pluginName, String message, String stackTrace) {
            this.id = id;
            this.pluginName = pluginName;
            this.message = message;
            this.stackTrace = stackTrace;
        }
    }

    private PluginManager() {
    }

    /**
     * The root directory plugins are discovered from -- one subfolder per
     * plugin, created under the app's private files directory (no storage
     * permission required).
     */
    public static File getPluginsRoot() {
        File root = new File(ApplicationLoader.applicationContext.getFilesDir(), PLUGINS_DIR_NAME);
        if (!root.exists()) {
            root.mkdirs();
        }
        return root;
    }

    /** The plugins currently loaded in memory. Empty until loadAll() has run at least once. */
    public static List<CaintPlugin> getLoadedPlugins() {
        return Collections.unmodifiableList(loadedPlugins);
    }

    /** Plugins that failed to load on the last loadAll() call, with details for the error popup. */
    public static List<PluginLoadFailure> getLoadFailures() {
        return Collections.unmodifiableList(loadFailures);
    }
    }

    /**
     * Scans the plugins root directory and (re)loads every valid plugin
     * found there. Clears anything previously loaded first. Leftover import
     * staging folders (from an import that crashed mid-way) are cleaned up
     * and skipped.
     *
     * @return the plugins that loaded successfully
     */
    public static List<CaintPlugin> loadAll() {
        loadedPlugins.clear();
        loadFailures.clear();

        File root = getPluginsRoot();
        File[] pluginDirs = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (pluginDirs == null) {
            return getLoadedPlugins();
        }

        for (File pluginDir : pluginDirs) {
            if (pluginDir.getName().startsWith(IMPORT_STAGING_PREFIX)) {
                deleteRecursive(pluginDir);
                continue;
            }
            CaintPlugin plugin = loadPlugin(pluginDir);
            if (plugin != null) {
                loadedPlugins.add(plugin);
            }
        }

        return getLoadedPlugins();
    }

    /**
     * Whether a plugin should currently be shown/active -- defaults to
     * visible until the user explicitly hides it. Keyed by plugin id
     * (its folder name), so the setting survives reloads.
     */
    public static boolean isPluginVisible(String id) {
        return getVisibilityPrefs().getBoolean(id, true);
    }

    /** Shows or hides a plugin in "My Plugins" without uninstalling it. */
    public static void setPluginVisible(String id, boolean visible) {
        getVisibilityPrefs().edit().putBoolean(id, visible).apply();
    }

    private static SharedPreferences getVisibilityPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(VISIBILITY_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Imports a plugin from a .zip archive: extracts it into a new folder
     * under the plugins root, verifies it contains a valid manifest.json,
     * and reloads all plugins so the import takes effect immediately.
     *
     * The zip may contain the plugin's files directly at its root, or
     * wrapped in a single top-level folder (as GitHub's "Download ZIP"
     * produces) -- both layouts are handled.
     *
     * @param zipFile the .zip file to import
     * @return the newly loaded plugin
     * @throws IOException                      if the zip can't be read/extracted, or the
     *                                           extracted content isn't a valid plugin
     * @throws PluginManifest.ManifestException if the extracted manifest.json is invalid
     */
    public static CaintPlugin importFromZip(File zipFile) throws IOException, PluginManifest.ManifestException {
        File stagingDir = newStagingDir();
        try {
            unzip(zipFile, stagingDir);
            return finishImport(flattenSingleTopLevelFolder(stagingDir), stagingDir);
        } catch (IOException | PluginManifest.ManifestException e) {
            deleteRecursive(stagingDir);
            throw e;
        }
    }

    /**
     * Imports a plugin from an already-unpacked folder: copies its contents
     * into a new folder under the plugins root, verifies it contains a
     * valid manifest.json, and reloads all plugins so the import takes
     * effect immediately.
     *
     * @param sourceFolder the folder to import (its contents are copied, not moved)
     * @return the newly loaded plugin
     * @throws IOException                      if the folder can't be read/copied, or the
     *                                           content isn't a valid plugin
     * @throws PluginManifest.ManifestException if manifest.json is invalid
     */
    public static CaintPlugin importFromFolder(File sourceFolder) throws IOException, PluginManifest.ManifestException {
        if (!sourceFolder.isDirectory()) {
            throw new IOException("Not a folder: " + sourceFolder.getPath());
        }

        File stagingDir = newStagingDir();
        try {
            copyDirectoryContents(sourceFolder, stagingDir);
            return finishImport(flattenSingleTopLevelFolder(stagingDir), stagingDir);
        } catch (IOException | PluginManifest.ManifestException e) {
            deleteRecursive(stagingDir);
            throw e;
        }
    }

    private static File newStagingDir() throws IOException {
        File stagingDir = new File(getPluginsRoot(), IMPORT_STAGING_PREFIX + System.currentTimeMillis());
        if (!stagingDir.mkdirs()) {
            throw new IOException("Could not create a staging directory for the import");
        }
        return stagingDir;
    }

    /** Validates the staged content and moves it into its final, id-named home. */
    private static CaintPlugin finishImport(File contentRoot, File stagingDir) throws IOException, PluginManifest.ManifestException {
        File manifestFile = new File(contentRoot, MANIFEST_FILE_NAME);
        if (!manifestFile.exists()) {
            throw new IOException("No manifest.json found in the imported plugin");
        }
        PluginManifest manifest = PluginManifest.parse(readFile(manifestFile));

        File entrypointFile = new File(contentRoot, manifest.entrypoint);
        if (!entrypointFile.exists()) {
            throw new IOException("Entrypoint \"" + manifest.entrypoint + "\" not found in the imported plugin");
        }

        File destDir = uniquePluginDir(manifest.name);
        if (!destDir.mkdirs()) {
            throw new IOException("Could not create the plugin's directory");
        }
        copyDirectoryContents(contentRoot, destDir);
        deleteRecursive(stagingDir);

        Log.i(TAG, "Imported plugin \"" + manifest.name + "\" v" + manifest.version + " (" + destDir.getName() + ")");
        loadAll();

        for (CaintPlugin plugin : loadedPlugins) {
            if (plugin.id.equals(destDir.getName())) {
                return plugin;
            }
        }
        throw new IOException("Plugin was imported but failed to load -- check its manifest.json and entrypoint");
    }

    /**
     * If the staged import contains a single top-level folder and no
     * manifest.json of its own (the "GitHub zip download" layout), returns
     * that inner folder as the real content root; otherwise returns dir as-is.
     */
    private static File flattenSingleTopLevelFolder(File dir) {
        if (new File(dir, MANIFEST_FILE_NAME).exists()) {
            return dir;
        }
        File[] children = dir.listFiles();
        if (children != null && children.length == 1 && children[0].isDirectory()) {
            return children[0];
        }
        return dir;
    }

    private static File uniquePluginDir(String pluginName) {
        String base = sanitizeId(pluginName);
        if (base.isEmpty()) {
            base = "plugin";
        }
        File dir = new File(getPluginsRoot(), base);
        int suffix = 2;
        while (dir.exists()) {
            dir = new File(getPluginsRoot(), base + "_" + suffix);
            suffix++;
        }
        return dir;
    }

    private static String sanitizeId(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toLowerCase(Locale.US).toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else if (c == ' ' || c == '-' || c == '_') {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private static void unzip(File zipFile, File destDir) throws IOException {
        String destCanonicalPath = destDir.getCanonicalPath();
        byte[] buffer = new byte[8192];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        try {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(destDir, entry.getName());
                String outCanonicalPath = outFile.getCanonicalPath();
                if (!outCanonicalPath.equals(destCanonicalPath) && !outCanonicalPath.startsWith(destCanonicalPath + File.separator)) {
                    // zip-slip guard: skip entries that would land outside destDir
                    zis.closeEntry();
                    continue;
                }
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }
                    OutputStream out = new FileOutputStream(outFile);
                    try {
                        int read;
                        while ((read = zis.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    } finally {
                        out.close();
                    }
                }
                zis.closeEntry();
            }
        } finally {
            zis.close();
        }
    }

    private static void copyDirectoryContents(File srcDir, File destDir) throws IOException {
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new IOException("Could not create directory: " + destDir.getPath());
        }
        File[] children = srcDir.listFiles();
        if (children == null) {
            return;
        }
        byte[] buffer = new byte[8192];
        for (File child : children) {
            File dest = new File(destDir, child.getName());
            if (child.isDirectory()) {
                copyDirectoryContents(child, dest);
            } else {
                InputStream in = new FileInputStream(child);
                try {
                    OutputStream out = new FileOutputStream(dest);
                    try {
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    } finally {
                        out.close();
                    }
                } finally {
                    in.close();
                }
            }
        }
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }

    /**
     * Loads a single plugin from its folder. Returns null (and logs why)
     * if the manifest is missing/invalid, the entrypoint file is missing,
     * or the script itself throws while executing.
     */
    private static CaintPlugin loadPlugin(File pluginDir) {
        String id = pluginDir.getName();

        File manifestFile = new File(pluginDir, MANIFEST_FILE_NAME);
        if (!manifestFile.exists()) {
            Log.w(TAG, "Skipping \"" + id + "\": no manifest.json found");
            loadFailures.add(new PluginLoadFailure(id, null, "No manifest.json found", null));
            return null;
        }

        String manifestJson;
        try {
            manifestJson = readFile(manifestFile);
        } catch (IOException e) {
            Log.w(TAG, "Skipping \"" + id + "\": failed to read manifest.json", e);
            loadFailures.add(new PluginLoadFailure(id, null, "Failed to read manifest.json: " + e.getMessage(), Log.getStackTraceString(e)));
            return null;
        }

        PluginManifest manifest;
        try {
            manifest = PluginManifest.parse(manifestJson);
        } catch (PluginManifest.ManifestException e) {
            Log.w(TAG, "Skipping \"" + id + "\": " + e.getMessage());
            loadFailures.add(new PluginLoadFailure(id, null, e.getMessage(), Log.getStackTraceString(e)));
            return null;
        }

        File entrypointFile = new File(pluginDir, manifest.entrypoint);
        if (!entrypointFile.exists()) {
            Log.w(TAG, "Skipping \"" + id + "\" (" + manifest.name + "): entrypoint \""
                    + manifest.entrypoint + "\" not found");
            return null;
        }

        String script;
        try {
            script = readFile(entrypointFile);
        } catch (IOException e) {
            Log.w(TAG, "Skipping \"" + id + "\" (" + manifest.name + "): failed to read entrypoint", e);
            return null;
        }

        Globals globals = JsePlatform.standardGlobals();
        LuaTable caintTable = new CaintLuaBridge().asLuaTable();
        globals.set("Caint", caintTable);

        try {
            LuaValue chunk = globals.load(script, manifest.entrypoint);
            chunk.call();
        } catch (LuaError e) {
            Log.e(TAG, "Skipping \"" + id + "\" (" + manifest.name + "): script threw while loading", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Skipping \"" + id + "\" (" + manifest.name + "): unexpected error while loading", e);
            return null;
        }

        Log.i(TAG, "Loaded plugin \"" + manifest.name + "\" v" + manifest.version + " (" + id + ")");
        return new CaintPlugin(id, manifest.entrypoint, manifest, globals);
    }

    private static String readFile(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        InputStream in = new FileInputStream(file);
        try {
            int offset = 0;
            int read;
            while (offset < bytes.length && (read = in.read(bytes, offset, bytes.length - offset)) != -1) {
                offset += read;
            }
        } finally {
            in.close();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
