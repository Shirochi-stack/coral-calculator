package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;

import org.junit.Test;

/** Plus run with a keyboard: every key does what its button does, several at once, and a held key is one press, not many. */
public class RunKeysTest {
    @Test public void eachKeyDoesWhatItsButtonDoes() {
        assertEquals(RunKeys.LEFT, RunKeys.of(KeyEvent.KEYCODE_A));
        assertEquals(RunKeys.LEFT, RunKeys.of(KeyEvent.KEYCODE_DPAD_LEFT));
        assertEquals(RunKeys.RIGHT, RunKeys.of(KeyEvent.KEYCODE_D));
        assertEquals(RunKeys.RIGHT, RunKeys.of(KeyEvent.KEYCODE_DPAD_RIGHT));
        for (int jump : new int[]{KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_SPACE}) assertEquals(RunKeys.JUMP, RunKeys.of(jump));
        assertEquals(RunKeys.DOWN, RunKeys.of(KeyEvent.KEYCODE_S));
        assertEquals(RunKeys.DOWN, RunKeys.of(KeyEvent.KEYCODE_DPAD_DOWN));
        assertEquals(RunKeys.STRIKE, RunKeys.of(KeyEvent.KEYCODE_J));
        assertEquals(RunKeys.STRIKE | RunKeys.CONFIRM, RunKeys.of(KeyEvent.KEYCODE_ENTER));
        assertEquals(RunKeys.SPECIAL, RunKeys.of(KeyEvent.KEYCODE_K));
        assertEquals(RunKeys.SWAP, RunKeys.of(KeyEvent.KEYCODE_Q));
        assertEquals(RunKeys.SWAP, RunKeys.of(KeyEvent.KEYCODE_TAB));
        assertEquals(RunKeys.EXTRA, RunKeys.of(KeyEvent.KEYCODE_E));
        assertEquals(RunKeys.BACK, RunKeys.of(KeyEvent.KEYCODE_ESCAPE));
        assertEquals(0, RunKeys.of(KeyEvent.KEYCODE_Z));
    }

    @Test public void severalHeldAtOnceAndAHeldKeyIsOnePress() {
        RunKeys keys = new RunKeys();
        // Run right and jump together.
        assertEquals(RunKeys.RIGHT, keys.press(KeyEvent.KEYCODE_D, true));
        assertEquals(RunKeys.JUMP, keys.press(KeyEvent.KEYCODE_SPACE, true));
        assertTrue(keys.held(RunKeys.RIGHT));
        assertTrue(keys.held(RunKeys.JUMP));
        // A key held on repeats: no new press.
        assertEquals(0, keys.press(KeyEvent.KEYCODE_SPACE, true));
        // Let go of the jump: still running.
        assertEquals(0, keys.press(KeyEvent.KEYCODE_SPACE, false));
        assertFalse(keys.held(RunKeys.JUMP));
        assertTrue(keys.held(RunKeys.RIGHT));
        // Two keys for one thing: letting one go keeps it held while the other is down.
        keys.press(KeyEvent.KEYCODE_DPAD_RIGHT, true);
        keys.press(KeyEvent.KEYCODE_D, false);
        assertTrue(keys.held(RunKeys.RIGHT));
        keys.press(KeyEvent.KEYCODE_DPAD_RIGHT, false);
        assertFalse(keys.held(RunKeys.RIGHT));
        // Everything let go.
        keys.press(KeyEvent.KEYCODE_A, true);
        keys.press(KeyEvent.KEYCODE_J, true);
        keys.clear();
        assertFalse(keys.held(RunKeys.LEFT | RunKeys.STRIKE));
    }

    /** Held keys drive the same rules as held buttons: running right and jumping crosses what the buttons would cross. */
    @Test public void keysDriveTheRulesAsTheButtonsDo() {
        RunKeys keys = new RunKeys();
        RunLevels.Level level = new RunLevels.Level(1, 40);
        for (int column = 0; column < 40; column++) for (int row = RunLevels.FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        level.land[RunLevels.FLOOR - 1][10] = RunLevels.BLOCK;
        level.gate = 37;
        PlusRun byKeys = new PlusRun(level), byButtons = new PlusRun(level);
        keys.press(KeyEvent.KEYCODE_D, true);
        for (int step = 0; step < 240; step++) {
            boolean jump = step % 60 >= 20 && step % 60 < 45;
            if (step % 60 == 20) keys.press(KeyEvent.KEYCODE_W, true);
            if (step % 60 == 45) keys.press(KeyEvent.KEYCODE_W, false);
            byKeys.step(1 / 60f, keys.held(RunKeys.LEFT), keys.held(RunKeys.RIGHT), keys.held(RunKeys.JUMP));
            byButtons.step(1 / 60f, false, true, jump);
        }
        assertEquals(byButtons.x(), byKeys.x(), .0001f);
        assertTrue("at " + byKeys.x(), byKeys.x() > 11);
    }
}
