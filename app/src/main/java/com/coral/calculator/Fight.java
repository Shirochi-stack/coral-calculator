package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Page brawl: a fight between two of the five of {@link FightCast}, the best of three rounds.
 * Each has a quick move, a heavy one, a special that must rest between uses, and an ultimate
 * for when her meter is full; and each fights her own way. Halgakos has her ring of keys up
 * close and Glossarion's scan from the middle. Chikuwa is slow and sturdy, with brick bread
 * in her hands and bread rolls to throw. Tsubakieh has her bat, which comes back when thrown.
 * Rune keeps away and throws ink tears, and every third one is a paper knife. Illya has
 * nothing but what she throws: hearts, magic, and a push to get them off her.
 *
 * Everything is measured in the stage's own lengths and in seconds. Across is from the
 * left; height is up from the floor. This class knows nothing of screens: it is moved on by
 * {@link #step}, with what the player holds down, and the other side plays itself.
 */
final class Fight {
    static final int HALGAKOS = 0, CHIKUWA = 1, TSUBAKIEH = 2, RUNE = 3, ILLYA = 4, FIGHTERS = 5;
    /** What a fighter is doing. */
    static final int IDLE = 0, WALK = 1, JUMP = 2, BLOCK = 3, ATTACK = 4, HIT = 5, DOWN = 6, RISE = 7, WON = 8, OUT = 9;
    static final int LIGHT = 0, HEAVY = 1, SPECIAL = 2, ULTIMATE = 3;
    /** What may fly: her scan, a bread roll, the bat, an ink tear, the paper knife, a heart, a ball of magic, and a flood of tears. */
    static final int NOTHING = -1, PANEL = 0, BREAD = 1, BAT = 2, TEAR = 3, KNIFE = 4, HEART = 5, ORB = 6, FLOOD = 7;
    /** Where the fight has got to: on, held for the flourish of an ultimate, between rounds, or over. */
    static final int FIGHTING = 0, FLOURISH = 1, ROUND_OVER = 2, MATCH_OVER = 3;

    static final float WIDE = 14, BODY = .9f, TALL = 2.4f, METER = 100;
    static final int ROUNDS = 2;
    static final float ROUND_SECONDS = 60, FLOURISH_SECONDS = 1.1f, BETWEEN = 2.6f;
    /** The hardest the other side plays, from 0. */
    static final int LEVELS = 4;

    private static final float STEP = 1 / 60f, GRAVITY = 30, LEAP = 11.5f, EDGE = 1.1f;
    private static final float[] HEALTH = {100, 108, 100, 98, 90};
    private static final float[] SPEED = {4.6f, 4.1f, 5.2f, 4.4f, 4.2f};
    /** How far off each likes to stand, and how near the other must be for her ultimate to be worth using. */
    private static final float[] WANTS = {3.2f, 1.2f, 1.5f, 6.5f, 7};
    private static final float[] ULTIMATE_REACH = {8, 2.8f, 6, 9, 9};

    /** One of a fighter's moves, as numbers. */
    static final class Move {
        /** How long it takes to come out, how long it can land for, and how long she is open after. */
        final float startup, active, recovery;
        /** How far in front of her it reaches, what it takes off, how hard it pushes and how long it stuns. */
        final float reach, damage, knock, stun;
        final boolean floors;
        /** What it throws, if anything, and how long before it may be used again. */
        final int shot;
        final float rest;
        /** For an ultimate: how many times it lands and how far apart, how fast she rushes in first, and whether it reaches the air. */
        int times = 1;
        float every, rush;
        boolean anywhere;

        Move(float startup, float active, float recovery, float reach, float damage, float knock, float stun, boolean floors, int shot,
                float rest) {
            this.startup = startup;
            this.active = active;
            this.recovery = recovery;
            this.reach = reach;
            this.damage = damage;
            this.knock = knock;
            this.stun = stun;
            this.floors = floors;
            this.shot = shot;
            this.rest = rest;
        }

        Move often(int times, float every, float rush, boolean anywhere) {
            this.times = times;
            this.every = every;
            this.rush = rush;
            this.anywhere = anywhere;
            return this;
        }

        float length() { return startup + Math.max(active, every * (times - 1) + .1f) + recovery; }
    }

    private static Move strike(float startup, float active, float recovery, float reach, float damage, float knock, float stun, boolean floors) {
        return new Move(startup, active, recovery, reach, damage, knock, stun, floors, NOTHING, 0);
    }

    private static Move thrown(float startup, float recovery, int shot, float rest) {
        return new Move(startup, .05f, recovery, 0, 0, 0, 0, false, shot, rest);
    }

    /** Every fighter's four moves: quick, heavy, special, ultimate. */
    static final Move[][] MOVES = {
            // Halgakos: the key ring, a key thrust, Glossarion's scan, and a translation in three chunks that reaches the whole stage.
            {strike(.09f, .1f, .16f, 1.3f, 7, 3, .22f, false), strike(.2f, .12f, .3f, 2.1f, 12, 6, .34f, false),
                    thrown(.18f, .28f, PANEL, .9f), strike(.3f, .1f, .5f, 9, 10, 4, .3f, false).often(3, .3f, 0, true)},
            // Chikuwa: a baguette, the brick bread, a bread roll, and the brick bread brought down from overhead.
            {strike(.1f, .1f, .18f, 1.5f, 7, 3, .24f, false), strike(.26f, .14f, .36f, 1.7f, 16, 9, .4f, true),
                    thrown(.2f, .28f, BREAD, 1), strike(.5f, .16f, .6f, 3, 34, 12, .5f, true).often(1, 0, 3, false)},
            // Tsubakieh: a poke of the bat, a full swing, the bat thrown to come back, and a home run after a rush.
            {strike(.09f, .1f, .17f, 1.5f, 6, 3, .2f, false), strike(.26f, .12f, .34f, 1.9f, 13, 8, .34f, false),
                    thrown(.2f, .3f, BAT, 2.8f), strike(.36f, .16f, .6f, 2.2f, 32, 16, .5f, true).often(1, 0, 14, false)},
            // Rune: a flick of ink, the paper knife, a tear thrown (every third a knife), and a flood.
            {strike(.1f, .1f, .2f, 1.4f, 5, 7, .22f, false), strike(.2f, .12f, .3f, 1.6f, 10, 5, .32f, false),
                    thrown(.16f, .26f, TEAR, .7f), thrown(.4f, .6f, FLOOD, 0)},
            // Illya: a heart that pushes them off, a ball of magic, a heart beam, and a beam the whole stage long.
            {strike(.1f, .1f, .22f, 1.2f, 4, 11, .2f, false), thrown(.26f, .34f, ORB, 1.1f),
                    thrown(.16f, .26f, HEART, 1), strike(.34f, .1f, .5f, 9, 8, 3, .28f, false).often(4, .24f, 0, true)},
    };

    /** What is held down: the arrows, the jump, the guard, and the three that hit. */
    static final class Pad {
        boolean left, right, jump, block, light, heavy, special;

        void clear() {
            left = false;
            right = false;
            jump = false;
            block = false;
            light = false;
            heavy = false;
            special = false;
        }
    }

    /** One of the two who fight. */
    static final class Fighter {
        final int kind, side;
        float x, y, across, up;
        boolean facingLeft;
        int state = IDLE, move = -1;
        /** How long she has been doing what she is doing, and how many times this move has landed or been thrown. */
        float clock;
        int landed;
        float health, meter;
        /** How long until each move may be used again, until she can be hit again, how long she is stunned, and how long she has lain. */
        final float[] rest = new float[4];
        float safe, stunned, lain;
        /** How many specials she has used, rounds won, and whether her bat is out of her hands. */
        int specials, wins;
        boolean unarmed;
        private boolean light, heavy, special, jump;

        Fighter(int kind, int side) {
            this.kind = kind;
            this.side = side;
        }

        Move doing() { return move < 0 ? null : MOVES[kind][move]; }

        /** Whether her ultimate is ready. */
        boolean charged() { return meter >= METER; }
    }

    /** Something thrown, flying across the stage. */
    static final class Shot {
        final int kind, side;
        float x, y, across, up, left, age;
        /** How many more times it may land, and how long before it may land again. */
        int lands = 1;
        float wait;

        Shot(int kind, int side) {
            this.kind = kind;
            this.side = side;
        }
    }

    final Fighter[] fighters = new Fighter[2];
    private final List<Shot> shots = new ArrayList<>();
    private final int[] level = new int[2];
    private final Pad[] minds = {new Pad(), new Pad()};
    private final float[] thinking = new float[2];
    private final Random random;
    /** How far apart the two may get: the screen they are shown on is only so wide. */
    private float span = 9;
    private int phase = FIGHTING, round = 1, flourisher = -1, lastWinner = -1;
    private float phaseClock, roundClock = ROUND_SECONDS, time;
    /** Counts that only go up, for whoever shows the fight to tell what has just happened; and where the last blow landed. */
    private int hits, blocks, throwsMade, falls, moves;
    private float hitX, hitY;
    private boolean hitHard;
    private int hitSide;

    /**
     * @param leftLevel how well the left side plays itself, from 0 to {@link #LEVELS} - 1, or below zero if it is played by hand
     */
    Fight(int left, int right, int leftLevel, int rightLevel, long seed) {
        fighters[0] = new Fighter(left, 0);
        fighters[1] = new Fighter(right, 1);
        level[0] = leftLevel;
        level[1] = rightLevel;
        random = new Random(seed);
        ready();
    }

    private void ready() {
        for (Fighter fighter : fighters) {
            fighter.x = WIDE / 2 + (fighter.side == 0 ? -2.2f : 2.2f);
            fighter.y = 0;
            fighter.across = 0;
            fighter.up = 0;
            fighter.facingLeft = fighter.side == 1;
            fighter.state = IDLE;
            fighter.move = -1;
            fighter.clock = 0;
            fighter.health = HEALTH[fighter.kind];
            fighter.safe = 0;
            fighter.stunned = 0;
            fighter.unarmed = false;
            java.util.Arrays.fill(fighter.rest, 0);
        }
        shots.clear();
        roundClock = ROUND_SECONDS;
        phase = FIGHTING;
        phaseClock = 0;
    }

    // What there is to see

    List<Shot> shots() { return shots; }
    int phase() { return phase; }
    int round() { return round; }
    /** How much of the round is left, in seconds. */
    float left() { return roundClock; }
    float time() { return time; }
    /** Whose ultimate the fight is held for, and how far through that hold it is, from 0 to 1. */
    int flourisher() { return flourisher; }
    float flourish() { return phase == FLOURISH ? phaseClock / FLOURISH_SECONDS : 0; }
    /** Who took the last round: a side, or below zero if nobody did. */
    int lastWinner() { return lastWinner; }
    /** Who has won the fight, once it is over. */
    int winner() { return phase != MATCH_OVER ? -1 : fighters[0].wins > fighters[1].wins ? 0 : 1; }
    static float fullHealth(int kind) { return HEALTH[kind]; }
    /** Blows landed, blows guarded, things thrown, knock-downs and moves begun, since the fight began. */
    int hits() { return hits; }
    int blocks() { return blocks; }
    int throwsMade() { return throwsMade; }
    int falls() { return falls; }
    int moves() { return moves; }
    /** Where the last blow landed, whether it was a heavy one, and on which side's fighter. */
    float hitX() { return hitX; }
    float hitY() { return hitY; }
    boolean hitHard() { return hitHard; }
    int hitSide() { return hitSide; }

    void setSpan(float span) { this.span = Math.max(5, Math.min(WIDE - 2 * EDGE, span)); }

    // Moving it on

    /** Moves the fight on by a length of time, with what the player holds down. */
    void step(float seconds, Pad pad) { step(seconds, pad, null); }

    /** The same, with what is held down for the other side too, should that side be played by hand as well. */
    void step(float seconds, Pad pad, Pad other) {
        seconds = Math.min(seconds, .1f);
        int began = round;
        // It stops where a new round begins, so whoever shows the fight can announce the round before anything in it happens.
        while (seconds > 0 && phase != MATCH_OVER && round == began) {
            float slice = Math.min(STEP, seconds);
            once(slice, pad, other);
            seconds -= slice;
        }
    }

    private void once(float seconds, Pad pad, Pad other) {
        time += seconds;
        if (phase == FLOURISH) {
            // The fight holds its breath while an ultimate is announced.
            if ((phaseClock += seconds) >= FLOURISH_SECONDS) phase = FIGHTING;
            return;
        }
        if (phase == ROUND_OVER) {
            phaseClock += seconds;
            for (Fighter fighter : fighters) settle(fighter, seconds);
            apart();
            if (phaseClock >= BETWEEN) {
                if (fighters[0].wins >= ROUNDS || fighters[1].wins >= ROUNDS) {
                    phase = MATCH_OVER;
                } else {
                    round++;
                    ready();
                }
            }
            return;
        }
        roundClock -= seconds;
        for (int side = 0; side < 2; side++) {
            Fighter me = fighters[side], foe = fighters[1 - side];
            Pad held = level[side] < 0 ? (side == 0 ? pad : other) : think(me, foe, seconds);
            act(me, foe, held == null ? minds[side] : held, seconds);
            if (phase != FIGHTING) {
                // An ultimate has just been begun: the fight stops here, with the two where they should be.
                apart();
                return;
            }
        }
        apart();
        fly(seconds);
        judge();
    }

    /** One fighter's moment: what she is in the middle of goes on, or she does what is asked of her. */
    private void act(Fighter me, Fighter foe, Pad pad, float seconds) {
        for (int index = 0; index < me.rest.length; index++) me.rest[index] = Math.max(0, me.rest[index] - seconds);
        me.safe = Math.max(0, me.safe - seconds);
        me.clock += seconds;
        // A press counts once: holding a button down does not repeat it.
        boolean light = pad.light && !me.light, heavy = pad.heavy && !me.heavy, special = pad.special && !me.special;
        boolean jump = pad.jump && !me.jump;
        me.light = pad.light;
        me.heavy = pad.heavy;
        me.special = pad.special;
        me.jump = pad.jump;
        if (me.state == HIT || me.state == DOWN || me.state == RISE || me.state == OUT || me.state == WON) {
            settle(me, seconds);
            return;
        }
        if (me.state == ATTACK) {
            attack(me, foe, seconds);
            return;
        }
        if (me.state == JUMP) {
            me.x += me.across * seconds;
            me.y += me.up * seconds;
            me.up -= GRAVITY * seconds;
            if (me.y <= 0) {
                me.y = 0;
                me.up = 0;
                me.across = 0;
                me.state = IDLE;
                me.clock = 0;
            }
            return;
        }
        me.facingLeft = foe.x < me.x;
        if (me.state == BLOCK) {
            // A blow taken on the guard pushes her back a little.
            me.x += me.across * seconds;
            me.across *= Math.max(0, 1 - 9 * seconds);
        }
        if (special && me.charged()) {
            begin(me, ULTIMATE);
        } else if (special && me.rest[SPECIAL] <= 0) {
            begin(me, SPECIAL);
        } else if (heavy && me.rest[HEAVY] <= 0) {
            begin(me, HEAVY);
        } else if (light) {
            begin(me, LIGHT);
        } else if (jump) {
            me.state = JUMP;
            me.clock = 0;
            me.up = LEAP;
            me.across = pad.left == pad.right ? 0 : (pad.left ? -1 : 1) * SPEED[me.kind] * .9f;
        } else if (pad.block) {
            if (me.state != BLOCK) {
                me.state = BLOCK;
                me.clock = 0;
                me.across = 0;
            }
        } else if (pad.left != pad.right) {
            if (me.state != WALK) me.clock = 0;
            me.state = WALK;
            // She backs away a little slower than she comes on.
            boolean away = pad.left != me.facingLeft;
            me.x += (pad.left ? -1 : 1) * SPEED[me.kind] * (away ? .8f : 1) * seconds;
        } else if (me.state != IDLE) {
            me.state = IDLE;
            me.clock = 0;
        }
    }

    private void begin(Fighter me, int move) {
        me.state = ATTACK;
        me.move = move;
        me.clock = 0;
        me.landed = 0;
        me.rest[move] = MOVES[me.kind][move].rest;
        moves++;
        if (move == ULTIMATE) {
            me.meter = 0;
            // Nothing stops an ultimate once it is begun: she cannot be hit until it has been let fly.
            Move grand = MOVES[me.kind][move];
            me.safe = grand.length() - grand.recovery;
            phase = FLOURISH;
            phaseClock = 0;
            flourisher = me.side;
        }
    }

    private void attack(Fighter me, Fighter foe, float seconds) {
        Move move = me.doing();
        float way = me.facingLeft ? -1 : 1;
        // Without her bat Tsubakieh has only her hands: half the reach, half the hurt.
        float weak = me.unarmed && me.move != ULTIMATE ? .5f : 1;
        if (me.clock < move.startup) {
            // A heavy move steps in as it comes; an ultimate may rush the length of the stage, and stops when it gets there.
            float rush = move.rush > 0 ? move.rush : me.move == HEAVY ? 1.6f : 0;
            if (rush > 0 && Math.abs(foe.x - me.x) > BODY + .3f) me.x += way * rush * seconds;
        } else if (move.shot != NOTHING) {
            if (me.landed == 0) {
                me.landed = 1;
                launch(me, move.shot);
            }
        } else {
            float since = me.clock - move.startup;
            boolean open = move.times > 1 ? me.landed < move.times && since >= me.landed * move.every : me.landed == 0 && since <= move.active;
            if (open) {
                float ahead = (foe.x - me.x) * way;
                boolean near = ahead > -.3f && ahead <= move.reach * weak + BODY / 2 && (move.anywhere || foe.y < 1.5f);
                if (move.times > 1) me.landed++;
                if (near && hurt(foe, me, move.damage * weak, move.knock, move.stun, move.floors, me.x)) {
                    if (move.times == 1) me.landed = 1;
                }
            }
        }
        if (me.clock >= move.length()) {
            me.state = IDLE;
            me.move = -1;
            me.clock = 0;
        }
    }

    /** She throws something. */
    private void launch(Fighter me, int kind) {
        float way = me.facingLeft ? -1 : 1;
        // Every third thing Rune throws is her paper knife.
        if (kind == TEAR && ++me.specials % 3 == 0) kind = KNIFE;
        Shot shot = new Shot(kind, me.side);
        shot.x = me.x + way * .8f;
        shot.y = 1.35f;
        float speed;
        switch (kind) {
            case PANEL: speed = 13; shot.left = .5f; break;
            case BREAD: speed = 8.5f; shot.up = 5.5f; shot.left = 2; break;
            case BAT: speed = 13; shot.left = 2.4f; shot.lands = 2; me.unarmed = true; break;
            case TEAR: speed = 8; shot.up = 2.4f; shot.left = 1.7f; break;
            case KNIFE: speed = 17; shot.left = .9f; break;
            case HEART: speed = 14; shot.left = 1; break;
            case ORB: speed = 5.5f; shot.left = 2.4f; break;
            default: speed = 7; shot.y = .6f; shot.left = 2.4f; shot.lands = 3; break;
        }
        shot.across = way * speed;
        shots.add(shot);
        throwsMade++;
    }

    /** What a thing thrown takes off when it lands. */
    static float damageOf(int kind) {
        switch (kind) {
            case PANEL: return 11;
            case BREAD: return 9;
            case BAT: return 8;
            case TEAR: return 8;
            case KNIFE: return 12;
            case HEART: return 8;
            case ORB: return 10;
            default: return 11;
        }
    }

    /** How large a thing is as it flies: half its width, and half its height. A flood is wide and low, to be jumped. */
    static float sizeOf(int kind) { return kind == FLOOD ? .9f : kind == ORB ? .5f : .3f; }

    static float heightOf(int kind) { return kind == FLOOD ? .6f : sizeOf(kind); }

    /** A fighter who is not acting for herself: reeling, falling, lying, getting up, or standing where the round left her. */
    private void settle(Fighter me, float seconds) {
        if (me.state == HIT) {
            me.x += me.across * seconds;
            me.across *= Math.max(0, 1 - 7 * seconds);
            if (me.clock >= me.stunned) {
                me.state = IDLE;
                me.clock = 0;
            }
        } else if (me.state == DOWN) {
            if (me.y > 0 || me.up > 0) {
                me.x += me.across * seconds;
                me.y += me.up * seconds;
                me.up -= GRAVITY * seconds;
                if (me.y <= 0) {
                    me.y = 0;
                    me.up = 0;
                    me.lain = 0;
                }
            } else if (me.health <= 0) {
                me.state = OUT;
            } else if ((me.lain += seconds) >= .7f) {
                me.state = RISE;
                me.clock = 0;
                me.safe = .7f;
            }
        } else if (me.state == RISE) {
            if (me.clock >= .3f) {
                me.state = IDLE;
                me.clock = 0;
            }
        } else if (me.state == JUMP) {
            me.y = Math.max(0, me.y + me.up * seconds);
            me.up -= GRAVITY * seconds;
            if (me.y <= 0) me.state = IDLE;
        }
        me.x = Math.max(EDGE, Math.min(WIDE - EDGE, me.x));
    }

    /**
     * A blow reaches a fighter. Taken on her guard, from in front, it costs a fifth and only
     * pushes her back; otherwise it hurts, and stuns her or knocks her down. Both meters fill.
     * Returns whether it landed at all.
     */
    private boolean hurt(Fighter her, Fighter by, float damage, float knock, float stun, boolean floors, float from) {
        if (her.safe > 0 || her.state == DOWN || her.state == RISE || her.state == OUT) return false;
        float push = from < her.x ? 1 : -1;
        hitX = her.x;
        hitY = her.y + TALL * .55f;
        hitSide = her.side;
        if (her.state == BLOCK && her.facingLeft == (from < her.x)) {
            her.health = Math.max(1, her.health - damage * .2f);
            her.across = push * knock * .6f;
            her.meter = Math.min(METER, her.meter + 5);
            by.meter = Math.min(METER, by.meter + damage * .5f);
            blocks++;
            hitHard = false;
            return true;
        }
        her.health = Math.max(0, her.health - damage);
        by.meter = Math.min(METER, by.meter + damage * 1.2f);
        her.meter = Math.min(METER, her.meter + damage * .8f);
        hits++;
        hitHard = floors || damage >= 14;
        her.move = -1;
        her.clock = 0;
        her.across = push * knock;
        if (floors || her.health <= 0 || her.y > .3f) {
            her.state = DOWN;
            her.up = 6.5f;
            her.lain = 0;
            her.across = push * Math.max(4, knock * .7f);
            falls++;
        } else {
            her.state = HIT;
            her.stunned = stun;
        }
        return true;
    }

    /** Keeps the two on the stage, out of each other, and no further apart than the screen is wide. */
    private void apart() {
        Fighter one = fighters[0], other = fighters[1];
        for (Fighter fighter : fighters) fighter.x = Math.max(EDGE, Math.min(WIDE - EDGE, fighter.x));
        float between = other.x - one.x, room = BODY * .95f;
        if (Math.abs(between) < room && Math.abs(one.y - other.y) < 1.4f) {
            float push = (room - Math.abs(between)) / 2 * (between >= 0 ? 1 : -1);
            one.x -= push;
            other.x += push;
        } else if (Math.abs(between) > span) {
            float pull = (Math.abs(between) - span) / 2 * (between >= 0 ? 1 : -1);
            one.x += pull;
            other.x -= pull;
        }
        for (Fighter fighter : fighters) fighter.x = Math.max(EDGE, Math.min(WIDE - EDGE, fighter.x));
    }

    /** Moves on everything thrown, and sees what it has met. */
    private void fly(float seconds) {
        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            Fighter owner = fighters[shot.side], foe = fighters[1 - shot.side];
            shot.age += seconds;
            shot.wait -= seconds;
            shot.x += shot.across * seconds;
            shot.y += shot.up * seconds;
            if (shot.kind == BREAD) shot.up -= 14 * seconds;
            if (shot.kind == TEAR) shot.up -= 5 * seconds;
            boolean gone = (shot.left -= seconds) <= 0 || shot.x < -1 || shot.x > WIDE + 1 || shot.y < 0;
            if (shot.kind == BAT) {
                // The bat slows, turns, and comes back to her hand.
                float home = owner.x > shot.x ? 1 : -1;
                if (shot.age > .32f) shot.across += home * 34 * seconds;
                if (shot.age > .5f && Math.abs(owner.x - shot.x) < .7f) gone = true;
            }
            float size = sizeOf(shot.kind), high = heightOf(shot.kind);
            if (!gone && shot.wait <= 0 && Math.abs(shot.x - foe.x) < BODY / 2 + size && shot.y + high > foe.y && shot.y - high < foe.y + TALL) {
                boolean heavy = shot.kind == ORB || shot.kind == KNIFE;
                if (hurt(foe, owner, damageOf(shot.kind), heavy ? 7 : 3.5f, heavy ? .34f : .24f, false, shot.x - shot.across * .05f)) {
                    shot.wait = shot.kind == BAT ? .45f : shot.kind == FLOOD ? .17f : .3f;
                    if (--shot.lands <= 0) gone = true;
                }
            }
            // Two things thrown that meet are both spent; only a flood rolls on through.
            for (int other = shots.size() - 1; !gone && other >= 0; other--) {
                Shot met = shots.get(other);
                if (met.side == shot.side || Math.abs(met.x - shot.x) > size + sizeOf(met.kind) || Math.abs(met.y - shot.y) > .9f) continue;
                if (met.kind != FLOOD) met.left = 0;
                if (shot.kind != FLOOD) gone = true;
            }
            if (gone) {
                if (shot.kind == BAT) owner.unarmed = false;
                shots.remove(index);
            }
        }
    }

    /** Sees whether the round is over: one of them out, or the time gone. */
    private void judge() {
        Fighter one = fighters[0], other = fighters[1];
        boolean oneOut = one.health <= 0, otherOut = other.health <= 0;
        if (!oneOut && !otherOut && roundClock > 0) return;
        if (oneOut == otherOut) {
            // Time: whoever has more of her health left takes it. Neither, if they are level or both went down together.
            float mine = one.health / HEALTH[one.kind], theirs = other.health / HEALTH[other.kind];
            lastWinner = oneOut || Math.abs(mine - theirs) < .001f ? -1 : mine > theirs ? 0 : 1;
        } else {
            lastWinner = oneOut ? 1 : 0;
        }
        if (lastWinner >= 0) {
            Fighter winner = fighters[lastWinner];
            winner.wins++;
            if (winner.state != JUMP) {
                winner.state = WON;
                winner.move = -1;
                winner.clock = 0;
            }
        }
        shots.clear();
        for (Fighter fighter : fighters) fighter.unarmed = false;
        phase = ROUND_OVER;
        phaseClock = 0;
    }

    // The side that plays itself

    /**
     * What a fighter who plays herself holds down. She makes up her mind every so often, the
     * oftener the better she is: guards what is coming, closes to the distance she likes or
     * backs off to it, and picks among her moves by what she is. The worse she is, the more
     * often she guesses wrong or stands about.
     */
    private Pad think(Fighter me, Fighter foe, float seconds) {
        Pad pad = minds[me.side];
        pad.light = false;
        pad.heavy = false;
        pad.special = false;
        pad.jump = false;
        if ((thinking[me.side] -= seconds) > 0) return pad;
        int skill = level[me.side];
        thinking[me.side] = new float[]{.5f, .34f, .22f, .13f}[skill] * (.7f + random.nextFloat() * .6f);
        float sharp = new float[]{.25f, .5f, .75f, .95f}[skill];
        pad.clear();
        float gap = Math.abs(foe.x - me.x), want = WANTS[me.kind];
        boolean ranged = want > 2.5f, toLeft = foe.x < me.x;
        boolean danger = foe.state == ATTACK && foe.doing().shot == NOTHING && gap < foe.doing().reach + 1.2f;
        boolean flood = false;
        for (Shot shot : shots) {
            if (shot.side != me.side && Math.abs(shot.x - me.x) < 3 && (shot.across > 0) == (me.x > shot.x)) {
                danger = true;
                flood |= shot.kind == FLOOD;
            }
        }
        if (danger && random.nextFloat() < sharp * .85f) {
            if (flood || random.nextFloat() < .15f) pad.jump = true;
            else pad.block = true;
        } else if (random.nextFloat() > sharp + .2f) {
            // Standing about.
        } else if (me.charged() && gap <= ULTIMATE_REACH[me.kind]) {
            pad.special = true;
        } else if (gap > want + .7f) {
            pad.left = toLeft;
            pad.right = !toLeft;
            // On the way in, something thrown.
            if (me.rest[SPECIAL] <= 0 && random.nextFloat() < (ranged ? .7f : .3f) * sharp) {
                pad.left = false;
                pad.right = false;
                pad.special = true;
            }
        } else if (ranged && gap < want - 1.5f && me.x > EDGE + .5f && me.x < WIDE - EDGE - .5f && random.nextFloat() < .6f) {
            // Too close for one who throws: she pushes them off if they are on her, or backs away.
            if (gap < 1.6f && random.nextFloat() < .6f) {
                pad.light = true;
            } else {
                pad.left = !toLeft;
                pad.right = toLeft;
            }
        } else {
            float pick = random.nextFloat();
            boolean close = gap < MOVES[me.kind][HEAVY].reach + BODY / 2 || MOVES[me.kind][HEAVY].shot != NOTHING;
            if (ranged && gap > 2.2f) {
                if (pick < .65f && me.rest[SPECIAL] <= 0) pad.special = true;
                else if (pick < .85f && me.rest[HEAVY] <= 0 && close) pad.heavy = true;
                else if (pick > .93f) pad.jump = true;
            } else if (gap < MOVES[me.kind][LIGHT].reach + BODY / 2 + .2f) {
                if (pick < .55f) pad.light = true;
                else if (pick < .88f && me.rest[HEAVY] <= 0) pad.heavy = true;
                else if (me.rest[SPECIAL] <= 0) pad.special = true;
            } else if (close && pick < .5f && me.rest[HEAVY] <= 0) {
                pad.heavy = true;
            } else {
                pad.left = toLeft;
                pad.right = !toLeft;
            }
        }
        return pad;
    }
}
