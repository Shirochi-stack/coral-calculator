package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * What comes after the flight, for whoever won it: the Mother Tongue, who decided that every
 * tongue should sound like her own. Halgakos rides Lucy, who is a dragon, into a sky of
 * pages after her; it is seen from behind them. They move across and up and down, and the
 * Mother does the same, far ahead and far larger.
 *
 * Two things are theirs to send. Halgakos's beam, as in the flight: the strike button, held,
 * gathers it; it lasts a while and then rests; it goes straight ahead. And Lucy's breath: a
 * puff of mist that takes a moment to cross the sky, as often as she has the wind for it.
 * The Mother keeps a shield of gilt about her, in pieces. Breath that reaches her takes a
 * piece off it; so, every so often, does one of the friends who pledged. With the last
 * piece gone she is open for a while, and only then does the beam read her; then her
 * shield forms again. Thirds of what she can take are stages: in each she has a piece more
 * and sends more. The very last of it is not taken by the beam alone: she stays open, and
 * it goes when Lucy's breath reaches her while the beam is on her, both together.
 *
 * What the Mother sends are her corrections, made solid, each with a mark where it will
 * arrive: a row of blocks, "Mr." and "Miss", with one way through it; a press, which closes
 * from above and below onto one height and flattens whatever is not at that height; a
 * name she has for someone, which follows whoever it is meant for until it is nearly there,
 * and which the beam and the breath cut out of the air; and, from the second stage, her own
 * pass, down one side of the sky, in which nothing touches her. Blocks, the press and her
 * pass are not cut by anything: they are flown round.
 *
 * A hurt costs hearts, in halves; when they are gone they have fallen, and whoever keeps
 * the lives decides whether it begins again. There is nothing here but numbers.
 */
final class RunRide {
    static final int FLYING = 0, WON = 1, FELL = 2;
    static final int BLOCKS = 0, PRESS = 1, NAME = 2, PASS = 3;
    static final float SPEED = 1.6f, SLOWED = .6f, HIGH = .8f;
    /** The beam, as in the flight; she is larger than Lucy, and so easier to keep it on. */
    static final float CHARGE = .45f, LASTS = 1.3f, WAIT = 1.5f, TICK = .12f, TRUE = .36f, CUTS = .27f;
    /** Lucy's breath: how often, how long it takes to cross, and how near the Mother it must arrive. */
    static final float BREATH_EVERY = 1, BREATH_FLIES = .55f, BREATH_TRUE = .38f;
    /** How many pieces her shield has in each stage, how long she is open once it is gone, how long it takes to form, and how often a friend takes a piece. */
    static final int[] SHIELD = {4, 5, 6};
    static final float OPEN = 4.5f, FORMS = 1.2f, HELP_EVERY = 9;
    /** How many times the beam must read her; and how long she reels as a stage ends. */
    static final int TAKES = 45;
    static final float SAFE = 1.3f, REELS = 2;
    /** How wide the way through a row of blocks is, from its middle; how tall the slit of a press; how near a name must arrive; and how wide her pass is. */
    static final float GAP = .3f, SLIT = .25f, NEAR = .26f, PASS_WIDE = .42f;
    /** How fast a name follows, and how near it is when it no longer does. */
    static final float FOLLOWS = .5f, LOCKS = .3f;
    private static final float[] EVERY = {2.1f, 1.75f, 1.5f}, HER_SPEED = {.28f, .38f, .48f}, TURNS = {2.8f, 2.3f, 1.9f};
    private static final int[][] SENDS = {{BLOCKS, NAME, PRESS, NAME}, {BLOCKS, NAME, PRESS, PASS, NAME, BLOCKS}, {PRESS, NAME, BLOCKS, PASS, NAME, PRESS, BLOCKS, NAME}};
    private static final float[] LANES = {-.62f, 0, .62f};

