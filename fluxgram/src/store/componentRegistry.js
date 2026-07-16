/**
 * FluxGram Component Registry
 * 
 * Plugins register components here. The registry is the single source of truth
 * for what components EXIST; the layout store controls WHERE they appear.
 *
 * Plugin API:
 *   registry.register(definition)   — add a component
 *   registry.get(id)                — look up one component
 *   registry.getAll()               — all registered components
 *   registry.getForSlot(slotId)     — components compatible with a slot
 */

import {
  Search, Menu, MoreVertical, Phone, Video, Settings,
  MessageCircle, Users, BookMarked, Archive, Mic,
  Paperclip, Smile, Camera, Send, Zap, Bot,
  Languages, Download, Bell, Shield, Star, Hash,
  ChevronLeft, Plus, Edit3, Globe, Volume2,
} from 'lucide-react'

// All slot IDs in the system
export const SLOT_IDS = {
  // TopBar
  TOPBAR_LEFT:    'TopBar.Left',
  TOPBAR_CENTER:  'TopBar.Center',
  TOPBAR_RIGHT:   'TopBar.Right',
  // BottomBar
  BOTTOMBAR_1:    'BottomBar.Slot1',
  BOTTOMBAR_2:    'BottomBar.Slot2',
  BOTTOMBAR_3:    'BottomBar.Slot3',
  BOTTOMBAR_4:    'BottomBar.Slot4',
  BOTTOMBAR_5:    'BottomBar.Slot5',
  // Sidebar
  SIDEBAR_MAIN:   'Sidebar.Main',
  // ChatInput
  CHATINPUT_LEFT:   'ChatInput.Left',
  CHATINPUT_CENTER: 'ChatInput.Center',
  CHATINPUT_RIGHT:  'ChatInput.Right',
  // ProfileScreen
  PROFILE_AVATAR:   'ProfileScreen.Avatar',
  PROFILE_ACTIONS:  'ProfileScreen.Actions',
  PROFILE_STATS:    'ProfileScreen.Stats',
  PROFILE_BIO:      'ProfileScreen.Bio',
  PROFILE_MEDIA:    'ProfileScreen.Media',
}

// Slot metadata
export const SLOT_META = {
  [SLOT_IDS.TOPBAR_LEFT]:    { label: 'Top Bar — Left',    group: 'TopBar',       maxItems: 4, direction: 'row' },
  [SLOT_IDS.TOPBAR_CENTER]:  { label: 'Top Bar — Center',  group: 'TopBar',       maxItems: 1, direction: 'row' },
  [SLOT_IDS.TOPBAR_RIGHT]:   { label: 'Top Bar — Right',   group: 'TopBar',       maxItems: 4, direction: 'row' },
  [SLOT_IDS.BOTTOMBAR_1]:    { label: 'Bottom Bar — 1',    group: 'BottomBar',    maxItems: 1, direction: 'row' },
  [SLOT_IDS.BOTTOMBAR_2]:    { label: 'Bottom Bar — 2',    group: 'BottomBar',    maxItems: 1, direction: 'row' },
  [SLOT_IDS.BOTTOMBAR_3]:    { label: 'Bottom Bar — 3',    group: 'BottomBar',    maxItems: 1, direction: 'row' },
  [SLOT_IDS.BOTTOMBAR_4]:    { label: 'Bottom Bar — 4',    group: 'BottomBar',    maxItems: 1, direction: 'row' },
  [SLOT_IDS.BOTTOMBAR_5]:    { label: 'Bottom Bar — 5',    group: 'BottomBar',    maxItems: 1, direction: 'row' },
  [SLOT_IDS.SIDEBAR_MAIN]:   { label: 'Sidebar',           group: 'Sidebar',      maxItems: 12, direction: 'col' },
  [SLOT_IDS.CHATINPUT_LEFT]:   { label: 'Chat Input — Left',   group: 'ChatInput', maxItems: 4, direction: 'row' },
  [SLOT_IDS.CHATINPUT_CENTER]: { label: 'Chat Input — Center', group: 'ChatInput', maxItems: 1, direction: 'row' },
  [SLOT_IDS.CHATINPUT_RIGHT]:  { label: 'Chat Input — Right',  group: 'ChatInput', maxItems: 4, direction: 'row' },
  [SLOT_IDS.PROFILE_AVATAR]:   { label: 'Profile — Avatar',    group: 'ProfileScreen', maxItems: 1, direction: 'col' },
  [SLOT_IDS.PROFILE_ACTIONS]:  { label: 'Profile — Actions',   group: 'ProfileScreen', maxItems: 6, direction: 'row' },
  [SLOT_IDS.PROFILE_STATS]:    { label: 'Profile — Stats',     group: 'ProfileScreen', maxItems: 4, direction: 'row' },
  [SLOT_IDS.PROFILE_BIO]:      { label: 'Profile — Bio',       group: 'ProfileScreen', maxItems: 1, direction: 'col' },
  [SLOT_IDS.PROFILE_MEDIA]:    { label: 'Profile — Media',     group: 'ProfileScreen', maxItems: 1, direction: 'col' },
}

