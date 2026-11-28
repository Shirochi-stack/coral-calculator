package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/** Her skill tree: over a thousand nodes, twenty-four paths, a dozen notables each, every node reachable; and hearts in layers of colour. */
public class RunSkillsTest {
    @Test public void overAThousandNodesInTwentyFourPathsEachWithTenNotablesOrMore() {
        List<RunSkills.Node> tree = RunSkills.tree();
        assertTrue("nodes " + tree.size(), tree.size() >= 1000);
        int[] notables = new int[RunSkills.PATHS], roots = new int[RunSkills.PATHS];
        for (RunSkills.Node node : tree) {
            if (node.type == RunSkills.NOTABLE) notables[node.path]++;
            if (node.type == RunSkills.ROOT) roots[node.path]++;
        }
        for (int path = 0; path < RunSkills.PATHS; path++) {
            assertTrue(RunSkills.PATH_NAMES[path] + " " + notables[path], notables[path] >= 10);
            assertTrue(roots[path] >= 4);
            List<RunSkills.Notable> list = RunSkills.notables(path);
            int actives = 0, costly = 0;
            for (RunSkills.Notable notable : list) {
                if (notable.kind == RunSkills.ACTIVE) actives++;
                if (notable.cost >= 0) costly++;
                assertTrue(notable.said().length() > 5);
            }
            assertTrue(actives >= 5);
            assertTrue(costly >= 1);
        }
        // Roots of +3, +5, +10 and +20 all appear.
        Set<Integer> figures = new HashSet<>();
        for (RunSkills.Node node : tree) if (node.type == RunSkills.ROOT) figures.add(node.figure);
        assertTrue(figures.containsAll(java.util.Arrays.asList(3, 5, 10, 20)));
        // Two paths for each of the thirteen classes.
        assertEquals(26, RunSkills.PATH_NAMES.length);
        assertEquals(13, RunSkills.CLASSES.length);
    }

    @Test public void everyNodeIsReachableFromTheCentreAndPathsCrossNearTheirEnds() {
        List<RunSkills.Node> tree = RunSkills.tree();
        BitSet seen = new BitSet();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        seen.set(0);
        while (!queue.isEmpty()) for (int next : tree.get(queue.poll()).next) if (!seen.get(next)) {
            seen.set(next);
            queue.add(next);
        }
        assertEquals(tree.size(), seen.cardinality());
        int crossings = 0;
        for (RunSkills.Node node : tree) for (int next : node.next) if (node.path >= 0 && tree.get(next).path >= 0 && tree.get(next).path != node.path) crossings++;
        // (Each path to the next round, and the first twenty-four's last still to the first, as before the knuckles' two.)
        assertEquals(RunSkills.PATHS * 2 + 2, crossings);
    }

    @Test public void nodesAreTakenNextToOnesTakenAndAddUp() {
        BitSet taken = new BitSet();
        RunSkills.Node first = RunSkills.tree().get(RunSkills.tree().get(0).next.get(0));
        RunSkills.Node far = RunSkills.tree().get(RunSkills.tree().size() - 1);
        assertTrue(RunSkills.canTake(taken, first.id));
        assertFalse(RunSkills.canTake(taken, far.id));
        taken.set(first.id);
        assertFalse(RunSkills.canTake(taken, first.id));
        RunSkills.Built built = RunSkills.built(taken);
        assertEquals(first.figure, built.stats[first.stat]);
        // Walk a whole path out and its notables and their costs count.
        int at = first.id;
        for (int step = 0; step < 60; step++) {
            int next = -1;
            for (int one : RunSkills.tree().get(at).next) if (!taken.get(one) && one != 0 && RunSkills.tree().get(one).path == first.path) next = one;
            if (next < 0) break;
            taken.set(next);
            at = next;
        }
        built = RunSkills.built(taken);
        assertFalse(built.actives.isEmpty());
        assertTrue(built.effects[RunSkills.E_DEF_PCT] < 0 || built.effects[RunSkills.E_ATK_PCT] != 0);
        assertEquals(taken, RunSkills.taken(RunSkills.note(taken)));
        assertTrue(RunSkills.respecCost(taken) > RunSkills.respecCost(new BitSet()));
        // Points without end.
        assertEquals(0, RunSkills.pointsFor(1));
        assertTrue(RunSkills.pointsFor(500) > RunSkills.pointsFor(100));
    }

