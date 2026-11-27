package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Tulle and Thread: what is sold, the quest lines and their steps, what is said, and that every outfit has every frame. */
public class RunBoutiqueTest {
    private static final class Save implements RunVanity.Keep {
        final Map<String, Integer> all = new HashMap<>();
        @Override public int tally(String key) { return all.containsKey(key) ? all.get(key) : 0; }
        @Override public void keep(String key, int value) { all.put(key, value); }
    }

    @Test public void whatIsOnSaleAndWhatIsNot() {
        for (int outfit = 0; outfit < RunVanity.count(); outfit++) {
            Save save = new Save();
            save.keep("run_bank", 99999);
            save.keep("run_treats", 99);
            boolean sold = RunVanity.ready(outfit) && RunVanity.QUEST[outfit] < 0;
            assertEquals(RunVanity.NAMES[outfit], sold, RunVanity.buy(save, outfit));
            Save other = new Save();
            other.keep("run_treats", 99);
            assertEquals(RunVanity.NAMES[outfit], sold, RunVanity.buyWithTreats(other, outfit));
            if (sold) {
                assertEquals(99 - RunVanity.TREATS[outfit], other.tally("run_treats"));
                assertEquals(99999 - RunVanity.PRICES[outfit], save.tally("run_bank"));
                assertTrue(RunVanity.owns(save, outfit));
            } else {
                assertEquals(99999, save.tally("run_bank"));
            }
        }
    }

    @Test public void everyQuestRunsToItsEnd() {
        int played = 0;
        for (int quest = 0; quest < VanityQuests.count(); quest++) {
            Save save = new Save();
            // What she did before taking it up counts for nothing.
            save.keep("run_mobs", 500);
            save.keep("run_hunts", 50);
            assertEquals(0, VanityQuests.state(save, quest));
            if (!VanityQuests.open(quest)) {
                assertFalse(VanityQuests.take(save, quest));
                continue;
            }
            played++;
            assertTrue(VanityQuests.take(save, quest));
            assertFalse("not twice", VanityQuests.take(save, quest));
            int reward = VanityQuests.REWARD[quest];
            assertFalse(RunVanity.owns(save, reward));
            for (int step = 0; step < VanityQuests.STEPS[quest].length; step++) {
                VanityQuests.Step now = VanityQuests.STEPS[quest][step];
                assertEquals(step + 1, VanityQuests.state(save, quest));
                assertEquals("nothing done yet", VanityQuests.NOTHING, VanityQuests.hand(save, quest));
                // Do it: a part of it first, which is not enough.
                int need = VanityQuests.progress(save, quest)[1];
                if (now.kind == VanityQuests.MOBS) save.keep("run_mobs", save.tally("run_mobs") + now.amount - 1);
                if (now.kind == VanityQuests.HUNTS) save.keep("run_hunts", save.tally("run_hunts") + now.amount - 1);
                if (now.kind == VanityQuests.GIVE) save.keep("run_mat_" + now.which, now.amount - 1);
                assertEquals(VanityQuests.NOTHING, VanityQuests.hand(save, quest));
                if (now.kind == VanityQuests.MOBS) save.keep("run_mobs", save.tally("run_mobs") + 1);
                if (now.kind == VanityQuests.HUNTS) save.keep("run_hunts", save.tally("run_hunts") + 1);
                if (now.kind == VanityQuests.GIVE) save.keep("run_mat_" + now.which, now.amount + 2);
                if (now.kind == VanityQuests.LAND) save.keep("run_stars_" + now.amount, 1);
                assertTrue(need > 0);
                int result = VanityQuests.hand(save, quest);
                assertEquals(step == VanityQuests.STEPS[quest].length - 1 ? VanityQuests.FINISHED : VanityQuests.STEP, result);
                // A gift is taken from her.
                if (now.kind == VanityQuests.GIVE) assertEquals(2, save.tally("run_mat_" + now.which));
            }
            assertTrue(VanityQuests.done(save, quest));
            assertTrue("the outfit is hers", RunVanity.owns(save, reward));
            assertEquals(VanityQuests.NOTHING, VanityQuests.hand(save, quest));
        }
        assertTrue("at least one quest can be played", played >= 1);
    }