// Component sizes
export const SIZES = ['xs', 'sm', 'md', 'lg', 'xl']

// ─── Registry ────────────────────────────────────────────────────────────────

const _registry = new Map()

export const registry = {
  register(def) {
    if (!def.id) throw new Error('Component definition must have an id')
    if (!def.compatibleSlots?.length) throw new Error(`Component ${def.id} must declare compatibleSlots`)
    _registry.set(def.id, {
      plugin: false,
      sizes: ['sm', 'md'],
      defaultSize: 'md',
      ...def,
    })
    return registry
  },

  get(id) { return _registry.get(id) },

  getAll() { return [..._registry.values()] },

  getForSlot(slotId) {
    return [..._registry.values()].filter(c => c.compatibleSlots.includes(slotId))
  },

  getByGroup(group) {
    return [..._registry.values()].filter(c =>
      c.compatibleSlots.some(s => SLOT_META[s]?.group === group)
    )
  },
}

// ─── Built-in components ──────────────────────────────────────────────────────

const TOPBAR_SLOTS = [SLOT_IDS.TOPBAR_LEFT, SLOT_IDS.TOPBAR_CENTER, SLOT_IDS.TOPBAR_RIGHT]
const BOTTOMBAR_SLOTS = [SLOT_IDS.BOTTOMBAR_1, SLOT_IDS.BOTTOMBAR_2, SLOT_IDS.BOTTOMBAR_3, SLOT_IDS.BOTTOMBAR_4, SLOT_IDS.BOTTOMBAR_5]
const CHATINPUT_SLOTS = [SLOT_IDS.CHATINPUT_LEFT, SLOT_IDS.CHATINPUT_CENTER, SLOT_IDS.CHATINPUT_RIGHT]
const PROFILE_ACTION_SLOTS = [SLOT_IDS.PROFILE_ACTIONS]

