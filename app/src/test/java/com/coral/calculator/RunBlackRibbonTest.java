package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Black on her is a curse while it lasts: heavy, weak and dripping, never to the death; it wears off, and a white ribbon washes it off. */
public class RunBlackRibbonTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    private static PlusRun land(int number, int... foes) {
        RunLevels.Level level = new RunLevels.Level(number, 80);
        for (int c = 0; c < 80; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 77;
        for (int column : foes) level.foes.add(new int[]{RunLevels.MINUS, column, FLOOR - 1, 30});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 10;
        PlusRun run = new PlusRun(level, perks);
        run.place(10.6f, FLOOR - PlusRun.TALL);
        return run;
    }

    private static float ran(PlusRun run) {
        float from = run.x();
        for (int step = 0; step < 60; step++) run.step(STEP, false, true, false);
        return run.x() - from;
    }

    private static float jumped(PlusRun run) {
        float from = run.y(), top = from;
        run.step(STEP, false, false, true);
        for (int step = 0; step < 60; step++) {
            run.step(STEP, false, false, true);
            top = Math.min(top, run.y());
        }
        return from - top;
    }

    @Test public void heavyInkSlowsHerAndLowersHerJumps() {
        PlusRun plain = land(10), black = land(10);
        black.lend(PlusRun.BLACK);
        assertTrue(black.inked());
        assertTrue(ran(black) < ran(plain) * .9f);
        PlusRun plainJump = land(10), blackJump = land(10);
        blackJump.lend(PlusRun.BLACK);
        assertTrue(jumped(blackJump) < jumped(plainJump) * .9f);
        // A little more, the further on, and never too much.
        assertTrue(land(90).curseSlow() < land(10).curseSlow());
        assertTrue(land(100).curseSlow() >= .74f);
    }

    @Test public void itsBlowsAreLighter() {
        PlusRun plain = land(10, 12), black = land(10, 12);
        plain.arm(RunArms.KNUCKLES);
        black.arm(RunArms.KNUCKLES);
        black.lend(PlusRun.BLACK);
        PlusRun.Foe one = plain.foes().get(0), other = black.foes().get(0);
        one.stunned = other.stunned = 999;
        plain.step(STEP, false, false, false, true);
        black.step(STEP, false, false, false, true);
        assertTrue(other.most - other.left < one.most - one.left);
        assertTrue(black.curseWeak() >= 20 && black.curseWeak() <= 30);
    }

    @Test public void itDripsButNeverTakesHerLastHeart() {
        PlusRun run = land(40);
        run.lend(PlusRun.BLACK);
        int hp = run.hp();
        for (int step = 0; step < Math.round(PlusRun.DRIP_EVERY / STEP) + 5; step++) run.step(STEP, false, false, false);
        assertEquals(hp - run.curseDrip(), run.hp());
        assertEquals(1, run.drips());
        // Down to a heart and a little more, it drips to the heart and no further.
        while (run.hp() > RunSkills.HEART + 16) run.wound();
        run.lend(PlusRun.BLACK);
        for (int step = 0; step < 60 * 25; step++) run.step(STEP, false, false, false);
        assertEquals(RunSkills.HEART, run.hp());
        assertEquals(PlusRun.PLAYING, run.state());
    }

    @Test public void itWearsOffAndWhiteWashesItOff() {
        PlusRun run = land(10);
        run.lend(PlusRun.BLACK);
        float lasts = run.cursedFor();
        for (int step = 0; step < Math.round((lasts + .5f) / STEP); step++) run.step(STEP, false, false, false);
        assertFalse(run.inked());
        PlusRun washed = land(10);
        washed.lend(PlusRun.BLACK);
        assertTrue(washed.inked());
        washed.lend(PlusRun.WHITE);
        assertFalse(washed.inked());
        assertEquals(1, washed.cleansed());
        // What it does is named, for the pause card; and she has a line for it.
        assertEquals(4, PlusRun.CURSE_NAMES.length);
        assertTrue(RunStory.BLACKENED[0][1].endsWith("Sloppy~♡"));
        assertTrue(new java.io.File("src/main/res/raw/run_v_blk1.ogg").isFile());
        assertTrue(new java.io.File("src/main/res/raw/run_v_title2.ogg").isFile());
    }
}
