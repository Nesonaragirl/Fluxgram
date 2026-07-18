package org.telegram.flux.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared bookkeeping for all Flux.UI containers: keeps components in
 * insertion order, keyed by id, and exposes {@link #onComponentAdded} /
 * {@link #onComponentRemoved} hooks that subclasses use to talk to native
 * Telegram UI once a host is bound.
 */
public abstract class AbstractFluxContainer<T extends FluxComponent> implements FluxContainer<T> {

    private final Map<String, T> components = new LinkedHashMap<>();
    private final ContainerType type;

    protected AbstractFluxContainer(ContainerType type) {
        this.type = type;
    }

    @Override
    public final ContainerType getType() {
        return type;
    }

    @Override
    public String add(T component) {
        if (component == null) {
            throw new IllegalArgumentException("component == null");
        }
        if (components.containsKey(component.getId())) {
            remove(component.getId());
        }
        components.put(component.getId(), component);
        onComponentAdded(component);
        return component.getId();
    }

    @Override
    public void remove(T component) {
        if (component == null) {
            return;
        }
        remove(component.getId());
    }

    @Override
    public void remove(String id) {
        if (id == null) {
            return;
        }
        T removed = components.remove(id);
        if (removed != null) {
            onComponentRemoved(removed);
        }
    }

    @Override
    public void clear() {
        List<T> snapshot = new ArrayList<>(components.values());
        components.clear();
        for (T component : snapshot) {
            onComponentRemoved(component);
        }
    }

    @Override
    public T get(String id) {
        return id == null ? null : components.get(id);
    }

    @Override
    public List<T> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(components.values()));
    }

    @Override
    public int size() {
        return components.size();
    }

    /** Called after a component is registered. Subclasses forward this to a bound native host, if any. */
    protected void onComponentAdded(T component) {
    }

    /** Called after a component is unregistered. Subclasses forward this to a bound native host, if any. */
    protected void onComponentRemoved(T component) {
    }
}
