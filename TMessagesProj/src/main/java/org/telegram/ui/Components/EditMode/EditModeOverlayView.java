package org.telegram.ui.Components.EditMode;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;

import java.util.HashMap;
import java.util.Map;

import static org.telegram.messenger.AndroidUtilities.dp;

/**
 * EditModeOverlayView — WYSIWYG view-transform editor.
 *
 * Sits full-screen above all Telegram UI. When edit mode is active:
 *  • Tap any real UI element to select it (shows a dashed amber border)
 *  • Drag inside the selection to MOVE the view (translationX/Y)
 *  • Drag a corner square to uniformly RESIZE (scale)
 *  • Drag the amber circle above the selection to ROTATE
 *  • Drag the green slider below the selection to set corner RADIUS
 *  • Tap "Reset" in the bottom bar to remove all transforms from the selected view
 *
 * Transforms are applied via View.setTranslationX/Y, setScaleX/Y, setRotation,
 * setClipToOutline + ViewOutlineProvider — no layout files are modified.
 * All states are persisted in SharedPreferences and re-applied on next launch.
 */
public class EditModeOverlayView extends FrameLayout implements EditModeManager.Listener {

    // ── Sizes (dp) ────────────────────────────────────────────────────────────
    private static final int HANDLE_R    = 10;  // corner handle half-size
    private static final int ROT_OFFSET  = 38;  // rotation handle above top edge
    private static final int PROP_H      = 52;  // property bar height
    private static final int MIN_VIEW_DP = 18;  // smallest selectable view

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final int C_DIM      = 0x50000000;
    private static final int C_SEL      = 0xFFF59E0B;   // amber selection border
    private static final int C_HANDLE   = 0xFFFFFFFF;
    private static final int C_ROT      = 0xFFF59E0B;   // amber rotation handle
    private static final int C_RADIUS   = 0xFF43A047;   // green radius slider
    private static final int C_PROP_BG  = 0xF017212B;
    private static final int C_RESET    = 0xFFE53935;
    private static final int C_TEXT     = 0xFFE8E8E8;

    // ── Paints ────────────────────────────────────────────────────────────────
    private final Paint pDim     = new Paint();
    private final Paint pSel     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pHandle  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pRot     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pLine    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pRadius  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pSlBg    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pSlFg    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPropBg  = new Paint();
    private final Paint pReset   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pHLabel  = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── Manager ───────────────────────────────────────────────────────────────
    private final EditModeManager mgr;

    // ── Overlay fade ──────────────────────────────────────────────────────────
    private float fade = 0f;  // 0..1

    // ── Selection ─────────────────────────────────────────────────────────────
    private View   selectedView;
    private String selectedId;
    private final RectF selRect = new RectF(); // visual bounds in overlay coords, UN-rotated

    // ── Transform state ───────────────────────────────────────────────────────
    private ViewTransformState currentState;
    private final Map<String, ViewTransformState> allStates = new HashMap<>();

    // ── Handles (all in UN-rotated selection space) ───────────────────────────
    private final RectF hTL  = new RectF(), hTR  = new RectF();
    private final RectF hBL  = new RectF(), hBR  = new RectF();
    private final RectF hRot  = new RectF();   // rotation circle
    private final RectF hSlider = new RectF(); // radius slider track (full)
    private final RectF propBar = new RectF(); // fixed-screen-space property bar
    private final RectF hReset  = new RectF(); // reset button inside prop bar

    // ── Drag state ────────────────────────────────────────────────────────────
    private enum Mode { NONE, MOVE, RESIZE_TL, RESIZE_TR, RESIZE_BL, RESIZE_BR, ROTATE, RADIUS }
    private Mode dragMode = Mode.NONE;
    private float touchDownX, touchDownY;
    private float centerAtDown_X, centerAtDown_Y;
    private ViewTransformState stateAtDown;

