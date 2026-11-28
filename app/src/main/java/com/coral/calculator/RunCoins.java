package com.coral.calculator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.io.IOException;
import java.io.InputStream;

/**
 * The pluses she picks up, drawn as the collectibles they are: glossy plus-shaped coins turning as they hang (twelve frames each, in the runart pack as
 * {@code runart/coins/}), a bronze one for one plus, silver for five, gold for twenty-five and a prismatic one for a hundred; the glossary gem, which
 * makes pluses count double for a while; and what goes with them: a glow (given up when there are many), a pop where one is taken, a coin that flies
 * from where it was to her when it is drawn in by a magnet, a bump of the counter, the chip that counts the gem down, and her own glow while it is on.
 * All the frames are shared and nothing is made as it is drawn, so that a land full of them costs no more than the flat ones did.
 */
final class RunCoins {
    static final int BRONZE = 0, SILVER = 1, GOLD = 2, PRISM = 3, GEM = 4, POP = 5, FRAMES = 12;
    static final String[] FILES = {"plus_coin", "plus_silver", "plus_gold", "plus_prism", "gem_glossary", "coin_pop"};
    /** What a coin is worth, by its look. */
    static final int[] WORTH = {1, 5, 25, 100};
    /** The colour a glow behind each is. */
    private static final int[] GLOW = {0xFFFF5A5A, 0xFFBFD8FF, 0xFFFFC433, 0xFFFF8AF0, 0xFF7AF0FF};
    /** How many may glow at once: beyond that a swarm of them is drawn without. */
    private static final int GLOWING = 24;
    private static final int POPS = 40, FLIGHTS = 24;

    private final RunGame g;
    private final Bitmap[] sheets = new Bitmap[FILES.length];
    private final boolean[] missing = new boolean[FILES.length];
    private final Rect from = new Rect();
    private final RectF to = new RectF();
    private final PorterDuffColorFilter silverTint = new PorterDuffColorFilter(0xFFBFD8FF, PorterDuff.Mode.MULTIPLY);
    private final float[] popX = new float[POPS], popY = new float[POPS], popAge = new float[POPS], popSize = new float[POPS];
    private final int[] popTier = new int[POPS];
    private final boolean[] popOn = new boolean[POPS];
    private final float[] flyX = new float[FLIGHTS], flyY = new float[FLIGHTS], flyAge = new float[FLIGHTS];
    private final boolean[] flyOn = new boolean[FLIGHTS];
    private float bump, lastClock = -1, flicker;

    RunCoins(RunGame game) { g = game; }

    /** The tier a coin of a worth looks as. */
    static int tierOf(int worth) { return worth >= 100 ? PRISM : worth >= 25 ? GOLD : worth >= 5 ? SILVER : BRONZE; }

    private Bitmap sheet(int which) {
        if (sheets[which] != null || missing[which]) return sheets[which];
        try (InputStream in = g.context.getAssets().open("runart/coins/" + FILES[which] + ".webp")) {
            sheets[which] = BitmapFactory.decodeStream(in);
        } catch (IOException gone) {
            sheets[which] = null;
        }
        if (sheets[which] == null) missing[which] = true;
        return sheets[which];
    }

    /** Whether the new coins are in the app (the pack is there); if not, the old flat plus is drawn. */
    boolean ready() { return sheet(BRONZE) != null; }

