package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * The tricks that Plus run unlocks: scenes out of its story, played over the user's screen.
 * Each is drawn from the time since it began, with what {@link TrickBase} gives it.
 */
abstract class TrickRun extends TrickBase {
    private static final int CORAL = CalculatorLayout.CORAL;
    private static final int STAMP_RED = 0xFFD8342B, SEAL_GREEN = 0xFF2E9E5B, INK_BLUE = 0xFF27348F;
    /** Her idle frames in each colour of hair, made as they are first wanted. */
    private final Bitmap[] dyedIdle = new Bitmap[Hair.NAMES.length];
    private Bitmap ownIdle;

    TrickRun(Context context, int kind, boolean dark, Bitmap art, Bitmap pet) { super(context, kind, dark, art, pet); }

    // Things several scenes draw

    /** A sign of a sum that has got up on two legs: it bobs as it goes. */
    void walker(Canvas c, int sign, float x, float y, float size, float step, float alpha) {
        float bob = Math.abs((float) Math.sin(step)) * size * .14f, swing = (float) Math.sin(step) * size * .22f;
        pen(0xFF3A2A22, size * .09f, alpha);
        c.drawLine(x - size * .16f, y + size * .3f - bob, x - size * .16f + swing, y + size * .62f, stroke);
        c.drawLine(x + size * .16f, y + size * .3f - bob, x + size * .16f - swing, y + size * .62f, stroke);
        piece(c, sign, x, y - bob, size, 0, alpha);
    }

    /** A row of the user's apps across the screen, at a height, with a name under each: where one of them is. */
    float appX(int which, int count) { return L + W * (which + 1) / (count + 1f); }

    int appCount() { return W > dp(430) ? 5 : 4; }

    float appRow() { return clamp(floor - tall - dp(170), T + dp(120), cy); }

    /** A rubber stamp's mark: words in a ring, askew, thumped down from a moment on. */
    void stamp(Canvas c, String words, float x, float y, float size, float turn, int colour, float since) {
        if (since < 0) return;
        float slam = since < .14f ? 2.2f - 1.2f * (since / .14f) : 1, alpha = Math.min(1, since / .08f) * .9f;
        c.save();
        c.rotate(turn, x, y);
        c.scale(slam, slam, x, y);
        text.setTextSize(size);
        float half = text.measureText(words) / 2 + size * .7f, high = size * 1.05f;
        pen(colour, size * .16f, alpha);
        box.set(x - half, y - high, x + half, y + high);
        c.drawRoundRect(box, size * .3f, size * .3f, stroke);
        box.inset(size * .2f, size * .2f);
        pen(colour, size * .06f, alpha);
        c.drawRoundRect(box, size * .2f, size * .2f, stroke);
        words(c, words, x, y, size, colour, alpha);
        c.restore();
    }

    /** A ribbon tied in a bow. */
    void bow(Canvas c, float x, float y, float size, int colour, float alpha) {
        fill(colour, alpha);
        path.reset();
        path.moveTo(x, y);
        path.lineTo(x - size, y - size * .6f);
        path.lineTo(x - size, y + size * .6f);
        path.close();
        path.moveTo(x, y);
        path.lineTo(x + size, y - size * .6f);
        path.lineTo(x + size, y + size * .6f);
        path.close();
        path.moveTo(x - size * .1f, y);
        path.lineTo(x - size * .5f, y + size * 1.2f);
        path.lineTo(x - size * .1f, y + size * 1.05f);
        path.close();
        path.moveTo(x + size * .1f, y);
        path.lineTo(x + size * .5f, y + size * 1.2f);
        path.lineTo(x + size * .1f, y + size * 1.05f);
        path.close();
        c.drawPath(path, paint);
        c.drawCircle(x, y, size * .26f, paint);
    }

    /** A crack, as a jagged line out from a point, so long and which way. */
    void crack(Canvas c, float x, float y, float length, float way, int which, float alpha) {
        path.reset();
        path.moveTo(x, y);
        float along = 0;
        for (int bend = 1; bend <= 5; bend++) {
            along += length / 5;
            float off = (rnd(which, bend) - .5f) * length * .22f;
            path.lineTo(x + (float) Math.cos(way) * along - (float) Math.sin(way) * off, y + (float) Math.sin(way) * along + (float) Math.cos(way) * off);
        }
        pen(0xAA000000, dp(3.2f), alpha);
        c.drawPath(path, stroke);
        pen(Color.WHITE, dp(1.5f), alpha);
        c.drawPath(path, stroke);
    }

    /** A blot of ink where a shot landed, with a run under it. */
    void blot(Canvas c, float x, float y, float size, int which, float alpha) {
        fill(INK_BLUE, alpha);
        c.drawCircle(x, y, size, paint);
        for (int lobe = 0; lobe < 5; lobe++) {
            double way = lobe * 1.257 + rnd(which, lobe) * .8;
            c.drawCircle(x + (float) Math.cos(way) * size * .85f, y + (float) Math.sin(way) * size * .85f, size * (.3f + rnd(which, lobe + 9) * .3f), paint);
        }
        box.set(x - size * .16f, y, x + size * .16f, y + size * (1.4f + rnd(which, 20) * 1.6f));
        c.drawRoundRect(box, size * .16f, size * .16f, paint);
    }

    // The scenes

    /** The World's Largest Book lands on her, as it did on her day off, and her output bursts into pluses. */
    void book(Canvas c) {
        float spot = clamp(cx, L + wide, R - wide), size = Math.min(W * .6f, dp(250));
        if (t < 1.5f) go(spot, floor, dp(430));
        // It comes to rest on top of her: what is left of her shows under it.
        float lands = floor - tall * .36f - size * .36f, bookY = T - size + (lands - (T - size)) * into(p(1.6f, 2.4f)), since = t - 2.4f;
        c.save();
        c.translate(shake(since, dp(10)), shake(since, dp(6)));
        if (t > 1.2f && t < 6.9f) {
            float grown = p(1.2f, 2.4f);
            fill(0x66000000, grown);
            c.drawOval(spot - size * .42f * grown, floor - dp(7), spot + size * .42f * grown, floor + dp(7), paint);
        }
        if (in(2.4f, 6.4f)) {
            put(spot, floor);
            wear(LOSE, 6, 3, 1.1f);
            squash = .58f;
        } else if (in(6.4f, 9.4f)) {
            put(spot, floor);
            armed(2, 7);
        }
        her(c);
        float lifted = p(6.4f, 6.9f);
        if (in(1.6f, 6.9f)) piece(c, BOOK, spot, bookY - lifted * tall * .8f, size * (1 - lifted * .8f), t >= 2.4f ? -7 : 0, 1);
        for (int which = 0; which < 22 && since >= 0; which++) {
            float side = rnd(which, 1) - .5f, x = spot + side * W * .9f * Math.min(since, 1.6f) / 1.6f;
            float hop = Math.max(0, (dp(210) + rnd(which, 2) * dp(260)) * since - dp(520) * since * since);
            if (since > 1.6f) x += (since - 1.6f) * dp(64) * (side < 0 ? -1 : 1);
            if (hop > 0) piece(c, PLUS, x, floor - dp(12) - hop, dp(26), since * 300 * side, 1);
            else walker(c, PLUS, x, floor - dp(16), dp(26), since * 9 + which, 1);
        }
        if (since >= 0 && since < .35f) fx(c, BURST, spot, lands, size * (1 + since), 0, 1 - since / .35f);
        c.restore();
        line(.2f, 1.7f, "My one day off. One ice cream. What could go wrong Sloppy~♡");
        line(2.7f, 2.7f, "...The World's Largest Book. It found me again Sloppy~♡");
        line(6.5f, 2.7f, "And my output burst into pluses. Look at them go Sloppy~♡");
        line(9.4f, 1.6f, "Help me pick them up Sloppy~♡");
    }

