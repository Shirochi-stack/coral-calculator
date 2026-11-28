package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Burning that spreads, chill that freezes solid and shatters, ink-poison that bursts; what afflicts her; and how creatures are seen going. */
public class RunStatusTest {
    private static final int FLOOR = 10;
    private static final float STEP = 1 / 60f;

    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(1, columns);
        for (int c = 0; c < columns; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = columns - 3;
        return level;
    }

    private static PlusRun run(RunLevels.Level level) {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 40;
        perks.main = PlusRun.SWORD;
        PlusRun run = new PlusRun(level, perks);
        run.place(2, FLOOR - PlusRun.TALL - .02f);
        return run;
    }

    private static void wait(PlusRun run, float seconds) {
        for (float gone = 0; gone < seconds; gone += STEP) run.step(STEP, false, false, false, false);
    }

    @Test public void fireLeapsToWhatIsBesideItButNotToWhatFireIsNothingTo() {
        RunLevels.Level level = flat(40);
        level.foes.add(new int[]{RunLevels.TIMES, 20, FLOOR - 1, 40});
        level.foes.add(new int[]{RunLevels.TIMES, 21, FLOOR - 1, 40});
        level.foes.add(new int[]{RunLevels.WISP, 30, FLOOR - 3, 40});
        PlusRun run = run(level);
        PlusRun.Foe one = run.foes().get(0), two = run.foes().get(1), wisp = run.foes().get(2);
        assertTrue(run.ignite(one, PlusRun.BURN_LASTS));
        wait(run, 1);
        assertTrue(two.burning > 0);
        assertTrue(run.spreads() >= 1);
        assertFalse(run.ignite(wisp, 2));
        assertEquals(0, wisp.burning, 0);
        assertTrue(run.resisted() >= 1);
    }

    @Test public void threeChillsFreezeOneSolidAndABlowShattersItHalfAsHardAgain() {
        RunLevels.Level level = flat(40);
        level.foes.add(new int[]{RunLevels.TIMES, 4, FLOOR - 1, 40});
        PlusRun run = run(level);
        PlusRun.Foe foe = run.foes().get(0);
        run.chillFoe(foe);
        run.chillFoe(foe);
        assertEquals(0, foe.iced, 0);
        run.chillFoe(foe);
        assertTrue(foe.iced > 0);
        assertEquals(1, run.frozenSolid());
        float x = foe.x;
        wait(run, .5f);
        assertEquals("it does nothing, frozen", x, foe.x, .01f);
        int before = foe.left;
        run.place(foe.x - PlusRun.WIDE - .3f, FLOOR - PlusRun.TALL - .02f);
        for (int step = 0; step < 6; step++) run.step(STEP, false, false, false, step == 0);
        assertEquals(1, run.iceShatters());
        assertEquals(0, foe.iced, 0);
        int shattered = before - foe.left;
        // The same blow on one not frozen.
        RunLevels.Level again = flat(40);
        again.foes.add(new int[]{RunLevels.TIMES, 4, FLOOR - 1, 40});
        PlusRun plain = run(again);
        PlusRun.Foe other = plain.foes().get(0);
        int was = other.left;
        plain.place(other.x - PlusRun.WIDE - .3f, FLOOR - PlusRun.TALL - .02f);
        for (int step = 0; step < 6; step++) plain.step(STEP, false, false, false, step == 0);
        assertEquals(Math.round((was - other.left) * PlusRun.SHATTERS), shattered);
    }

    @Test public void oneInkPoisonedBurstsWhenBeatenPoisoningAndHurtingWhatIsNearAndLeavingAPool() {
        RunLevels.Level level = flat(40);
        level.foes.add(new int[]{RunLevels.MINUS, 4, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.TIMES, 5, FLOOR - 1, 40});
        PlusRun run = run(level);
        PlusRun.Foe first = run.foes().get(0), near = run.foes().get(1);
        assertTrue(run.poison(first));
        int pools = run.puddles().size(), was = near.left;
        run.place(first.x - PlusRun.WIDE - .3f, FLOOR - PlusRun.TALL - .02f);
        for (int step = 0; step < 6; step++) run.step(STEP, false, false, false, step == 0);
        assertTrue(first.flat);
        assertEquals(1, run.inkBursts());
        assertTrue(near.poisoned > 0);
        assertTrue(near.left < was);
        assertTrue(run.puddles().size() > pools);
    }

    @Test public void burningAndPoisonNeverTakeHerBelowHalfAHeartAndRedKeepsFireOff() {
        PlusRun run = run(flat(40));
        run.burnHer();
        run.poisonHer();
        assertTrue(run.herBurn() > 0 && run.herPoison() > 0);
        int before = run.hp();
        wait(run, 1.2f);
        assertTrue(run.hp() < before);
        assertTrue(run.statusHurt() > 0);
        for (int again = 0; again < 200; again++) {
            run.burnHer();
            run.poisonHer();
            wait(run, .5f);
        }
        assertTrue(run.hp() >= PlusRun.HALF);
        assertEquals(PlusRun.PLAYING, run.state());
        run.cleanseHer();
        assertEquals(0, run.herBurn(), 0);
    }

    @Test public void chilledSheRunsSlowerAndStunnedSheCanDoNothingAndIsNotStunnedAgainAtOnce() {
        PlusRun plain = run(flat(60)), cold = run(flat(60));
        cold.chillHer();
        for (float gone = 0; gone < 1; gone += STEP) {
            plain.step(STEP, false, true, false, false);
            cold.step(STEP, false, true, false, false);
        }
        assertTrue(cold.x() < plain.x() - .5f);
        PlusRun run = run(flat(40));
        wait(run, .2f);
        run.stunHer();
        float x = run.x();
        for (float gone = 0; gone < .4f; gone += STEP) run.step(STEP, false, true, true, false);
        assertEquals(x, run.x(), .05f);
        assertTrue(run.onGround());
        wait(run, .3f);
        run.stunHer();
        assertEquals(1, run.herStuns());
    }

    @Test public void eachIsSeenGoingInItsOwnWay() {
        assertEquals(PlusRun.DEFEAT_SPLAT, PlusRun.defeatOf(RunLevels.SLIME));
        assertEquals(PlusRun.DEFEAT_MELT, PlusRun.defeatOf(RunLevels.BLOT));
        assertEquals(PlusRun.DEFEAT_FADE, PlusRun.defeatOf(RunLevels.MINUS));
        RunLevels.Level level = flat(40);
        level.foes.add(new int[]{RunLevels.MINUS, 4, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 12, FLOOR - 1});
        PlusRun run = run(level);
        PlusRun.Foe frozen = run.foes().get(0), burning = run.foes().get(1);
        for (int chill = 0; chill < 3; chill++) run.chillFoe(frozen);
        run.place(frozen.x - PlusRun.WIDE - .3f, FLOOR - PlusRun.TALL - .02f);
        for (int step = 0; step < 6; step++) run.step(STEP, false, false, false, step == 0);
        assertTrue(frozen.flat);
        assertEquals(PlusRun.DEFEAT_SHATTER, frozen.defeat);
        run.ignite(burning, 5);
        run.place(burning.x - PlusRun.WIDE - .3f, FLOOR - PlusRun.TALL - .02f);
        for (int step = 0; step < 30; step++) run.step(STEP, false, false, false, step == 25);
        assertTrue(burning.flat);
        assertEquals(PlusRun.DEFEAT_ASH, burning.defeat);
    }
}