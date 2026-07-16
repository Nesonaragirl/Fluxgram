/**
 * BottomNav — the bottom navigation bar.
 * Each of the 5 slots maps to one tab position.
 * In normal mode: renders as interactive nav tabs.
 * In edit mode: shows the slot system with drag handles.
 */
import clsx from 'clsx'
import { useLayoutStore } from '../../store/layoutStore'
import { Slot } from '../layout/Slot'
import { registry } from '../../store/componentRegistry'
import { SLOT_IDS } from '../../store/componentRegistry'

const BOTTOM_SLOTS = [
  SLOT_IDS.BOTTOMBAR_1,
  SLOT_IDS.BOTTOMBAR_2,
  SLOT_IDS.BOTTOMBAR_3,
  SLOT_IDS.BOTTOMBAR_4,
  SLOT_IDS.BOTTOMBAR_5,
]

export function BottomNav() {
  const editMode = useLayoutStore(s => s.editMode)
  const activeScreen = useLayoutStore(s => s.activeScreen)
  const setActiveScreen = useLayoutStore(s => s.setActiveScreen)
  const layout = useLayoutStore(s => s.layout)

  return (
    <div className={clsx(
      'flex items-center border-t border-flux-border bg-flux-surface flex-shrink-0',
      editMode && 'edit-outline-soft py-1 gap-1 px-2',
      !editMode && 'h-14',
    )}>
      {editMode && (
        <span className="text-[9px] text-flux-edit uppercase tracking-widest font-semibold flex-shrink-0 mr-1">
          Bottom Bar
        </span>
      )}

      {BOTTOM_SLOTS.map((slotId, idx) => {
        if (editMode) {
          return (
            <div key={slotId} className="flex-1 flex flex-col items-center min-w-0">
              <span className="text-[8px] text-flux-muted mb-0.5 font-mono">Slot {idx + 1}</span>
              <Slot slotId={slotId} className="justify-center" />
            </div>
          )
        }

        // Normal mode — render as nav tabs
        const instances = (layout.slots[slotId] ?? []).filter(i => i.visible)
        if (!instances.length) return (
          <div key={slotId} className="flex-1" />
        )

        const inst = instances[0]
        const def = registry.get(inst.componentId)
        if (!def) return <div key={slotId} className="flex-1" />

        const Icon = def.icon
        const screenMap = {
          'nav-chats': 'chat',
          'nav-contacts': 'sidebar',
          'nav-saved': 'chat',
          'nav-settings': 'chat',
          'nav-archive': 'chat',
        }
        const targetScreen = screenMap[def.id] ?? 'chat'
        const isActive = activeScreen === targetScreen && def.id === 'nav-chats'

        return (
          <button
            key={slotId}
            onClick={() => setActiveScreen(targetScreen)}
            className={clsx(
              'flex-1 flex flex-col items-center justify-center h-full gap-0.5 transition-colors',
              isActive ? 'text-flux-accent' : 'text-flux-muted hover:text-flux-text',
            )}
          >
            <Icon size={20} />
            <span className="text-[10px] font-medium">{def.name}</span>
          </button>
        )
      })}
    </div>
  )
}
