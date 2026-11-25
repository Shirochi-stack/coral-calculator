package com.coral.calculator;

import java.util.Random;

/** Three in a row against Halgakos: the player marks crosses, she marks noughts. */
final class TicTacToe {
    static final int EMPTY = 0, PLAYER = 1, HALGAKOS = 2, DRAW = 3;
    static final int NO_CELL = -1;
    private static final int[][] LINES = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}};
    private static final int[] CORNERS = {0, 2, 6, 8};

    private final int[] cells = new int[9];

    int at(int cell) { return cells[cell]; }

    /** Marks a cell, unless it is taken or the game is over. */
    boolean place(int cell, int who) {
        if (cell < 0 || cell >= cells.length || cells[cell] != EMPTY || result() != EMPTY) return false;
        cells[cell] = who;
        return true;
    }

    /** Who has won, {@link #DRAW} once the board is full, or {@link #EMPTY} while it is on. */
    int result() {
        int[] line = winningLine();
        if (line != null) return cells[line[0]];
        for (int cell : cells) {
            if (cell == EMPTY) return EMPTY;
        }
        return DRAW;
    }

    /** The three cells that won the game, or null. */
    int[] winningLine() {
        for (int[] line : LINES) {
            int who = cells[line[0]];
            if (who != EMPTY && who == cells[line[1]] && who == cells[line[2]]) return line;
        }
        return null;
    }

    /**
     * The cell Halgakos takes. She always finishes a line of her own, and otherwise stops the
     * player's, takes the middle, then a corner. Now and then she is not paying attention and
     * takes any cell at all, which is what lets her be beaten.
     *
     * @param slipPercent how often, out of a hundred, she is not paying attention
     * @return the cell, or {@link #NO_CELL} if there is none left or the game is over
     */
    int choose(Random random, int slipPercent) {
        if (result() != EMPTY) return NO_CELL;
        int cell = finishing(HALGAKOS);
        if (cell != NO_CELL) return cell;
        if (random.nextInt(100) >= slipPercent) {
            cell = finishing(PLAYER);
            if (cell != NO_CELL) return cell;
            if (cells[4] == EMPTY) return 4;
            cell = anyOf(CORNERS, random);
            if (cell != NO_CELL) return cell;
        }
        return anyOf(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, random);
    }

    /** The empty cell that would complete a line for one side, if there is one. */
    private int finishing(int who) {
        for (int[] line : LINES) {
            int mine = 0, empty = NO_CELL;
            for (int cell : line) {
                if (cells[cell] == who) mine++;
                else if (cells[cell] == EMPTY) empty = cell;
            }
            if (mine == 2 && empty != NO_CELL) return empty;
        }
        return NO_CELL;
    }

    private int anyOf(int[] candidates, Random random) {
        int count = 0;
        for (int cell : candidates) {
            if (cells[cell] == EMPTY) count++;
        }
        if (count == 0) return NO_CELL;
        int pick = random.nextInt(count);
        for (int cell : candidates) {
            if (cells[cell] == EMPTY && pick-- == 0) return cell;
        }
        return NO_CELL;
    }
}
