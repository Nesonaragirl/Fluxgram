/**
 * Slot — a named region that holds 0-N component instances.
 * In Edit Mode, shows as a droppable target with an Add button.
 */
import { useDroppable } from '@dnd-kit/core'
import { SortableContext, horizontalListSortingStrategy, verticalListSortingStrategy } from '@dnd-kit/sortable'
import { Plus } from 'lucide-react'
import clsx from 'clsx'
import { useLayoutStore } from '../../store/layoutStore'
import { SLOT_META } from '../../store/componentRegistry'
import { SlotItem } from './SlotItem'
import { Tooltip } from '../ui/Tooltip'

export function Slot({ slotId, className, itemClassName, showLabel }) {
  const editMode = useLayoutStore(s => s.editMode)
  const layout = useLayoutStore(s => s.layout)
  const openPalette = useLayoutStore(s => s.openPalette)
  const deselect = useLayoutStore(s => s.deselect)

  const instances = layout.slots[slotId] ?? []
  const meta = SLOT_META[slotId]
  const isHorizontal = meta?.direction === 'row'
  const canAdd = instances.length < (meta?.maxItems ?? 10)

  const { setNodeRef, isOver } = useDroppable({
    id: `droppable:${slotId}`,
    data: { slotId },
  })

  const itemIds = instances.map(i => i.instanceId)

  return (
    <div
      ref={setNodeRef}
      className={clsx(
        'flex items-center',
        isHorizontal ? 'flex-row' : 'flex-col',
        'gap-1 transition-all',
        editMode && instances.length === 0 && 'slot-empty rounded-lg min-w-8 min-h-8 p-1',
        editMode && isOver && 'slot-drop-active rounded-lg',
        className
      )}
      onClick={editMode ? e => { e.stopPropagation(); deselect() } : undefined}
    >
      {showLabel && editMode && (
        <span className="text-[9px] text-flux-muted uppercase tracking-widest mb-0.5 font-semibold">
          {meta?.label}
        </span>
      )}

      <SortableContext
        items={itemIds}
        strategy={isHorizontal ? horizontalListSortingStrategy : verticalListSortingStrategy}
      >
        {instances.map((inst, idx) => (
          <SlotItem
            key={inst.instanceId}
            instance={inst}
            slotId={slotId}
            isLast={idx === instances.length - 1}
          />
        ))}
      </SortableContext>

      {/* Add button — only in edit mode when slot isn't full */}
      {editMode && canAdd && (
        <Tooltip text={`Add to ${meta?.label ?? slotId}`}>
          <button
            onClick={e => { e.stopPropagation(); openPalette(slotId) }}
            className={clsx(
              'flex items-center justify-center rounded-lg transition-all',
              'text-flux-muted hover:text-flux-edit hover:bg-flux-edit/10',
              'border border-dashed border-flux-border hover:border-flux-edit',
              isHorizontal ? 'w-6 h-6' : 'w-full h-6',
            )}
          >
            <Plus size={11} />
          </button>
        </Tooltip>
      )}
    </div>
  )
}
