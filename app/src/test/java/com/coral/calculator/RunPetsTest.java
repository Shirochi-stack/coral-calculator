package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/** The pets of those she has beaten: when each can be had, and that they are nowhere until then. */
public class RunPetsTest {
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

    @Test public void theFirstTenAreHadFromTheFirstAndTheRestAreNot() {
        assertEquals(14, PlusRun.PETS);
        assertEquals(10, RunPets.FOUND);
        Kept nothing = new Kept();
        for (int kind = 0; kind < RunPets.FOUND; kind++) assertTrue(RunPets.open(kind, nothing));
        for (int kind = RunPets.FOUND; kind < PlusRun.PETS; kind++) assertFalse("kind " + kind, RunPets.open(kind, nothing));
        assertEquals(10, RunPets.open(nothing));
        assertFalse(RunPets.open(-1, nothing));
        assertFalse(RunPets.open(PlusRun.PETS, nothing));
    }

    @Test public void eachOpensWhenSheIsBeatenInHerLand() {
        assertEquals(18, RunPets.land(PlusRun.MINI_ILLYA));
        assertEquals(24, RunPets.land(PlusRun.MINI_TSUBAKIEH));
        assertEquals(80, RunPets.land(PlusRun.MINI_MAWANG));
        assertEquals(RunLevels.ILLYA, RunLevels.bossOf(18));
        assertEquals(RunLevels.TSUBAKIEH, RunLevels.bossOf(24));
        assertEquals(RunLevels.MAWANG, RunLevels.bossOf(80));
        assertTrue(RunPets.open(PlusRun.MINI_ILLYA, new Kept().with("run_stars_18", 1)));
        assertFalse("reaching the land is not beating her", RunPets.open(PlusRun.MINI_ILLYA, new Kept().with("run_open", 18)));
        assertTrue(RunPets.open(PlusRun.MINI_TSUBAKIEH, new Kept().with("run_stars_24", 3)));
        assertFalse(RunPets.open(PlusRun.MINI_TSUBAKIEH, new Kept().with("run_stars_18", 3)));
        assertTrue(RunPets.open(PlusRun.MINI_MAWANG, new Kept().with("run_stars_80", 1)));
        assertEquals(11, RunPets.open(new Kept().with("run_stars_18", 1)));
    }

    @Test public void miniLucyOpensWhenTheFlightIsWonEitherWay() {
        assertTrue("the land unmade", RunPets.open(PlusRun.MINI_LUCY, new Kept().with("run_ending", 1)));
        assertTrue("the true ending", RunPets.open(PlusRun.MINI_LUCY, new Kept().with("run_ending", 2)));
        assertFalse("the Mother Tongue risen alone is not yet", RunPets.open(PlusRun.MINI_LUCY, new Kept().with("run_ending", 4)));
        assertFalse(RunPets.open(PlusRun.MINI_LUCY, new Kept().with("run_stars_100", 0)));
        assertEquals(PlusRun.PETS, RunPets.open(new Kept().with("run_stars_18", 1).with("run_stars_24", 1).with("run_stars_80", 1).with("run_ending", 6)));
    }

    @Test public void onlyTheMawangFlies() {
        assertTrue(PlusRun.floats(PlusRun.MINI_MAWANG));
        assertFalse(PlusRun.floats(PlusRun.MINI_TSUBAKIEH));
        assertFalse(PlusRun.floats(PlusRun.MINI_ILLYA));
        assertFalse(PlusRun.floats(PlusRun.MINI_LUCY));
    }

    @Test public void theyAreNotFoundAsStraysNorInThePetShops() {
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int[] item : level.items) {
                if (item[0] >= PlusRun.PET && item[0] < PlusRun.PET + PlusRun.PETS) assertTrue("land " + number + ": a stray of kind " + (item[0] - PlusRun.PET), item[0] - PlusRun.PET < RunPets.FOUND);
            }
            assertTrue(RunLevels.petOf(number) < RunPets.FOUND);
        }
    }

    @Test public void eachHasItsPictureAndHerLineWhenOneAppears() {
        for (String name : new String[]{"tsubakieh", "illya", "mawang", "lucy"}) assertTrue(name, new File("src/main/res/drawable-nodpi/run_pet_" + name + ".webp").isFile());
        assertEquals(3, RunStory.NEW_PET.length);
        for (String[] line : RunStory.NEW_PET) {
            assertTrue(line[1], line[1].endsWith("Sloppy~♡"));
            assertTrue(line[1], line[1].contains("counter"));
            assertTrue(line[0], new File("src/main/res/raw/run_v_" + line[0] + ".ogg").isFile());
        }
    }
}
