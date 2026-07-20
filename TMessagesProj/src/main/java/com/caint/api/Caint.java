package com.caint.api;

import com.caint.plugin.PluginManager;

import org.telegram.messenger.ApplicationLoader;

/**
 * Caint
 *
 * Public entry point for the Caint API wrapper.
 *
 * Caint exposes a stable, modular surface for interacting with the client
 * without leaking Telegram's internal classes to consumers. Each module
 * below (UI, Events, Settings, Dialogs, Utils) is a self-contained namespace
 * that can evolve independently.
 *
 * Still no sandboxing (permission enforcement) -- that comes later. This
 * stage wires Events, UI, and plugin loading up to real behavior while
 * keeping the public API unchanged.
 *
 * Usage:
 *   Caint.UI.createButton("Tap me").onClick(data -> ...).attach();
 *   Caint.Events.on("messagesReceived", data -> ...);
 *   Caint.Settings.get("key");
 *   Caint.Dialogs.alert("Title", "Text");
 */
public final class Caint {

    public static final CaintUI UI = new CaintUI();
    public static final CaintEvents Events = new CaintEvents();
    public static final CaintSettings Settings = new CaintSettings();
    public static final CaintDialogs Dialogs = new CaintDialogs();
    public static final CaintUtils Utils = new CaintUtils();

    private static boolean initialized;

    private Caint() {
        // Caint is a static entry point and is never instantiated.
    }

    /**
     * Wires Caint into the running Telegram client -- attaches the
     * NotificationCenter -> Caint.Events bridge, starts tracking the
     * foreground Activity so Caint.UI components can attach() themselves,
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
        CaintNotificationBridge.attach();
        CaintActivityTracker.attach(ApplicationLoader.applicationLoaderInstance);
        PluginManager.loadAll();
    }
}
