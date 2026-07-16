/**
 * DndController — wraps the entire app with DnD context.
 * Handles cross-slot drops by finding source/dest slot IDs from drag data.
 */
import {
  DndContext,
  PointerSensor,
  KeyboardSensor,
  useSensor,
  useSensors,
  DragOverlay,
  closestCenter,
} from '@dnd-kit/core'
import { sortableKeyboardCoordinates } from '@dnd-kit/sortable'
import { useState } from 'react'
import { useLayoutStore } from '../../store/layoutStore'
import { registry } from '../../store/componentRegistry'

export function DndController({ children }) {
  const editMode = useLayoutStore(s => s.editMode)
  const layout = useLayoutStore(s => s.layout)
  const reorderInSlot = useLayoutStore(s => s.reorderInSlot)
  const moveToSlot = useLayoutStore(s => s.moveToSlot)

  const [activeItem, setActiveItem] = useState(null) // { instance, def }

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  )

  function findSlotForInstance(instanceId) {
    for (const [slotId, items] of Object.entries(layout.slots)) {
      if (items.find(i => i.instanceId === instanceId)) return slotId
    }
    return null
  }

  function handleDragStart({ active }) {
    const instance = active.data.current?.instance
    if (!instance) return
    const def = registry.get(instance.componentId)
    setActiveItem({ instance, def })
  }

  function handleDragEnd({ active, over }) {
    setActiveItem(null)
    if (!over) return

    const activeInstanceId = active.id
    const overId = over.id // could be an instanceId OR "droppable:<slotId>"

    const sourceSlotId = findSlotForInstance(activeInstanceId)
    if (!sourceSlotId) return

    // Determine target slot
    let destSlotId
    let destIndex = null

    if (typeof overId === 'string' && overId.startsWith('droppable:')) {
      // Dropped directly onto the slot container
      destSlotId = overId.replace('droppable:', '')
    } else {
      // Dropped onto another item — find that item's slot
      destSlotId = findSlotForInstance(overId)
      if (destSlotId) {
        destIndex = layout.slots[destSlotId].findIndex(i => i.instanceId === overId)
      }
    }

    if (!destSlotId) return

    // Check compatibility
    const instance = layout.slots[sourceSlotId].find(i => i.instanceId === activeInstanceId)
    if (!instance) return
    const def = registry.get(instance.componentId)
    if (!def?.compatibleSlots.includes(destSlotId)) return

    if (sourceSlotId === destSlotId) {
      // Same-slot reorder
      const slot = layout.slots[sourceSlotId]
      const fromIndex = slot.findIndex(i => i.instanceId === activeInstanceId)
      const toIndex = destIndex ?? slot.length - 1
      if (fromIndex !== toIndex) reorderInSlot(sourceSlotId, fromIndex, toIndex)
    } else {
      // Cross-slot move
      moveToSlot(activeInstanceId, sourceSlotId, destSlotId, destIndex)
    }
  }

  if (!editMode) return children

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      {children}
      <DragOverlay dropAnimation={null}>
        {activeItem && (
          <div className="drag-float flex items-center gap-2 bg-flux-bg border border-flux-edit rounded-lg px-2.5 py-1.5 shadow-2xl opacity-90">
            <activeItem.def.icon size={14} style={{ color: activeItem.def.color }} />
            <span className="text-xs text-flux-text">{activeItem.def.name}</span>
          </div>
        )}
      </DragOverlay>
    </DndContext>
  )
}
