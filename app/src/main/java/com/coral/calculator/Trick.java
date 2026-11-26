package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

/**
 * One of the tricks that playing unlocks, on its stage: which scene it is, how long it runs
 * and what she says as she owns up; and the scenes that Page brawl and the ten small games
 * unlock. Those of Plus run are in {@link TrickRun}.
 */
final class Trick extends TrickRun {
    private static final int CORAL = CalculatorLayout.CORAL;
    /** How long each runs, in seconds, in the order of {@link Unlocks#TRICK_NAMES}. */
    private static final float[] LENGTHS = {11, 10.8f, 11, 10.5f, 11.4f, 11, 12.2f, 11, 10.8f, 10.4f, 10.2f, 10.4f, 10.8f, 11.2f, 11, 10.4f, 10.4f, 11, 0, 0,
            10.4f, 10.4f, 10, 11, 10, 10.4f, 10, 9.8f, 10, 10.4f, 9.8f, 10.4f, 9.6f, 10.8f, 10.4f, 10.2f};
    private static final String[] OWNING = {
            "Just kidding! Nothing fell on anything. This time Sloppy~♡",
            "Just kidding! Your apps mean what they say Sloppy~♡",
            "Just kidding! You're still the main character. Sadly Sloppy~♡",
            "Just kidding! Your paperwork is fine. Probably Sloppy~♡",
            "Just kidding! Not a drop of slime on it Sloppy~♡",
            "Just kidding! That was only my shadow. Mostly Sloppy~♡",
            "Just kidding! Back to how you dressed me Sloppy~♡",
            "Just kidding! Your apps aren't for sale. I checked Sloppy~♡",
            "Just kidding! Nobody locked your screen. I'd never Sloppy~♡",
            "Just kidding! All your apps still add up Sloppy~♡",
            "Just kidding! Nothing about you made the news Sloppy~♡",
            "Just kidding! Nothing is being said about your phone Sloppy~♡",
            "Just kidding! Not a cut on it. She's napping again Sloppy~♡",
            "Just kidding! No rice cakes behind your apps. I looked Sloppy~♡",
            "Just kidding! No secret door in your phone. That I'll tell you about Sloppy~♡",
            "Just kidding! Not a drop of ink on it Sloppy~♡",
            "Just kidding! Feet on the ground the whole time Sloppy~♡",
            "Just kidding! Your screen's dry. And shallow Sloppy~♡",
            "Just kidding Sloppy~♡",
            "Just kidding Sloppy~♡",
            "Just kidding! Nothing was locked. I just like jingling Sloppy~♡",
            "Just kidding! Not a crumb or a crack on it Sloppy~♡",
            "Just kidding! Nothing hit anything. Normally Sloppy~♡",
            "Just kidding! Dry as a diary. Rune says sorry Sloppy~♡",
            "Just kidding! Nobody complimented your screen Sloppy~♡",
            "Just kidding! That one didn't count Sloppy~♡",
            "Just kidding! I'd never cheat. On a screen this small Sloppy~♡",
            "Just kidding! Screen beats all three. Barely Sloppy~♡",
            "Just kidding! Your apps are face up. I peeked at all of them Sloppy~♡",
            "Just kidding! Those weren't your sums. Yours are worse Sloppy~♡",
            "Just kidding! There's only one of me. Lucky you Sloppy~♡",
            "Just kidding! Dry as a bone. And all positive Sloppy~♡",
            "Just kidding! No bubbles. I can count, you know Sloppy~♡",
            "Just kidding! Your apps aren't in check Sloppy~♡",
            "Just kidding! Not one mine. I checked every square Sloppy~♡",
            "Just kidding! I never touched your deck. You don't have one Sloppy~♡",
    };

    Trick(Context context, int trick, boolean dark, Bitmap art, Bitmap pet) { super(context, trick, dark, art, pet); }

    @Override float length() { return LENGTHS[kind]; }

    @Override String owningUp() { return OWNING[kind]; }

    @Override void scene(Canvas c) {
        switch (kind) {
            case Unlocks.BOOK: book(c); break;
            case Unlocks.BAT: bat(c); break;
            case Unlocks.REWRITE: rewrite(c); break;
            case Unlocks.STAMP: stampings(c); break;
            case Unlocks.SLIME: slime(c); break;
            case Unlocks.SHADOW: shadow(c); break;
            case Unlocks.COLOURS: colours(c); break;
            case Unlocks.STALL: stall(c); break;
            case Unlocks.ARENA: arena(c); break;
            case Unlocks.NOTHING: nothing(c); break;
            case Unlocks.GAZETTE: gazette(c); break;
            case Unlocks.FAN: fan(c); break;
            case Unlocks.NAP: nap(c); break;
            case Unlocks.CAKES: cakes(c); break;
            case Unlocks.DOOR: door(c); break;
            case Unlocks.INK_GUN: inkGun(c); break;
            case Unlocks.FLIGHT: flight(c); break;
            case Unlocks.DIVE: dive(c); break;
            case Unlocks.KEYS: keys(c); break;
            case Unlocks.BRICK: brick(c); break;
            case Unlocks.BOOMERANG: boomerang(c); break;
            case Unlocks.FLOOD: flood(c); break;
            case Unlocks.BEAM: beam(c); break;
            case Unlocks.CLASH: clash(c); break;
            case Unlocks.GRID: grid(c); break;
            case Unlocks.HANDS: hands(c); break;
            case Unlocks.FLIPPED: flipped(c); break;
            case Unlocks.MARKING: marking(c); break;
            case Unlocks.DOUBLES: doubles(c); break;
            case Unlocks.DOWNPOUR: downpour(c); break;
            case Unlocks.COUNTING: counting(c); break;
            case Unlocks.KNIGHT: knight(c); break;
            case Unlocks.FIELD: field(c); break;
            case Unlocks.DEAL: deal(c); break;
            default: break;
        }
    }

    /** A cut-in of one of the five, as when an ultimate is announced: her face, and what she fights with. */
    private void cutIn(Canvas c, int who, float from, float to) {
        float shown = out(p(from, from + .35f)) * (1 - p(to - .3f, to));
        if (shown <= 0) return;
        float y = T + dp(96), x = L + dp(56) - (1 - shown) * dp(160);
        portrait(c, who, x, y, dp(36), shown);
        // Her name and what she fights with, beside her face; what she fights with alone, where there is no room for both.
        String words = FightCast.NAMES[who] + "  ·  " + FightCast.WEAPONS[who];
        text.setTextSize(dp(11.5f));
        float room = R - dp(8) - (x + dp(44));
        if (text.measureText(words) + dp(15) > room) words = FightCast.WEAPONS[who];
        float half = (text.measureText(words) + dp(15)) / 2;
        plate(c, words, Math.min(x + dp(44) + half, R - dp(8) - half), y, dp(11.5f), Color.WHITE, FightCast.COLOURS[who] & 0xF2FFFFFF, shown);
    }

    // Page brawl

