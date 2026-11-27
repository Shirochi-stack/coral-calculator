package com.coral.calculator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

/** Plus run's films and its gallery: which film follows what, which files it is made of, what is written under it, and what opens what. */
public class RunFilmsTest {
    /** What the game remembers, held here in place of the device. */
    private static final class Kept implements Unlocks.Store {
        final Map<String, Integer> numbers = new HashMap<>();

        @Override public int tally(String key) {
            Integer value = numbers.get(key);
            return value == null ? 0 : value;
        }

        @Override public String note(String key) { return null; }

        @Override public void keepNote(String key, String value) {}

        Kept with(String key, int value) {
            numbers.put(key, value);
            return this;
        }
    }

    private static final File MAIN = new File("src/main");

    private static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    // Which film, and when

    @Test public void thePrologueFollowsTheFirstBossOnceAndOnlyWhenSheWon() {
        assertEquals(6, RunLevels.GUARD_LAND);
        assertTrue("someone waits at the end of that land, and at the end of none before it", RunLevels.bossOf(RunLevels.GUARD_LAND) >= 0);
        for (int land = 1; land < RunLevels.GUARD_LAND; land++) assertTrue(RunLevels.bossOf(land) < 0);
        assertEquals(RunFilms.PROLOGUE, RunFilms.due(6, true, 0, 0));
        assertEquals("not when the land was lost", RunFilms.NONE, RunFilms.due(6, false, 0, 0));
        assertEquals("not a second time", RunFilms.NONE, RunFilms.due(6, true, 0, RunFilms.bit(RunFilms.PROLOGUE)));
        assertEquals("the idol song having been played does not stand in its way", RunFilms.PROLOGUE, RunFilms.due(6, true, 2, RunFilms.bit(RunFilms.IDOL)));
        for (int land = 1; land <= RunLevels.TOTAL; land++) {
            if (land != 6 && land != RunLevels.TOTAL) assertEquals("no film after land " + land, RunFilms.NONE, RunFilms.due(land, true, 7, 0));
        }
    }

    @Test public void theIdolSongFollowsTheTrueEndingOnce() {
        assertEquals(RunFilms.IDOL, RunFilms.due(RunLevels.TOTAL, true, 2, 0));
        assertEquals(RunFilms.IDOL, RunFilms.due(RunLevels.TOTAL, true, 7, RunFilms.bit(RunFilms.PROLOGUE) | RunFilms.bit(RunFilms.CLIP)));
        assertEquals("the land unmade is not the true ending", RunFilms.NONE, RunFilms.due(RunLevels.TOTAL, false, 1, 0));
        assertEquals("the Mother Tongue risen is not yet the true ending", RunFilms.NONE, RunFilms.due(RunLevels.TOTAL, true, 5, 0));
        assertEquals("not a second time", RunFilms.NONE, RunFilms.due(RunLevels.TOTAL, true, 2, RunFilms.bit(RunFilms.IDOL)));
    }

    @Test public void eachFilmHasABitOfItsOwn() {
        assertEquals(1, RunFilms.bit(RunFilms.PROLOGUE));
        assertEquals(2, RunFilms.bit(RunFilms.IDOL));
        assertEquals(4, RunFilms.bit(RunFilms.CLIP));
    }

    // Which files

    @Test public void theIdolSongsPictureGoesByTheShapeOfTheScreen() {
        assertTrue(RunFilms.upright(864, 1960));
        assertFalse(RunFilms.upright(1960, 864));
        assertFalse("a square screen is shown the wide one", RunFilms.upright(1000, 1000));
        assertEquals("idol_tall", RunFilms.picture(RunFilms.IDOL, true));
        assertEquals("idol_wide", RunFilms.picture(RunFilms.IDOL, false));
        assertEquals("the prologue has one picture, whatever the screen", RunFilms.picture(RunFilms.PROLOGUE, true), RunFilms.picture(RunFilms.PROLOGUE, false));
        assertEquals("prologue", RunFilms.picture(RunFilms.PROLOGUE, true));
    }