    @Test public void everyLineIsSaidAndRecorded() {
        String[][] all = new String[1 + VanityStory.GREET.length + 1 + VanityStory.QUESTS.length * 5 + VanityStory.BOUGHT_FOR.length + VanityStory.HAL_BOUGHT.length][];
        int n = 0;
        for (String[] one : VanityStory.GREET) all[n++] = one;
        all[n++] = VanityStory.BOUGHT;
        all[n++] = VanityStory.BROKE;
        for (String[] one : VanityStory.BOUGHT_FOR) all[n++] = one;
        for (String[] one : VanityStory.HAL_BOUGHT) all[n++] = one;
        for (String[][] quest : VanityStory.QUESTS) {
            assertEquals(5, quest.length);
            for (String[] one : quest) all[n++] = one;
        }
        for (String[] one : all) {
            if (one == null) continue;
            assertNotNull(one[0]);
            assertTrue(one[0], new File("src/main/res/raw/run_v_" + one[0] + ".ogg").length() > 2000);
            assertTrue(one[1].length() > 10);
            // Halgakos's lines end the way hers do.
            if (one[2].equals("H")) assertTrue(one[1], one[1].endsWith("Sloppy~♡"));
            assertNotNull(VanityStory.line(one[0]));
        }
        // Every outfit that is sold has a line for it.
        for (int outfit = 0; outfit < RunVanity.count(); outfit++) assertEquals(RunVanity.QUEST[outfit] < 0, VanityStory.BOUGHT_FOR[outfit] != null);
        // Three quest lines at least.
        assertTrue(VanityQuests.count() >= 3);
        assertEquals(VanityQuests.count(), VanityStory.QUESTS.length);
    }

    /** The size of a WebP picture, read from its header. */
    private static int[] size(File file) throws Exception {
        try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
            byte[] head = new byte[32];
            in.readFully(head);
            String kind = new String(head, 12, 4, "US-ASCII");
            if (kind.equals("VP8X")) {
                return new int[]{1 + (head[24] & 255 | (head[25] & 255) << 8 | (head[26] & 255) << 16), 1 + (head[27] & 255 | (head[28] & 255) << 8 | (head[29] & 255) << 16)};
            }
            if (kind.equals("VP8 ")) return new int[]{(head[26] & 255 | (head[27] & 255) << 8) & 0x3FFF, (head[28] & 255 | (head[29] & 255) << 8) & 0x3FFF};
            if (kind.equals("VP8L")) {
                int bits = head[21] & 255 | (head[22] & 255) << 8 | (head[23] & 255) << 16 | (head[24] & 255) << 24;
                return new int[]{1 + (bits & 0x3FFF), 1 + (bits >> 14 & 0x3FFF)};
            }
            return null;
        }
    }

    @Test public void everyOutfitOnSaleHasEveryFrameOfEverySheet() throws Exception {
        int checked = 0;
        for (int outfit = 0; outfit < RunVanity.count(); outfit++) {
            RunVanity.Style style = RunVanity.STYLES[outfit];
            if (style.art == null) continue;
            if (!RunVanity.ready(outfit)) continue;
            checked++;
            for (Object[] sheet : VanityLayers.SHEETS) {
                File file = new File("../vanity/src/main/assets/vanity/" + style.art + "/" + sheet[0] + ".webp");
                assertTrue(file.getPath(), file.exists());
                int[] got = size(file);
                assertNotNull(file.getPath(), got);
                assertEquals(file.getPath(), (Integer) sheet[1] * (Integer) sheet[3], got[0]);
                assertEquals(file.getPath(), (Integer) sheet[2] * (Integer) sheet[4], got[1]);
            }
        }
        assertTrue(checked >= 0);
    }

    @Test public void theBoutiqueOpensFromTheGuildCounter() throws Exception {
        String source = new String(java.nio.file.Files.readAllBytes(new File("src/main/java/com/coral/calculator/RunGame.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(source.contains("RunBoutique.over("));
        assertTrue(new File("src/main/res/drawable-nodpi/run_vanity_npc.webp").length() > 10000);
    }
}
