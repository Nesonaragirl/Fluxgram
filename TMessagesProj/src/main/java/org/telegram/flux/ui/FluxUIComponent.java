package org.telegram.flux.ui;

/**
 * Base type for components created through a Flux.UI factory method
 * (createButton, createMenuItem, createDialog, createSettingsPage), as
 * opposed to {@link FluxComponent} subclasses that just wrap a caller-supplied
 * view. These components share a common, predictable surface regardless of
 * which native Telegram element backs them.
 */
public abstract class FluxUIComponent extends FluxComponent {

    public interface OnClickListener {
        void onClick(FluxUIComponent component);
    }

    protected FluxUIComponent(String id) {
        super(id);
    }

    public abstract FluxUIComponent setText(String text);

    public abstract FluxUIComponent setIcon(int iconResId);

    public abstract FluxUIComponent setEnabled(boolean enabled);

    public abstract FluxUIComponent onClick(OnClickListener listener);
}
