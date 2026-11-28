package com.coral.calculator;

import android.view.KeyEvent;

/**
 * Page brawl's keys, for a keyboard (on a computer, or a phone with one): which of the
 * buttons on the screen each key stands for, what it does on the screens between fights,
 * and how what is held, on the screen or on the keys, is made into what the fighter is
 * asked to do. Nothing here knows of a screen, so it can be tested on its own.
 */
final class FightKeys {
    /** The buttons, in the order the screen has them. */
    static final int LEFT = 0, RIGHT = 1, JUMP = 2, GUARD = 3, LIGHT = 4, HEAVY = 5, SPECIAL = 6, DODGE = 7, ULT = 8, DASH = 9, BUTTONS = 10;
    /** What a key does on the screens between fights: nothing, the one before or after, up or down, or choose. */
    static final int NONE = 0, PREVIOUS = 1, NEXT = 2, UP = 3, DOWN = 4, CHOOSE = 5;
    /** The keys, as they are shown in the pause menu. */
    static final String[] HELP = {
            "A D or \u2190 \u2192  move  \u00b7  W, \u2191 or Space  jump",
            "S or \u2193  guard  \u00b7  Shift or O dash, on the ground or in the air",
            "J quick  \u00b7  K heavy  \u00b7  L special",
            "I or U ultimate  \u00b7  Esc pause",
    };

    private FightKeys() {}

    /** Which button a key stands for in a fight, or -1. */
    static int button(int code) {
        switch (code) {
            case KeyEvent.KEYCODE_A: case KeyEvent.KEYCODE_DPAD_LEFT: return LEFT;
            case KeyEvent.KEYCODE_D: case KeyEvent.KEYCODE_DPAD_RIGHT: return RIGHT;
            case KeyEvent.KEYCODE_W: case KeyEvent.KEYCODE_DPAD_UP: case KeyEvent.KEYCODE_SPACE: return JUMP;
            case KeyEvent.KEYCODE_S: case KeyEvent.KEYCODE_DPAD_DOWN: return GUARD;
            case KeyEvent.KEYCODE_J: return LIGHT;
            case KeyEvent.KEYCODE_K: return HEAVY;
            case KeyEvent.KEYCODE_L: return SPECIAL;
            // The dash carries the dodge in it, so the dodge's old key is a dash too.
            case KeyEvent.KEYCODE_SHIFT_LEFT: case KeyEvent.KEYCODE_SHIFT_RIGHT: case KeyEvent.KEYCODE_O: return DASH;
            case KeyEvent.KEYCODE_I: case KeyEvent.KEYCODE_U: return ULT;
            default: return -1;
        }
    }

    /** What a key does on the screens between fights. */
    static int menu(int code) {
        switch (code) {
            case KeyEvent.KEYCODE_A: case KeyEvent.KEYCODE_DPAD_LEFT: return PREVIOUS;
            case KeyEvent.KEYCODE_D: case KeyEvent.KEYCODE_DPAD_RIGHT: return NEXT;
            case KeyEvent.KEYCODE_W: case KeyEvent.KEYCODE_DPAD_UP: return UP;
            case KeyEvent.KEYCODE_S: case KeyEvent.KEYCODE_DPAD_DOWN: return DOWN;
            case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_NUMPAD_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER: case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_J: return CHOOSE;
            default: return NONE;
        }
    }

    /**
     * What the fighter is asked to do: each button held on the screen or on the keys, or
     * pressed and let go since the fight last moved on (so that a quick tap, or two of an
     * arrow for a dash, is not lost between frames).
     */
    static void fill(Fight.Pad into, boolean[] holding, boolean[] tapped, boolean[] keys) {
        into.left = holding[LEFT] || tapped[LEFT] || keys[LEFT];
        into.right = holding[RIGHT] || tapped[RIGHT] || keys[RIGHT];
        into.block = holding[GUARD] || keys[GUARD];
        into.jump = holding[JUMP] || tapped[JUMP] || keys[JUMP];
        into.light = holding[LIGHT] || tapped[LIGHT] || keys[LIGHT];
        into.heavy = holding[HEAVY] || tapped[HEAVY] || keys[HEAVY];
        into.special = holding[SPECIAL] || tapped[SPECIAL] || keys[SPECIAL];
        into.dodge = holding[DODGE] || tapped[DODGE] || keys[DODGE];
        into.ultimate = holding[ULT] || tapped[ULT] || keys[ULT];
        into.dash = holding[DASH] || tapped[DASH] || keys[DASH];
    }
}
