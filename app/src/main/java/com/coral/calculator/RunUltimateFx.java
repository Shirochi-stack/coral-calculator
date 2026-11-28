package com.coral.calculator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

/**
 * What the six ultimates look like (their rules are in {@link PlusRun}, their numbers in {@link RunUltimates}): the colour laid over her
 * (never new frames, so that whatever she is dressed in shows through), the aura round her, the light over the whole screen with a bar that
 * shows how long it has left, the world gone grey in the stilled hour, and the clocks on the blows held back. Everything is drawn from what
 * the rules say is going on, with the paints it is lent; the sheets of effects ({@link Vfx}) are played from {@code RunGame}.
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
    private static final ColorFilter RED = new LightingColorFilter(0xFFFF9088, 0x00380000), INK = new LightingColorFilter(0xFF6A78C0, 0x00080C30),
            GOLD = new LightingColorFilter(0xFFFFE8A8, 0x00302000), ROSE = new LightingColorFilter(0xFFFFC4DC, 0x00300818), STILL = new LightingColorFilter(0xFFFFFFFF, 0x00101830);

    /** The colour laid over her for an ultimate, or none: red in a rage (a little brighter as it beats), ink blue, gold, rose; the stilled hour leaves her her own, in the grey world. */
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
    private static final Path PATH = new Path();
    private static final RectF BOX = new RectF();

    static {
        LINE.setStyle(Paint.Style.STROKE);
        WORDS.setTypeface(Typeface.DEFAULT_BOLD);
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
     * The aura round her, behind her pictures: a rage is a red glow with flames that lick up from her; the stilled hour a ring of clock ticks
     * going round; the tide a swirl of ink where she was; the giant a gold glow with rings going out from her feet; the legion a ring of violet
     * light; the bloom a ring of petals going round her. (Her middle, and her height.)
     */
    static void aura(Canvas canvas, PlusRun run, float x, float y, float tall, float clock, Paint paint, Paint stroke) {
        int which = run.ultimate();
        if (which == 0) return;
        int colour = RunUltimates.COLOURS[which];
        float beat = .5f + .5f * (float) Math.sin(clock * (which == RunUltimates.BERSERK ? 14 : 6));
        float feet = y + tall * .45f;
        switch (which) {
            case RunUltimates.BERSERK:
                glow(canvas, x, y, tall * (.95f + .15f * beat), colour, .75f);
                // Flames licking up from her shoulders: tongues of red and then of gold, each leaning and flickering by its own beat.
                for (int tongue = 0; tongue < 7; tongue++) {
                    float lean = (float) Math.sin(clock * 9 + tongue * 1.7f), across = (tongue - 3) * tall * .13f, high = tall * (.5f + .22f * (float) Math.abs(Math.sin(clock * 11 + tongue * 2.3f)));
                    PATH.reset();
                    PATH.moveTo(x + across - tall * .07f, y + tall * .05f);
                    PATH.quadTo(x + across - tall * .04f + lean * tall * .05f, y - high * .55f, x + across + lean * tall * .09f, y - high);
                    PATH.quadTo(x + across + tall * .04f + lean * tall * .05f, y - high * .5f, x + across + tall * .07f, y + tall * .05f);
                    PATH.close();
                    FILL.setStyle(Paint.Style.FILL);
                    FILL.setColor(alpha(tongue % 2 == 0 ? 0xFFFF3A20 : 0xFFFFA030, .55f));
                    canvas.drawPath(PATH, FILL);
                }
                break;
            case RunUltimates.STILL:
                glow(canvas, x, y, tall * .9f, 0xFFB0D0FF, .35f);
                // A clock face of ticks round her, its one hand going backward.
                LINE.setColor(alpha(0xFFE8F4FF, .8f));
                for (int tick = 0; tick < 12; tick++) {
                    double a = tick * Math.PI / 6;
                    float inner = tall * (tick % 3 == 0 ? .66f : .72f), outer = tall * .8f;
                    LINE.setStrokeWidth(tall * (tick % 3 == 0 ? .035f : .018f));
                    canvas.drawLine(x + (float) Math.cos(a) * inner, y + (float) Math.sin(a) * inner, x + (float) Math.cos(a) * outer, y + (float) Math.sin(a) * outer, LINE);
                }
                double hand = -clock * 1.4;
                LINE.setStrokeWidth(tall * .03f);
                canvas.drawLine(x, y, x + (float) Math.cos(hand) * tall * .7f, y + (float) Math.sin(hand) * tall * .7f, LINE);
                break;
            case RunUltimates.TIDE:
                // She is a wave: a body of ink along her way, and drops flung off it.
                glow(canvas, x, y, tall * 1.1f, 0xFF27348F, .8f);
                for (int drop = 0; drop < 9; drop++) {
                    float t = (clock * 1.3f + drop * .37f) % 1, side = -run.tideWay();
                    FILL.setStyle(Paint.Style.FILL);
                    FILL.setColor(alpha(drop % 2 == 0 ? 0xFF1B2152 : 0xFFDDE8FF, 1 - t));
                    canvas.drawCircle(x + side * t * tall * 1.4f, y - tall * .2f + (float) Math.sin(drop * 2.1f) * tall * .35f - t * tall * .3f, tall * .07f * (1 - t * .5f), FILL);
                }
                break;
            case RunUltimates.TITAN:
                glow(canvas, x, y, tall * 1.0f, colour, .5f);
                // Rings going out from her feet along the ground, one after another.
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
                glow(canvas, x, y, tall * .95f, colour, .5f);
                LINE.setColor(alpha(0xFFE0D0FF, .6f));
                LINE.setStrokeWidth(tall * .02f);
                canvas.drawCircle(x, y, tall * (.7f + .03f * beat), LINE);
                break;
            default:
                glow(canvas, x, y, tall * 1.0f, colour, .55f);
                // Petals going round her.
                for (int petal = 0; petal < 8; petal++) {
                    double a = clock * 1.6 + petal * Math.PI / 4;
                    float px = x + (float) Math.cos(a) * tall * .75f, py = y + (float) Math.sin(a) * tall * .55f;
                    BOX.set(px - tall * .06f, py - tall * .1f, px + tall * .06f, py + tall * .1f);
                    canvas.save();
                    canvas.rotate((float) Math.toDegrees(a), px, py);
                    FILL.setStyle(Paint.Style.FILL);
                    FILL.setColor(alpha(petal % 2 == 0 ? 0xFFFF7FAF : 0xFFFFC4DC, .85f));
                    canvas.drawOval(BOX, FILL);
                    canvas.restore();
                }
        }
    }

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
            canvas.drawText("x" + count, x + r * 1.1f, y + tile * .15f, WORDS);
        }
    }

    /**
     * Over the whole screen, over the land and under her buttons: the light of the ultimate that is going (a red edge that beats in a rage, a
     * pale chill and a great clock in the stilled hour, ink at the edges in the tide, gold dust under the giant, violet for the legion, rose in
     * the bloom); the flash of one beginning or ending; and a bar at the top that shows its name and how much longer it lasts.
     */
    static void screen(Canvas canvas, PlusRun run, int width, int height, float clock, Paint paint, float flash) {
        int which = run.ultimate();
        if (flash > 0) canvas.drawColor(alpha(RunUltimates.COLOURS[which != 0 ? which : run.lastEnded()], .45f * Math.min(1, flash / .3f)));
        if (which == 0) {
            if (run.exhausted() > 0) {
                // Spent after the rage: the edges of the screen grey and heavy.
                edge(canvas, width, height, 0xFF404048, .35f);
            }
            return;
        }
        float beat = .5f + .5f * (float) Math.sin(clock * (which == RunUltimates.BERSERK ? 12 : 3));
        switch (which) {
            case RunUltimates.BERSERK:
                canvas.drawColor(alpha(0xFFB00010, .12f + .06f * beat));
                edge(canvas, width, height, 0xFFFF1A10, .55f + .25f * beat);
                break;
            case RunUltimates.STILL:
                canvas.drawColor(alpha(0xFFC8DCFF, .1f));
                edge(canvas, width, height, 0xFF0A1030, .5f);
                // A great clock, faint, going backward, in the middle of the screen.
                float r = Math.min(width, height) * .38f, cx = width / 2f, cy = height / 2f;
                LINE.setColor(alpha(0xFFFFFFFF, .22f));
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
                edge(canvas, width, height, 0xFF1B2152, .7f);
                break;
            case RunUltimates.TITAN:
                edge(canvas, width, height, 0xFFFFC433, .3f);
                break;
            case RunUltimates.LEGION:
                edge(canvas, width, height, 0xFF9A6AE8, .4f);
                break;
            default:
                canvas.drawColor(alpha(0xFFFF9EC0, .08f));
                edge(canvas, width, height, 0xFFFF6FA0, .4f + .1f * beat);
        }
        bar(canvas, run, width, height, which);
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
        WORDS.setTextAlign(Paint.Align.CENTER);
        WORDS.setTextSize(tall * .68f);
        WORDS.setColor(Color.WHITE);
        WORDS.setShadowLayer(tall * .1f, 0, 0, 0xFF000000);
        canvas.drawText(RunUltimates.NAMES[which] + "  " + String.format(java.util.Locale.ROOT, "%.1f", run.ultimateLeft()) + " s", left + wide / 2, top + tall * .72f, WORDS);
        WORDS.clearShadowLayer();
    }
}
