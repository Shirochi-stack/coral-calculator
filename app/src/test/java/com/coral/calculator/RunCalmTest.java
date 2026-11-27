package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The wood behind the land stands on the land's usual ground: steps, ledges and pits leave it where it is; a long climb moves it. */
public class RunCalmTest {
    private static final int FLOOR = RunLevels.FLOOR;

    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(1, columns);
        for (int column = 0; column < columns; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        level.gate = columns - 3;
        return level;
    }

    @Test public void stepsAndPitsLeaveTheWoodWhereItIs() {
        RunLevels.Level level = flat(80);
        // Single steps every few columns, and a pit.
        for (int column = 10; column < 60; column += 6) level.land[FLOOR - 1][column] = RunLevels.BLOCK;
        for (int column = 30; column < 33; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.AIR;
        for (int middle = 12; middle < 60; middle++) assertEquals("at " + middle, FLOOR, RunLevels.usualGround(level, middle), 0);
    }

    @Test public void aLongClimbMovesIt() {
        RunLevels.Level level = flat(120);
        for (int column = 60; column < 120; column++) for (int row = FLOOR - 4; row < FLOOR; row++) level.land[row][column] = RunLevels.BLOCK;
        assertEquals(FLOOR, RunLevels.usualGround(level, 30), 0);
        assertEquals(FLOOR - 4, RunLevels.usualGround(level, 90), 0);
        // And more than the stillness asks for, so it does move there.
        assertTrue(4 > RunLevels.WOOD_STILL);
    }

    @Test public void everyLandsWoodMovesOnlyForRealChanges() {
        // Over every land, walking its length, the wood's ground changes by whole rows only a few times, never back and forth at each step.
        for (int number = 1; number <= RunLevels.TOTAL; number += 5) {
            RunLevels.Level level = RunLevels.level(number);
            float target = RunLevels.usualGround(level, 12);
            int moves = 0;
            for (int middle = 13; middle < level.columns - 12; middle++) {
                float usual = RunLevels.usualGround(level, middle);
                if (usual >= 0 && Math.abs(usual - target) > RunLevels.WOOD_STILL) {
                    target = usual;
                    moves++;
                }
            }
            assertTrue("land " + number + " moves " + moves, moves <= level.columns / 40 + 3);
        }
    }
}
