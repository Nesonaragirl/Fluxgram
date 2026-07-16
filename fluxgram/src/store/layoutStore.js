/**
 * FluxGram Layout Store
 *
 * Single source of truth for all layout state. The JSON schema here is
 * designed for multi-device sync: serialize `state.layout` and send it
 * to any backend / sync layer. On restore, call `importLayout(json)`.
 *
 * Schema version: "1.0"
 */

import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import { SLOT_IDS } from './componentRegistry'

// ─── JSON Schema Types (documentation) ───────────────────────────────────────
// ComponentInstance {
//   instanceId: string     — unique within layout
//   componentId: string    — matches registry entry id
//   visible: boolean
//   pinned: boolean        — excluded from drag reorder
//   size: 'xs'|'sm'|'md'|'lg'|'xl'
// }
//
// LayoutJSON {
//   version: '1.0'
//   schemaVersion: number
//   deviceId: string
//   lastModified: number   — unix ms
//   slots: Record<SlotId, ComponentInstance[]>
// }

function uid() {
  return Math.random().toString(36).slice(2, 9) + Date.now().toString(36)
}

// ─── Default Layout ───────────────────────────────────────────────────────────
function makeInstance(componentId, overrides = {}) {
  return {
    instanceId: uid(),
    componentId,
    visible: true,
    pinned: false,
    size: 'md',
    ...overrides,
  }
}

const DEFAULT_SLOTS = {
  [SLOT_IDS.TOPBAR_LEFT]:    [makeInstance('menu-button', { size: 'sm' })],
  [SLOT_IDS.TOPBAR_CENTER]:  [makeInstance('chat-title', { size: 'lg', pinned: true })],
  [SLOT_IDS.TOPBAR_RIGHT]:   [
    makeInstance('search-button', { size: 'sm' }),
    makeInstance('call-button',   { size: 'sm' }),
    makeInstance('more-button',   { size: 'sm' }),
  ],
  [SLOT_IDS.BOTTOMBAR_1]:    [makeInstance('nav-chats',    { size: 'md' })],
  [SLOT_IDS.BOTTOMBAR_2]:    [makeInstance('nav-contacts', { size: 'md' })],
  [SLOT_IDS.BOTTOMBAR_3]:    [makeInstance('nav-saved',    { size: 'md' })],
  [SLOT_IDS.BOTTOMBAR_4]:    [makeInstance('nav-settings', { size: 'md' })],
  [SLOT_IDS.BOTTOMBAR_5]:    [],
  [SLOT_IDS.SIDEBAR_MAIN]:   [
    makeInstance('sidebar-newgroup', { size: 'md' }),
    makeInstance('sidebar-starchat', { size: 'md' }),
    makeInstance('nav-archive',      { size: 'md' }),
    makeInstance('sidebar-privacy',  { size: 'md' }),
  ],
  [SLOT_IDS.CHATINPUT_LEFT]:   [
    makeInstance('emoji-button',      { size: 'sm' }),
    makeInstance('attachment-button', { size: 'sm' }),
  ],
  [SLOT_IDS.CHATINPUT_CENTER]: [makeInstance('message-input', { size: 'xl', pinned: true })],
  [SLOT_IDS.CHATINPUT_RIGHT]:  [
    makeInstance('camera-button', { size: 'sm' }),
    makeInstance('voice-button',  { size: 'sm' }),
    makeInstance('send-button',   { size: 'md' }),
  ],
  [SLOT_IDS.PROFILE_AVATAR]:   [makeInstance('profile-avatar',  { size: 'lg' })],
  [SLOT_IDS.PROFILE_ACTIONS]:  [
    makeInstance('call-button',       { size: 'md' }),
    makeInstance('video-call-button', { size: 'md' }),
    makeInstance('profile-mute-btn',  { size: 'md' }),
    makeInstance('profile-edit-btn',  { size: 'md' }),
  ],
  [SLOT_IDS.PROFILE_STATS]:    [makeInstance('profile-stats', { size: 'md' })],
  [SLOT_IDS.PROFILE_BIO]:      [makeInstance('profile-bio',   { size: 'md' })],
  [SLOT_IDS.PROFILE_MEDIA]:    [makeInstance('profile-media-grid', { size: 'lg' })],
}

function buildDefaultLayout() {
  return {
    version: '1.0',
    schemaVersion: 1,
    deviceId: uid(),
    lastModified: Date.now(),
    slots: DEFAULT_SLOTS,
  }
}

// ─── Persistence ──────────────────────────────────────────────────────────────
const STORAGE_KEY = 'fluxgram:layout:v1'

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw)
    // Validate schema version
    if (parsed.schemaVersion !== 1) return null
    return parsed
  } catch { return null }
}

function saveToStorage(layout) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(layout))
  } catch { /* storage quota exceeded — silently ignore */ }
}