    /** Her own kit: she locks the user's apps, scans them, and has a key for each. One is the wrong key. */
    private void keys(Canvas c) {
        pickApps(5);
        int count = appCount();
        float row = appRow(), size = dp(52);
        cutIn(c, 0, .3f, 3);
        if (t < 1.4f) go(clamp(cx, L + wide, R - wide), floor, dp(400));
        else act(in(3.4f, 8.6f) ? PetArt.WORK : t >= 8.6f ? PetArt.WAVE : PetArt.IDLE);
        float scan = p(2, 3.3f);
        for (int which = 0; which < count; which++) {
            float x = appX(which, count), thrown = 3.6f + which * .7f, wrong = which == count - 1 ? 1.4f : 0, opened = t - (thrown + .5f + wrong);
            icon(c, which, x, row, size, 0, 1);
            float locked = p(.8f + which * .15f, 1.1f + which * .15f);
            if (opened < .5f) {
                // A padlock over it: its bow lifts as it opens.
                float lifts = opened > 0 ? opened / .5f : 0;
                pen(0xFFB8C0CA, dp(4), locked * (1 - lifts));
                box.set(x - dp(9), row - dp(20) - lifts * dp(9), x + dp(9), row + dp(2) - lifts * dp(9));
                c.drawArc(box, 180, 180, false, stroke);
                fill(0xFFFFC53D, locked * (1 - lifts));
                box.set(x - dp(14), row - dp(8), x + dp(14), row + dp(14));
                c.drawRoundRect(box, dp(4), dp(4), paint);
            } else if (opened < 1) {
                piece(c, STAR, x, row - size * .7f - (opened - .5f) * dp(40), dp(24), opened * 300, 2 - opened * 2);
            }
            for (int tried = 0; tried <= (wrong > 0 ? 1 : 0); tried++) {
                float flown = (t - thrown - tried * 1.4f) / .5f;
                if (flown > 0 && flown < 1) fx(c, KEY, hx + (x - hx) * flown, hy - tall * .6f + (row - hy + tall * .6f) * flown - (float) Math.sin(flown * Math.PI) * dp(50), dp(40), flown * 540, 1);
            }
            if (wrong > 0 && in(thrown + .5f, thrown + 1.5f)) plate(c, "401", x, row - size * .9f - (float) Math.abs(Math.sin((t - thrown) * 9)) * dp(8), dp(13), Color.WHITE, 0xF2D8342B, 1);
        }
        her(c);
        float tabletX = hx + (leftward ? -1 : 1) * wide * .72f, tabletY = hy - tall * .6f + (float) Math.sin(t * 3) * dp(4);
        fx(c, TABLET, tabletX, tabletY, dp(54), 0, p(1.4f, 1.8f) * (1 - p(9, 10)));
        if (scan > 0 && scan < 1) {
            float y = row - size + scan * size * 2;
            pen(0xFF5CE0E6, dp(3), 1);
            c.drawLine(L + dp(16), y, R - dp(16), y, stroke);
            fill(0x335CE0E6, 1);
            c.drawRect(L + dp(16), y - dp(26), R - dp(16), y, paint);
        }
        if (in(2.2f, 3.6f)) plate(c, "QA scan", cx, row + size * 1.5f, dp(12), 0xFF0E3A3D, 0xF2A6F1F4, 1);
        line(.3f, 2.4f, "I locked your apps. Don't panic. I have plenty of keys Sloppy~♡");
        line(3.3f, 2.6f, "QA scan first. Then one key each Sloppy~♡");
        line(3.6f + (count - 1) * .7f + .6f, 1.5f, "Wrong key. 401. Hold on Sloppy~♡");
        line(8.6f, 1.8f, "All unlocked. Chunked translation Sloppy~♡");
    }

    /** Chikuwa shares bread with the screen; one of the loaves is the brick. */
    private void brick(Canvas c) {
        cutIn(c, 1, .3f, 2.8f);
        float bakerX = L + dp(60), lands = t - 4.3f, spot = clamp(cx + dp(40), L + wide, R - wide), brickX = clamp(cx - dp(30), L + dp(80), R - dp(80)), brickY = T + H * .36f;
        strip(c, TrickArt.run_chikuwa, 3, t < 1.2f ? 2 : t < 4.5f ? 1 : t < 7.8f ? 0 : 2, bakerX + (1 - out(p(.2f, 1))) * -dp(160), floor, tall * 1.08f, false, 1);
        c.save();
        c.translate(shake(lands, dp(11)), shake(lands, dp(7)));
        for (int which = 0; which < 4; which++) {
            float thrown = 1.3f + which * .55f, flown = Math.min(1, Math.max(0, (t - thrown) / .7f));
            if (flown <= 0) continue;
            float x = L + W * (.3f + which * .17f), y = T + H * (.2f + rnd(which, 1) * .3f), jump = lands >= 0 && lands < .4f ? (float) Math.sin(lands / .4f * Math.PI) * dp(24) : 0;
            fx(c, LOAF, bakerX + (x - bakerX) * flown, floor - tall * .7f + (y - floor + tall * .7f) * flown - (float) Math.sin(flown * Math.PI) * dp(90) - jump, dp(54), flown * 360 + rnd(which, 2) * 40, 1);
        }
        float flown = Math.min(1, Math.max(0, (t - 3.5f) / .8f));
        boolean mended = t >= 8;
        if (flown > 0) {
            float x = bakerX + (brickX - bakerX) * flown, y = floor - tall * .7f + (brickY - floor + tall * .7f) * flown - (float) Math.sin(flown * Math.PI) * dp(170);
            fx(c, mended ? LOAF : BRICK, x, y, dp(mended ? 60 : 78), flown * 360 * (mended ? 0 : 1), 1);
            if (lands >= 0 && !mended) for (int which = 0; which < 7; which++) crack(c, brickX, brickY, dp(60 + rnd(which, 3) * 90) * Math.min(1, lands / .12f), which * .9f + rnd(which, 4), which, 1);
            if (lands >= 0) plate(c, mended ? "bread" : "\"bread\"", brickX, brickY + dp(58), dp(12), mended ? Color.WHITE : 0xFF292B2A, mended ? 0xF22E9E5B : 0xF2FFFFFF, Math.min(1, lands / .3f));
        }
        if (lands >= 0 && lands < .35f) fx(c, BURST, brickX, brickY, dp(170), 0, 1 - lands / .35f);
        if (t < 3.6f) go(spot, floor, dp(380));
        else if (in(3.6f, 5)) squash = .62f;
        else if (in(7, 8.2f)) {
            go(clamp(brickX + dp(50), L + wide / 2, R - wide / 2), clamp(brickY + tall * .9f, T + tall, floor), dp(480));
            armed(0, t > 7.7f ? 7 : 0);
        } else if (t >= 8.2f) act(PetArt.WAVE);
        her(c);
        c.restore();
        if (in(1.4f, 3.4f)) plate(c, "Everyone gets bread!", bakerX + dp(64), floor - tall * 1.22f, dp(12), 0xFF292B2A, 0xF2FFFFFF, 1);
        line(.3f, 2, "Chikuwa's sharing bread with your screen Sloppy~♡");
        line(3.4f, 1.4f, "Wait. That one's the brick Sloppy~♡");
        line(5, 2.2f, "Brick bread. The meaning got knocked off it Sloppy~♡");
        line(8, 2.2f, "Names stay names. There. Bread again Sloppy~♡");
    }

