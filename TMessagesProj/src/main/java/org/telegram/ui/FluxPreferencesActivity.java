package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;

import androidx.documentfile.provider.DocumentFile;

import com.fluxgram.plugin.FluxPlugin;
import com.fluxgram.plugin.PluginManager;
import com.fluxgram.plugin.PluginManifest;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * FluxGram Preferences screen, opened from the top row in Settings.
 *
 * Sections:
 *  - Plugins: entry point into the plugin system (see {@link PluginsActivity}).
 *             "My Plugins" and "Import from File" (zip or folder) are wired
 *             up; Auto-Update Plugins is a placeholder toggle.
 *  - Updates & About: current version, changelog, GitHub repository, and about links.
 */
public class FluxPreferencesActivity extends BaseFragment {

    private static final String FLUXGRAM_VERSION = "0.1.0 - dev";
    private static final String GITHUB_REPOSITORY_URL = "https://github.com/Nesonaragirl/Fluxgram";
    private static final String CHANGELOG_URL = "https://github.com/Nesonaragirl/Fluxgram/blob/los/CHANGELOG.md";

    /**
     * Log entries shown in the "Changelogs" popup. Kept in sync by hand with
     * CHANGELOG.md on the los branch (linked at the bottom of the popup for
     * the full history).
     */
    private static final String CHANGELOG_ENTRIES =
            "Plugins\n" +
            "\u2022 Added manifest.json plugin manifest format\n" +
            "\u2022 Added PluginManifest parser with validation\n" +
            "\u2022 Added PluginManager: scans flux_plugins/, loads each plugin's Entrypoint script\n" +
            "\u2022 Wired plugin loading into app startup\n" +
            "\u2022 Added zip/folder import for plugins\n" +
            "\u2022 My Plugins now lists installed plugins with visibility toggles\n" +
            "\u2022 Added retry queue so UI components attach reliably\n\n" +
            "FluxGram Preferences\n" +
            "\u2022 Added FluxGram Preferences screen under Settings\n" +
            "\u2022 Modernized layout with real switch toggles\n" +
            "\u2022 Removed the standalone Plugins on/off row\n" +
            "\u2022 Removed the Appearance section\n" +
            "\u2022 Wired up Changelog, GitHub Repository, and About FluxGram\n" +
            "\u2022 Renamed \"Check for Updates\" to \"Versions\"\n\n" +
            "Build\n" +
            "\u2022 Fixed R8 release build failure from the LuaJ dependency\n" +
            "\u2022 Added GitHub Actions CI workflow";

    private static final int ID_MY_PLUGINS = 1;
    private static final int ID_AUTO_UPDATE_PLUGINS = 3;
    private static final int ID_IMPORT_FROM_FILE = 4;

    private static final int ID_VERSIONS = 10;
    private static final int ID_CHANGELOG = 11;
    private static final int ID_GITHUB_REPOSITORY = 12;
    private static final int ID_ABOUT_FLUXGRAM = 13;

    private static final int REQUEST_CODE_IMPORT_ZIP = 4001;
    private static final int REQUEST_CODE_IMPORT_FOLDER = 4002;

    private boolean autoUpdatePlugins = true;

