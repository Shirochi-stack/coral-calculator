package com.coral.calculator;

/**
 * How the animated Halgakos's frames are laid out in {@code halgakos_pet}: one pose to a row.
 * Keep in step with PET_ROWS in tools/make_art.py.
 */
final class PetArt {
    static final int IDLE = 0, RUN_RIGHT = 1, RUN_LEFT = 2, JUMP = 3, SAD = 4, WORK = 5, WAVE = 6;
    /** The first of the two rows of her looking each of {@link #LOOKS} ways, clockwise from straight up. */
    static final int LOOK = 7;
    static final int LOOKS = 16;
    static final int COLUMNS = 8;
    /** Frames in each row, and how long each of a row's frames is shown. */
    static final int[] FRAMES = {7, 8, 8, 5, 8, 6, 4, 8, 8};
    static final int[] FRAME_MS = {170, 85, 85, 100, 140, 180, 150, 110, 110};
    /** No way in particular: she is not looking at anything. */
    static final int NOWHERE = -1;

    private PetArt() {}

    /** The row of the atlas that has her looking a way, and the frame of it. */
    static int lookRow(int way) { return LOOK + way / COLUMNS; }

    static int lookColumn(int way) { return way % COLUMNS; }

    /**
     * The way she looks to face something that lies so far across and so far down from her:
     * 0 for straight up, then clockwise in sixteenths of a turn.
     */
    static int lookAt(float across, float down) {
        double turn = Math.atan2(across, -down) / (2 * Math.PI);
        return (int) Math.floor((turn + 1) * LOOKS + .5) % LOOKS;
    }
}
