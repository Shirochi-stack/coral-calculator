package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** D6: paper trees cut for paper, paper weapons folded, weapons honed, a Halgakos Rose of every class, and the Mother Tongue's Quill. */
public class RunCraftTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    @Test public void treesGrowFinerFurtherIn() {
        assertEquals(0, RunCraft.fineOdds(2));
        assertEquals(0, RunCraft.goldenOdds(6));
        assertTrue(RunCraft.fineOdds(5) > 0 && RunCraft.goldenOdds(9) > 0);
        for (int roll = 0; roll < 100; roll++) assertEquals(RunCraft.PLAIN, RunCraft.grade(0, roll));
        int trees = 0, golden = 0, bare = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            if (number < RunLevels.TREES_FROM) assertTrue(level.trees.isEmpty());
            else if (level.trees.isEmpty()) bare++;
            assertTrue(level.trees.size() <= RunLevels.treesIn(number));
            int chapter = Math.min(11, RunLevels.chapterOf(number));
            for (int[] tree : level.trees) {
                trees++;
                assertEquals("land " + number, RunLevels.top(level, tree[0]) - 1, tree[1]);
                for (int row = tree[1] - 3; row <= tree[1]; row++) assertEquals(RunLevels.AIR, level.land[row][tree[0]]);
                for (int flag : level.flags) assertTrue(Math.abs(flag - tree[0]) > 4);
                if (tree[2] == RunCraft.GOLDEN) {
                    golden++;
                    assertTrue(chapter >= 7);
                }
                if (tree[2] == RunCraft.FINE) assertTrue(chapter >= 3);
            }
            // (A layered land keeps its trees.)
            if (number < RunLevels.OPEN_TO) assertEquals(level.trees.size(), RunLevels.layered(level, 1).trees.size());
        }
        assertTrue("trees " + trees, trees > 250);
        assertTrue("lands without " + bare, bare <= 5);
        assertTrue("golden " + golden, golden > 3);
    }

    @Test public void aTreeIsChoppedDownForItsPaper() {
        RunLevels.Level flat = new RunLevels.Level(10, 40);
        for (int column = 0; column < 40; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) flat.land[row][column] = RunLevels.BLOCK;
        flat.gate = 37;
        flat.trees.add(new int[]{3, FLOOR - 1, RunCraft.FINE});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        PlusRun run = new PlusRun(flat, perks);
        for (int step = 0; step < 40 * 8 && run.treesCut() == 0; step++) run.step(STEP, false, false, false, step % 40 == 0);
        assertEquals(1, run.treesCut());
        assertEquals(RunCraft.treeGives(RunCraft.FINE), run.paperCut(RunCraft.FINE));
        assertEquals(0, run.trees().get(0)[3]);
    }

    @Test public void paperWeaponsOfEveryKindHaveTheirOwnPictures() {
        Set<Integer> icons = new HashSet<>();
        for (int base = 0; base < 12; base++) {
            int code = RunCraft.folded(base, false);
            assertTrue(RunLoot.paper(code));
            assertEquals("Folded Page " + RunLoot.WEAPONS[base], RunLoot.name(code));
            icons.add(RunLoot.iconOf(code));
            assertEquals(RunLoot.BLUE, RunLoot.rarity(RunCraft.folded(base, true)));
            assertEquals(-1, RunArms.itemBlows(code));
            // Held, it is drawn as its own picture.
            assertTrue(RunGrips.axis(RunLoot.iconOf(code)) != null);
        }
        assertEquals(12, icons.size());
        assertTrue(icons.contains(RunLoot.PAPER_ICONS));
        // Only the scythe drops as paper; the rest are folded.
        assertEquals(1, RunLoot.PAPER_DROPS.length);
    }

    @Test public void honingRosesAndTheQuill() {
        int found = RunLoot.code(RunLoot.WEAPON, 3, 0, RunLoot.PURPLE);
        assertTrue(RunCraft.honeable(found));
        assertEquals(RunLoot.GOLD, RunLoot.rarity(RunCraft.honed(found)));
        assertFalse(RunCraft.honeable(RunLoot.code(RunLoot.WEAPON, 3, 0, RunLoot.RED)));
        assertFalse(RunCraft.honeable(RunCraft.folded(0, true)));
        assertFalse(RunCraft.honeable(RunLoot.QUILL));
        assertTrue(RunCraft.honeFine(3) > RunCraft.honeFine(0));
        // A Rose of every class.
        Set<Integer> kinds = new HashSet<>();
        for (int base = 0; base < 12; base++) {
            int rose = RunCraft.rose(base);
            assertEquals(RunLoot.ROSE, RunLoot.rarity(rose));
            assertTrue(RunLoot.name(rose).startsWith("Halgakos Rose"));
            kinds.add(RunArms.kindOf(rose));
        }
        assertEquals(12, kinds.size());
        // The Quill: only with every Rose made and at its level; above everything.
        assertFalse(RunCraft.quillOpen(RunCraft.ALL_ROSES >> 1, 120));
        assertFalse(RunCraft.quillOpen(RunCraft.ALL_ROSES, 80));
        assertTrue(RunCraft.quillOpen(RunCraft.ALL_ROSES, 90));
        assertEquals("The Mother Tongue's Quill", RunLoot.name(RunLoot.QUILL));
        assertEquals(RunLoot.QUILL_ICON, RunLoot.iconOf(RunLoot.QUILL));
        assertTrue(RunGrips.axis(RunLoot.QUILL_ICON) != null);
        for (int base = 0; base < 12; base++) assertTrue(RunArms.itemBlows(RunLoot.QUILL) > RunArms.itemBlows(RunCraft.rose(base)));
        assertTrue(RunArms.itemReach(RunLoot.QUILL) > RunArms.itemReach(RunCraft.rose(0)));
    }

    @Test public void heldItsWaveBeatsEverythingNear() {
        RunLevels.Level flat = new RunLevels.Level(40, 40);
        for (int column = 0; column < 40; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) flat.land[row][column] = RunLevels.BLOCK;
        flat.gate = 37;
        flat.foes.add(new int[]{RunLevels.MINUS, 9, FLOOR - 1});
        flat.foes.add(new int[]{RunLevels.MINUS, 1, FLOOR - 1});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.item = RunLoot.QUILL;
        perks.main = RunArms.kindOf(RunLoot.QUILL);
        perks.pack |= 1 << perks.main;
        PlusRun run = new PlusRun(flat, perks);
        run.place(4, FLOOR - PlusRun.TALL);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        for (int step = 0; step < 90 && run.quillWaves() == 0; step++) run.step(STEP, false, false, false, true);
        assertEquals(1, run.quillWaves());
        for (PlusRun.Foe foe : run.foes()) assertTrue(foe.flat || foe.left < foe.most);
    }

    @Test public void huntsAskForTreesCutDown() {
        int hunt = RunHunts.of(RunHunts.BY_WAY, RunHunts.TREES, 6, 0, false);
        assertTrue(RunHunts.countsTree(hunt));
        assertFalse(RunHunts.counts(hunt, RunLevels.MINUS, RunHunts.TREES));
        assertEquals("Cut down 6 paper trees", RunHunts.words(hunt));
        assertTrue(RunHunts.here(hunt, RunLevels.level(10)));
        // Offered, sooner or later.
        boolean offered = false;
        for (int serial = 0; serial < 60 && !offered; serial++) offered = RunHunts.countsTree(RunHunts.offer(serial, 20, 1, 0, new int[3]));
        assertTrue(offered);
    }
}
