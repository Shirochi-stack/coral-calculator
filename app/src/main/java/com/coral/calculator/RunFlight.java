package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The last third of the fight with Lucy, which is not fought on the ground. Halgakos, in
 * the white that is truly hers, flies into the sky over the Dragon's Library after Lucy,
 * who is a dragon now and keeps ahead of her; it is seen from behind Halgakos. She moves
 * across and up and down, and Lucy does the same, far ahead. What Lucy sends comes from
 * far to near, each thing with a mark where it will arrive: a puff of mist thrown at where
 * Halgakos is; a star thrown beside her; a gust, which is a bar of wind across the whole
 * sky at one height, to be flown over or under; and a pillar, the same from top to bottom.
 *
 * Nothing hurts Lucy now but the beam. The strike button, held, gathers it; it then lasts a
 * while, and must rest before it is gathered again. While she gathers and while it lasts
 * she flies more slowly. It goes straight ahead from where she is: it reaches Lucy only
 * while Lucy is before her, and whatever is coming at her along it is gone. Thirds of what
 * Lucy can take are stages: in each she is quicker and sends more; as one ends she reels,
 * everything in the air is gone, and for a breath nothing comes.
 *
 * A hurt costs hearts, counted in halves, as it does on the ground; when they are gone she
 * has fallen, and the flight is over: whoever keeps the lives decides whether it begins
 * again. There is nothing here but numbers: the screen is someone else's.
 */
final class RunFlight {
    static final int FLYING = 0, WON = 1, FELL = 2;
    static final int PUFF = 0, STAR = 1, GUST = 2, PILLAR = 3;
    /** How fast she flies, and how much of that while the beam is gathered or out; and how far up and down the sky goes, as a share of how far it goes across. */
    static final float SPEED = 1.7f, SLOWED = .55f, HIGH = .8f;
    /** The beam: how long it is gathered, how long it lasts, how long it rests, how often it lands while it is on Lucy, and how near her it must be. */
    static final float CHARGE = .45f, LASTS = 1.3f, WAIT = 1.5f, TICK = .12f, TRUE = .3f, CUTS = .27f;
    /** How many times the beam must land; how long she cannot be hurt again after a hurt; and how long Lucy reels as a stage ends. */
    static final int TAKES = 27;
    static final float SAFE = 1.3f, REELS = 1.8f;
    /** How near a puff or a star must arrive to hurt, and how near the line of a gust or a pillar. */
    static final float NEAR = .27f, BAR = .2f;
    private static final float[] EVERY = {1.55f, 1.25f, 1.05f}, LUCY_SPEED = {.42f, .6f, .8f}, TURNS = {2.4f, 1.8f, 1.3f};

    /** Something on its way to her: what it is, where it will arrive, how far off it still is, from one to nothing, and how fast it comes. */
    static final class Thing {
        int kind;
        float x, y, far = 1, speed;
        boolean gone;
    }

    private final Random random;
    private final List<Thing> things = new ArrayList<>();
    private final int most, costs;
    private int state = FLYING, halves, left = TAKES, stage, turn;
    private int beams, hits, hurts, wiped, thrown, stages;
    private float x, y = .25f, lucyX, lucyY = -.2f, goalX, goalY, sinceGoal = 9;
    private float safe, held, beam, wait, tick, reel = 1.2f, next = 1, time, sinceThrow = 9, sinceStruck = 9, sinceHurt = 9;

    /** A flight for someone with so many halves of hearts, whom a hurt costs so many of them. */
    RunFlight(int halves, int costs, long seed) {
        this.halves = Math.max(1, halves);
        this.most = this.halves;
        this.costs = Math.max(1, costs);
        random = new Random(seed);
    }

    int state() { return state; }
    float x() { return x; }
    float y() { return y; }
    float lucyX() { return lucyX; }
    float lucyY() { return lucyY; }
    List<Thing> things() { return things; }
    int halves() { return halves; }
    int most() { return most; }
    /** How much Lucy can still take, from one to nothing; and which third of it she is in. */
    float left() { return left / (float) TAKES; }
    int stage() { return stage; }
    /** How far the beam is gathered, from nothing to one; how much longer it lasts; and how far through its rest it is, from nothing to one. */
    float charge() { return beam > 0 ? 0 : Math.min(1, held / CHARGE); }
    float beam() { return beam; }
    float rested() { return wait <= 0 ? 1 : 1 - wait / WAIT; }
    /** Whether the beam is on Lucy just now. */
    boolean onHer() { return beam > 0 && reel <= 0 && Math.abs(x - lucyX) < TRUE && Math.abs(y - lucyY) < TRUE; }
    boolean safe() { return safe > 0; }
    /** How much longer Lucy reels; and how long ago she last sent something, was last reached by the beam, and Halgakos was last hurt. */
    float reel() { return reel; }
    float sinceThrow() { return sinceThrow; }
    float sinceStruck() { return sinceStruck; }
    float sinceHurt() { return sinceHurt; }
    float time() { return time; }
    /** Counts that only go up: beams let go, times the beam has landed, hurts, things the beam has cut out of the air, things sent, and stages ended. */
    int beams() { return beams; }
    int hits() { return hits; }
    int hurts() { return hurts; }
    int wiped() { return wiped; }
    int thrown() { return thrown; }
    int stages() { return stages; }

