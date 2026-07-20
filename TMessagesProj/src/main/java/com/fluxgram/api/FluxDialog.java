package com.fluxgram.api;

import android.content.Context;
import android.util.Log;

import org.telegram.ui.ActionBar.AlertDialog;

/**
 * FluxDialog
 *
 * Backs Flux.UI.createDialog(...). Wraps Telegram's own AlertDialog/Builder
 * so dialogs created through Flux look and behave like native Telegram
 * dialogs, without exposing AlertDialog itself to callers.
 *
 * Note: icon, enabled/disabled state, and custom colors don't have an
 * obvious equivalent for a modal dialog yet, so those methods are logged
 * placeholders for now rather than guessed-at implementations.
 */
final class FluxDialog implements FluxComponent {

    private static final String TAG = "Flux.UI.Dialog";

    private final AlertDialog.Builder builder;
    private final AlertDialog dialog;
    private FluxCallback clickCallback;

    FluxDialog(Context context, String title, String message) {
        builder = new AlertDialog.Builder(context);
        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        builder.setPositiveButton("OK", (dialogInterface, which) -> {
            if (clickCallback != null) {
                clickCallback.run(null);
            }
        });
        dialog = builder.show();
    }

    @Override
    public FluxComponent setText(String text) {
        builder.setMessage(text);
        return this;
    }

    @Override
    public FluxComponent setIcon(int iconResId) {
        Log.d(TAG, "setIcon: Not implemented yet for dialogs.");
        return this;
    }

    @Override
    public FluxComponent setBackgroundColor(Integer color) {
        Log.d(TAG, "setBackgroundColor: Not implemented yet for dialogs.");
        return this;
    }

    @Override
    public FluxComponent setTextColor(Integer color) {
        Log.d(TAG, "setTextColor: Not implemented yet for dialogs.");
        return this;
    }

    @Override
    public FluxComponent setVisible(boolean visible) {
        if (visible) {
            dialog.show();
        } else {
            dialog.dismiss();
        }
        return this;
    }

    @Override
    public FluxComponent setEnabled(boolean enabled) {
        Log.d(TAG, "setEnabled: Not implemented yet for dialogs.");
        return this;
    }

    @Override
    public FluxComponent onClick(FluxCallback callback) {
        clickCallback = callback;
        return this;
    }

    @Override
    public FluxComponent attach() {
        // A dialog is already shown as soon as it's created (see the
        // constructor), so there's nowhere further to attach it to.
        Log.d(TAG, "attach: dialogs are shown immediately; nothing to do.");
        return this;
    }

    @Override
    public void remove() {
        dialog.dismiss();
    }
}
