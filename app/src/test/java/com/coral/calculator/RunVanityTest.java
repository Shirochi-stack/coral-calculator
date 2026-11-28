package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Vanity: a cosmetic layer, three settings, outfits over every frame, and never a stat. */
public class RunVanityTest {
    private static final class Save implements RunVanity.Keep {
        final Map<String, Integer> all = new HashMap<>();
        @Override public int tally(String key) { return all.containsKey(key) ? all.get(key) : 0; }
        @Override public void keep(String key, int value) { all.put(key, value); }
    }

    private static RunLoot.Worn rose() { return new RunLoot.Worn(RunLoot.code(RunLoot.ARMOUR, 0, 29, RunLoot.ROSE), 0, 0); }

    @Test public void anOldSaveIsDefault() {
        Save save = new Save();
        assertEquals(RunVanity.DEFAULT, RunVanity.mode(save));
        assertEquals(-1, RunVanity.outfit(save));
        assertNull(RunVanity.styleFor(RunVanity.mode(save), RunVanity.outfit(save), rose()));
    }

    @Test public void theThreeSettings() {
        Save save = new Save();
        save.keep("run_bank", 5000);
        assertTrue(RunVanity.buy(save, 1));
        // Default: her own look, whatever she wears and has bought.
        RunVanity.setMode(save, RunVanity.DEFAULT);
        assertNull(RunVanity.styleFor(RunVanity.mode(save), RunVanity.outfit(save), rose()));
        // Show gear: the armour's cloth, not the outfit's.
        RunVanity.setMode(save, RunVanity.GEAR);
        RunVanity.Style gear = RunVanity.styleFor(RunVanity.mode(save), RunVanity.outfit(save), rose());
        assertNotNull(gear);
        // (The Rosebound Armour is drawn whole once its pictures are in; until then it is its colours with a moving light.)
        assertTrue(RunVanity.ROSE_READY ? "rose".equals(gear.art) : gear.shimmer);
        assertNotEquals(RunVanity.STYLES[1].key(), gear.key());
        // With nothing worn, gear shows nothing.
        assertNull(RunVanity.styleFor(RunVanity.GEAR, 1, RunLoot.NOTHING));
        // Vanity: the outfit, whatever she wears.
        RunVanity.setMode(save, RunVanity.VANITY);
        assertEquals(RunVanity.STYLES[1].key(), RunVanity.styleFor(RunVanity.mode(save), RunVanity.outfit(save), rose()).key());
        // Vanity with no outfit chosen is her own look.
        RunVanity.choose(save, -1);
        assertNull(RunVanity.styleFor(RunVanity.mode(save), RunVanity.outfit(save), rose()));
    }

    @Test public void buyingAndChoosing() {
        Save save = new Save();
        save.keep("run_bank", RunVanity.PRICES[0] - 1);
        assertFalse(RunVanity.buy(save, 0));
        assertEquals(RunVanity.PRICES[0] - 1, save.tally("run_bank"));
        save.keep("run_bank", RunVanity.PRICES[0] + 10);
        assertTrue(RunVanity.buy(save, 0));
        assertEquals(10, save.tally("run_bank"));
        assertFalse(RunVanity.buy(save, 0));
        assertEquals(0, RunVanity.outfit(save));
        // One she does not own cannot be chosen; a forged save of one is ignored.
        assertFalse(RunVanity.choose(save, 2));
        save.keep(RunVanity.OUTFIT_KEY, 3);
        assertEquals(-1, RunVanity.outfit(save));
        save.keep(RunVanity.MODE_KEY, 9);
        assertEquals(RunVanity.VANITY, RunVanity.mode(save));
    }

