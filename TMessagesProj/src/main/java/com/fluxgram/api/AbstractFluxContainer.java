package com.fluxgram.api;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shared bookkeeping and placement logic for every Flux.UI container. See
 * {@link FluxContainer} for the bound-vs-unbound placement story.
 */
class AbstractFluxContainer implements FluxContainer {

    private static final String TAG = "Flux.UI.Container";

    private final ContainerType type;
    private final int defaultGravity;
    private final Map<String, FluxComponent> components = new LinkedHashMap<>();

    private ViewGroup host;

    AbstractFluxContainer(ContainerType type, int defaultGravity) {
        this.type = type;
        this.defaultGravity = defaultGravity;
    }

    @Override
    public ContainerType getType() {
        return type;
    }

    @Override
    public void bindHost(ViewGroup hostView) {
        host = hostView;
        for (FluxComponent component : components.values()) {
            place(component);
        }
    }

    @Override
    public void unbindHost() {
        host = null;
        for (FluxComponent component : components.values()) {
            place(component);
        }
    }

    @Override
    public String add(FluxComponent component) {
        return add(UUID.randomUUID().toString(), component);
    }

    @Override
    public String add(String id, FluxComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("component == null");
        }
        remove(id);
        components.put(id, component);
        place(component);
        return id;
    }

    @Override
    public void remove(FluxComponent component) {
        String matchingId = null;
        for (Map.Entry<String, FluxComponent> entry : components.entrySet()) {
            if (entry.getValue() == component) {
                matchingId = entry.getKey();
                break;
            }
        }
        remove(matchingId);
    }

    @Override
    public void remove(String id) {
        if (id == null) {
            return;
        }
        FluxComponent removed = components.remove(id);
        if (removed != null) {
            removed.remove();
        }
    }

    @Override
    public void clear() {
        List<FluxComponent> snapshot = new ArrayList<>(components.values());
        components.clear();
        for (FluxComponent component : snapshot) {
            component.remove();
        }
    }

    @Override
    public FluxComponent get(String id) {
        return id == null ? null : components.get(id);
    }

    @Override
    public List<FluxComponent> getAll() {
        return new ArrayList<>(components.values());
    }

    @Override
    public int size() {
        return components.size();
    }

    private void place(FluxComponent component) {
        if (!(component instanceof FluxViewProvider)) {
            // Non-view components (e.g. a dialog) have no location to be placed in.
            return;
        }
        View view = ((FluxViewProvider) component).getView();

        ViewGroup target = host != null ? host : fallbackHost();
        if (target == null) {
            Log.d(TAG, "place: no host available for " + type + " yet.");
            return;
        }

        ViewGroup currentParent = (ViewGroup) view.getParent();
        if (currentParent != null) {
            currentParent.removeView(view);
        }

        if (host != null) {
            // Bound to a real native location -- let it lay the view out however it wants.
            host.addView(view);
        } else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = defaultGravity;
            params.leftMargin = params.rightMargin = params.topMargin = params.bottomMargin = AndroidUtilities.dp(16);
            target.addView(view, params);
        }
    }

    private ViewGroup fallbackHost() {
        Activity activity = FluxActivityTracker.getCurrentActivity();
        if (activity == null) {
            return null;
        }
        return activity.findViewById(android.R.id.content);
    }
}
