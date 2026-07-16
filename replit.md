# Telegram Android — FluxGram Edit Mode

Official Telegram Android source with a FluxGram Edit Mode layer added on top. The existing Telegram UI is untouched; the edit mode is a transparent overlay injected above it.

## How to compile

This is a native Android project. It **cannot be compiled or run on Replit** — you need Android Studio locally:

1. Open the project in **Android Studio 3.4+**
2. Install **Android NDK rev. 20** and **Android SDK 8.1**
3. Fill in your credentials in `TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java`
4. Copy your `release.keystore` → `TMessagesProj/config/`
5. Copy your `google-services.json` → `TMessagesProj/`
6. Build and run on a device or emulator

## FluxGram Edit Mode — what was added

Four new Java files in `TMessagesProj/src/main/java/org/telegram/ui/Components/EditMode/`:

| File | Purpose |
|------|---------|
| `LayoutConfig.java` | JSON schema + persistence. Stores layout state in `SharedPreferences` under `fluxgram_layout`. Sync-ready (has `deviceId` + `lastModified`). |
| `EditModeManager.java` | Singleton that owns layout state and fires callbacks to all listeners when edit mode toggles or layout changes. |
| `EditModeOverlayView.java` | Full-screen `FrameLayout` overlaid above the real UI. Draws translucent slot regions, draggable component chips, per-chip controls (show/hide, pin, remove). |
| `EditModeButton.java` | Floating `View` in the bottom-right corner. Tap → toggle edit mode. Long-press → shows layout JSON length (hook for copy/share). |

One minimal injection in `LaunchActivity.java` (~10 lines, around line 558):
```java
EditModeOverlayView editModeOverlay = new EditModeOverlayView(this);
frameLayout.addView(editModeOverlay,
    LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
EditModeButton.attachTo(frameLayout);
```

## Slot system

| Slot ID | Region |
|---------|--------|
| `TopBar.Left` | Action bar — left side |
| `TopBar.Center` | Action bar — center |
| `TopBar.Right` | Action bar — right side (search, call, video, more) |
| `BottomBar.Slot1–5` | Bottom tab bar slots |
| `ChatInput.Left` | Chat input row — left (emoji, attach) |
| `ChatInput.Center` | Chat input row — center |
| `ChatInput.Right` | Chat input row — right (camera, voice, send) |
| `Sidebar.Main` | Navigation drawer items |

## Layout JSON schema

Stored in `SharedPreferences("fluxgram_layout")` — serialize and POST to a backend for multi-device sync:

```json
{
  "version": "1.0",
  "schemaVersion": 1,
  "deviceId": "abc123",
  "lastModified": 1721123456789,
  "slots": {
    "TopBar.Right": [
      { "instanceId": "a1b2c3", "componentId": "search-button", "visible": true, "pinned": false, "order": 0 },
      { "instanceId": "d4e5f6", "componentId": "call-button",   "visible": true, "pinned": false, "order": 1 }
    ]
  }
}
```

## Slot rect registration (wiring the overlay to real views)

The overlay needs to know where each UI region sits on screen. After views have laid out, call:

```java
// In a fragment's onViewCreated / onGlobalLayout:
EditModeOverlayView overlay = ...; // retrieve from LaunchActivity
overlay.setSlotView(LayoutConfig.SLOT_TOPBAR_RIGHT, myActionBarRightGroup);
overlay.setSlotView(LayoutConfig.SLOT_BOTTOMBAR_1,  tabs[0]);
// etc.
```

This maps real view bounds → overlay slot rects so chip positions stay accurate.