    /** Tsubakieh gives the screen a tap with Normal No. 2, and the names fall off the user's apps; Halgakos puts them back. */
    void bat(Canvas c) {
        pickApps(5);
        int count = appCount();
        float row = appRow(), size = dp(52), batter = L + dp(46);
        String[] wrong = {"fish cake", "a brick", "normal app", "not bread", "a Tuesday"};
        float swing = t - 2.6f;
        strip(c, TrickArt.run_tsubakieh, 6, t < 1.8f ? 0 : t < 2.5f ? 2 : t < 3.3f ? 3 : t < 8.6f ? 1 : 5,
                L - dp(70) + (batter + dp(70) - L) * out(p(.3f, 1.8f)), floor, tall * 1.05f, false, 1);
        // The blow goes along the row; what it passes loses its name, and the name walks off.
        float wave = batter + (R - batter) * p(2.6f, 3.4f);
        if (swing >= 0 && swing < .8f) {
            pen(Color.WHITE, dp(5), 1 - swing / .8f);
            c.drawArc(wave - dp(60), row - dp(70), wave + dp(60), row + dp(70), -60, 120, false, stroke);
        }
        for (int which = 0; which < count; which++) {
            float x = appX(which, count), fixed = t - (5.9f + which * .62f), hit = wave >= x ? t - (2.6f + .8f * (x - batter) / (R - batter)) : -1;
            float jolt = hit >= 0 && hit < .4f ? (float) Math.sin(hit * 50) * dp(5) * (1 - hit / .4f) : 0;
            icon(c, which, x + jolt, row, size, hit >= 0 && fixed < 0 ? (float) Math.sin(t * 3 + which) * 5 : 0, 1);
            if (hit < 0 || fixed >= 0) {
                plate(c, tag(which), x, row + size * .86f, dp(11.5f), 0xFF292B2A, 0xF2FFFFFF, 1);
                if (fixed >= 0 && fixed < .5f) piece(c, STAR, x, row - size * .7f - fixed * dp(40), dp(22), fixed * 300, 1 - fixed / .5f);
            } else {
                // Its name, wrong now, on two legs.
                float away = Math.max(0, hit - 1.3f), nameX = x + away * dp(46) * (which % 2 == 0 ? 1 : -1), nameY = row + size * .86f + Math.min(away, .6f) * dp(50);
                plate(c, wrong[which % wrong.length], nameX, nameY - Math.abs((float) Math.sin(away * 10)) * dp(4), dp(11.5f), Color.WHITE, 0xF2D8342B, 1);
                if (away > 0) {
                    pen(0xFF3A2A22, dp(2), 1);
                    float kick = (float) Math.sin(away * 10) * dp(5);
                    c.drawLine(nameX - dp(10), nameY + dp(9), nameX - dp(10) + kick, nameY + dp(19), stroke);
                    c.drawLine(nameX + dp(10), nameY + dp(9), nameX + dp(10) - kick, nameY + dp(19), stroke);
                }
            }
        }
        if (in(8.7f, 11)) plate(c, "That seems normal.", batter + dp(40), floor - tall * 1.25f, dp(12), 0xFF292B2A, 0xF2FFFFFF, p(8.7f, 9));
        if (t < 5.6f) go(clamp(cx + dp(30), L + wide, R - wide), floor, dp(360));
        else if (t < 8.6f) go(clamp(appX(Math.min(count - 1, (int) ((t - 5.6f) / .62f)), count), L + wide / 2, R - wide / 2), floor, dp(520));
        else act(PetArt.WAVE);
        line(.3f, 2, "Tsubakieh's warming up. Normally. Near your apps Sloppy~♡");
        line(3.3f, 2.2f, "She knocked the meaning off them! Look at the names Sloppy~♡");
        line(5.7f, 2.7f, "Names stay names. That's the basics. Hold still Sloppy~♡");
        line(8.7f, 2, "There. Fixed. You're welcome Sloppy~♡");
    }

    /** Illya rewrites the screen with herself as its only heroine, and Halgakos mends the cast list. */
    void rewrite(Canvas c) {
        float pink = ease(p(1.4f, 3)) * (1 - p(9.2f, 10.6f)), cardY = clamp(T + H * .3f, T + dp(150), floor - tall - dp(120));
        wash(c, 0x66FF7FB2, pink);
        for (int which = 0; which < 26; which++) {
            float began = 1.5f + rnd(which, 1) * 6, fell = t - began;
            if (fell < 0 || fell > 3.2f) continue;
            fx(c, HEART, L + rnd(which, 2) * W + (float) Math.sin(fell * 3 + which) * dp(14), T - dp(20) + fell * (H * .36f), dp(20 + rnd(which, 3) * 22),
                    (float) Math.sin(fell * 2 + which) * 20, Math.min(1, 3.2f - fell));
        }
        float illyaX = R - dp(70), illyaY = cardY + dp(96) + (float) Math.sin(t * 2.2f) * dp(5);
        strip(c, TrickArt.run_illya, 4, t < 1.3f ? 0 : t < 7.6f ? 1 : t < 9 ? 2 : 1, illyaX + (1 - out(p(.2f, 1.1f))) * dp(160), illyaY, tall * 1.1f, true, 1);
        // The cast list, written a line at a time.
        float shown = p(2.4f, 2.9f), middle = Math.min(cx, illyaX - dp(140) < L + dp(120) ? cx : cx - dp(30));
        if (shown > 0) {
            fill(0xF7FFFFFF, shown);
            box.set(middle - dp(128), cardY - dp(46), middle + dp(128), cardY + dp(46));
            c.drawRoundRect(box, dp(14), dp(14), paint);
            pen(0xFFFF7FB2, dp(2), shown);
            c.drawRoundRect(box, dp(14), dp(14), stroke);
            String heroine = "Heroine: Illya", cast = t < 8.2f ? "Supporting cast: none" : "Supporting cast: everyone";
            words(c, heroine.substring(0, Math.round(heroine.length() * p(2.8f, 3.6f))), middle, cardY - dp(18), dp(17), 0xFFE0457F, 1);
            words(c, cast.substring(0, Math.round(cast.length() * (t < 8.2f ? p(3.8f, 5) : 1))), middle, cardY + dp(16), dp(15), t < 8.2f ? 0xFF565A57 : SEAL_GREEN, 1);
            if (in(7.6f, 8.2f)) {
                pen(STAMP_RED, dp(3), 1);
                c.drawLine(middle + dp(20), cardY + dp(16), middle + dp(20) + dp(50) * p(7.6f, 8), cardY + dp(16), stroke);
            }
        }
        // Her hearts turn the arrows round: she runs one way and goes the other.
        if (t < 3.2f) {
            go(clamp(cx + dp(60), L + wide, R - wide), floor, dp(380));
        } else if (t < 6.6f) {
            put(clamp(hx + dt * dp(70), L + wide / 2, R - wide / 2), floor);
            act(PetArt.RUN_LEFT);
        } else if (t < 8.4f) {
            go(clamp(middle + dp(60), L + wide / 2, R - wide / 2), floor, dp(520));
            if (t > 7.4f) armed(0, 7);
        } else {
            act(PetArt.IDLE);
        }
        if (in(9.1f, 11)) plate(c, "I can share a page. Just this once.", middle, cardY + dp(74), dp(12), 0xFFE0457F, 0xF7FFFFFF, p(9.1f, 9.4f));
        line(1.5f, 2.2f, "Illya's rewriting your screen. With herself as the heroine Sloppy~♡");
        line(4, 2.5f, "Her hearts turned my arrows round! Left is right! Don't laugh Sloppy~♡");
        line(7, 2.3f, "Supporting cast: none? I'll fix her typo Sloppy~♡");
        line(9.4f, 1.6f, "Cast: everyone Sloppy~♡");
    }

