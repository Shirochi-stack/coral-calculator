package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.SparseArray;

/**
 * The colour of Halgakos's hair. Her own is pink; the others are the colours she wins in Plus run,
 * and one that is won may be worn by the companion too. Only the animated, pink-haired Halgakos
 * is dyed: the still one has white hair of her own.
 */
final class Hair {
    static final int PINK = 0;
    /** A colour of the user's own mixing, open to them once every colour is won: what the companion's setting is then. */
    static final int CUSTOM = 9;
    /** In the order Plus run numbers its forms, after her own: {@link PlusRun#RED} is 1 and {@link PlusRun#WHITE} is 8. */
    static final String[] NAMES = {"Pink", "Red", "Blue", "Brown", "Green", "Yellow", "Purple", "Black", "White"};
    static final int[] COLOURS = {0xFFF4A3BE, 0xFFE0362F, 0xFF3B7BE0, 0xFFCF987A, 0xFF3FB56A, 0xFFF2C230, 0xFF9A4FD0, 0xFF3A3A44, 0xFFF4F4FA};
    /** Where Plus run keeps the colours won, among what the games remember: bit (colour - 1) is set for each. */
    static final String OWNED = "run_colours";
    /**
     * For each colour: its hue, how much stronger than her pink it is, how bright, and what is added to the brightness.
     * Brown is the shade of Chikuwa's hair, measured from her picture in Plus run.
     */
    private static final float[][] DYES = {{0, 2, .98f, 0}, {.6f, 1.7f, .98f, 0}, {.058f, 1.09f, .95f, 0}, {.37f, 1.5f, .92f, 0}, {.145f, 1.9f, 1.05f, 0},
            {.76f, 1.6f, .9f, 0}, {.7f, .5f, .3f, 0}, {.6f, .12f, .5f, .52f}};

    /** The pictures of her last made, all in one colour: they are large, and making them takes a moment. */
    private static final SparseArray<Bitmap> made = new SparseArray<>();
    private static int madeIn;

    private Hair() {}

    /** The colours won so far, a bit for each. */
    static int owned(Context context) {
        return context.getApplicationContext().getSharedPreferences("play", Context.MODE_PRIVATE).getInt(OWNED, 0);
    }

    /** Whether every colour has been won: then the user may mix a colour of their own for the companion, and have her glow. */
    static boolean all(Context context) {
        int every = (1 << NAMES.length - 1) - 1;
        return (owned(context) & every) == every;
    }

    /** Whether a look is a colour of the user's own: those are kept as the colour itself, with nothing let through. */
    static boolean custom(int look) { return look >>> 24 == 0xFF; }

    /** The colour of a look, to draw a mark of it in. */
    static int colour(int look) { return custom(look) ? look : COLOURS[look > 0 && look < COLOURS.length ? look : PINK]; }

    /** Whether the companion glows: the user's choice, once every colour is won. */
    static boolean glows(Context context) { return all(context) && new CompanionPrefs(context).hairGlow(); }

    static boolean owns(int owned, int colour) { return colour == PINK || colour > 0 && colour < NAMES.length && (owned & 1 << colour - 1) != 0; }

    /**
     * The look the companion wears: the colour chosen, if it has been won, a colour of the
     * user's own if every colour has been, and her own pink otherwise.
     */
    static int chosen(Context context) {
        CompanionPrefs prefs = new CompanionPrefs(context);
        int colour = prefs.hair();
        if (colour == CUSTOM) return all(context) ? 0xFF000000 | prefs.hairCustom() : PINK;
        return colour != PINK && owns(owned(context), colour) ? colour : PINK;
    }

    /** A picture of the animated Halgakos with her hair in a colour. It may take a moment the first time: ask away from the screen where that matters. */
    static Bitmap art(Context context, int resource, int colour) {
        if (!custom(colour) && (colour <= PINK || colour >= NAMES.length)) return BitmapFactory.decodeResource(context.getResources(), resource);
        synchronized (made) {
            if (colour != madeIn) {
                made.clear();
                madeIn = colour;
            }
            Bitmap kept = made.get(resource);
            if (kept != null) return kept;
        }
        Bitmap from = BitmapFactory.decodeResource(context.getResources(), resource);
        if (from == null) return null;
        Bitmap dyed = dyed(from, colour);
        synchronized (made) {
            if (colour == madeIn) made.put(resource, dyed);
        }
        return dyed;
    }

    /** The dye that turns the middle of her pink into a colour: its hue, and its strength and brightness as shares of her pink's. */
    private static float[] dyeOf(int colour) {
        float[] parts = new float[3];
        Color.colorToHSV(colour, parts);
        return new float[]{parts[0] / 360, parts[1] / .376f, parts[2] / .851f, 0};
    }

    /** A picture of her with her hair a colour: every pink of her hair, ears and tail, and nothing else, is turned to it. */
    static Bitmap dyed(Bitmap from, int colour) {
        int wide = from.getWidth(), tall = from.getHeight();
        int[] pixels = new int[wide * tall];
        from.getPixels(pixels, 0, wide, 0, 0, wide, tall);
        float[] dye = custom(colour) ? dyeOf(colour) : DYES[colour - 1], to = new float[3];
        for (int index = 0; index < pixels.length; index++) {
            int was = pixels[index];
            if (was >>> 24 == 0) continue;
            int red = was >> 16 & 255, green = was >> 8 & 255, blue = was & 255;
            int most = Math.max(red, Math.max(green, blue)), least = Math.min(red, Math.min(green, blue));
            if (most == least) continue;
            float bright = most / 255f, strong = (most - least) / (float) most, spread = most - least;
            if (strong <= .12f || strong >= .66f || bright <= .35f) continue;
            float hue = most == red ? ((green - blue) / spread + (green < blue ? 6 : 0)) / 6 : most == green ? ((blue - red) / spread + 2) / 6
                    : ((red - green) / spread + 4) / 6;
            float pink = hue >= .5f ? (hue - .86f) / .04f : (.055f - hue) / .02f;
            if (pink <= 0) continue;
            float weight = Math.min(1, pink) * Math.min(1, (strong - .12f) / .1f) * Math.min(1, (bright - .35f) / .15f) * Math.min(1, (.66f - strong) / .1f);
            to[0] = dye[0] * 360;
            to[1] = Math.min(1, strong * dye[1]);
            to[2] = Math.max(0, Math.min(1, bright * dye[2] + dye[3]));
            int now = Color.HSVToColor(to);
            pixels[index] = was & 0xFF000000 | Math.round(red + ((now >> 16 & 255) - red) * weight) << 16
                    | Math.round(green + ((now >> 8 & 255) - green) * weight) << 8 | Math.round(blue + ((now & 255) - blue) * weight);
        }
        Bitmap result = Bitmap.createBitmap(wide, tall, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, wide, 0, 0, wide, tall);
        return result;
    }
}
