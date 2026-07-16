# FluxGram

A fully customizable Telegram-like chat interface system with a visual drag-and-drop layout editor. Built as a React web app on top of an imported Telegram Android source repo.

## What it is

FluxGram lets users visually rearrange, hide, resize, and pin UI components across a Telegram-like interface — no code required. The design is inspired by Android launchers, OBS Studio panel layouts, and VS Code workspaces.

## How to run

The workflow **"Start application"** runs automatically:

```
cd fluxgram && node ../node_modules/.bin/vite --port 5000 --host 0.0.0.0
```

Node packages are installed in the **workspace root** `node_modules/` (not in `fluxgram/`). Vite resolves them via Node's upward module search.

## Tech stack

| Layer | Package | Version |
|-------|---------|---------|
| UI framework | React | 19 |
| Build tool | Vite | 8 |
| Styling | Tailwind CSS (via `@tailwindcss/vite`) | 4 |
| Drag & drop | @dnd-kit/core + sortable | 6 / 10 |
| State management | Zustand + immer middleware | 5 |
| Icons | lucide-react | 1 |

## Project structure

```
fluxgram/
  src/
    store/
      componentRegistry.js   # Plugin registry + all slot metadata
      layoutStore.js         # Zustand store — layout state, persistence, import/export
    plugins/
      samplePlugins.js       # 4 demo plugins (AI Summary, Translator, Downloads, Quick Actions)
    components/
      layout/
        Slot.jsx             # Droppable slot container
        SlotItem.jsx         # Draggable component instance with edit controls
      editor/
        DndController.jsx    # DnD context, cross-slot drop logic
        EditModeBar.jsx      # Edit mode banner + JSON import/export drawer
        ComponentPalette.jsx # Add-component modal (filtered by slot compatibility)
        PluginShowcase.jsx   # First-run plugin demo panel
      screens/
        ChatScreen.jsx       # Chat view (TopBar + messages + ChatInput slots)
        ProfileScreen.jsx    # Profile view (Avatar/Actions/Stats/Bio/Media slots)
        SidebarScreen.jsx    # Sidebar + chat list (Sidebar.Main slot)
        BottomNav.jsx        # Bottom navigation (BottomBar.Slot1–5)
      ui/                    # Button, Badge, Tooltip primitives
    App.jsx                  # Root shell, screen routing, info card
    main.jsx                 # Entry point — registers sample plugins before mount
```

## Slot system

Every customizable region exposes named slots. Components are assigned to slots; the layout stays responsive.

| Slot Group | Slots |
|------------|-------|
| TopBar | Left · Center · Right |
| BottomBar | Slot1 · Slot2 · Slot3 · Slot4 · Slot5 |
| Sidebar | Main |
| ChatInput | Left · Center · Right |
| ProfileScreen | Avatar · Actions · Stats · Bio · Media |

## Layout JSON schema

The full layout state is stored as a single JSON object (persisted to `localStorage` under key `fluxgram:layout:v1`). This is sync-ready:

```jsonc
{
  "version": "1.0",
  "schemaVersion": 1,
  "deviceId": "...",
  "lastModified": 1234567890,
  "slots": {
    "TopBar.Left": [
      { "instanceId": "...", "componentId": "menu-button", "visible": true, "pinned": false, "size": "sm" }
    ],
    "ChatInput.Center": [
      { "instanceId": "...", "componentId": "message-input", "visible": true, "pinned": true, "size": "xl" }
    ]
  }
}
```

Export via the JSON button in Edit Mode. Import by pasting JSON back.

## Plugin API

Plugins call `registry.register(def)` with a component definition. The registry is the source of truth for what components *exist*; the layout store controls *where* they appear. Users decide placement via Edit Mode.

```js
import { registry } from 'fluxgram/src/store/componentRegistry'

registry.register({
  id: 'my-plugin-btn',
  name: 'My Feature',
  icon: MyIcon,
  plugin: true,
  pluginMeta: { author: 'You', version: '1.0.0' },
  compatibleSlots: ['TopBar.Left', 'ChatInput.Right'],
  sizes: ['sm', 'md'],
  defaultSize: 'sm',
  color: '#a855f7',
})
```

Four sample plugins are pre-registered in `src/plugins/samplePlugins.js`:
- **AI Summary** — FluxAI Labs
- **Translator** — LinguaFlex  
- **Download Manager** — FluxUtils
- **Quick Actions** — SpeedDesk

## User preferences

- Keep the slot-based layout system intact; do not switch to free-form absolute positioning
- The JSON schema is the single source of truth for layout state
- Plugins must not control their own placement — only the user does
