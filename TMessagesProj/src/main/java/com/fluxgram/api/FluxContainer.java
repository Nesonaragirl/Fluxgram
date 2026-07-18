package com.fluxgram.api;

import android.view.ViewGroup;

import java.util.List;

/**
 * A named slot for one native Telegram UI location (chat header, sidebar,
 * message menu, etc). Holds zero or more FluxComponents, keyed by id, in
 * insertion order.
 *
 * Two ways a real Telegram screen can make a container's components
 * actually visible in the right place:
 *
 *  - Do nothing: components fall back to a generic corner overlay on the
 *    current foreground screen. Works everywhere, out of the box.
 *  - Call {@link #bindHost}, passing the real header/footer/sidebar/etc
 *    ViewGroup once that screen is ready. From then on components are
 *    placed there instead -- true native placement -- until
 *    {@link #unbindHost} is called (e.g. when that screen is destroyed).
 */
public interface FluxContainer {

    /** Which native UI location this container represents. */
    ContainerType getType();

    /**
     * Binds a real native ViewGroup as this container's host. Once bound,
     * every current and future component is placed inside it.
     */
    void bindHost(ViewGroup hostView);

    /** Reverts to the generic overlay fallback. */
    void unbindHost();

    /** Adds a component under a generated id. @return the generated id. */
    String add(FluxComponent component);

    /** Adds a component under a caller-supplied id, replacing any existing one with that id. */
    String add(String id, FluxComponent component);

    /** Removes a previously added component. No-op if it isn't present. */
    void remove(FluxComponent component);

    /** Removes a previously added component by id. No-op if it isn't present. */
    void remove(String id);

    /** Removes every component currently in this container. */
    void clear();

    /** Returns the component with the given id, or {@code null} if none. */
    FluxComponent get(String id);

    /** Returns a snapshot list of every component currently in this container, in insertion order. */
    List<FluxComponent> getAll();

    /** Number of components currently in this container. */
    int size();
}
