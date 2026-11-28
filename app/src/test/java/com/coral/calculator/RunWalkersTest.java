package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Creatures that walk are always on something: they are never laid over a gap or water, and one knocked or pushed off an edge falls. */
public class RunWalkersTest {

    /** Whether what it stands on is under it: a solid square, a ledge or a thin one, under some part of it. */
    static boolean held(PlusRun run, PlusRun.Foe foe) {
        int row = (int) Math.floor(foe.ground + foe.tall() + .05f);
        for (int column = (int) Math.floor(foe.x + .12f); column <= (int) Math.floor(foe.x + foe.wide() - .12f); column++) {
            char under = run.at(column, row);
            if (PlusRun.solid(under) || under == RunLevels.THIN || under == RunLevels.LEDGE) return true;
        }
        return false;
    }

    static String unheld(PlusRun run, String where) {
        StringBuilder out = new StringBuilder();
        for (PlusRun.Foe foe : run.foes()) {
            if (foe.flat || !PlusRun.walks(foe.kind) || held(run, foe)) continue;
            out.append("\n").append(where).append(" kind ").append(foe.kind).append(RunUniques.is(foe.kind) ? " (" + RunUniques.WAY_NAMES[RunUniques.way(foe.kind)] + ")" : "")
                    .append(foe.swarm ? " swarm" : "").append(" at ").append(foe.x).append(",").append(foe.ground + foe.tall());
        }
        return out.toString();
    }