    /** Moves the flight on by a length of time, with the arrows and the strike button held or not. */
    void step(float seconds, boolean left, boolean right, boolean up, boolean down, boolean strike) {
        seconds = Math.min(seconds, .1f);
        while (seconds > 0 && state == FLYING) {
            float slice = Math.min(1 / 60f, seconds);
            once(slice, left, right, up, down, strike);
            seconds -= slice;
        }
    }

    private void once(float seconds, boolean goLeft, boolean goRight, boolean goUp, boolean goDown, boolean strike) {
        time += seconds;
        sinceThrow += seconds;
        sinceStruck += seconds;
        sinceHurt += seconds;
        if (safe > 0) safe -= seconds;
        shine(seconds, strike);
        if (state != FLYING) return;
        float pace = SPEED * (held > 0 || beam > 0 ? SLOWED : 1);
        x = Math.max(-1, Math.min(1, x + ((goRight ? 1 : 0) - (goLeft ? 1 : 0)) * pace * seconds));
        y = Math.max(-HIGH, Math.min(HIGH, y + ((goDown ? 1 : 0) - (goUp ? 1 : 0)) * pace * seconds));
        // Lucy: reeling, she hangs where she is; if not, she makes for somewhere else in the sky every so often, and sends what she sends.
        if (reel > 0) {
            reel -= seconds;
        } else {
            if ((sinceGoal += seconds) > TURNS[stage]) {
                sinceGoal = 0;
                goalX = (random.nextFloat() * 2 - 1) * .75f;
                goalY = (random.nextFloat() * 2 - 1) * .55f;
            }
            float across = goalX - lucyX, down = goalY - lucyY, length = (float) Math.hypot(across, down);
            if (length > .01f) {
                float go = Math.min(length, LUCY_SPEED[stage] * seconds);
                lucyX += across / length * go;
                lucyY += down / length * go;
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
            if ((thing.far -= thing.speed * seconds) > 0) continue;
            things.remove(index);
            boolean lands = thing.kind == GUST ? Math.abs(y - thing.y) < BAR : thing.kind == PILLAR ? Math.abs(x - thing.x) < BAR
                    : Math.hypot(x - thing.x, y - thing.y) < NEAR;
            if (lands) hurt();
            if (state != FLYING) return;
        }
    }

    /** The beam: gathered while the button is held, then out for a while, then resting. Out, it lands on Lucy while she is before it, and cuts what is coming along it out of the air. */
    private void shine(float seconds, boolean strike) {
        if (wait > 0) wait -= seconds;
        if (beam > 0) {
            beam -= seconds;
            for (Thing thing : things) {
                if (thing.gone) continue;
                boolean cut = thing.kind == GUST ? Math.abs(thing.y - y) < CUTS : thing.kind == PILLAR ? Math.abs(thing.x - x) < CUTS
                        : Math.abs(thing.x - x) < CUTS && Math.abs(thing.y - y) < CUTS;
                if (cut) {
                    thing.gone = true;
                    wiped++;
                }
            }
            if ((tick -= seconds) <= 0) {
                tick = TICK;
                if (onHer()) {
                    hits++;
                    sinceStruck = 0;
                    if (--left <= 0) {
                        left = 0;
                        state = WON;
                        things.clear();
                        return;
                    }
                    int now = Math.min(2, (TAKES - left) * 3 / TAKES);
                    if (now > stage) {
                        // A third of it gone: she reels, and whatever she had in the air is gone with a breath to follow.
                        stage = now;
                        stages++;
                        reel = REELS;
                        next = .6f;
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

    /** Lucy sends something: more kinds of thing, and more at once, in each later stage. */
    private void send() {
        turn++;
        sinceThrow = 0;
        if (stage >= 1 && turn % 3 == 0) {
            add(GUST, 0, y, .55f);
        } else if (stage >= 2 && turn % 3 == 1) {
            add(PILLAR, x, 0, .55f);
        } else {
            add(PUFF, x, y, .75f);
            // And one beside where she is, on one side or the other, so that the way out is not always the same.
            boolean across = random.nextBoolean();
            float side = random.nextBoolean() ? .5f : -.5f;
            if (stage >= 1 || turn % 2 == 0) add(STAR, clamp(x + (across ? side : 0), 1), clamp(y + (across ? 0 : side), HIGH), .68f);
            if (stage >= 2) add(STAR, clamp(x - (across ? side : 0), 1), clamp(y - (across ? 0 : side), HIGH), .62f);
        }
    }

    private static float clamp(float value, float most) { return Math.max(-most, Math.min(most, value)); }

    private void add(int kind, float atX, float atY, float speed) {
        Thing thing = new Thing();
        thing.kind = kind;
        thing.x = atX;
        thing.y = atY;
        thing.speed = speed;
        things.add(thing);
        thrown++;
    }

    private void hurt() {
        if (safe > 0) return;
        hurts++;
        sinceHurt = 0;
        safe = SAFE;
        // What she was gathering is lost with it.
        held = 0;
        if ((halves -= costs) <= 0) {
            halves = 0;
            state = FELL;
        }
    }
}
