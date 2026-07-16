/**
 * EditModeBar — persistent header shown at the top in Edit Mode.
 * Shows context, controls, and layout export/import actions.
 */
import {
  Pencil, X, RotateCcw, Download, Upload, Info,
  Eye, EyeOff, ChevronRight, Puzzle, Layers,
} from 'lucide-react'
import { useState } from 'react'
import { useLayoutStore } from '../../store/layoutStore'
import { registry } from '../../store/componentRegistry'
import { Button } from '../ui/Button'
import { Badge } from '../ui/Badge'

export function EditModeBar() {
  const editMode = useLayoutStore(s => s.editMode)
  const toggleEditMode = useLayoutStore(s => s.toggleEditMode)
  const resetLayout = useLayoutStore(s => s.resetLayout)
  const exportLayout = useLayoutStore(s => s.exportLayout)
  const importLayout = useLayoutStore(s => s.importLayout)
  const layout = useLayoutStore(s => s.layout)
  const [showExport, setShowExport] = useState(false)
  const [importText, setImportText] = useState('')
  const [importError, setImportError] = useState('')
  const [tab, setTab] = useState('export')

  if (!editMode) return null

  const pluginCount = registry.getAll().filter(c => c.plugin).length
  const lastMod = new Date(layout.lastModified).toLocaleTimeString()

  function handleImport() {
    const result = importLayout(importText)
    if (result.ok) {
      setImportText('')
      setImportError('')
      setShowExport(false)
    } else {
      setImportError(result.error)
    }
  }

  return (
    <>
      {/* Edit mode banner */}
      <div className="flex items-center gap-2 px-3 py-2 bg-flux-edit/10 border-b border-flux-edit/30 text-xs">
        <Pencil size={12} className="text-flux-edit flex-shrink-0" />
        <span className="text-flux-edit font-semibold">Edit Mode</span>
        <span className="text-flux-muted">— drag components between slots, click to select &amp; configure</span>

        <div className="ml-auto flex items-center gap-1.5">
          <Badge color="plugin">
            <Puzzle size={9} className="mr-0.5" />
            {pluginCount} plugins
          </Badge>
          <span className="text-flux-muted text-[10px]">saved {lastMod}</span>
          <Button size="xs" variant="warning" onClick={() => setShowExport(v => !v)}>
            <Layers size={11} />
            JSON
          </Button>
          <Button size="xs" variant="danger" onClick={resetLayout}>
            <RotateCcw size={11} />
            Reset
          </Button>
          <Button size="xs" variant="primary" onClick={toggleEditMode}>
            <X size={11} />
            Done
          </Button>
        </div>
      </div>

      {/* Import / Export drawer */}
      {showExport && (
        <div className="bg-flux-panel border-b border-flux-border px-4 py-3 slide-up">
          <div className="flex gap-2 mb-2">
            <button
              onClick={() => setTab('export')}
              className={`text-xs px-2 py-1 rounded transition-colors ${tab === 'export' ? 'bg-flux-accent text-white' : 'text-flux-muted hover:text-flux-text'}`}
            >Export / Copy</button>
            <button
              onClick={() => setTab('import')}
              className={`text-xs px-2 py-1 rounded transition-colors ${tab === 'import' ? 'bg-flux-accent text-white' : 'text-flux-muted hover:text-flux-text'}`}
            >Import / Paste</button>
          </div>

          {tab === 'export' && (
            <div>
              <p className="text-[11px] text-flux-muted mb-1.5">
                This JSON is your full layout config. Copy it to sync across devices or share your setup.
              </p>
              <textarea
                readOnly
                value={exportLayout()}
                className="w-full h-36 text-[10px] font-mono bg-flux-bg border border-flux-border rounded-lg p-2 text-flux-muted resize-none outline-none"
              />
              <Button size="xs" variant="default" className="mt-2"
                onClick={() => navigator.clipboard.writeText(exportLayout())}>
                <Download size={11} /> Copy to clipboard
              </Button>
            </div>
          )}

          {tab === 'import' && (
            <div>
              <p className="text-[11px] text-flux-muted mb-1.5">
                Paste a previously exported layout JSON to restore it.
              </p>
              <textarea
                value={importText}
                onChange={e => setImportText(e.target.value)}
                placeholder='Paste layout JSON here…'
                className="w-full h-36 text-[10px] font-mono bg-flux-bg border border-flux-border rounded-lg p-2 text-flux-text resize-none outline-none"
              />
              {importError && (
                <p className="text-[11px] text-flux-danger mt-1">{importError}</p>
              )}
              <Button size="xs" variant="primary" className="mt-2" onClick={handleImport}>
                <Upload size={11} /> Apply Layout
              </Button>
            </div>
          )}
        </div>
      )}
    </>
  )
}

// ── Floating Edit Toggle button (shown when NOT in edit mode) ─────────────────
export function EditToggleButton() {
  const editMode = useLayoutStore(s => s.editMode)
  const toggleEditMode = useLayoutStore(s => s.toggleEditMode)

  return (
    <button
      onClick={toggleEditMode}
      title={editMode ? 'Exit Edit Mode' : 'Enter Edit Mode — rearrange the interface'}
      className={`
        fixed bottom-20 right-4 z-50 flex items-center gap-2 px-3 py-2 rounded-xl shadow-xl
        text-sm font-semibold transition-all
        ${editMode
          ? 'bg-flux-edit text-black edit-pulse'
          : 'bg-flux-accent text-white hover:bg-flux-accentHover'
        }
      `}
    >
      <Pencil size={14} />
      {editMode ? 'Done Editing' : 'Edit Layout'}
    </button>
  )
}
