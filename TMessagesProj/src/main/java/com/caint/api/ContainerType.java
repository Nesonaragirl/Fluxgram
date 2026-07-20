package com.caint.api;

/**
 * Every native Telegram UI location a Caint.UI container can represent.
 * Adding a new location later is just one more enum value plus one more
 * field in Caint.UI -- existing containers/callers are unaffected.
 */
public enum ContainerType {
    CHAT_HEADER,
    CHAT_FOOTER,
    CHAT_MENU,
    MESSAGE_MENU,
    SIDEBAR,
    BOTTOM_BAR,
    SETTINGS
}