    /** Tsubakieh throws the bat, which goes round the screen and comes back, by way of Halgakos's head. */
    private void boomerang(Canvas c) {
        cutIn(c, 2, .3f, 2.6f);
        float batterX = L + dp(60), thrown = t - 1.8f, round = 4.6f;
        boolean out = thrown >= 0 && thrown < round;
        strip(c, TrickArt.run_tsubakieh, 6, t < 1.2f ? 1 : t < 1.8f ? 2 : out ? 3 : 1, batterX + (1 - out(p(.2f, 1))) * -dp(160), floor, tall * 1.08f, false, 1);
        float spot = clamp(cx + W * .18f, L + wide, R - wide / 2);
        if (t < 1.6f) go(spot, floor, dp(380));
        double way = thrown / round * Math.PI * 4;
        float batX = cx + (batterX - cx) * (float) Math.cos(way), batY = floor - tall * .55f - (float) Math.abs(Math.sin(way)) * H * .34f * (thrown < round / 2 ? 1 : .05f);
        // It goes over her head once; the second time round it does not.
        boolean near = out && Math.abs(batX - hx) < dp(70) && thrown < round / 2 + .2f, struck = thrown >= round * .75f - .1f && t < 8;
        if (struck) {
            wear(LOSE, 6, thrown < round * .75f + .4f ? 0 : 3, 1.1f);
            leftward = false;
        } else if (near) {
            squash = .6f;
        } else if (t >= 8) {
            act(PetArt.SAD);
        }
        her(c);
        if (out) fx(c, BAT, batX, batY, dp(92), thrown * 900, 1);
        if (thrown >= round * .75f - .1f && thrown < round * .75f + .25f) fx(c, BURST, hx, hy - tall * .6f, dp(120), 0, 1);
        if (t >= 7.8f) plate(c, "A perfectly normal home run.", batterX + dp(70), floor - tall * 1.22f, dp(12), 0xFF292B2A, 0xF2FFFFFF, p(7.8f, 8.1f));
        line(.3f, 1.4f, "Tsubakieh's warming up. Mind your head Sloppy~♡");
        line(2, 2.4f, "Boomerang bat! It comes back. Duck Sloppy~♡");
        line(5.4f, 2.2f, "Ow. It came back Sloppy~♡");
        line(8, 1.8f, "A normal amount of pain, she says Sloppy~♡");
    }

    /** Rune cries on the screen: tears, every third one a knife, and then the flood. */
    private void flood(Canvas c) {
        cutIn(c, 3, .3f, 2.8f);
        float runeX = R - dp(64), perch = T + dp(210), level = B - (B - T) * .6f * ease(p(4.6f, 8)) * (1 - ease(p(8.8f, 10.2f)));
        strip(c, TrickArt.run_rune, 3, t < 8.6f ? 0 : 2, runeX + (1 - out(p(.2f, 1))) * dp(160), perch, tall * 1.05f, true, 1);
        if (t < 1.4f) go(clamp(cx - dp(40), L + wide, R - wide), floor, dp(380));
        else if (t < 5.4f) go(clamp(cx - dp(40) + (float) Math.sin(t * 2.6f) * W * .22f, L + wide / 2, R - wide / 2), floor, dp(330));
        for (int which = 0; which < 34; which++) {
            float began = 1.2f + which * .2f, fell = (t - began) / 1.3f;
            if (fell <= 0 || fell >= 1 || t > 8.4f) continue;
            float x = L + dp(30) + rnd(which, 1) * (W - dp(60)), y = T - dp(20) + fell * (Math.min(level, floor) - T);
            fx(c, which % 3 == 2 ? KNIFE : TEAR, x, y, dp(which % 3 == 2 ? 52 : 34), 90, 1);
        }
        fill(0xD927348F, 1);
        path.reset();
        path.moveTo(L, B);
        for (int wave = 0; wave <= 12; wave++) path.lineTo(L + W * wave / 12f, level + (float) Math.sin(t * 2.2f + wave) * dp(6));
        path.lineTo(R, B);
        path.close();
        if (level < B - dp(2)) c.drawPath(path, paint);
        // Afloat on a book, once it is up to her knees.
        float afloat = Math.max(0, hy - tall * .25f - level);
        if (afloat > 0) {
            lift = afloat + tall * .3f + (float) Math.sin(t * 3) * dp(4);
            wear(LOSE, 6, 2, 1.1f);
            piece(c, BOOK, hx, hy - lift + dp(10), dp(92), (float) Math.sin(t * 3) * 5, 1);
        }
        her(c);
        if (in(8.6f, 11)) plate(c, "S-sorry about your screen...", runeX - dp(40), perch - tall * 1.2f, dp(12), 0xFF292B2A, 0xF2FFFFFF, p(8.6f, 8.9f));
        line(.3f, 2.2f, "Don't look now. Rune's crying on your screen Sloppy~♡");
        line(2.6f, 2.2f, "Every third tear is a knife. Don't ask Sloppy~♡");
        line(5.4f, 2.6f, "Flood of tears! I can't swim in ink either Sloppy~♡");
        line(8.6f, 2.2f, "There, there, crybaby. It was a nice screen Sloppy~♡");
    }

    /** Illya charges a heart beam at the screen: compliments, thrown very hard. Halgakos holds her tablet up to it. */
    private void beam(Canvas c) {
        cutIn(c, 4, .3f, 2.6f);
        float illyaX = R - dp(62), y = floor - tall * .58f, fired = p(2.8f, 3.1f) * (1 - p(5.6f, 6)), charge = p(1.2f, 2.8f);
        strip(c, TrickArt.run_illya, 4, t < 1.2f ? 0 : t < 6 ? 1 : 2, illyaX + (1 - out(p(.2f, 1))) * dp(160), floor, tall * 1.1f, true, 1);
        float spot = clamp(L + W * .28f, L + wide, R - wide);
        if (t < 1.8f) go(spot, floor, dp(380));
        else if (in(6.2f, 8.2f)) act(PetArt.SAD);
        else if (t >= 8.2f) act(PetArt.WAVE);
        leftward = t >= 1.8f ? false : leftward;
        her(c);
        float shieldX = hx + wide * .7f;
        if (fired > 0) {
            float thick = dp(46) * fired;
            fill(0xE6FF7FB2, 1);
            box.set(shieldX, y - thick / 2, illyaX - dp(30), y + thick / 2);
            c.drawRoundRect(box, thick / 2, thick / 2, paint);
            fill(0xF2FFFFFF, 1);
            box.set(shieldX, y - thick * .2f, illyaX - dp(30), y + thick * .2f);
            c.drawRoundRect(box, thick * .2f, thick * .2f, paint);
            String[] said = {"You are wonderful", "Nice wallpaper", "Lovely thumbs", "Such tidy icons"};
            for (int which = 0; which < said.length; which++) {
                float along = (t * .5f + which * .25f) % 1;
                words(c, said[which], illyaX - dp(40) - along * (illyaX - shieldX - dp(60)), y - thick * (.9f + (which % 2) * .5f), dp(11.5f), 0xFFE0457F, (1 - Math.abs(along - .5f) * 2) * fired);
                fx(c, HEART, illyaX - dp(30) - (along * 1.7f % 1) * (illyaX - shieldX), y + (float) Math.sin(along * 20 + which) * thick * .5f, dp(22), 0, fired);
            }
            // What gets past the tablet goes off above and below her.
            pen(0xE6FF7FB2, dp(9), fired);
            c.drawLine(shieldX, y, shieldX - dp(90), y - dp(120), stroke);
            c.drawLine(shieldX, y, shieldX - dp(90), y + dp(70), stroke);
        }
        fx(c, TABLET, shieldX, y, dp(70), 90, p(2.2f, 2.6f) * (1 - p(6, 6.6f)));
        if (charge > 0 && t < 3) fx(c, MAGIC, illyaX - dp(34), y, dp(30 + charge * 60), t * 200, 1);
        if (in(6, 8)) {
            fill(0x99FF7FB2, (float) Math.abs(Math.sin(t * 4)));
            c.drawCircle(hx - wide * .14f, hy - tall * .52f, dp(5), paint);
            c.drawCircle(hx + wide * .14f, hy - tall * .52f, dp(5), paint);
        }
        line(.3f, 2, "Illya's charging a heart beam. At your screen Sloppy~♡");
        line(3, 2.6f, "It's just compliments. Thrown very hard Sloppy~♡");
        line(6.1f, 2.1f, "N-no. I'm not blushing. It's the beam Sloppy~♡");
        line(8.2f, 1.6f, "Grand compliment, she calls it Sloppy~♡");
    }

