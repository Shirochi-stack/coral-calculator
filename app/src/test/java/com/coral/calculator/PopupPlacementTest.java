package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PopupPlacementTest {
    private static final int GAP = 8;

    /** The popup's two edges never reach across her, whichever side it took. */
    private static void assertClear(int x, int width, int left, int right) {
        assertTrue("popup at " + x + " covers her at " + left + ".." + right, x + width <= left || x >= right);
    }

    @Test public void standsOnThePreferredSideWhenItFits() {
        assertEquals(208, PopupPlacement.beside(100, 200, 400, GAP, 16, 1984, true));
        assertEquals(1392, PopupPlacement.beside(1800, 1900, 400, GAP, 16, 1984, false));
    }

    @Test public void takesTheOtherSideRatherThanCoverHer() {
        // She stands just right of the middle of a wide screen, and the popup is kept to
        // her half of it: the room is on her right, not toward the middle.
        int left = 1100, right = 1250, width = 485;
        int x = PopupPlacement.beside(left, right, width, GAP, 1016, 1984, false);
        assertEquals(right + GAP, x);
        assertClear(x, width, left, right);
    }

    @Test public void reportsWhenNeitherSideHasRoom() {
        // In the middle of a phone, with a popup wider than either side of her.
        assertEquals(PopupPlacement.NOWHERE, PopupPlacement.beside(450, 630, 640, GAP, 24, 1056, true));
        // Kept to a half she all but fills.
        assertEquals(PopupPlacement.NOWHERE, PopupPlacement.beside(1100, 1250, 800, GAP, 1016, 1984, false));
    }

    @Test public void fitsOnceSheHasSteppedAside() {
        int width = 640, figure = 180, spanLeft = 24, spanRight = 1056;
        int left = PopupPlacement.aside(450, 630, width, GAP, spanLeft, spanRight, true);
        int x = PopupPlacement.beside(left, left + figure, width, GAP, spanLeft, spanRight, true);
        assertEquals(spanRight - width, x);
        assertClear(x, width, left, left + figure);

        left = PopupPlacement.aside(450, 630, width, GAP, spanLeft, spanRight, false);
        x = PopupPlacement.beside(left, left + figure, width, GAP, spanLeft, spanRight, false);
        assertEquals(spanLeft, x);
        assertClear(x, width, left, left + figure);
    }
}
