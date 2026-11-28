package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Her skill tree, redrawn as a web: thirteen class regions round a shared core, clusters, dead ends, hybrid routes; every node reachable
 * from every start; nothing drawn over anything; a save of the old tree let go with every point back; actives in slots; and hearts.
 */
public class RunSkillsTest {
    private static final List<RunSkills.Node> TREE = RunSkills.tree();

    private static int start(int clazz) {
        for (RunSkills.Node node : TREE) if (node.type == RunSkills.START && node.path == clazz) return node.id;
        return -1;
    }

    @Test public void aWebOfThirteenRegionsWithManyMoreNotablesThanBefore() {
        assertTrue("nodes " + TREE.size(), TREE.size() >= 1000);
        assertEquals(RunSkills.SKY_CLASSES + RunSkills.SKY_CLASS.length, RunSkills.NOTABLES.size());
        assertTrue(RunSkills.NOTABLES.size() >= 140);
        // Every notable is in the tree exactly once, and every one has its own name and its own picture.
        int[] placed = new int[RunSkills.NOTABLES.size()];
        for (RunSkills.Node node : TREE) if (node.type == RunSkills.NOTABLE) placed[node.notable]++;
        Set<String> names = new HashSet<>();
        Set<Integer> icons = new HashSet<>();
        int actives = 0, keystones = 0;
        for (RunSkills.Notable notable : RunSkills.NOTABLES) {
            assertEquals(notable.name, 1, placed[notable.index]);
            assertTrue(notable.name, names.add(notable.name));
            assertTrue(icons.add(notable.icon));
            assertTrue(notable.said(), notable.said().length() > 5);
            if (notable.kind == RunSkills.ACTIVE) actives++;
            if (notable.keystone) keystones++;
        }
        assertTrue(actives >= 30);
        // (And Skyborne, of the moves of the air.)
        assertEquals(RunSkills.CLASS_COUNT + RunSkills.CORE_KEYSTONE_COUNT + 1, keystones);
        // Thirteen starts, one for each class; each class region with its seven.
        for (int clazz = 0; clazz < RunSkills.CLASS_COUNT; clazz++) {
            assertTrue(start(clazz) > 0);
            int mine = 0;
            for (RunSkills.Node node : TREE) if (node.type == RunSkills.NOTABLE && node.path == clazz && node.notable < RunSkills.HYBRIDS) mine++;
            assertEquals(RunSkills.CLASSES[clazz], RunSkills.PER_CLASS, mine);
        }
        // Clusters: notables whose links are both to small nodes that are linked to each other's ring.
        int clusters = 0;
        for (RunSkills.Node node : TREE) {
            if (node.type != RunSkills.NOTABLE || node.next.size() != 2) continue;
            RunSkills.Node one = TREE.get(node.next.get(0)), two = TREE.get(node.next.get(1));
            if (one.type == RunSkills.SMALL && two.type == RunSkills.SMALL && one.next.contains(two.id)) clusters++;
        }
        assertTrue("clusters " + clusters, clusters >= RunSkills.CLASS_COUNT * 4 + 13);
        // Hybrid routes between each class and the next, each with its notable, for both classes.
        for (int clazz = 0; clazz < RunSkills.CLASS_COUNT; clazz++) {
            RunSkills.Notable hybrid = RunSkills.NOTABLES.get(RunSkills.HYBRIDS + clazz);
            assertEquals(1 << clazz | 1 << (clazz + 1) % RunSkills.CLASS_COUNT, hybrid.classes);
        }
    }

    @Test public void everyNodeIsReachableFromEachStartWithoutTheHiddenRoot() {
        for (int clazz = 0; clazz < RunSkills.CLASS_COUNT; clazz++) {
            BitSet seen = new BitSet();
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(start(clazz));
            seen.set(start(clazz));
            while (!queue.isEmpty()) for (int next : TREE.get(queue.poll()).next) if (next != 0 && !seen.get(next)) {
                seen.set(next);
                queue.add(next);
            }
            assertEquals(RunSkills.CLASSES[clazz], TREE.size() - 1, seen.cardinality());
        }
        // The hidden root hangs every start, and only the starts.
        assertEquals(RunSkills.CLASS_COUNT, TREE.get(0).next.size());
        for (int id : TREE.get(0).next) assertEquals(RunSkills.START, TREE.get(id).type);
        // Any start can be taken first; nothing else can.
        BitSet none = new BitSet();
        for (RunSkills.Node node : TREE) if (node.id > 0) assertEquals(node.type == RunSkills.START, RunSkills.canTake(none, node.id));
    }

