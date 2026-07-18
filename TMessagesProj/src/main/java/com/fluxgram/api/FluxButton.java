package com.fluxgram.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

/**
 * FluxButton
 *
 * Backs Flux.UI.createButton(...). Wraps a plain, real Android TextView
 * styled with Telegram's own Theme drawable helpers so it looks native, but
 * never hands that TextView out through the FluxComponent surface.
 *
 * Implements FluxViewProvider so it can also be placed inside a
 * FluxContainer (Flux.UI.ChatHeader, etc), in addition to its own attach().
 */
final class FluxButton implements FluxComponent, FluxViewProvider {

    private static final String TAG = "Flux.UI.Button";

    // Simple default styling. Deliberately literal colors (rather than a
    // half-guessed Theme color key) so this compiles reliably; theming can
    // be refined once Flux.Settings/theming support lands.
    private static final int COLOR_BACKGROUND = 0xFF4E9CDE;
    private static final int COLOR_BACKGROUND_PRESSED = 0xFF3E82BD;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    private final TextView view;

    FluxButton(Context context, String text) {
        view = new TextView(context);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(COLOR_TEXT);
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        view.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(10), AndroidUtilities.dp(24), AndroidUtilities.dp(10));
        view.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), COLOR_BACKGROUND, COLOR_BACKGROUND_PRESSED));
        view.setText(text != null ? text : "");
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public FluxComponent setText(String text) {
        view.setText(text != null ? text : "");
        return this;
    }

    @Override
    public FluxComponent setIcon(int iconResId) {
        view.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
        view.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        return this;
    }

    @Override
    public FluxComponent setVisible(boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    @Override
    public FluxComponent setEnabled(boolean enabled) {
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.5f);
        return this;
    }

    @Override
    public FluxComponent onClick(FluxCallback callback) {
        view.setOnClickListener(v -> {
            if (callback != null) {
                callback.run(null);
            }
        });
        return this;
    }

    @Override
    public FluxComponent attach() {
        Activity activity = FluxActivityTracker.getCurrentActivity();
        if (activity == null) {
            Log.d(TAG, "attach: no foreground Activity to attach to.");
            return this;
        }
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            Log.d(TAG, "attach: current Activity has no content root.");
            return this;
        }
        ViewGroup currentParent = (ViewGroup) view.getParent();
        if (currentParent != null) {
            currentParent.removeView(view);
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.rightMargin = params.bottomMargin = AndroidUtilities.dp(16);
        content.addView(view, params);
        return this;
    }

    @Override
    public void remove() {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
    }
}