    /** One frame of a sheet, its middle at a place, as wide as that (its own proportions kept). */
    private void put(Canvas canvas, int which, int frame, float x, float y, float size, int alpha) {
        Bitmap sheet = sheet(which);
        if (sheet == null) return;
        int fw = sheet.getWidth() / FRAMES, fh = sheet.getHeight();
        frame = ((frame % FRAMES) + FRAMES) % FRAMES;
        from.set(frame * fw, 0, frame * fw + fw, fh);
        float scale = size / Math.max(fw, fh), w = fw * scale, h = fh * scale;
        to.set(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
        g.paint.setAlpha(alpha);
        canvas.drawBitmap(sheet, from, to, g.paint);
        g.paint.setAlpha(255);
    }

    private void tick(float clock) {
        float seconds = lastClock < 0 ? 0 : Math.max(0, Math.min(.1f, clock - lastClock));
        lastClock = clock;
        bump = Math.max(0, bump - seconds * 3.2f);
        for (int index = 0; index < POPS; index++) {
            if (popOn[index] && (popAge[index] += seconds) > FRAMES / 26f) popOn[index] = false;
        }
        for (int index = 0; index < FLIGHTS; index++) {
            if (flyOn[index] && (flyAge[index] += seconds) > .26f) flyOn[index] = false;
        }
    }

    /** The pluses laid out in the land, turning where they hang, each at its own turn of the way round. */
    void drawWorld(Canvas canvas, PlusRun run, int first, int last, float cameraX, float cameraY, float tile, float clock) {
        tick(clock);
        int seen = 0;
        for (int[] plus : run.pluses()) if (plus[0] >= first - 1 && plus[0] <= last + 1) seen++;
        boolean glow = seen <= GLOWING;
        for (int[] plus : run.pluses()) {
            if (plus[0] < first - 1 || plus[0] > last + 1 || run.hidden(plus[0], plus[1])) continue;
            float x = (plus[0] + .5f - cameraX) * tile;
            float y = (plus[1] + .6f - cameraY) * tile + (float) Math.sin(clock * 3.2f + plus[0] * 1.3f) * tile * .07f;
            if (glow) g.glowAt(canvas, x, y, tile * .62f, GLOW[BRONZE], 70 + (int) (30 * Math.sin(clock * 3 + plus[0])));
            put(canvas, BRONZE, (int) (clock * 10 + plus[0] * 1.7f + plus[1] * .9f), x, y, tile * .74f, 255);
        }
    }

    /** What fell and lies about: a coin of its worth, or the gem. */
    void drawDrop(Canvas canvas, PlusRun.Drop drop, float x, float y, float tile, float clock) {
        int which = drop.gem ? GEM : tierOf(drop.worth);
        float size = tile * (which == GEM ? .86f : which == PRISM ? .86f : which == GOLD ? .74f : which == SILVER ? .6f : .5f);
        if (which >= GOLD) g.glowAt(canvas, x, y, size * .95f, GLOW[which], 90 + (int) (50 * Math.sin(clock * 4 + drop.x)));
        else if (which == SILVER) g.glowAt(canvas, x, y, size * .8f, GLOW[which], 60);
        put(canvas, which, (int) (drop.age * 10 + drop.x * 2.1f), x, y - size * .1f, size, 255);
    }

    /** A pop where a coin was taken (in the land's own place), of a tier's size. */
    void pop(float worldX, float worldY, int tier) {
        for (int index = 0; index < POPS; index++) {
            if (popOn[index]) continue;
            popOn[index] = true;
            popX[index] = worldX;
            popY[index] = worldY;
            popAge[index] = 0;
            popTier[index] = tier;
            popSize[index] = 1.3f + .55f * Math.min(tier, GEM);
            break;
        }
        bump = 1;
    }

    /** A coin that goes from where it was to her (one the magnet drew in), with a trail of sparkles. */
    void fly(float fromX, float fromY) {
        for (int index = 0; index < FLIGHTS; index++) {
            if (flyOn[index]) continue;
            flyOn[index] = true;
            flyX[index] = fromX;
            flyY[index] = fromY;
            flyAge[index] = 0;
            break;
        }
    }

    /** The pops and the flights in the air: over the land, over her. */
    void drawOver(Canvas canvas, float cameraX, float cameraY, float tile, float herX, float herY) {
        for (int index = 0; index < FLIGHTS; index++) {
            if (!flyOn[index]) continue;
            float through = Math.min(1, flyAge[index] / .26f);
            // a coin and two fainter ones behind it, which are the trail
            for (int tail = 2; tail >= 0; tail--) {
                float at = Math.max(0, through - tail * .12f), eased = at * at * (3 - 2 * at);
                float x = ((flyX[index] + (herX - flyX[index]) * eased) - cameraX) * tile, y = ((flyY[index] + (herY - flyY[index]) * eased) - cameraY) * tile
                        - (float) Math.sin(eased * Math.PI) * tile * .5f;
                put(canvas, tail == 0 ? BRONZE : POP, tail == 0 ? (int) (flyAge[index] * 30) : 2 + tail, x, y, tile * (tail == 0 ? .42f : .5f - tail * .1f), tail == 0 ? 255 : 150 - tail * 40);
            }
        }
        for (int index = 0; index < POPS; index++) {
            if (!popOn[index]) continue;
            int frame = Math.min(FRAMES - 1, (int) (popAge[index] * 26));
            boolean silver = popTier[index] == SILVER;
            if (silver) g.paint.setColorFilter(silverTint);
            put(canvas, POP, frame, (popX[index] - cameraX) * tile, (popY[index] - cameraY) * tile, tile * popSize[index], 255);
            if (silver) g.paint.setColorFilter(null);
        }
    }

    // The counter

    /** The scale the count is drawn at, a little over one just after a plus was taken. */
    float bumpScale() { return 1 + .3f * bump * bump; }

    /** Her plus, as the icon in a line of words: the coin, turned to its face and a little either side of it. */
    void icon(Canvas canvas, float x, float y, float size, int alpha, float clock) {
        int turn = (int) (clock * 2.4f) % 4;
        put(canvas, BRONZE, turn == 1 ? 1 : turn == 3 ? 11 : 0, x, y, size * (1 + .3f * bump * bump), alpha);
    }

    /** The chip that counts the gem down: its picture, the factor, the seconds left, and a bar of them. */
    void chip(Canvas canvas, PlusRun run, float left, float top, float clock) {
        float left10 = run.gem();
        if (left10 <= 0) return;
        float dp = g.density, high = dp * 28, wide = dp * 104;
        RectF at = new RectF(left, top, left + wide, top + high);
        boolean ending = left10 < 4 && (int) (clock * 6) % 2 == 0;
        g.paint.setStyle(Paint.Style.FILL);
        g.paint.setColor(ending ? 0xF04A2A5E : 0xF0262B58);
        canvas.drawRoundRect(at, high / 2, high / 2, g.paint);
        g.stroke.setColor(0xFF7AF0FF);
        g.stroke.setStrokeWidth(dp * 1.6f);
        canvas.drawRoundRect(at, high / 2, high / 2, g.stroke);
        // the bar along its foot
        float full = wide - dp * 20, filled = full * Math.min(1, left10 / PlusRun.GEM_MOST);
        g.paint.setColor(0x66FFFFFF);
        canvas.drawRoundRect(left + dp * 10, top + high - dp * 6, left + dp * 10 + full, top + high - dp * 3.5f, dp * 2, dp * 2, g.paint);
        g.paint.setColor(0xFF7AF0FF);
        canvas.drawRoundRect(left + dp * 10, top + high - dp * 6, left + dp * 10 + filled, top + high - dp * 3.5f, dp * 2, dp * 2, g.paint);
        g.paint.setColor(Color.BLACK);
        put(canvas, GEM, (int) (clock * 8), left + dp * 15, top + high / 2 - dp * 1.5f, dp * 24, 255);
        g.text.setTextAlign(Paint.Align.LEFT);
        g.text.setColor(Color.WHITE);
        g.text.setTypeface(Typeface.DEFAULT_BOLD);
        g.text.setTextSize(dp * 13);
        canvas.drawText("×" + PlusRun.GEM_FACTOR + "  " + (int) Math.ceil(left10) + " s", left + dp * 32, top + high / 2 + dp * 3, g.text);
        g.text.setTypeface(Typeface.DEFAULT);
    }

    /** A glow about her while the gem is on, faster as it runs out. */
    void aura(Canvas canvas, PlusRun run, float x, float y, float tall, float clock) {
        float left = run.gem();
        if (left <= 0) return;
        float beat = .5f + .5f * (float) Math.sin(clock * (left < 4 ? 14 : 4));
        g.glowAt(canvas, x, y, tall * 1.1f, 0xFF7AF0FF, 50 + (int) (45 * beat));
    }
}
