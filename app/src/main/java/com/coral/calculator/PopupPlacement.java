package com.coral.calculator;

/**
 * Where a popup of the companion's stands: to her left or to her right, never over her.
 * Everything here is measured along the screen's width, in pixels.
 */
final class PopupPlacement {
    /** Neither side of her has room for the popup. */
    static final int NOWHERE = Integer.MIN_VALUE;

    private PopupPlacement() {}

    /**
     * The left edge of a popup standing beside her, inside a span of the screen.
     *
     * @param left       her left edge
     * @param right      her right edge
     * @param width      the popup's width
     * @param gap        the space kept between her and the popup
     * @param spanLeft   where the span the popup must stay inside begins
     * @param spanRight  where it ends
     * @param afterFirst whether her right side is tried before her left
     * @return the popup's left edge, or {@link #NOWHERE} if it fits on neither side
     */
    static int beside(int left, int right, int width, int gap, int spanLeft, int spanRight, boolean afterFirst) {
        int after = right + gap, before = left - gap - width;
        boolean afterFits = after >= spanLeft && after + width <= spanRight;
        boolean beforeFits = before >= spanLeft && before + width <= spanRight;
        if (afterFirst ? afterFits : beforeFits) return afterFirst ? after : before;
        if (afterFirst ? beforeFits : afterFits) return afterFirst ? before : after;
        return NOWHERE;
    }

    /**
     * Where her left edge has to be for a popup that fits on neither side to fit on one: she
     * steps toward one end of the span, and the popup takes the other.
     *
     * @param towardLeft whether she steps toward the span's beginning
     */
    static int aside(int left, int right, int width, int gap, int spanLeft, int spanRight, boolean towardLeft) {
        return towardLeft ? spanRight - width - gap - (right - left) : spanLeft + width + gap;
    }
}
