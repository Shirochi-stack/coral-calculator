package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/** Knuckles, the thirteenth class: its rules, its loot and class gear, its Rose and the Quill, its two skill paths, and its hunts. */
public class RunKnucklesTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    @Test public void aThirteenthClassWithEverythingItNeeds() {
        assertEquals(19, RunArms.COUNT);
        assertEquals(RunArms.COUNT, RunArms.NAMES.length);
        assertEquals("Knuckles", RunArms.NAMES[RunArms.KNUCKLES]);
        assertEquals(13, RunArms.OF_KIND.length);
        assertEquals(RunArms.KNUCKLES, RunArms.OF_KIND[RunLoot.KNUCKLES]);
        assertEquals(RunLoot.WEAPONS.length, RunArms.OF_KIND.length);
        assertEquals(RunLoot.BRAWLERS, RunLoot.classOf(RunArms.KNUCKLES));
        assertTrue(RunArms.heavy(RunArms.KNUCKLES));
        for (int level = 1; level <= RunArms.LEVELS; level++) assertFalse(RunArms.step(RunArms.KNUCKLES, level).isEmpty());
        assertTrue(RunGrips.of(RunArms.KNUCKLES) >= 0);
        assertTrue(new java.io.File("src/main/res/drawable-nodpi/halgakos_arm18.webp").isFile());
        assertTrue(new java.io.File("src/main/res/drawable-nodpi/run_grip_knuckles.webp").isFile());
    }

    /** A flat land with minus signs in a row ahead of her, and her with knuckles on. */
    private static PlusRun withKnuckles(int foes) {
        RunLevels.Level level = new RunLevels.Level(10, 60);
        for (int c = 0; c < 60; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 57;
        for (int one = 0; one < foes; one++) level.foes.add(new int[]{RunLevels.MINUS, 12 + one, FLOOR - 1, 6});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 20;
        PlusRun run = new PlusRun(level, perks);
        run.arm(RunArms.KNUCKLES);
        run.place(10.6f, FLOOR - PlusRun.TALL);
        return run;
    }

    private static void tap(PlusRun run) {
        run.step(STEP, false, false, false, true);
        for (int step = 0; step < 16; step++) run.step(STEP, false, false, false, false);
    }

    @Test public void jabJabUppercutAndTheUppercutLeavesItReeling() {
        PlusRun run = withKnuckles(1);
        tap(run);
        tap(run);
        assertEquals(2, run.punchChain());
        assertEquals(0, run.uppercuts());
        tap(run);
        assertEquals(1, run.uppercuts());
        assertEquals(3, run.punches());
        // (Let go too long, the combo begins again.)
        for (int step = 0; step < 60; step++) run.step(STEP, false, false, false, false);
        tap(run);
        assertEquals(1, run.punchChain());
    }

    @Test public void atARunADashPunch() {
        PlusRun run = withKnuckles(0);
        run.place(4, FLOOR - PlusRun.TALL);
        for (int step = 0; step < 40; step++) run.step(STEP, false, true, false, false);
        float from = run.x();
        run.step(STEP, false, true, false, true);
        for (int step = 0; step < 6; step++) run.step(STEP, false, true, false, false);
        assertEquals(1, run.dashPunches());
        assertTrue(run.x() > from + .5f);
    }

    @Test public void theHaymakerBreaksAShieldOutright() {
        PlusRun run = withKnuckles(1);
        PlusRun.Foe foe = run.foes().get(0);
        foe.guard = foe.guardMost = 9;
        foe.guardNeeds = 10000;
        foe.leftward = true;
        for (int step = 0; step < 45; step++) run.step(STEP, false, false, false, true);
        run.step(STEP, false, false, false, false);
        assertEquals(1, run.haymakers());
        assertEquals(0, foe.guard);
        assertEquals(1, run.shieldsBroken());
    }

    @Test public void itsLootHasPicturesOfItsOwnAndItsClassGearIsForItAlone() {
        Set<Integer> icons = new HashSet<>();
        for (int theme = 0; theme < 12; theme++) assertTrue(icons.add(RunLoot.iconOf(RunLoot.code(RunLoot.WEAPON, theme, RunLoot.KNUCKLES, RunLoot.GOLD))));
        assertTrue(icons.add(RunLoot.iconOf(RunLoot.code(RunLoot.WEAPON, 11, RunLoot.KNUCKLES, RunLoot.ROSE))));
        assertTrue(icons.add(RunLoot.iconOf(RunCraft.folded(RunLoot.KNUCKLES, true))));
        int gi = RunLoot.code(RunLoot.ARMOUR, 5, RunLoot.BRAWLER_GI, RunLoot.RED), wraps = RunLoot.code(RunLoot.ACCESSORY, 5, RunLoot.CHAMPION_WRAPS, RunLoot.RED);
        assertTrue(icons.add(RunLoot.iconOf(gi)));
        assertTrue(icons.add(RunLoot.iconOf(wraps)));
        for (int icon : icons) assertTrue(icon >= 256 && icon < 272);
        // (No other thing's picture moved.)
        assertEquals(0, RunLoot.iconOf(RunLoot.code(RunLoot.WEAPON, 0, 0, RunLoot.WHITE)));
        assertEquals(RunLoot.ARMOUR_ICONS + 29, RunLoot.iconOf(RunLoot.code(RunLoot.ARMOUR, 3, RunLoot.ROSEBOUND, RunLoot.ROSE)));
        assertEquals("Folded Page Knuckles", RunLoot.name(RunCraft.folded(RunLoot.KNUCKLES, false)));
        assertEquals("Halgakos Rose Knuckles", RunLoot.name(RunCraft.rose(RunLoot.KNUCKLES)));
        // The class gear: for knuckles, and much of it.
        RunLoot.Worn worn = new RunLoot.Worn(gi, wraps, 0);
        assertTrue(worn.of(RunLoot.HASTE, RunLoot.BRAWLERS) > worn.of(RunLoot.HASTE, RunLoot.BLADES));
        assertTrue(worn.of(RunLoot.POWER, RunLoot.BRAWLERS) >= 30);
        assertEquals(RunLoot.BRAWLERS, RunLoot.leansTo(RunLoot.BRAWLER_GI));
        // (The armours that were lean as they did.)
        assertEquals(RunLoot.ANY, RunLoot.leansTo(RunLoot.ROSEBOUND));
        for (int base = 0; base < 29; base++) assertEquals(base % 7, RunLoot.leansTo(base));
        // Rare: only from those who wait, gold or better.
        int found = 0;
        for (int roll = 0; roll < 10000; roll += 7) {
            int code = RunLoot.dropped(RunLoot.ORDINARY, 4, RunLoot.GOLD, 9000, roll);
            assertNotEquals(RunLoot.BRAWLER_GI, RunLoot.type(code) == RunLoot.ARMOUR ? RunLoot.base(code) : -1);
            int boss = RunLoot.dropped(RunLoot.WAITS, 4, RunLoot.GOLD, 9000, roll);
            if (RunLoot.base(boss) == RunLoot.CHAMPION_WRAPS && RunLoot.type(boss) == RunLoot.ACCESSORY || RunLoot.base(boss) == RunLoot.BRAWLER_GI && RunLoot.type(boss) == RunLoot.ARMOUR) found++;
        }
        assertTrue(found > 0 && found < 200);
    }

    @Test public void theQuillAsksForThirteenRosesAndASaveWithTheFirstTwelveIsGivenTheThirteenth() {
        int twelve = (1 << 12) - 1;
        assertFalse(RunCraft.quillOpen(twelve, 100));
        assertEquals(RunCraft.ALL_ROSES, RunCraft.roses(twelve));
        assertTrue(RunCraft.quillOpen(RunCraft.roses(twelve), 100));
        // (Fewer than twelve: nothing given.)
        assertEquals(twelve - 1, RunCraft.roses(twelve - 1));
    }

    @Test public void theKnucklesHaveARegionOfTheirOwnInTheTree() {
        assertEquals(13, RunSkills.CLASS_COUNT);
        assertEquals("Knuckles", RunSkills.CLASSES[12]);
        assertEquals(RunArms.KNUCKLES, RunSkills.weaponOf(12));
        assertEquals(12, RunSkills.classOfWeapon(RunArms.KNUCKLES));
        // Its start, and its seven notables: finishers and launchers, two actives, a deep one and a keystone with a cost.
        int start = -1, notables = 0;
        for (RunSkills.Node node : RunSkills.tree()) {
            if (node.type == RunSkills.START && node.path == 12) start = node.id;
            // (And Meteor Fist, of the moves of the air, added after.)
            if (node.type == RunSkills.NOTABLE && node.path == 12 && node.notable < RunSkills.HYBRIDS) notables++;
        }
        assertTrue(start > 0);
        assertEquals(RunSkills.PER_CLASS, notables);
        RunSkills.Notable keystone = RunSkills.NOTABLES.get(12 * RunSkills.PER_CLASS + RunSkills.CLASS_KEYSTONE);
        assertTrue(keystone.keystone);
        assertTrue(keystone.costly());
        assertTrue(keystone.of(RunSkills.E_EXECUTE) > 0);
        assertTrue(RunSkills.NOTABLES.get(12 * RunSkills.PER_CLASS + RunSkills.OUTER_LEFT).of(RunSkills.E_STUN) > 0
                || RunSkills.NOTABLES.get(12 * RunSkills.PER_CLASS + RunSkills.OUTER_RIGHT).of(RunSkills.E_STUN) > 0);
    }
    @Test public void huntsKnowItAndTheTreesKeepTheirNumber() {
        assertEquals(19, RunHunts.TREES);
        assertEquals(RunHunts.KNUCKLE_WAY, RunHunts.wayOf(RunArms.KNUCKLES));
        assertEquals(RunArms.SCYTHE + 1, RunHunts.wayOf(RunArms.SCYTHE));
        int hunt = RunHunts.of(RunHunts.BY_WAY, RunHunts.KNUCKLE_WAY, 10, 0, false);
        assertTrue(RunHunts.counts(hunt, RunLevels.MINUS, RunHunts.KNUCKLE_WAY));
        assertFalse(RunHunts.counts(hunt, RunLevels.MINUS, RunHunts.TREES));
    }
}
