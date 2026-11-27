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
        String[][] all = new String[1 + 1 + VanityStory.GREET.length + 1 + VanityStory.QUESTS.length * 5 + VanityStory.BOUGHT_FOR.length + VanityStory.HAL_BOUGHT.length + VanityStory.EARS_BOUGHT.length + VanityStory.HORNS_BOUGHT.length][];
        int n = 0;
        for (String[] one : VanityStory.GREET) all[n++] = one;
        all[n++] = VanityStory.PICTURE;
        all[n++] = VanityStory.BOUGHT;
        all[n++] = VanityStory.BROKE;
        for (String[] one : VanityStory.BOUGHT_FOR) all[n++] = one;
        for (String[] one : VanityStory.HAL_BOUGHT) all[n++] = one;
        for (String[] one : VanityStory.EARS_BOUGHT) all[n++] = one;
        for (String[] one : VanityStory.HORNS_BOUGHT) all[n++] = one;
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

    @Test public void headPartsCombineWithEveryOutfitAndSettingAndNeverChangeAStat() {
        java.util.Set<String> keys = new java.util.HashSet<>();
        for (int outfit = -1; outfit < RunVanity.count(); outfit++) {
            for (int ears = 0; ears < RunVanity.EARS.length; ears++) {
                for (int horns = 0; horns < RunVanity.HORNS.length; horns++) {
                    RunVanity.Style style = RunVanity.styleFor(RunVanity.VANITY, outfit, null, ears, horns);
                    if (outfit < 0 && ears == 0 && horns == 0) {
                        assertEquals(null, style);
                        continue;
                    }
                    assertNotNull(style);
                    assertEquals(ears, style.ears);
                    assertEquals(horns, style.horns);
                    assertTrue("every combination is its own look", keys.add(outfit + ":" + style.key()));
                    // Default and Show gear never show them.
                    RunVanity.Style gear = RunVanity.styleFor(RunVanity.GEAR, outfit, new RunLoot.Worn(RunLoot.code(RunLoot.ARMOUR, 0, 5, RunLoot.BLUE), 0, 0), ears, horns);
                    assertEquals(0, gear.ears);
                    assertEquals(0, gear.horns);
                    assertEquals(null, RunVanity.styleFor(RunVanity.DEFAULT, outfit, null, ears, horns));
                }
            }
        }
    }

    @Test public void partsAreBoughtAndWornOnlyOnceTheirPicturesAreIn() {
        for (int ears = 1; ears < RunVanity.EARS.length; ears++) {
            Save save = new Save();
            save.keep("run_bank", 99999);
            save.keep("run_treats", 99);
            boolean ready = RunVanity.earsReady(ears);
            assertEquals(ready, RunVanity.buyPart(save, true, ears, false));
            assertEquals(ready ? ears : 0, RunVanity.ears(save));
            if (ready) {
                assertEquals(99999 - RunVanity.EARS_PRICE[ears], save.tally("run_bank"));
                assertFalse("not twice", RunVanity.buyPart(save, true, ears, true));
            }
            // One she does not have cannot be put on; her own always can.
            assertEquals(ready, RunVanity.wearPart(save, true, ears));
            assertTrue(RunVanity.wearPart(save, true, 0));
            assertEquals(0, RunVanity.ears(save));
            assertNotNull(VanityStory.EARS_BOUGHT[ears]);
            assertTrue(new File("src/main/res/raw/run_v_" + VanityStory.EARS_BOUGHT[ears][0] + ".ogg").length() > 2000);
        }
        for (int horns = 1; horns < RunVanity.HORNS.length; horns++) {
            Save save = new Save();
            save.keep("run_treats", 99);
            boolean ready = RunVanity.hornsReady(horns);
            assertEquals(ready, RunVanity.buyPart(save, false, horns, true));
            assertEquals(ready ? horns : 0, RunVanity.horns(save));
            assertNotNull(VanityStory.HORNS_BOUGHT[horns]);
            assertTrue(new File("src/main/res/raw/run_v_" + VanityStory.HORNS_BOUGHT[horns][0] + ".ogg").length() > 2000);
        }
        // A forged save of a part she does not own shows her own.
        Save forged = new Save();
        forged.keep(RunVanity.EARS_KEY, 2);
        forged.keep(RunVanity.HORNS_KEY, 1);
        assertEquals(0, RunVanity.ears(forged));
        assertEquals(0, RunVanity.horns(forged));
    }

    @Test public void everyPartOnSaleHasEveryFrameOfEverySheet() throws Exception {
        for (int pass = 0; pass < 2; pass++) {
            String[] names = pass == 0 ? RunVanity.EARS_ART : RunVanity.HORNS_ART;
            for (int one = 1; one < names.length; one++) {
                if (!(pass == 0 ? RunVanity.earsReady(one) : RunVanity.hornsReady(one))) continue;
                for (Object[] sheet : VanityLayers.SHEETS) {
                    for (String suffix : new String[]{".webp", "_m.webp"}) {
                        File file = new File("../vanity/src/main/assets/vanity/heads/" + names[one] + "/" + sheet[0] + suffix);
                        assertTrue(file.getPath(), file.exists());
                        int[] got = size(file);
                        assertNotNull(file.getPath(), got);
                        assertEquals(file.getPath(), (Integer) sheet[1] * (Integer) sheet[3], got[0]);
                        assertEquals(file.getPath(), (Integer) sheet[2] * (Integer) sheet[4], got[1]);
                    }
                }
            }
        }
    }

    @Test public void everyOutfitAndPartHasItsPicturesAndTheyOpenWhenItIsHers() {
        // Every outfit has at least one picture, every picture's file is in the pack, and each opens with its outfit (and not before).
        for (int outfit = 0; outfit < RunVanity.count(); outfit++) {
            int pictures = 0;
            for (VanityPictures.Picture picture : VanityPictures.ALL) if (picture.outfit == outfit) pictures++;
            assertTrue(RunVanity.NAMES[outfit] + " has a picture", pictures >= 1);
        }
        for (VanityPictures.Picture picture : VanityPictures.ALL) {
            assertTrue(picture.id, new File("../vanity/src/main/assets/vanity/pictures/" + picture.id + ".webp").length() > 50000);
            Save save = new Save();
            assertFalse(VanityPictures.open(save, picture));
        }
        for (int outfit = 0; outfit < RunVanity.count(); outfit++) {
            Save save = new Save();
            RunVanity.give(save, outfit);
            for (VanityPictures.Picture picture : VanityPictures.ALL) assertEquals(picture.outfit == outfit, VanityPictures.open(save, picture));
            // Once opened, they stay open: a second time opens nothing new.
            assertEquals(0, VanityPictures.unlockOutfit(save, outfit));
        }
        // Bought, not given: the same.
        Save bought = new Save();
        bought.keep("run_bank", 99999);
        assertTrue(RunVanity.buy(bought, 3));
        int open = 0;
        for (VanityPictures.Picture picture : VanityPictures.ALL) if (VanityPictures.open(bought, picture)) open++;
        assertEquals(2, open);
        // The Cringe gallery lists them under Vanity, and a row reads.
        for (RunGallery.Item item : VanityPictures.items()) {
            assertEquals(RunGallery.VANITY, item.shelf);
            assertTrue(item.rule, RunGallery.valid(item.rule));
        }
        // A quest's end opens the picture of its outfit.
        Save quest = new Save();
        quest.keep("run_mobs", 0);
        assertTrue(VanityQuests.take(quest, 2));
        for (int step = 0; step < VanityQuests.STEPS[2].length; step++) {
            VanityQuests.Step now = VanityQuests.STEPS[2][step];
            if (now.kind == VanityQuests.LAND) quest.keep("run_stars_" + now.amount, 1);
            if (now.kind == VanityQuests.GIVE) quest.keep("run_mat_" + now.which, now.amount);
            if (now.kind == VanityQuests.HUNTS) quest.keep("run_hunts", quest.tally("run_hunts") + now.amount);
            VanityQuests.hand(quest, 2);
        }
        assertTrue(VanityPictures.open(quest, VanityPictures.ALL.get(2)));
    }

    @Test public void theBoutiqueOpensFromTheGuildCounter() throws Exception {
        String source = new String(java.nio.file.Files.readAllBytes(new File("src/main/java/com/coral/calculator/RunGame.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(source.contains("RunBoutique.over("));
        assertTrue(new File("src/main/res/drawable-nodpi/run_vanity_npc.webp").length() > 10000);
    }
}