    /** Something on its way: what it is; where it will arrive (for blocks, the way through; for a press, its slit; for her pass, its middle); how far off it is, from one to nothing; how fast; and which word is on it. */
    static final class Thing {
        int kind, word;
        float x, y, far = 1, speed;
        boolean gone;
    }

    /** A breath on its way to her: where it was sent from, and how far it has got, from nothing to one. */
    static final class Breath {
        float x, y, gone;
    }

    private final Random random;
    private final List<Thing> things = new ArrayList<>();
    private final List<Breath> breaths = new ArrayList<>();
    private final int most, costs;
    private int state = FLYING, halves, left = TAKES, stage, turn, shield = SHIELD[0];
    private int beams, hits, hurts, cut, sent, stages, puffs, chips, opens, helps, names;
    private float x, y = .3f, herX, herY = -.1f, goalX, goalY, sinceGoal = 9;
    private float safe, held, beam, wait, tick, wind, reel = 2.5f, next = 1.5f, open, forming, passing, help = HELP_EVERY, time;
    private float sinceSent = 9, sinceRead = 9, sinceHurt = 9, sinceChip = 9, sinceHelp = 9;

    RunRide(int halves, int costs, long seed) {
        this.halves = Math.max(1, halves);
        this.most = this.halves;
        this.costs = Math.max(1, costs);
        random = new Random(seed);
    }

    int state() { return state; }
    float x() { return x; }
    float y() { return y; }
    float herX() { return herX; }
    float herY() { return herY; }
    List<Thing> things() { return things; }
    List<Breath> breaths() { return breaths; }
    int halves() { return halves; }
    int most() { return most; }
    float left() { return left / (float) TAKES; }
    int stage() { return stage; }
    /** Her shield: how many pieces are left of how many; how much longer she is open; whether it is forming; and whether she is making her pass, when nothing touches her. */
    int shield() { return shield; }
    int shieldMost() { return SHIELD[stage]; }
    float open() { return open; }
    boolean forming() { return forming > 0; }
    boolean passing() { return passing > 0; }
    /** Whether only the last of it is left: the beam and the breath together. */
    boolean finishing() { return left == 1 && open > 0; }
    float charge() { return beam > 0 ? 0 : Math.min(1, held / CHARGE); }
    float beam() { return beam; }
    float rested() { return wait <= 0 ? 1 : 1 - wait / WAIT; }
    /** How far Lucy has her wind back, from nothing to one. */
    float wind() { return wind <= 0 ? 1 : 1 - wind / BREATH_EVERY; }
    /** Whether the beam is on her just now; and whether it reads her, which it does only while she is open. */
    boolean onHer() { return beam > 0 && reel <= 0 && passing <= 0 && Math.abs(x - herX) < TRUE && Math.abs(y - herY) < TRUE; }
    boolean reads() { return onHer() && open > 0; }
    boolean safe() { return safe > 0; }
    float reel() { return reel; }
    float sinceSent() { return sinceSent; }
    float sinceRead() { return sinceRead; }
    float sinceHurt() { return sinceHurt; }
    float sinceChip() { return sinceChip; }
    float sinceHelp() { return sinceHelp; }
    float time() { return time; }
    /** Counts that only go up: beams, times the beam has read her, hurts, names cut out of the air, things sent, stages ended, breaths, pieces off her shield, times it has been broken, and pieces a friend took. */
    int beams() { return beams; }
    int hits() { return hits; }
    int hurts() { return hurts; }
    int cut() { return cut; }
    int sent() { return sent; }
    int stages() { return stages; }
    int puffs() { return puffs; }
    int chips() { return chips; }
    int opens() { return opens; }
    int helps() { return helps; }

    /** Moves it on by a length of time, with the arrows, the strike button and the breath button held or not. */
    void step(float seconds, boolean left, boolean right, boolean up, boolean down, boolean strike, boolean breath) {
        seconds = Math.min(seconds, .1f);
        while (seconds > 0 && state == FLYING) {
            float slice = Math.min(1 / 60f, seconds);
            once(slice, left, right, up, down, strike, breath);
            seconds -= slice;
        }
    }