registry
  // ── Global / TopBar
  .register({
    id: 'back-button',
    name: 'Back Button',
    icon: ChevronLeft,
    description: 'Navigate to previous screen',
    category: 'Navigation',
    compatibleSlots: [SLOT_IDS.TOPBAR_LEFT],
    sizes: ['sm'],
    defaultSize: 'sm',
    color: '#7d8e9e',
  })
  .register({
    id: 'chat-title',
    name: 'Chat Title',
    icon: Hash,
    description: 'Shows current chat name & status',
    category: 'Info',
    compatibleSlots: [SLOT_IDS.TOPBAR_CENTER, SLOT_IDS.TOPBAR_LEFT],
    sizes: ['md', 'lg'],
    defaultSize: 'lg',
    color: '#e8e8e8',
  })
  .register({
    id: 'search-button',
    name: 'Search',
    icon: Search,
    description: 'Search messages & conversations',
    category: 'Navigation',
    compatibleSlots: TOPBAR_SLOTS,
    sizes: ['sm', 'md'],
    defaultSize: 'sm',
    color: '#5288c1',
  })
  .register({
    id: 'menu-button',
    name: 'Menu',
    icon: Menu,
    description: 'Open navigation drawer',
    category: 'Navigation',
    compatibleSlots: TOPBAR_SLOTS,
    sizes: ['sm'],
    defaultSize: 'sm',
    color: '#7d8e9e',
  })
  .register({
    id: 'more-button',
    name: 'More Options',
    icon: MoreVertical,
    description: 'Show contextual overflow menu',
    category: 'Navigation',
    compatibleSlots: TOPBAR_SLOTS,
    sizes: ['sm'],
    defaultSize: 'sm',
    color: '#7d8e9e',
  })
  .register({
    id: 'call-button',
    name: 'Voice Call',
    icon: Phone,
    description: 'Start a voice call',
    category: 'Actions',
    compatibleSlots: [...TOPBAR_SLOTS, ...PROFILE_ACTION_SLOTS],
    sizes: ['sm', 'md'],
    defaultSize: 'sm',
    color: '#43a047',
  })
  .register({
    id: 'video-call-button',
    name: 'Video Call',
    icon: Video,
    description: 'Start a video call',
    category: 'Actions',
    compatibleSlots: [...TOPBAR_SLOTS, ...PROFILE_ACTION_SLOTS],
    sizes: ['sm', 'md'],
    defaultSize: 'sm',
    color: '#5288c1',
  })
  .register({
    id: 'notification-button',
    name: 'Notifications',
    icon: Bell,
    description: 'Mute / unmute notifications',
    category: 'Actions',
    compatibleSlots: [...TOPBAR_SLOTS, ...PROFILE_ACTION_SLOTS],
    sizes: ['sm'],
    defaultSize: 'sm',
    color: '#fb8c00',
  })

  // ── BottomBar / Navigation tabs
  .register({
    id: 'nav-chats',
    name: 'Chats',
    icon: MessageCircle,
    description: 'All conversations',
    category: 'Navigation',
    compatibleSlots: BOTTOMBAR_SLOTS,
    sizes: ['md'],
    defaultSize: 'md',
    color: '#5288c1',
  })
  .register({
    id: 'nav-contacts',
    name: 'Contacts',
    icon: Users,
    description: 'Contact list',
    category: 'Navigation',
    compatibleSlots: BOTTOMBAR_SLOTS,
    sizes: ['md'],
    defaultSize: 'md',
    color: '#7d8e9e',
  })
  .register({
    id: 'nav-saved',
    name: 'Saved',
    icon: BookMarked,
    description: 'Saved messages',
    category: 'Navigation',
    compatibleSlots: BOTTOMBAR_SLOTS,
    sizes: ['md'],
    defaultSize: 'md',
    color: '#7d8e9e',
  })
  .register({
    id: 'nav-settings',
    name: 'Settings',
    icon: Settings,
    description: 'App settings',
    category: 'Navigation',
    compatibleSlots: [...BOTTOMBAR_SLOTS, SLOT_IDS.SIDEBAR_MAIN],
    sizes: ['md'],
    defaultSize: 'md',
    color: '#7d8e9e',
  })
  .register({
    id: 'nav-archive',
    name: 'Archive',
    icon: Archive,
    description: 'Archived chats',
    category: 'Navigation',
    compatibleSlots: [...BOTTOMBAR_SLOTS, SLOT_IDS.SIDEBAR_MAIN],
    sizes: ['md'],
    defaultSize: 'md',
    color: '#7d8e9e',
  })

  // ── Sidebar
  .register({
    id: 'sidebar-newgroup',
    name: 'New Group',
    icon: Plus,
    description: 'Create a new group chat',
    category: 'Actions',
    compatibleSlots: [SLOT_IDS.SIDEBAR_MAIN],
    sizes: ['md'],
    defaultSize: 'md',
    color: '#5288c1',
  })
  .register({
    id: 'sidebar-privacy',
    name: 'Privacy & Security',
    icon: Shield,
    description: 'Privacy settings',
    category: 'Settings',
    compatibleSlots: [SLOT_IDS.SIDEBAR_MAIN],
    sizes: ['md'],
    defaultSize: 'md',
    color: '#7d8e9e',
  })
  .register({
    id: 'sidebar-starchat',
    name: 'Starred Chats',
    icon: Star,
    description: 'Pinned/starred conversations',
    category: 'Navigation',
    compatibleSlots: [SLOT_IDS.SIDEBAR_MAIN],
    sizes: ['md'],
    defaultSize: 'md',
    color: '#fb8c00',
  })

  // ── ChatInput
  .register({
    id: 'emoji-button',
    name: 'Emoji',
    icon: Smile,
    description: 'Open emoji picker',
    category: 'Chat',
    compatibleSlots: CHATINPUT_SLOTS,
    sizes: ['sm'],
    defaultSize: 'sm',
    color: '#fb8c00',
  })
  .register({
    id: 'attachment-button',
    name: 'Attachment',
    icon: Paperclip,
    description: 'Attach files or media',
    category: 'Chat',
    compatibleSlots: CHATINPUT_SLOTS,
    sizes: ['sm'],
    defaultSize: 'sm',
    color: '#5288c1',
  })
  .register({
    id: 'camera-button',
    name: 'Camera',
    icon: Camera,
    description: 'Take a photo or video',
    category: 'Chat',
    compatibleSlots: CHATINPUT_SLOTS,
    sizes: ['sm'],
    defaultSize: 'sm',
    color: '#7d8e9e',
  })
  .register({
    id: 'voice-button',
    name: 'Voice Message',
    icon: Mic,
    description: 'Record a voice message',
    category: 'Chat',
    compatibleSlots: [SLOT_IDS.CHATINPUT_RIGHT],
    sizes: ['sm', 'md'],
    defaultSize: 'sm',
    color: '#43a047',
  })
  .register({
    id: 'send-button',
    name: 'Send',
    icon: Send,
    description: 'Send message',
    category: 'Chat',
    compatibleSlots: [SLOT_IDS.CHATINPUT_RIGHT],
    sizes: ['sm', 'md'],
    defaultSize: 'md',
    color: '#5288c1',
  })
  .register({
    id: 'message-input',
    name: 'Message Input',
    icon: Edit3,
    description: 'Text composition field',
    category: 'Chat',
    compatibleSlots: [SLOT_IDS.CHATINPUT_CENTER],
    sizes: ['lg', 'xl'],
    defaultSize: 'xl',
    color: '#e8e8e8',
  })

  // ── ProfileScreen
  .register({
    id: 'profile-avatar',
    name: 'Avatar',
    icon: Users,
    description: "User's profile photo",
    category: 'Profile',
    compatibleSlots: [SLOT_IDS.PROFILE_AVATAR],
    sizes: ['sm', 'md', 'lg'],
    defaultSize: 'lg',
    color: '#5288c1',
  })
  .register({
    id: 'profile-bio',
    name: 'Bio Section',
    icon: Edit3,
    description: 'User biography and info',
    category: 'Profile',
    compatibleSlots: [SLOT_IDS.PROFILE_BIO],
    sizes: ['md'],
    defaultSize: 'md',
    color: '#7d8e9e',
  })
  .register({
    id: 'profile-stats',
    name: 'Stats Widget',
    icon: Globe,
    description: 'Messages, media, links counts',
    category: 'Profile',
    compatibleSlots: [SLOT_IDS.PROFILE_STATS],
    sizes: ['md', 'lg'],
    defaultSize: 'md',
    color: '#5288c1',
  })
  .register({
    id: 'profile-media-grid',
    name: 'Media Grid',
    icon: Camera,
    description: 'Shared photos and videos grid',
    category: 'Profile',
    compatibleSlots: [SLOT_IDS.PROFILE_MEDIA],
    sizes: ['lg', 'xl'],
    defaultSize: 'lg',
    color: '#7d8e9e',
  })
  .register({
    id: 'profile-mute-btn',
    name: 'Mute',
    icon: Volume2,
    description: 'Mute this user/chat',
    category: 'Profile',
    compatibleSlots: [SLOT_IDS.PROFILE_ACTIONS],
    sizes: ['sm', 'md'],
    defaultSize: 'md',
    color: '#fb8c00',
  })
  .register({
    id: 'profile-edit-btn',
    name: 'Edit Profile',
    icon: Edit3,
    description: 'Edit profile details',
    category: 'Profile',
    compatibleSlots: [SLOT_IDS.PROFILE_ACTIONS],
    sizes: ['sm', 'md'],
    defaultSize: 'md',
    color: '#5288c1',
  })
