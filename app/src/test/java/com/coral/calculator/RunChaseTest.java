package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The signs of a land go after her: down off ledges, up onto them, to the end of their patience; but not into a pit, nor in a village. */
public class RunChaseTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    private static RunLevels.Level flat(int number, int columns) {
        RunLevels.Level level = new RunLevels.Level(number, columns);
        for (int column = 0; column < columns; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        level.gate = columns - 3;
        return level;
    }

    private static PlusRun runOf(RunLevels.Level level) {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 80;
        PlusRun run = new PlusRun(level, perks);
        run.awaken();
        return run;
    }

    private static PlusRun.Foe first(PlusRun run, int kind) {
        for (PlusRun.Foe foe : run.foes()) if (foe.kind == kind) return foe;
        throw new AssertionError("none of " + kind);
    }

    private static void wait(PlusRun run, float seconds) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, false, false, false);
    }

    /** One on a ledge four rows up, she on the floor beyond its edge: it walks off the end and comes down to her. */
    @Test public void aMobOnALowerPlatformFollowsHerDownOffTheLedge() {
        RunLevels.Level level = flat(40, 60);
        for (int column = 10; column <= 20; column++) level.land[FLOOR - 4][column] = RunLevels.BLOCK;
        level.foes.add(new int[]{RunLevels.MINUS, 14, FLOOR - 5, 1});
        PlusRun run = runOf(level);
        PlusRun.Foe foe = first(run, RunLevels.MINUS);
        run.place(22, FLOOR - PlusRun.TALL - .02f);
        boolean down = false;
        for (float gone = 0; gone < 10 && !down; gone += STEP) {
            run.step(STEP, false, false, false);
            run.place(22, FLOOR - PlusRun.TALL - .02f);
            down = Math.abs(foe.ground + foe.tall() - FLOOR) < .05f && run.held(foe);
        }
        assertTrue("it came down off its ledge, at " + foe.x + "," + (foe.ground + foe.tall()), down);
        assertTrue(run.chasesBegun() > 0);
    }

    /** One on the floor, she up on a ledge three rows over it: it crouches and leaps up onto the ledge. */
    @Test public void aMobBelowJumpsUpToHerLedge() {
        RunLevels.Level level = flat(40, 60);
        for (int column = 12; column <= 20; column++) level.land[FLOOR - 3][column] = RunLevels.LEDGE;
        level.foes.add(new int[]{RunLevels.MINUS, 8, FLOOR - 1, 1});
        PlusRun run = runOf(level);
        PlusRun.Foe foe = first(run, RunLevels.MINUS);
        boolean up = false;
        for (float gone = 0; gone < 10 && !up; gone += STEP) {
            run.step(STEP, false, false, false);
            run.place(15, FLOOR - 3 - PlusRun.TALL - .02f);
            up = Math.abs(foe.ground + foe.tall() - (FLOOR - 3)) < .05f && foe.leaping == 0;
        }
        assertTrue("it jumped up onto her ledge, at " + foe.x + "," + (foe.ground + foe.tall()), up);
        assertTrue(run.chaseJumps() > 0);
    }

    /** She is behind a wall it can never get over: it comes to the wall, and after a few seconds it gives up and goes home. */
    @Test public void aMobGivesUpWhenItCannotGetNearer() {
        RunLevels.Level level = flat(40, 60);
        for (int row = 0; row < FLOOR; row++) level.land[row][14] = RunLevels.BLOCK;
        level.foes.add(new int[]{RunLevels.MINUS, 10, FLOOR - 1, 1});
        PlusRun run = runOf(level);
        PlusRun.Foe foe = first(run, RunLevels.MINUS);
        run.place(16.5f, FLOOR - PlusRun.TALL - .02f);
        wait(run, 2);
        assertTrue("it was after her", run.chasesBegun() > 0);
        float home = foe.spawnX;
        for (float gone = 0; gone < 14 && run.chasesGivenUp() == 0; gone += STEP) {
            run.step(STEP, false, false, false);
            run.place(16.5f, FLOOR - PlusRun.TALL - .02f);
        }
        assertTrue("it gave up", run.chasesGivenUp() > 0);
        assertFalse(foe.hunting);
        boolean back = false;
        for (float gone = 0; gone < 6 && !back; gone += STEP) {
            run.step(STEP, false, false, false);
            back = foe.homing == 0 && Math.abs(foe.x - home) < 1;
        }
        assertTrue("and went back to where it was put", back);
    }

    /** Having lost her, with her far off, it gives up too. */
    @Test public void aMobGivesUpWhenSheIsFarOff() {
        RunLevels.Level level = flat(40, 120);
        level.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1, 1});
        PlusRun run = runOf(level);
        run.place(26, FLOOR - PlusRun.TALL - .02f);
        wait(run, 1.2f);
        assertTrue(run.chasesBegun() > 0);
        for (float gone = 0; gone < 5; gone += STEP) {
            run.step(STEP, false, false, false);
            run.place(90, FLOOR - PlusRun.TALL - .02f);
        }
        assertTrue(run.chasesGivenUp() > 0);
    }

    /** She is across a pit too wide to jump: it comes to the edge and stops there. */
    @Test public void aMobDoesNotWalkIntoAPit() {
        RunLevels.Level level = flat(60, 60);
        for (int column = 20; column <= 24; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.AIR;
        level.foes.add(new int[]{RunLevels.MINUS, 18, FLOOR - 1, 1});
        level.foes.add(new int[]{RunLevels.TIMES, 17, FLOOR - 1, 1});
        PlusRun run = runOf(level);
        run.place(26.5f, FLOOR - PlusRun.TALL - .02f);
        for (float gone = 0; gone < 12; gone += STEP) {
            run.step(STEP, false, false, false);
            run.place(26.5f, FLOOR - PlusRun.TALL - .02f);
            for (PlusRun.Foe foe : run.foes()) {
                assertFalse("it fell in", foe.flat);
                assertTrue("it went over the edge at " + foe.x, foe.x + foe.wide() <= 20.3f);
            }
        }
        assertEquals(0, run.foesSank());
        assertEquals(0, run.foesFell());
    }

    /** Over a deep drop onto water, it does not step off after her either. */
    @Test public void aMobDoesNotStepOffIntoWater() {
        RunLevels.Level level = flat(60, 60);
        for (int column = 10; column <= 20; column++) level.land[FLOOR - 4][column] = RunLevels.BLOCK;
        for (int column = 21; column <= 30; column++) for (int row = FLOOR - 1; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.WATER;
        level.foes.add(new int[]{RunLevels.MINUS, 18, FLOOR - 5, 1});
        PlusRun run = runOf(level);
        for (float gone = 0; gone < 10; gone += STEP) {
            run.step(STEP, false, false, false);
            run.place(24, FLOOR - 4 - PlusRun.TALL - .02f);
        }
        assertEquals(0, run.foesSank());
    }

    /** In a village nothing goes after her. */
    @Test public void mobsInAVillageStayCalm() {
        RunLevels.Level level = flat(1, 80);
        level.town = 2;
        level.foes.add(new int[]{RunLevels.MINUS, 8, FLOOR - 1, 1});
        level.foes.add(new int[]{RunLevels.DRIFTER, 10, FLOOR - 3, 1});
        PlusRun run = runOf(level);
        run.place(9, FLOOR - PlusRun.TALL - .02f);
        wait(run, 4);
        assertEquals(0, run.chasesBegun());
        // Where there is no village the same two have her.
        RunLevels.Level open = flat(1, 80);
        open.foes.add(new int[]{RunLevels.MINUS, 8, FLOOR - 1, 1});
        open.foes.add(new int[]{RunLevels.DRIFTER, 10, FLOOR - 3, 1});
        PlusRun there = runOf(open);
        there.place(9, FLOOR - PlusRun.TALL - .02f);
        wait(there, 2);
        assertTrue(there.chasesBegun() > 0);
    }

    /** One in the air homes in on her, and strikes at her by itself, on purpose. */
    @Test public void aFlierHomesInAndLunges() {
        RunLevels.Level level = flat(40, 80);
        level.foes.add(new int[]{RunLevels.DRIFTER, 14, FLOOR - 4, 1});
        PlusRun run = runOf(level);
        PlusRun.Foe foe = first(run, RunLevels.DRIFTER);
        run.place(20, FLOOR - PlusRun.TALL - .02f);
        float near = 99;
        for (float gone = 0; gone < 8; gone += STEP) {
            run.step(STEP, false, false, false);
            run.place(20, FLOOR - PlusRun.TALL - .02f);
            near = Math.min(near, Math.abs(foe.x + foe.wide() / 2 - 20.3f));
        }
        assertTrue("it came to her: " + near, near < 2);
        assertTrue(run.chaseLunges() > 0);
    }

    /** One at her heels never gets a hand on her while she keeps running: it is slower than she is, in every land. */
    @Test public void noMobOutrunsHer() {
        for (int number : new int[]{1, 20, 50, 90}) {
            RunLevels.Level level = flat(number, 200);
            level.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1, 1});
            PlusRun run = runOf(level);
            PlusRun.Foe foe = first(run, RunLevels.MINUS);
            run.place(24, FLOOR - PlusRun.TALL - .02f);
            float fastest = 0;
            for (int step = 0; step < 60 * 6; step++) {
                float was = foe.x;
                run.step(STEP, false, true, false);
                if (foe.leaping == 0 && foe.wind <= 0 && foe.lunge <= 0) fastest = Math.max(fastest, Math.abs(foe.x - was) / STEP);
            }
            assertTrue("land " + number + " pace " + fastest, fastest < 6.2f * .85f);
        }
    }
}
