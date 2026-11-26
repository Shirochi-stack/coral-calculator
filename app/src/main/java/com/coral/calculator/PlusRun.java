package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Plus run: a run and jump from one end of a land of signs to the other. Halgakos runs along
 * the ground and over the gaps in it, collects the pluses, lands on the minuses that walk
 * about or is knocked back by them, and reaches the gate at the far end.
 *
 * Everything is measured in squares of the land and in seconds. Across is from the left, and
 * down is from the top. This class knows nothing of screens: it is moved on by {@link #step}.
 */
final class PlusRun {
    static final int PLAYING = 0, WON = 1, LOST = 2;
    static final char AIR = ' ', BLOCK = '#', LEDGE = '=', GATE = 'G', FLAG = 'f';
    static final int COLUMNS = 118, ROWS = 14;
    /** The row the ground's top squares are in. */
    static final int FLOOR = 12;
    static final int HEARTS = 3;
    /** The columns of the flags she comes back to once she has passed them, and of the gate that ends the run. */
    private static final int[] FLAGS = {47, 96};
    static final int GATE_COLUMN = 112;
    /** Her size, a little narrower than a square and a little taller. */
    static final float WIDE = .6f, TALL = 1.2f;
    /** A minus's size. */
    static final float MINUS_WIDE = .8f, MINUS_TALL = .55f;

    private static final float RUN = 6.2f, PUSH = 40, PUSH_IN_AIR = 24, DRAG = 50;
    private static final float GRAVITY = 42, LEAP = 15.5f, SHORT_LEAP = 6, FALL = 22, BOUNCE = 10;
    /** A jump still counts this long after she has run off an edge, and this long before she lands. */
    private static final float LATE = .08f, EARLY = .1f;
    private static final float MINUS_SPEED = 1.6f, SAFE = 1.2f, STEP = 1 / 60f;

    /** A minus: where it is, which way it is walking, and whether it has been landed on. */
    static final class Minus {
        float x, y;
        boolean leftward = true, flat;
    }

    private final char[][] land = new char[ROWS][COLUMNS];
    private final List<int[]> pluses = new ArrayList<>();
    private final List<Minus> minuses = new ArrayList<>();
    private final int total;
    private float x, y, speedAcross, speedDown;
    private float startX, startY;
    private boolean onGround, facingLeft, jumpHeld;
    private float sinceGround = 1, sinceJump = 1, safe;
    private int hearts = HEARTS, state = PLAYING;
    private float time;
    /** Counts that only go up, for whoever shows the game to tell what has just happened. */
    private int collected, flattened, hurts, jumps, landings;
    /** Where the last plus was collected and the last minus flattened: across and down, in squares. */
    private float plusX, plusY, minusX, minusY;

    PlusRun() {
        for (char[] row : land) java.util.Arrays.fill(row, AIR);
        // The ground, with the gaps in it.
        ground(0, 15);
        ground(19, 34);
        ground(37, 51);
        ground(73, 90);
        ground(94, COLUMNS - 1);
        // Steps before the first gap.
        block(12, 1);
        block(13, 1);
        block(14, 2);
        block(15, 2);
        // A ledge to climb to, over the first minus, and a block that keeps it from the gap beyond.
        ledge(22, 9, 4);
        block(27, 1);
        // A staircase up and a drop down.
        block(41, 1);
        block(42, 2);
        block(43, 3);
        for (int flag : FLAGS) land[FLOOR - 1][flag] = FLAG;
        // Three ledges over the long gap.
        ledge(54, 11, 4);
        ledge(60, 9, 3);
        ledge(65, 10, 6);
        // A wall in the next stretch, and a ledge over it.
        block(79, 2);
        block(80, 2);
        ledge(82, 9, 3);
        // The last stairs, and the gate.
        block(104, 1);
        block(105, 2);
        block(106, 3);
        land[FLOOR - 1][GATE_COLUMN] = GATE;
        land[FLOOR - 2][GATE_COLUMN] = GATE;

        row(5, 9, 10);
        row(14, 15, 8);
        row(16, 18, 8);
        row(22, 25, 8);
        row(28, 29, 11);
        row(35, 36, 9);
        row(43, 44, 7);
        row(54, 57, 10);
        row(60, 62, 8);
        row(65, 70, 9);
        row(75, 77, 11);
        row(82, 84, 8);
        row(87, 89, 11);
        row(91, 93, 9);
        row(97, 100, 11);
        row(106, 108, 7);
        total = pluses.size();

        minus(23);
        minus(76);
        minus(98);
        minus(102);

        startX = 2;
        startY = FLOOR - TALL;
        x = startX;
        y = startY;
    }

    private void ground(int from, int to) {
        for (int column = from; column <= to; column++) {
            for (int row = FLOOR; row < ROWS; row++) land[row][column] = BLOCK;
        }
    }

    /** A pile of blocks standing on the ground. */
    private void block(int column, int high) {
        for (int up = 1; up <= high; up++) land[FLOOR - up][column] = BLOCK;
    }

    private void ledge(int column, int row, int length) {
        for (int along = 0; along < length; along++) land[row][column + along] = LEDGE;
    }

    private void row(int from, int to, int row) {
        for (int column = from; column <= to; column++) pluses.add(new int[]{column, row});
    }

    private void minus(int column) {
        Minus minus = new Minus();
        minus.x = column + (1 - MINUS_WIDE) / 2;
        minus.y = FLOOR - MINUS_TALL;
        minuses.add(minus);
    }

    // What there is to see

    char at(int column, int row) {
        return column < 0 || column >= COLUMNS || row < 0 || row >= ROWS ? AIR : land[row][column];
    }

    static boolean solid(char square) { return square == BLOCK || square == LEDGE; }

    /** The pluses not yet collected: the column and row of each. */
    List<int[]> pluses() { return pluses; }

    List<Minus> minuses() { return minuses; }

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
    /** How many times she has left the ground by jumping, and come down onto it again. */
    int jumps() { return jumps; }
    int landings() { return landings; }
    /** How fast she is moving down, in squares a second: below zero while she rises. */
    float speedDown() { return speedDown; }
    float speedAcross() { return speedAcross; }
    /** The middle of the plus last collected, and of the minus last flattened. */
    float plusX() { return plusX; }
    float plusY() { return plusY; }
    float minusX() { return minusX; }
    float minusY() { return minusY; }

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
    void step(float seconds, boolean left, boolean right, boolean jump) {
        seconds = Math.min(seconds, .1f);
        while (seconds > 0 && state == PLAYING) {
            float slice = Math.min(STEP, seconds);
            once(slice, left, right, jump);
            seconds -= slice;
        }
    }

    private void once(float seconds, boolean left, boolean right, boolean jump) {
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
        // Letting go early makes for a lower jump.
        if (!jump && speedDown < -SHORT_LEAP) speedDown = -SHORT_LEAP;
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
        for (Minus minus : minuses) walk(minus, seconds);
        for (int index = minuses.size() - 1; index >= 0; index--) {
            Minus minus = minuses.get(index);
            if (minus.flat || x >= minus.x + MINUS_WIDE || x + WIDE <= minus.x || y >= minus.y + MINUS_TALL || y + TALL <= minus.y) continue;
            // Coming down onto it flattens it and sends her up again; any other touch knocks her back.
            if (speedDown > 0 && y + TALL - speedDown * seconds <= minus.y + MINUS_TALL * .6f) {
                minus.flat = true;
                flattened++;
                minusX = minus.x + MINUS_WIDE / 2;
                minusY = minus.y + MINUS_TALL;
                speedDown = -BOUNCE;
            } else if (safe <= 0) {
                hurt();
                return;
            }
        }
        if (y > ROWS + 1) {
            hurt();
            return;
        }
        // A flag she has passed, at any height, is where she comes back to; passing the gate is the end.
        float middle = x + WIDE / 2;
        for (int flag : FLAGS) {
            if (middle >= flag && startX < flag) startX = flag + (1 - WIDE) / 2;
        }
        if (middle >= GATE_COLUMN) state = WON;
    }

    private void hurt() {
        hurts++;
        if (--hearts <= 0) {
            state = LOST;
            return;
        }
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
        if (x > COLUMNS - WIDE) {
            x = COLUMNS - WIDE;
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

    /** A minus walks until it meets a wall or the end of the ground under it, and then turns. */
    private void walk(Minus minus, float seconds) {
        if (minus.flat) return;
        float ahead = minus.leftward ? minus.x - MINUS_SPEED * seconds : minus.x + MINUS_WIDE + MINUS_SPEED * seconds;
        int column = (int) Math.floor(ahead), row = (int) Math.floor(minus.y + MINUS_TALL - .001f);
        if (ahead < 0 || ahead > COLUMNS || solid(at(column, row)) || !solid(at(column, row + 1))) {
            minus.leftward = !minus.leftward;
        } else {
            minus.x += (minus.leftward ? -1 : 1) * MINUS_SPEED * seconds;
        }
    }
}
