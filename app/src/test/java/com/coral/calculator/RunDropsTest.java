package com.coral.calculator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** What creatures drop never ends inside the ground: beside steps, on ledges, over hatches and pits, at any frame rate. */
public class RunDropsTest {
    /** Every drop still about is out of anything solid, and, once settled, rests on something. */
    private static void allClear(PlusRun run, String where, boolean settled) {
        for (PlusRun.Drop drop : run.scattered()) {
            assertFalse(where + ": a drop inside the ground at " + drop.x + "," + drop.y, run.dropStuck(drop.x, drop.y));
            if (!settled || drop.code == 0 && !drop.resting && drop.age > .35f) continue;
            assertTrue(where + ": a drop not resting at " + drop.x + "," + drop.y, drop.resting);
            int row = Math.round(drop.y), left = (int) Math.floor(drop.x - PlusRun.DROP_HALF), right = (int) Math.floor(drop.x + PlusRun.DROP_HALF - .001f);
            boolean under = false;
            for (int column = left; column <= right; column++) {
                char square = run.at(column, row);
                under |= PlusRun.solid(square) || square == RunLevels.THIN || square == RunLevels.FAKE;
            }
            assertTrue(where + ": resting on nothing at " + drop.x + "," + drop.y, under);
        }
    }

    /** Drops thrown out at every few columns of a land, from in the air, from the ground's top, and from deep inside it, at a frame rate. */
    private static void throwAll(RunLevels.Level level, float step, String name) {
        PlusRun run = new PlusRun(level);
        // (She stands at the very start, out of the way, so no plus is drawn to her.)
        for (int column = 12; column < level.land[0].length - 12; column += 5) {
            int top = RunLevels.top(level, column);
            if (top >= level.land.length - 1) continue;
            run.scatterAll(column, top - 3);
            run.scatterAll(column + .5f, top);
            run.scatterAll(column + .9f, top + 1.6f);
        }
        allClear(run, name + " thrown", false);
        for (float gone = 0; gone < 6; gone += Math.min(step, .1f)) {
            run.step(step, false, false, false);
            allClear(run, name + " falling", false);
        }
        allClear(run, name + " settled", true);
    }

    @Test public void dropsNeverEndInTheGroundAtAnyFrameRate() {
        for (int number = 1; number <= RunLevels.TOTAL; number += 7) {
            RunLevels.Level plain = RunLevels.level(number);
            throwAll(plain, 1 / 60f, "land " + number);
            throwAll(plain, 1 / 8f, "land " + number + " at 8 fps");
            // Its layers: caves under it, hatches, high ledges.
            RunLevels.Level layered = RunLevels.layered(plain, 7 + number);
            throwAll(layered, 1 / 8f, "land " + number + " in layers at 8 fps");
            throwAll(layered, 1 / 4f, "land " + number + " in layers at 4 fps");
        }
    }

    /** Over a hatch, a drop rests on the hatch; over a pit, it falls to the cave's floor, never into rock. */
    @Test public void dropsRestOnHatchesAndCaveFloors() {
        int hatches = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number += 3) {
            RunLevels.Level level = RunLevels.layered(RunLevels.level(number), 11 + number);
            PlusRun run = new PlusRun(level);
            for (int[] way : level.ways) {
                if (way[2] != 0) continue;
                hatches++;
                run.scatterAll(way[0] + .5f - 3, way[1] - 2);
            }
            for (float gone = 0; gone < 6; gone += .1f) run.step(1 / 8f, false, false, false);
            allClear(run, "land " + number + " hatches", true);
        }
        assertTrue(hatches > 10);
    }

    /** Creatures beaten where they stand, all through the lands: their drops end on something, out of the ground. */
    @Test public void creaturesDropsEndOnSomething() {
        for (int number = 1; number <= RunLevels.TOTAL; number += 4) {
            for (float step : new float[]{1 / 60f, 1 / 8f}) {
                PlusRun run = new PlusRun(RunLevels.layered(RunLevels.level(number), number));
                for (PlusRun.Foe foe : run.foes()) if (foe.kind != RunLevels.TARGET && foe.x > 12) foe.flat = true;
                for (float gone = 0; gone < 6; gone += Math.min(step, .1f)) {
                    run.step(step, false, false, false);
                    allClear(run, "land " + number + " creatures", false);
                }
                allClear(run, "land " + number + " creatures settled", true);
            }
        }
    }
}
