package com.coral.calculator;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** How many creatures a land has (and a dungeon floor), from the first lands to the last; and that none is laid where she starts. */
public class RunDensityTest {
    /** The creatures of a land, not counting those who wait at its end, targets, or what is in the places off the land. */
    static int mobs(RunLevels.Level level) {
        int count = 0;
        for (int[] foe : level.foes) if (foe[1] < level.main && !RunLevels.boss(foe[0]) && foe[0] != RunLevels.TARGET) count++;
        return count;
    }

    /** How many are laid within so many squares of where she starts. */
    static int nearStart(RunLevels.Level level, int squares) {
        int count = 0;
        for (int[] foe : level.foes) if (foe[1] < level.main && !RunLevels.boss(foe[0]) && foe[0] != RunLevels.TARGET && foe[1] < 2 + squares) count++;
        return count;
    }

    @Test public void printsHowManyEveryLandAndFloorHas() {
        StringBuilder out = new StringBuilder("DENSITY");
        for (int number : new int[]{1, 2, 3, 5, 8, 10, 15, 20, 30, 50, 75, 100}) {
            RunLevels.Level level = RunLevels.level(number);
            out.append("\n land ").append(number).append(": ").append(mobs(level)).append(" in ").append(level.columns).append(" columns (").append(String.format("%.2f", mobs(level) * 10f / level.columns)).append(" per ten), near start ").append(nearStart(level, 14));
        }
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon += 3) {
            for (int floor : new int[]{0, 4}) {
                if (floor >= RunDungeons.floors(dungeon)) continue;
                RunLevels.Level level = RunLevels.dungeon(dungeon, floor, 31 + dungeon);
                out.append("\n dungeon ").append(dungeon).append(" floor ").append(floor).append(": ").append(mobs(level)).append(" in ").append(level.columns).append(" columns, near start ").append(nearStart(level, 14));
            }
        }
        System.out.println(out);
    }

    @Test public void noLandHasTooManyAtItsStart() {
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            assertTrue("land " + number + " has " + nearStart(level, 10) + " within ten squares of the start", nearStart(level, 10) <= 2);
            assertTrue("land " + number + " has " + nearStart(level, 6) + " within six squares of the start", nearStart(level, 6) == 0);
        }
    }

    /** The first lands are no longer left nearly bare (the first land of a chapter is mostly its village, which is calm, and has least); a floor of a dungeon has more than it had. */
    @Test public void earlyLandsAndDungeonFloorsAreDenserThanTheyWere() {
        // What they had before: lands 2, 3, 5, 8 and 10 had 22, 22, 43, 131 and 159; and floors 0 and 4 of the first dungeon 7 and 20.
        assertTrue(mobs(RunLevels.level(2)) >= 40);
        assertTrue(mobs(RunLevels.level(3)) >= 45);
        assertTrue(mobs(RunLevels.level(5)) >= 70);
        assertTrue(mobs(RunLevels.level(8)) >= 150);
        assertTrue(mobs(RunLevels.level(10)) >= 180);
        assertTrue(mobs(RunLevels.dungeon(0, 0, 31)) >= 9);
        assertTrue(mobs(RunLevels.dungeon(0, 4, 31)) >= 23);
        // And more of them the further on it goes.
        assertTrue(mobs(RunLevels.level(10)) > mobs(RunLevels.level(5)) && mobs(RunLevels.level(5)) > mobs(RunLevels.level(2)));
    }

    /** None is laid where she starts or in a village: nothing there has to be got past before she has had a look round. */
    @Test public void noneIsLaidInAVillageOrWhereSheStarts() {
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            if (level.town < 0) continue;
            int end = level.town + RunLevels.townWide(RunLevels.townOf(number).length);
            for (int[] foe : level.foes) {
                if (RunLevels.boss(foe[0]) || foe[1] >= level.main) continue;
                assertTrue("land " + number + " has one at " + foe[1] + " in its village " + level.town + "-" + end, foe[1] < level.town - 2 || foe[1] > end);
            }
        }
    }
}