    /** The guild counter opens on the screen, and Inju stamps it. */
    void stampings(Canvas c) {
        float desk = L + dp(74), top = floor - dp(54);
        float in = out(p(.2f, 1));
        fill(0xFF7A5536, in);
        box.set(L - dp(10), top, desk + dp(64), floor + dp(4));
        c.drawRoundRect(box, dp(8), dp(8), paint);
        strip(c, NPC, 9, INJU, desk - dp(14), top + dp(18) + (1 - in) * dp(120), tall * 1.02f, false, 1);
        fill(0xFF9A6B45, in);
        box.set(L - dp(10), top, desk + dp(64), top + dp(12));
        c.drawRoundRect(box, dp(5), dp(5), paint);
        plate(c, "GUILD COUNTER", desk, top - tall * 1.02f - dp(4), dp(11), Color.WHITE, 0xF2C0562F, in);
        String[] marks = {"FORM 12-B", "REJECTED", "IN TRIPLICATE", "NEXT, PLEASE", "REJECTED"};
        float[] when = {1.7f, 2.4f, 3, 3.5f, 3.9f};
        float land = -1;
        for (int which = 0; which < marks.length; which++) {
            float since = t - when[which];
            if (since >= 0 && since < .45f) land = since;
            stamp(c, marks[which], L + W * (.24f + rnd(which, 4) * .52f), T + dp(110) + (floor - tall - dp(190) - T) * (which + rnd(which, 5) * .6f) / 5, dp(19 + rnd(which, 6) * 6),
                    -22 + rnd(which, 7) * 44, STAMP_RED, t < 6.6f ? since : -1);
        }
        if (t >= 6.6f) stamp(c, "APPROVED", cx, T + dp(110) + (floor - tall - dp(190) - T) * .5f, dp(30), -9, SEAL_GREEN, t - 6.6f);
        c.save();
        c.translate(0, shake(land, dp(5)));
        if (t < 1.6f) go(clamp(desk + dp(150), L + wide, R - wide), floor, dp(380));
        if (in(6.6f, 7.4f)) act(PetArt.JUMP);
        else if (t >= 7.4f) act(PetArt.WAVE);
        her(c);
        c.restore();
        plate(c, t < 6.6f ? "Now serving: 3   ·   Your number: 4,096" : "Now serving: 4,096", cx, T + dp(64), dp(12.5f), 0xFF292B2A, 0xF2FFFFFF, p(1, 1.4f));
        line(.3f, 2, "The guild counter needs your screen in triplicate Sloppy~♡");
        line(3.3f, 2.5f, "Rejected? You filled in your whole phone wrong Sloppy~♡");
        line(6.3f, 2.4f, "Form 12-B. Stamped. I had a word with Inju Sloppy~♡");
        line(8.9f, 1.5f, "Next, please Sloppy~♡");
    }

    /** King Slime comes down the screen, and is chunked along the lines Rune drew. */
    void slime(Canvas c) {
        float high = Math.min(dp(170), W * .42f), kingX = clamp(cx - W * .12f, L + high, R - high), herSpot = clamp(kingX + high + dp(40), L + wide, R - wide / 2);
        // Ooze running down from the top.
        for (int which = 0; which < 9; which++) {
            float began = .3f + rnd(which, 1) * 2.4f, run = Math.max(0, t - began) * dp(60 + rnd(which, 2) * 50), x = L + W * (which + .5f) / 9, wide = dp(10 + rnd(which, 3) * 12);
            fill(0xCC9AA7B5, 1);
            box.set(x - wide / 2, T - dp(20), x + wide / 2, T + Math.min(run, H * (.2f + rnd(which, 4) * .45f)));
            c.drawRoundRect(box, wide / 2, wide / 2, paint);
        }
        float[] lands = {1.6f, 3, 4.3f};
        float since = -1, kingY = floor, hop = 0;
        for (float land : lands) if (t >= land && t - land < .45f) since = t - land;
        if (t < 1.6f) kingY = T - dp(40) + (floor - T + dp(40)) * into(p(.8f, 1.6f));
        else if (t < 4.3f) hop = (float) Math.abs(Math.sin((t - 1.6f) / 1.35f * Math.PI)) * dp(90);
        c.save();
        c.translate(0, shake(since, dp(9)));
        if (t < 1.6f) go(herSpot, floor, dp(420));
        // She is in the air each time he lands.
        for (float land : lands) if (Math.abs(t - land) < .32f) {
            act(PetArt.JUMP);
            lift = (1 - Math.abs(t - land) / .32f) * tall * .7f;
        }
        float cut = t - 6;
        if (in(5.8f, 7.2f)) {
            put(herSpot - (herSpot - kingX + high * .7f) * ease(p(6, 6.9f)), floor);
            leftward = true;
            armed(4, cut > 0 ? 7 : 0);
        } else if (t >= 7.2f) {
            act(PetArt.IDLE);
        }
        if (t < 7) {
            strip(c, TrickArt.run_kingslime, 4, t < 1.6f || hop > dp(30) ? 1 : t >= 4.9f ? 2 : 0, kingX, kingY - hop, high, false, 1);
        } else {
            // In three, and the three hop away.
            strip(c, TrickArt.run_kingslime, 4, 3, kingX, floor, high * .8f, false, 1 - p(7, 8));
            for (int which = 0; which < 3; which++) {
                float away = t - 7, x = kingX + (which - 1) * away * dp(86);
                strip(c, TrickArt.run_kingslime, 4, 0, x, floor - (float) Math.abs(Math.sin(away * 5 + which)) * dp(34), high * .36f, which == 0, 1 - p(10, 11.3f));
            }
        }
        her(c);
        for (int which = 0; which < 3; which++) {
            float flash = cut - which * .3f;
            if (flash < 0 || flash > .3f) continue;
            pen(Color.WHITE, dp(6) * (1 - flash / .3f), 1);
            c.drawLine(kingX - high * .55f, floor - high * (.2f + which * .24f) - dp(18), kingX + high * .55f, floor - high * (.2f + which * .24f) + dp(18), stroke);
        }
        c.restore();
        line(.2f, 1.4f, "Uh oh. Something's oozing down your screen Sloppy~♡");
        line(1.8f, 2.6f, "King Slime! Be in the air when he lands Sloppy~♡");
        line(4.9f, 2, "Rune drew the lines. We chunk him. Three cuts Sloppy~♡");
        line(7.4f, 2.6f, "Chunked. Mind the little ones Sloppy~♡");
    }

    /** The Mawang, as her shadow, does the opposite of whatever she does; until she is offered bread. */
    void shadow(Canvas c) {
        wash(c, 0xCC151020, ease(p(.2f, 1.2f)) * (1 - p(9.2f, 10.6f)) * .7f);
        float reach = Math.min(W * .3f, dp(150));
        if (t < 2.6f) go(cx - reach, floor, dp(260));
        else if (in(6.4f, 8)) go(cx - reach * .35f, floor, dp(200));
        boolean jumping = in(3, 3.7f) || in(3.9f, 4.6f);
        if (jumping) {
            act(PetArt.JUMP);
            lift = (float) Math.sin((t < 3.8f ? p(3, 3.7f) : p(3.9f, 4.6f)) * Math.PI) * tall * .6f;
        } else if (in(4.8f, 6.3f)) {
            act(PetArt.WAVE);
        } else if (t >= 8) {
            act(PetArt.IDLE);
        }
        // Where she is, mirrored: nearer when Halgakos goes away, further when she comes closer.
        float mirror = 2 * cx - hx + (in(6.4f, 9) ? (cx - reach - hx) * -1.2f * (1 - p(8.4f, 9)) : 0);
        mirror = clamp(mirror, L + dp(40), R - dp(40));
        float duck = jumping ? .72f : 1;
        c.save();
        c.scale(1, duck, mirror, floor);
        strip(c, TrickArt.run_mawang, 6, in(4.8f, 8.3f) ? 5 : t >= 9 ? 0 : 3, mirror, floor, tall * 1.22f, true, ease(p(.6f, 1.5f)));
        c.restore();
        her(c);
        float thrown = p(8.3f, 9.1f);
        if (thrown > 0 && t < 10.4f) {
            piece(c, BREAD, hx + (mirror - dp(26) - hx) * thrown, floor - tall * .55f - (float) Math.sin(thrown * Math.PI) * dp(60), dp(34), thrown * 360, 1);
        }
        if (t >= 9.2f) plate(c, "...What took you so long.", mirror, floor - tall * 1.36f, dp(12), Color.WHITE, 0xE63A2A4A, p(9.2f, 9.5f));
        line(.4f, 2.2f, "My shadow's acting strange today Sloppy~♡");
        line(3, 2.4f, "I jump, she ducks. I wave, she sulks Sloppy~♡");
        line(6.3f, 1.9f, "Alone is easier, she says. Then stay there Sloppy~♡");
        line(8.3f, 2.6f, "Fine. Have some of Chikuwa's bread, horn-unnie Sloppy~♡");
    }

