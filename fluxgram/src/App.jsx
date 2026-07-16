/**
 * FluxGram — root application shell
 * Composes screen routing, edit mode, slot-based layout, and DnD.
 */
import { useState } from 'react'
import clsx from 'clsx'
import { useLayoutStore } from './store/layoutStore'
import { DndController } from './components/editor/DndController'
import { EditModeBar, EditToggleButton } from './components/editor/EditModeBar'
import { ComponentPalette } from './components/editor/ComponentPalette'
import { PluginShowcase } from './components/editor/PluginShowcase'
import { ChatScreen } from './components/screens/ChatScreen'
import { ProfileScreen } from './components/screens/ProfileScreen'
import { SidebarScreen } from './components/screens/SidebarScreen'
import { BottomNav } from './components/screens/BottomNav'

export default function App() {
  const editMode = useLayoutStore(s => s.editMode)
  const activeScreen = useLayoutStore(s => s.activeScreen)
  const deselect = useLayoutStore(s => s.deselect)
  const [showPluginInfo, setShowPluginInfo] = useState(true)

  return (
    <DndController>
      {/* ── Outer frame: simulates a phone/app viewport ──────────────────── */}
      <div className="h-full flex items-center justify-center bg-[#0d1520] p-4">
        <div
          className={clsx(
            'relative flex flex-col overflow-hidden shadow-2xl transition-all duration-200',
            'w-full max-w-sm h-full max-h-[780px]',
            'rounded-2xl border',
            editMode ? 'border-flux-edit/60 shadow-flux-edit/20' : 'border-flux-border',
          )}
          onClick={editMode ? e => { e.stopPropagation(); deselect() } : undefined}
        >
          {/* Edit mode top banner */}
          <EditModeBar />

          {/* Screen router */}
          <div className="flex-1 overflow-hidden">
            {activeScreen === 'chat'    && <ChatScreen />}
            {activeScreen === 'profile' && <ProfileScreen />}
            {activeScreen === 'sidebar' && <SidebarScreen />}
          </div>

          {/* Bottom navigation bar */}
          <BottomNav />

          {/* Screen switcher tabs (always visible, top-right corner) */}
          <ScreenSwitcher />

          {/* Plugin showcase panel (first-run info) */}
          {showPluginInfo && !editMode && (
            <PluginShowcase />
          )}
        </div>

        {/* Edit Layout floating button — lives outside the phone frame */}
        <EditToggleButton />

        {/* Info card outside the frame */}
        <aside className="hidden lg:flex flex-col gap-4 w-64 ml-6 self-center">
          <InfoCard />
        </aside>
      </div>

      {/* Palette modal (portal-style, full screen overlay) */}
      <ComponentPalette />
    </DndController>
  )
}

// ── Screen Switcher ────────────────────────────────────────────────────────────
function ScreenSwitcher() {
  const activeScreen = useLayoutStore(s => s.activeScreen)
  const setActiveScreen = useLayoutStore(s => s.setActiveScreen)

  const screens = [
    { id: 'chat',    label: 'Chat' },
    { id: 'profile', label: 'Profile' },
    { id: 'sidebar', label: 'Sidebar' },
  ]

  return (
    <div className="absolute top-2 right-2 flex gap-1 z-20 pointer-events-auto">
      {screens.map(s => (
        <button
          key={s.id}
          onClick={e => { e.stopPropagation(); setActiveScreen(s.id) }}
          className={clsx(
            'text-[10px] font-semibold px-2 py-0.5 rounded-full transition-all',
            activeScreen === s.id
              ? 'bg-flux-accent text-white'
              : 'bg-flux-panel/80 text-flux-muted hover:text-flux-text backdrop-blur-sm',
          )}
        >
          {s.label}
        </button>
      ))}
    </div>
  )
}

// ── Info Card (desktop sidebar) ────────────────────────────────────────────────
function InfoCard() {
  return (
    <div className="bg-flux-surface border border-flux-border rounded-xl p-4 space-y-3 text-sm">
      <div>
        <h1 className="text-base font-bold text-flux-text flex items-center gap-2">
          <span>⚡</span> FluxGram
        </h1>
        <p className="text-xs text-flux-muted mt-0.5">Visual layout editor for a Telegram-like UI</p>
      </div>

      <div className="space-y-2 text-xs">
        <Step n="1" text='Click "Edit Layout" to enter Edit Mode' />
        <Step n="2" text="Drag components between highlighted slots" />
        <Step n="3" text="Click any component to resize, hide, pin, or remove it" />
        <Step n="4" text='Click "+" in any slot to add built-in or plugin components' />
        <Step n="5" text="Switch between Chat / Profile / Sidebar screens to customize each" />
        <Step n="6" text='Use JSON export to back up or sync your layout across devices' />
      </div>

      <div className="border-t border-flux-border pt-2">
        <p className="text-[11px] text-flux-muted font-semibold uppercase tracking-widest mb-1.5">Slot Groups</p>
        <div className="space-y-0.5">
          {[
            ['TopBar', 'Left · Center · Right'],
            ['BottomBar', 'Slots 1–5'],
            ['ChatInput', 'Left · Center · Right'],
            ['Sidebar', 'Main'],
            ['Profile', 'Avatar · Actions · Stats · Bio · Media'],
          ].map(([group, slots]) => (
            <div key={group} className="flex gap-1.5 text-[11px]">
              <span className="text-flux-accent font-medium w-20 flex-shrink-0">{group}</span>
              <span className="text-flux-muted">{slots}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="border-t border-flux-border pt-2">
        <p className="text-[11px] text-flux-muted">
          Layout auto-saved to localStorage. JSON schema is sync-ready for multi-device support.
        </p>
      </div>
    </div>
  )
}

function Step({ n, text }) {
  return (
    <div className="flex gap-2 items-start">
      <span className="w-4 h-4 rounded-full bg-flux-accent/20 text-flux-accent text-[10px] font-bold flex items-center justify-center flex-shrink-0 mt-0.5">
        {n}
      </span>
      <span className="text-flux-muted leading-tight">{text}</span>
    </div>
  )
}
