package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** The homes: each kept by whoever lives there, with her inside, her cottage, and what is dear to her in it to be looked at. */
public class RunHomesTest {
    private static final float STEP = 1 / 60f;

    @Test public void everyHomeHasItsOwnerAndHerKeepsake() {
        Set<Integer> owners = new HashSet<>();
        int homes = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int[] room : level.rooms) {
                if (room[0] != RunLevels.HOUSE) continue;
                int owner = RunLevels.ownerOf(room);
                if (room[14] != RunLevels.HOME) {
                    assertEquals(-1, owner);
                    continue;
                }
                homes++;
                assertTrue(owner >= 0 && owner < RunLevels.OWNERS);
                owners.add(owner);
                // She is in, holding what she gives; and her keepsake stands inside, one of hers.
                int keepsakes = 0;
                boolean in = false;
                for (int[] item : level.items) {
                    if (item[1] < room[3] || item[1] > room[4]) continue;
                    if (item[0] == PlusRun.SPOT && item[4] >= PlusRun.KEEPSAKE && item[4] < PlusRun.KEEPSAKE + RunLevels.OWNERS) {
                        keepsakes++;
                        assertEquals(PlusRun.KEEPSAKE + owner, item[4]);
                    }
                    in |= item.length > 3 && item[3] == RunLevels.SOMI + owner;
                }
                assertEquals("land " + number, 1, keepsakes);
                assertTrue("land " + number, in);
            }
        }
        assertTrue(homes >= 20);
        assertEquals(RunLevels.OWNERS, owners.size());
    }

    @Test public void lookedAtTheFirstTimeAKeepsakeLeavesAChest() {
        int number = -1;
        int[] home = null;
        for (int land = 1; land <= RunLevels.TOTAL && home == null; land++) {
            for (int[] room : RunLevels.level(land).rooms) if (RunLevels.ownerOf(room) >= 0) { home = room; number = land; break; }
        }
        RunLevels.Level level = RunLevels.level(number);
        for (int[] room : level.rooms) if (RunLevels.ownerOf(room) >= 0) { home = room; break; }
        int[] spot = null;
        for (int[] item : level.items) if (item[0] == PlusRun.SPOT && item[4] >= PlusRun.KEEPSAKE && item[4] < PlusRun.KEEPSAKE + RunLevels.OWNERS && item[1] >= home[3] && item[1] <= home[4]) spot = item;
        PlusRun run = new PlusRun(level, new PlusRun.Perks());
        // In through its door, then up to the keepsake.
        run.place(home[1] + .5f - PlusRun.WIDE / 2, home[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        assertEquals(level.rooms.indexOf(home), run.room());
        run.place(spot[1] + .5f - PlusRun.WIDE / 2, spot[2] + 1 - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.spot() >= PlusRun.KEEPSAKE);
        int before = run.bonus();
        run.keepsakeChest(spot[1], spot[2], RunGame.KEEPSAKE_CHEST);
        boolean put = false;
        for (int[] item : run.items()) put |= item[0] == PlusRun.CHEST && item[1] == spot[1] && item[4] == RunGame.KEEPSAKE_CHEST;
        assertTrue(put);
        for (int step = 0; step < 30; step++) run.step(STEP, false, false, false);
        assertEquals(before + RunGame.KEEPSAKE_CHEST, run.bonus());
    }

    @Test public void theirPicturesAreInTheRunPackAndTheirWordsAreSpoken() {
        for (String owner : new String[]{"somi", "inju", "boksun", "ttosun", "galpi", "yeseo", "nabi", "moru"}) {
            assertTrue(owner, new java.io.File("../runart/src/main/assets/runart/home_" + owner + ".webp").isFile());
            assertTrue(owner, new java.io.File("../assets/plus-run/homes/home_" + owner + ".png").isFile());
        }
        assertTrue(new java.io.File("../runart/src/main/assets/runart/homes_out.webp").isFile());
        assertTrue(new java.io.File("../runart/src/main/assets/runart/home_keepsakes.webp").isFile());
        assertEquals(RunLevels.OWNERS, RunStory.KEEPSAKES.length);
        assertEquals(RunLevels.OWNERS, RunStory.KEEPSAKE_REPLIES.length);
        String letters = "SUBOPEAW";
        for (int owner = 0; owner < RunLevels.OWNERS; owner++) {
            String[] hers = RunStory.KEEPSAKES[owner], reply = RunStory.KEEPSAKE_REPLIES[owner];
            assertEquals("H", hers[2]);
            assertTrue(hers[1], hers[1].endsWith(" Sloppy~♡") && !hers[1].contains(", Sloppy"));
            assertEquals(String.valueOf(letters.charAt(owner)), reply[2]);
            assertFalse(reply[1], reply[1].contains("Sloppy"));
            assertEquals(RunStory.NAMES[RunStory.SPEAKERS.indexOf(reply[2])], RunStory.NAMES[11 + owner]);
            for (String[] line : new String[][]{hers, reply}) assertTrue(line[0], RunVoicesTest.recorded(line[0]));
        }
    }
}