    /** Her hair runs through every colour she has won, each with what it lets her do. */
    void colours(Canvas c) {
        int owned = Hair.owned(context) | (BuildConfig.DEBUG && PlaySession.get(context).tally("unlock_all") == 1 ? 255 : 0), count = Integer.bitCount(owned & 255);
        String[] gives = {"", "Red: lava is safe", "Blue: she swims", "Brown: knocks things over", "Green: she flies", "Yellow: she runs faster",
                "Purple: sends it back", "Black: quite ordinary", "White: all of them at once"};
        if (t < 1.4f) go(clamp(cx, L + wide, R - wide), floor, dp(420));
        float each = Math.max(.9f, 6.6f / Math.max(1, count));
        int turn = t < 1.6f ? -1 : (int) ((t - 1.6f) / each), colour = 0, seen = 0;
        float since = t - 1.6f - turn * each;
        for (int which = 1; which <= 8 && turn >= 0; which++) {
            if ((owned & 1 << which - 1) == 0) continue;
            if (seen++ == turn) colour = which;
        }
        if (colour != 0 && pet() != null) {
            if (dyedIdle[colour] == null) {
                if (ownIdle == null) {
                    Bitmap all = BitmapFactory.decodeResource(context.getResources(), TrickArt.halgakos_pet);
                    int high = all.getHeight() / PetArt.FRAMES.length;
                    ownIdle = Bitmap.createBitmap(all, 0, PetArt.IDLE * high, all.getWidth() / PetArt.COLUMNS * PetArt.FRAMES[PetArt.IDLE], high);
                }
                dyedIdle[colour] = Hair.dyed(ownIdle, colour);
            }
            wear(dyedIdle[colour], PetArt.FRAMES[PetArt.IDLE], (int) (t * 6) % PetArt.FRAMES[PetArt.IDLE], 1);
            if (colour == PlusRun.GREEN) lift = ease(Math.min(1, since / .4f)) * tall * .5f + (float) Math.sin(t * 4) * dp(4);
            if (colour == PlusRun.YELLOW) {
                herAlpha = .35f;
                copy(c, hx - dp(26), hy);
                copy(c, hx + dp(26), hy);
                herAlpha = 1;
            }
            if (colour == PlusRun.BLACK) herScale = .9f;
        }
        // The ribbons of the colours she has, going round her.
        seen = 0;
        for (int which = 1; which <= 8; which++) {
            if ((owned & 1 << which - 1) == 0) continue;
            double way = t * 1.4 + seen++ * 2 * Math.PI / Math.max(1, count);
            float x = hx + (float) Math.cos(way) * tall * .95f, y = hy - tall * .55f + (float) Math.sin(way) * tall * .36f;
            if (Math.sin(way) < 0) bow(c, x, y, dp(which == colour ? 13 : 9), Hair.COLOURS[which], p(.6f, 1.4f));
        }
        her(c);
        seen = 0;
        for (int which = 1; which <= 8; which++) {
            if ((owned & 1 << which - 1) == 0) continue;
            double way = t * 1.4 + seen++ * 2 * Math.PI / Math.max(1, count);
            float x = hx + (float) Math.cos(way) * tall * .95f, y = hy - tall * .55f + (float) Math.sin(way) * tall * .36f;
            if (Math.sin(way) >= 0) bow(c, x, y, dp(which == colour ? 13 : 9), Hair.COLOURS[which], p(.6f, 1.4f));
        }
        if (colour != 0) {
            if (since < .4f) fx(c, BURST, hx, hy - tall * .6f, tall * (1 + since * 2), since * 90, 1 - since / .4f);
            plate(c, gives[colour], hx, clamp(hy + dp(26), T, B - dp(20)), dp(13), colour == 8 || colour == 5 ? 0xFF292B2A : Color.WHITE, Hair.COLOURS[colour] & 0xF2FFFFFF, Math.min(1, since / .2f));
        }
        line(.2f, 1.6f, "Galpi's ribbons. I kept every colour I won Sloppy~♡");
        line(8.4f, 2.2f, "Pick one for me in my settings. Choose well Sloppy~♡");
    }

    /** Altteul sets up his stall on the screen and prices the user's apps. */
    void stall(Canvas c) {
        pickApps(4);
        float up = out(p(.3f, 1.3f)), left = L + dp(18), right = Math.min(R - dp(18), left + dp(300)), counter = floor - dp(52), top = counter - tall * 1.02f - dp(4);
        float keeper = left + dp(46);
        pen(0xFF8A5A33, dp(5), up);
        c.drawLine(left + dp(4), top - dp(10), left + dp(4), counter, stroke);
        c.drawLine(right - dp(4), top - dp(10), right - dp(4), counter, stroke);
        // The awning, in stripes.
        for (int stripe = 0; stripe < 8; stripe++) {
            fill(stripe % 2 == 0 ? 0xFFE8554B : 0xFFFFF4E6, up);
            float from = left + (right - left) * stripe / 8, to = left + (right - left) * (stripe + 1) / 8;
            path.reset();
            path.moveTo(from, top - dp(40) * up);
            path.lineTo(to, top - dp(40) * up);
            path.lineTo(to + dp(3), top - dp(8) * up);
            path.lineTo(from - dp(3), top - dp(8) * up);
            path.close();
            c.drawPath(path, paint);
        }
        strip(c, NPC, 9, ALTTEUL, keeper, counter + dp(8) + (1 - up) * dp(140), tall * 1.02f, false, 1);
        fill(0xFF8A5A33, up);
        box.set(left, counter, right, counter + dp(16));
        c.drawRoundRect(box, dp(5), dp(5), paint);
        box.set(left + dp(8), counter + dp(14), right - dp(8), floor + dp(2));
        fill(0xFF6E4526, up);
        c.drawRect(box, paint);
        String[] prices = {"+ 40", "+ 15", "+ 99", "+ 7"};
        float bought = p(6.2f, 7.2f);
        for (int which = 0; which < 3; which++) {
            float x = keeper + dp(62) + which * dp(66), shown = p(1.4f + which * .5f, 1.8f + which * .5f), y = counter - dp(26);
            boolean hers = which == 0 && bought > 0;
            float atX = hers ? x + (hx - x) * ease(bought) : x, atY = hers ? y + (hy - tall * 1.25f - y) * ease(bought) - (float) Math.sin(bought * Math.PI) * dp(40) : y;
            if (x > right - dp(20)) break;
            icon(c, which, atX, atY, dp(44) * shown, 0, 1);
            plate(c, hers && bought >= 1 ? "SOLD" : prices[which], x, counter + dp(30), dp(11.5f), hers && bought >= 1 ? Color.WHITE : 0xFF292B2A, hers && bought >= 1 ? 0xF2D8342B : 0xF2FFE9A8, p(2.6f + which * .3f, 2.9f + which * .3f));
        }
        if (t < 2.2f) go(clamp(right + dp(20), L + wide, R - wide / 2), floor, dp(360));
        else if (in(7.2f, 8.6f)) act(PetArt.JUMP);
        else if (t >= 8.6f) act(PetArt.IDLE);
        // Her pluses go over the counter.
        for (int which = 0; which < 6; which++) {
            float sent = p(5.2f + which * .12f, 6 + which * .12f);
            if (sent > 0 && sent < 1) piece(c, PLUS, hx + (keeper - hx) * sent, hy - tall * .5f - (float) Math.sin(sent * Math.PI) * dp(70), dp(22), sent * 400, 1);
        }
        her(c);
        if (t >= 8.8f) plate(c, "A pleasure doing sums with you!", (left + right) / 2, top - dp(56), dp(12), 0xFF292B2A, 0xF2FFFFFF, p(8.8f, 9.1f));
        line(.3f, 2.2f, "Altteul's set up his stall. On your screen Sloppy~♡");
        line(2.8f, 2.4f, "He's pricing your apps. " + label(0) + " for forty pluses Sloppy~♡");
        line(5.4f, 2.6f, "I'll take that one. Pluses from this very screen Sloppy~♡");
        line(8.9f, 2, "No refunds, he says Sloppy~♡");
    }

