package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** D4: moves bought at the guild counter: a third jump, an air dash, thorns, a ground pound, a dodge roll, a wall jump, a glide, a second wind. */
public class RunMovesTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(1, columns);
        for (int column = 0; column < columns; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        level.gate = columns - 3;
        return level;
    }

    private static PlusRun.Perks all() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.wings = perks.triple = perks.airDash = perks.dashStrikes = perks.pound = perks.roll = perks.wallJump = perks.glide = perks.secondWind = true;
        perks.thorns = 2;
        return perks;
    }

    @Test public void aThirdJumpIsAFlip() {
        PlusRun run = new PlusRun(flat(60), all());
        run.place(10, FLOOR - PlusRun.TALL);
        int presses = 0;
        for (int step = 0; step < 120; step++) {
            boolean press = step < 60 && step % 20 < 10;
            run.step(STEP, false, false, press);
            if (step == 42) assertTrue("shown " + run.moveShown(), run.moveShown() >= PlusRun.MOVE_FLIP && run.moveShown() <= PlusRun.MOVE_FLIP + 2);
        }
        assertEquals(1, run.flips());
        // Without it, two jumps only: no flip.
        PlusRun.Perks wings = new PlusRun.Perks();
        wings.wings = true;
        PlusRun two = new PlusRun(flat(60), wings);
        two.place(10, FLOOR - PlusRun.TALL);
        for (int step = 0; step < 120; step++) two.step(STEP, false, false, step < 60 && step % 20 < 10);
        assertEquals(0, two.flips());
    }

    @Test public void anAirDashGoesFastWithoutFallingAndStrikes() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 16, FLOOR - 1});
        PlusRun run = new PlusRun(level, all());
        run.place(10, FLOOR - PlusRun.TALL);
        for (int step = 0; step < 14; step++) run.step(STEP, false, true, step < 10);
        float x = run.x(), y = run.y();
        assertTrue(run.dash());
        assertFalse(run.dash());
        for (int step = 0; step < 10; step++) run.step(STEP, false, false, false);
        assertTrue(run.x() - x > 1.5f);
        assertEquals(y, run.y(), .05f);
        assertEquals(PlusRun.MOVE_DASH, run.moveShown());
        assertEquals(1, run.airDashes());
        // On the ground it is a roll: fast, and nothing hurts her in it.
        PlusRun ground = new PlusRun(flat(60), all());
        ground.place(10, FLOOR - PlusRun.TALL);
        ground.step(STEP, false, false, false);
        assertTrue(ground.dash());
        assertEquals(PlusRun.MOVE_ROLL, ground.moveShown());
        assertTrue(ground.safe());
        for (int step = 0; step < 20; step++) ground.step(STEP, false, false, false);
        assertTrue(ground.x() > 12);
        assertEquals(1, ground.rolls());
    }

    @Test public void theDashStrikesWhatItGoesThrough() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 13, FLOOR - 1});
        PlusRun run = new PlusRun(level, all());
        run.place(10, FLOOR - PlusRun.TALL - .2f);
        PlusRun.Foe foe = run.foes().get(0);
        foe.y = foe.ground = FLOOR - PlusRun.TALL - .2f;
        run.step(STEP, false, true, false);
        run.dash();
        for (int step = 0; step < 14; step++) {
            foe.y = foe.ground;
            run.step(STEP, false, false, false);
        }
        assertTrue(run.dashStruck() >= 1);
    }

    @Test public void aGroundPoundStrikesAllRoundWhereSheLands() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 12, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 8, FLOOR - 1});
        PlusRun run = new PlusRun(level, all());
        run.place(10, FLOOR - PlusRun.TALL - 4);
        for (PlusRun.Foe foe : run.foes()) foe.left = foe.most = 1;
        run.down(true);
        for (int step = 0; step < 90 && run.pounds() == 0; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.pounds());
        for (PlusRun.Foe foe : run.foes()) assertTrue(foe.flat);
    }

    @Test public void againstAWallSheClingsAndKicksOff() {
        RunLevels.Level level = flat(60);
        for (int row = 0; row < FLOOR; row++) level.land[row][14] = RunLevels.BLOCK;
        PlusRun run = new PlusRun(level, all());
        run.place(14 - PlusRun.WIDE - .01f, FLOOR - PlusRun.TALL - 6);
        boolean clung = false;
        for (int step = 0; step < 40; step++) {
            run.step(STEP, false, true, false);
            clung |= run.clinging();
        }
        assertTrue(clung);
        assertTrue(run.speedDown() <= PlusRun.WALL_SLIDE + .01f);
        float x = run.x();
        run.step(STEP, false, true, true);
        for (int step = 0; step < 12; step++) run.step(STEP, false, false, true);
        assertEquals(1, run.wallJumps());
        assertTrue(run.x() < x - .3f);
    }

    @Test public void holdingJumpAsSheFallsSheGlides() {
        PlusRun run = new PlusRun(flat(60), all());
        run.place(10, 2);
        for (int step = 0; step < 60; step++) run.step(STEP, false, false, true);
        assertTrue(run.gliding());
        assertTrue(run.speedDown() <= PlusRun.GLIDE_FALL + .01f);
        assertEquals(PlusRun.MOVE_GLIDE, run.moveShown());
    }

    @Test public void aSecondWindOnceALandAndThornsFromTheCounter() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 12, FLOOR - 1});
        PlusRun.Perks perks = all();
        perks.hearts = 1;
        PlusRun run = new PlusRun(level, perks);
        run.place(10, FLOOR - PlusRun.TALL);
        int lives = run.lives();
        for (int step = 0; step < 60 * 3 && run.secondWinds() == 0; step++) run.step(STEP, false, true, false);
        assertEquals(1, run.secondWinds());
        assertEquals(lives, run.lives());
        assertTrue(run.hp() > 0);
        assertEquals(2, run.gear(RunLoot.THORNS));
    }
}
