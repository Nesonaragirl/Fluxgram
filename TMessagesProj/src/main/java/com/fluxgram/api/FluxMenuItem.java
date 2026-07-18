package com.fluxgram.api;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.ui.ActionBar.ActionBarMenuSubItem;

/**
 * FluxMenuItem
 *
 * Backs Flux.UI.createMenuItem(...). Wraps Telegram's own
 * ActionBarMenuSubItem -- the same row component Telegram uses for its
 * native popup/context menus -- without exposing that class to callers.
 */
final class FluxMenuItem implements FluxComponent {

    private final ActionBarMenuSubItem view;

    FluxMenuItem(Context context, String text, int iconResId) {
        view = new ActionBarMenuSubItem(context, true, true);
        view.setTextAndIcon(text != null ? text : "", iconResId);
    }

    /**
     * Package-private escape hatch for other Flux internals. Never exposed
     * publicly.
     */
    View getView() {
        return view;
    }

    @Override
    public FluxComponent setText(String text) {
        view.setText(text != null ? text : "");
        return this;
    }

    @Override
    public FluxComponent setIcon(int iconResId) {
        view.setIcon(iconResId);
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
    public void remove() {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
    }
}
