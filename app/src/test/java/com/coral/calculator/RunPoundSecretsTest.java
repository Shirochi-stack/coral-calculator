package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Cracked ground, which only a ground pound breaks, and the places under it: what breaks it and what does not, how strong a pound each crack
 * asks for, that every place has a way back out, and that nothing under a crack can be the end of her.
 */
public class RunPoundSecretsTest {
    private static final float STEP = 1 / 60f;
    private static final int ROWS = RunLevels.ROWS;
    private static final int FLOOR = 10;

    /** Flat ground with a patch of cracked squares in it (a stack of them, of one strength), and rock under all of it. */
    private static RunLevels.Level flat(char crack, int depth) {
        RunLevels.Level level = new RunLevels.Level(40, 40);
        for (int c = 0; c < 40; c++) for (int r = FLOOR; r < ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        for (int c = 18; c <= 22; c++) for (int r = FLOOR; r < FLOOR + (c == 20 ? depth : 1); r++) level.land[r][c] = crack;
        level.gate = 37;
        return level;
    }

    private static PlusRun.Perks perks(int pound) {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 40;
        perks.main = PlusRun.SWORD;
        perks.pound = pound > 0;
        perks.poundLevel = pound;
        return perks;
    }

    private static void hold(PlusRun run, float seconds, boolean jump, boolean strike, boolean down) {
        run.down(down);
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, false, false, jump, strike);
        run.down(false);
    }

    /** She comes down from a height onto a column, with the down button held or not. */
    private static PlusRun comeDown(RunLevels.Level level, int pound, boolean pounding, float middle, float height) {
        PlusRun run = new PlusRun(level, perks(pound));
        run.place(middle - PlusRun.WIDE / 2, FLOOR - height);
        // (The button is let go once she has landed, so that she does not pound again as she falls on through what she broke.)
        run.down(pounding);
        for (float gone = 0; gone < 2 && run.pounds() == 0 && !(run.onGround() && gone > .2f); gone += STEP) run.step(STEP, false, false, false);
        run.down(false);
        for (float gone = 0; gone < .3f; gone += STEP) run.step(STEP, false, false, false);
        return run;
    }

    // What breaks it

    @Test public void cracksNeverBreakToWalkingStrikingOrFalling() {
        RunLevels.Level level = flat(RunLevels.CRACK, 1);
        // Walked over, and stood on, with the move bought.
        PlusRun run = new PlusRun(level, perks(3));
        run.place(15, FLOOR - PlusRun.TALL - .02f);
        for (float gone = 0; gone < 3; gone += STEP) run.step(STEP, false, true, false, true);
        assertTrue(run.x() > 22);
        for (int column = 18; column <= 22; column++) assertEquals(RunLevels.CRACK, run.at(column, FLOOR));
        // Stood on and struck at, over and over.
        run = new PlusRun(level, perks(3));
        run.place(20.2f, FLOOR - PlusRun.TALL - .02f);
        for (float gone = 0; gone < 2; gone += STEP) run.step(STEP, false, false, false, true);
        assertEquals(RunLevels.CRACK, run.at(20, FLOOR));
        assertEquals(0, run.crackBursts());
        // Fallen onto from a great height, without a pound (the move not bought, the button not held, and then the button held without the move).
        run = comeDown(level, 3, false, 20.5f, 8);
        assertTrue(run.onGround());
        assertEquals(RunLevels.CRACK, run.at(20, FLOOR));
        run = comeDown(level, 0, true, 20.5f, 8);
        assertTrue(run.onGround());
        assertEquals(RunLevels.CRACK, run.at(20, FLOOR));
        assertEquals(0, run.crackBursts());
        // Jumped on, ten times.
        run = new PlusRun(level, perks(3));
        run.place(20.2f, FLOOR - PlusRun.TALL - .02f);
        for (int jump = 0; jump < 10; jump++) {
            hold(run, .35f, true, false, false);
            hold(run, 1, false, false, false);
        }
        assertEquals(RunLevels.CRACK, run.at(20, FLOOR));
    }