    /** The screen is locked like the arena of a land's boss, and the Guild Guard sets her exam. */
    void arena(Canvas c) {
        float shut = ease(p(.5f, 1.4f)) * (1 - ease(p(9.4f, 10.6f))), bars = Math.min(W * .16f, dp(64));
        float[] thrusts = {3, 4.6f, 5.6f};
        int landed = 0;
        for (float thrust : thrusts) if (t >= thrust + .5f) landed++;
        float guardX = R - bars - dp(54), spot = clamp(cx - dp(40), L + bars + wide, guardX - dp(110));
        if (t < 2) go(spot, floor, dp(380));
        for (float thrust : thrusts) if (t >= thrust && t < thrust + .7f) {
            act(PetArt.JUMP);
            lift = (float) Math.sin((t - thrust) / .7f * Math.PI) * tall * .85f;
        }
        if (in(6.4f, 7.2f)) act(PetArt.JUMP);
        else if (t >= 7.2f) act(PetArt.WAVE);
        int frame = landed >= 3 ? 3 : 0;
        float lunge = 0;
        for (float thrust : thrusts) if (t >= thrust - .1f && t < thrust + .5f) {
            frame = 1;
            lunge = (float) Math.sin(p(thrust - .1f, thrust + .5f) * Math.PI) * dp(70);
        }
        strip(c, TrickArt.run_guard, 4, frame, guardX + (1 - out(p(1.2f, 2.2f))) * dp(200) - lunge, floor, tall * 1.3f, true, 1);
        for (int which = 0; which < 3; which++) {
            float asked = t - (thrusts[which] - .7f);
            if (asked >= 0 && asked < .7f) words(c, "?", guardX - dp(10), floor - tall * 1.5f - asked * dp(20), dp(30), 0xFFFFD84D, 1 - asked / .7f * .5f);
        }
        her(c);
        if (landed >= 3 && t < 9.6f) {
            float given = p(6.4f, 7.3f);
            piece(c, RED_HEART, guardX + (hx - guardX) * ease(given), floor - tall * .6f - (float) Math.sin(given * Math.PI) * dp(90) - (given >= 1 ? tall * .7f : 0), dp(34), 0, 1);
        }
        // The way in and the way out, barred.
        for (int side = 0; side < 2; side++) {
            float edge = side == 0 ? L - bars + bars * shut : R - bars * shut;
            fill(0xE61B1D22, 1);
            c.drawRect(edge, T, edge + bars, B, paint);
            pen(0xFF8E97A3, dp(5), 1);
            for (int bar = 0; bar < 3; bar++) c.drawLine(edge + bars * (bar + .5f) / 3, T, edge + bars * (bar + .5f) / 3, B, stroke);
            pen(0xFFB8C0CA, dp(7), 1);
            c.drawLine(edge, T + H * .3f, edge + bars, T + H * .3f, stroke);
            c.drawLine(edge, T + H * .7f, edge + bars, T + H * .7f, stroke);
        }
        // Whose health it is.
        float bar = Math.min(W - bars * 2 - dp(40), dp(260)), barY = T + dp(70), left = cx - bar / 2, health = 1 - landed / 3f;
        fill(0xE61B1D22, shut);
        box.set(left - dp(8), barY - dp(26), left + bar + dp(8), barY + dp(14));
        c.drawRoundRect(box, dp(10), dp(10), paint);
        words(c, "The Guild Guard", cx, barY - dp(13), dp(12.5f), Color.WHITE, shut);
        fill(0xFF4A4E55, shut);
        box.set(left, barY, left + bar, barY + dp(7));
        c.drawRoundRect(box, dp(4), dp(4), paint);
        fill(CORAL, shut);
        box.set(left, barY, left + bar * health, barY + dp(7));
        c.drawRoundRect(box, dp(4), dp(4), paint);
        line(.5f, 1.7f, "Locked in. Whoever's in here has the key Sloppy~♡");
        line(2.2f, 2.1f, "Halt, she says. It's the guild entrance exam Sloppy~♡");
        line(4.4f, 2, "Three questions. All of them are halberd Sloppy~♡");
        line(6.6f, 2.8f, "Passed. And they gave me a heart for it Sloppy~♡");
    }

    /** Zero rolls along the user's apps and multiplies them by nothing; she lands on him, and they come back. */
    void nothing(Canvas c) {
        pickApps(5);
        int count = appCount();
        float row = appRow(), size = dp(52), zeroX = L - dp(60) + (R + dp(120) - L) * p(1.4f, 5), zeroSize = dp(64);
        boolean squashed = t >= 6.5f;
        float zeroAt = squashed ? clamp(cx + dp(70), L + dp(60), R - dp(60)) : t < 5 ? zeroX : clamp(cx + dp(70), L + dp(60), R - dp(60));
        int left = 0;
        for (int which = 0; which < count; which++) {
            float x = appX(which, count), gone = t < 5 ? (zeroX > x ? Math.min(1, (zeroX - x) / dp(70)) : 0) : 1, back = p(7 + which * .35f, 7.4f + which * .35f);
            float there = Math.max(1 - gone, back);
            if (there > .5f) left++;
            icon(c, which, x, row, size * there, (1 - there) * 200, 1);
            if (there < 1) words(c, gone < 1 ? "× 0" : "0", x, row, dp(22), Color.WHITE, (1 - there) * (1 - back));
            if (back > 0 && back < 1) piece(c, STAR, x, row - size * .7f, dp(24), back * 200, 1 - back);
        }
        plate(c, t < 7 ? "Apps: " + count + " × 0 = " + (t < 5 ? left : 0) : "Apps: " + left + " of " + count + " back", cx, T + dp(64), dp(13), 0xFF292B2A, 0xF2FFFFFF, p(1.6f, 2));
        // His floor glows before it bites: she is off it by then.
        float glow = t < 5.2f || t > 6.5f ? 0 : (float) Math.abs(Math.sin((t - 5.2f) * 6));
        fill(0xFFFFE066, glow * .55f);
        c.drawRect(L, floor - dp(6), R, floor + dp(10), paint);
        if (t < 5) piece(c, ZERO, zeroAt, row + dp(4), zeroSize, zeroX * .9f, 1);
        else if (t < 6.5f) piece(c, ZERO, zeroAt, row + (floor - dp(30) - row) * out(p(5, 5.5f)), zeroSize * 1.3f, t * 200, 1);
        else {
            c.save();
            c.scale(1.25f, .5f, zeroAt, floor);
            piece(c, ZERO, zeroAt, floor - dp(36), zeroSize * 1.3f, 0, 1 - p(8.6f, 9.6f));
            c.restore();
        }
        if (t < 5.4f) go(clamp(cx - dp(60), L + wide, R - wide), floor, dp(380));
        if (in(5.5f, 6.5f)) {
            act(PetArt.JUMP);
            float over = p(5.5f, 6.5f);
            put(clamp(cx - dp(60), L + wide, R - wide) + (zeroAt - clamp(cx - dp(60), L + wide, R - wide)) * over, floor);
            lift = (float) Math.sin(over * Math.PI) * tall * 1.1f + over * dp(20);
        } else if (t >= 6.5f) {
            lift = dp(20) * (1 - p(8.6f, 9.6f));
            act(t < 7.3f ? PetArt.JUMP : PetArt.WAVE);
        }
        her(c);
        if (in(6.5f, 6.85f)) fx(c, BURST, zeroAt, floor - dp(26), dp(150), 0, 1 - (t - 6.5f) / .35f);
        line(.3f, 2, "Uh oh. That's Zero. Nothing, with a crown on Sloppy~♡");
        line(2.5f, 2.5f, "Times nothing! He's multiplying your apps by zero Sloppy~♡");
        line(5.1f, 1.4f, "He's glowing. Off the floor Sloppy~♡");
        line(6.8f, 2.6f, "Take that, nothing. Something out of nothing Sloppy~♡");
    }

    /** The Gazette Twins print the user's screen: Miri spoils it, and Gari blacks it out. */
    void gazette(Canvas c) {
        pickApps(2);
        String[] heads = {named("EXTRA: Sloppy opens " + tag(0) + " again"), "SPOILER: the battery dies at 4%", named("SPOILER: Sloppy scrolls for an hour"),
                "EXTRA: local translator still right"};
        strip(c, TrickArt.run_miri, 3, 0, L + dp(46), T + dp(128) + (1 - out(p(.2f, 1))) * -dp(220), tall, false, 1);
        strip(c, TrickArt.run_gari, 3, t < 9 ? 0 : 2, R - dp(64), floor + (1 - out(p(1.6f, 2.4f))) * dp(240), tall * 1.2f, true, 1);
        for (int which = 0; which < heads.length; which++) {
            float began = 1.2f + which * 1.05f, flown = t - began;
            if (flown < 0) continue;
            float y = T + dp(158) + which * Math.max(dp(44), Math.min(dp(70), (floor - tall - dp(230) - T) / heads.length)), x = L - dp(160) + (cx + dp(160) - L) * out(Math.min(1, flown / .6f));
            text.setTextSize(dp(12.5f));
            float half = Math.min(W * .46f, text.measureText(heads[which]) / 2 + dp(14));
            c.save();
            c.rotate(-4 + rnd(which, 1) * 8, x, y);
            fill(0xF7F5EEDD, 1);
            box.set(x - half, y - dp(20), x + half, y + dp(20));
            c.drawRect(box, paint);
            pen(0xFF3A3A44, dp(1), 1);
            c.drawRect(box, stroke);
            c.drawLine(x - half + dp(6), y - dp(12), x + half - dp(6), y - dp(12), stroke);
            c.save();
            c.clipRect(box);
            words(c, heads[which], x, y + dp(3), dp(12.5f), 0xFF22242A, 1);
            c.restore();
            // Gari's bar across it, a moment after it lands.
            float barred = Math.min(1, Math.max(0, (flown - 1.5f) / .4f));
            fill(0xFF0A0A0C, 1);
            c.drawRect(x + half - (half * 1.3f) * barred, y - dp(5), x + half - dp(4), y + dp(11), paint);
            c.restore();
        }
        if (t < 1.6f) go(clamp(cx, L + wide, R - wide), floor, dp(380));
        else if (t >= 7.4f) act(PetArt.WAVE);
        her(c);
        // And across her eyes, until she objects.
        float bar = p(4.2f, 4.5f) * (1 - p(7.2f, 7.5f));
        fill(0xFF0A0A0C, bar);
        c.drawRect(hx - wide * .34f * bar, hy - tall * .64f, hx + wide * .34f * bar, hy - tall * .5f, paint);
        line(.3f, 2.2f, "Extra, extra. The Gazette Twins are printing your screen Sloppy~♡");
        line(3.2f, 1.3f, "Miri spoils it. Gari blacks it out Sloppy~♡");
        line(4.6f, 2.4f, "Hey! Not my eyes. I'm not a spoiler Sloppy~♡");
        line(7.5f, 2.6f, "Print the real page. And no peeking Sloppy~♡");
    }

