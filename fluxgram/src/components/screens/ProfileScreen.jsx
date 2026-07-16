/**
 * ProfileScreen — simulates a Telegram profile view.
 * Each section is backed by its own slot.
 */
import clsx from 'clsx'
import { useLayoutStore } from '../../store/layoutStore'
import { Slot } from '../layout/Slot'
import { SLOT_IDS } from '../../store/componentRegistry'

export function ProfileScreen() {
  const editMode = useLayoutStore(s => s.editMode)

  return (
    <div className="flex flex-col h-full bg-flux-bg overflow-y-auto">
      {/* ── Avatar Hero ──────────────────────────────────────────────────── */}
      <div className={clsx(
        'relative flex flex-col items-center justify-end pb-6 pt-12',
        'bg-gradient-to-b from-flux-accent/30 to-flux-surface border-b border-flux-border flex-shrink-0',
        editMode && 'edit-outline-soft',
      )}>
        {editMode && <SlotLabel>Avatar Area</SlotLabel>}
        <AvatarSection editMode={editMode} />
        <div className="mt-3 text-center">
          <h2 className="text-lg font-bold text-flux-text">Alice Chen</h2>
          <p className="text-sm text-flux-muted">@alicechen · last seen recently</p>
        </div>
      </div>

      {/* ── Action Buttons ────────────────────────────────────────────────── */}
      <div className={clsx(
        'flex items-center justify-center gap-3 py-4 border-b border-flux-border bg-flux-surface flex-shrink-0',
        editMode && 'edit-outline-soft',
      )}>
        {editMode && <SlotLabel>Actions</SlotLabel>}
        <Slot slotId={SLOT_IDS.PROFILE_ACTIONS} className="flex-row flex-wrap justify-center gap-2" />
      </div>

      {/* ── Stats ─────────────────────────────────────────────────────────── */}
      <div className={clsx(
        'py-3 px-4 border-b border-flux-border flex-shrink-0',
        editMode && 'edit-outline-soft',
      )}>
        {editMode && <SlotLabel>Stats</SlotLabel>}
        <StatsSection editMode={editMode} />
      </div>

      {/* ── Bio ───────────────────────────────────────────────────────────── */}
      <div className={clsx(
        'py-3 px-4 border-b border-flux-border flex-shrink-0',
        editMode && 'edit-outline-soft',
      )}>
        {editMode && <SlotLabel>Bio</SlotLabel>}
        <BioSection editMode={editMode} />
      </div>

      {/* ── Media ─────────────────────────────────────────────────────────── */}
      <div className={clsx(
        'py-3 px-4 flex-1',
        editMode && 'edit-outline-soft',
      )}>
        {editMode && <SlotLabel>Media</SlotLabel>}
        <MediaSection editMode={editMode} />
      </div>
    </div>
  )
}

// ── Avatar ─────────────────────────────────────────────────────────────────────
function AvatarSection({ editMode }) {
  if (editMode) {
    return (
      <div className="flex flex-col items-center gap-2">
        <Slot slotId={SLOT_IDS.PROFILE_AVATAR} />
        <div className="w-20 h-20 rounded-full bg-flux-panel border-2 border-flux-border flex items-center justify-center text-3xl opacity-30">
          👤
        </div>
      </div>
    )
  }

  return (
    <div className="relative">
      <div className="w-24 h-24 rounded-full bg-gradient-to-br from-flux-accent to-purple-600 flex items-center justify-center text-4xl shadow-lg border-4 border-flux-surface">
        🟣
      </div>
      <div className="absolute bottom-1 right-1 w-4 h-4 bg-flux-success rounded-full border-2 border-flux-surface" />
    </div>
  )
}

// ── Stats ──────────────────────────────────────────────────────────────────────
function StatsSection({ editMode }) {
  if (editMode) {
    return <Slot slotId={SLOT_IDS.PROFILE_STATS} className="flex-row flex-wrap gap-2" />
  }

  const stats = [
    { label: 'Messages', value: '2.4k' },
    { label: 'Photos', value: '186' },
    { label: 'Videos', value: '43' },
    { label: 'Links', value: '91' },
  ]

  return (
    <div className="grid grid-cols-4 gap-2">
      {stats.map(s => (
        <div key={s.label} className="flex flex-col items-center bg-flux-panel rounded-xl py-2.5 px-1">
          <span className="text-sm font-bold text-flux-text">{s.value}</span>
          <span className="text-[10px] text-flux-muted mt-0.5">{s.label}</span>
        </div>
      ))}
    </div>
  )
}

// ── Bio ────────────────────────────────────────────────────────────────────────
function BioSection({ editMode }) {
  if (editMode) {
    return <Slot slotId={SLOT_IDS.PROFILE_BIO} className="flex-col" />
  }

  return (
    <div>
      <p className="text-xs text-flux-muted uppercase tracking-widest font-semibold mb-1.5">Bio</p>
      <p className="text-sm text-flux-text leading-relaxed">
        Building things at the intersection of design and technology ✦ FluxGram power user 🔧
      </p>
      <div className="mt-2 flex flex-wrap gap-1.5">
        {['@fluxgram.app', 'San Francisco', 'Joined 2019'].map(item => (
          <span key={item} className="text-[11px] text-flux-accent bg-flux-accent/10 rounded-full px-2 py-0.5">
            {item}
          </span>
        ))}
      </div>
    </div>
  )
}

// ── Media ──────────────────────────────────────────────────────────────────────
function MediaSection({ editMode }) {
  if (editMode) {
    return <Slot slotId={SLOT_IDS.PROFILE_MEDIA} className="flex-col" />
  }

  const colors = [
    '#5288c1', '#43a047', '#fb8c00', '#7c3aed',
    '#e53935', '#06b6d4', '#a855f7', '#10b981',
    '#f59e0b', '#ef4444', '#3b82f6', '#8b5cf6',
  ]

  return (
    <div>
      <p className="text-xs text-flux-muted uppercase tracking-widest font-semibold mb-2">Shared Media</p>
      <div className="grid grid-cols-4 gap-1">
        {colors.map((color, i) => (
          <div
            key={i}
            className="aspect-square rounded-lg"
            style={{ background: `${color}40`, border: `1px solid ${color}30` }}
          />
        ))}
      </div>
    </div>
  )
}

function SlotLabel({ children }) {
  return (
    <span className="absolute top-1.5 left-2 text-[9px] text-flux-edit uppercase tracking-widest font-semibold opacity-70">
      {children}
    </span>
  )
}