    @Test public void deadEndsReallyEnd() {
        int ends = 0;
        for (RunSkills.Node node : TREE) {
            if (node.type == RunSkills.NOTABLE && RunSkills.NOTABLES.get(node.notable).keystone) assertEquals(RunSkills.NOTABLES.get(node.notable).name, 1, node.next.size());
            if (node.type == RunSkills.NOTABLE && node.notable % RunSkills.PER_CLASS == RunSkills.DEEP && node.notable < RunSkills.HYBRIDS) assertEquals(1, node.next.size());
            if (node.id > 0 && node.next.size() == 1 && node.type != RunSkills.START) {
                ends++;
                // A dead end is reached by a spur: walk back from it, and only after small nodes does the way fork.
                int at = node.next.get(0), from = node.id, steps = 0;
                while (TREE.get(at).next.size() == 2 && steps < 5) {
                    int next = TREE.get(at).next.get(0) == from ? TREE.get(at).next.get(1) : TREE.get(at).next.get(0);
                    from = at;
                    at = next;
                    steps++;
                }
                assertTrue(node.id + " ends after " + steps, steps >= 1);
                assertTrue(TREE.get(at).next.size() >= 3);
            }
        }
        // Spurs to every class's deep notable and keystone, the core's keystones, its other dead ends, and the regions' jewels.
        assertTrue("dead ends " + ends, ends >= RunSkills.CLASS_COUNT * 3 + RunSkills.CORE_KEYSTONE_COUNT + 13);
    }

    @Test public void noNodeIsDrawnOverAnotherAndNoLinkRunsUnderOne() {
        for (int one = 1; one < TREE.size(); one++) {
            RunSkills.Node a = TREE.get(one);
            for (int two = one + 1; two < TREE.size(); two++) {
                RunSkills.Node b = TREE.get(two);
                float far = (float) Math.hypot(a.x - b.x, a.y - b.y), need = (RunTree.size(a) + RunTree.size(b)) * 1.35f + .2f;
                assertTrue(one + " and " + two + " are " + far + " apart, need " + need, far >= need);
            }
        }
        for (RunSkills.Node a : TREE) {
            if (a.id == 0) continue;
            for (int other : a.next) {
                if (other < a.id) continue;
                RunSkills.Node b = TREE.get(other);
                for (RunSkills.Node node : TREE) {
                    if (node.id == 0 || node.id == a.id || node.id == b.id) continue;
                    assertTrue("the link " + a.id + "-" + b.id + " runs under " + node.id, gap(node.x, node.y, a.x, a.y, b.x, b.y) >= RunTree.size(node) * 1.1f + .1f);
                }
            }
        }
        // Everything is inside the tree's reach, with room for the emblems past it.
        for (RunSkills.Node node : TREE) assertTrue(Math.hypot(node.x, node.y) < RunSkills.REACH - 3);
    }