    @Test public void aPoundBreaksTheCrackUnderHerAndTheOnesBesideHerAndNothingFurther() {
        RunLevels.Level level = flat(RunLevels.CRACK, 1);
        PlusRun run = comeDown(level, 1, true, 20.5f, 6);
        assertEquals(1, run.pounds());
        assertEquals(1, run.crackBursts());
        // Under her and one to either side (the first step reaches one square out); the two beyond stand.
        for (int column = 19; column <= 21; column++) assertTrue("column " + column, run.shattered(column, FLOOR));
        assertEquals(RunLevels.AIR, run.at(20, FLOOR));
        assertFalse(run.shattered(18, FLOOR));
        assertFalse(run.shattered(22, FLOOR));
        assertEquals(RunLevels.CRACK, run.at(18, FLOOR));
        // The bigger the pound the wider: the third step takes all five.
        run = comeDown(level, 3, true, 20.5f, 6);
        for (int column = 18; column <= 22; column++) assertTrue("column " + column, run.shattered(column, FLOOR));
        assertEquals(5, run.crackLog().size());
        // A pound that lands on solid ground beside it breaks what is in reach and no more.
        run = comeDown(level, 2, true, 24.5f, 6);
        assertTrue(run.shattered(22, FLOOR));
        assertFalse(run.shattered(21, FLOOR));
        // Gone for good, and she goes through: down into what is under it.
        for (float gone = 0; gone < 3; gone += STEP) run.step(STEP, false, false, false);
        assertEquals(RunLevels.AIR, run.at(22, FLOOR));
    }

    @Test public void aWeakPoundCannotBreakAStrongCrack() {
        for (char crack : new char[]{RunLevels.CRACK2, RunLevels.CRACK3}) {
            int need = RunLevels.crackNeed(crack);
            RunLevels.Level level = flat(crack, 2);
            for (int pound = 1; pound <= 3; pound++) {
                PlusRun run = comeDown(level, pound, true, 20.5f, 6);
                assertEquals(1, run.pounds());
                if (pound < need) {
                    assertEquals("a step " + pound + " pound on a crack of " + need, 0, run.shattered(20, FLOOR) ? 1 : 0);
                    assertEquals(crack, run.at(20, FLOOR));
                    assertEquals(0, run.crackBursts());
                    assertEquals(1, run.crackHolds());
                    assertTrue(run.onGround());
                } else {
                    // The whole stack goes at once, however deep it is.
                    assertTrue(run.shattered(20, FLOOR));
                    assertTrue(run.shattered(20, FLOOR + 1));
                    assertEquals(1, run.crackBursts());
                }
            }
        }
    }

    @Test public void aPoundBreaksAStackToItsFootAndNoFurther() {
        RunLevels.Level level = flat(RunLevels.CRACK, 1);
        // A stack of four, which is more than any pound goes through: three squares.
        for (int row = FLOOR; row < FLOOR + 4; row++) level.land[row][20] = RunLevels.CRACK;
        PlusRun run = comeDown(level, 3, true, 20.5f, 6);
        for (int row = FLOOR; row < FLOOR + 3; row++) assertTrue(run.shattered(20, row));
        assertFalse(run.shattered(20, FLOOR + 3));
    }

    @Test public void theCountsAndTheHintFollowTheCracks() {
        RunLevels.Level level = flat(RunLevels.CRACK, 1);
        PlusRun run = new PlusRun(level, perks(1));
        run.place(17, FLOOR - PlusRun.TALL - .02f);
        for (float gone = 0; gone < 1.5f; gone += STEP) run.step(STEP, false, true, false);
        assertTrue("stood on it: " + run.crackStood(), run.crackStood() >= 1);
        // Without the move bought, there is nothing to break it with: it only holds.
        assertEquals(RunLevels.CRACK, run.at(19, FLOOR));
    }

    // The places under it

    /** A land as laid for a play, with no one in it to knock her about. */
    private static RunLevels.Level calm(int number, long play) {
        RunLevels.Level level = RunLevels.layered(RunLevels.level(number), play);
        level.foes.clear();
        level.guarded = false;
        return level;
    }

    private static RunLevels.Level calmFloor(int dungeon, int floor, long dive) {
        RunLevels.Level level = RunLevels.dungeon(dungeon, floor, dive);
        level.foes.clear();
        level.guarded = false;
        return level;
    }

    private static List<int[]> hazards(RunLevels.Level level, int[] vault) {
        List<int[]> found = new ArrayList<>();
        for (int column = vault[7] - 1; column <= vault[8] + 1; column++) {
            for (int row = ROWS - 1; row < ROWS + RunLevels.LOWER; row++) {
                char here = level.land[row][column];
                if (here == RunLevels.SPIKE || here == RunLevels.LAVA || here == RunLevels.WATER) found.add(new int[]{column, row});
            }
        }
        return found;
    }

