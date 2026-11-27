package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** W: swimming with the blue ribbon, holding her breath without it, ink bolts underwater, lagoons and the creatures that swim in them. */
public class RunWaterTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    /** A flat land with a deep pool: water from row 4 to the floor, columns 10 to 30, walled at both ends. */
    private static RunLevels.Level pool() {
        RunLevels.Level level = new RunLevels.Level(20, 50);
        for (int column = 0; column < 50; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        for (int column = 10; column <= 30; column++) for (int row = 4; row < FLOOR; row++) level.land[row][column] = RunLevels.WATER;
        for (int row = 0; row < FLOOR; row++) {
            level.land[row][9] = RunLevels.BLOCK;
            level.land[row][31] = RunLevels.BLOCK;
        }
        level.gate = 47;
        return level;
    }

    @Test public void inBlueSheSwimsUpDownAndAlong() {
        PlusRun run = new PlusRun(pool(), new PlusRun.Perks());
        run.lend(PlusRun.BLUE);
        run.place(18, 8);
        run.step(STEP, false, false, false);
        assertTrue(run.inWater());
        assertTrue(run.swimming());
        float y = run.y();
        for (int step = 0; step < 40; step++) run.step(STEP, false, false, true);
        assertTrue("up from " + y + " to " + run.y(), run.y() < y - .8f);
        y = run.y();
        run.down(true);
        for (int step = 0; step < 40; step++) run.step(STEP, false, false, false);
        run.down(false);
        assertTrue(run.y() > y + .8f);
        float x = run.x();
        for (int step = 0; step < 40; step++) run.step(STEP, false, true, false);
        assertTrue(run.x() > x + 1);
        // Never out of breath in blue.
        for (int step = 0; step < 60 * 12; step++) run.step(STEP, false, false, false);
        assertTrue(run.breath() >= PlusRun.HOLD_BREATH - .1f);
        assertEquals(0, run.livesLost());
    }

    @Test public void inBlueSheWalksIntoDeepWaterFromTheSideAndStandsOnItFromAbove() {
        // Water from row 4 to the floor at columns 20 to 40, open on its left: she walks along the ground into it.
        RunLevels.Level level = new RunLevels.Level(20, 50);
        for (int column = 0; column < 50; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        for (int column = 20; column <= 40; column++) for (int row = 4; row < FLOOR; row++) level.land[row][column] = RunLevels.WATER;
        level.gate = 47;
        PlusRun run = new PlusRun(level, new PlusRun.Perks());
        run.lend(PlusRun.BLUE);
        run.place(14, FLOOR - 2);
        boolean in = false;
        for (int step = 0; step < 60 * 3; step++) {
            run.step(STEP, false, true, false);
            in |= run.inWater();
        }
        assertTrue("stopped at " + run.x(), in && run.x() > 21);
        // Dropped onto its top from above, it holds her up.
        PlusRun above = new PlusRun(level, new PlusRun.Perks());
        above.lend(PlusRun.BLUE);
        above.place(30, 0);
        for (int step = 0; step < 60 * 2; step++) above.step(STEP, false, false, false);
        assertFalse(above.inWater());
        assertTrue(above.y() < 4);
    }

    @Test public void withoutBlueSheHoldsHerBreathAndThenItCostsHer() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 10;
        PlusRun run = new PlusRun(pool(), perks);
        run.place(18, 8);
        run.step(STEP, false, false, false);
        assertTrue(run.inWater());
        assertFalse(run.swimming());
        int hp = run.hp();
        for (int step = 0; step < 60 * 5; step++) run.step(STEP, false, false, false);
        if (run.inWater()) assertEquals(hp, run.hp());
        // Held under (kept in the water), past her breath, it costs her.
        for (int step = 0; step < 60 * 7; step++) {
            run.place(18, 8);
            run.step(STEP, false, false, false);
        }
        assertTrue(run.breath() <= 0);
        assertTrue(run.hp() < hp);
        assertEquals(PlusRun.SANK, run.cause());
    }

    @Test public void underwaterWhatSheUsesSendsAnInkBolt() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        PlusRun run = new PlusRun(pool(), perks);
        run.lend(PlusRun.BLUE);
        run.place(18, 8);
        for (int step = 0; step < 4; step++) run.step(STEP, false, false, false, step == 1);
        assertEquals(1, run.inkBolts());
    }

    @Test public void lagoonsAreFullOfDeepWaterAndCreaturesThatSwim() {
        int lagoons = 0;
        for (int number = 1; number < RunLevels.OPEN_TO; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int[] lagoon = null;
            for (int[] room : level.rooms) if (room[0] == RunLevels.LAGOON) lagoon = room;
            assertEquals("land " + number, RunLevels.lagoonIn(number), lagoon != null);
            if (lagoon == null) continue;
            lagoons++;
            // Deep: ten rows of water in it; the blue ribbon by its door; a chest; and swimmers in the water.
            int deep = 0;
            for (int row = 0; row < FLOOR; row++) if (level.land[row][lagoon[3] + 20] == RunLevels.WATER) deep++;
            assertTrue("land " + number + " deep " + deep, deep >= 9);
            boolean ribbon = false, chest = false;
            for (int[] item : level.items) {
                if (item[1] < lagoon[3] || item[1] > lagoon[4]) continue;
                ribbon |= item[0] == PlusRun.FORM + PlusRun.BLUE;
                chest |= item[0] == PlusRun.CHEST;
            }
            assertTrue(ribbon && chest);
            int swimmers = 0;
            for (int[] foe : level.foes) {
                if (!RunUniques.swims(foe[0])) continue;
                assertTrue(foe[1] >= lagoon[3] && foe[1] <= lagoon[4]);
                assertEquals(RunLevels.WATER, level.land[foe[2]][foe[1]]);
                swimmers++;
            }
            assertTrue(swimmers >= 3);
        }
        assertTrue("lagoons " + lagoons, lagoons >= 25);
    }

    @Test public void swimmersStayInTheWaterAndFightEachTheirOwnWay() {
        for (int which = 0; which < RunUniques.WATER_COUNT; which++) {
            RunLevels.Level level = pool();
            level.foes.add(new int[]{RunUniques.WATER_FIRST + which, 24, 8, 1, 0, 2});
            PlusRun.Perks perks = new PlusRun.Perks();
            perks.hearts = 20;
            PlusRun run = new PlusRun(level, perks);
            run.lend(PlusRun.BLUE);
            run.place(16, 8);
            PlusRun.Foe foe = run.foes().get(0);
            boolean wound = false;
            for (int step = 0; step < 60 * 6 && !foe.flat; step++) {
                run.place(16, 8);
                run.step(STEP, false, false, false);
                wound |= foe.mode >= 1;
                if (!foe.flat) assertEquals(RunLevels.WATER, level.land[(int) Math.floor(foe.y + foe.tall() / 2)][(int) Math.floor(foe.x + foe.wide() / 2)]);
            }
            // The jelly only drifts; the rest wind up and strike.
            if (which != 3) assertTrue("swimmer " + which, wound);
        }
    }
}
