package org.telegram.ui.Components.EditMode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.telegram.messenger.AndroidUtilities.dp;

/**
 * EditModeOverlayView
 *
 * Full-screen overlay rendered on top of the real Telegram UI when edit mode is active.
 * Draws translucent slot containers over the four customizable regions of the app:
 *   • TopBar (left / center / right zones)
 *   • BottomBar (5 slots)
 *   • ChatInput (left / center / right zones)
 *   • Sidebar
 *
 * Each slot region:
 *   - Shows its contained component chips with a drag handle
 *   - Highlights on drag-over
 *   - Provides per-chip controls: show/hide, pin, remove, reorder
 *
 * Interaction model:
 *   - Long-press a chip to start dragging (View.startDragAndDrop)
 *   - Drop on any compatible slot region to move it there
 *   - Tap a chip to select and show its control panel
 *   - Tap outside to deselect
 */
public class EditModeOverlayView extends FrameLayout implements EditModeManager.Listener {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final int COLOR_SLOT_BG        = 0x18F59E0B;  // amber tint
    private static final int COLOR_SLOT_BORDER     = 0x88F59E0B;
    private static final int COLOR_SLOT_HOVER      = 0x30F59E0B;
    private static final int COLOR_CHIP_BG         = 0xCC1E2C3A;
    private static final int COLOR_CHIP_BORDER     = 0xFF2B5278;
    private static final int COLOR_CHIP_SELECTED   = 0xFF5288C1;
    private static final int COLOR_CHIP_PINNED     = 0xFF43A047;
    private static final int COLOR_TEXT            = 0xFFE8E8E8;
    private static final int COLOR_MUTED           = 0xFF7D8E9E;
    private static final int COLOR_PANEL_BG        = 0xFF17212B;
    private static final int CHIP_H                = 28;   // dp
    private static final int CHIP_PADDING          = 6;    // dp
    private static final int SLOT_PADDING          = 8;    // dp
    private static final int PANEL_H               = 44;   // dp  (control panel)

    // ── Paints ────────────────────────────────────────────────────────────────
    private final Paint slotBgPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint slotBorderPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint chipBgPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint chipBorderPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint        = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mutedPaint       = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayDimPaint  = new Paint();
    private final RectF rect             = new RectF();

    // ── State ─────────────────────────────────────────────────────────────────
    private final EditModeManager   mgr;
    private       float             alpha       = 0f;   // 0→1 animation
    private       String            selectedId  = null; // selected instanceId
    private       String            dragId      = null; // currently dragged instanceId
    private       String            hoverSlotId = null; // slot being hovered over during drag

    // Slot → screen position mapping (populated in onLayout / from host)
    private final Map<String, RectF> slotRects = new LinkedHashMap<>();

    // Chip touch tracking
    private float touchStartX, touchStartY;
    private static final int LONG_PRESS_TIMEOUT_MS = 400;
    private final Runnable longPressRunnable = this::startChipDrag;
    private String touchedInstanceId = null;

    // ── Constructor ───────────────────────────────────────────────────────────
    public EditModeOverlayView(Context context) {
        super(context);
        mgr = EditModeManager.getInstance(context);
        mgr.addListener(this);

        setWillNotDraw(false);
        setVisibility(View.GONE);
        setClickable(true); // intercept taps

        slotBgPaint.setColor(COLOR_SLOT_BG);
        slotBorderPaint.setColor(COLOR_SLOT_BORDER);
        slotBorderPaint.setStyle(Paint.Style.STROKE);
        slotBorderPaint.setStrokeWidth(dp(1));
        chipBgPaint.setColor(COLOR_CHIP_BG);
        chipBorderPaint.setColor(COLOR_CHIP_BORDER);
        chipBorderPaint.setStyle(Paint.Style.STROKE);
        chipBorderPaint.setStrokeWidth(dp(1));
        textPaint.setColor(COLOR_TEXT);
        textPaint.setTextSize(dp(11));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mutedPaint.setColor(COLOR_MUTED);
        mutedPaint.setTextSize(dp(9));
        overlayDimPaint.setColor(0x60000000);

        buildDefaultSlotRects();
        addControlPanel();
    }

    // ── Slot rect registration (called by host with actual view bounds) ────────
    /**
     * Register the screen rect for a named slot so the overlay can draw over it.
     * Call this from onLayout / onGlobalLayout in LaunchActivity / fragments.
     */
    public void registerSlotRect(String slotId, int left, int top, int right, int bottom) {
        slotRects.put(slotId, new RectF(left, top, right, bottom));
        invalidate();
    }