    /** Whether what is under a place is what makes it safe: ground over rock, a clear shaft to a chamber with a floor, a vine all the way up to a hatch. */
    private static void checkVault(RunLevels.Level level, int[] vault, String where) {
        int shaft = vault[1], ground = vault[2], stack = vault[5], from = vault[7], to = vault[8], floor = vault[9], exit = vault[10], hatch = vault[11];
        assertTrue(where, vault[6] >= 1 && vault[6] <= 3);
        char crack = level.land[ground][shaft];
        assertEquals(where, vault[6], RunLevels.crackNeed(crack));
        for (int row = ground; row < ground + stack; row++) assertEquals(where + " stack", crack, level.land[row][shaft]);
        // The shaft under the crack is clear to the chamber, which has a floor of rock to come down onto, and nothing in it to hurt.
        for (int row = ground + stack; row <= floor; row++) assertEquals(where + " shaft at " + row, RunLevels.AIR, level.land[row][shaft]);
        for (int column = from; column <= to; column++) {
            assertEquals(where + " floor at " + column, RunLevels.BLOCK, level.land[floor + 1][column]);
            assertTrue(where + " under", level.under[column]);
        }
        assertTrue(where + " hazards", hazards(level, vault).isEmpty());
        // The cracked patch lies over rock and over nothing that is not the shaft: no pit, no spikes, no lava beside or under it.
        for (int column = vault[3]; column <= vault[4]; column++) {
            assertTrue(where + " patch", RunLevels.crackNeed(level.land[ground][column]) > 0);
            if (column != shaft) assertEquals(where + " rock under the patch", RunLevels.BLOCK, level.land[ROWS - 1][column]);
            assertNotEquals(where, RunLevels.SPIKE, level.land[ground - 1][column]);
            assertNotEquals(where, RunLevels.LAVA, level.land[ground - 1][column]);
        }
        // A vine from the floor of the chamber to the hatch, which is in the ground, with sky over it.
        for (int row = hatch + 1; row <= floor; row++) assertEquals(where + " vine at " + row, RunLevels.VINE, level.land[row][exit]);
        assertEquals(where, RunLevels.THIN, level.land[hatch][exit]);
        assertEquals(where, RunLevels.AIR, level.land[hatch - 1][exit]);
        assertTrue(where, exit > from && exit < to && shaft > from && shaft < to && Math.abs(exit - shaft) >= 5);
        // The hatch is the way up only: from above it is not given way to.
        assertTrue(where, level.ways.stream().anyMatch(way -> way[0] == exit && way[1] == hatch && way[2] == RunLevels.WAY_CLIMB));
        assertTrue(where, level.ways.stream().anyMatch(way -> way[0] == shaft && way[1] == ground && way[2] == RunLevels.WAY_SHAFT));
    }

