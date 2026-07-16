package org.telegram.ui.Components.EditMode;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * EditModeManager — singleton that owns layout config state and notifies
 * listeners (overlay, activity, fragments) when edit mode toggles or layout changes.
 *
 * Usage:
 *   EditModeManager mgr = EditModeManager.getInstance(context);
 *   mgr.addListener(myListener);
 *   mgr.toggleEditMode();
 */
public class EditModeManager {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static EditModeManager instance;

    public static EditModeManager getInstance(Context context) {
        if (instance == null) {
            instance = new EditModeManager(context.getApplicationContext());
        }
        return instance;
    }

    // ── Listener ──────────────────────────────────────────────────────────────
    public interface Listener {
        void onEditModeChanged(boolean editMode);
        void onLayoutChanged(LayoutConfig config);
    }

    // ── State ─────────────────────────────────────────────────────────────────
    private final Context         appContext;
    private       LayoutConfig    config;
    private       boolean         editMode = false;
    private final List<Listener>  listeners = new ArrayList<>();

    private EditModeManager(Context context) {
        this.appContext = context;
        this.config     = LayoutConfig.load(context);
    }

    // ── Edit mode toggle ──────────────────────────────────────────────────────
    public boolean isEditMode() {
        return editMode;
    }

    public void enterEditMode() {
        if (editMode) return;
        editMode = true;
        notifyEditModeChanged();
    }

    public void exitEditMode() {
        if (!editMode) return;
        editMode = false;
        config.save(appContext);
        notifyEditModeChanged();
    }

    public void toggleEditMode() {
        if (editMode) exitEditMode(); else enterEditMode();
    }

    // ── Layout access ─────────────────────────────────────────────────────────
    public LayoutConfig getConfig() {
        return config;
    }

    /** Persist config immediately and notify all listeners. */
    public void applyConfig(LayoutConfig newConfig) {
        this.config = newConfig;
        config.save(appContext);
        notifyLayoutChanged();
    }

    /** Move a component between slots and notify listeners live. */
    public void moveComponent(String instanceId, String destSlotId, int destIndex) {
        config.moveToSlot(instanceId, destSlotId, destIndex);
        notifyLayoutChanged();
    }

    /** Reorder within a slot and notify listeners live. */
    public void reorderInSlot(String slotId, int fromIndex, int toIndex) {
        config.reorder(slotId, fromIndex, toIndex);
        notifyLayoutChanged();
    }

    /** Toggle visibility for a component instance. */
    public void toggleVisibility(String instanceId) {
        LayoutConfig.ComponentInstance inst = config.findInstance(instanceId);
        if (inst == null) return;
        inst.visible = !inst.visible;
        notifyLayoutChanged();
    }

    /** Toggle pinned state for a component instance. */
    public void togglePin(String instanceId) {
        LayoutConfig.ComponentInstance inst = config.findInstance(instanceId);
        if (inst == null) return;
        inst.pinned = !inst.pinned;
        notifyLayoutChanged();
    }

    /** Reset to default layout. */
    public void resetLayout() {
        config = new LayoutConfig();
        config.save(appContext);
        notifyLayoutChanged();
    }

    /** Export layout JSON string (for copy/sharing). */
    public String exportJson() {
        return config.toJson();
    }

    /** Import layout from JSON string. Returns true on success. */
    public boolean importJson(String json) {
        try {
            LayoutConfig imported = LayoutConfig.fromJson(json);
            if (imported == null) return false;
            applyConfig(imported);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Listener management ───────────────────────────────────────────────────
    public void addListener(Listener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    private void notifyEditModeChanged() {
        for (Listener l : new ArrayList<>(listeners)) l.onEditModeChanged(editMode);
    }

    private void notifyLayoutChanged() {
        for (Listener l : new ArrayList<>(listeners)) l.onLayoutChanged(config);
    }
}
