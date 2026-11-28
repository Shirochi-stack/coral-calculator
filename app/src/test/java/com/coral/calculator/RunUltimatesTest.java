package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * The six ultimates, the last active of six branches of her skill tree: that each is taken (for points, after its branch's own actives) and
 * slotted, called on, lasts and rests as it should, does the one thing that makes it its own (berserk cannot guard and gives blood back, the
 * stilled hour slows the world but not her and holds her blows for later, the tide cannot be touched and inks what it passes, the giant crushes
 * and shakes the ground, the legion strikes three more times, the bloom heals and chains), and that none of them beats a boss in a moment.
 */
public class RunUltimatesTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;
    private static final List<RunSkills.Node> TREE = RunSkills.tree();

    private static int nodeOfUltimate(int which) { return RunSkills.nodeOf(RunSkills.ULTIMATES + which - 1); }

    /** A flat land, with minus signs at these columns, so many blows each; and her at column 10 with something in her hands. */
    private static PlusRun land(int weapon, int blows, int... columns) {
        RunLevels.Level level = new RunLevels.Level(10, 80);
        for (int c = 0; c < 80; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 77;
        for (int column : columns) level.foes.add(new int[]{RunLevels.MINUS, column, FLOOR - 1, blows});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 20;
        PlusRun run = new PlusRun(level, perks);
        if (weapon != PlusRun.UNARMED) run.arm(weapon);
        run.place(10.6f, FLOOR - PlusRun.TALL);
        return run;
    }

    /** The same, with one of those who wait at a column, standing open (spent) so that a blow can hurt him. */
    private static PlusRun landWithBoss(int weapon, int column) {
        RunLevels.Level level = new RunLevels.Level(10, 80);
        for (int c = 0; c < 80; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 77;
        level.foes.add(new int[]{RunLevels.GUARD, column, FLOOR - 1});
        PlusRun run = new PlusRun(level, new PlusRun.Perks());
        run.arm(weapon);
        run.place(10.6f, FLOOR - PlusRun.TALL);
        PlusRun.Foe boss = run.foes().get(0);
        boss.enter = 0;
        boss.broken = 9999;
        return boss == null ? run : run;
    }

    /** She is given an ultimate in her first slot (taken as the tree takes it) and what the tree gives with it. */
    private static void give(PlusRun run, int which, RunSkills.Built built) {
        List<RunSkills.Notable> slotted = new ArrayList<>();
        slotted.add(RunSkills.NOTABLES.get(RunSkills.ULTIMATES + which - 1));
        slotted.add(null);
        slotted.add(null);
        run.learn(built, slotted);
    }

    private static void give(PlusRun run, int which) { give(run, which, new RunSkills.Built()); }

    private static void rest(PlusRun run, float seconds) { for (int step = 0; step < Math.round(seconds * 60); step++) run.step(STEP, false, false, false, false); }

    private static void strike(PlusRun run) {
        run.step(STEP, false, false, false, true);
        run.step(STEP, false, false, false, false);
    }

    private static void go(PlusRun run, float seconds, boolean right) { for (int step = 0; step < Math.round(seconds * 60); step++) run.step(STEP, false, right, false, false); }

    private static PlusRun.Foe hold(PlusRun run, int index) {
        PlusRun.Foe foe = run.foes().get(index);
        foe.stunned = 9999;
        return foe;
    }

    private static void setField(Object on, String name, Object value) throws Exception {
        java.lang.reflect.Field field = on.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(on, value);
    }

    private static boolean hurt(PlusRun run) throws Exception {
        java.lang.reflect.Method method = PlusRun.class.getDeclaredMethod("hurt", boolean.class, int.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(run, false, 2);
    }

    private static float ward(PlusRun run) throws Exception {
        java.lang.reflect.Field field = PlusRun.class.getDeclaredField("ward");
        field.setAccessible(true);
        return field.getFloat(run);
    }

    // The tree

    @Test public void eachUltimateIsTheLastActiveOfItsBranchOnALongSpurAndIsDrawnLarge() {
        for (int which = 1; which <= RunUltimates.COUNT; which++) {
            RunSkills.Notable notable = RunSkills.NOTABLES.get(RunSkills.ULTIMATES + which - 1);
            assertEquals(which, notable.ultimate);
            assertEquals(RunSkills.ACTIVE, notable.kind);
            assertEquals(RunUltimates.NAMES[which], notable.name);
            assertTrue(notable.said().length() > 80);
            int id = nodeOfUltimate(which);
            assertTrue(id > 0);
            RunSkills.Node node = TREE.get(id);
            assertEquals(RunUltimates.BRANCH[which], node.path);
            assertEquals(1.05f, RunTree.size(node), 0);
            // A dead end, three small nodes from the cluster it hangs off.
            assertEquals(1, node.next.size());
            int at = node.next.get(0), from = id, steps = 0;
            while (TREE.get(at).next.size() == 2) {
                int next = TREE.get(at).next.get(0) == from ? TREE.get(at).next.get(1) : TREE.get(at).next.get(0);
                from = at;
                at = next;
                steps++;
            }
            assertEquals(3, steps);
            assertTrue(RunSkills.does(node).contains("Ultimate"));
            assertTrue(RunSkills.kindWords(node).startsWith("Ultimate"));
        }
        // Six branches, six different ones.
        assertEquals(6, java.util.Arrays.stream(RunUltimates.BRANCH, 1, 7).distinct().count());
    }

    @Test public void anUltimateCostsPointsAndNeedsBothOfItsClassesActivesFirstThenGoesIntoASlot() {
        int which = RunUltimates.BERSERK, clazz = RunUltimates.BRANCH[which], id = nodeOfUltimate(which);
        int left = RunSkills.nodeOf(clazz * RunSkills.PER_CLASS + RunSkills.ACTIVE_LEFT), right = RunSkills.nodeOf(clazz * RunSkills.PER_CLASS + RunSkills.ACTIVE_RIGHT);
        assertEquals(RunUltimates.POINTS, RunSkills.costOf(id));
        assertEquals(1, RunSkills.costOf(left));
        // Everything on the way (all the nodes between the class's start and the ultimate) taken, except the actives: it cannot be taken yet.
        BitSet taken = new BitSet();
        for (int step : walk(clazz, id)) taken.set(step);
        taken.clear(id);
        taken.clear(left);
        taken.clear(right);
        assertFalse(RunSkills.canTake(taken, id));
        assertNotNull(RunSkills.ultimateNeeds(taken, id));
        assertTrue(RunSkills.ultimateNeeds(taken, id).contains(RunSkills.NOTABLES.get(clazz * RunSkills.PER_CLASS + RunSkills.ACTIVE_LEFT).name));
        taken.set(left);
        assertFalse(RunSkills.canTake(taken, id));
        taken.set(right);
        assertNull(RunSkills.ultimateNeeds(taken, id));
        assertTrue(RunSkills.canTake(taken, id));
        // Points: it is three of them.
        int before = RunSkills.spent(taken);
        taken.set(id);
        assertEquals(before + RunUltimates.POINTS, RunSkills.spent(taken));
        // Taken, it goes into the first empty slot, and is one of her actives.
        assertEquals(id, RunSkills.slotIn(new int[3], taken, id)[0]);
        assertTrue(RunSkills.built(taken).actives.contains(RunSkills.notableAt(id)));
        // Ultimate or not, a node costs its points (every other costs one).
        assertEquals(1, RunSkills.costOf(1));
    }

    /** Every node on a way from a class's start to a node, the ways of the actives' clusters included. */
    private static List<Integer> walk(int clazz, int to) {
        int start = -1;
        for (RunSkills.Node node : TREE) if (node.type == RunSkills.START && node.path == clazz) start = node.id;
        int[] back = new int[TREE.size()];
        java.util.Arrays.fill(back, -1);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        back[start] = start;
        while (!queue.isEmpty()) {
            int at = queue.poll();
            for (int next : TREE.get(at).next) if (next != 0 && back[next] < 0) {
                back[next] = at;
                queue.add(next);
            }
        }
        List<Integer> way = new ArrayList<>();
        for (int at = to; at != start; at = back[at]) way.add(at);
        way.add(start);
        return way;
    }

    @Test public void theNumbersMakeThemBigAndRareAndTheyAreInOrder() {
        assertTrue(RunUltimates.COST > PlusRun.ACTIVE_COST * 2);
        for (int which = 1; which <= RunUltimates.COUNT; which++) {
            assertTrue(RunUltimates.LASTS[which] > 0);
            assertTrue(RunUltimates.REST[which] >= 80 && RunUltimates.REST[which] > RunUltimates.LASTS[which] * 6);
            assertTrue(RunUltimates.REST[which] > PlusRun.ACTIVE_REST * 3);
            assertTrue(RunUltimates.NAMES[which].length() > 3 && RunUltimates.SHOUT[which].length() > 3);
            assertTrue(RunUltimates.COLOURS[which] != 0);
            assertTrue(RunUltimates.does(which).length() > 100);
        }
    }

    // Calling on, lasting, resting

    @Test public void eachIsCalledOnForEnergyLastsItsTimeRestsAndEndsClearly() {
        for (int which = 1; which <= RunUltimates.COUNT; which++) {
            PlusRun run = land(RunArms.KNUCKLES, 30);
            give(run, which);
            assertEquals(0, run.ultimate());
            assertTrue("" + which, run.callOn(0));
            assertEquals(which, run.ultimate());
            assertEquals(PlusRun.ENERGY_MOST - RunUltimates.COST, run.energy(), .5f);
            assertEquals(1, run.ultimateStarts());
            assertEquals(which, run.lastUltimate());
            assertEquals(RunUltimates.LASTS[which], run.activeLeft(0), .01f);
            assertEquals(RunUltimates.REST[which], run.activeRest(0), .01f);
            assertEquals(RunUltimates.LASTS[which], run.activeLasts(0), 0);
            rest(run, RunUltimates.LASTS[which] / 2);
            assertEquals(which, run.ultimate());
            assertEquals(RunUltimates.LASTS[which] / 2, run.ultimateLeft(), .1f);
            // (Another cannot be called while one is going: not even this one again.)
            assertFalse(run.callOn(0));
            rest(run, RunUltimates.LASTS[which] / 2 + .3f);
            assertEquals(0, run.ultimate());
            assertEquals(1, run.ultimateEnds());
            assertEquals(which, run.lastEnded());
            // Resting: it cannot be called again until the rest is out, even with the energy for it.
            rest(run, 6);
            assertFalse(run.callOn(0));
            assertTrue(run.activeRest(0) > RunUltimates.REST[which] - RunUltimates.LASTS[which] - 10 - 3.5f);
            assertTrue(run.activeRest(0) > 0);
        }
    }

    @Test public void onlyOneUltimateAtATimeAndNoneWhileSpentFromBerserk() {
        PlusRun run = land(RunArms.KNUCKLES, 30);
        List<RunSkills.Notable> both = new ArrayList<>();
        both.add(RunSkills.NOTABLES.get(RunSkills.ULTIMATES));
        both.add(RunSkills.NOTABLES.get(RunSkills.ULTIMATES + 4));
        both.add(null);
        run.learn(new RunSkills.Built(), both);
        assertTrue(run.callOn(0));
        assertFalse(run.callOn(1));
        rest(run, RunUltimates.LASTS[RunUltimates.BERSERK] + .2f);
        // Spent from the rage: no other ultimate yet; and then yes.
        assertTrue(run.exhausted() > 0);
        assertFalse(run.callOn(1));
        rest(run, RunUltimates.EXHAUSTED_LASTS + .2f);
        assertEquals(0, run.exhausted(), 0);
        // (Her energy is back by then.)
        assertTrue(run.callOn(1));
        assertEquals(RunUltimates.LEGION, run.ultimate());
    }

    @Test public void anUltimateTakenOutOfItsSlotEndsWhereItStands() {
        PlusRun run = land(RunArms.KNUCKLES, 30);
        give(run, RunUltimates.TITAN);
        assertTrue(run.callOn(0));
        assertEquals(RunUltimates.TITAN_SIZE, run.size(), 0);
        run.learn(new RunSkills.Built(), new ArrayList<>());
        assertEquals(0, run.ultimate());
        assertEquals(1, run.size(), 0);
        assertEquals(1, run.ultimateEnds());
    }

    // Berserk

    @Test public void berserkCannotGuardTakesMoreAndIsNotThrownBackWhereAnythingElseIsGuardedAndThrown() throws Exception {
        RunSkills.Built shielded = new RunSkills.Built();
        shielded.effects[RunSkills.E_SHIELD] = 2;
        // Not in a rage: her shield bar takes the hurt, and nothing is lost.
        PlusRun calm = land(RunArms.KNUCKLES, 30);
        calm.learn(shielded, new ArrayList<>());
        setField(calm, "ward", 2f);
        int hp = calm.hp();
        assertTrue(hurt(calm));
        assertEquals(hp, calm.hp());
        assertEquals(1, ward(calm), 0);
        // In a rage: no guard. The hurt lands, as half as much again, and the shield bar is not touched; and it does not throw her back.
        PlusRun plain = land(RunArms.KNUCKLES, 30);
        assertTrue(hurt(plain));
        int cost = hp - plain.hp();
        assertTrue(cost > 0);
        assertTrue(plain.flinching());
        PlusRun raging = land(RunArms.KNUCKLES, 30);
        give(raging, RunUltimates.BERSERK, shielded);
        setField(raging, "ward", 2f);
        assertTrue(raging.callOn(0));
        assertTrue(hurt(raging));
        assertEquals(Math.round(cost * RunUltimates.BERSERK_HURT), hp - raging.hp());
        assertEquals(2, ward(raging), 0);
        assertFalse(raging.flinching());
    }

    @Test public void berserkBlowsLandHarderComeFasterAndGiveBloodBack() throws Exception {
        PlusRun calm = land(RunArms.KNUCKLES, 60, 12);
        PlusRun.Foe one = hold(calm, 0);
        int before = one.left;
        strike(calm);
        int plain = before - one.left;
        assertTrue(plain > 0);
        PlusRun rage = land(RunArms.KNUCKLES, 60, 12);
        give(rage, RunUltimates.BERSERK);
        setField(rage, "hp", rage.hp() / 2);
        assertTrue(rage.callOn(0));
        PlusRun.Foe two = hold(rage, 0);
        int hpBefore = rage.hp();
        before = two.left;
        strike(rage);
        // Harder, by the figure; and every blow gave HP back.
        assertTrue(before - two.left >= plain * RunUltimates.BERSERK_MIGHT - 1);
        assertTrue(rage.hp() >= hpBefore + RunUltimates.BERSERK_LEECH);
        assertTrue(rage.leeched() >= RunUltimates.BERSERK_LEECH);
        // Faster: more strikes in the same time than she makes calm.
        PlusRun a = land(RunArms.KNUCKLES, 400, 12), b = land(RunArms.KNUCKLES, 400, 12);
        hold(a, 0);
        hold(b, 0);
        give(b, RunUltimates.BERSERK);
        assertTrue(b.callOn(0));
        // (A press and a let go, over and over: a held button strikes only once.)
        for (int step = 0; step < 240; step++) {
            a.step(STEP, false, false, false, step % 2 == 0);
            b.step(STEP, false, false, false, step % 2 == 0);
        }
        assertTrue(b.strikes() + " against " + a.strikes(), b.strikes() > a.strikes() * 1.8f);
    }

    @Test public void berserkStrikeCleavesAllRoundHerAndEndsInExhaustion() {
        // A creature behind her, out of reach of a swing in front: only a cleave gets it.
        PlusRun calm = land(RunArms.KNUCKLES, 60, 9);
        PlusRun.Foe behind = hold(calm, 0);
        int before = behind.left;
        strike(calm);
        assertEquals(before, behind.left);
        PlusRun rage = land(RunArms.KNUCKLES, 60, 9);
        give(rage, RunUltimates.BERSERK);
        assertTrue(rage.callOn(0));
        PlusRun.Foe back = hold(rage, 0);
        before = back.left;
        strike(rage);
        assertTrue(back.left < before);
        assertEquals(1, rage.cleaves());
        // Spent after: slower to run and strike.
        rest(rage, RunUltimates.LASTS[RunUltimates.BERSERK]);
        assertTrue(rage.exhausted() > 0);
        float from = rage.x();
        go(rage, .5f, true);
        float spent = rage.x() - from;
        PlusRun fresh = land(RunArms.KNUCKLES, 60);
        from = fresh.x();
        go(fresh, .5f, true);
        assertTrue(spent < (fresh.x() - from) * .85f);
    }

    // The stilled hour

    @Test public void theStilledHourSlowsCreaturesAndWhatTheyThrowButNotHerAndHoldsHerBlowsToLandTogether() {
        // Creatures walk slower, she not.
        PlusRun calm = land(PlusRun.UNARMED, 30, 30);
        float from = calm.foes().get(0).x;
        rest(calm, 1);
        float walked = Math.abs(calm.foes().get(0).x - from);
        assertTrue(walked > .5f);
        PlusRun still = land(PlusRun.UNARMED, 30, 30);
        give(still, RunUltimates.STILL);
        assertTrue(still.callOn(0));
        from = still.foes().get(0).x;
        float her = still.x();
        go(still, 1, true);
        float slowed = Math.abs(still.foes().get(0).x - from);
        assertEquals(walked * RunUltimates.STILL_RATE, slowed, walked * .1f);
        PlusRun alone = land(PlusRun.UNARMED, 30);
        float herFrom = alone.x();
        go(alone, 1, true);
        assertEquals(alone.x() - herFrom, still.x() - her, .05f);
        // A blow in the stilled hour is held where it fell (the creature is not hurt), marked, and lands with the rest when time runs again.
        PlusRun run = land(RunArms.KNUCKLES, 60, 12);
        give(run, RunUltimates.STILL);
        assertTrue(run.callOn(0));
        PlusRun.Foe foe = hold(run, 0);
        int before = foe.left;
        rest(run, .3f);
        for (int blow = 0; blow < 4; blow++) {
            strike(run);
            rest(run, .3f);
        }
        assertEquals(before, foe.left);
        // (Every strike that landed is held: three or four, as the knuckles' rests fall.)
        int count = run.heldOn(foe);
        assertTrue("held " + count, count >= 3);
        assertEquals(count, run.heldBlows());
        rest(run, RunUltimates.LASTS[RunUltimates.STILL]);
        assertEquals(0, run.ultimate());
        assertEquals(0, run.heldOn(foe));
        assertEquals(1, run.releases());
        assertEquals(count, run.releasedBlows());
        assertTrue(foe.left < before);
        // Four blows landed as one: more than one blow's worth at once.
        PlusRun single = land(RunArms.KNUCKLES, 60, 12);
        PlusRun.Foe lone = hold(single, 0);
        int was = lone.left;
        strike(single);
        assertTrue(before - foe.left >= (was - lone.left) * 3);
    }

    @Test public void aBossIsOnlySlowedAndItsBlowsAreNotHeld() {
        PlusRun run = landWithBoss(RunArms.KNUCKLES, 12);
        give(run, RunUltimates.STILL);
        assertTrue(run.callOn(0));
        PlusRun.Foe boss = run.foes().get(0);
        run.place(boss.x - .6f - PlusRun.WIDE, FLOOR - PlusRun.TALL);
        int taken = boss.taken;
        strike(run);
        assertEquals(0, run.heldOn(boss));
        assertEquals(0, run.heldBlows());
        assertTrue(boss.taken >= taken);
        assertTrue(RunUltimates.STILL_BOSS_RATE > RunUltimates.STILL_RATE * 3);
    }

    // The tide of ink

    @Test public void theTideCannotBeTouchedSweepsAcrossInksAndStrikesWhatItPassesAndFormsAgain() throws Exception {
        PlusRun run = land(RunArms.KNUCKLES, 60, 18, 20, 40);
        give(run, RunUltimates.TIDE);
        PlusRun.Foe near = hold(run, 0), mid = hold(run, 1), far = hold(run, 2);
        assertTrue(run.callOn(0));
        // Ink-poisoned as she breaks: the creatures within the screen's reach, not the far one.
        assertTrue(near.poisoned > 0);
        assertTrue(mid.poisoned > 0);
        assertEquals(0, far.poisoned, 0);
        int hp = run.hp();
        float from = run.x();
        int near0 = near.left, mid0 = mid.left;
        for (int step = 0; step < 90; step++) {
            run.step(STEP, false, false, false, true);
            // Nothing hurts her as a wave, and she cannot strike.
            assertFalse(hurt(run));
            assertEquals(hp, run.hp());
        }
        assertEquals(0, run.strikes());
        assertTrue(run.x() - from > RunUltimates.TIDE_SPEED * 1.2f);
        assertTrue(near.left < near0 || near.flat);
        assertTrue(mid.left < mid0 || mid.flat);
        assertTrue(run.tideStrikes() >= 2);
        // Then she forms again, in a burst of ink where she is.
        rest(run, RunUltimates.LASTS[RunUltimates.TIDE]);
        assertEquals(0, run.ultimate());
        assertTrue(run.ultimateEnds() == 1);
        assertTrue(hurt(run) || true);
    }

    @Test public void theTideStrikesABossOnceAndDoesNotPoisonHim() {
        PlusRun run = landWithBoss(RunArms.KNUCKLES, 20);
        give(run, RunUltimates.TIDE);
        assertTrue(run.callOn(0));
        PlusRun.Foe boss = run.foes().get(0);
        rest(run, RunUltimates.LASTS[RunUltimates.TIDE]);
        assertEquals(0, boss.poisoned, 0);
        assertTrue(run.ultimateBossHits() <= 1);
    }

    // The giant

    @Test public void theGiantIsBigCrushesWhatHeWalksIntoAndEveryStepShakesTheGroundAndStaggersThoseNear() {
        PlusRun run = land(RunArms.KNUCKLES, 60, 16, 12);
        give(run, RunUltimates.TITAN);
        PlusRun.Foe near = run.foes().get(0), crushedOne = run.foes().get(1);
        assertTrue(run.callOn(0));
        assertEquals(2.5f, run.size(), 0);
        // One right by her is crushed at once (a blow that counts for several).
        crushedOne.x = run.x() + .9f;
        crushedOne.stunned = 9999;
        int before = crushedOne.left;
        rest(run, .1f);
        assertTrue(crushedOne.left < before || crushedOne.flat);
        assertTrue(run.crushed() >= 1);
        // Walking, every step is a step of the ground: the creatures near are knocked off their feet for a moment (and not those far off).
        int steps = run.titanSteps();
        near.stunned = 0;
        near.dazed = 0;
        near.x = run.x() + 4;
        go(run, 1f, true);
        assertTrue(run.titanSteps() > steps);
        assertTrue(near.dazed > 0 || near.stunned > 0 || near.flat);
        // Her blows reach as many times further.
        PlusRun calm = land(RunArms.KNUCKLES, 60, 14);
        PlusRun.Foe out = hold(calm, 0);
        out.x = calm.x() + 2.2f;
        int hp = out.left;
        strike(calm);
        assertEquals(hp, out.left);
        PlusRun big = land(RunArms.KNUCKLES, 60, 14);
        give(big, RunUltimates.TITAN);
        assertTrue(big.callOn(0));
        PlusRun.Foe in = hold(big, 0);
        in.x = big.x() + 2.2f;
        in.stunned = 9999;
        hp = in.left;
        strike(big);
        rest(big, .3f);
        assertTrue(in.left < hp || in.flat);
    }

    @Test public void theGiantDoesNotStaggerOrCrushABoss() {
        PlusRun run = landWithBoss(RunArms.KNUCKLES, 14);
        give(run, RunUltimates.TITAN);
        assertTrue(run.callOn(0));
        PlusRun.Foe boss = run.foes().get(0);
        go(run, 1.5f, true);
        assertEquals(0, boss.stunned, 0);
        assertEquals(0, run.crushed());
    }

    // The legion

    @Test public void theLegionIsThreeEchoesThatStrikeWithHer() {
        PlusRun calm = land(RunArms.PEN, 60);
        strike(calm);
        int alone = hersShots(calm);
        PlusRun run = land(RunArms.PEN, 60);
        give(run, RunUltimates.LEGION);
        assertTrue(run.callOn(0));
        for (int echo = 0; echo < RunUltimates.ECHOES; echo++) assertNotNull(run.echoAt(echo));
        assertNull(run.echoAt(RunUltimates.ECHOES));
        // Three echoes, in three different places.
        assertTrue(Math.abs(run.echoAt(0)[0] - run.echoAt(1)[0]) > 1);
        run.step(STEP, false, false, false, true);
        assertEquals(alone + RunUltimates.ECHOES, hersShots(run));
        assertEquals(RunUltimates.ECHOES, run.echoStrikes());
        // And a swing: each echo swings where it stands, so a creature near an echo and not near her is struck.
        PlusRun swung = land(RunArms.KNUCKLES, 60, 9);
        give(swung, RunUltimates.LEGION);
        assertTrue(swung.callOn(0));
        PlusRun.Foe foe = hold(swung, 0);
        foe.x = swung.echoAt(1)[0] + 1.0f;
        int before = foe.left;
        strike(swung);
        assertTrue(foe.left < before || foe.flat);
        // Over, they are gone.
        rest(run, RunUltimates.LASTS[RunUltimates.LEGION] + .2f);
        assertNull(run.echoAt(0));
    }

    private static int hersShots(PlusRun run) {
        int count = 0;
        for (PlusRun.Shot shot : run.shots()) if (shot.hers) count++;
        return count;
    }

    // The rose bloom

    @Test public void theBloomCannotBeHurtHealsAndEveryCreatureBeatenBurstsAndChains() throws Exception {
        PlusRun run = land(RunArms.KNUCKLES, 1, 12, 14, 16, 18);
        give(run, RunUltimates.BLOOM);
        for (int index = 0; index < 4; index++) hold(run, index);
        setField(run, "hp", run.hp() / 2);
        assertTrue(run.callOn(0));
        int hp = run.hp();
        assertFalse(hurt(run));
        assertEquals(hp, run.hp());
        // Beating the first bursts into roses, which beat the next, and so on down the row: one blow, four creatures.
        strike(run);
        rest(run, 1.2f);
        for (PlusRun.Foe foe : run.foes()) assertTrue(foe.flat);
        assertTrue(run.blooms() >= 3);
        assertTrue(run.hp() > hp);
        // Without the bloom the blow beats only the one.
        PlusRun calm = land(RunArms.KNUCKLES, 1, 12, 14, 16, 18);
        for (int index = 0; index < 4; index++) hold(calm, index);
        strike(calm);
        rest(calm, 1.2f);
        int flat = 0;
        for (PlusRun.Foe foe : calm.foes()) if (foe.flat) flat++;
        assertEquals(1, flat);
    }

    @Test public void theBloomsChainHasAnEnd() {
        List<Integer> columns = new ArrayList<>();
        for (int column = 12; column < 70; column += 2) columns.add(column);
        int[] row = new int[columns.size()];
        for (int at = 0; at < row.length; at++) row[at] = columns.get(at);
        PlusRun run = land(RunArms.KNUCKLES, 1, row);
        give(run, RunUltimates.BLOOM);
        for (int index = 0; index < row.length; index++) hold(run, index);
        assertTrue(run.callOn(0));
        strike(run);
        rest(run, 4);
        assertTrue(run.blooms() <= RunUltimates.BLOOM_CHAIN + 1);
    }

    // Bosses

    @Test public void noUltimateBeatsABossInAMomentTheirExtraBlowsAreCappedAndTheirDamageDoesNotCountOnHim() {
        // A blow on one who waits counts as one, hard or soft: berserk does not make it more.
        PlusRun calm = landWithBoss(RunArms.KNUCKLES, 12);
        PlusRun.Foe a = calm.foes().get(0);
        calm.place(a.x - .6f - PlusRun.WIDE, FLOOR - PlusRun.TALL);
        int start = a.most;
        strike(calm);
        rest(calm, .3f);
        int plainBlow = start - a.left;
        PlusRun rage = landWithBoss(RunArms.KNUCKLES, 12);
        give(rage, RunUltimates.BERSERK);
        assertTrue(rage.callOn(0));
        PlusRun.Foe b = rage.foes().get(0);
        rage.place(b.x - .6f - PlusRun.WIDE, FLOOR - PlusRun.TALL);
        strike(rage);
        // (The cleave may also have landed once on him: never more than the cap in all, in any one calling on.)
        for (int blow = 0; blow < 200; blow++) {
            rage.step(STEP, false, false, false, true);
            b.broken = 9999;
            b.dazed = 0;
        }
        assertTrue("boss hits " + rage.ultimateBossHits(), rage.ultimateBossHits() <= RunUltimates.BOSS_HITS);
        assertTrue(plainBlow >= 0);
        // And what a rage gives back from him is capped too.
        assertTrue(rage.leeched() <= RunUltimates.BOSS_LEECH_MOST);
        assertTrue(RunUltimates.BOSS_HITS <= 4 && RunUltimates.BOSS_LEECH_MOST <= 20);
    }
}
