package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlusRunTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;
    private static final float GROUND = FLOOR - PlusRun.TALL;

    /** A land of flat ground, to put things on: the gate three columns from its end. */
    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(1, columns);
        for (int column = 0; column < columns; column++) {
            for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        }
        level.gate = columns - 3;
        return level;
    }

    private static void hole(RunLevels.Level level, int from, int to) {
        for (int column = from; column <= to; column++) {
            for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.AIR;
        }
    }

    private static void hold(PlusRun run, float seconds, boolean left, boolean right, boolean jump) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, left, right, jump);
    }

    // The rules

    @Test public void sheStandsOnTheGroundWhereSheStarts() {
        PlusRun run = new PlusRun(flat(40));
        hold(run, 1, false, false, false);
        assertTrue(run.onGround());
        assertEquals(GROUND, run.y(), .01f);
        assertEquals(2, run.x(), .01f);
        assertEquals(PlusRun.HEARTS, run.hearts());
    }

    @Test public void sheRunsTheWayTheArrowPoints() {
        PlusRun run = new PlusRun(flat(40));
        hold(run, 1, false, true, false);
        assertTrue(run.x() > 6);
        assertFalse(run.facingLeft());
        hold(run, .5f, true, false, false);
        assertTrue(run.facingLeft());
    }

    @Test public void aWallStopsHer() {
        RunLevels.Level level = flat(40);
        level.land[FLOOR - 1][12] = RunLevels.BLOCK;
        level.land[FLOOR - 2][12] = RunLevels.BLOCK;
        PlusRun run = new PlusRun(level);
        hold(run, 4, false, true, false);
        assertTrue(run.x() <= 12 - PlusRun.WIDE);
        assertTrue(run.x() > 11);
    }

    @Test public void aJumpGoesUpAndComesDownAndLettingGoEarlyJumpsLower() {
        PlusRun run = new PlusRun(flat(40));
        hold(run, .2f, false, false, false);
        float highest = run.y();
        for (int step = 0; step < 30; step++) {
            run.step(STEP, false, false, true);
            highest = Math.min(highest, run.y());
        }
        assertTrue("rose " + (GROUND - highest), GROUND - highest > 2.4f && GROUND - highest < 3.2f);
        hold(run, 1, false, false, false);
        assertTrue(run.onGround());
        run.step(STEP, false, false, true);
        highest = run.y();
        for (int step = 0; step < 40; step++) {
            run.step(STEP, false, false, false);
            highest = Math.min(highest, run.y());
        }
        assertTrue(GROUND - highest < 1.5f);
    }

    @Test public void aPlusIsCollectedByTouchingIt() {
        RunLevels.Level level = flat(40);
        level.pluses.add(new int[]{5, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, 1, false, true, false);
        assertEquals(1, run.collected());
        assertEquals(1, run.total());
        assertTrue(run.pluses().isEmpty());
    }

    @Test public void fallingIntoAGapCostsAHeartAndAFlagIsWhereSheComesBackTo() {
        RunLevels.Level level = flat(60);
        hole(level, 16, 18);
        hole(level, 40, 42);
        level.flags.add(30);
        PlusRun run = new PlusRun(level);
        run.place(17, 6);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
        assertEquals(2, run.x(), .01f);
        run.place(31, GROUND);
        hold(run, .2f, false, false, false);
        assertEquals(1, run.flagsPassed());
        run.place(41, 6);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.HEARTS - 2, run.hearts());
        assertEquals(30, (int) run.x());
    }

    @Test public void landingOnAMinusFlattensItAndWalkingIntoOneCostsAHeart() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        run.place(19.9f, FLOOR - 6);
        hold(run, 1, false, false, false);
        assertEquals(1, run.flattened());
        assertEquals(PlusRun.HEARTS, run.hearts());

        PlusRun other = new PlusRun(level);
        other.place(17, GROUND);
        for (int step = 0; step < 180 && other.hurts() == 0; step++) other.step(STEP, false, true, false);
        assertEquals(1, other.hurts());
        assertEquals(0, other.flattened());
    }

    @Test public void aTimesCannotBeLandedOn() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.TIMES, 20, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        run.place(19.9f, FLOOR - 6);
        hold(run, 1, false, false, false);
        assertEquals(0, run.flattened());
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
    }

    @Test public void aDivideHopsOnTheSpotAndCanBeLandedOn() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.DIVIDE, 20, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        float highest = -Float.MAX_VALUE, across = run.foes().get(0).x;
        for (int step = 0; step < 240; step++) {
            run.step(STEP, false, false, false);
            highest = Math.max(highest, FLOOR - run.foes().get(0).y);
        }
        assertEquals(across, run.foes().get(0).x, .001f);
        assertTrue("hopped " + highest, highest > 2 && highest < 3.2f);
        run.place(19.9f, FLOOR - 9);
        hold(run, 1.5f, false, false, false);
        assertEquals(1, run.flattened());
    }

    @Test public void spikesCostAHeartAndASpringThrowsHerHigh() {
        RunLevels.Level level = flat(60);
        level.land[FLOOR - 1][10] = RunLevels.SPIKE;
        level.land[FLOOR - 1][30] = RunLevels.SPRING;
        PlusRun run = new PlusRun(level);
        run.place(7, GROUND);
        for (int step = 0; step < 180 && run.hurts() == 0; step++) run.step(STEP, false, true, false);
        assertEquals(1, run.hurts());

        PlusRun other = new PlusRun(level);
        other.place(28.5f, GROUND);
        float highest = other.y();
        for (int step = 0; step < 120; step++) {
            other.step(STEP, false, true, false);
            highest = Math.min(highest, other.y());
        }
        assertEquals(1, other.springs());
        assertTrue("rose " + (GROUND - highest), GROUND - highest > 5);
    }

    @Test public void threeHurtsEndTheRun() {
        RunLevels.Level level = flat(60);
        hole(level, 16, 18);
        PlusRun run = new PlusRun(level);
        for (int fall = 0; fall < PlusRun.HEARTS; fall++) {
            run.place(17, 6);
            hold(run, 2, false, false, false);
        }
        assertEquals(PlusRun.LOST, run.state());
    }

    // What she fights with, and what is thrown at her

    /** Flat ground with something to fight with lying two columns from where she starts. */
    private static RunLevels.Level armed(int kind) {
        RunLevels.Level level = flat(60);
        level.weapons.add(new int[]{kind, 4, FLOOR - 1});
        return level;
    }

    /** Stands her where she is, using what she carries whenever something thrown at her is close in front. */
    private static void fend(PlusRun run, float seconds) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) {
            boolean close = false;
            for (PlusRun.Shot shot : run.shots()) {
                if (!shot.hers && shot.x > run.x() && shot.x - run.x() - PlusRun.WIDE < 1.1f) close = true;
            }
            run.step(STEP, false, false, false, close);
        }
    }

    @Test public void aPenIsPickedUpAndThrownAndDealsWithATimes() {
        RunLevels.Level level = armed(PlusRun.PEN);
        level.foes.add(new int[]{RunLevels.TIMES, 12, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        assertEquals(PlusRun.UNARMED, run.weapon());
        run.step(STEP, false, false, false, true);
        assertEquals(0, run.strikes());
        hold(run, .5f, false, true, false);
        assertEquals(PlusRun.PEN, run.weapon());
        assertEquals(1, run.picked());
        assertTrue(run.arms().isEmpty());
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.strikes());
        assertEquals(1, run.shots().size());
        // Holding the button does not throw another: it takes a new press, and a rest between.
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.strikes());
        hold(run, 1, false, false, false);
        assertEquals(1, run.flattened());
        assertTrue(run.shots().isEmpty());
        assertEquals(PlusRun.HEARTS, run.hearts());
    }

    @Test public void aKnifeThrownAtHerCostsAHeartUnlessSheJumpsIt() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.THROWER, 10, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        run.place(6, GROUND);
        float across = run.foes().get(0).x;
        for (int step = 0; step < 240 && run.hurts() == 0; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.hurts());
        assertTrue(run.thrown() >= 1);
        // It stands where it is.
        assertEquals(across, run.foes().get(0).x, .001f);

        PlusRun other = new PlusRun(level);
        other.place(6, GROUND);
        for (int step = 0; step < 400; step++) {
            boolean close = false;
            for (PlusRun.Shot shot : other.shots()) if (shot.x - other.x() - PlusRun.WIDE < .9f) close = true;
            other.step(STEP, false, false, close);
        }
        assertTrue(other.thrown() >= 2);
        assertEquals(0, other.hurts());
    }

    @Test public void aSwordSendsAKnifeBackAndABrushWipesItAway() {
        RunLevels.Level level = armed(PlusRun.SWORD);
        level.foes.add(new int[]{RunLevels.THROWER, 10, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .5f, false, true, false);
        run.place(6, GROUND);
        fend(run, 4);
        assertTrue(run.turned() >= 1);
        assertEquals(1, run.flattened());
        assertEquals(PlusRun.HEARTS, run.hearts());

        level = armed(PlusRun.BRUSH);
        level.foes.add(new int[]{RunLevels.THROWER, 10, FLOOR - 1});
        PlusRun other = new PlusRun(level);
        hold(other, .5f, false, true, false);
        other.place(6, GROUND);
        fend(other, 6);
        assertTrue(other.turned() >= 2);
        assertEquals(0, other.flattened());
        assertEquals(PlusRun.HEARTS, other.hearts());
    }

    @Test public void aSwingDealsWithWhatIsInFrontOfHerAndABookGoesThroughMoreThanOne() {
        RunLevels.Level level = armed(PlusRun.SWORD);
        level.foes.add(new int[]{RunLevels.TIMES, 9, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .5f, false, true, false);
        // No further than the blade is drawn to reach: something a little beyond it is not touched.
        run.place(run.foes().get(0).x - 1.2f - PlusRun.WIDE, GROUND);
        run.step(STEP, false, false, false, true);
        assertTrue(run.swinging() >= 0);
        assertEquals(0, run.flattened());
        hold(run, .4f, false, false, false);
        run.place(run.foes().get(0).x - .7f - PlusRun.WIDE, GROUND);
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.flattened());

        level = armed(PlusRun.BOOK);
        // A book is lobbed: it goes over what is close and comes down some six columns on, on two that stand together.
        level.foes.add(new int[]{RunLevels.MINUS, 7, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.THROWER, 11, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.THROWER, 11, FLOOR - 1});
        level.land[FLOOR - 1][6] = RunLevels.BLOCK;
        level.land[FLOOR - 1][9] = RunLevels.BLOCK;
        PlusRun other = new PlusRun(level);
        hold(other, .4f, false, true, false);
        other.place(5, GROUND);
        other.step(STEP, false, false, false, true);
        hold(other, 1, false, false, false);
        assertEquals(2, other.flattened());
        assertFalse(other.foes().get(0).flat);
    }

    @Test public void anInkGunFiresWhileItsButtonIsHeldUntilItIsEmpty() {
        PlusRun run = new PlusRun(armed(PlusRun.GUN));
        hold(run, .5f, false, true, false);
        assertEquals(PlusRun.GUN, run.weapon());
        assertEquals(PlusRun.GUN_SHOTS, run.rounds());
        for (int step = 0; step < 600; step++) run.step(STEP, false, false, false, true);
        assertEquals(PlusRun.GUN_SHOTS, run.strikes());
        assertEquals(PlusRun.UNARMED, run.weapon());
    }

    // What helps her

    /** Flat ground with something to help lying two columns from where she starts. */
    private static RunLevels.Level helped(int kind) {
        RunLevels.Level level = flat(60);
        level.items.add(new int[]{kind, 4, FLOOR - 1, 0});
        return level;
    }

    @Test public void breadGivesAHeartBackAndIceCreamMakesHerFaster() {
        RunLevels.Level level = helped(PlusRun.BREAD);
        hole(level, 16, 18);
        PlusRun run = new PlusRun(level);
        run.place(17, 6);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
        hold(run, .5f, false, true, false);
        assertEquals(PlusRun.HEARTS, run.hearts());
        assertEquals(1, run.taken());
        assertEquals(PlusRun.BREAD, run.lastTaken());
        assertEquals(0, run.bonus());

        PlusRun plain = new PlusRun(flat(60)), quick = new PlusRun(helped(PlusRun.ICE));
        hold(plain, 2, false, true, false);
        hold(quick, 2, false, true, false);
        assertTrue(quick.rush() > 0);
        assertTrue(quick.x() > plain.x() + 2);
    }

    @Test public void aBallOfInkTakesOneHurtForHer() {
        RunLevels.Level level = helped(PlusRun.SHIELD);
        level.foes.add(new int[]{RunLevels.TIMES, 9, FLOOR - 1});
        level.land[FLOOR - 1][7] = RunLevels.SPIKE;
        PlusRun run = new PlusRun(level);
        for (int step = 0; step < 240 && run.spared() == 0; step++) run.step(STEP, false, true, false);
        assertEquals(1, run.spared());
        assertEquals(0, run.hurts());
        assertEquals(PlusRun.HEARTS, run.hearts());
        assertFalse(run.shield());
        // She is not put back for it; and standing on in the spikes, the next one costs her.
        assertTrue(run.x() > 5);
        hold(run, 2, false, false, false);
        assertEquals(1, run.hurts());
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
    }

    @Test public void aMagnetBringsPlusesAndTheNotesDealWithWhatSheTouches() {
        RunLevels.Level level = helped(PlusRun.MAGNET);
        for (int column = 6; column <= 12; column++) level.pluses.add(new int[]{column, FLOOR - 4});
        PlusRun run = new PlusRun(level);
        hold(run, 2, false, true, false);
        assertEquals(7, run.collected());
        PlusRun without = new PlusRun(flat(60));
        hold(without, 2, false, true, false);
        assertEquals(0, without.collected());

        level = helped(PlusRun.NOTES);
        level.foes.add(new int[]{RunLevels.TIMES, 9, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 13, FLOOR - 1});
        level.land[FLOOR - 1][7] = RunLevels.SPIKE;
        PlusRun other = new PlusRun(level);
        hold(other, 3, false, true, false);
        assertEquals(2, other.flattened());
        assertEquals(0, other.hurts());
    }

    @Test public void landingOnOneSignAfterAnotherIsWorthMoreEachTime() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.THROWER, 20, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.THROWER, 22, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        // Neither has a knife to throw for a while.
        for (PlusRun.Foe foe : run.foes()) foe.wait = 9;
        run.place(19.9f, FLOOR - 6);
        // Straight down onto the first, and off it to the right onto the second, the jump held for the whole of the bounce.
        for (int step = 0; step < 200 && run.flattened() < 2; step++) run.step(STEP, false, run.flattened() > 0, run.flattened() > 0);
        assertEquals(2, run.flattened());
        assertEquals(2, run.combo());
        assertEquals(1, run.bonus());
        hold(run, 1, false, false, false);
        assertEquals(0, run.combo());
    }

    @Test public void whatSheHasBeenGivenToKeepChangesTheRun() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 5;
        perks.wings = true;
        perks.reach = 2;
        perks.shots = 32;
        RunLevels.Level level = armed(PlusRun.GUN);
        level.pluses.add(new int[]{8, FLOOR - 2});
        PlusRun run = new PlusRun(level, perks), plain = new PlusRun(level);
        assertEquals(5, run.hearts());
        assertEquals(5, run.maxHearts());
        hold(run, 1.2f, false, true, false);
        hold(plain, 1.2f, false, true, false);
        assertEquals(32, run.rounds());
        assertEquals(1, run.collected());
        assertEquals(0, plain.collected());
        // A second jump in the air, once, and only with her wings.
        float highest = run.y(), plainHighest = plain.y();
        for (int step = 0; step < 80; step++) {
            boolean press = step < 14 || step >= 18 && step < 32 || step >= 36 && step < 50;
            run.step(STEP, false, false, press);
            plain.step(STEP, false, false, press);
            highest = Math.min(highest, run.y());
            plainHighest = Math.min(plainHighest, plain.y());
        }
        assertEquals(1, run.flaps());
        assertEquals(0, plain.flaps());
        assertTrue("rose " + (GROUND - highest) + " against " + (GROUND - plainHighest), GROUND - highest > GROUND - plainHighest + 1.5f);
    }

    // Illya and Tsubakieh

    @Test public void aLedgeIsAsThinAsItLooks() {
        RunLevels.Level level = flat(40);
        lay(level, 10, 12, FLOOR - 2, RunLevels.LEDGE);
        // She walks under one two rows above the floor.
        PlusRun run = new PlusRun(level);
        run.place(7, GROUND);
        hold(run, 1.5f, false, true, false);
        assertTrue(run.x() > 13);
        // And jumps up through it, and stands on it.
        run.place(10.6f, GROUND);
        hold(run, .2f, false, false, false);
        hold(run, .5f, false, false, true);
        hold(run, 1, false, false, false);
        assertTrue(run.onGround());
        assertEquals(FLOOR - 2 - PlusRun.TALL, run.y(), .01f);
        // A sign still walks on it, and what is thrown still stops at it.
        level.foes.add(new int[]{RunLevels.MINUS, 11, FLOOR - 3});
        PlusRun other = new PlusRun(level);
        hold(other, 2, false, false, false);
        assertEquals(FLOOR - 2, other.foes().get(0).y + other.foes().get(0).tall(), .01f);
    }

    // The ground of the second volume

    private static void lay(RunLevels.Level level, int from, int to, int row, char what) {
        for (int column = from; column <= to; column++) level.land[row][column] = what;
    }

    /** Flat ground with a pit from one column to another, filled with something from the level of the ground down, or with nothing. */
    private static RunLevels.Level pit(int from, int to, char with) {
        RunLevels.Level level = flat(40);
        hole(level, from, to);
        for (int row = FLOOR; row < RunLevels.ROWS; row++) lay(level, from, to, row, with);
        return level;
    }

    @Test public void aThinLedgeIsJumpedUpThroughAndStoodOn() {
        RunLevels.Level level = flat(40);
        lay(level, 10, 12, FLOOR - 2, RunLevels.THIN);
        PlusRun run = new PlusRun(level);
        run.place(10.6f, GROUND);
        hold(run, .2f, false, false, false);
        hold(run, .5f, false, false, true);
        hold(run, 1, false, false, false);
        assertTrue(run.onGround());
        assertEquals(FLOOR - 2 - PlusRun.TALL, run.y(), .01f);
    }

    @Test public void aLedgeThatCrumblesFallsAMomentAfterItIsStoodOnAndComesBack() {
        RunLevels.Level level = pit(10, 12, RunLevels.AIR);
        lay(level, 10, 12, FLOOR, RunLevels.CRUMBLE);
        PlusRun run = new PlusRun(level);
        run.place(10.7f, GROUND);
        hold(run, .3f, false, false, false);
        assertTrue(run.onGround());
        hold(run, .4f, false, false, false);
        assertTrue(run.crumbling(10, FLOOR) < 0 || run.crumbling(11, FLOOR) < 0);
        hold(run, 2, false, false, false);
        assertEquals(1, run.hurts());
        hold(run, 4, false, false, false);
        assertEquals(RunLevels.CRUMBLE, run.at(10, FLOOR));
        // Run across without stopping and it holds.
        PlusRun other = new PlusRun(level);
        other.place(8, GROUND);
        hold(other, 1.2f, false, true, false);
        assertTrue(other.x() > 13);
        assertEquals(0, other.hurts());
    }

    @Test public void aBeltCarriesHerAndSlimeDoesNotLetHerStop() {
        RunLevels.Level level = flat(60);
        lay(level, 5, 20, FLOOR, RunLevels.BELT_RIGHT);
        lay(level, 30, 50, FLOOR, RunLevels.BELT_LEFT);
        PlusRun run = new PlusRun(level);
        run.place(6, GROUND);
        hold(run, .6f, false, false, false);
        assertTrue(run.x() > 7.2f);
        run.place(40, GROUND);
        hold(run, .6f, false, false, false);
        assertTrue(run.x() < 38.8f);

        RunLevels.Level slimy = flat(60);
        lay(slimy, 0, 59, FLOOR, RunLevels.SLICK);
        PlusRun plain = new PlusRun(flat(60)), slid = new PlusRun(slimy);
        for (PlusRun each : new PlusRun[]{plain, slid}) {
            each.place(10, GROUND);
            hold(each, 2, false, true, false);
        }
        float plainAt = plain.x(), slidAt = slid.x();
        hold(plain, .5f, false, false, false);
        hold(slid, .5f, false, false, false);
        assertTrue(plain.x() - plainAt < .6f);
        assertTrue("slid " + (slid.x() - slidAt), slid.x() - slidAt > 1.5f);
    }

    @Test public void aDraughtCarriesHerOverAGapTooWideToJump() {
        RunLevels.Level level = pit(10, 16, RunLevels.DRAUGHT);
        for (int row = FLOOR - 6; row < FLOOR; row++) lay(level, 10, 16, row, RunLevels.DRAUGHT);
        PlusRun run = new PlusRun(level), without = new PlusRun(pit(10, 16, RunLevels.AIR));
        for (PlusRun each : new PlusRun[]{run, without}) {
            each.place(8, GROUND);
            hold(each, 3, false, true, false);
        }
        assertTrue(run.x() > 17);
        assertEquals(0, run.hurts());
        assertTrue(without.hurts() >= 1);
    }

    @Test public void aLedgeThatMovesCarriesHerAcrossOrUp() {
        RunLevels.Level level = pit(10, 16, RunLevels.AIR);
        level.movers.add(new int[]{10, FLOOR, 2, 5, 0, 44});
        PlusRun run = new PlusRun(level);
        run.place(10.5f, GROUND - .01f);
        hold(run, 2.2f, false, false, false);
        assertEquals(0, run.riding());
        assertTrue("at " + run.x(), run.x() > 15);
        assertEquals(0, run.hurts());

        RunLevels.Level lift = pit(10, 11, RunLevels.AIR);
        lift.movers.add(new int[]{10, FLOOR, 2, 5, 1, 50});
        PlusRun other = new PlusRun(lift);
        other.place(10.5f, GROUND - .01f);
        hold(other, 2.5f, false, false, false);
        assertEquals(FLOOR - 5 - PlusRun.TALL, other.y(), .1f);
    }

    @Test public void aBookmarkDoorLetsHerOutAtTheOtherOne() {
        RunLevels.Level level = flat(40);
        level.doors.add(new int[]{10, 20});
        PlusRun run = new PlusRun(level);
        run.place(8, GROUND);
        hold(run, .6f, false, true, false);
        assertEquals(1, run.doorsUsed());
        assertTrue(run.x() >= 20);
    }

    // Her forms

    /** Flat ground with a ribbon lying two columns from where she starts. */
    private static RunLevels.Level ribbon(RunLevels.Level level, int form) {
        level.items.add(new int[]{PlusRun.FORM + form, 4, FLOOR - 1, 0});
        return level;
    }

    @Test public void inRedSheWalksOnLavaAndThrowsFireAndInBlueSheSwimsAndThrowsWater() {
        for (int form : new int[]{PlusRun.RED, PlusRun.BLUE}) {
            char pool = form == PlusRun.RED ? RunLevels.LAVA : RunLevels.WATER;
            PlusRun without = new PlusRun(pit(10, 14, pool));
            without.place(8, GROUND);
            hold(without, 4, false, true, false);
            assertTrue(without.hurts() >= 1);
            assertEquals(form == PlusRun.RED ? PlusRun.BURNED : PlusRun.SANK, without.cause());

            RunLevels.Level level = ribbon(pit(10, 14, pool), form);
            level.foes.add(new int[]{RunLevels.TIMES, 22, FLOOR - 1});
            level.foes.add(new int[]{RunLevels.TIMES, 24, FLOOR - 1});
            PlusRun run = new PlusRun(level);
            hold(run, 2.2f, false, true, false);
            assertEquals(form, run.form());
            assertEquals(1, run.formed());
            assertTrue(run.x() > 15);
            assertEquals(0, run.hurts());
            // The button throws it, with nothing in her hands.
            run.step(STEP, false, false, false, true);
            assertEquals(1, run.strikes());
            assertEquals(form == PlusRun.RED ? PlusRun.FIRE : PlusRun.WATER, run.shots().get(0).kind);
            hold(run, 1, false, false, false);
            assertTrue("form " + form + " at " + run.x() + " facing left " + run.facingLeft() + " shots " + run.shots().size() + " signs at " + run.foes().get(0).x + " " + run.foes().get(1).x, run.flattened() >= 1);
            // Fire goes on through one sign to the next.
            if (form == PlusRun.RED) assertEquals(2, run.flattened());
        }
    }

    @Test public void inYellowSheRunsMuchFasterAndInGreenSheFlies() {
        PlusRun plain = new PlusRun(flat(60)), quick = new PlusRun(ribbon(flat(60), PlusRun.YELLOW));
        hold(plain, 2, false, true, false);
        hold(quick, 2, false, true, false);
        assertTrue(quick.x() > plain.x() + 4);

        PlusRun run = new PlusRun(ribbon(flat(60), PlusRun.GREEN));
        hold(run, .5f, false, true, false);
        float highest = run.y();
        for (int step = 0; step < 150; step++) {
            run.step(STEP, false, false, true);
            highest = Math.min(highest, run.y());
        }
        assertTrue("rose " + (GROUND - highest), GROUND - highest > 6);
        assertFalse(run.onGround());
        hold(run, 2, false, false, false);
        assertTrue(run.onGround());
    }

    @Test public void inBrownWhatSheTouchesGoesOverButWhatIsThrownStillHurts() {
        RunLevels.Level level = ribbon(flat(60), PlusRun.BROWN);
        level.foes.add(new int[]{RunLevels.TIMES, 9, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.THROWER, 30, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, 1.5f, false, true, false);
        assertEquals(1, run.flattened());
        assertEquals(0, run.hurts());
        run.place(26, GROUND);
        for (int step = 0; step < 400 && run.hurts() == 0; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.hurts());
        assertEquals(PlusRun.STRUCK, run.cause());
    }

    @Test public void inPurpleWhatIsThrownAtHerGoesBack() {
        RunLevels.Level level = ribbon(flat(60), PlusRun.PURPLE);
        level.foes.add(new int[]{RunLevels.THROWER, 10, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .5f, false, true, false);
        run.place(6, GROUND);
        hold(run, 4, false, false, false);
        assertTrue(run.turned() >= 1);
        assertEquals(0, run.hurts());
        assertEquals(1, run.flattened());
    }

    @Test public void inBlackSheIsSlowAndCanHurtNothingAndItPasses() {
        RunLevels.Level level = ribbon(armed(PlusRun.PEN), PlusRun.BLACK);
        level.foes.add(new int[]{RunLevels.THROWER, 30, FLOOR - 1});
        PlusRun run = new PlusRun(level), plain = new PlusRun(flat(60));
        hold(run, 2, false, true, false);
        hold(plain, 2, false, true, false);
        assertEquals(PlusRun.BLACK, run.form());
        assertTrue(run.x() < plain.x() - 3);
        // Nothing she carries is any use, and coming down on a sign only bounces her off it.
        run.step(STEP, false, false, false, true);
        assertEquals(0, run.strikes());
        for (PlusRun.Foe foe : run.foes()) foe.wait = 99;
        run.place(29.9f, FLOOR - 5);
        hold(run, 1, false, false, false);
        assertEquals(0, run.flattened());
        assertEquals(0, run.hurts());
        hold(run, 6, false, false, false);
        assertEquals(0, run.form());
    }

    @Test public void inWhiteSheHasEveryPowerButBlack() {
        PlusRun run = new PlusRun(ribbon(flat(60), PlusRun.WHITE));
        hold(run, .5f, false, true, false);
        assertEquals(PlusRun.WHITE, run.form());
        for (int power = PlusRun.RED; power <= PlusRun.PURPLE; power++) assertTrue("power " + power, run.has(power));
        assertFalse(run.has(PlusRun.BLACK));
        run.step(STEP, false, false, false, true);
        assertEquals(2, run.shots().size());
        // And another ribbon takes its place.
        RunLevels.Level level = ribbon(flat(60), PlusRun.WHITE);
        level.items.add(new int[]{PlusRun.FORM + PlusRun.YELLOW, 8, FLOOR - 1, 0});
        PlusRun other = new PlusRun(level);
        hold(other, 1.2f, false, true, false);
        assertEquals(PlusRun.YELLOW, other.form());
        assertEquals(2, other.formed());
    }

    @Test public void theMendedBatClearsTheSignsNearHerAndACompliementLeavesThemStanding() {
        for (int gift : new int[]{PlusRun.HOMERUN, PlusRun.COMPLIMENT}) {
            RunLevels.Level level = helped(gift);
            level.foes.add(new int[]{RunLevels.TIMES, 9, FLOOR - 1});
            level.foes.add(new int[]{RunLevels.MINUS, 11, FLOOR - 1});
            PlusRun run = new PlusRun(level);
            hold(run, .5f, false, true, false);
            assertEquals(1, run.cleared());
            assertEquals(gift == PlusRun.HOMERUN ? 2 : 0, run.flattened());
            hold(run, 2, false, true, false);
            assertEquals(0, run.hurts());
            assertTrue(run.x() > 12);
        }
    }

    @Test public void whatTheCounterSellsForTheSecondVolumeWorks() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.flagHeart = true;
        perks.inkHurts = 2;
        RunLevels.Level level = helped(PlusRun.SHIELD);
        level.land[FLOOR - 1][8] = RunLevels.SPIKE;
        level.flags.add(30);
        PlusRun run = new PlusRun(level, perks);
        hold(run, .5f, false, true, false);
        run.place(8.1f, GROUND);
        hold(run, 3, false, false, false);
        // The ink took two, and the third cost her a heart.
        assertEquals(2, run.spared());
        assertEquals(1, run.hurts());
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
        run.place(31, GROUND);
        hold(run, .2f, false, false, false);
        assertEquals(PlusRun.HEARTS, run.hearts());
    }

    // The signs of the second volume

    private static PlusRun facing(int kind, int column, float from) {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{kind, column, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        run.place(from, GROUND);
        return run;
    }

    @Test public void eachSignOfTheSecondVolumeDoesWhatItDoesAndCanBeLandedOn() {
        // A slime hops at her, and one of the Heart Guard marches at her.
        for (int kind : new int[]{RunLevels.SLIME, RunLevels.FAN}) {
            PlusRun run = facing(kind, 20, 14);
            float began = run.foes().get(0).x;
            hold(run, 1.5f, false, false, false);
            assertTrue("kind " + kind, run.foes().get(0).x < began - 1);
        }
        // A question mark bounds high, to and fro.
        PlusRun pogo = facing(RunLevels.POGO, 20, 5);
        float highest = 0, began = pogo.foes().get(0).x;
        for (int step = 0; step < 200; step++) {
            pogo.step(STEP, false, false, false);
            highest = Math.max(highest, pogo.foes().get(0).ground - pogo.foes().get(0).y);
        }
        assertTrue(highest > 2 && Math.abs(pogo.foes().get(0).x - began) > 1);
        // A dragon keeps over her head and spits full stops at her.
        PlusRun dragon = facing(RunLevels.DRAGON, 12, 9);
        for (int step = 0; step < 300 && dragon.hurts() == 0; step++) {
            dragon.step(STEP, false, false, false);
            PlusRun.Foe foe = dragon.foes().get(0);
            assertTrue(foe.y + foe.tall() < FLOOR - PlusRun.TALL && Math.abs(foe.x - foe.home) < 3.5f);
        }
        assertTrue(dragon.thrown() >= 1);
        assertEquals(PlusRun.STRUCK, dragon.cause());
        // A censor bar tiptoes until it sees her, then dashes.
        PlusRun bar = facing(RunLevels.BAR, 17, 13);
        float fastest = 0, last = bar.foes().get(0).x;
        for (int step = 0; step < 200 && bar.hurts() == 0; step++) {
            bar.step(STEP, false, false, false);
            fastest = Math.max(fastest, Math.abs(bar.foes().get(0).x - last) / STEP);
            last = bar.foes().get(0).x;
        }
        assertTrue("fastest " + fastest, fastest > 8);
        assertEquals(1, bar.hurts());
        // One of the queen's words hangs until she is under it, and comes down on whoever stops there; run under and it misses.
        PlusRun word = facing(RunLevels.WORD, 10, 5);
        hold(word, 1, false, false, false);
        assertEquals(word.foes().get(0).ground - 4.2f, word.foes().get(0).y, .01f);
        word.place(10.2f, GROUND);
        hold(word, 1, false, false, false);
        assertEquals(1, word.hurts());
        PlusRun past = facing(RunLevels.WORD, 12, 5);
        hold(past, 2, false, true, false);
        assertEquals(0, past.hurts());
        assertTrue(past.x() > 14);
        // Every one of them goes down when landed on, and how long it has been down is counted.
        for (int kind = RunLevels.SLIME; kind <= RunLevels.POGO; kind++) {
            PlusRun run = facing(kind, 20, 40);
            PlusRun.Foe foe = run.foes().get(0);
            for (int step = 0; step < 600 && run.flattened() == 0; step++) {
                if (step % 20 == 0) run.place(foe.x + .1f, foe.y - PlusRun.TALL - .4f);
                run.step(STEP, false, false, false);
            }
            assertEquals("kind " + kind, 1, run.flattened());
            hold(run, .5f, false, false, false);
            assertTrue(foe.flat && foe.act > .3f);
        }
    }

    // Those who wait at the end of its chapters

    // The lands of the second volume

    // Those who wait at the end

    private static final int[] WAITED_IN = {RunLevels.ILLYA_LAND, RunLevels.TSUBAKIEH_LAND, RunLevels.COUNT, 40, 50, 60, 70, 80};

    private static int floorOf(PlusRun run) { return RunLevels.top(run.level(), run.level().arena + 3); }

    /** The first of those who wait in a run that still stands. */
    private static PlusRun.Foe waiting(PlusRun run) {
        for (PlusRun.Foe foe : run.foes()) if (RunLevels.boss(foe.kind) && !foe.flat) return foe;
        return null;
    }

    /**
     * Her in the place where someone waits at the end of a land, a step after its door has
     * shut behind her; with the notes about her for as good as ever if the fight is only to
     * be watched.
     */
    private static PlusRun shutIn(int land, boolean watching) { return shutIn(RunLevels.level(land), watching); }

    private static PlusRun shutIn(RunLevels.Level level, boolean watching) {
        int floor = RunLevels.top(level, level.arena + 3);
        PlusRun.Perks perks = new PlusRun.Perks();
        if (watching) {
            level.items.add(new int[]{PlusRun.NOTES, level.arena + 3, floor - 1, 0});
            perks.lasting = 1000;
        }
        PlusRun run = new PlusRun(level, perks);
        run.place(level.arena + 3.2f, floor - PlusRun.TALL);
        run.step(STEP, false, false, false);
        return run;
    }

    /** Holds her out of the way, high over the middle of the place, for a step. */
    private static void park(PlusRun run) {
        run.place(run.level().arena + 11, floorOf(run) - 9);
        run.step(STEP, false, false, false);
    }

    /** Puts her onto one of them for a step: with the notes about her, a touch is a blow, if a blow can land. */
    private static void touch(PlusRun run, PlusRun.Foe foe) {
        run.place(foe.x + foe.wide() / 2 - PlusRun.WIDE / 2, foe.y + .1f);
        run.step(STEP, false, false, false);
    }

    /**
     * Watches the fight until one of them begins to show that a thing of theirs is coming,
     * with blows whenever one can land for as long as that thing belongs to a later third.
     */
    private static void until(PlusRun run, PlusRun.Foe foe, int mode) {
        for (int step = 0; step < 60 * 300; step++) {
            if (foe.mode == mode && foe.step == 0 && foe.act > .2f && !foe.away && foe.enter <= 0 && foe.flourish <= 0 && foe.dazed <= 0) {
                // Whatever was left in the air and whatever they had made is out of it, so that only the thing in question is.
                for (PlusRun.Shot shot : run.shots()) if (!shot.hers) shot.left = 0;
                for (PlusRun.Foe made : run.foes()) if (made.spawned) made.flat = true;
                return;
            }
            boolean has = false;
            for (int one : PlusRun.does(foe.kind, run.phase(foe))) has |= one == mode;
            if (!has && run.open(foe) && foe.dazed <= 0 && foe.left > 1) touch(run, foe);
            else park(run);
        }
        throw new AssertionError("never did " + mode);
    }

    @Test public void theWayBackShutsBehindHerAndWhoeverWaitsArrivesAndNothingHurtsUntilTheyHave() {
        for (int land : WAITED_IN) {
            RunLevels.Level level = RunLevels.level(land);
            int floor = RunLevels.top(level, level.arena + 3);
            PlusRun run = new PlusRun(level);
            // At the door no one is there, and nothing is shut.
            run.place(level.arena - 2, floor - PlusRun.TALL);
            hold(run, .2f, false, false, false);
            assertFalse("land " + land, run.locked());
            for (PlusRun.Foe foe : run.foes()) if (RunLevels.boss(foe.kind)) assertTrue("land " + land, foe.away && run.absent(foe));
            // A few steps in, it shuts behind her and they arrive, which takes two seconds.
            hold(run, 1, false, true, false);
            assertTrue("land " + land, run.locked());
            assertEquals(1, run.entrances());
            PlusRun.Foe foe = waiting(run);
            assertTrue("land " + land, !foe.away && foe.enter > 0 && run.absent(foe));
            assertFalse(run.gateOpen());
            // Nothing she does lands, and nothing of theirs hurts her, until they have.
            touch(run, foe);
            hold(run, .3f, false, false, false);
            assertEquals("land " + land, 0, run.blows());
            assertEquals("land " + land, 0, run.hurts());
            assertEquals("land " + land, 0, run.specials());
            // The way back is a wall.
            run.place(level.arena + 3, floor - PlusRun.TALL);
            hold(run, .8f, true, false, false);
            assertTrue("land " + land + " at " + run.x(), run.x() >= level.arena + 1);
            assertEquals("land " + land, 0, run.hurts());
            // Arrived, they begin; and they keep to the open.
            run = shutIn(land, true);
            for (int step = 0; step < 60 * 20; step++) {
                park(run);
                for (PlusRun.Foe one : run.foes()) {
                    if (!RunLevels.boss(one.kind) || one.kind == RunLevels.MIRI) continue;
                    assertTrue("land " + land + " at " + one.x, one.x >= level.arena + 2 - .01f && one.x + one.wide() <= level.gate - 1.5f + .01f);
                }
            }
            assertTrue("land " + land, run.specials() >= 3);
            assertTrue("land " + land, run.locked());
        }
    }

    @Test public void eachOfThoseWhoWaitDoesAllSheDoesChangesTwiceAndCanBeBeatenInTheMomentsSheIsSpent() {
        for (int land : WAITED_IN) {
            PlusRun run = shutIn(land, true);
            RunLevels.Level level = run.level();
            int most = 0;
            java.util.List<PlusRun.Foe> all = new java.util.ArrayList<>();
            for (PlusRun.Foe foe : run.foes()) {
                if (!RunLevels.boss(foe.kind)) continue;
                all.add(foe);
                most += foe.most;
                assertEquals(PlusRun.health(foe.kind), foe.most);
            }
            assertTrue("land " + land + " takes " + most, most >= 6 && most <= 10);
            java.util.List<Integer> changedAt = new java.util.ArrayList<>();
            int parried = 0, seen = 0;
            for (int step = 0; step < 60 * 600 && run.standing(); step++) {
                PlusRun.Foe open = null;
                for (PlusRun.Foe foe : all) {
                    if (foe.flat || !run.open(foe) || foe.dazed > 0) continue;
                    // Of the twins, never the one who has taken more: so that both are there to the end.
                    boolean more = false;
                    for (PlusRun.Foe other : all) more |= !other.flat && other.left > foe.left;
                    if (!more) open = foe;
                }
                // Until every thing of this third has been seen, and once outside the moments they are spent, a blow is turned aside.
                PlusRun.Foe first = waiting(run);
                int phase = run.phase(first), wanted = 0, done = 0;
                for (PlusRun.Foe foe : all) {
                    if (foe.flat) continue;
                    for (int one : PlusRun.does(foe.kind, phase)) wanted |= 1 << one << (foe.kind == RunLevels.GARI ? 8 : 0);
                    done |= foe.did << (foe.kind == RunLevels.GARI ? 8 : 0);
                }
                if (open == null && parried == 0 && !run.absent(first) && first.dazed <= 0 && first.step == 1) {
                    int blows = run.blows(), turned = run.parried();
                    touch(run, first);
                    if (run.parried() > turned) {
                        assertEquals(blows, run.blows());
                        parried++;
                    }
                } else if (open != null && (done & wanted) == wanted) {
                    int phases = run.phases(), blows = run.blows();
                    touch(run, open);
                    assertEquals("land " + land, blows + 1, run.blows());
                    if (run.phases() > phases) {
                        changedAt.add(run.blows());
                        // Changing takes a moment, in which nothing touches them.
                        for (PlusRun.Foe foe : all) if (!foe.flat) assertTrue("land " + land + " changing", run.absent(foe));
                    }
                } else {
                    park(run);
                }
                seen = run.specials();
            }
            assertFalse("land " + land + " still stands after " + run.blows() + " blows", run.standing());
            assertEquals("land " + land, most, run.blows());
            assertTrue("land " + land + " turned nothing aside", parried > 0);
            // They changed when a third of what they could take was gone, and again at two thirds.
            assertEquals("land " + land, java.util.Arrays.asList((most + 2) / 3, (2 * most + 2) / 3), changedAt);
            // Everything each of them does was done.
            for (PlusRun.Foe foe : all) {
                int things = 0;
                for (int phase = 0; phase < 3; phase++) for (int one : PlusRun.does(foe.kind, phase)) things |= 1 << one;
                assertTrue("land " + land + " has " + Integer.bitCount(things) + " things", Integer.bitCount(things) >= 3);
                assertEquals("land " + land + " sign " + foe.kind, things, foe.did & things);
            }
            assertTrue("land " + land + " did " + seen, seen >= 6);
            // Down, they are seen to be for a moment; then both ways open.
            assertTrue("land " + land + " not shut", run.locked());
            assertFalse("land " + land + " gate", run.gateOpen());
            for (int step = 0; step < 100; step++) park(run);
            assertFalse(run.locked());
            assertTrue(run.gateOpen());
            assertEquals(1, run.unlocks());
            // (Illya's hearts may still have the arrows the wrong way round.)
            for (int step = 0; step < 240 && run.charmed() > 0; step++) park(run);
            run.place(level.arena + 3, floorOf(run) - PlusRun.TALL);
            hold(run, 1.5f, true, false, false);
            assertTrue("land " + land, run.x() < level.arena);
            // And the gate lets her through.
            run.place(level.gate - 1, RunLevels.top(level, level.gate) - PlusRun.TALL);
            hold(run, .5f, false, true, false);
            assertEquals("land " + land, PlusRun.WON, run.state());
        }
    }

    @Test public void aHurtInThereSendsHerBackToTheDoorAndThePlaceIsAsItWas() {
        for (int land : WAITED_IN) {
            PlusRun run = shutIn(land, true);
            RunLevels.Level level = run.level();
            PlusRun.Foe foe = waiting(run);
            // Blows until she has changed once, and one more.
            int most = foe.kind == RunLevels.MIRI || foe.kind == RunLevels.GARI ? 8 : foe.most, wanted = (most + 2) / 3 + 1;
            for (int step = 0; step < 60 * 300 && run.blows() < wanted; step++) {
                PlusRun.Foe open = null;
                for (PlusRun.Foe one : run.foes()) if (RunLevels.boss(one.kind) && !one.flat && run.open(one) && one.dazed <= 0) open = one;
                if (open != null) touch(run, open);
                else park(run);
            }
            assertEquals("land " + land, wanted, run.blows());
            assertEquals(1, run.phases());
            // Then a fall of rubble, or anything else: here, simply, a touch with nothing to keep her from it.
            foe = waiting(run);
            for (int step = 0; step < 60 * 60 && run.hurts() == 0; step++) {
                if (run.absent(foe) || foe.dazed > 0 || run.open(foe)) {
                    park(run);
                    continue;
                }
                run.bare();
                run.place(foe.x + foe.wide() + .01f - PlusRun.WIDE / 2, foe.y + foe.tall() - PlusRun.TALL);
                run.step(STEP, false, false, false);
            }
            assertEquals("land " + land, 1, run.hurts());
            hold(run, .1f, false, false, false);
            // She is at the flag at the door, outside; no one is there; and what will do against them lies at the door.
            assertEquals("land " + land, level.arena - 3 + (1 - PlusRun.WIDE) / 2, run.x(), .01f);
            assertFalse(run.locked());
            boolean lies = false;
            for (int[] arm : run.arms()) lies |= arm[1] == level.arena - 2;
            assertTrue("land " + land, lies);
            for (PlusRun.Foe one : run.foes()) {
                assertFalse("land " + land, one.spawned);
                if (!RunLevels.boss(one.kind)) continue;
                assertTrue(one.away);
                // They have back what they had lost since they changed: the twins keep count each for herself.
                if (one.kind != RunLevels.MIRI && one.kind != RunLevels.GARI) assertEquals("land " + land, one.most - (most + 2) / 3, one.left);
            }
            // In again, they arrive again, more quickly.
            hold(run, 1.2f, false, true, false);
            assertTrue("land " + land, run.locked());
            assertEquals(2, run.entrances());
            assertTrue(waiting(run).enter > 0 && waiting(run).enter <= PlusRun.ENTER / 2);
        }
    }

    /** Lets a fight go on with her doing something at each step, until nothing of a kind is left in the air; returns how many times she was hurt. */
    private interface Doing { void step(PlusRun run); }

    private static int through(PlusRun run, float seconds, Doing doing) {
        int hurts = run.hurts();
        for (int step = 0; step < seconds * 60 && run.hurts() == hurts; step++) doing.step(run);
        return run.hurts() - hurts;
    }

    /** The nearest thing of a kind in the air that is not hers: how far across it is from her middle, or a long way if there is none. */
    private static float nearest(PlusRun run, int kind) {
        float least = 99;
        for (PlusRun.Shot shot : run.shots()) {
            float across = shot.x - run.x() - PlusRun.WIDE / 2;
            if (!shot.hers && shot.kind == kind && Math.abs(across) < Math.abs(least)) least = across;
        }
        return least;
    }

    @Test public void whatRunsAlongTheGroundIsJumpedAndWhatComesBackIsJumpedTwice() {
        // Illya's grand compliment.
        PlusRun run = shutIn(RunLevels.ILLYA_LAND, true);
        PlusRun.Foe foe = waiting(run);
        until(run, foe, 3);
        int floor = floorOf(run);
        run.bare();
        run.place(foe.x - 6, floor - PlusRun.TALL);
        PlusRun stands = run.copy();
        assertEquals(1, through(stands, 3, one -> one.step(STEP, false, false, false)));
        assertEquals(PlusRun.STRUCK, stands.cause());
        assertEquals(0, through(run, 2.4f, one -> one.step(STEP, false, false, Math.abs(nearest(one, PlusRun.SHOCK)) < 1.5f)));
        // Tsubakieh's leap sends one each way; and her bat goes out and comes back.
        run = shutIn(RunLevels.TSUBAKIEH_LAND, true);
        foe = waiting(run);
        until(run, foe, 2);
        run.bare();
        run.place(run.level().arena + 3, floor - 7);
        // Out of the way while she leaps, and on the floor away from where she came down by the time it runs.
        for (int step = 0; step < 200 && run.special() != 72; step++) park(run);
        for (int step = 0; step < 200 && nearest(run, PlusRun.SHOCK) > 90; step++) park(run);
        float from = foe.x - 5;
        if (from < run.level().arena + 2) from = foe.x + 5;
        run.place(from, floorOf(run) - PlusRun.TALL);
        stands = run.copy();
        assertEquals(1, through(stands, 1.2f, one -> one.step(STEP, false, false, false)));
        assertEquals(0, through(run, 1.2f, one -> one.step(STEP, false, false, Math.abs(nearest(one, PlusRun.SHOCK)) < 1.4f)));
        run = shutIn(RunLevels.TSUBAKIEH_LAND, true);
        foe = waiting(run);
        until(run, foe, 3);
        for (int step = 0; step < 200 && nearest(run, PlusRun.BOOMERANG) > 90; step++) park(run);
        assertEquals(73, run.special());
        run.bare();
        boolean left = foe.leftward;
        run.place(foe.x + (left ? -3.5f : 3.5f), floorOf(run) - PlusRun.TALL);
        stands = run.copy();
        assertEquals(1, through(stands, 2.2f, one -> one.step(STEP, false, false, false)));
        // With it out of her hands she is spent, and can be landed on.
        assertTrue(run.open(foe) || foe.step == 1);
        assertEquals(0, through(run, 2.2f, one -> one.step(STEP, false, false, Math.abs(nearest(one, PlusRun.BOOMERANG)) < 1.7f)));
    }

    @Test public void rubbleHangsBeforeItFallsAndALedgeKeepsItOffAndAStepAsideIsEnough() {
        PlusRun run = shutIn(50, true);
        PlusRun.Foe miri = null;
        for (PlusRun.Foe foe : run.foes()) if (foe.kind == RunLevels.MIRI) miri = foe;
        // Gari out of it first, so that only the headline is in question.
        for (int step = 0; step < 60 * 300; step++) {
            PlusRun.Foe gari = null;
            for (PlusRun.Foe foe : run.foes()) if (foe.kind == RunLevels.GARI && !foe.flat) gari = foe;
            if (gari == null) break;
            if (run.open(gari) && gari.dazed <= 0) touch(run, gari);
            else park(run);
        }
        assertEquals(4, run.blows());
        until(run, miri, 3);
        RunLevels.Level level = run.level();
        int floor = floorOf(run);
        // And the bars Gari made.
        for (PlusRun.Foe foe : run.foes()) if (foe.spawned) foe.flat = true;
        run.bare();
        // In the open, standing still, one of the three comes down on her.
        run.place(level.arena + 8, floor - PlusRun.TALL);
        PlusRun stands = run.copy(), covered = run.copy();
        assertEquals(1, through(stands, 2.2f, one -> one.step(STEP, false, false, false)));
        // A step aside while it hangs, and it misses.
        assertEquals(0, through(run, 2.2f, one -> {
            float across = nearest(one, PlusRun.DEBRIS);
            one.step(STEP, Math.abs(across) < .9f && across >= 0, Math.abs(across) < .9f && across < 0, false);
        }));
        // And under a ledge it never reaches her.
        covered.place(level.arena + 5, floor - PlusRun.TALL);
        assertEquals(0, through(covered, 2.2f, one -> one.step(STEP, false, false, false)));
    }

    @Test public void aDashIsJumpedAndAQuakeAndATwistAndTimesNothingMissWhoeverIsOffTheFloor() {
        // Yeobaek's dash the length of the place.
        PlusRun run = shutIn(60, true);
        PlusRun.Foe foe = waiting(run);
        until(run, foe, 2);
        int floor = floorOf(run);
        run.bare();
        run.place(run.level().arena + 11, floor - PlusRun.TALL);
        PlusRun stands = run.copy();
        assertEquals(1, through(stands, 3, one -> one.step(STEP, false, false, false)));
        assertEquals(PlusRun.SLASHED, stands.cause());
        final PlusRun.Foe dashing = foe;
        assertEquals(0, through(run, 2.4f, one -> one.step(STEP, false, false, !dashing.unseen && Math.abs(dashing.x - one.x()) < 2.6f)));
        // King Slime's hops: on the floor near him she is thrown, up on a ledge she is not.
        run = shutIn(70, true);
        foe = waiting(run);
        floor = floorOf(run);
        until(run, foe, 1);
        run.bare();
        PlusRun near = run.copy();
        final PlusRun.Foe king = waiting(near);
        assertEquals(1, through(near, 4, one -> {
            if (Math.abs(king.x + king.wide() + .3f - one.x()) > .5f || !one.onGround()) one.place(king.x + king.wide() + .3f, floorOf(one) - PlusRun.TALL);
            one.step(STEP, false, false, false);
        }));
        assertTrue(near.cause() == PlusRun.QUAKED || near.cause() == PlusRun.TOUCHED);
        int quakes = run.quakes();
        run.place(run.level().arena + 5, floor - 2 - PlusRun.TALL);
        final PlusRun.Foe hopping = foe;
        assertEquals(0, through(run, 2.5f, one -> {
            // Kept clear of him, a square off the floor at the least.
            if (Math.abs(hopping.x + hopping.wide() / 2 - one.x()) < 4.5f) one.place(hopping.x > one.level().arena + 11 ? one.level().arena + 5 : one.level().arena + 16, floorOf(one) - 2 - PlusRun.TALL);
            one.step(STEP, false, false, false);
        }));
        assertTrue(run.quakes() > quakes);
        // Gokhae's twist: on the ground the arrows turn round; in the air they do not.
        run = shutIn(40, true);
        foe = waiting(run);
        floor = floorOf(run);
        until(run, foe, 3);
        run.bare();
        run.place(run.level().arena + 3, floor - PlusRun.TALL);
        PlusRun grounded = run.copy();
        for (int step = 0; step < 120 && grounded.special() != 143; step++) grounded.step(STEP, false, false, false);
        assertEquals(143, grounded.special());
        assertTrue(grounded.charmed() > 0);
        for (int step = 0; step < 120 && run.special() != 143; step++) {
            if (run.onGround()) run.place(run.level().arena + 3, floor - 3);
            run.step(STEP, false, false, false);
        }
        assertEquals(143, run.special());
        assertEquals(0, run.charmed(), 0);
        // Zero's times nothing: what she carries and her form are gone if she stands on his floor, and not if she is up on a ledge.
        RunLevels.Level level = RunLevels.level(RunLevels.COUNT);
        floor = RunLevels.top(level, level.arena + 3);
        level.weapons.add(new int[]{PlusRun.SWORD, level.arena + 5, floor - 3});
        level.items.add(new int[]{PlusRun.FORM + PlusRun.YELLOW, level.arena + 5, floor - 3, 0});
        run = shutIn(level, true);
        foe = waiting(run);
        run.place(level.arena + 5.2f, floor - 2 - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.SWORD, run.weapon());
        assertEquals(PlusRun.YELLOW, run.form());
        // Up to the thing itself with her out of the way, and then on the ledge or on the floor.
        for (int step = 0; step < 60 * 300 && !(foe.mode == 4 && foe.step == 0 && foe.act > .2f); step++) {
            boolean has = false;
            for (int one : PlusRun.does(foe.kind, run.phase(foe))) has |= one == 4;
            if (!has && run.open(foe) && foe.dazed <= 0) {
                PlusRun.Foe zero = foe;
                run.place(zero.x + zero.wide() / 2 - PlusRun.WIDE / 2, zero.y + .1f);
                run.step(STEP, false, false, false);
            } else {
                park(run);
            }
        }
        assertEquals(4, foe.mode);
        assertEquals(PlusRun.YELLOW, run.form());
        PlusRun below = run.copy();
        below.place(level.arena + 3, floor - PlusRun.TALL);
        for (int step = 0; step < 120 && below.special() != 34; step++) below.step(STEP, false, false, false);
        assertEquals(34, below.special());
        assertEquals(PlusRun.UNARMED, below.weapon());
        assertEquals(0, below.form());
        assertEquals(1, below.wiped());
        run.place(level.arena + 5.2f, floor - 2 - PlusRun.TALL);
        for (int step = 0; step < 120 && run.special() != 34; step++) run.step(STEP, false, false, false);
        assertEquals(34, run.special());
        assertEquals(PlusRun.SWORD, run.weapon());
        assertEquals(PlusRun.YELLOW, run.form());
        assertEquals(0, run.wiped());
        // And with nothing in her hands, shut in, what will do against him comes back, up on the middle ledge.
        for (int step = 0; step < 60 * 4; step++) park(below);
        boolean back = false;
        for (int[] arm : below.arms()) back |= arm[1] == level.arena + 10 && arm[2] == floor - 5;
        assertTrue(back);
    }

    @Test public void nothingThrownGetsThroughZeroAndTsubakiehBatsItBackUnlessSheIsSpentAndTheBlackRibbonOnlyChangesHer() {
        // Zero, spent: a pen thrown at him is nothing; a sword is something.
        PlusRun run;
        PlusRun.Foe foe;
        RunLevels.Level level = RunLevels.level(RunLevels.COUNT);
        int floor = RunLevels.top(level, level.arena + 3);
        for (int with : new int[]{PlusRun.PEN, PlusRun.SWORD}) {
            level = RunLevels.level(RunLevels.COUNT);
            level.weapons.add(new int[]{with, level.arena + 4, floor - 9});
            PlusRun armed = shutIn(level, true), pen = with == PlusRun.PEN ? armed : null;
            armed.place(level.arena + 4.2f, floor - 9);
            armed.step(STEP, false, false, false);
            assertEquals(with, armed.weapon());
            PlusRun.Foe zero = waiting(armed);
            for (int step = 0; step < 60 * 60 && !(armed.open(zero) && zero.act > .8f); step++) park(armed);
            boolean right = zero.x > level.arena + 11;
            armed.place(right ? zero.x - 1.2f : zero.x + zero.wide() + .6f, floor - PlusRun.TALL);
            armed.step(STEP, !right, right, false);
            armed.step(STEP, false, false, false, true);
            hold(armed, .4f, false, false, false);
            assertEquals(armed == pen ? 0 : 1, armed.blows());
        }
        // Tsubakieh: on her feet she bats a pen back; spent, it lands.
        level = RunLevels.level(RunLevels.TSUBAKIEH_LAND);
        floor = RunLevels.top(level, level.arena + 3);
        for (boolean spent : new boolean[]{false, true}) {
            RunLevels.Level with = RunLevels.level(RunLevels.TSUBAKIEH_LAND);
            with.weapons.add(new int[]{PlusRun.PEN, with.arena + 3, floor - 9});
            with.items.add(new int[]{PlusRun.NOTES, with.arena + 3, floor - 9, 0});
            PlusRun.Perks perks = new PlusRun.Perks();
            perks.lasting = 1000;
            PlusRun armed = new PlusRun(with, perks);
            armed.place(with.arena + 3.2f, floor - 9);
            armed.step(STEP, false, false, false);
            assertEquals(PlusRun.PEN, armed.weapon());
            PlusRun.Foe she = waiting(armed);
            for (int step = 0; step < 60 * 60 && !(!armed.absent(she) && she.dazed <= 0 && (spent ? armed.open(she) && she.act > .6f : she.step == 0 && she.act > .7f)); step++) park(armed);
            boolean right = she.x > with.arena + 11;
            armed.place(right ? she.x - 3 : she.x + she.wide() + 2.4f, floor - PlusRun.TALL);
            armed.step(STEP, !right, right, false);
            int turned = armed.turned();
            armed.step(STEP, false, false, false, true);
            hold(armed, .25f, false, false, false);
            assertEquals(spent ? 1 : 0, armed.blows());
            assertEquals(spent ? turned : turned + 1, armed.turned());
        }
        // The Mawang's black ribbon: it does not hurt, it leaves her an ordinary girl for a while.
        run = shutIn(80, true);
        foe = waiting(run);
        until(run, foe, 6);
        for (int step = 0; step < 60 * 5 && run.form() != PlusRun.BLACK; step++) {
            park(run);
            // Into the way of it, once it is in the air, with nothing to keep her from it.
            for (PlusRun.Shot shot : run.shots()) {
                if (shot.kind != PlusRun.CURSE || shot.age < .4f) continue;
                run.bare();
                run.place(shot.x - PlusRun.WIDE / 2, shot.y - PlusRun.TALL / 2);
                run.step(STEP, false, false, false);
                break;
            }
        }
        assertEquals(PlusRun.BLACK, run.form());
        assertEquals(0, run.hurts());
        assertTrue(run.formLeft() <= 4);
    }

    @Test public void oneSignAfterAnotherHitWithWhatSheThrowsIsWorthMoreAndABigOneTakesThree() {
        RunLevels.Level level = flat(40);
        level.weapons.add(new int[]{PlusRun.GUN, 3, FLOOR - 1});
        for (int column = 8; column <= 14; column += 2) level.foes.add(new int[]{RunLevels.MINUS, column, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1, 3});
        PlusRun run = new PlusRun(level);
        hold(run, .3f, false, true, false);
        assertEquals(PlusRun.GUN, run.weapon());
        PlusRun.Foe big = run.foes().get(4);
        assertEquals(3, big.left);
        assertTrue(big.wide() > run.foes().get(0).wide());
        for (int step = 0; step < 240; step++) run.step(STEP, false, false, false, true);
        // Four in a row: one, two and three more for the second, third and fourth. And the big one is down after three shots.
        assertEquals(5, run.flattened());
        assertTrue("bonus " + run.bonus(), run.bonus() >= 1 + 2 + 3);
        assertTrue(big.flat);
        assertEquals(0, run.hurts());
    }

    @Test public void aVillagerGivesWhatSheHoldsAndAStallSellsForWhatHasBeenCollectedHere() {
        RunLevels.Level level = flat(40);
        level.items.add(new int[]{PlusRun.ICE, 6, FLOOR - 1, RunLevels.TTOSUN});
        level.items.add(new int[]{PlusRun.STALL, 12, FLOOR - 1, RunLevels.ALTTEUL});
        for (int column = 7; column <= 10; column++) level.pluses.add(new int[]{column, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .8f, false, true, false);
        assertEquals(RunLevels.TTOSUN, run.giver());
        assertEquals(PlusRun.ICE, run.lastTaken());
        assertTrue(run.rush() > 0);
        while (run.x() < 11.6f) run.step(STEP, false, true, false);
        hold(run, .3f, false, false, false);
        assertTrue(run.atStall());
        assertEquals(4, run.purse());
        // The stall stays where it is; what she has not got she cannot spend.
        assertEquals(1, run.items().size());
        assertFalse(run.buy(PlusRun.SHIELD, 5));
        assertFalse(run.shield());
        assertTrue(run.buy(PlusRun.SHIELD, 3));
        assertTrue(run.shield());
        assertEquals(1, run.purse());
        assertEquals(3, run.spent());
        hold(run, .6f, false, true, false);
        assertFalse(run.atStall());
        // Its keeper greeted her once, when she first came up to it; running away and back again, she is not greeted again.
        assertEquals(1, run.greetings());
        for (int turn = 0; turn < 3; turn++) {
            while (run.x() > 11.8f) run.step(STEP, true, false, false);
            hold(run, .2f, false, false, false);
            assertTrue(run.atStall());
            hold(run, .7f, false, true, false);
            assertFalse(run.atStall());
        }
        assertEquals(1, run.greetings());
        // And what a villager holds is given once: there is nothing there to be given again.
        assertEquals(1, run.taken());
    }

    // The places off the land

    /** The first land from one on that has a place of a kind off it, hidden or not, with her standing at its door; and which of its places that is. */
    private static PlusRun atDoor(int from, int kind, boolean hidden, int[] which) {
        for (int number = from; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int index = 0; index < level.rooms.size(); index++) {
                int[] one = level.rooms.get(index);
                if (one[0] != kind || (one[7] != 0) != hidden) continue;
                PlusRun run = new PlusRun(level);
                run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
                run.step(STEP, false, false, false);
                which[0] = index;
                return run;
            }
        }
        throw new AssertionError("no place of kind " + kind);
    }

    @Test public void aDoorLeadsToAStashAndBackAndAHiddenOneIsBrokenThroughToFirst() {
        int[] which = new int[1];
        PlusRun run = atDoor(RunLevels.SHAPED_FROM, RunLevels.STASH, false, which);
        int[] one = run.level().rooms.get(which[0]);
        assertEquals(which[0], run.door());
        assertEquals(-1, run.room());
        int collected = run.collected(), taken = run.taken();
        assertTrue(run.enter());
        assertEquals(which[0], run.room());
        assertTrue(run.x() >= one[3] && run.x() <= one[4]);
        assertEquals(1 << which[0], run.roomsFound());
        assertEquals(1, run.entered());
        // What is in it is hers for the walking: pluses along the floor, and something to help up on its ledge.
        hold(run, 1.4f, false, true, false);
        assertTrue(run.collected() >= collected + 5);
        assertTrue(run.x() <= one[4] + 1);
        // Back at its door, she goes out where she came in.
        while (run.x() > one[6] + .3f) run.step(STEP, true, false, false);
        hold(run, .2f, false, false, false);
        assertEquals(which[0], run.door());
        assertTrue(run.enter());
        assertEquals(-1, run.room());
        assertEquals(one[1] + (1 - PlusRun.WIDE) / 2, run.x(), .01f);
        assertEquals(one[2] - PlusRun.TALL, run.y(), .01f);
        assertEquals(taken, run.taken());
        // A hidden one: the first use of it only breaks through to it.
        run = atDoor(RunLevels.SHAPED_FROM, RunLevels.STASH, true, which);
        assertEquals(which[0], run.door());
        assertEquals(0, run.revealed());
        assertTrue(run.enter());
        assertEquals(-1, run.room());
        assertEquals(1, run.reveals());
        assertEquals(1 << which[0], run.revealed());
        assertTrue(run.enter());
        assertEquals(which[0], run.room());
        assertEquals(1, run.reveals());
        // Away from any door there is none to use.
        hold(run, .5f, false, true, false);
        assertEquals(-1, run.door());
        assertFalse(run.enter());
    }

    @Test public void aPassageComesOutFurtherOnAndCanBeGoneBackThrough() {
        int[] which = new int[1];
        PlusRun run = atDoor(RunLevels.SHAPED_FROM, RunLevels.PASSAGE, true, which);
        int[] one = run.level().rooms.get(which[0]);
        assertTrue("comes out at " + one[5] + " from " + one[1], one[5] > one[1] + 20);
        assertTrue(run.enter());
        assertTrue(run.enter());
        assertEquals(which[0], run.room());
        // Through it to its far door, and out beyond the crowd it passes.
        for (int step = 0; step < 600 && run.x() < one[8] - .2f; step++) run.step(STEP, false, true, false);
        hold(run, .2f, false, false, false);
        assertEquals(which[0], run.door());
        assertTrue(run.enter());
        assertEquals(-1, run.room());
        assertEquals(one[5] + (1 - PlusRun.WIDE) / 2, run.x(), .01f);
        assertEquals(0, run.hurts());
        // And back in by the door it came out at, which leads to the far end.
        hold(run, .2f, false, false, false);
        assertEquals(which[0], run.door());
        assertTrue(run.enter());
        assertEquals(which[0], run.room());
        assertEquals(one[8] + (1 - PlusRun.WIDE) / 2, run.x(), .01f);
    }

    @Test public void aCellarShutsAndFillsWithThreeWavesAndPaysWhenTheyAreCleared() {
        int[] which = new int[1];
        PlusRun run = atDoor(RunLevels.SHAPED_FROM, RunLevels.ARENA, false, which);
        int[] one = run.level().rooms.get(which[0]);
        int bonus = run.bonus();
        assertTrue(run.enter());
        assertTrue(run.sealed());
        assertEquals(1, run.wave());
        for (int wave = 1; wave <= PlusRun.WAVES; wave++) {
            assertEquals(wave, run.wave());
            int there = 0;
            for (PlusRun.Foe foe : run.foes()) {
                if (!foe.spawned || foe.flat) continue;
                there++;
                assertTrue(foe.x >= one[3] && foe.x <= one[4]);
                // (Dealt with, for the purposes of this.)
                foe.flat = true;
            }
            assertEquals(2 + wave, there);
            // Shut until the last of them is: there is no door to use.
            assertEquals(-1, run.door());
            run.step(STEP, false, false, false);
        }
        assertEquals(0, run.wave());
        assertFalse(run.sealed());
        assertEquals(1, run.roomWins());
        assertEquals(bonus + 20, run.bonus());
        assertEquals(1 << which[0], run.roomsDone());
        hold(run, .2f, false, false, false);
        assertEquals(which[0], run.door());
        assertTrue(run.enter());
        assertEquals(-1, run.room());
        // Cleared, it does not fill again.
        hold(run, .2f, false, false, false);
        assertTrue(run.enter());
        assertEquals(0, run.wave());
        assertFalse(run.sealed());
        // A hurt in there, before it is cleared, sends her back to the flag outside, and it is as it was.
        run = atDoor(RunLevels.SHAPED_FROM, RunLevels.ARENA, false, which);
        assertTrue(run.enter());
        for (int step = 0; step < 60 * 30 && run.hurts() == 0; step++) {
            PlusRun.Foe near = null;
            for (PlusRun.Foe foe : run.foes()) if (foe.spawned && !foe.flat && foe.dazed <= 0) near = foe;
            if (near != null) run.place(near.x + near.wide() - .1f, near.y + near.tall() - PlusRun.TALL);
            run.step(STEP, false, false, false);
        }
        assertEquals(1, run.hurts());
        hold(run, .1f, false, false, false);
        assertEquals(-1, run.room());
        assertTrue(run.x() < run.level().main);
        for (PlusRun.Foe foe : run.foes()) assertFalse(foe.spawned);
        assertEquals(0, run.roomsDone());
    }

    @Test public void pluseFallForTenSecondsInTheRoomWhereTheyAreCaught() {
        int[] which = new int[1];
        PlusRun run = atDoor(RunLevels.SHAPED_FROM, RunLevels.GAME, false, which);
        int[] one = run.level().rooms.get(which[0]);
        int bonus = run.bonus();
        assertTrue(run.enter());
        assertTrue(run.sealed());
        assertEquals(PlusRun.FALLING, run.gameLeft(), .01f);
        // Under each as it falls, she catches it.
        int fell = 0;
        for (int step = 0; step < 60 * 11 && run.gameLeft() > 0; step++) {
            PlusRun.Shot lowest = null;
            for (PlusRun.Shot shot : run.shots()) if (shot.kind == PlusRun.PLUSDROP && (lowest == null || shot.y > lowest.y)) lowest = shot;
            if (lowest != null) {
                assertTrue(lowest.x >= one[3] && lowest.x <= one[4] + 1);
                fell++;
            }
            float want = lowest == null ? run.x() : lowest.x - PlusRun.WIDE / 2;
            run.step(STEP, want < run.x() - .15f, want > run.x() + .15f, false);
            assertEquals(-1, run.door());
        }
        assertTrue(fell > 0);
        assertEquals(0, run.gameLeft(), 0);
        assertFalse(run.sealed());
        assertTrue("caught " + run.caught(), run.caught() >= PlusRun.ENOUGH);
        assertEquals(bonus + run.caught() + 10, run.bonus());
        assertEquals(1, run.roomWins());
        assertEquals(0, run.hurts());
        // And then the door lets her out.
        while (run.x() > one[6] + .3f) run.step(STEP, true, false, false);
        hold(run, .2f, false, false, false);
        assertTrue(run.enter());
        assertEquals(-1, run.room());
    }

    @Test public void everyLandWithShapeHasPlacesOffItWalledAllRound() {
        boolean[] kinds = new boolean[RunLevels.GAME + 1];
        int hidden = 0;
        for (int number = RunLevels.SHAPED_FROM; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            assertTrue("land " + number + " has " + level.rooms.size(), level.rooms.size() >= 2);
            assertTrue(level.main > level.gate && level.main < level.columns);
            for (int[] one : level.rooms) {
                kinds[one[0]] = true;
                if (one[7] != 0) hidden++;
                // Its door is in the land proper, on the ground; the place itself is beyond the end of it.
                assertTrue("land " + number, one[1] < level.gate && one[3] >= level.main && one[4] < level.columns - 1);
                assertEquals("land " + number, RunLevels.ENTRY, level.land[one[2] - 1][one[1]]);
                assertEquals("land " + number, one[2], RunLevels.top(level, one[1]));
                assertEquals(RunLevels.ENTRY, level.land[FLOOR - 1][one[6]]);
                for (int row = 0; row < RunLevels.ROWS; row++) {
                    assertEquals(RunLevels.BLOCK, level.land[row][one[3] - 1]);
                    assertEquals(RunLevels.BLOCK, level.land[row][one[4] + 1]);
                }
                for (int column = one[3]; column <= one[4]; column++) {
                    assertEquals(RunLevels.BLOCK, level.land[FLOOR][column]);
                    assertEquals(RunLevels.BLOCK, level.land[FLOOR - 9][column]);
                    assertFalse(PlusRun.solid(level.land[FLOOR - 1][column]));
                }
                // A passage comes out on the ground further on, at a door of its own.
                if (one[0] == RunLevels.PASSAGE) {
                    assertTrue(one[5] > one[1] && one[5] < level.gate && one[8] > one[6]);
                    assertEquals(RunLevels.ENTRY, level.land[one[9] - 1][one[5]]);
                    assertEquals(RunLevels.ENTRY, level.land[FLOOR - 1][one[8]]);
                } else {
                    assertEquals(one[1], one[5]);
                }
            }
            // No sign of the land is in them, and nothing of the land's to fight with.
            for (int[] foe : level.foes) assertTrue(foe[1] < level.main);
            for (int[] weapon : level.weapons) assertTrue(weapon[1] < level.main);
        }
        for (int kind = 0; kind <= RunLevels.GAME; kind++) assertTrue("no place of kind " + kind, kinds[kind]);
        assertTrue(hidden >= 20);
    }

    // What is asked of her

    /** A land from one on in which something of a kind is asked, with her having just been asked it. */
    private static PlusRun asked(int from, int quest) {
        for (int number = from; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            if (level.quest != quest) continue;
            PlusRun run = new PlusRun(level);
            assertEquals(0, run.quest());
            at(run, RunLevels.giverOf(quest), -1);
            assertEquals("land " + number, PlusRun.ASKED, run.quest());
            assertEquals(1, run.questGiven());
            return run;
        }
        throw new AssertionError("nothing of kind " + quest + " is asked");
    }

    /** Puts her at whoever holds something out, or at the first thing of a kind that lies about, for a step. */
    private static void at(PlusRun run, int who, int kind) {
        for (int[] item : run.items()) {
            if (who >= 0 ? item[3] != who || item[0] == PlusRun.STALL : item[0] != kind) continue;
            run.place(item[1] + (1 - PlusRun.WIDE) / 2, item[2] + 1 - PlusRun.TALL);
            run.step(STEP, false, false, false);
            return;
        }
        throw new AssertionError("no one " + who + " and nothing " + kind);
    }

    @Test public void whatIsAskedOfHerIsDoneAndPaidForOrFailsAndIsNeverNeeded() {
        // Boksun's rice cakes: three lie about the land, out of the way; found, the last of them is the doing of it.
        PlusRun run = asked(RunLevels.SHAPED_FROM, RunLevels.CAKES);
        int cakes = 0;
        for (int[] item : run.items()) if (item[0] == PlusRun.CAKE) cakes++;
        assertEquals(3, cakes);
        int bonus = run.bonus();
        for (int found = 1; found <= 3; found++) {
            assertEquals(PlusRun.ASKED, run.quest());
            at(run, -1, PlusRun.CAKE);
            assertEquals(found, run.cakes());
        }
        assertEquals(PlusRun.DONE, run.quest());
        assertEquals(1, run.questsDone());
        assertEquals(bonus + 15, run.bonus());
        // Found before they were asked for, they still count: asked, it is done at once.
        PlusRun early = new PlusRun(run.level());
        for (int found = 1; found <= 3; found++) at(early, -1, PlusRun.CAKE);
        assertEquals(0, early.quest());
        at(early, RunLevels.BOKSUN, -1);
        assertEquals(PlusRun.DONE, early.quest());

        // Inju's cellar: cleared, it is done.
        run = asked(RunLevels.SHAPED_FROM, RunLevels.CELLAR);
        int cellar = -1;
        for (int index = 0; index < run.level().rooms.size(); index++) if (run.level().rooms.get(index)[0] == RunLevels.ARENA) cellar = index;
        int[] one = run.level().rooms.get(cellar);
        run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        for (int wave = 0; wave < PlusRun.WAVES; wave++) {
            for (PlusRun.Foe foe : run.foes()) if (foe.spawned) foe.flat = true;
            assertEquals(PlusRun.ASKED, run.quest());
            run.step(STEP, false, false, false);
        }
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.DONE, run.quest());

        // Galpi's hidden room: gone into, it is found.
        run = asked(RunLevels.SHAPED_FROM, RunLevels.ROOM);
        int hidden = -1;
        for (int index = 0; index < run.level().rooms.size(); index++) if (run.level().rooms.get(index)[7] != 0) hidden = index;
        one = run.level().rooms.get(hidden);
        run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.ASKED, run.quest());
        assertTrue(run.enter());
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.DONE, run.quest());

        // Somi's letter: brought to Yeseo by the gate, it is done; through a hurt on the way, it is not to be handed over.
        run = asked(RunLevels.SHAPED_FROM, RunLevels.LETTER);
        PlusRun creased = run.copy();
        at(run, -1, PlusRun.DROPOFF);
        assertEquals(PlusRun.DONE, run.quest());
        int[] spikes = {-1, -1};
        RunLevels.Level level = creased.level();
        for (int column = 0; column < level.main && spikes[0] < 0; column++) {
            for (int row = 0; row < RunLevels.ROWS; row++) {
                if (level.land[row][column] == RunLevels.SPIKE) {
                    spikes[0] = column;
                    spikes[1] = row;
                }
            }
        }
        if (spikes[0] >= 0) {
            creased.place(spikes[0] + .2f, spikes[1] + 1 - PlusRun.TALL);
            hold(creased, .3f, false, false, false);
            assertEquals(1, creased.hurts());
            assertEquals(PlusRun.FAILED, creased.quest());
            assertEquals(1, creased.questFails());
            at(creased, -1, PlusRun.DROPOFF);
            assertEquals(PlusRun.FAILED, creased.quest());
        }

        // Ttosun's race: the next flag reached while her count runs is the winning of it; standing about loses it.
        run = asked(RunLevels.SHAPED_FROM, RunLevels.TIME);
        assertTrue("count " + run.questLeft(), run.questLeft() > 6);
        PlusRun slow = run.copy();
        int next = -1;
        for (int flag : run.level().flags) if (flag > run.x() && next < 0) next = flag;
        run.place(next + .5f, RunLevels.top(run.level(), next) - PlusRun.TALL);
        run.step(STEP, false, false, false);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.DONE, run.quest());
        float count = slow.questLeft();
        for (int step = 0; step < (count + 1) * 60 && slow.quest() == PlusRun.ASKED; step++) slow.step(STEP, false, false, false);
        assertEquals(PlusRun.FAILED, slow.quest());

        // Something is asked in every land with shape but those where someone waits; and whoever asks it is there, in its first village.
        int[] kinds = new int[RunLevels.ROOM + 1];
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level here = RunLevels.level(number);
            assertEquals("land " + number, number >= RunLevels.SHAPED_FROM && RunLevels.bossOf(number) < 0, here.quest != 0);
            if (here.quest == 0) continue;
            kinds[here.quest]++;
            int asker = -1;
            for (int[] item : here.items) if (item[3] >= RunLevels.SOMI && item[0] != PlusRun.STALL && item[0] != PlusRun.DROPOFF && asker < 0) asker = item[3];
            assertEquals("land " + number, RunLevels.giverOf(here.quest), asker);
        }
        for (int kind = RunLevels.CAKES; kind <= RunLevels.ROOM; kind++) assertTrue("never asked: " + kind, kinds[kind] >= 3);
    }

    @Test public void aFlagOnHighGroundBringsHerBackOntoIt() {
        RunLevels.Level level = flat(40);
        for (int column = 10; column < 20; column++) for (int row = FLOOR - 3; row < FLOOR; row++) level.land[row][column] = RunLevels.BLOCK;
        level.land[FLOOR - 4][12] = RunLevels.FLAG;
        level.flags.add(12);
        hole(level, 24, 28);
        PlusRun run = new PlusRun(level);
        run.place(11, FLOOR - 3 - PlusRun.TALL);
        while (run.hurts() == 0 && run.time() < 20) run.step(STEP, false, true, false);
        assertEquals(1, run.hurts());
        hold(run, .3f, false, false, false);
        assertEquals(12 + (1 - PlusRun.WIDE) / 2, run.x(), .01f);
        assertEquals(FLOOR - 3 - PlusRun.TALL, run.y(), .01f);
    }

    @Test public void theLandsOfTheSecondVolumeAreLongerAndHaveWhatTheirChaptersAreMadeOf() {
        boolean[] ground = new boolean[128], signs = new boolean[20], ribbons = new boolean[PlusRun.WHITE + 1], met = new boolean[RunLevels.NURI + 1];
        int movers = 0, doors = 0, first = 0, total = 0, heights = 0, crowd = 0;
        for (int number = 1; number <= RunLevels.COUNT; number++) first += RunLevels.level(number).columns;
        for (int number = RunLevels.COUNT + 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int boss = RunLevels.bossOf(number);
            assertEquals(5 + (number - 31) / 10, level.chapter);
            assertEquals("land " + number, number % 10 == 0, boss >= 0);
            assertEquals("land " + number, boss >= 0 ? 4 : 3, level.flags.size());
            assertEquals(boss >= 0, level.guarded);
            assertEquals(boss >= 0, level.arena >= 0);
            // By where she starts, by each flag and before whoever waits; and a book or a brush, and an ink gun or two, each before what it is good against.
            int guns = 0;
            for (int[] weapon : level.weapons) {
                assertEquals("land " + number + " column " + weapon[1], weapon[2] + 1, RunLevels.top(level, weapon[1]));
                if (weapon[0] == PlusRun.GUN) guns++;
            }
            assertEquals("land " + number, (boss >= 0 ? 5 : 4) + 2 + number % 2, level.weapons.size());
            assertTrue("land " + number, guns >= 1);
            assertEquals(level.columns, RunLevels.level(number).columns);
            total += level.columns;
            crowd += level.foes.size();
            int friends = 0, villagers = 0, stalls = 0;
            for (int[] item : level.items) {
                if (item[0] == PlusRun.STALL) stalls++;
                else if (item[0] == PlusRun.DROPOFF) assertEquals(RunLevels.LETTER, level.quest);
                else if (item[3] >= RunLevels.SOMI) villagers++;
                else if (item[3] != 0) friends++;
                met[item[3]] = true;
                if (item[0] > PlusRun.FORM) ribbons[item[0] - PlusRun.FORM] = true;
                int top = RunLevels.top(level, item[1]);
                assertTrue("land " + number + " column " + item[1], top >= 0 && item[2] < top);
            }
            assertEquals("land " + number, boss == RunLevels.MAWANG ? 5 : 3, friends);
            // Two villages, each with someone in it and a stall, and things to look at.
            assertEquals("land " + number, 2, villagers);
            assertEquals("land " + number, 2, stalls);
            assertTrue("land " + number, level.decor.size() >= 8);
            for (char[] row : level.land) for (char square : row) ground[square] = true;
            for (int[] foe : level.foes) signs[foe[0]] = true;
            movers += level.movers.size();
            doors += level.doors.size();
            // The ground is not all at one height.
            java.util.Set<Integer> tops = new java.util.HashSet<>();
            for (int column = 0; column < level.columns; column++) tops.add(RunLevels.top(level, column));
            tops.remove(-1);
            assertTrue("land " + number + " is flat", tops.size() >= 2);
            heights += tops.size();
        }
        // About twice as long as those of the first volume, on the whole; five heights of ground to a land or more; and a crowd.
        assertTrue("mean " + total / 50 + " against " + first / 30, total / 50 > first / 30 * 1.8f);
        assertTrue("heights " + heights / 50f, heights / 50f >= 5);
        assertTrue("signs " + crowd / 50f, crowd / 50f >= 25);
        for (char square : new char[]{RunLevels.THIN, RunLevels.CRUMBLE, RunLevels.BELT_RIGHT, RunLevels.BELT_LEFT, RunLevels.SLICK, RunLevels.DRAUGHT,
                RunLevels.DOOR, RunLevels.LAVA, RunLevels.WATER}) {
            assertTrue("no " + square, ground[square]);
        }
        assertTrue(movers > 5 && doors > 5);
        for (int kind = RunLevels.THROWER; kind <= RunLevels.MAWANG; kind++) {
            if (kind != RunLevels.ILLYA && kind != RunLevels.TSUBAKIEH) assertTrue("no sign " + kind, signs[kind]);
        }
        for (int form = PlusRun.RED; form <= PlusRun.WHITE; form++) assertTrue("no ribbon " + form, ribbons[form]);
        for (int who = 1; who <= RunLevels.NURI; who++) assertTrue("no one met " + who, met[who]);
    }

    // Her lines

    @Test public void everyLineOfTheStoryEndsOnHerCatchphraseAndHasANameOfItsOwn() {
        java.util.Set<String> names = new java.util.HashSet<>();
        int lines = 0;
        for (String[][] group : RunStory.all()) {
            for (String[] line : group) {
                lines++;
                assertTrue("a name used twice: " + line[0], names.add(line[0]));
                assertTrue(line[0], line[0].matches("[a-z0-9]+"));
                // Its words, who says it, and the scene it is said in, which is what the voice is told.
                assertEquals(line[0], 4, line.length);
                assertTrue(line[0], RunStory.speaker(line) >= 0 && line[2].length() == 1);
                assertTrue(line[0], line[3].length() > 20 && line[3].length() <= 600);
                if (RunStory.speaker(line) != 0) {
                    // Someone else's line: never her name for the player.
                    assertFalse(line[1], line[1].contains("Sloppy"));
                    continue;
                }
                assertTrue("does not end on Sloppy~♡: " + line[1], line[1].endsWith(" " + Chatter.SLOPPY));
                assertFalse("a comma before her catchphrase: " + line[1], line[1].endsWith(", " + Chatter.SLOPPY));
                assertEquals("more than one heart: " + line[1], line[1].indexOf('♡'), line[1].lastIndexOf('♡'));
            }
        }
        assertEquals(95 + 86 + 37 + 7 + 11, lines);
        assertEquals(RunLevels.ROOM, RunStory.ASKS.length);
        assertEquals(RunLevels.ROOM, RunStory.ASKED.length);
        assertEquals(RunLevels.GAME + 1, RunStory.ROOMS.length);
        assertEquals(RunLevels.NURI - RunLevels.SOMI + 1, RunStory.MEET.length);
        assertEquals(8, RunStory.ENTER.length);
        assertEquals(16, RunStory.PHASES.length);
        for (int kind : new int[]{RunLevels.ZERO, RunLevels.ILLYA, RunLevels.TSUBAKIEH, RunLevels.GOKHAE, RunLevels.MIRI, RunLevels.YEOBAEK, RunLevels.KING,
                RunLevels.MAWANG}) {
            assertTrue(RunStory.waiting(kind) >= 0 && RunStory.waiting(kind) < 8);
        }
        assertEquals(RunLevels.ALL_CHAPTERS, RunStory.CHAPTERS.length);
        assertEquals(PlusRun.WHITE, RunStory.FORMS.length);
        assertEquals(RunLevels.MAWANG - RunLevels.GOKHAE + 1, RunStory.BLOWS_LATER.length);
        for (int land = RunLevels.COUNT + 1; land <= RunLevels.TOTAL; land++) {
            assertEquals("land " + land, RunLevels.bossOf(land) >= 0, RunStory.outro(land) != null);
            if (land % 10 == 0 || land % 10 == 1) assertTrue("land " + land, RunStory.intro(land) != null);
        }
        assertEquals(RunStory.SPEAKERS.length(), RunStory.NAMES.length);
        assertEquals(PlusRun.GUN + 1, RunStory.ARMS.length);
        assertEquals(PlusRun.NOTES + 1, RunStory.TOOK.length);
        assertEquals(10, RunStory.SEE.length);
        // The story moves on where someone joins her and where someone waits for her.
        for (int land : new int[]{1, 3, 7, 9, 13, RunLevels.ILLYA_LAND, 19, RunLevels.TSUBAKIEH_LAND, 25, RunLevels.COUNT}) {
            assertTrue("land " + land, RunStory.intro(land) != null);
        }
        for (int land = 1; land <= RunLevels.COUNT; land++) {
            assertEquals("land " + land, RunLevels.bossOf(land) >= 0, RunStory.outro(land) != null);
        }
    }

    // The thirty lands

    private static final int WALK = 0, WAIT = 1, BACK = 2;
    /** How many steps the jump button is held in each of the jumps the player tries. */
    private static final int[] HOLDS = {1, 4, 8, 13, 22};

    /** Plays a plan out on a run: what the buttons do at each step of it. */
    private static void act(PlusRun run, int plan, int step) {
        if (plan == WALK) run.step(STEP, false, true, false);
        else if (plan == WAIT) run.step(STEP, false, false, false);
        else if (plan == BACK) run.step(STEP, true, false, false);
        else run.step(STEP, false, true, step < HOLDS[plan - 3]);
    }

    private static int length(int plan) { return plan == WALK ? 4 : plan == WAIT ? 12 : plan == BACK ? 14 : 6; }

    /**
     * How a plan would come out: it is tried on a copy of the run and followed until she is
     * back on the ground, and then the best of what she could do next is tried too. A plan
     * that costs a heart is worth nothing; otherwise the further on she is, the better.
     */
    private static float climb;

    private static float worth(PlusRun run, int plan, int further) {
        PlusRun trial = run.copy();
        int hurts = trial.hurts(), blows = trial.blows();
        int step = 0;
        for (; step < length(plan) && trial.state() == PlusRun.PLAYING; step++) act(trial, plan, step);
        for (; step < 200 && !trial.onGround() && trial.state() == PlusRun.PLAYING; step++) act(trial, plan, step);
        if (trial.hurts() > hurts) return -1000;
        float here = (trial.blows() - blows) * 6 + (trial.state() == PlusRun.WON ? 50 : 0) - (plan == WAIT ? .4f : plan == BACK ? 3 : 0);
        here -= climb * trial.y();
        if (further <= 0 || trial.state() != PlusRun.PLAYING) return trial.x() + here;
        float most = -1000;
        for (int next = 0; next < 3 + HOLDS.length; next++) most = Math.max(most, worth(trial, next, further - 1));
        return most + here;
    }
    /**
     * A player with no skill, only foresight: at each moment it tries each thing it might do
     * on a copy of the run, and does the one that comes out furthest on without a hurt. If
     * it can reach the gate, the land can be crossed.
     */
    private static PlusRun cross(RunLevels.Level level, float seconds) {
        PlusRun run = new PlusRun(level);
        float furthest = run.x(), since = 0;
        while (run.state() == PlusRun.PLAYING && run.time() < seconds) {
            // Getting nowhere, it backs off for a run at whatever is in the way.
            if (run.x() > furthest + .05f) {
                furthest = run.x();
                since = run.time();
            } else if (run.time() - since > 1.2f && run.onGround() && run.riding() < 0 && worth(run, BACK, 0) > -1000) {
                for (int step = 0; step < 28 && run.state() == PlusRun.PLAYING; step++) run.step(STEP, true, false, false);
                furthest = run.x();
                since = run.time();
            }
            int best = WAIT;
            float most = -Float.MAX_VALUE;
            // Stopped by something, it looks for a way up it.
            climb = run.time() - since > .5f ? 1.5f : 0;
            if (run.onGround()) {
                for (int plan = 0; plan < 3 + HOLDS.length; plan++) {
                    float worth = worth(run, plan, 1);
                    if (worth > most) {
                        most = worth;
                        best = plan;
                    }
                }
            } else {
                best = WALK;
            }
            int step = 0;
            for (; step < length(best) && run.state() == PlusRun.PLAYING; step++) act(run, best, step);
            if (best >= 3) {
                for (; step < 200 && !run.onGround() && run.state() == PlusRun.PLAYING; step++) act(run, best, step);
            }
        }
        return run;
    }

    /** The land round where she is, as text, for a message about where she got stuck. */
    private static String around(PlusRun run) {
        StringBuilder text = new StringBuilder();
        for (int row = FLOOR - 7; row <= FLOOR; row++) {
            text.append('|');
            for (int column = (int) run.x() - 8; column <= (int) run.x() + 14; column++) {
                char here = run.at(column, row);
                for (PlusRun.Foe foe : run.foes()) if (!foe.flat && (int) foe.x == column && (int) (foe.y + foe.tall() - .01f) == row) here = (char) ('0' + foe.kind);
                if (column == (int) run.x() && row == (int) (run.y() + PlusRun.TALL - .01f)) here = '@';
                text.append(here == ' ' ? '.' : here);
            }
        }
        return text.toString();
    }

    /** A land without Zero, Illya or Tsubakieh in it: getting past them is a matter of skill, not of whether the land can be crossed. */
    private static RunLevels.Level unguarded(RunLevels.Level level) {
        for (int index = level.foes.size() - 1; index >= 0; index--) {
            if (RunLevels.boss(level.foes.get(index)[0])) level.foes.remove(index);
        }
        level.guarded = false;
        return level;
    }

    @Test public void everyLandCanBeCrossedWithoutLosingAHeart() {
        for (int number = 1; number <= RunLevels.COUNT; number++) {
            PlusRun run = cross(unguarded(RunLevels.level(number)), 240);
            assertEquals("land " + number + " stopped at " + run.x() + " of " + run.columns() + " with " + run.hearts() + " hearts" + around(run),
                    PlusRun.WON, run.state());
            assertEquals("land " + number, PlusRun.HEARTS, run.hearts());
        }
    }

    @Test public void everyLandOfTheSecondVolumeCanBeCrossedWithoutLosingAHeart() {
        for (int number = RunLevels.COUNT + 1; number <= RunLevels.TOTAL; number++) {
            PlusRun run = cross(unguarded(RunLevels.level(number)), 400);
            assertEquals("land " + number + " stopped at " + run.x() + " of " + run.columns() + " with " + run.hearts() + " hearts" + around(run),
                    PlusRun.WON, run.state());
            assertEquals("land " + number, PlusRun.HEARTS, run.hearts());
        }
    }

    /** Not a check: run with RUN_SEEDS2 set in the environment to get the numbers for the lands of the second volume. */
    @Test public void findsTheNumbersTheLaterLandsAreMadeFrom() {
        if (System.getenv("RUN_SEEDS2") == null) return;
        StringBuilder found = new StringBuilder();
        for (int number = RunLevels.COUNT + 1; number <= RunLevels.TOTAL; number++) {
            long chosen = -1;
            String stuck = "";
            for (long seed = 1; seed <= 60 && chosen < 0; seed++) {
                PlusRun run = cross(unguarded(RunLevels.level(number, seed)), 400);
                if (run.state() == PlusRun.WON && run.hearts() == PlusRun.HEARTS) chosen = seed;
                else if (seed <= 3) stuck += "\n  " + number + "/" + seed + " at " + (int) run.x() + " of " + run.columns() + " hearts " + run.hearts() + " " + around(run);
            }
            found.append(chosen).append((number - RunLevels.COUNT) % 25 == 0 ? ",\n" : ", ");
            if (chosen < 0 || chosen > 3) found.append(stuck).append("\n");
        }
        throw new AssertionError("SEEDS2:\n" + found);
    }

    /** Not a check: run with RUN_COUNT set to see how many signs the lands have in them. */
    @Test public void countsTheSignsInTheLands() {
        if (System.getenv("RUN_COUNT") == null) return;
        int[] from = {1, 23, 31}, to = {22, 30, 80};
        StringBuilder found = new StringBuilder();
        for (int part = 0; part < 3; part++) {
            int signs = 0, columns = 0;
            for (int number = from[part]; number <= to[part]; number++) {
                RunLevels.Level level = RunLevels.level(number);
                signs += level.foes.size();
                columns += level.columns;
            }
            int lands = to[part] - from[part] + 1;
            found.append("lands ").append(from[part]).append("-").append(to[part]).append(": ").append(signs / (float) lands).append(" signs a land, ")
                    .append(columns / lands).append(" columns, one every ").append(columns / (float) signs).append(" columns\n");
        }
        // And where there is something to go and look at, in a few of them.
        for (int number : new int[]{2, 5, 9, 14, 23, 24, 25, 27, 31, 36, 44, 47, 55, 58, 66, 69, 77}) {
            RunLevels.Level level = RunLevels.level(number);
            found.append("land ").append(number).append(": arena ").append(level.arena).append(" gate ").append(level.gate);
            for (int[] one : level.rooms) found.append(" room").append(one[0]).append(one[7] != 0 ? "h " : " ").append(one[1]);
            for (int[] item : level.items) if (item[3] >= RunLevels.SOMI) found.append(item[0] == PlusRun.STALL ? " stall " : " who" + item[3] + " ").append(item[1]);
            for (int[] weapon : level.weapons) if (weapon[0] == PlusRun.GUN) found.append(" gun ").append(weapon[1]);
            for (int[] item : level.items) if (item[0] > PlusRun.FORM) found.append(" ribbon").append(item[0] - PlusRun.FORM).append(" ").append(item[1]);
            if (!level.movers.isEmpty()) found.append(" mover ").append(level.movers.get(0)[0]);
            String seen = "";
            for (int column = 0; column < level.columns; column++) {
                for (int row = 0; row < RunLevels.ROWS; row++) {
                    char here = level.land[row][column];
                    if ("-c><~uDLw".indexOf(here) >= 0 && seen.indexOf(here) < 0) {
                        seen += here;
                        found.append(" ").append(here).append(column);
                    }
                }
            }
            boolean[] signs = new boolean[20];
            for (int[] foe : level.foes) {
                if (foe[0] >= RunLevels.SLIME && foe[0] <= RunLevels.POGO && !signs[foe[0]]) {
                    signs[foe[0]] = true;
                    found.append(" sign").append(foe[0]).append(" ").append(foe[1]);
                }
            }
            found.append("\n");
        }
        throw new AssertionError("COUNT:\n" + found);
    }

    @Test public void theLandsGrowAndEachHasEnoughInIt() {
        int before = 0;
        for (int number = 1; number <= RunLevels.COUNT; number++) {
            RunLevels.Level level = RunLevels.level(number);
            boolean shaped = number >= RunLevels.SHAPED_FROM, waits = RunLevels.bossOf(number) >= 0;
            assertEquals(number, level.number);
            assertEquals((number - 1) / 6, level.chapter);
            assertTrue("land " + number + " has " + level.pluses.size() + " pluses", level.pluses.size() >= 15);
            // Two flags along the way, and one more at the door of whoever waits at the end.
            assertEquals(waits ? 3 : 2, level.flags.size());
            assertEquals(RunLevels.GATE, level.land[RunLevels.top(level, level.gate) - 1][level.gate]);
            assertTrue("land " + number + " is " + level.columns, level.columns > 60 && level.columns < (shaped ? 600 : 400));
            // Made from the number kept for it, it is the same land every time.
            assertEquals(level.columns, RunLevels.level(number).columns);
            // Something to fight with by each flag, and from the second land on by where she starts; each lies on the ground.
            // From the twenty-third there is also a book or a brush, and an ink gun, each before what it is good against.
            assertEquals("land " + number, (number == 1 ? 2 : waits ? 4 : 3) + (shaped ? 2 : 0), level.weapons.size());
            // Chikuwa waits with bread from the third land and Rune with her ink from the ninth; ice cream and the rest from the fourth.
            int bread = 0, ink = 0, loose = 0, villagers = 0, stalls = 0;
            for (int[] item : level.items) {
                int top = RunLevels.top(level, item[1]);
                assertTrue("land " + number + " column " + item[1], top >= 0 && item[2] < top);
                if (!shaped) assertEquals(FLOOR, top);
                assertFalse(PlusRun.solid(level.land[item[2]][item[1]]));
                if (item[0] == PlusRun.STALL) stalls++;
                else if (item[0] == PlusRun.DROPOFF) assertEquals(RunLevels.LETTER, level.quest);
                else if (item[3] == 1) bread++;
                else if (item[3] == 2) ink++;
                else if (item[3] >= RunLevels.SOMI) villagers++;
                else loose++;
            }
            assertEquals("land " + number, number >= 3 ? 1 : 0, bread);
            assertEquals("land " + number, number >= 9 ? 1 : 0, ink);
            // From the twenty-third, a village: someone in it who gives her something, a stall, and things to look at.
            assertEquals("land " + number, shaped ? 1 : 0, villagers);
            assertEquals("land " + number, shaped ? 1 : 0, stalls);
            assertEquals("land " + number, shaped, !level.decor.isEmpty());
            if (number >= 4) assertTrue("land " + number + " has nothing lying about to help", loose >= 1);
            assertEquals("land " + number, waits, level.guarded);
            assertEquals("land " + number, waits, level.arena >= 0);
            for (int[] weapon : level.weapons) {
                assertTrue(weapon[0] >= PlusRun.PEN && weapon[0] <= PlusRun.GUN);
                assertEquals("land " + number + " column " + weapon[1], weapon[2] + 1, RunLevels.top(level, weapon[1]));
            }
            // And from there on the ground is not all at one height.
            java.util.Set<Integer> heights = new java.util.HashSet<>();
            for (int column = 0; column < level.columns; column++) heights.add(RunLevels.top(level, column));
            heights.remove(-1);
            if (shaped) assertTrue("land " + number + " is flat", heights.size() >= 3);
            if (number % 6 == 0 && number < RunLevels.COUNT) {
                assertTrue("land " + number + " is " + level.columns + " long, after " + before, level.columns > before - 30);
                before = level.columns;
            }
        }
        RunLevels.Level last = RunLevels.level(RunLevels.COUNT);
        assertTrue(last.guarded);
        int zeros = 0;
        for (int[] foe : last.foes) if (foe[0] == RunLevels.ZERO) zeros++;
        assertEquals(1, zeros);
    }

    /**
     * Not a check: run with RUN_SEEDS set in the environment to get, for each land, the first number from which
     * a land comes that the player above can cross without a hurt.
     */
    @Test public void findsTheNumbersTheLandsAreMadeFrom() {
        if (System.getenv("RUN_SEEDS") == null) return;
        StringBuilder found = new StringBuilder();
        for (int number = 1; number <= RunLevels.COUNT; number++) {
            long chosen = -1;
            for (long seed = 1; seed <= 400 && chosen < 0; seed++) {
                PlusRun run = cross(unguarded(RunLevels.level(number, seed)), 240);
                if (run.state() == PlusRun.WON && run.hearts() == PlusRun.HEARTS) chosen = seed;
            }
            found.append(chosen).append(number % 15 == 0 ? ",\n" : ", ");
        }
        System.out.println("SEEDS:\n" + found);
        throw new AssertionError("SEEDS:\n" + found);
    }
}
