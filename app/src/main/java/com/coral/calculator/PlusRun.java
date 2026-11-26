package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Plus run: a run and jump from one end of a land of signs to the other. Halgakos runs along
 * the ground and over the gaps in it, collects the pluses, keeps clear of spikes, takes to
 * the air off springs, deals with the signs that walk and hop about, and reaches the gate at
 * the far end. Along the way she finds things to fight with, a pen, a knife, a book, a brush,
 * a sword and an ink gun, and some of the signs throw and shoot things of their own. The
 * lands themselves are made by {@link RunLevels}.
 *
 * Everything is measured in squares of the land and in seconds. Across is from the left, and
 * down is from the top. This class knows nothing of screens: it is moved on by {@link #step}.
 */
final class PlusRun {
    static final int PLAYING = 0, WON = 1, LOST = 2;
    static final int HEARTS = 3;
    /** Her size, a little narrower than a square and a little taller. */
    static final float WIDE = .6f, TALL = 1.2f;
    /** How many times Zero has to be landed on. */
    static final int ZERO_HITS = 3;
    /**
     * What she may carry: nothing, or one of six things. A pen and a knife are thrown straight,
     * the knife faster and not as far; a book is lobbed and goes through whatever it meets; a
     * brush and a sword are swung, the brush over her head as well and the sword further, and
     * the sword sends back what is thrown at her; an ink gun fires for as long as its button is
     * held, until it is empty. The last of these numbers is the dot a divide shoots.
     */
    static final int UNARMED = -1, PEN = 0, KNIFE = 1, BOOK = 2, BRUSH = 3, SWORD = 4, GUN = 5, DOT = 6;
    static final int GUN_SHOTS = 16;
    /** How long a swing of the brush or the sword lasts. */
    static final float SWING = .18f;
    /** How long before it throws a sign that throws knives can be seen to raise one. */
    static final float WARNING = .8f;

    private static final float RUN = 6.2f, PUSH = 40, PUSH_IN_AIR = 24, DRAG = 50;
    private static final float GRAVITY = 42, LEAP = 15.5f, SHORT_LEAP = 6, FALL = 22, BOUNCE = 10, SPRING = 22.5f;
    /** A jump still counts this long after she has run off an edge, and this long before she lands. */
    private static final float LATE = .08f, EARLY = .1f;
    private static final float SAFE = 1.2f, STEP = 1 / 60f;
    /** How fast each sign walks, how wide and tall it is, and how high a divide hops. */
    private static final float[] SPEED = {1.6f, 1.3f, 0, 1.9f, 0, 0};
    private static final float[] FOE_WIDE = {.8f, .85f, .8f, 1.8f, .8f, .8f};
    private static final float[] FOE_TALL = {.55f, .85f, .8f, 2.2f, .55f, .8f};
    private static final float HOP_HIGH = 1.9f, HOP_EVERY = 1.3f, STUNNED = 1.1f;
    /** How long each of her things must rest between one use and the next. */
    private static final float[] REST = {.42f, .24f, .75f, .42f, .3f, .15f};
    /** How near she must be for a knife to be thrown at her, how often one is, and how fast it flies; and the same for a dot and for Zero's books. */
    private static final float THROW_REACH = 5.2f, THROW_EVERY = 2.6f, THROWN = 4.2f;
    private static final float DOT_REACH = 6, DOT_SPEED = 6.5f, BOOK_REACH = 9, BOOK_EVERY = 3.4f, BOOK_WEIGHT = 30;

    /** Something flying through the air: hers, or thrown at her. Its place is its middle. */
    static final class Shot {
        final int kind;
        boolean hers;
        float x, y, across, down, left, age;

        Shot(int kind) { this.kind = kind; }
    }

    /** One of the signs in her way: where it is, which way it is going, and how it stands. */
    static final class Foe {
        final int kind;
        float x, y;
        /** Where it stands when it is on the ground: a divide leaves it as it hops. */
        float ground;
        boolean leftward = true, flat;
        /** How long it has been about, how long Zero stays dazed after a landing, and how many more he can take. */
        float clock, dazed;
        int left;
        /** How long until it next throws something, if it is one that does. */
        float wait = 1;

        Foe(int kind) { this.kind = kind; }

        float wide() { return FOE_WIDE[kind]; }
        float tall() { return FOE_TALL[kind]; }
    }

    private final RunLevels.Level level;
    private final int columns;
    private final List<int[]> pluses = new ArrayList<>();
    private final List<Foe> foes = new ArrayList<>();
    /** The things to fight with that still lie about: the kind, column and row of each. */
    private final List<int[]> arms = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final int total;
    private int weapon = UNARMED, rounds, swungWith = UNARMED;
    private float sinceStrike = 1, swing;
    private boolean strikeHeld;
    private int strikes, picked, thrown, turned;
    private float x, y, speedAcross, speedDown;
    private float startX, startY;
    private boolean onGround, facingLeft, jumpHeld;
    /** She is in the air off a spring, and stays so until she lands. */
    private boolean sprung;
    private float sinceGround = 1, sinceJump = 1, safe;
    private int hearts = HEARTS, state = PLAYING;
    private float time;
    /** Counts that only go up, for whoever shows the game to tell what has just happened. */
    private int collected, flattened, hurts, jumps, landings, springs, flagsPassed, blows;
    /** Where the last plus was collected and the last sign flattened: across and down, in squares. */
    private float plusX, plusY, foeX, foeY;

    PlusRun(RunLevels.Level level) {
        this.level = level;
        columns = level.columns;
        pluses.addAll(level.pluses);
        arms.addAll(level.weapons);
        total = pluses.size();
        for (int[] spec : level.foes) {
            Foe foe = new Foe(spec[0]);
            foe.x = spec[1] + (1 - foe.wide()) / 2;
            foe.ground = spec[2] + 1 - foe.tall();
            foe.y = foe.ground;
            foe.left = spec[0] == RunLevels.ZERO ? ZERO_HITS : 1;
            // Each divide starts its hop at a different moment.
            foe.clock = spec[1] * .37f % HOP_EVERY;
            if (spec[0] == RunLevels.ZERO) foe.wait = 2;
            foes.add(foe);
        }
        startX = 2;
        startY = RunLevels.FLOOR - TALL;
        x = startX;
        y = startY;
    }

    private PlusRun(PlusRun other) {
        level = other.level;
        columns = other.columns;
        pluses.addAll(other.pluses);
        total = other.total;
        for (Foe from : other.foes) {
            Foe foe = new Foe(from.kind);
            foe.x = from.x;
            foe.y = from.y;
            foe.ground = from.ground;
            foe.leftward = from.leftward;
            foe.flat = from.flat;
            foe.clock = from.clock;
            foe.dazed = from.dazed;
            foe.left = from.left;
            foe.wait = from.wait;
            foes.add(foe);
        }
        arms.addAll(other.arms);
        for (Shot from : other.shots) {
            Shot shot = new Shot(from.kind);
            shot.hers = from.hers;
            shot.x = from.x;
            shot.y = from.y;
            shot.across = from.across;
            shot.down = from.down;
            shot.left = from.left;
            shot.age = from.age;
            shots.add(shot);
        }
        weapon = other.weapon;
        rounds = other.rounds;
        swungWith = other.swungWith;
        sinceStrike = other.sinceStrike;
        swing = other.swing;
        strikeHeld = other.strikeHeld;
        strikes = other.strikes;
        picked = other.picked;
        thrown = other.thrown;
        turned = other.turned;
        x = other.x;
        y = other.y;
        speedAcross = other.speedAcross;
        speedDown = other.speedDown;
        startX = other.startX;
        startY = other.startY;
        onGround = other.onGround;
        facingLeft = other.facingLeft;
        jumpHeld = other.jumpHeld;
        sprung = other.sprung;
        sinceGround = other.sinceGround;
        sinceJump = other.sinceJump;
        safe = other.safe;
        hearts = other.hearts;
        state = other.state;
        time = other.time;
        collected = other.collected;
        flattened = other.flattened;
        hurts = other.hurts;
        jumps = other.jumps;
        landings = other.landings;
        springs = other.springs;
        flagsPassed = other.flagsPassed;
        blows = other.blows;
    }

    /** This run as it stands, to try something on without touching the run itself. */
    PlusRun copy() { return new PlusRun(this); }

    // What there is to see

    RunLevels.Level level() { return level; }

    int columns() { return columns; }

    char at(int column, int row) {
        return column < 0 || column >= columns || row < 0 || row >= RunLevels.ROWS ? RunLevels.AIR : level.land[row][column];
    }

    static boolean solid(char square) { return square == RunLevels.BLOCK || square == RunLevels.LEDGE; }

    /** The pluses not yet collected: the column and row of each. */
    List<int[]> pluses() { return pluses; }

    List<Foe> foes() { return foes; }

    /** The things to fight with not yet picked up: the kind, column and row of each. */
    List<int[]> arms() { return arms; }

    /** Everything in the air, hers and theirs. */
    List<Shot> shots() { return shots; }

    /** What she carries, and how many shots an ink gun has left in it. */
    int weapon() { return weapon; }
    int rounds() { return rounds; }
    /** How far through a swing of the brush or sword she is, from 0 to 1; below zero when she is not swinging. */
    float swinging() { return swing > 0 ? 1 - swing / SWING : -1; }
    /** How many times she has used what she carries, picked something up, had something thrown at her, and sent or wiped it away. */
    int strikes() { return strikes; }
    int picked() { return picked; }
    int thrown() { return thrown; }
    int turned() { return turned; }

    float x() { return x; }
    float y() { return y; }
    boolean onGround() { return onGround; }
    boolean facingLeft() { return facingLeft; }
    /** Whether she is moving across fast enough to be running. */
    boolean running() { return Math.abs(speedAcross) > .6f; }
    /** Whether she has just been knocked back, and cannot be again for a moment. */
    boolean safe() { return safe > 0; }
    int hearts() { return hearts; }
    int state() { return state; }
    float time() { return time; }
    int total() { return total; }
    int collected() { return collected; }
    int flattened() { return flattened; }
    int hurts() { return hurts; }
    /** How many times she has left the ground by jumping, come down onto it again, and been thrown up by a spring. */
    int jumps() { return jumps; }
    int landings() { return landings; }
    int springs() { return springs; }
    /** How many of the land's flags she has passed, and how many times Zero has been landed on. */
    int flagsPassed() { return flagsPassed; }
    int blows() { return blows; }
    /** How fast she is moving down, in squares a second: below zero while she rises. */
    float speedDown() { return speedDown; }
    float speedAcross() { return speedAcross; }
    /** The middle of the plus last collected, and where the sign last flattened stood. */
    float plusX() { return plusX; }
    float plusY() { return plusY; }
    float foeX() { return foeX; }
    float foeY() { return foeY; }

    /** Whether the gate will let her through: always, but for the land where Zero stands in front of it. */
    boolean gateOpen() {
        if (!level.guarded) return true;
        for (Foe foe : foes) if (foe.kind == RunLevels.ZERO && !foe.flat) return false;
        return true;
    }

    /** Puts her somewhere, standing still: for checking the rules from a place of one's choosing. */
    void place(float across, float down) {
        x = across;
        y = down;
        speedAcross = 0;
        speedDown = 0;
        onGround = false;
    }

    // Moving it on

    /**
     * Moves the game on by a length of time, with the arrows and the jump button held or not.
     * A long time is taken in small steps, so nothing passes through anything.
     */
    void step(float seconds, boolean left, boolean right, boolean jump) { step(seconds, left, right, jump, false); }

    /** The same, with the button that uses what she carries held or not. */
    void step(float seconds, boolean left, boolean right, boolean jump, boolean strike) {
        seconds = Math.min(seconds, .1f);
        while (seconds > 0 && state == PLAYING) {
            float slice = Math.min(STEP, seconds);
            once(slice, left, right, jump, strike);
            seconds -= slice;
        }
    }

    private void once(float seconds, boolean left, boolean right, boolean jump, boolean strike) {
        time += seconds;
        if (safe > 0) safe -= seconds;
        sinceGround = onGround ? 0 : sinceGround + seconds;
        sinceJump = jump && !jumpHeld ? 0 : sinceJump + seconds;

        // Across: she speeds up toward the arrow that is held, and slows when neither is.
        float wanted = left == right ? 0 : left ? -RUN : RUN;
        float push = (wanted == 0 ? DRAG : onGround ? PUSH : PUSH_IN_AIR) * seconds;
        speedAcross = speedAcross < wanted ? Math.min(wanted, speedAcross + push) : Math.max(wanted, speedAcross - push);
        if (wanted != 0) facingLeft = wanted < 0;

        // Up: a press of the button a moment early, or a moment after the edge, still jumps.
        if (sinceJump <= EARLY && sinceGround <= LATE) {
            speedDown = -LEAP;
            sinceJump = 1;
            sinceGround = 1;
            onGround = false;
            jumps++;
        }
        // Letting go early makes for a lower jump, but not for a lower flight off a spring.
        if (!jump && !sprung && speedDown < -SHORT_LEAP) speedDown = -SHORT_LEAP;
        jumpHeld = jump;
        speedDown = Math.min(FALL, speedDown + GRAVITY * seconds);

        moveAcross(speedAcross * seconds);
        moveDown(speedDown * seconds);

        for (int index = pluses.size() - 1; index >= 0; index--) {
            int[] plus = pluses.get(index);
            if (x < plus[0] + .8f && x + WIDE > plus[0] + .2f && y < plus[1] + .8f && y + TALL > plus[1] + .2f) {
                pluses.remove(index);
                collected++;
                plusX = plus[0] + .5f;
                plusY = plus[1] + .5f;
            }
        }
        // A thing to fight with is picked up by touching it, in place of whatever she carried.
        for (int index = arms.size() - 1; index >= 0; index--) {
            int[] arm = arms.get(index);
            if (x < arm[1] + .85f && x + WIDE > arm[1] + .15f && y < arm[2] + .9f && y + TALL > arm[2] + .1f) {
                arms.remove(index);
                weapon = arm[0];
                rounds = weapon == GUN ? GUN_SHOTS : 0;
                picked++;
            }
        }
        // A press of the button uses it, once it has rested; the ink gun goes on for as long as the button is held.
        sinceStrike += seconds;
        if (swing > 0) swing -= seconds;
        if (weapon != UNARMED && strike && (!strikeHeld || weapon == GUN) && sinceStrike >= REST[weapon]) use();
        strikeHeld = strike;
        // What she is standing in: a spring throws her up, and spikes cost a heart.
        int feetRow = (int) Math.floor(y + TALL - .05f);
        for (int column = (int) Math.floor(x + .08f); column <= (int) Math.floor(x + WIDE - .08f); column++) {
            char here = at(column, feetRow);
            if (here == RunLevels.SPRING && speedDown >= 0 && y + TALL >= feetRow + .45f) {
                speedDown = -SPRING;
                onGround = false;
                sprung = true;
                springs++;
            } else if (here == RunLevels.SPIKE && y + TALL >= feetRow + .5f && safe <= 0) {
                hurt();
                return;
            }
        }
        for (Foe foe : foes) move(foe, seconds);
        if (swing > 0) swung();
        if (fly(seconds)) return;
        for (Foe foe : foes) {
            if (foe.flat || x >= foe.x + foe.wide() || x + WIDE <= foe.x || y >= foe.y + foe.tall() || y + TALL <= foe.y) continue;
            // Coming down onto a sign deals with it and sends her up again; any other touch knocks her back.
            // A times has a spike on every arm and cannot be landed on at all.
            boolean onto = speedDown > 0 && y + TALL - speedDown * seconds <= foe.y + foe.tall() * .6f;
            if (onto && foe.kind != RunLevels.TIMES && foe.dazed <= 0) {
                speedDown = -BOUNCE;
                beat(foe);
            } else if (safe <= 0 && foe.dazed <= 0) {
                hurt();
                return;
            }
        }
        if (y > RunLevels.ROWS + 1) {
            hurt();
            return;
        }
        // A flag she has passed, at any height, is where she comes back to; passing the gate is the end.
        float middle = x + WIDE / 2;
        for (int flag : level.flags) {
            if (middle >= flag && startX < flag) {
                startX = flag + (1 - WIDE) / 2;
                flagsPassed++;
            }
        }
        if (middle >= level.gate && gateOpen()) state = WON;
    }

    /** A sign is landed on, struck or hit: all but Zero go flat at once, and he is dazed until he has taken enough. */
    private void beat(Foe foe) {
        foeX = foe.x + foe.wide() / 2;
        foeY = foe.y + foe.tall();
        if (--foe.left <= 0) {
            foe.flat = true;
            foe.y = foe.ground;
            flattened++;
        } else {
            foe.dazed = STUNNED;
        }
        if (foe.kind == RunLevels.ZERO) blows++;
    }

    /** She uses what she carries: throws it, fires it, or begins a swing of it. */
    private void use() {
        sinceStrike = 0;
        strikes++;
        float way = facingLeft ? -1 : 1, fromX = x + WIDE / 2 + way * .5f, fromY = y + TALL * .45f;
        if (weapon == PEN) {
            shoot(PEN, true, fromX, fromY, way * 13, 0, 1);
        } else if (weapon == KNIFE) {
            shoot(KNIFE, true, fromX, fromY, way * 19, 0, .5f);
        } else if (weapon == BOOK) {
            shoot(BOOK, true, fromX, fromY - .3f, way * 7.5f + speedAcross * .4f, -11, 3);
        } else if (weapon == GUN) {
            shoot(GUN, true, fromX, fromY, way * 22, 0, .9f);
            if (--rounds <= 0) weapon = UNARMED;
        } else {
            swing = SWING;
            swungWith = weapon;
        }
    }

    private void shoot(int kind, boolean hers, float fromX, float fromY, float across, float down, float lasting) {
        Shot shot = new Shot(kind);
        shot.hers = hers;
        shot.x = fromX;
        shot.y = fromY;
        shot.across = across;
        shot.down = down;
        shot.left = lasting;
        shots.add(shot);
    }

    /**
     * What a swing reaches: the signs in front of her, and whatever has been thrown at her.
     * The brush reaches over her head and a little behind, and wipes away what is thrown; the
     * sword reaches further in front, and sends it back the way it came.
     */
    private void swung() {
        boolean brush = swungWith == BRUSH;
        float ahead = brush ? 1.3f : 1.8f, behind = brush ? .3f : 0, top = y - (brush ? .9f : .15f), bottom = y + TALL;
        float from = facingLeft ? x - ahead : x - behind, to = facingLeft ? x + WIDE + behind : x + WIDE + ahead;
        for (Foe foe : foes) {
            if (foe.flat || foe.dazed > 0 || foe.x >= to || foe.x + foe.wide() <= from || foe.y >= bottom || foe.y + foe.tall() <= top) continue;
            beat(foe);
        }
        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            if (shot.hers || shot.x < from - .2f || shot.x > to + .2f || shot.y < top - .2f || shot.y > bottom + .2f) continue;
            turned++;
            if (brush) {
                shots.remove(index);
            } else {
                shot.hers = true;
                shot.across = (facingLeft ? -1 : 1) * Math.max(9, Math.abs(shot.across) * 1.6f);
                shot.down = Math.min(shot.down, 0);
                shot.left = 1;
            }
        }
    }

    /** Moves on everything in the air, and sees what it has met. Returns whether she was hurt by it. */
    private boolean fly(float seconds) {
        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            shot.x += shot.across * seconds;
            shot.y += shot.down * seconds;
            shot.age += seconds;
            if (shot.kind == BOOK) shot.down += BOOK_WEIGHT * seconds;
            if ((shot.left -= seconds) <= 0 || shot.x < 0 || shot.x > columns || shot.y > RunLevels.ROWS
                    || solid(at((int) Math.floor(shot.x), (int) Math.floor(shot.y)))) {
                shots.remove(index);
            } else if (shot.hers) {
                for (Foe foe : foes) {
                    if (foe.flat || shot.x < foe.x - .2f || shot.x > foe.x + foe.wide() + .2f || shot.y < foe.y - .2f
                            || shot.y > foe.y + foe.tall() + .2f) continue;
                    // Nothing thrown gets through Zero: anything times nothing is nothing.
                    if (foe.kind == RunLevels.ZERO) {
                        shots.remove(index);
                    } else if (foe.dazed <= 0) {
                        beat(foe);
                        // A book goes on through.
                        if (shot.kind != BOOK) shots.remove(index);
                    }
                    break;
                }
            } else if (safe <= 0 && shot.x > x - .15f && shot.x < x + WIDE + .15f && shot.y > y + .05f && shot.y < y + TALL) {
                shots.remove(index);
                hurt();
                return true;
            }
        }
        return false;
    }

    private void hurt() {
        hurts++;
        if (--hearts <= 0) {
            state = LOST;
            return;
        }
        // Whatever was in the air is gone by the time she is back.
        shots.clear();
        swing = 0;
        x = startX;
        y = startY;
        speedAcross = 0;
        speedDown = 0;
        safe = SAFE;
    }

    private void moveAcross(float by) {
        x += by;
        if (x < 0) {
            x = 0;
            speedAcross = 0;
        }
        if (x > columns - WIDE) {
            x = columns - WIDE;
            speedAcross = 0;
        }
        // A gate that is still shut stops her like a wall.
        if (!gateOpen() && x + WIDE > level.gate) {
            x = level.gate - WIDE;
            speedAcross = 0;
        }
        int top = (int) Math.floor(y), bottom = (int) Math.floor(y + TALL - .001f);
        if (by > 0) {
            int column = (int) Math.floor(x + WIDE);
            for (int row = top; row <= bottom; row++) {
                if (solid(at(column, row))) {
                    x = column - WIDE - .001f;
                    speedAcross = 0;
                    break;
                }
            }
        } else if (by < 0) {
            int column = (int) Math.floor(x);
            for (int row = top; row <= bottom; row++) {
                if (solid(at(column, row))) {
                    x = column + 1 + .001f;
                    speedAcross = 0;
                    break;
                }
            }
        }
    }

    private void moveDown(float by) {
        y += by;
        boolean wasOnGround = onGround;
        float fell = speedDown;
        onGround = false;
        int left = (int) Math.floor(x), right = (int) Math.floor(x + WIDE - .001f);
        if (by > 0) {
            int row = (int) Math.floor(y + TALL);
            for (int column = left; column <= right; column++) {
                if (solid(at(column, row))) {
                    y = row - TALL;
                    speedDown = 0;
                    onGround = true;
                    sprung = false;
                    // Coming down from a height counts as a landing; standing on the ground does not.
                    if (!wasOnGround && fell > 4) landings++;
                    break;
                }
            }
        } else if (by < 0) {
            int row = (int) Math.floor(y);
            for (int column = left; column <= right; column++) {
                if (solid(at(column, row))) {
                    y = row + 1 + .001f;
                    speedDown = 0;
                    break;
                }
            }
        }
    }

    /**
     * A minus, a times and Zero walk until they meet a wall or the end of the ground under
     * them, and then turn. A divide hops on the spot. Zero stands dazed for a moment after
     * each landing, and walks faster for every one he has taken; and when she keeps her
     * distance he lobs a book at her. A minus with a knife stands where it is and throws
     * knives along the ground at her, to be jumped. A divide that shoots fires one of its dots
     * from the top of every other hop, over her head if she keeps to the ground.
     */
    private void move(Foe foe, float seconds) {
        if (foe.flat) return;
        foe.clock += seconds;
        if (foe.dazed > 0) {
            foe.dazed -= seconds;
            return;
        }
        float middle = foe.x + foe.wide() / 2, away = Math.abs(x + WIDE / 2 - middle);
        boolean toLeft = x + WIDE / 2 < middle;
        if (foe.kind == RunLevels.DIVIDE || foe.kind == RunLevels.GUNNER) {
            float before = (foe.clock - seconds) % HOP_EVERY / HOP_EVERY, turn = foe.clock % HOP_EVERY / HOP_EVERY;
            foe.y = foe.ground - (float) Math.sin(turn * Math.PI) * HOP_HIGH;
            if (foe.kind == RunLevels.GUNNER) {
                foe.leftward = toLeft;
                if (before < .5f && turn >= .5f && (int) (foe.clock / HOP_EVERY) % 2 == 0 && away < DOT_REACH) {
                    shoot(DOT, false, middle, foe.y + .25f, toLeft ? -DOT_SPEED : DOT_SPEED, 0, 1.2f);
                    thrown++;
                }
            }
            return;
        }
        if (foe.kind == RunLevels.THROWER) {
            foe.leftward = toLeft;
            // It holds its knife until she is near enough to throw it at.
            foe.wait = Math.max(foe.wait - seconds, away < THROW_REACH + 1.5f ? -1 : WARNING);
            if (foe.wait <= 0 && away < THROW_REACH && Math.abs(y + TALL - foe.y - foe.tall()) < 2.5f) {
                foe.wait = THROW_EVERY;
                shoot(KNIFE, false, middle, foe.y + foe.tall() - .45f, toLeft ? -THROWN : THROWN, 0, 1.9f);
                thrown++;
            }
            return;
        }
        if (foe.kind == RunLevels.ZERO) {
            foe.wait -= seconds;
            if (foe.wait <= 0 && away > 3 && away < BOOK_REACH) {
                foe.wait = BOOK_EVERY;
                shoot(BOOK, false, middle, foe.y + .3f, Math.max(-9, Math.min(9, (x + WIDE / 2 - middle) / .78f)), -10, 4);
                thrown++;
            }
        }
        float speed = SPEED[foe.kind] * (foe.kind == RunLevels.ZERO ? 1 + (ZERO_HITS - foe.left) * .45f : 1);
        float ahead = foe.leftward ? foe.x - speed * seconds : foe.x + foe.wide() + speed * seconds;
        int column = (int) Math.floor(ahead), row = (int) Math.floor(foe.ground + foe.tall() - .001f);
        if (ahead < 0 || ahead > columns || solid(at(column, row)) || !solid(at(column, row + 1))
                || at(column, row) == RunLevels.SPIKE) {
            foe.leftward = !foe.leftward;
        } else {
            foe.x += (foe.leftward ? -1 : 1) * speed * seconds;
        }
    }
}
