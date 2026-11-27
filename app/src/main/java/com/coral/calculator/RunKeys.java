package com.coral.calculator;

import android.view.KeyEvent;

/**
 * Plus run with a keyboard, as on a computer: which keys do what, and which are held. A or D and the arrows move her; W, Up or Space
 * jumps (held, higher, as the button is); S or Down lets her down, or through a door; J or Enter strikes, and K holds a strike for her
 * special; Q or Tab changes what she holds; E is the button over the jump (a stall, a door, a gate, a pet); Esc is back and pause;
 * 1, 2 and 3 call on the actives in her slots; Shift or L dashes (in the air) or rolls (on the ground).
 * Several held at once all count, so she can run and jump together. Enter also says yes to a card.
 */
final class RunKeys {
    static final int LEFT = 1, RIGHT = 2, JUMP = 4, DOWN = 8, STRIKE = 16, SPECIAL = 32, SWAP = 64, EXTRA = 128, BACK = 256, CONFIRM = 512, SKILL_1 = 1024, SKILL_2 = 2048, SKILL_3 = 4096, DASH = 8192;

    /** What a key does, as the bits above; nothing for a key that does nothing here. */
    static int of(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_A: case KeyEvent.KEYCODE_DPAD_LEFT: return LEFT;
            case KeyEvent.KEYCODE_D: case KeyEvent.KEYCODE_DPAD_RIGHT: return RIGHT;
            case KeyEvent.KEYCODE_W: case KeyEvent.KEYCODE_DPAD_UP: case KeyEvent.KEYCODE_SPACE: return JUMP;
            case KeyEvent.KEYCODE_S: case KeyEvent.KEYCODE_DPAD_DOWN: return DOWN;
            case KeyEvent.KEYCODE_J: return STRIKE;
            case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_NUMPAD_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER: return STRIKE | CONFIRM;
            case KeyEvent.KEYCODE_K: return SPECIAL;
            case KeyEvent.KEYCODE_Q: case KeyEvent.KEYCODE_TAB: return SWAP;
            case KeyEvent.KEYCODE_E: return EXTRA;
            case KeyEvent.KEYCODE_ESCAPE: return BACK;
            case KeyEvent.KEYCODE_SHIFT_LEFT: case KeyEvent.KEYCODE_SHIFT_RIGHT: case KeyEvent.KEYCODE_L: return DASH;
            case KeyEvent.KEYCODE_1: case KeyEvent.KEYCODE_NUMPAD_1: return SKILL_1;
            case KeyEvent.KEYCODE_2: case KeyEvent.KEYCODE_NUMPAD_2: return SKILL_2;
            case KeyEvent.KEYCODE_3: case KeyEvent.KEYCODE_NUMPAD_3: return SKILL_3;
            default: return 0;
        }
    }

    /** Which keys are down, by their codes, and so which of the bits are held. */
    private final java.util.Set<Integer> down = new java.util.HashSet<>();
    private int held;

    /** A key goes down or up. Returns the bits it newly began: none for a key held on (its repeats) or let go. */
    int press(int keyCode, boolean isDown) {
        int does = of(keyCode);
        if (does == 0) return 0;
        int was = held;
        if (isDown) down.add(keyCode);
        else down.remove(keyCode);
        held = 0;
        for (int code : down) held |= of(code);
        return isDown ? does & ~was : 0;
    }

    boolean held(int bits) { return (held & bits) != 0; }

    /** Everything let go (the land left, the window gone from the front). */
    void clear() {
        down.clear();
        held = 0;
    }
}