    /** Gokhae has the screen say everything in the passive voice from behind her fan; Halgakos lands on her when she lowers it. */
    void fan(Canvas c) {
        pickApps(1);
        String[] said = {"It is being said that this screen is being looked at.", "It is suggested that left is now right.",
                "It is regretted that " + label(0) + " was opened.", "A regent is, regrettably, required."};
        String[] meant = {"You are looking at your screen.", "Left is left.", "You opened " + label(0) + ".", "Nobody asked for a regent."};
        float gokhaeX = R - dp(70), perch = clamp(T + H * .36f, T + dp(210), floor - tall - dp(60));
        boolean hit = t >= 7;
        strip(c, TrickArt.run_gokhae, 4, hit ? (t < 7.8f ? 2 : 3) : in(5.9f, 7) ? 0 : 1, gokhaeX + (1 - out(p(.2f, 1.1f))) * dp(180), perch + (hit ? p(7, 7.6f) * dp(30) : 0), tall * 1.25f, true, 1);
        for (int which = 0; which < said.length; which++) {
            float began = 1.2f + which * 1.1f, shown = Math.min(1, Math.max(0, (t - began) / .4f));
            if (shown <= 0) continue;
            String words = hit && t - 7.4f > which * .3f ? meant[which] : said[which];
            float y = T + dp(96) + which * dp(40) - (1 - shown) * dp(60), x = Math.min(cx, gokhaeX - dp(150) < L + dp(130) ? cx : cx - dp(24));
            boolean turned = which == 1 && !hit;
            c.save();
            if (turned) c.scale(-1, 1, x, y);
            text.setTextSize(dp(12));
            float half = Math.min(W * .47f, text.measureText(words) / 2 + dp(12));
            fill(hit && t - 7.4f > which * .3f ? 0xF2FFFFFF : 0xF2EFE3C4, shown);
            box.set(x - half, y - dp(15), x + half, y + dp(15));
            c.drawRoundRect(box, dp(4), dp(4), paint);
            c.save();
            c.clipRect(box);
            words(c, words, x, y, dp(12), 0xFF3A2A22, shown);
            c.restore();
            c.restore();
        }
        if (t < 5.9f) {
            go(clamp(cx - dp(40), L + wide, R - wide), floor, dp(360));
        } else if (t < 7) {
            act(PetArt.JUMP);
            float over = ease(p(6.1f, 7));
            put(hx + (gokhaeX - dp(30) - hx) * Math.min(1, dt * 4), floor);
            lift = over * (floor - perch + tall * .5f) + (float) Math.sin(over * Math.PI) * dp(50);
        } else if (t < 8.2f) {
            lift = (floor - perch + tall * .5f) * (1 - ease(p(7, 8.2f)));
            put(hx + (clamp(cx, L + wide, R - wide) - hx) * Math.min(1, dt * 3), floor);
            act(PetArt.JUMP);
        } else {
            act(PetArt.WAVE);
        }
        her(c);
        if (in(7, 7.35f)) fx(c, BURST, gokhaeX - dp(20), perch - tall * .7f, dp(130), 0, 1 - (t - 7) / .35f);
        line(.3f, 2.2f, "Gokhae. She hides behind a fan and the passive voice Sloppy~♡");
        line(3.4f, 2.4f, "It is being said? By whom? By her Sloppy~♡");
        line(5.9f, 1.2f, "She lowered the fan to talk. Now Sloppy~♡");
        line(7.6f, 2.6f, "Mistakes were made. By her. Active voice Sloppy~♡");
    }

    /** Between the lines, where Yeobaek is napping; woken, she has to cut something, and it is the screen. */
    void nap(Canvas c) {
        float white = ease(p(.2f, 1.4f)) * (1 - p(9.4f, 10.6f)), gap = dp(46), slash = t - 5.4f;
        wash(c, 0xFFFFFFFF, white * .62f);
        float guardX = L + dp(70), cutY = floor - tall * .7f;
        for (int which = 0; T + dp(90) + which * gap < B - dp(20); which++) {
            float y = T + dp(90) + which * gap, twang = y > floor - gap && y <= floor && in(4, 4.6f) ? (float) Math.sin((t - 4) * 40) * dp(4) * (1 - (t - 4) / .6f) : 0;
            // What lies under her cut has slipped a little to one side.
            float slip = slash > 0 && y > cutY ? dp(7) * Math.min(1, slash / .15f) * (1 - p(8.6f, 9.6f)) : 0;
            pen(0xFFB9BEC6, dp(1.5f), white);
            c.drawLine(L + dp(20) + slip, y + twang, R - dp(20) + slip, y + twang, stroke);
        }
        boolean asleep = t < 4.3f || t >= 6.6f;
        strip(c, TrickArt.run_yeobaek, 6, asleep ? (t < 4.3f ? 3 : 5) : t < 5.2f ? 0 : 2, guardX, floor, tall * 1.2f, false, ease(p(.6f, 1.4f)));
        if (asleep && t > 1.4f) {
            for (int which = 0; which < 3; which++) {
                float rise = (t * .5f + which / 3f) % 1;
                words(c, "z", guardX + dp(24) + rise * dp(22), floor - tall * 1.1f - rise * dp(46), dp(12 + which * 4), 0xFF6F7B8E, (1 - rise) * white);
            }
        }
        // She tiptoes past, and steps on the line.
        if (t < 4) go(clamp(guardX + dp(150) + (cx - guardX) * .6f, L + wide, R - wide), floor, dp(90));
        else if (in(5.2f, 6.1f)) {
            act(PetArt.JUMP);
            lift = (float) Math.sin(p(5.2f, 6.1f) * Math.PI) * tall * .9f;
        } else if (t >= 6.1f) {
            act(PetArt.IDLE);
        }
        her(c);
        if (slash > 0 && t < 9.6f) {
            float shown = slash < .15f ? slash / .15f : 1, fading = 1 - p(8.6f, 9.6f);
            pen(Color.WHITE, dp(slash < .3f ? 9 : 2.5f), fading);
            c.drawLine(L, cutY + dp(30), L + W * shown, cutY - dp(30), stroke);
            pen(0xFF6F7B8E, dp(1), fading);
            c.drawLine(L, cutY + dp(33), L + W * shown, cutY - dp(27), stroke);
        }
        if (t >= 6.8f) plate(c, "Sorry. Mmm.", guardX + dp(40), floor - tall * 1.36f, dp(12), 0xFF292B2A, 0xF2FFFFFF, p(6.8f, 7.1f) * white);
        line(.3f, 2.3f, "Between the lines. Shh. It's very white in here Sloppy~♡");
        line(2.7f, 1.4f, "Five more minutes, she says. Tiptoe Sloppy~♡");
        line(4.2f, 1.9f, "I stepped on her line. She has to cut something now Sloppy~♡");
        line(6.2f, 2.8f, "She cut your screen in her sleep. And said sorry Sloppy~♡");
    }