    @Test public void noWalkerIsLaidOverAGapOrWater() {
        StringBuilder out = new StringBuilder();
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            PlusRun run = new PlusRun(RunLevels.level(number));
            run.step(.02f, false, false, false);
            out.append(unheld(run, "land " + number));
            if (run.foesFell() > 0) out.append("\nland ").append(number).append(": ").append(run.foesFell()).append(" laid over nothing");
        }
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon++) {
            for (int floor = 0; floor < RunDungeons.floors(dungeon); floor += 3) {
                PlusRun run = new PlusRun(RunLevels.dungeon(dungeon, floor, 31 + dungeon));
                run.step(.02f, false, false, false);
                out.append(unheld(run, "dungeon " + dungeon + "/" + floor));
            }
        }
        assertEquals("", out.toString());
    }

    /** A plain bot that runs on, jumping now and then and striking all the time, through every land: no walker is ever without ground under it for more than a moment. */
    @Test public void noWalkerStaysInTheAirWhileSheFightsThroughEveryLand() {
        StringBuilder out = new StringBuilder();
        int fell = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number += 2) {
            PlusRun run = new PlusRun(RunLevels.level(number));
            java.util.Map<PlusRun.Foe, Float> since = new java.util.HashMap<>();
            for (int step = 0; step < 60 * 40 && run.state() == PlusRun.PLAYING; step++) {
                boolean back = step % 600 > 420;
                run.step(1 / 60f, back, !back, step % 50 < 14, step % 6 == 0);
                for (PlusRun.Foe foe : run.foes()) {
                    if (foe.flat || !PlusRun.walks(foe.kind) || held(run, foe)) {
                        since.remove(foe);
                        continue;
                    }
                    float gone = since.getOrDefault(foe, 0f) + 1 / 60f;
                    since.put(foe, gone);
                    if (gone > .5f && gone < .52f) out.append(unheld(run, "land " + number + " at " + step).split("\n", 3).length > 1 ? "\nland " + number + " step " + step + " kind " + foe.kind + (RunUniques.is(foe.kind) ? " (" + RunUniques.WAY_NAMES[RunUniques.way(foe.kind)] + ")" : "") + (foe.swarm ? " swarm" : "") + " at " + foe.x + "," + (foe.ground + foe.tall()) + " home " + foe.home : "");
                }
            }
            fell += run.foesFell();
        }
        System.out.println("WALKERS fell " + fell);
        assertEquals("", out.toString());
    }

    private static final int FLOOR = 10;
    private static final float STEP = 1 / 60f;

    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(40, columns);
        for (int c = 0; c < columns; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = columns - 3;
        return level;
    }

    private static PlusRun.Foe first(PlusRun run, int kind) {
        for (PlusRun.Foe foe : run.foes()) if (foe.kind == kind) return foe;
        throw new AssertionError("none of " + kind);
    }

    /**
     * What was seen in a land of the ninth level: a mini-boss that had leapt up onto her ledge over a pond came down when she left it
     * and stopped at the height of its own floor, standing on the air over the water. Now it leaps back down onto its floor.
     */
    @Test public void aMiniBossThatLeaptOntoALedgeOverWaterComesBackDownOntoGround() {
        int kind = -1;
        for (int one = RunUniques.FIRST; one < RunUniques.FIRST + RunUniques.COUNT && kind < 0; one++) if (RunUniques.way(one) == RunUniques.MINIBOSS) kind = one;
        RunLevels.Level level = flat(60);
        for (int c = 13; c <= 28; c++) for (int r = FLOOR; r < FLOOR + 3; r++) level.land[r][c] = RunLevels.WATER;
        for (int c = 16; c <= 25; c++) level.land[FLOOR - 4][c] = RunLevels.LEDGE;
        level.foes.add(new int[]{kind, 32, FLOOR - 1, 1, 0, 2});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 60;
        PlusRun run = new PlusRun(level, perks);
        PlusRun.Foe boss = first(run, kind);
        run.place(20, FLOOR - 4 - PlusRun.TALL - .02f);
        for (float gone = 0; gone < 10 && boss.ground > FLOOR - 4 - boss.tall() + .1f; gone += STEP) run.step(STEP, false, false, false);
        assertTrue("it came up onto her ledge", boss.ground < FLOOR - 3 - boss.tall());
        // She goes, down onto the floor past the pond; it comes down.
        run.place(33, FLOOR - PlusRun.TALL - .02f);
        float unheld = 0;
        for (float gone = 0; gone < 8; gone += STEP) {
            run.step(STEP, false, false, false);
            unheld = boss.leaping == 0 && !run.held(boss) ? unheld + STEP : 0;
            assertTrue("in the air at " + boss.x + "," + (boss.ground + boss.tall()), unheld < .3f);
        }
        assertEquals(FLOOR, boss.ground + boss.tall(), .05f);
        assertTrue(run.held(boss));
    }

    /** A creature struck again and again at the end of its ledge is knocked back only as far as there is ground. */
    @Test public void blowsNeverKnockAWalkerOffItsLedge() {
        RunLevels.Level level = flat(40);
        for (int c = 8; c <= 30; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.AIR;
        for (int c = 8; c <= 14; c++) level.land[FLOOR][c] = RunLevels.BLOCK;
        level.foes.add(new int[]{RunLevels.TIMES, 13, FLOOR - 1, 40});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 60;
        PlusRun run = new PlusRun(level, perks);
        PlusRun.Foe foe = first(run, RunLevels.TIMES);
        for (int blow = 0; blow < 40 && !foe.flat; blow++) {
            run.place(foe.x - PlusRun.WIDE - .2f, FLOOR - PlusRun.TALL - .02f);
            for (int step = 0; step < 12; step++) run.step(STEP, false, false, false, step == 0);
            if (!foe.flat) assertTrue("knocked off at " + foe.x, run.held(foe));
        }
    }

    /** One whose ground goes from under it falls and lands on what is below; one that falls into water is gone, and gives nothing. */
    @Test public void aWalkerWithNothingUnderItFallsAndOneThatFallsIntoWaterIsGone() {
        RunLevels.Level level = flat(40);
        for (int c = 20; c <= 24; c++) level.land[FLOOR - 4][c] = RunLevels.BLOCK;
        level.foes.add(new int[]{RunLevels.MINUS, 22, FLOOR - 5});
        level.foes.add(new int[]{RunLevels.MINUS, 32, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        run.place(2, FLOOR - PlusRun.TALL - .02f);
        run.step(STEP, false, false, false);
        PlusRun.Foe high = run.foes().get(0), low = run.foes().get(1);
        assertTrue(run.held(high));
        // Its ledge is taken away: down it comes, onto the floor.
        for (int c = 20; c <= 24; c++) run.level().land[FLOOR - 4][c] = RunLevels.AIR;
        for (int step = 0; step < 90; step++) run.step(STEP, false, false, false);
        assertFalse(high.flat);
        assertEquals(FLOOR, high.ground + high.tall(), .05f);
        assertTrue(run.foesFell() >= 1);
        // The floor under the other one is water: it sinks.
        int xp = run.xp();
        for (int c = 26; c < 40; c++) run.level().land[FLOOR][c] = RunLevels.WATER;
        for (int step = 0; step < 90; step++) run.step(STEP, false, false, false);
        assertTrue(low.flat);
        assertEquals(1, run.foesSank());
        assertEquals(xp, run.xp());
    }

    /** Each blow that lands is seen in the way of what dealt it, and the last one on a creature, and a shield breaking, are marked to be seen bigger. */
    @Test public void blowsAreSeenByWhatDealtThemAndTheLastIsMarked() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.SWORD;
        RunLevels.Level level = flat(30);
        level.foes.add(new int[]{RunLevels.TIMES, 6, FLOOR - 1, 3});
        PlusRun run = new PlusRun(level, perks);
        PlusRun.Foe foe = first(run, RunLevels.TIMES);
        int blows = 0;
        float[] last = null;
        for (int step = 0; step < 60 * 8 && !foe.flat; step++) {
            run.place(foe.x - PlusRun.WIDE - .3f, FLOOR - PlusRun.TALL - .02f);
            run.step(STEP, false, false, false, step % 20 == 0);
            if (!run.hits().isEmpty()) last = run.hits().get(run.hits().size() - 1);
        }
        assertTrue(foe.flat);
        assertEquals(PlusRun.CUTS, (int) last[7]);
        assertEquals(1, last[8], 0);
    }
}