    @Test public void theSoundIsTheOneChosenInTheLanguageChosenOrInEnglish() {
        assertArrayEquals(new String[]{"prologue_h"}, RunFilms.sounds(RunFilms.PROLOGUE, RunFilms.HALGAKOS, RunFilms.ENGLISH, true));
        assertArrayEquals(new String[]{"prologue_h_ja", "prologue_h"}, RunFilms.sounds(RunFilms.PROLOGUE, RunFilms.HALGAKOS, RunFilms.JAPANESE, true));
        assertArrayEquals(new String[]{"idol_h_ko", "idol_h"}, RunFilms.sounds(RunFilms.IDOL, RunFilms.HALGAKOS, RunFilms.KOREAN, false));
        assertArrayEquals(new String[]{"idol_h_zh", "idol_h"}, RunFilms.sounds(RunFilms.IDOL, RunFilms.HALGAKOS, RunFilms.CHINESE, true));
        assertArrayEquals("a language there is none of is English", new String[]{"idol_h"}, RunFilms.sounds(RunFilms.IDOL, RunFilms.HALGAKOS, 9, true));
        assertArrayEquals(new String[]{"prologue_o"}, RunFilms.sounds(RunFilms.PROLOGUE, RunFilms.MODEL, RunFilms.JAPANESE, true));
        assertArrayEquals("the model's sound belongs to its picture", new String[]{"idol_tall_m"}, RunFilms.sounds(RunFilms.IDOL, RunFilms.MODEL, RunFilms.ENGLISH, true));
        assertArrayEquals(new String[]{"idol_wide_m"}, RunFilms.sounds(RunFilms.IDOL, RunFilms.MODEL, RunFilms.ENGLISH, false));
        assertArrayEquals("as she first did it, in the one language she did it in", new String[]{"idol_l"}, RunFilms.sounds(RunFilms.IDOL, RunFilms.LEGACY, RunFilms.KOREAN, true));
        assertArrayEquals(new String[]{"prologue_l"}, RunFilms.sounds(RunFilms.PROLOGUE, RunFilms.LEGACY, RunFilms.ENGLISH, false));
        assertEquals("with nothing chosen it is Halgakos, in English", 0, RunFilms.HALGAKOS + RunFilms.ENGLISH);
    }

    @Test public void everyPictureAndEveryEnglishSoundIsThere() {
        File raw = new File(MAIN, "res/raw");
        for (int film : new int[]{RunFilms.PROLOGUE, RunFilms.IDOL}) {
            for (boolean upright : new boolean[]{true, false}) {
                assertTrue(new File(raw, "run_film_" + RunFilms.picture(film, upright) + ".mp4").isFile());
                for (int choice = RunFilms.HALGAKOS; choice <= RunFilms.LEGACY; choice++) {
                    String[] names = RunFilms.sounds(film, choice, RunFilms.ENGLISH, upright);
                    assertTrue(names[0], new File(raw, "run_film_" + names[0] + ".m4a").isFile());
                    assertTrue("what is heard in " + names[0] + " is written out in English", new File(MAIN, "assets/" + RunFilms.captions(names[0], RunFilms.ENGLISH)).isFile());
                }
            }
        }
        assertTrue(new File(raw, "run_cut_mother.mp4").isFile());
    }

    @Test public void theFilmsAreKeptThoughNothingNamesThem() throws IOException {
        assertTrue(read(new File(MAIN, "res/raw/keep.xml")).contains("@raw/run_film_*"));
    }

    // What is written under a film

    @Test public void whatIsWrittenIsNamedForItsSoundAndLanguageAndOffIsNothing() {
        assertEquals("films/prologue_h.en.txt", RunFilms.captions("prologue_h", RunFilms.ENGLISH));
        assertEquals("films/idol_tall_m.ja.txt", RunFilms.captions("idol_tall_m", RunFilms.JAPANESE));
        assertEquals("films/idol_h_ko.ko.txt", RunFilms.captions("idol_h_ko", RunFilms.KOREAN));
        assertEquals("films/idol_l.zh.txt", RunFilms.captions("idol_l", RunFilms.CHINESE));
        assertNull(RunFilms.captions("prologue_h", RunFilms.NO_CAPTIONS));
        assertNull(RunFilms.captions("prologue_h", -1));
        assertEquals("with nothing chosen what is written is English", 0, RunFilms.ENGLISH);
        assertEquals("one line of hers for each language, in the clip", RunFilms.TONGUES.length, RunFilms.CLIP_SAYS.length);
        assertEquals(RunFilms.TONGUES.length, RunFilms.TONGUE_NAMES.length);
        for (String says : RunFilms.CLIP_SAYS) assertTrue(says, says.endsWith("Sloppy~♡"));
    }

