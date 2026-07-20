package com.caint.api;

import android.content.Context;
import android.util.Log;

import org.telegram.ui.ActionBar.AlertDialog;

/**
 * CaintDialog
 *
 * Backs Caint.UI.createDialog(...). Wraps Telegram's own AlertDialog/Builder
 * so dialogs created through Caint look and behave like native Telegram
 * dialogs, without exposing AlertDialog itself to callers.
 *
 * Note: icon, enabled/disabled state, and custom colors don't have an
 * obvious equivalent for a modal dialog yet, so those methods are logged
 * placeholders for now rather than guessed-at implementations.
 */
final class CaintDialog implements CaintComponent {

    private static final String TAG = "Caint.UI.Dialog";

    private final AlertDialog.Builder builder;
    private final AlertDialog dialog;
    private CaintCallback clickCallback;

    CaintDialog(Context context, String title, String message) {
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
    public CaintComponent setText(String text) {
        builder.setMessage(text);
        return this;
    }

    @Override
    public CaintComponent setIcon(int iconResId) {
        Log.d(TAG, "setIcon: Not implemented yet for dialogs.");
        return this;
    }

    @Override
    public CaintComponent setBackgroundColor(Integer color) {
        Log.d(TAG, "setBackgroundColor: Not implemented yet for dialogs.");
        return this;
    }

    @Override
    public CaintComponent setTextColor(Integer color) {
        Log.d(TAG, "setTextColor: Not implemented yet for dialogs.");
        return this;
    }

    @Override
    public CaintComponent setVisible(boolean visible) {
        if (visible) {
            dialog.show();
        } else {
            dialog.dismiss();
        }
        return this;
    }

    @Override
    public CaintComponent setEnabled(boolean enabled) {
        Log.d(TAG, "setEnabled: Not implemented yet for dialogs.");
        return this;
    }

    @Override
    public CaintComponent onClick(CaintCallback callback) {
        clickCallback = callback;
        return this;
    }

    @Override
    public CaintComponent attach() {
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
