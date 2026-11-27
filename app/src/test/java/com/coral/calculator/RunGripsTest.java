package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** What she holds is the weapon found itself: its own picture, laid along a grip measured in every frame. */
public class RunGripsTest {
    @Test public void theHeldWeaponIsTheEquippedItemsOwnDesignNotItsKinds() {
        // Every design of every kind (12 chapters, and the rose), and the Book of Addition: drawn as its own icon, which has an axis.
        Set<Integer> drawn = new HashSet<>();
        for (int base = 0; base < 12; base++) {
            int kind = RunArms.kindOf(RunLoot.code(RunLoot.WEAPON, 0, base, RunLoot.WHITE));
            Set<Integer> ofKind = new HashSet<>();
            for (int theme = 0; theme < 12; theme++) {
                int item = RunLoot.code(RunLoot.WEAPON, theme, base, RunLoot.BLUE);
                int icon = RunGrips.heldIcon(item, kind);
                assertEquals(RunLoot.iconOf(item), icon);
                assertNotNull(RunGrips.axis(icon));
                ofKind.add(icon);
            }
            int rose = RunLoot.code(RunLoot.WEAPON, 3, base, RunLoot.ROSE);
            ofKind.add(RunGrips.heldIcon(rose, kind));
            assertNotNull(RunGrips.axis(RunGrips.heldIcon(rose, kind)));
            // Thirteen designs of a kind, thirteen different pictures in her hands.
            assertEquals(13, ofKind.size());
            drawn.addAll(ofKind);
        }
        assertEquals(156, drawn.size());
        int addition = RunLoot.code(RunLoot.WEAPON, RunLoot.ADDITION, RunLoot.BOOK, RunLoot.WHITE);
        assertEquals(RunLoot.ADDITION_ICON, RunGrips.heldIcon(addition, RunArms.BOOK));
        assertNotNull(RunGrips.axis(RunLoot.ADDITION_ICON));
        // Nothing equipped, or something of another kind: her own sheet.
        assertEquals(-1, RunGrips.heldIcon(0, RunArms.SCYTHE));
        assertEquals(-1, RunGrips.heldIcon(RunLoot.code(RunLoot.WEAPON, 0, 2, RunLoot.BLUE), RunArms.SCYTHE));
    }

    @Test public void thePaperScytheIsItsOwnWeaponPlainAndWeak() {
        int paper = RunLoot.code(RunLoot.WEAPON, RunLoot.PAPER, RunLoot.SCYTHE, RunLoot.BLUE);
        assertEquals("Folded Page Scythe", RunLoot.name(paper));
        assertEquals(RunLoot.PAPER_ICONS, RunGrips.heldIcon(paper, RunArms.SCYTHE));
        assertNotNull(RunGrips.axis(RunLoot.PAPER_ICONS));
        int real = RunLoot.code(RunLoot.WEAPON, 0, RunLoot.SCYTHE, RunLoot.BLUE);
        assertTrue(RunArms.itemBlows(paper) < RunArms.itemBlows(real));
        assertTrue(RunArms.itemReach(paper) < RunArms.itemReach(real));
        // It drops only white or blue, only early.
        int found = 0;
        for (int roll = 0; roll < 20000; roll++) {
            for (int rarity = RunLoot.WHITE; rarity <= RunLoot.ROSE; rarity++) {
                int code = RunLoot.dropped(RunLoot.ORDINARY, roll / 7 % 6, rarity, roll % 5000, roll);
                if (!RunLoot.paper(code)) continue;
                found++;
                assertTrue(RunLoot.rarity(code) <= RunLoot.BLUE);
                assertTrue(roll / 7 % 6 <= 2);
            }
        }
        assertTrue(found > 0);
    }

    @Test public void aKatanaIsSheathedAtHerHipAndDrawnOutInHerHands() {
        int grip = RunGrips.of(RunArms.KATANA);
        int hip = 0, hands = 0;
        for (boolean one : RunGrips.HIP[grip]) if (one) hip++; else hands++;
        assertTrue(hip > 0 && hands > 0);
        Set<Integer> blades = new HashSet<>();
        for (int theme = 0; theme < 12; theme++) blades.add(RunGrips.bladeIcon(RunLoot.code(RunLoot.WEAPON, theme, 1, RunLoot.BLUE)));
        blades.add(RunGrips.bladeIcon(RunLoot.code(RunLoot.WEAPON, 2, 1, RunLoot.ROSE)));
        assertEquals(13, blades.size());
        for (int blade : blades) assertNotNull(RunGrips.axis(blade));
        // Every found kind has a grip now.
        for (int base = 0; base < 12; base++) assertTrue(RunGrips.of(RunArms.kindOf(RunLoot.code(RunLoot.WEAPON, 0, base, RunLoot.WHITE))) >= 0);
    }

    @Test public void everyGripHasALineInEveryFrame() {
        for (int grip = 0; grip < RunGrips.KINDS.length; grip++) {
            assertEquals(RunGrips.FRAMES[grip], RunGrips.LINES[grip].length);
            for (float[] line : RunGrips.LINES[grip]) {
                assertNotNull(RunGrips.NAMES[grip], line);
                float length = (float) Math.hypot(line[2] - line[0], line[3] - line[1]);
                assertTrue(RunGrips.NAMES[grip] + " " + length, length > 20);
                // Inside its cell, near enough.
                assertTrue(line[0] > -50 && line[0] < RunGrips.CELLS[grip][0] + 50 && line[1] > -50 && line[1] < RunGrips.CELLS[grip][1] + 50);
            }
        }
        assertTrue(RunGrips.of(RunArms.SCYTHE) >= 0);
    }
}
