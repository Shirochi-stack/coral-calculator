package com.coral.calculator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** A picture's clear rows at its bottom are found, so that it is drawn standing on the ground and not over it. */
public class StandingTest {
    /** A picture, so wide and tall, with something drawn in it from one row down to another; the rest clear (or faint). */
    private static int[] picture(int wide, int tall, int from, int to, int faint) {
        int[] pixels = new int[wide * tall];
        for (int y = 0; y < tall; y++) for (int x = 0; x < wide; x++) pixels[y * wide + x] = (y >= from && y <= to && x > 2 && x < wide - 3 ? 0xFF : faint) << 24 | 0x336699;
        return pixels;
    }

    @Test public void theClearRowsAtTheBottomAreFound() {
        // Drawn down to row 89 of 100: ten clear rows.
        assertEquals(10, Standing.clearRows(picture(40, 100, 10, 89, 0), 40, 100));
        // Down to the last row: none.
        assertEquals(0, Standing.clearRows(picture(40, 100, 10, 99, 0), 40, 100));
        // A faint haze under it counts as clear.
        assertEquals(10, Standing.clearRows(picture(40, 100, 10, 89, Standing.FAINT), 40, 100));
        // A single drawn pixel in a row is enough to stop at it.
        int[] pixels = picture(40, 100, 10, 80, 0);
        pixels[95 * 40 + 20] = 0xFF000000;
        assertEquals(4, Standing.clearRows(pixels, 40, 100));
        // Never more than a third of it.
        assertEquals(34, Standing.clearRows(picture(40, 100, 0, 10, 0), 40, 100));
    }

    /** Drawn that much lower, the lowest drawn row lands on the ground's line, within a pixel, whatever the size it is drawn at. */
    @Test public void drawnLowerByThemItsLowestRowIsOnTheGround() {
        int tall = 288, clear = Standing.clearRows(picture(224, tall, 20, 270, 0), 224, tall);
        for (float drawn : new float[]{97.2f, 140.4f, 210.6f, 311.04f}) {
            float ground = 1000, scale = drawn / tall;
            float bottom = ground + drawn * clear / tall;
            // (The lowest drawn row is row 270: under it, the rows down to the last, each a scale tall.)
            float lowestRow = bottom - (tall - 1 - 270) * scale;
            assertEquals(ground, lowestRow, 1);
        }
    }
}