    /** Boksun-halmeoni's three rice cakes have rolled off behind the user's apps. */
    void cakes(Canvas c) {
        pickApps(3);
        float trackY = T + dp(70), size = dp(50);
        float[] xs = {L + W * .22f, L + W * .72f, L + W * .46f}, ys = {T + H * .3f, T + H * .4f, floor - tall - dp(60)};
        float[] found = {2.8f, 4.6f, 6.3f};
        int got = 0;
        strip(c, NPC, 9, BOKSUN, L + dp(54), floor + (1 - out(p(.2f, 1))) * dp(200), tall * 1.02f, false, 1);
        for (int which = 0; which < 3; which++) {
            float since = t - found[which], x = xs[which], y = clamp(ys[which], T + dp(130), floor - tall - dp(40));
            if (since >= .8f) got++;
            // The cake rolls out from behind it, and goes up to be counted.
            float out = Math.min(1, Math.max(0, since / .3f)), up = Math.min(1, Math.max(0, (since - .3f) / .5f));
            float cakeX = x + out * dp(40) + (cx + dp(70) - x - dp(40)) * ease(up), cakeY = y + dp(10) + (trackY - y - dp(10)) * ease(up);
            if (up < 1) {
                fill(0xFFFFFDF6, since >= 0 ? 1 : 0);
                box.set(cakeX - dp(13), cakeY - dp(8), cakeX + dp(13), cakeY + dp(8));
                c.drawRoundRect(box, dp(8), dp(8), paint);
                fill(0xFFF7A1B5, since >= 0 ? 1 : 0);
                c.drawRect(cakeX - dp(13), cakeY - dp(2), cakeX + dp(13), cakeY + dp(2), paint);
            }
            icon(c, which, x, y - (since >= 0 && since < .4f ? (float) Math.sin(since / .4f * Math.PI) * dp(18) : 0), size, since >= 0 && since < .4f ? (float) Math.sin(since * 30) * 12 : 0, 1);
        }
        int at = t < found[0] ? 0 : t < found[1] ? 1 : 2;
        if (t < 7) go(clamp(xs[at], L + wide / 2, R - wide / 2), clamp(clamp(ys[at], T + dp(130), floor - tall - dp(40)) + size * .5f + tall, T + tall, floor), dp(430));
        else if (t < 8) go(clamp(cx, L + wide, R - wide), floor, dp(430));
        else act(PetArt.WAVE);
        her(c);
        plate(c, "Three rice cakes:  " + got + " of 3", cx - dp(10), trackY, dp(13), 0xFF292B2A, 0xF2FFFFFF, p(.8f, 1.2f));
        if (t >= 8) {
            float since = t - 8, slam = since < .14f ? 2 - since / .14f : 1;
            fill(STAMP_RED, .92f);
            c.drawCircle(cx + dp(100), trackY, dp(17) * slam, paint);
            words(c, "+40", cx + dp(100), trackY, dp(11), Color.WHITE, 1);
        }
        if (t >= 8.6f) plate(c, "Now eat something, dear.", L + dp(100), floor - tall * 1.18f, dp(12), 0xFF292B2A, 0xF2FFFFFF, p(8.6f, 8.9f));
        line(.3f, 2.3f, "Boksun-halmeoni lost three rice cakes. On your screen Sloppy~♡");
        line(3, 1.5f, "One was behind " + label(0) + " Sloppy~♡");
        line(5, 2.6f, "Two. Three. I didn't eat any. Much Sloppy~♡");
        line(8.2f, 2.8f, "A guild seal. Stamped and everything Sloppy~♡");
    }

    /** A draught from a crack in the screen: broken open, it is the door of a hidden room where pluses rain. */
    void door(Canvas c) {
        float doorX = clamp(cx + dp(30), L + dp(90), R - dp(90)), wide = dp(92), high = Math.min(dp(190), floor - T - dp(140)), top = floor - high;
        float open = ease(p(2.8f, 3.6f)) * (1 - ease(p(8.8f, 9.8f))), cracked = p(.8f, 1.8f) * (1 - p(9.6f, 10.6f));
        if (open > 0) {
            // The room behind it, and what falls there.
            fill(0xFF1B1D2A, 1);
            box.set(doorX - wide / 2, top, doorX + wide / 2, floor);
            c.drawRoundRect(box, dp(8), dp(8), paint);
            c.save();
            c.clipRect(box);
            for (int which = 0; which < 16; which++) {
                float fell = (t * (.5f + rnd(which, 1) * .4f) + rnd(which, 2)) % 1;
                piece(c, PLUS, doorX - wide * .42f + rnd(which, 3) * wide * .84f, top + fell * high, dp(18), fell * 300, open);
            }
            c.restore();
            // Its leaf, swinging back.
            fill(0xFF8A5A33, 1);
            box.set(doorX - wide / 2, top, doorX - wide / 2 + wide * (1 - open), floor);
            c.drawRoundRect(box, dp(8), dp(8), paint);
            pen(0xFFFFD84D, dp(3), 1);
            c.drawRoundRect(doorX - wide / 2, top, doorX + wide / 2, floor, dp(8), dp(8), stroke);
        }
        if (open < 1) {
            for (int which = 0; which < 5; which++) crack(c, doorX, top + high * .45f, high * (.3f + rnd(which, 1) * .3f) * Math.min(1, cracked * 1.5f), which * 1.26f + rnd(which, 2), which, cracked * (1 - open));
            for (int which = 0; which < 4 && cracked > .5f && open == 0; which++) {
                float blown = (t * .8f + which * .25f) % 1;
                pen(0xCCFFFFFF, dp(2), (1 - blown) * .8f);
                c.drawLine(doorX + blown * dp(30), top + high * (.3f + which * .1f), doorX + dp(26) + blown * dp(60), top + high * (.28f + which * .1f) - blown * dp(10), stroke);
            }
        }
        if (t < 2.4f) go(clamp(doorX - dp(96), L + wide, R - wide), floor, dp(380));
        else if (in(2.4f, 3)) {
            leftward = false;
            armed(4, t > 2.6f ? 7 : 0);
        } else if (in(4.4f, 8.4f)) {
            go(clamp(doorX - dp(60), L + wide / 2, R - wide / 2), floor, dp(200));
            act(PetArt.JUMP);
        } else if (t >= 8.4f) {
            act(PetArt.IDLE);
        }
        // What pours out of the door at her.
        int caught = 0;
        for (int which = 0; which < 12; which++) {
            float sent = p(4.2f + which * .3f, 4.9f + which * .3f);
            if (sent >= 1) caught++;
            else if (sent > 0) piece(c, PLUS, doorX + (hx - doorX) * sent, top + high * .5f + (hy - tall * .6f - top - high * .5f) * sent - (float) Math.sin(sent * Math.PI) * dp(40), dp(22), sent * 500, 1);
        }
        her(c);
        if (in(2.8f, 3.1f)) fx(c, BURST, doorX, top + high * .45f, dp(150), 0, 1 - (t - 2.8f) / .3f);
        if (caught > 0) plate(c, "+ " + caught, hx, hy - tall - dp(40), dp(15), Color.WHITE, 0xF2E8554B, 1);
        line(.3f, 1.6f, "There's a draught coming from your screen Sloppy~♡");
        line(1.9f, 1.6f, "A crack. Break it open Sloppy~♡");
        line(3.6f, 2.8f, "A hidden room! Pluses are falling from the ceiling Sloppy~♡");
        line(8.2f, 2.6f, "Finders keepers. Every book keeps a hidden page Sloppy~♡");
    }

    /** A crowd of minus signs crosses the screen, and she has the ink gun. */
    void inkGun(Canvas c) {
        float stand = L + wide * .8f, muzzle = stand + wide * .5f;
        int shots = 16, fired = 0;
        if (t < 1.4f) go(stand, floor, dp(420));
        else {
            put(stand, floor);
            leftward = false;
            armed(5, in(2.2f, 7.4f) && (t * 4) % 1 < .5f ? 7 : 0);
        }
        for (int which = 0; which < shots; which++) {
            float shot = 2.3f + which * .31f, flown = t - shot;
            // Each of them comes on from the right until a shot meets it; then it is a blot where it stood.
            float came = Math.max(0, t - .8f), reach = .22f, hitAt = shot + reach;
            float x = L + W * .5f + which * dp(17) - came * dp(38), atHit = Math.max(muzzle + dp(24), L + W * .5f + which * dp(17) - (hitAt - .8f) * dp(38));
            if (flown >= 0) fired++;
            if (t < hitAt) {
                if (x < R + dp(20)) walker(c, MINUS, x, floor - dp(18), dp(30), came * 9 + which, 1);
                if (flown >= 0) piece(c, INK, muzzle + (atHit - muzzle) * (flown / reach), floor - tall * .52f + (tall * .52f - dp(18)) * (flown / reach), dp(18), 0, 1);
            } else {
                blot(c, atHit, floor - dp(18), dp(11), which, 1 - Math.min(1, (t - hitAt) / 1.6f));
            }
            // And some of it goes on the screen.
            float splat = t - (shot + .15f);
            if (which % 2 == 0 && splat >= 0) blot(c, L + W * (.3f + rnd(which, 5) * .62f), T + H * (.14f + rnd(which, 6) * .42f), dp(13 + rnd(which, 7) * 16) * Math.min(1, splat / .12f), which + 40, .92f);
        }
        her(c);
        plate(c, "Ink: " + (shots - Math.min(shots, fired)), cx, T + dp(64), dp(13), Color.WHITE, 0xF227348F, p(1.4f, 1.8f));
        line(.3f, 2, "A crowd of minus signs. And I have an ink gun Sloppy~♡");
        line(2.4f, 2.6f, "Oh, this is going to get messy Sloppy~♡");
        line(7.6f, 2.6f, "Sixteen shots. Sixteen minus signs. I can count Sloppy~♡");
    }

