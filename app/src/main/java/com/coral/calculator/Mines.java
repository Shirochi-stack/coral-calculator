package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A field of minesweeper. Squares are numbered across each row from the top. The mines are
 * laid when the first square is opened, clear of that square and the ones round it, so the
 * first touch never ends the game and always opens some room.
 */
final class Mines {
    static final int PLAYING = 0, WON = 1, LOST = 2;
    static final int NONE = -1;

    final int columns, rows, mines;
    private final boolean[] mine, open, flag;
    private final int[] near;
    private boolean laid;
    private int state = PLAYING;
    private int opened, flags;
    private int struck = NONE;

    /**
     * @param mines how many to lay; no more than leaves the first square and those round it
     *              clear, and at least one
     */
    Mines(int columns, int rows, int mines) {
        this.columns = Math.max(2, columns);
        this.rows = Math.max(2, rows);
        int squares = this.columns * this.rows;
        this.mines = Math.max(1, Math.min(mines, squares - Math.min(9, squares - 1)));
        mine = new boolean[squares];
        open = new boolean[squares];
        flag = new boolean[squares];
        near = new int[squares];
    }

    /** How many mines a field of a size is given: about one square in six and a half. */
    static int fair(int columns, int rows) { return Math.max(1, Math.round(columns * rows * .155f)); }

    int state() { return state; }

    boolean isOpen(int square) { return open[square]; }

    boolean isFlagged(int square) { return flag[square]; }

    /** Whether there is a mine under a square. Nothing is known of this before the first square is opened. */
    boolean isMine(int square) { return mine[square]; }

    /** How many of the squares round a square have mines under them. */
    int near(int square) { return near[square]; }

    /** Mines not yet accounted for by a flag; less than none if too many flags are planted. */
    int left() { return mines - flags; }

    /** The mine that was opened, once the game is lost, or {@link #NONE}. */
    int struck() { return struck; }

    /** Plants a flag on a square that is not open, or takes it off again. Says whether anything changed. */
    boolean flag(int square) {
        if (state != PLAYING || open[square]) return false;
        flag[square] = !flag[square];
        flags += flag[square] ? 1 : -1;
        return true;
    }

    /**
     * Opens a square. A flagged square is left alone. A square with no mines round it opens
     * the squares round it too, and so on outward. An open square that has as many flags
     * round it as its number opens the rest of the squares round it.
     *
     * @return how many squares were opened
     */
    int open(int square, Random random) {
        if (state != PLAYING || flag[square]) return 0;
        if (!laid) lay(square, random);
        int before = opened;
        if (open[square]) {
            int flagged = 0;
            List<Integer> round = round(square);
            for (int other : round) if (flag[other]) flagged++;
            if (near[square] == 0 || flagged != near[square]) return 0;
            for (int other : round) if (!flag[other] && !open[other]) dig(other);
        } else {
            dig(square);
        }
        if (state == PLAYING && opened == mine.length - mines) {
            state = WON;
            // Every mine is as good as flagged: nothing else is left shut.
            for (int index = 0; index < mine.length; index++) {
                if (mine[index] && !flag[index]) {
                    flag[index] = true;
                    flags++;
                }
            }
        }
        return opened - before + (state == LOST ? 1 : 0);
    }

    private void dig(int square) {
        if (state != PLAYING) return;
        if (mine[square]) {
            state = LOST;
            struck = square;
            return;
        }
        List<Integer> waiting = new ArrayList<>();
        waiting.add(square);
        while (!waiting.isEmpty()) {
            int next = waiting.remove(waiting.size() - 1);
            if (open[next] || flag[next]) continue;
            open[next] = true;
            opened++;
            if (near[next] == 0) waiting.addAll(round(next));
        }
    }

    private void lay(int first, Random random) {
        laid = true;
        List<Integer> clear = round(first);
        clear.add(first);
        List<Integer> free = new ArrayList<>();
        for (int square = 0; square < mine.length; square++) if (!clear.contains(square)) free.add(square);
        // A field too small to keep the squares round the first one clear keeps only the first.
        if (free.size() < mines) {
            free.clear();
            for (int square = 0; square < mine.length; square++) if (square != first) free.add(square);
        }
        for (int laidSoFar = 0; laidSoFar < mines && !free.isEmpty(); laidSoFar++) {
            mine[free.remove(random.nextInt(free.size()))] = true;
        }
        for (int square = 0; square < mine.length; square++) {
            int count = 0;
            for (int other : round(square)) if (mine[other]) count++;
            near[square] = count;
        }
    }

    /** The squares that touch a square, up to eight of them. */
    private List<Integer> round(int square) {
        List<Integer> round = new ArrayList<>(8);
        int row = square / columns, column = square % columns;
        for (int down = -1; down <= 1; down++) {
            for (int across = -1; across <= 1; across++) {
                int r = row + down, c = column + across;
                if ((down != 0 || across != 0) && r >= 0 && r < rows && c >= 0 && c < columns) round.add(r * columns + c);
            }
        }
        return round;
    }
}
