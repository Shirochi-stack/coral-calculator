package com.coral.calculator;

/**
 * Draws a cloth over one frame of her, in plain pixels (ARGB ints), so that it can be tested without a screen. The frame's
 * outfit mask says where her blouse, skirt, socks and ribbon are; the cloth is the shading of those pixels put through a
 * ramp of its colours, a pattern tile that is laid out from her waist (so it travels with her), a trim on the mask's
 * edge, and the light of a rose that moves from frame to frame. A pauldron and a stone are drawn at her anchors. Only
 * pixels that are already hers are changed, never an edge: a silhouette is what it was.
 */
final class VanityPaint {
    private VanityPaint() {}

    /** Frame anchors, in order: top, shoulder, chest, waist, hem, hand: x then y each. */
    static final int TOP = 0, SHOULDER = 2, CHEST = 4, WAIST = 6, HEM = 8, HAND = 10;

    private static int lerp(int a, int b, float t) {
        int r = (int) (Color3.r(a) + (Color3.r(b) - Color3.r(a)) * t), g = (int) (Color3.g(a) + (Color3.g(b) - Color3.g(a)) * t),
                bl = (int) (Color3.b(a) + (Color3.b(b) - Color3.b(a)) * t);
        return 0xFF000000 | clamp(r) << 16 | clamp(g) << 8 | clamp(bl);
    }

    private static int clamp(int v) { return v < 0 ? 0 : Math.min(v, 255); }

    private static final class Color3 {
        static int r(int c) { return c >> 16 & 255; }
        static int g(int c) { return c >> 8 & 255; }
        static int b(int c) { return c & 255; }
    }

    /** A hue, as a colour: for the moving light of a rose. */
    static int hue(float degrees, float saturation) {
        float h = (degrees % 360 + 360) % 360 / 60, f = h - (int) h, p = 1 - saturation, q = 1 - saturation * f, t = 1 - saturation * (1 - f);
        float[] rgb;
        switch ((int) h) {
            case 0: rgb = new float[]{1, t, p}; break;
            case 1: rgb = new float[]{q, 1, p}; break;
            case 2: rgb = new float[]{p, 1, t}; break;
            case 3: rgb = new float[]{p, q, 1}; break;
            case 4: rgb = new float[]{t, p, 1}; break;
            default: rgb = new float[]{1, p, q};
        }
        return 0xFF000000 | (int) (rgb[0] * 255) << 16 | (int) (rgb[1] * 255) << 8 | (int) (rgb[2] * 255);
    }

    /** How much a pattern darkens or lightens a pixel, from -1 to 1, in her own coordinates. */
    static float pattern(int kind, int x, int y) {
        int u = Math.floorMod(x, 12), v = Math.floorMod(y, 12);
        switch (kind) {
            case VanityKind.STARS: return Math.abs(u - 6) + Math.abs(v - 6) <= 1 ? 1 : 0;
            case VanityKind.STRIPES: return Math.floorMod(x + y, 8) < 2 ? -.5f : 0;
            case VanityKind.DIAMONDS: return Math.abs(Math.floorMod(x, 8) - 4) + Math.abs(Math.floorMod(y, 8) - 4) == 4 ? -.6f : 0;
            case VanityKind.ROSES: return Math.abs(u - 6) + Math.abs(v - 6) == 3 ? .8f : Math.abs(u - 6) + Math.abs(v - 6) == 0 ? -.8f : 0;
            default: return 0;
        }
    }

    /**
     * Paints one frame in place. {@code px} is the frame's pixels (w by h), {@code mask} its outfit mask (0..255, same size) or
     * null for a frame with no outfit, {@code anchors} its twelve numbers or null, and {@code phase} the frame's number, which
     * is where a moving light is. With a style that has no cloth (mid 0) only what is added is drawn.
     */
    static void paint(int[] px, int[] mask, int w, int h, RunVanity.Style style, float[] anchors, int phase) {
        if (style == null) return;
        boolean cloth = style.mid != 0 && mask != null;
        float wx = anchors != null ? anchors[WAIST] : w / 2f, wy = anchors != null ? anchors[WAIST + 1] : h / 2f, hy = anchors != null ? anchors[HEM + 1] : h;
        if (cloth) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int i = y * w + x, a = px[i] >>> 24, m = mask[i] & 255;
                    if (a == 0 || m == 0) continue;
                    int c = px[i];
                    float luma = ((c >> 16 & 255) * .3f + (c >> 8 & 255) * .59f + (c & 255) * .11f) / 255f;
                    // The skirt is dark and the blouse light: the ramp runs between its darkest and its lightest.
                    float t = Math.max(0, Math.min(1, (luma - .2f) / .7f));
                    int out = t < .5f ? lerp(style.dark, style.mid, t * 2) : lerp(style.mid, style.light, (t - .5f) * 2);
                    float p = pattern(style.pattern, Math.round(x - wx), Math.round(y - wy));
                    if (p > 0) out = lerp(out, style.light, p * .65f);
                    else if (p < 0) out = lerp(out, style.dark, -p * .5f);
                    if (style.trim != 0) {
                        boolean edge = x == 0 || y == 0 || x == w - 1 || y == h - 1 || (mask[i - 1] & 255) < 100 || (mask[i + 1] & 255) < 100
                                || (mask[i - w] & 255) < 100 || (mask[i + w] & 255) < 100;
                        if (edge && m > 90 || Math.abs(y - hy) < 1.6f && luma < .75f) out = lerp(out, style.trim, .75f);
                    }
                    if (style.shimmer) {
                        float band = (float) Math.sin((x * .9f + y * .55f) * .16f - phase * 1.1f);
                        if (band > .8f) out = lerp(out, hue(330 + phase * 27 + x, .35f), (band - .8f) * 4f);
                    }
                    float blend = m / 255f;
                    px[i] = a << 24 | lerp(c, out, blend) & 0xFFFFFF;
                }
            }
        }
        if (anchors == null) return;
        if (style.pauldron) disc(px, w, h, anchors[SHOULDER], anchors[SHOULDER + 1] - 1, Math.max(3, w / 22f), style.mid != 0 ? style.mid : style.trim, style.trim != 0 ? style.trim : style.light);
        if (style.brooch) disc(px, w, h, anchors[CHEST], anchors[CHEST + 1] + 1, Math.max(2, w / 45f), style.gem != 0 ? style.gem : style.trim, style.light);
    }

    /** A round thing, with a rim and a light, over pixels that are hers already. */
    private static void disc(int[] px, int w, int h, float cx, float cy, float r, int fill, int rim) {
        for (int y = (int) Math.floor(cy - r - 1); y <= Math.ceil(cy + r + 1); y++) {
            for (int x = (int) Math.floor(cx - r - 1); x <= Math.ceil(cx + r + 1); x++) {
                if (x < 0 || y < 0 || x >= w || y >= h || px[y * w + x] >>> 24 < 200) continue;
                float d = (float) Math.hypot(x - cx, y - cy);
                if (d > r) continue;
                int c = d > r - 1.1f ? rim : fill;
                if (d < r * .45f && x < cx && y < cy) c = lerp(fill, 0xFFFFFFFF, .6f);
                px[y * w + x] = px[y * w + x] & 0xFF000000 | c & 0xFFFFFF;
            }
        }
    }

    /** Names for the patterns, kept beside the drawing of them. */
    static final class VanityKind {
        static final int STARS = RunVanity.STARS, STRIPES = RunVanity.STRIPES, DIAMONDS = RunVanity.DIAMONDS, ROSES = RunVanity.ROSES;
    }
}
