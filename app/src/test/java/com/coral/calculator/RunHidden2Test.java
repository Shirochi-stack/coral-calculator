package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** The six later hidden bosses: one of its own for each even chapter from the third and one more in the last, with its own Rose piece, picture and words. */
public class RunHidden2Test {
    @Test public void theyAreHiddenBossesAfterTheShadowCast() {
        assertEquals(RunUniques.CAST_FIRST + RunUniques.CAST_COUNT, RunUniques.HIDDEN2_FIRST);
        assertEquals(RunUniques.LILITH, RunUniques.HIDDEN2_FIRST + RunUniques.HIDDEN2_COUNT);
        assertEquals(RunUniques.FIRST + RunUniques.COUNT, RunUniques.LILITH + 1);
        String[] names = {"The Overdue Notice", "The Papercut Queen", "The Gilded Bookworm", "The Wax Seal Tyrant", "The Footnote King", "The Last Librarian"};
        for (int at = 0; at < RunUniques.HIDDEN2_COUNT; at++) {
            int kind = RunUniques.HIDDEN2_FIRST + at;
            assertTrue(RunUniques.hidden(kind));
            assertTrue(RunUniques.hidden2(kind));
            assertEquals(names[at], RunUniques.name(kind));
            assertEquals(Math.min(11, at * 2 + 2), RunUniques.chapter(kind));
            assertTrue(RunUniques.WIDE[RunUniques.index(kind)] >= 1.5f && RunUniques.TALL[RunUniques.index(kind)] >= 1.5f);
        }
        assertFalse(RunUniques.hidden2(RunUniques.HIDDEN_FIRST));
        assertFalse(RunUniques.hidden2(RunUniques.HIDDEN2_FIRST - 1));
    }

    @Test public void eachChapterWithHideoutsHasAHiddenBossOfItsOwn() {
        Set<Integer> bosses = new HashSet<>();
        for (int chapter = 1; chapter < 12; chapter++) {
            int boss = RunUniques.hiddenOf(chapter);
            assertTrue(RunUniques.hidden(boss));
            assertEquals(chapter % 2 == 0, RunUniques.hidden2(boss));
            assertEquals(chapter, RunUniques.chapter(boss));
            bosses.add(boss);
        }
        assertEquals(11, bosses.size());
        // And all twelve are met in hideouts, each in its own chapter (the last chapter's two by turns).
        Set<Integer> met = new HashSet<>();
        for (int number = 1; number < RunLevels.OPEN_TO; number++) {
            for (int[] foe : RunLevels.level(number).foes) {
                if (!RunUniques.hidden(foe[0])) continue;
                met.add(foe[0]);
                assertEquals(RunLevels.chapterOf(number), RunUniques.chapter(foe[0]));
            }
        }
        assertEquals(RunUniques.HIDDEN_COUNT + RunUniques.HIDDEN2_COUNT, met.size());
    }

    @Test public void eachHasItsOwnShotDefeatAndRosePiece() {
        Set<Integer> shots = new HashSet<>(), pieces = new HashSet<>();
        for (int at = 0; at < RunUniques.HIDDEN2_COUNT; at++) {
            int kind = RunUniques.HIDDEN2_FIRST + at;
            shots.add(RunUniques.hiddenShot(kind));
            int piece = RunUniques.signature(kind);
            assertEquals(RunLoot.ROSE, RunLoot.rarity(piece));
            pieces.add(piece);
            assertTrue(RunUniques.defeat(kind) != PlusRun.DEFEAT_FADE || at == 4);
        }
        assertEquals(6, shots.size());
        assertEquals(6, pieces.size());
        assertEquals(PlusRun.DEFEAT_MELT, RunUniques.defeat(RunUniques.HIDDEN2_FIRST + 3));
    }

    @Test public void theirPictureIsInTheRunPackAndTheirWordsAreSpoken() throws Exception {
        java.io.File sheet = new java.io.File("../runart/src/main/assets/" + RunUniques.HIDDEN2_SHEET);
        assertTrue(sheet.isFile());
        assertTrue(new java.io.File("../assets/plus-run/uniques_hidden2/unique_librarian.png").isFile());
        assertEquals(RunUniques.HIDDEN2_COUNT, RunStory.HIDDEN2.length);
        for (String[] line : RunStory.HIDDEN2) {
            assertEquals("H", line[2]);
            assertTrue(line[1], line[1].endsWith(" Sloppy~♡"));
            assertFalse(line[1], line[1].contains(", Sloppy"));
            assertTrue(line[0], RunVoicesTest.recorded(line[0]));
        }
    }
}
