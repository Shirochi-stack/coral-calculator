package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** Twelve kinds of weapon found: the six new ones each do what they say; a weapon found counts for more by its rarity; over a hundred, a rose of each. */
public class RunWeaponsTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(1, columns);
        for (int column = 0; column < columns; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        level.gate = columns - 3;
        return level;
    }

    private static int weapon(int base, int rarity) { return RunLoot.code(RunLoot.WEAPON, 2, base, rarity); }

    /** A run holding a found weapon, with creatures at some columns, each taking so many blows. */
    private static PlusRun holding(int code, int blows, int... columns) {
        RunLevels.Level level = flat(80);
        for (int column : columns) level.foes.add(new int[]{RunLevels.MINUS, column, FLOOR - 1, blows});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.item = code;
        perks.main = RunArms.kindOf(code);
        PlusRun run = new PlusRun(level, perks);
        run.place(20, FLOOR - PlusRun.TALL);
        for (int step = 0; step < 20; step++) run.step(STEP, false, false, false);
        return run;
    }

    private static void tap(PlusRun run) {
        run.step(STEP, false, false, false, true);
        for (int step = 0; step < 50; step++) {
            for (PlusRun.Foe foe : run.foes()) foe.x = foe.home;
            run.step(STEP, false, false, false);
        }
    }

    private static void hold(PlusRun run) {
        for (int step = 0; step < 90; step++) {
            for (PlusRun.Foe foe : run.foes()) foe.x = foe.home;
            run.step(STEP, false, false, false, true);
        }
        for (int step = 0; step < 50; step++) run.step(STEP, false, false, false);
    }

    private static int hurt(PlusRun run) {
        int hurt = 0;
        for (PlusRun.Foe foe : run.foes()) if (foe.left < foe.most) hurt++;
        return hurt;
    }

    @Test public void everyKindIsAThingSheFightsWithAndTheSixNewOnesAreNew() {
        assertEquals(19, RunArms.COUNT);
        Set<Integer> kinds = new HashSet<>();
        for (int base = 0; base < 12; base++) kinds.add(RunArms.kindOf(weapon(base, RunLoot.WHITE)));
        assertEquals(12, kinds.size());
        for (int kind = RunArms.GREATSWORD; kind <= RunArms.SCYTHE; kind++) {
            assertTrue(kinds.contains(kind));
            assertTrue(RunArms.does(kind).length() > 20);
            assertTrue(RunArms.goodFor(kind).length() > 10);
            assertTrue(RunArms.heavy(kind));
        }
    }

    @Test public void overAHundredWeaponsWithARoseOfEachKind() {
        Set<String> names = new HashSet<>();
        for (int theme = 0; theme < RunLoot.THEMES.length; theme++) for (int base = 0; base < 12; base++) names.add(RunLoot.name(RunLoot.code(RunLoot.WEAPON, theme, base, RunLoot.BLUE)));
        assertTrue(names.size() >= 100);
        Set<Integer> roses = new HashSet<>();
        for (int base = 0; base < 12; base++) {
            int rose = weapon(base, RunLoot.ROSE);
            assertTrue(RunLoot.name(rose).startsWith("Halgakos Rose"));
            roses.add(RunArms.kindOf(rose));
        }
        assertEquals(12, roses.size());
    }

    @Test public void aBetterRarityCountsForMore() {
        for (int base = 0; base < 12; base++) {
            assertTrue(RunArms.itemBlows(weapon(base, RunLoot.RED)) > RunArms.itemBlows(weapon(base, RunLoot.WHITE)));
            assertTrue(RunArms.itemBlows(weapon(base, RunLoot.ROSE)) > RunArms.itemBlows(weapon(base, RunLoot.RED)));
            assertTrue(RunArms.itemReach(weapon(base, RunLoot.GOLD)) > 1);
            assertTrue(RunArms.itemRest(weapon(base, RunLoot.GOLD)) < 1);
        }
        // Red ink guns are machine guns; white ones of the first chapter are not.
        assertTrue(RunArms.machine(weapon(10, RunLoot.RED)));
        assertFalse(RunArms.machine(RunLoot.code(RunLoot.WEAPON, 0, 10, RunLoot.WHITE)));
        // A red greatsword fells in fewer blows than a white one.
        PlusRun white = holding(weapon(2, RunLoot.WHITE), 6, 22), red = holding(weapon(2, RunLoot.RED), 6, 22);
        tap(white);
        tap(red);
        assertTrue(red.foes().get(0).left < white.foes().get(0).left);
    }

    @Test public void theGreatswordSweepsWideAndItsSlamTakesBothSides() {
        PlusRun run = holding(weapon(2, RunLoot.BLUE), 9, 22, 18);
        tap(run);
        assertEquals(1, run.greats());
        assertTrue(run.foes().get(0).left < run.foes().get(0).most);
        assertEquals(run.foes().get(1).most, run.foes().get(1).left);
        hold(run);
        assertEquals(2, hurt(run));
    }

    @Test public void theStaffSendsSpiritsAtTheNearest() {
        PlusRun run = holding(weapon(4, RunLoot.BLUE), 9, 26, 15);
        tap(run);
        assertTrue(run.summons() >= 1);
        assertTrue(hurt(run) >= 1);
        hold(run);
        assertEquals(2, hurt(run));
    }

    @Test public void theMaceMakesWhatItHitsReelAndTheHammerShakesTheGround() {
        PlusRun mace = holding(weapon(7, RunLoot.BLUE), 9, 22);
        mace.step(STEP, false, false, false, true);
        boolean reeled = false;
        for (int step = 0; step < 10; step++) {
            mace.step(STEP, false, false, false);
            reeled |= mace.foes().get(0).dazed > .3f;
        }
        assertTrue(reeled);
        assertEquals(1, mace.smashes());
        PlusRun hammer = holding(weapon(8, RunLoot.BLUE), 9, 22, 18);
        tap(hammer);
        assertEquals(1, hammer.slams());
        // The shockwave goes both ways along the ground.
        assertEquals(2, hurt(hammer));
    }

    @Test public void theWandsElementIsByItsChapter() {
        int fire = RunLoot.code(RunLoot.WEAPON, 0, 9, RunLoot.BLUE), ice = RunLoot.code(RunLoot.WEAPON, 1, 9, RunLoot.BLUE);
        assertEquals(RunArms.FIRE, RunArms.element(fire));
        assertEquals(RunArms.ICE, RunArms.element(ice));
        PlusRun run = holding(ice, 9, 26);
        tap(run);
        assertTrue(run.bolts() >= 1);
        assertTrue(hurt(run) >= 1);
        hold(run);
        assertTrue(run.foes().get(0).left < run.foes().get(0).most - 1);
    }

    @Test public void theScytheTakesBehindTooAndItsHookPulls() {
        PlusRun run = holding(weapon(11, RunLoot.BLUE), 9, 22, 19);
        tap(run);
        assertEquals(1, run.reaps());
        assertEquals(2, hurt(run));
        // Its hook: one a few squares ahead is pulled close.
        PlusRun hooked = holding(weapon(11, RunLoot.BLUE), 99, 25);
        PlusRun.Foe far = hooked.foes().get(0);
        for (int step = 0; step < 90; step++) hooked.step(STEP, false, false, false, true);
        assertTrue("at " + far.x, far.x < 23);
    }
}