    /** Default rects based on a typical portrait phone layout (approximate). */
    private void buildDefaultSlotRects() {
        // These are overridden by registerSlotRect() once the real views lay out.
        // Values here are in px estimates for a 1080-wide portrait screen.
        // They serve as fallback so the overlay renders immediately on entry.
    }

    /**
     * Call from the host after layout to auto-compute slot positions from
     * real view references.
     *
     * Example:
     *   overlay.setSlotView(LayoutConfig.SLOT_TOPBAR_LEFT, myActionBarLeftGroup);
     */
    public void setSlotView(String slotId, View view) {
        if (view == null) return;
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        int[] myLoc = new int[2];
        getLocationOnScreen(myLoc);
        registerSlotRect(
            slotId,
            loc[0] - myLoc[0],
            loc[1] - myLoc[1],
            loc[0] + view.getWidth() - myLoc[0],
            loc[1] + view.getHeight() - myLoc[1]
        );
    }

    // ── Control panel (selected chip controls) ────────────────────────────────
    private LinearLayout controlPanel;
    private TextView      ctrlLabel;
    private TextView      ctrlToggleVisible;
    private TextView      ctrlTogglePin;
    private TextView      ctrlRemove;

    private void addControlPanel() {
        controlPanel = new LinearLayout(getContext());
        controlPanel.setOrientation(LinearLayout.HORIZONTAL);
        controlPanel.setGravity(Gravity.CENTER_VERTICAL);
        controlPanel.setBackgroundColor(COLOR_PANEL_BG);
        controlPanel.setPadding(dp(12), dp(8), dp(12), dp(8));
        controlPanel.setVisibility(View.GONE);

        // Label
        ctrlLabel = makeTextView(12, true, COLOR_TEXT);
        ctrlLabel.setMaxWidth(dp(100));
        ctrlLabel.setEllipsize(TextUtils.TruncateAt.END);
        ctrlLabel.setSingleLine(true);
        controlPanel.addView(ctrlLabel, linearParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        controlPanel.addView(makeDivider(), linearParams(dp(1), dp(18), 0f));

        // Toggle visibility
        ctrlToggleVisible = makeControlButton("👁 Show/Hide");
        ctrlToggleVisible.setOnClickListener(v -> {
            if (selectedId != null) {
                mgr.toggleVisibility(selectedId);
                refreshControlPanel();
                invalidate();
            }
        });
        controlPanel.addView(ctrlToggleVisible, linearParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));

        // Toggle pin
        ctrlTogglePin = makeControlButton("📌 Pin");
        ctrlTogglePin.setOnClickListener(v -> {
            if (selectedId != null) {
                mgr.togglePin(selectedId);
                refreshControlPanel();
                invalidate();
            }
        });
        controlPanel.addView(ctrlTogglePin, linearParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));

