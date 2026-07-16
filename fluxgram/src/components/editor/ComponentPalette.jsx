/**
 * Component Palette — shown when the user clicks "Add" on a slot.
 * Lists all components compatible with that slot, grouped by category.
 * Plugin components are visually distinguished.
 */
import { X, Search, Puzzle } from 'lucide-react'
import { useState } from 'react'
import clsx from 'clsx'
import { useLayoutStore } from '../../store/layoutStore'
import { registry, SLOT_META } from '../../store/componentRegistry'
import { Badge } from '../ui/Badge'

export function ComponentPalette() {
  const palette = useLayoutStore(s => s.palette)
  const closePalette = useLayoutStore(s => s.closePalette)
  const addComponent = useLayoutStore(s => s.addComponent)
  const layout = useLayoutStore(s => s.layout)
  const [query, setQuery] = useState('')

  if (!palette.open) return null

  const slotId = palette.slotId
  const meta = SLOT_META[slotId]
  const allCompatible = registry.getForSlot(slotId)

  // IDs already in this slot
  const existingIds = new Set((layout.slots[slotId] ?? []).map(i => i.componentId))

  const filtered = allCompatible.filter(c =>
    c.name.toLowerCase().includes(query.toLowerCase()) ||
    c.description?.toLowerCase().includes(query.toLowerCase()) ||
    c.category?.toLowerCase().includes(query.toLowerCase())
  )

  // Group by category
  const groups = {}
  for (const c of filtered) {
    const cat = c.category ?? 'Other'
    if (!groups[cat]) groups[cat] = []
    groups[cat].push(c)
  }

  function handleAdd(componentId) {
    const def = registry.get(componentId)
    addComponent(slotId, componentId, { size: def.defaultSize })
    closePalette()
  }

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm"
        onClick={closePalette}
      />

      {/* Panel */}
      <div className="fixed right-4 top-1/2 -translate-y-1/2 z-50 w-72 bg-flux-bg border border-flux-border rounded-xl shadow-2xl slide-up overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-flux-border">
          <div>
            <h3 className="text-sm font-semibold text-flux-text">Add Component</h3>
            <p className="text-[11px] text-flux-muted mt-0.5">
              Slot: <span className="text-flux-accent">{meta?.label ?? slotId}</span>
            </p>
          </div>
          <button
            onClick={closePalette}
            className="w-6 h-6 rounded flex items-center justify-center text-flux-muted hover:text-flux-text hover:bg-flux-panel"
          >
            <X size={14} />
          </button>
        </div>

        {/* Search */}
        <div className="px-3 py-2 border-b border-flux-border">
          <div className="flex items-center gap-2 bg-flux-panel rounded-lg px-2.5 py-1.5">
            <Search size={13} className="text-flux-muted flex-shrink-0" />
            <input
              autoFocus
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Search components…"
              className="flex-1 bg-transparent text-xs text-flux-text placeholder-flux-muted outline-none"
            />
          </div>
        </div>

        {/* Component list */}
        <div className="overflow-y-auto max-h-80 p-2 space-y-3">
          {Object.entries(groups).length === 0 && (
            <div className="text-center text-flux-muted text-xs py-6">
              No matching components
            </div>
          )}
          {Object.entries(groups).map(([category, components]) => (
            <div key={category}>
              <p className="text-[10px] text-flux-muted uppercase tracking-widest font-semibold px-1 mb-1">
                {category}
              </p>
              <div className="space-y-0.5">
                {components.map(def => {
                  const Icon = def.icon
                  const alreadyAdded = existingIds.has(def.id)
                  return (
                    <button
                      key={def.id}
                      onClick={() => !alreadyAdded && handleAdd(def.id)}
                      disabled={alreadyAdded}
                      className={clsx(
                        'w-full flex items-center gap-2.5 px-2.5 py-2 rounded-lg text-left transition-all',
                        alreadyAdded
                          ? 'opacity-40 cursor-not-allowed'
                          : 'hover:bg-flux-panel cursor-pointer',
                      )}
                    >
                      <div
                        className="w-7 h-7 rounded-lg flex items-center justify-center flex-shrink-0"
                        style={{ background: `${def.color ?? '#5288c1'}20` }}
                      >
                        <Icon size={14} style={{ color: def.color ?? '#5288c1' }} />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-1.5">
                          <span className="text-xs font-medium text-flux-text truncate">{def.name}</span>
                          {def.plugin && (
                            <Badge color="plugin">Plugin</Badge>
                          )}
                          {alreadyAdded && (
                            <Badge color="muted">Added</Badge>
                          )}
                        </div>
                        <p className="text-[11px] text-flux-muted truncate">{def.description}</p>
                      </div>
                    </button>
                  )
                })}
              </div>
            </div>
          ))}
        </div>

        {/* Plugin note */}
        <div className="border-t border-flux-border px-3 py-2 flex items-center gap-1.5">
          <Puzzle size={11} className="text-purple-400 flex-shrink-0" />
          <p className="text-[10px] text-flux-muted">
            <span className="text-purple-400 font-medium">Plugin</span> components are installed externally
          </p>
        </div>
      </div>
    </>
  )
}
