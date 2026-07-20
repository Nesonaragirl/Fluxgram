package com.fluxgram.api;

import com.fluxgram.plugin.PluginManager;

import org.telegram.messenger.ApplicationLoader;

/**
 * Flux
 *
 * Public entry point for the Flux API wrapper.
 *
 * Flux exposes a stable, modular surface for interacting with the client
 * without leaking Telegram's internal classes to consumers. Each module
 * below (UI, Events, Settings, Dialogs, Utils) is a self-contained namespace
 * that can evolve independently.
 *
 * Still no sandboxing (permission enforcement) -- that comes later. This
 * stage wires Events, UI, and plugin loading up to real behavior while
 * keeping the public API unchanged.
 *
 * Usage:
 *   Flux.UI.createButton("Tap me").onClick(data -> ...).attach();
 *   Flux.Events.on("messagesReceived", data -> ...);
 *   Flux.Settings.get("key");
 *   Flux.Dialogs.alert("Title", "Text");
 */
public final class Flux {

    public static final FluxUI UI = new FluxUI();
    public static final FluxEvents Events = new FluxEvents();
    public static final FluxSettings Settings = new FluxSettings();
    public static final FluxDialogs Dialogs = new FluxDialogs();
    public static final FluxUtils Utils = new FluxUtils();

    private static boolean initialized;

    private Flux() {
        // Flux is a static entry point and is never instantiated.
    }

    /**
     * Wires Flux into the running Telegram client -- attaches the
     * NotificationCenter -> Flux.Events bridge, starts tracking the
     * foreground Activity so Flux.UI components can attach() themselves,
     * and loads every plugin found under PluginManager.getPluginsRoot().
     * Safe to call more than once; only does real work the first time.
     *
     * Should be called once core services are up, e.g. from
     * ApplicationLoader.postInitApplication().
     */
    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        FluxNotificationBridge.attach();
        FluxActivityTracker.attach(ApplicationLoader.applicationLoaderInstance);
        PluginManager.loadAll();
    }
}
