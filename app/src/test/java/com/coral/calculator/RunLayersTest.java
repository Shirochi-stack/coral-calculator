package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/** The lands in layers: caves under them, high ledges over them, laid fresh for each play; every way down has a way back up. */
public class RunLayersTest {
    private static final float STEP = 1 / 60f;
    private static final int ROWS = RunLevels.ROWS;

    /** A land as laid for a play, with no one in it to knock her about: whether its layers can be got through is the question here. */
    private static RunLevels.Level calm(int number, long play) {
        RunLevels.Level level = RunLevels.layered(RunLevels.level(number), play);
        for (int index = level.foes.size() - 1; index >= 0; index--) {
            int[] foe = level.foes.get(index);
            level.foes.remove(index);
        }
        level.guarded = false;
        return level;
    }

    /** Every land but the last has a lower layer; most have a cave in it; the land itself above it is the land as it always is. */
    @Test public void everyLandHasLayersAndTheLandItselfIsAsItWas() {
        int caves = 0, heights = 0, lands = 0;
        for (int number = 1; number < RunLevels.TOTAL; number++) {
            RunLevels.Level base = RunLevels.level(number), level = RunLevels.layered(base, 3);
            assertNotNull("land " + number, level.under);
            assertTrue("land " + number, level.land.length >= ROWS + RunLevels.LOWER);
            lands++;
            boolean cave = false, high = false;
            for (int[] way : level.ways) {
                cave |= way[2] == 0;
                high |= way[2] == 3;
            }
            if (cave) caves++;
            if (high) heights++;
            // The land's own squares are as they were, but where a way between layers is: a hatch, a vine, the ledges over it.
            for (int column = 0; column < base.main; column++) {
                for (int row = 0; row < ROWS; row++) {
                    char was = base.land[row][column], now = level.land[row][column];
                    if (was == now) continue;
                    boolean way = now == RunLevels.THIN || now == RunLevels.VINE || now == RunLevels.LEDGE || now == RunLevels.SPRING
                            || now == RunLevels.AIR && level.under[column];
                    assertTrue("land " + number + " column " + column + " row " + row + ": " + was + " became " + now, way);
                }
            }
            assertEquals("land " + number, base.gate, level.gate);
            assertEquals("land " + number, base.main, level.main);
        }
        assertTrue("caves in " + caves + " of " + lands, caves >= lands * 8 / 10);
        assertTrue("high ledges in " + heights + " of " + lands, heights >= lands * 3 / 10);
        assertEquals(RunLevels.level(RunLevels.TOTAL).land.length, RunLevels.layered(RunLevels.level(RunLevels.TOTAL), 3).land.length);
    }

    /** The same play lays the same layers; another play, others: a land is not the same twice below and above, though it is itself. */
    @Test public void eachPlayLaysItsOwnLayers() {
        int differ = 0;
        for (int number = 1; number < RunLevels.TOTAL; number += 3) {
            assertEquals(picture(RunLevels.layered(RunLevels.level(number), 11)), picture(RunLevels.layered(RunLevels.level(number), 11)));
            if (!picture(RunLevels.layered(RunLevels.level(number), 11)).equals(picture(RunLevels.layered(RunLevels.level(number), 12)))) differ++;
        }
        assertTrue(differ + " lands differ", differ >= 30);
    }

    private static String picture(RunLevels.Level level) {
        StringBuilder text = new StringBuilder();
        for (char[] row : level.land) text.append(row).append('\n');
        for (int[] item : level.items) text.append(item[0]).append(',').append(item[1]).append(',').append(item[2]).append(';');
        return text.toString();
    }

