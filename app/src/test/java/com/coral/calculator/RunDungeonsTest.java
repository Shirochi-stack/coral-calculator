package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** F: the dungeons, their floors laid afresh for every dive, those that live in them, their keepers, and what a dive gives. */
public class RunDungeonsTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    @Test public void twentyFourDungeonsTwoToAChapterEachFiveFloorsDeepOrMore() {
        assertEquals(24, RunDungeons.COUNT);
        assertEquals(RunDungeons.COUNT, RunDungeons.NAMES.length);
        assertEquals(RunDungeons.COUNT, RunDungeons.LANDS.length);
        int[] perChapter = new int[RunLevels.ALL_CHAPTERS];
        Set<String> names = new HashSet<>();
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon++) {
            int land = RunDungeons.land(dungeon);
            assertTrue(RunDungeons.floors(dungeon) >= 5);
            // (Never where someone waits at the end.)
            assertEquals("land " + land, -1, RunLevels.bossOf(land));
            assertEquals(dungeon, RunDungeons.inLand(land));
            perChapter[RunLevels.chapterOf(land)]++;
            assertTrue(names.add(RunDungeons.NAMES[dungeon]));
            // Three of its own kinds, all different, and a keeper.
            int[] kinds = RunDungeons.delvers(dungeon);
            assertEquals(3, new HashSet<>(Arrays.asList(kinds[0], kinds[1], kinds[2])).size());
            for (int kind : kinds) assertTrue(RunUniques.delver(kind));
            assertTrue(RunUniques.keeper(RunDungeons.keeper(dungeon)));
        }
        for (int count : perChapter) assertEquals(2, count);
        assertEquals(8, RunDungeons.floors(RunDungeons.COUNT - 1));
    }

    @Test public void theStairwayStandsInItsLandAndNowhereElse() {
        for (int number = 1; number < RunLevels.OPEN_TO; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int stairs = RunLevels.stairsOf(level);
            assertEquals("land " + number, RunDungeons.inLand(number) >= 0, stairs >= 0);
            if (stairs < 0) continue;
            System.out.println("STAIRS land " + number + " column " + stairs + " of " + level.main);
            // On the ground of the land proper, with room over it, before the gate.
            assertTrue(stairs < level.gate);
            int floor = RunLevels.top(level, stairs);
            assertTrue(floor > 0);
            for (int row = floor - 3; row < floor; row++) assertEquals(RunLevels.AIR, level.land[row][stairs]);
        }
    }

    @Test public void everyDiveIsLaidAfreshButTheSameDiveIsTheSame() {
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon += 5) {
            RunLevels.Level one = RunLevels.dungeon(dungeon, 1, 101), again = RunLevels.dungeon(dungeon, 1, 101), other = RunLevels.dungeon(dungeon, 1, 202);
            assertEquals(one.columns, again.columns);
            for (int row = 0; row < one.land.length; row++) assertTrue(Arrays.equals(one.land[row], again.land[row]));
            boolean differs = one.columns != other.columns;
            for (int row = 0; row < Math.min(one.land.length, other.land.length) && !differs; row++) differs = !Arrays.equals(one.land[row], other.land[row]);
            assertTrue("dungeon " + dungeon, differs);
            // And floors of the same dive differ from each other.
            assertNotEquals(RunLevels.dungeon(dungeon, 0, 101).columns * 1000 + RunLevels.dungeon(dungeon, 0, 101).foes.size(),
                    RunLevels.dungeon(dungeon, 2, 101).columns * 1000 + RunLevels.dungeon(dungeon, 2, 101).foes.size());
        }
    }

    @Test public void aFloorHasItsOwnCreaturesAChestAndOnTheLastFloorItsKeeper() {
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon++) {
            int floors = RunDungeons.floors(dungeon);
            for (int floor : new int[]{0, floors - 1}) {
                RunLevels.Level level = RunLevels.dungeon(dungeon, floor, 7 + dungeon);
                int delvers = 0, keepers = 0, chests = 0;
                for (int[] foe : level.foes) {
                    if (RunUniques.delver(foe[0])) delvers++;
                    if (RunUniques.keeper(foe[0])) {
                        keepers++;
                        assertEquals(RunDungeons.keeper(dungeon), foe[0]);
                        // In its hall, between the last of the way and the gate.
                        assertTrue(foe[1] < level.gate && foe[1] > level.gate - RunLevels.KEEPER_HALL - 6);
                    }
                }
                for (int[] item : level.items) if (item[0] == PlusRun.CHEST) chests++;
                assertTrue("dungeon " + dungeon + " floor " + floor + ": " + delvers, delvers >= 2);
                assertEquals(floor == floors - 1 ? 1 : 0, keepers);
                assertTrue(chests >= 1);
                assertEquals(0, level.quest);
                assertEquals(-1, level.arena);
                assertEquals(1, level.flags.size());
            }
        }
    }

    @Test public void floorsAreOfSomeKindsAndTheFirstAndLastArePlain() {
        int[] seen = new int[RunDungeons.KINDS];
        for (long dive = 1; dive < 60; dive++) {
            for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon += 3) {
                assertEquals(RunDungeons.PLAIN, RunDungeons.kindOf(dungeon, 0, dive));
                assertEquals(RunDungeons.PLAIN, RunDungeons.kindOf(dungeon, RunDungeons.floors(dungeon) - 1, dive));
                for (int floor = 1; floor < RunDungeons.floors(dungeon) - 1; floor++) seen[RunDungeons.kindOf(dungeon, floor, dive)]++;
            }
        }
        for (int kind = 0; kind < RunDungeons.KINDS; kind++) assertTrue("kind " + kind, seen[kind] > 0);
        // A teeming floor has more creatures than a quiet one.
        long teeming = -1, quiet = -1;
        for (long dive = 1; dive < 400 && (teeming < 0 || quiet < 0); dive++) {
            if (RunDungeons.kindOf(10, 2, dive) == RunDungeons.TEEMING && teeming < 0) teeming = dive;
            if (RunDungeons.kindOf(10, 2, dive) == RunDungeons.QUIET && quiet < 0) quiet = dive;
        }
        assertTrue(count(RunLevels.dungeon(10, 2, teeming)) > count(RunLevels.dungeon(10, 2, quiet)));
    }

    private static int count(RunLevels.Level level) {
        int count = 0;
        for (int[] foe : level.foes) if (RunUniques.is(foe[0])) count++;
        return count;
    }

    @Test public void threeDifferentBlessingsAreOfferedAndMendingOnlyWhenHurt() {
        for (long dive = 1; dive < 200; dive++) {
            for (int floor = 0; floor < 6; floor++) {
                int[] whole = RunDungeons.offered(dive, floor, false, 0), hurt = RunDungeons.offered(dive, floor, true, 1 << RunDungeons.LIFE);
                assertEquals(3, new HashSet<>(Arrays.asList(whole[0], whole[1], whole[2])).size());
                assertEquals(3, new HashSet<>(Arrays.asList(hurt[0], hurt[1], hurt[2])).size());
                for (int one : whole) assertNotEquals(RunDungeons.MEND, one);
                for (int one : hurt) assertNotEquals(RunDungeons.LIFE, one);
            }
        }
        assertEquals(RunDungeons.BLESSINGS, RunDungeons.BLESSING_NAMES.length);
        assertEquals(RunDungeons.BLESSINGS, RunDungeons.BLESSING_WORDS.length);
    }

    @Test public void aRelicForEachDungeonRedOrBetter() {
        Set<Integer> relics = new HashSet<>();
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon++) {
            int relic = RunDungeons.relic(dungeon);
            assertTrue(RunLoot.rarity(relic) >= RunLoot.RED);
            assertTrue(relics.add(relic));
            assertFalse(RunLoot.name(relic).isEmpty());
            assertTrue(RunDungeons.pluses(dungeon, true) == RunDungeons.pluses(dungeon, false) * 3);
        }
    }

    @Test public void theKeeperShutsTheWayOutUntilItIsBeaten() {
        RunLevels.Level level = RunLevels.dungeon(0, RunDungeons.floors(0) - 1, 5);
        PlusRun run = new PlusRun(level, new PlusRun.Perks());
        assertTrue(run.keeperStands());
        assertFalse(run.gateOpen());
        for (PlusRun.Foe foe : run.foes()) if (RunUniques.keeper(foe.kind)) foe.flat = true;
        assertFalse(run.keeperStands());
        assertTrue(run.gateOpen());
    }

    /** A flat land, walled at its ends, with one creature in it and her standing at a place. */
    private static PlusRun alone(int kind, int column, int row, float herColumn) {
        RunLevels.Level level = new RunLevels.Level(20, 60);
        for (int c = 0; c < 60; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 57;
        level.foes.add(new int[]{kind, column, row, 1, 0, 2});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 30;
        PlusRun run = new PlusRun(level, perks);
        run.place(herColumn, FLOOR - PlusRun.TALL);
        return run;
    }

    private static void wait(PlusRun run, float seconds) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, false, false, false);
    }

    @Test public void eachOfTheSevenFightsItsOwnWay() {
        // The moth blinks to behind her.
        PlusRun run = alone(RunUniques.DELVER_FIRST, 26, FLOOR - 3, 22);
        wait(run, 6);
        assertTrue("blinks", run.blinks() > 0);
        // The spider hangs over the way, and comes down when she is under it.
        run = alone(RunUniques.DELVER_FIRST + 1, 24, FLOOR - 1, 14);
        PlusRun.Foe spider = run.foes().get(0);
        assertTrue(spider.y < spider.ground - 3);
        run.place(24, FLOOR - PlusRun.TALL);
        wait(run, 2);
        assertTrue(run.spidersDropped() > 0 || spider.flat);
        assertTrue(spider.flat || spider.y >= spider.ground - .01f);
        // The frog leaps at her, and comes down on the ground.
        run = alone(RunUniques.DELVER_FIRST + 2, 28, FLOOR - 1, 24);
        PlusRun.Foe frog = run.foes().get(0);
        float from = frog.x;
        boolean up = false;
        for (float gone = 0; gone < 4 && run.state() == PlusRun.PLAYING; gone += STEP) {
            run.place(22, FLOOR - PlusRun.TALL);
            run.step(STEP, false, false, false);
            up |= frog.y < frog.ground - .3f;
        }
        assertTrue(run.leaps() > 0 && up);
        assertTrue(frog.x != from);
        assertEquals(frog.ground, frog.y, .01f);
        // The wisp flings its quills, and its quills going round cut her.
        run = alone(RunUniques.DELVER_FIRST + 3, 28, FLOOR - 3, 23);
        int shots = run.uniqueShots();
        wait(run, 6);
        assertTrue(run.uniqueShots() >= shots + 3);
        // The stamp springs at her.
        run = alone(RunUniques.DELVER_FIRST + 4, 28, FLOOR - 1, 24);
        wait(run, 4);
        assertTrue(run.leaps() > 0);
        // The anvil tome slams the ground, and the blow runs along it both ways.
        run = alone(RunUniques.DELVER_FIRST + 5, 28, FLOOR - 1, 25);
        wait(run, 5);
        assertTrue(run.stamps() > 0);
        // Her double goes where she would be in a mirror.
        run = alone(RunUniques.DELVER_FIRST + 6, 30, FLOOR - 1, 20);
        PlusRun.Foe twin = run.foes().get(0);
        float home = twin.x;
        for (float gone = 0; gone < 1.5f; gone += STEP) run.step(STEP, true, false, false);
        assertTrue("mirror " + home + " " + twin.x + " her " + run.x(), twin.x > home + .5f);
    }

    @Test public void aKeeperFightsInThreePhasesAndCallsUpItsOwnAtTheLast() {
        PlusRun run = alone(RunUniques.KEEPER_FIRST + 2, 32, FLOOR - 1, 25);
        PlusRun.Foe keeper = run.foes().get(0);
        wait(run, 8);
        assertTrue(run.windUps() >= 2);
        assertTrue(run.stamps() > 0 || run.uniqueShots() > 0);
        // In its last third it calls up those of its dungeon.
        keeper.left = keeper.most / 5;
        int before = run.calledUp();
        wait(run, 12);
        assertTrue(run.calledUp() > before);
        for (PlusRun.Foe foe : run.foes()) if (foe != keeper && !RunUniques.keeper(foe.kind)) assertTrue(RunUniques.delver(foe.kind));
    }

    @Test public void aKeeperBeatenDropsThreeThingsOfGoldOrBetter() {
        PlusRun run = alone(RunUniques.KEEPER_FIRST, 32, FLOOR - 1, 20);
        PlusRun.Foe keeper = run.foes().get(0);
        keeper.left = 1;
        run.deal(keeper);
        wait(run, .2f);
        assertTrue(keeper.flat);
        assertEquals(1, run.keepersBeaten());
        assertTrue(run.gateOpen());
    }

    @Test public void everyFiveFloorsAreABandOfTheirOwnGrittierTheDeeper() {
        for (int floor = 0; floor < 5; floor++) assertEquals(0, RunDungeons.band(floor));
        for (int floor = 5; floor < 10; floor++) assertEquals(1, RunDungeons.band(floor));
        // (Past the tenth floor, if floors ever go so deep, the deepest look.)
        for (int floor = 10; floor < 40; floor++) assertEquals(RunDungeons.BANDS - 1, RunDungeons.band(floor));
        // The deepest dungeons reach the second band.
        assertTrue(RunDungeons.band(RunDungeons.floors(RunDungeons.COUNT - 1) - 1) >= 1);
        // Each band has its backdrops and its squares in the app.
        for (String picture : new String[]{"run_depths_deep_catacomb", "run_depths_deep_inkcrypt", "run_depths_deep_papercave", "run_depths_deep_vault",
                "run_depths_abyss", "run_ground_depths", "run_ground_rooms", "run_room_lagoon", "run_room_cellar", "halgakos_climb"}) {
            assertTrue(picture, new java.io.File("src/main/res/drawable-nodpi/" + picture + ".webp").isFile());
        }
    }

    @Test public void heartsAndLivesAreCarriedDown() {
        PlusRun run = new PlusRun(RunLevels.dungeon(3, 1, 9), new PlusRun.Perks());
        run.carry(PlusRun.HALF * 3, 1);
        assertEquals(PlusRun.HALF * 3, run.hp());
        assertEquals(1, run.lives());
        run.mend();
        assertEquals(run.maxHp(), run.hp());
    }
}