    @Test public void aSaveKeepsOnlyWhatIsJoinedAndActivesGoIntoSlots() {
        List<RunSkills.Node> tree = RunSkills.tree();
        // A node far out, taken with nothing joining it to the centre (an old save): let go.
        BitSet taken = new BitSet();
        int first = tree.get(0).next.get(0);
        taken.set(first);
        taken.set(tree.size() - 1);
        BitSet whole = RunSkills.whole(taken);
        assertTrue(whole.get(first));
        assertFalse(whole.get(tree.size() - 1));
        // Walk out to the first active of the path; it goes into the first empty slot, and a second into the next.
        BitSet path = new BitSet();
        int at = 0, active = -1, second = -1;
        for (int step = 0; step < 80 && second < 0; step++) {
            int next = -1;
            for (int one : tree.get(at).next) if (one != 0 && !path.get(one) && tree.get(one).path == tree.get(first).path) next = one;
            if (next < 0) break;
            path.set(next);
            at = next;
            RunSkills.Notable notable = RunSkills.notableAt(next);
            if (notable != null && notable.kind == RunSkills.ACTIVE) {
                if (active < 0) active = next;
                else second = next;
            }
        }
        assertTrue(active > 0 && second > 0);
        int[] slots = RunSkills.slotIn(new int[3], path, active);
        slots = RunSkills.slotIn(slots, path, second);
        assertEquals(active, slots[0]);
        assertEquals(second, slots[1]);
        assertEquals(0, slots[2]);
        // Kept and read back; and a slot whose node is no longer taken is empty.
        assertEquals("" + active + "," + second + ",0", RunSkills.slotsNote(slots));
        int[] read = RunSkills.slots(RunSkills.slotsNote(slots));
        List<RunSkills.Notable> slotted = RunSkills.slotted(path, read);
        assertEquals(RunSkills.notableAt(active), slotted.get(0));
        assertEquals(RunSkills.notableAt(second), slotted.get(1));
        assertEquals(null, slotted.get(2));
        assertEquals(null, RunSkills.slotted(new BitSet(), read).get(0));
        // A passive does not go into a slot.
        int passive = -1;
        for (int id = path.nextSetBit(0); id >= 0; id = path.nextSetBit(id + 1)) {
            RunSkills.Notable notable = RunSkills.notableAt(id);
            if (notable != null && notable.kind == RunSkills.PASSIVE) passive = id;
        }
        assertTrue(passive > 0);
        assertEquals(0, RunSkills.slotIn(new int[3], path, passive)[0]);
        // Rubbish in a save reads as nothing.
        assertEquals(0, RunSkills.slots("x,,")[0]);
        assertTrue(RunSkills.taken("%%%").isEmpty());
        // Every node says what it is and what it does; notables know their path.
        for (RunSkills.Node node : tree) {
            assertTrue(RunSkills.title(node).length() > 2);
            assertTrue(RunSkills.does(node).length() > 10);
            if (node.type == RunSkills.NOTABLE) assertEquals(node.path, RunSkills.notableAt(node.id).path);
        }
    }

