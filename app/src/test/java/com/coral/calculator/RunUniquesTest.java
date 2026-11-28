package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** D3: the unique creatures, two to each chapter, each with its own way of fighting and a wind-up before it strikes; tougher than the signs. */
public class RunUniquesTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    @Test public void twoToEachChapterAndEveryLandHasItsOwn() {
        assertEquals(59, RunUniques.COUNT);
        assertEquals(RunUniques.FIRST + 24, RunUniques.HIDDEN_FIRST);
        assertEquals(RunLevels.LUCY + 1, RunUniques.FIRST);
        Set<Integer> ways = new HashSet<>();
        for (int index = 0; index < 24; index++) {
            int kind = RunUniques.FIRST + index;
            assertTrue(RunUniques.is(kind));
            assertEquals(index / 2, RunUniques.chapter(kind));
            assertFalse(RunLevels.boss(kind));
            assertFalse(RunLevels.flier(kind));
            assertTrue(RunUniques.name(kind).length() > 3);
            ways.add(RunUniques.way(kind));
        }
        assertEquals(12, ways.size());
        int minibosses = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int chapter = Math.min(11, RunLevels.chapterOf(number)), here = 0, bosses = 0;
            for (int[] foe : level.foes) {
                if (!RunUniques.is(foe[0]) || foe[1] >= level.main) continue;
                here++;
                assertTrue(RunLevels.uniqueSpec(foe));
                assertEquals("land " + number, chapter, RunUniques.chapter(foe[0]));
                assertTrue(foe[1] >= 30 && foe[1] < level.gate - 5);
                for (int flag : level.flags) assertTrue(Math.abs(flag - foe[1]) >= 6);
                if (!RunUniques.flies(foe[0])) {
                    assertEquals("land " + number + " at " + foe[1], RunLevels.top(level, foe[1]), foe[2] + 1);
                    assertEquals(RunLevels.AIR, level.land[foe[2]][foe[1]]);
                }
                if (RunUniques.way(foe[0]) == RunUniques.MINIBOSS) bosses++;
            }
            assertTrue("land " + number + " has " + here, here >= Math.min(2, RunLevels.uniquesIn(number)) && here <= RunLevels.uniquesIn(number));
            assertTrue(bosses <= 1);
            minibosses += bosses;
        }
        assertTrue(minibosses >= 15);
    }

    @Test public void theyAreTougherThanTheSignsAndBiteHarder() {
        for (int number : new int[]{13, 30, 60, 99}) {
            for (int index = 0; index < RunUniques.COUNT; index++) {
                int kind = RunUniques.FIRST + index;
                assertTrue(RunUniques.health(kind, number) > PlusRun.healthOf(RunLevels.MINUS));
                assertTrue(RunUniques.bite(kind, number) >= 1);
            }
            assertTrue(RunUniques.health(RunUniques.FIRST, 99) > RunUniques.health(RunUniques.FIRST, 1) * 4);
            assertEquals(0, RunUniques.bite(RunUniques.FIRST, 5));
        }
    }

    /** One of them alone on flat ground, and her a few squares off. */
    private static PlusRun alone(int index, float herAt, float itAt, int number) {
        RunLevels.Level flat = new RunLevels.Level(number, 60);
        for (int column = 0; column < 60; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) flat.land[row][column] = RunLevels.BLOCK;
        flat.gate = 57;
        int kind = RunUniques.FIRST + index;
        flat.foes.add(new int[]{kind, (int) itAt, RunUniques.flies(kind) ? FLOOR - 4 : FLOOR - 1, 1, 0, 2});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 40;
        PlusRun run = new PlusRun(flat, perks);
        run.place(herAt, FLOOR - PlusRun.TALL);
        return run;
    }

    private static PlusRun.Foe it(PlusRun run) { return run.foes().get(0); }

    @Test public void aChargerWindsUpThenRunsAtHer() {
        PlusRun run = alone(0, 10, 15, 3);
        PlusRun.Foe dog = it(run);
        float from = dog.x;
        boolean wound = false;
        int hp = run.hp();
        for (int step = 0; step < 60 * 4 && run.hp() == hp; step++) {
            run.step(STEP, false, false, false);
            if (dog.mode == 1) wound = true;
            if (dog.mode == 2) assertTrue("struck before winding up", wound);
        }
        assertTrue(wound);
        assertTrue(run.windUps() >= 1);
        assertTrue(dog.x < from);
        assertTrue("its charge hurts her", run.hp() < hp);
    }

    @Test public void anExploderSwellsAndBursts() {
        PlusRun run = alone(4, 10, 12, 10);
        PlusRun.Foe letter = it(run);
        for (int step = 0; step < 60 * 5 && !letter.flat; step++) run.step(STEP, false, false, false);
        assertTrue(letter.flat);
        assertTrue(run.uniqueShots() >= 8);
    }

    @Test public void aSniperKeepsItsDistanceAndShoots() {
        PlusRun run = alone(5, 10, 17, 10);
        for (int step = 0; step < 60 * 4 && run.uniqueShots() == 0; step++) run.step(STEP, false, false, false);
        assertTrue(run.uniqueShots() >= 1);
        assertTrue(run.windUps() >= 1);
    }

    @Test public void aSummonerCallsUpMinusSigns() {
        PlusRun run = alone(8, 10, 17, 25);
        for (int step = 0; step < 60 * 6 && run.calledUp() == 0; step++) run.step(STEP, false, false, false);
        assertTrue(run.calledUp() >= 1);
        int minus = 0;
        for (PlusRun.Foe foe : run.foes()) if (foe.kind == RunLevels.MINUS) minus++;
        assertTrue(minus >= 1);
    }

    @Test public void aHealerMendsWhoeverIsHurtNearIt() {
        RunLevels.Level flat = new RunLevels.Level(25, 60);
        for (int column = 0; column < 60; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) flat.land[row][column] = RunLevels.BLOCK;
        flat.gate = 57;
        flat.foes.add(new int[]{RunUniques.FIRST + 9, 18, FLOOR - 1, 1, 0, 2});
        flat.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1});
        PlusRun run = new PlusRun(flat, new PlusRun.Perks());
        run.place(4, FLOOR - PlusRun.TALL);
        PlusRun.Foe minus = run.foes().get(1);
        minus.most = 5 * PlusRun.BLOW;
        minus.left = 1;
        for (int step = 0; step < 60 * 6 && run.heals() == 0; step++) {
            minus.x = 20;
            run.step(STEP, false, false, false);
        }
        assertTrue(run.heals() >= 1);
        assertTrue(minus.left > 1);
    }
    @Test public void aSplitterComesApartOnce() {
        PlusRun run = alone(16, 10, 13, 40);
        PlusRun.Foe slug = it(run);
        slug.left = 1;
        slug.dazed = 0;
        run.place(slug.x + slug.wide() / 2 - PlusRun.WIDE / 2, FLOOR - PlusRun.TALL - 3);
        for (int step = 0; step < 90 && !slug.flat; step++) run.step(STEP, false, false, false);
        assertTrue(slug.flat);
        run.step(STEP, false, false, false);
        int halves = 0;
        for (PlusRun.Foe foe : run.foes()) if (foe.kind == slug.kind && !foe.flat) {
            halves++;
            assertTrue(foe.size < slug.size);
            assertEquals(1, foe.did);
        }
        assertEquals(2, halves);
        assertEquals(1, run.halvings());
    }

    @Test public void aBurrowerGoesUnderAndComesUpBeneathHer() {
        PlusRun run = alone(7, 10, 15, 20);
        PlusRun.Foe mole = it(run);
        boolean under = false, up = false;
        for (int step = 0; step < 60 * 6 && !up; step++) {
            run.step(STEP, false, false, false);
            under |= mole.unseen;
            up |= under && mole.mode == 2;
        }
        assertTrue(under);
        assertTrue(up);
    }

    @Test public void aMimicLiesStillUntilSheIsNear() {
        PlusRun run = alone(2, 4, 20, 8);
        PlusRun.Foe book = it(run);
        float at = book.x;
        // Within waking distance but far: it does not stir.
        run.place(8, FLOOR - PlusRun.TALL);
        for (int step = 0; step < 60; step++) run.step(STEP, false, false, false);
        assertEquals(at, book.x, 1e-4f);
        assertEquals(0, book.turn);
        run.place(book.x - 2, FLOOR - PlusRun.TALL);
        for (int step = 0; step < 30; step++) run.step(STEP, false, false, false);
        assertEquals(1, book.turn);
    }

    @Test public void aWallGhostGoesThroughWalls() {
        PlusRun run = alone(3, 6, 16, 8);
        RunLevels.Level level = run.level();
        for (int row = 0; row < FLOOR; row++) level.land[row][12] = RunLevels.BLOCK;
        PlusRun.Foe ghost = it(run);
        for (int step = 0; step < 60 * 8 && ghost.x > 11; step++) run.step(STEP, false, false, false);
        assertTrue("ghost at " + ghost.x, ghost.x < 12);
    }

    @Test public void aMiniBossFightsInThreePhases() {
        PlusRun run = alone(10, 10, 14, 40);
        PlusRun.Foe knight = it(run);
        assertTrue(knight.most > RunUniques.health(RunUniques.FIRST, 40) * 3);
        knight.left = knight.most / 2;
        for (int step = 0; step < 60 * 6 && run.uniqueShots() == 0; step++) run.step(STEP, false, false, false);
        assertTrue("in its second phase it throws knives: mode " + knight.mode + " wait " + knight.wait + " at " + knight.x + " winds " + run.windUps() + " hp " + run.hp() + " flat " + knight.flat + " dazed " + knight.dazed, run.uniqueShots() >= 3);
    }

    @Test public void formationFlyersDiveOneAtATime() {
        PlusRun run = alone(12, 10, 10, 40);
        PlusRun.Foe hornet = it(run);
        float high = hornet.y;
        float lowest = high;
        for (int step = 0; step < 60 * 5; step++) {
            run.step(STEP, false, false, false);
            lowest = Math.max(lowest, hornet.y);
        }
        assertTrue(run.windUps() >= 1);
        assertTrue(lowest > high + 1);
    }

    @Test public void aShielderCarriesItsOwnShield() {
        PlusRun run = alone(1, 10, 13, 3);
        PlusRun.Foe snail = it(run);
        assertTrue(snail.guard > 0);
        assertEquals(-1, snail.guardKind);
    }
}
