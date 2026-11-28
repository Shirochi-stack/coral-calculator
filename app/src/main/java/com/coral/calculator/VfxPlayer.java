package com.coral.calculator;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Plays the effects of {@link Vfx}: each at a place, size, angle and tint, one frame of its sheet at a time, with the blend it wants. A fixed
 * pool of them, so that a swarm costs no allocation as it goes (when it is full, the one furthest along is the one that goes), and a sheet
 * is read the first time it is wanted and let go again when it has not been for a while.
 */
final class VfxPlayer {
    private static final int MAX = 160;
    private static final float KEEP = 25;

    private static final class Inst {
        boolean live;
        int id, gen, tint, flags;
        float x, y, size, angle, age, speed, life;
    }

    private final AssetManager assets;
    private final Inst[] pool = new Inst[MAX];
    private final Bitmap[] sheets = new Bitmap[Vfx.COUNT];
    private final float[] used = new float[Vfx.COUNT];
    private final boolean[] missing = new boolean[Vfx.COUNT];
    /** The colour each effect glows in: that of its brightest, most there pixels, found when its sheet is read. */
    private final int[] glowColour = new int[Vfx.COUNT];
    private final Paint normal = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG), add = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Rect from = new Rect();
    private final RectF to = new RectF();
    private final Map<Integer, PorterDuffColorFilter> tints = new HashMap<>();
    private Bitmap glow;
    private float clock, trimmed;
    private int generation;

    VfxPlayer(Context context) {
        assets = context.getAssets();
        for (int index = 0; index < MAX; index++) pool[index] = new Inst();
        add.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        glowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
    }

    int play(int id, float x, float y, float size, float angle, int tint) { return play(id, x, y, size, angle, tint, 0, 1, 0); }

    /**
     * One more effect. {@code speed} is how many times as fast as usual; {@code seconds} how long a looping one goes on (nought: until stopped; a
     * one-shot goes on until its twelve frames are over).
     */
    int play(int id, float x, float y, float size, float angle, int tint, int flags, float speed, float seconds) {
        if (id < 0 || id >= Vfx.COUNT || missing[id]) return -1;
        if (sheet(id) == null) return -1;
        int slot = -1;
        float most = -1;
        for (int index = 0; index < MAX; index++) {
            Inst one = pool[index];
            if (!one.live) {
                slot = index;
                break;
            }
            float along = one.age * one.speed * Vfx.FPS[one.id] / Vfx.FRAMES;
            if (along > most) {
                most = along;
                slot = index;
            }
        }
        Inst one = pool[slot];
        one.live = true;
        one.id = id;
        one.gen = ++generation & 0xFFFFFF;
        one.x = x;
        one.y = y;
        one.size = size;
        one.angle = angle;
        one.tint = tint;
        one.flags = flags;
        one.age = 0;
        one.speed = speed <= 0 ? 1 : speed;
        one.life = seconds;
        used[id] = clock;
        return one.gen << 8 | slot;
    }

    void move(int handle, float x, float y) {
        Inst one = find(handle);
        if (one != null) {
            one.x = x;
            one.y = y;
        }
    }

    void stop(int handle) {
        Inst one = find(handle);
        if (one != null) one.live = false;
    }

    private Inst find(int handle) {
        if (handle < 0) return null;
        Inst one = pool[handle & 0xFF];
        return one.live && one.gen == handle >> 8 ? one : null;
    }

    void clear() {
        for (Inst one : pool) one.live = false;
    }

    void update(float seconds) {
        clock += seconds;
        for (Inst one : pool) {
            if (!one.live) continue;
            one.age += seconds;
            float frames = one.age * one.speed * Vfx.FPS[one.id];
            if (Vfx.LOOP[one.id]) {
                if (one.life > 0 && one.age >= one.life) one.live = false;
            } else if (frames >= Vfx.FRAMES) {
                one.live = false;
            }
            if (one.live) used[one.id] = clock;
        }
        // (A sheet not wanted for a while is let go: they are a few megabytes each when they are open.)
        if (clock - trimmed > 5) {
            trimmed = clock;
            for (int id = 0; id < Vfx.COUNT; id++) {
                if (sheets[id] != null && clock - used[id] > KEEP) {
                    sheets[id].recycle();
                    sheets[id] = null;
                }
            }
        }
    }

    private Bitmap sheet(int id) {
        if (sheets[id] != null) return sheets[id];
        if (missing[id]) return null;
        try (InputStream in = assets.open(Vfx.path(id))) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            sheets[id] = BitmapFactory.decodeStream(in, null, options);
        } catch (IOException gone) {
            sheets[id] = null;
        }
        if (sheets[id] == null) missing[id] = true;
        else glowColour[id] = averageColour(sheets[id]);
        return sheets[id];
    }

    /** The colour of what is most there and brightest in a sheet, sampled: what a glow behind it is to be. */
    private static int averageColour(Bitmap sheet) {
        long r = 0, g = 0, b = 0, weight = 0;
        int step = Math.max(1, Math.min(sheet.getWidth(), sheet.getHeight()) / 24);
        for (int y = 0; y < sheet.getHeight(); y += step) {
            for (int x = 0; x < sheet.getWidth(); x += step) {
                int pixel = sheet.getPixel(x, y), alpha = pixel >>> 24;
                if (alpha < 200) continue;
                int red = Color.red(pixel), green = Color.green(pixel), blue = Color.blue(pixel);
                int sat = Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue));
                int w = 1 + sat;
                r += (long) red * w;
                g += (long) green * w;
                b += (long) blue * w;
                weight += w;
            }
        }
        return weight == 0 ? 0xFFFFFFFF : Color.rgb((int) (r / weight), (int) (g / weight), (int) (b / weight));
    }

    /** A tint on a colour: the colour itself for white. */
    private static int tinted(int tint, int colour) {
        if (tint == Vfx.WHITE) return colour;
        return Color.rgb(Color.red(tint) * Color.red(colour) / 255, Color.green(tint) * Color.green(colour) / 255, Color.blue(tint) * Color.blue(colour) / 255);
    }

    /** Whether a sheet is in the app (the pack it is in is there). */
    boolean has(int id) { return id >= 0 && id < Vfx.COUNT && !missing[id] && sheet(id) != null; }

    private Paint paintFor(boolean light, int tint, int alpha) {
        Paint paint = light ? add : normal;
        if (tint == Vfx.WHITE) {
            paint.setColorFilter(null);
        } else {
            PorterDuffColorFilter filter = tints.get(tint);
            if (filter == null) {
                filter = new PorterDuffColorFilter(tint | 0xFF000000, PorterDuff.Mode.MULTIPLY);
                tints.put(tint, filter);
            }
            paint.setColorFilter(filter);
        }
        paint.setAlpha(alpha);
        return paint;
    }

    void draw(Canvas canvas) {
        for (Inst one : pool) {
            if (!one.live) continue;
            Bitmap sheet = sheets[one.id];
            if (sheet == null) continue;
            int frame = (int) (one.age * one.speed * Vfx.FPS[one.id]);
            if (Vfx.LOOP[one.id]) frame %= Vfx.FRAMES;
            else frame = Math.min(Vfx.FRAMES - 1, frame);
            int fw = sheet.getWidth() / Vfx.FRAMES, fh = sheet.getHeight();
            from.set(frame * fw, 0, frame * fw + fw, fh);
            float scale = one.size / Math.max(fw, fh), w = fw * scale, h = fh * scale;
            // (A looping one that has a time to go on for fades out over its last moments.)
            int alpha = 255;
            if (Vfx.LOOP[one.id] && one.life > 0 && one.life - one.age < .25f) alpha = Math.max(0, Math.round(255 * (one.life - one.age) / .25f));
            boolean lit = Vfx.ADD[one.id] && (one.flags & Vfx.NORMAL) == 0;
            canvas.save();
            canvas.translate(one.x, one.y);
            canvas.rotate(one.angle);
            if ((one.flags & Vfx.MIRROR) != 0) canvas.scale(-1, 1);
            if ((one.flags & Vfx.BLOOM) != 0 && lit) {
                // (A soft round light behind it, in its own colour, as big as it is and a little more: a bloom.)
                canvas.restore();
                int colour = tinted(one.tint, glowColour[one.id]);
                float radius = Math.max(w, h) * .62f;
                glow(canvas, one.x, one.y, radius, colour, alpha * 110 / 255);
                canvas.save();
                canvas.translate(one.x, one.y);
                canvas.rotate(one.angle);
                if ((one.flags & Vfx.MIRROR) != 0) canvas.scale(-1, 1);
            }
            to.set(-w / 2, -h / 2, w / 2, h / 2);
            canvas.drawBitmap(sheet, from, to, paintFor(lit, one.tint, alpha));
            canvas.restore();
        }
        add.setColorFilter(null);
        normal.setColorFilter(null);
        add.setAlpha(255);
        normal.setAlpha(255);
    }

    /** A soft round glow in light, in a colour, as an extra under or over an effect. */
    void glow(Canvas canvas, float x, float y, float radius, int colour, int alpha) {
        if (glow == null) {
            int side = 128;
            glow = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888);
            Canvas on = new Canvas(glow);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setShader(new RadialGradient(side / 2f, side / 2f, side / 2f, new int[]{0xFFFFFFFF, 0x99FFFFFF, 0x33FFFFFF, 0x00FFFFFF}, new float[]{0, .25f, .6f, 1}, Shader.TileMode.CLAMP));
            on.drawCircle(side / 2f, side / 2f, side / 2f, paint);
        }
        glowPaint.setColorFilter(tintFilter(colour));
        glowPaint.setAlpha(Math.max(0, Math.min(255, alpha)));
        to.set(x - radius, y - radius, x + radius, y + radius);
        canvas.drawBitmap(glow, null, to, glowPaint);
        glowPaint.setColorFilter(null);
    }

    private PorterDuffColorFilter tintFilter(int colour) {
        PorterDuffColorFilter filter = tints.get(colour);
        if (filter == null) {
            filter = new PorterDuffColorFilter(colour | 0xFF000000, PorterDuff.Mode.MULTIPLY);
            tints.put(colour, filter);
        }
        return filter;
    }

    /** How many are playing. */
    int playing() {
        int count = 0;
        for (Inst one : pool) if (one.live) count++;
        return count;
    }

    static int rgb(int r, int g, int b) { return Color.rgb(r, g, b); }
}