    /** Through every hatch she drops into a cave; across it she walks to its vine, through its false wall if she likes; up the vine she is back on the land. */
    @Test public void everyCaveCanBeDroppedIntoAndClimbedOutOf() {
        int caves = 0;
        for (int number = 1; number < RunLevels.TOTAL; number++) {
            for (long play = 1; play <= 4; play++) {
                RunLevels.Level level = calm(number, play);
                List<int[]> hatches = new ArrayList<>(), vines = new ArrayList<>();
                for (int[] way : level.ways) {
                    if (way[2] == 0) hatches.add(way);
                    if (way[2] == 1) vines.add(way);
                }
                assertEquals("land " + number, hatches.size(), vines.size());
                for (int index = 0; index < hatches.size(); index++) {
                    int[] hatch = hatches.get(index), vine = vines.get(index);
                    String where = "land " + number + " play " + play + " hatch " + hatch[0] + " vine " + vine[0];
                    assertTrue(where, vine[0] > hatch[0]);
                    PlusRun run = new PlusRun(level);
                    run.place(hatch[0] + (1 - PlusRun.WIDE) / 2, hatch[1] - PlusRun.TALL - .05f);
                    for (float gone = 0; gone < .4f; gone += STEP) run.step(STEP, false, false, false);
                    assertTrue(where + ": stood on the hatch", run.onGround() && run.y() + PlusRun.TALL <= hatch[1] + .1f);
                    run.down(true);
                    for (float gone = 0; gone < 3 && !(run.onGround() && run.y() > ROWS); gone += STEP) run.step(STEP, false, false, false);
                    run.down(false);
                    assertEquals(where, PlusRun.PLAYING, run.state());
                    assertTrue(where + ": in the cave at " + run.y(), run.onGround() && run.y() > ROWS);
                    // Across to the vine, jumping whatever step is in the way.
                    float last = run.x(), since = 0, jumping = 0;
                    for (float gone = 0; gone < 20 && run.x() + PlusRun.WIDE / 2 < vine[0] + .35f; gone += STEP) {
                        boolean stuck = run.x() <= last + .001f;
                        since = stuck ? since + STEP : 0;
                        last = run.x();
                        if (since > .15f && run.onGround()) jumping = .35f;
                        run.step(STEP, false, true, jumping > 0);
                        jumping -= STEP;
                    }
                    for (float gone = 0; gone < 1; gone += STEP) run.step(STEP, false, false, false);
                    assertTrue(where + ": got to the vine at " + run.x(), Math.abs(run.x() + PlusRun.WIDE / 2 - vine[0] - .5f) < .7f);
                    // Up it, and out onto the land.
                    for (float gone = 0; gone < 8 && !(run.onGround() && run.y() + PlusRun.TALL <= ROWS - .5f); gone += STEP) run.step(STEP, false, false, true);
                    for (float gone = 0; gone < 1 && !run.onGround(); gone += STEP) run.step(STEP, false, false, false);
                    assertEquals(where, PlusRun.PLAYING, run.state());
                    assertTrue(where + ": back up at " + run.y(), run.onGround() && run.y() + PlusRun.TALL <= vine[1] + .1f);
                    caves++;
                }
            }
        }
        assertTrue(caves + " caves", caves > 300);
    }

    /** A vine up to the high ledges is climbed to the first of them; each ledge along them is a jump from the last. */
    @Test public void theHighLedgesCanBeClimbedToAndCrossed() {
        int climbed = 0;
        for (int number = 1; number < RunLevels.TOTAL; number++) {
            RunLevels.Level level = calm(number, 5);
            for (int[] way : level.ways) {
                if (way[2] != 3) continue;
                String where = "land " + number + " vine " + way[0];
                PlusRun run = new PlusRun(level);
                run.place(way[0] + (1 - PlusRun.WIDE) / 2, way[1] + 1 - PlusRun.TALL - .05f);
                for (float gone = 0; gone < .3f; gone += STEP) run.step(STEP, false, false, false);
                for (float gone = 0; gone < 4 && run.y() > 2.5f && run.climbing() | gone < .3f; gone += STEP) run.step(STEP, false, false, true);
                for (float gone = 0; gone < 1.2f && !(run.onGround() && run.y() + PlusRun.TALL < 9); gone += STEP) run.step(STEP, false, true, false);
                for (float gone = 0; gone < .6f; gone += STEP) run.step(STEP, false, false, false);
                assertTrue(where + ": on the first ledge at " + run.x() + ", " + run.y(), run.onGround() && run.y() + PlusRun.TALL < 9);
                climbed++;
                // Each ledge after the first: no more than two squares further on, and no more than one higher, than the end of the one before.
                int previousEnd = -1, previousRow = -1;
                for (int column = way[0] + 1; column < way[0] + 30; column++) {
                    int row = -1;
                    for (int up = 2; up < 9; up++) if (level.land[up][column] == RunLevels.LEDGE) row = up;
                    if (row < 0) continue;
                    if (previousEnd >= 0 && column - previousEnd - 1 > 2) break;
                    if (previousEnd >= 0 && column > previousEnd + 1) {
                        assertTrue(where + ": gap at " + column, column - previousEnd - 1 <= 2);
                        assertTrue(where + ": rise at " + column, previousRow - row <= 1);
                    }
                    previousEnd = column;
                    previousRow = row;
                }
            }
        }
        assertTrue(climbed + " climbed", climbed > 30);
    }

    /** What is hidden is worth finding: behind each false wall a chest with more in it than an open one, and something else besides. */
    @Test public void falseWallsHideTheBetterChests() {
        int walls = 0;
        for (int number = 1; number < RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.layered(RunLevels.level(number), 7);
            for (int[] way : level.ways) {
                if (way[2] != 2 || way[1] < ROWS) continue;
                assertEquals(RunLevels.FAKE, level.land[way[1]][way[0]]);
                assertFalse(PlusRun.solid(RunLevels.FAKE));
                int best = 0;
                for (int[] item : level.items) if (item[0] == PlusRun.CHEST && Math.abs(item[1] - way[0]) <= 3 && item[2] == way[1]) best = Math.max(best, item[4]);
                assertTrue("land " + number, best >= 40);
                walls++;
            }
        }
        assertTrue(walls + " walls", walls > 40);
    }
}
