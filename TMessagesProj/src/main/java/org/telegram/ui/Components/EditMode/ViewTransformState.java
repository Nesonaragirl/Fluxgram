package org.telegram.ui.Components.EditMode;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Stores all transform properties for one view.
 * Applied directly via Android's View transform APIs (no layout changes needed).
 */
public class ViewTransformState {

    public float tx, ty;              // translationX/Y (px)
    public float scaleX = 1f, scaleY = 1f;
    public float rotation;            // degrees
    public float cornerRadius;        // px  (0 = no rounding)

    private static final String PREFS = "fluxgram_transforms";
    private static final String KEY   = "v1";

    // ── Apply to a real View ──────────────────────────────────────────────────

    public void applyTo(View v) {
        v.setTranslationX(tx);
        v.setTranslationY(ty);
        v.setScaleX(scaleX);
        v.setScaleY(scaleY);
        v.setRotation(rotation);
        applyCornerRadius(v);
    }

    public void applyCornerRadius(View v) {
        if (cornerRadius > 0f) {
            final float r = cornerRadius;
            v.setClipToOutline(true);
            v.setOutlineProvider(new ViewOutlineProvider() {
                @Override public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), r);
                }
            });
        } else {
            v.setClipToOutline(false);
            v.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }
    }

    /** Remove all transforms so the view returns to its natural state. */
    public static void reset(View v) {
        v.setTranslationX(0); v.setTranslationY(0);
        v.setScaleX(1);       v.setScaleY(1);
        v.setRotation(0);
        v.setClipToOutline(false);
        v.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("tx",  tx);  o.put("ty",  ty);
        o.put("sx",  scaleX); o.put("sy", scaleY);
        o.put("rot", rotation);
        o.put("cr",  cornerRadius);
        return o;
    }

    public static ViewTransformState fromJson(JSONObject o) throws JSONException {
        ViewTransformState s = new ViewTransformState();
        s.tx           = (float) o.optDouble("tx",  0);
        s.ty           = (float) o.optDouble("ty",  0);
        s.scaleX       = (float) o.optDouble("sx",  1);
        s.scaleY       = (float) o.optDouble("sy",  1);
        s.rotation     = (float) o.optDouble("rot", 0);
        s.cornerRadius = (float) o.optDouble("cr",  0);
        return s;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public static Map<String, ViewTransformState> loadAll(Context ctx) {
        Map<String, ViewTransformState> map = new HashMap<>();
        String json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                         .getString(KEY, null);
        if (json == null) return map;
        try {
            JSONObject root = new JSONObject(json);
            for (Iterator<String> it = root.keys(); it.hasNext(); ) {
                String k = it.next();
                try { map.put(k, fromJson(root.getJSONObject(k))); }
                catch (JSONException ignored) {}
            }
        } catch (JSONException ignored) {}
        return map;
    }

    public static void saveAll(Context ctx, Map<String, ViewTransformState> states) {
        try {
            JSONObject root = new JSONObject();
            for (Map.Entry<String, ViewTransformState> e : states.entrySet()) {
                root.put(e.getKey(), e.getValue().toJson());
            }
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
               .edit().putString(KEY, root.toString()).apply();
        } catch (JSONException ignored) {}
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    public static ViewTransformState copy(ViewTransformState s) {
        ViewTransformState c = new ViewTransformState();
        c.tx = s.tx; c.ty = s.ty;
        c.scaleX = s.scaleX; c.scaleY = s.scaleY;
        c.rotation = s.rotation; c.cornerRadius = s.cornerRadius;
        return c;
    }
}
