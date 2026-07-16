/**
 * PluginShowcase — a dismissable info panel that explains the plugin system
 * and shows the 4 registered demo plugins with their registration code snippet.
 */
import { useState } from 'react'
import { X, Puzzle, ChevronDown, ChevronUp, Code2 } from 'lucide-react'
import { registry } from '../../store/componentRegistry'
import { Badge } from '../ui/Badge'

export function PluginShowcase() {
  const [open, setOpen] = useState(true)
  const [expanded, setExpanded] = useState(false)

  if (!open) return null

  const plugins = registry.getAll().filter(c => c.plugin)

  return (
    <div className="absolute bottom-20 left-3 right-3 z-40 bg-flux-surface border border-purple-500/30 rounded-xl shadow-2xl slide-up overflow-hidden max-w-sm">
      {/* Header */}
      <div className="flex items-center justify-between px-3 py-2.5 bg-purple-500/10 border-b border-purple-500/20">
        <div className="flex items-center gap-2">
          <Puzzle size={14} className="text-purple-400" />
          <span className="text-sm font-semibold text-purple-300">Plugin System Demo</span>
          <Badge color="plugin">{plugins.length} registered</Badge>
        </div>
        <button onClick={() => setOpen(false)} className="text-flux-muted hover:text-flux-text">
          <X size={14} />
        </button>
      </div>

      {/* Plugin list */}
      <div className="px-3 py-2 space-y-1.5">
        {plugins.map(p => {
          const Icon = p.icon
          return (
            <div key={p.id} className="flex items-start gap-2.5">
              <div
                className="w-7 h-7 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5"
                style={{ background: `${p.color}20` }}
              >
                <Icon size={13} style={{ color: p.color }} />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-1.5 flex-wrap">
                  <span className="text-xs font-semibold text-flux-text">{p.name}</span>
                  <span className="text-[10px] text-flux-muted">by {p.pluginMeta?.author}</span>
                </div>
                <p className="text-[11px] text-flux-muted leading-tight">{p.description}</p>
                <p className="text-[10px] text-purple-400 mt-0.5">
                  {p.compatibleSlots.length} compatible slot{p.compatibleSlots.length !== 1 ? 's' : ''}
                </p>
              </div>
            </div>
          )
        })}
      </div>

      {/* Code snippet toggle */}
      <button
        onClick={() => setExpanded(v => !v)}
        className="w-full flex items-center justify-between px-3 py-2 border-t border-flux-border text-[11px] text-flux-muted hover:text-flux-text transition-colors"
      >
        <div className="flex items-center gap-1.5">
          <Code2 size={11} />
          Plugin registration API
        </div>
        {expanded ? <ChevronUp size={11} /> : <ChevronDown size={11} />}
      </button>

      {expanded && (
        <div className="px-3 pb-3">
          <pre className="text-[10px] font-mono text-flux-muted bg-flux-bg rounded-lg p-2.5 overflow-x-auto leading-relaxed">
{`// Any plugin registers via:
import { registry } from 'fluxgram/registry'

registry.register({
  id: 'my-plugin-btn',
  name: 'My Feature',
  icon: MyIcon,
  description: 'Does something cool',
  plugin: true,
  pluginMeta: { author: 'You', version: '1.0.0' },
  // Which slots this COULD appear in:
  compatibleSlots: ['TopBar.Left', 'ChatInput.Right'],
  sizes: ['sm', 'md'],
  defaultSize: 'sm',
  color: '#a855f7',
})
// That's it. The USER decides if/where
// it appears using Edit Mode — not the plugin.`}
          </pre>
        </div>
      )}
    </div>
  )
}
