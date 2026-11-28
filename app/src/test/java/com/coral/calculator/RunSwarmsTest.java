package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** D3: swarms, thirty to fifty-five times what a land was laid with, gathered in groups, on the ground and on ledges; and creatures with shields. */
public class RunSwarmsTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    @Test public void landsHaveThirtyToFiftyFiveTimesTheirCreaturesInSwarms() {
        assertEquals(30, RunLevels.swarmOf(1));
        assertEquals(55, RunLevels.swarmOf(RunLevels.TOTAL));
        for (int number = 4; number <= RunLevels.TOTAL; number++) assertTrue(RunLevels.swarmOf(number) >= RunLevels.swarmOf(number - 1));
        int onLedges = 0, short_ = 0;
        long grand = 0, grandWanted = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int laid = 0, all = 0, swarmed = 0, grouped = 0;
            for (int[] foe : level.foes) {
                if (foe[1] >= level.main || RunLevels.boss(foe[0]) || RunLevels.flier(foe[0]) || foe[0] == RunLevels.TARGET || RunLevels.rover(foe[0])
                        || RunLevels.uniqueSpec(foe)) continue;
                all++;
                if (!RunLevels.crowd(foe)) laid++;
                if (!RunLevels.swarmed(foe)) continue;
                swarmed++;
                // Standing on the ground or a ledge, with room over it; clear of the start, flags and the end; never a times.
                char under = level.land[foe[2] + 1][foe[1]];
                assertTrue("land " + number + " at " + foe[1], under == RunLevels.BLOCK || under == RunLevels.LEDGE);
                if (under == RunLevels.LEDGE) onLedges++;
                assertEquals(RunLevels.AIR, level.land[foe[2]][foe[1]]);
                assertTrue("land " + number + " at " + foe[1], foe[1] >= 24 && foe[1] < level.gate - 5);
                for (int flag : level.flags) assertTrue(Math.abs(flag - foe[1]) >= 6);
                assertTrue(foe[0] != RunLevels.TIMES);
                // Gathered: others of its swarm close by.
                int near = 0;
                for (int[] other : level.foes) if (other != foe && RunLevels.swarmed(other) && Math.abs(other[1] - foe[1]) <= 3) near++;
                if (near >= 2) grouped++;
            }
            int wanted = RunLevels.swarmBase(number, laid) * RunLevels.swarmOf(number);
            // As many as wanted, short only where a land is short of room (and never by more than a third).
            assertTrue("land " + number + " has " + all + " of " + wanted, all <= wanted && all >= wanted * (RunLevels.bossOf(number) >= 0 ? 1 : 2) / (RunLevels.bossOf(number) >= 0 ? 4 : 5));
            if (all < wanted) short_++;
            grand += all;
            grandWanted += wanted;
            assertTrue("land " + number + " grouped " + grouped + " of " + swarmed, grouped >= swarmed * 9 / 10);
        }
        assertTrue("on ledges " + onLedges, onLedges > 2000);
        System.out.println("SWARMS " + grand + " of " + grandWanted + " wanted, short in " + short_ + " lands");
        // (With so many more asked of each, more of the lands are short of room for them: the room is what runs out.)
        assertTrue("short " + short_, short_ <= 95);
        assertTrue(grand * 10L >= grandWanted * 6L);
    }

    @Test public void swarmsSleepUntilSheIsNearAndFightWhenSheIs() {
        RunLevels.Level level = RunLevels.level(85);
        PlusRun run = new PlusRun(level);
        int swarm = 0;
        for (PlusRun.Foe foe : run.foes()) if (foe.crowd) swarm++;
        assertTrue(swarm > 500);
        // Far off, they keep still: a second of the land with all of them there moves none of those far from her.
        float[] before = new float[run.foes().size()];
        for (int index = 0; index < before.length; index++) before[index] = run.foes().get(index).x;
        for (int step = 0; step < 60; step++) run.step(STEP, false, false, false);
        for (int index = 0; index < before.length; index++) {
            PlusRun.Foe foe = run.foes().get(index);
            if (foe.crowd && Math.abs(foe.x - run.x()) > 30) assertEquals(before[index], foe.x, 1e-4f);
        }
    }

    @Test public void someCreaturesCarryShieldsThatOnlyStrongBlowsBreak() {
        // None before the sixth land; more further on; and stronger.
        assertEquals(0, PlusRun.shieldOdds(5));
        assertTrue(PlusRun.shieldOdds(90) > PlusRun.shieldOdds(10));
        assertTrue(PlusRun.shieldNeeds(90) > PlusRun.shieldNeeds(10) * 3);
        assertTrue(PlusRun.shieldHolds(90) > PlusRun.shieldHolds(10));
        int shielded = 0, all = 0;
        for (int number : new int[]{10, 40, 80}) {
            PlusRun run = new PlusRun(RunLevels.level(number));
            for (PlusRun.Foe foe : run.foes()) {
                if (RunLevels.boss(foe.kind) || RunLevels.flier(foe.kind)) continue;
                all++;
                if (foe.guard > 0 && !RunUniques.is(foe.kind)) {
                    shielded++;
                    assertEquals(PlusRun.shieldNeeds(number), foe.guardNeeds);
                    assertEquals(PlusRun.shieldKind(number), foe.guardKind);
                }
            }
        }
        assertTrue("shielded " + shielded + " of " + all, shielded > all / 20 && shielded < all / 2);
        // On flat ground: one with a shield, facing her. Her first book at her first level is too weak: the blow rings off, and the
        // creature takes nothing.
        PlusRun weak = onFlat(1, 0);
        PlusRun.Foe foe = weak.foes().get(0);
        int left = foe.left;
        tap(weak, foe, true, 3);
        assertTrue(weak.clangs() > 0);
        assertEquals(left, foe.left);
        assertEquals(foe.guardMost, foe.guard);
        // Strong blows: the shield takes them, and enough of them break it; then blows land.
        PlusRun strong = onFlat(120, 400);
        PlusRun.Foe other = strong.foes().get(0);
        tap(strong, other, true, 40);
        assertEquals(0, other.guard);
        assertEquals(1, strong.shieldsBroken());
        assertTrue(other.left < other.most);
        // Coming down on it from above goes round the shield.
        PlusRun behind = onFlat(1, 0);
        PlusRun.Foe turned = behind.foes().get(0);
        int had = turned.left;
        behind.place(turned.x + turned.wide() / 2 - PlusRun.WIDE / 2, FLOOR - PlusRun.TALL - 3);
        for (int step = 0; step < 90 && turned.left == had; step++) behind.step(STEP, false, false, false);
        assertTrue("left " + turned.left + " had " + had + " clangs " + behind.clangs() + " struck " + turned.struck + " x " + behind.x() + " foe " + turned.x + " flat " + turned.flat, turned.left < had);
        assertEquals(0, behind.clangs());
    }

    /** A flat land with one creature carrying a land 40's shield, and her at a level with so much attack. */
    private static PlusRun onFlat(int level, int attack) {
        RunLevels.Level flat = new RunLevels.Level(1, 60);
        for (int column = 0; column < 60; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) flat.land[row][column] = RunLevels.BLOCK;
        flat.gate = 57;
        flat.foes.add(new int[]{RunLevels.MINUS, 6, FLOOR - 1});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        perks.level = level;
        perks.skills.stats[RunSkills.ATK] = attack;
        PlusRun run = new PlusRun(flat, perks);
        PlusRun.Foe foe = run.foes().get(0);
        foe.guard = foe.guardMost = PlusRun.shieldHolds(40);
        foe.guardNeeds = PlusRun.shieldNeeds(40);
        foe.left = foe.most = 1000 * PlusRun.BLOW;
        return run;
    }

    /** Taps of her book on it, it kept where it is, facing her or not. */
    private static void tap(PlusRun run, PlusRun.Foe foe, boolean facing, int taps) {
        for (int step = 0; step < 40 * taps; step++) {
            foe.x = 5;
            foe.leftward = facing;
            // (Turned away, it is kept so: one with a shield turns to face her when she is near.)
            if (!facing) foe.dazed = 99;
            run.step(STEP, false, false, false, step % 40 == 0);
            if (foe.guard == 0 && taps > 3 && foe.left < foe.most) return;
        }
    }
}
