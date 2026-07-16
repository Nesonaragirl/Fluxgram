package org.telegram.ui.Components.EditMode;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * FluxGram Layout Configuration
 *
 * JSON schema (stored in SharedPreferences under "fluxgram_layout"):
 * {
 *   "version": "1.0",
 *   "schemaVersion": 1,
 *   "deviceId": "<uuid>",
 *   "lastModified": <unix-ms>,
 *   "slots": {
 *     "TopBar.Left":    [ { "instanceId": "...", "componentId": "...", "visible": true, "pinned": false, "order": 0 }, ... ],
 *     "TopBar.Center":  [ ... ],
 *     "TopBar.Right":   [ ... ],
 *     "BottomBar.Slot1": [ ... ],
 *     "BottomBar.Slot2": [ ... ],
 *     "BottomBar.Slot3": [ ... ],
 *     "BottomBar.Slot4": [ ... ],
 *     "BottomBar.Slot5": [ ... ],
 *     "ChatInput.Left":   [ ... ],
 *     "ChatInput.Center": [ ... ],
 *     "ChatInput.Right":  [ ... ],
 *     "Sidebar.Main":     [ ... ]
 *   }
 * }
 *
 * This JSON is the single source of truth for layout state and is sync-ready
 * (deviceId + lastModified enable last-write-wins conflict resolution).
 */
public class LayoutConfig {

    // ── Slot IDs ──────────────────────────────────────────────────────────────
    public static final String SLOT_TOPBAR_LEFT     = "TopBar.Left";
    public static final String SLOT_TOPBAR_CENTER   = "TopBar.Center";
    public static final String SLOT_TOPBAR_RIGHT    = "TopBar.Right";
    public static final String SLOT_BOTTOMBAR_1     = "BottomBar.Slot1";
    public static final String SLOT_BOTTOMBAR_2     = "BottomBar.Slot2";
    public static final String SLOT_BOTTOMBAR_3     = "BottomBar.Slot3";
    public static final String SLOT_BOTTOMBAR_4     = "BottomBar.Slot4";
    public static final String SLOT_BOTTOMBAR_5     = "BottomBar.Slot5";
    public static final String SLOT_CHATINPUT_LEFT   = "ChatInput.Left";
    public static final String SLOT_CHATINPUT_CENTER = "ChatInput.Center";
    public static final String SLOT_CHATINPUT_RIGHT  = "ChatInput.Right";
    public static final String SLOT_SIDEBAR_MAIN    = "Sidebar.Main";

    public static final String[] ALL_SLOTS = {
        SLOT_TOPBAR_LEFT, SLOT_TOPBAR_CENTER, SLOT_TOPBAR_RIGHT,
        SLOT_BOTTOMBAR_1, SLOT_BOTTOMBAR_2, SLOT_BOTTOMBAR_3, SLOT_BOTTOMBAR_4, SLOT_BOTTOMBAR_5,
        SLOT_CHATINPUT_LEFT, SLOT_CHATINPUT_CENTER, SLOT_CHATINPUT_RIGHT,
        SLOT_SIDEBAR_MAIN,
    };

    // ── Component IDs (built-in) ───────────────────────────────────────────────
    public static final String COMP_BACK_BUTTON     = "back-button";
    public static final String COMP_SEARCH          = "search-button";
    public static final String COMP_MENU            = "menu-button";
    public static final String COMP_MORE_OPTIONS    = "more-button";
    public static final String COMP_VOICE_CALL      = "call-button";
    public static final String COMP_VIDEO_CALL      = "video-call-button";
    public static final String COMP_NAV_CHATS       = "nav-chats";
    public static final String COMP_NAV_CONTACTS    = "nav-contacts";
    public static final String COMP_NAV_CALLS       = "nav-calls";
    public static final String COMP_NAV_SETTINGS    = "nav-settings";
    public static final String COMP_NAV_PROFILE     = "nav-profile";
    public static final String COMP_EMOJI           = "emoji-button";
    public static final String COMP_ATTACHMENT      = "attachment-button";
    public static final String COMP_CAMERA          = "camera-button";
    public static final String COMP_VOICE_MSG       = "voice-button";
    public static final String COMP_SEND            = "send-button";
    public static final String COMP_BOT             = "bot-button";
    public static final String COMP_SCHEDULE        = "schedule-button";
    public static final String COMP_SIDEBAR_NEW_GROUP    = "sidebar-newgroup";
    public static final String COMP_SIDEBAR_CONTACTS     = "sidebar-contacts";
    public static final String COMP_SIDEBAR_SAVED        = "sidebar-saved";
    public static final String COMP_SIDEBAR_SETTINGS     = "sidebar-settings";
    public static final String COMP_SIDEBAR_ARCHIVE      = "sidebar-archive";

