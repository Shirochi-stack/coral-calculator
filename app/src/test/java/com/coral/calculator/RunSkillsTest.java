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
        // Two paths for each of the twelve classes.
        assertEquals(24, RunSkills.PATH_NAMES.length);
        assertEquals(12, RunSkills.CLASSES.length);
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
        assertEquals(RunSkills.PATHS * 2, crossings);
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