    @Test public void theStatsComeToWhatTheRulesUse() {
        RunSkills.Built built = new RunSkills.Built();
        assertEquals(60, RunSkills.mostHp(3, built));
        built.stats[RunSkills.HP] = 20;
        built.effects[RunSkills.E_HP_PCT] = 50;
        assertEquals(120, RunSkills.mostHp(3, built));
        assertEquals(75, RunSkills.defencePercent(200));
        assertEquals(-50, RunSkills.defencePercent(-90));
        built.stats[RunSkills.INT] = 10;
        built.effects[RunSkills.E_INT_PCT] = 50;
        assertEquals(15, RunSkills.intelligence(built, 0));
        assertEquals(20, RunSkills.intelligence(built, 50));
        // In a land: HP more at most is HP more now, and a strong passive's cost to defence makes a hurt cost more.
        PlusRun.Perks perks = new PlusRun.Perks();
        RunLevels.Level level = new RunLevels.Level(1, 40);
        for (int column = 0; column < 40; column++) for (int row = RunLevels.FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        level.gate = 37;
        PlusRun run = new PlusRun(level, perks);
        int before = run.hp();
        RunSkills.Built more = new RunSkills.Built();
        more.stats[RunSkills.HP] = 40;
        run.learn(more, new java.util.ArrayList<>());
        assertEquals(before + 40, run.hp());
        assertEquals(run.maxHp(), run.hp());
        more.effects[RunSkills.E_DEF_PCT] = -30;
        run.learn(more, new java.util.ArrayList<>());
        assertEquals(-30, run.defencePercent());
        // Actives in slots: called on for energy, resting after; energy spent while divine power is made fills it, and full, it is let go.
        RunSkills.Notable active = null;
        for (RunSkills.Notable notable : RunSkills.notables(1)) if (notable.kind == RunSkills.ACTIVE && active == null) active = notable;
        RunSkills.Built divine = new RunSkills.Built();
        divine.effects[RunSkills.E_DP] = 120;
        java.util.List<RunSkills.Notable> slots = java.util.Arrays.asList(active, active, active);
        run.learn(divine, new java.util.ArrayList<>(slots));
        assertEquals(120, run.dpMaking());
        assertTrue(run.callOn(0));
        assertFalse(run.callOn(0));
        assertEquals(PlusRun.ENERGY_MOST - PlusRun.ACTIVE_COST, run.energy(), .01f);
        assertEquals(36, run.dp(), .01f);
        assertTrue(run.callOn(1));
        assertTrue(run.callOn(2));
        assertEquals(1, run.divines());
        assertTrue(run.divineLeft() > 0);
        assertEquals(0, run.dp(), .01f);
        // Taken out of its slot, an active stops at once.
        run.learn(divine, new java.util.ArrayList<>());
        assertEquals(0, run.activeLeft(0), 0);
    }

    @Test public void anActiveIsFeltOnlyWhileCalledOn() {
        // A path walked out to its first active: the built tree does not carry the active's effect; its list does.
        java.util.List<RunSkills.Node> tree = RunSkills.tree();
        BitSet taken = new BitSet();
        int at = 0, active = -1;
        int path = tree.get(tree.get(0).next.get(0)).path;
        for (int step = 0; step < 80 && active < 0; step++) {
            int next = -1;
            for (int one : tree.get(at).next) if (one != 0 && !taken.get(one) && tree.get(one).path == path) next = one;
            if (next < 0) break;
            taken.set(next);
            at = next;
            RunSkills.Notable notable = RunSkills.notableAt(next);
            if (notable != null && notable.kind == RunSkills.ACTIVE) active = next;
        }
        RunSkills.Notable notable = RunSkills.notableAt(active);
        BitSet before = (BitSet) taken.clone();
        before.clear(active);
        assertEquals(RunSkills.built(before).effects[notable.effect], RunSkills.built(taken).effects[notable.effect]);
        assertEquals(1, RunSkills.built(taken).actives.size());
    }

    @Test public void heartsAreTwentyHpAndPastTwentyTheyTurnColour() {
        assertEquals(20, RunSkills.HEART);
        // Three hearts, two and a half full.
        float[] third = RunSkills.heart(2, 50, 60);
        assertEquals(0, third[0], 0);
        assertEquals(.5f, third[1], .001f);
        // Thirty hearts, all full: the first ten show the gold layer, the rest the red one, full.
        float[] first = RunSkills.heart(0, 600, 600), eleventh = RunSkills.heart(10, 600, 600);
        assertEquals(1, first[0], 0);
        assertEquals(1, first[1], .001f);
        assertEquals(0, eleventh[0], 0);
        assertEquals(1, eleventh[1], .001f);
        // Down to twenty-one hearts: only the first heart still shows gold.
        assertEquals(1, RunSkills.heart(0, 420, 600)[0], 0);
        assertEquals(0, RunSkills.heart(1, 420, 600)[0], 0);
        // Layers go gold, purple, orange, blue, green.
        assertEquals(6, RunSkills.LAYER_COLOURS.length);
        assertEquals(4, RunSkills.heart(0, 100 * 20, 100 * 20)[0], 0);
    }
}
