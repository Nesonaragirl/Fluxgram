package com.fluxgram.plugin;

import org.luaj.vm2.Globals;

/**
 * A single loaded plugin: its script file plus its own, unshared Lua state.
 *
 * Deliberately just a data holder for now -- lifecycle hooks
 * (onEnable/onDisable), a manifest (name/version/permissions), and per-plugin
 * component tracking are follow-up work once the loading mechanism itself
 * is solid.
 */
public final class FluxPlugin {

    public final String id;
    public final String fileName;

    final Globals globals;

    FluxPlugin(String id, String fileName, Globals globals) {
        this.id = id;
        this.fileName = fileName;
        this.globals = globals;
    }
}
