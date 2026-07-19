package com.fluxgram.api;

import android.util.Log;
import android.view.Gravity;

import org.telegram.messenger.ApplicationLoader;

/**
 * Flux.UI
 *
 * Creates real, native Telegram-backed UI elements without exposing
 * Telegram's internal view classes -- callers only ever see FluxComponent.
 *
 * Two ways to place a created component:
 *  - component.attach() -- quick, fire-and-forget corner overlay.
 *  - Flux.UI.ChatHeader / ChatFooter / ChatMenu / MessageMenu / Sidebar /
 *    BottomBar / Settings -- named containers for a specific native
 *    location. Each falls back to the same overlay placement until a real
 *    screen calls bindHost(...) on it with its actual header/footer/sidebar
 *    view, at which point components move to true native placement.
 *
 * createDialog is the one exception to "created detached", since a dialog
 * is shown immediately by nature.
 */
public final class FluxUI {

    private static final String TAG = "Flux.UI";

    // Named containers, one per native Telegram UI location. Real screens
    // opt into true native placement later via bindHost(); until then these
    // just work via the generic overlay fallback baked into
    // AbstractFluxContainer.
    public final FluxContainer ChatHeader = new AbstractFluxContainer(ContainerType.CHAT_HEADER, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
    public final FluxContainer ChatFooter = new AbstractFluxContainer(ContainerType.CHAT_FOOTER, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    public final FluxContainer ChatMenu = new AbstractFluxContainer(ContainerType.CHAT_MENU, Gravity.TOP | Gravity.END);
    public final FluxContainer MessageMenu = new AbstractFluxContainer(ContainerType.MESSAGE_MENU, Gravity.CENTER);
    public final FluxContainer Sidebar = new AbstractFluxContainer(ContainerType.SIDEBAR, Gravity.START | Gravity.CENTER_VERTICAL);
    public final FluxContainer BottomBar = new AbstractFluxContainer(ContainerType.BOTTOM_BAR, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    public final FluxContainer Settings = new AbstractFluxContainer(ContainerType.SETTINGS, Gravity.CENTER);

    private final FluxContainer[] allContainers = {
            ChatHeader, ChatFooter, ChatMenu, MessageMenu, Sidebar, BottomBar, Settings
    };

    FluxUI() {
    }

    /**
     * Looks up a container by its ContainerType, for generic/plugin code
     * that doesn't know the concrete field name.
     */
    public FluxContainer getContainer(ContainerType type) {
        for (FluxContainer container : allContainers) {
            if (container.getType() == type) {
                return container;
            }
        }
        throw new IllegalArgumentException("Unknown Flux.UI container type: " + type);
    }

    /** Removes every component from every container. */
    public void clearAllContainers() {
        for (FluxContainer container : allContainers) {
            container.clear();
        }
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
