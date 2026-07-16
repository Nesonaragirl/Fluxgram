/**
 * ChatScreen — simulates a Telegram chat view.
 * TopBar, message list, and ChatInput rows are all slot-driven.
 */
import { useState } from 'react'
import clsx from 'clsx'
import { useLayoutStore } from '../../store/layoutStore'
import { Slot } from '../layout/Slot'
import { SLOT_IDS } from '../../store/componentRegistry'

// ── Fake message data ─────────────────────────────────────────────────────────
const MESSAGES = [
  { id: 1, from: 'Alice', text: 'Hey! Have you seen the new FluxGram update?', time: '10:21', mine: false, avatar: '🟣' },
  { id: 2, from: 'Me', text: 'Not yet — what changed?', time: '10:22', mine: true },
  { id: 3, from: 'Alice', text: 'You can now fully customize the entire interface — drag panels, hide buttons, rearrange the bottom bar. It\'s like Android launcher energy but for a chat app 🔥', time: '10:22', mine: false, avatar: '🟣' },
  { id: 4, from: 'Me', text: 'Wait, no code required?', time: '10:23', mine: true },
  { id: 5, from: 'Alice', text: 'None at all. There\'s an Edit Layout button — tap it and everything becomes rearrangeable. Plugins too — I added the AI Summary button to my top bar.', time: '10:24', mine: false, avatar: '🟣' },
  { id: 6, from: 'Me', text: 'That\'s wild. Trying it now 👀', time: '10:25', mine: true },
  { id: 7, from: 'Alice', text: 'Let me know how you set it up — I keep my voice message button on the left lol', time: '10:25', mine: false, avatar: '🟣' },
  { id: 8, from: 'Me', text: 'Bold choice 😂 Okay I pinned the message input so it stays centered and moved the attachment to the right. Feels cleaner.', time: '10:27', mine: true },
  { id: 9, from: 'Alice', text: 'The pinning feature is great btw — so things you never want to move stay locked even if you accidentally drag', time: '10:28', mine: false, avatar: '🟣' },
  { id: 10, from: 'Me', text: '💯 okay seriously impressive', time: '10:28', mine: true },
]

export function ChatScreen() {
  const editMode = useLayoutStore(s => s.editMode)
  const [inputText, setInputText] = useState('')

  return (
    <div className="flex flex-col h-full bg-flux-bg">
      {/* ── Top Bar ─────────────────────────────────────────────────────── */}
      <TopBar editMode={editMode} />

      {/* ── Message List ────────────────────────────────────────────────── */}
      <div className="flex-1 overflow-y-auto px-3 py-4 space-y-2">
        {MESSAGES.map(msg => (
          <ChatBubble key={msg.id} msg={msg} />
        ))}
        {editMode && (
          <div className="text-center py-2">
            <span className="inline-flex items-center gap-1.5 text-[11px] text-flux-edit bg-flux-edit/10 rounded-full px-3 py-1">
              <span className="w-1.5 h-1.5 rounded-full bg-flux-edit animate-pulse" />
              Edit Mode — drag components between slots
            </span>
          </div>
        )}
      </div>

      {/* ── Chat Input ──────────────────────────────────────────────────── */}
      <ChatInputBar editMode={editMode} inputText={inputText} setInputText={setInputText} />
    </div>
  )
}

// ── TopBar ────────────────────────────────────────────────────────────────────
function TopBar({ editMode }) {
  return (
    <div className={clsx(
      'flex items-center gap-1 px-2 py-2 border-b border-flux-border bg-flux-surface flex-shrink-0',
      editMode && 'edit-outline-soft',
    )}>
      {editMode && <SlotGroupLabel>Top Bar</SlotGroupLabel>}

      {/* Left slot */}
      <Slot slotId={SLOT_IDS.TOPBAR_LEFT} className="flex-shrink-0" />

      {/* Center slot — takes available space */}
      <div className="flex-1 flex items-center justify-center min-w-0">
        <Slot slotId={SLOT_IDS.TOPBAR_CENTER} className="flex-1 justify-center" />
      </div>

      {/* Right slot */}
      <Slot slotId={SLOT_IDS.TOPBAR_RIGHT} className="flex-shrink-0" />
    </div>
  )
}

// ── ChatInput ─────────────────────────────────────────────────────────────────
function ChatInputBar({ editMode, inputText, setInputText }) {
  return (
    <div className={clsx(
      'flex items-center gap-1 px-2 py-2 border-t border-flux-border bg-flux-surface flex-shrink-0',
      editMode && 'edit-outline-soft',
    )}>
      {editMode && <SlotGroupLabel>Chat Input</SlotGroupLabel>}

      {/* Left slot */}
      <Slot slotId={SLOT_IDS.CHATINPUT_LEFT} className="flex-shrink-0" />

      {/* Center slot — input field lives here as a slot component */}
      <div className="flex-1 flex items-center min-w-0">
        <ChatInputCenter inputText={inputText} setInputText={setInputText} editMode={editMode} />
      </div>

      {/* Right slot */}
      <Slot slotId={SLOT_IDS.CHATINPUT_RIGHT} className="flex-shrink-0" />
    </div>
  )
}

function ChatInputCenter({ inputText, setInputText, editMode }) {
  // The message-input component is rendered as a real textarea when in normal mode.
  // In edit mode it's shown as a placeholder block (the SlotItem renders it).
  if (editMode) {
    return (
      <div className="flex-1 flex items-center">
        <Slot slotId={SLOT_IDS.CHATINPUT_CENTER} className="flex-1" />
      </div>
    )
  }

  return (
    <div className="flex-1 mx-1">
      <input
        type="text"
        value={inputText}
        onChange={e => setInputText(e.target.value)}
        placeholder="Message"
        className="w-full bg-flux-panel rounded-xl px-3 py-2 text-sm text-flux-text placeholder-flux-muted outline-none border border-flux-border focus:border-flux-accent transition-colors"
      />
    </div>
  )
}

// ── Chat Bubble ───────────────────────────────────────────────────────────────
function ChatBubble({ msg }) {
  return (
    <div className={clsx('flex items-end gap-2', msg.mine ? 'flex-row-reverse' : 'flex-row')}>
      {!msg.mine && (
        <div className="w-7 h-7 rounded-full bg-flux-panel flex items-center justify-center text-base flex-shrink-0">
          {msg.avatar}
        </div>
      )}
      <div
        className={clsx(
          'max-w-[72%] px-3 py-2 rounded-2xl text-sm leading-relaxed',
          msg.mine
            ? 'bg-flux-accent text-white rounded-br-sm'
            : 'bg-flux-surface text-flux-text rounded-bl-sm border border-flux-border',
        )}
      >
        {!msg.mine && (
          <p className="text-[11px] font-semibold text-flux-accent mb-0.5">{msg.from}</p>
        )}
        <p>{msg.text}</p>
        <p className={clsx('text-[10px] mt-1 text-right', msg.mine ? 'text-white/60' : 'text-flux-muted')}>
          {msg.time}
        </p>
      </div>
    </div>
  )
}

function SlotGroupLabel({ children }) {
  return (
    <span className="text-[9px] text-flux-edit uppercase tracking-widest font-semibold flex-shrink-0 mr-1 opacity-60">
      {children}
    </span>
  )
}
