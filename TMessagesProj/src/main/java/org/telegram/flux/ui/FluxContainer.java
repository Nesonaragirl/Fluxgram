package org.telegram.flux.ui;

import java.util.List;

/**
 * Contract implemented by every Flux.UI container (ChatHeader, ChatFooter,
 * ChatMenu, MessageMenu, Sidebar, BottomBar, Settings, and any future one).
 *
 * All methods are expected to be called from the UI thread, consistent with
 * the rest of the Telegram UI layer.
 */
public interface FluxContainer<T extends FluxComponent> {

    /**
     * Adds a component to this container. If a component with the same id is
     * already present it is replaced.
     *
     * @return the component's id, for convenience.
     */
    String add(T component);

    /** Removes a previously added component. No-op if it isn't present. */
    void remove(T component);

    /** Removes a previously added component by id. No-op if it isn't present. */
    void remove(String id);

    /** Removes every component currently in this container. */
    void clear();

    /** Returns the component with the given id, or {@code null} if none. */
    T get(String id);

    /** Returns a snapshot list of every component currently in this container, in insertion order. */
    List<T> getAll();

    /** Number of components currently in this container. */
    int size();

    /** Which native UI location this container represents. */
    ContainerType getType();
}
