package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;

/** What the redrawn tree's notables do in a land, each by itself: every new mechanic, and that a class's own are felt only with its weapons. */
public class RunTreeMechanicsTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    /** A flat land, minus signs at these columns, so many blows each, and her with something in her hands at column 10. */
    private static PlusRun land(int weapon, int blows, int... columns) {
        RunLevels.Level level = new RunLevels.Level(10, 60);
        for (int c = 0; c < 60; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 57;
        for (int column : columns) level.foes.add(new int[]{RunLevels.MINUS, column, FLOOR - 1, blows});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 20;
        PlusRun run = new PlusRun(level, perks);
        // (Nothing goes after her: what is tried here is her blows, on creatures that stand where they were put.)
        run.calm = true;
        if (weapon != PlusRun.UNARMED) run.arm(weapon);
        run.place(10.6f, FLOOR - PlusRun.TALL);
        return run;
    }

    /** Her tree as so much of each effect, felt whatever she holds. */
    private static RunSkills.Built tree(int... pairs) {
        RunSkills.Built built = new RunSkills.Built();
        for (int at = 0; at < pairs.length; at += 2) built.effects[pairs[at]] += pairs[at + 1];
        return built;
    }

    private static void learn(PlusRun run, RunSkills.Built built) { run.learn(built, new ArrayList<>()); }

    private static void tap(PlusRun run) {
        run.step(STEP, false, false, false, true);
        for (int step = 0; step < 16; step++) run.step(STEP, false, false, false, false);
    }

    private static void rest(PlusRun run, float seconds) {
        for (int step = 0; step < seconds * 60; step++) run.step(STEP, false, false, false, false);
    }

    private static int hers(PlusRun run) {
        int count = 0;
        for (PlusRun.Shot shot : run.shots()) if (shot.hers) count++;
        return count;
    }

    /** Holds a creature nearly still (knocked off its feet for good), so that it does not walk into her while a test waits. */
    private static PlusRun.Foe held(PlusRun run, int index) {
        PlusRun.Foe foe = run.foes().get(index);
        foe.stunned = 9999;
        return foe;
    }

    /** How much one punch takes off a creature (nearly beaten, or not yet hurt). */
    private static int punch(RunSkills.Built built, boolean low) {
        PlusRun run = land(RunArms.KNUCKLES, 30, 12);
        learn(run, built);
        PlusRun.Foe foe = held(run, 0);
        if (low) foe.left = foe.most / 4;
        int before = foe.left;
        rest(run, .2f);
        run.step(STEP, false, false, false, true);
        return before - foe.left;
    }

    @Test public void moreShotsInAFanAndOnlyWithTheClassWhoseNotableItIs() {
        PlusRun run = land(RunArms.PEN, 30);
        learn(run, tree());
        tap(run);
        int alone = hers(run);
        assertEquals(1, alone);
        run = land(RunArms.PEN, 30);
        RunSkills.Built pens = new RunSkills.Built();
        pens.add(RunSkills.E_PROJECTILES, 2, 1 << 6);
        learn(run, pens);
        run.step(STEP, false, false, false, true);
        assertEquals(3, hers(run));
        assertEquals(2, run.fanned());
        // Spread out: not all on one line.
        float lowest = 99, highest = -99;
        for (int step = 0; step < 10; step++) run.step(STEP, false, false, false, false);
        for (PlusRun.Shot shot : run.shots()) if (shot.hers) {
            lowest = Math.min(lowest, shot.y);
            highest = Math.max(highest, shot.y);
        }
        assertTrue(highest - lowest > .2f);
        // A knife is not a pen: the pens' notable does nothing for it.
        run = land(RunArms.KNIFE, 30);
        learn(run, pens);
        run.step(STEP, false, false, false, true);
        assertEquals(0, run.fanned());
    }

    @Test public void piercingAndJumpingOn() {
        PlusRun run = land(RunArms.PEN, 30);
        learn(run, tree(RunSkills.E_PIERCE, 2));
        run.step(STEP, false, false, false, true);
        int pierce = -1;
        for (PlusRun.Shot shot : run.shots()) if (shot.hers) pierce = shot.pierce;
        PlusRun plain = land(RunArms.PEN, 30);
        plain.step(STEP, false, false, false, true);
        for (PlusRun.Shot shot : plain.shots()) if (shot.hers) assertEquals(shot.pierce + 2, pierce);
        // A shot that jumps on: it hits one, and goes on to the next one near.
        run = land(RunArms.PEN, 30, 14, 17);
        learn(run, tree(RunSkills.E_CHAIN, 1));
        run.step(STEP, false, false, false, true);
        rest(run, .6f);
        assertEquals(1, run.hopped());
        assertTrue(run.foes().get(1).left < run.foes().get(1).most);
    }

    @Test public void aShotBouncesOffAWall() {
        PlusRun run = land(RunArms.GUN, 30);
        // A wall a few squares ahead of her, as high as she is.
        RunLevels.Level level = new RunLevels.Level(10, 60);
        for (int c = 0; c < 60; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        for (int r = FLOOR - 4; r < FLOOR; r++) level.land[r][15] = RunLevels.BLOCK;
        level.gate = 57;
        run = new PlusRun(level, new PlusRun.Perks());
        run.arm(RunArms.GUN);
        run.place(10.6f, FLOOR - PlusRun.TALL);
        RunSkills.Built guns = new RunSkills.Built();
        guns.add(RunSkills.E_RICOCHET, 1, 1 << 10);
        learn(run, guns);
        run.step(STEP, false, false, false, true);
        boolean back = false;
        for (int step = 0; step < 40; step++) {
            run.step(STEP, false, false, false, false);
            for (PlusRun.Shot shot : run.shots()) back |= shot.hers && shot.across < 0;
        }
        assertEquals(1, run.bounced());
        assertTrue(back);
    }

    @Test public void aSwingStrikesAgainABeatLater() {
        PlusRun run = land(RunArms.KNUCKLES, 30, 12);
        learn(run, tree(RunSkills.E_ECHO, 100));
        PlusRun.Foe foe = run.foes().get(0);
        int before = foe.left;
        run.step(STEP, false, false, false, true);
        int once = before - foe.left;
        rest(run, .5f);
        assertEquals(1, run.echoes());
        assertTrue(before - foe.left >= once * 2 - 1);
    }

    @Test public void harderOnTheNearlyBeatenTheUnhurtAndAfterARest() {
        assertTrue(punch(tree(RunSkills.E_EXECUTE, 100), true) > punch(tree(), true) * 3 / 2);
        assertEquals(punch(tree(), false), punch(tree(RunSkills.E_EXECUTE, 100), false));
        assertTrue(punch(tree(RunSkills.E_AMBUSH, 100), false) > punch(tree(), false) * 3 / 2);
        // A charge: a punch after a rest of a second and a half; not one just after another.
        PlusRun run = land(RunArms.KNUCKLES, 30, 12);
        learn(run, tree(RunSkills.E_CHARGE, 100));
        PlusRun.Foe foe = held(run, 0);
        rest(run, 2);
        int before = foe.left;
        run.step(STEP, false, false, false, true);
        int charged = before - foe.left;
        rest(run, .3f);
        before = foe.left;
        run.step(STEP, false, false, false, true);
        assertTrue(charged > (before - foe.left) * 3 / 2);
        // At a third of her HP or less, a last stand.
        PlusRun low = land(RunArms.KNUCKLES, 30, 12);
        learn(low, tree(RunSkills.E_LAST_STAND, 100));
        int full = punch(tree(RunSkills.E_LAST_STAND, 100), false);
        low.wound();
        while (low.hp() * 3 > low.maxHp()) low.wound();
        foe = held(low, 0);
        before = foe.left;
        rest(low, .2f);
        low.step(STEP, false, false, false, true);
        assertTrue(before - foe.left > full * 3 / 2);
    }

    @Test public void shieldbreakingTakesTwoPiecesAtItsStrongest() {
        // A shield any punch tells on: a piece a punch; with the tree at its strongest, two.
        assertEquals(1, piecesTaken(tree(), 1));
        assertEquals(2, piecesTaken(tree(RunSkills.E_SHIELD_BREAK, 100), 1));
        // One that asks for half as much again as a punch: it rings off; with sixty percent more against shields, a piece is taken.
        int needs = punch(tree(), false) * 3 / 2;
        assertEquals(0, piecesTaken(tree(), needs));
        assertEquals(1, piecesTaken(tree(RunSkills.E_SHIELD_BREAK, 60), needs));
    }

    /** How many pieces of a creature's shield one punch takes, with her tree as it is and the shield asking so much of a blow. */
    private static int piecesTaken(RunSkills.Built built, int needs) {
        PlusRun run = land(RunArms.KNUCKLES, 30, 12);
        learn(run, built);
        PlusRun.Foe foe = held(run, 0);
        foe.guard = foe.guardMost = 9;
        foe.guardNeeds = needs;
        foe.leftward = true;
        tap(run);
        return 9 - foe.guard;
    }
    @Test public void knockedOffItsFeetItCrawlsAndCanStillBeHit() {
        PlusRun run = land(RunArms.KNUCKLES, 30, 12);
        learn(run, tree(RunSkills.E_STUN, 100));
        PlusRun.Foe foe = run.foes().get(0);
        PlusRun plain = land(RunArms.KNUCKLES, 30, 12);
        run.step(STEP, false, false, false, true);
        plain.step(STEP, false, false, false, true);
        assertTrue(foe.stunned > 0);
        assertEquals(1, run.stuns());
        float x = foe.x, walked = plain.foes().get(0).x;
        rest(run, .3f);
        rest(plain, .3f);
        assertTrue(Math.abs(foe.x - x) < Math.abs(plain.foes().get(0).x - walked) / 3);
        // (Knocked back a little by the first, it is stepped up to again.)
        run.place(foe.x - 1.38f, FLOOR - PlusRun.TALL);
        int left = foe.left;
        run.step(STEP, false, false, false, true);
        assertTrue(foe.left < left);
    }

    @Test public void poolsOfInkAndShockwaves() {
        PlusRun run = land(RunArms.KNUCKLES, 30, 12, 13);
        learn(run, tree(RunSkills.E_POOL, 100));
        run.step(STEP, false, false, false, true);
        assertTrue(run.pools() >= 1);
        assertTrue(run.mired(run.foes().get(0)));
        // A shockwave: one blow, and the one beside it is hit too.
        run = land(RunArms.KNUCKLES, 30, 12, 14);
        learn(run, tree(RunSkills.E_QUAKE, 100));
        PlusRun.Foe beside = run.foes().get(1);
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.shockwaves());
        assertTrue(beside.left < beside.most);
    }

    @Test public void beatingACreatureGivesBackAndBringsOnAFrenzy() {
        PlusRun run = land(RunArms.KNUCKLES, 1, 12);
        learn(run, tree(RunSkills.E_KILL_HEAL, 7, RunSkills.E_KILL_ENERGY, 20, RunSkills.E_FRENZY, 30));
        run.wound();
        int hp = run.hp();
        rest(run, .1f);
        float energy = run.energy();
        run.step(STEP, false, false, false, true);
        assertTrue(run.foes().get(0).flat);
        assertEquals(1, run.treeKills());
        assertEquals(hp + 7, run.hp());
        assertTrue(run.energy() >= Math.min(PlusRun.ENERGY_MOST, energy + 20) - .5f);
        assertTrue(run.frenzyLeft() > 0);
    }

    @Test public void theComboMeterGrowsAndRunsOut() {
        PlusRun run = land(RunArms.KNUCKLES, 60, 12);
        learn(run, tree(RunSkills.E_METER, 5));
        // (Two jabs: the third punch is an uppercut, which leaves it reeling out of reach of the next.)
        tap(run);
        tap(run);
        assertEquals(2, run.meter());
        rest(run, 2.2f);
        assertEquals(0, run.meter());
    }

    @Test public void howSheMoves() {
        PlusRun plain = land(PlusRun.UNARMED, 1), fast = land(PlusRun.UNARMED, 1);
        learn(fast, tree(RunSkills.E_SPEED, 50));
        for (int step = 0; step < 60; step++) {
            plain.step(STEP, false, true, false);
            fast.step(STEP, false, true, false);
        }
        assertTrue(fast.x() - 10.6f > (plain.x() - 10.6f) * 1.3f);
        learn(plain, tree(RunSkills.E_SWIM, 50));
        assertEquals(PlusRun.HOLD_BREATH * 1.5f, plain.holdBreath(), .01f);
        // Forms last longer.
        PlusRun forms = land(PlusRun.UNARMED, 1), bare = land(PlusRun.UNARMED, 1);
        learn(forms, tree(RunSkills.E_FORMS, 50));
        forms.lend(PlusRun.RED);
        bare.lend(PlusRun.RED);
        assertEquals(bare.on(PlusRun.RED) * 1.5f, forms.on(PlusRun.RED), .01f);
    }

    @Test public void aSecondWindFromTheTree() {
        PlusRun run = land(PlusRun.UNARMED, 30, 12);
        run.learn(tree(RunSkills.E_SECOND_WIND, 20), new ArrayList<>());
        while (run.hp() > 15) run.wound();
        int lives = run.lives();
        for (int step = 0; step < 90 && run.secondWinds() == 0; step++) run.step(STEP, false, true, false);
        assertEquals(1, run.secondWinds());
        assertEquals(20, run.hp());
        assertEquals(lives, run.lives());
    }

    @Test public void bloodInkSpendsHpAndIronReflexesMakeDefence() {
        PlusRun run = land(PlusRun.UNARMED, 30);
        RunSkills.Notable active = RunSkills.NOTABLES.get(RunSkills.CORE_ACTIVES);
        run.learn(tree(RunSkills.E_BLOOD, 1), new ArrayList<>(java.util.Arrays.asList(active, null, null)));
        int hp = run.hp();
        float energy = run.energy();
        assertTrue(run.callOn(0));
        assertEquals(hp - PlusRun.bloodOf(PlusRun.ACTIVE_COST), run.hp());
        assertEquals(energy, run.energy(), .01f);
        assertEquals(1, run.bled());
        // Never her last ten.
        while (run.hp() > RunSkills.BLOOD_KEEPS + 5) run.wound();
        rest(run, 21);
        assertFalse(run.canSpend(PlusRun.ACTIVE_COST));
        // Iron reflexes: no hurt slipped, and as much defence.
        PlusRun iron = land(PlusRun.UNARMED, 30);
        learn(iron, tree(RunSkills.E_DODGE, 20, RunSkills.E_IRON, 1));
        assertEquals(0, iron.gear(RunLoot.DODGE));
        assertEquals(20, iron.defencePercent());
        PlusRun quick = land(PlusRun.UNARMED, 30);
        learn(quick, tree(RunSkills.E_DODGE, 20));
        assertEquals(20, quick.gear(RunLoot.DODGE));
        assertEquals(0, quick.defencePercent());
    }

    @Test public void theTreeAddsToWhatGearDoes() {
        PlusRun run = land(PlusRun.UNARMED, 30);
        learn(run, tree(RunSkills.E_MAGNET, 2, RunSkills.E_PLUSES, 20, RunSkills.E_AIR, 1, RunSkills.E_SHRUG, 8, RunSkills.E_SPARK, 2, RunSkills.E_SPECIAL, 20,
                RunSkills.E_WARD_HIT, 6, RunSkills.E_HASTE, 500));
        assertEquals(20, run.gear(RunLoot.MAGNET));
        assertEquals(20, run.gear(RunLoot.PLUSES));
        assertEquals(1, run.gear(RunLoot.AIR));
        assertEquals(8, run.gear(RunLoot.SHRUG));
        assertEquals(2, run.gear(RunLoot.SPARK));
        assertEquals(20, run.gear(RunLoot.SPECIAL));
        assertEquals(6, run.gear(RunLoot.WARD_HIT));
        // (However much haste, never a use with no rest at all.)
        assertEquals(75, run.gear(RunLoot.HASTE));
        // Pets act sooner, with more of it, and landing on a creature counts for more.
        PlusRun stomp = land(PlusRun.UNARMED, 30, 12), soft = land(PlusRun.UNARMED, 30, 12);
        learn(stomp, tree(RunSkills.E_STOMP, 3));
        for (PlusRun one : new PlusRun[]{stomp, soft}) {
            PlusRun.Foe foe = one.foes().get(0);
            one.place(foe.x + foe.wide() / 2 - PlusRun.WIDE / 2, FLOOR - PlusRun.TALL - 3);
            for (int step = 0; step < 40 && foe.left == foe.most; step++) one.step(STEP, false, false, false);
        }
        int hard = stomp.foes().get(0).most - stomp.foes().get(0).left, light = soft.foes().get(0).most - soft.foes().get(0).left;
        assertTrue(hard + " " + light, hard > light * 2);
    }
}
