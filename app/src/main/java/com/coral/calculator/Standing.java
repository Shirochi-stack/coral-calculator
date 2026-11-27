package com.coral.calculator;

/**
 * What stands on the ground stands on it: a picture's clear rows at its bottom are not part of it, so it is drawn that much lower, its
 * lowest drawn row on the ground's top line. For a sheet of pictures, the rows that are clear in every one of them (so that a picture of
 * someone jumping, whose feet are higher, is not drawn lower than the rest).
 */
final class Standing {
    private Standing() {}

    /** How faint a pixel may be and still count as nothing. */
    static final int FAINT = 24;

    /**
     * How many rows at the bottom of a picture are clear all the way across, from its pixels (ARGB, a row after another), looked for no
     * higher than a third of the way up.
     */
    static int clearRows(int[] pixels, int wide, int tall) {
        int clear = 0;
        for (int y = tall - 1; y >= tall * 2 / 3; y--) {
            boolean drawn = false;
            for (int x = 0; x < wide && !drawn; x++) drawn = pixels[y * wide + x] >>> 24 > FAINT;
            if (drawn) break;
            clear++;
        }
        return clear;
    }
}
