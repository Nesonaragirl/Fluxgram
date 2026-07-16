/**
 * SidebarScreen — the navigation drawer / sidebar.
 * Items in the sidebar slot are rendered as menu rows.
 */
import clsx from 'clsx'
import { useLayoutStore } from '../../store/layoutStore'
import { Slot } from '../layout/Slot'
import { SLOT_IDS, registry } from '../../store/componentRegistry'

// Mock chat list
const CHATS = [
  { id: 1, name: 'Alice Chen', preview: 'okay seriously impressive', time: '10:28', unread: 0, avatar: '🟣' },
  { id: 2, name: 'FluxGram Updates', preview: 'v2.4 is live — layout editor is here', time: '9:15', unread: 3, avatar: '⚡' },
  { id: 3, name: 'Dev Team', preview: 'the plugin API docs are up', time: 'Yesterday', unread: 12, avatar: '👥' },
  { id: 4, name: 'Bob Martinez', preview: 'did you try the translator plugin?', time: 'Yesterday', unread: 0, avatar: '🔵' },
  { id: 5, name: 'Design Sync', preview: 'Figma link updated', time: 'Mon', unread: 0, avatar: '🎨' },
  { id: 6, name: 'Saved Messages', preview: 'Layout JSON backup', time: 'Sun', unread: 0, avatar: '🔖' },
]

export function SidebarScreen() {
  const editMode = useLayoutStore(s => s.editMode)
  const setActiveScreen = useLayoutStore(s => s.setActiveScreen)

  return (
    <div className="flex h-full bg-flux-bg">
      {/* ── Navigation drawer ─────────────────────────────────────────────── */}
      <div className={clsx(
        'w-64 flex flex-col border-r border-flux-border bg-flux-surface flex-shrink-0',
        editMode && 'edit-outline-soft',
      )}>
        {/* User header */}
        <div className="flex items-center gap-3 p-4 border-b border-flux-border">
          <div className="w-10 h-10 rounded-full bg-flux-accent flex items-center justify-center text-xl">
            ⚡
          </div>
          <div>
            <p className="text-sm font-semibold text-flux-text">You</p>
            <p className="text-xs text-flux-muted">@fluxgram_user</p>
          </div>
        </div>

        {/* Sidebar slot items */}
        {editMode && (
          <p className="text-[9px] text-flux-edit uppercase tracking-widest font-semibold px-3 pt-2 opacity-70">
            Sidebar Slot
          </p>
        )}
        <div className="px-2 py-1">
          <SidebarSlotItems editMode={editMode} />
        </div>

        <div className="border-t border-flux-border" />

        {/* Spacer */}
        <div className="flex-1" />

        {/* Footer version */}
        <div className="p-3 text-center">
          <p className="text-[10px] text-flux-muted">FluxGram v2.4.0</p>
        </div>
      </div>

      {/* ── Chat list ────────────────────────────────────────────────────── */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <div className="px-3 py-3 border-b border-flux-border">
          <h2 className="text-base font-bold text-flux-text">Chats</h2>
        </div>
        <div className="flex-1 overflow-y-auto">
          {CHATS.map(chat => (
            <button
              key={chat.id}
              onClick={() => setActiveScreen('chat')}
              className="w-full flex items-center gap-3 px-3 py-3 hover:bg-flux-surface transition-colors text-left"
            >
              <div className="w-10 h-10 rounded-full bg-flux-panel flex items-center justify-center text-xl flex-shrink-0">
                {chat.avatar}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-flux-text truncate">{chat.name}</span>
                  <span className="text-[11px] text-flux-muted flex-shrink-0 ml-2">{chat.time}</span>
                </div>
                <div className="flex items-center justify-between mt-0.5">
                  <span className="text-[12px] text-flux-muted truncate">{chat.preview}</span>
                  {chat.unread > 0 && (
                    <span className="ml-2 flex-shrink-0 bg-flux-accent text-white text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
                      {chat.unread}
                    </span>
                  )}
                </div>
              </div>
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}

// Renders sidebar slot items as menu rows
function SidebarSlotItems({ editMode }) {
  const layout = useLayoutStore(s => s.layout)

  if (editMode) {
    return (
      <Slot
        slotId={SLOT_IDS.SIDEBAR_MAIN}
        className="flex-col gap-0.5"
        showLabel={false}
      />
    )
  }

  const instances = layout.slots[SLOT_IDS.SIDEBAR_MAIN] ?? []
  return (
    <div className="flex flex-col gap-0.5 py-1">
      {instances.filter(i => i.visible).map(inst => {
        const def = registry.get(inst.componentId)
        if (!def) return null
        const Icon = def.icon
        return (
          <button
            key={inst.instanceId}
            className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-flux-panel transition-colors text-left w-full"
          >
            <Icon size={16} style={{ color: def.color ?? '#7d8e9e' }} />
            <span className="text-sm text-flux-text">{def.name}</span>
            {def.plugin && (
              <span className="ml-auto w-1.5 h-1.5 rounded-full bg-purple-400" />
            )}
          </button>
        )
      })}
    </div>
  )
}