    @Test public void linesAreReadFromTheirFileAndWhatIsNotALineIsLeftOut() {
        List<RunFilms.Line> lines = RunFilms.lines("1.50\t3.00\tSquish! (Sloppy~♡)\nnot a line\n4\t2\tends before it begins\n\n5.0\t7.5\tLights up.\twith a tab in it\n8\t9\t \nx\t9\tno\n");
        assertEquals(2, lines.size());
        assertEquals(1.5f, lines.get(0).from, 1e-6f);
        assertEquals(3f, lines.get(0).to, 1e-6f);
        assertEquals("Squish! (Sloppy~♡)", lines.get(0).words);
        assertEquals("Lights up.\twith a tab in it", lines.get(1).words);
        assertTrue(RunFilms.lines(null).isEmpty());
        assertTrue(RunFilms.lines("").isEmpty());
    }

    @Test public void aLineIsShownFromWhenItBeginsUntilItEnds() {
        List<RunFilms.Line> lines = RunFilms.lines("1\t3\tone\n3\t5\ttwo\n4.5\t6\tthree\n10\t11\tfour\n");
        assertNull(RunFilms.at(lines, .9f));
        assertEquals("one", RunFilms.at(lines, 1f));
        assertEquals("one", RunFilms.at(lines, 2.99f));
        assertEquals("two", RunFilms.at(lines, 3f));
        assertEquals("of two that overlap, the later", "three", RunFilms.at(lines, 4.7f));
        assertEquals("three", RunFilms.at(lines, 5.5f));
        assertNull("nothing between lines", RunFilms.at(lines, 8f));
        assertNull(RunFilms.at(lines, 11f));
        assertNull(RunFilms.at(null, 2f));
    }

    @Test public void everyFileOfLinesIsInOrderAndWithinItsFilm() throws IOException {
        File[] files = new File(MAIN, "assets/films").listFiles();
        assertNotNull(files);
        assertTrue(files.length >= 7);
        for (File file : files) {
            String name = file.getName();
            assertTrue(name, name.matches("(prologue|idol)_[a-z_]+\\.(en|ja|ko|zh)\\.txt"));
            String text = read(file);
            List<RunFilms.Line> lines = RunFilms.lines(text);
            assertEquals(name + ": every row is a line", text.trim().split("\n").length, lines.size());
            assertTrue(name, lines.size() >= 20);
            float last = -1;
            for (RunFilms.Line line : lines) {
                assertTrue(name + ": " + line.words, line.from >= last && line.from >= 0 && line.to <= 91 && line.to - line.from <= 8);
                assertFalse(name + ": a line of two languages", line.words.contains("\n"));
                last = line.from;
            }
        }
    }

    @Test public void whatIsWrittenLiesClearOfThePicturesOwnWriting() {
        // The upright picture has names at its lower left from 80% of its height down, and its title from there too: what is written lies above them.
        assertTrue(RunFilms.foot(true) <= .79f && RunFilms.foot(true) >= .7f);
        // A wide one has them higher: what is written lies at its foot, under them.
        assertTrue(RunFilms.foot(false) >= .97f && RunFilms.foot(false) <= 1f);
        assertEquals(.043f * 1080, RunFilms.size(1080, true, 35), 1e-3f);
        assertEquals("never smaller than can be read", 35f, RunFilms.size(864, false, 35), 1e-3f);
        assertEquals(.024f * 1960, RunFilms.size(1960, false, 35), 1e-3f);
    }

