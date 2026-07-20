package com.caint.api;

import android.util.Log;
import android.view.Gravity;

import org.telegram.messenger.ApplicationLoader;

/**
 * Caint.UI
 *
 * Creates real, native Telegram-backed UI elements without exposing
 * Telegram's internal view classes -- callers only ever see CaintComponent.
 *
 * Two ways to place a created component:
 *  - component.attach() -- quick, fire-and-forget corner overlay.
 *  - Caint.UI.ChatHeader / ChatFooter / ChatMenu / MessageMenu / Sidebar /
 *    BottomBar / Settings -- named containers for a specific native
 *    location. Each falls back to the same overlay placement until a real
 *    screen calls bindHost(...) on it with its actual header/footer/sidebar
 *    view, at which point components move to true native placement.
 *
 * createDialog is the one exception to "created detached", since a dialog
 * is shown immediately by nature.
 */
public final class CaintUI {

    private static final String TAG = "Caint.UI";

    // Named containers, one per native Telegram UI location. Real screens
    // opt into true native placement later via bindHost(); until then these
    // just work via the generic overlay fallback baked into
    // AbstractCaintContainer.
    public final CaintContainer ChatHeader = new AbstractCaintContainer(ContainerType.CHAT_HEADER, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
    public final CaintContainer ChatFooter = new AbstractCaintContainer(ContainerType.CHAT_FOOTER, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    public final CaintContainer ChatMenu = new AbstractCaintContainer(ContainerType.CHAT_MENU, Gravity.TOP | Gravity.END);
    public final CaintContainer MessageMenu = new AbstractCaintContainer(ContainerType.MESSAGE_MENU, Gravity.CENTER);
    public final CaintContainer Sidebar = new AbstractCaintContainer(ContainerType.SIDEBAR, Gravity.START | Gravity.CENTER_VERTICAL);
    public final CaintContainer BottomBar = new AbstractCaintContainer(ContainerType.BOTTOM_BAR, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    public final CaintContainer Settings = new AbstractCaintContainer(ContainerType.SETTINGS, Gravity.CENTER);

    private final CaintContainer[] allContainers = {
            ChatHeader, ChatFooter, ChatMenu, MessageMenu, Sidebar, BottomBar, Settings
    };

    CaintUI() {
    }

    /**
     * Looks up a container by its ContainerType, for generic/plugin code
     * that doesn't know the concrete field name.
     */
    public CaintContainer getContainer(ContainerType type) {
        for (CaintContainer container : allContainers) {
            if (container.getType() == type) {
                return container;
            }
        }
        throw new IllegalArgumentException("Unknown Caint.UI container type: " + type);
    }

    /** Removes every component from every container. */
    public void clearAllContainers() {
        for (CaintContainer container : allContainers) {
            container.clear();
        }
    }

    /**
     * Creates a native-styled button.
     *
     * @param text the button's label
     * @return a CaintComponent wrapping the created button
     */
    public CaintComponent createButton(String text) {
        return new CaintButton(ApplicationLoader.applicationContext, text);
    }

    /**
     * Creates a native Telegram menu item (the same row style used in
     * Telegram's own popup/context menus).
     *
     * @param text     the menu item's label
     * @param iconResId an Android drawable resource id, or 0 for no icon
     * @return a CaintComponent wrapping the created menu item
     */
    public CaintComponent createMenuItem(String text, int iconResId) {
        return new CaintMenuItem(ApplicationLoader.applicationContext, text, iconResId);
    }

    /**
     * Creates and immediately shows a native Telegram alert dialog with a
     * single "OK" action.
     *
     * @param title   dialog title
     * @param content dialog body content
     * @return a CaintComponent wrapping the created dialog
     */
    public CaintComponent createDialog(String title, String content) {
        return new CaintDialog(ApplicationLoader.applicationContext, title, content);
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
