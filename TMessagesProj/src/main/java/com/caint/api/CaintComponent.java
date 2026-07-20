package com.caint.api;

/**
 * Common surface implemented by every UI element Caint.UI creates (buttons,
 * menu items, dialogs, and future component types). This is what keeps
 * Caint.UI's output uniform regardless of which native Telegram view backs a
 * given component underneath.
 *
 * Implementations wrap a real Android/Telegram view internally but never
 * expose it through this interface -- callers only ever see CaintComponent.
 *
 * Every component looks like a native Telegram element by default (it's
 * styled from Telegram's own Theme colors, and follows the user's chosen
 * Telegram theme). setBackgroundColor()/setTextColor() let a plugin opt
 * into a literal, fixed color instead -- passing null clears the override
 * and reverts to Telegram's own themed look.
 */
public interface CaintComponent {

    /**
     * Updates the component's primary text (button label, menu item text,
     * dialog message, etc).
     */
    CaintComponent setText(String text);

    /**
     * Sets the component's icon from an Android drawable resource id.
     */
    CaintComponent setIcon(int iconResId);

    /**
     * Overrides the component's background color (an ARGB int, e.g. from
     * android.graphics.Color). Pass null to clear the override and go back
     * to Telegram's own themed default -- which is what every component
     * uses out of the box, before this is ever called.
     */
    CaintComponent setBackgroundColor(Integer color);

    /**
     * Overrides the component's text/icon color (an ARGB int, e.g. from
     * android.graphics.Color). Pass null to clear the override and go back
     * to Telegram's own themed default -- which is what every component
     * uses out of the box, before this is ever called.
     */
    CaintComponent setTextColor(Integer color);

    /**
     * Shows or hides the component.
     */
    CaintComponent setVisible(boolean visible);

    /**
     * Enables or disables interaction with the component.
     */
    CaintComponent setEnabled(boolean enabled);

    /**
     * Registers a callback fired when the component is activated (tapped,
     * confirmed, etc, depending on the component type).
     */
    CaintComponent onClick(CaintCallback callback);

    /**
     * Attaches the component to the current foreground screen so it's
     * actually visible, using whatever placement makes sense for its type
     * (e.g. a corner overlay for buttons/menu items). Dialogs are already
     * shown as soon as they're created, so this is a no-op for them.
     *
     * No-ops (and logs) if there's currently no foreground Activity to
     * attach to.
     */
    CaintComponent attach();

    /**
     * Permanently removes the component from wherever it's attached
     * (detaches its view from its parent, dismisses it if it's a dialog).
     */
    void remove();
}
