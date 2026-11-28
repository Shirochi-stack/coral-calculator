package com.coral.calculator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.Random;

/**
 * What the six ultimates look like (their rules are in {@link PlusRun}, their numbers in {@link RunUltimates}). The big things are the
 * animated sheets of {@link Vfx} (real flames, shattered glass, sand, a wave of ink, dust, wisps, petals and roses), played and kept on her by
 * {@code RunGame}. Here is what goes with them: a light colour laid over her (never new frames, so whatever she is dressed in shows through),
 * a glow behind her, the bits that fly (embers, sand, drops of ink, pebbles, petals, each with a little weight and drift of its own), the
 * light over the whole screen with a bar that shows how long the ultimate has left, the banner that names it as it begins, the world gone
 * grey in the stilled hour, and the clocks on the blows held back.
 */
final class RunUltimateFx {
    private RunUltimateFx() {}

    /** The world with the colour mostly drawn out of it, a little cool: for the layer everything but her is drawn in, in the stilled hour. */
    private static Paint grey;

    static Paint greyscale() {
        if (grey == null) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(.08f);
            ColorMatrix cool = new ColorMatrix(new float[]{.92f, 0, 0, 0, 0, 0, .98f, 0, 0, 6, 0, 0, 1.08f, 0, 14, 0, 0, 0, 1, 0});
            matrix.postConcat(cool);
            grey = new Paint();
            grey.setColorFilter(new ColorMatrixColorFilter(matrix));
        }
        return grey;
    }

    /** The echoes of the legion: pale violet, over what she wears. */
    static final ColorFilter ECHO = new LightingColorFilter(0xFFC8B0FF, 0x00281048);
    /** Only a touch of colour over her, so that she stays herself (and what she wears shows): the flames and ink and petals round her do the rest. */
    private static final ColorFilter RED = new LightingColorFilter(0xFFFFE4DC, 0x00200800), INK = new LightingColorFilter(0xFFB8C4F0, 0x00060A20),
            GOLD = new LightingColorFilter(0xFFFFF4D0, 0x00201400), ROSE = new LightingColorFilter(0xFFFFE0EC, 0x00200610);

    /** The colour laid over her for an ultimate, or none. */
    static ColorFilter tint(int which, float clock) {
        switch (which) {
            case RunUltimates.BERSERK: return RED;
            case RunUltimates.TIDE: return INK;
            case RunUltimates.TITAN: return GOLD;
            case RunUltimates.BLOOM: return ROSE;
            default: return null;
        }
    }

    private static final Paint FILL = new Paint(Paint.ANTI_ALIAS_FLAG), LINE = new Paint(Paint.ANTI_ALIAS_FLAG), WORDS = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF BOX = new RectF();

    static {
        LINE.setStyle(Paint.Style.STROKE);
        WORDS.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
    }

    private static int alpha(int colour, float part) { return Color.argb(Math.max(0, Math.min(255, Math.round(255 * part))), Color.red(colour), Color.green(colour), Color.blue(colour)); }

    /** A soft round glow of a colour: lit in the middle, clear at the rim. */
    private static void glow(Canvas canvas, float x, float y, float radius, int colour, float strength) {
        FILL.setStyle(Paint.Style.FILL);
        FILL.setShader(new RadialGradient(x, y, Math.max(1, radius), alpha(colour, strength), alpha(colour, 0), Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, radius, FILL);
        FILL.setShader(null);
    }

    /**
     * Behind her pictures, only a soft light in the colour of the ultimate (the flames, ink, sand and petals themselves are the sheets of
     * {@link Vfx}); and for the giant, rings of gold going out from her feet. (Her middle, and her height.)
     */
    static void aura(Canvas canvas, PlusRun run, float x, float y, float tall, float clock, Paint paint, Paint stroke) {
        int which = run.ultimate();
        if (which == 0) return;
        int colour = RunUltimates.COLOURS[which];
        float beat = .5f + .5f * (float) Math.sin(clock * (which == RunUltimates.BERSERK ? 14 : 5));
        switch (which) {
            case RunUltimates.BERSERK:
                glow(canvas, x, y, tall * (.9f + .1f * beat), 0xFFFF5A1A, .45f);
                break;
            case RunUltimates.STILL:
                glow(canvas, x, y, tall * .85f, 0xFFB0D0FF, .28f);
                break;
            case RunUltimates.TIDE:
                glow(canvas, x, y, tall * 1.0f, 0xFF27348F, .5f);
                break;
            case RunUltimates.TITAN:
                glow(canvas, x, y, tall * .9f, colour, .35f);
                float feet = y + tall * .45f;
                LINE.setColor(alpha(colour, .7f));
                for (int wave = 0; wave < 2; wave++) {
                    float t = (clock * .9f + wave * .5f) % 1;
                    LINE.setStrokeWidth(tall * .03f * (1 - t));
                    LINE.setAlpha(Math.round(200 * (1 - t)));
                    BOX.set(x - tall * (.2f + .9f * t), feet - tall * .05f, x + tall * (.2f + .9f * t), feet + tall * .05f);
                    canvas.drawOval(BOX, LINE);
                }
                break;
            case RunUltimates.LEGION:
                glow(canvas, x, y, tall * .8f, colour, .3f);
                break;
            default:
                glow(canvas, x, y, tall * .9f, colour, .35f);
        }
    }

    // The bits that fly

    private static final int MOST = 180;
    private static final int EMBER = 0, SAND = 1, DROP = 2, PEBBLE = 3, PETAL = 4;
    private static final float[] PX = new float[MOST], PY = new float[MOST], VX = new float[MOST], VY = new float[MOST], LIFE = new float[MOST], MAX = new float[MOST],
            SPIN = new float[MOST], SEED = new float[MOST];
    private static final int[] KIND = new int[MOST];
    private static final boolean[] ALIVE = new boolean[MOST];
    private static final Random DICE = new Random(11);
    private static float owed;

    private static void spawn(int kind, float x, float y, float vx, float vy, float life) {
        for (int at = 0; at < MOST; at++) {
            if (ALIVE[at]) continue;
            ALIVE[at] = true;
            KIND[at] = kind;
            PX[at] = x;
            PY[at] = y;
            VX[at] = vx;
            VY[at] = vy;
            LIFE[at] = MAX[at] = life;
            SPIN[at] = DICE.nextFloat() * 6.28f;
            SEED[at] = DICE.nextFloat() * 6.28f;
            return;
        }
    }

    /** Pebbles thrown up from a footstep, where she stands. */
    static void pebbles(float x, float y) {
        for (int pebble = 0; pebble < 14; pebble++) spawn(PEBBLE, x + (DICE.nextFloat() - .5f) * 2, y - .1f, (DICE.nextFloat() - .5f) * 7, -4 - DICE.nextFloat() * 5, .8f + DICE.nextFloat() * .4f);
    }

    /** Drops of ink flung up where she breaks, or forms again. */
    static void splash(float x, float y) {
        for (int drop = 0; drop < 22; drop++) spawn(DROP, x, y - .4f, (DICE.nextFloat() - .5f) * 9, -3 - DICE.nextFloat() * 6, .7f + DICE.nextFloat() * .5f);
    }

    /** Petals thrown out from a rose that has bloomed. */
    static void petals(float x, float y) {
        for (int petal = 0; petal < 14; petal++) spawn(PETAL, x, y, (DICE.nextFloat() - .5f) * 7, -2 - DICE.nextFloat() * 4, 1.1f + DICE.nextFloat() * .8f);
    }

    /** Every bit moves a step: what falls falls, what rises rises, and each drifts as it goes; and the ultimate going now lets go of more. (Her place and size, in squares.) */
    static void update(PlusRun run, float seconds, float x, float feet, float tall) {
        float high = tall / 1.62f;
        for (int at = 0; at < MOST; at++) {
            if (!ALIVE[at]) continue;
            if ((LIFE[at] -= seconds) <= 0) {
                ALIVE[at] = false;
                continue;
            }
            float age = MAX[at] - LIFE[at];
            switch (KIND[at]) {
                case EMBER:
                    // Rising, and swaying as it rises; a little quicker as it goes.
                    VX[at] += (float) Math.sin(age * 7 + SEED[at]) * 5 * seconds;
                    VY[at] -= 1.2f * seconds;
                    break;
                case SAND:
                    VX[at] += (float) Math.cos(age * 2.4 + SEED[at]) * 1.6f * seconds;
                    VY[at] += (float) Math.sin(age * 1.7 + SEED[at]) * 1.2f * seconds - .3f * seconds;
                    break;
                case PETAL:
                    VX[at] += (float) Math.sin(age * 4 + SEED[at]) * 6 * seconds;
                    VY[at] = Math.min(1.6f, VY[at] + 4 * seconds);
                    SPIN[at] += seconds * 5;
                    break;
                default:
                    // Weight: a drop and a pebble fall.
                    VY[at] += 16 * seconds;
            }
            PX[at] += VX[at] * seconds;
            PY[at] += VY[at] * seconds;
        }
        int which = run.ultimate();
        float rate = which == RunUltimates.BERSERK ? 60 : which == RunUltimates.STILL ? 14 : which == RunUltimates.TIDE ? 45 : which == RunUltimates.BLOOM ? 26 : 0;
        owed += rate * seconds;
        while (owed >= 1) {
            owed -= 1;
            float middle = x + (DICE.nextFloat() - .5f) * .8f * high;
            switch (which) {
                case RunUltimates.BERSERK:
                    spawn(EMBER, middle, feet - DICE.nextFloat() * tall * .9f, (DICE.nextFloat() - .5f) * 1.5f, -2.5f - DICE.nextFloat() * 3, .9f + DICE.nextFloat() * .9f);
                    break;
                case RunUltimates.STILL:
                    spawn(SAND, x + (DICE.nextFloat() - .5f) * 3.4f * high, feet - DICE.nextFloat() * tall * 1.3f, 0, 0, 1.8f + DICE.nextFloat() * 1.4f);
                    break;
                case RunUltimates.TIDE:
                    spawn(DROP, x, feet - tall * .4f + (DICE.nextFloat() - .5f) * tall * .6f, -run.tideWay() * (2 + DICE.nextFloat() * 5), -2 - DICE.nextFloat() * 4, .5f + DICE.nextFloat() * .4f);
                    break;
                default:
                    spawn(PETAL, x + (DICE.nextFloat() - .5f) * 3 * high, feet - tall * 1.5f - DICE.nextFloat() * high, 0, 0, 1.6f + DICE.nextFloat());
            }
        }
    }

    /** The bits, drawn in front of her, in the land's squares seen from where the view is. */
    static void drawBits(Canvas canvas, float cameraX, float cameraY, float tile, float clock) {
        FILL.setStyle(Paint.Style.FILL);
        for (int at = 0; at < MOST; at++) {
            if (!ALIVE[at]) continue;
            float x = (PX[at] - cameraX) * tile, y = (PY[at] - cameraY) * tile, t = LIFE[at] / MAX[at];
            switch (KIND[at]) {
                case EMBER:
                    // A hot point with a trail below it: white-yellow at its heart, red as it goes out.
                    glow(canvas, x, y, tile * .16f, 0xFFFF7A20, .7f * t);
                    FILL.setColor(t > .5f ? 0xFFFFE9A0 : 0xFFFF6A1A);
                    FILL.setAlpha(Math.round(255 * Math.min(1, t * 2)));
                    canvas.drawCircle(x, y, tile * .035f * (.5f + t), FILL);
                    break;
                case SAND:
                    FILL.setColor(0xFFF4DC9A);
                    FILL.setAlpha(Math.round(220 * Math.min(1, Math.min(t * 3, (1 - t) * 6))));
                    canvas.drawCircle(x, y, tile * .03f, FILL);
                    break;
                case DROP:
                    FILL.setColor(0xFF161B4A);
                    FILL.setAlpha(Math.round(255 * Math.min(1, t * 3)));
                    canvas.drawCircle(x, y, tile * .07f, FILL);
                    FILL.setColor(0xFF9EE8FF);
                    FILL.setAlpha(Math.round(200 * Math.min(1, t * 3)));
                    canvas.drawCircle(x - tile * .022f, y - tile * .025f, tile * .02f, FILL);
                    break;
                case PEBBLE:
                    FILL.setColor(0xFF7A6A58);
                    FILL.setAlpha(Math.round(255 * Math.min(1, t * 3)));
                    canvas.drawRect(x - tile * .05f, y - tile * .05f, x + tile * .05f, y + tile * .05f, FILL);
                    break;
                default:
                    // A petal: a soft oval of rose, turning as it falls.
                    canvas.save();
                    canvas.rotate((float) Math.toDegrees(SPIN[at]), x, y);
                    BOX.set(x - tile * .1f, y - tile * .055f, x + tile * .1f, y + tile * .055f);
                    FILL.setColor(at % 3 == 0 ? 0xFFFFD2E0 : at % 3 == 1 ? 0xFFFF7FAF : 0xFFE2406F);
                    FILL.setAlpha(Math.round(235 * Math.min(1, Math.min(t * 3, (1 - t) * 8 + .3f))));
                    canvas.drawOval(BOX, FILL);
                    canvas.restore();
            }
        }
        FILL.setAlpha(255);
    }

    /** All the bits gone (a land begun again). */
    static void clearBits() { java.util.Arrays.fill(ALIVE, false); owed = 0; }

    // In the stilled hour

    /** In the stilled hour, over each creature that has blows held on it: a small clock, and how many are waiting to land. */
    static void held(Canvas canvas, PlusRun run, float cameraX, float cameraY, float tile, float clock, Paint paint, Paint text) {
        for (PlusRun.Foe foe : run.foes()) {
            int count = run.heldOn(foe);
            if (count == 0 || foe.flat) continue;
            float x = (foe.x + foe.wide() / 2 - cameraX) * tile, y = (foe.y - cameraY) * tile - tile * .3f, r = tile * .3f;
            FILL.setStyle(Paint.Style.FILL);
            FILL.setColor(0xEEF4F8FF);
            canvas.drawCircle(x, y, r, FILL);
            LINE.setColor(0xFF2A3A66);
            LINE.setStrokeWidth(tile * .05f);
            canvas.drawCircle(x, y, r, LINE);
            LINE.setStrokeWidth(tile * .04f);
            double hand = clock * 3;
            canvas.drawLine(x, y, x + (float) Math.cos(hand) * r * .75f, y + (float) Math.sin(hand) * r * .75f, LINE);
            canvas.drawLine(x, y, x, y - r * .5f, LINE);
            WORDS.setTextAlign(Paint.Align.LEFT);
            WORDS.setTextSize(tile * .42f);
            WORDS.setColor(0xFFFFFFFF);
            WORDS.setShadowLayer(tile * .08f, 0, 0, 0xFF000000);
            canvas.drawText("x" + count, x + r * 1.1f, y + tile * .15f, WORDS);
            WORDS.clearShadowLayer();
        }
    }

    /**
     * Over the whole screen, over the land and under her buttons: the light of the ultimate that is going (a red edge that beats in a rage, a
     * pale chill and a great faint clock in the stilled hour, ink at the edges in the tide, gold dust under the giant, violet for the legion,
     * rose in the bloom); the flash of one beginning or ending; the banner that names one as it begins; and a bar at the top that shows its
     * name and how much longer it lasts.
     */
    static void screen(Canvas canvas, PlusRun run, int width, int height, float clock, Paint paint, float flash, int shout, float shoutAge) {
        int which = run.ultimate();
        if (flash > 0) canvas.drawColor(alpha(RunUltimates.COLOURS[which != 0 ? which : run.lastEnded()], .4f * Math.min(1, flash / .3f)));
        if (which == 0) {
            if (run.exhausted() > 0) edge(canvas, width, height, 0xFF404048, .35f);
        } else {
            float beat = .5f + .5f * (float) Math.sin(clock * (which == RunUltimates.BERSERK ? 12 : 3));
            switch (which) {
                case RunUltimates.BERSERK:
                    edge(canvas, width, height, 0xFFE81408, .5f + .2f * beat);
                    break;
                case RunUltimates.STILL:
                    canvas.drawColor(alpha(0xFFC8DCFF, .08f));
                    edge(canvas, width, height, 0xFF0A1030, .5f);
                    float r = Math.min(width, height) * .38f, cx = width / 2f, cy = height / 2f;
                    LINE.setColor(alpha(0xFFFFFFFF, .16f));
                    LINE.setStrokeWidth(r * .03f);
                    canvas.drawCircle(cx, cy, r, LINE);
                    for (int tick = 0; tick < 12; tick++) {
                        double a = tick * Math.PI / 6;
                        canvas.drawLine(cx + (float) Math.cos(a) * r * .86f, cy + (float) Math.sin(a) * r * .86f, cx + (float) Math.cos(a) * r * .96f, cy + (float) Math.sin(a) * r * .96f, LINE);
                    }
                    LINE.setStrokeWidth(r * .025f);
                    double hand = -clock * .9;
                    canvas.drawLine(cx, cy, cx + (float) Math.cos(hand) * r * .8f, cy + (float) Math.sin(hand) * r * .8f, LINE);
                    canvas.drawLine(cx, cy, cx + (float) Math.cos(hand / 12) * r * .5f, cy + (float) Math.sin(hand / 12) * r * .5f, LINE);
                    break;
                case RunUltimates.TIDE:
                    edge(canvas, width, height, 0xFF1B2152, .6f);
                    break;
                case RunUltimates.TITAN:
                    edge(canvas, width, height, 0xFFFFC433, .25f);
                    break;
                case RunUltimates.LEGION:
                    edge(canvas, width, height, 0xFF9A6AE8, .35f);
                    break;
                default:
                    edge(canvas, width, height, 0xFFFF6FA0, .35f + .1f * beat);
            }
            bar(canvas, run, width, height, which);
        }
        if (shout > 0 && shoutAge < 1.9f) banner(canvas, width, height, shout, shoutAge);
    }

    /** The name of the ultimate that has begun, on a banner of its own: dark-backed, in a colour with a white edge, large, away from anything she says. It slides in, stays, and fades. */
    private static void banner(Canvas canvas, int width, int height, int which, float age) {
        float in = Math.min(1, age / .18f), out = Math.min(1, (1.9f - age) / .4f), show = Math.max(0, Math.min(in, out));
        float tall = height * .13f, wide = Math.min(width * .5f, tall * 6.2f), cx = width * .56f, top = height * .25f - (1 - in) * tall * .5f;
        float grow = .9f + .1f * in;
        FILL.setStyle(Paint.Style.FILL);
        FILL.setColor(alpha(0xFF0A0610, .78f * show));
        canvas.drawRoundRect(cx - wide / 2 * grow, top, cx + wide / 2 * grow, top + tall, tall * .22f, tall * .22f, FILL);
        LINE.setColor(alpha(RunUltimates.COLOURS[which], show));
        LINE.setStrokeWidth(tall * .045f);
        canvas.drawRoundRect(cx - wide / 2 * grow, top, cx + wide / 2 * grow, top + tall, tall * .22f, tall * .22f, LINE);
        WORDS.setTextAlign(Paint.Align.CENTER);
        float size = tall * .52f;
        WORDS.setTextSize(size);
        float measured = WORDS.measureText(RunUltimates.SHOUT[which]);
        if (measured > wide * .9f) WORDS.setTextSize(size * wide * .9f / measured);
        WORDS.setStyle(Paint.Style.STROKE);
        WORDS.setStrokeWidth(tall * .09f);
        WORDS.setColor(alpha(0xFF000000, show));
        canvas.drawText(RunUltimates.SHOUT[which], cx, top + tall * .66f, WORDS);
        WORDS.setStyle(Paint.Style.FILL);
        WORDS.setColor(alpha(which == RunUltimates.STILL ? 0xFFDDEBFF : which == RunUltimates.TIDE ? 0xFF8FB0FF : RunUltimates.COLOURS[which] | 0xFF000000, show));
        if (which == RunUltimates.BERSERK) WORDS.setColor(alpha(0xFFFF5A3C, show));
        canvas.drawText(RunUltimates.SHOUT[which], cx, top + tall * .66f, WORDS);
    }

    /** A soft edge of a colour round the screen: clear in the middle, the colour at its rim. */
    private static void edge(Canvas canvas, int width, int height, int colour, float strength) {
        FILL.setStyle(Paint.Style.FILL);
        FILL.setShader(new RadialGradient(width / 2f, height / 2f, Math.max(width, height) * .75f, new int[]{alpha(colour, 0), alpha(colour, 0), alpha(colour, strength)},
                new float[]{0, .55f, 1}, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, FILL);
        FILL.setShader(null);
    }

    /** The bar at the top: its name, and a line of its colour that shortens as it runs out. */
    private static void bar(Canvas canvas, PlusRun run, int width, int height, int which) {
        float wide = Math.min(width * .5f, height * 1.1f), tall = height * .045f, left = (width - wide) / 2f, top = height * .125f;
        float part = Math.max(0, Math.min(1, run.ultimateLeft() / RunUltimates.LASTS[which]));
        FILL.setStyle(Paint.Style.FILL);
        FILL.setColor(0xAA101018);
        canvas.drawRoundRect(left, top, left + wide, top + tall, tall / 2, tall / 2, FILL);
        FILL.setColor(RunUltimates.COLOURS[which]);
        canvas.drawRoundRect(left + tall * .15f, top + tall * .15f, left + tall * .15f + (wide - tall * .3f) * part, top + tall * .85f, tall * .35f, tall * .35f, FILL);
        WORDS.setStyle(Paint.Style.FILL);
        WORDS.setTextAlign(Paint.Align.CENTER);
        WORDS.setTextSize(tall * .68f);
        WORDS.setColor(Color.WHITE);
        WORDS.setShadowLayer(tall * .1f, 0, 0, 0xFF000000);
        canvas.drawText(RunUltimates.NAMES[which] + "  " + String.format(java.util.Locale.ROOT, "%.1f", run.ultimateLeft()) + " s", left + wide / 2, top + tall * .72f, WORDS);
        WORDS.clearShadowLayer();
    }
}