    /** A tap battle between her and the user, which she wins by herself. */
    private void clash(Canvas c) {
        float shown = ease(p(.6f, 1.3f)) * (1 - p(9.4f, 10.4f)), y = clamp(T + H * .3f, T + dp(170), floor - tall - dp(120)), half = Math.min(W * .4f, dp(190));
        float left = 5.6f - (t - 1.6f), share = t < 1.6f ? .5f : left > 0 ? Math.min(.86f, .5f + (t - 1.6f) * .06f + (float) Math.sin(t * 9) * .02f) : 1;
        wash(c, 0x99101018, shown * .5f);
        portrait(c, 0, cx - half + dp(26), y - dp(54), dp(30), shown);
        fill(0xFF8E97A3, shown);
        c.drawCircle(cx + half - dp(26), y - dp(54), dp(30), paint);
        words(c, "?", cx + half - dp(26), y - dp(54), dp(30), Color.WHITE, shown);
        // The bar: who is winning.
        fill(0xE61B1D22, shown);
        box.set(cx - half - dp(6), y - dp(16), cx + half + dp(6), y + dp(16));
        c.drawRoundRect(box, dp(16), dp(16), paint);
        fill(FightCast.COLOURS[0], shown);
        box.set(cx - half, y - dp(10), cx - half + half * 2 * share, y + dp(10));
        c.drawRoundRect(box, dp(10), dp(10), paint);
        fill(0xFF6FB6E8, shown);
        box.set(cx - half + half * 2 * share, y - dp(10), cx + half, y + dp(10));
        if (share < 1) c.drawRoundRect(box, dp(10), dp(10), paint);
        fill(Color.WHITE, shown);
        if (share < 1) c.drawRect(cx - half + half * 2 * share - dp(2), y - dp(15), cx - half + half * 2 * share + dp(2), y + dp(15), paint);
        // The time left, as a number and nothing more.
        if (t >= 1.6f && left > 0) {
            words(c, String.valueOf((int) Math.ceil(left)), cx, y - dp(58), dp(36), Color.WHITE, 1);
            if ((t * 4) % 1 < .6f) words(c, "TAP!", cx, y + dp(46), dp(22), 0xFFFFD84D, 1);
        }
        if (t < 1.4f) go(clamp(cx - half * .5f, L + wide, R - wide), floor, dp(380));
        else if (left > 0) {
            act(PetArt.WORK);
            for (int tap = 0; tap < 3; tap++) {
                float age = (t * 5 + tap / 3f) % 1;
                pen(Color.WHITE, dp(2.5f), 1 - age);
                c.drawCircle(hx + (rnd((int) (t * 5) + tap, 1) - .5f) * dp(80), hy - tall * 1.2f - rnd((int) (t * 5) + tap, 2) * dp(40), dp(8) + age * dp(22), stroke);
            }
        } else act(t < 8 ? PetArt.JUMP : PetArt.WAVE);
        her(c);
        float won = t - 7.2f;
        if (won >= 0) {
            if (won < .4f) fx(c, BURST, cx, y, dp(240) * (1 + won), 0, 1 - won / .4f);
            plate(c, FightCast.GREATER[0], cx, y + dp(48), dp(15), Color.WHITE, FightCast.COLOURS[0] & 0xF2FFFFFF, Math.min(1, won / .3f) * shown);
            // The pages of the whole novel come down.
            for (int which = 0; which < 22; which++) {
                float fell = (won * (.25f + rnd(which, 1) * .2f) + rnd(which, 2) * .4f);
                if (fell > 1) continue;
                c.save();
                c.rotate(fell * 300 + which * 40, L + rnd(which, 3) * W, T + fell * H);
                fill(0xF2FFFFFF, 1);
                c.drawRect(L + rnd(which, 3) * W - dp(11), T + fell * H - dp(15), L + rnd(which, 3) * W + dp(11), T + fell * H + dp(15), paint);
                c.restore();
            }
        }
        line(.3f, 1.4f, "Tap battle. Me against you. Ready Sloppy~♡");
        line(2.2f, 2.6f, "Tap faster. It's in the name Sloppy~♡");
        line(7.3f, 2.6f, "I win. Whole novel translation Sloppy~♡");
    }

    // The ten small games

    /** Noughts and crosses over the screen, the user's crosses played for them; one of them falls off. */
    private void grid(Canvas c) {
        float cell = Math.min(W * .24f, dp(96)), top = clamp(cy - cell * 1.9f, T + dp(110), floor - tall - cell * 3 - dp(20)), left = cx - cell * 1.5f;
        float drawn = p(.6f, 1.5f), gone = 1 - p(9, 10);
        pen(Color.WHITE, dp(5), drawn * gone);
        for (int which = 1; which <= 2; which++) {
            c.drawLine(left + cell * which, top, left + cell * which, top + cell * 3 * drawn, stroke);
            c.drawLine(left, top + cell * which, left + cell * 3 * drawn, top + cell * which, stroke);
        }
        int[] where = {4, 0, 8, 2, 1, 6, 3};
        float[] when = {1.8f, 2.5f, 3.2f, 3.9f, 4.6f, 5.3f, 6};
        for (int move = 0; move < where.length; move++) {
            float since = t - when[move], x = left + cell * (where[move] % 3 + .5f), y = top + cell * (where[move] / 3 + .5f), mark = cell * .28f * Math.min(1, since / .15f);
            if (since < 0) continue;
            boolean cross = move % 2 == 0;
            // The cross in her way comes loose, falls, and a nought is there instead.
            float fell = where[move] == 1 ? p(6.8f, 7.5f) : 0;
            if (cross) {
                pen(0xFFB8C0CA, dp(6), gone * (1 - fell));
                c.save();
                c.rotate(fell * 70, x, y);
                c.translate(0, fell * fell * dp(260));
                c.drawLine(x - mark, y - mark, x + mark, y + mark, stroke);
                c.drawLine(x + mark, y - mark, x - mark, y + mark, stroke);
                c.restore();
                if (fell >= 1) {
                    pen(CORAL, dp(6), gone);
                    c.drawCircle(x, y, cell * .28f * p(7.5f, 7.7f), stroke);
                }
            } else {
                pen(CORAL, dp(6), gone);
                c.drawCircle(x, y, mark, stroke);
            }
        }
        float through = p(7.8f, 8.2f);
        if (through > 0) {
            pen(0xFFFFD84D, dp(7), gone);
            c.drawLine(left + cell * .15f, top + cell * .5f, left + cell * .15f + cell * 2.7f * through, top + cell * .5f, stroke);
        }
        if (t < 1.6f) go(clamp(cx, L + wide, R - wide), floor, dp(380));
        else if (in(6.6f, 7.6f)) {
            go(clamp(left + cell * 1.5f, L + wide / 2, R - wide / 2), clamp(top + cell + tall, T + tall, floor), dp(520));
        } else if (t >= 7.8f) {
            go(clamp(cx, L + wide, R - wide), floor, dp(420));
            if (t > 8.6f) act(PetArt.WAVE);
        } else act(PetArt.WORK);
        her(c);
        line(.3f, 2, "Noughts and crosses. On your screen. You're crosses Sloppy~♡");
        line(4.6f, 1.6f, "Blocked? Hmph Sloppy~♡");
        line(6.7f, 1.2f, "Oops. Your cross fell off Sloppy~♡");
        line(7.9f, 2, "Three in a row. Too easy Sloppy~♡");
    }

