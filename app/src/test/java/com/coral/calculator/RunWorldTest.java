package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** The world's people and places: one village in each chapter, each its own; homes with secrets; everyone met has something to say. */
public class RunWorldTest {
    private static final float STEP = 1 / 60f;

    @Test public void thereIsOneVillageInEachChapterEachItsOwn() {
        Set<Integer> landmarks = new HashSet<>();
        int villages = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            boolean village = RunLevels.villageLand(number);
            assertEquals("land " + number, village, level.town >= 0);
            if (!village) continue;
            villages++;
            assertEquals(number, RunLevels.firstOf(RunLevels.chapterOf(number)));
            for (int[] decor : level.decor) if (decor[0] >= 410) landmarks.add(decor[0]);
            // Every building of its time: the guild hall, the pet shop, the smithy and a home; the gacha from the fifth land, the market from the ninth.
            Set<Integer> sorts = new HashSet<>();
            for (int[] room : level.rooms) if (room[0] == RunLevels.HOUSE && room[1] >= level.town && room[1] < level.town + 80) sorts.add(room[14]);
            assertTrue("land " + number, sorts.contains(RunLevels.GUILD_HALL) && sorts.contains(RunLevels.PET_SHOP) && sorts.contains(RunLevels.SMITHY) && sorts.contains(RunLevels.HOME));
            assertEquals("land " + number, number >= RunLevels.GACHA_FROM, sorts.contains(RunLevels.GACHA));
            assertEquals("land " + number, number >= RunLevels.MARKET_FROM, sorts.contains(RunLevels.MARKET));
        }
        assertEquals(RunLevels.ALL_CHAPTERS, villages);
        assertEquals(RunLevels.ALL_CHAPTERS, landmarks.size());
        for (int chapter = 1; chapter <= RunLevels.ALL_CHAPTERS; chapter++) assertTrue(new File("src/main/res/drawable-nodpi/run_landmark" + chapter + ".webp").isFile());
        assertTrue(new File("src/main/res/drawable-nodpi/run_village_arch.webp").isFile());
    }

    /** Homes have secrets, of all three kinds, in plenty; the insides of homes are six, and every diary page is somewhere to be found. */
    @Test public void homesHaveSecrets() {
        int hidden = 0, loose = 0, pages = 0;
        Set<Integer> looks = new HashSet<>(), found = new HashSet<>();
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int[] room : level.rooms) {
                if (room[0] != RunLevels.HOUSE || room[14] != RunLevels.HOME) continue;
                looks.add(room[13]);
                for (int column = room[3]; column <= room[4]; column++) if (level.land[RunLevels.FLOOR - 1][column] == RunLevels.FAKE) hidden++;
                for (int[] item : level.items) {
                    if (item[1] < room[3] || item[1] > room[4]) continue;
                    if (item[0] == PlusRun.LOOSE) loose++;
                    if (item[0] == PlusRun.DIARY) {
                        pages++;
                        found.add(item[4]);
                    }
                }
            }
        }
        assertTrue("hidden " + hidden + " loose " + loose + " pages " + pages, hidden >= 25 && loose >= 25 && pages >= 50);
        assertEquals(RunLevels.HOME_LOOKS, looks.size());
        assertEquals(RunLevels.DIARY_PAGES, RunStory.DIARY.length);
        assertEquals(RunLevels.DIARY_PAGES, found.size());
        assertFalse(PlusRun.solid(RunLevels.FAKE));
        for (int home = 1; home <= RunLevels.HOME_LOOKS; home++) assertTrue(new File("src/main/res/drawable-nodpi/run_home" + home + ".webp").isFile());
    }

    /** A loose floorboard comes up when she stands on it with the down button held, and what was under it is hers; a diary page is kept and read. */
    @Test public void aLooseFloorboardAndADiaryPageGiveWhatTheyHold() {
        RunLevels.Level level = null;
        int[] board = null, page = null;
        for (int number = 1; number <= RunLevels.TOTAL && (board == null || page == null); number++) {
            RunLevels.Level here = RunLevels.level(number);
            for (int[] item : here.items) {
                if (item[0] == PlusRun.LOOSE && board == null && page == null) {
                    level = here;
                    board = item;
                }
            }
            if (board != null) for (int[] item : here.items) if (item[0] == PlusRun.DIARY && here == level) page = item;
            if (page == null) board = null;
        }
        assertTrue(board != null && page != null);
        PlusRun run = new PlusRun(level);
        run.place(board[1] + (1 - PlusRun.WIDE) / 2, board[2] + 1 - PlusRun.TALL - .05f);
        for (float gone = 0; gone < .3f; gone += STEP) run.step(STEP, false, false, false);
        assertEquals(0, run.chests());
        run.down(true);
        for (float gone = 0; gone < .3f; gone += STEP) run.step(STEP, false, false, false);
        run.down(false);
        for (float gone = 0; gone < .3f; gone += STEP) run.step(STEP, false, false, false);
        assertEquals(1, run.lifted());
        assertEquals(1, run.chests());
        run.place(page[1] + (1 - PlusRun.WIDE) / 2, page[2] + 1 - PlusRun.TALL - .05f);
        for (float gone = 0; gone < .3f; gone += STEP) run.step(STEP, false, false, false);
        assertEquals(1, run.diaries());
        assertEquals(page[4], run.lastDiary());
    }

    /** Everyone met has a line for each part of the story, and Halgakos an answer to each, in her voice and on her catchphrase; all recorded. */
    @Test public void everyoneMetHasSomethingToSayAndSheAnswers() {
        assertEquals(4, RunStory.TALK.length);
        assertEquals(4, RunStory.ANSWERS.length);
        String speakers = "SUBOPEAWLN";
        for (int stage = 0; stage < 4; stage++) {
            assertEquals(10, RunStory.TALK[stage].length);
            assertEquals(10, RunStory.ANSWERS[stage].length);
            for (int index = 0; index < 10; index++) {
                String[] line = RunStory.TALK[stage][index], answer = RunStory.ANSWERS[stage][index];
                assertEquals(String.valueOf(speakers.charAt(index)), line[2]);
                assertEquals("H", answer[2]);
                assertTrue(answer[1], answer[1].endsWith("Sloppy~♡"));
                assertTrue(line[0], new File("src/main/res/raw/run_v_" + line[0] + ".ogg").isFile());
                assertTrue(answer[0], new File("src/main/res/raw/run_v_" + answer[0] + ".ogg").isFile());
            }
        }
    }
}
