package com.fluxgram.api;

import android.util.Log;

import org.telegram.messenger.ApplicationLoader;

/**
 * Flux.UI
 *
 * Creates real, native Telegram-backed UI elements without exposing
 * Telegram's internal view classes -- callers only ever see FluxComponent.
 *
 * Components are created detached (not yet attached to any screen); a
 * future attach/container API will handle placement. createDialog is the
 * one exception, since a dialog is shown immediately by nature.
 */
public final class FluxUI {

    private static final String TAG = "Flux.UI";

    FluxUI() {
    }

    /**
     * Creates a native-styled button.
     *
     * @param text the button's label
     * @return a FluxComponent wrapping the created button
     */
    public FluxComponent createButton(String text) {
        return new FluxButton(ApplicationLoader.applicationContext, text);
    }

    /**
     * Creates a native Telegram menu item (the same row style used in
     * Telegram's own popup/context menus).
     *
     * @param text     the menu item's label
     * @param iconResId an Android drawable resource id, or 0 for no icon
     * @return a FluxComponent wrapping the created menu item
     */
    public FluxComponent createMenuItem(String text, int iconResId) {
        return new FluxMenuItem(ApplicationLoader.applicationContext, text, iconResId);
    }

    /**
     * Creates and immediately shows a native Telegram alert dialog with a
     * single "OK" action.
     *
     * @param title   dialog title
     * @param content dialog body content
     * @return a FluxComponent wrapping the created dialog
     */
    public FluxComponent createDialog(String title, String content) {
        return new FluxDialog(ApplicationLoader.applicationContext, title, content);
    }

    /**
     * Creates a custom settings page.
     *
     * Still a placeholder -- settings pages need a host screen/navigation
     * concept that hasn't been built yet.
     *
     * @param id    unique identifier for the settings page
     * @param title title displayed at the top of the page
     * @return a handle representing the created settings page (placeholder)
     */
    public Object createSettingsPage(String id, String title) {
        Log.d(TAG, "createSettingsPage: Not implemented yet.");
        return null;
    }
}