    /** Rock, paper and scissors, each played against the screen. */
    private void hands(Canvas c) {
        float snip = p(1.6f, 3), cover = ease(p(3.4f, 4.4f)) * (1 - into(p(6.2f, 7.2f))), drop = t - 5.6f, cutY = T + H * .34f;
        // Scissors go across, and leave their cut.
        if (snip > 0) {
            float fading = 1 - p(8.6f, 9.6f);
            pen(0xCC000000, dp(3), fading);
            for (float x = L; x < L + W * snip; x += dp(22)) c.drawLine(x, cutY, x + dp(12), cutY, stroke);
            if (snip < 1) {
                float x = L + W * snip, open = (float) Math.abs(Math.sin(t * 14)) * 22;
                pen(0xFFDDE3E8, dp(7), 1);
                c.save();
                c.rotate(open, x, cutY);
                c.drawLine(x - dp(46), cutY, x + dp(22), cutY, stroke);
                c.restore();
                c.save();
                c.rotate(-open, x, cutY);
                c.drawLine(x - dp(46), cutY, x + dp(22), cutY, stroke);
                c.restore();
                pen(CORAL, dp(5), 1);
                c.drawCircle(x - dp(58), cutY - dp(12), dp(11), stroke);
                c.drawCircle(x - dp(58), cutY + dp(12), dp(11), stroke);
            }
        }
        // Paper comes down over everything; the rock goes through it.
        if (cover > 0) {
            float fall = into(p(6.2f, 7.2f)) * H;
            fill(0xF0FBF8EF, 1);
            c.drawRect(L, T + fall, R, T + H * cover + fall, paint);
            pen(0xFFC9D4E6, dp(1.5f), 1);
            for (float y = T + dp(60) + fall; y < T + H * cover + fall; y += dp(34)) c.drawLine(L + dp(20), y, R - dp(20), y, stroke);
        }
        float rockY = drop < 0 ? T - dp(80) : T - dp(80) + (cy - T + dp(80)) * into(Math.min(1, drop / .45f));
        if (drop >= 0 && t < 8.8f) {
            if (drop >= .45f) for (int which = 0; which < 8; which++) crack(c, cx, cy, dp(70 + rnd(which, 1) * 110) * Math.min(1, (drop - .45f) / .12f), which * .8f + rnd(which, 2), which, 1 - p(8, 8.8f));
            fill(0xFF8E97A3, 1 - p(8.2f, 8.8f));
            c.drawOval(cx - dp(54), rockY - dp(42), cx + dp(54), rockY + dp(42), paint);
            fill(0xFFB8C0CA, 1 - p(8.2f, 8.8f));
            c.drawOval(cx - dp(34), rockY - dp(32), cx + dp(8), rockY - dp(6), paint);
        }
        c.save();
        c.translate(0, shake(drop - .45f, dp(10)));
        if (t < 1.4f) go(clamp(cx + W * .24f, L + wide, R - wide / 2), floor, dp(380));
        else if (t >= 7.4f) act(PetArt.WAVE);
        her(c);
        c.restore();
        int mine = t < 3 ? 0 : t < 5.4f ? 1 : t < 6.4f ? 2 : 3;
        plate(c, "Halgakos " + mine + "  ·  Screen 0", cx, T + dp(64), dp(13), 0xFF292B2A, 0xF2FFFFFF, p(1.2f, 1.6f));
        line(.3f, 1.3f, "Rock, paper, scissors. Against your screen Sloppy~♡");
        line(1.7f, 1.6f, "Scissors cut screen Sloppy~♡");
        line(3.5f, 1.8f, "Paper covers screen Sloppy~♡");
        line(5.7f, 1.7f, "Rock breaks paper. And nearly the screen Sloppy~♡");
        line(7.5f, 2, "Three nil. A clean sweep Sloppy~♡");
    }

    /** The user's apps turned face down as cards, for her to find the pairs of. */
    private void flipped(Canvas c) {
        pickApps(4);
        int[] faces = {0, 1, 2, 3, 1, 0, 3, 2};
        // When each card is turned up, when it is turned back, and when it is turned up again for good.
        float[] up = {1.6f, 1.9f, 5.4f, 6.2f, 4.4f, 3.5f, 6.5f, 5.7f}, down = {2.7f, 2.7f, 99, 99, 99, 99, 99, 99}, again = {3.2f, 4.1f, 99, 99, 99, 99, 99, 99};
        float cardW = Math.min((W - dp(60)) / 4, dp(82)), cardH = cardW * 1.3f, top = clamp(cy - cardH * 1.2f, T + dp(110), floor - tall - cardH * 2 - dp(30)), dealt = 1 - p(9, 10);
        for (int card = 0; card < 8; card++) {
            float x = cx + (card % 4 - 1.5f) * (cardW + dp(8)), y = top + (card / 4 + .5f) * (cardH + dp(8)), arrived = out(p(.5f + card * .08f, .9f + card * .08f));
            float turn = t >= again[card] ? Math.min(1, (t - again[card]) / .3f) : t >= down[card] ? 1 - Math.min(1, (t - down[card]) / .3f) : Math.min(1, Math.max(0, (t - up[card]) / .3f));
            boolean face = turn > .5f;
            float across = Math.abs(turn * 2 - 1);
            c.save();
            c.translate(0, (1 - arrived) * -H);
            c.scale(Math.max(.04f, across), 1, x, y);
            fill(face ? 0xFFFFFFFF : CORAL, dealt);
            box.set(x - cardW / 2, y - cardH / 2, x + cardW / 2, y + cardH / 2);
            c.drawRoundRect(box, dp(9), dp(9), paint);
            pen(face ? 0xFFE4E4E4 : 0xFFFFFFFF, dp(2), dealt);
            c.drawRoundRect(box, dp(9), dp(9), stroke);
            if (face) icon(c, faces[card], x, y, cardW * .62f, 0, dealt);
            else words(c, "?", x, y, cardW * .5f, Color.WHITE, dealt);
            c.restore();
            float kept = t - Math.max(up[card], again[card] < 99 ? again[card] : 0) - .35f;
            if (kept > 0 && kept < .5f && (card >= 2 || t >= 3.2f)) piece(c, STAR, x, y - cardH * .5f - kept * dp(30), dp(22), kept * 300, 1 - kept * 2);
        }
        int turns = t < 1.9f ? 0 : t < 3.5f ? 1 : t < 4.4f ? 2 : t < 5.7f ? 3 : t < 6.5f ? 4 : 5;
        plate(c, "Turns: " + turns, cx, top - dp(22), dp(13), 0xFF292B2A, 0xF2FFFFFF, p(1, 1.4f) * dealt);
        if (t < 1.4f) go(clamp(cx, L + wide, R - wide), floor, dp(380));
        else act(t < 6.9f ? PetArt.WORK : PetArt.WAVE);
        her(c);
        line(.3f, 1.6f, "I turned your apps face down. Find the pairs Sloppy~♡");
        line(2.2f, 1.2f, "Wrong~ Not even close Sloppy~♡");
        line(3.6f, 2.4f, "I remember where everything is. Unlike you Sloppy~♡");
        line(7, 2.4f, "All four pairs in five turns. Top that Sloppy~♡");
    }

