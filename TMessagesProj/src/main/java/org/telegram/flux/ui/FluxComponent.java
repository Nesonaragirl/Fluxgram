package org.telegram.flux.ui;

import java.util.Objects;
import java.util.UUID;

/**
 * Base type for anything that can live inside a Flux.UI container.
 *
 * A FluxComponent never exposes a Telegram-internal class as part of its
 * public API - subclasses either wrap a plain Android SDK type (e.g. a
 * caller-supplied {@link android.view.View}) or plain data (text, icon
 * resource id, listeners). Concrete containers translate components into
 * native Telegram UI on demand, via the host interfaces in
 * {@code org.telegram.flux.ui.host}.
 */
public abstract class FluxComponent {

    private final String id;
    private boolean visible = true;
    private Object tag;

    protected FluxComponent() {
        this(null);
    }

    /**
     * @param id caller-supplied id, or {@code null}/empty to have Flux generate a unique one.
     */
    protected FluxComponent(String id) {
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
    }

    /** Unique, immutable id used to retrieve or remove this component from its container. */
    public final String getId() {
        return id;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            onVisibilityChanged(visible);
        }
    }

    /** Override to react to visibility changes (e.g. toggle a wrapped native view). */
    protected void onVisibilityChanged(boolean visible) {
    }

    /** Free-form slot for caller bookkeeping (e.g. which plugin owns this component). Never read by Flux itself. */
    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluxComponent)) return false;
        return id.equals(((FluxComponent) o).id);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(id);
    }
}