    // ── Constructor ───────────────────────────────────────────────────────────
    public EditModeOverlayView(Context context) {
        super(context);
        mgr = EditModeManager.getInstance(context);
        mgr.addListener(this);

        setWillNotDraw(false);
        setVisibility(GONE);
        setClickable(true);

        pDim.setColor(C_DIM);

        pSel.setStyle(Paint.Style.STROKE);
        pSel.setStrokeWidth(dp(2));
        pSel.setColor(C_SEL);
        pSel.setPathEffect(new DashPathEffect(new float[]{dp(8), dp(4)}, 0));

        pHandle.setColor(C_HANDLE);

        pRot.setColor(C_ROT);

        pLine.setColor(C_ROT);
        pLine.setStrokeWidth(dp(1.5f));
        pLine.setStyle(Paint.Style.STROKE);

        pRadius.setColor(C_RADIUS);

        pSlBg.setColor(0x55FFFFFF);
        pSlFg.setColor(C_RADIUS);

        pPropBg.setColor(C_PROP_BG);

        pReset.setColor(C_RESET);

        pText.setColor(C_TEXT);
        pText.setTextSize(dp(11));
        pText.setTypeface(Typeface.DEFAULT_BOLD);
        pText.setAntiAlias(true);

        pHLabel.setColor(0xFF17212B);
        pHLabel.setTextSize(dp(8));
        pHLabel.setTextAlign(Paint.Align.CENTER);
        pHLabel.setAntiAlias(true);

        // Load persisted transforms
        allStates.putAll(ViewTransformState.loadAll(context));
    }

