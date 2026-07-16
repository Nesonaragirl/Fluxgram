/**
 * SlotItem — a single component instance rendered inside a slot.
 * In Edit Mode it gains drag handles, selection ring, and control overlay.
 */
import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical, Eye, EyeOff, Pin, PinOff, Trash2, Minus, Plus } from 'lucide-react'
import clsx from 'clsx'
import { useLayoutStore } from '../../store/layoutStore'
import { registry, SIZES } from '../../store/componentRegistry'
import { Tooltip } from '../ui/Tooltip'
import { Badge } from '../ui/Badge'

export function SlotItem({ instance, slotId, isLast }) {
  const editMode = useLayoutStore(s => s.editMode)
  const selectedInstanceId = useLayoutStore(s => s.selectedInstanceId)
  const selectInstance = useLayoutStore(s => s.selectInstance)
  const toggleVisibility = useLayoutStore(s => s.toggleVisibility)
  const togglePin = useLayoutStore(s => s.togglePin)
  const removeInstance = useLayoutStore(s => s.removeInstance)
  const setSize = useLayoutStore(s => s.setSize)

  const def = registry.get(instance.componentId)
  const isSelected = selectedInstanceId === instance.instanceId
  const isPinned = instance.pinned

  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: instance.instanceId,
    disabled: !editMode || isPinned,
    data: { instance, slotId },
  })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  if (!def) return null

  const Icon = def.icon
  const sizeIdx = SIZES.indexOf(instance.size)
  const canGrow = sizeIdx < (def.sizes ?? SIZES).length - 1
  const canShrink = sizeIdx > 0

  function handleClick(e) {
    if (!editMode) return
    e.stopPropagation()
    selectInstance(instance.instanceId)
  }

  function cycleSize(dir) {
    const allowedSizes = def.sizes ?? SIZES
    const idx = allowedSizes.indexOf(instance.size)
    const next = allowedSizes[Math.max(0, Math.min(allowedSizes.length - 1, idx + dir))]
    setSize(instance.instanceId, next)
  }

  // Rendered content
  const content = (
    <div
      ref={setNodeRef}
      style={style}
      className={clsx(
        'relative group flex items-center justify-center',
        'rounded-lg select-none transition-all',
        isDragging && 'opacity-40 z-50',
        editMode && !isPinned && 'cursor-pointer',
        editMode && isPinned && 'cursor-default',
        editMode && isSelected && 'ring-2 ring-flux-edit ring-offset-1 ring-offset-flux-surface',
        editMode && !isSelected && 'hover:ring-1 hover:ring-flux-edit/50 hover:ring-offset-1 hover:ring-offset-flux-surface',
        !instance.visible && editMode && 'opacity-40',
        !instance.visible && !editMode && 'hidden',
      )}
      onClick={handleClick}
    >
      {/* Drag handle — only in edit mode, not pinned */}
      {editMode && !isPinned && (
        <div
          {...attributes}
          {...listeners}
          className="absolute -top-1 -left-1 z-10 w-4 h-4 bg-flux-edit rounded flex items-center justify-center opacity-0 group-hover:opacity-100 cursor-grab active:cursor-grabbing"
        >
          <GripVertical size={10} className="text-black" />
        </div>
      )}

      {/* Pin badge */}
      {editMode && isPinned && (
        <div className="absolute -top-1 -left-1 z-10 w-4 h-4 bg-flux-muted/60 rounded flex items-center justify-center">
          <Pin size={8} className="text-white" />
        </div>
      )}

      {/* The component's visual representation */}
      <ComponentVisual def={def} instance={instance} editMode={editMode} />

      {/* Edit overlay controls — show when selected */}
      {editMode && isSelected && (
        <EditControls
          instance={instance}
          def={def}
          onToggleVisibility={() => toggleVisibility(instance.instanceId)}
          onTogglePin={() => togglePin(instance.instanceId)}
          onRemove={() => removeInstance(instance.instanceId)}
          onGrow={() => cycleSize(1)}
          onShrink={() => cycleSize(-1)}
          canGrow={canGrow}
          canShrink={canShrink}
        />
      )}
    </div>
  )

  return editMode ? content : content
}

