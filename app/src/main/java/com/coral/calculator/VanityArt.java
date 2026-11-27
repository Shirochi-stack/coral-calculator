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
    private final Map<String, Bitmap> made = new ConcurrentHashMap<>();
    private final java.util.Set<String> making = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile VanityLayers.Anchors anchors;

    VanityArt(Context context) { this.context = context.getApplicationContext(); }

    /** The sheet, dressed in the style; or the sheet itself with no style, or while the dressed one is being made. */
    Bitmap sheet(String sheet, Bitmap base, RunVanity.Style style) {
        if (style == null || base == null) return base;
        String key = sheet + "|" + style.key() + "|" + System.identityHashCode(base);
        Bitmap done = made.get(key);
        if (done != null) return done;
        if (made.size() > 20) made.clear();
        if (making.add(key)) {
            new Thread(() -> {
                try {
                    Bitmap dressed = style.art != null ? drawn(sheet, base, style.art) : style.mid != 0 ? dress(sheet, base, style) : null;
                    if (style.ears != 0 || style.horns != 0) dressed = parts(sheet, dressed != null ? dressed : base, style);
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

    /**
     * The horns and the ears over a sheet: for each part, its pictures of the sheet (the new pixels) and where they go (a mask),
     * made by tools/make_vanity_heads.py. Where the mask is, the sheet shows the part's pixels and not its own.
     */
    private Bitmap parts(String sheet, Bitmap from, RunVanity.Style style) {
        Bitmap out = from.copy(Bitmap.Config.ARGB_8888, true);
        String[] names = {RunVanity.HORNS_ART[style.horns], RunVanity.EARS_ART[style.ears]};
        int[] pixels = null, patch = null, mask = null;
        for (String name : names) {
            if (name == null) continue;
            Bitmap picture = load("vanity/heads/" + name + "/" + sheet + ".webp"), where = load("vanity/heads/" + name + "/" + sheet + "_m.webp");
            if (picture == null || where == null || picture.getWidth() != out.getWidth() || picture.getHeight() != out.getHeight()
                    || where.getWidth() != out.getWidth() || where.getHeight() != out.getHeight()) continue;
            int n = out.getWidth() * out.getHeight();
            if (pixels == null) {
                pixels = new int[n];
                out.getPixels(pixels, 0, out.getWidth(), 0, 0, out.getWidth(), out.getHeight());
            }
            patch = new int[n];
            mask = new int[n];
            picture.getPixels(patch, 0, out.getWidth(), 0, 0, out.getWidth(), out.getHeight());
            where.getPixels(mask, 0, out.getWidth(), 0, 0, out.getWidth(), out.getHeight());
            for (int i = 0; i < n; i++) {
                int m = mask[i] >>> 24;
                if (m == 0) continue;
                int p = patch[i], o = pixels[i], pa = p >>> 24, oa = o >>> 24;
                int alpha = oa + (pa - oa) * m / 255;
                int rgb = o & 0xFFFFFF;
                if (pa > 0) {
                    int r = (o >> 16 & 255) + ((p >> 16 & 255) - (o >> 16 & 255)) * m / 255, g = (o >> 8 & 255) + ((p >> 8 & 255) - (o >> 8 & 255)) * m / 255,
                            b = (o & 255) + ((p & 255) - (o & 255)) * m / 255;
                    rgb = oa == 0 ? p & 0xFFFFFF : r << 16 | g << 8 | b;
                }
                pixels[i] = alpha << 24 | rgb;
            }
            picture.recycle();
            where.recycle();
        }
        if (pixels == null) return from;
        out.setPixels(pixels, 0, out.getWidth(), 0, 0, out.getWidth(), out.getHeight());
        return out;
    }

    private Bitmap load(String path) {
        try (InputStream in = context.getAssets().open(path)) {
            return BitmapFactory.decodeStream(in);
        } catch (java.io.IOException missing) {
            return null;
        }
    }

    /** An outfit that was drawn whole: its picture of this sheet, if it is in the app and is the sheet's size. */
    private Bitmap drawn(String sheet, Bitmap base, String outfit) {
        try (InputStream in = context.getAssets().open("vanity/" + outfit + "/" + sheet + ".webp")) {
            Bitmap picture = BitmapFactory.decodeStream(in);
            if (picture == null || picture.getWidth() != base.getWidth() || picture.getHeight() != base.getHeight()) return null;
            return picture;
        } catch (java.io.IOException missing) {
            return null;
        }
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
