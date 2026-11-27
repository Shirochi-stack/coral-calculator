package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

/** The lands as one world: each runs on into the next past its gate, with no card between; the gate is a teleporter; stars do not go by time. */
public class RunConnectedTest {
    private static final float STEP = 1 / 60f;

    private static void hold(PlusRun run, float seconds, boolean right) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, false, right, false);
    }

    /** Passing the gate clears the land once; she goes on past it; further on she goes into the way to the next land. */
    @Test public void theGateClearsTheLandAndSheGoesOnIntoTheNext() {
        for (int number : new int[]{1, 7, 31, 55, 63}) {
            RunLevels.Level level = RunLevels.level(number);
            // (Where someone waits at the gate she has to be dealt with first: those lands are crossed by the bot in the other tests.)
            if (level.guarded || level.arena >= 0) continue;
            PlusRun run = new PlusRun(level);
            run.place(level.gate - 1.5f, RunLevels.top(level, level.gate - 1) - PlusRun.TALL - .05f);
            hold(run, 1.5f, true);
            assertEquals("land " + number, PlusRun.WON, run.state());
            assertFalse(run.through());
            run.carryOn();
            assertEquals(PlusRun.PLAYING, run.state());
            assertTrue(run.passed());
            // On to the way on: nothing stops her, and the gate does not win the land again.
            for (float gone = 0; gone < 6 && !run.through(); gone += STEP) {
                run.step(STEP, false, true, false);
                assertEquals("land " + number + " at " + run.x(), PlusRun.PLAYING, run.state());
            }
            assertTrue("land " + number + " stopped at " + run.x() + " of " + level.main, run.through());
        }
    }

    /** Between the gate and the way on the ground is level and open, in every land: she can always walk from the one to the other. */
    @Test public void theWayOnCanBeReachedFromEveryGate() {
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int floor = RunLevels.top(level, level.gate);
            assertTrue("land " + number, level.main - level.gate >= RunLevels.PAST_GATE);
            for (int column = level.gate + 1; column < level.main; column++) {
                assertEquals("land " + number + " column " + column, floor, RunLevels.top(level, column));
                for (int row = floor - 3; row < floor; row++) assertEquals("land " + number, RunLevels.AIR, level.land[row][column]);
            }
            assertTrue("land " + number, level.main - PlusRun.THROUGH > level.gate + 3);
        }
    }

    /** From the teleporter she comes out at a land's gate, as one she has been through: it takes her nowhere, and on she goes. */
    @Test public void theTeleporterPutsHerAtAGateSheHasFound() {
        for (int number : new int[]{2, 6, 12, 40, 80}) {
            RunLevels.Level level = RunLevels.level(number);
            PlusRun run = new PlusRun(level);
            run.atGate();
            assertTrue(run.passed());
            assertTrue(run.x() > level.gate - 1 && run.x() < level.gate + 3);
            hold(run, .5f, false);
            assertTrue(run.onGround());
            assertEquals(PlusRun.PLAYING, run.state());
            for (float gone = 0; gone < 6 && !run.through(); gone += STEP) run.step(STEP, false, true, false);
            assertTrue("land " + number + " at " + run.x() + " of " + level.main, run.through());
            assertEquals(PlusRun.PLAYING, run.state());
        }
    }

    /** The stars go by how much was collected and nothing else; a cellar's rank by the hurts, not the time. */
    @Test public void starsAndRanksDoNotGoByTime() {
        assertEquals(1, PlusRun.starsFor(0));
        assertEquals(1, PlusRun.starsFor(.59f));
        assertEquals(2, PlusRun.starsFor(.6f));
        assertEquals(3, PlusRun.starsFor(.9f));
        assertEquals(3, PlusRun.starsFor(1));
        assertEquals(PlusRun.RANK_S, PlusRun.rank(5, 0, 0));
        assertEquals(PlusRun.RANK_S, PlusRun.rank(100000, 2, 0));
        assertEquals(PlusRun.RANK_A, PlusRun.rank(1, 3, 0));
        assertEquals(PlusRun.RANK_C, PlusRun.rank(1, 0, 1));
    }

    /** The game: no card at a land's gate but in the last land; the stars kept under the names they always were; a way on drawn for every chapter. */
    @Test public void thereIsNoCardBetweenLands() throws Exception {
        String game = new String(Files.readAllBytes(new File("src/main/java/com/coral/calculator/RunGame.java").toPath()), StandardCharsets.UTF_8);
        assertTrue(game.contains("if (onward()) clearLand();"));
        assertTrue(game.contains("private boolean onward() { return blackRung == 0 && number < RunLevels.OPEN_TO && finale == NO_FINALE; }"));
        // (What was kept before is read as it was: the stars of a land, the land open furthest.)
        assertTrue(game.contains("session.keep(\"run_stars_\" + number, stars)"));
        assertTrue(game.contains("session.keep(\"run_open\", number + 1)"));
        // (And the films come where they did: the prologue after its land, the idol song after the true ending.)
        assertTrue(game.contains("RunFilms.due(number, true, session.tally(\"run_ending\"), session.tally(RunFilms.SEEN))"));
        for (int chapter = 1; chapter <= RunStory.CHAPTERS.length; chapter++) {
            assertTrue("chapter " + chapter, new File("src/main/res/drawable-nodpi/run_way" + chapter + ".webp").isFile());
        }
        for (String[] line : RunStory.WARP) {
            assertTrue(line[1].endsWith("Sloppy~♡"));
            assertTrue(new File("src/main/res/raw/run_v_" + line[0] + ".ogg").isFile());
        }
    }
}