    private UniversalRecyclerView listView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("FluxGram Preferences");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        final FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, null);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        // Plugins
        items.add(UItem.asHeader("Plugins"));
        items.add(UItem.asButton(ID_MY_PLUGINS, R.drawable.settings_features, "My Plugins", "3"));
        items.add(UItem.asSwitch(ID_AUTO_UPDATE_PLUGINS, "Auto-Update Plugins").setChecked(autoUpdatePlugins));
        items.add(UItem.asButton(ID_IMPORT_FROM_FILE, "Import from File", ""));
        items.add(UItem.asShadow(null));

        // Updates & About
        items.add(UItem.asHeader("Updates & About"));
        items.add(UItem.asButton(ID_VERSIONS, "Versions", FLUXGRAM_VERSION));
        items.add(UItem.asButton(ID_CHANGELOG, "Changelog"));
        items.add(UItem.asButton(ID_GITHUB_REPOSITORY, "GitHub Repository"));
        items.add(UItem.asButton(ID_ABOUT_FLUXGRAM, "About FluxGram"));
        items.add(UItem.asShadow(null));
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ID_MY_PLUGINS) {
            presentFragment(new PluginsActivity());
        } else if (item.id == ID_AUTO_UPDATE_PLUGINS) {
            autoUpdatePlugins = !autoUpdatePlugins;
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        } else if (item.id == ID_IMPORT_FROM_FILE) {
            showImportSourcePicker();
        } else if (item.id == ID_CHANGELOG) {
            showChangelogDialog();
        } else if (item.id == ID_GITHUB_REPOSITORY) {
            openUrl(GITHUB_REPOSITORY_URL);
        } else if (item.id == ID_ABOUT_FLUXGRAM) {
            showAboutDialog();
        }
    }

    /** Shows the current version and recent log entries in an in-app popup. */
    private void showChangelogDialog() {
        if (getContext() == null) {
            return;
        }
        String message = FLUXGRAM_VERSION + "\n\n" + CHANGELOG_ENTRIES + "\n\nFull history: " + CHANGELOG_URL;
        new AlertDialog.Builder(getContext(), getResourceProvider())
                .setTitle("Changelogs")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void openUrl(String url) {
        if (getContext() == null) {
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            getContext().startActivity(intent);
        } catch (Exception e) {
            showImportError("Couldn't open a browser on this device.");
        }
    }

    private void showAboutDialog() {
        if (getContext() == null) {
            return;
        }
        new AlertDialog.Builder(getContext(), getResourceProvider())
                .setTitle("About FluxGram")
                .setMessage("FluxGram " + FLUXGRAM_VERSION + "\n\nA Telegram client fork with a Lua-based plugin system, built on top of DrKLO/Telegram.\n\n" + GITHUB_REPOSITORY_URL)
                .setPositiveButton("OK", null)
                .show();
    }

    /** Lets the user choose whether they're importing a .zip archive or an unpacked folder. */
    private void showImportSourcePicker() {
        if (getContext() == null) {
            return;
        }
        new AlertDialog.Builder(getContext(), getResourceProvider())
                .setTitle("Import Plugin")
                .setItems(new CharSequence[]{"From Zip File", "From Folder"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("application/zip");
                        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/zip", "application/x-zip-compressed"});
                        try {
                            startActivityForResult(intent, REQUEST_CODE_IMPORT_ZIP);
                        } catch (Exception e) {
                            showImportError("Couldn't open a file picker on this device.");
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        try {
                            startActivityForResult(intent, REQUEST_CODE_IMPORT_FOLDER);
                        } catch (Exception e) {
                            showImportError("Couldn't open a folder picker on this device.");
                        }
                    }
                })
                .show();
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        super.onActivityResultFragment(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        Uri uri = data.getData();
        if (requestCode == REQUEST_CODE_IMPORT_ZIP) {
            importZipFromUri(uri);
        } else if (requestCode == REQUEST_CODE_IMPORT_FOLDER) {
            importFolderFromUri(uri);
        }
    }

    private void importZipFromUri(Uri uri) {
        File tempZip = new File(ApplicationLoader.applicationContext.getCacheDir(), "flux_import_" + System.currentTimeMillis() + ".zip");
        try {
            copyUriToFile(uri, tempZip);
            FluxPlugin plugin = PluginManager.importFromZip(tempZip);
            showImportSuccess(plugin);
        } catch (IOException | PluginManifest.ManifestException e) {
            showImportError(e.getMessage() != null ? e.getMessage() : "Couldn't import that zip file.");
        } finally {
            tempZip.delete();
        }
    }

    private void importFolderFromUri(Uri treeUri) {
        File tempFolder = new File(ApplicationLoader.applicationContext.getCacheDir(), "flux_import_" + System.currentTimeMillis());
        try {
            DocumentFile sourceTree = DocumentFile.fromTreeUri(ApplicationLoader.applicationContext, treeUri);
            if (sourceTree == null || !sourceTree.isDirectory()) {
                showImportError("Couldn't read that folder.");
                return;
            }
            if (!tempFolder.mkdirs()) {
                showImportError("Couldn't prepare that folder for import.");
                return;
            }
            copyDocumentTree(sourceTree, tempFolder);
            FluxPlugin plugin = PluginManager.importFromFolder(tempFolder);
            showImportSuccess(plugin);
        } catch (IOException | PluginManifest.ManifestException e) {
            showImportError(e.getMessage() != null ? e.getMessage() : "Couldn't import that folder.");
        } finally {
            deleteRecursive(tempFolder);
        }
    }

    private void copyUriToFile(Uri uri, File dest) throws IOException {
        InputStream in = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
        if (in == null) {
            throw new IOException("Couldn't open the selected file.");
        }
        try {
            OutputStream out = new FileOutputStream(dest);
            try {
                byte[] buffer = new byte[8192];
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

    private void copyDocumentTree(DocumentFile srcDir, File destDir) throws IOException {
        for (DocumentFile child : srcDir.listFiles()) {
            if (child.getName() == null) {
                continue;
            }
            File dest = new File(destDir, child.getName());
            if (child.isDirectory()) {
                if (!dest.mkdirs()) {
                    throw new IOException("Couldn't create directory: " + dest.getPath());
                }
                copyDocumentTree(child, dest);
            } else {
                copyUriToFile(child.getUri(), dest);
            }
        }
    }

    private void deleteRecursive(File file) {
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

    private void showImportSuccess(FluxPlugin plugin) {
        if (getContext() == null) {
            return;
        }
        BulletinFactory.of(this).createSimpleBulletin(R.raw.contact_check, "Imported \"" + plugin.manifest.name + "\"").show();
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    private void showImportError(String message) {
        if (getContext() == null) {
            return;
        }
        BulletinFactory.of(this).createErrorBulletin(message).show();
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }
}