    /** She drops into a vault by a third-step pound, walks to its vine and climbs out onto the land. */
    private static void goInAndOut(RunLevels.Level level, int[] vault, String where) {
        int shaft = vault[1], ground = vault[2], exit = vault[10], hatch = vault[11];
        PlusRun run = new PlusRun(level, perks(3));
        // (From a jump's height over it: the sky over cracked ground is clear for three squares, and no more is promised.)
        run.place(shaft + (1 - PlusRun.WIDE) / 2, ground - PlusRun.TALL - 1.5f);
        run.down(true);
        for (float gone = 0; gone < 3 && run.pounds() == 0; gone += STEP) run.step(STEP, false, false, false);
        run.down(false);
        assertEquals(where, 1, run.pounds());
        for (float gone = 0; gone < 4 && !(run.onGround() && run.y() > ROWS); gone += STEP) run.step(STEP, false, false, false);
        assertEquals(where, PlusRun.PLAYING, run.state());
        assertTrue(where + ": in the chamber at " + run.y(), run.onGround() && run.y() > ROWS);
        assertTrue(where + ": found", (run.roomsFound() >> (RunLevels.VAULT_BIT + level.vaults.indexOf(vault)) & 1) != 0);
        assertEquals(where, 1, run.vaultsOpened());
        // Across to the vine, then up it and out.
        // (Steering by where she is against the vine: the way across is a flat floor.)
        for (float gone = 0; gone < 6; gone += STEP) {
            float off = run.x() + PlusRun.WIDE / 2 - exit - .5f;
            run.step(STEP, off > .08f, off < -.08f, false);
        }
        assertTrue(where + ": got to the vine at " + run.x() + " exit " + exit + " kind " + vault[0] + " chamber " + vault[7] + ".." + vault[8] + " y " + run.y() + " ground " + run.onGround(),Math.abs(run.x() + PlusRun.WIDE / 2 - exit - .5f) < .7f);
        for (float gone = 0; gone < 8 && !(run.onGround() && run.y() + PlusRun.TALL <= ROWS - .5f); gone += STEP) run.step(STEP, false, false, true);
        for (float gone = 0; gone < 1 && !run.onGround(); gone += STEP) run.step(STEP, false, false, false);
        assertEquals(where, PlusRun.PLAYING, run.state());
        assertTrue(where + ": back up at " + run.x() + ", " + run.y() + " climbing " + run.climbing() + " exit " + exit + " kind " + vault[0] + " chamber " + vault[7] + ".." + vault[8], run.onGround() && run.y() + PlusRun.TALL <= hatch + .1f);
        // And standing on the hatch with the down button held, she is not let down through it.
        run.down(true);
        for (float gone = 0; gone < .6f; gone += STEP) run.step(STEP, false, false, false);
        run.down(false);
        assertTrue(where + ": still on the hatch", run.onGround() && run.y() + PlusRun.TALL <= hatch + .1f);
    }

    @Test public void everyLandsPlacesAreSafeAndHaveAWayOutAndAreFound() {
        int lands = 0, with = 0, vaults = 0;
        int[] kinds = new int[4];
        for (int number = 2; number < RunLevels.OPEN_TO; number++) {
            for (long play = 1; play <= 2; play++) {
                RunLevels.Level level = calm(number, play);
                lands++;
                if (!level.vaults.isEmpty()) with++;
                assertTrue("land " + number + " has " + level.vaults.size(), level.vaults.size() <= 3);
                assertTrue("rooms " + level.rooms.size(), level.rooms.size() < RunLevels.VAULT_BIT);
                for (int[] vault : level.vaults) {
                    String where = "land " + number + " play " + play + " vault at " + vault[1];
                    checkVault(level, vault, where);
                    goInAndOut(level, vault, where);
                    kinds[vault[0]]++;
                    vaults++;
                }
            }
        }
        System.out.println("POUND lands " + lands + " with " + with + " vaults " + vaults + " kinds " + java.util.Arrays.toString(kinds));
        assertTrue(with + " of " + lands, with >= lands * 9 / 10);
        for (int kind = 0; kind < 3; kind++) assertTrue("kind " + kind, kinds[kind] > 40);
    }

    @Test public void theCrackAsksForAStrongerPoundLaterOn() {
        int[][] counts = new int[RunLevels.CHAPTERS * 3 + 1][4];
        for (int number = 2; number < RunLevels.OPEN_TO; number++) {
            for (int[] vault : calm(number, 1).vaults) counts[RunLevels.chapterOf(number)][vault[6]]++;
        }
        for (int chapter = 0; chapter < 2; chapter++) assertEquals(0, counts[chapter][2] + counts[chapter][3]);
        int hard = 0, mid = 0;
        for (int[] chapter : counts) {
            hard += chapter[3];
            mid += chapter[2];
        }
        assertTrue(hard > 10 && mid > 10);
    }

    @Test public void theEarlyLandsHaveOneAndTheLaterOnesMore() {
        int early = 0, late = 0, earlyLands = 0, lateLands = 0;
        for (int number = 2; number < RunLevels.OPEN_TO; number++) {
            int count = calm(number, 3).vaults.size();
            if (RunLevels.chapterOf(number) < 2) {
                early += count;
                earlyLands++;
            } else if (RunLevels.chapterOf(number) >= 6) {
                late += count;
                lateLands++;
            }
        }
        assertTrue(early + " in " + earlyLands, early >= earlyLands * 8 / 10 && early <= earlyLands);
        assertTrue(late + " in " + lateLands + " against " + early + " in " + earlyLands, late > lateLands && late * earlyLands > early * lateLands * 12 / 10);
    }