        // Remove
        ctrlRemove = makeControlButton("🗑 Remove");
        ctrlRemove.setTextColor(0xFFE53935);
        ctrlRemove.setOnClickListener(v -> {
            if (selectedId != null) {
                String slotId = mgr.getConfig().findSlotForInstance(selectedId);
                if (slotId != null) {
                    LayoutConfig cfg = mgr.getConfig();
                    List<LayoutConfig.ComponentInstance> items = new ArrayList<>(cfg.getSlot(slotId));
                    items.removeIf(i -> i.instanceId.equals(selectedId));
                    cfg.setSlot(slotId, items);
                    mgr.applyConfig(cfg);
                }
                selectedId = null;
                controlPanel.setVisibility(View.GONE);
                invalidate();
            }
        });
        controlPanel.addView(ctrlRemove, linearParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(PANEL_H));
        lp.gravity = Gravity.BOTTOM;
        addView(controlPanel, lp);
    }

    private void refreshControlPanel() {
        if (selectedId == null) { controlPanel.setVisibility(View.GONE); return; }
        LayoutConfig.ComponentInstance inst = mgr.getConfig().findInstance(selectedId);
        if (inst == null) { controlPanel.setVisibility(View.GONE); return; }

        controlPanel.setVisibility(View.VISIBLE);
        ctrlLabel.setText(componentLabel(inst.componentId));
        ctrlToggleVisible.setText(inst.visible ? "🙈 Hide" : "👁 Show");
        ctrlTogglePin.setText(inst.pinned ? "📌 Unpin" : "📌 Pin");
    }

    // ── Drawing ───────────────────────────────────────────────────────────────
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (alpha <= 0f) return;

        // Dim the underlying UI slightly
        overlayDimPaint.setAlpha((int) (0x60 * alpha));
        canvas.drawRect(0, 0, getWidth(), getHeight(), overlayDimPaint);

        // Draw each registered slot region
        for (Map.Entry<String, RectF> entry : slotRects.entrySet()) {
            drawSlot(canvas, entry.getKey(), entry.getValue());
        }

        super.dispatchDraw(canvas); // draw control panel on top
    }

    private void drawSlot(Canvas canvas, String slotId, RectF r) {
        boolean isHover = slotId.equals(hoverSlotId);
        float corner = dp(10);

        // Background
        slotBgPaint.setColor(isHover ? COLOR_SLOT_HOVER : COLOR_SLOT_BG);
        slotBgPaint.setAlpha((int) (slotBgPaint.getAlpha() * alpha));
        canvas.drawRoundRect(r, corner, corner, slotBgPaint);

        // Border
        slotBorderPaint.setAlpha((int) (0x88 * alpha));
        if (isHover) slotBorderPaint.setColor(0xFFF59E0B);
        else         slotBorderPaint.setColor(COLOR_SLOT_BORDER);
        canvas.drawRoundRect(r, corner, corner, slotBorderPaint);

        // Slot label
        String label = slotLabel(slotId);
        mutedPaint.setAlpha((int) (0xCC * alpha));
        canvas.drawText(label, r.left + dp(SLOT_PADDING), r.top + dp(SLOT_PADDING) + mutedPaint.getTextSize(), mutedPaint);

        // Component chips
        List<LayoutConfig.ComponentInstance> items = mgr.getConfig().getSlot(slotId);
        float chipX = r.left + dp(SLOT_PADDING);
        float chipY = r.top + dp(SLOT_PADDING) + mutedPaint.getTextSize() + dp(4);
        float chipH  = dp(CHIP_H);

        for (int i = 0; i < items.size(); i++) {
            LayoutConfig.ComponentInstance inst = items.get(i);
            String chipText = "⠿ " + componentLabel(inst.componentId);
            float chipW = textPaint.measureText(chipText) + dp(CHIP_PADDING * 2);

            // Wrap if needed
            if (chipX + chipW > r.right - dp(SLOT_PADDING)) {
                chipX = r.left + dp(SLOT_PADDING);
                chipY += chipH + dp(4);
            }

            rect.set(chipX, chipY, chipX + chipW, chipY + chipH);

            // Chip background
            boolean selected = inst.instanceId.equals(selectedId);
            boolean dragged  = inst.instanceId.equals(dragId);
            if (dragged) {
                chipBgPaint.setColor(0x80F59E0B);
            } else if (selected) {
                chipBgPaint.setColor(0xFF1E2C3A);
                chipBorderPaint.setColor(COLOR_CHIP_SELECTED);
            } else if (inst.pinned) {
                chipBgPaint.setColor(0xFF1E2C3A);
                chipBorderPaint.setColor(COLOR_CHIP_PINNED);
            } else {
                chipBgPaint.setColor(COLOR_CHIP_BG);
                chipBorderPaint.setColor(COLOR_CHIP_BORDER);
            }
            chipBgPaint.setAlpha(dragged ? 128 : (int) (0xCC * alpha));
            canvas.drawRoundRect(rect, dp(6), dp(6), chipBgPaint);
            chipBorderPaint.setAlpha((int) (0xFF * alpha));
            canvas.drawRoundRect(rect, dp(6), dp(6), chipBorderPaint);

            // Chip text
            textPaint.setColor(inst.visible ? COLOR_TEXT : COLOR_MUTED);
            textPaint.setAlpha(dragged ? 80 : (int) (0xFF * alpha));
            float textY = chipY + (chipH - textPaint.getTextSize()) / 2f + textPaint.getTextSize();
            canvas.drawText(chipText, chipX + dp(CHIP_PADDING), textY, textPaint);

            // Hidden badge
            if (!inst.visible) {
                mutedPaint.setAlpha((int) (0xAA * alpha));
                canvas.drawText("hidden", rect.right - dp(30), rect.bottom - dp(3), mutedPaint);
            }
            // Pin badge
            if (inst.pinned) {
                mutedPaint.setAlpha((int) (0xAA * alpha));
                canvas.drawText("📌", rect.right - dp(14), rect.top + dp(10), mutedPaint);
            }

            chipX += chipW + dp(6);
        }

        // Empty slot hint
        if (items.isEmpty()) {
            mutedPaint.setAlpha((int) (0x66 * alpha));
            canvas.drawText("+ drop here", r.left + dp(SLOT_PADDING), r.centerY() + mutedPaint.getTextSize() / 2f, mutedPaint);
        }
    }

    // ── Touch / drag handling ─────────────────────────────────────────────────
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = x;
                touchStartY = y;
                touchedInstanceId = findChipAt(x, y);
                if (touchedInstanceId != null) {
                    postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT_MS);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - touchStartX);
                float dy = Math.abs(y - touchStartY);
                if (dx > dp(8) || dy > dp(8)) {
                    removeCallbacks(longPressRunnable);
                    if (touchedInstanceId != null && !touchedInstanceId.equals(dragId)) {
                        startChipDrag();
                    }
                }
                if (dragId != null) {
                    hoverSlotId = findSlotAt(x, y);
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                removeCallbacks(longPressRunnable);
                if (dragId != null) {
                    String destSlot = findSlotAt(x, y);
                    if (destSlot != null) {
                        int destIdx = estimateDropIndex(destSlot, x, y);
                        mgr.moveComponent(dragId, destSlot, destIdx);
                        invalidate();
                    }
                    dragId      = null;
                    hoverSlotId = null;
                    invalidate();
                } else if (touchedInstanceId != null) {
                    // Tap → select
                    selectedId = touchedInstanceId.equals(selectedId) ? null : touchedInstanceId;
                    refreshControlPanel();
                    invalidate();
                } else {
                    // Tap on empty space → deselect
                    selectedId = null;
                    refreshControlPanel();
                    invalidate();
                }
                touchedInstanceId = null;
                return true;

            case MotionEvent.ACTION_CANCEL:
                removeCallbacks(longPressRunnable);
                dragId = null; hoverSlotId = null; touchedInstanceId = null;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void startChipDrag() {
        if (touchedInstanceId == null) return;
        LayoutConfig.ComponentInstance inst = mgr.getConfig().findInstance(touchedInstanceId);
        if (inst == null || inst.pinned) { touchedInstanceId = null; return; }
        dragId = touchedInstanceId;
        performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
        invalidate();
    }

    // ── Hit testing ───────────────────────────────────────────────────────────
    /** Returns the instanceId of the chip at (x, y), or null. */
    private String findChipAt(float x, float y) {
        for (Map.Entry<String, RectF> entry : slotRects.entrySet()) {
            String slotId = entry.getKey();
            RectF  r      = entry.getValue();
            if (!r.contains(x, y)) continue;

            List<LayoutConfig.ComponentInstance> items = mgr.getConfig().getSlot(slotId);
            float chipX = r.left + dp(SLOT_PADDING);
            float chipY = r.top + dp(SLOT_PADDING) + mutedPaint.getTextSize() + dp(4);
            float chipH = dp(CHIP_H);

            for (LayoutConfig.ComponentInstance inst : items) {
                String chipText = "⠿ " + componentLabel(inst.componentId);
                float  chipW    = textPaint.measureText(chipText) + dp(CHIP_PADDING * 2);
                if (chipX + chipW > r.right - dp(SLOT_PADDING)) {
                    chipX = r.left + dp(SLOT_PADDING);
                    chipY += chipH + dp(4);
                }
                rect.set(chipX, chipY, chipX + chipW, chipY + chipH);
                if (rect.contains(x, y)) return inst.instanceId;
                chipX += chipW + dp(6);
            }
        }
        return null;
    }

    /** Returns the slotId whose rect contains (x, y), or null. */
    private String findSlotAt(float x, float y) {
        for (Map.Entry<String, RectF> entry : slotRects.entrySet()) {
            if (entry.getValue().contains(x, y)) return entry.getKey();
        }
        return null;
    }

    /** Estimate drop position index within a slot based on x position. */
    private int estimateDropIndex(String slotId, float x, float y) {
        List<LayoutConfig.ComponentInstance> items = mgr.getConfig().getSlot(slotId);
        if (items.isEmpty()) return 0;
        RectF r = slotRects.get(slotId);
        if (r == null) return items.size();
        float fraction = (x - r.left) / r.width();
        return Math.min((int) (fraction * items.size() + 0.5f), items.size());
    }

    // ── EditModeManager.Listener ──────────────────────────────────────────────
    @Override
    public void onEditModeChanged(boolean editMode) {
        if (editMode) {
            setVisibility(View.VISIBLE);
            animateAlpha(0f, 1f);
        } else {
            animateAlpha(1f, 0f, () -> {
                setVisibility(View.GONE);
                selectedId = null;
                controlPanel.setVisibility(View.GONE);
            });
        }
    }

    @Override
    public void onLayoutChanged(LayoutConfig config) {
        invalidate();
        refreshControlPanel();
    }

    // ── Alpha animation ───────────────────────────────────────────────────────
    private void animateAlpha(float from, float to) {
        animateAlpha(from, to, null);
    }

    private void animateAlpha(float from, float to, Runnable onEnd) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.setDuration(200);
        anim.addUpdateListener(a -> { alpha = (float) a.getAnimatedValue(); invalidate(); });
        if (onEnd != null) anim.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator a) { onEnd.run(); }
        });
        anim.start();
    }

    // ── Labels ────────────────────────────────────────────────────────────────
    private static String slotLabel(String slotId) {
        switch (slotId) {
            case LayoutConfig.SLOT_TOPBAR_LEFT:      return "Top Bar · Left";
            case LayoutConfig.SLOT_TOPBAR_CENTER:    return "Top Bar · Center";
            case LayoutConfig.SLOT_TOPBAR_RIGHT:     return "Top Bar · Right";
            case LayoutConfig.SLOT_BOTTOMBAR_1:      return "Bottom · Slot 1";
            case LayoutConfig.SLOT_BOTTOMBAR_2:      return "Bottom · Slot 2";
            case LayoutConfig.SLOT_BOTTOMBAR_3:      return "Bottom · Slot 3";
            case LayoutConfig.SLOT_BOTTOMBAR_4:      return "Bottom · Slot 4";
            case LayoutConfig.SLOT_BOTTOMBAR_5:      return "Bottom · Slot 5";
            case LayoutConfig.SLOT_CHATINPUT_LEFT:   return "Input · Left";
            case LayoutConfig.SLOT_CHATINPUT_CENTER: return "Input · Center";
            case LayoutConfig.SLOT_CHATINPUT_RIGHT:  return "Input · Right";
            case LayoutConfig.SLOT_SIDEBAR_MAIN:     return "Sidebar";
            default:                                 return slotId;
        }
    }

    private static String componentLabel(String compId) {
        switch (compId) {
            case LayoutConfig.COMP_BACK_BUTTON:   return "Back";
            case LayoutConfig.COMP_SEARCH:        return "Search";
            case LayoutConfig.COMP_MENU:          return "Menu";
            case LayoutConfig.COMP_MORE_OPTIONS:  return "More";
            case LayoutConfig.COMP_VOICE_CALL:    return "Call";
            case LayoutConfig.COMP_VIDEO_CALL:    return "Video";
            case LayoutConfig.COMP_NAV_CHATS:     return "Chats";
            case LayoutConfig.COMP_NAV_CONTACTS:  return "Contacts";
            case LayoutConfig.COMP_NAV_CALLS:     return "Calls";
            case LayoutConfig.COMP_NAV_SETTINGS:  return "Settings";
            case LayoutConfig.COMP_NAV_PROFILE:   return "Profile";
            case LayoutConfig.COMP_EMOJI:         return "Emoji";
            case LayoutConfig.COMP_ATTACHMENT:    return "Attach";
            case LayoutConfig.COMP_CAMERA:        return "Camera";
            case LayoutConfig.COMP_VOICE_MSG:     return "Voice";
            case LayoutConfig.COMP_SEND:          return "Send";
            case LayoutConfig.COMP_BOT:           return "Bot";
            case LayoutConfig.COMP_SCHEDULE:      return "Schedule";
            default:                              return compId;
        }
    }

    // ── View helpers ──────────────────────────────────────────────────────────
    private TextView makeTextView(int textSizeDp, boolean bold, int color) {
        TextView tv = new TextView(getContext());
        tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, textSizeDp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    private TextView makeControlButton(String text) {
        TextView tv = makeTextView(11, false, COLOR_TEXT);
        tv.setText(text);
        tv.setPadding(dp(8), dp(4), dp(8), dp(4));
        tv.setBackground(makeRoundedBg(0x22FFFFFF, dp(6)));
        return tv;
    }

    private static android.graphics.drawable.Drawable makeRoundedBg(int color, float radius) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private static View makeDivider() {
        View v = new View(null);
        v.setBackgroundColor(0x33FFFFFF);
        return v;
    }

    private static LinearLayout.LayoutParams linearParams(int w, int h, float weight) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h, weight);
        lp.setMarginStart(dp(6));
        return lp;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mgr.removeListener(this);
        removeCallbacks(longPressRunnable);
    }
}
