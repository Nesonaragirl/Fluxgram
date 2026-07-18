package com.fluxgram.api;

/**
 * Generic callback interface used across Flux modules (Events, Dialogs, UI
 * click handlers, etc). Kept intentionally simple during the foundation
 * phase so it can be reshaped once real functionality lands.
 */
public interface FluxCallback {
    void run(Object data);
}