    // ── Touch ─────────────────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:  return handleDown(x, y);
            case MotionEvent.ACTION_MOVE:  handleMove(x, y); return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: handleUp(); return true;
        }
        return true; // consume all touches when overlay is visible
    }

    private boolean handleDown(float x, float y) {
        // 1. Reset button (fixed screen coords, not rotated)
        if (selectedView != null && hReset.contains(x, y)) {
            doReset(); return true;
        }

        // 2. Handle hit-test — inverse-rotate touch into selection space
        if (selectedView != null) {
            float[] loc = inverseRotate(x, y);
            Mode hit = hitTest(loc[0], loc[1]);
            if (hit != Mode.NONE) {
                startDrag(hit, x, y); return true;
            }
            // 3. Inside selection body → MOVE
            if (selRect.contains(loc[0], loc[1])) {
                startDrag(Mode.MOVE, x, y); return true;
            }
            // 4. Outside everything → deselect, maybe select new
            deselect();
        }

        // 5. Try to pick a view at touch position
        View picked = findViewAt(x, y);
        if (picked != null) {
            doSelect(picked);
        }
        return true;
    }

    private void handleMove(float x, float y) {
        if (dragMode == Mode.NONE || selectedView == null || currentState == null) return;

        float dx = x - touchDownX, dy = y - touchDownY;

        switch (dragMode) {
            case MOVE:
                currentState.tx = stateAtDown.tx + dx;
                currentState.ty = stateAtDown.ty + dy;
                selectedView.setTranslationX(currentState.tx);
                selectedView.setTranslationY(currentState.ty);
                break;

            case RESIZE_TL: case RESIZE_TR: case RESIZE_BL: case RESIZE_BR: {
                float d0 = dist(touchDownX, touchDownY, centerAtDown_X, centerAtDown_Y);
                float d1 = dist(x, y, centerAtDown_X, centerAtDown_Y);
                float ratio = (d0 > 1f) ? (d1 / d0) : 1f;
                currentState.scaleX = Math.max(0.05f, stateAtDown.scaleX * ratio);
                currentState.scaleY = Math.max(0.05f, stateAtDown.scaleY * ratio);
                selectedView.setScaleX(currentState.scaleX);
                selectedView.setScaleY(currentState.scaleY);
                break;
            }

            case ROTATE: {
                float a0 = angleDeg(touchDownX - centerAtDown_X, touchDownY - centerAtDown_Y);
                float a1 = angleDeg(x - centerAtDown_X, y - centerAtDown_Y);
                currentState.rotation = stateAtDown.rotation + (a1 - a0);
                selectedView.setRotation(currentState.rotation);
                break;
            }

            case RADIUS: {
                float maxR = Math.min(selectedView.getWidth(), selectedView.getHeight()) / 2f;
                if (maxR <= 0) break;
                float slW = hSlider.width();
                float fraction = slW > 0
                    ? Math.max(0f, Math.min(1f, (x - hSlider.left) / slW))
                    : 0f;
                currentState.cornerRadius = fraction * maxR;
                currentState.applyCornerRadius(selectedView);
                break;
            }
        }
        invalidate();
    }

    private void handleUp() {
        dragMode = Mode.NONE;
        ViewTransformState.saveAll(getContext(), allStates);
    }

    private void startDrag(Mode mode, float x, float y) {
        dragMode = mode;
        touchDownX = x; touchDownY = y;
        centerAtDown_X = selRect.centerX();
        centerAtDown_Y = selRect.centerY();
        stateAtDown = ViewTransformState.copy(currentState);
    }

    // ── Hit-testing (in UN-rotated / inverse-rotated space) ───────────────────

    private Mode hitTest(float lx, float ly) {
        if (hRot.contains(lx, ly))  return Mode.ROTATE;
        if (hTL.contains(lx, ly))   return Mode.RESIZE_TL;
        if (hTR.contains(lx, ly))   return Mode.RESIZE_TR;
        if (hBL.contains(lx, ly))   return Mode.RESIZE_BL;
        if (hBR.contains(lx, ly))   return Mode.RESIZE_BR;
        // Slider: check proximity (fat touch target)
        if (lx >= hSlider.left && lx <= hSlider.right
                && Math.abs(ly - hSlider.centerY()) <= dp(18)) return Mode.RADIUS;
        return Mode.NONE;
    }

    // ── Selection / deselection ───────────────────────────────────────────────

    private void doSelect(View v) {
        selectedView = v;
        selectedId   = makeId(v);
        // Restore or create state
        currentState = allStates.get(selectedId);
        if (currentState == null) {
            currentState = new ViewTransformState();
            // Seed with whatever transforms are already on the view
            currentState.tx       = v.getTranslationX();
            currentState.ty       = v.getTranslationY();
            currentState.scaleX   = v.getScaleX();
            currentState.scaleY   = v.getScaleY();
            currentState.rotation = v.getRotation();
            allStates.put(selectedId, currentState);
        }
        dragMode = Mode.NONE;
        invalidate();
    }

    private void deselect() {
        selectedView = null;
        selectedId   = null;
        currentState = null;
        dragMode     = Mode.NONE;
        invalidate();
    }

    private void doReset() {
        if (selectedView == null || currentState == null) return;
        currentState.tx = 0; currentState.ty = 0;
        currentState.scaleX = 1; currentState.scaleY = 1;
        currentState.rotation = 0; currentState.cornerRadius = 0;
        currentState.applyTo(selectedView);
        ViewTransformState.saveAll(getContext(), allStates);
        invalidate();
    }

    // ── View finder ───────────────────────────────────────────────────────────

    private View findViewAt(float ox, float oy) {
        if (!(getParent() instanceof ViewGroup)) return null;
        int[] myLoc = new int[2]; getLocationOnScreen(myLoc);
        int sx = (int)(ox + myLoc[0]), sy = (int)(oy + myLoc[1]);
        return searchAt((ViewGroup) getParent(), sx, sy);
    }

    private View searchAt(ViewGroup group, int sx, int sy) {
        for (int i = group.getChildCount() - 1; i >= 0; i--) {
            View child = group.getChildAt(i);
            if (!canSelect(child)) continue;

            int[] loc = new int[2]; child.getLocationOnScreen(loc);
            if (sx < loc[0] || sx >= loc[0] + child.getWidth() ||
                sy < loc[1] || sy >= loc[1] + child.getHeight()) continue;

            // Try going deeper first (prefer leaf views)
            if (child instanceof ViewGroup) {
                View deeper = searchAt((ViewGroup) child, sx, sy);
                if (deeper != null) return deeper;
            }
            // Accept this view if it's big enough to be meaningful
            if (child.getWidth() >= dp(MIN_VIEW_DP) && child.getHeight() >= dp(MIN_VIEW_DP)) {
                return child;
            }
        }
        return null;
    }

    private boolean canSelect(View v) {
        if (v == this) return false;
        if (v.getVisibility() != VISIBLE) return false;
        String n = v.getClass().getSimpleName();
        // Skip our own overlay views and Telegram's fullscreen overlays
        if (n.contains("EditMode") || n.contains("Fireworks") ||
            n.contains("BottomSheetTabsOverlay")) return false;
        return true;
    }

    // ── View ID ───────────────────────────────────────────────────────────────

    private String makeId(View view) {
        StringBuilder sb = new StringBuilder(view.getClass().getSimpleName());
        View v = view;
        int depth = 0;
        while (v.getParent() instanceof ViewGroup && depth < 10) {
            ViewGroup p = (ViewGroup) v.getParent();
            for (int i = 0; i < p.getChildCount(); i++) {
                if (p.getChildAt(i) == v) { sb.append("_").append(i); break; }
            }
            v = (View) v.getParent();
            depth++;
        }
        return sb.toString();
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (fade <= 0f) return;

        // Semi-transparent dim over entire screen
        pDim.setAlpha((int)(0x50 * fade));
        canvas.drawRect(0, 0, getWidth(), getHeight(), pDim);

        if (selectedView != null && isAttached(selectedView)) {
            refreshSelRect();
            refreshHandles();

            float cx  = selRect.centerX();
            float cy  = selRect.centerY();
            float rot = selectedView.getRotation();

            canvas.save();
            canvas.rotate(rot, cx, cy);

            // Dashed selection border
            pSel.setAlpha((int)(0xFF * fade));
            canvas.drawRect(selRect, pSel);

            // Stem from top-center to rotation handle
            pLine.setAlpha((int)(0xCC * fade));
            canvas.drawLine(cx, selRect.top, cx, selRect.top - dp(ROT_OFFSET), pLine);

            // Rotation handle (amber circle)
            pRot.setAlpha((int)(0xFF * fade));
            canvas.drawCircle(hRot.centerX(), hRot.centerY(), dp(HANDLE_R), pRot);
            pHLabel.setColor(0xFF17212B);
            canvas.drawText("↻", hRot.centerX(), hRot.centerY() + dp(4), pHLabel);

            // Corner resize handles (white squares)
            pHandle.setAlpha((int)(0xFF * fade));
            float hr = dp(4);
            canvas.drawRoundRect(hTL, hr, hr, pHandle);
            canvas.drawRoundRect(hTR, hr, hr, pHandle);
            canvas.drawRoundRect(hBL, hr, hr, pHandle);
            canvas.drawRoundRect(hBR, hr, hr, pHandle);
            pHLabel.setColor(0xFF17212B);
            drawLabel(canvas, hTL, "↔"); drawLabel(canvas, hTR, "↔");
            drawLabel(canvas, hBL, "↔"); drawLabel(canvas, hBR, "↔");

            // Corner radius slider (green) below selection
            float maxR = Math.min(selectedView.getWidth(), selectedView.getHeight()) / 2f;
            float fill = (maxR > 0 && currentState != null)
                ? Math.max(0f, Math.min(1f, currentState.cornerRadius / maxR)) : 0f;
            float sr = dp(3);
            pSlBg.setAlpha((int)(0xAA * fade));
            canvas.drawRoundRect(hSlider, sr, sr, pSlBg);
            if (fill > 0f) {
                pSlFg.setAlpha((int)(0xFF * fade));
                RectF fr = new RectF(hSlider.left, hSlider.top,
                                     hSlider.left + hSlider.width() * fill, hSlider.bottom);
                canvas.drawRoundRect(fr, sr, sr, pSlFg);
            }
            float thumbX = hSlider.left + hSlider.width() * fill;
            pRadius.setAlpha((int)(0xFF * fade));
            canvas.drawCircle(thumbX, hSlider.centerY(), dp(8), pRadius);

            // Slider label
            pHLabel.setColor(0xFFE8E8E8);
            pHLabel.setTextSize(dp(9));
            canvas.drawText("⬤ corner radius", hSlider.centerX(),
                hSlider.top - dp(5), pHLabel);
            pHLabel.setTextSize(dp(8));

            canvas.restore();

            // Property bar (screen-fixed, not rotated)
            drawPropBar(canvas);
        }

        super.dispatchDraw(canvas);
    }

    private void drawLabel(Canvas canvas, RectF h, String txt) {
        canvas.drawText(txt, h.centerX(), h.centerY() + dp(3), pHLabel);
    }

    private void drawPropBar(Canvas canvas) {
        pPropBg.setAlpha((int)(0xF0 * fade));
        canvas.drawRect(propBar, pPropBg);

        // Info text
        String info = "";
        if (currentState != null) {
            info = String.format("Scale %.0f%% · Rot %.0f° · Radius %.0fpx",
                currentState.scaleX * 100f,
                currentState.rotation,
                currentState.cornerRadius);
        }
        pText.setTextAlign(Paint.Align.LEFT);
        pText.setAlpha((int)(0xFF * fade));
        canvas.drawText(info, propBar.left + dp(12),
            propBar.centerY() + pText.getTextSize() / 3f, pText);

        // Reset button
        pReset.setAlpha((int)(0xFF * fade));
        canvas.drawRoundRect(hReset, dp(6), dp(6), pReset);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setColor(0xFFFFFFFF);
        canvas.drawText("✕ Reset", hReset.centerX(),
            hReset.centerY() + pText.getTextSize() / 3f, pText);
        pText.setColor(C_TEXT);
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────

    /** Recompute the visual selection rect from the view's current position + transforms. */
    private void refreshSelRect() {
        if (selectedView == null) return;
        int[] myLoc = new int[2]; getLocationOnScreen(myLoc);
        int[] vLoc  = new int[2]; selectedView.getLocationOnScreen(vLoc);

        // getLocationOnScreen accounts for translationX/Y but NOT scale.
        // Pivot is at (width/2, height/2) by default, so the visual center is fixed.
        float cx = (vLoc[0] - myLoc[0]) + selectedView.getWidth()  / 2f;
        float cy = (vLoc[1] - myLoc[1]) + selectedView.getHeight() / 2f;
        float hw = selectedView.getWidth()  * selectedView.getScaleX() / 2f;
        float hh = selectedView.getHeight() * selectedView.getScaleY() / 2f;
        selRect.set(cx - hw, cy - hh, cx + hw, cy + hh);
    }

    private void refreshHandles() {
        float hs = dp(HANDLE_R);
        float l = selRect.left, r = selRect.right;
        float t = selRect.top,  b = selRect.bottom;
        float cx = selRect.centerX();

        hTL.set(l - hs, t - hs, l + hs, t + hs);
        hTR.set(r - hs, t - hs, r + hs, t + hs);
        hBL.set(l - hs, b - hs, l + hs, b + hs);
        hBR.set(r - hs, b - hs, r + hs, b + hs);
        hRot.set(cx - hs, t - dp(ROT_OFFSET) - hs, cx + hs, t - dp(ROT_OFFSET) + hs);

        float slY = b + dp(14);
        hSlider.set(l + dp(20), slY, r - dp(20), slY + dp(6));

        // Property bar — fixed at screen bottom
        propBar.set(0, getHeight() - dp(PROP_H), getWidth(), getHeight());
        hReset.set(propBar.right - dp(80), propBar.top + dp(10),
                   propBar.right - dp(8),  propBar.bottom - dp(10));
    }

    /** Inverse-rotate a touch point back into the un-rotated selection space. */
    private float[] inverseRotate(float x, float y) {
        float rot = (selectedView != null) ? selectedView.getRotation() : 0f;
        float cx = selRect.centerX(), cy = selRect.centerY();
        double angle = Math.toRadians(-rot);
        float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);
        float dx = x - cx, dy = y - cy;
        return new float[]{ dx * cos - dy * sin + cx, dx * sin + dy * cos + cy };
    }

    private static float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static float angleDeg(float dx, float dy) {
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    private static boolean isAttached(View v) {
        return v.getWindowToken() != null;
    }

    // ── EditModeManager.Listener ──────────────────────────────────────────────

    @Override
    public void onEditModeChanged(boolean editMode) {
        if (editMode) {
            setVisibility(VISIBLE);
            animateFade(0f, 1f);
        } else {
            if (!allStates.isEmpty()) ViewTransformState.saveAll(getContext(), allStates);
            deselect();
            animateFade(1f, 0f);
            postDelayed(() -> setVisibility(GONE), 220);
        }
    }

    @Override
    public void onLayoutChanged(LayoutConfig config) {
        // Not needed for transform-based editor
    }

    private void animateFade(float from, float to) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.setDuration(200);
        anim.addUpdateListener(a -> { fade = (float) a.getAnimatedValue(); invalidate(); });
        anim.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mgr.removeListener(this);
    }
}