    /** She finds some sums on the screen and marks them. */
    private void marking(Canvas c) {
        String[] sums = {"7 × 8 = 54", "12 + 9 = 21", "100 − 1 = 98", "6 × 6 = 36", "50% of 90 = 40"}, right = {"56", "", "99", "", "45"};
        float top = clamp(T + H * .2f, T + dp(120), floor - tall - dp(250)), gap = Math.min(dp(48), (floor - tall - dp(30) - top) / 5), x = clamp(cx - dp(30), L + dp(120), R - dp(140));
        float gone = 1 - p(9.4f, 10.4f), clock = 1 - p(1.4f, 8);
        fill(0xE61B1D22, p(1, 1.4f) * gone);
        box.set(cx - dp(110), T + dp(58), cx + dp(110), T + dp(70));
        c.drawRoundRect(box, dp(6), dp(6), paint);
        fill(CORAL, p(1, 1.4f) * gone);
        box.set(cx - dp(108), T + dp(60), cx - dp(108) + dp(216) * clock, T + dp(68));
        c.drawRoundRect(box, dp(4), dp(4), paint);
        int marks = 0;
        for (int which = 0; which < sums.length; which++) {
            float y = top + which * gap, shown = out(p(.6f + which * .15f, 1 + which * .15f)), marked = t - (2.4f + which * 1.05f);
            plate(c, sums[which], x, y, dp(15), 0xFF292B2A, 0xF2FFFFFF, shown * gone);
            if (marked < 0) continue;
            float drawn = Math.min(1, marked / .25f), markX = x + dp(104);
            if (right[which].isEmpty()) {
                marks++;
                pen(0xFF2E9E5B, dp(4), gone);
                c.drawLine(markX - dp(9), y, markX - dp(9) + dp(7) * drawn, y + dp(8) * drawn, stroke);
                if (drawn >= 1) c.drawLine(markX - dp(2), y + dp(8), markX + dp(12), y - dp(10), stroke);
            } else {
                pen(0xFFD8342B, dp(4), gone);
                c.drawLine(markX - dp(9), y - dp(9), markX - dp(9) + dp(18) * drawn, y - dp(9) + dp(18) * drawn, stroke);
                if (drawn >= 1) c.drawLine(markX + dp(9), y - dp(9), markX - dp(9), y + dp(9), stroke);
                words(c, right[which], markX + dp(34), y, dp(15), 0xFFD8342B, Math.min(1, Math.max(0, (marked - .3f) / .2f)) * gone);
            }
        }
        if (t < 2.2f) go(clamp(x + dp(150), L + wide / 2, R - wide / 2), clamp(top + tall * .7f, T + tall, floor), dp(480));
        else if (t < 7.7f) {
            go(clamp(x + dp(150), L + wide / 2, R - wide / 2), clamp(top + Math.min(4, (int) ((t - 2.2f) / 1.05f)) * gap + tall * .7f, T + tall, floor), dp(260));
            leftward = true;
            armed(0, (t - 2.4f) % 1.05f < .35f ? 7 : 0);
        } else {
            go(clamp(cx, L + wide, R - wide), floor, dp(420));
            if (t > 8.6f) act(PetArt.WAVE);
        }
        her(c);
        if (t >= 7.9f) {
            float since = t - 7.9f, slam = since < .14f ? 2.2f - 1.2f * since / .14f : 1;
            pen(0xFFD8342B, dp(5), gone);
            c.drawCircle(x - dp(96), top + gap * 4.6f, dp(34) * slam, stroke);
            words(c, marks + " / 5", x - dp(96), top + gap * 4.6f, dp(19) * slam, 0xFFD8342B, gone);
        }
        line(.3f, 1.8f, "I found your sums. I'm marking them Sloppy~♡");
        line(2.6f, 2.2f, "Seven times eight is fifty-six. It always was Sloppy~♡");
        line(5.2f, 2.2f, "A hundred minus one. Take your time Sloppy~♡");
        line(8, 2.2f, "Two out of five. See me after class Sloppy~♡");
    }

    /** Catch Halgakos, with three of her about the screen. */
    private void doubles(Canvas c) {
        float gone = 1 - p(9, 9.6f);
        int step = (int) ((t - 1.2f) / .85f);
        if (t < 1.2f) go(clamp(cx, L + wide, R - wide), floor, dp(380));
        else if (t < 7.2f) go(L + wide + rnd(step, 1) * (W - wide * 2), T + tall + dp(90) + rnd(step, 2) * (floor - T - tall - dp(90)), dp(560));
        else {
            go(clamp(cx, L + wide, R - wide), floor, dp(520));
            if (t > 8) act(PetArt.WAVE);
        }
        // The other two go where she went a moment ago, and where she will go next.
        for (int other = 0; other < 2 && t >= 1.2f; other++) {
            int theirs = step + (other == 0 ? 3 : 7);
            float along = ease(Math.min(1, ((t - 1.2f) % .85f) / .6f));
            float fromX = L + wide + rnd(theirs - 1, 1 + other * 5) * (W - wide * 2), fromY = T + tall + dp(90) + rnd(theirs - 1, 2 + other * 5) * (floor - T - tall - dp(90));
            float toX = L + wide + rnd(theirs, 1 + other * 5) * (W - wide * 2), toY = T + tall + dp(90) + rnd(theirs, 2 + other * 5) * (floor - T - tall - dp(90));
            float x = fromX + (toX - fromX) * along, y = fromY + (toY - fromY) * along, poof = t - 7.2f - other * .3f;
            if (poof < 0) {
                herAlpha = .82f;
                copy(c, x, y);
                herAlpha = 1;
            } else if (poof < .4f) {
                fx(c, BURST, x, y - tall * .5f, tall * (1 + poof * 2), 0, 1 - poof / .4f);
            }
        }
        her(c);
        float clock = Math.max(0, 30 - (t - 1.2f) * 4.2f);
        plate(c, "Tap the real one   ·   " + (int) Math.ceil(clock) + " s", cx, T + dp(64), dp(13), 0xFF292B2A, 0xF2FFFFFF, p(.8f, 1.2f) * gone);
        line(.3f, 2, "Catch me. The real me. There are three Sloppy~♡");
        line(3.4f, 2, "Too slow. That one was a fake Sloppy~♡");
        line(7.4f, 2.2f, "They were all me. I'm very fast Sloppy~♡");
    }

    /** Number rain over the screen: she catches the pluses, and the minus signs pile up. */
    private void downpour(Canvas c) {
        float[] bumps = {3, 5, 6.6f};
        int caught = 0, hearts = 3, heaped = 0;
        for (float bump : bumps) if (t >= bump) hearts--;
        if (t < 1.2f) go(clamp(cx, L + wide, R - wide), floor, dp(380));
        else if (t < 6.6f) go(clamp(cx + (float) Math.sin(t * 1.9f) * W * .3f, L + wide / 2, R - wide / 2), floor, dp(360));
        boolean bumped = false;
        for (float bump : bumps) bumped |= t >= bump && t < bump + (bump > 6 ? 2 : .5f);
        if (bumped) wear(LOSE, 6, t >= 6.6f ? 3 : 0, 1.1f);
        else if (t >= 8.6f) act(PetArt.SAD);
        her(c);
        for (int which = 0; which < 40; which++) {
            boolean plus = which % 5 < 2;
            float began = 1.2f + which * .16f, fell = (t - began) / 1.4f, x = L + dp(24) + rnd(which, 1) * (W - dp(48));
            if (fell <= 0) continue;
            if (fell < 1) {
                piece(c, plus ? PLUS : MINUS, x, T - dp(20) + fell * (floor - T + dp(8)), dp(28), 0, 1);
            } else if (plus) {
                caught++;
            } else {
                // What nobody caught lies where it fell, in a heap along the bottom.
                piece(c, MINUS, x, floor + dp(4) - (heaped++ / 8) * dp(13), dp(28), rnd(which, 2) * 30 - 15, 1 - p(9.2f, 10.2f));
            }
        }
        if (in(6.3f, 6.9f)) piece(c, MINUS, hx, T + (hy - tall - T) * into(p(6.3f, 6.6f)), dp(96), 0, 1);
        for (int heart = 0; heart < 3; heart++) piece(c, RED_HEART, cx - dp(28) + heart * dp(28), T + dp(64), dp(24), 0, heart < hearts ? 1 : .25f);
        plate(c, "+ " + caught, cx, T + dp(96), dp(13), Color.WHITE, 0xF2E8554B, p(1.2f, 1.6f));
        line(.3f, 1.8f, "Number rain. Without an umbrella Sloppy~♡");
        line(3, 1.4f, "Ow. Not that one Sloppy~♡");
        line(6.8f, 2.3f, "That's three bumps. Your screen is all minus signs Sloppy~♡");
        line(9.1f, 1.2f, "I only like the pluses Sloppy~♡");
    }