    private void once(float seconds, boolean goLeft, boolean goRight, boolean goUp, boolean goDown, boolean strike, boolean breath) {
        time += seconds;
        sinceSent += seconds;
        sinceRead += seconds;
        sinceHurt += seconds;
        sinceChip += seconds;
        sinceHelp += seconds;
        if (safe > 0) safe -= seconds;
        shine(seconds, strike);
        if (state != FLYING) return;
        float pace = SPEED * (held > 0 || beam > 0 ? SLOWED : 1);
        x = Math.max(-1, Math.min(1, x + ((goRight ? 1 : 0) - (goLeft ? 1 : 0)) * pace * seconds));
        y = Math.max(-HIGH, Math.min(HIGH, y + ((goDown ? 1 : 0) - (goUp ? 1 : 0)) * pace * seconds));
        // Lucy's breath: sent when she has the wind for it, from where they are, straight ahead.
        if (wind > 0) wind -= seconds;
        if (breath && wind <= 0) {
            wind = BREATH_EVERY;
            Breath puff = new Breath();
            puff.x = x;
            puff.y = y;
            breaths.add(puff);
            puffs++;
        }
        for (int index = breaths.size() - 1; index >= 0; index--) {
            Breath puff = breaths.get(index);
            puff.gone += seconds / BREATH_FLIES;
            // What it passes on its way, of the names she sends, is gone.
            for (Thing thing : things) {
                if (thing.gone || thing.kind != NAME || Math.abs(thing.far - puff.gone) > .12f) continue;
                if (Math.abs(thing.x - puff.x) < CUTS && Math.abs(thing.y - puff.y) < CUTS) {
                    thing.gone = true;
                    cut++;
                }
            }
            if (puff.gone < 1) continue;
            breaths.remove(index);
            if (reel > 0 || passing > 0 || Math.abs(puff.x - herX) > BREATH_TRUE || Math.abs(puff.y - herY) > BREATH_TRUE) continue;
            if (shield > 0 && forming <= 0) {
                chip();
            } else if (finishing() && onHer()) {
                // The last of it: the beam on her and the breath reaching her, together.
                left = 0;
                state = WON;
                things.clear();
                breaths.clear();
                return;
            }
        }
        // Her friends: every so often one of them takes a piece off her shield.
        if (shield > 0 && forming <= 0 && reel <= 0 && (help -= seconds) <= 0) {
            help = HELP_EVERY;
            helps++;
            sinceHelp = 0;
            chip();
        }
        // The Mother: reeling, she hangs; making her pass, she is not where she was; open, she waits to be read; and otherwise she drifts, and sends.
        if (passing > 0) passing -= seconds;
        if (forming > 0 && (forming -= seconds) <= 0) shield = SHIELD[stage];
        if (open > 0 && left > 1 && (open -= seconds) <= 0) forming = FORMS;
        if (reel > 0) {
            reel -= seconds;
        } else {
            if ((sinceGoal += seconds) > TURNS[stage]) {
                sinceGoal = 0;
                goalX = (random.nextFloat() * 2 - 1) * .7f;
                goalY = (random.nextFloat() * 2 - 1) * .5f;
            }
            float across = goalX - herX, down = goalY - herY, length = (float) Math.hypot(across, down);
            if (length > .01f) {
                float go = Math.min(length, HER_SPEED[stage] * (open > 0 ? .5f : 1) * seconds);
                herX += across / length * go;
                herY += down / length * go;
            }
            if ((next -= seconds) <= 0) {
                next = EVERY[stage];
                send();
            }
        }
        for (int index = things.size() - 1; index >= 0; index--) {
            Thing thing = things.get(index);
            if (thing.gone) {
                things.remove(index);
                continue;
            }
            // A name follows whoever it is for, until it is nearly there.
            if (thing.kind == NAME && thing.far > LOCKS) {
                float across = x - thing.x, down = y - thing.y, length = (float) Math.hypot(across, down);
                if (length > .01f) {
                    float go = Math.min(length, FOLLOWS * seconds);
                    thing.x += across / length * go;
                    thing.y += down / length * go;
                }
            }
            if ((thing.far -= thing.speed * seconds) > 0) continue;
            things.remove(index);
            if (lands(thing, x, y)) hurt();
            if (state != FLYING) return;
        }
    }

