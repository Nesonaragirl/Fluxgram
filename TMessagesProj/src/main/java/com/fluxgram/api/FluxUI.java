package com.fluxgram.api;

import android.util.Log;

/**
 * Flux.UI
 *
 * Surface for creating custom UI elements (buttons, menu items, dialogs,
 * settings pages) without exposing Telegram's internal UI classes to
 * consumers of the Flux API.
 *
 * All methods are placeholders for now.
 */
public final class FluxUI {

    private static final String TAG = "Flux.UI";

    FluxUI() {
    }

    /**
     * Creates a custom button.
     *
     * @param id      unique identifier for the button
     * @param label   text displayed on the button
     * @param onClick callback invoked when the button is pressed
     * @return a handle representing the created button (placeholder)
     */
    public Object createButton(String id, String label, FluxCallback onClick) {
        Log.d(TAG, "createButton: Not implemented yet.");
        return null;
    }

    /**
     * Creates a custom menu item.
     *
     * @param id      unique identifier for the menu item
     * @param label   text displayed for the menu item
     * @param onClick callback invoked when the item is selected
     * @return a handle representing the created menu item (placeholder)
     */
    public Object createMenuItem(String id, String label, FluxCallback onClick) {
        Log.d(TAG, "createMenuItem: Not implemented yet.");
        return null;
    }

    /**
     * Creates a custom dialog.
     *
     * @param title   dialog title
     * @param content dialog body content
     * @return a handle representing the created dialog (placeholder)
     */
    public Object createDialog(String title, String content) {
        Log.d(TAG, "createDialog: Not implemented yet.");
        return null;
    }

    /**
     * Creates a custom settings page.
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