    /** Bubbles to pop from the smallest number up, which she does not quite manage. */
    private void counting(Canvas c) {
        float[] popped = {2, 2.6f, 3.2f, 4.8f, 5.3f, 6.4f, 6.5f, 6.6f};
        float wrong = t - 3.9f;
        if (t < 1.4f) go(clamp(cx, L + wide, R - wide), floor, dp(380));
        else if (in(6.2f, 7)) {
            act(PetArt.JUMP);
            lift = (float) Math.sin(p(6.2f, 7) * Math.PI) * tall * .8f;
        } else act(t < 6.2f ? PetArt.WORK : PetArt.WAVE);
        her(c);
        for (int which = 0; which < 8; which++) {
            float rose = out(p(.5f + which * .1f, 1.5f + which * .1f)), x = L + W * (.14f + rnd(which, 1) * .72f) + (float) Math.sin(t * 1.3f + which) * dp(12);
            float y = B + dp(40) - rose * (B + dp(40) - (T + H * (.16f + rnd(which, 2) * .36f))) + (float) Math.cos(t * 1.1f + which * 2) * dp(9), since = t - popped[which];
            boolean shaken = which == 4 && wrong >= 0 && wrong < .5f;
            if (since < 0) {
                float sway = shaken ? (float) Math.sin(wrong * 60) * dp(5) : 0;
                fill(dark ? 0xF21D2023 : 0xF2FFFFFF, 1);
                c.drawCircle(x + sway, y, dp(28), paint);
                pen(CORAL, dp(shaken ? 4 : 2.5f), 1);
                c.drawCircle(x + sway, y, dp(28), stroke);
                words(c, String.valueOf(which + 1), x + sway, y, dp(20), shaken ? CORAL : dark ? 0xFFF3F3F3 : 0xFF292B2A, 1);
                if (shaken) plate(c, "+ 2 s", x, y - dp(44), dp(12), Color.WHITE, 0xF2D8342B, 1);
            } else if (since < .4f) {
                pen(CORAL, dp(3), 1 - since / .4f);
                c.drawCircle(x, y, dp(28) + since * dp(60), stroke);
            }
        }
        line(.3f, 1.6f, "Bubbles on your screen. Smallest first Sloppy~♡");
        line(3.9f, 2, "Five? I meant four. Two seconds' penalty Sloppy~♡");
        line(6.6f, 2.4f, "Six, seven, eight. All at once. That counts Sloppy~♡");
    }

    /** Chess with the user's apps for pieces: a knight takes one, and a pawn walks the whole board. */
    private void knight(Canvas c) {
        pickApps(1);
        float cell = Math.min(W / 6.2f, dp(64)), top = clamp(cy - cell * 3.2f, T + dp(100), floor - tall - cell * 6), left = cx - cell * 3, board = ease(p(.3f, 1)) * (1 - p(9.6f, 10.6f));
        for (int square = 0; square < 36; square++) {
            fill((square / 6 + square % 6) % 2 == 0 ? 0x40FFFFFF : 0x40000000, board);
            c.drawRect(left + square % 6 * cell, top + square / 6 * cell, left + (square % 6 + 1) * cell, top + (square / 6 + 1) * cell, paint);
        }
        // The knight's way to the app, a hop at a time; the app stands where it ends.
        int[][] hops = {{0, 0}, {2, 1}, {1, 3}, {3, 4}};
        float[] when = {1.2f, 2, 2.8f, 3.6f};
        int at = 0;
        for (int hop = 1; hop < hops.length; hop++) if (t >= when[hop]) at = hop;
        float along = at + 1 < hops.length ? ease(Math.min(1, Math.max(0, (t - when[at + 1] + .5f) / .5f))) : 0;
        int next = Math.min(hops.length - 1, at + 1);
        float knightX = left + (hops[at][0] + (hops[next][0] - hops[at][0]) * along + .5f) * cell, knightY = top + (hops[at][1] + (hops[next][1] - hops[at][1]) * along + .5f) * cell - (float) Math.sin(along * Math.PI) * cell * .5f;
        float taken = p(3.6f, 4.3f), back = p(8.4f, 9.2f), homeX = left + 3.5f * cell, homeY = top + 4.5f * cell, awayX = L + dp(40), awayY = top + cell * 6 + dp(34);
        float appX = homeX + (awayX - homeX) * ease(taken) * (1 - ease(back)), appY = homeY + (awayY - homeY) * ease(taken) * (1 - ease(back));
        icon(c, 0, appX, appY, cell * .78f * (1 - .3f * taken * (1 - back)), taken * (1 - back) * 360, board);
        words(c, "♞", knightX, knightY, cell * .82f, 0xFF15161A, board * p(.9f, 1.2f) * (1 - p(8, 8.5f)));
        // The pawn, a square at a time, and what it is at the far end.
        float walked = p(5, 7.6f) * 5;
        boolean queen = walked >= 5;
        words(c, queen ? "♛" : "♟", left + 1.5f * cell, top + (5.5f - Math.min(5, (float) Math.floor(walked))) * cell, cell * (queen ? .9f : .7f), Color.WHITE, board * p(4.6f, 5));
        if (queen && t < 8.2f) piece(c, STAR, left + 1.5f * cell, top + cell * .1f - (t - 7.6f) * dp(24), dp(26), t * 200, 1 - p(7.6f, 8.2f));
        if (t < 1.2f) go(clamp(cx + cell * 2.2f, L + wide, R - wide / 2), floor, dp(380));
        else act(in(4, 5) ? PetArt.JUMP : t >= 8.4f ? PetArt.WAVE : PetArt.WORK);
        her(c);
        line(.3f, 1.8f, "Chess. Your apps are the pieces Sloppy~♡");
        line(3.9f, 2, "Knight takes " + label(0) + ". Yoink Sloppy~♡");
        line(6, 1.8f, "A pawn, walking the whole board Sloppy~♡");
        line(7.9f, 2.6f, "Read to the end and you're a queen. Fine. Have it back Sloppy~♡");
    }