    private static float gap(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay, t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)));
        return (float) Math.hypot(px - ax - t * dx, py - ay - t * dy);
    }

    @Test public void theRulesOfWhatNotablesDo() {
        for (RunSkills.Notable notable : RunSkills.NOTABLES) {
            // Only keystones, a few big late notables and an active or two cost anything; a cost is always felt.
            boolean late = notable.index < RunSkills.HYBRIDS && notable.index % RunSkills.PER_CLASS >= RunSkills.DEEP;
            if (notable.costly()) assertTrue(notable.name, notable.keystone || late || notable.kind == RunSkills.ACTIVE);
            if (notable.keystone) assertTrue(notable.name, notable.costly() || notable.of(RunSkills.E_BLOOD) > 0 || notable.of(RunSkills.E_IRON) > 0);
            for (int at = 0; at < notable.effects.length; at++) assertTrue(notable.name, !RunSkills.phrase(notable.effects[at], notable.figures[at]).isEmpty());
        }
        // A class's passives are for its own weapons; its actives for anything; the core's for anything.
        RunSkills.Notable twin = RunSkills.NOTABLES.get(RunSkills.OUTER_LEFT);
        assertEquals(1, twin.classes);
        assertTrue(twin.said().startsWith("With swords and the brush"));
        assertEquals(0, RunSkills.NOTABLES.get(RunSkills.ACTIVE_LEFT).classes);
        // Effects that are what she is are felt whatever she holds, even from a class's notable.
        RunSkills.Built built = new RunSkills.Built();
        built.add(RunSkills.E_HP_PCT, 20, 1 << 2);
        built.add(RunSkills.E_AREA, 30, 1 << 2);
        built.add(RunSkills.E_AREA, -10, 1 << 2);
        assertEquals(20, built.with(RunSkills.E_HP_PCT, -1));
        assertEquals(30 - 10, built.with(RunSkills.E_AREA, 2));
        assertEquals(-10, built.with(RunSkills.E_AREA, 0));
        // The weapons of each class.
        assertEquals(0, RunSkills.classOfWeapon(RunArms.BRUSH));
        assertEquals(3, RunSkills.classOfWeapon(RunArms.TABLET));
        assertEquals(5, RunSkills.classOfWeapon(RunArms.KEYS));
        assertEquals(10, RunSkills.classOfWeapon(RunArms.CANNON));
        assertEquals(11, RunSkills.classOfWeapon(RunArms.WHIP));
        assertEquals(-1, RunSkills.classOfWeapon(PlusRun.UNARMED));
        for (int clazz = 0; clazz < RunSkills.CLASS_COUNT; clazz++) assertEquals(clazz, RunSkills.classOfWeapon(RunSkills.weaponOf(clazz)));
    }

    @Test public void nodesAreTakenNextToOnesTakenAndAddUp() {
        BitSet taken = new BitSet();
        int start = start(0);
        assertTrue(RunSkills.canTake(taken, start));
        taken.set(start);
        assertFalse(RunSkills.canTake(taken, start));
        RunSkills.Built built = RunSkills.built(taken);
        assertEquals(RunSkills.START_ATTACK, built.with(RunSkills.E_ATK_PCT, 0));
        assertEquals(0, built.with(RunSkills.E_ATK_PCT, 1));
        // Two steps in, the notable by the start: fun at once.
        RunSkills.Node early = null;
        for (int one : TREE.get(start).next) for (int two : TREE.get(one).next) if (TREE.get(two).type == RunSkills.NOTABLE) early = TREE.get(two);
        assertNotNull(early);
        assertEquals(RunSkills.EARLY, early.notable % RunSkills.PER_CLASS);
        assertEquals(taken, RunSkills.taken(RunSkills.note(taken)));
        assertTrue(RunSkills.respecCost(taken) > RunSkills.respecCost(new BitSet()));
        assertEquals(0, RunSkills.pointsFor(1));
        assertTrue(RunSkills.pointsFor(500) > RunSkills.pointsFor(100));
    }

    @Test public void aSaveOfTheOldTreeIsLetGoOnceWithEveryPointBack() {
        // An old save with nodes taken: let go, the first time; never again after.
        BitSet old = new BitSet();
        for (int id = 1; id < 60; id += 3) old.set(id);
        String note = RunSkills.note(old);
        assertTrue(RunSkills.fromBefore(0, note));
        assertFalse(RunSkills.fromBefore(1, note));
        assertFalse(RunSkills.fromBefore(0, ""));
        assertFalse(RunSkills.fromBefore(0, null));
        // Let go, nothing is spent: every point her level has earned is hers to spend, exactly.
        for (int level : new int[]{1, 2, 10, 37, 100, 250}) {
            BitSet after = RunSkills.whole(RunSkills.taken(""));
            assertEquals(RunSkills.pointsFor(level), RunSkills.pointsFor(level) - RunSkills.spent(after));
            assertEquals(0, RunSkills.slotted(after, RunSkills.slots("")).stream().filter(java.util.Objects::nonNull).count());
        }
        assertEquals("The tree was redrawn; your points are back to spend", RunSkills.REDRAWN_NOTE);
        // And a save of the new tree keeps what is joined, from any start.
        BitSet kept = new BitSet();
        kept.set(start(4));
        kept.set(TREE.get(start(4)).next.get(1));
        kept.set(TREE.size() - 1);
        BitSet whole = RunSkills.whole(kept);
        assertTrue(whole.get(start(4)));
        assertFalse(whole.get(TREE.size() - 1));
    }

    @Test public void theNodesKeepTheirNumbers() {
        // What each node is, by its number, folded into one figure: if this changes, saves of the tree change meaning. Anything new goes after.
        long sum = 17;
        // (The first 1199 as first drawn; the moves of the air were added after them.)
        for (RunSkills.Node node : TREE.subList(0, 1199)) sum = sum * 31 + node.type * 7 + node.path * 101 + (node.type == RunSkills.NOTABLE ? RunSkills.NOTABLES.get(node.notable).name.hashCode() : node.stat * 13 + node.effect);
        assertTrue("nodes " + TREE.size(), TREE.size() > 1199 + 30);
        assertEquals(KEPT, sum);
    }

    /** (The figure the tree as first drawn folds to.) */
    private static final long KEPT = 2940483755120975924L;

    @Test public void activesGoIntoSlotsAndAreFeltOnlyWhileCalledOn() {
        // From a start into its first cluster of actives.
        int start = start(1);
        RunSkills.Node active = null;
        for (RunSkills.Node node : TREE) if (node.type == RunSkills.NOTABLE && node.notable == RunSkills.PER_CLASS + RunSkills.ACTIVE_LEFT) active = node;
        assertNotNull(active);
        BitSet taken = path(start, active.id);
        assertTrue(taken.get(active.id));
        assertEquals(taken, RunSkills.whole(taken));
        RunSkills.Notable notable = RunSkills.notableAt(active.id);
        assertEquals(RunSkills.ACTIVE, notable.kind);
        int effect = notable.effects[0];
        BitSet before = (BitSet) taken.clone();
        before.clear(active.id);
        assertEquals(RunSkills.built(before).with(effect, 1), RunSkills.built(taken).with(effect, 1));
        assertEquals(1, RunSkills.built(taken).actives.size());
        int[] slots = RunSkills.slotIn(new int[3], taken, active.id);
        assertEquals(active.id, slots[0]);
        assertEquals("" + active.id + ",0,0", RunSkills.slotsNote(slots));
        assertEquals(notable, RunSkills.slotted(taken, RunSkills.slots(RunSkills.slotsNote(slots))).get(0));
        assertNull(RunSkills.slotted(new BitSet(), slots).get(0));
        // A passive does not go into a slot.
        assertEquals(0, RunSkills.slotIn(new int[3], taken, start)[0]);
        // Called on in a land: felt while it lasts, through what she has now.
        PlusRun.Perks perks = new PlusRun.Perks();
        RunLevels.Level level = new RunLevels.Level(1, 40);
        for (int column = 0; column < 40; column++) for (int row = RunLevels.FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        level.gate = 37;
        PlusRun run = new PlusRun(level, perks);
        run.learn(RunSkills.built(taken), RunSkills.slotted(taken, slots));
        int was = run.skillOf(effect);
        assertTrue(run.callOn(0));
        assertEquals(was + notable.of(effect), run.skillOf(effect));
        for (int step = 0; step < 60 * 9; step++) run.step(1 / 60f, false, false, false);
        assertEquals(was, run.skillOf(effect));
        // Every node says what it is and what it does.
        for (RunSkills.Node node : TREE) {
            assertTrue(RunSkills.title(node).length() > 2);
            assertTrue(RunSkills.does(node).length() > 10);
            assertTrue(RunSkills.kindWords(node).length() > 3);
        }
    }

    /** The nodes on the shortest way from one node to another, both taken. */
    private static BitSet path(int from, int to) {
        int[] back = new int[TREE.size()];
        java.util.Arrays.fill(back, -1);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(from);
        back[from] = from;
        while (!queue.isEmpty()) {
            int at = queue.poll();
            for (int next : TREE.get(at).next) if (next != 0 && back[next] < 0) {
                back[next] = at;
                queue.add(next);
            }
        }
        BitSet taken = new BitSet();
        for (int at = to; at != from; at = back[at]) taken.set(at);
        taken.set(from);
        return taken;
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
        more.effects[RunSkills.E_DEF_PCT] = -30;
        run.learn(more, new java.util.ArrayList<>());
        assertEquals(-30, run.defencePercent());
        // Divine power from energy spent while it is made; full, it is let go.
        RunSkills.Notable spark = RunSkills.NOTABLES.get(RunSkills.CORE_ACTIVES + 3);
        assertTrue(spark.of(RunSkills.E_DP) > 0);
        RunSkills.Built divine = new RunSkills.Built();
        divine.effects[RunSkills.E_DP] = 120;
        run.learn(divine, new java.util.ArrayList<>(java.util.Arrays.asList(spark, spark, spark)));
        assertEquals(120, run.dpMaking());
        assertTrue(run.callOn(0));
        assertFalse(run.callOn(0));
        assertEquals(PlusRun.ENERGY_MOST - PlusRun.ACTIVE_COST, run.energy(), .01f);
        assertTrue(run.dp() > 0);
        run.learn(divine, new java.util.ArrayList<>());
        assertEquals(0, run.activeLeft(0), 0);
    }

    @Test public void theNotablesPicturesAreInTheRunsAssetPackNotTheBase() {
        java.io.File sheet = new java.io.File("../runart/src/main/assets/" + RunTree.NOTABLE_SHEET);
        assertTrue(sheet.isFile());
        assertTrue(sheet.length() > 100_000);
        // (The base keeps only the old sheet of emblems and node pictures.)
        assertTrue(new java.io.File("src/main/res/drawable-nodpi/run_skill_icons.webp").length() < 300_000);
    }

    @Test public void eachClassRegionHasItsOwnNodePictures() {
        Set<Integer> smalls = new HashSet<>(), roots = new HashSet<>();
        for (int clazz = 0; clazz < RunSkills.CLASS_COUNT; clazz++) {
            Set<Integer> mine = new HashSet<>();
            for (RunSkills.Node node : TREE) {
                if (node.path != clazz || node.type != RunSkills.SMALL && node.type != RunSkills.ROOT) continue;
                int icon = RunTree.iconOf(node);
                assertTrue(icon >= RunTree.PACK_NODES && icon < RunTree.PACK_NODES + RunTree.PACK_NODE_COUNT);
                mine.add(icon);
                (node.type == RunSkills.SMALL ? smalls : roots).add(icon);
            }
            assertEquals(RunSkills.CLASSES[clazz], 2, mine.size());
        }
        assertEquals(RunSkills.CLASS_COUNT, smalls.size());
        assertEquals(RunSkills.CLASS_COUNT, roots.size());
        // The core's small nodes by stat, and the hybrid routes' their own; each card still names what it gives.
        for (RunSkills.Node node : TREE) {
            if (node.type != RunSkills.SMALL || node.path < RunSkills.CLASS_COUNT) continue;
            int icon = RunTree.iconOf(node);
            assertEquals(node.path >= RunSkills.HYBRID ? RunTree.PACK_NODES + RunTree.HYBRID_SMALL : RunTree.PACK_NODES + RunTree.CORE_SMALLS + node.stat, icon);
            assertTrue(RunSkills.title(node).length() > 3);
        }
        assertTrue(new java.io.File("../runart/src/main/assets/" + RunTree.NODE_SHEET).isFile());
    }

    @Test public void heartsAreTwentyHpAndPastTwentyTheyTurnColour() {
        assertEquals(20, RunSkills.HEART);
        float[] third = RunSkills.heart(2, 50, 60);
        assertEquals(0, third[0], 0);
        assertEquals(.5f, third[1], .001f);
        float[] first = RunSkills.heart(0, 600, 600), eleventh = RunSkills.heart(10, 600, 600);
        assertEquals(1, first[0], 0);
        assertEquals(1, first[1], .001f);
        assertEquals(0, eleventh[0], 0);
        assertEquals(1, eleventh[1], .001f);
        assertEquals(1, RunSkills.heart(0, 420, 600)[0], 0);
        assertEquals(0, RunSkills.heart(1, 420, 600)[0], 0);
        assertEquals(6, RunSkills.LAYER_COLOURS.length);
        assertEquals(4, RunSkills.heart(0, 100 * 20, 100 * 20)[0], 0);
    }
}
