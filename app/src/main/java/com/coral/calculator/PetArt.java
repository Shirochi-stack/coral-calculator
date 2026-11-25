package com.coral.calculator;

/**
 * How the animated Halgakos's frames are laid out in {@code halgakos_pet}: one pose to a row.
 * Keep in step with PET_ROWS in tools/make_art.py.
 */
final class PetArt {
    static final int IDLE = 0, RUN_RIGHT = 1, RUN_LEFT = 2, JUMP = 3, SAD = 4, WORK = 5;
    static final int COLUMNS = 8;
    /** Frames in each row, and how long each of a row's frames is shown. */
    static final int[] FRAMES = {7, 8, 8, 5, 8, 6};
    static final int[] FRAME_MS = {170, 85, 85, 100, 140, 180};

    private PetArt() {}
}