    @Test public void vanityNeverTouchesAStat() throws Exception {
        // Nothing in the game's rules reads RunVanity, and RunVanity reads nothing but its own keys and the bank it spends.
        for (String name : new String[]{"PlusRun", "RunLoot", "RunGear", "RunLevels", "RunArms"}) {
            File file = new File("src/main/java/com/coral/calculator/" + name + ".java");
            String source = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            assertFalse(name + " reads vanity", source.contains("RunVanity") || source.contains("VanityPaint"));
        }
        RunLoot.Worn worn = rose();
        int[] before = RunLoot.does(worn.armour);
        Save save = new Save();
        RunVanity.setMode(save, RunVanity.VANITY);
        assertArrayEqualsInts(before, RunLoot.does(worn.armour));
    }

    private static void assertArrayEqualsInts(int[] a, int[] b) {
        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) assertEquals(a[i], b[i]);
    }

    @Test public void everyOutfitHasEveryFrameOfEverySheet() throws Exception {
        String text = new String(Files.readAllBytes(new File("src/main/assets/vanity_anchors.txt").toPath()), StandardCharsets.UTF_8);
        VanityLayers.Anchors anchors = VanityLayers.parse(text);
        int total = 0, covered = 0;
        for (Object[] sheet : VanityLayers.SHEETS) {
            String name = (String) sheet[0];
            int frames = VanityLayers.frames(name);
            // Her climbing and her knuckles came after every outfit was drawn whole: they have no mask (no outfit is a dyed one now), only drawn frames.
            if (name.equals("climb") || name.equals("arm18") || name.equals("grip18") || name.startsWith("smash_") || name.startsWith("aerial_") || name.equals("gun_walk") || name.equals("great_charge")) continue;
            assertTrue(name, new File("src/main/res/drawable-nodpi/vanity_mask_" + name + ".webp").exists());
            total += frames;
            covered += anchors.count(name);
            // The pet atlas has empty cells; every other sheet is full of frames, and at most a frame or two where the cut found no skirt.
            // (Her moves, rolling and spinning, show her skirt in only a few frames: those are the ones that can be cut; the drawn outfits cover all eight.)
            if (name.equals("moves")) assertTrue(name, anchors.count(name) >= 3);
            else if (!name.equals("pet")) assertTrue(name + " has " + anchors.count(name) + " of " + frames, anchors.count(name) >= frames - 2);
        }
        assertEquals(62, anchors.count("pet"));
        assertTrue("covered " + covered + " of " + total, covered >= 215);
        // Every style paints every one of those frames: a cell goes through, whatever the cloth.
        for (RunVanity.Style style : RunVanity.STYLES) {
            if (style.art != null) continue;
            int w = 40, h = 50;
            int[] px = new int[w * h], mask = new int[w * h];
            for (int i = 0; i < px.length; i++) {
                px[i] = 0xFFEEEEEE;
                mask[i] = i % w > 10 && i % w < 30 && i / w > 20 ? 255 : 0;
            }
            int[] was = px.clone();
            VanityPaint.paint(px, mask, w, h, style, new float[]{20, 2, 20, 20, 20, 24, 20, 20, 20, 45, 30, 20}, 3);
            boolean changed = false;
            for (int i = 0; i < px.length; i++) {
                assertEquals("alpha is hers", was[i] >>> 24, px[i] >>> 24);
                if (mask[i] == 0 && !isAnchorSprite(i, w)) assertEquals("outside the cloth nothing moves", was[i], px[i]);
                changed |= was[i] != px[i];
            }
            assertTrue(changed);
        }
    }

    /** The pauldron and the stone are the only things drawn off the cloth: near the shoulder and chest anchors. */
    private static boolean isAnchorSprite(int i, int w) {
        int x = i % w, y = i / w;
        return Math.abs(x - 2) <= 6 && Math.abs(y - 20) <= 6 || Math.abs(x - 20) <= 6 && Math.abs(y - 20) <= 6 || Math.abs(x - 2) <= 6 && Math.abs(y - 2) <= 6;
    }

    @Test public void theGuildCounterHasAVanityTab() throws Exception {
        String source = new String(Files.readAllBytes(new File("src/main/java/com/coral/calculator/RunGame.java").toPath()), StandardCharsets.UTF_8);
        assertTrue(source.contains("\"Arms\", \"Vanity\"}"));
    }
}