    @Test public void aFilmIsShownWholeUnlessNextToNothingWouldBeLost() {
        float lost = RunVideo.FILM_LOST;
        assertTrue("a film has names at its edge: far less of it may be lost than of a clip", lost <= .1f);
        // The prologue on an upright phone: whole, with black above and below; never turned.
        assertEquals(864 / 1280f, RunVideo.scale(1280, 720, 864, 1960, lost), 1e-4f);
        // The upright idol song on an upright phone that is taller than it, as phones are: whole, with a little black above and below,
        // where a clip would fill the screen and lose a fifth of its width.
        assertEquals(864 / 720f, RunVideo.scale(720, 1280, 864, 1960, lost), 1e-4f);
        assertEquals(1960 / 1280f, RunVideo.scale(720, 1280, 864, 1960), 1e-4f);
        assertEquals(1080 / 720f, RunVideo.scale(720, 1280, 1080, 2400, lost), 1e-4f);
        // On a screen of its own shape, or nearly, it fills it.
        assertEquals(1.5f, RunVideo.scale(720, 1280, 1080, 1920, lost), 1e-4f);
        assertEquals(2000 / 1280f, RunVideo.scale(720, 1280, 1080, 2000, lost), 1e-4f);
        // The wide one on a wide screen: whole on one that is wider than it, filling one of nearly its shape.
        assertEquals(864 / 720f, RunVideo.scale(1280, 720, 1960, 864, lost), 1e-4f);
        assertEquals(2000 / 1280f, RunVideo.scale(1280, 720, 2000, 1080, lost), 1e-4f);
        // A tablet, squarer than either, and a square screen: each is shown whole.
        assertEquals(1600 / 1280f, RunVideo.scale(1280, 720, 1600, 2560, lost), 1e-4f);
        assertEquals(2560 / 1280f, RunVideo.scale(720, 1280, 1600, 2560, lost), 1e-4f);
        assertEquals(2000 / 1280f, RunVideo.scale(1280, 720, 2000, 2000, lost), 1e-4f);
        assertEquals(2000 / 1280f, RunVideo.scale(720, 1280, 2000, 2000, lost), 1e-4f);
    }

    // The gallery

    @Test public void aRuleIsReadOrItIsNotARule() {
        for (String rule : new String[]{"film:1", "film:4", "land:6", "land:100", "open:31", "ending:1", "ending:2", "key:run_hunts:5"}) assertTrue(rule, RunGallery.valid(rule));
        for (String rule : new String[]{"", "film", "film:", "film:x", "land:0", "land:-3", "key:run_hunts", "key::3", "key:run_hunts:0", "always", "film:1:2"}) assertFalse(rule, RunGallery.valid(rule));
    }

    @Test public void whatARuleAsksIsFoundInWhatTheGameKeeps() {
        Kept kept = new Kept();
        assertFalse(RunGallery.holds("film:1", kept));
        assertFalse(RunGallery.holds("land:6", kept));
        assertFalse(RunGallery.holds("ending:2", kept));
        assertFalse(RunGallery.holds("key:run_hunts:5", kept));
        assertTrue("the first land has always been reached", RunGallery.holds("open:1", kept));
        assertFalse(RunGallery.holds("open:2", kept));
        assertFalse("what is not a rule holds for no one", RunGallery.holds("always", kept.with("always", 9)));
        kept.with(RunFilms.SEEN, 5).with("run_stars_6", 1).with("run_ending", 2).with("run_hunts", 5).with("run_open", 31);
        assertTrue(RunGallery.holds("film:1", kept));
        assertFalse(RunGallery.holds("film:2", kept));
        assertTrue(RunGallery.holds("film:4", kept));
        assertTrue(RunGallery.holds("land:6", kept));
        assertFalse(RunGallery.holds("land:7", kept));
        assertTrue(RunGallery.holds("ending:2", kept));
        assertFalse("one ending is not the other", RunGallery.holds("ending:1", kept));
        assertTrue(RunGallery.holds("key:run_hunts:5", kept));
        assertFalse(RunGallery.holds("key:run_hunts:6", kept));
        assertTrue(RunGallery.holds("open:31", kept));
        assertFalse(RunGallery.holds("open:32", kept));
    }

