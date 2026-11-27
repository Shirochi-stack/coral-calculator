package com.coral.calculator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Plus run's short clips: the table they are listed in, when each is due, what files it is made of, and what it opens in the gallery. */
public class RunClipsTest {
    private static final class Kept implements Unlocks.Store {
        final Map<String, Integer> numbers = new HashMap<>();

        @Override public int tally(String key) {
            Integer value = numbers.get(key);
            return value == null ? 0 : value;
        }

        @Override public String note(String key) { return null; }

        @Override public void keepNote(String key, String value) {}
    }

    private static final File MAIN = new File("src/main");

    @Test public void theClipsAreRowsOfTheirTable() {
        List<RunClips.Clip> clips = RunClips.parse("# number\ttitle\thint\tland\twhen\n1\tThe first\tLand 1\t1\tbefore\nbad row\n2\tThe second\tLand 3\t3\tafter\n3\tNo moment\tLand 4\t4\tsometime\n\n04\tAnother\tLand 3\t3\tafter\textra\n");
        assertEquals(3, clips.size());
        assertEquals("The first", clips.get(0).title);
        assertTrue(clips.get(0).before);
        assertFalse(clips.get(1).before);
        assertEquals(4, clips.get(2).number);
        assertTrue(RunClips.parse(null).isEmpty());
    }

    @Test public void aClipIsDueOnceAtItsMomentAndInTheOrderOfTheTable() {
        List<RunClips.Clip> clips = RunClips.parse("1\tA\tL\t5\tbefore\n2\tB\tL\t5\tafter\n3\tC\tL\t5\tafter\n");
        Kept kept = new Kept();
        assertEquals(1, RunClips.due(clips, 5, true, kept));
        assertEquals(2, RunClips.due(clips, 5, false, kept));
        assertEquals(RunClips.NONE, RunClips.due(clips, 6, true, kept));
        kept.numbers.put(RunClips.SEEN + 2, 1);
        assertEquals("the next of the same moment", 3, RunClips.due(clips, 5, false, kept));
        kept.numbers.put(RunClips.SEEN + 3, 1);
        assertEquals(RunClips.NONE, RunClips.due(clips, 5, false, kept));
        assertEquals("another moment is not touched", 1, RunClips.due(clips, 5, true, kept));
    }

    @Test public void aClipsFilesAreNamedByItsNumberAndTheLanguageOfHerVoice() {
        assertEquals("run_clip_07", RunClips.picture(7));
        assertArrayEquals(new String[]{"clip_07_h"}, RunClips.sounds(7, RunFilms.ENGLISH));
        assertArrayEquals(new String[]{"clip_17_h_ko", "clip_17_h"}, RunClips.sounds(17, RunFilms.KOREAN));
        assertEquals("films/clip_17_h_ko.ja.txt", RunFilms.captions(RunClips.sounds(17, RunFilms.KOREAN)[0], RunFilms.JAPANESE));
    }

    @Test public void eachClipOpensItsFilmAndItsStoryboardWhenItHasBeenSeen() {
        List<RunGallery.Item> all = RunGallery.all(null, "5\tThe clip\tLand 5\t5\tafter\n");
        assertEquals(RunGallery.FIXED.length + 2, all.size());
        RunGallery.Item film = all.get(all.size() - 2), board = all.get(all.size() - 1);
        assertEquals(RunGallery.VIDEOS, film.shelf);
        assertEquals(RunClips.BASE + 5, film.film);
        assertEquals(RunGallery.BOARDS, board.shelf);
        assertEquals(RunFilms.NONE, board.film);
        Kept kept = new Kept();
        assertFalse(RunGallery.open(film, kept));
        kept.numbers.put(RunClips.SEEN + 5, 1);
        assertTrue(RunGallery.open(film, kept));
        assertTrue(RunGallery.open(board, kept));
    }

    @Test public void everyClipOfTheRealTableHasItsFilesInFourLanguages() throws IOException {
        File table = new File(MAIN, "assets/clips/clips.tsv");
        assertTrue(table.isFile());
        String text = new String(Files.readAllBytes(table.toPath()), StandardCharsets.UTF_8);
        List<RunClips.Clip> clips = RunClips.parse(text);
        int rows = 0;
        for (String row : text.split("\n")) if (!row.startsWith("#") && !row.trim().isEmpty()) rows++;
        assertEquals("every row is a clip", rows, clips.size());
        Set<Integer> numbers = new HashSet<>();
        for (RunClips.Clip clip : clips) {
            assertTrue("clip " + clip.number + " has a number of its own", numbers.add(clip.number));
            assertFalse(clip.title.isEmpty() || clip.hint.isEmpty());
            assertTrue(clip.land >= 1 && clip.land <= RunLevels.TOTAL + 100);
            assertTrue(clip.number + " picture", new File(MAIN, "res/raw/" + RunClips.picture(clip.number) + ".mp4").isFile());
            assertTrue(clip.number + " tile", new File(MAIN, "assets/gallery/video_clip" + RunClips.two(clip.number) + ".webp").isFile());
            assertTrue(clip.number + " storyboard", new File(MAIN, "assets/gallery/board_clip" + RunClips.two(clip.number) + ".webp").isFile());
            for (int tongue = 0; tongue < RunFilms.TONGUES.length; tongue++) {
                String sound = RunClips.sounds(clip.number, tongue)[0];
                assertTrue(clip.number + " voice " + RunFilms.TONGUES[tongue], new File(MAIN, "res/raw/run_" + sound + ".m4a").isFile());
                for (int script = 0; script < RunFilms.TONGUES.length; script++) {
                    assertTrue(sound + " written in " + RunFilms.TONGUES[script], new File(MAIN, "assets/" + RunFilms.captions(sound, script)).isFile());
                }
            }
        }
    }
}
