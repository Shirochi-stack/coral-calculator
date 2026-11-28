package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/** The twelve creatures of the later chapters: two more for each of chapters six to eleven, tougher, each fighting its own way. */
public class RunLaterUniquesTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    private static int kind(int way) {
        for (int index = 0; index < RunUniques.LATER_COUNT; index++) if (RunUniques.way(RunUniques.LATER_FIRST + index) == way) return RunUniques.LATER_FIRST + index;
        throw new AssertionError("no " + RunUniques.WAY_NAMES[way]);
    }

    /** One of them alone in a flat land, and her standing at a column. */
    private static PlusRun alone(int kind, int column, float herColumn) {
        RunLevels.Level level = new RunLevels.Level(70, 60);
        for (int c = 0; c < 60; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 57;
        level.foes.add(new int[]{kind, column, RunUniques.floats(kind) ? FLOOR - 3 : FLOOR - 1, 1, 0, 2});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 40;
        PlusRun run = new PlusRun(level, perks);
        run.place(herColumn, FLOOR - PlusRun.TALL);
        return run;
    }

    private static void wait(PlusRun run, float seconds) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, false, false, false);
    }

    @Test public void twelveMoreTwoForEachLaterChapterAndTougher() {
        assertEquals(75, RunUniques.COUNT);
        assertEquals(12, RunUniques.LATER_COUNT);
        Set<String> names = new HashSet<>();
        Set<Integer> ways = new HashSet<>();
        for (int index = 0; index < RunUniques.COUNT; index++) assertTrue(names.add(RunUniques.NAMES[index]));
        for (int index = 0; index < RunUniques.LATER_COUNT; index++) {
            int kind = RunUniques.LATER_FIRST + index;
            assertTrue(RunUniques.later(kind));
            assertTrue(ways.add(RunUniques.way(kind)));
            int chapter = RunUniques.chapter(kind);
            assertEquals(RunUniques.LATER_CHAPTER + index / 2, chapter);
            assertEquals(RunUniques.LATER_FIRST + index / 2 * 2, RunUniques.laterOf(chapter));
            // Tougher than both of the chapter's first two, in a land of that chapter.
            int land = 31 + chapter * 6;
            for (int old = 0; old < 2; old++) {
                int before = RunUniques.first(chapter) + old;
                if (RunUniques.way(before) == RunUniques.MINIBOSS) continue;
                assertTrue(RunUniques.name(kind) + " vs " + RunUniques.name(before), RunUniques.health(kind, land) > RunUniques.health(before, land));
            }
            assertTrue(RunStory.LATER_UNIQUES[index][1].endsWith("Sloppy~♡"));
            assertTrue(new java.io.File("src/main/res/raw/run_v_" + RunStory.LATER_UNIQUES[index][0] + ".ogg").isFile());
        }
        assertEquals(-1, RunUniques.laterOf(5));
        assertTrue(new java.io.File("../runart/src/main/assets/" + RunUniques.LATER_SHEET).isFile());
    }

    @Test public void theLaterChaptersLandsMeetThemAndTheEarlierOnesDoNot() {
        int[] seen = new int[12];
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int[] foe : level.foes) {
                if (!RunUniques.later(foe[0])) continue;
                assertTrue("land " + number, RunLevels.chapterOf(number) >= RunUniques.LATER_CHAPTER);
                assertEquals("land " + number, Math.min(11, RunLevels.chapterOf(number)), RunUniques.chapter(foe[0]));
                seen[foe[0] - RunUniques.LATER_FIRST]++;
            }
        }
        for (int index = 0; index < 12; index++) assertTrue(RunUniques.NAMES[RunUniques.index(RunUniques.LATER_FIRST) + index] + " " + seen[index], seen[index] > 0);
    }

    @Test public void thePillbugRollsAndComesBackOffAWall() {
        PlusRun run = alone(kind(RunUniques.ROLLER), 22, 16);
        PlusRun.Foe foe = run.foes().get(0);
        float from = foe.x, least = from;
        for (int step = 0; step < 240 && run.state() == PlusRun.PLAYING; step++) {
            run.step(STEP, false, false, false);
            least = Math.min(least, foe.x);
        }
        assertTrue(from - least > 3);
        assertTrue(run.windUps() > 0);
    }

    @Test public void theHeraldShoutsWordsAtHer() {
        PlusRun run = alone(kind(RunUniques.HERALD), 24, 18);
        wait(run, 3);
        assertTrue(run.uniqueShots() >= 3);
    }

    @Test public void theSerpentLashesFromOutOfArmsReach() {
        PlusRun run = alone(kind(RunUniques.LASHER), 22, 18.8f);
        int hp = run.hp();
        wait(run, 2.5f);
        assertTrue(run.hp() < hp);
    }

    @Test public void theStrikethroughsLineCanBeJumped() {
        PlusRun run = alone(kind(RunUniques.LINER), 26, 19);
        int hp = run.hp();
        wait(run, 3);
        assertTrue("struck through on the ground", run.hp() < hp);
        // Up on a high ledge over its line, never.
        RunLevels.Level level = new RunLevels.Level(70, 60);
        for (int c = 0; c < 60; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        for (int c = 16; c < 22; c++) level.land[FLOOR - 4][c] = RunLevels.BLOCK;
        level.gate = 57;
        level.foes.add(new int[]{kind(RunUniques.LINER), 26, FLOOR - 1, 1, 0, 2});
        PlusRun high = new PlusRun(level, new PlusRun.Perks());
        high.place(19, FLOOR - 4 - PlusRun.TALL);
        int was = high.hp();
        for (int step = 0; step < 180; step++) high.step(STEP, false, false, false);
        assertEquals(was, high.hp());
    }

    @Test public void theToadsTonguePullsHerIn() {
        PlusRun run = alone(kind(RunUniques.PULLER), 24, 19.5f);
        for (int step = 0; step < 240 && run.tongued() == 0; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.tongued());
        float before = run.x();
        for (int step = 0; step < 6; step++) run.step(STEP, false, false, false);
        assertTrue(run.x() > before + .3f);
    }

    @Test public void smearsSlowHerAndSoDoesTheSandGlass() {
        PlusRun smudge = alone(kind(RunUniques.SMEARER), 30, 22);
        wait(smudge, 4);
        assertFalse(smudge.smears().isEmpty());
        // She runs through a smear at half her pace.
        PlusRun run = alone(RunLevels.MINUS, 50, 10), plain = alone(RunLevels.MINUS, 50, 10);
        run.smears().add(new float[]{13.5f, FLOOR, 6});
        for (int step = 0; step < 50; step++) {
            run.step(STEP, false, true, false);
            plain.step(STEP, false, true, false);
        }
        assertTrue(run.x() < plain.x() - .3f);
        // By the sand-glass, too.
        PlusRun sand = alone(kind(RunUniques.HOURGLASS), 22, 20.5f);
        sand.foes().get(0).stunned = 999;
        PlusRun open = alone(RunLevels.MINUS, 50, 20.5f);
        for (int step = 0; step < 20; step++) {
            sand.step(STEP, true, false, false);
            open.step(STEP, true, false, false);
        }
        assertTrue(20.5f - sand.x() < (20.5f - open.x()) * .7f);
    }

    @Test public void theMirrorCrabSendsBackWhatComesAtItsFront() {
        PlusRun run = alone(kind(RunUniques.REFLECTOR), 24, 18);
        run.arm(RunArms.PEN);
        PlusRun.Foe crab = run.foes().get(0);
        crab.stunned = 999;
        crab.leftward = true;
        run.step(STEP, false, false, false, true);
        for (int step = 0; step < 40; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.mirrored());
        assertEquals(crab.most, crab.left);
        // From behind, before it has turned round to her, it tells.
        PlusRun back = alone(kind(RunUniques.REFLECTOR), 24, 28);
        back.arm(RunArms.PEN);
        PlusRun.Foe other = back.foes().get(0);
        other.stunned = 999;
        other.leftward = true;
        back.step(STEP, true, false, false, true);
        for (int step = 0; step < 40; step++) back.step(STEP, false, false, false);
        assertEquals(0, back.mirrored());
        assertTrue(other.left < other.most);
    }

    @Test public void theEchoAnswersWhatSheDoes() {
        PlusRun run = alone(kind(RunUniques.ECHOER), 24, 19);
        run.arm(RunArms.KNIFE);
        wait(run, 1);
        int shots = run.uniqueShots();
        run.step(STEP, true, false, false, true);
        wait(run, 1.5f);
        assertTrue(run.uniqueShots() >= shots + 2);
    }

    @Test public void theHermitIsOnlyHitWhenOutOfItsShell() {
        PlusRun run = alone(kind(RunUniques.HERMIT), 13, 11);
        run.arm(RunArms.KNUCKLES);
        PlusRun.Foe hermit = run.foes().get(0);
        for (int step = 0; step < 120 && hermit.turn == 0; step++) run.step(STEP, false, false, false);
        assertEquals(1, hermit.turn);
        int clangs = run.clangs(), left = hermit.left;
        run.place(hermit.x - 1.4f, FLOOR - PlusRun.TALL);
        run.step(STEP, false, false, false, true);
        assertEquals(left, hermit.left);
        assertTrue(run.clangs() > clangs);
    }

    @Test public void theWyrmlingBreathesFireAndTheSentinelsBeamReaches() {
        PlusRun run = alone(kind(RunUniques.BREATHER), 22, 18.5f);
        wait(run, 3);
        assertTrue(run.uniqueShots() >= 4);
        // The beam: on her within its reach, never past it.
        PlusRun near = alone(kind(RunUniques.SWEEPER), 22, 17);
        int hp = near.hp();
        wait(near, 9);
        assertTrue(near.hp() < hp);

    }
}
