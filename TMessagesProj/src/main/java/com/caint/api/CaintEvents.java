package com.caint.api;

import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Caint.Events
 *
 * A simple, real pub/sub event bus. Anything in the app -- Caint internals,
 * future plugins, or a bridge like {@link CaintNotificationBridge} -- can
 * emit or listen for named events here without ever touching Telegram's
 * internal notification system directly.
 */
public final class CaintEvents {

    private static final String TAG = "Caint.Events";

    private final Map<String, CopyOnWriteArrayList<CaintCallback>> listeners = new ConcurrentHashMap<>();

    CaintEvents() {
    }

    /**
     * Registers a callback for a named event. The same callback instance can
     * later be removed with {@link #off}.
     *
     * @param event    the event name to listen for
     * @param callback invoked with the event's data when emitted
     */
    public void on(String event, CaintCallback callback) {
        if (event == null || callback == null) {
            return;
        }
        listeners.computeIfAbsent(event, key -> new CopyOnWriteArrayList<>()).add(callback);
    }

    /**
     * Unregisters a previously registered callback for an event. No-op if the
     * callback was never registered (or already removed).
     *
     * @param event    the event name
     * @param callback the exact callback instance passed to {@link #on}
     */
    public void off(String event, CaintCallback callback) {
        if (event == null || callback == null) {
            return;
        }
        List<CaintCallback> callbacks = listeners.get(event);
        if (callbacks != null) {
            callbacks.remove(callback);
        }
    }

    /**
     * Emits a named event to every currently registered listener, in
     * registration order. A misbehaving listener (one that throws) is
     * logged and skipped rather than breaking the remaining listeners.
     *
     * @param event the event name to emit
     * @param data  payload passed to listeners
     */
    public void emit(String event, Object data) {
        if (event == null) {
            return;
        }
        List<CaintCallback> callbacks = listeners.get(event);
        if (callbacks == null || callbacks.isEmpty()) {
            return;
        }
        for (CaintCallback callback : callbacks) {
            try {
                callback.run(data);
            } catch (Exception e) {
                Log.e(TAG, "emit: listener for \"" + event + "\" threw an exception", e);
            }
        }
    }
}
