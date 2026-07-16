package org.telegram.ui.Components.EditMode;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import static org.telegram.messenger.AndroidUtilities.dp;

/**
 * EditModeButton — the "✏ Edit Layout" floating action button.
 *
 * Injected into LaunchActivity's root FrameLayout so it floats above all screens.
 * Tap → toggles edit mode.
 * Long-press → shows JSON export toast (quick access to layout JSON).
 *
 * Positioning: bottom-right corner, above the system nav bar, 72dp from bottom.
 */
public class EditModeButton extends View implements EditModeManager.Listener {

    private final EditModeManager mgr;
    private final Paint   bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint   textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF   rect      = new RectF();
    private       boolean editMode  = false;
    private       float   pressAnim = 0f;   // 0→1 on press

    private static final int COLOR_NORMAL    = 0xFF5288C1;
    private static final int COLOR_EDIT      = 0xFFF59E0B;
    private static final int COLOR_TEXT      = 0xFFFFFFFF;
    private static final int BUTTON_H        = 38;   // dp
    private static final int BUTTON_W        = 120;  // dp (approximate, text-driven)

    public EditModeButton(Context context) {
        super(context);
        mgr = EditModeManager.getInstance(context);
        mgr.addListener(this);

        bgPaint.setColor(COLOR_NORMAL);
        textPaint.setColor(COLOR_TEXT);
        textPaint.setTextSize(dp(12));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);

        setOnClickListener(v -> mgr.toggleEditMode());

        setOnLongClickListener(v -> {
            // Quick JSON export via Toast (for testing without a full dialog)
            String json = mgr.exportJson();
            Toast.makeText(context,
                "Layout JSON copied. Length: " + json.length() + " chars.",
                Toast.LENGTH_SHORT).show();
            // In a full implementation, this would open a dialog with copy/share options.
            return true;
        });

        setElevation(dp(8));
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    /** Attach this button to a root FrameLayout with correct gravity and margins. */
    public static EditModeButton attachTo(ViewGroup parent) {
        EditModeButton btn = new EditModeButton(parent.getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(BUTTON_W), dp(BUTTON_H));
        lp.gravity = Gravity.BOTTOM | Gravity.END;
        lp.bottomMargin = dp(80); // above bottom nav + some breathing room
        lp.rightMargin  = dp(16);
        parent.addView(btn, lp);
        return btn;
    }

    // ── Drawing ───────────────────────────────────────────────────────────────
    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        float scale = 1f - pressAnim * 0.05f;
        canvas.save();
        canvas.scale(scale, scale, w / 2f, h / 2f);

        // Shadow / glow effect (simple)
        bgPaint.setShadowLayer(dp(6), 0, dp(2), editMode ? 0x60F59E0B : 0x605288C1);
        rect.set(0, 0, w, h);
        bgPaint.setColor(editMode ? COLOR_EDIT : COLOR_NORMAL);
        canvas.drawRoundRect(rect, h / 2f, h / 2f, bgPaint);

        // Label
        String label = editMode ? "✓ Done Editing" : "✏ Edit Layout";
        float textY  = h / 2f - (textPaint.ascent() + textPaint.descent()) / 2f;
        canvas.drawText(label, w / 2f, textY, textPaint);

        canvas.restore();
    }

    // ── Press animation ───────────────────────────────────────────────────────
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                animatePressAnim(0f, 1f);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                animatePressAnim(1f, 0f);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void animatePressAnim(float from, float to) {
        ValueAnimator a = ValueAnimator.ofFloat(from, to);
        a.setDuration(100);
        a.addUpdateListener(anim -> { pressAnim = (float) anim.getAnimatedValue(); invalidate(); });
        a.start();
    }

    // ── EditModeManager.Listener ──────────────────────────────────────────────
    @Override
    public void onEditModeChanged(boolean isEditMode) {
        this.editMode = isEditMode;
        // Pulse animation when entering edit mode
        if (isEditMode) {
            ValueAnimator pulse = ValueAnimator.ofFloat(0f, 1f, 0f);
            pulse.setDuration(400);
            pulse.addUpdateListener(a -> { pressAnim = (float) a.getAnimatedValue() * 0.5f; invalidate(); });
            pulse.start();
        } else {
            invalidate();
        }
    }

    @Override
    public void onLayoutChanged(LayoutConfig config) {
        // No visual change needed on the button itself
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mgr.removeListener(this);
    }
}
