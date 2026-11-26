package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Page brawl: a fight between two of the five of {@link FightCast}, the best of three rounds.
 * Each fights her own way. Halgakos has her ring of keys up close and Glossarion's scan from
 * the middle. Chikuwa is slow and sturdy, with brick bread in her hands and bread rolls to
 * throw. Tsubakieh has her bat, which comes back when thrown. Rune keeps away and throws ink
 * tears, and every third one is a paper knife. Illya has nothing but what she throws: hearts,
 * magic, and a push to get them off her.
 *
 * A quick or a heavy blow pressed again in time goes on to the next of a chain of five, each
 * a different blow, and the two may be mixed; the fifth knocks down. In the air there are
 * three quick ones and a smash, which comes straight down and shakes the ground. A dash, a
 * back-step and a dodge get her about, and there is a blow that breaks a guard, one that
 * launches, and a shove. A special must rest between uses; an ultimate needs a full meter;
 * and when one goes for her ultimate before anything of the other's has landed the two clash,
 * and whoever taps faster has a greater one.
 *
 * Everything is measured in the stage's own lengths and in seconds. Across is from the
 * left; height is up from the floor. This class knows nothing of screens: it is moved on by
 * {@link #step}, with what the player holds down, and the other side plays itself.
 */
final class Fight {
    static final int HALGAKOS = 0, CHIKUWA = 1, TSUBAKIEH = 2, RUNE = 3, ILLYA = 4, FIGHTERS = 5;
    /** What a fighter is doing. */
    static final int IDLE = 0, WALK = 1, JUMP = 2, BLOCK = 3, ATTACK = 4, HIT = 5, DOWN = 6, RISE = 7, WON = 8, OUT = 9, DASH = 10,
            BACKSTEP = 11, LAND = 12, DODGE = 13;
    /**
     * Her moves. The first four are what the buttons begin: a quick blow, a heavy one, her
     * special and her ultimate. Then the second to fifth of the quick chain and of the heavy
     * one, three quick blows in the air, the smash, her special thrown from the air, the two
     * blows out of a dash, the blow that breaks a guard, the one that launches, the shove,
     * and the greater ultimate of whoever wins a clash.
     */
    static final int LIGHT = 0, HEAVY = 1, SPECIAL = 2, ULTIMATE = 3, QUICK2 = 4, QUICK5 = 7, HEAVY2 = 8, HEAVY5 = 11, AIR1 = 12, AIR3 = 14,
            SMASH = 15, AIR_SPECIAL = 16, DASH_LIGHT = 17, DASH_HEAVY = 18, BREAK = 19, LAUNCH = 20, SHOVE = 21, CLASH = 22, MOVE_COUNT = 23;
    /** What may fly: her scan, a bread roll, the bat, an ink tear, the paper knife, a heart, a ball of magic, and a flood of tears. */
    static final int NOTHING = -1, PANEL = 0, BREAD = 1, BAT = 2, TEAR = 3, KNIFE = 4, HEART = 5, ORB = 6, FLOOD = 7;
    /** Where the fight has got to: on, held for the flourish of an ultimate, between rounds, over, or held while two ultimates clash. */
    static final int FIGHTING = 0, FLOURISH = 1, ROUND_OVER = 2, MATCH_OVER = 3, CLASHING = 4;

    static final float WIDE = 14, BODY = .9f, TALL = 2.4f, METER = 100;
    static final int ROUNDS = 2;
    static final float ROUND_SECONDS = 60, FLOURISH_SECONDS = 1.1f, BETWEEN = 2.6f;
    /** How long a clash lasts, and how long after it a last tap still counts. */
    static final float CLASH_SECONDS = 4, CLASH_GRACE = .25f;
    /** How long the bar of a clash is left on show once it is settled, and how much of that it takes to fill for whoever won. */
    static final float CLASH_SHOWN = .45f, CLASH_SWEEP = .14f;
    /** How long after a blow the next press still goes on with its chain; how long a dodge lasts, how much of it nothing can touch her, and how long before the next. */
    static final float CHAIN_WINDOW = .3f, DODGE_SECONDS = .34f, DODGE_SAFE = .22f, DODGE_REST = .9f;
    static final float DASH_SECONDS = .26f, BACKSTEP_SECONDS = .24f, LAND_SECONDS = .09f;
    /** The hardest the other side plays, from 0. */
    static final int LEVELS = 4;

    private static final float STEP = 1 / 60f, GRAVITY = 30, LEAP = 11.5f, EDGE = 1.1f, DOUBLE_TAP = .28f, PLUNGE = 24;
    /** How many times a flood lands as it rolls through. */
    private static final int FLOOD_LANDS = 3;
    private static final float[] HEALTH = {100, 108, 100, 98, 90};
    private static final float[] SPEED = {4.6f, 4.1f, 5.2f, 4.4f, 4.2f};
    /** How far off each likes to stand, and how near the other must be for her ultimate to be worth using. */
    private static final float[] WANTS = {3.2f, 1.2f, 1.5f, 6.5f, 7};
    private static final float[] ULTIMATE_REACH = {8, 2.8f, 6, 9, 9};
    /** How much further each blow of a chain reaches than the one before, and how fast she steps in with it: Illya's spells grow, and she stays where she is. */
    private static final float[] GROWS = {.06f, .05f, .05f, .12f, .5f};
    private static final float[] STEPS_IN = {2.4f, 2.2f, 2.6f, 1.6f, 0};
    /** How fast one who plays herself taps in a clash, by how well she plays, in taps a second. */
    static final float[] TAP_RATE = {3, 4.2f, 5.4f, 6.6f};

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
        /** Whether it goes through a guard, throws them into the air, and reaches behind her as well as in front. */
        boolean breaks, launches, around;

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

        Move rushing(float speed) {
            rush = speed;
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

    /**
     * All of a fighter's moves, from the four she is given: the rest of each chain is made
     * from its first blow, each a little different in its speed, its reach and what it does,
     * and the fifth the hardest; the others are made from the same two.
     */
    private static Move[] all(int kind, Move light, Move heavy, Move special, Move ultimate) {
        Move[] moves = new Move[MOVE_COUNT];
        moves[LIGHT] = light;
        moves[HEAVY] = heavy;
        moves[SPECIAL] = special;
        moves[ULTIMATE] = ultimate;
        float grow = GROWS[kind], in = STEPS_IN[kind];
        float[] quickTime = {.9f, 1, 1.1f, 1.5f}, quickAfter = {1, 1, 1.1f, 1.9f}, quickHurt = {.9f, 1, 1.1f, 1.7f}, quickPush = {.7f, .8f, 1, 2.2f};
        float[] heavyTime = {.85f, .9f, 1, 1.25f}, heavyAfter = {1, 1, 1.1f, 1.6f}, heavyHurt = {.8f, .9f, 1, 1.5f}, heavyPush = {.6f, .7f, .9f, 1.8f};
        boolean throwsHeavy = heavy.shot != NOTHING;
        for (int index = 0; index < 4; index++) {
            int number = index + 2;
            // Each stuns for long enough that the next of its own chain, pressed in time, lands before she can do anything about it.
            float quickNext = index < 3 ? light.active + light.startup * quickTime[index + 1] + .05f : 0;
            float heavyNext = index < 3 ? heavy.active + heavy.startup * heavyTime[index + 1] + .05f : 0;
            moves[QUICK2 + index] = strike(light.startup * quickTime[index], light.active, light.recovery * quickAfter[index],
                    light.reach * (1 + grow * (number - 1)), light.damage * quickHurt[index], light.knock * quickPush[index],
                    Math.max(light.stun * (number == 5 ? 1.3f : 1.05f), quickNext), number == 5).rushing(in);
            // What is thrown in a chain needs no rest between, but the chain leaves her heavy move resting when it is done.
            moves[HEAVY2 + index] = throwsHeavy ? thrown(heavy.startup * .9f, heavy.recovery * .9f, number % 2 == 0 ? HEART : ORB, heavy.rest)
                    : strike(heavy.startup * heavyTime[index], heavy.active, heavy.recovery * heavyAfter[index],
                    heavy.reach * (1 + grow * .6f * (number - 1)), heavy.damage * heavyHurt[index], heavy.knock * heavyPush[index],
                    Math.max(heavy.stun, heavyNext), number == 5).rushing(in + .6f);
        }
        float heavyReach = throwsHeavy ? 2.2f : heavy.reach, heavyHurts = throwsHeavy ? damageOf(heavy.shot) : heavy.damage;
        for (int index = 0; index < 3; index++) {
            moves[AIR1 + index] = strike(.07f, .14f, .12f, light.reach * 1.15f * (1 + grow), light.damage * (index == 2 ? 1.3f : .95f), 3, .32f, false);
        }
        moves[SMASH] = strike(.16f, .1f, .5f, 2.1f, Math.max(12, heavyHurts * 1.15f), 9, .5f, true);
        moves[SMASH].around = true;
        moves[AIR_SPECIAL] = thrown(special.startup * .8f, .2f, special.shot, special.rest);
        moves[DASH_LIGHT] = strike(.06f, .12f, .22f, light.reach * 1.3f, light.damage * 1.3f, 5, .3f, false).rushing(5);
        moves[DASH_HEAVY] = strike(.14f, .14f, .4f, Math.max(1.9f, heavyReach * 1.15f), 13, 9, .4f, true).rushing(6);
        moves[BREAK] = strike(.4f, .12f, .45f, Math.max(1.7f, light.reach * 1.3f), 12, 6, .55f, false);
        moves[BREAK].breaks = true;
        moves[LAUNCH] = strike(.16f, .1f, .5f, kind == ILLYA ? 2.2f : 1.5f, 8, 2, .3f, false);
        moves[LAUNCH].launches = true;
        moves[SHOVE] = strike(.08f, .1f, .3f, 1.3f, 2, 13, .25f, false);
        moves[CLASH] = strike(.3f, .1f, .6f, 12, 10, 4, .3f, false).often(5, .22f, 0, true);
        return moves;
    }

    /** Every fighter's moves, by the numbers above. */
    static final Move[][] MOVES = {
            // Halgakos: the key ring, a key thrust, Glossarion's scan, and a translation in three chunks that reaches the whole stage.
            all(HALGAKOS, strike(.09f, .1f, .16f, 1.3f, 7, 3, .22f, false), strike(.2f, .12f, .3f, 2.1f, 12, 6, .34f, false),
                    thrown(.18f, .28f, PANEL, .9f), strike(.3f, .1f, .5f, 9, 10, 4, .3f, false).often(3, .3f, 0, true)),
            // Chikuwa: a baguette, the brick bread, a bread roll, and the brick bread brought down from overhead.
            all(CHIKUWA, strike(.1f, .1f, .18f, 1.5f, 7, 3, .24f, false), strike(.26f, .14f, .36f, 1.7f, 16, 9, .4f, true),
                    thrown(.2f, .28f, BREAD, 1), strike(.5f, .16f, .6f, 3, 34, 12, .5f, true).often(1, 0, 3, false)),
            // Tsubakieh: a poke of the bat, a full swing, the bat thrown to come back, and a home run after a rush.
            all(TSUBAKIEH, strike(.09f, .1f, .17f, 1.5f, 6, 3, .2f, false), strike(.26f, .12f, .34f, 1.9f, 13, 8, .34f, false),
                    thrown(.2f, .3f, BAT, 2.8f), strike(.36f, .16f, .6f, 2.2f, 32, 16, .5f, true).often(1, 0, 14, false)),
            // Rune: a flick of ink, the paper knife, a tear thrown (every third a knife), and a flood.
            all(RUNE, strike(.1f, .1f, .2f, 1.4f, 5, 7, .22f, false), strike(.2f, .12f, .3f, 1.6f, 10, 5, .32f, false),
                    thrown(.16f, .26f, TEAR, .7f), thrown(.4f, .6f, FLOOD, 0)),
            // Illya: a heart that pushes them off, a ball of magic, a heart beam, and a beam the whole stage long.
            all(ILLYA, strike(.1f, .1f, .22f, 1.2f, 4, 11, .2f, false), thrown(.26f, .34f, ORB, 1.1f),
                    thrown(.16f, .26f, HEART, 1), strike(.34f, .1f, .5f, 9, 8, 3, .28f, false).often(4, .24f, 0, true)),
    };

    /** Which blow of a chain of five a move is, from 1, or 0 if it is none. A blow out of a dash counts as a first. */
    static int chainStep(int move) {
        return move == LIGHT || move == HEAVY || move == DASH_LIGHT ? 1 : move >= QUICK2 && move <= QUICK5 ? move - QUICK2 + 2
                : move >= HEAVY2 && move <= HEAVY5 ? move - HEAVY2 + 2 : 0;
    }

    /** The move that follows a blow of a chain: the next quick one, or the next heavy one. */
    static int next(int step, boolean heavy) { return (heavy ? HEAVY2 : QUICK2) + step - 1; }

    /** Whether a move is one of the heavy kind. */
    static boolean heavyKind(int move) {
        return move == HEAVY || move >= HEAVY2 && move <= HEAVY5 || move == SMASH || move == DASH_HEAVY || move == BREAK || move == LAUNCH;
    }

    /** Which of the four rests a move has to wait for and sets going: the quick one's, the heavy one's, the special's or the ultimate's. */
    private static int slot(int move) {
        return move == SPECIAL || move == AIR_SPECIAL ? SPECIAL : move == ULTIMATE || move == CLASH ? ULTIMATE : heavyKind(move) ? HEAVY : LIGHT;
    }

    /** What is held down: the arrows, the jump, the guard, the three that hit, the dodge and the ultimate; and how many times the screen was tapped, for a clash. */
    static final class Pad {
        boolean left, right, jump, block, light, heavy, special, dodge, ultimate;
        int taps;

        void clear() {
            left = false;
            right = false;
            jump = false;
            block = false;
            light = false;
            heavy = false;
            special = false;
            dodge = false;
            ultimate = false;
            taps = 0;
        }

        /** What is held down as one number, a bit for each, to send to another device. */
        int bits() {
            return (left ? 1 : 0) | (right ? 2 : 0) | (jump ? 4 : 0) | (block ? 8 : 0) | (light ? 16 : 0) | (heavy ? 32 : 0) | (special ? 64 : 0)
                    | (dodge ? 128 : 0) | (ultimate ? 256 : 0);
        }

        void set(int bits) {
            left = (bits & 1) != 0;
            right = (bits & 2) != 0;
            jump = (bits & 4) != 0;
            block = (bits & 8) != 0;
            light = (bits & 16) != 0;
            heavy = (bits & 32) != 0;
            special = (bits & 64) != 0;
            dodge = (bits & 128) != 0;
            ultimate = (bits & 256) != 0;
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
        /** How long until each kind of move may be used again, until she can be hit again, how long she is stunned, and how long she has lain. */
        final float[] rest = new float[4];
        float safe, stunned, lain;
        /** How many specials she has used, rounds won, and whether her bat is out of her hands. */
        int specials, wins;
        boolean unarmed;
        /** Which blow of a chain her last was, how long ago it ended, and whether the one she is in the middle of has landed. */
        int chain;
        float since = 9;
        boolean hitLanded;
        /** How many blows running she has landed on the other without letting her go, what they took off, and how long ago the last was. */
        int combo;
        float comboDamage, comboAge = 9;
        /** How long ago she turned round and took a blow on her guard; how she reels (0 and 1 lightly, 2 hard); when a smash of hers came down. */
        float turned = 9, blockAge = 9, mark;
        int reel;
        /** Walking away from the other; guarding in the air; how many blows she has taken in the air since she was thrown there. */
        boolean backing, guarding;
        int juggled;
        /** How long before she can dodge again, how many quick blows she has struck this jump, and whether she has dodged in it. */
        float dodgeRest;
        int airMoves;
        boolean airDodged;
        private boolean light, heavy, special, jump, dodge, ultimate, left, right, followed;
        private float sinceLeft = 9, sinceRight = 9;
        /** What she pressed in the middle of a blow, to do as soon as she may: 1 a quick one, 2 a heavy one, 3 her special. */
        private int queued;

        Fighter(int kind, int side) {
            this.kind = kind;
            this.side = side;
        }

        Move doing() { return move < 0 ? null : MOVES[kind][move]; }

        /** Whether her ultimate is ready. */
        boolean charged() { return meter >= METER; }

        boolean inAir() { return y > 0 || up != 0; }
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
    private int hits, blocks, throwsMade, falls, moves, quakes, dodges, clashes;
    private float hitX, hitY, quakeX;
    private boolean hitHard;
    private int hitSide;
    /** How long the fight is held still for a blow that has just landed; and whether what is landing now is to take off all it would alone. */
    private float stop;
    private boolean whole;
    /** The buttons each pressed while the fight was held still, as bits, to count when it moves again: the jump, the three that hit, the dodge and the ultimate. */
    private static final int PRESSES = 4 | 16 | 32 | 64 | 128 | 256;
    private final int[] kept = new int[2];
    private final Pad[] with = {new Pad(), new Pad()};
    /** A clash: how much of it is left, how often each has tapped, who went for her ultimate second, and when one who plays herself answers an ultimate with her own. */
    private float clashLeft, answer = -1;
    private final int[] taps = new int[2];
    private final float[] tapped = new float[2];
    private int clashSecond = -1;

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
            fighter.chain = 0;
            fighter.since = 9;
            fighter.combo = 0;
            fighter.comboAge = 9;
            fighter.turned = 9;
            fighter.blockAge = 9;
            fighter.queued = 0;
            fighter.guarding = false;
            fighter.juggled = 0;
            fighter.dodgeRest = 0;
            fighter.airMoves = 0;
            java.util.Arrays.fill(fighter.rest, 0);
        }
        shots.clear();
        roundClock = ROUND_SECONDS;
        phase = FIGHTING;
        phaseClock = 0;
        stop = 0;
        kept[0] = 0;
        kept[1] = 0;
        answer = -1;
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
    /** Smashes that have come down, where the last did, dodges made and clashes begun. */
    int quakes() { return quakes; }
    float quakeX() { return quakeX; }
    int dodges() { return dodges; }
    int clashes() { return clashes; }
    /** Where the last blow landed, whether it was a heavy one, and on which side's fighter. */
    float hitX() { return hitX; }
    float hitY() { return hitY; }
    boolean hitHard() { return hitHard; }
    int hitSide() { return hitSide; }
    /**
     * Where the divider of a clash's bar stands, from 0 at its left end to 1 at its right: the
     * left side's colour fills up to it and the right side's from it. It starts in the middle
     * and goes toward whoever is behind, by how many taps the other leads: a tap of a lead is
     * easily seen, and however far ahead one is it never reaches an end while they still tap.
     */
    static float clashBar(int left, int right) { return .5f + .44f * (float) Math.tanh((left - right) / 8f); }

    float clashBar() { return clashBar(taps[0], taps[1]); }

    /** Who won the clash that has just been settled, while her greater ultimate is announced; below zero at any other time. */
    int clashWinner() { return phase == FLOURISH && flourisher >= 0 && fighters[flourisher].move == CLASH ? flourisher : -1; }

    /** How long ago it was settled, and how far the bar has filled for whoever won it: all of it, quickly. */
    float clashAge() { return clashWinner() < 0 ? 0 : phaseClock; }

    float clashSweep() { return clashWinner() < 0 ? 0 : Math.min(1, phaseClock / CLASH_SWEEP); }

    /** How much of a clash is left, in seconds; how often a side has tapped in it; and who went for her ultimate second. */
    float clashLeft() { return clashLeft; }
    int taps(int side) { return taps[side]; }
    int clashSecond() { return clashSecond; }

    void setSpan(float span) { this.span = Math.max(5, Math.min(WIDE - 2 * EDGE, span)); }

    /**
     * Writes down the whole of the fight as it stands, so that another device can show it:
     * everything that is drawn or heard, and nothing of how either side decides what to do.
     */
    void write(java.io.DataOutput out) throws java.io.IOException {
        out.writeByte(phase);
        out.writeByte(round);
        out.writeByte(flourisher);
        out.writeByte(lastWinner);
        out.writeFloat(phaseClock);
        out.writeFloat(roundClock);
        out.writeFloat(time);
        out.writeInt(hits);
        out.writeInt(blocks);
        out.writeInt(throwsMade);
        out.writeInt(falls);
        out.writeInt(moves);
        out.writeInt(quakes);
        out.writeInt(dodges);
        out.writeInt(clashes);
        out.writeFloat(hitX);
        out.writeFloat(hitY);
        out.writeFloat(quakeX);
        out.writeBoolean(hitHard);
        out.writeByte(hitSide);
        out.writeFloat(clashLeft);
        out.writeShort(taps[0]);
        out.writeShort(taps[1]);
        out.writeByte(clashSecond);
        for (Fighter fighter : fighters) {
            out.writeFloat(fighter.x);
            out.writeFloat(fighter.y);
            out.writeFloat(fighter.across);
            out.writeFloat(fighter.up);
            out.writeBoolean(fighter.facingLeft);
            out.writeByte(fighter.state);
            out.writeByte(fighter.move);
            out.writeFloat(fighter.clock);
            out.writeInt(fighter.landed);
            out.writeFloat(fighter.health);
            out.writeFloat(fighter.meter);
            for (float rest : fighter.rest) out.writeFloat(rest);
            out.writeFloat(fighter.safe);
            out.writeFloat(fighter.stunned);
            out.writeFloat(fighter.lain);
            out.writeInt(fighter.specials);
            out.writeByte(fighter.wins);
            out.writeBoolean(fighter.unarmed);
            out.writeByte(fighter.chain);
            out.writeFloat(fighter.since);
            out.writeBoolean(fighter.hitLanded);
            out.writeByte(fighter.combo);
            out.writeFloat(fighter.comboDamage);
            out.writeFloat(fighter.comboAge);
            out.writeFloat(fighter.turned);
            out.writeFloat(fighter.blockAge);
            out.writeFloat(fighter.mark);
            out.writeByte(fighter.reel);
            out.writeBoolean(fighter.backing);
            out.writeBoolean(fighter.guarding);
            out.writeFloat(fighter.dodgeRest);
        }
        out.writeByte(shots.size());
        for (Shot shot : shots) {
            out.writeByte(shot.kind);
            out.writeByte(shot.side);
            out.writeFloat(shot.x);
            out.writeFloat(shot.y);
            out.writeFloat(shot.across);
            out.writeFloat(shot.up);
            out.writeFloat(shot.left);
            out.writeFloat(shot.age);
            out.writeByte(shot.lands);
            out.writeFloat(shot.wait);
        }
    }

    /** Makes this fight stand as one written down by {@link #write}: for showing a fight that is run on another device. */
    void read(java.io.DataInput in) throws java.io.IOException {
        phase = in.readByte();
        round = in.readByte();
        flourisher = in.readByte();
        lastWinner = in.readByte();
        phaseClock = in.readFloat();
        roundClock = in.readFloat();
        time = in.readFloat();
        hits = in.readInt();
        blocks = in.readInt();
        throwsMade = in.readInt();
        falls = in.readInt();
        moves = in.readInt();
        quakes = in.readInt();
        dodges = in.readInt();
        clashes = in.readInt();
        hitX = in.readFloat();
        hitY = in.readFloat();
        quakeX = in.readFloat();
        hitHard = in.readBoolean();
        hitSide = in.readByte();
        clashLeft = in.readFloat();
        taps[0] = in.readShort();
        taps[1] = in.readShort();
        clashSecond = in.readByte();
        for (Fighter fighter : fighters) {
            fighter.x = in.readFloat();
            fighter.y = in.readFloat();
            fighter.across = in.readFloat();
            fighter.up = in.readFloat();
            fighter.facingLeft = in.readBoolean();
            fighter.state = in.readByte();
            fighter.move = in.readByte();
            fighter.clock = in.readFloat();
            fighter.landed = in.readInt();
            fighter.health = in.readFloat();
            fighter.meter = in.readFloat();
            for (int index = 0; index < fighter.rest.length; index++) fighter.rest[index] = in.readFloat();
            fighter.safe = in.readFloat();
            fighter.stunned = in.readFloat();
            fighter.lain = in.readFloat();
            fighter.specials = in.readInt();
            fighter.wins = in.readByte();
            fighter.unarmed = in.readBoolean();
            fighter.chain = in.readByte();
            fighter.since = in.readFloat();
            fighter.hitLanded = in.readBoolean();
            fighter.combo = in.readByte();
            fighter.comboDamage = in.readFloat();
            fighter.comboAge = in.readFloat();
            fighter.turned = in.readFloat();
            fighter.blockAge = in.readFloat();
            fighter.mark = in.readFloat();
            fighter.reel = in.readByte();
            fighter.backing = in.readBoolean();
            fighter.guarding = in.readBoolean();
            fighter.dodgeRest = in.readFloat();
        }
        shots.clear();
        for (int count = in.readUnsignedByte(); count > 0; count--) {
            Shot shot = new Shot(in.readByte(), in.readByte());
            shot.x = in.readFloat();
            shot.y = in.readFloat();
            shot.across = in.readFloat();
            shot.up = in.readFloat();
            shot.left = in.readFloat();
            shot.age = in.readFloat();
            shot.lands = in.readByte();
            shot.wait = in.readFloat();
            shots.add(shot);
        }
    }

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
            // A tap is counted once, however many slices the time was taken in.
            if (pad != null) pad.taps = 0;
            if (other != null) other.taps = 0;
        }
    }

    private Pad held(int side, Pad pad, Pad other) { return level[side] >= 0 ? null : side == 0 ? pad : other; }

    private void once(float seconds, Pad pad, Pad other) {
        time += seconds;
        if (phase == CLASHING) {
            // Nothing moves but the count of who taps faster.
            clashLeft -= seconds;
            for (int side = 0; side < 2; side++) {
                Pad mine = held(side, pad, other);
                if (mine != null) {
                    taps[side] += mine.taps;
                } else {
                    tapped[side] += TAP_RATE[level[side]] * (.75f + random.nextFloat() * .5f) * seconds;
                    if (clashLeft > 0) taps[side] = (int) tapped[side];
                }
            }
            if (clashLeft <= -CLASH_GRACE) settleClash();
            return;
        }
        if (phase == FLOURISH) {
            // The fight holds its breath while an ultimate is announced. The other may answer it with her own, and then the two clash:
            // her button held at any moment of the hold is enough, so a press made while nothing moves is not lost.
            Fighter first = fighters[flourisher], second = fighters[1 - flourisher];
            if (first.move == ULTIMATE && second.charged() && second.state != OUT) {
                Pad hers = held(second.side, pad, other);
                if (hers != null ? hers.ultimate : answer >= 0 && phaseClock >= answer) {
                    clash(second);
                    return;
                }
            }
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
        if (stop > 0) {
            // A blow that has just landed holds everything still for a moment. What is pressed in that moment is kept for when it is over.
            stop -= seconds;
            for (int side = 0; side < 2; side++) {
                Pad mine = held(side, pad, other);
                if (mine != null) kept[side] |= mine.bits() & PRESSES;
            }
            return;
        }
        roundClock -= seconds;
        for (int side = 0; side < 2; side++) {
            Fighter me = fighters[side], foe = fighters[1 - side];
            Pad mine = held(side, pad, other);
            if (mine != null && kept[side] != 0) {
                with[side].set(mine.bits() | kept[side]);
                kept[side] = 0;
                mine = with[side];
            }
            if (mine == null) mine = level[side] >= 0 ? think(me, foe, seconds) : minds[side];
            act(me, foe, mine, seconds);
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
        me.dodgeRest = Math.max(0, me.dodgeRest - seconds);
        me.clock += seconds;
        me.since += seconds;
        me.comboAge += seconds;
        me.turned += seconds;
        me.blockAge += seconds;
        me.sinceLeft += seconds;
        me.sinceRight += seconds;
        // A press counts once: holding a button down does not repeat it.
        boolean light = pad.light && !me.light, heavy = pad.heavy && !me.heavy, special = pad.special && !me.special;
        boolean jump = pad.jump && !me.jump, dodge = pad.dodge && !me.dodge, ultimate = pad.ultimate && !me.ultimate;
        boolean left = pad.left && !me.left, right = pad.right && !me.right;
        me.light = pad.light;
        me.heavy = pad.heavy;
        me.special = pad.special;
        me.jump = pad.jump;
        me.dodge = pad.dodge;
        me.ultimate = pad.ultimate;
        me.left = pad.left;
        me.right = pad.right;
        // An arrow pressed twice quickly is a dash, or a step back.
        boolean twice = left && me.sinceLeft <= DOUBLE_TAP || right && me.sinceRight <= DOUBLE_TAP;
        if (left) me.sinceLeft = 0;
        if (right) me.sinceRight = 0;
        // Her ultimate, gone for while the other's is under way and has not yet landed, is a clash, whatever she is in the middle of.
        if (ultimate && me.charged() && me.state != OUT && pending(foe)) {
            clash(me);
            return;
        }
        if (me.state == HIT || me.state == DOWN || me.state == RISE || me.state == OUT || me.state == WON) {
            settle(me, seconds);
            return;
        }
        if (me.state == ATTACK) {
            if (light) me.queued = 1;
            if (heavy) me.queued = 2;
            if (special) me.queued = 3;
            attack(me, foe, seconds);
            return;
        }
        if (me.state == DODGE) {
            me.x += me.across * seconds;
            fall(me, seconds);
            if (me.clock >= DODGE_SECONDS) {
                me.across = me.inAir() ? me.across * .4f : 0;
                me.state = me.inAir() ? JUMP : IDLE;
                me.clock = 1;
            }
            return;
        }
        if (me.state == JUMP) {
            me.x += me.across * seconds;
            me.guarding = pad.block;
            if (dodge && !me.airDodged && me.dodgeRest <= 0) {
                me.airDodged = true;
                dodge(me, pad);
            } else if (special && me.rest[SPECIAL] <= 0) {
                begin(me, foe, AIR_SPECIAL);
            } else if (heavy) {
                begin(me, foe, SMASH);
            } else if (light && me.airMoves < 3) {
                begin(me, foe, AIR1 + me.airMoves);
            } else if (fall(me, seconds)) {
                me.state = LAND;
                me.clock = 0;
            }
            return;
        }
        if (me.state == DASH || me.state == BACKSTEP) {
            me.x += me.across * seconds;
            if (me.state == DASH && heavy) {
                begin(me, foe, DASH_HEAVY);
            } else if (me.state == DASH && light) {
                begin(me, foe, DASH_LIGHT);
            } else if (me.clock >= (me.state == DASH ? DASH_SECONDS : BACKSTEP_SECONDS)) {
                me.state = IDLE;
                me.clock = 0;
                me.across = 0;
            }
            return;
        }
        if (me.state == LAND) {
            if (me.clock < LAND_SECONDS) return;
            me.state = IDLE;
            me.clock = 0;
        }
        boolean was = me.facingLeft;
        me.facingLeft = foe.x < me.x;
        if (was != me.facingLeft) me.turned = 0;
        if (me.state == BLOCK) {
            // A blow taken on the guard pushes her back a little.
            me.x += me.across * seconds;
            me.across *= Math.max(0, 1 - 9 * seconds);
        }
        // Whether she holds the arrow that points away from the other; and whether a press now goes on with a chain.
        boolean away = pad.left != pad.right && pad.left != me.facingLeft;
        boolean chaining = me.since <= CHAIN_WINDOW && me.chain >= 1 && me.chain < 5;
        if (ultimate && me.charged()) {
            begin(me, foe, ULTIMATE);
        } else if (dodge && me.dodgeRest <= 0) {
            dodge(me, pad);
        } else if (special && me.rest[SPECIAL] <= 0) {
            begin(me, foe, SPECIAL);
        } else if (heavy && pad.block) {
            begin(me, foe, LAUNCH);
        } else if (light && pad.block) {
            begin(me, foe, SHOVE);
        } else if ((heavy || light) && chaining) {
            begin(me, foe, next(me.chain, heavy));
        } else if (heavy && away && me.rest[HEAVY] <= 0) {
            begin(me, foe, BREAK);
        } else if (heavy && me.rest[HEAVY] <= 0) {
            begin(me, foe, HEAVY);
        } else if (light) {
            begin(me, foe, LIGHT);
        } else if (jump) {
            me.state = JUMP;
            me.clock = 0;
            me.up = LEAP;
            me.across = pad.left == pad.right ? 0 : (pad.left ? -1 : 1) * SPEED[me.kind] * .9f;
            me.airMoves = 0;
            me.airDodged = false;
            me.followed = false;
            me.guarding = false;
        } else if (pad.block) {
            if (me.state != BLOCK) {
                me.state = BLOCK;
                me.clock = 0;
                me.across = 0;
            }
        } else if (twice && pad.left != pad.right) {
            // Toward the other it is a dash; away from her, a hop back.
            me.state = away ? BACKSTEP : DASH;
            me.clock = 0;
            me.across = (pad.left ? -1 : 1) * SPEED[me.kind] * (away ? 2.2f : 2.6f);
        } else if (pad.left != pad.right) {
            if (me.state != WALK || me.backing != away) me.clock = 0;
            me.state = WALK;
            me.backing = away;
            // She backs away a little slower than she comes on.
            me.x += (pad.left ? -1 : 1) * SPEED[me.kind] * (away ? .8f : 1) * seconds;
        } else if (me.state != IDLE) {
            me.state = IDLE;
            me.clock = 0;
        }
    }

    /** She falls, if she is in the air. Returns whether she has just come down. */
    private boolean fall(Fighter me, float seconds) {
        if (!me.inAir()) return false;
        me.y += me.up * seconds;
        me.up -= GRAVITY * seconds;
        if (me.y > 0) return false;
        me.y = 0;
        me.up = 0;
        me.across = 0;
        me.guarding = false;
        return true;
    }

    /** A dodge: for a moment nothing can touch her, and she slips the way an arrow is held, through the other if she is in the way. */
    private void dodge(Fighter me, Pad pad) {
        float way = pad.left == pad.right ? 0 : pad.left ? -1 : 1;
        me.state = DODGE;
        me.move = -1;
        me.clock = 0;
        me.safe = DODGE_SAFE;
        me.dodgeRest = DODGE_REST;
        me.across = way * (me.inAir() ? 6 : 9.5f);
        me.chain = 0;
        dodges++;
    }

    private void begin(Fighter me, Fighter foe, int move) {
        me.state = ATTACK;
        me.move = move;
        me.clock = 0;
        me.landed = 0;
        me.hitLanded = false;
        me.queued = 0;
        me.followed = false;
        me.mark = 0;
        me.chain = chainStep(move);
        me.guarding = false;
        int slot = slot(move);
        me.rest[slot] = Math.max(me.rest[slot], MOVES[me.kind][move].rest);
        moves++;
        if (move == SPECIAL || move == AIR_SPECIAL) me.specials++;
        if (move >= AIR1 && move <= AIR3) {
            // Each quick blow in the air holds her up a little.
            me.airMoves++;
            me.up = Math.max(me.up, 3);
        } else if (move == SMASH) {
            me.across *= .3f;
            me.up = 2;
        } else if (move == DASH_LIGHT || move == DASH_HEAVY) {
            me.across = 0;
        }
        if (move == ULTIMATE || move == CLASH) {
            me.meter = 0;
            // Nothing stops an ultimate once it is begun: she cannot be hit until it has been let fly.
            Move grand = MOVES[me.kind][move];
            me.safe = grand.length() - grand.recovery;
            phase = FLOURISH;
            phaseClock = 0;
            flourisher = me.side;
            // One who plays herself, with her own meter full, may answer it: the better she plays the likelier.
            int other = 1 - me.side;
            answer = move == ULTIMATE && level[other] >= 0 && foe.charged() && random.nextFloat() < .35f + .18f * level[other]
                    ? .2f + random.nextFloat() * .6f : -1;
        }
    }

    /**
     * Whether an ultimate of hers is under way and nothing of it has landed yet: announced,
     * coming out, or thrown and still on its way. Until then the other may meet it with her own.
     */
    private boolean pending(Fighter her) {
        if (her.state == ATTACK && her.move == ULTIMATE) {
            Move move = her.doing();
            if (her.hitLanded) return false;
            if (move.times > 1 ? her.landed < move.times : her.landed == 0 && her.clock <= move.startup + move.active) return true;
        }
        // A flood that has not reached anyone yet.
        for (Shot shot : shots) if (shot.kind == FLOOD && shot.side == her.side && shot.lands == FLOOD_LANDS) return true;
        return false;
    }

    /** Two ultimates at once: neither goes off, and the two tap for it. */
    private void clash(Fighter second) {
        phase = CLASHING;
        clashLeft = CLASH_SECONDS;
        clashSecond = second.side;
        taps[0] = 0;
        taps[1] = 0;
        tapped[0] = 0;
        tapped[1] = 0;
        second.meter = 0;
        answer = -1;
        clashes++;
    }

    /** The clash is over: whoever tapped more lets fly her greater ultimate, and the other can only take it. Level, it goes to whoever went second. */
    private void settleClash() {
        int winner = taps[0] > taps[1] ? 0 : taps[1] > taps[0] ? 1 : clashSecond;
        Fighter won = fighters[winner], lost = fighters[1 - winner];
        won.facingLeft = lost.x < won.x;
        won.y = 0;
        won.up = 0;
        begin(won, lost, CLASH);
        lost.state = HIT;
        lost.move = -1;
        lost.clock = 0;
        lost.stunned = 2.4f;
        lost.across = 0;
        lost.y = 0;
        lost.up = 0;
        lost.safe = 0;
        lost.reel = 2;
        lost.meter = 0;
        shots.clear();
        for (Fighter fighter : fighters) fighter.unarmed = false;
    }

    private void attack(Fighter me, Fighter foe, float seconds) {
        Move move = me.doing();
        float way = me.facingLeft ? -1 : 1;
        // Without her bat Tsubakieh has only her hands: half the reach, half the hurt.
        float weak = me.unarmed && me.move != ULTIMATE && me.move != CLASH ? .5f : 1;
        if (me.move == SMASH) {
            smash(me, foe, move, weak, seconds);
            return;
        }
        boolean air = me.inAir();
        if (air) {
            me.x += me.across * seconds;
            if (fall(me, seconds)) {
                // Come down in the middle of it: the blow is over.
                me.state = LAND;
                me.move = -1;
                me.clock = 0;
                me.since = 0;
                return;
            }
        }
        if (me.clock < move.startup) {
            // A heavy move steps in as it comes, and so does each blow of a chain; an ultimate may rush the length of the stage, and stops when it gets there.
            float rush = move.rush > 0 ? move.rush : me.move == HEAVY ? 1.6f : 0;
            if (!air && rush > 0 && Math.abs(foe.x - me.x) > BODY + .3f) me.x += way * rush * seconds;
        } else if (move.shot != NOTHING) {
            if (me.landed == 0) {
                me.landed = 1;
                launch(me, move.shot, air);
            }
        } else {
            float since = me.clock - move.startup;
            boolean open = move.times > 1 ? me.landed < move.times && since >= me.landed * move.every : me.landed == 0 && since <= move.active;
            if (open) {
                float ahead = (foe.x - me.x) * way;
                boolean level = move.anywhere || (air ? Math.abs(foe.y - me.y) < 2.2f : foe.y < 1.5f);
                boolean near = ahead > -.3f && ahead <= move.reach * weak + BODY / 2 && level;
                if (move.times > 1) me.landed++;
                // The last blow of the greater ultimate throws her down.
                boolean floors = move.floors || me.move == CLASH && me.landed == move.times;
                if (near && hurt(foe, me, move.damage * weak, move.knock, move.stun, floors, me.x, move)) {
                    if (move.times == 1) me.landed = 1;
                    me.hitLanded = true;
                }
            }
        }
        boolean over = me.clock >= move.length(), grand = me.move == ULTIMATE || me.move == CLASH;
        // What she pressed in the middle of it she does as soon as she may: at once if the blow landed, or when it is over.
        if (!grand && me.queued != 0 && (over || me.hitLanded && me.clock >= move.startup + move.active)) {
            int step = me.chain, wanted = me.queued;
            if (wanted == 3) {
                if (me.rest[SPECIAL] <= 0) {
                    begin(me, foe, air ? AIR_SPECIAL : SPECIAL);
                    return;
                }
            } else if (air) {
                if (wanted == 2) {
                    begin(me, foe, SMASH);
                    return;
                }
                if (me.airMoves < 3 && me.move >= AIR1 && me.move <= AIR3) {
                    begin(me, foe, AIR1 + me.airMoves);
                    return;
                }
            } else if (step >= 1 && step < 5) {
                begin(me, foe, next(step, wanted == 2));
                return;
            }
        }
        if (over) {
            me.state = air ? JUMP : IDLE;
            me.move = -1;
            me.clock = air ? 1 : 0;
            me.since = 0;
        }
    }

    /** The smash: she hangs for a moment, comes straight down, and whoever is near where she lands is thrown down. If nobody is, she is slow to get up. */
    private void smash(Fighter me, Fighter foe, Move move, float weak, float seconds) {
        if (me.landed == 0) {
            if (me.clock < move.startup) {
                me.x += me.across * seconds;
                me.y += me.up * seconds * .3f;
            } else {
                me.up = -PLUNGE;
                me.x += (me.facingLeft ? -1 : 1) * 2 * seconds;
                me.y += me.up * seconds;
            }
            if (me.y <= 0 && me.clock >= move.startup) {
                me.y = 0;
                me.up = 0;
                me.across = 0;
                me.mark = me.clock;
                quakes++;
                quakeX = me.x;
                boolean near = Math.abs(foe.x - me.x) <= move.reach * weak + BODY / 2 && foe.y < 1.2f;
                me.landed = near && hurt(foe, me, move.damage * weak, move.knock, move.stun, true, me.x, move) ? 1 : 2;
                me.hitLanded = me.landed == 1;
            }
        } else if (me.clock - me.mark >= move.recovery + (me.landed == 2 ? .25f : 0)) {
            me.state = IDLE;
            me.move = -1;
            me.clock = 0;
            me.since = 0;
        }
    }

    /** She throws something: straight ahead from the ground, down and ahead from the air. */
    private void launch(Fighter me, int kind, boolean air) {
        float way = me.facingLeft ? -1 : 1;
        // Every third thing Rune throws is her paper knife.
        if (kind == TEAR && me.specials % 3 == 0) kind = KNIFE;
        Shot shot = new Shot(kind, me.side);
        shot.x = me.x + way * .8f;
        shot.y = me.y + 1.35f;
        float speed;
        switch (kind) {
            case PANEL: speed = 13; shot.left = .5f; break;
            case BREAD: speed = 8.5f; shot.up = 5.5f; shot.left = 2; break;
            case BAT: speed = 13; shot.left = 2.4f; shot.lands = 2; me.unarmed = true; break;
            case TEAR: speed = 8; shot.up = 2.4f; shot.left = 1.7f; break;
            case KNIFE: speed = 17; shot.left = .9f; break;
            case HEART: speed = 14; shot.left = 1; break;
            case ORB: speed = 5.5f; shot.left = 2.4f; break;
            default: speed = 7; shot.y = .6f; shot.left = 2.4f; shot.lands = FLOOD_LANDS; break;
        }
        shot.across = way * speed;
        if (air && kind != FLOOD) {
            // From the air it is aimed at the ground a little way in front of her.
            shot.up = -Math.min(speed * .8f, me.y * speed / 3.5f);
            if (kind == PANEL) shot.left = .8f;
        }
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
                me.juggled = 0;
            }
        } else if (me.state == RISE) {
            if (me.clock >= .3f) {
                me.state = IDLE;
                me.clock = 0;
            }
        } else if (me.state == JUMP || me.state == ATTACK || me.state == DODGE || me.state == LAND || me.state == DASH || me.state == BACKSTEP) {
            // The round ended with her in the middle of something: she comes down and stands.
            me.y = Math.max(0, me.y + me.up * seconds);
            me.up -= GRAVITY * seconds;
            if (me.y <= 0) {
                me.up = 0;
                me.state = IDLE;
                me.move = -1;
            }
        }
        me.x = Math.max(EDGE, Math.min(WIDE - EDGE, me.x));
    }

    /**
     * A blow reaches a fighter. Taken on her guard, from in front, it costs a fifth and only
     * pushes her back, unless it is one that breaks a guard; otherwise it hurts, and stuns
     * her or knocks her down. One who has been thrown into the air can be hit there a few
     * times more. Blow after blow without letting her go, each takes off less than the last.
     * Both meters fill. Returns whether it landed at all.
     */
    private boolean hurt(Fighter her, Fighter by, float damage, float knock, float stun, boolean floors, float from, Move move) {
        if (her.safe > 0 || her.state == RISE || her.state == OUT) return false;
        boolean juggle = her.state == DOWN;
        if (juggle && (her.y <= .25f || her.juggled >= 3)) return false;
        float push = from < her.x ? 1 : -1;
        hitX = her.x;
        hitY = her.y + TALL * .55f;
        hitSide = her.side;
        boolean guarded = (her.state == BLOCK || her.state == JUMP && her.guarding) && her.facingLeft == (from < her.x);
        if (guarded && !(move != null && move.breaks)) {
            her.health = Math.max(1, her.health - damage * .2f);
            her.across = push * knock * .6f;
            her.meter = Math.min(METER, her.meter + 5);
            by.meter = Math.min(METER, by.meter + damage * .5f);
            her.blockAge = 0;
            blocks++;
            hitHard = false;
            return true;
        }
        boolean grand = whole || by.move == ULTIMATE || by.move == CLASH, chained = her.state == HIT || juggle;
        boolean hard = floors || damage >= 12;
        by.combo = chained ? by.combo + 1 : 1;
        if (!grand) damage *= Math.max(.3f, 1 - .17f * (by.combo - 1));
        by.comboDamage = (chained ? by.comboDamage : 0) + damage;
        by.comboAge = 0;
        her.health = Math.max(0, her.health - damage);
        by.meter = Math.min(METER, by.meter + damage * 1.2f);
        her.meter = Math.min(METER, her.meter + damage * .8f);
        hits++;
        hitHard = hard;
        if (!grand) stop = Math.max(stop, hard ? .09f : .04f);
        her.move = -1;
        her.clock = 0;
        her.across = push * knock;
        her.reel = hard ? 2 : hits % 2;
        her.guarding = false;
        her.queued = 0;
        her.chain = 0;
        if (juggle) her.juggled++;
        if (move != null && move.launches && her.health > 0) {
            // Thrown straight up, to be hit again as she comes down.
            her.state = DOWN;
            her.up = 11;
            her.y = Math.max(her.y, .3f);
            her.lain = 0;
            her.across = push * .4f;
            if (!juggle) {
                her.juggled = 0;
                falls++;
            }
        } else if (floors || her.health <= 0 || her.y > .3f) {
            her.state = DOWN;
            her.up = juggle ? 5 : 6.5f;
            her.lain = 0;
            her.across = push * (juggle ? 2 : Math.max(4, knock * .7f));
            if (!juggle) {
                if (her.y <= .3f) her.juggled = 0;
                falls++;
            }
        } else {
            her.state = HIT;
            her.stunned = stun;
        }
        return true;
    }

    /** Keeps the two on the stage, out of each other, and no further apart than the screen is wide. One who dodges may pass through the other. */
    private void apart() {
        Fighter one = fighters[0], other = fighters[1];
        for (Fighter fighter : fighters) fighter.x = Math.max(EDGE, Math.min(WIDE - EDGE, fighter.x));
        float between = other.x - one.x, room = BODY * .95f;
        boolean through = one.state == DODGE || other.state == DODGE;
        if (!through && Math.abs(between) < room && Math.abs(one.y - other.y) < 1.4f) {
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
                // A flood is an ultimate, and takes off all it should however often it lands.
                whole = shot.kind == FLOOD;
                boolean met = hurt(foe, owner, damageOf(shot.kind), heavy ? 7 : 3.5f, heavy ? .34f : .24f, false, shot.x - shot.across * .05f, null);
                whole = false;
                if (met) {
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
            if (!winner.inAir()) {
                winner.state = WON;
                winner.move = -1;
                winner.clock = 0;
            }
        }
        shots.clear();
        for (Fighter fighter : fighters) fighter.unarmed = false;
        phase = ROUND_OVER;
        phaseClock = 0;
        stop = 0;
    }

    // The side that plays itself

    /**
     * What a fighter who plays herself holds down. She makes up her mind every so often, the
     * oftener the better she is: guards or dodges what is coming, closes to the distance she
     * likes or backs off to it, and picks among her moves by what she is. A blow of hers
     * that lands she follows with the next of its chain, the better she is the more surely.
     * The worse she is, the more often she guesses wrong or stands about.
     */
    private Pad think(Fighter me, Fighter foe, float seconds) {
        Pad pad = minds[me.side];
        pad.light = false;
        pad.heavy = false;
        pad.special = false;
        pad.jump = false;
        pad.dodge = false;
        pad.ultimate = false;
        int skill = level[me.side];
        float sharp = new float[]{.25f, .5f, .75f, .95f}[skill];
        float gap = Math.abs(foe.x - me.x);
        // In the middle of a blow of a chain that has landed, the next; coming down on the other from a jump, the smash.
        if (me.state == ATTACK && !me.followed && me.chain >= 1 && me.chain < 5 && me.clock >= me.doing().startup + me.doing().active) {
            me.followed = true;
            // Something thrown has not landed yet: she goes on with it or not as she pleases.
            boolean worth = me.hitLanded || me.doing().shot != NOTHING && random.nextFloat() < .5f;
            if (worth && random.nextFloat() < new float[]{.2f, .45f, .7f, .9f}[skill]) {
                if (random.nextFloat() < .3f) pad.heavy = true;
                else pad.light = true;
            }
            return pad;
        }
        if (me.state == JUMP && !me.followed && me.up < 1 && gap < 2.2f) {
            me.followed = true;
            if (random.nextFloat() < sharp * .6f) {
                if (random.nextFloat() < .6f) pad.heavy = true;
                else pad.light = true;
            }
            return pad;
        }
        if ((thinking[me.side] -= seconds) > 0) return pad;
        thinking[me.side] = new float[]{.5f, .34f, .22f, .13f}[skill] * (.7f + random.nextFloat() * .6f);
        pad.clear();
        float want = WANTS[me.kind];
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
            if (flood || random.nextFloat() < .15f) {
                pad.jump = true;
            } else if (skill >= 2 && me.dodgeRest <= 0 && random.nextFloat() < .3f) {
                // The better ones slip a blow now and then instead of taking it on the guard.
                pad.dodge = true;
                pad.left = !toLeft && random.nextBoolean();
                pad.right = toLeft && !pad.left && random.nextBoolean();
            } else {
                pad.block = true;
            }
        } else if (random.nextFloat() > sharp + .2f) {
            // Standing about.
        } else if (me.charged() && (gap <= ULTIMATE_REACH[me.kind] || pending(foe))) {
            // Her own ultimate when it would reach, or to meet the other's before it lands.
            pad.ultimate = true;
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
                if (foe.state == BLOCK && skill >= 1 && pick < .25f) {
                    // One who only stands behind her guard has it broken.
                    pad.heavy = true;
                    pad.left = !toLeft;
                    pad.right = toLeft;
                } else if (pick < .55f) {
                    pad.light = true;
                } else if (pick < .88f && me.rest[HEAVY] <= 0) {
                    pad.heavy = true;
                } else if (me.rest[SPECIAL] <= 0) {
                    pad.special = true;
                }
            } else if (close && pick < .5f && me.rest[HEAVY] <= 0) {
                pad.heavy = true;
            } else if (skill >= 2 && pick > .9f && gap < 4) {
                // Now and then she comes in over the top.
                pad.jump = true;
                pad.left = toLeft;
                pad.right = !toLeft;
            } else {
                pad.left = toLeft;
                pad.right = !toLeft;
            }
        }
        return pad;
    }
}