    /** A green ribbon from Yeseo, and she flies about the screen. */
    void flight(Canvas c) {
        strip(c, NPC, 9, YESEO, L + dp(56), floor + (1 - out(p(.2f, 1))) * dp(200) + p(9, 10.4f) * dp(200), tall * 1.05f, false, 1);
        float start = clamp(L + dp(150), L + wide, R - wide), up = ease(p(1.6f, 2.6f)), down = ease(p(8.4f, 9.6f));
        if (t < 1.6f) go(start, floor, dp(420));
        if (t >= 1.6f) {
            double way = (t - 1.6f) * 1.25;
            float flyX = cx + (float) Math.sin(way) * W * .34f, flyY = T + H * .36f + (float) Math.sin(way * 2) * H * .16f;
            float x = start + (flyX - start) * up, y = floor + (flyY - floor) * up;
            x += (clamp(cx, L + wide, R - wide) - x) * down;
            y += (floor - y) * down;
            put(clamp(x, L + wide / 2, R - wide / 2), floor);
            lift = floor - y;
            leftward = Math.cos(way) < 0;
            if (down < 1) wear(JUMP, 6, 1 + (int) (t * 7) % 4, 1.05f);
            else act(PetArt.IDLE);
            // The ribbon streams behind her, and the draughts she rides.
            pen(Hair.COLOURS[PlusRun.GREEN], dp(4), (1 - down) * up);
            path.reset();
            for (int back = 0; back <= 9; back++) {
                double then = way - back * .035;
                float bx = cx + (float) Math.sin(then) * W * .34f, by = T + H * .36f + (float) Math.sin(then * 2) * H * .16f - tall * .82f + (float) Math.sin(t * 9 + back) * dp(3);
                if (back == 0) path.moveTo(x, y - tall * .82f);
                else path.lineTo(start + (bx - start) * up, by);
            }
            c.drawPath(path, stroke);
        }
        for (int which = 0; which < 7 && t > 1.6f && t < 9; which++) {
            float blown = (t * .45f + rnd(which, 1)) % 1;
            pen(0xCCFFFFFF, dp(2), (1 - Math.abs(blown - .5f) * 2) * .7f);
            float y = T + H * (.14f + rnd(which, 2) * .5f), x = L + blown * W;
            c.drawLine(x, y, x + dp(46), y - dp(5), stroke);
        }
        her(c);
        if (t > 1.2f && t < 9.4f) bow(c, hx + (leftward ? dp(6) : -dp(6)), hy - lift - tall * .9f, dp(8), Hair.COLOURS[PlusRun.GREEN], 1);
        if (in(1, 3)) plate(c, "Tie it on, and hold the jump.", L + dp(120), floor - tall * 1.24f, dp(12), 0xFF292B2A, 0xF2FFFFFF, p(1, 1.3f) * (1 - p(2.6f, 3)));
        line(.3f, 1.4f, "Yeseo tied a green ribbon on me Sloppy~♡");
        line(2.4f, 2.5f, "I'm flying! My wings finally agree with me Sloppy~♡");
        line(5.6f, 2.5f, "The whole screen from above. Your wallpaper is crooked Sloppy~♡");
        line(8.5f, 1.8f, "Coming in to land Sloppy~♡");
    }

    /** Sumbi dives between the lines of the screen, nets Halgakos by mistake, and comes up with a heart. */
    void dive(Canvas c) {
        float level = B - (B - T) * .46f * ease(p(1.4f, 3.4f)) * (1 - ease(p(9.4f, 10.6f))), diverX = R - dp(70), gap = dp(44);
        for (int which = 0; T + dp(100) + which * gap < B - dp(10); which++) {
            float y = T + dp(100) + which * gap;
            pen(0xFFB9BEC6, dp(6), p(.2f, 1) * (1 - p(9.6f, 10.6f)) * .55f);
            c.drawLine(L + dp(24), y, R - dp(24) - rnd(which, 1) * W * .3f, y, stroke);
        }
        float under = ease(p(3.6f, 4.4f)) * (1 - ease(p(7.6f, 8.4f)));
        strip(c, TrickArt.run_sumbi, 4, in(3.4f, 8) ? 1 : 0, diverX + (1 - out(p(.3f, 1.2f))) * dp(180) - under * (diverX - hx - dp(40)), floor + under * dp(120), tall * 1.25f, true, 1 - under * .45f);
        if (t < 3.4f) go(clamp(cx - dp(30), L + wide, R - wide), floor, dp(380));
        // The net comes up under her, and she is in it until it tears.
        float netted = ease(p(5, 5.8f)) * (1 - ease(p(7.4f, 7.9f)));
        if (netted > 0) {
            lift = netted * tall * .9f;
            wear(LOSE, 6, 0, 1.1f);
            spin = (float) Math.sin(t * 16) * 14 * netted;
        } else if (t >= 8.4f) {
            act(PetArt.WAVE);
        }
        her(c);
        if (netted > 0) {
            float netY = hy - lift - tall * .45f, round = tall * .72f;
            pen(0xFFE9D9A6, dp(2), netted);
            c.drawCircle(hx, netY, round, stroke);
            for (int which = -2; which <= 2; which++) {
                c.drawLine(hx + which * round * .36f, netY - round * .93f, hx + which * round * .2f, netY + round * .93f, stroke);
                c.drawLine(hx - round * .93f, netY + which * round * .36f, hx + round * .93f, netY + which * round * .36f, stroke);
            }
            c.drawLine(hx, netY - round, hx + dp(30), T - dp(10), stroke);
        }
        // The water, over everything that is under it.
        fill(0x8C3B7BE0, 1);
        path.reset();
        path.moveTo(L, B);
        for (int wave = 0; wave <= 12; wave++) path.lineTo(L + W * wave / 12f, level + (float) Math.sin(t * 2.4f + wave * .9f) * dp(5));
        path.lineTo(R, B);
        path.close();
        if (level < B - dp(2)) c.drawPath(path, paint);
        for (int which = 0; which < 10 && level < B - dp(30); which++) {
            float rise = (t * (.3f + rnd(which, 1) * .3f) + rnd(which, 2)) % 1;
            pen(0xCCFFFFFF, dp(1.5f), 1 - rise);
            c.drawCircle(L + rnd(which, 3) * W, B - rise * (B - level), dp(3 + rnd(which, 4) * 4), stroke);
        }
        float given = p(8.2f, 9.2f);
        if (given > 0 && t < 10.2f) piece(c, RED_HEART, diverX + (hx - diverX) * ease(given), floor - tall * .6f - (float) Math.sin(given * Math.PI) * dp(80) - (given >= 1 ? tall * .7f : 0), dp(34), 0, 1);
        if (in(5.2f, 7.4f)) plate(c, "Net's out! Don't wriggle!", clamp(hx, L + dp(100), R - dp(100)), clamp(hy - lift - tall * 1.9f, T + dp(60), B), dp(12), 0xFF292B2A, 0xF2FFFFFF, 1);
        line(.3f, 2.2f, "Sumbi dives between the lines for a living Sloppy~♡");
        line(2.4f, 2.4f, "The page is filling up. I can't swim. Don't test that Sloppy~♡");
        line(5.9f, 1.5f, "Hey! I'm not a footnote Sloppy~♡");
        line(8, 2.8f, "She came up with a heart from the bottom of the page Sloppy~♡");
    }
}
