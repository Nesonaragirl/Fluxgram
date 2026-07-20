package com.caint.api;

import android.view.View;

/**
 * Implemented by CaintComponents that are backed by a real Android View
 * (buttons, menu items) so containers can place them into a host ViewGroup.
 *
 * Package-private -- this is an internal wiring detail, never part of the
 * public CaintComponent surface callers see.
 */
interface CaintViewProvider {
    View getView();
}