// ─── Component visual ─────────────────────────────────────────────────────────
function ComponentVisual({ def, instance, editMode }) {
  const Icon = def.icon
  const isSmall = instance.size === 'xs' || instance.size === 'sm'

  return (
    <Tooltip text={editMode ? undefined : def.name}>
      <div
        className={clsx(
          'flex items-center justify-center gap-1.5 rounded-lg transition-colors',
          instance.size === 'xs' && 'w-6 h-6',
          instance.size === 'sm' && 'w-7 h-7',
          instance.size === 'md' && 'w-8 h-8',
          instance.size === 'lg' && 'px-3 py-1.5 h-8',
          instance.size === 'xl' && 'flex-1 px-3 py-1.5 h-8',
          editMode
            ? 'hover:bg-flux-edit/10'
            : 'hover:bg-white/5 active:bg-white/10',
        )}
        style={{ color: def.color ?? '#7d8e9e' }}
      >
        <Icon size={isSmall ? 14 : 16} />
        {(instance.size === 'lg' || instance.size === 'xl') && (
          <span className="text-xs text-flux-text font-medium truncate max-w-24">
            {def.name}
          </span>
        )}
        {def.plugin && (
          <span className="w-1.5 h-1.5 rounded-full bg-purple-400 flex-shrink-0" />
        )}
      </div>
    </Tooltip>
  )
}

// ─── Edit controls popup ─────────────────────────────────────────────────────
function EditControls({ instance, def, onToggleVisibility, onTogglePin, onRemove, onGrow, onShrink, canGrow, canShrink }) {
  return (
    <div
      className="absolute -bottom-11 left-1/2 -translate-x-1/2 z-30 flex items-center gap-0.5
        bg-flux-bg border border-flux-border rounded-lg px-1 py-1 shadow-xl slide-up whitespace-nowrap"
      onClick={e => e.stopPropagation()}
    >
      <Tooltip text={instance.visible ? 'Hide' : 'Show'}>
        <button
          onClick={onToggleVisibility}
          className="w-6 h-6 rounded flex items-center justify-center text-flux-muted hover:text-flux-text hover:bg-flux-panel transition-colors"
        >
          {instance.visible ? <Eye size={12} /> : <EyeOff size={12} />}
        </button>
      </Tooltip>
      <Tooltip text={instance.pinned ? 'Unpin' : 'Pin (lock in place)'}>
        <button
          onClick={onTogglePin}
          className={clsx(
            'w-6 h-6 rounded flex items-center justify-center transition-colors',
            instance.pinned ? 'text-flux-accent bg-flux-accent/10' : 'text-flux-muted hover:text-flux-text hover:bg-flux-panel'
          )}
        >
          {instance.pinned ? <PinOff size={12} /> : <Pin size={12} />}
        </button>
      </Tooltip>
      <Tooltip text="Shrink">
        <button
          onClick={onShrink}
          disabled={!canShrink}
          className="w-6 h-6 rounded flex items-center justify-center text-flux-muted hover:text-flux-text hover:bg-flux-panel transition-colors disabled:opacity-30"
        >
          <Minus size={12} />
        </button>
      </Tooltip>
      <span className="text-[10px] text-flux-muted font-mono px-0.5">{instance.size}</span>
      <Tooltip text="Grow">
        <button
          onClick={onGrow}
          disabled={!canGrow}
          className="w-6 h-6 rounded flex items-center justify-center text-flux-muted hover:text-flux-text hover:bg-flux-panel transition-colors disabled:opacity-30"
        >
          <Plus size={12} />
        </button>
      </Tooltip>
      <div className="w-px h-4 bg-flux-border mx-0.5" />
      <Tooltip text="Remove from slot">
        <button
          onClick={onRemove}
          className="w-6 h-6 rounded flex items-center justify-center text-flux-danger hover:bg-flux-danger/10 transition-colors"
        >
          <Trash2 size={12} />
        </button>
      </Tooltip>
    </div>
  )
}