    @Test public void dungeonFloorsHaveCachesAndVaultsToo() {
        int floors = 0, with = 0, caches = 0, vaults = 0;
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon++) {
            for (int floor = 0; floor < RunDungeons.floors(dungeon); floor++) {
                RunLevels.Level level = calmFloor(dungeon, floor, 31 + dungeon);
                floors++;
                if (floor == 0) assertTrue(level.vaults.isEmpty());
                if (!level.vaults.isEmpty()) with++;
                for (int[] vault : level.vaults) {
                    String where = "dungeon " + dungeon + " floor " + floor + " vault at " + vault[1];
                    checkVault(level, vault, where);
                    goInAndOut(level, vault, where);
                    if (vault[0] == RunLevels.V_CACHE) {
                        caches++;
                        int marked = 0;
                        for (int[] item : level.items) if (item[0] == PlusRun.CHEST && item.length > 5 && item[5] == PlusRun.CACHE && item[1] >= vault[7] && item[1] <= vault[8]) marked++;
                        assertEquals(where, 1, marked);
                    } else {
                        vaults++;
                    }
                }
            }
        }
        System.out.println("POUND floors " + floors + " with " + with + " caches " + caches + " vaults " + vaults);
        assertTrue(with + " of " + floors, with >= (floors - RunDungeons.COUNT) * 8 / 10);
        assertTrue(caches > 80 && vaults > 20);
    }

    @Test public void theMirrorIsLeftAlone() {
        for (int floor = 0; floor < RunDungeons.MIRROR_FLOORS; floor++) assertTrue(RunLevels.dungeon(RunDungeons.MIRROR, floor, 77).vaults.isEmpty());
    }

    @Test public void aVaultsContentsAreWhatItsKindSaysAndOnlyThePoundGetsInto() {
        int checked = 0;
        for (int number = 2; number < RunLevels.OPEN_TO; number += 2) {
            RunLevels.Level level = calm(number, 5);
            RunLevels.Level base = RunLevels.level(number);
            for (int[] vault : level.vaults) {
                int chests = 0, pluses = 0;
                for (int[] item : level.items) if (item[0] == PlusRun.CHEST && item[1] >= vault[7] && item[1] <= vault[8] && item[2] > ROWS) chests++;
                for (int[] plus : level.pluses) if (plus[0] >= vault[7] && plus[0] <= vault[8] && plus[1] > ROWS) pluses++;
                assertTrue("chests " + chests, chests >= 1);
                assertTrue("pluses " + pluses, pluses >= 1);
                if (vault[0] == RunLevels.V_VAULT) assertTrue(chests >= 2);
                // Nothing but the pound gets in: without it she cannot go down the shaft, nor through the hatch of its vine.
                PlusRun run = new PlusRun(level, perks(0));
                run.place(vault[1] + (1 - PlusRun.WIDE) / 2, vault[2] - PlusRun.TALL - .02f);
                run.down(true);
                for (float gone = 0; gone < 1; gone += STEP) run.step(STEP, false, false, false);
                assertTrue(run.onGround() && run.y() + PlusRun.TALL <= vault[2] + .1f);
                run = new PlusRun(level, perks(3));
                run.place(vault[10] + (1 - PlusRun.WIDE) / 2, vault[11] - PlusRun.TALL - .02f);
                run.down(true);
                for (float gone = 0; gone < 1; gone += STEP) run.step(STEP, false, false, false);
                assertTrue("not through the hatch", run.y() + PlusRun.TALL <= vault[11] + .1f);
                // (The land above is the land as it was, but for the cracked squares and the hatch and the shaft.)
                for (int column = 0; column < base.main; column++) {
                    for (int row = 0; row < ROWS; row++) {
                        if (RunLevels.crackNeed(level.land[row][column]) > 0) assertEquals(RunLevels.BLOCK, base.land[row][column]);
                    }
                }
                checked++;
            }
        }
        assertTrue(checked > 40);
    }

    @Test public void aBrokenCrackStaysBrokenInACopy() {
        RunLevels.Level level = flat(RunLevels.CRACK, 1);
        PlusRun run = comeDown(level, 3, true, 20.5f, 6);
        PlusRun copy = run.copy();
        assertTrue(copy.shattered(20, FLOOR));
        assertEquals(RunLevels.AIR, copy.at(20, FLOOR));
        assertEquals(run.crackBursts(), copy.crackBursts());
        assertEquals(run.crackLog().size(), copy.crackLog().size());
    }
}