    /** The screen as a minefield. She knows where they all are. */
    private void field(Canvas c) {
        int columns = Math.max(5, Math.min(8, (int) ((W - dp(24)) / dp(52)))), rows = 5;
        float cell = (W - dp(24)) / columns, top = clamp(cy - cell * 3, T + dp(110), floor - tall - cell * rows - dp(10)), left = L + dp(12), boom = t - 6.6f, gone = 1 - p(9.4f, 10.4f);
        int first = columns + 1, flagged = columns * 2 + columns - 2, stepped = columns * 3 + columns / 2;
        c.save();
        c.translate(shake(boom, dp(12)), shake(boom, dp(8)));
        for (int square = 0; square < columns * rows; square++) {
            float x = left + (square % columns + .5f) * cell, y = top + (square / columns + .5f) * cell, laid = out(p(.4f + square * .012f, .7f + square * .012f));
            boolean mine = rnd(square, 1) < .22f && square != first || square == flagged || square == stepped;
            // It opens outward from the first square touched, a ring of them at a time.
            int far = Math.max(Math.abs(square % columns - first % columns), Math.abs(square / columns - first / columns));
            boolean open = !mine && t >= 1.6f + far * .55f && far <= 2;
            fill(open ? 0x33FFFFFF : dark ? 0xF23A3F45 : 0xF2C9CFD6, laid * gone);
            box.set(x - cell * .46f, y - cell * .46f, x + cell * .46f, y + cell * .46f);
            c.drawRoundRect(box, dp(5), dp(5), paint);
            if (open) {
                int near = 0;
                for (int other = 0; other < columns * rows; other++) {
                    boolean theirs = rnd(other, 1) < .22f && other != first || other == flagged || other == stepped;
                    if (theirs && other != square && Math.abs(other % columns - square % columns) <= 1 && Math.abs(other / columns - square / columns) <= 1) near++;
                }
                int[] inks = {0, 0xFF2F7DE1, 0xFF2E9E5B, 0xFFFF393F, 0xFF8257D8, 0xFFC0562F};
                if (near > 0) words(c, String.valueOf(near), x, y, cell * .44f, inks[Math.min(5, near)], gone);
            } else if (mine && boom >= 0) {
                fill(0xFF15161A, gone);
                c.drawCircle(x, y, cell * .22f, paint);
                pen(0xFF15161A, dp(2.5f), gone);
                for (int spike = 0; spike < 4; spike++) c.drawLine(x - (float) Math.cos(spike * .785f) * cell * .32f, y - (float) Math.sin(spike * .785f) * cell * .32f, x + (float) Math.cos(spike * .785f) * cell * .32f, y + (float) Math.sin(spike * .785f) * cell * .32f, stroke);
            }
            if (square == flagged && t >= 4.6f) piece(c, FLAG, x, y - cell * .08f, cell * .8f * Math.min(1, (t - 4.6f) / .15f), 0, gone);
        }
        float goX = left + (stepped % columns + .5f) * cell, goY = top + (stepped / columns + .8f) * cell;
        if (t < 5.6f) go(clamp(cx + cell, L + wide, R - wide), floor, dp(380));
        else if (t < 6.6f) {
            act(PetArt.JUMP);
            float over = p(5.7f, 6.6f);
            put(hx + (goX - hx) * Math.min(1, dt * 5), hy + (goY - hy) * Math.min(1, dt * 5));
            lift = (float) Math.sin(over * Math.PI) * tall * .7f;
        } else if (t < 9) wear(LOSE, 6, boom < .5f ? 1 : 3, 1.1f);
        her(c);
        c.restore();
        if (boom >= 0 && boom < .5f) {
            wash(c, 0xFFFFFFFF, (1 - boom / .5f) * .8f);
            fx(c, BURST, goX, goY - tall * .4f, dp(260) * (1 + boom), 0, 1 - boom / .5f);
        }
        line(.3f, 1.8f, "Your screen is a minefield now. Tread softly Sloppy~♡");
        line(2.4f, 2, "The numbers are hints. Hints Sloppy~♡");
        line(4.6f, 1.4f, "Flag that one. Told you so Sloppy~♡");
        line(6.8f, 2.6f, "Boom. ...I knew that was there. I said nothing Sloppy~♡");
    }

    /** Last card: she deals, plays everything she holds, and goes out on an any-sign card. */
    private void deal(Canvas c) {
        float deckX = cx, deckY = clamp(cy, T + dp(260), floor - tall - dp(60)), cardW = dp(44), cardH = dp(62), gone = 1 - p(9.2f, 10.2f);
        String[] played = {"+ 7", "− 7", "− 3", "take 2", "× 3", "take 2", "any"};
        int[] inks = {0xFFE8554B, 0xFF3B7BE0, 0xFF3B7BE0, 0xFF2E9E5B, 0xFF9A4FD0, 0xFF2E9E5B, 0xFFFFC53D};
        float[] when = {2.2f, 2.9f, 3.5f, 4.1f, 4.9f, 5.5f, 7};
        // The user's hand, face down along the top: seven, and two more for each take two.
        int theirs = 7;
        for (int card = 0; card < played.length; card++) if (played[card].startsWith("take") && t >= when[card] + .5f) theirs += 2;
        for (int card = 0; card < theirs; card++) {
            float dealtAt = card < 7 ? .5f + card * .1f : (card < 9 ? when[3] : when[5]) + .2f + (card % 2) * .12f, flown = out(p(dealtAt, dealtAt + .35f));
            if (flown <= 0) continue;
            float x = cx + (card - (theirs - 1) / 2f) * Math.min(cardW * .7f, (W - dp(60)) / theirs), y = T + dp(130);
            c.save();
            c.rotate((card - (theirs - 1) / 2f) * 3, x, y);
            fill(CORAL, gone);
            box.set(deckX + (x - deckX) * flown - cardW / 2, deckY + (y - deckY) * flown - cardH / 2, deckX + (x - deckX) * flown + cardW / 2, deckY + (y - deckY) * flown + cardH / 2);
            c.drawRoundRect(box, dp(6), dp(6), paint);
            pen(Color.WHITE, dp(1.5f), gone);
            c.drawRoundRect(box, dp(6), dp(6), stroke);
            c.restore();
        }
        // Hers go from her to the pile, face up, one on another.
        int left = played.length;
        for (int card = 0; card < played.length; card++) {
            float flown = p(when[card], when[card] + .4f);
            if (flown <= 0) continue;
            left--;
            float x = hx + (deckX - hx) * ease(flown), y = hy - tall * .6f + (deckY - hy + tall * .6f) * ease(flown) - (float) Math.sin(flown * Math.PI) * dp(50), burst = card == played.length - 1 ? Math.max(0, t - 7.6f) : 0;
            c.save();
            c.rotate(-20 + rnd(card, 1) * 40 + flown * 360, x, y);
            fill(0xFFFFFFFF, gone);
            box.set(x - cardW / 2, y - cardH / 2, x + cardW / 2, y + cardH / 2);
            c.drawRoundRect(box, dp(6), dp(6), paint);
            pen(inks[card], dp(2.5f), gone);
            c.drawRoundRect(box, dp(6), dp(6), stroke);
            words(c, played[card], x, y, dp(played[card].length() > 3 ? 11 : 16), inks[card], gone);
            c.restore();
            if (burst > 0 && burst < .5f) fx(c, BURST, deckX, deckY, dp(200) * (1 + burst), 0, 1 - burst * 2);
        }
        plate(c, left == 1 ? "Last card!" : left == 0 ? "Out first" : "Halgakos: " + left + "   ·   You: " + theirs, cx, deckY + cardH * .5f + dp(26), dp(13), left <= 1 ? Color.WHITE : 0xFF292B2A, left <= 1 ? 0xF2E8554B : 0xF2FFFFFF, p(1.4f, 1.8f) * gone);
        if (t < 1.4f) go(clamp(cx + W * .22f, L + wide, R - wide / 2), floor, dp(380));
        else act(t < 7.4f ? PetArt.WORK : t < 8.2f ? PetArt.JUMP : PetArt.WAVE);
        her(c);
        line(.3f, 1.8f, "Last card. Seven each. I'm dealing Sloppy~♡");
        line(4.1f, 1.6f, "Take two. You're welcome Sloppy~♡");
        line(5.9f, 1, "Last card Sloppy~♡");
        line(7, 2.4f, "Any sign. I pick the winning one. Out first Sloppy~♡");
    }
}