    // ── Storage ───────────────────────────────────────────────────────────────
    private static final String PREFS_NAME   = "fluxgram_layout";
    private static final String PREFS_KEY    = "layout_json";
    private static final int    SCHEMA_VER   = 1;

    // ── Data model ────────────────────────────────────────────────────────────
    public static class ComponentInstance {
        public String instanceId;
        public String componentId;
        public boolean visible;
        public boolean pinned;
        public int order;

        public ComponentInstance(String componentId) {
            this.instanceId  = UUID.randomUUID().toString().substring(0, 8);
            this.componentId = componentId;
            this.visible     = true;
            this.pinned      = false;
            this.order       = 0;
        }

        public ComponentInstance(JSONObject obj) throws JSONException {
            this.instanceId  = obj.getString("instanceId");
            this.componentId = obj.getString("componentId");
            this.visible     = obj.optBoolean("visible", true);
            this.pinned      = obj.optBoolean("pinned", false);
            this.order       = obj.optInt("order", 0);
        }

        public JSONObject toJson() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("instanceId",  instanceId);
            o.put("componentId", componentId);
            o.put("visible",     visible);
            o.put("pinned",      pinned);
            o.put("order",       order);
            return o;
        }
    }

    // ── Live state ────────────────────────────────────────────────────────────
    private final android.util.ArrayMap<String, List<ComponentInstance>> slots = new android.util.ArrayMap<>();
    public  String deviceId;
    public  long   lastModified;

    // ── Constructor ───────────────────────────────────────────────────────────
    public LayoutConfig() {
        deviceId     = UUID.randomUUID().toString().substring(0, 12);
        lastModified = System.currentTimeMillis();
        buildDefaults();
    }

    private void buildDefaults() {
        // TopBar
        setSlot(SLOT_TOPBAR_LEFT,   list(new ComponentInstance(COMP_BACK_BUTTON), new ComponentInstance(COMP_MENU)));
        setSlot(SLOT_TOPBAR_CENTER, list());
        setSlot(SLOT_TOPBAR_RIGHT,  list(new ComponentInstance(COMP_SEARCH), new ComponentInstance(COMP_VOICE_CALL), new ComponentInstance(COMP_VIDEO_CALL), new ComponentInstance(COMP_MORE_OPTIONS)));
        // BottomBar
        ComponentInstance navChats     = new ComponentInstance(COMP_NAV_CHATS);     navChats.pinned = true;
        ComponentInstance navContacts  = new ComponentInstance(COMP_NAV_CONTACTS);
        ComponentInstance navCalls     = new ComponentInstance(COMP_NAV_CALLS);
        ComponentInstance navSettings  = new ComponentInstance(COMP_NAV_SETTINGS);
        ComponentInstance navProfile   = new ComponentInstance(COMP_NAV_PROFILE);
        setSlot(SLOT_BOTTOMBAR_1, list(navChats));
        setSlot(SLOT_BOTTOMBAR_2, list(navContacts));
        setSlot(SLOT_BOTTOMBAR_3, list(navCalls));
        setSlot(SLOT_BOTTOMBAR_4, list(navSettings));
        setSlot(SLOT_BOTTOMBAR_5, list(navProfile));
        // ChatInput
        setSlot(SLOT_CHATINPUT_LEFT,   list(new ComponentInstance(COMP_EMOJI), new ComponentInstance(COMP_ATTACHMENT)));
        setSlot(SLOT_CHATINPUT_CENTER, list());
        setSlot(SLOT_CHATINPUT_RIGHT,  list(new ComponentInstance(COMP_CAMERA), new ComponentInstance(COMP_VOICE_MSG), new ComponentInstance(COMP_SEND)));
        // Sidebar
        setSlot(SLOT_SIDEBAR_MAIN, list(
            new ComponentInstance(COMP_SIDEBAR_NEW_GROUP),
            new ComponentInstance(COMP_SIDEBAR_CONTACTS),
            new ComponentInstance(COMP_SIDEBAR_SAVED),
            new ComponentInstance(COMP_SIDEBAR_ARCHIVE),
            new ComponentInstance(COMP_SIDEBAR_SETTINGS)
        ));
    }

    // ── Slot access ───────────────────────────────────────────────────────────
    public List<ComponentInstance> getSlot(String slotId) {
        List<ComponentInstance> s = slots.get(slotId);
        return s != null ? s : new ArrayList<>();
    }

    public void setSlot(String slotId, List<ComponentInstance> items) {
        for (int i = 0; i < items.size(); i++) items.get(i).order = i;
        slots.put(slotId, items);
        lastModified = System.currentTimeMillis();
    }

    /** Find which slot contains a given instanceId. Returns null if not found. */
    public String findSlotForInstance(String instanceId) {
        for (String slotId : ALL_SLOTS) {
            for (ComponentInstance inst : getSlot(slotId)) {
                if (inst.instanceId.equals(instanceId)) return slotId;
            }
        }
        return null;
    }

    /** Find a ComponentInstance by instanceId. */
    public ComponentInstance findInstance(String instanceId) {
        for (String slotId : ALL_SLOTS) {
            for (ComponentInstance inst : getSlot(slotId)) {
                if (inst.instanceId.equals(instanceId)) return inst;
            }
        }
        return null;
    }

    /** Move instance to a different slot at a given index. */
    public void moveToSlot(String instanceId, String destSlotId, int destIndex) {
        String srcSlotId = findSlotForInstance(instanceId);
        if (srcSlotId == null) return;

        ComponentInstance inst = null;
        List<ComponentInstance> srcList = new ArrayList<>(getSlot(srcSlotId));
        for (int i = 0; i < srcList.size(); i++) {
            if (srcList.get(i).instanceId.equals(instanceId)) {
                inst = srcList.remove(i);
                break;
            }
        }
        if (inst == null) return;
        setSlot(srcSlotId, srcList);

        List<ComponentInstance> destList = new ArrayList<>(getSlot(destSlotId));
        int insertAt = Math.min(destIndex, destList.size());
        destList.add(insertAt, inst);
        setSlot(destSlotId, destList);
    }

    /** Reorder within the same slot. */
    public void reorder(String slotId, int fromIndex, int toIndex) {
        List<ComponentInstance> list = new ArrayList<>(getSlot(slotId));
        if (fromIndex < 0 || fromIndex >= list.size() || toIndex < 0 || toIndex >= list.size()) return;
        ComponentInstance moved = list.remove(fromIndex);
        list.add(toIndex, moved);
        setSlot(slotId, list);
    }

    // ── Serialization ─────────────────────────────────────────────────────────
    public String toJson() {
        try {
            JSONObject root = new JSONObject();
            root.put("version",       "1.0");
            root.put("schemaVersion", SCHEMA_VER);
            root.put("deviceId",      deviceId);
            root.put("lastModified",  lastModified);

            JSONObject slotsObj = new JSONObject();
            for (String slotId : ALL_SLOTS) {
                JSONArray arr = new JSONArray();
                for (ComponentInstance inst : getSlot(slotId)) arr.put(inst.toJson());
                slotsObj.put(slotId, arr);
            }
            root.put("slots", slotsObj);
            return root.toString(2);
        } catch (JSONException e) {
            return "{}";
        }
    }

    public static LayoutConfig fromJson(String json) {
        LayoutConfig config = new LayoutConfig();
        try {
            JSONObject root = new JSONObject(json);
            if (root.optInt("schemaVersion", 0) != SCHEMA_VER) return config; // version mismatch → use defaults
            config.deviceId     = root.optString("deviceId", config.deviceId);
            config.lastModified = root.optLong("lastModified", config.lastModified);

            JSONObject slotsObj = root.optJSONObject("slots");
            if (slotsObj != null) {
                for (String slotId : ALL_SLOTS) {
                    JSONArray arr = slotsObj.optJSONArray(slotId);
                    if (arr == null) continue;
                    List<ComponentInstance> items = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        items.add(new ComponentInstance(arr.getJSONObject(i)));
                    }
                    config.slots.put(slotId, items);
                }
            }
        } catch (JSONException ignored) {}
        return config;
    }

    // ── Persistence ───────────────────────────────────────────────────────────
    public void save(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
               .edit().putString(PREFS_KEY, toJson()).apply();
    }

    public static LayoutConfig load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(PREFS_KEY, null);
        return (json != null) ? fromJson(json) : new LayoutConfig();
    }

    public static void reset(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().remove(PREFS_KEY).apply();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    @SafeVarargs
    private static <T> List<T> list(T... items) {
        List<T> l = new ArrayList<>();
        for (T item : items) l.add(item);
        return l;
    }
}
