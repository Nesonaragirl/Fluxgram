/**
 * Sample Plugin Registrations
 *
 * These demonstrate the plugin API: a plugin calls registry.register() with
 * a component definition. The user then decides — through the Edit Mode UI —
 * whether to show it, where it appears, and how large it is.
 *
 * Plugins have NO say in placement. They only define what the component IS,
 * what slots it's compatible with, and what it renders.
 */

import { Bot, Languages, Download, Zap, Globe, Wand2 } from 'lucide-react'
import { registry, SLOT_IDS } from '../store/componentRegistry'

const TOPBAR_SLOTS = [SLOT_IDS.TOPBAR_LEFT, SLOT_IDS.TOPBAR_CENTER, SLOT_IDS.TOPBAR_RIGHT]
const CHATINPUT_SLOTS = [SLOT_IDS.CHATINPUT_LEFT, SLOT_IDS.CHATINPUT_CENTER, SLOT_IDS.CHATINPUT_RIGHT]
const BOTTOMBAR_SLOTS = [SLOT_IDS.BOTTOMBAR_1, SLOT_IDS.BOTTOMBAR_2, SLOT_IDS.BOTTOMBAR_3, SLOT_IDS.BOTTOMBAR_4, SLOT_IDS.BOTTOMBAR_5]
const PROFILE_ACTIONS = [SLOT_IDS.PROFILE_ACTIONS]

// ── Plugin 1: AI Summary ──────────────────────────────────────────────────────
registry.register({
  id: 'plugin-ai-summary',
  name: 'AI Summary',
  icon: Bot,
  description: 'Summarize long conversations with AI in one tap.',
  category: 'AI',
  plugin: true,
  pluginMeta: {
    author: 'FluxAI Labs',
    version: '1.2.0',
    homepage: 'https://fluxgram.app/plugins/ai-summary',
  },
  compatibleSlots: [...TOPBAR_SLOTS, ...CHATINPUT_SLOTS, ...PROFILE_ACTIONS],
  sizes: ['sm', 'md'],
  defaultSize: 'sm',
  color: '#a855f7',
  accentColor: '#7c3aed',
})

// ── Plugin 2: Translator ──────────────────────────────────────────────────────
registry.register({
  id: 'plugin-translator',
  name: 'Translator',
  icon: Languages,
  description: 'Auto-translate incoming messages to your preferred language.',
  category: 'Language',
  plugin: true,
  pluginMeta: {
    author: 'LinguaFlex',
    version: '2.0.1',
    homepage: 'https://fluxgram.app/plugins/translator',
  },
  compatibleSlots: [...TOPBAR_SLOTS, ...CHATINPUT_SLOTS, SLOT_IDS.SIDEBAR_MAIN],
  sizes: ['sm', 'md'],
  defaultSize: 'sm',
  color: '#06b6d4',
  accentColor: '#0891b2',
})

// ── Plugin 3: Download Manager ────────────────────────────────────────────────
registry.register({
  id: 'plugin-download-manager',
  name: 'Download Manager',
  icon: Download,
  description: 'Queue and manage all media downloads in one panel.',
  category: 'Utility',
  plugin: true,
  pluginMeta: {
    author: 'FluxUtils',
    version: '0.9.4',
    homepage: 'https://fluxgram.app/plugins/downloads',
  },
  compatibleSlots: [...TOPBAR_SLOTS, ...BOTTOMBAR_SLOTS, SLOT_IDS.SIDEBAR_MAIN],
  sizes: ['sm', 'md', 'lg'],
  defaultSize: 'md',
  color: '#10b981',
  accentColor: '#059669',
})

// ── Plugin 4: Quick Actions (bonus) ──────────────────────────────────────────
registry.register({
  id: 'plugin-quick-actions',
  name: 'Quick Actions',
  icon: Zap,
  description: 'Customizable shortcuts to your most-used features.',
  category: 'Productivity',
  plugin: true,
  pluginMeta: {
    author: 'SpeedDesk',
    version: '1.0.0',
    homepage: 'https://fluxgram.app/plugins/quick-actions',
  },
  compatibleSlots: [...TOPBAR_SLOTS, ...BOTTOMBAR_SLOTS, ...CHATINPUT_SLOTS],
  sizes: ['sm', 'md'],
  defaultSize: 'sm',
  color: '#f59e0b',
  accentColor: '#d97706',
})
