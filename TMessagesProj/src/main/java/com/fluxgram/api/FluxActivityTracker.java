package com.fluxgram.api;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/**
 * Tracks the current foreground Activity so Flux.UI components can attach
 * themselves somewhere real without Flux needing to know anything about
 * Telegram's fragment/navigation internals.
 *
 * This is intentionally the only thing Flux knows about Activities -- a
 * weak reference to whichever one is currently resumed.
 */
final class FluxActivityTracker implements Application.ActivityLifecycleCallbacks {

    private static final FluxActivityTracker INSTANCE = new FluxActivityTracker();
    private static boolean attached;

    private WeakReference<Activity> currentActivity = new WeakReference<>(null);

    private FluxActivityTracker() {
    }

    static void attach(Application application) {
        if (attached) {
            return;
        }
        attached = true;
        application.registerActivityLifecycleCallbacks(INSTANCE);
    }

    static Activity getCurrentActivity() {
        return INSTANCE.currentActivity.get();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (currentActivity.get() == activity) {
            currentActivity.clear();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