// ─── Store ────────────────────────────────────────────────────────────────────
export const useLayoutStore = create(
  immer((set, get) => ({
    // ── State
    layout: loadFromStorage() ?? buildDefaultLayout(),
    editMode: false,
    activeScreen: 'chat',     // 'chat' | 'profile' | 'sidebar'
    selectedInstanceId: null, // which item is selected in edit mode
    palette: { open: false, slotId: null }, // add-component palette state
    importExportModal: false,

    // ── Derived helpers (not reactive — call via get())
    getSlot: (slotId) => get().layout.slots[slotId] ?? [],

    // ── Edit mode
    toggleEditMode() {
      set(s => {
        s.editMode = !s.editMode
        if (!s.editMode) {
          s.selectedInstanceId = null
          s.palette.open = false
        }
      })
    },

    setActiveScreen(screen) {
      set(s => { s.activeScreen = screen })
    },

    selectInstance(instanceId) {
      set(s => {
        s.selectedInstanceId = s.selectedInstanceId === instanceId ? null : instanceId
      })
    },

    deselect() {
      set(s => { s.selectedInstanceId = null })
    },

    // ── Palette
    openPalette(slotId) {
      set(s => { s.palette = { open: true, slotId } })
    },
    closePalette() {
      set(s => { s.palette = { open: false, slotId: null } })
    },

    // ── Component CRUD
    addComponent(slotId, componentId, overrides = {}) {
      set(s => {
        const inst = makeInstance(componentId, overrides)
        s.layout.slots[slotId] = [...(s.layout.slots[slotId] ?? []), inst]
        s.layout.lastModified = Date.now()
        s.selectedInstanceId = inst.instanceId
        saveToStorage(s.layout)
      })
    },

    removeInstance(instanceId) {
      set(s => {
        for (const slotId of Object.keys(s.layout.slots)) {
          s.layout.slots[slotId] = s.layout.slots[slotId].filter(
            i => i.instanceId !== instanceId
          )
        }
        s.layout.lastModified = Date.now()
        if (s.selectedInstanceId === instanceId) s.selectedInstanceId = null
        saveToStorage(s.layout)
      })
    },

    updateInstance(instanceId, patch) {
      set(s => {
        for (const slotId of Object.keys(s.layout.slots)) {
          const idx = s.layout.slots[slotId].findIndex(i => i.instanceId === instanceId)
          if (idx !== -1) {
            Object.assign(s.layout.slots[slotId][idx], patch)
            break
          }
        }
        s.layout.lastModified = Date.now()
        saveToStorage(s.layout)
      })
    },

    toggleVisibility(instanceId) {
      const { layout } = get()
      for (const slotId of Object.keys(layout.slots)) {
        const inst = layout.slots[slotId].find(i => i.instanceId === instanceId)
        if (inst) { get().updateInstance(instanceId, { visible: !inst.visible }); return }
      }
    },

    togglePin(instanceId) {
      const { layout } = get()
      for (const slotId of Object.keys(layout.slots)) {
        const inst = layout.slots[slotId].find(i => i.instanceId === instanceId)
        if (inst) { get().updateInstance(instanceId, { pinned: !inst.pinned }); return }
      }
    },

    setSize(instanceId, size) {
      get().updateInstance(instanceId, { size })
    },

    // ── Reorder within slot
    reorderInSlot(slotId, fromIndex, toIndex) {
      set(s => {
        const slot = [...(s.layout.slots[slotId] ?? [])]
        const [moved] = slot.splice(fromIndex, 1)
        slot.splice(toIndex, 0, moved)
        s.layout.slots[slotId] = slot
        s.layout.lastModified = Date.now()
        saveToStorage(s.layout)
      })
    },

    // ── Move between slots (drag & drop cross-slot)
    moveToSlot(instanceId, fromSlotId, toSlotId, toIndex) {
      set(s => {
        const fromSlot = [...(s.layout.slots[fromSlotId] ?? [])]
        const idx = fromSlot.findIndex(i => i.instanceId === instanceId)
        if (idx === -1) return
        const [inst] = fromSlot.splice(idx, 1)
        s.layout.slots[fromSlotId] = fromSlot

        const toSlot = [...(s.layout.slots[toSlotId] ?? [])]
        const insertAt = toIndex != null ? toIndex : toSlot.length
        toSlot.splice(insertAt, 0, inst)
        s.layout.slots[toSlotId] = toSlot

        s.layout.lastModified = Date.now()
        saveToStorage(s.layout)
      })
    },

    // ── Import / Export
    exportLayout() {
      return JSON.stringify(get().layout, null, 2)
    },

    importLayout(jsonStr) {
      try {
        const parsed = JSON.parse(jsonStr)
        if (parsed.schemaVersion !== 1) throw new Error('Unsupported schema version')
        set(s => {
          s.layout = { ...parsed, lastModified: Date.now() }
          saveToStorage(s.layout)
        })
        return { ok: true }
      } catch (e) {
        return { ok: false, error: e.message }
      }
    },

    resetLayout() {
      set(s => {
        s.layout = buildDefaultLayout()
        saveToStorage(s.layout)
        s.selectedInstanceId = null
        s.palette.open = false
      })
    },

    // ── Toggle import/export modal
    toggleImportExportModal() {
      set(s => { s.importExportModal = !s.importExportModal })
    },
  }))
)
