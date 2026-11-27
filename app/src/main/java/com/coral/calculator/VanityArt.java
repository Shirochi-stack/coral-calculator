package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Her sheets, dressed: for a sheet and a style, the same sheet with the cloth painted over every frame ({@link VanityPaint}).
 * Dressing a whole sheet takes a moment, so it is done away from the screen: until it is ready, the sheet that was asked for
 * (her own look) is what is drawn, and the dressed one is there from then on.
 */
final class VanityArt {
    private final Context context;
    private final Map<String, Bitmap> made = new ConcurrentHashMap<>(), asked = new ConcurrentHashMap<>();
    private final java.util.Set<String> making = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile VanityLayers.Anchors anchors;

    VanityArt(Context context) { this.context = context.getApplicationContext(); }

    /** The sheet, dressed in the style; or the sheet itself with no style, or while the dressed one is being made. */
    Bitmap sheet(String sheet, Bitmap base, RunVanity.Style style) {
        if (style == null || base == null) return base;
        String key = sheet + "|" + style.key() + "|" + System.identityHashCode(base);
        Bitmap done = made.get(key);
        if (done != null) return done;
        if (made.size() > 40) made.clear();
        if (making.add(key)) {
            new Thread(() -> {
                try {
                    Bitmap dressed = dress(sheet, base, style);
                    if (dressed != null) made.put(key, dressed);
                } catch (RuntimeException | OutOfMemoryError ignored) {
                    // A sheet that cannot be dressed stays as she is.
                } finally {
                    making.remove(key);
                }
            }).start();
        }
        return base;
    }

    private VanityLayers.Anchors anchors() {
        if (anchors == null) {
            StringBuilder text = new StringBuilder();
            try (InputStream in = context.getAssets().open("vanity_anchors.txt")) {
                byte[] bytes = new byte[in.available()];
                int got = 0;
                while (got < bytes.length) {
                    int more = in.read(bytes, got, bytes.length - got);
                    if (more < 0) break;
                    got += more;
                }
                text.append(new String(bytes, 0, got, "UTF-8"));
            } catch (java.io.IOException ignored) {
                // No table: cloth with nothing added.
            }
            anchors = VanityLayers.parse(text.toString());
        }
        return anchors;
    }

    private Bitmap dress(String sheet, Bitmap base, RunVanity.Style style) {
        int[] g = VanityLayers.geometry(sheet);
        int id = context.getResources().getIdentifier("vanity_mask_" + sheet, "drawable", context.getPackageName());
        if (g == null || id == 0) return null;
        Bitmap mask = BitmapFactory.decodeResource(context.getResources(), id);
        if (mask == null || mask.getWidth() != base.getWidth() || mask.getHeight() != base.getHeight()) return null;
        Bitmap out = base.copy(Bitmap.Config.ARGB_8888, true);
        int cols = g[0], cw = g[2], ch = g[3];
        int[] px = new int[cw * ch], mk = new int[cw * ch];
        VanityLayers.Anchors table = anchors();
        for (int frame = 0; frame < cols * g[1]; frame++) {
            int x = frame % cols * cw, y = frame / cols * ch;
            out.getPixels(px, 0, cw, x, y, cw, ch);
            mask.getPixels(mk, 0, cw, x, y, cw, ch);
            for (int i = 0; i < mk.length; i++) mk[i] = mk[i] >>> 24;
            float[] anchor = table.get(sheet, frame);
            VanityPaint.paint(px, anchor == null ? null : mk, cw, ch, style, anchor, frame);
            out.setPixels(px, 0, cw, x, y, cw, ch);
        }
        mask.recycle();
        return out;
    }
}