    /** Whether something, arriving, would land on whoever is at a place. */
    static boolean lands(Thing thing, float atX, float atY) {
        return thing.kind == BLOCKS ? Math.abs(atX - thing.x) >= GAP : thing.kind == PRESS ? Math.abs(atY - thing.y) >= SLIT
                : thing.kind == PASS ? Math.abs(atX - thing.x) < PASS_WIDE : Math.hypot(atX - thing.x, atY - thing.y) < NEAR;
    }

    private void chip() {
        shield--;
        chips++;
        sinceChip = 0;
        if (shield > 0) return;
        shield = 0;
        open = OPEN;
        opens++;
    }

    private void shine(float seconds, boolean strike) {
        if (wait > 0) wait -= seconds;
        if (beam > 0) {
            beam -= seconds;
            for (Thing thing : things) {
                if (thing.gone || thing.kind != NAME || Math.abs(thing.x - x) >= CUTS || Math.abs(thing.y - y) >= CUTS) continue;
                thing.gone = true;
                cut++;
            }
            if ((tick -= seconds) <= 0) {
                tick = TICK;
                // It reads her only while she is open; and the last of it it does not take alone.
                if (reads() && left > 1) {
                    left--;
                    hits++;
                    sinceRead = 0;
                    int now = Math.min(2, (TAKES - left) * 3 / TAKES);
                    if (now > stage) {
                        stage = now;
                        stages++;
                        reel = REELS;
                        next = 1;
                        open = 0;
                        forming = 0;
                        passing = 0;
                        shield = SHIELD[stage];
                        help = HELP_EVERY;
                        things.clear();
                    }
                }
            }
            if (beam <= 0) wait = WAIT;
            return;
        }
        if (strike && wait <= 0) {
            if ((held += seconds) >= CHARGE) {
                beam = LASTS;
                held = 0;
                tick = 0;
                beams++;
            }
        } else {
            held = 0;
        }
    }

    /** The Mother sends the next of what she sends in this stage. */
    private void send() {
        int[] sends = SENDS[stage];
        int kind = sends[turn++ % sends.length];
        // (Open, she does not make her pass: she is where she is, to be read.)
        if (kind == PASS && open > 0) kind = NAME;
        sinceSent = 0;
        Thing thing = new Thing();
        thing.kind = kind;
        thing.word = names++;
        if (kind == BLOCKS) {
            // The way through is never the lane they are in already, when there is another to make for in time.
            int lane = random.nextInt(LANES.length);
            thing.x = LANES[lane];
            thing.speed = .48f;
        } else if (kind == PRESS) {
            thing.y = (random.nextFloat() * 2 - 1) * (HIGH - SLIT);
            thing.speed = .48f;
        } else if (kind == PASS) {
            thing.x = x < 0 ? -.58f : .58f;
            thing.speed = .4f;
            passing = 1 / thing.speed + .4f;
        } else {
            thing.x = herX;
            thing.y = herY;
            thing.speed = .4f;
        }
        things.add(thing);
        sent++;
    }

    private void hurt() {
        if (safe > 0) return;
        hurts++;
        sinceHurt = 0;
        safe = SAFE;
        held = 0;
        if ((halves -= costs) <= 0) {
            halves = 0;
            state = FELL;
        }
    }
}