    @Test public void everythingIsLockedUntilItIsOpenedAndEachFilmOpensWhatBelongsToIt() {
        List<RunGallery.Item> all = RunGallery.all(null);
        assertEquals(RunGallery.FIXED.length, all.size());
        Kept kept = new Kept();
        assertEquals("nothing is open to begin with", 0, RunGallery.open(all, kept));
        kept.with(RunFilms.SEEN, RunFilms.bit(RunFilms.PROLOGUE));
        Set<String> open = new HashSet<>();
        for (RunGallery.Item item : all) if (RunGallery.open(item, kept)) open.add(item.name);
        assertEquals(new HashSet<>(java.util.Arrays.asList("video_prologue", "board_prologue", "cast_prologue")), open);
        kept.with(RunFilms.SEEN, RunFilms.bit(RunFilms.IDOL));
        open.clear();
        for (RunGallery.Item item : all) if (RunGallery.open(item, kept)) open.add(item.name);
        assertEquals(new HashSet<>(java.util.Arrays.asList("video_idol", "board_idol_tall", "board_idol_wide", "cast_idol")), open);
        kept.with(RunFilms.SEEN, RunFilms.bit(RunFilms.CLIP));
        assertEquals("the clip opens itself alone", 1, RunGallery.open(all, kept));
        kept.with(RunFilms.SEEN, 7);
        assertEquals(all.size(), RunGallery.open(all, kept));
        int videos = 0;
        for (RunGallery.Item item : all) {
            assertFalse(item.name + " says where it opens", item.hint.isEmpty());
            assertEquals(item.name, item.shelf == RunGallery.VIDEOS, item.film != RunFilms.NONE);
            if (item.shelf == RunGallery.VIDEOS) videos++;
        }
        assertEquals("four videos in the app, the idol song's two pictures being one of them here", 3, videos);
    }

    @Test public void thePicturesAreRowsOfTheirTable() {
        List<RunGallery.Item> pictures = RunGallery.pictures("# name\ttitle\thint\trule\np001\tThe first land\tLand 1\tland:1\nbad row\np002\tNo rule\tHint\tnever\n\np003\tAn ending\tAn ending\tending:1\textra\n");
        assertEquals(2, pictures.size());
        assertEquals("p001", pictures.get(0).name);
        assertEquals("The first land", pictures.get(0).title);
        assertEquals("Land 1", pictures.get(0).hint);
        assertEquals(RunGallery.PICTURES, pictures.get(0).shelf);
        assertEquals("p003", pictures.get(1).name);
        assertTrue(RunGallery.open(pictures.get(1), new Kept().with("run_ending", 1)));
        assertFalse(RunGallery.open(pictures.get(1), new Kept().with("run_ending", 2)));
        assertTrue(RunGallery.pictures(null).isEmpty());
        assertEquals(RunGallery.FIXED.length + 2, RunGallery.all("p001\tA\tB\tland:1\np002\tC\tD\topen:3\n").size());
    }

    @Test public void everyRowOfTheRealTableIsAPictureThatIsThere() throws IOException {
        File table = new File(MAIN, "assets/gallery/pictures.tsv");
        assertTrue(table.isFile());
        String text = read(table);
        List<RunGallery.Item> pictures = RunGallery.pictures(text);
        int rows = 0;
        for (String row : text.split("\n")) if (!row.startsWith("#") && !row.trim().isEmpty()) rows++;
        assertEquals("every row is a picture", rows, pictures.size());
        Set<String> names = new HashSet<>();
        int unmade = 0, truly = 0;
        for (RunGallery.Item item : pictures) {
            assertTrue(item.name + " has a name of its own", names.add(item.name));
            assertTrue(item.name + " is there", new File(MAIN, "assets/gallery/" + item.name + ".webp").isFile());
            assertFalse(item.name, item.title.isEmpty() || item.hint.isEmpty());
            if (item.rule.equals("ending:1")) unmade++;
            if (item.rule.equals("ending:2")) truly++;
            if (item.rule.startsWith("ending:")) assertEquals("an ending is not told of beforehand", "An ending", item.hint);
        }
        // (When the pictures of the endings are in, each ending has five of its own at the least.)
        assertTrue(unmade == 0 || unmade >= 5);
        assertTrue(truly == 0 || truly >= 5);
        for (RunGallery.Item item : RunGallery.FIXED) assertTrue(item.name, new File(MAIN, "assets/gallery/" + item.name + ".webp").isFile());
    }

    @Test public void sheSaysThatSomethingNewIsInTheGallery() {
        assertTrue(RunStory.GALLERY.length >= 4);
        Set<String> names = new HashSet<>();
        for (String[] line : RunStory.GALLERY) {
            assertTrue(names.add(line[0]));
            assertEquals("H", line[2]);
            assertTrue(line[1], line[1].endsWith("Sloppy~♡"));
            assertTrue(line[1] + ": says plainly where", line[1].toLowerCase(java.util.Locale.ROOT).contains("gallery") || line[1].contains("Cringe"));
            assertTrue(line[0] + " is recorded", new File(MAIN, "res/raw/run_v_" + line[0] + ".ogg").isFile());
        }
    }
}
