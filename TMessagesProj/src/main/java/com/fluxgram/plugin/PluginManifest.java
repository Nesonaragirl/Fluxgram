package com.fluxgram.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parsed contents of a plugin's manifest.json -- the metadata file that
 * sits next to a plugin's .lua script(s) and describes what the plugin is,
 * what version of the Flux API it targets, what it's allowed to touch, and
 * where to find it/update it.
 *
 * Required fields: Name, Version, Entrypoint. Everything else is optional
 * and falls back to a safe default so older or minimal manifests still load.
 *
 * Example manifest.json:
 * {
 *   "Name": "Ghost Mode",
 *   "Version": "1.0.0",
 *   "Author": "Yahimugly",
 *   "Description": "Hides your read receipts",
 *   "ApiVersion": 1,
 *   "Entrypoint": "main.lua",
 *   "Permissions": ["Ui", "Events", "Settings"],
 *   "Containers": ["ChatHeader"],
 *   "Homepage": "https://github.com/...",
 *   "Icon": "icon.png",
 *   "Tags": ["Privacy"],
 *   "UpdateUrl": "https://..."
 * }
 *
 * Icon accepts any image file placed next to the plugin's manifest --
 * .png, .jpg/.jpeg, or .ico are all valid, e.g. "Icon": "icon.png",
 * "Icon": "logo.ico", "Icon": "art.jpg". No format validation is done
 * here; it's just a relative file path/name as a string.
 */
public final class PluginManifest {

    private static final int DEFAULT_API_VERSION = 1;

    public final String name;
    public final String version;
    public final String author;
    public final String description;
    public final int apiVersion;
    public final String entrypoint;
    public final List<String> permissions;
    public final List<String> containers;
    public final String homepage;
    public final String icon;
    public final List<String> tags;
    public final String updateUrl;

    private PluginManifest(
        String name,
        String version,
        String author,
        String description,
        int apiVersion,
        String entrypoint,
        List<String> permissions,
        List<String> containers,
        String homepage,
        String icon,
        List<String> tags,
        String updateUrl
    ) {
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
        this.apiVersion = apiVersion;
        this.entrypoint = entrypoint;
        this.permissions = permissions;
        this.containers = containers;
        this.homepage = homepage;
        this.icon = icon;
        this.tags = tags;
        this.updateUrl = updateUrl;
    }

    /**
     * Parses and validates a manifest.json's contents.
     *
     * @param json the raw JSON text of manifest.json
     * @return a validated PluginManifest
     * @throws ManifestException if the JSON is malformed or a required
     *                           field (Name, Version, Entrypoint) is missing/blank
     */
    public static PluginManifest parse(String json) throws ManifestException {
        final JSONObject root;
        try {
            root = new JSONObject(json);
        } catch (JSONException e) {
            throw new ManifestException("manifest.json is not valid JSON: " + e.getMessage(), e);
        }

        String name = optNonBlankString(root, "Name");
        if (name == null) {
            throw new ManifestException("manifest.json is missing required field \"Name\"");
        }

        String version = optNonBlankString(root, "Version");
        if (version == null) {
            throw new ManifestException("manifest.json is missing required field \"Version\"");
        }

        String entrypoint = optNonBlankString(root, "Entrypoint");
        if (entrypoint == null) {
            throw new ManifestException("manifest.json is missing required field \"Entrypoint\"");
        }

        String author = root.optString("Author", "");
        String description = root.optString("Description", "");
        int apiVersion = root.optInt("ApiVersion", DEFAULT_API_VERSION);
        String homepage = root.optString("Homepage", "");
        String icon = root.optString("Icon", "");
        String updateUrl = root.optString("UpdateUrl", "");

        List<String> permissions = optStringList(root, "Permissions");
        List<String> containers = optStringList(root, "Containers");
        List<String> tags = optStringList(root, "Tags");

        return new PluginManifest(
            name, version, author, description, apiVersion, entrypoint,
            permissions, containers, homepage, icon, tags, updateUrl
        );
    }

    private static String optNonBlankString(JSONObject root, String key) {
        String value = root.optString(key, null);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value;
    }

    private static List<String> optStringList(JSONObject root, String key) {
        JSONArray array = root.optJSONArray(key);
        if (array == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i, null);
            if (value != null) {
                result.add(value);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** Whether this manifest declares the given permission (case-sensitive, matches the Permissions list). */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /** Thrown when a manifest.json is malformed or missing a required field. */
    public static final class ManifestException extends Exception {
        public ManifestException(String message) {
            super(message);
        }
        public ManifestException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
