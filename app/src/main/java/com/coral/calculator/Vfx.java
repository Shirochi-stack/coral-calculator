package com.coral.calculator;

import android.content.Context;
import android.graphics.Canvas;

/**
 * The game's visual effects: animated sprite sheets (twelve frames in a row, bright cores and glowing edges, drawn once and kept in the
 * {@code runart} asset pack as {@code runart/vfx/NAME.webp}, never in the base module), and how to play them. Made by tools/make_vfx.py:
 * keep the table below (name, speed, loops, blend) in step with EFFECTS there.
 *
 * <p>To use one, once: {@code Vfx.init(context)}; then wherever a thing happens, {@code Vfx.play(Vfx.SLASH_HEAVY, x, y, size, angleDegrees, tint)}
 * with x and y in the pixels of the canvas it is drawn on (the effect's middle), {@code size} the width in pixels of the effect's longest side
 * and {@code tint} {@link #WHITE} for its own colours or a colour to tint it with. Each frame, {@code Vfx.update(seconds)} and, after what
 * they are to lie over is drawn, {@code Vfx.draw(canvas)}. An effect that is one shot goes by itself; one that loops (flames, poison,
 * stun stars) goes on until {@code Vfx.stop(handle)} or for the {@code seconds} that it is given. The ones that point somewhere (the slashes, the
 * thrust, the feathers, the beams) are drawn pointing right, or as a smile for the slashes, at angle 0.
 */
final class Vfx {
    private Vfx() {}

    static final int SLASH_LIGHT = 0, SLASH_HEAVY = 1, SLASH_CRESCENT = 2, SLASH_CROSS = 3, THRUST = 4, IMPACT_STAR = 5, SHOCKWAVE = 6, INK_SPLASH = 7,
            INK_EXPLOSION = 8, INK_SPLAT = 9, FIRE_BURST = 10, FLAMES = 11, ICE_BURST = 12, FREEZE = 13, POISON = 14, STUN = 15, LIGHTNING = 16, SPARKLE = 17,
            HEAL = 18, CRIT = 19, SHIELD_BREAK = 20, POOF = 21, DUST = 22, FEATHERS = 23, BEAM_IMPACT = 24, BEAM_START = 25, EXPLOSION = 26,
            // (The ultimates': flames round a body, a crown of dark flames, an eruption of fire; a shattered clock, a spiral of sand; a wave of ink; a footstep's dust;
            // ghostly wisps; a storm of petals, a rose in bloom.)
            RAMPAGE_BODY = 27, RAMPAGE_CROWN = 28, RAMPAGE_BURST = 29, CLOCK_SHARDS = 30, TIME_SAND = 31, INK_WAVE = 32, TITAN_STRIDE = 33, LEGION_WISPS = 34,
            PETAL_STORM = 35, ROSE_BURST = 36, COUNT = 37;

    static final String[] NAMES = {"slash_light", "slash_heavy", "slash_crescent", "slash_cross", "thrust", "impact_star", "shockwave", "ink_splash",
            "ink_explosion", "ink_splat", "fire_burst", "flames", "ice_burst", "freeze", "poison", "stun", "lightning", "sparkle",
            "heal", "crit", "shield_break", "poof", "dust", "feathers", "beam_impact", "beam_start", "explosion",
            "rampage_body", "rampage_crown", "rampage_burst", "clock_shards", "time_sand", "ink_wave", "titan_stride", "legion_wisps", "petal_storm", "rose_burst"};
    /** Frames to a sheet. */
    static final int FRAMES = 12;
    /** Frames a second at normal speed. */
    static final int[] FPS = {30, 26, 26, 28, 30, 28, 24, 24, 24, 22, 26, 20, 26, 22, 14, 14, 28, 22,
            20, 30, 24, 20, 18, 20, 26, 26, 22,
            20, 20, 24, 14, 14, 22, 20, 16, 18, 22};
    /** Whether it goes round and round (and so goes on until it is stopped). */
    static final boolean[] LOOP = {false, false, false, false, false, false, false, false, false, false, false, true, false, false, true, true, false, false,
            false, false, false, false, false, false, false, false, false,
            true, true, false, true, true, true, false, true, true, false};
    /** Whether it is drawn with the ADD blend (glows, sparks and fire add their light to what is behind them) or the normal one (ink, smoke, dust and gas cover it). */
    static final boolean[] ADD = {true, true, true, true, true, true, true, false, true, false, true, true, true, true, false, true, true, true,
            true, true, true, false, false, true, true, true, true,
            true, false, true, true, true, false, false, true, false, false};

    static final int WHITE = 0xFFFFFFFF;
    /** Flags for {@link VfxPlayer#play}: a second, wider and fainter pass of light round it; drawn the other way about; drawn with the normal blend even if it is a glow (on a light ground, where light added to it is lost). */
    static final int BLOOM = 1, MIRROR = 2, NORMAL = 4;
    /** Drawn in front of her, by {@link #drawFront}, and not behind her with the rest: flames over her shoulders, petals in front of her, the wave of ink she is. */
    static final int FRONT = 8;

    static String path(int id) { return "runart/vfx/" + NAMES[id] + ".webp"; }

    /** How long a one-shot lasts at normal speed, in seconds. */
    static float seconds(int id) { return FRAMES / (float) FPS[id]; }

    // The one player the game shares

    private static VfxPlayer shared;

    static void init(Context context) {
        if (shared == null) shared = new VfxPlayer(context.getApplicationContext());
    }

    /** Plays one at a place. Returns a handle to stop it by, or -1 where the library has not been started or the sheet is not there. */
    static int play(int id, float x, float y, float size, float angle, int tint) { return shared == null ? -1 : shared.play(id, x, y, size, angle, tint); }

    static int play(int id, float x, float y, float size, float angle, int tint, int flags, float speed, float seconds) {
        return shared == null ? -1 : shared.play(id, x, y, size, angle, tint, flags, speed, seconds);
    }

    static void move(int handle, float x, float y) { if (shared != null) shared.move(handle, x, y); }

    static void stop(int handle) { if (shared != null) shared.stop(handle); }

    static void update(float seconds) { if (shared != null) shared.update(seconds); }

    static void draw(Canvas canvas) { if (shared != null) shared.draw(canvas, false); }

    /** The ones played with {@link #FRONT}: drawn after her. */
    static void drawFront(Canvas canvas) { if (shared != null) shared.draw(canvas, true); }

    /** A soft round glow of a colour: an extra of light for a big hit, behind or in front of an effect. */
    static void glow(Canvas canvas, float x, float y, float radius, int colour, int alpha) { if (shared != null) shared.glow(canvas, x, y, radius, colour, alpha); }

    static void clear() { if (shared != null) shared.clear(); }
}
