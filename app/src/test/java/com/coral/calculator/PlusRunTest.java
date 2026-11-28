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

    @Test public void fallingIntoAGapCostsAHeartAndPutsHerBackOnTheLastFirmGroundAndTheLastHeartCostsALifeAtTheFlag() {
        RunLevels.Level level = flat(60);
        hole(level, 16, 18);
        hole(level, 40, 42);
        level.flags.add(30);
        PlusRun run = new PlusRun(level);
        assertEquals(PlusRun.LIVES, run.lives());
        run.place(17, 6);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
        assertEquals(2, run.x(), .01f);
        run.place(31, GROUND);
        hold(run, .2f, false, false, false);
        assertEquals(1, run.flagsPassed());
        // She walks on to the edge and off it: she is back on the ground she last stood on, not at the flag.
        for (int step = 0; step < 300 && run.hurts() < 2; step++) run.step(STEP, false, true, false);
        assertEquals(PlusRun.HEARTS - 2, run.hearts());
        assertEquals(PlusRun.LIVES, run.lives());
        assertTrue("at " + run.x(), run.x() > 37 && run.x() < 40);
        assertEquals(GROUND, run.y(), .01f);
        // The last heart costs a life: she is at the flag, with her hearts full again.
        run.place(41, 6);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.LIVES - 1, run.lives());
        assertEquals(1, run.livesLost());
        assertEquals(PlusRun.HEARTS, run.hearts());
        assertEquals(PlusRun.HEARTS * 2, run.halves());
        assertEquals(30, (int) run.x());
        assertEquals(PlusRun.PLAYING, run.state());
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

    @Test public void aHurtCostsAHeartAndNothingElseAndTheLastLifeEndsTheRun() {
        // Walking into a sign costs a heart, and she is not sent anywhere: a step back from it, and a moment in which nothing hurts her.
        RunLevels.Level signs = flat(60);
        signs.flags.add(6);
        signs.foes.add(new int[]{RunLevels.TIMES, 14, FLOOR - 1});
        PlusRun hit = new PlusRun(signs);
        hit.place(7, GROUND);
        for (int step = 0; step < 300 && hit.hurts() == 0; step++) hit.step(STEP, false, true, false);
        assertEquals(1, hit.hurts());
        assertEquals(PlusRun.HEARTS - 1, hit.hearts());
        assertEquals(PlusRun.LIVES, hit.lives());
        assertTrue(hit.safe());
        assertTrue(hit.flinching());
        float where = hit.x();
        hold(hit, .3f, false, false, false);
        assertTrue("at " + hit.x() + " from " + where, hit.x() > 11 && hit.x() < where);
        assertFalse(hit.flinching());
        // Safe for that moment, she walks through it; and when the moment is over, it hurts again.
        hold(hit, .3f, false, true, false);
        assertEquals(1, hit.hurts());
        // Falls: each costs a heart, the last of the hearts a life, and the last of the lives the land.
        RunLevels.Level level = flat(60);
        hole(level, 16, 18);
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.lives = 2;
        PlusRun run = new PlusRun(level, perks);
        assertEquals(2, run.lives());
        for (int fall = 0; fall < PlusRun.HEARTS * 2 - 1; fall++) {
            run.place(17, 6);
            hold(run, 2, false, false, false);
            assertEquals(PlusRun.PLAYING, run.state());
            assertEquals(fall < PlusRun.HEARTS - 1 ? 2 : 1, run.lives());
        }
        assertEquals(1, run.hearts());
        run.place(17, 6);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.LOST, run.state());
        assertEquals(0, run.lives());
        assertEquals(0, run.hearts());
        // What she has been given to keep may be more lives.
        perks.lives = PlusRun.LIVES + 3;
        assertEquals(PlusRun.LIVES + 3, new PlusRun(level, perks).lives());
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
            assertEquals(1, run.casts());
            assertEquals(0, run.strikes());
            assertEquals(form == PlusRun.RED ? PlusRun.FIRE : PlusRun.WATER, run.shots().get(0).kind);
            hold(run, 1, false, false, false);
            assertTrue("form " + form + " at " + run.x() + " facing left " + run.facingLeft() + " shots " + run.shots().size() + " signs at " + run.foes().get(0).x + " " + run.foes().get(1).x, run.flattened() >= 1);
            // Fire goes on through one sign to the next.
            if (form == PlusRun.RED) assertEquals(2, run.flattened());
        }
    }

    @Test public void inRedAndInBlueWhatSheCarriesIsUsedAndTheFireOrTheWaterIsThrownWithIt() {
        int[] carried = {PlusRun.PEN, PlusRun.BRUSH, PlusRun.SWORD, PlusRun.KNIFE, PlusRun.BOOK, PlusRun.GUN};
        for (int form : new int[]{PlusRun.RED, PlusRun.BLUE, PlusRun.WHITE}) {
            for (int weapon : carried) {
                RunLevels.Level level = flat(60);
                level.weapons.add(new int[]{weapon, 3, FLOOR - 1});
                level.items.add(new int[]{PlusRun.FORM + form, 5, FLOOR - 1, 0});
                PlusRun run = new PlusRun(level);
                hold(run, .8f, false, true, false);
                assertEquals(weapon, run.weapon());
                assertEquals(form, run.form());
                hold(run, .5f, false, false, false);
                run.step(STEP, false, false, false, true);
                String which = "form " + form + " with " + weapon;
                // What she carries is used as it always is: thrown, fired, or swung.
                assertEquals(which, 1, run.strikes());
                boolean swung = weapon == PlusRun.BRUSH || weapon == PlusRun.SWORD;
                assertEquals(which, swung, run.swinging() >= 0);
                int own = 0, fire = 0, water = 0, steam = 0;
                for (PlusRun.Shot shot : run.shots()) {
                    if (shot.kind == PlusRun.FIRE) fire++;
                    else if (shot.kind == PlusRun.WATER) water++;
                    else if (shot.kind == PlusRun.STEAM) steam++;
                    else if (shot.kind == weapon) own++;
                }
                assertEquals(which, swung ? 0 : 1, own);
                // And the colour's own is thrown with it.
                assertEquals(which, 1, run.casts());
                // Fire in red, water in blue; and in white, where both are worn, the steam they make together.
                assertEquals(which, form == PlusRun.RED ? 1 : 0, fire);
                assertEquals(which, form == PlusRun.BLUE ? 1 : 0, water);
                assertEquals(which, form == PlusRun.WHITE ? 1 : 0, steam);
                // The ink gun goes on for as long as the button is held, and so does what goes with it.
                if (weapon == PlusRun.GUN) {
                    hold(run, 1, false, false, false);
                    for (int step = 0; step < 60; step++) run.step(STEP, false, false, false, true);
                    assertTrue(which + " fired " + run.strikes(), run.strikes() >= 5);
                    assertTrue(which + " cast " + run.casts(), run.casts() >= 3);
                }
            }
        }
        // With her own hair the button does only what it did.
        RunLevels.Level level = flat(60);
        level.weapons.add(new int[]{PlusRun.BOOK, 3, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .6f, false, true, false);
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.strikes());
        assertEquals(0, run.casts());
        assertEquals(1, run.shots().size());
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

    @Test public void inBrownNothingAtArmsLengthHurtsHerAndWhatIsThrownCostsHalfAHeart() {
        RunLevels.Level level = ribbon(flat(60), PlusRun.BROWN);
        level.foes.add(new int[]{RunLevels.TIMES, 9, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.THROWER, 30, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, 1.5f, false, true, false);
        // A touch: the sign goes over, and she is not hurt.
        assertEquals(1, run.flattened());
        assertEquals(0, run.hurts());
        // What is thrown from a distance still lands: for half a heart.
        run.place(26, GROUND);
        for (int step = 0; step < 400 && run.hurts() == 0; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.hurts());
        assertEquals(PlusRun.STRUCK, run.cause());
        assertEquals(PlusRun.BROWN, run.form());
        assertEquals(PlusRun.HEARTS * 2 - 1, run.halves());
        assertEquals(PlusRun.HEARTS, run.hearts());
        // With her own hair the same knife costs a whole one.
        RunLevels.Level plain = flat(60);
        plain.foes.add(new int[]{RunLevels.THROWER, 30, FLOOR - 1});
        PlusRun other = new PlusRun(plain);
        other.place(26, GROUND);
        for (int step = 0; step < 400 && other.hurts() == 0; step++) other.step(STEP, false, false, false);
        assertEquals(PlusRun.HEARTS * 2 - 2, other.halves());
        // In white, which is every colour, it does not land at all: it goes back, as in purple.
        RunLevels.Level all = ribbon(flat(60), PlusRun.WHITE);
        all.foes.add(new int[]{RunLevels.THROWER, 30, FLOOR - 1});
        PlusRun white = new PlusRun(all);
        hold(white, 1.5f, false, true, false);
        white.place(26, GROUND);
        for (int step = 0; step < 400 && white.turned() == 0; step++) white.step(STEP, false, false, false);
        assertEquals(1, white.turned());
        assertEquals(PlusRun.HEARTS * 2, white.halves());

        // Those who wait: Tsubakieh's bat, and Sumbi's crowbar. With her own hair each costs a heart; in brown
        // each is turned aside, where it met her, and she is not hurt; and standing against whoever waits does not hurt her either.
        for (int[] what : new int[][]{{24, 71, PlusRun.BATTED}, {55, 242, PlusRun.BATTED}}) {
            for (boolean brown : new boolean[]{false, true}) {
                PlusRun fight = shutIn(what[0], true);
                PlusRun.Foe foe = waiting(fight);
                until(fight, foe, what[1] % 10);
                if (brown) fight.lend(PlusRun.BROWN);
                int hurts = fight.hurts(), braces = fight.braces();
                String which = "land " + what[0] + (brown ? " in brown" : "");
                for (int step = 0; step < 60 * 4 && fight.hurts() == hurts && foe.mode == what[1] % 10 && foe.step < 2; step++) {
                    fight.bare();
                    fight.place(foe.leftward ? foe.x - 1 : foe.x + foe.wide() + .3f, floorOf(fight) - PlusRun.TALL);
                    fight.step(STEP, false, false, false);
                    fight.step(STEP, false, false, false);
                }
                assertEquals(which, brown ? 0 : 1, fight.hurts() - hurts);
                if (brown) assertTrue(which, fight.braces() > braces);
                else assertEquals(which, what[2], fight.cause());
                if (!brown) continue;
                hurts = fight.hurts();
                braces = fight.braces();
                for (int step = 0; step < 60 && !fight.open(foe); step++) {
                    fight.bare();
                    fight.place(foe.x + foe.wide() / 2 - PlusRun.WIDE / 2, foe.y + foe.tall() - PlusRun.TALL);
                    fight.step(STEP, false, false, false);
                }
                assertEquals(which, hurts, fight.hurts());
            }
        }
    }

    @Test public void inBrownATouchFromAnySideDealsWithEverySignThereIsAndCostsHerNothing() {
        int signs = 0;
        for (int kind = 0; kind < RunLevels.TARGET; kind++) {
            if (RunLevels.boss(kind)) continue;
            signs++;
            for (int form : new int[]{PlusRun.BROWN, PlusRun.WHITE}) {
                for (int side = 0; side < 4; side++) {
                    for (boolean big : new boolean[]{false, true}) {
                        RunLevels.Level level = flat(40);
                        level.foes.add(big ? new int[]{kind, 20, FLOOR - 1, 3} : new int[]{kind, 20, FLOOR - 1});
                        PlusRun run = new PlusRun(level);
                        run.lend(form);
                        PlusRun.Foe foe = run.foes().get(0);
                        String which = "sign " + kind + (big ? " big" : "") + " side " + side + " form " + form;
                        // From the left, the right, above and below: she is put against it, moving into it, and whatever is in the air is taken out of it.
                        for (int step = 0; step < 90 && !foe.flat; step++) {
                            for (PlusRun.Shot shot : run.shots()) shot.left = 0;
                            float across = side == 0 ? foe.x - PlusRun.WIDE + .12f : side == 1 ? foe.x + foe.wide() - .12f : foe.x + foe.wide() / 2 - PlusRun.WIDE / 2;
                            float down = side == 2 ? foe.y - PlusRun.TALL + .12f : side == 3 ? foe.y + foe.tall() - .12f : foe.y + foe.tall() - PlusRun.TALL;
                            run.place(across, down);
                            run.step(STEP, side == 1, side == 0, false);
                        }
                        assertTrue(which + " still stands", foe.flat);
                        assertEquals(which, 0, run.hurts());
                        assertEquals(which, PlusRun.HEARTS * 2, run.halves());
                        assertEquals(which, 1, run.flattened());
                    }
                }
            }
        }
        assertEquals(11, signs);
    }

    @Test public void aStallHasOnlySoMuchOfAThingAndSellsNoRibbonWhileSheWearsAColour() {
        RunLevels.Level level = flat(120);
        level.items.add(new int[]{PlusRun.STALL, 20, FLOOR - 1, RunLevels.ALTTEUL});
        level.items.add(new int[]{PlusRun.STALL, 80, FLOOR - 1, RunLevels.ALTTEUL});
        for (int column = 3; column < 19; column++) for (int row = FLOOR - 4; row < FLOOR; row++) level.pluses.add(new int[]{column, row});
        PlusRun.Perks drawing = new PlusRun.Perks();
        drawing.reach = 6;
        PlusRun run = new PlusRun(level, drawing);
        hold(run, 3.2f, false, true, false);
        assertTrue(run.atStall());
        assertTrue("purse " + run.purse(), run.purse() > 50);
        int ribbon = PlusRun.FORM + PlusRun.GREEN, other = PlusRun.FORM + PlusRun.RED;
        // One ribbon: and no other while she wears it, nor when it is gone, since this stall had the one.
        assertEquals(PlusRun.SOLD, run.refusal(ribbon, 20));
        assertTrue(run.buy(ribbon, 20));
        assertEquals(PlusRun.GREEN, run.form());
        assertEquals(1, run.stallSold(ribbon));
        int purse = run.purse();
        assertEquals(PlusRun.SOLD_OUT, run.refusal(other, 20));
        assertFalse(run.buy(other, 20));
        assertEquals(PlusRun.GREEN, run.form());
        assertEquals(purse, run.purse());
        while (run.form() != 0) run.step(STEP, false, false, false);
        assertFalse(run.buy(ribbon, 20));
        assertEquals(0, run.form());
        // The wait to call on a colour began when that one went, as it does for any.
        assertTrue(run.untilCall(PlusRun.GREEN) > 9);
        // Bread is not sold to her while her hearts are full, nor ink while she has a ball of it, nor ice cream while she is full of it.
        assertEquals(PlusRun.NEEDLESS, run.refusal(PlusRun.BREAD, 12));
        assertTrue(run.buy(PlusRun.SHIELD, 15));
        assertEquals(PlusRun.SOLD_OUT, run.refusal(PlusRun.SHIELD, 15));
        assertTrue(run.buy(PlusRun.ICE, 8));
        assertEquals(PlusRun.NEEDLESS, run.refusal(PlusRun.ICE, 8));
        // The next stall has its own: a ribbon again, but not while a colour is on.
        run.place(79, GROUND);
        hold(run, .3f, false, false, false);
        assertTrue(run.atStall());
        assertEquals(0, run.stallSold(ribbon));
        run.lend(PlusRun.RED);
        assertEquals(PlusRun.WORN, run.refusal(ribbon, 20));
        assertFalse(run.buy(ribbon, 20));
        while (run.form() != 0) run.step(STEP, false, false, false);
        if (run.purse() >= 20) {
            assertTrue(run.buy(ribbon, 20));
            assertEquals(PlusRun.SOLD_OUT, run.refusal(ribbon, 20));
        } else {
            assertEquals(PlusRun.POOR, run.refusal(ribbon, 20));
        }
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

    @Test public void blackPutOnHerTakesEveryColourAndLetsHerWearNoneForTwiceARibbonsTime() {
        RunLevels.Level level = flat(80);
        level.items.add(new int[]{PlusRun.FORM + PlusRun.GREEN, 6, FLOOR - 1, 0});
        level.items.add(new int[]{PlusRun.FORM + PlusRun.BLACK, 10, FLOOR - 1, 0});
        level.items.add(new int[]{PlusRun.FORM + PlusRun.RED, 14, FLOOR - 1, 0});
        level.foes.add(new int[]{RunLevels.MINUS, 60, FLOOR - 1});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.colours = 1 << PlusRun.BLUE - 1;
        PlusRun run = new PlusRun(level, perks), plain = new PlusRun(flat(80));
        run.place(5.8f, GROUND);
        hold(run, .1f, false, false, false);
        assertTrue(run.call(PlusRun.BLUE));
        assertTrue(run.has(PlusRun.GREEN) && run.has(PlusRun.BLUE));
        // A black ribbon: every colour she wore is gone, a ribbon's and her own alike, and black lies on her for twice a ribbon's time.
        run.place(9.8f, GROUND);
        hold(run, .1f, false, false, false);
        assertEquals(1, run.curses());
        assertEquals(0, run.mix());
        assertFalse(run.coloured());
        assertEquals(PlusRun.BLACK, run.form());
        assertEquals(2 * PlusRun.RIBBON, PlusRun.BLACKENED, 0);
        assertEquals(PlusRun.BLACKENED, run.cursedFor(), 0);
        assertTrue(run.cursed() > PlusRun.BLACKENED - .2f);
        // Under it she can call on nothing, and picks up no ribbon: the red one stays where it lies.
        assertFalse(run.callable(PlusRun.BLUE));
        assertEquals(0, run.ready());
        assertEquals(0, run.wear(2));
        int lying = run.items().size();
        run.place(13.8f, GROUND);
        hold(run, .3f, false, false, false);
        assertEquals(lying, run.items().size());
        assertFalse(run.has(PlusRun.RED));
        // It weighs on her: she is slower under it; and what she comes down on is still dealt with.
        run.place(30, GROUND);
        plain.place(30, GROUND);
        hold(run, 1, false, true, false);
        hold(plain, 1, false, true, false);
        assertTrue(run.x() < plain.x() - .3f);
        assertTrue(stomp(run, run.foes().get(0)));
        // Twice a ribbon's time after it was put on her, it is gone; and then the ribbon is hers to take.
        float gone = 0;
        while (run.cursed() > 0 && gone < 40) {
            run.place(40, GROUND);
            run.step(STEP, false, false, false);
            gone += STEP;
        }
        assertEquals(PlusRun.BLACKENED - 1.5f, gone, .5f);
        assertEquals(0, run.form());
        run.place(13.8f, GROUND);
        hold(run, .1f, false, false, false);
        assertTrue(run.has(PlusRun.RED));
        // What the Mawang gives is that it lies on her half as long: a ribbon's time; and black is no colour of hers to wear or to call on.
        PlusRun given = new PlusRun(flat(20));
        given.gift(PlusRun.BLACK);
        assertEquals(0, given.form());
        assertEquals(0, given.mix());
        assertFalse(given.callable(PlusRun.BLACK));
        assertFalse(given.call(PlusRun.BLACK));
        given.lend(PlusRun.BLACK);
        assertEquals(PlusRun.BLACK, given.form());
        assertEquals(PlusRun.RIBBON, given.cursedFor(), 0);
        PlusRun.Perks all = new PlusRun.Perks();
        all.colours = 127;
        all.together = 7;
        PlusRun every = new PlusRun(flat(20), all);
        assertEquals(63, every.callAll(127));
        assertFalse(every.has(PlusRun.BLACK));
        assertFalse(every.shadow());
        assertEquals(63, PlusRun.most(127, 127, 7));

        // Black that is thrown, from the fifty-first land on, by an asterisk: every third thing it fires. In purple it goes back; in brown it
        // lies on her half as long.
        for (int form : new int[]{0, PlusRun.BROWN, PlusRun.PURPLE}) {
            RunLevels.Level late = new RunLevels.Level(RunLevels.CURSES_FROM, 80);
            for (int column = 0; column < 80; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) late.land[row][column] = RunLevels.BLOCK;
            late.gate = 77;
            late.foes.add(new int[]{RunLevels.SHOOTER, 30, FLOOR - 4});
            PlusRun cursed = new PlusRun(late);
            PlusRun.Foe star = cursed.foes().get(0);
            java.util.List<Integer> fired = new java.util.ArrayList<>();
            boolean longer = false;
            for (int step = 0; step < 60 * 30 && cursed.curses() == 0 && cursed.turned() == 0; step++) {
                if (form != 0 && cursed.on(form) < 3) cursed.lend(form);
                if (cursed.shots().isEmpty()) cursed.place(star.x - 3.5f, GROUND);
                for (PlusRun.Shot shot : new java.util.ArrayList<>(cursed.shots())) {
                    if (shot.age == 0 || shot.age <= STEP * 1.5f) fired.add(shot.kind);
                    // What is not black she is kept out of the way of; what is, she waits for.
                    if (shot.kind != PlusRun.CURSE) cursed.shots().remove(shot);
                }
                longer |= star.mode == 1 && star.step == 1 && star.act > .6f;
                cursed.bare();
                cursed.step(STEP, false, false, false);
            }
            assertTrue("form " + form + " fired " + fired, fired.size() >= 3 && fired.get(0) == PlusRun.DOT && fired.get(1) == PlusRun.DOT && fired.get(2) == PlusRun.CURSE);
            assertTrue(longer);
            assertEquals("form " + form, form == PlusRun.PURPLE ? 0 : 1, cursed.curses());
            assertEquals("form " + form, form == PlusRun.PURPLE ? 1 : 0, cursed.turned());
            assertEquals(0, cursed.hurts());
            if (form != PlusRun.PURPLE) assertEquals("form " + form, form == PlusRun.BROWN ? PlusRun.BLACKENED / 2 : PlusRun.BLACKENED, cursed.cursedFor(), 0);
        }
        // Before the fifty-first land it throws nothing black.
        PlusRun early = flown(RunLevels.SHOOTER, 30, 3);
        for (int step = 0; step < 60 * 14; step++) {
            early.place(26.5f, GROUND - 6);
            early.step(STEP, false, false, false);
            for (PlusRun.Shot shot : early.shots()) assertEquals(PlusRun.DOT, shot.kind);
        }
        assertEquals(0, early.curses());
    }

    @Test public void theButtonThatLetsHerDownDropsHerThroughThinLedgesAndNeverThroughGround() {
        // Three thin ledges one over another, of both kinds, over solid ground; and beside them a thin ledge over a pit.
        RunLevels.Level level = flat(60);
        for (int column = 10; column <= 16; column++) {
            level.land[FLOOR - 4][column] = RunLevels.LEDGE;
            level.land[FLOOR - 8][column] = RunLevels.THIN;
            level.land[FLOOR - 12][column] = RunLevels.LEDGE;
        }
        hole(level, 30, 36);
        for (int column = 30; column <= 36; column++) level.land[FLOOR - 4][column] = RunLevels.LEDGE;
        level.foes.add(new int[]{RunLevels.MINUS, 12, FLOOR - 13});
        PlusRun run = new PlusRun(level);
        float top = FLOOR - 12 - PlusRun.TALL;
        run.place(14, top - .2f);
        hold(run, .5f, false, false, false);
        assertTrue(run.onGround());
        assertEquals(top, run.y(), .01f);
        assertTrue(run.droppable());
        assertEquals(0, run.drops());
        // A press: she goes down through the one she stands on, and is held by the next.
        run.down(true);
        hold(run, .12f, false, false, false);
        run.down(false);
        hold(run, 1, false, false, false);
        assertEquals(1, run.drops());
        assertEquals(FLOOR - 8 - PlusRun.TALL, run.y(), .01f);
        assertTrue(run.onGround() && run.droppable());
        // Held: through every thin ledge she meets, down to the ground, which holds her whatever is held.
        run.down(true);
        hold(run, 2, false, false, false);
        assertEquals(3, run.drops());
        assertEquals(GROUND, run.y(), .01f);
        assertTrue(run.onGround());
        assertFalse(run.droppable());
        hold(run, 1, false, false, false);
        assertEquals(GROUND, run.y(), .01f);
        assertEquals(PlusRun.PLAYING, run.state());
        assertEquals(0, run.hurts());
        // What walks on a ledge does not go down with her.
        PlusRun.Foe walker = run.foes().get(0);
        assertEquals(FLOOR - 12, walker.y + walker.tall(), .01f);
        // Over a pit a ledge holds her, held down or not: there is nothing under it to come down on.
        run.down(false);
        run.place(33, FLOOR - 4 - PlusRun.TALL - .2f);
        hold(run, .5f, false, false, false);
        assertFalse(run.droppable());
        run.down(true);
        hold(run, 1.5f, false, false, false);
        assertEquals(FLOOR - 4 - PlusRun.TALL, run.y(), .01f);
        assertEquals(3, run.drops());
        // In the air it brings her down faster than she falls; and, with the wind in her hair, down all the same.
        PlusRun slow = new PlusRun(flat(60)), fast = new PlusRun(flat(60));
        slow.place(20, GROUND - 14);
        fast.place(20, GROUND - 14);
        fast.down(true);
        hold(slow, .6f, false, false, false);
        hold(fast, .6f, false, false, false);
        assertTrue(fast.y() > slow.y() + 1.5f);
        assertTrue(fast.speedDown() > slow.speedDown() * 1.3f || fast.onGround());
        PlusRun flier = new PlusRun(flat(60));
        flier.lend(PlusRun.GREEN);
        flier.place(20, GROUND - 6);
        hold(flier, 1, false, false, true);
        assertFalse(flier.onGround());
        flier.down(true);
        hold(flier, 1.2f, false, false, true);
        assertTrue(flier.onGround());
        // Coming down hard on something that takes two blows deals with it at once.
        RunLevels.Level tough = flat(60);
        tough.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1, 2});
        for (boolean hard : new boolean[]{false, true}) {
            PlusRun stomper = new PlusRun(tough);
            PlusRun.Foe big = stomper.foes().get(0);
            stomper.down(hard);
            for (int step = 0; step < 240 && stomper.flattened() == 0 && stomper.hurts() == 0 && big.left == 2 * PlusRun.BLOW; step++) {
                if (stomper.onGround() || step == 0) stomper.place(big.x + big.wide() / 2 - PlusRun.WIDE / 2, big.y - 9);
                stomper.step(STEP, false, false, false);
            }
            assertEquals("hard " + hard, hard ? 1 : 0, stomper.flattened());
            assertEquals(0, stomper.hurts());
        }
    }

    @Test public void whateverIsBeatenIsDownAndHurtsNoOneAndIsNoLongerCounted() {
        // Every sign that walks, hops, shoots or flies in either volume, and the paper target: beaten, each is down, however it was beaten.
        int[] kinds = {RunLevels.MINUS, RunLevels.TIMES, RunLevels.DIVIDE, RunLevels.THROWER, RunLevels.GUNNER, RunLevels.SLIME, RunLevels.DRAGON, RunLevels.BAR,
                RunLevels.FAN, RunLevels.WORD, RunLevels.POGO, RunLevels.DRIFTER, RunLevels.SWOOPER, RunLevels.BOMBER, RunLevels.SHOOTER, RunLevels.MITE, RunLevels.WISP,
                RunLevels.RIME};
        for (int kind : kinds) {
            for (int size = 1; size <= 2; size++) {
                RunLevels.Level level = flat(60);
                // (The second time round it is one that takes three blows, as the outsized ones of a cellar do.)
                level.foes.add(size == 1 ? new int[]{kind, 20, FLOOR - 1} : new int[]{kind, 20, FLOOR - 1, 3});
                PlusRun run = new PlusRun(level);
                PlusRun.Foe foe = run.foes().get(0);
                run.lend(PlusRun.BROWN);
                String what = "kind " + kind + " size " + size;
                assertFalse(what, foe.flat);
                run.place(4, GROUND);
                run.step(STEP, false, false, false);
                touch(run, foe);
                assertTrue(what, foe.flat);
                assertEquals(what, 1, run.flattened());
                assertEquals(what, 0, run.hurts());
                // Down, it hurts no one who stands in it, with nothing to keep her from harm; nothing more comes from it; and the time since it went down is kept.
                float since = foe.act;
                int thrown = run.thrown();
                for (int step = 0; step < 240; step++) {
                    run.bare();
                    if (run.has(PlusRun.BROWN)) run.drop(PlusRun.BROWN);
                    run.place(foe.x + foe.wide() / 2 - PlusRun.WIDE / 2, foe.y + foe.tall() - PlusRun.TALL);
                    run.shots().clear();
                    run.step(STEP, false, false, false);
                }
                assertEquals(what, 0, run.hurts());
                assertEquals(what, thrown, run.thrown());
                assertTrue(what, foe.flat && foe.act > since + 3.5f);
                assertEquals(what, 1, run.flattened());
            }
        }
        // And a times is beaten by each of the ways there are: fire, water, something thrown, something swung, the notes, and brown.
        for (int way = 0; way < 5; way++) {
            RunLevels.Level level = flat(60);
            level.foes.add(new int[]{RunLevels.TIMES, 14, FLOOR - 1});
            if (way == 4) level.items.add(new int[]{PlusRun.NOTES, 3, FLOOR - 1, 0});
            PlusRun run = new PlusRun(level);
            PlusRun.Foe times = run.foes().get(0);
            if (way == 0) run.lend(PlusRun.RED);
            if (way == 1) run.lend(PlusRun.BLUE);
            if (way == 2) run.arm(PlusRun.PEN);
            if (way == 3) run.arm(PlusRun.SWORD);
            hold(run, .3f, false, true, false);
            for (int step = 0; step < 300 && !times.flat && run.hurts() == 0; step++) {
                if (way != 4) run.place(times.x - (way == 3 ? 1.1f : 3.5f), GROUND);
                else touch(run, times);
                run.step(STEP, false, true, false, step % 20 == 0);
            }
            assertTrue("way " + way, times.flat);
            assertEquals("way " + way, 0, run.hurts());
        }
    }

    @Test public void whiteHasEveryPowerOfItsColoursAndTheWhiteThatIsTrulyHersHasTheBeamBesides() {
        // In white, of ribbons or of her own colours, and in the white that is truly hers: every power by its name, whatever she carries.
        for (int kind = 0; kind < 2; kind++) {
            boolean hers = kind == 1;
            String what = hers ? "true white" : "white";
            // Fire and water, thrown as one with every strike: with nothing in her hands and with each thing she may carry.
            for (int weapon = PlusRun.UNARMED; weapon <= PlusRun.GUN; weapon++) {
                PlusRun run = new PlusRun(flat(80));
                if (hers) run.master();
                else run.lend(PlusRun.WHITE);
                if (weapon != PlusRun.UNARMED) run.arm(weapon);
                run.place(20, GROUND);
                hold(run, .2f, false, true, false);
                int casts = run.casts();
                run.step(STEP, false, false, false, true);
                assertEquals(what + " weapon " + weapon, casts + 1, run.casts());
                boolean steam = false;
                for (PlusRun.Shot shot : run.shots()) steam |= shot.kind == PlusRun.STEAM && shot.hers;
                assertTrue(what + " weapon " + weapon, steam);
            }
            PlusRun run = new PlusRun(flat(80));
            if (hers) run.master();
            else run.lend(PlusRun.WHITE);
            assertTrue(what, run.white());
            assertEquals(what, PlusRun.WHITE, run.form());
            for (int colour = PlusRun.RED; colour <= PlusRun.PURPLE; colour++) assertTrue(what + " " + colour, run.has(colour));
            assertFalse(what, run.has(PlusRun.BLACK));
            // Brown: a touch deals with a times, and costs her nothing.
            RunLevels.Level level = flat(80);
            level.foes.add(new int[]{RunLevels.TIMES, 30, FLOOR - 1});
            level.foes.add(new int[]{RunLevels.THROWER, 50, FLOOR - 1});
            for (int column = 60; column < 64; column++) level.land[FLOOR][column] = RunLevels.LAVA;
            for (int column = 66; column < 70; column++) level.land[FLOOR][column] = RunLevels.WATER;
            run = new PlusRun(level);
            if (hers) run.master();
            else run.lend(PlusRun.WHITE);
            touch(run, run.foes().get(0));
            assertTrue(what, run.foes().get(0).flat);
            assertEquals(what, 0, run.hurts());
            // Purple: what is thrown at her goes back.
            run.place(46, GROUND);
            for (int step = 0; step < 60 * 6 && run.turned() == 0; step++) run.step(STEP, false, false, false);
            assertTrue(what, run.turned() >= 1);
            assertEquals(what, 0, run.hurts());
            // Red and blue: lava and water hold her up.
            run.place(61, GROUND - .3f);
            hold(run, .6f, false, false, false);
            assertEquals(what, GROUND, run.y(), .05f);
            run.place(67, GROUND - .3f);
            hold(run, .6f, false, false, false);
            assertEquals(what, GROUND, run.y(), .05f);
            assertEquals(what, 0, run.hurts());
            // Green: she flies for as long as the jump is held. Yellow: she runs faster than she runs.
            PlusRun flier = new PlusRun(flat(120)), plain = new PlusRun(flat(120)), yellow = new PlusRun(flat(120));
            if (hers) flier.master();
            else flier.lend(PlusRun.WHITE);
            yellow.lend(PlusRun.YELLOW);
            hold(flier, 2, false, true, false);
            hold(plain, 2, false, true, false);
            hold(yellow, 2, false, true, false);
            assertTrue(what, flier.x() > plain.x() * 1.4f);
            // (The white that is truly hers is faster than yellow; the white of ribbons is as fast as yellow, and no faster.)
            if (hers) assertTrue(flier.x() > yellow.x() * 1.15f);
            else assertEquals(yellow.x(), flier.x(), .5f);
            float before = flier.y();
            hold(flier, 1.5f, false, false, true);
            assertTrue(what, flier.y() < before - 3 && !flier.onGround());
        }

        // The white of ribbons has a time, and no beam however long the strike is held; the white that is truly hers has no time.
        PlusRun lent = new PlusRun(flat(80));
        lent.lend(PlusRun.WHITE);
        for (int step = 0; step < 60 * 4; step++) lent.step(STEP, false, false, false, true);
        assertEquals(0, lent.beams());
        assertEquals(0, lent.beam(), 0);
        hold(lent, 10, false, false, false);
        assertFalse(lent.white());
        PlusRun hers = new PlusRun(flat(80));
        hers.master();
        hold(hers, 60, false, false, false);
        assertTrue(hers.white() && hers.mastered());
        // Black does not lie on her.
        hers.lend(PlusRun.BLACK);
        assertEquals(0, hers.cursed(), 0);
        assertTrue(hers.white());

        // The beam: gathered by holding the strike, it goes through a wall and a ledge and lands on everything along it, on both sides of
        // them; it lasts a moment, and waits before it can be again.
        RunLevels.Level level = flat(80);
        for (int row = FLOOR - 6; row < FLOOR; row++) level.land[row][26] = RunLevels.BLOCK;
        level.land[FLOOR - 1][30] = RunLevels.LEDGE;
        level.foes.add(new int[]{RunLevels.MINUS, 24, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.TIMES, 28, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 33, FLOOR - 1, 3});
        level.foes.add(new int[]{RunLevels.WISP, 31, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 12, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 45, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        run.master();
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(20, GROUND);
        hold(run, .2f, false, true, false);
        run.place(20, GROUND);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 0;
        // A press is not enough.
        run.step(STEP, false, false, false, true);
        hold(run, .2f, false, false, false);
        assertEquals(0, run.beams());
        for (int step = 0; step < 60 && run.beams() == 0; step++) {
            run.place(20, GROUND);
            run.step(STEP, false, false, false, true);
            assertTrue(run.beams() > 0 || run.beamCharge() > 0);
        }
        assertEquals(1, run.beams());
        assertTrue(run.beam() > 1 && !run.beamLeft());
        for (int step = 0; step < 60 * 2 && run.beam() > 0; step++) {
            run.place(20, GROUND);
            run.step(STEP, false, false, false, true);
        }
        // Those ahead of her within its reach are down, the one that takes three blows among them, and the wisp; the one behind her and
        // the one beyond its reach are not.
        for (int index = 0; index < 4; index++) assertTrue("foe " + index, run.foes().get(index).flat);
        assertFalse(run.foes().get(4).flat);
        assertFalse(run.foes().get(5).flat);
        assertTrue(run.beamWait() > 2);
        for (int step = 0; step < 60; step++) run.step(STEP, false, false, false, true);
        assertEquals(1, run.beams());
        hold(run, 2, false, false, false);
        for (int step = 0; step < 60; step++) run.step(STEP, false, false, false, true);
        assertEquals(2, run.beams());
        // One who waits behind her shield is not touched by it; spent, she is.
        PlusRun fight = shutIn(RunLevels.GUARD_LAND, true);
        fight.master();
        PlusRun.Foe guard = waiting(fight);
        int blows = fight.blows();
        for (int step = 0; step < 60 * 40 && fight.blows() == blows; step++) {
            fight.bare();
            fight.place(guard.x - 4, guard.y + guard.tall() - PlusRun.TALL);
            int was = fight.blows();
            boolean open = fight.open(guard);
            fight.step(STEP, false, true, false, true);
            if (fight.blows() > was) assertTrue(open || fight.open(guard) || guard.dazed > 0);
        }
        assertEquals(blows + 1, fight.blows());
    }

    private static int whole, base;

    private static String short_ = "";

    @Test public void everyLandHasFiveTimesTheCreaturesItWasLaidWithAndTheFirstThreeTwice() {
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int laid = 0, more = 0, packs = 0, was = -99, wasRow = -9, most = 0, together = 0;
            java.util.List<int[]> sorted = new java.util.ArrayList<>(level.foes);
            java.util.Collections.sort(sorted, (one, other) -> one[2] != other[2] ? one[2] - other[2] : one[1] * 2 + (RunLevels.crowd(one) ? one[4] : 0) - other[1] * 2 - (RunLevels.crowd(other) ? other[4] : 0));
            float tightest = 99;
            java.util.Set<Integer> kinds = new java.util.TreeSet<>();
            if (number == 4) {
                whole = 0;
                base = 0;
            }
            for (int[] foe : level.foes) {
                if (foe[1] >= level.main || RunLevels.boss(foe[0]) || RunLevels.flier(foe[0]) || foe[0] == RunLevels.TARGET || RunLevels.uniqueSpec(foe)) continue;
                if (!RunLevels.crowd(foe)) {
                    laid++;
                    kinds.add(foe[0]);
                }
            }
            for (int[] foe : sorted) {
                if (!RunLevels.crowd(foe) || RunLevels.flier(foe[0]) || RunLevels.swarmed(foe)) continue;
                more++;
                // Of a kind the land has already; standing on its ground or on a ledge, with room over it; clear of the start, of flags and of where the land ends.
                assertTrue("land " + number + " kind " + foe[0], kinds.contains(foe[0]) || foe[0] == RunLevels.MINUS);
                char under = level.land[foe[2] + 1][foe[1]];
                assertTrue("land " + number + " at " + foe[1], under == RunLevels.BLOCK || under == RunLevels.LEDGE);
                assertEquals("land " + number + " at " + foe[1], RunLevels.AIR, level.land[foe[2]][foe[1]]);
                assertTrue("land " + number + " at " + foe[1], foe[1] >= 24 && foe[1] < level.gate - 5);
                // (And well clear of one met in the middle of the land.)
                for (int[] other : level.foes) if (RunLevels.rover(other[0])) assertTrue("land " + number + " at " + foe[1], Math.abs(other[1] - foe[1]) >= 16);
                for (int flag : level.flags) assertTrue("land " + number + " at " + foe[1] + " by flag " + flag, Math.abs(flag - foe[1]) >= 6);
                for (int[] thing : level.decor) assertTrue("land " + number + " at " + foe[1] + " in a village", Math.abs(thing[1] - foe[1]) >= 8);
                // (A pack: each a square and a half from the last, or two where the last of it is a times.)
                int place = foe[1] * 2 + foe[4];
                if (place - was > 4 || foe[2] != wasRow) {
                    packs++;
                    together = 0;
                }
                most = Math.max(most, ++together);
                was = place;
                wasRow = foe[2];
                // None stands shoulder to shoulder with another: between any two there is a gap she fits in, with room to spare. And a
                // times, which cannot be landed on, has no other of its kind within five squares of it, and whoever is beside it is two
                // squares off: there is a way over it.
                float middle = foe[1] + (foe[4] == 2 ? 1 : .5f), wide = PlusRun.SMALL * (foe[0] == RunLevels.TIMES ? .85f : .9f);
                for (int[] other : level.foes) {
                    if (other == foe || RunLevels.flier(other[0]) || other[2] != foe[2] || other[1] >= level.main) continue;
                    if (foe[0] == RunLevels.TIMES && other[0] == RunLevels.TIMES) {
                        assertTrue("land " + number + " times at " + foe[1], Math.abs(other[1] + (RunLevels.crowd(other) && other[4] == 2 ? 1 : .5f) - middle) >= 5);
                    }
                    if (!RunLevels.crowd(other) || RunLevels.swarmed(other)) continue;
                    float apart = Math.abs(other[1] + (other[4] == 2 ? 1 : .5f) - middle);
                    assertTrue("land " + number + " at " + foe[1] + " apart " + apart, apart >= (foe[0] == RunLevels.TIMES || other[0] == RunLevels.TIMES ? 2 : 1.5f));
                    tightest = Math.min(tightest, apart - wide);
                }
            }
            assertTrue("land " + number + " gap " + tightest, tightest >= PlusRun.WIDE + .15f);
            assertTrue("land " + number + " together " + most, most <= (number <= 12 ? 3 : number <= RunLevels.COUNT ? 4 : 5));
            if (System.getenv("RUN_COUNT") != null && number > 3) short_ += (laid * 5 - laid - more > 0 ? " " + number + ":" + (laid + more) + "/" + laid * 5 : "");
            int times = RunLevels.crowdOf(number);
            // Twice in the first three lands; five times after; and one time more with every three levels of the land: ten times in the last.
            assertEquals("land " + number, number <= 3 ? 2 : 5 + (RunLevels.levelOf(number) - 1) / 3, times);
            if (number > 3) assertTrue(times >= 5 && times <= 11 && times >= RunLevels.crowdOf(number - 1));
            // So many times what it was laid with, where there is room for all of them: and never fewer than there were when every land
            // had five times, which was twice over at the least where someone waits at the end and the land is short.
            assertTrue("land " + number + " laid " + laid + " more " + more, more <= laid * (times - 1)
                    && more >= (RunLevels.bossOf(number) >= 0 || number <= 12 ? Math.min(laid, laid * (times - 1)) : laid * Math.min(4, times - 1) * 5 / 10));
            whole += laid + more;
            base += laid;
            // In packs, spread along the land: not all in one place.
            if (more >= 8) assertTrue("land " + number + " packs " + packs + " of " + more, packs >= more / 4 && packs <= more);
            // In the short lands of the first two chapters they come no more than three together.
            if (number <= 12 && more > 0) assertTrue("land " + number + " packs " + packs + " of " + more, packs * 3 >= more);
            // And there is more to fight with lying about: a thing for every forty squares or so, over what lies by the flags.
            int things = 0;
            for (int[] weapon : level.weapons) if (weapon[1] < level.main) things++;
            assertTrue("land " + number + " things " + things, things >= level.flags.size() + (level.gate - 60) / 60);
        }
        // Over all the lands from the fourth on, with room for them or not: more than four times what they were laid with.
        assertTrue("whole " + whole + " of " + base, whole * 10 >= base * 41);
        if (System.getenv("RUN_COUNT") != null) throw new AssertionError("CROWD: whole " + whole + " of " + base + " short:" + short_);
        // One of them keeps still while she is far off, and is about its business when she is near.
        RunLevels.Level level = flat(120);
        level.foes.add(new int[]{RunLevels.MINUS, 90, FLOOR - 1, 1, 1});
        level.foes.add(new int[]{RunLevels.MINUS, 14, FLOOR - 1, 1, 1});
        PlusRun run = new PlusRun(level);
        PlusRun.Foe far = run.foes().get(0), near = run.foes().get(1);
        assertEquals(16, PlusRun.WAKES, 0);
        float farAt = far.x, nearAt = near.x;
        hold(run, 2, false, false, false);
        assertEquals(farAt, far.x, 0);
        assertTrue(Math.abs(near.x - nearAt) > 1);
        run.place(80, GROUND);
        hold(run, 1, false, false, false);
        assertTrue(Math.abs(far.x - farAt) > .5f);
    }

    @Test public void theLandsHaveMoreCreaturesTheHigherTheirLevel() {
        // What each chapter had when every land had five times what it was laid with, and twice as many again in the air.
        // (The two chapters of the third volume are new: there is nothing they had.)
        // (The third chapter one fewer: one of its villages is a homestead now, and the land after it lies a little differently.)
        int[] had = {41, 190, 417, 657, 856, 1530, 1655, 1761, 1815, 2195, 0, 0}, has = new int[RunLevels.ALL_CHAPTERS];
        int ground = 0, air = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int[] foe : level.foes) {
                if (foe[1] >= level.main || RunLevels.boss(foe[0]) || foe[0] == RunLevels.TARGET) continue;
                has[RunLevels.chapterOf(number)]++;
                if (RunLevels.flier(foe[0])) air++;
                else ground++;
            }
        }
        int was = 0, is = 0;
        for (int chapter = 0; chapter < has.length; chapter++) {
            // No chapter has fewer; and from the fourth chapter on, whose lands are of the higher levels, every one has more.
            assertTrue("chapter " + chapter + " has " + has[chapter], has[chapter] >= had[chapter]);
            if (chapter >= 3) assertTrue("chapter " + chapter + " has " + has[chapter], has[chapter] > had[chapter]);
            was += had[chapter];
            is += has[chapter];
        }
        assertTrue("was " + was + " is " + is + ": ground " + ground + " air " + air, is >= was + 700);
        // Mostly in the air: on the ground there is no more room for them with room left to come down among them.
        assertTrue(air >= 2300);
    }

    @Test public void theCreaturesOfTheLandsAreFourFifthsTheSizeTheyWereAndWhatIsSeenIsWhatIsTouched() {
        assertEquals(.8f, PlusRun.SMALL, 0);
        // Those that walk, hop, throw, shoot and fly; not those who wait, nor those met in the middle of a land, nor a word that falls, nor a target.
        for (int kind = 0; kind <= RunLevels.RIME; kind++) {
            boolean keeps = RunLevels.boss(kind) || kind == RunLevels.TARGET || kind == RunLevels.WORD;
            assertEquals("kind " + kind, keeps ? 1 : PlusRun.SMALL, new PlusRun.Foe(kind).size, 0);
            assertEquals("kind " + kind, !keeps, PlusRun.little(kind));
        }
        RunLevels.Level level = flat(70);
        level.foes.add(new int[]{RunLevels.THROWER, 20, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.TIMES, 30, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 40, FLOOR - 1, 3});
        level.foes.add(new int[]{RunLevels.MINUS, 50, FLOOR - 1, 1, 2});
        level.foes.add(new int[]{RunLevels.THROWER, 58, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        PlusRun.Foe thrower = run.foes().get(0), times = run.foes().get(1), big = run.foes().get(2), along = run.foes().get(3), other = run.foes().get(4);
        // Each stands on the ground in the middle of its square, as wide and as tall as it is drawn.
        assertEquals(.8f * .8f, thrower.wide(), 1e-4f);
        assertEquals(.55f * .8f, thrower.tall(), 1e-4f);
        assertEquals(20.5f, thrower.x + thrower.wide() / 2, 1e-4f);
        assertEquals(FLOOR, thrower.y + thrower.tall(), 1e-4f);
        assertEquals(.85f * .8f, times.wide(), 1e-4f);
        assertEquals(.85f * .8f, times.tall(), 1e-4f);
        // An outsized one is what it was: its kind's old size and three tenths more.
        assertEquals(.8f * 1.3f, big.wide(), 1e-4f);
        // One put in to make up the numbers may stand half a square along.
        assertEquals(51, along.x + along.wide() / 2, 1e-4f);
        assertEquals(.8f * .8f, along.wide(), 1e-4f);
        // What is touched is what is seen: a hair clear of it nothing happens, and a hair into it costs a heart.
        run.place(thrower.x - PlusRun.WIDE - .03f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(0, run.hurts());
        run.place(thrower.x - PlusRun.WIDE + .03f, GROUND);
        hold(run, .1f, false, false, false);
        assertEquals(1, run.hurts());
        // Coming down onto one is forgiven a hair to either side, and no more than a hair.
        run = new PlusRun(level);
        thrower = run.foes().get(0);
        run.place(thrower.x + thrower.wide() + .16f, GROUND - 1.2f);
        hold(run, .5f, false, false, false);
        assertFalse(thrower.flat);
        assertEquals(0, run.hurts());
        run.place(thrower.x + thrower.wide() + .06f, GROUND - 1.2f);
        hold(run, .5f, false, false, false);
        assertTrue(thrower.flat);
        assertEquals(0, run.hurts());
        // Never onto a times, from anywhere.
        times = run.foes().get(1);
        run.place(times.x + times.wide() + .06f, GROUND - 1.2f);
        hold(run, .5f, false, false, false);
        assertFalse(times.flat);
        // And what she swings takes a small one from as far off as it took one of the old size.
        level = flat(70);
        level.foes.add(new int[]{RunLevels.THROWER, 20, FLOOR - 1});
        level.weapons.add(new int[]{PlusRun.SWORD, 10, FLOOR - 1});
        run = new PlusRun(level);
        thrower = run.foes().get(0);
        run.place(10, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.SWORD, run.weapon());
        // (Her blade reaches nine tenths of a square: the old one's near edge was at 20.1.)
        run.place(20.1f - .88f - PlusRun.WIDE, GROUND);
        run.step(STEP, false, true, false);
        run.step(STEP, false, false, false, true);
        hold(run, .3f, false, false, false);
        assertTrue(thrower.flat);
    }

    /** A land of flat ground with things to fight with lying on it, each at a column. */
    private static RunLevels.Level strewn(int... kindsAndColumns) {
        RunLevels.Level level = flat(120);
        for (int index = 0; index < kindsAndColumns.length; index += 2) level.weapons.add(new int[]{kindsAndColumns[index], kindsAndColumns[index + 1], FLOOR - 1});
        return level;
    }

    private static void strike(PlusRun run) {
        run.step(STEP, false, false, false, true);
        run.step(STEP, false, false, false, false);
    }

    @Test public void whatSheFindsIsKeptInHerBackpackAndSheChangesBetweenThemAtWill() {
        // A thing found is in her backpack and in her hands; another found, the first is still hers, and a swap goes between the two.
        PlusRun run = new PlusRun(strewn(PlusRun.PEN, 10, PlusRun.SWORD, 14, PlusRun.PEN, 18, PlusRun.GUN, 22, PlusRun.GUN, 26, PlusRun.BOOK, 30, PlusRun.BOOK, 34));
        assertEquals(PlusRun.UNARMED, run.weapon());
        assertEquals(0, run.owned());
        assertFalse(run.swap());
        run.place(10.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.PEN, run.weapon());
        assertEquals(1 << PlusRun.PEN, run.owned());
        assertEquals(1 << PlusRun.PEN, run.newly());
        assertEquals(1, run.finds());
        assertEquals(PlusRun.PEN, run.lastFound());
        run.place(14.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.SWORD, run.weapon());
        assertEquals(PlusRun.PEN, run.previous());
        assertEquals(1 << PlusRun.PEN | 1 << PlusRun.SWORD, run.owned());
        assertTrue(run.swap());
        assertEquals(PlusRun.PEN, run.weapon());
        assertTrue(run.swap());
        assertEquals(PlusRun.SWORD, run.weapon());
        assertEquals(2, run.switches());
        assertFalse(run.switchTo(PlusRun.GUN));
        assertFalse(run.switchTo(PlusRun.SWORD));
        assertTrue(run.usable(PlusRun.PEN) && !run.usable(PlusRun.GUN) && !run.usable(PlusRun.TABLET));
        // One she has already is not put in her hands again: what she holds is the stronger for it, for a while.
        assertEquals(0, run.boost(), 0);
        run.place(18.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.SWORD, run.weapon());
        assertEquals(2, run.finds());
        assertEquals(1, run.refills());
        assertEquals(PlusRun.PEN, run.lastRefill());
        assertEquals(RunArms.BOOSTED, run.boost(), .05f);
        assertEquals(3, run.picked());
        // The ink gun keeps its own ink: found, a full tank; fired empty, it is put away and cannot be taken up; ink found fills it by so much.
        run.place(22.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.GUN, run.weapon());
        assertEquals(PlusRun.GUN_SHOTS, run.rounds());
        assertEquals(RunArms.holds(RunArms.GUN, PlusRun.GUN_SHOTS), run.rounds());
        for (int step = 0; step < 60 * 6 && run.weapon() == PlusRun.GUN; step++) run.step(STEP, false, false, false, true);
        assertEquals(PlusRun.UNARMED, run.weapon());
        assertEquals(0, run.rounds());
        assertFalse(run.usable(PlusRun.GUN));
        assertFalse(run.switchTo(PlusRun.GUN));
        run.place(26.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(RunArms.INK, run.rounds());
        assertEquals(PlusRun.UNARMED, run.weapon());
        assertTrue(run.switchTo(PlusRun.GUN));
        // Books: so many to a land; a find of more gives two.
        run.place(30.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.BOOK, run.weapon());
        assertEquals(RunArms.BOOKS, run.books());
        hold(run, 1, false, false, false);
        for (int thrown = 0; thrown < RunArms.BOOKS; thrown++) {
            assertEquals(PlusRun.BOOK, run.weapon());
            strike(run);
            assertEquals("throw " + thrown, RunArms.BOOKS - thrown - 1, run.books());
            hold(run, RunArms.rest(RunArms.BOOK, 0) + .05f, false, false, false);
        }
        assertEquals(0, run.books());
        assertEquals(PlusRun.UNARMED, run.weapon());
        run.place(34.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(RunArms.PAGES, run.books());
        assertTrue(run.switchTo(PlusRun.BOOK));

        // What is kept from other lands is with her from the start, what she begins a land with is in her hands, and a thing that runs
        // out leaves her holding that.
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.pack = 1 << PlusRun.PEN | 1 << PlusRun.GUN;
        perks.main = PlusRun.TABLET;
        run = new PlusRun(strewn(PlusRun.PEN, 10, PlusRun.KEYS, 14), perks);
        assertEquals(PlusRun.TABLET, run.weapon());
        assertEquals(1 << PlusRun.PEN | 1 << PlusRun.GUN | 1 << PlusRun.TABLET, run.owned());
        assertEquals(0, run.newly());
        assertTrue(run.switchTo(PlusRun.GUN));
        assertEquals(PlusRun.GUN_SHOTS, run.rounds());
        for (int step = 0; step < 60 * 6 && run.weapon() == PlusRun.GUN; step++) run.step(STEP, false, false, false, true);
        assertEquals(PlusRun.TABLET, run.weapon());
        // Where a pen lay there is still something for her: not a pen again, but a while of what she holds being stronger.
        run.place(10.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.TABLET, run.weapon());
        assertEquals(0, run.finds());
        assertEquals(1, run.refills());
        assertTrue(run.boost() > 0);
        // And a ring of keys found is hers as anything else is.
        run.place(14.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.KEYS, run.weapon());
        assertEquals(1 << PlusRun.KEYS, run.newly());
        assertTrue(RunArms.main(RunArms.KEYS) && RunArms.main(RunArms.TABLET) && !RunArms.main(RunArms.GUN));
    }

    @Test public void whatEachThingDoesIsWhatItsCardSaysAndTheFiguresAreTheTables() {
        // What is thrown: as fast as the table says, and for as long as takes it as far as the table says.
        for (int weapon : new int[]{PlusRun.PEN, PlusRun.KNIFE, PlusRun.GUN}) {
            PlusRun run = new PlusRun(flat(120));
            run.place(20, GROUND);
            run.arm(weapon);
            run.step(STEP, false, false, false, true);
            assertEquals("weapon " + weapon, 1, run.shots().size());
            PlusRun.Shot shot = run.shots().get(0);
            assertEquals("weapon " + weapon, RunArms.speed(weapon), shot.across, .001f);
            float started = shot.x - shot.across * STEP;
            while (!run.shots().isEmpty()) run.step(STEP, false, false, false);
            assertEquals("weapon " + weapon, RunArms.reach(weapon, 0), shot.x - started, RunArms.speed(weapon) * STEP * 2);
        }
        // Each rests for as long as the table says: no sooner is it used again.
        for (int weapon = 0; weapon < RunArms.COUNT; weapon++) {
            PlusRun run = new PlusRun(flat(120));
            run.place(20, GROUND);
            run.arm(weapon);
            strike(run);
            int strikes = run.strikes();
            hold(run, RunArms.rest(weapon, 0) - .08f, false, false, false);
            strike(run);
            assertEquals("weapon " + weapon, strikes, run.strikes());
            hold(run, .1f, false, false, false);
            strike(run);
            assertEquals("weapon " + weapon, strikes + 1, run.strikes());
            assertEquals(1 / RunArms.rest(weapon, 0), RunArms.pace(weapon, 0), 0);
            assertFalse(RunArms.does(weapon).isEmpty() || RunArms.goodFor(weapon).isEmpty() || RunArms.NAMES[weapon].isEmpty());
        }
        // What is swung takes what is within its reach, and nothing a little beyond it.
        for (int weapon : new int[]{PlusRun.BRUSH, PlusRun.SWORD}) {
            for (int beyond = 0; beyond < 2; beyond++) {
                RunLevels.Level level = flat(60);
                level.foes.add(new int[]{RunLevels.THROWER, 30, FLOOR - 1});
                PlusRun run = new PlusRun(level);
                PlusRun.Foe foe = run.foes().get(0);
                // (A small one is taken from as far off as one of the old size: an eighth of its old width is spare.)
                float edge = foe.x - .8f * .5f * (1 - PlusRun.SMALL);
                run.arm(weapon);
                run.place(edge - RunArms.reach(weapon, 0) - PlusRun.WIDE + (beyond == 0 ? .05f : -.08f), GROUND);
                run.step(STEP, false, true, false);
                strike(run);
                hold(run, .3f, false, false, false);
                assertEquals("weapon " + weapon + " beyond " + beyond, beyond == 0, foe.flat);
            }
        }
        // What each takes off a shield is the table's: tried on one of those who wait, standing in the open with a shield of twenty pieces.
        float[] took = new float[RunArms.COUNT];
        for (int weapon : new int[]{PlusRun.PEN, PlusRun.KNIFE, PlusRun.SWORD, PlusRun.BRUSH, PlusRun.GUN, PlusRun.TABLET, PlusRun.KEYS}) {
            RunLevels.Level level = flat(80);
            level.foes.add(new int[]{RunLevels.GUARD, 30, FLOOR - 1});
            PlusRun run = new PlusRun(level);
            PlusRun.Foe guard = run.foes().get(0);
            guard.shield = guard.shieldMost = 20;
            run.arm(weapon);
            run.place(guard.x - (RunArms.way(weapon) == RunArms.SWUNG ? .6f : 3) - PlusRun.WIDE, GROUND);
            run.step(STEP, false, true, false);
            run.step(STEP, false, false, false, true);
            for (int step = 0; step < 40 && guard.shield == 20; step++) run.step(STEP, false, false, false);
            took[weapon] = 20 - guard.shield;
            assertEquals("weapon " + weapon, RunArms.bite(weapon, 0), took[weapon], .001f);
        }
        assertTrue(took[PlusRun.SWORD] > took[PlusRun.PEN] && took[PlusRun.PEN] > took[PlusRun.GUN]);
    }

    @Test public void theTabletBookFlicksAPageOutAndBackAndOpensAPanelWhenHeld() {
        // A tap: a page, as far as the table says and back to where it left her, through a row of them.
        RunLevels.Level level = flat(120);
        level.foes.add(new int[]{RunLevels.TIMES, 23, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.TIMES, 24, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.TIMES, 29, FLOOR - 1});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        PlusRun run = new PlusRun(level, perks);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 0;
        run.place(20, GROUND);
        run.step(STEP, false, true, false);
        assertEquals(PlusRun.TABLET, run.weapon());
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.shots().size());
        PlusRun.Shot page = run.shots().get(0);
        assertEquals(PlusRun.PAGE, page.kind);
        float from = page.x - page.across * STEP, furthest = from, last = from;
        for (int step = 0; step < 90 && !run.shots().isEmpty(); step++) {
            for (PlusRun.Foe foe : run.foes()) if (!foe.flat) foe.x = foe.home;
            run.step(STEP, false, false, false);
            furthest = Math.max(furthest, page.x);
            last = page.x;
        }
        assertEquals(RunArms.reach(RunArms.TABLET, 0), furthest - from, .5f);
        assertTrue("back to " + (last - from), Math.abs(last - from) < 1.2f);
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);
        assertFalse(run.foes().get(2).flat);
        assertEquals(0, run.panels());
        // Held: after a moment a panel opens in front of her, once, and takes all that stands in it, and nothing beyond it; and then
        // the tablet-book rests the longer.
        level = flat(120);
        level.foes.add(new int[]{RunLevels.THROWER, 21, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.THROWER, 22, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.THROWER, 26, FLOOR - 1});
        run = new PlusRun(level, perks);
        for (PlusRun.Foe foe : run.foes()) foe.wait = 99;
        run.place(20.3f - PlusRun.WIDE, GROUND);
        run.step(STEP, false, true, false);
        float held = 0;
        while (run.panels() == 0 && held < 2) {
            run.shots().clear();
            run.step(STEP, false, false, false, true);
            held += STEP;
        }
        assertEquals(RunArms.CHARGE, held, .05f);
        assertEquals(1, run.panels());
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);
        assertFalse(run.foes().get(2).flat);
        hold(run, 1, false, false, false);
        for (int step = 0; step < 60; step++) run.step(STEP, false, false, false, true);
        assertEquals(2, run.panels());
        // It rests after: a tap at once does nothing; when it has rested, it flicks a page.
        run.step(STEP, false, false, false, false);
        run.shots().clear();
        strike(run);
        assertTrue(run.shots().isEmpty());
        hold(run, RunArms.heavyRest(RunArms.TABLET), false, false, false);
        strike(run);
        assertEquals(1, run.shots().size());
        // The panel takes two pieces off a shield, and wipes away what is thrown into it.
        level = flat(80);
        level.foes.add(new int[]{RunLevels.GUARD, 30, FLOOR - 1});
        run = new PlusRun(level, perks);
        PlusRun.Foe guard = run.foes().get(0);
        guard.shield = guard.shieldMost = 20;
        run.place(guard.x - 1 - PlusRun.WIDE, GROUND);
        run.step(STEP, false, true, false);
        for (int step = 0; step < 60 && run.panels() == 0; step++) {
            run.shots().clear();
            run.step(STEP, false, false, false, true);
        }
        assertEquals(1, run.panels());
        // (A page was flicked at the press, and took its piece before it was cleared away or after: one or none.)
        assertTrue("took " + (20 - guard.shield), 20 - guard.shield >= RunArms.PANEL_BITE && 20 - guard.shield <= RunArms.PANEL_BITE + RunArms.bite(RunArms.TABLET, 0));
    }

    @Test public void aKeyLeavesWhatItSticksInUnlockedAndTheRingGoesOutInAFanAndOneFailsNowAndThen() {
        RunLevels.Level level = flat(120);
        level.foes.add(new int[]{RunLevels.MINUS, 24, FLOOR - 1, 4});
        PlusRun run = new PlusRun(level);
        PlusRun.Foe big = run.foes().get(0);
        run.arm(PlusRun.KEYS);
        run.arm(PlusRun.PEN);
        run.arm(PlusRun.KEYS);
        run.place(20, GROUND);
        run.step(STEP, false, true, false);
        // A key: one blow, and it sticks: for a while the next blow that is not a key counts twice.
        run.step(STEP, false, false, false, true);
        assertEquals(PlusRun.THROWN_KEY, run.shots().get(0).kind);
        for (int step = 0; step < 60 && big.left == 4 * PlusRun.BLOW; step++) {
            big.x = big.home;
            run.step(STEP, false, false, false);
        }
        assertEquals(3 * PlusRun.BLOW, big.left);
        assertEquals(RunArms.UNLOCKS, big.unlocked, .1f);
        assertEquals(0, run.doubled());
        assertTrue(run.switchTo(PlusRun.PEN));
        hold(run, .3f, false, false, false);
        run.step(STEP, false, false, false, true);
        for (int step = 0; step < 60 && big.left == 3 * PlusRun.BLOW; step++) {
            big.x = big.home;
            run.step(STEP, false, false, false);
        }
        assertEquals(PlusRun.BLOW, big.left);
        assertEquals(1, run.doubled());
        assertEquals(0, big.unlocked, 0);
        // Held: the whole ring, in a fan.
        run = new PlusRun(flat(120));
        run.arm(PlusRun.KEYS);
        run.place(20, GROUND);
        for (int step = 0; step < 60 && run.fans() == 0; step++) {
            run.shots().clear();
            run.step(STEP, false, false, false, true);
        }
        assertEquals(1, run.fans());
        assertEquals(RunArms.FAN, run.shots().size());
        float highest = 0, lowest = 0;
        for (PlusRun.Shot key : run.shots()) {
            assertEquals(PlusRun.THROWN_KEY, key.kind);
            highest = Math.min(highest, key.down);
            lowest = Math.max(lowest, key.down);
        }
        assertTrue(highest < -1 && lowest > 1);
        // Now and then a key fails, and nothing leaves her hand: one throw in thirteen, never twice running, and the next is ready at once.
        run = new PlusRun(flat(120));
        run.arm(PlusRun.KEYS);
        run.place(20, GROUND);
        int thrown = 0;
        for (int throwing = 1; throwing < RunArms.FAILS_IN; throwing++) {
            run.shots().clear();
            strike(run);
            assertEquals("throw " + throwing, 1, run.shots().size());
            hold(run, RunArms.rest(RunArms.KEYS, 0), false, false, false);
            thrown++;
        }
        assertEquals(RunArms.FAILS_IN - 1, thrown);
        // (Twenty seconds must have gone by since the last that failed: the first is free of that.)
        run.shots().clear();
        strike(run);
        assertEquals(0, run.shots().size());
        assertEquals(1, run.duds());
        hold(run, .13f, false, false, false);
        strike(run);
        assertEquals(1, run.shots().size());
        for (int throwing = 0; throwing < RunArms.FAILS_IN * 2; throwing++) {
            hold(run, RunArms.rest(RunArms.KEYS, 0), false, false, false);
            strike(run);
        }
        assertEquals(1, run.duds());
    }

    @Test public void aBookBurstsWhereItLandsAndIsAFindAndNeverHurtsHer() {
        // No land has more than one book lying in it, and none where she starts; and most have none.
        int with = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            int books = 0;
            for (int[] weapon : RunLevels.level(number).weapons) {
                if (weapon[0] != PlusRun.BOOK) continue;
                books++;
                assertTrue("land " + number, weapon[1] >= 8);
            }
            assertTrue("land " + number + " books " + books, books <= 1);
            if (books > 0) with++;
        }
        assertTrue("with " + with, with >= 10 && with <= RunLevels.TOTAL / 2);

        // Thrown, it comes down a little way off, and bursts there: whatever stands within a little way of where it lands is dealt with,
        // on either side of it, the spiked times among them; what stands further off is not.
        RunLevels.Level level = armed(PlusRun.BOOK);
        PlusRun probe = new PlusRun(level);
        hold(probe, .4f, false, true, false);
        float from = probe.x();
        probe.place(from, GROUND);
        probe.step(STEP, false, false, false, true);
        for (int step = 0; step < 240 && probe.blasts() == 0; step++) probe.step(STEP, false, false, false);
        assertEquals(1, probe.blasts());
        float lands = probe.blastX(), reach = lands - from;
        assertTrue("reach " + reach, reach > 3 && reach < 12);
        assertEquals(FLOOR, probe.blastY(), 1.1f);
        assertEquals(0, probe.hurts());
        level = armed(PlusRun.BOOK);
        int at = Math.round(lands);
        level.foes.add(new int[]{RunLevels.TIMES, at + 1, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, at - 2, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, at + 6, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 0;
        hold(run, .4f, false, true, false);
        for (PlusRun.Foe foe : run.foes()) foe.x = foe.home;
        run.place(from, GROUND);
        run.step(STEP, false, false, false, true);
        for (int step = 0; step < 240 && run.blasts() == 0; step++) {
            for (PlusRun.Foe foe : run.foes()) if (!foe.flat) foe.x = foe.home;
            run.step(STEP, false, false, false);
        }
        assertEquals(1, run.blasts());
        assertTrue(run.foes().get(0).flat);
        assertTrue(run.foes().get(1).flat);
        assertFalse(run.foes().get(2).flat);
        assertEquals(0, run.hurts());
        assertTrue(PlusRun.BLAST >= 2 && PlusRun.BLAST <= 3);
        // It bursts under her own feet and does nothing to her.
        PlusRun close = new PlusRun(armed(PlusRun.BOOK));
        hold(close, .4f, false, true, false);
        for (int step = 0; step < 300 && close.blasts() == 0; step++) {
            close.step(STEP, false, false, false, step == 0);
            for (PlusRun.Shot shot : close.shots()) if (shot.kind == PlusRun.BOOK && shot.down > 0) close.place(shot.x - PlusRun.WIDE / 2, GROUND);
        }
        assertEquals(1, close.blasts());
        assertEquals(0, close.hurts());
        // It does nothing to one who waits behind her shield; spent, she takes it as a blow. And in red the fire still goes with the book.
        PlusRun fight = shutIn(RunLevels.GUARD_LAND, true);
        PlusRun.Foe guard = waiting(fight);
        fight.arm(PlusRun.BOOK);
        int blows = fight.blows(), blasts = 0;
        boolean shielded = false, spent = false;
        for (int step = 0; step < 60 * 120 && !(shielded && spent); step++) {
            fight.bare();
            fight.place(guard.x - reach + .5f, guard.y + guard.tall() - PlusRun.TALL);
            boolean open = fight.open(guard) && guard.dazed <= 0;
            int before = fight.blows(), burst = fight.blasts();
            fight.step(STEP, false, true, false, step % 40 == 0);
            fight.shots().removeIf(shot -> !shot.hers);
            if (fight.blasts() > burst && fight.blows() == before && !open) shielded = true;
            if (fight.blows() > before) spent = true;
        }
        assertTrue(shielded);
        assertTrue(spent);
        PlusRun red = new PlusRun(armed(PlusRun.BOOK));
        red.lend(PlusRun.RED);
        hold(red, .4f, false, true, false);
        red.step(STEP, false, false, false, true);
        boolean fire = false, book = false;
        for (PlusRun.Shot shot : red.shots()) {
            fire |= shot.kind == PlusRun.FIRE;
            book |= shot.kind == PlusRun.BOOK;
        }
        assertTrue(fire && book);
    }

    @Test public void whatIsBoughtWhileSheIsInALandIsHersThereAndThen() {
        PlusRun run = new PlusRun(flat(60));
        // With no wings, a second press in the air does nothing.
        run.step(STEP, false, false, true);
        hold(run, .25f, false, false, false);
        float before = run.speedDown();
        run.step(STEP, false, false, true);
        assertEquals(0, run.flaps());
        assertFalse(run.winged());
        hold(run, 1.5f, false, false, false);
        assertTrue(run.onGround());
        // Wings bought, at the counter by a stall, while she is in the land: the next jump has its second.
        PlusRun.Perks more = new PlusRun.Perks();
        more.wings = true;
        more.hearts = PlusRun.HEARTS + 1;
        more.lives = PlusRun.LIVES + 1;
        more.together = 3;
        more.shots = PlusRun.GUN_SHOTS + 8;
        more.colours = 1;
        run.grant(more);
        assertTrue(run.winged());
        run.step(STEP, false, false, true);
        hold(run, .25f, false, false, false);
        run.step(STEP, false, false, true);
        assertEquals(1, run.flaps());
        assertTrue(run.speedDown() < before);
        // And a heart more and a life more are there at once; and a colour, and how many she may wear.
        assertEquals(PlusRun.HEARTS + 1, run.hearts());
        assertEquals(PlusRun.LIVES + 1, run.lives());
        assertEquals(3, run.together());
        hold(run, 1.5f, false, false, false);
        assertTrue(run.call(PlusRun.RED));
        // Given again, nothing is given twice.
        run.grant(more);
        assertEquals(PlusRun.HEARTS + 1, run.hearts());
        assertEquals(PlusRun.LIVES + 1, run.lives());
    }

    @Test public void aShieldIsBrokenByBlowsAndNotWaitedOutAndFormsAgain() {
        for (int land : WAITED_IN) {
            // Left alone, she does all she does and is never bare: what she does herself does not open her.
            PlusRun idle = shutIn(land, true);
            PlusRun.Foe she = waiting(idle);
            for (int step = 0; step < 60 * 40; step++) {
                park(idle);
                // (One who flies through where she is kept, with the notes about her, runs her own shield onto them: that is a blow, and not her doing.)
                for (PlusRun.Foe foe : idle.foes()) if (RunLevels.boss(foe.kind)) assertTrue("land " + land, idle.cracks() > 0 || idle.guard(foe) != PlusRun.BARE && !idle.open(foe));
            }
            assertTrue("land " + land, idle.specials() >= 4);
            if (idle.cracks() == 0) {
                assertEquals("land " + land, 0, idle.shatters());
                assertEquals("land " + land, 0, idle.blows());
                assertEquals("land " + land, 1f, idle.shield(she), 0);
            }
            assertTrue("land " + land, idle.shatters() <= idle.cracks());

            // Her shield has as many pieces as its place in the story gives it; each plain blow takes one, and does nothing to her.
            PlusRun run = shutIn(land, true);
            PlusRun.Foe foe = waiting(run);
            int pieces = PlusRun.strength(foe.kind);
            assertTrue("land " + land + " pieces " + pieces, pieces >= 2 && pieces <= 6);
            for (int step = 0; step < 60 * 20 && !pressable(run, foe); step++) park(run);
            for (int piece = 0; piece < pieces; piece++) {
                assertEquals("land " + land, PlusRun.SHIELDED, run.guard(foe));
                assertEquals("land " + land, 1 - piece / (float) pieces, run.shield(foe), .001f);
                touch(run, foe);
                assertEquals("land " + land, piece + 1, run.cracks());
                assertEquals("land " + land, 0, run.blows());
            }
            // The last piece gone, it is broken: she reels, and is bare for a while; and does nothing while she is.
            assertEquals("land " + land, 1, run.shatters());
            assertEquals("land " + land, PlusRun.BARE, run.guard(foe));
            assertEquals("land " + land, run.opens(foe), run.bareLeft(foe), .05f);
            int did = run.specials();
            // A blow lands on her; and when the while is over, with her having done nothing in it, it is over.
            touch(run, foe);
            assertEquals("land " + land, 1, run.blows());
            for (int step = 0; step < 60 * 4 && run.open(foe); step++) {
                // (Of the twins the other is still at it.)
                if (foe.kind != RunLevels.MIRI && foe.kind != RunLevels.GARI) assertEquals("land " + land, did, run.specials());
                park(run);
            }
            park(run);
            // It forms again, piece by piece, and while it is forming nothing touches it or her.
            assertFalse("land " + land, run.open(foe));
            assertTrue("land " + land, run.forming(foe) && run.shield(foe) < .5f);
            int cracks = run.cracks(), blows = run.blows(), turned = run.parried();
            touch(run, foe);
            assertEquals("land " + land, cracks, run.cracks());
            assertEquals("land " + land, blows, run.blows());
            // (Turned aside, where she is there to be touched at all.)
            assertTrue("land " + land, run.parried() == turned + 1 || run.absent(foe));
            for (int step = 0; step < 60 * 5 && run.forming(foe); step++) park(run);
            assertEquals("land " + land, 1f, run.shield(foe), 0);
            assertEquals("land " + land, PlusRun.SHIELDED, run.guard(foe));
        }
        // Two blows are the most that land on her before it forms again, however much of the while is left.
        PlusRun twice = shutIn(RunLevels.COUNT, true);
        PlusRun.Foe zero = waiting(twice);
        for (int step = 0; step < 60 * 60 && twice.blows() < 2; step++) {
            if (pressable(twice, zero)) touch(twice, zero);
            else park(twice);
        }
        assertEquals(2, twice.blows());
        assertEquals(0, twice.phases());
        for (int step = 0; step < 240 && zero.dazed > 0; step++) park(twice);
        park(twice);
        assertTrue(twice.forming(zero) && !twice.open(zero));
        // Left unbroken for its while, a broken shield forms again all the same.
        PlusRun run = shutIn(RunLevels.GUARD_LAND, true);
        PlusRun.Foe guard = waiting(run);
        for (int step = 0; step < 60 * 20 && !pressable(run, guard); step++) park(run);
        for (int piece = 0; piece < PlusRun.strength(RunLevels.GUARD); piece++) touch(run, guard);
        assertEquals(PlusRun.BARE, run.guard(guard));
        for (int step = 0; step < 60 * 3; step++) park(run);
        assertFalse(run.open(guard));
        assertEquals(0, run.blows());
        // What is heavy takes more off it: a sword two pieces at a stroke; and what it is weak to, one more: fire on the Old Book's.
        assertEquals(PlusRun.FIRE, PlusRun.weak(RunLevels.OLD_BOOK));
        assertEquals(PlusRun.WATER, PlusRun.weak(RunLevels.MAWANG));
        assertEquals(0, PlusRun.weak(RunLevels.GUARD));
        PlusRun armed = shutIn(RunLevels.BUTLER_LAND, true);
        PlusRun.Foe butler = waiting(armed);
        armed.arm(PlusRun.SWORD);
        for (int step = 0; step < 60 * 20 && !pressable(armed, butler); step++) park(armed);
        float before = armed.shield(butler);
        for (int step = 0; step < 240 && armed.cracks() == 0; step++) {
            armed.place(butler.x - .8f, butler.y + butler.tall() - PlusRun.TALL);
            armed.step(STEP, false, true, false, step % 30 == 0);
        }
        assertEquals(1, armed.cracks());
        assertEquals(before - 2f / PlusRun.strength(RunLevels.BUTLER), armed.shield(butler), .01f);
        // In brown a touch only stops against it, as before: it takes nothing off.
        PlusRun brown = shutIn(RunLevels.GUARD_LAND, false);
        PlusRun.Foe stood = waiting(brown);
        for (int step = 0; step < 60 * 20 && !pressable(brown, stood); step++) park(brown);
        brown.lend(PlusRun.BROWN);
        touch(brown, stood);
        assertEquals(0, brown.cracks());
        assertEquals(1f, brown.shield(stood), 0);
        // In the last third a shield that breaks throws whoever broke it back.
        PlusRun late = shutIn(RunLevels.COUNT, true);
        PlusRun.Foe last = waiting(late);
        for (int step = 0; step < 60 * 400 && late.phase(last) < 2; step++) {
            if (pressable(late, last)) touch(late, last);
            else park(late);
        }
        assertEquals(2, late.phase(last));
        assertEquals(0, late.thrownBack());
        for (int step = 0; step < 60 * 60 && late.thrownBack() == 0; step++) {
            if (pressable(late, last) && !late.open(last)) touch(late, last);
            else park(late);
        }
        assertEquals(1, late.thrownBack());
        assertEquals(PlusRun.strength(RunLevels.ZERO) + 2, Math.round(last.shieldMost));
        assertTrue(late.opens(last) < 2 && late.forms(last) < 1.5f);
        // The Guild Guard and Penchok-jipsa, the first anyone meets, are as they began to the end: three pieces, as long a while, and no throwing back.
        for (int land : new int[]{RunLevels.GUARD_LAND, RunLevels.BUTLER_LAND}) {
            PlusRun first = shutIn(land, true);
            PlusRun.Foe early = waiting(first);
            assertEquals(3, PlusRun.strength(early.kind));
            float opens = first.opens(early), forms = first.forms(early);
            for (int step = 0; step < 60 * 400 && first.phase(early) < 2; step++) {
                if (pressable(first, early)) touch(first, early);
                else park(first);
            }
            assertEquals(2, first.phase(early));
            for (int step = 0; step < 60 * 20 && (first.absent(early) || first.forming(early) || first.open(early)); step++) park(first);
            assertEquals("land " + land, PlusRun.strength(early.kind), Math.round(early.shieldMost));
            assertEquals("land " + land, opens, first.opens(early), 0);
            assertEquals("land " + land, forms, first.forms(early), 0);
            for (int step = 0; step < 60 * 60 && early.left > 1; step++) {
                if (pressable(first, early)) touch(first, early);
                else park(first);
            }
            assertEquals("land " + land, 0, first.thrownBack());
        }
    }

    @Test public void aPetWaitsInEveryLandFromTheThirdAndKeepsBesideHerForTheWholeOfIt() {
        // Where they wait: none in the first two lands; from the third one in every land, on its ground, past its first flag; no two lands running the same kind; and every kind.
        java.util.Set<Integer> kinds = new java.util.TreeSet<>();
        int last = -1;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int waiting = 0;
            for (int[] item : level.items) {
                // (One that waits in a stash is another matter.)
                if (item[0] < PlusRun.PET || item[0] >= PlusRun.PET + PlusRun.PETS || item[1] >= level.main) continue;
                waiting++;
                assertEquals("land " + number, RunLevels.petOf(number), item[0] - PlusRun.PET);
                assertTrue("land " + number, item[1] > level.flags.get(0) && item[1] < level.flags.get(0) + 12 && item[1] < level.main);
                assertEquals("land " + number, RunLevels.top(level, item[1]) - 1, item[2]);
                kinds.add(item[0] - PlusRun.PET);
                assertTrue("land " + number, item[0] - PlusRun.PET != last);
                last = item[0] - PlusRun.PET;
            }
            assertEquals("land " + number, number < 3 ? 0 : 1, waiting);
        }
        // (Every kind that is had from the first: those of the ones she has beaten wait in no land.)
        assertEquals(RunPets.FOUND, kinds.size());

        // Touched, it goes with her: one that runs keeps on the ground behind her; one that floats, in the air at her shoulder.
        for (int kind = 0; kind < PlusRun.PETS; kind++) {
            RunLevels.Level level = flat(120);
            level.items.add(new int[]{PlusRun.PET + kind, 8, FLOOR - 1, 0});
            hole(level, 60, 62);
            PlusRun run = new PlusRun(level);
            assertEquals(-1, run.pet());
            run.place(7.8f, GROUND);
            hold(run, .2f, false, false, false);
            assertEquals(kind, run.pet());
            assertEquals(1, run.petJoined());
            hold(run, 3, false, true, false);
            hold(run, .6f, false, false, false);
            assertTrue("pet " + kind + " at " + run.petX() + " her " + run.x(), Math.abs(run.petX() - run.x()) < 2.5f && run.petX() < run.x() + .4f);
            if (PlusRun.floats(kind)) assertTrue("pet " + kind, run.petY() < FLOOR - .8f && run.petY() > FLOOR - 2);
            else assertEquals("pet " + kind, FLOOR, run.petY(), .05f);
            // Over a pit it is not left behind, nor lost in it.
            for (int step = 0; step < 60 * 12 && run.x() < 70; step++) run.step(STEP, false, true, run.x() > 59 && run.x() < 64);
            hold(run, 1, false, false, false);
            assertEquals("pet " + kind, 0, run.livesLost());
            assertTrue("pet " + kind + " at " + run.petX() + "," + run.petY(), Math.abs(run.petX() - run.x()) < 2.5f && run.petY() <= FLOOR + .05f);
            // Left far behind, it is beside her again.
            run.place(110, GROUND);
            run.step(STEP, false, false, false);
            assertTrue("pet " + kind, Math.abs(run.petX() - 110) < 2);
            // It is hers for the whole of the land: a life lost does not cost her it.
            PlusRun.Perks frail = new PlusRun.Perks();
            frail.hearts = 1;
            RunLevels.Level holed = flat(60);
            hole(holed, 20, 24);
            PlusRun fallen = new PlusRun(holed, frail);
            fallen.adopt(kind);
            fallen.place(22, 6);
            hold(fallen, 2.5f, false, false, false);
            assertEquals(1, fallen.livesLost());
            assertEquals(kind, fallen.pet());
            hold(fallen, .5f, false, false, false);
            assertTrue(Math.abs(fallen.petX() - fallen.x()) < 2.5f);
        }
        // In a cellar one waits by the things to fight with, for the first wave, if she has none.
        int[] which = new int[1];
        PlusRun cellar = atDoor(RunLevels.SHAPED_FROM, RunLevels.ARENA, false, which);
        int[] one = cellar.level().rooms.get(which[0]);
        assertTrue(cellar.enter());
        arrived(cellar);
        int waits = 0;
        for (int[] item : cellar.items()) if (item[0] >= PlusRun.PET && item[1] >= one[3] && item[1] <= one[4]) waits++;
        assertEquals(1, waits);
    }

    @Test public void eachPetGoesAtWhatIsNearInItsOwnWayAndOnlyHelpsAgainstThoseWhoWait() {
        // The ink jar: from a distance, a blot; a sign it deals with is hers; what takes more stands dazed.
        RunLevels.Level level = flat(80);
        level.foes.add(new int[]{RunLevels.TIMES, 26, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.TIMES, 50, FLOOR - 1, 3});
        PlusRun run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(20, GROUND);
        run.adopt(PlusRun.JAR);
        hold(run, .5f, false, false, false);
        boolean blot = false;
        for (int step = 0; step < 60 * 4 && run.flattened() == 0; step++) {
            for (PlusRun.Foe foe : run.foes()) if (!foe.flat) foe.x = foe.home;
            run.step(STEP, false, false, false);
            for (PlusRun.Shot shot : run.shots()) blot |= shot.kind == PlusRun.BLOT_SHOT && shot.pets && shot.hers;
        }
        assertTrue(blot);
        assertTrue(run.foes().get(0).flat);
        assertEquals(1, run.petKills());
        assertEquals(1, run.flattened());
        assertEquals(0, run.hurts());
        run.place(45, GROUND);
        PlusRun.Foe tough = run.foes().get(1);
        tough.dazed = 0;
        for (int step = 0; step < 60 * 4 && tough.left == 3 * PlusRun.BLOW; step++) {
            tough.x = tough.home;
            run.step(STEP, false, false, false);
        }
        assertEquals(2 * PlusRun.BLOW, tough.left);
        assertTrue(tough.dazed > 1);

        // The cat: what is close to it, and nothing further off; and often.
        level = flat(80);
        level.foes.add(new int[]{RunLevels.MINUS, 28, FLOOR - 1});
        run = new PlusRun(level);
        PlusRun.Foe minus = run.foes().get(0);
        run.place(20, GROUND);
        run.adopt(PlusRun.CAT);
        for (int step = 0; step < 60 * 3; step++) {
            minus.x = minus.home;
            run.step(STEP, false, false, false);
        }
        assertEquals(0, run.petStrikes());
        assertFalse(minus.flat);
        run.place(27, GROUND);
        for (int step = 0; step < 60 * 3 && !minus.flat; step++) {
            minus.x = minus.home;
            run.step(STEP, false, false, false);
        }
        assertTrue(minus.flat);
        assertEquals(1, run.petKills());

        // The dog: along the ground in a line, through all of them; not at what is up on a ledge.
        level = flat(80);
        for (int column = 30; column <= 34; column++) level.land[FLOOR - 4][column] = RunLevels.LEDGE;
        level.foes.add(new int[]{RunLevels.MINUS, 32, FLOOR - 5});
        run = new PlusRun(level);
        run.place(28, GROUND);
        run.adopt(PlusRun.DOG);
        for (int step = 0; step < 60 * 4; step++) {
            run.foes().get(0).x = run.foes().get(0).home;
            run.step(STEP, false, false, false);
        }
        assertEquals(0, run.petStrikes());
        level = flat(80);
        for (int each = 0; each < 3; each++) level.foes.add(new int[]{RunLevels.TIMES, 24 + each, FLOOR - 1});
        run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(22, GROUND);
        run.adopt(PlusRun.DOG);
        for (int step = 0; step < 60 * 3 && run.flattened() < 3; step++) run.step(STEP, false, false, false);
        assertEquals(3, run.flattened());
        assertEquals(1, run.petStrikes());
        assertEquals(3, run.petKills());

        // The duck: what is in the air before what is on the ground; and with its third drop, a quack that dazes what is near.
        level = flat(80);
        level.foes.add(new int[]{RunLevels.MINUS, 22, FLOOR - 1, 9});
        level.foes.add(new int[]{RunLevels.DRIFTER, 25, FLOOR - 4});
        level.foes.add(new int[]{RunLevels.MINUS, 23, FLOOR - 1, 9});
        run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(20, GROUND - 6);
        run.adopt(PlusRun.DUCK);
        PlusRun.Foe moth = run.foes().get(1);
        for (int step = 0; step < 60 * 3 && !moth.flat; step++) {
            run.place(20, GROUND - 6);
            run.step(STEP, false, false, false);
        }
        assertTrue(moth.flat);
        assertEquals(9 * PlusRun.BLOW, run.foes().get(0).left);
        assertEquals(1, run.petStrikes());
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 0;
        for (int step = 0; step < 60 * 8 && run.petStrikes() < 3; step++) {
            run.place(20, GROUND - 6);
            for (PlusRun.Foe foe : run.foes()) if (!foe.flat) foe.x = foe.home;
            run.step(STEP, false, false, false);
        }
        assertEquals(3, run.petStrikes());
        for (int step = 0; step < 30; step++) {
            run.place(20, GROUND - 6);
            run.step(STEP, false, false, false);
        }
        assertTrue(run.foes().get(0).dazed > .3f && run.foes().get(2).dazed > .3f);

        // The monkey: a nut, lobbed; and it fetches: the pluses it passes are hers, and a ribbon it comes by is tied on her.
        level = flat(80);
        level.foes.add(new int[]{RunLevels.TIMES, 26, FLOOR - 1});
        for (int column = 14; column <= 16; column++) level.pluses.add(new int[]{column, FLOOR - 2});
        level.items.add(new int[]{PlusRun.FORM + PlusRun.GREEN, 15, FLOOR - 1, 0});
        run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(22, GROUND);
        run.adopt(PlusRun.MONKEY);
        boolean nut = false;
        for (int step = 0; step < 60 * 4 && run.flattened() == 0; step++) {
            run.step(STEP, false, false, false);
            for (PlusRun.Shot shot : run.shots()) nut |= shot.kind == PlusRun.NUT && shot.pets && shot.down < 0;
        }
        assertTrue(nut);
        assertEquals(1, run.petKills());
        int collected = run.collected();
        run.place(17.2f, GROUND);
        for (int step = 0; step < 60 * 2; step++) {
            run.place(17.2f, GROUND);
            run.step(STEP, true, false, false);
        }
        assertEquals(collected + 3, run.collected());
        assertTrue(run.has(PlusRun.GREEN));

        // Against one who waits a pet only helps: it takes half a piece off her shield and never the last; it never breaks it; and broken by
        // her, it lands blows, but never the last.
        for (int kind = 0; kind < PlusRun.PETS; kind++) {
            // (She keeps over him, out of his reach and not touching him, with her pet under her; in a bare place, so that nothing is in the way.)
            RunLevels.Level pen = flat(70);
            pen.arena = 20;
            pen.foes.add(new int[]{RunLevels.GUARD, 34, FLOOR - 1});
            PlusRun fight = shutIn(pen, true);
            PlusRun.Foe guard = waiting(fight);
            fight.adopt(kind);
            // (The one that copies her does nothing unless she does: she throws pens, over his head, and it throws small ones along the ground.)
            if (kind == PlusRun.TWIN) fight.arm(PlusRun.PEN);
            for (int step = 0; step < 60 * 30; step++) {
                fight.place(guard.x - 1.05f, floorOf(fight) - PlusRun.TALL - (kind == PlusRun.TWIN ? 1 : 1.8f));
                fight.shots().removeIf(shot -> !shot.hers);
                fight.step(STEP, false, false, false, kind == PlusRun.TWIN && step % 40 == 0);
            }
            assertTrue("pet " + kind + " cracks " + fight.cracks() + " strikes " + fight.petStrikes() + " pet at " + fight.petX() + "," + fight.petY() + " her " + fight.x() + "," + fight.y()
                    + " guard " + guard.x + "," + guard.y + " away " + guard.away + " shield " + fight.shield(guard) + " forming " + fight.forming(guard), fight.cracks() >= 1);
            assertEquals("pet " + kind, 0, fight.shatters());
            assertEquals("pet " + kind, 0, fight.blows());
            assertTrue("pet " + kind, fight.shield(guard) >= .5f / PlusRun.strength(RunLevels.GUARD) - .001f && fight.shield(guard) < 1);
            assertFalse(fight.open(guard));
        }
        RunLevels.Level pen = flat(70);
        pen.arena = 20;
        pen.foes.add(new int[]{RunLevels.GUARD, 34, FLOOR - 1});
        PlusRun fight = shutIn(pen, true);
        PlusRun.Foe guard = waiting(fight);
        fight.adopt(PlusRun.CAT);
        for (int step = 0; step < 60 * 20 && !fight.open(guard); step++) {
            if (pressable(fight, guard)) touch(fight, guard);
            else park(fight);
        }
        assertTrue(fight.open(guard));
        guard.left = 1;
        guard.broken = 30;
        for (int step = 0; step < 60 * 6; step++) {
            // (With nothing about her that would land a blow of her own.)
            fight.bare();
            fight.place(guard.x - 1.05f, floorOf(fight) - PlusRun.TALL - 1.8f);
            fight.shots().removeIf(shot -> !shot.hers);
            fight.step(STEP, false, false, false);
        }
        assertFalse(guard.flat);
        assertEquals(1, guard.left);
        assertTrue(fight.standing());
        guard.left = 3;
        guard.dazed = 0;
        int blows = fight.blows();
        for (int step = 0; step < 60 * 6 && fight.blows() == blows; step++) {
            // (With nothing about her that would land a blow of her own.)
            fight.bare();
            fight.place(guard.x - 1.05f, floorOf(fight) - PlusRun.TALL - 1.8f);
            fight.shots().removeIf(shot -> !shot.hers);
            fight.step(STEP, false, false, false);
        }
        assertEquals(blows + 1, fight.blows());
        assertEquals(2, guard.left);
    }

    @Test public void theFourOfThoseSheHasBeatenEachHelpInTheirOwnWay() {
        // Each deals with a sign near her, by what it throws or by its swing; and what it throws is its own.
        int[] kinds = {PlusRun.MINI_TSUBAKIEH, PlusRun.MINI_ILLYA, PlusRun.MINI_MAWANG, PlusRun.MINI_LUCY};
        int[] throwsOf = {-1, PlusRun.RAY, PlusRun.FIRE, PlusRun.WATER};
        for (int index = 0; index < kinds.length; index++) {
            RunLevels.Level level = flat(80);
            level.foes.add(new int[]{RunLevels.TIMES, 25, FLOOR - 1});
            PlusRun run = new PlusRun(level);
            for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
            run.place(22, GROUND);
            run.adopt(kinds[index]);
            int most = 0;
            boolean own = throwsOf[index] < 0;
            for (int step = 0; step < 60 * 5 && run.flattened() == 0; step++) {
                run.step(STEP, false, false, false);
                int now = 0;
                for (PlusRun.Shot shot : run.shots()) {
                    if (!shot.pets) continue;
                    own |= shot.kind == throwsOf[index];
                    now++;
                }
                most = Math.max(most, now);
            }
            assertTrue("kind " + kinds[index], own);
            assertEquals("kind " + kinds[index], 1, run.petKills());
            // A fan of three sparkles; three tongues of flame; one jet of water; a swing throws nothing.
            assertEquals("kind " + kinds[index], new int[]{0, 3, 3, 1}[index], most);
        }
        // A swing of mini Tsubakieh's: all within it are dealt with, and the one she went at; what is further off is not.
        RunLevels.Level level = flat(80);
        level.foes.add(new int[]{RunLevels.MINUS, 25, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 26, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 34, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(22, GROUND);
        run.adopt(PlusRun.MINI_TSUBAKIEH);
        for (int step = 0; step < 60 * 2 && run.flattened() < 2; step++) run.step(STEP, false, false, false);
        assertEquals(2, run.flattened());
        assertFalse(run.foes().get(2).flat);
        // Against one of those who wait, each only helps, as every pet does: it never lands the last blow.
        for (int kind : kinds) {
            level = flat(80);
            level.foes.add(new int[]{RunLevels.GUARD, 26, FLOOR - 1});
            run = new PlusRun(level);
            run.place(22, GROUND);
            run.adopt(kind);
            PlusRun.Foe boss = run.foes().get(0);
            hold(run, 30, false, false, false);
            assertFalse("kind " + kind, boss.flat);
        }
    }

    @Test public void theFiveSmallCopiesOfPeopleSheKnowsEachHelpInTheirOwnWay() {
        assertEquals(14, PlusRun.PETS);
        assertEquals("the first ten are had from the first", 10, RunPets.FOUND);
        assertTrue(PlusRun.floats(PlusRun.IMP) && PlusRun.floats(PlusRun.CHERUB));
        assertFalse(PlusRun.floats(PlusRun.MINI_CHIKUWA) || PlusRun.floats(PlusRun.MINI_RUNE) || PlusRun.floats(PlusRun.TWIN));
        // A mini Chikuwa: a roll, lobbed, at the nearest; and while she is short of a heart, half of one back every so often, and never more than she can have.
        RunLevels.Level level = flat(80);
        level.foes.add(new int[]{RunLevels.TIMES, 26, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(22, GROUND);
        run.adopt(PlusRun.MINI_CHIKUWA);
        boolean roll = false;
        for (int step = 0; step < 60 * 4 && run.flattened() == 0; step++) {
            run.step(STEP, false, false, false);
            for (PlusRun.Shot shot : run.shots()) roll |= shot.kind == PlusRun.ROLL && shot.pets && shot.down < 0;
        }
        assertTrue(roll);
        assertEquals(1, run.petKills());
        hold(run, PlusRun.GIFT_EVERY + 2, false, false, false);
        assertEquals(0, run.petGifts());
        run.wound();
        int halves = run.halves();
        assertEquals(PlusRun.HEARTS * 2 - 2, halves);
        hold(run, PlusRun.GIFT_EVERY - 1, false, false, false);
        assertEquals(0, run.petGifts());
        hold(run, 1.5f, false, false, false);
        assertEquals(1, run.petGifts());
        assertEquals(halves + 1, run.halves());
        hold(run, PlusRun.GIFT_EVERY * 3, false, false, false);
        assertEquals(2, run.petGifts());
        assertEquals(PlusRun.HEARTS * 2, run.halves());

        // A mini Rune: a puddle where she has been, in which a sign goes at less than half its pace; a flick of ink at what is close, which
        // leaves what it does not deal with dazed; and with one of those who wait near, she keeps on the far side of Halgakos.
        level = flat(80);
        level.foes.add(new int[]{RunLevels.MINUS, 40, FLOOR - 1});
        run = new PlusRun(level);
        run.place(20, GROUND);
        run.adopt(PlusRun.MINI_RUNE);
        hold(run, 1.5f, false, false, false);
        assertFalse(run.puddles().isEmpty());
        for (float[] puddle : run.puddles()) assertTrue(Math.abs(puddle[0] - run.petX()) < 1.5f && puddle[1] == FLOOR && puddle[2] <= PlusRun.PUDDLE_LASTS);
        PlusRun.Foe minus = run.foes().get(0);
        float was = minus.x;
        hold(run, 1, false, false, false);
        float free = Math.abs(minus.x - was);
        assertFalse(run.mired(minus));
        run.puddles().clear();
        run.puddles().add(new float[]{minus.x + minus.wide() / 2 - .3f, FLOOR, 9});
        run.puddles().add(new float[]{minus.x + minus.wide() / 2 - 1, FLOOR, 9});
        assertTrue(run.mired(minus));
        was = minus.x;
        hold(run, .5f, false, false, false);
        assertEquals(free * .5f * PlusRun.PUDDLE_SLOWS, Math.abs(minus.x - was), .05f);
        hold(run, 12, false, false, false);
        assertTrue(run.puddles().size() <= 6);
        level = flat(80);
        level.foes.add(new int[]{RunLevels.TIMES, 21, FLOOR - 1, 3});
        run = new PlusRun(level);
        run.place(20, GROUND);
        run.adopt(PlusRun.MINI_RUNE);
        PlusRun.Foe tough = run.foes().get(0);
        for (int step = 0; step < 60 * 4 && tough.left == 3 * PlusRun.BLOW; step++) {
            tough.x = tough.home;
            run.place(20, GROUND);
            run.step(STEP, false, false, false);
        }
        assertEquals(0, run.hurts());
        assertEquals(2 * PlusRun.BLOW, tough.left);
        assertTrue(tough.dazed > 1);
        RunLevels.Level pen = flat(70);
        pen.arena = 20;
        pen.foes.add(new int[]{RunLevels.GUARD, 34, FLOOR - 1});
        PlusRun fight = shutIn(pen, true);
        PlusRun.Foe guard = waiting(fight);
        fight.adopt(PlusRun.MINI_RUNE);
        for (int side = 0; side < 2; side++) {
            for (int step = 0; step < 60 * 2; step++) {
                fight.place(guard.x + (side == 0 ? -3 : 3.5f), floorOf(fight) - PlusRun.TALL - 1.8f);
                fight.shots().removeIf(shot -> !shot.hers);
                fight.step(STEP, false, false, false);
            }
            assertEquals("side " + side, side == 0, fight.petX() < fight.x());
        }

        // The one with black wings: at the nearest, from close, again and again: more than twice as often as the duck.
        level = flat(80);
        level.foes.add(new int[]{RunLevels.MINUS, 22, FLOOR - 1, 30});
        level.foes.add(new int[]{RunLevels.MINUS, 30, FLOOR - 1});
        run = new PlusRun(level);
        run.place(20, GROUND);
        run.adopt(PlusRun.IMP);
        for (int step = 0; step < 60 * 5; step++) {
            for (PlusRun.Foe foe : run.foes()) foe.x = foe.home;
            run.place(20, GROUND);
            run.step(STEP, false, false, false);
        }
        assertEquals(0, run.hurts());
        assertTrue("strikes " + run.petStrikes(), run.petStrikes() >= 5);
        assertEquals((30 - run.petStrikes()) * PlusRun.BLOW, run.foes().get(0).left, PlusRun.BLOW);
        assertFalse(run.foes().get(1).flat);

        // The one with white wings: a ray, from a long way off, slowly; and a ring of light that turns aside one thing thrown at her, and
        // is then a while in coming back.
        level = flat(80);
        level.foes.add(new int[]{RunLevels.TIMES, 28, FLOOR - 1});
        run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(20, GROUND);
        run.adopt(PlusRun.CHERUB);
        boolean ray = false;
        for (int step = 0; step < 60 * 5 && run.flattened() == 0; step++) {
            run.step(STEP, false, false, false);
            for (PlusRun.Shot shot : run.shots()) ray |= shot.kind == PlusRun.RAY && shot.pets && shot.hers;
        }
        assertTrue(ray);
        assertEquals(1, run.petKills());
        level = flat(80);
        level.foes.add(new int[]{RunLevels.THROWER, 24, FLOOR - 1});
        run = new PlusRun(level);
        run.place(20, GROUND);
        run.adopt(PlusRun.CHERUB);
        PlusRun.Foe thrower = run.foes().get(0);
        assertFalse(run.warded());
        // (The one who throws holds back until the ring is ready.)
        for (int step = 0; step < 60 * 40 && run.petWards() < 2; step++) {
            boolean coming = false;
            for (PlusRun.Shot shot : run.shots()) coming |= !shot.hers;
            thrower.wait = run.warded() && !coming ? Math.min(thrower.wait, .3f) : 99;
            run.place(20, GROUND);
            run.step(STEP, false, false, false);
            run.shots().removeIf(shot -> shot.pets);
        }
        assertEquals(2, run.petWards());
        assertEquals(0, run.hurts());
        assertFalse(run.warded());
        // With the ring not ready, the next thing thrown is not turned aside.
        thrower.wait = .1f;
        for (int step = 0; step < 60 * 3 && run.hurts() == 0; step++) {
            run.place(20, GROUND);
            run.step(STEP, false, false, false);
            run.shots().removeIf(shot -> shot.pets);
        }
        assertEquals(1, run.hurts());
        assertEquals(2, run.petWards());

        // The one with no wings: nothing by itself; when she uses what she holds, a small one of the same from where it is; and with
        // nothing in her hands, nothing.
        // (She stands on a ledge of her own, three squares up, so that what she does goes over what is on the ground and what it does does not.)
        level = flat(80);
        level.land[FLOOR - 3][20] = RunLevels.LEDGE;
        level.land[FLOOR - 3][21] = RunLevels.LEDGE;
        level.foes.add(new int[]{RunLevels.TIMES, 23, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.TIMES, 17, FLOOR - 1});
        run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(20.2f, GROUND - 3);
        run.adopt(PlusRun.TWIN);
        for (int step = 0; step < 60 * 3; step++) run.step(STEP, false, false, false, step % 30 == 0);
        assertEquals(FLOOR, run.petY(), .05f);
        assertEquals(0, run.petStrikes());
        assertEquals(0, run.petCopies());
        run.arm(PlusRun.PEN);
        boolean small = false;
        for (int step = 0; step < 60 * 3 && run.flattened() == 0; step++) {
            run.step(STEP, false, false, false, step % 30 == 0);
            for (PlusRun.Shot shot : run.shots()) small |= shot.kind == PlusRun.PEN && shot.pets;
        }
        assertTrue(small);
        assertTrue(run.petCopies() >= 1);
        // (Her own pen went over its head: the one in front of her on the ground was the small one's.)
        assertTrue(run.foes().get(0).flat);
        assertEquals(1, run.petKills());
        // A swing: at what is in front of it.
        level = flat(80);
        level.land[FLOOR - 3][20] = RunLevels.LEDGE;
        level.land[FLOOR - 3][21] = RunLevels.LEDGE;
        level.foes.add(new int[]{RunLevels.TIMES, 20, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.TIMES, 17, FLOOR - 1});
        run = new PlusRun(level);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 99;
        run.place(20.2f, GROUND - 3);
        run.adopt(PlusRun.TWIN);
        run.arm(PlusRun.SWORD);
        for (int step = 0; step < 60 * 3 && run.flattened() == 0; step++) run.step(STEP, false, false, false, step % 30 == 0);
        assertEquals(1, run.flattened());
        assertEquals(1, run.petKills());
        assertTrue(run.foes().get(0).flat);
        assertFalse(run.foes().get(1).flat);
    }

    @Test public void herPetsAreThoseThatAreHersForGoodAndTheStraysOfALandAsManyAsSheHasRoomFor() {
        // To begin with there is room for one stray: one found goes with her; another found lies where it is and is offered; changed for,
        // it takes the place of the stray she had.
        RunLevels.Level level = flat(120);
        level.items.add(new int[]{PlusRun.PET + PlusRun.CAT, 10, FLOOR - 1, 0});
        level.items.add(new int[]{PlusRun.PET + PlusRun.DOG, 20, FLOOR - 1, 0});
        PlusRun run = new PlusRun(level);
        assertTrue(run.herd().isEmpty());
        assertEquals(-1, run.pet());
        run.place(10.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(1, run.herd().size());
        assertEquals(PlusRun.CAT, run.pet());
        assertFalse(run.herd().get(0).stays);
        assertEquals(1, run.strays());
        assertEquals(1, run.petJoined());
        assertEquals(PlusRun.CAT, run.lastJoined());
        assertEquals(-1, run.petOffer());
        assertFalse(run.swapPet());
        run.place(20.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.DOG, run.petOffer());
        assertEquals(1, run.herd().size());
        assertEquals(PlusRun.CAT, run.pet());
        assertEquals(1, run.items().size());
        hold(run, .5f, false, false, false);
        assertEquals(PlusRun.DOG, run.petOffer());
        assertTrue(run.swapPet());
        assertEquals(1, run.herd().size());
        assertEquals(PlusRun.DOG, run.pet());
        assertEquals(1, run.petSwaps());
        assertEquals(2, run.petJoined());
        assertEquals(0, run.items().size());
        run.step(STEP, false, false, false);
        assertEquals(-1, run.petOffer());

        // Those that are hers for good come into the land with her, before any stray, and are never the ones changed.
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.petWith = 1 << PlusRun.DUCK | 1 << PlusRun.CAT;
        perks.tempSlots = 2;
        level = flat(120);
        level.items.add(new int[]{PlusRun.PET + PlusRun.DOG, 10, FLOOR - 1, 0});
        level.items.add(new int[]{PlusRun.PET + PlusRun.MONKEY, 20, FLOOR - 1, 0});
        level.items.add(new int[]{PlusRun.PET + PlusRun.JAR, 30, FLOOR - 1, 0});
        run = new PlusRun(level, perks);
        assertEquals(2, run.herd().size());
        assertEquals(PlusRun.CAT, run.herd().get(0).kind);
        assertEquals(PlusRun.DUCK, run.herd().get(1).kind);
        assertTrue(run.herd().get(0).stays && run.herd().get(1).stays);
        assertEquals(0, run.petJoined());
        assertEquals(0, run.strays());
        for (float column : new float[]{10.2f, 20.2f}) {
            run.place(column, GROUND);
            run.step(STEP, false, false, false);
        }
        assertEquals(4, run.herd().size());
        assertEquals(2, run.strays());
        assertEquals(PlusRun.DOG, run.herd().get(2).kind);
        assertEquals(PlusRun.MONKEY, run.herd().get(3).kind);
        run.place(30.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.JAR, run.petOffer());
        assertTrue(run.swapPet());
        assertEquals(4, run.herd().size());
        assertEquals(PlusRun.CAT, run.herd().get(0).kind);
        assertEquals(PlusRun.DUCK, run.herd().get(1).kind);
        assertEquals(PlusRun.MONKEY, run.herd().get(2).kind);
        assertEquals(PlusRun.JAR, run.herd().get(3).kind);
        // What is chosen at the counter in the middle of a land is so there and then: one more of hers comes, one goes home.
        PlusRun.Perks now = new PlusRun.Perks();
        now.petWith = 1 << PlusRun.CAT | 1 << PlusRun.IMP;
        now.tempSlots = 2;
        run.grant(now);
        assertEquals(4, run.herd().size());
        assertEquals(PlusRun.CAT, run.herd().get(0).kind);
        assertEquals(PlusRun.IMP, run.herd().get(1).kind);
        assertTrue(run.herd().get(1).stays);
        assertEquals(2, run.strays());

        // Six of them, three of hers and three strays: each has a place of its own behind her, none on her and none on another; those
        // that run stand on the ground and those that float hang in the air.
        perks = new PlusRun.Perks();
        perks.petWith = 1 << PlusRun.CAT | 1 << PlusRun.DUCK | 1 << PlusRun.TWIN;
        perks.tempSlots = 3;
        run = new PlusRun(flat(120), perks);
        run.place(40, GROUND);
        run.adopt(PlusRun.JAR);
        run.adopt(PlusRun.DOG);
        run.adopt(PlusRun.CHERUB);
        assertEquals(6, run.herd().size());
        hold(run, 3, false, true, false);
        hold(run, 2, false, false, false);
        float her = run.x() + PlusRun.WIDE / 2;
        for (PlusRun.Pet one : run.herd()) {
            assertTrue("pet " + one.kind + " at " + (one.x - her), one.x < her - .6f && one.x > her - 5);
            if (PlusRun.floats(one.kind)) assertTrue("pet " + one.kind, one.y < FLOOR - .8f && one.y > FLOOR - 2.6f);
            else assertEquals("pet " + one.kind, FLOOR, one.y, .05f);
            for (PlusRun.Pet other : run.herd()) {
                if (other != one) assertTrue("pets " + one.kind + " and " + other.kind, Math.hypot(one.x - other.x, one.y - other.y) > .5f);
            }
        }
        // A seventh has no room; the oldest stray makes way for one adopted.
        run.adopt(PlusRun.MONKEY);
        assertEquals(6, run.herd().size());
        assertEquals(3, run.strays());
        assertEquals(PlusRun.MONKEY, run.herd().get(5).kind);
        assertEquals(PlusRun.DOG, run.herd().get(3).kind);

        // They take turns: with things all about her, no two of them begin on something in the same moment.
        level = flat(120);
        for (int column = 34; column <= 46; column += 2) level.foes.add(new int[]{RunLevels.MINUS, column, FLOOR - 1, 60});
        perks = new PlusRun.Perks();
        perks.petWith = 1 << PlusRun.CAT | 1 << PlusRun.IMP | 1 << PlusRun.MONKEY;
        perks.tempSlots = 3;
        run = new PlusRun(level, perks);
        run.adopt(PlusRun.JAR);
        run.adopt(PlusRun.DOG);
        run.adopt(PlusRun.CHERUB);
        int strikes = 0, steps = 0, last = -99;
        for (int step = 0; step < 60 * 8; step++) {
            for (PlusRun.Foe foe : run.foes()) foe.x = foe.home;
            run.place(40, GROUND - 3);
            run.step(STEP, false, false, false);
            if (run.petStrikes() != strikes) {
                assertEquals(strikes + 1, run.petStrikes());
                assertTrue("only " + (step - last) + " steps apart", step - last >= 12);
                strikes = run.petStrikes();
                last = step;
                steps++;
            }
        }
        assertTrue("strikes " + steps, steps >= 12);

        // The kennel: brisker pets wait less between one thing and the next; stronger ones count twice on a big one, and take a whole
        // piece off a shield, which they still never break.
        int[] done = new int[2];
        for (int brisk = 0; brisk < 2; brisk++) {
            level = flat(80);
            level.foes.add(new int[]{RunLevels.MINUS, 24, FLOOR - 1, 99});
            perks = new PlusRun.Perks();
            perks.petBrisk = brisk == 0 ? 1 : .6f;
            run = new PlusRun(level, perks);
            run.place(20, GROUND);
            run.adopt(PlusRun.JAR);
            for (int step = 0; step < 60 * 10; step++) {
                run.foes().get(0).x = run.foes().get(0).home;
                run.place(20, GROUND);
                run.step(STEP, false, false, false);
            }
            done[brisk] = run.petStrikes();
        }
        assertTrue("strikes " + done[0] + " and " + done[1], done[1] >= done[0] * 14 / 10);
        level = flat(80);
        level.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1, 9});
        perks = new PlusRun.Perks();
        perks.petStrong = 1;
        run = new PlusRun(level, perks);
        run.place(18.2f, GROUND);
        run.adopt(PlusRun.IMP);
        PlusRun.Foe big = run.foes().get(0);
        for (int step = 0; step < 60 * 4 && run.petStrikes() < 1; step++) {
            big.x = big.home;
            run.place(18.2f, GROUND);
            run.step(STEP, false, false, false);
        }
        for (int step = 0; step < 22; step++) {
            big.x = big.home;
            run.place(18.2f, GROUND);
            run.step(STEP, false, false, false);
        }
        assertEquals(0, run.hurts());
        assertEquals(1, run.petStrikes());
        assertEquals(7 * PlusRun.BLOW, big.left);
        RunLevels.Level pen = flat(70);
        pen.arena = 20;
        pen.foes.add(new int[]{RunLevels.GUARD, 34, FLOOR - 1});
        perks = new PlusRun.Perks();
        perks.petStrong = 2;
        PlusRun fight = shutIn(pen, true);
        fight.grant(perks);
        PlusRun.Foe guard = waiting(fight);
        guard.shield = guard.shieldMost = 6;
        fight.adopt(PlusRun.CAT);
        float least = 6;
        java.util.Set<Float> seen = new java.util.TreeSet<>();
        for (int step = 0; step < 60 * 30; step++) {
            fight.place(guard.x - 1.05f, floorOf(fight) - PlusRun.TALL - 1.8f);
            fight.shots().removeIf(shot -> !shot.hers);
            fight.step(STEP, false, false, false);
            if (!fight.forming(guard) && !fight.open(guard)) {
                least = Math.min(least, guard.shield);
                seen.add(guard.shield);
            }
        }
        assertEquals(0, fight.shatters());
        assertEquals(.5f, least, .001f);
        assertTrue("shield went " + seen, seen.contains(5f) && seen.contains(4f) && !seen.contains(5.5f));
    }

    @Test public void noPlaceGivesWhiteAndBlackLiesWhereAGreedyHandTakesIt() {
        int tempted = 0, stashed = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            // What a place off the land or a game gives when it is won: a ribbon of one of the six, one in the first volume and two after.
            int[] given = RunLevels.ribbonsOf(number);
            assertEquals("land " + number, number > RunLevels.COUNT ? 2 : 1, given.length);
            for (int colour : given) assertTrue("land " + number, colour >= PlusRun.RED && colour <= PlusRun.PURPLE);
            if (given.length == 2) assertTrue(given[0] != given[1]);
            boolean black = false;
            for (int[] item : level.items) {
                // Nowhere, in a land or in a place off it, does a white ribbon lie.
                assertTrue("land " + number + " has a white ribbon at " + item[1], item[0] != PlusRun.FORM + PlusRun.WHITE);
                if (item[0] != PlusRun.FORM + PlusRun.BLACK) continue;
                assertTrue("land " + number, number >= RunLevels.BLACK_FROM);
                if (item[1] >= level.main) stashed++;
                else black = true;
            }
            if (black && number <= RunLevels.COUNT) tempted++;
        }
        assertTrue("tempted " + tempted + " stashed " + stashed, tempted >= 8 && stashed >= 10);
        // A black ribbon of the first volume lies on the ground a little past a good one.
        RunLevels.Level level = RunLevels.level(RunLevels.BLACK_FROM);
        boolean beside = false;
        for (int[] item : level.items) {
            if (item[0] != PlusRun.FORM + PlusRun.BLACK || item[1] >= level.main) continue;
            for (int[] other : level.items) {
                beside |= other[0] > PlusRun.FORM && other[0] < PlusRun.FORM + PlusRun.BLACK && item[1] - other[1] >= 2 && item[1] - other[1] <= 4 && other[2] == item[2];
            }
        }
        assertTrue(beside);
    }

    @Test public void inWhiteSheHasEveryPowerButBlack() {
        PlusRun run = new PlusRun(ribbon(flat(60), PlusRun.WHITE));
        hold(run, .5f, false, true, false);
        assertEquals(PlusRun.WHITE, run.form());
        for (int power = PlusRun.RED; power <= PlusRun.PURPLE; power++) assertTrue("power " + power, run.has(power));
        assertFalse(run.has(PlusRun.BLACK));
        // Red and blue are both in it, so what she throws is steam.
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.shots().size());
        assertEquals(PlusRun.STEAM, run.shots().get(0).kind);
        // Another ribbon is worn with it, and she is white for as long as the white one lasts.
        RunLevels.Level level = ribbon(flat(60), PlusRun.WHITE);
        level.items.add(new int[]{PlusRun.FORM + PlusRun.YELLOW, 8, FLOOR - 1, 0});
        PlusRun other = new PlusRun(level);
        hold(other, 1.2f, false, true, false);
        assertEquals(PlusRun.WHITE, other.form());
        assertTrue(other.white());
        assertTrue(other.on(PlusRun.YELLOW) > 13);
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

    private static final int[] WAITED_IN = {RunLevels.GUARD_LAND, RunLevels.BUTLER_LAND, RunLevels.ILLYA_LAND, RunLevels.TSUBAKIEH_LAND, RunLevels.COUNT,
            35, 40, 45, 50, 55, 60, 65, 70, 75, 80};

    private static int floorOf(PlusRun run) { return RunLevels.top(run.level(), run.level().arena + 3); }

    /** The first of those who wait in a run that still stands. */
    /**
     * No spot in a fight with one who waits is safe: stood still on the highest ledge of the place she is shut in, and on the furthest one
     * from where she waits, a hurt comes within eight seconds of her being there.
     */
    @Test public void noLedgeInAPlaceWhereSomeoneWaitsIsSafe() {
        StringBuilder safe = new StringBuilder(), took = new StringBuilder();
        int checked = 0, leaps = 0;
        for (int land = 1; land <= RunLevels.TOTAL; land++) {
            if (RunLevels.bossOf(land) < 0 || RunLevels.level(land).arena < 0) continue;
            for (int which = 0; which < 2; which++) {
                RunLevels.Level level = RunLevels.level(land);
                PlusRun run = shutIn(level, false);
                PlusRun.Foe boss = waiting(run);
                // (Once she is there.)
                for (int step = 0; step < 60 * 6 && boss != null && (run.absent(boss) || boss.enter > 0 || boss.flourish > 0); step++) run.step(STEP, false, false, false);
                if (boss == null) continue;
                int[] spot = ledge(level, boss, which == 1);
                if (spot == null) continue;
                run.place(spot[0] + (1 - PlusRun.WIDE) / 2, spot[1] - PlusRun.TALL - .02f);
                int hurts = run.hurts();
                float gone = 0;
                while (gone < 8 && run.hurts() == hurts && run.state() == PlusRun.PLAYING) {
                    run.step(STEP, false, false, false);
                    gone += STEP;
                }
                checked++;
                leaps += run.bossLeaps();
                took.append(" ").append(land).append(which == 0 ? "h" : "f").append(":").append(Math.round(gone * 10) / 10f);
                if (run.hurts() == hurts) safe.append(" land ").append(land).append(which == 0 ? " highest" : " furthest").append(" at ").append(spot[0]).append(",").append(spot[1]);
            }
        }
        System.out.println("REACH seconds to a hurt" + took + "  (leaps " + leaps + ")");
        assertTrue(checked >= 20);
        assertEquals("safe:" + safe, "", safe.toString());
    }

    /** The highest ledge in the place she is shut in (or the one furthest from whoever waits, raised over the floor), with room to stand on it. */
    private static int[] ledge(RunLevels.Level level, PlusRun.Foe boss, boolean furthest) {
        int floor = RunLevels.top(level, level.arena + 3), best = -1, bestRow = 99;
        float far = -1;
        for (int column = level.arena + 1; column < level.gate - 1; column++) {
            for (int row = 3; row < level.land.length; row++) {
                char here = level.land[row][column];
                boolean stands = here == RunLevels.BLOCK || here == RunLevels.LEDGE || here == RunLevels.THIN;
                if (!stands || level.land[row - 1][column] != RunLevels.AIR || level.land[row - 2][column] != RunLevels.AIR || level.land[row - 3][column] != RunLevels.AIR) continue;
                if (row >= floor) break;
                float away = Math.abs(column - boss.x);
                if (furthest ? away > far : row < bestRow) {
                    best = column;
                    bestRow = row;
                    far = away;
                }
                break;
            }
        }
        return best < 0 ? null : new int[]{best, bestRow};
    }

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

    /** Whether a blow on one of those who wait would do something just now, to her shield or to her: she is there, not reeling, and her shield is not forming. */
    private static boolean pressable(PlusRun run, PlusRun.Foe foe) {
        return !foe.flat && !run.absent(foe) && foe.dazed <= 0 && !run.forming(foe);
    }

    /**
     * Watches the fight until one of them begins to show that a thing of theirs is coming,
     * with blows whenever one can land, on her shield and then on her, for as long as that
     * thing belongs to a later third.
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
            if (!has && pressable(run, foe) && foe.left > 1) touch(run, foe);
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
            assertTrue("land " + land + " takes " + most, most >= 5 && most <= 12);
            java.util.List<Integer> changedAt = new java.util.ArrayList<>();
            int parried = 0, seen = 0, chipped = 0;
            for (int step = 0; step < 60 * 600 && run.standing(); step++) {
                // (Whoever a blow is to be dealt to, on her shield or on her.)
                PlusRun.Foe open = null;
                for (PlusRun.Foe foe : all) {
                    if (!pressable(run, foe)) continue;
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
                // What is shown of each of them says whether a blow would land: every so often one is tried against it.
                boolean turning = first.act <= .04f || first.enter > 0 && first.enter <= .04f || first.flourish > 0 && first.flourish <= .04f
                        || first.dazed > 0 && first.dazed <= .04f;
                if (step % 7 == 0 && first.left > 1 && (done & wanted) != wanted && !turning) {
                    int blows = run.blows(), turned = run.parried(), guard = run.guard(first);
                    PlusRun trial = run.copy();
                    PlusRun.Foe same = waiting(trial);
                    int onShield = trial.parried() + trial.cracks();
                    touch(trial, same);
                    String shown = "land " + land + " shown " + guard + " at step " + step + " step " + first.step + " enter " + first.enter + " flourish "
                            + first.flourish + " dazed " + first.dazed + " act " + first.act + " mode " + first.mode;
                    assertEquals(shown, guard == PlusRun.BARE, trial.blows() > blows);
                    // (Shielded, the blow goes onto her shield: a piece off it, or, while it is forming, nothing at all.)
                    assertEquals(shown, guard == PlusRun.SHIELDED, trial.parried() + trial.cracks() > onShield);
                    if (guard == PlusRun.BARE) assertTrue(shown, run.bareLeft(first) > 0);
                    else assertEquals(shown, 0, guard == PlusRun.SHIELDED ? run.bareLeft(first) : 0, 0);
                }
                // What she does herself never opens her: she is bare only when her shield has been broken.
                for (PlusRun.Foe foe : all) if (!foe.flat && run.guard(foe) == PlusRun.BARE) assertTrue("land " + land, foe.broken > 0 && run.shatters() > 0);
                if (parried == 0 && pressable(run, first) && first.step == 1 && run.guard(first) == PlusRun.SHIELDED) {
                    // A blow while she is at something lands on her shield, and not on her.
                    int blows = run.blows(), cracks = run.cracks();
                    touch(run, first);
                    if (run.cracks() > cracks) {
                        assertEquals(blows, run.blows());
                        parried++;
                    }
                } else if (open != null && (done & wanted) == wanted) {
                    int phases = run.phases(), blows = run.blows(), cracks = run.cracks(), guard = run.guard(open);
                    float shield = run.shield(open);
                    touch(run, open);
                    if (guard == PlusRun.BARE) {
                        assertEquals("land " + land, blows + 1, run.blows());
                    } else {
                        // On her shield: a piece off it, and nothing to her; the last piece gone, it is broken and she is bare.
                        assertEquals("land " + land, PlusRun.SHIELDED, guard);
                        assertEquals("land " + land, blows, run.blows());
                        assertEquals("land " + land, cracks + 1, run.cracks());
                        assertTrue("land " + land, run.shield(open) < shield);
                        if (run.shield(open) <= 0) assertEquals("land " + land, PlusRun.BARE, run.guard(open));
                        chipped++;
                    }
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
            // Her shield was broken at least once for every two blows that landed on her, and it took as many pieces at least as it has.
            assertTrue("land " + land + " broke " + run.shatters(), run.shatters() >= (most + 1) / 2);
            assertTrue("land " + land + " chipped " + chipped, chipped + parried >= run.shatters() * 2);
            // They changed when a third of what they could take was gone, and again at two thirds.
            assertEquals("land " + land, java.util.Arrays.asList((most + 2) / 3, (2 * most + 2) / 3), changedAt);
            // Everything each of them does was done.
            for (PlusRun.Foe foe : all) {
                int things = 0;
                for (int phase = 0; phase < 3; phase++) for (int one : PlusRun.does(foe.kind, phase)) things |= 1 << one;
                assertTrue("land " + land + " has " + Integer.bitCount(things) + " things", Integer.bitCount(things) >= 3);
                assertEquals("land " + land + " sign " + foe.kind, things, foe.did & things);
            }
            assertTrue("land " + land + " did " + seen, seen >= 4);
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

    /** Has whoever waits touch her, with nothing to keep her from it, until it has hurt her once more. */
    private static void touched(PlusRun run) {
        int hurts = run.hurts();
        for (int step = 0; step < 60 * 60 && run.hurts() == hurts && run.state() == PlusRun.PLAYING; step++) {
            PlusRun.Foe foe = waiting(run);
            if (run.absent(foe) || foe.dazed > 0 || run.open(foe) || run.safe()) {
                park(run);
                continue;
            }
            run.bare();
            run.place(foe.x + foe.wide() + .01f - PlusRun.WIDE / 2, foe.y + foe.tall() - PlusRun.TALL);
            run.step(STEP, false, false, false);
        }
    }

    @Test public void aHurtInThereLeavesTheFightAsItIsAndALifeLostSendsHerBackToTheDoorAndThePlaceIsAsItWas() {
        for (int land : WAITED_IN) {
            PlusRun run = shutIn(land, true);
            RunLevels.Level level = run.level();
            PlusRun.Foe foe = waiting(run);
            // Blows until she has changed once, and one more.
            int most = foe.kind == RunLevels.MIRI || foe.kind == RunLevels.GARI ? 8 : foe.most, wanted = (most + 2) / 3 + 1;
            for (int step = 0; step < 60 * 300 && run.blows() < wanted; step++) {
                PlusRun.Foe open = null;
                for (PlusRun.Foe one : run.foes()) if (RunLevels.boss(one.kind) && pressable(run, one)) open = one;
                if (open != null) touch(run, open);
                else park(run);
            }
            assertEquals("land " + land, wanted, run.blows());
            assertEquals(1, run.phases());
            // Then a fall of rubble, or anything else: here, simply, a touch with nothing to keep her from it. It costs a heart, and that is all:
            // she is still in there, the way is still shut, and whoever waits has lost what they had lost.
            foe = waiting(run);
            int left = foe.left;
            touched(run);
            assertEquals("land " + land, 1, run.hurts());
            hold(run, .1f, false, false, false);
            // (A heart; and in the later lands, by the land's level, half a heart or a heart more.)
            int costs = 2 + RunLevels.biteOf(land);
            assertEquals(costs - 2, run.bite());
            assertEquals("land " + land, PlusRun.HEARTS * 2 - costs, run.halves());
            assertEquals("land " + land, PlusRun.LIVES, run.lives());
            assertTrue("land " + land, run.locked());
            assertTrue("land " + land, run.x() > level.arena);
            assertFalse("land " + land, foe.away);
            assertEquals("land " + land, left, foe.left);
            assertEquals("land " + land, 1, run.phase(foe));
            assertEquals(1, run.entrances());
            // Twice more, and her hearts are gone: that is a life.
            while (run.livesLost() == 0 && run.hurts() < 9) touched(run);
            assertEquals("land " + land, (PlusRun.HEARTS * 2 + costs - 1) / costs, run.hurts());
            hold(run, .1f, false, false, false);
            assertEquals("land " + land, PlusRun.LIVES - 1, run.lives());
            assertEquals("land " + land, PlusRun.HEARTS, run.hearts());
            // She is at the flag at the door, outside; no one is there; and what will do against them lies at the door.
            assertEquals("land " + land, level.arena - 3 + (1 - PlusRun.WIDE) / 2, run.x(), .01f);
            assertFalse(run.locked());
            boolean lies = false;
            for (int[] arm : run.arms()) lies |= arm[1] == level.arena - 2;
            assertTrue("land " + land, lies);
            for (PlusRun.Foe one : run.foes()) {
                assertFalse("land " + land, one.spawned);
                if (!RunLevels.boss(one.kind) || one.flat) continue;
                assertTrue(one.away);
                // She left: they have back all they had lost.
                assertEquals("land " + land, one.most, one.left);
                assertEquals("land " + land, 0, run.phase(one));
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
            if (pressable(run, gari)) touch(run, gari);
            else park(run);
        }
        assertEquals(PlusRun.health(RunLevels.GARI), run.blows());
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
            if (!has && pressable(run, foe)) {
                PlusRun.Foe zero = foe;
                run.place(zero.x + zero.wide() / 2 - PlusRun.WIDE / 2, zero.y + .1f);
                run.step(STEP, false, false, false);
            } else {
                park(run);
            }
        }
        assertEquals(4, foe.mode);
        // (Changing, he has shut her colour off: it is put on her again, for the purposes of this.)
        if (run.form() != PlusRun.YELLOW) run.lend(PlusRun.YELLOW);
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
            // (His shield is broken first: a blow lands on him only then.)
            for (int step = 0; step < 60 * 60 && !armed.open(zero); step++) {
                if (pressable(armed, zero)) touch(armed, zero);
                else park(armed);
            }
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
            for (int step = 0; step < 60 * 60 && !(!armed.absent(she) && she.dazed <= 0 && (spent ? armed.open(she) : she.step == 0 && she.act > .7f)); step++) {
                // (Spent now means her shield broken: it is broken for her.)
                if (spent && pressable(armed, she)) touch(armed, she);
                else park(armed);
            }
            boolean right = she.x > with.arena + 11;
            armed.place(right ? she.x - 3 : she.x + she.wide() + 2.4f, floor - PlusRun.TALL);
            armed.step(STEP, !right, right, false);
            int turned = armed.turned();
            armed.step(STEP, false, false, false, true);
            hold(armed, .25f, false, false, false);
            assertEquals(spent ? 1 : 0, armed.blows());
            assertEquals(spent ? turned : turned + 1, armed.turned());
        }
        // The Mawang's black ribbon: it does not hurt; it is black put on her, for twice as long as a ribbon lasts.
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
        assertEquals(1, run.curses());
        assertEquals(PlusRun.BLACKENED, run.cursedFor(), 0);
        assertTrue(run.formLeft() > PlusRun.BLACKENED - .5f);
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
        assertEquals(3 * PlusRun.BLOW, big.left);
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
                if (one[0] != kind || (one[7] == 1) != hidden) continue;
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
        // It is a way of its own, several screens long, with a ribbon up out of the way and a chest by its far door.
        RunLevels.Level level = run.level();
        assertTrue(one[4] - one[3] >= 70);
        int ribbons = 0, chests = 0;
        for (int[] item : level.items) {
            if (item[1] < one[3] || item[1] > one[4]) continue;
            if (item[0] > PlusRun.FORM && item[0] < PlusRun.CHEST) ribbons++;
            if (item[0] == PlusRun.CHEST) {
                chests++;
                assertEquals(one[8] - 3, item[1]);
                assertEquals(FLOOR - 1, item[2]);
            }
        }
        assertEquals(1, ribbons);
        assertEquals(1, chests);
        // The chest by the far door is opened by touching it: ten pluses, once.
        int bonus = run.bonus();
        run.place(one[8] - 3 + (1 - PlusRun.WIDE) / 2, GROUND);
        hold(run, .2f, false, false, false);
        assertEquals(1, run.chests());
        assertEquals(bonus + 10, run.bonus());
        // At its far door, and out beyond the crowd it passes.
        run.place(one[8] + (1 - PlusRun.WIDE) / 2, GROUND);
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

    /** Lets a cellar's wave be announced and its creatures come in, without her doing anything; returns how many came in. */
    private static int arrived(PlusRun run) {
        for (int step = 0; step < 60 * 10 && run.wavePhase() != PlusRun.FOUGHT; step++) run.step(STEP, false, false, false);
        for (int step = 0; step < 60 * 10 && !run.pending().isEmpty(); step++) run.step(STEP, false, false, false);
        int there = 0;
        for (PlusRun.Foe foe : run.foes()) if (foe.spawned && !foe.flat) there++;
        return there;
    }

    /** Deals with the whole of a cellar's wave, as if she had: those that are there, and each of those that come after as they come; and the wave is over. */
    private static void dealt(PlusRun run) {
        int wave = run.wave();
        for (int step = 0; step < 60 * 400 && run.wave() == wave && run.wavePhase() == PlusRun.FOUGHT; step++) {
            for (PlusRun.Foe foe : run.foes()) if (foe.spawned && !foe.flat) foe.flat = true;
            run.shots().clear();
            run.step(STEP, false, false, false);
        }
    }

    /** Deals with those of a cellar's wave as they come, until one of those who wait is among them: the Old Book, where he sleeps. */
    private static void thinned(PlusRun run) {
        for (int step = 0; step < 60 * 400; step++) {
            boolean waits = false;
            for (PlusRun.Foe foe : run.foes()) waits |= foe.spawned && !foe.flat && RunLevels.boss(foe.kind);
            if (waits) return;
            run.shots().clear();
            for (PlusRun.Foe foe : run.foes()) if (foe.spawned && !foe.flat) foe.flat = true;
            run.shots().clear();
            run.step(STEP, false, false, false);
        }
    }

    @Test public void aCellarIsAnArenaWhoseThreeWavesAreAnnouncedAndMarkedAndNoTwoAlike() {
        int[] which = new int[1];
        PlusRun run = atDoor(RunLevels.SHAPED_FROM, RunLevels.ARENA, false, which);
        int[] one = run.level().rooms.get(which[0]);
        int number = run.level().number, bonus = run.bonus();
        assertTrue(run.enter());
        assertTrue(run.sealed());
        assertEquals(1, run.wave());
        assertEquals(one[11] - PlusRun.TALL, run.y(), .01f);
        for (int wave = 1; wave <= PlusRun.WAVES; wave++) {
            // Announced: for a beat nothing is there and nothing is marked.
            assertEquals(wave, run.wave());
            assertEquals(PlusRun.ANNOUNCED, run.wavePhase());
            assertTrue(run.pending().isEmpty());
            hold(run, PlusRun.ANNOUNCE - .5f, false, false, false);
            assertEquals(PlusRun.ANNOUNCED, run.wavePhase());
            for (PlusRun.Foe foe : run.foes()) assertFalse(foe.spawned && !foe.flat);
            hold(run, .6f, false, false, false);
            // Fought: every one of the wave is marked where it will come in, before it is there, inside the arena and not on top of her.
            assertEquals(PlusRun.FOUGHT, run.wavePhase());
            // (As many as the place holds at once; the rest of the wave wait their turn, and are counted as left.)
            int[] host = RunLevels.host(number, wave, one[10] != 0);
            int first = Math.min(PlusRun.CEILING, host.length);
            assertEquals(first, run.pending().size());
            assertEquals(host.length, run.waveLeft());
            for (float[] mark : run.pending()) {
                assertTrue(mark[1] > one[3] && mark[1] < one[4] + 1 && mark[2] > one[12] && mark[2] <= one[11]);
                assertTrue(mark[3] > .5f);
                assertTrue(Math.abs(mark[1] - run.x()) > 5 || Math.abs(mark[2] - one[11]) > 2.5f);
            }
            for (PlusRun.Foe foe : run.foes()) assertFalse(foe.spawned && !foe.flat);
            // Shut until the last of them is dealt with: there is no door to use.
            assertEquals(-1, run.door());
            assertEquals(wave == 2, run.dark());
            assertEquals(first, arrived(run));
            assertEquals(host.length, run.waveLeft());
            for (PlusRun.Foe foe : run.foes()) {
                if (!foe.spawned || foe.flat) continue;
                assertTrue("at " + foe.x, foe.x >= one[3] - .5f && foe.x <= one[4] + 1);
            }
            // Things to fight with lie on three of its storeys for each wave; in the last one of them is a book.
            int lying = 0, books = 0;
            java.util.Set<Integer> storeys = new java.util.TreeSet<>();
            for (int[] arm : run.arms()) {
                if (arm[1] < one[3] || arm[1] > one[4]) continue;
                lying++;
                storeys.add(arm[2]);
                if (arm[0] == PlusRun.BOOK) books++;
            }
            assertTrue("lying " + lying + " on " + storeys, lying >= 3 && storeys.size() >= 2);
            assertEquals(wave == PlusRun.WAVES ? 1 : 0, books);
            if (wave == PlusRun.WAVES) {
                // The last: something to help lies in the place for it, a ribbon.
                int ribbons = 0;
                for (int[] item : run.items()) if (item[0] > PlusRun.FORM && item[0] < PlusRun.CHEST && item[1] >= one[3] && item[1] <= one[4]) ribbons++;
                assertEquals(1, ribbons);
            }
            // One for each that falls, and never more there at once than the place holds; the outsized ones, which take six, at the end.
            int seen = 0, most = 0, giants = 0, left = host.length;
            java.util.Set<PlusRun.Foe> counted = new java.util.HashSet<>();
            for (int step = 0; step < 60 * 400 && run.wave() == wave && run.wavePhase() == PlusRun.FOUGHT; step++) {
                int there = run.pending().size();
                for (PlusRun.Foe foe : run.foes()) {
                    if (!foe.spawned || foe.flat) continue;
                    there++;
                    if (counted.add(foe)) {
                        seen++;
                        if (foe.size > 1.5f && foe.left == 6 * PlusRun.healthOf(foe.kind)) {
                            giants++;
                            assertTrue("giant came as " + seen + " of " + host.length, seen > host.length - RunLevels.GIANTS - (one[10] != 0 ? 1 : 0));
                        }
                    }
                }
                most = Math.max(most, there);
                assertTrue(run.waveLeft() <= left);
                left = run.waveLeft();
                // (One of them is dealt with every tenth of a second.)
                if (step % 6 == 0) for (PlusRun.Foe foe : run.foes()) if (foe.spawned && !foe.flat) {
                    foe.flat = true;
                    break;
                }
                run.shots().clear();
                run.step(STEP, false, false, false);
            }
            // (A few come twice: one that was far from her is gone from where it was and comes in again nearer.)
            assertTrue("seen " + seen + " of " + host.length, seen >= host.length && seen <= host.length + 30);
            assertEquals(PlusRun.CEILING, most);
            assertEquals(wave < PlusRun.WAVES ? 0 : one[10] != 0 ? RunLevels.GIANTS - 1 : RunLevels.GIANTS, giants);
            if (wave < PlusRun.WAVES) {
                // Cleared: a breath before the next.
                assertEquals(PlusRun.CLEARED, run.wavePhase());
                hold(run, PlusRun.BREATH - .2f, false, false, false);
                assertEquals(PlusRun.CLEARED, run.wavePhase());
                hold(run, .3f, false, false, false);
            }
        }
        assertEquals(0, run.wave());
        assertEquals(0, run.wavePhase());
        assertFalse(run.sealed());
        assertEquals(1, run.roomWins());
        assertEquals(1 << which[0], run.roomsDone());
        // What it is worth is by how it was fought: here, without a hurt and in no time, the best.
        assertEquals(PlusRun.RANK_S, run.ranked());
        assertEquals(bonus + PlusRun.RANK_PAYS[PlusRun.RANK_S] + (one[10] != 0 ? 20 : 0), run.bonus());
        assertEquals(PlusRun.RANK_S, PlusRun.rank(60, 0, 0));
        assertEquals(PlusRun.RANK_S, PlusRun.rank(PlusRun.PAR, 2, 0));
        // (How long it took counts for nothing.)
        assertEquals(PlusRun.RANK_S, PlusRun.rank(PlusRun.PAR * 10, 0, 0));
        assertEquals(PlusRun.RANK_A, PlusRun.rank(60, 3, 0));
        assertEquals(PlusRun.RANK_A, PlusRun.rank(60, 8, 0));
        assertEquals(PlusRun.RANK_B, PlusRun.rank(60, 9, 0));
        assertEquals(PlusRun.RANK_C, PlusRun.rank(60, 0, 1));
        // A wave is its troop twenty times over; and the whole cellar some hundreds.
        int whole = 0;
        for (int wave = 1; wave <= PlusRun.WAVES; wave++) {
            int[] troop = RunLevels.wave(number, wave, false), host = RunLevels.host(number, wave, false);
            assertEquals((troop.length - (wave == PlusRun.WAVES ? 1 : 0)) * RunLevels.SWELL + (wave == PlusRun.WAVES ? RunLevels.GIANTS : 0), host.length);
            whole += host.length;
        }
        assertEquals(20, RunLevels.SWELL);
        assertTrue("whole " + whole, whole >= 300);
        assertTrue(PlusRun.RANK_PAYS[PlusRun.RANK_S] > PlusRun.RANK_PAYS[PlusRun.RANK_A] && PlusRun.RANK_PAYS[PlusRun.RANK_A] > PlusRun.RANK_PAYS[PlusRun.RANK_B]
                && PlusRun.RANK_PAYS[PlusRun.RANK_B] > PlusRun.RANK_PAYS[PlusRun.RANK_C]);
        run.place(one[6] + (1 - PlusRun.WIDE) / 2, one[11] - PlusRun.TALL);
        hold(run, .2f, false, false, false);
        assertEquals(which[0], run.door());
        assertTrue(run.enter());
        assertEquals(-1, run.room());
        // It can be fought again, for how well it is fought: gone into again, its first wave is announced again.
        hold(run, .2f, false, false, false);
        assertTrue(run.enter());
        assertEquals(1, run.wave());
        assertEquals(PlusRun.ANNOUNCED, run.wavePhase());
        assertTrue(run.sealed());

        // A hurt in there costs a heart and leaves the wave as it is, and is counted against how the cellar is fought. Her hearts gone,
        // a life: she is back at the cellar's door, inside it, and the wave she was in is announced again, whole. The cellar is not left.
        run = atDoor(RunLevels.SHAPED_FROM, RunLevels.ARENA, false, which);
        assertTrue(run.enter());
        dealt(run);
        arrived(run);
        dealt(run);
        hold(run, PlusRun.BREATH + .1f, false, false, false);
        assertEquals(2, run.wave());
        int came = arrived(run);
        for (int step = 0; step < 60 * 90 && run.livesLost() == 0; step++) {
            PlusRun.Foe near = null;
            for (PlusRun.Foe foe : run.foes()) if (foe.spawned && !foe.flat && foe.dazed <= 0 && foe.kind != RunLevels.DRAGON) near = foe;
            if (near != null && !run.safe()) run.place(near.x + near.wide() - .1f, near.y + near.tall() - PlusRun.TALL);
            int hurts = run.hurts();
            run.step(STEP, false, false, false);
            if (run.hurts() > hurts && run.livesLost() == 0) {
                assertEquals(which[0], run.room());
                assertTrue(run.sealed());
                assertEquals(2, run.wave());
                assertEquals(PlusRun.FOUGHT, run.wavePhase());
            }
        }
        assertEquals(1, run.livesLost());
        assertTrue("hurts " + run.arenaHurts(), run.arenaHurts() >= PlusRun.HEARTS);
        assertEquals(1, run.arenaLives());
        hold(run, .05f, false, false, false);
        assertEquals(which[0], run.room());
        assertEquals(one[6] + (1 - PlusRun.WIDE) / 2, run.x(), .3f);
        assertEquals(2, run.wave());
        assertEquals(PlusRun.ANNOUNCED, run.wavePhase());
        for (PlusRun.Foe foe : run.foes()) assertFalse(foe.spawned);
        assertTrue(run.pending().isEmpty());
        assertEquals(came, arrived(run));
        assertEquals(0, run.roomsDone());
    }

    @Test public void everyWaveOfEveryCellarIsDifferentFromTheLastAndTheLastEndsOnSomethingParticular() {
        int cellars = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            for (boolean den : new boolean[]{false, true}) {
                int[] before = null;
                for (int wave = 1; wave <= PlusRun.WAVES; wave++) {
                    int[] kinds = RunLevels.wave(number, wave, den);
                    java.util.Set<Integer> seen = new java.util.TreeSet<>();
                    for (int kind : kinds) seen.add(kind % RunLevels.GIANT);
                    // More in each than in the last; no two alike; and after the first chapter none is all of one kind.
                    if (before != null) {
                        assertTrue("land " + number + " wave " + wave, kinds.length > before.length);
                        int[] sorted = kinds.clone(), last = before.clone();
                        java.util.Arrays.sort(sorted);
                        java.util.Arrays.sort(last);
                        assertFalse(java.util.Arrays.equals(sorted, last));
                        java.util.Set<Integer> was = new java.util.TreeSet<>();
                        for (int kind : before) was.add(kind % RunLevels.GIANT);
                        assertFalse("land " + number + " wave " + wave, seen.equals(was));
                    }
                    if (RunLevels.chapterOf(number) >= 1 || wave > 1) assertTrue("land " + number + " wave " + wave, seen.size() >= 2);
                    // The first is of those that walk; the second brings what throws, shoots, hops or flies; the last ends on the Old Book
                    // where he sleeps, and on an outsized one where he does not, and nothing else in it is either.
                    for (int index = 0; index < kinds.length; index++) {
                        boolean lastOne = wave == PlusRun.WAVES && index == kinds.length - 1;
                        assertEquals("land " + number, lastOne && den, kinds[index] == RunLevels.OLD_BOOK);
                        assertEquals("land " + number, lastOne && !den, kinds[index] >= RunLevels.GIANT);
                        assertFalse(RunLevels.boss(kinds[index] % RunLevels.GIANT) && kinds[index] != RunLevels.OLD_BOOK);
                    }
                    if (wave == 2) assertTrue(seen.contains(RunLevels.DIVIDE) || seen.contains(RunLevels.THROWER) || seen.contains(RunLevels.DRAGON));
                    before = kinds;
                }
            }
            // Cellars differ by chapter: the first wave of a land of one chapter is not that of the same land of the chapter after.
            if (number + 10 <= RunLevels.TOTAL && RunLevels.chapterOf(number) != RunLevels.chapterOf(number + 10) && number > RunLevels.COUNT) {
                assertFalse(java.util.Arrays.equals(RunLevels.wave(number, 1, false), RunLevels.wave(number + 10, 1, false)));
            }
            for (int[] room : RunLevels.level(number).rooms) if (room[0] == RunLevels.ARENA) cellars++;
        }
        assertTrue("cellars " + cellars, cellars >= 20);
    }

    /** Whether she could stand at a square of a land: something under it that holds her, and room over it. */
    private static boolean stands(RunLevels.Level level, int column, int row) {
        if (row < 2 || row >= level.land.length || column < 0 || column >= level.columns) return false;
        char under = level.land[row][column], here = level.land[row - 1][column], over = level.land[row - 2][column];
        boolean holds = under == RunLevels.BLOCK || under == RunLevels.LEDGE || under == RunLevels.THIN || under == RunLevels.CRUMBLE;
        return holds && here != RunLevels.BLOCK && over != RunLevels.BLOCK;
    }

    @Test public void anArenaIsTenTimesTheOldCellarAndEveryPlatformInItCanBeReached() {
        java.util.Set<Integer> styles = new java.util.TreeSet<>();
        int arenas = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int index = 0; index < level.rooms.size(); index++) {
                int[] one = level.rooms.get(index);
                if (one[0] != RunLevels.ARENA) continue;
                arenas++;
                styles.add(RunLevels.chapterOf(number) * 3 + number / 3 % 3);
                int from = one[3], to = one[4], floor = one[11], top = one[12];
                String where = "land " + number;
                // A hundred and twenty across and forty-three tall, on rows of its own; walled all round, on every row.
                assertEquals(where, RunLevels.DEEP, level.land.length);
                assertEquals(where, RunLevels.ARENA_WIDE, to - from + 1);
                assertEquals(where, 43, floor - top);
                assertTrue(where, (to - from + 1) * (floor - top) >= 10 * 18 * 8 * 3);
                for (int row = 0; row < level.land.length; row++) {
                    assertEquals(where, RunLevels.BLOCK, level.land[row][from - 1]);
                    assertEquals(where, RunLevels.BLOCK, level.land[row][to + 1]);
                }
                for (int column = from; column <= to; column++) {
                    assertEquals(RunLevels.BLOCK, level.land[floor][column]);
                    assertEquals(RunLevels.BLOCK, level.land[top - 1][column]);
                }
                assertEquals(RunLevels.ENTRY, level.land[floor - 1][one[6]]);
                // Storeys of platforms: on each of eight, four squares apart, there is platform, and a good deal of it in all.
                int platform = 0;
                for (int storey = 1; storey <= RunLevels.STOREYS; storey++) {
                    int row = floor - storey * RunLevels.STOREY, on = 0;
                    for (int column = from; column <= to; column++) if (level.land[row][column] == RunLevels.LEDGE) on++;
                    assertTrue(where + " storey " + storey + " has " + on, on >= 12);
                    platform += on;
                }
                assertTrue(where + " platform " + platform, platform >= 240);
                // Its loft, with the chest: nothing within a jump of it.
                int[] loft = null;
                for (int[] each : level.lofts) if (each[0] == index) loft = each;
                assertTrue(where, loft != null);
                for (int column = loft[1] - 8; column < loft[1]; column++) {
                    for (int row = loft[3] - 3; row <= loft[3] + 5; row++) assertFalse(where, stands(level, column, row));
                }
                int chests = 0;
                for (int[] item : level.items) if (item[0] == PlusRun.CHEST && item[1] >= loft[1] && item[1] <= loft[2] && item[2] == loft[3] - 1) chests++;
                assertEquals(where, 1, chests);
                // Every other square she could stand on can be got to from the door: by walking, by a jump of two up and three across or
                // four across on the level, by dropping, by a spring, or by the ledge that rides up and down.
                boolean[][] reached = new boolean[level.land.length][level.columns];
                java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
                reached[floor][one[6]] = true;
                queue.add(new int[]{one[6], floor});
                while (!queue.isEmpty()) {
                    int[] at = queue.poll();
                    boolean spring = level.land[at[1] - 1][at[0]] == RunLevels.SPRING;
                    for (int across = -5; across <= 5; across++) {
                        int column = at[0] + across;
                        if (column < from || column > to) continue;
                        for (int row = top + 2; row <= floor; row++) {
                            if (reached[row][column] || !stands(level, column, row)) continue;
                            int up = at[1] - row;
                            boolean can = up == 0 && Math.abs(across) <= 4 || up > 0 && up <= 2 && Math.abs(across) <= 3 || up < 0 && Math.abs(across) <= 5
                                    || spring && up > 0 && up <= 6 && Math.abs(across) <= 3;
                            if (!can) continue;
                            reached[row][column] = true;
                            queue.add(new int[]{column, row});
                        }
                    }
                    // The ledge that rides: from beside where it comes down, to wherever it passes.
                    for (int[] mover : level.movers) {
                        if (mover[4] == 0 || mover[0] < from || mover[0] > to || Math.abs(at[0] - mover[0] - 1) > 4 || at[1] < mover[1] - mover[3] || at[1] > mover[1] + 1) continue;
                        for (int row = mover[1] - mover[3]; row <= mover[1]; row++) {
                            for (int column = mover[0] - 4; column <= mover[0] + mover[2] + 3; column++) {
                                if (column < from || column > to || reached[row][column] || !stands(level, column, row)) continue;
                                reached[row][column] = true;
                                queue.add(new int[]{column, row});
                            }
                        }
                    }
                }
                for (int row = top + 2; row <= floor; row++) {
                    for (int column = from; column <= to; column++) {
                        boolean lofty = row == loft[3] && column >= loft[1] && column <= loft[2];
                        if (stands(level, column, row) && !lofty) assertTrue(where + " cannot get to " + (column - from) + "/" + (floor - row), reached[row][column]);
                    }
                }
                // Where a wave may come in: on firm ground with room over it, or, for what flies, in the open air; and there is plenty of each.
                int[] counts = new int[4];
                for (int[] spot : level.spawns) {
                    if (spot[0] != index) continue;
                    counts[spot[1]]++;
                    assertTrue(where, spot[2] > from && spot[2] < to);
                    if (spot[1] == RunLevels.IN_AIR) {
                        for (int across = -1; across <= 1; across++) for (int down = -1; down <= 1; down++) assertEquals(RunLevels.AIR, level.land[spot[3] + down][spot[2] + across]);
                    } else {
                        assertTrue(where + " at " + (spot[2] - from) + "/" + (floor - spot[3]), stands(level, spot[2], spot[3]));
                        assertEquals(RunLevels.AIR, level.land[spot[3] - 1][spot[2]]);
                        assertTrue(reached[spot[3]][spot[2]]);
                    }
                }
                assertTrue(where + " floor " + counts[0], counts[RunLevels.ON_FLOOR] >= 8);
                assertTrue(where + " platform " + counts[1], counts[RunLevels.ON_PLATFORM] >= 6);
                assertTrue(where + " perch " + counts[2], counts[RunLevels.ON_PERCH] >= 4);
                assertTrue(where + " air " + counts[3], counts[RunLevels.IN_AIR] >= 4);
            }
        }
        assertTrue(arenas >= 20);
        // Three ways of laying it in every chapter that has three cellars.
        for (int chapter = 1; chapter < RunLevels.ALL_CHAPTERS; chapter++) {
            int ways = 0;
            for (int style = 0; style < 3; style++) if (styles.contains(chapter * 3 + style)) ways++;
            assertTrue("chapter " + chapter + " has " + ways, ways >= 2);
        }
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
            // She goes for the lowest of those she can still be under in time.
            PlusRun.Shot best = null;
            for (PlusRun.Shot shot : run.shots()) {
                if (shot.kind != PlusRun.PLUSDROP) continue;
                float left = (one[11] - 1.2f - shot.y) / 5.5f;
                if (Math.abs(shot.x - run.x() - PlusRun.WIDE / 2) > 5.6f * left + .3f) continue;
                if (best == null || shot.y > best.y) best = shot;
            }
            float want = best == null ? (one[3] + one[4]) / 2f : best.x - PlusRun.WIDE / 2;
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

    @Test public void aStashIsAClimbToAChestAndSignsKeepComingInTheRoomWhereTheyAreDealtWith() {
        // Both kinds of stash: steps, and springs. The chest is seen from the door and not reached from it; reached, it is opened, and that
        // is the stash done, once.
        boolean[] seen = new boolean[2];
        for (int number = RunLevels.PLACES_FROM; number <= RunLevels.TOTAL && !(seen[0] && seen[1]); number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int index = 0; index < level.rooms.size(); index++) {
                int[] one = level.rooms.get(index);
                if (one[0] != RunLevels.STASH || seen[number % 4 < 2 ? 0 : 1]) continue;
                seen[number % 4 < 2 ? 0 : 1] = true;
                int[] chest = null;
                for (int[] item : level.items) if (item[0] == PlusRun.CHEST && item[1] >= one[3] && item[1] <= one[4]) chest = item;
                PlusRun run = new PlusRun(level);
                run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
                run.step(STEP, false, false, false);
                if (one[7] == 1) assertTrue(run.enter());
                assertTrue(run.enter());
                assertEquals(index, run.room());
                assertEquals(0, run.roomsDone());
                // Walking the floor from end to end does not reach it.
                hold(run, 4, false, true, false);
                assertEquals("land " + number, 0, run.chests());
                assertEquals(0, run.roomWins());
                // Standing where it is does.
                int bonus = run.bonus();
                run.place(chest[1] + (1 - PlusRun.WIDE) / 2, chest[2] + 1 - PlusRun.TALL);
                hold(run, .3f, false, false, false);
                assertEquals("land " + number, 1, run.chests());
                assertEquals(1 << index, run.roomsDone());
                assertEquals(1, run.roomWins());
                assertEquals(bonus + 10, run.bonus());
                // And there is ground under it to stand on: a shelf.
                assertEquals("land " + number, RunLevels.LEDGE, level.land[chest[2] + 1][chest[1]]);
            }
        }
        assertTrue(seen[0] && seen[1]);

        // The room where signs keep coming: for fifteen seconds there are never more than five of them, each one dealt with counts, and
        // at the end the room is done and whoever is left is gone.
        int[] which = new int[1];
        PlusRun run = atDoor(RunLevels.SHAPED_FROM, RunLevels.TRAMPLE, false, which);
        int bonus = run.bonus();
        assertTrue(run.enter());
        assertTrue(run.sealed());
        assertEquals(15, run.gameLeft(), .01f);
        assertEquals(15, run.gameLasts(), .01f);
        int most = 0;
        for (int step = 0; step < 60 * 16 && run.gameLeft() > 0; step++) {
            PlusRun.Foe near = null;
            for (PlusRun.Foe foe : run.foes()) if (foe.spawned && !foe.flat && foe.dazed <= 0) near = foe;
            most = Math.max(most, run.waveLeft());
            // Down onto each of them as it comes.
            if (near != null && step % 20 == 0) run.place(near.x + near.wide() / 2 - PlusRun.WIDE / 2, near.y - PlusRun.TALL - .6f);
            run.step(STEP, false, false, false);
        }
        assertTrue("most " + most, most >= 1 && most <= 5);
        assertFalse(run.sealed());
        assertTrue("dealt with " + run.caught(), run.caught() >= PlusRun.ENOUGH);
        // A plus for each, ten for having dealt with enough, and whatever landing on one after another is worth.
        assertTrue(run.bonus() >= bonus + run.caught() + 10);
        assertEquals(1, run.roomWins());
        assertEquals(0, run.waveLeft());
        assertEquals(0, run.hurts());
    }

    @Test public void everyLandWithShapeHasPlacesOffItWalledAllRound() {
        boolean[] kinds = new boolean[RunLevels.HOUSE + 1];
        int raised = 0, passages = 0, dens = 0;
        int hidden = 0;
        // (The first land has nothing off it but the two buildings of its village.)
        for (int[] one : RunLevels.level(1).rooms) assertEquals(RunLevels.HOUSE, one[0]);
        for (int number = RunLevels.PLACES_FROM; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            // A door in its village from the second land; from the second chapter, in two lands of three, another by its first crowd.
            assertTrue("land " + number + " has " + level.rooms.size(), level.rooms.size() >= (level.chapter >= 1 && number % 3 != 1 || level.chapter >= 2 ? 2 : 1));
            assertTrue(level.main > level.gate && level.main < level.columns);
            for (int[] one : level.rooms) {
                // (A hideout and a lagoon have tests of their own: RunSecretsTest, RunWaterTest.)
                if (one[0] == RunLevels.HIDEOUT || one[0] == RunLevels.LAGOON) continue;
                kinds[one[0]] = true;
                if (one[7] != 0) hidden++;
                // Its door is in the land proper, on the ground; the place itself is beyond the end of it.
                assertTrue("land " + number, one[1] < level.gate && one[3] >= level.main && one[4] < level.columns - 1);
                // (A building's door is not marked in the land itself: the land is laid about as it was before there were buildings to go into.)
                if (one[0] != RunLevels.HOUSE) assertEquals("land " + number, RunLevels.ENTRY, level.land[one[2] - 1][one[1]]);
                // On the ground, or, up out of the way, on a ledge of its own a jump over the ledge that is over a crowd.
                if (one[7] == 2) {
                    raised++;
                    assertEquals(RunLevels.LEDGE, level.land[one[2]][one[1]]);
                    assertEquals(RunLevels.LEDGE, level.land[one[2] + 2][one[1] - 1]);
                } else {
                    assertEquals("land " + number, one[2], RunLevels.top(level, one[1]));
                }
                if (one[0] == RunLevels.PASSAGE) passages++;
                if (one[10] != 0) dens++;
                assertEquals(RunLevels.ENTRY, level.land[one[11] - 1][one[6]]);
                if (one[0] == RunLevels.ARENA) continue;
                for (int row = 0; row < RunLevels.ROWS; row++) {
                    assertEquals(RunLevels.BLOCK, level.land[row][one[3] - 1]);
                    assertEquals(RunLevels.BLOCK, level.land[row][one[4] + 1]);
                }
                for (int column = one[3]; column <= one[4]; column++) {
                    assertEquals(RunLevels.BLOCK, level.land[FLOOR][column]);
                    assertEquals(RunLevels.BLOCK, level.land[FLOOR - RunLevels.ROOM_TALL - 1][column]);
                    // (The rooms where a game is played are open from wall to wall; a stash and a passage have things in the way.)
                    if (one[0] < RunLevels.GAME) continue;
                    assertEquals(RunLevels.AIR, level.land[FLOOR - RunLevels.ROOM_TALL][column]);
                    assertFalse(PlusRun.solid(level.land[FLOOR - 1][column]));
                }
                // Each has a look of its own kind: one of the eight.
                if (one[0] == RunLevels.HOUSE && one[14] == RunLevels.HOME) assertTrue("land " + number, one[13] >= RunLevels.HOME_LOOK && one[13] < RunLevels.HOME_LOOK + RunLevels.HOME_LOOKS);
                else if (one[0] == RunLevels.HOUSE) assertEquals("land " + number, RunLevels.LOOKS + one[14], one[13]);
                else assertTrue("land " + number, one[13] >= 0 && one[13] < RunLevels.LOOKS);
                // A passage comes out on the ground further on, at a door of its own.
                if (one[0] == RunLevels.PASSAGE) {
                    assertTrue(one[5] > one[1] && one[5] < level.gate && one[8] > one[6]);
                    assertEquals(RunLevels.ENTRY, level.land[one[9] - 1][one[5]]);
                    assertEquals(RunLevels.ENTRY, level.land[FLOOR - 1][one[8]]);
                } else {
                    assertEquals(one[1], one[5]);
                }
            }
            // They are places to move in: a stash and the rooms for games twenty squares and more across, a cellar wider, a passage twice that.
            for (int[] one : level.rooms) {
                int wide = one[4] - one[3] + 1;
                assertTrue("land " + number + " kind " + one[0] + " is " + wide, wide >= (one[0] == RunLevels.PASSAGE ? 80 : one[0] == RunLevels.ARENA ? 100 : one[0] == RunLevels.STASH ? 60 : one[0] == RunLevels.HOUSE ? 14 : 30));
                // A stash has a chest, which is not on its floor; a passage has one too.
                int chests = 0;
                for (int[] item : level.items) {
                    if (item[0] != PlusRun.CHEST || item[1] < one[3] || item[1] > one[4]) continue;
                    chests++;
                    if (one[0] == RunLevels.STASH) assertTrue("land " + number, item[2] <= FLOOR - 6);
                }
                // (A home may have one behind a false wall of its own.)
                if (one[0] == RunLevels.HOUSE && one[14] == RunLevels.HOME) assertTrue("land " + number, chests <= 1);
                else assertEquals("land " + number, one[0] == RunLevels.STASH || one[0] == RunLevels.PASSAGE || one[0] == RunLevels.ARENA || one[0] == RunLevels.LAGOON ? 1 : 0, chests);
            }
            // No sign of the land is in them but those that walk in its passages, and nothing of the land's to fight with.
            for (int[] foe : level.foes) {
                boolean passing = false;
                // (And a hideout's hidden boss.)
                for (int[] one : level.rooms) passing |= (one[0] <= RunLevels.PASSAGE || one[0] == RunLevels.HIDEOUT || one[0] == RunLevels.LAGOON) && foe[1] >= one[3] && foe[1] <= one[4];
                assertTrue(foe[1] < level.main || passing);
            }
            for (int[] weapon : level.weapons) assertTrue(weapon[1] < level.main);
        }
        for (int kind = 0; kind <= RunLevels.TRAMPLE; kind++) assertTrue("no place of kind " + kind, kinds[kind]);
        // Some doors are up out of the way; passages are in two lands of three once there are crowds for them to pass; and the Old Book sleeps in a good few cellars.
        assertTrue("raised " + raised, raised >= 8);
        assertTrue("passages " + passages, passages >= 35);
        assertTrue("dens " + dens, dens >= 8);
        assertTrue(hidden >= 20);
    }

    /** Whether she could stand on a square of a place off the land, with its puzzles solved or not: never in spikes, and never inside a door, a bridge that is not there, or anything else. */
    private static boolean rests(RunLevels.Level level, int column, int row, int solved) {
        if (row < 3 || row > FLOOR || column < 1 || column >= level.columns - 1) return false;
        char under = seen(level, column, row, solved), here = seen(level, column, row - 1, solved), over = seen(level, column, row - 2, solved);
        boolean holds = under == RunLevels.BLOCK || under == RunLevels.LEDGE || under == RunLevels.THIN || under == RunLevels.CRUMBLE;
        return holds && here != RunLevels.BLOCK && here != RunLevels.SPIKE && over != RunLevels.BLOCK;
    }

    /**
     * A square as it is with the place's bridges raised (1) or not, and its doors unlocked
     * (2) or not. Bars count as lifted either way: there is a plate on each side of them,
     * which is checked apart.
     */
    private static char seen(RunLevels.Level level, int column, int row, int solved) {
        char here = level.land[row][column];
        if (here == RunLevels.LOCKED) return (solved & 2) != 0 ? RunLevels.AIR : RunLevels.BLOCK;
        if (here == RunLevels.SPAN) return (solved & 1) != 0 ? RunLevels.LEDGE : RunLevels.AIR;
        if (here == RunLevels.GRILLE) return RunLevels.AIR;
        return here;
    }

    /**
     * Whether she can get from one square she could stand on to another in one go: a walk
     * or a jump of four across on the level, two up and three across, a drop of any height
     * within five across, or from a spring five up and three across; and never through a
     * wall that stands between them.
     */
    private static boolean goes(RunLevels.Level level, int[] from, int[] to, int solved) {
        int across = Math.abs(to[0] - from[0]), up = from[1] - to[1];
        boolean spring = level.land[from[1] - 1][from[0]] == RunLevels.SPRING;
        boolean can = up == 0 && across <= 4 || up > 0 && up <= 2 && across <= 3 || up < 0 && across <= 5 || spring && up > 0 && up <= 5 && across <= 3;
        if (!can) return false;
        // What is between them, at the height of the higher of the two and for two squares over it, is not wall.
        int high = Math.min(from[1], to[1]);
        for (int column = Math.min(from[0], to[0]) + 1; column < Math.max(from[0], to[0]); column++) {
            if (seen(level, column, high - 1, solved) == RunLevels.BLOCK || seen(level, column, high - 2, solved) == RunLevels.BLOCK) return false;
        }
        // Going up, there is nothing solid over her head where she leaves the ground; a ledge is no matter.
        if (up > 0) for (int row = from[1] - 3; row >= to[1] - 2 && row >= 2; row--) if (seen(level, from[0], row, solved) == RunLevels.BLOCK && across == 0) return false;
        return true;
    }

    /** Every square of a place she can get to from one of them, forwards, or every square she can get to it from, backwards. */
    private static boolean[][] spread(RunLevels.Level level, int[] one, int column, int solved, boolean backwards) {
        boolean[][] reached = new boolean[RunLevels.ROWS][level.columns];
        java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
        reached[FLOOR][column] = true;
        queue.add(new int[]{column, FLOOR});
        while (!queue.isEmpty()) {
            int[] at = queue.poll();
            for (int next = Math.max(one[3], at[0] - 5); next <= Math.min(one[4], at[0] + 5); next++) {
                for (int row = 3; row <= FLOOR; row++) {
                    if (reached[row][next] || !rests(level, next, row, solved)) continue;
                    int[] other = {next, row};
                    if (backwards ? !goes(level, other, at, solved) : !goes(level, at, other, solved)) continue;
                    reached[row][next] = true;
                    queue.add(other);
                }
            }
            // A ledge that rides across spikes: from the floor at one end of its way to the floor at the other, either way.
            for (int[] mover : level.movers) {
                if (mover[4] != 0 || mover[0] < one[3] || mover[0] > one[4]) continue;
                int near = mover[0] - 1, far = mover[0] + mover[2] + mover[3];
                for (int[] pair : new int[][]{{near, far}, {far, near}}) {
                    if (at[0] == pair[0] && at[1] == FLOOR && !reached[FLOOR][pair[1]] && rests(level, pair[1], FLOOR, solved)) {
                        reached[FLOOR][pair[1]] = true;
                        queue.add(new int[]{pair[1], FLOOR});
                    }
                }
            }
        }
        return reached;
    }

    /** Whether something that lies in a place can be touched from somewhere she can get to: from beside it or under it, within a jump. */
    private static boolean touched(boolean[][] reached, int[] item) {
        for (int column = item[1] - 1; column <= item[1] + 1; column++) {
            for (int row = item[2] + 1; row <= Math.min(FLOOR, item[2] + 3); row++) if (reached[row][column]) return true;
        }
        return false;
    }

    @Test public void everyStashAndPassageIsItsOwnAndCanBeGotThroughAndBackWithNoDeadEnd() {
        java.util.Set<String> plans = new java.util.HashSet<>();
        boolean[] pieces = new boolean[RunLevels.PIECES], looks = new boolean[RunLevels.LOOKS], holds = new boolean[3];
        int places = 0, locks = 0, spans = 0, bars = 0, pets = 0, shrines = 0;
        java.util.Map<Integer, java.util.Set<Integer>> byChapter = new java.util.TreeMap<>();
        for (int number = RunLevels.PLACES_FROM; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int[] made : level.made) {
                int[] one = level.rooms.get(made[0]);
                String where = "land " + number + " place " + made[0] + " " + java.util.Arrays.toString(made);
                boolean through = one[0] == RunLevels.PASSAGE;
                assertTrue(where, one[0] == RunLevels.STASH || through);
                places++;
                // Four or five pieces to a stash and six or seven to a passage; no piece twice; a puzzle at most, two in the second
                // volume; and in the first chapter nothing with spikes in it.
                assertEquals(where, true, through ? made.length == 7 || made.length == 8 : made.length == 5 || made.length == 6);
                int puzzles = 0, pits = 0, grilles = 0;
                for (int index = 1; index < made.length; index++) {
                    pieces[made[index]] = true;
                    if (made[index] == RunLevels.SPAN_PIT) pits++;
                    if (made[index] == RunLevels.BARRED) grilles++;
                    for (int before = 1; before < index; before++) assertTrue(where, made[index] != made[before]);
                    if (made[index] == RunLevels.LOCK || made[index] == RunLevels.SPAN_PIT || made[index] == RunLevels.BARRED) puzzles++;
                }
                assertTrue(where, puzzles <= (number > RunLevels.COUNT ? 2 : 1));
                plans.add(one[0] + java.util.Arrays.toString(java.util.Arrays.copyOfRange(made, 1, made.length)));
                looks[one[13]] = true;
                assertEquals(where, RunLevels.lookOf(number, made[0]), one[13]);
                for (int[] other : level.rooms) if (other != one && other[13] >= 0) assertTrue(where, other[13] != one[13]);
                if (!byChapter.containsKey(level.chapter)) byChapter.put(level.chapter, new java.util.TreeSet<>());
                byChapter.get(level.chapter).add(one[13]);
                int door = one[6], floor = one[11];
                assertEquals(where, FLOOR, floor);
                // What is in it.
                int[] chest = null, key = null;
                java.util.List<int[]> levers = new java.util.ArrayList<>(), plates = new java.util.ArrayList<>();
                int spikes = 0;
                for (int[] item : level.items) {
                    if (item[1] < one[3] || item[1] > one[4]) continue;
                    if (item[0] == PlusRun.CHEST) chest = item;
                    if (item[0] == PlusRun.KEY) key = item;
                    if (item[0] == PlusRun.LEVER) levers.add(item);
                    if (item[0] == PlusRun.PLATE) plates.add(item);
                    if (item[0] >= PlusRun.PET) pets++;
                    if (item[0] == PlusRun.SHRINE) {
                        shrines++;
                        assertTrue(where, item[4] >= PlusRun.RED && item[4] <= PlusRun.PURPLE);
                    }
                    // Nothing lies inside anything solid.
                    assertEquals(where + " item " + item[0] + " at " + (item[1] - one[3]), RunLevels.AIR, level.land[item[2]][item[1]]);
                }
                boolean locked = false, spanned = false, barred = false;
                for (int column = one[3]; column <= one[4]; column++) {
                    for (int row = 0; row < RunLevels.ROWS; row++) {
                        locked |= level.land[row][column] == RunLevels.LOCKED;
                        spanned |= level.land[row][column] == RunLevels.SPAN;
                        barred |= level.land[row][column] == RunLevels.GRILLE;
                        if (level.land[row][column] == RunLevels.SPIKE) spikes++;
                    }
                }
                if (level.chapter == 0) assertEquals(where, 0, spikes);
                assertEquals(where, locked, key != null);
                assertEquals(where, pits > 0, spanned);
                assertEquals(where, pits * (through ? 2 : 1), levers.size());
                assertEquals(where, grilles * 2, plates.size());
                if (locked) locks++;
                if (spanned) spans++;
                if (barred) bars++;
                if (!through) holds[one[14]] = true;
                assertTrue(where, chest != null);

                // From its door she solves it in whatever order it is laid in: at every stage, wherever she can get to she can get back
                // from; and there is always the next lever or the key among the things she can touch, until all of it is open.
                int solved = 0, all = (spanned ? 1 : 0) | (locked ? 2 : 0);
                boolean[][] out, home;
                while (true) {
                    out = spread(level, one, door, solved, false);
                    home = spread(level, one, door, solved, true);
                    for (int row = 3; row <= FLOOR; row++) {
                        for (int column = one[3]; column <= one[4]; column++) {
                            if (out[row][column]) assertTrue(where + " solved " + solved + " no way back from " + (column - one[3]) + "/" + (FLOOR - row), home[row][column]);
                        }
                    }
                    boolean lever = false;
                    for (int[] each : levers) lever |= touched(out, each);
                    if ((solved & 1) == 0 && spanned && lever) solved |= 1;
                    else if ((solved & 2) == 0 && key != null && touched(out, key)) solved |= 2;
                    else break;
                }
                assertEquals(where + " is stuck at", all, solved);
                // With all of it open: its chest can be touched; and, in a passage, its far door reached.
                assertTrue(where + " chest", touched(out, chest));
                if (through) {
                    assertTrue(where + " far door", out[FLOOR][one[8]]);
                    // Come into by its far door with nothing solved, she can get back out of that door from wherever she gets to, and there
                    // is a lever on that side for her to pull; and with that done, the way to the near door is open.
                    boolean[][] back = spread(level, one, one[8], 0, false), again = spread(level, one, one[8], 0, true);
                    for (int row = 3; row <= FLOOR; row++) {
                        for (int column = one[3]; column <= one[4]; column++) {
                            if (back[row][column]) assertTrue(where + " no way back to the far door from " + (column - one[3]), again[row][column]);
                        }
                    }
                    if (spanned) assertTrue(where + " far lever", touched(back, levers.get(levers.size() - 1)));
                    assertTrue(where, spread(level, one, one[8], all, false)[FLOOR][door]);
                }
                // A plate on either side of the bars, each within a few squares' run of them.
                if (barred) {
                    int first = -1, last = -1;
                    for (int column = one[3]; column <= one[4]; column++) {
                        if (level.land[FLOOR - 1][column] != RunLevels.GRILLE) continue;
                        if (first < 0) first = column;
                        last = column;
                    }
                    boolean before = false, after = false;
                    for (int[] plate : plates) {
                        before |= plate[1] < first && first - plate[1] <= 9;
                        after |= plate[1] > last && plate[1] - last <= 5;
                    }
                    assertTrue(where + " plates", before && after);
                }
            }
            // A stash's chamber holds one thing more; what stands about in a place stands on clear floor.
            for (int[] thing : level.decor) {
                if (thing[0] < 200 || thing[0] >= 300) continue;
                assertTrue("land " + number, thing[0] < 200 + RunLevels.LOOKS * 2 && thing[1] >= level.main);
                assertEquals("land " + number, RunLevels.AIR, level.land[thing[2]][thing[1]]);
                assertEquals("land " + number, RunLevels.BLOCK, level.land[thing[2] + 1][thing[1]]);
            }
        }
        // No two alike, or very nearly (the few that are laid alike are in different lands, and most of those look different): every piece is used somewhere, every look, every thing a chamber may hold, and each of the puzzles often.
        assertTrue("places " + places + " plans " + plans.size(), places >= 80 && plans.size() >= places * 9 / 10);
        for (int piece = 0; piece < RunLevels.PIECES; piece++) assertTrue("piece " + piece, pieces[piece]);
        for (int look = 0; look < RunLevels.LOOKS; look++) assertTrue("look " + look, looks[look]);
        for (java.util.Map.Entry<Integer, java.util.Set<Integer>> entry : byChapter.entrySet()) assertTrue("chapter " + entry.getKey() + " looks " + entry.getValue(), entry.getValue().size() >= 2);
        assertTrue(holds[RunLevels.HOLDS_TREAT] && holds[RunLevels.HOLDS_PET] && holds[RunLevels.HOLDS_SHRINE]);
        assertTrue("locks " + locks + " spans " + spans + " bars " + bars + " pets " + pets + " shrines " + shrines, locks >= 5 && spans >= 8 && bars >= 8 && pets >= 5 && shrines >= 5);
    }

    @Test public void aKeyOpensItsDoorALeverRaisesItsBridgeAPlateLiftsTheBarsAndAShrineSellsARibbonForAHeart() {
        // A place of its own, twenty-four squares long: a locked door, a pit with a bridge, and bars.
        RunLevels.Level level = flat(60);
        level.main = 20;
        for (int column = 22; column <= 55; column++) level.land[1][column] = RunLevels.BLOCK;
        level.rooms.add(new int[]{RunLevels.STASH, 10, FLOOR, 22, 55, 10, 23, 0, -1, FLOOR, 0, FLOOR, 2, 0, 0});
        level.land[FLOOR - 1][10] = RunLevels.ENTRY;
        level.land[FLOOR - 1][23] = RunLevels.ENTRY;
        for (int row = 2; row < FLOOR; row++) level.land[row][30] = row >= FLOOR - 2 ? RunLevels.LOCKED : RunLevels.BLOCK;
        level.items.add(new int[]{PlusRun.KEY, 26, FLOOR - 1, 0});
        for (int column = 34; column <= 37; column++) {
            level.land[FLOOR - 1][column] = RunLevels.SPIKE;
            level.land[FLOOR - 2][column] = RunLevels.SPAN;
        }
        level.items.add(new int[]{PlusRun.LEVER, 32, FLOOR - 1, 0});
        for (int row = 2; row < FLOOR; row++) level.land[row][44] = row >= FLOOR - 2 ? RunLevels.GRILLE : RunLevels.BLOCK;
        level.items.add(new int[]{PlusRun.PLATE, 41, FLOOR - 1, 0});
        level.items.add(new int[]{PlusRun.SHRINE, 50, FLOOR - 1, 0, PlusRun.GREEN});
        PlusRun run = new PlusRun(level);
        run.place(10 + (1 - PlusRun.WIDE) / 2, GROUND);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        assertEquals(0, run.room());
        // The door is a wall, and stops her; with the key taken it is nothing.
        assertEquals(RunLevels.BLOCK, run.at(30, FLOOR - 1));
        assertEquals(RunLevels.LOCKED, run.laid(30, FLOOR - 1));
        run.place(28, GROUND);
        hold(run, 1, false, true, false);
        assertTrue(run.x() < 30 - PlusRun.WIDE + .01f);
        assertEquals(0, run.keys());
        run.place(26.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(1, run.keys());
        assertEquals(RunLevels.AIR, run.at(30, FLOOR - 1));
        assertEquals(RunLevels.AIR, run.at(30, FLOOR - 2));
        assertEquals(RunLevels.BLOCK, run.at(30, FLOOR - 3));
        run.place(28, GROUND);
        hold(run, .6f, false, true, false);
        assertTrue(run.x() > 30);
        // The bridge is not there; with the lever pulled it is, for good, and holds her over the spikes.
        assertEquals(RunLevels.AIR, run.at(35, FLOOR - 2));
        assertEquals(0, run.levers());
        assertFalse(run.pulled(32));
        run.place(32.2f, GROUND);
        hold(run, .2f, false, false, false);
        assertEquals(1, run.levers());
        assertTrue(run.pulled(32));
        assertEquals(RunLevels.LEDGE, run.at(35, FLOOR - 2));
        run.place(35.2f, FLOOR - 2 - PlusRun.TALL - .5f);
        hold(run, 1, false, false, false);
        assertEquals(FLOOR - 2 - PlusRun.TALL, run.y(), .01f);
        assertEquals(0, run.hurts());
        hold(run, .2f, false, false, false);
        assertEquals(1, run.levers());
        // The bars are a wall; the plate lifts them for six seconds, each time it is stood on, and then they are a wall again: but not
        // while she stands in the way.
        assertEquals(RunLevels.BLOCK, run.at(44, FLOOR - 1));
        run.place(41.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(1, run.plates());
        assertEquals(PlusRun.BARS_OPEN, run.barsLeft(), .05f);
        assertEquals(RunLevels.AIR, run.at(44, FLOOR - 1));
        assertEquals(RunLevels.BLOCK, run.at(44, FLOOR - 3));
        run.place(38, GROUND);
        hold(run, PlusRun.BARS_OPEN + .2f, false, false, false);
        assertEquals(RunLevels.BLOCK, run.at(44, FLOOR - 1));
        run.place(41.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(2, run.plates());
        run.place(44.2f, GROUND);
        hold(run, PlusRun.BARS_OPEN + 1, false, false, false);
        assertEquals(RunLevels.AIR, run.at(44, FLOOR - 1));
        assertEquals(0, run.hurts());
        run.place(47, GROUND);
        hold(run, .3f, false, false, false);
        assertEquals(RunLevels.BLOCK, run.at(44, FLOOR - 1));
        // The shrine: a heart for its ribbon, from whoever has more than one; from anyone with one heart, nothing.
        assertEquals(PlusRun.HEARTS * 2, run.halves());
        assertFalse(run.has(PlusRun.GREEN));
        run.place(50.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(1, run.shrines());
        assertEquals(PlusRun.HEARTS * 2 - 2, run.halves());
        assertTrue(run.has(PlusRun.GREEN));
        PlusRun poor = new PlusRun(level);
        poor.wound();
        poor.wound();
        assertEquals(2, poor.halves());
        poor.place(50.2f, GROUND);
        hold(poor, .3f, false, false, false);
        assertEquals(0, poor.shrines());
        assertEquals(2, poor.halves());
    }

    @Test public void everyVillageHasTwoBuildingsSheCanGoIntoOfDifferentSortsAndNothingInThemHurtsHer() {
        boolean[] sorts = new boolean[9];
        int shops = 0, halls = 0, homes = 0, smithies = 0, markets = 0, gachas = 0;
        PlusRun inShop = null, inHall = null, inSmithy = null, inMarket = null;
        int[] shop = null, hall = null, smithy = null, market = null;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int villages = number > RunLevels.COUNT ? 2 : 1, houses = 0;
            boolean begun = false;
            java.util.List<int[]> built = new java.util.ArrayList<>();
            for (int[] one : level.rooms) {
                // The buildings come after every other place of the land, so that the places it had before keep their numbers.
                if (one[0] == RunLevels.HOUSE) begun = true;
                else assertFalse("land " + number, begun);
                if (one[0] != RunLevels.HOUSE) continue;
                houses++;
                built.add(one);
                int sort = one[14];
                assertTrue("land " + number, sort >= RunLevels.PET_SHOP && sort <= RunLevels.GACHA || sort == RunLevels.BOUTIQUE || sort == RunLevels.ELF);
                sorts[sort] = true;
                // (A home has one of the six insides of homes; every other building its own.)
                if (sort == RunLevels.HOME) assertTrue(one[13] >= RunLevels.HOME_LOOK && one[13] < RunLevels.HOME_LOOK + RunLevels.HOME_LOOKS);
                else assertEquals(sort == RunLevels.BOUTIQUE ? RunLevels.HOME_LOOK + 1 : sort == RunLevels.ELF ? RunLevels.HOME_LOOK + 2 : RunLevels.LOOKS + sort, one[13]);
                // Its door is on the ground of the land, where a building stands to be looked at; the land's own squares there are as they were.
                assertEquals("land " + number, one[2], RunLevels.top(level, one[1]));
                assertEquals("land " + number, RunLevels.AIR, level.land[one[2] - 1][one[1]]);
                boolean stands = false;
                for (int[] thing : level.decor) stands |= thing[1] == one[1] && thing[2] == one[2] - 1 && (thing[0] < 3 || thing[0] >= 300);
                assertTrue("land " + number, stands);
                // Inside: walled, a floor, its door; and nothing of the land's creatures.
                for (int row = 0; row < RunLevels.ROWS; row++) {
                    assertEquals(RunLevels.BLOCK, level.land[row][one[3] - 1]);
                    assertEquals(RunLevels.BLOCK, level.land[row][one[4] + 1]);
                }
                assertEquals(RunLevels.ENTRY, level.land[FLOOR - 1][one[6]]);
                for (int[] foe : level.foes) assertTrue("land " + number, foe[1] < one[3] || foe[1] > one[4]);
                java.util.List<int[]> spots = new java.util.ArrayList<>();
                int givers = 0;
                for (int[] item : level.items) {
                    if (item[1] < one[3] || item[1] > one[4]) continue;
                    if (item[0] == PlusRun.SPOT) spots.add(item);
                    else if (item[3] >= RunLevels.SOMI) {
                        givers++;
                        if (level.quest != 0) assertTrue("land " + number, item[3] != RunLevels.giverOf(level.quest));
                    }
                }
                if (sort == RunLevels.PET_SHOP) {
                    shops++;
                    // The ten pets, each once: five along the floor and five on a shelf two squares over it; and Nabi at her desk.
                    assertEquals(11, spots.size());
                    int seen = 0, low = 0, high = 0;
                    for (int[] spot : spots) {
                        if (spot[4] >= 100) {
                            assertEquals(RunLevels.NABI, spot[3]);
                            assertEquals(104, spot[4]);
                            continue;
                        }
                        seen |= 1 << spot[4];
                        if (spot[2] == FLOOR - 1) low++;
                        else {
                            high++;
                            assertEquals(FLOOR - 3, spot[2]);
                            assertEquals(RunLevels.LEDGE, level.land[FLOOR - 2][spot[1]]);
                        }
                    }
                    // (Every pet that is had from the first sits in a pet shop; those of the ones she has beaten in none.)
        assertEquals((1 << RunPets.FOUND) - 1, seen);
                    assertEquals(5, low);
                    assertEquals(5, high);
                    if (inShop == null) {
                        inShop = new PlusRun(level);
                        shop = one;
                    }
                } else if (sort == RunLevels.GUILD_HALL) {
                    halls++;
                    // Inju at her desk, and her board of hunts.
                    assertEquals(2, spots.size());
                    assertEquals(RunLevels.INJU, spots.get(0)[3]);
                    assertEquals(100, spots.get(0)[4]);
                    assertEquals(PlusRun.BOARD, spots.get(1)[4]);
                    if (inHall == null) {
                        inHall = new PlusRun(level);
                        hall = one;
                    }
                } else if (sort == RunLevels.ELF) {
                    // Aelwyn's wagon: her counter, in the middle of it, in the villages of the even chapters after the first.
                    assertTrue(RunLevels.elfHere(number));
                    assertEquals(1, spots.size());
                    assertEquals(PlusRun.ELF, spots.get(0)[4]);
                    assertEquals(0, givers);
                } else if (sort == RunLevels.BOUTIQUE) {
                    // Tulle's boutique, in Rosemarrow's village: her counter, in the middle of it.
                    assertEquals(RunLevels.BOUTIQUE_CHAPTER, level.chapter);
                    assertEquals(1, spots.size());
                    assertEquals(PlusRun.BOUTIQUE, spots.get(0)[4]);
                    assertEquals(0, givers);
                } else if (sort == RunLevels.GACHA) {
                    gachas++;
                    // Its machine, in the middle of it; and no gacha before the fifth land.
                    assertTrue("land " + number, number >= RunLevels.GACHA_FROM);
                    assertEquals(1, spots.size());
                    assertEquals(PlusRun.MACHINE, spots.get(0)[4]);
                    assertEquals(0, givers);
                } else if (sort == RunLevels.MARKET) {
                    markets++;
                    // Whoever deals there; and no market before the ninth land.
                    assertTrue("land " + number, number >= RunLevels.MARKET_FROM);
                    assertEquals(1, spots.size());
                    assertEquals(PlusRun.DEALER, spots.get(0)[4]);
                    assertEquals(0, givers);
                    if (inMarket == null) {
                        inMarket = new PlusRun(level);
                        market = one;
                    }
                } else if (sort == RunLevels.SMITHY) {
                    smithies++;
                    // Moru at her anvil; and from the thirteenth land her two sisters, each at her own, well apart.
                    assertEquals("land " + number, number >= RunLevels.SISTERS_FROM ? 3 : 1, spots.size());
                    for (int which = 0; which < spots.size(); which++) {
                        assertEquals(PlusRun.SMITH + which, spots.get(which)[4]);
                        assertEquals(0, spots.get(which)[3]);
                        assertEquals(one[3] + 5 + which * 4, spots.get(which)[1]);
                    }
                    assertEquals(0, givers);
                    if (inSmithy == null && spots.size() == 3) {
                        inSmithy = new PlusRun(level);
                        smithy = one;
                    }
                } else {
                    homes++;
                    assertEquals(0, spots.size());
                    assertEquals("land " + number, 1, givers);
                }
            }
            // A chapter's first land has its village, with every building of its time, in order, and its own name and landmark; every
            // other place where a village was is a homestead, with one home.
            java.util.Collections.sort(built, (one, other) -> one[1] - other[1]);
            int[] town = RunLevels.villageLand(number) ? RunLevels.townOf(number) : new int[0];
            assertEquals("land " + number, villages - (town.length > 0 ? 1 : 0) + town.length, houses);
            for (int index = 0; index < town.length; index++) assertEquals("land " + number, town[index], built.get(index)[14]);
            for (int index = town.length; index < built.size(); index++) assertEquals("land " + number, RunLevels.HOME, built.get(index)[14]);
            if (town.length > 0) {
                assertTrue("land " + number, level.town >= 0);
                boolean landmark = false, arch = false;
                for (int[] decor : level.decor) {
                    landmark |= decor[0] == 410 + level.chapter;
                    arch |= decor[0] == 400 && decor[1] == level.town + 1;
                }
                assertTrue("land " + number, landmark && arch);
            } else {
                assertEquals("land " + number, -1, level.town);
            }
            // (Whoever is at home is not whoever keeps a building of the same land, nor whoever has something to ask of her, who is out at her door.)
            for (int[] home : built) {
                if (home[14] != RunLevels.HOME) continue;
                for (int[] item : level.items) {
                    if (item[1] < home[3] || item[1] > home[4] || item[3] < RunLevels.SOMI) continue;
                    for (int[] other : built) {
                        int sort2 = other[14];
                        assertTrue("land " + number, item[3] != (sort2 == RunLevels.PET_SHOP ? RunLevels.NABI : sort2 == RunLevels.GUILD_HALL ? RunLevels.INJU : sort2 == RunLevels.SMITHY ? RunLevels.MORU : -1));
                    }
                    if (level.quest != 0) assertTrue("land " + number, item[3] != RunLevels.giverOf(level.quest));
                }
            }
        }
        assertTrue(sorts[RunLevels.PET_SHOP] && sorts[RunLevels.GUILD_HALL] && sorts[RunLevels.HOME] && sorts[RunLevels.SMITHY] && sorts[RunLevels.MARKET]);
        // Twelve villages, one in each chapter, each with its own name; and many homes.
        assertEquals(RunLevels.ALL_CHAPTERS, shops);
        assertEquals(RunLevels.ALL_CHAPTERS, halls);
        assertEquals(RunLevels.ALL_CHAPTERS, smithies);
        assertEquals(RunLevels.ALL_CHAPTERS - 1, gachas);
        assertEquals(RunLevels.ALL_CHAPTERS - 2, markets);
        assertTrue("homes " + homes, homes >= 150);
        assertEquals(RunLevels.ALL_CHAPTERS, new java.util.HashSet<>(java.util.Arrays.asList(RunLevels.VILLAGES)).size());
        assertTrue(sorts[RunLevels.GACHA]);

        // Gone into, a pet shop: each pet is walked up to, and Nabi's desk is stood at anywhere along it; nothing hurts her; and out again.
        PlusRun run = inShop;
        run.place(shop[1] + (1 - PlusRun.WIDE) / 2, shop[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertEquals(-1, run.spot());
        assertTrue(run.enter());
        int index = run.room();
        assertEquals(RunLevels.HOUSE, run.level().rooms.get(index)[0]);
        assertFalse(run.sealed());
        assertEquals(1 << index, run.roomsDone());
        int from = shop[3];
        for (int pet = 0; pet < 5; pet++) {
            run.place(from + 7 + pet * 2 + .2f, GROUND);
            run.step(STEP, false, false, false);
            assertEquals(pet, run.spot());
            run.place(from + 7 + pet * 2 + .2f, GROUND - 2);
            hold(run, .2f, false, false, false);
            assertEquals(5 + pet, run.spot());
        }
        run.place(from + 5.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(-1, run.spot());
        run.place(from + 3, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(104, run.spot());
        hold(run, 3, true, false, false);
        assertEquals(0, run.hurts());
        hold(run, 3, true, false, false);
        run.place(shop[6] + (1 - PlusRun.WIDE) / 2, GROUND);
        hold(run, .2f, false, false, false);
        assertTrue(run.enter());
        assertEquals(-1, run.room());
        assertEquals(shop[1] + (1 - PlusRun.WIDE) / 2, run.x(), .01f);
        // And the guild hall: Inju's desk is the counter.
        run = inHall;
        run.place(hall[1] + (1 - PlusRun.WIDE) / 2, hall[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        run.place(hall[3] + 12, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(100, run.spot());
        run.place(hall[3] + 7.2f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.BOARD, run.spot());
        // And the smithy: each of the three at her anvil.
        run = inSmithy;
        run.place(smithy[1] + (1 - PlusRun.WIDE) / 2, smithy[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        for (int which = 0; which < 3; which++) {
            run.place(smithy[3] + 5.6f + which * 4, GROUND);
            run.step(STEP, false, false, false);
            assertEquals(PlusRun.SMITH + which, run.spot());
        }
        hold(run, 2, true, false, false);
        assertEquals(0, run.hurts());
        // And the black market: whoever deals there.
        run = inMarket;
        run.place(market[1] + (1 - PlusRun.WIDE) / 2, market[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        run.place(market[3] + 8.6f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.DEALER, run.spot());
    }

    // The smithy: what is forged, what is worn, and how tough the lands are

    /** Flat ground that is a land of some number, and so as tough as that land's level makes it. */
    private static RunLevels.Level flatOf(int number, int columns) {
        RunLevels.Level level = new RunLevels.Level(number, columns);
        for (int column = 0; column < columns; column++) {
            for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        }
        level.gate = columns - 3;
        return level;
    }

    /** She stands at the start of flat ground holding a thing the smith has worked on as far as a level, with minus signs at some columns. */
    private static PlusRun forged(int weapon, int level, int... columns) {
        RunLevels.Level land = flat(80);
        for (int column : columns) land.foes.add(new int[]{RunLevels.MINUS, column, FLOOR - 1});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.levels[weapon] = level;
        PlusRun run = new PlusRun(land, perks);
        run.arm(weapon);
        for (PlusRun.Foe foe : run.foes()) foe.dazed = 0;
        return run;
    }

    /** Time goes by, with every sign that still stands kept where it began. */
    private static void still(PlusRun run, float seconds) {
        for (float gone = 0; gone < seconds; gone += STEP) {
            for (PlusRun.Foe foe : run.foes()) if (!foe.flat) foe.x = foe.home;
            run.step(STEP, false, false, false);
        }
    }

    private static int flying(PlusRun run, int kind) {
        int count = 0;
        for (PlusRun.Shot shot : run.shots()) if (shot.kind == kind && shot.hers) count++;
        return count;
    }

    /** She walks into a times sign with nothing to keep her from it: one hurt. Returns how many halves of a heart it cost. */
    private static int bitten(PlusRun run) {
        PlusRun.Foe foe = run.foes().get(0);
        int had = run.halves(), hurts = run.hurts();
        run.bare();
        for (int step = 0; step < 120 && run.hurts() == hurts; step++) {
            run.place(foe.x, GROUND);
            run.step(STEP, false, false, false);
        }
        assertEquals(hurts + 1, run.hurts());
        return had - run.halves();
    }

    @Test public void theLaterLandsAreTougherByTheirLevelsAndWhatSheWearsAnswersIt() {
        // A level more with every five lands; a blow more with every five levels, and half a heart more to a hurt with every six: through
        // the first two volumes. The third is harder than that: five blows throughout, and a hurt of two hearts and a half, then three.
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            assertEquals(1 + (number - 1) / 5, RunLevels.levelOf(number));
            assertEquals(number <= 25 ? 0 : number <= 50 ? 1 : number <= 75 ? 2 : number <= 80 ? 3 : 4, RunLevels.toughOf(number));
            assertEquals(number <= 30 ? 0 : number <= 60 ? 1 : number <= 80 ? 2 : number <= 90 ? 3 : 4, RunLevels.biteOf(number));
        }
        // (In the third volume a creature takes five blows, and a hurt costs five halves of a heart and then six, before what she wears.)
        assertEquals(5, 1 + RunLevels.toughOf(81));
        assertEquals(5, 2 + RunLevels.biteOf(85));
        assertEquals(6, 2 + RunLevels.biteOf(100));
        assertEquals(5, PlusRun.strength(RunLevels.LUCY));
        assertEquals(20, RunLevels.levelOf(RunLevels.TOTAL));
        // In the lands themselves: every creature takes as many more as its land's level says; those who wait, a paper target, a wisp and a flake do not.
        for (int number : new int[]{5, 30, 55, 80}) {
            RunLevels.Level level = RunLevels.level(number);
            PlusRun run = new PlusRun(level);
            assertEquals(RunLevels.toughOf(number), run.tough());
            assertEquals(RunLevels.biteOf(number), run.bite());
            assertEquals(level.foes.size(), run.foes().size());
            int tougher = 0;
            for (int index = 0; index < level.foes.size(); index++) {
                int[] spec = level.foes.get(index);
                PlusRun.Foe foe = run.foes().get(index);
                assertEquals(spec[0], foe.kind);
                if (RunLevels.boss(spec[0])) continue;
                // (The unique creatures have health of their own: RunUniquesTest.)
                if (RunUniques.is(spec[0])) {
                    assertEquals(RunUniques.health(spec[0], number) * PlusRun.TIER_HEALTH[foe.tier] + run.tough() * PlusRun.BLOW, foe.left);
                    continue;
                }
                boolean spared = spec[0] == RunLevels.TARGET || spec[0] == RunLevels.WISP || spec[0] == RunLevels.RIME;
                assertEquals("land " + number, PlusRun.healthOf(spec[0]) * (spec.length > 3 ? spec[3] : 1) * PlusRun.TIER_HEALTH[foe.tier] + (spared ? 0 : run.tough()) * PlusRun.BLOW, foe.left);
                if (!spared) tougher++;
            }
            assertTrue("land " + number + " has " + tougher, tougher > 5);
        }
        // One that takes two: landed on, it stands; landed on again, it is flat. In boots, once is enough.
        RunLevels.Level level = flatOf(26, 80);
        level.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        PlusRun.Foe foe = run.foes().get(0);
        assertEquals(1, run.tough());
        assertEquals(2 * PlusRun.BLOW, foe.left);
        run.bare();
        run.place(foe.x + foe.wide() / 2 - PlusRun.WIDE / 2, foe.y - PlusRun.TALL - .4f);
        for (int step = 0; step < 30 && foe.left == 2 * PlusRun.BLOW; step++) run.step(STEP, false, false, false);
        assertEquals(PlusRun.BLOW, foe.left);
        assertFalse(foe.flat);
        assertTrue(run.fells().isEmpty());
        foe.dazed = 0;
        assertTrue(stomp(run, foe));
        assertEquals(1, run.fells().size());
        assertEquals(0, run.hurts());
        PlusRun.Perks booted = new PlusRun.Perks();
        booted.stomp = 1;
        run = new PlusRun(level, booted);
        assertTrue(stomp(run, run.foes().get(0)));
        // A hurt: a heart in the early lands; a heart and a half from the thirty-first, two from the sixty-first. A helm takes half a heart
        // off for each of its three sets, and never the last half.
        int[][] costs = {{1, 0, 2}, {31, 0, 3}, {61, 0, 4}, {31, 1, 2}, {61, 1, 3}, {61, 3, 1}, {1, 3, 1}, {1, 1, 1}};
        for (int[] one : costs) {
            level = flatOf(one[0], 60);
            level.foes.add(new int[]{RunLevels.TIMES, 20, FLOOR - 1});
            PlusRun.Perks worn = new PlusRun.Perks();
            worn.defence = one[1];
            run = new PlusRun(level, worn);
            assertEquals("land " + one[0] + " helm " + one[1], one[2], bitten(run));
        }
        // A coat: nothing hurts her again for so much longer after a hurt.
        float[] lasted = new float[2];
        for (int coat = 0; coat < 2; coat++) {
            level = flat(60);
            level.foes.add(new int[]{RunLevels.TIMES, 20, FLOOR - 1});
            PlusRun.Perks worn = new PlusRun.Perks();
            worn.mercy = coat == 0 ? 1 : 1.5f;
            run = new PlusRun(level, worn);
            bitten(run);
            run.place(40, GROUND);
            while (run.safe() && lasted[coat] < 5) {
                run.step(STEP, false, false, false);
                lasted[coat] += STEP;
            }
        }
        assertEquals(1.5f, lasted[1] / lasted[0], .06f);

        // What is worn, as it is kept: a bit for each piece made, and the set of the piece in each place.
        int made = RunGear.bit(RunGear.HELM, 1) | RunGear.bit(RunGear.HELM, 3) | RunGear.bit(RunGear.COAT, 2) | RunGear.bit(RunGear.BOOTS, 3) | RunGear.bit(RunGear.CHARM, 3);
        assertEquals(12, Integer.bitCount((1 << RunGear.SLOTS * RunGear.TIERS) - 1));
        int on = 0;
        on = RunGear.wear(on, RunGear.HELM, 3);
        on = RunGear.wear(on, RunGear.COAT, 2);
        on = RunGear.wear(on, RunGear.BOOTS, 3);
        on = RunGear.wear(on, RunGear.CHARM, 3);
        assertEquals(on, RunGear.sound(on, made));
        assertEquals(3, RunGear.defence(on));
        assertEquals(1.5f, RunGear.mercy(on), 0);
        assertEquals(3, RunGear.stomp(on));
        assertEquals(30, RunGear.luck(on));
        assertFalse(RunGear.whole(on));
        // What has not been made is not on her; and all four of one set are a set.
        int taken = RunGear.sound(RunGear.wear(on, RunGear.COAT, 3), made);
        assertEquals(0, RunGear.worn(taken, RunGear.COAT));
        assertEquals(3, RunGear.worn(taken, RunGear.HELM));
        assertTrue(RunGear.whole(RunGear.wear(on, RunGear.COAT, 3)));
        assertFalse(RunGear.whole(0));
        assertEquals(0, RunGear.defence(0));
        assertEquals(1f, RunGear.mercy(0), 0);
        assertEquals("Type-steel helm", RunGear.name(RunGear.HELM, 3));
        // What things cost rises with the set and with the step; and what went into a thing is what re-forging it gives back.
        for (int tier = 1; tier <= RunGear.TIERS; tier++) {
            if (tier > 1) assertTrue(RunGear.piecePluses(tier) > RunGear.piecePluses(tier - 1) && RunGear.stepPluses(tier) > RunGear.stepPluses(tier - 1));
            assertTrue(RunGear.pieceNeeds(tier)[0] > 0 && RunGear.stepNeeds(tier)[0] > 0);
            for (int slot = 0; slot < RunGear.SLOTS; slot++) assertFalse(RunGear.does(slot, tier).isEmpty());
        }
        int[] back = RunGear.spent(RunArms.LEVELS);
        for (int index = 0; index < 3; index++) {
            assertEquals(RunGear.stepNeeds(1)[index] + RunGear.stepNeeds(2)[index] + RunGear.stepNeeds(3)[index], back[index]);
            assertTrue(RunGear.BUYS[index] > RunGear.SELLS[index] * 2);
        }
        assertArrayEquals0(new int[]{0, 0, 0}, RunGear.spent(0));
        assertTrue(RunGear.affords(120, new int[]{2, 0, 0}, RunGear.stepPluses(1), RunGear.stepNeeds(1)));
        assertFalse(RunGear.affords(119, new int[]{2, 0, 0}, RunGear.stepPluses(1), RunGear.stepNeeds(1)));
        assertFalse(RunGear.affords(900, new int[]{1, 9, 9}, RunGear.stepPluses(1), RunGear.stepNeeds(1)));
        assertEquals("+ 300  \u00B7  4 type metal  \u00B7  2 ink crystal", RunGear.price(RunGear.stepPluses(2), RunGear.stepNeeds(2)));
    }

    private static void assertArrayEquals0(int[] wanted, int[] found) { org.junit.Assert.assertArrayEquals(wanted, found); }

    @Test public void whatTheSmithMakesOfAThingChangesWhatItDoesAndNothingOfItIsThereBeforeSheHas() {
        // As found, everything is as it was: no waves, nothing goes on through, only a book bursts, one page, five keys; and a blow is a blow.
        for (int weapon = 0; weapon < RunArms.COUNT; weapon++) {
            assertEquals(0, RunArms.waves(weapon, 0));
            assertEquals(0, RunArms.pierces(weapon, 0));
            assertEquals(weapon == RunArms.BOOK ? PlusRun.BLAST : weapon == RunArms.CANNON ? 2.6f : 0, RunArms.burst(weapon, 0), 0);
            assertEquals(1, RunArms.blows(weapon, 0));
            assertEquals(2, RunArms.blows(weapon, 1));
            assertEquals(2, RunArms.blows(weapon, 2));
            assertEquals(3, RunArms.blows(weapon, 3));
            for (int level = 1; level <= RunArms.LEVELS; level++) assertFalse(RunArms.step(weapon, level).isEmpty());
        }
        assertEquals(0, RunArms.chains(0));
        assertEquals(0, RunArms.burns(0), 0);
        assertEquals(1, RunArms.pages(0));
        assertEquals(RunArms.PANEL_WIDE, RunArms.panelWide(0), 0);
        assertEquals(RunArms.PANEL_TALL, RunArms.panelTall(0), 0);
        assertEquals(RunArms.FAN, RunArms.fan(0));
        assertEquals(RunArms.bite(RunArms.KEYS, 0) + 1, RunArms.bite(RunArms.KEYS, 3), 0);

        // A sword. As found, a stroke takes what is at arm's length and nothing beyond it. Worked on once, every stroke sends a wave ahead,
        // which takes the first thing it meets; twice, the wave goes further and through; three times, there are two of them.
        PlusRun run = forged(PlusRun.SWORD, 0, 6, 8);
        strike(run);
        still(run, .5f);
        assertEquals(0, run.waves());
        assertFalse(run.foes().get(0).flat);
        run = forged(PlusRun.SWORD, 1, 6, 8);
        strike(run);
        assertEquals(1, run.waves());
        assertEquals(1, flying(run, PlusRun.SLASH));
        still(run, .5f);
        assertTrue(run.foes().get(0).flat);
        assertFalse(run.foes().get(1).flat);
        org.junit.Assert.assertArrayEquals(new int[]{RunLevels.MINUS, PlusRun.SWORD + 1}, run.fells().get(0));
        assertEquals(0, flying(run, PlusRun.SLASH));
        run = forged(PlusRun.SWORD, 2, 6, 8);
        strike(run);
        still(run, .6f);
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);
        run = forged(PlusRun.SWORD, 3, 6, 8);
        strike(run);
        assertEquals(2, run.waves());
        // A knife: a thin wave with every throw; then three, in a fan.
        run = forged(PlusRun.KNIFE, 0);
        strike(run);
        assertEquals(0, flying(run, PlusRun.SLASH));
        assertEquals(1, flying(run, PlusRun.KNIFE));
        run = forged(PlusRun.KNIFE, 1);
        strike(run);
        assertEquals(1, flying(run, PlusRun.SLASH));
        run = forged(PlusRun.KNIFE, 2);
        strike(run);
        assertEquals(3, flying(run, PlusRun.SLASH));
        float up = 0, down = 0;
        for (PlusRun.Shot shot : run.shots()) {
            if (shot.kind != PlusRun.SLASH) continue;
            up = Math.min(up, shot.down);
            down = Math.max(down, shot.down);
            assertEquals(PlusRun.KNIFE, shot.by);
        }
        assertTrue(up < 0 && down > 0);
        // A brush: a wave of paint, as tall as its sweep, that takes what is beyond its bristles; and, worked on again, wipes away what is thrown.
        run = forged(PlusRun.BRUSH, 0, 5);
        strike(run);
        still(run, .6f);
        assertFalse(run.foes().get(0).flat);
        run = forged(PlusRun.BRUSH, 1, 5);
        strike(run);
        assertEquals(1, flying(run, PlusRun.SLASH));
        still(run, .6f);
        assertTrue(run.foes().get(0).flat);
        org.junit.Assert.assertArrayEquals(new int[]{RunLevels.MINUS, PlusRun.BRUSH + 1}, run.fells().get(0));
        assertFalse(RunArms.waveWipes(RunArms.BRUSH, 1));
        assertTrue(RunArms.waveWipes(RunArms.BRUSH, 2));
        // A pen: as found it stops in the first thing it meets; worked on, it goes on through that one; again, it bursts where it ends.
        run = forged(PlusRun.PEN, 0, 6, 8);
        strike(run);
        still(run, .8f);
        assertTrue(run.foes().get(0).flat);
        assertFalse(run.foes().get(1).flat);
        run = forged(PlusRun.PEN, 1, 6, 8, 9);
        run.foes().get(2).home = 8.6f;
        strike(run);
        still(run, .8f);
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);
        assertFalse(run.foes().get(2).flat);
        assertTrue(run.booms().isEmpty());
        run = forged(PlusRun.PEN, 2, 6, 8, 9);
        run.foes().get(2).home = 8.6f;
        strike(run);
        boolean burst = false;
        for (int step = 0; step < 50; step++) {
            still(run, STEP);
            for (float[] boom : run.booms()) burst |= boom[3] == PlusRun.PEN && boom[2] == RunArms.burst(RunArms.PEN, 2);
        }
        assertTrue(burst);
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat && run.foes().get(2).flat);
        // An ink gun: its blot splashes; and, worked on again, jumps on to the next creature.
        run = forged(PlusRun.GUN, 0, 8, 11);
        strike(run);
        still(run, .8f);
        assertTrue(run.foes().get(0).flat);
        assertFalse(run.foes().get(1).flat);
        assertEquals(0, run.chains());
        run = forged(PlusRun.GUN, 1, 8, 11);
        strike(run);
        boolean splashed = false;
        for (int step = 0; step < 50; step++) {
            still(run, STEP);
            for (float[] boom : run.booms()) splashed |= boom[3] == PlusRun.GUN && boom[2] == RunArms.burst(RunArms.GUN, 1);
        }
        assertTrue(splashed);
        assertFalse(run.foes().get(1).flat);
        run = forged(PlusRun.GUN, 2, 8, 11);
        strike(run);
        still(run, .8f);
        assertEquals(1, run.chains());
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);
        // A tablet-book: two pages at a time; a wider, taller panel; and a page that bursts where it turns back.
        run = forged(PlusRun.TABLET, 0);
        strike(run);
        assertEquals(1, flying(run, PlusRun.PAGE));
        run = forged(PlusRun.TABLET, 1);
        strike(run);
        assertEquals(2, flying(run, PlusRun.PAGE));
        assertTrue(RunArms.panelWide(2) > RunArms.PANEL_WIDE && RunArms.panelTall(2) > RunArms.PANEL_TALL);
        run = forged(PlusRun.TABLET, 3);
        strike(run);
        boolean turned = false;
        for (int step = 0; step < 60; step++) {
            still(run, STEP);
            for (float[] boom : run.booms()) turned |= boom[3] == PlusRun.PAGE;
        }
        assertTrue(turned);
        // The keys: seven in the fan; and keys that go on through.
        run = forged(PlusRun.KEYS, 2);
        run.place(20, GROUND);
        for (int step = 0; step < 60 && run.fans() == 0; step++) {
            run.shots().clear();
            run.step(STEP, false, false, false, true);
        }
        assertEquals(1, run.fans());
        assertEquals(7, run.shots().size());
        for (PlusRun.Shot key : run.shots()) assertEquals(99, key.pierce);
        // A book: a wider burst; and its pages burn for a while where it burst, and take what walks into them.
        for (int level = 0; level <= 2; level++) {
            run = forged(PlusRun.BOOK, level);
            run.place(30, GROUND);
            strike(run);
            for (int step = 0; step < 240 && run.blasts() == 0; step++) run.step(STEP, false, false, false);
            assertEquals(1, run.blasts());
            assertEquals(level == 0 ? PlusRun.BLAST : 2.9f, run.blastWide(), 0);
            assertEquals(level == 2 ? 1 : 0, run.fires().size());
            if (level == 2) {
                float[] fire = run.fires().get(0);
                assertEquals(RunArms.burns(2), fire[3], .01f);
                hold(run, RunArms.burns(2) + .1f, false, false, false);
                assertTrue(run.fires().isEmpty());
            }
        }
        // And a blow of a thing that has been worked on counts for more against a creature that takes several: three, then two at a time, then all three.
        int[] left = {2, 1, 1, 0};
        for (int level = 0; level <= RunArms.LEVELS; level++) {
            RunLevels.Level land = flat(80);
            land.foes.add(new int[]{RunLevels.MINUS, 8, FLOOR - 1, 3});
            PlusRun.Perks perks = new PlusRun.Perks();
            perks.levels[PlusRun.KNIFE] = level;
            run = new PlusRun(land, perks);
            run.arm(PlusRun.KNIFE);
            PlusRun.Foe big = run.foes().get(0);
            big.dazed = 0;
            assertEquals(3 * PlusRun.BLOW, big.left);
            run.step(STEP, false, false, false, true);
            // (Only the knife itself: its waves are taken out of the air.)
            for (int index = run.shots().size() - 1; index >= 0; index--) if (run.shots().get(index).kind == PlusRun.SLASH) run.shots().remove(index);
            still(run, .6f);
            assertEquals("level " + level, left[level] * PlusRun.BLOW, Math.max(0, big.left));
            assertEquals(level == RunArms.LEVELS, big.flat);
        }
    }

    // The third volume

    @Test public void theThirdVolumeHasTwentyLandsAndAFriendInEachButTheLastWhoPledgesWhenSheIsWalkedUpTo() {
        assertEquals(100, RunLevels.TOTAL);
        assertEquals(80, RunLevels.SECOND);
        assertEquals(12, RunLevels.ALL_CHAPTERS);
        int seen = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int friend = RunLevels.friendOf(number), here = 0;
            assertEquals(number > 80 && number < 100 ? number - 81 : -1, friend);
            // Two chapters of ten; no one waits in any of its lands but the last, where Lucy does; and each is longer than any of the first volume.
            if (number > RunLevels.SECOND) {
                assertEquals(10 + (number - 81) / 10, RunLevels.chapterOf(number));
                assertEquals(number == RunLevels.TOTAL ? RunLevels.LUCY : -1, RunLevels.bossOf(number));
                assertTrue("land " + number + " is " + level.gate, level.gate > (number == RunLevels.TOTAL ? 150 : 250));
                assertEquals(number == RunLevels.TOTAL ? 4 : 3, level.flags.size());
            }
            for (int[] item : level.items) {
                if (item[0] != PlusRun.PLEDGE) continue;
                here++;
                // On flat ground of the land itself, past its second flag, in the open.
                assertEquals("land " + number, friend, item[4]);
                assertTrue(item[1] > level.flags.get(1) && item[1] < level.gate);
                assertEquals(item[2] + 1, RunLevels.top(level, item[1]));
                assertEquals(RunLevels.AIR, level.land[item[2]][item[1]]);
                seen |= 1 << item[4];
            }
            assertEquals("land " + number, friend >= 0 ? 1 : 0, here);
        }
        assertEquals((1 << RunLevels.FRIENDS) - 1, seen);
        // Walked up to, she pledges: once, however long she is stood by.
        RunLevels.Level level = RunLevels.level(85);
        int[] friend = null;
        for (int[] item : level.items) if (item[0] == PlusRun.PLEDGE) friend = item;
        PlusRun run = new PlusRun(level);
        hold(run, .5f, false, false, false);
        assertEquals(0, run.pledges());
        assertEquals(-1, run.lastPledge());
        run.place(friend[1] - .2f, friend[2] + 1 - PlusRun.TALL);
        hold(run, 1.5f, false, false, false);
        assertEquals(1, run.pledges());
        assertEquals(4, run.lastPledge());
        assertTrue(run.items().contains(friend));
    }

    // Lucy, and the end of the last land

    /** Her shut in with Lucy, with the notes about her for as good as ever; whole or not. */
    private static PlusRun withLucy(boolean whole) {
        RunLevels.Level level = RunLevels.level(RunLevels.TOTAL);
        int floor = RunLevels.top(level, level.arena + 3);
        PlusRun.Perks perks = new PlusRun.Perks();
        level.items.add(new int[]{PlusRun.NOTES, level.arena + 3, floor - 1, 0});
        perks.lasting = 1000;
        perks.whole = whole;
        PlusRun run = new PlusRun(level, perks);
        run.place(level.arena + 3.2f, floor - PlusRun.TALL);
        run.step(STEP, false, false, false);
        return run;
    }

    /** Blows on Lucy whenever one can land, until she can take no more than so much. */
    private static void wornTo(PlusRun run, PlusRun.Foe lucy, int until) {
        for (int step = 0; step < 60 * 400 && run.state() == PlusRun.PLAYING && !run.decided() && lucy.left > until; step++) {
            if (pressable(run, lucy)) touch(run, lucy);
            else park(run);
        }
    }

    /** She falls out of the land until it has cost her a life. */
    private static void lifeLost(PlusRun run) {
        int before = run.livesLost();
        for (int fall = 0; fall < 12 && run.livesLost() == before; fall++) {
            run.place(run.level().arena + 5, run.level().land.length + 2);
            hold(run, .3f, false, false, false);
        }
        assertEquals(before + 1, run.livesLost());
        hold(run, .1f, false, false, false);
    }

    @Test public void lucyWaitsAtTheEndOfTheLastLandAndHerLastThirdIsNotTakenFromHerOnTheGround() {
        assertEquals(RunLevels.LUCY, RunLevels.bossOf(RunLevels.TOTAL));
        assertTrue(RunLevels.boss(RunLevels.LUCY));
        assertFalse(RunLevels.rover(RunLevels.LUCY) || RunLevels.flier(RunLevels.LUCY));
        assertEquals(RunLevels.TOTAL, RunLevels.OPEN_TO);
        assertEquals(12, PlusRun.health(RunLevels.LUCY));
        for (boolean whole : new boolean[]{true, false}) {
            PlusRun run = withLucy(whole);
            PlusRun.Foe lucy = waiting(run);
            assertEquals(RunLevels.LUCY, lucy.kind);
            assertTrue(run.locked());
            assertEquals(0, run.phase(lucy));
            // A third of what she can take: her dragon side comes out, as anyone else changes.
            wornTo(run, lucy, 8);
            assertEquals(8, lucy.left);
            assertEquals(1, run.phase(lucy));
            assertEquals(1, run.phases());
            assertFalse(run.decided());
            // Another third, and it is not a change: it is the moment of the black ribbon, in which nothing moves.
            wornTo(run, lucy, 4);
            assertTrue(run.decided());
            assertEquals(4, lucy.left);
            assertEquals(2, run.phases());
            assertEquals(PlusRun.PLAYING, run.state());
            assertTrue(run.absent(lucy));
            for (PlusRun.Shot shot : run.shots()) assertTrue(shot.left <= 0);
            float time = run.time(), x = run.x(), y = run.y();
            hold(run, PlusRun.MOMENT - PlusRun.MOMENT_LANDS - .1f, false, true, true);
            assertEquals(time, run.time(), 0);
            assertEquals(x, run.x(), 0);
            assertEquals(y, run.y(), 0);
            assertFalse(run.ribboned());
            assertFalse(run.mastered());
            // The ribbon reaches her: whole, it is the white that is truly hers; not whole, black lies on her and every colour is gone.
            hold(run, .2f, false, false, false);
            assertTrue(run.ribboned());
            assertEquals(whole, run.mastered());
            assertEquals(whole, run.white());
            assertEquals(!whole, run.cursed() > 0);
            assertEquals(PlusRun.PLAYING, run.state());
            hold(run, PlusRun.MOMENT, false, false, false);
            assertEquals(whole ? PlusRun.RISEN : PlusRun.UNMADE, run.state());
            assertEquals(0, run.moment(), 0);
            assertFalse(lucy.flat);
            // And then the land is not played on the ground any more.
            hold(run, 1, false, true, false);
            assertEquals(x, run.x(), 0);
            if (whole) {
                // In the air a life lost is one of hers; and the flight won is the land won, with Lucy down.
                int lives = run.lives();
                assertTrue(run.spend());
                assertEquals(lives - 1, run.lives());
                assertEquals(PlusRun.RISEN, run.state());
                run.flown();
                assertEquals(PlusRun.WON, run.state());
                assertTrue(lucy.flat);
                assertFalse(run.standing());
            } else {
                assertFalse(run.spend());
                run.flown();
                assertEquals(PlusRun.UNMADE, run.state());
                assertFalse(lucy.flat);
            }
        }
        // The last life spent in the air loses the land.
        PlusRun run = withLucy(true);
        wornTo(run, waiting(run), 4);
        hold(run, PlusRun.MOMENT + .2f, false, false, false);
        assertEquals(PlusRun.RISEN, run.state());
        for (int life = run.lives(); life > 1; life--) assertTrue(run.spend());
        assertFalse(run.spend());
        assertEquals(PlusRun.LOST, run.state());
        assertEquals(0, run.lives());
    }

    @Test public void aLifeLostToLucyBeginsThatPartOfTheFightAgainAndNotTheWholeOfIt() {
        // As a child: she has back all she had lost.
        PlusRun run = withLucy(false);
        PlusRun.Foe lucy = waiting(run);
        wornTo(run, lucy, 10);
        assertEquals(10, lucy.left);
        lifeLost(run);
        assertFalse(run.locked());
        assertTrue(lucy.away);
        assertEquals(12, lucy.left);
        // With her dragon side out: she keeps it, and has back what she had lost of that third.
        run = withLucy(false);
        lucy = waiting(run);
        wornTo(run, lucy, 6);
        assertEquals(6, lucy.left);
        assertEquals(1, run.phase(lucy));
        lifeLost(run);
        assertFalse(run.locked());
        assertEquals(8, lucy.left);
        assertEquals(1, run.phase(lucy));
        // Come back in, she arrives as she was: with her dragon side out.
        int floor = RunLevels.top(run.level(), run.level().arena + 3);
        run.place(run.level().arena + 3.2f, floor - PlusRun.TALL);
        hold(run, PlusRun.ENTER + .3f, false, false, false);
        assertTrue(run.locked());
        assertFalse(lucy.away);
        assertEquals(1, run.phase(lucy));
        assertFalse(run.decided());
    }

    @Test public void lucyDoesThreeThingsAsAChildAndTwoMoreWithHerDragonSideOut() {
        PlusRun run = withLucy(false);
        PlusRun.Foe lucy = waiting(run);
        int did = 0;
        for (int step = 0; step < 60 * 40; step++) {
            park(run);
            if (lucy.step == 1) did |= 1 << lucy.mode;
        }
        assertEquals(1 << 1 | 1 << 2 | 1 << 3, did);
        assertEquals(lucy.ground, lucy.y, .01f);
        wornTo(run, lucy, 8);
        did = 0;
        boolean flew = false, landed = false;
        for (int step = 0; step < 60 * 60; step++) {
            park(run);
            // (Parked over her, she is swooped at, and with the notes about her that is a blow: what Lucy has left is kept where it was.)
            lucy.left = 8;
            if (run.absent(lucy)) continue;
            if (lucy.step == 1) did |= 1 << lucy.mode;
            // On the wing while she is at it; down while she is spent.
            flew |= lucy.y < lucy.ground - 2;
            landed |= lucy.step == 2 && lucy.y > lucy.ground - .1f;
        }
        assertEquals(1 << 1 | 1 << 2 | 1 << 4 | 1 << 5, did);
        assertTrue(flew && landed);
        assertFalse(run.decided());
    }

    /** How far clear of something on its way a place is: nothing or less, and it lands there. */
    private static float clear(RunFlight.Thing thing, float x, float y) {
        return thing.kind == RunFlight.GUST ? Math.abs(y - thing.y) - RunFlight.BAR : thing.kind == RunFlight.PILLAR ? Math.abs(x - thing.x) - RunFlight.BAR
                : (float) Math.hypot(x - thing.x, y - thing.y) - RunFlight.NEAR;
    }

    /**
     * A flight flown by someone who has understood it: out of the way of what is about to arrive, if she is not behind the beam; and
     * otherwise to where Lucy is, with the beam held. Until it is over, or the time is up.
     */
    private static void fly(RunFlight flight, float seconds) {
        float[][] ways = {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        for (int step = 0; step < seconds * 60 && flight.state() == RunFlight.FLYING; step++) {
            float x = flight.x(), y = flight.y();
            boolean threatened = false;
            for (RunFlight.Thing thing : flight.things()) threatened |= !thing.gone && thing.far / thing.speed < .7f && clear(thing, x, y) < .1f;
            if (threatened && flight.beam() <= 0) {
                int chosen = 0;
                float best = -9;
                for (int way = 0; way < ways.length; way++) {
                    float least = 9;
                    for (RunFlight.Thing thing : flight.things()) {
                        float until = thing.far / thing.speed;
                        if (thing.gone || until > 1) continue;
                        float thereX = Math.max(-1, Math.min(1, x + ways[way][0] * RunFlight.SPEED * until));
                        float thereY = Math.max(-RunFlight.HIGH, Math.min(RunFlight.HIGH, y + ways[way][1] * RunFlight.SPEED * until));
                        least = Math.min(least, clear(thing, thereX, thereY));
                    }
                    if (least > best + .001f) {
                        best = least;
                        chosen = way;
                    }
                }
                flight.step(STEP, ways[chosen][0] < 0, ways[chosen][0] > 0, ways[chosen][1] < 0, ways[chosen][1] > 0, false);
            } else {
                flight.step(STEP, flight.lucyX() < x - .06f, flight.lucyX() > x + .06f, flight.lucyY() < y - .06f, flight.lucyY() > y + .06f, true);
            }
        }
    }

    @Test public void theFlightIsWonWithTheBeamAndByNothingElse() {
        int hurts = 0;
        for (long seed = 1; seed <= 24; seed++) {
            RunFlight flight = new RunFlight(6, 2, seed);
            assertEquals(RunFlight.FLYING, flight.state());
            assertEquals(1, flight.left(), 0);
            fly(flight, 240);
            assertEquals("seed " + seed + " after " + flight.time() + " with " + flight.hurts() + " hurts", RunFlight.WON, flight.state());
            assertEquals(0, flight.left(), 0);
            assertEquals(RunFlight.TAKES, flight.hits());
            assertEquals(2, flight.stages());
            assertEquals(2, flight.stage());
            assertTrue("seed " + seed, flight.beams() >= 3);
            assertTrue(flight.things().isEmpty());
            hurts += flight.hurts();
        }
        // Flown well it costs little: less than a hurt a flight, on the whole.
        assertTrue("hurts " + hurts, hurts < 24);
        // Without the beam nothing is ever taken from her, however long it goes on; and what comes has to be flown round.
        RunFlight dodging = new RunFlight(600, 2, 5);
        for (int step = 0; step < 60 * 60; step++) dodging.step(STEP, step / 90 % 2 == 0, step / 90 % 2 == 1, false, false, false);
        assertEquals(RunFlight.FLYING, dodging.state());
        assertEquals(1, dodging.left(), 0);
        assertEquals(0, dodging.beams());
        assertTrue(dodging.thrown() > 20);
        // Left where she is, she is hurt until she falls.
        RunFlight still = new RunFlight(6, 2, 7);
        for (int step = 0; step < 60 * 60 && still.state() == RunFlight.FLYING; step++) still.step(STEP, false, false, false, false, false);
        assertEquals(RunFlight.FELL, still.state());
        assertEquals(3, still.hurts());
        assertEquals(0, still.halves());
        // What she wears takes some of a hurt off there too: at a half a hurt she lasts twice as many of them.
        RunFlight worn = new RunFlight(6, 1, 7);
        for (int step = 0; step < 60 * 90 && worn.state() == RunFlight.FLYING; step++) worn.step(STEP, false, false, false, false, false);
        assertEquals(6, worn.hurts());
    }

    @Test public void theBeamIsGatheredAndLastsAndRestsAndCutsWhatComesAlongItOutOfTheAir() {
        RunFlight flight = new RunFlight(6, 2, 3);
        assertEquals(0, flight.charge(), 0);
        assertEquals(1, flight.rested(), 0);
        // Held, it gathers; let go of early, it is lost.
        for (int step = 0; step < 20; step++) flight.step(STEP, false, false, false, false, true);
        assertTrue(flight.charge() > .5f && flight.charge() < 1);
        flight.step(STEP, false, false, false, false, false);
        assertEquals(0, flight.charge(), 0);
        assertEquals(0, flight.beams());
        // Held long enough, it is out, for as long as it lasts; and then it rests before it can be gathered again.
        float before = flight.time();
        while (flight.beam() <= 0 && flight.time() < before + 2) flight.step(STEP, false, false, false, false, true);
        assertEquals(RunFlight.CHARGE, flight.time() - before, .03f);
        assertEquals(1, flight.beams());
        before = flight.time();
        while (flight.beam() > 0) flight.step(STEP, false, false, false, false, true);
        assertEquals(RunFlight.LASTS, flight.time() - before, .03f);
        assertTrue(flight.rested() < .1f);
        assertEquals(0, flight.charge(), 0);
        before = flight.time();
        while (flight.beam() <= 0 && flight.time() < before + 5) flight.step(STEP, false, false, false, false, true);
        assertEquals(RunFlight.WAIT + RunFlight.CHARGE, flight.time() - before, .05f);
        assertEquals(2, flight.beams());
        // While it is gathered and while it is out she flies more slowly.
        RunFlight slow = new RunFlight(6, 2, 3), quick = new RunFlight(6, 2, 3);
        for (int step = 0; step < 20; step++) {
            slow.step(STEP, false, true, false, false, true);
            quick.step(STEP, false, true, false, false, false);
        }
        assertEquals(quick.x() * RunFlight.SLOWED, slow.x(), .01f);
        // And out, it cuts what is coming at her along it out of the air: held on where she is, nothing arrives while it lasts.
        RunFlight behind = new RunFlight(6, 2, 11);
        int hurts = 0, cut = 0;
        for (int step = 0; step < 60 * 40 && behind.state() == RunFlight.FLYING; step++) {
            int had = behind.hurts();
            boolean out = behind.beam() > 0;
            behind.step(STEP, false, false, false, false, true);
            if (out && behind.hurts() > had) hurts++;
            cut = behind.wiped();
        }
        assertEquals(0, hurts);
        assertTrue(cut > 3);
    }

    @Test public void theWhiteThatIsTrulyHersIsHersToCallOnInAnyLandOnceItHasBeenWon() {
        // Not hers: it cannot be called on.
        PlusRun run = new PlusRun(RunLevels.level(40));
        assertFalse(run.canMaster());
        assertFalse(run.callWhite());
        assertFalse(run.mastered());
        // Hers: called on, it is every colour at once, with no time to it; and the beam is what a held strike is.
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.white = true;
        run = new PlusRun(RunLevels.level(40), perks);
        hold(run, .3f, false, false, false);
        assertTrue(run.canMaster());
        assertFalse(run.white());
        assertTrue(run.callWhite());
        assertTrue(run.mastered() && run.white());
        assertFalse(run.canMaster());
        assertFalse(run.callWhite());
        hold(run, 60, false, false, false);
        assertTrue(run.mastered() && run.white());
        assertEquals(0, run.beams());
        for (int step = 0; step < 60 && run.beams() == 0; step++) run.step(STEP, false, false, false, true);
        assertEquals(1, run.beams());
        // And it rests between one beam and the next, for longer than it lasts.
        float began = run.time();
        while (run.beams() < 2 && run.time() < began + 20) run.step(STEP, false, false, false, true);
        assertEquals(PlusRun.BEAM_LASTS + PlusRun.BEAM_WAIT + PlusRun.BEAM_CHARGE, run.time() - began, .1f);
        assertTrue(PlusRun.BEAM_WAIT > PlusRun.BEAM_LASTS);
        // Not in the last land before the ribbon is thrown: there it is what the ribbon turns into.
        PlusRun.Perks whole = new PlusRun.Perks();
        whole.white = true;
        whole.whole = true;
        PlusRun last = new PlusRun(RunLevels.level(RunLevels.TOTAL), whole);
        hold(last, .3f, false, false, false);
        assertFalse(last.canMaster());
        assertFalse(last.callWhite());
        assertFalse(last.mastered());
    }

    // The ride: Halgakos on Lucy's back, and the Mother Tongue

    /** How far clear of something the Mother has sent a place is: nothing or less, and it lands there. */
    private static float clearOf(RunRide.Thing thing, float x, float y) { return RunRide.clear(thing, x, y); }

    /**
     * The ride ridden by someone who has understood it: to the way through what is coming, in time; otherwise to where the Mother is,
     * with Lucy's breath always, and the beam when she is open or nearly so. Without the breath, or without the beam, if asked.
     */
    private static void ride(RunRide ride, float seconds, boolean beams, boolean breathes) {
        float[][] ways = {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        for (int step = 0; step < seconds * 60 && ride.state() == RunRide.FLYING; step++) {
            float x = ride.x(), y = ride.y(), pace = RunRide.SPEED * (ride.beam() > 0 ? RunRide.SLOWED : 1);
            boolean threatened = false;
            for (RunRide.Thing thing : ride.things()) {
                threatened |= !thing.gone && thing.far / thing.speed < (thing.kind == RunRide.NAME ? .5f : 1.7f) && clearOf(thing, x, y) < .08f;
            }
            if (threatened) {
                int chosen = 0;
                float best = -9;
                for (int way = 0; way < ways.length; way++) {
                    float least = 9;
                    for (RunRide.Thing thing : ride.things()) {
                        float until = thing.far / thing.speed;
                        if (thing.gone || until > (thing.kind == RunRide.NAME ? .6f : 1.9f)) continue;
                        float thereX = Math.max(-1, Math.min(1, x + ways[way][0] * pace * until));
                        float thereY = Math.max(-RunRide.HIGH, Math.min(RunRide.HIGH, y + ways[way][1] * pace * until));
                        least = Math.min(least, Math.min(.2f, clearOf(thing, thereX, thereY)));
                    }
                    if (least > best + .001f) {
                        best = least;
                        chosen = way;
                    }
                }
                ride.step(STEP, ways[chosen][0] < 0, ways[chosen][0] > 0, ways[chosen][1] < 0, ways[chosen][1] > 0, false, breathes);
            } else {
                ride.step(STEP, ride.herX() < x - .06f, ride.herX() > x + .06f, ride.herY() < y - .06f, ride.herY() > y + .06f,
                        beams && (ride.open() > 0 || ride.shield() <= 1), breathes);
            }
        }
    }

    @Test public void theMotherTongueSendsHerWideOnesEachMarkedAndFairAndIsDesperateAtTheLast() {
        // Each of the wide ones lands where it says and nowhere else.
        RunRide.Thing sweep = new RunRide.Thing(), ring = new RunRide.Thing(), rain = new RunRide.Thing(), ray = new RunRide.Thing(), shard = new RunRide.Thing();
        sweep.kind = RunRide.SWEEP;
        sweep.y = .3f;
        assertTrue(RunRide.lands(sweep, -.9f, .35f));
        assertTrue(RunRide.lands(sweep, .9f, .2f));
        assertFalse(RunRide.lands(sweep, 0, -.1f));
        assertFalse(RunRide.lands(sweep, 0, .6f));
        ring.kind = RunRide.RING;
        ring.size = .6f;
        assertTrue(RunRide.lands(ring, .6f, 0));
        assertTrue(RunRide.lands(ring, 0, -.55f));
        assertFalse("inside it", RunRide.lands(ring, .1f, .1f));
        assertFalse("outside it", RunRide.lands(ring, .95f, .6f));
        rain.kind = RunRide.RAIN;
        rain.x = -.5f;
        rain.other = .4f;
        assertFalse(RunRide.lands(rain, -.5f, .7f));
        assertFalse(RunRide.lands(rain, .45f, -.7f));
        assertTrue(RunRide.lands(rain, 0, 0));
        assertTrue(RunRide.lands(rain, .9f, 0));
        ray.kind = RunRide.RAY;
        ray.size = (float) (Math.PI / 4);
        assertTrue(RunRide.lands(ray, .3f, .3f));
        assertTrue(RunRide.lands(ray, -.5f, -.5f));
        assertFalse(RunRide.lands(ray, .5f, -.4f));
        assertFalse(RunRide.lands(ray, -.6f, .5f));
        shard.kind = RunRide.SHARD;
        shard.x = .2f;
        assertTrue(RunRide.lands(shard, .25f, .05f));
        assertFalse(RunRide.lands(shard, .5f, 0));
        // Ridden well: she sends every kind she has, words burst into pieces, the beam and the blasts meet on her, and at the last she is desperate;
        // and it is still won, with hurts to spare.
        int[] kinds = new int[10];
        int bursts = 0, met = 0, hurts = 0;
        for (long seed = 1; seed <= 16; seed++) {
            RunRide ride = new RunRide(20, 3, seed);
            ride(ride, 400, true, true);
            assertEquals("seed " + seed, RunRide.WON, ride.state());
            assertTrue("seed " + seed, ride.desperate());
            for (int kind = 0; kind < 10; kind++) kinds[kind] += ride.sentOf(kind);
            bursts += ride.bursts();
            met += ride.met();
            hurts += ride.hurts();
        }
        for (int kind : new int[]{RunRide.BLOCKS, RunRide.PRESS, RunRide.NAME, RunRide.PASS, RunRide.SWEEP, RunRide.RING, RunRide.RAIN, RunRide.RAY, RunRide.BURST}) {
            assertTrue("kind " + kind, kinds[kind] > 0);
        }
        assertEquals("pieces are not sent: they come of a burst", 0, kinds[RunRide.SHARD]);
        assertTrue(bursts > 0);
        assertTrue("met " + met, met >= 16);
        assertTrue("hurts " + hurts, hurts < 48);
    }

    @Test public void theMotherTongueIsReadOnlyWhileHerShieldIsBrokenAndTheLastOfItTakesTheBeamAndTheBreathTogether() {
        // (With the most hearts there are, ten, and the best there is to wear, which leaves a hurt of the last land a heart and a half.)
        int hurts = 0;
        for (long seed = 1; seed <= 16; seed++) {
            RunRide ride = new RunRide(20, 3, seed);
            assertEquals(RunRide.SHIELD[0], ride.shield());
            assertEquals(0, ride.open(), 0);
            ride(ride, 400, true, true);
            assertEquals("seed " + seed + " after " + ride.time() + " with " + ride.hurts() + " hurts", RunRide.WON, ride.state());
            // The beam took all of it but the last; her shield was broken once for each stage at the least; and every piece of it went to a breath or a friend.
            assertEquals(RunRide.TAKES - 1, ride.hits());
            assertEquals(0, ride.left(), 0);
            assertEquals(2, ride.stages());
            assertTrue("seed " + seed, ride.opens() >= 3);
            assertTrue("seed " + seed, ride.chips() >= RunRide.SHIELD[0] + RunRide.SHIELD[1] + RunRide.SHIELD[2]);
            assertTrue("seed " + seed, ride.puffs() > ride.chips() - ride.helps());
            assertTrue(ride.things().isEmpty());
            hurts += ride.hurts();
        }
        assertTrue("hurts " + hurts, hurts < 48);
        // With the beam and no breath: her friends break her shield for her, in time, and the beam reads her; but the last of it never goes.
        RunRide beamOnly = new RunRide(6000, 2, 3);
        ride(beamOnly, 500, true, false);
        assertEquals(RunRide.FLYING, beamOnly.state());
        assertEquals(RunRide.TAKES - 1, beamOnly.hits());
        assertTrue(beamOnly.finishing());
        assertEquals(beamOnly.chips(), beamOnly.helps());
        assertEquals(0, beamOnly.puffs());
        // With the breath and no beam: her shield is broken over and over, and nothing is ever read.
        RunRide breathOnly = new RunRide(6000, 2, 3);
        ride(breathOnly, 200, false, true);
        assertEquals(RunRide.FLYING, breathOnly.state());
        assertEquals(1, breathOnly.left(), 0);
        assertEquals(0, breathOnly.hits());
        assertTrue(breathOnly.opens() >= 4);
        // And someone who only makes for her, with both buttons held, flies into what she sends, and falls.
        RunRide careless = new RunRide(6, 2, 4);
        for (int step = 0; step < 60 * 300 && careless.state() == RunRide.FLYING; step++) {
            careless.step(STEP, careless.herX() < careless.x() - .06f, careless.herX() > careless.x() + .06f, careless.herY() < careless.y() - .06f,
                    careless.herY() > careless.y() + .06f, true, true);
        }
        assertEquals(RunRide.FELL, careless.state());
        assertEquals(3, careless.hurts());
    }

    @Test public void whatTheMotherSendsIsFlownRoundEachInItsOwnWay() {
        RunRide.Thing blocks = new RunRide.Thing(), press = new RunRide.Thing(), name = new RunRide.Thing(), pass = new RunRide.Thing();
        blocks.kind = RunRide.BLOCKS;
        blocks.x = .62f;
        press.kind = RunRide.PRESS;
        press.y = -.3f;
        name.kind = RunRide.NAME;
        name.x = .2f;
        name.y = .2f;
        pass.kind = RunRide.PASS;
        pass.x = -.58f;
        // A row of blocks lands everywhere but in its way through, whatever the height.
        assertFalse(RunRide.lands(blocks, .62f, .7f));
        assertFalse(RunRide.lands(blocks, .4f, -.7f));
        assertTrue(RunRide.lands(blocks, 0, 0));
        assertTrue(RunRide.lands(blocks, -.62f, 0));
        // A press lands everywhere but at its slit, wherever across.
        assertFalse(RunRide.lands(press, -.9f, -.3f));
        assertFalse(RunRide.lands(press, .9f, -.1f));
        assertTrue(RunRide.lands(press, 0, .3f));
        // A name lands only where it arrives; and her pass, down its own side of the sky.
        assertTrue(RunRide.lands(name, .3f, .3f));
        assertFalse(RunRide.lands(name, .6f, .2f));
        assertTrue(RunRide.lands(pass, -.9f, .5f));
        assertTrue(RunRide.lands(pass, -.2f, -.5f));
        assertFalse(RunRide.lands(pass, 0, 0));
        // A name follows whoever it is for, and the breath cuts it out of the air; blocks and a press are not cut by anything.
        RunRide ride = new RunRide(6000, 2, 2);
        int followed = 0;
        for (int step = 0; step < 60 * 60; step++) {
            float[] was = null;
            for (RunRide.Thing thing : ride.things()) if (thing.kind == RunRide.NAME && thing.far > RunRide.LOCKS + .05f && !thing.gone) was = new float[]{thing.x, thing.y, ride.x(), ride.y()};
            RunRide.Thing one = null;
            for (RunRide.Thing thing : ride.things()) if (thing.kind == RunRide.NAME && thing.far > RunRide.LOCKS + .05f && !thing.gone) one = thing;
            ride.step(STEP, step / 120 % 2 == 0, step / 120 % 2 == 1, false, false, false, false);
            if (was != null && one != null && !one.gone && Math.hypot(was[2] - was[0], was[3] - was[1]) > .05) {
                assertTrue(Math.hypot(ride.x() - one.x, ride.y() - one.y) < Math.hypot(was[2] - was[0], was[3] - was[1]) + .03);
                followed++;
            }
        }
        assertTrue(followed > 60);
        assertEquals(0, ride.cut());
        RunRide breathing = new RunRide(6000, 2, 2);
        for (int step = 0; step < 60 * 60; step++) breathing.step(STEP, false, false, false, false, false, true);
        assertTrue(breathing.cut() >= 4);
        int kinds = 0;
        for (int step = 0; step < 60 * 30; step++) {
            breathing.step(STEP, false, false, false, false, true, true);
            for (RunRide.Thing thing : breathing.things()) if (thing.kind != RunRide.NAME) assertFalse(thing.gone);
            for (RunRide.Thing thing : breathing.things()) kinds |= 1 << thing.kind;
        }
        assertTrue((kinds & 1 << RunRide.BLOCKS) != 0 && (kinds & 1 << RunRide.PRESS) != 0);
    }

    // The ribbon gacha, and the pots of black ink

    /** A run of a land that has a ribbon gacha in it, with her standing at its machine; or, not gone in, at its door. The room is put in the array. */
    private static PlusRun atMachine(int from, boolean in, int[][] found) {
        for (int number = from; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int[] one : level.rooms) {
                if (one[0] != RunLevels.HOUSE || one[14] != RunLevels.GACHA) continue;
                PlusRun run = new PlusRun(level);
                run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
                run.step(STEP, false, false, false);
                if (in) {
                    assertTrue(run.enter());
                    run.place(one[3] + 7.4f, GROUND);
                    run.step(STEP, false, false, false);
                    assertEquals(PlusRun.MACHINE, run.spot());
                }
                found[0] = one;
                return run;
            }
        }
        throw new AssertionError("no gacha from land " + from);
    }

    /** She leaves the room she is in by its door. */
    private static void outOf(PlusRun run, int[] room) {
        run.place(room[6] + (1 - PlusRun.WIDE) / 2, GROUND);
        hold(run, .2f, false, false, false);
        assertTrue(run.enter());
        assertEquals(-1, run.room());
    }

    @Test public void theRibbonGachaGivesRibbonsByChanceKeepsTheirTimeStillAndBlowsUpOnBlack() {
        // What a machine holds: four colours early, then five, then all six; never black and never white, which are not chosen.
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            int[] holds = RunLevels.machineOf(number);
            assertEquals(number <= 20 ? 4 : number <= 40 ? 5 : 6, holds.length);
            for (int colour : holds) assertTrue(colour >= PlusRun.RED && colour <= PlusRun.PURPLE);
        }
        assertEquals(13, PlusRun.blackOdds(5));
        assertEquals(9, PlusRun.blackOdds(50));
        int[][] room = new int[1][];
        // Not gone into, nothing is rolled.
        PlusRun run = atMachine(RunLevels.GACHA_FROM, false, room);
        assertFalse(run.canRoll());
        assertEquals(0, run.roll(0));
        // In its room a ribbon's time stands still.
        run = atMachine(RunLevels.GACHA_FROM, true, room);
        assertTrue(run.level().number <= 20);
        run.lend(PlusRun.PURPLE);
        float had = run.on(PlusRun.PURPLE);
        assertTrue(had > 5);
        hold(run, 3, false, false, false);
        assertTrue(run.frozen());
        assertEquals(had, run.on(PlusRun.PURPLE), .001f);
        // A roll costs more each time; and gives a ribbon of one of the machine's colours, which is in her hair.
        assertTrue(run.canRoll());
        assertEquals(PlusRun.GACHA_FIRST, run.gachaCost());
        assertEquals(PlusRun.RED, run.roll(0));
        assertTrue(run.on(PlusRun.RED) > 5);
        assertEquals(1 << PlusRun.RED - 1, run.gachaHeld());
        assertEquals(PlusRun.GACHA_FIRST + PlusRun.GACHA_MORE, run.gachaCost());
        // One she has had from it already is not given again: twice; and after two such the next is one she has not.
        assertEquals(-PlusRun.RED, run.roll(5));
        assertEquals(-PlusRun.RED, run.roll(9));
        assertEquals(2, run.gachaDupes());
        assertEquals(PlusRun.BLUE, run.roll(0));
        assertEquals(2, run.gachaDupes());
        assertEquals(PlusRun.GREEN, run.roll(20));
        assertFalse(run.white());
        assertFalse(run.gachaDone());
        // The last of what it holds: all its colours in one land are a white ribbon, of the kind that is not mastered. And it has no more for her.
        assertEquals(PlusRun.YELLOW, run.roll(30));
        assertTrue(run.gachaDone());
        assertEquals(1, run.gachaWhites());
        assertTrue(run.white());
        assertFalse(run.mastered());
        assertFalse(run.canRoll());
        assertEquals(0, run.roll(0));
        assertEquals(6, run.gachaRolls());
        // Still, all of it, for as long as she stays; and running from the moment she leaves.
        float blue = run.on(PlusRun.BLUE);
        hold(run, 4, false, false, false);
        assertEquals(blue, run.on(PlusRun.BLUE), .001f);
        outOf(run, room[0]);
        assertFalse(run.frozen());
        hold(run, 2, false, false, false);
        assertEquals(blue - 2, run.on(PlusRun.BLUE), .3f);
        assertTrue(run.white());

        // A black one: the machine blows up, and is shut; every ribbon she wore is gone; and black lies on her for twice a ribbon's time,
        // which does not begin to run until she leaves.
        run = atMachine(RunLevels.GACHA_FROM, true, room);
        assertEquals(PlusRun.RED, run.roll(3));
        assertEquals(PlusRun.BLUE, run.roll(13));
        assertEquals(PlusRun.BLACK, run.roll(40));
        assertTrue(run.gachaBlown());
        assertEquals(0, run.gachaHeld());
        assertFalse(run.coloured());
        assertEquals(PlusRun.BLACKENED, run.cursed(), .001f);
        assertFalse(run.canRoll());
        assertEquals(0, run.roll(0));
        hold(run, 3, false, false, false);
        assertEquals(PlusRun.BLACKENED, run.cursed(), .001f);
        outOf(run, room[0]);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.BLACKENED - 2, run.cursed(), .3f);
        assertEquals(0, run.hurts());
        // While black lies on her the machine gives nothing.
        run = atMachine(RunLevels.GACHA_FROM, false, room);
        run.lend(PlusRun.BLACK);
        assertTrue(run.cursed() > 0);
        assertTrue(run.enter());
        run.place(room[0][3] + 7.4f, GROUND);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.MACHINE, run.spot());
        assertFalse(run.canRoll());
        // A machine of the later lands holds all six: and all six from it are white as well.
        run = atMachine(41, true, room);
        assertEquals(6, RunLevels.machineOf(run.level().number).length);
        for (int index = 0; index < 6; index++) assertTrue(run.roll(index * PlusRun.GACHA_COLOUR) > 0);
        assertTrue(run.gachaDone() && run.white());
    }

    @Test public void potsOfBlackInkStandInTheLaterLandsAndThrowTheCurseAtWhoeverIsNear() {
        int pots = 0, without = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int here = 0, was = -99;
            for (int[] item : level.items) {
                if (item[0] != PlusRun.INKPOT) continue;
                here++;
                // On flat ground of the land itself, in the open, and well apart.
                assertTrue("land " + number, item[1] < level.main && item[1] >= 40);
                assertEquals("land " + number, item[2] + 1, RunLevels.top(level, item[1]));
                assertEquals(RunLevels.AIR, level.land[item[2]][item[1]]);
                assertEquals(item[2] + 1, RunLevels.top(level, item[1] - 1));
                assertEquals(item[2] + 1, RunLevels.top(level, item[1] + 1));
                for (int flag : level.flags) assertTrue(Math.abs(flag - item[1]) > 8);
                for (int[] other : level.items) if (other != item && other[0] == PlusRun.INKPOT) assertTrue(Math.abs(other[1] - item[1]) > 30);
            }
            if (number < RunLevels.POTS_FROM) assertEquals("land " + number, 0, here);
            else assertTrue("land " + number + " has " + here, here <= 1 + (number - RunLevels.POTS_FROM) / 15);
            if (number >= RunLevels.POTS_FROM && here == 0) without++;
            pots += here;
        }
        // (A land here and there has no flat, empty ground for one.)
        assertTrue("pots " + pots + " lands without " + without, pots >= 45 && without <= 6);
        // One swells, and throws: at her, if she is near; black lies on her, and it costs her nothing else.
        RunLevels.Level level = flat(90);
        level.items.add(new int[]{PlusRun.INKPOT, 20, FLOOR - 1, 0, 0});
        PlusRun run = new PlusRun(level);
        run.place(14, GROUND);
        run.lend(PlusRun.RED);
        float least = 1, most = 0;
        for (float gone = 0; gone < PlusRun.POT_EVERY + 2.5f; gone += STEP) {
            run.step(STEP, false, false, false);
            float turn = run.potTurn(level.items.get(0));
            least = Math.min(least, turn);
            most = Math.max(most, turn);
        }
        assertTrue(least < .05f && most > .95f);
        assertEquals(1, run.potShots());
        assertEquals(1, run.curses());
        assertTrue(run.cursed() > 0);
        assertFalse(run.coloured());
        assertEquals(0, run.hurts());
        assertEquals(PlusRun.HEARTS, run.hearts());
        // Far from it, nothing is thrown.
        run = new PlusRun(level);
        run.place(60, GROUND);
        hold(run, PlusRun.POT_EVERY * 2 + 1, false, false, false);
        assertEquals(0, run.potShots());
        assertEquals(0, run.curses());
    }

    // The four won in the black arena

    /** The button is held, with every sign kept where it began, until something heavy has been done or a second has gone by. */
    private static void heldUntil(PlusRun run, java.util.function.IntSupplier count) {
        int had = count.getAsInt();
        for (int step = 0; step < 60 && count.getAsInt() == had; step++) {
            for (PlusRun.Foe foe : run.foes()) if (!foe.flat) foe.x = foe.home;
            run.step(STEP, false, false, false, true);
        }
        assertEquals(had + 1, count.getAsInt());
    }

    @Test public void theFourWonInTheBlackArenaEachDoWhatTheirCardsSay() {
        assertEquals(RunArms.COUNT, RunArms.NAMES.length);
        assertEquals(6, RunArms.MAINS.length);
        for (int thing : RunArms.MAINS) assertTrue(RunArms.main(thing) && RunArms.heavy(thing) && RunArms.holds(thing, 16) == 0);
        for (int thing = 0; thing < RunArms.COUNT; thing++) {
            assertFalse(RunArms.does(thing).isEmpty() || RunArms.goodFor(thing).isEmpty());
            assertTrue(RunArms.rest(thing, 0) > 0 && RunArms.reach(thing, 0) > 0 && RunArms.bite(thing, 0) > 0);
        }

        // The katana. A tap is a cut that reaches further than a sword's stroke, and no further than it says.
        PlusRun run = forged(PlusRun.KATANA, 0, 5, 7);
        strike(run);
        assertEquals(1, run.cuts());
        assertEquals(1, run.chain());
        assertTrue(run.foes().get(0).flat);
        assertFalse(run.foes().get(1).flat);
        org.junit.Assert.assertArrayEquals(new int[]{RunLevels.MINUS, PlusRun.KATANA + 1}, run.fells().get(0));
        assertTrue(RunArms.reach(RunArms.KATANA, 0) > RunArms.reach(RunArms.SWORD, 0) * 2);
        // Taps in rhythm are one, two, three in a row, and then one again; a tap that comes late begins again.
        run = forged(PlusRun.KATANA, 0);
        for (int wanted : new int[]{1, 2, 3, 1, 2}) {
            strike(run);
            assertEquals(wanted, run.chain());
            still(run, .4f);
        }
        still(run, RunArms.RHYTHM);
        strike(run);
        assertEquals(1, run.chain());
        // The third of three counts for one blow more: one that takes five has one left after three cuts, not two.
        RunLevels.Level land = flat(80);
        land.foes.add(new int[]{RunLevels.MINUS, 4, FLOOR - 1, 5});
        run = new PlusRun(land);
        run.arm(PlusRun.KATANA);
        PlusRun.Foe big = run.foes().get(0);
        for (int left : new int[]{4, 3, 1}) {
            big.dazed = 0;
            big.x = big.home;
            strike(run);
            assertEquals(left * PlusRun.BLOW, big.left);
            still(run, .4f);
        }
        // Held, she dashes: so far along her line, through whatever stands on it; nothing on it is cut until a beat later, and then all of it is.
        run = forged(PlusRun.KATANA, 0, 6, 7);
        float from = run.x();
        heldUntil(run, run::dashes);
        assertEquals(from + RunArms.DASH, run.x(), .3f);
        assertFalse(run.foes().get(0).flat || run.foes().get(1).flat);
        assertTrue(run.dashLeft() > 0);
        still(run, RunArms.BEAT + .05f);
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);
        assertEquals(0, run.hurts());
        // And no further than a wall.
        land = flat(80);
        for (int row = FLOOR - 3; row < FLOOR; row++) land.land[row][5] = RunLevels.BLOCK;
        run = new PlusRun(land);
        run.arm(PlusRun.KATANA);
        heldUntil(run, run::dashes);
        assertTrue("stopped at " + run.x(), run.x() + PlusRun.WIDE <= 5.01f && run.x() + PlusRun.WIDE > 4.6f);
        // In the air the cut is downward.
        run = forged(PlusRun.KATANA, 0, 20);
        run.place(run.foes().get(0).x, GROUND - 1.4f);
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.airCuts());
        assertEquals(0, run.cuts());
        assertTrue(run.foes().get(0).flat);
        // Worked on: a longer draw; then a wave with every draw.
        assertTrue(RunArms.reach(RunArms.KATANA, 1) > RunArms.reach(RunArms.KATANA, 0));
        run = forged(PlusRun.KATANA, 2, 7);
        strike(run);
        assertFalse(run.foes().get(0).flat);
        assertEquals(1, flying(run, PlusRun.SLASH));
        still(run, .4f);
        assertTrue(run.foes().get(0).flat);

        // The lance: a thrust through everything in front of her; and, held, a jet that goes further still.
        run = forged(PlusRun.LANCE, 0, 4, 5, 7);
        strike(run);
        assertEquals(1, run.thrusts());
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);
        assertFalse(run.foes().get(2).flat);
        org.junit.Assert.assertArrayEquals(new int[]{RunLevels.MINUS, PlusRun.LANCE + 1}, run.fells().get(0));
        run = forged(PlusRun.LANCE, 0, 7, 8);
        heldUntil(run, run::jets);
        assertEquals(1, flying(run, PlusRun.SLASH));
        still(run, .5f);
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);

        // The cannon: a ball that bursts wide where it lands; and, held, a great one that goes straight through and bursts at the end.
        run = forged(PlusRun.CANNON, 0, 8, 9);
        strike(run);
        assertEquals(1, flying(run, PlusRun.INKBALL));
        boolean burst = false;
        for (int step = 0; step < 200 && !burst; step++) {
            still(run, STEP);
            for (float[] boom : run.booms()) burst |= boom[3] == PlusRun.SHELLED && boom[2] == RunArms.burst(RunArms.CANNON, 0);
        }
        assertTrue(burst);
        assertTrue(run.foes().get(0).flat || run.foes().get(1).flat);
        assertTrue(RunArms.rest(RunArms.CANNON, 0) >= 1);
        run = forged(PlusRun.CANNON, 0, 6, 8);
        heldUntil(run, run::shells);
        boolean great = false;
        for (int step = 0; step < 200 && !great; step++) {
            still(run, STEP);
            for (float[] boom : run.booms()) great |= boom[3] == PlusRun.SHELLED && boom[2] == RunArms.SHELL_WIDE;
        }
        assertTrue(great);
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);

        // The whip: a lash that takes what is well over her head, which a sword's stroke would not; and, held, a spin that takes both sides.
        for (int thing : new int[]{PlusRun.WHIP, PlusRun.SWORD}) {
            run = forged(thing, 0, 3);
            PlusRun.Foe over = run.foes().get(0);
            over.ground -= 1.7f;
            over.y = over.ground;
            run.step(STEP, false, false, false, true);
            assertEquals(thing == PlusRun.WHIP, over.flat);
        }
        run = forged(PlusRun.WHIP, 0, 8, 12);
        run.place(10, GROUND);
        hold(run, .05f, false, false, false);
        for (PlusRun.Foe foe : run.foes()) foe.x = foe.home;
        heldUntil(run, run::spins);
        assertTrue(run.foes().get(0).flat && run.foes().get(1).flat);
        org.junit.Assert.assertArrayEquals(new int[]{RunLevels.MINUS, PlusRun.WHIP + 1}, run.fells().get(0));
        // And the hunts know the ways of all four.
        for (int thing : new int[]{PlusRun.KATANA, PlusRun.LANCE, PlusRun.CANNON, PlusRun.WHIP}) {
            int hunt = RunHunts.of(RunHunts.BY_WAY, thing + 1, 10, 0, false);
            assertTrue(RunHunts.words(hunt).startsWith("Beat 10 creatures with the "));
            assertTrue(RunHunts.counts(hunt, RunLevels.MINUS, thing + 1));
        }
    }

    @Test public void theBlackArenaIsALadderOfThoseSheHasGotPastStrongerAndEveryRungGivesSomething() {
        assertEquals(RunMarket.RUNGS, RunMarket.LANDS.length);
        java.util.Set<Integer> weapons = new java.util.HashSet<>();
        for (int rung = 1; rung <= RunMarket.RUNGS; rung++) {
            int land = RunMarket.LANDS[rung - 1];
            // Someone waits in each of its lands; the rungs go on through the story; and each asks more than the one before.
            assertTrue("rung " + rung, RunLevels.bossOf(land) >= 0);
            if (rung > 1) {
                assertTrue(land > RunMarket.LANDS[rung - 2]);
                assertTrue(RunMarket.takes(rung) >= RunMarket.takes(rung - 1) && RunMarket.shields(rung) >= RunMarket.shields(rung - 1) && RunMarket.bites(rung) >= RunMarket.bites(rung - 1));
            }
            assertTrue(RunMarket.takes(rung) >= 2 && RunMarket.shields(rung) >= 1);
            assertTrue(RunMarket.tokens(rung, true) > RunMarket.tokens(rung, false));
            assertEquals(1, RunMarket.tokens(rung, false));
            assertFalse(RunMarket.prizeWords(rung).isEmpty());
            if (RunMarket.prize(rung) == RunMarket.WEAPON) weapons.add(RunMarket.prizeOf(rung));
            // In the arena whoever waits takes that much more, behind that much more of a shield; and a hurt costs that much more.
            RunLevels.Level level = RunLevels.level(land);
            PlusRun plain = new PlusRun(level);
            PlusRun.Perks perks = new PlusRun.Perks();
            perks.black = rung;
            PlusRun black = new PlusRun(level, perks);
            assertEquals(plain.bite() + RunMarket.bites(rung), black.bite());
            assertEquals(plain.tough(), black.tough());
            boolean waited = false;
            for (int index = 0; index < plain.foes().size(); index++) {
                PlusRun.Foe was = plain.foes().get(index), is = black.foes().get(index);
                if (!RunLevels.boss(was.kind) || RunLevels.rover(was.kind)) {
                    assertEquals(was.left, is.left);
                    continue;
                }
                waited = true;
                assertEquals("rung " + rung, was.left + RunMarket.takes(rung), is.left);
                assertEquals(is.left, is.most);
                assertEquals("rung " + rung, was.shieldMost + (was.shieldMost > 0 ? RunMarket.shields(rung) : 0), is.shieldMost, 0);
                assertEquals(is.shieldMost, is.shield, 0);
            }
            assertTrue(waited);
        }
        assertEquals(0, RunMarket.takes(0) + RunMarket.shields(0) + RunMarket.bites(0));
        // The four found nowhere else: each won on a rung of its own, and each one she may begin a land with.
        assertEquals(new java.util.HashSet<>(java.util.Arrays.asList(RunArms.KATANA, RunArms.LANCE, RunArms.CANNON, RunArms.WHIP)), weapons);
        for (int thing : weapons) assertTrue(java.util.Arrays.stream(RunArms.MAINS).anyMatch(one -> one == thing));
        assertEquals(RunArms.KATANA, RunMarket.prizeOf(2));
        // She begins a rung at the door of the place where whoever waits is: and that is where a life lost puts her back.
        RunLevels.Level level = RunLevels.level(RunMarket.LANDS[0]);
        PlusRun run = new PlusRun(level);
        float at = level.arena - 3, down = RunLevels.top(level, level.arena - 3) - PlusRun.TALL;
        run.begins(at, down);
        hold(run, .3f, false, false, false);
        assertEquals(at, run.x(), .2f);
        assertEquals(0, run.hurts());
        // Harder, by choice: so many more blows to every creature and halves of a heart to every hurt, in any land.
        for (int step = 0; step <= RunMarket.STEPS; step++) {
            PlusRun.Perks perks = new PlusRun.Perks();
            perks.harder = step;
            PlusRun hard = new PlusRun(RunLevels.level(40), perks);
            assertEquals(RunLevels.toughOf(40) + step, hard.tough());
            assertEquals(RunLevels.biteOf(40) + step, hard.bite());
            assertFalse(RunMarket.stepWords(step).isEmpty());
        }
        assertEquals(RunMarket.WARES, RunMarket.WARE_NAMES.length);
        assertEquals(RunMarket.WARES, RunMarket.WARE_COSTS.length);
    }

    // The hunts of the guild hall

    @Test public void whatSheDealsWithIsWrittenDownWithTheWayOfItForTheHunts() {
        // A pen thrown: the kind of what it dealt with, and the pen's way.
        RunLevels.Level level = armed(PlusRun.PEN);
        level.foes.add(new int[]{RunLevels.TIMES, 12, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        assertTrue(run.fells().isEmpty());
        hold(run, .5f, false, true, false);
        run.step(STEP, false, false, false, true);
        hold(run, 1, false, false, false);
        assertEquals(1, run.fells().size());
        org.junit.Assert.assertArrayEquals(new int[]{RunLevels.TIMES, PlusRun.PEN + 1}, run.fells().get(0));
        // Landed on: the way of landing.
        run = flown(RunLevels.MINUS, 20, 0);
        assertTrue(stomp(run, run.foes().get(0)));
        assertEquals(1, run.fells().size());
        org.junit.Assert.assertArrayEquals(new int[]{RunLevels.MINUS, RunHunts.LANDING}, run.fells().get(0));
        // A sword swung: the sword's way.
        level = armed(PlusRun.SWORD);
        level.foes.add(new int[]{RunLevels.MINUS, 30, FLOOR - 1});
        run = new PlusRun(level);
        hold(run, .5f, false, true, false);
        assertEquals(PlusRun.SWORD, run.weapon());
        PlusRun.Foe foe = run.foes().get(0);
        run.place(foe.x - PlusRun.WIDE - .3f, GROUND);
        strike(run);
        hold(run, .3f, false, false, false);
        assertTrue(foe.flat);
        org.junit.Assert.assertArrayEquals(new int[]{RunLevels.MINUS, PlusRun.SWORD + 1}, run.fells().get(0));
        // And each counts for the hunts it is of: by its kind, or by the way of it; a paper target for none.
        int minuses = RunHunts.of(RunHunts.OF_KIND, 0, 16, 0, false), moths = RunHunts.of(RunHunts.OF_KIND, 7, 30, 4, false);
        int landing = RunHunts.of(RunHunts.BY_WAY, RunHunts.LANDING, 12, 0, false), books = RunHunts.of(RunHunts.BY_WAY, PlusRun.BOOK + 1, 9, 2, true);
        assertTrue(RunHunts.counts(minuses, RunLevels.MINUS, PlusRun.SWORD + 1));
        assertTrue(RunHunts.counts(minuses, RunLevels.THROWER, -1));
        assertFalse(RunHunts.counts(minuses, RunLevels.TIMES, RunHunts.LANDING));
        assertTrue(RunHunts.counts(moths, RunLevels.DRIFTER, -1));
        assertFalse(RunHunts.counts(moths, RunLevels.SWOOPER, -1));
        assertTrue(RunHunts.counts(landing, RunLevels.SLIME, RunHunts.LANDING));
        assertFalse(RunHunts.counts(landing, RunLevels.SLIME, PlusRun.PEN + 1));
        assertFalse(RunHunts.counts(landing, RunLevels.TARGET, RunHunts.LANDING));
        assertFalse(RunHunts.counts(landing, RunLevels.ZERO, RunHunts.LANDING));
        assertTrue(RunHunts.counts(books, RunLevels.MINUS, PlusRun.BOOK + 1));
        assertFalse(RunHunts.counts(books, RunLevels.MINUS, -1));
        int blot = RunHunts.of(RunHunts.ROVER, RunLevels.BLOT - RunLevels.WYRM, 1, 0, false), cellar = RunHunts.of(RunHunts.CELLAR, PlusRun.RANK_A, 1, 0, false);
        assertTrue(RunHunts.countsRover(blot, RunLevels.BLOT));
        assertFalse(RunHunts.countsRover(blot, RunLevels.GECKO));
        assertFalse(RunHunts.counts(blot, RunLevels.BLOT, -1));
        assertTrue(RunHunts.countsCellar(cellar, PlusRun.RANK_A) && RunHunts.countsCellar(cellar, PlusRun.RANK_S));
        assertFalse(RunHunts.countsCellar(cellar, PlusRun.RANK_B));
        assertFalse(RunHunts.countsCellar(RunHunts.of(RunHunts.CELLAR, PlusRun.RANK_S, 1, 3, false), PlusRun.RANK_A));
        // What each asks, in words.
        assertEquals("Beat 30 comma moths", RunHunts.words(moths));
        assertEquals("Beat Blot", RunHunts.words(blot));
        assertEquals("Clear a cellar at rank A or better", RunHunts.words(cellar));
        assertEquals("Beat 9 creatures with a thrown book", RunHunts.words(books));
        assertEquals("Beat 12 creatures by landing on them", RunHunts.words(landing));
        assertEquals("Comma moths", RunHunts.brief(moths));
        assertTrue(RunHunts.pays(books).contains("a treat") && !RunHunts.pays(moths).contains("treat"));
    }

    @Test public void theBoardHasThreeHuntsNoTwoTheSameThatSheCanDoAndThatAskAndPayMoreTheMoreSheHasHandedIn() {
        // The kinds a hunt may be for are first met where the board takes them to be; and the three met in the middle of a land, and the cellars.
        int[] first = new int[RunHunts.GROUPS.length], rovers = new int[3];
        int cellars = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            boolean cellar = false;
            for (int[] room : level.rooms) cellar |= room[0] == RunLevels.ARENA;
            if (cellar && cellars == 0) cellars = number;
            for (int group = 0; group < first.length; group++) {
                int hunt = RunHunts.of(RunHunts.OF_KIND, group, 1, 0, false);
                boolean met = false;
                for (int[] foe : level.foes) met |= foe[1] < level.main && RunHunts.counts(hunt, foe[0] % RunLevels.GIANT, -1);
                if (met && first[group] == 0) first[group] = number;
                if (met) assertTrue(RunHunts.here(hunt, level));
            }
            for (int rover = 0; rover < 3; rover++) {
                int hunt = RunHunts.of(RunHunts.ROVER, rover, 1, 0, false);
                if (RunHunts.here(hunt, level) && rovers[rover] == 0) rovers[rover] = number;
            }
            assertEquals(cellar, RunHunts.here(RunHunts.of(RunHunts.CELLAR, PlusRun.RANK_A, 1, 0, false), level));
            assertTrue(RunHunts.here(RunHunts.of(RunHunts.BY_WAY, RunHunts.LANDING, 12, 0, false), level));
        }
        org.junit.Assert.assertArrayEquals(RunHunts.GROUP_FROM, first);
        org.junit.Assert.assertArrayEquals(RunHunts.ROVER_FROM, rovers);
        assertEquals(RunHunts.CELLAR_FROM, cellars);

        // The board, kept full as hunts are handed in: whatever the furthest land open to her and whatever she has to fight with.
        int all = (1 << RunArms.COUNT) - 1, tablet = 1 << PlusRun.TABLET;
        for (int open : new int[]{1, 5, 9, 15, 22, 30, 41, 80}) {
            for (int owned : new int[]{tablet, tablet | 1 << PlusRun.KNIFE, all}) {
                int[] on = new int[RunHunts.SLOTS];
                boolean[] sorts = new boolean[4];
                for (int serial = 0; serial < 90; serial++) {
                    int slot = serial < RunHunts.SLOTS ? serial : (serial * 7 + 1) % RunHunts.SLOTS, done = Math.max(0, serial - RunHunts.SLOTS);
                    on[slot] = 0;
                    int hunt = RunHunts.offer(serial, open, owned, done, on);
                    String where = "open " + open + " owned " + owned + " serial " + serial;
                    assertTrue(where, hunt != 0);
                    int sort = RunHunts.sort(hunt), what = RunHunts.what(hunt), many = RunHunts.many(hunt);
                    sorts[sort] = true;
                    for (int other : on) assertFalse(where, other != 0 && RunHunts.sort(other) == sort && RunHunts.what(other) == what);
                    on[slot] = hunt;
                    assertEquals(where, RunHunts.tierOf(done), RunHunts.tier(hunt));
                    assertEquals(where, serial % 3 == 2, RunHunts.treat(hunt));
                    assertTrue(where, many >= 1 && RunHunts.pluses(hunt) >= 30 && RunHunts.pluses(hunt) % 5 == 0);
                    if (sort == RunHunts.OF_KIND) assertTrue(where, RunHunts.GROUP_FROM[what] <= open && many >= 3);
                    else if (sort == RunHunts.ROVER) assertTrue(where, RunHunts.ROVER_FROM[what] <= open && many == 1);
                    else if (sort == RunHunts.CELLAR) assertTrue(where, open >= RunHunts.CELLAR_FROM && many == 1 && what == (RunHunts.tier(hunt) >= 3 ? PlusRun.RANK_S : PlusRun.RANK_A));
                    else assertTrue(where, what == RunHunts.LANDING || what == RunHunts.TREES && open >= RunLevels.TREES_FROM || what != RunHunts.TREES && (owned >> (what == RunHunts.KNUCKLE_WAY ? RunArms.KNUCKLES : what - 1) & 1) != 0);
                    assertTrue(where, RunHunts.materials(hunt)[0] >= 1);
                    assertFalse(where, RunHunts.words(hunt).isEmpty() || RunHunts.brief(hunt).isEmpty());
                }
                // Every sort there can be comes up.
                assertTrue(sorts[RunHunts.OF_KIND] && sorts[RunHunts.BY_WAY]);
                assertEquals(open >= RunHunts.CELLAR_FROM, sorts[RunHunts.CELLAR]);
                assertEquals(open >= 15, sorts[RunHunts.ROVER]);
            }
        }
        // The same hunt, later: it asks more, and pays more.
        for (int serial = 0; serial < 12; serial++) {
            int early = RunHunts.offer(serial, 80, all, 0, new int[RunHunts.SLOTS]), late = RunHunts.offer(serial, 80, all, 40, new int[RunHunts.SLOTS]);
            assertEquals(0, RunHunts.tier(early));
            assertEquals(RunHunts.TIERS - 1, RunHunts.tier(late));
            assertEquals(RunHunts.sort(early), RunHunts.sort(late));
            assertTrue(RunHunts.pluses(late) > RunHunts.pluses(early));
            assertTrue(RunHunts.many(late) >= RunHunts.many(early));
            if (RunHunts.sort(early) == RunHunts.OF_KIND || RunHunts.sort(early) == RunHunts.BY_WAY) assertEquals(RunHunts.many(early) * 3, RunHunts.many(late));
            int[] less = RunHunts.materials(early), more = RunHunts.materials(late);
            assertTrue(more[0] + more[1] + more[2] > less[0] + less[1] + less[2]);
        }
        assertEquals(30, RunHunts.many(RunHunts.offer(0, 21, tablet, 40, new int[]{RunHunts.of(RunHunts.OF_KIND, 0, 1, 0, false), RunHunts.of(RunHunts.OF_KIND, 1, 1, 0, false),
                RunHunts.of(RunHunts.OF_KIND, 2, 1, 0, false)})));

        // Her rank in the guild, and what comes with so many handed in.
        assertEquals(0, RunHunts.rank(0));
        for (int rank = 1; rank <= RunHunts.RANK_AT.length; rank++) {
            assertEquals(rank, RunHunts.rank(RunHunts.RANK_AT[rank - 1]));
            assertEquals(rank - 1, RunHunts.rank(RunHunts.RANK_AT[rank - 1] - 1));
        }
        assertEquals(RunHunts.RANK_AT.length, RunHunts.RANK_NAMES.length);
        int pets = 0, places = 0, strays = 0;
        for (int done = 1; done <= 60; done++) {
            int brings = RunHunts.brings(done);
            if (brings == RunHunts.A_PET) pets++;
            else if (brings == RunHunts.A_PLACE) places++;
            else if (brings == RunHunts.A_STRAY) strays++;
            // What is said to come next is what does.
            int next = RunHunts.nextAt(done - 1);
            assertTrue(next >= done);
            assertTrue(RunHunts.brings(next) != RunHunts.NOTHING || RunHunts.rank(next) > RunHunts.rank(next - 1));
            for (int between = done; between < next; between++) assertTrue(RunHunts.brings(between) == RunHunts.NOTHING && RunHunts.rank(between) == RunHunts.rank(between - 1));
            assertFalse(RunHunts.nextIs(done - 1).isEmpty());
        }
        assertEquals(10, pets);
        assertEquals(2, places);
        assertEquals(2, strays);
        assertEquals(1, RunHunts.nextAt(0));
        assertEquals("the Paper badge", RunHunts.nextIs(0));
        assertEquals("a pet of her own", RunHunts.nextIs(1));
        assertEquals(3, RunHunts.nextAt(1));
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

    /** Brings her back to whoever asked something of her, for a step. */
    private static void back(PlusRun run) {
        run.place(run.questColumn() + (1 - PlusRun.WIDE) / 2, run.questRow() + 1 - PlusRun.TALL);
        run.step(STEP, false, false, false);
    }

    /** What was done is handed in to whoever asked it, by going back to her: once, and paid for once, however often she is gone back to. */
    private static void handedIn(PlusRun run, int bonus) {
        assertEquals(PlusRun.DONE, run.quest());
        assertEquals(1, run.questsDone());
        assertEquals(0, run.questsPaid());
        assertEquals(bonus, run.bonus());
        // Anywhere else, nothing comes of it.
        hold(run, .5f, false, false, false);
        assertEquals(PlusRun.DONE, run.quest());
        PlusRun left = run.copy();
        back(run);
        assertEquals(PlusRun.PAID, run.quest());
        assertEquals(1, run.questsPaid());
        assertEquals(bonus + 15, run.bonus());
        for (int again = 0; again < 3; again++) {
            run.place(run.questColumn() + 12, run.questRow() + 1 - PlusRun.TALL);
            run.step(STEP, false, false, false);
            back(run);
        }
        assertEquals(1, run.questsPaid());
        assertEquals(1, run.questsDone());
        assertEquals(bonus + 15, run.bonus());
        assertEquals(0, run.reminders());
        // Not gone back to, it is handed in when the land is over: once.
        assertTrue(left.settle());
        assertEquals(PlusRun.PAID, left.quest());
        assertEquals(1, left.questsPaid());
        assertEquals(bonus + 15, left.bonus());
        assertFalse(left.settle());
        assertEquals(1, left.questsPaid());
    }

    @Test public void whatIsAskedOfHerIsDoneAndHandedInOnceOrFailsAndIsNeverNeeded() {
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
        handedIn(run, bonus);
        // Found before they were asked for, they still count: asked, it is done at once, and handed in where she stands, which is with Boksun.
        PlusRun early = new PlusRun(run.level());
        for (int found = 1; found <= 3; found++) at(early, -1, PlusRun.CAKE);
        assertEquals(0, early.quest());
        at(early, RunLevels.BOKSUN, -1);
        assertEquals(PlusRun.PAID, early.quest());
        assertEquals(1, early.questsDone());
        assertEquals(1, early.questsPaid());
        hold(early, .5f, false, false, false);
        assertEquals(1, early.questsPaid());
        // Coming back to her before it is done, she says again what she asked: each time she is come back to from somewhere else.
        PlusRun reminded = asked(RunLevels.SHAPED_FROM, RunLevels.CAKES);
        hold(reminded, .5f, false, false, false);
        assertEquals(0, reminded.reminders());
        reminded.place(reminded.questColumn() + 12, reminded.questRow() + 1 - PlusRun.TALL);
        reminded.step(STEP, false, false, false);
        back(reminded);
        assertEquals(1, reminded.reminders());
        hold(reminded, .5f, false, false, false);
        assertEquals(1, reminded.reminders());
        assertEquals(PlusRun.ASKED, reminded.quest());

        // Inju's cellar: cleared, it is done.
        run = asked(RunLevels.SHAPED_FROM, RunLevels.CELLAR);
        int cellar = -1;
        for (int index = 0; index < run.level().rooms.size(); index++) if (run.level().rooms.get(index)[0] == RunLevels.ARENA) cellar = index;
        int[] one = run.level().rooms.get(cellar);
        run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        for (int wave = 1; wave <= PlusRun.WAVES; wave++) {
            assertEquals(PlusRun.ASKED, run.quest());
            arrived(run);
            dealt(run);
            if (wave < PlusRun.WAVES) hold(run, PlusRun.BREATH + .1f, false, false, false);
        }
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.DONE, run.quest());
        // Out of the cellar again, and back to Inju.
        run.place(one[6] + (1 - PlusRun.WIDE) / 2, one[11] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        run.step(STEP, false, false, false);
        handedIn(run, run.bonus());

        // Galpi's hidden room: gone into, it is found.
        run = asked(RunLevels.SHAPED_FROM, RunLevels.ROOM);
        int hidden = -1;
        for (int index = 0; index < run.level().rooms.size(); index++) if (run.level().rooms.get(index)[7] != 0 && run.level().rooms.get(index)[0] != RunLevels.HIDEOUT) hidden = index;
        one = run.level().rooms.get(hidden);
        run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertTrue(run.enter());
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.ASKED, run.quest());
        assertTrue(run.enter());
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.DONE, run.quest());
        assertTrue(run.enter());
        run.step(STEP, false, false, false);
        handedIn(run, run.bonus());

        // Somi's letter: brought to Yeseo by the gate, it is done; through a hurt on the way, it is not to be handed over.
        run = asked(RunLevels.SHAPED_FROM, RunLevels.LETTER);
        PlusRun creased = run.copy();
        int before = run.bonus();
        at(run, -1, PlusRun.DROPOFF);
        // Yeseo, whom it was for, is there to thank her: it is handed in as it is done, once.
        assertEquals(PlusRun.PAID, run.quest());
        assertEquals(1, run.questsDone());
        assertEquals(1, run.questsPaid());
        assertEquals(before + 15, run.bonus());
        at(run, -1, PlusRun.DROPOFF);
        hold(run, .3f, false, false, false);
        assertEquals(1, run.questsPaid());
        assertFalse(run.settle());
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
        before = run.bonus();
        run.step(STEP, false, false, false);
        run.step(STEP, false, false, false);
        // Ttosun has run after her: won, it is handed in at the flag, once.
        assertEquals(PlusRun.PAID, run.quest());
        assertEquals(1, run.questsDone());
        assertEquals(1, run.questsPaid());
        assertEquals(before + 15, run.bonus());
        hold(run, .3f, false, false, false);
        assertEquals(1, run.questsPaid());
        float count = slow.questLeft();
        for (int step = 0; step < (count + 1) * 60 && slow.quest() == PlusRun.ASKED; step++) slow.step(STEP, false, false, false);
        assertEquals(PlusRun.FAILED, slow.quest());

        // Something is asked in every land with shape but those where someone waits; and whoever asks it is there, in its first village.
        int[] kinds = new int[RunLevels.ROOM + 1];
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level here = RunLevels.level(number);
            assertEquals("land " + number, number >= RunLevels.QUESTS_FROM && RunLevels.bossOf(number) < 0, here.quest != 0);
            if (here.quest == 0) continue;
            kinds[here.quest]++;
            int asker = -1;
            for (int[] item : here.items) if (item[3] >= RunLevels.SOMI && item[0] != PlusRun.STALL && item[0] != PlusRun.DROPOFF && asker < 0) asker = item[3];
            assertEquals("land " + number, RunLevels.giverOf(here.quest), asker);
        }
        for (int kind = RunLevels.CAKES; kind <= RunLevels.ROOM; kind++) assertTrue("never asked: " + kind, kinds[kind] >= 3);
    }

    /** Her with some colours hers to keep, on flat ground, able to wear so many of them at once. */
    private static PlusRun owning(int together, int... colours) {
        PlusRun.Perks perks = new PlusRun.Perks();
        for (int colour : colours) perks.colours |= 1 << colour - 1;
        perks.together = together;
        PlusRun run = new PlusRun(flat(60), perks);
        hold(run, .2f, false, false, false);
        return run;
    }

    @Test public void coloursAreWornTogetherEachWithItsOwnTimeAndItsOwnWait() {
        PlusRun run = owning(2, PlusRun.YELLOW, PlusRun.GREEN, PlusRun.RED);
        // Not one she has not won.
        assertFalse(run.call(PlusRun.BLUE));
        assertEquals(0, run.form());
        assertEquals(0, run.mix());
        // One she has: it is in her hair for nine seconds.
        assertTrue(run.call(PlusRun.YELLOW));
        assertEquals(PlusRun.YELLOW, run.form());
        assertEquals(9, run.on(PlusRun.YELLOW), .01f);
        assertEquals(9, run.lasted(PlusRun.YELLOW), .01f);
        assertEquals(1, run.called());
        // The same one again, not while she wears it.
        assertFalse(run.call(PlusRun.YELLOW));
        // Another, four seconds on: it is added to the first, which goes on as it was. Each keeps its power, and each has its own time.
        hold(run, 4, false, false, false);
        assertTrue(run.call(PlusRun.GREEN));
        assertEquals(1 << PlusRun.YELLOW - 1 | 1 << PlusRun.GREEN - 1, run.mix());
        assertTrue(run.has(PlusRun.YELLOW) && run.has(PlusRun.GREEN));
        assertFalse(run.has(PlusRun.RED));
        assertEquals(5, run.on(PlusRun.YELLOW), .05f);
        assertEquals(9, run.on(PlusRun.GREEN), .05f);
        assertEquals(PlusRun.GREEN, run.form());
        assertEquals(2, run.called());
        // Yellow's speed and green's flight, both: she runs much faster than her own run, and rises while the jump is held.
        float from = run.x();
        hold(run, 1, false, true, false);
        assertTrue("ran " + (run.x() - from), run.x() - from > 8);
        float height = run.y();
        hold(run, 1, false, false, true);
        assertTrue(run.y() < height - 3);
        // A third is one more than she can hold of her own.
        assertFalse(run.callable(PlusRun.RED));
        assertFalse(run.call(PlusRun.RED));
        // Yellow goes when its nine seconds are up, and green is still on; yellow's wait begins then, from the whole of it, and green has none.
        hold(run, 3.2f, false, false, false);
        assertFalse(run.has(PlusRun.YELLOW));
        assertTrue(run.has(PlusRun.GREEN));
        assertEquals(1, run.faded());
        assertTrue("wait " + run.untilCall(PlusRun.YELLOW), run.untilCall(PlusRun.YELLOW) > 9.7f && run.untilCall(PlusRun.YELLOW) <= 10);
        assertEquals(0, run.untilCall(PlusRun.GREEN), 0);
        assertTrue(run.charge(PlusRun.YELLOW) < .05f);
        assertEquals(1, run.charge(PlusRun.RED), 0);
        // While yellow waits it cannot be called on; red, which has not been worn, can, now that there is room for it.
        assertFalse(run.callable(PlusRun.YELLOW));
        assertFalse(run.call(PlusRun.YELLOW));
        assertTrue(run.call(PlusRun.RED));
        assertTrue(run.has(PlusRun.RED) && run.has(PlusRun.GREEN));
        // Yellow's wait runs on while the others are worn, and when it is over yellow can be called on again, if there is room.
        hold(run, 4, false, false, false);
        assertFalse(run.has(PlusRun.GREEN));
        assertTrue(run.untilCall(PlusRun.GREEN) > 9);
        assertTrue(run.untilCall(PlusRun.YELLOW) > 5 && run.untilCall(PlusRun.YELLOW) < 7);
        hold(run, 6.5f, false, false, false);
        assertEquals(0, run.untilCall(PlusRun.YELLOW), 0);
        assertTrue(run.call(PlusRun.YELLOW));
        // Several at once, in one call: as many as she can hold, in their order.
        PlusRun three = owning(3, PlusRun.RED, PlusRun.BLUE, PlusRun.BROWN, PlusRun.GREEN);
        assertEquals(1 | 2 | 4, three.callAll(1 | 2 | 4 | 8));
        assertEquals(1 | 2 | 4, three.mix());
        assertEquals(3, three.called());
        assertEquals(3, three.together());
        // One of them let go of: it is gone at once, its wait begins, and there is room for another.
        assertTrue(three.drop(PlusRun.BLUE));
        assertFalse(three.has(PlusRun.BLUE));
        assertTrue(three.untilCall(PlusRun.BLUE) > 9.9f);
        assertFalse(three.drop(PlusRun.BLUE));
        assertTrue(three.call(PlusRun.GREEN));
        // The wait is what she has been given to keep makes it: a shorter one is shorter.
        PlusRun.Perks quick = new PlusRun.Perks();
        quick.colours = 1;
        quick.recharge = 7;
        PlusRun sooner = new PlusRun(flat(60), quick);
        assertTrue(sooner.call(PlusRun.RED));
        hold(sooner, 9.1f, false, false, false);
        assertTrue("wait " + sooner.untilCall(PlusRun.RED), sooner.untilCall(PlusRun.RED) > 6.7f && sooner.untilCall(PlusRun.RED) <= 7);
        hold(sooner, 7, false, false, false);
        assertTrue(sooner.call(PlusRun.RED));
        // With Zero's ring every colour lasts half as long again, called on or from a ribbon.
        PlusRun.Perks ring = new PlusRun.Perks();
        ring.colours = 1 << PlusRun.YELLOW - 1;
        ring.formLonger = 1.5f;
        RunLevels.Level level = flat(60);
        level.items.add(new int[]{PlusRun.FORM + PlusRun.GREEN, 6, FLOOR - 1, 0});
        PlusRun ringed = new PlusRun(level, ring);
        assertTrue(ringed.call(PlusRun.YELLOW));
        assertEquals(13.5f, ringed.on(PlusRun.YELLOW), .01f);
        hold(ringed, .9f, false, true, false);
        assertTrue(ringed.on(PlusRun.GREEN) > 20);
    }

    @Test public void aMixIsChosenAllAtOnceAndWornAgainByOneCall() {
        // Several picked and worn together, as the picker does it: all at once, for as many as she can hold.
        PlusRun run = owning(3, PlusRun.RED, PlusRun.BLUE, PlusRun.GREEN, PlusRun.YELLOW, PlusRun.PURPLE);
        int mix = 1 | 2 | 8;
        assertEquals(mix, run.wear(mix));
        assertEquals(mix, run.mix());
        assertEquals(3, run.called());
        // The same mix asked for again while it is on changes nothing.
        assertEquals(0, run.wear(mix));
        assertEquals(mix, run.mix());
        // Another mix asked for: what is on and not in it is let go of, and what is in it and ready is put on.
        int other = 1 | 16;
        assertEquals(16, run.wear(other));
        assertEquals(other, run.mix());
        assertTrue(run.untilCall(PlusRun.BLUE) > 9 && run.untilCall(PlusRun.GREEN) > 9);
        // The remembered mix, called on again by the one call later: every colour of it that is ready is put on, and one still waiting is
        // passed over without stopping the rest.
        hold(run, 9.2f, false, false, false);
        assertEquals(0, run.mix());
        assertTrue(run.untilCall(PlusRun.RED) > 0);
        hold(run, 4, false, false, false);
        assertEquals(2 | 8, run.ready() & mix);
        assertEquals(2 | 8, run.wear(mix));
        assertEquals(2 | 8, run.mix());
        assertFalse(run.has(PlusRun.RED));
        // The limit holds: of five asked for she wears the three she can.
        PlusRun many = owning(3, PlusRun.RED, PlusRun.BLUE, PlusRun.GREEN, PlusRun.YELLOW, PlusRun.PURPLE);
        assertEquals(3, Integer.bitCount(many.wear(1 | 2 | 8 | 16 | 32)));
        assertEquals(3, Integer.bitCount(many.mix()));
        // A ribbon's colour is not hers to let go of by choosing another mix.
        many.lend(PlusRun.BROWN);
        many.wear(1);
        assertTrue(many.has(PlusRun.BROWN));
        assertTrue(many.has(PlusRun.RED));
        assertFalse(many.has(PlusRun.BLUE));

        // The quick choices. What each named mix is made of, and what some colours together are called.
        assertEquals(1 | 2, PlusRun.mixOf(PlusRun.STEAMS));
        assertEquals(1 | 16, PlusRun.mixOf(PlusRun.COMET));
        assertEquals(2 | 8, PlusRun.mixOf(PlusRun.TIDE));
        assertEquals(4 | 32, PlusRun.mixOf(PlusRun.MIRROR));
        assertEquals(8 | 16, PlusRun.mixOf(PlusRun.GALE));
        assertEquals(1 | 4, PlusRun.mixOf(PlusRun.EMBER));
        assertEquals(63, PlusRun.mixOf(PlusRun.WHITEN));
        assertEquals(0, PlusRun.mixOf(PlusRun.PRISM));
        for (int which = PlusRun.STEAMS; which <= PlusRun.EMBER; which++) assertEquals(which, PlusRun.naming(PlusRun.mixOf(which)));
        assertEquals(PlusRun.WHITEN, PlusRun.naming(127));
        assertEquals(PlusRun.PRISM, PlusRun.naming(1 | 2 | 4));
        assertEquals(PlusRun.WHITEN, PlusRun.naming(63));
        assertEquals(PlusRun.PRISM, PlusRun.naming(31));
        assertEquals(-1, PlusRun.naming(1));
        assertEquals(-1, PlusRun.naming(2 | 4));
        assertEquals(-1, PlusRun.naming(0));
        // Worn, each is what the engine says it is.
        for (int which = PlusRun.STEAMS; which <= PlusRun.EMBER; which++) {
            PlusRun.Perks all = new PlusRun.Perks();
            all.colours = 127;
            PlusRun named = new PlusRun(flat(60), all);
            assertEquals(PlusRun.mixOf(which), named.wear(PlusRun.mixOf(which)));
            assertEquals(1 << which, named.mixed());
        }
        // "All I can wear": as many as the limit allows, those that are ready first, each in its order.
        assertEquals(1 | 2, PlusRun.most(127, 127, 2));
        assertEquals(1 | 2 | 4, PlusRun.most(127, 127, 3));
        assertEquals(63, PlusRun.most(127, 127, 7));
        assertEquals(4 | 8, PlusRun.most(127, 4 | 8 | 16, 2));
        assertEquals(1 | 8 | 32, PlusRun.most(1 | 2 | 8 | 32, 8 | 32, 3));
        assertEquals(2 | 16, PlusRun.most(2 | 16, 0, 5));
        assertEquals(0, PlusRun.most(0, 0, 3));
    }

    /** A long flat land with one of the three met in the middle of lands put in it, and the notes about her for as good as ever. */
    private static PlusRun roving(int rover, int column) {
        RunLevels.Level level = flat(220);
        level.gate = 200;
        level.foes.add(new int[]{rover, column, FLOOR - 1});
        level.items.add(new int[]{PlusRun.NOTES, 3, FLOOR - 1, 0});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.lasting = 1000;
        PlusRun run = new PlusRun(level, perks);
        hold(run, .5f, false, true, false);
        assertTrue(run.notes() > 100);
        return run;
    }

    @Test public void theThreeMetInTheMiddleOfLandsAreRousedDoWhatTheyDoAndCanBeBeatenWhenTheyAreSpent() {
        // Where they are met: one in each of thirty lands, from the middle of the first volume on, more of them in the second; none where
        // someone waits at the end; each on the ground of the land itself, past its middle and short of its gate.
        int[] met = new int[RunLevels.GECKO + 1];
        int first = 0, second = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int rover = RunLevels.roverOf(number), there = 0;
            for (int[] foe : level.foes) {
                if (!RunLevels.rover(foe[0])) continue;
                there++;
                assertEquals("land " + number, rover, foe[0]);
                assertTrue("land " + number + " at " + foe[1], foe[1] > level.gate * 2 / 5 && foe[1] < level.gate - 8);
                assertEquals("land " + number, RunLevels.top(level, foe[1]) - 1, foe[2]);
            }
            assertEquals("land " + number, rover >= 0 ? 1 : 0, there);
            if (rover < 0) continue;
            assertTrue("land " + number, number >= 15 && RunLevels.bossOf(number) < 0);
            met[rover]++;
            if (number <= RunLevels.COUNT) first++;
            else second++;
        }
        assertEquals(13, met[RunLevels.WYRM]);
        assertEquals(13, met[RunLevels.BLOT]);
        assertEquals(13, met[RunLevels.GECKO]);
        assertTrue(first >= 5 && second > first * 3);
        for (int rover = RunLevels.WYRM; rover <= RunLevels.GECKO; rover++) {
            assertTrue(RunLevels.rover(rover) && RunLevels.boss(rover));
            assertTrue(PlusRun.health(rover) >= 4);
        }

        // Each of them: not there until she comes near; then shielded but for the moments it is spent; and beaten in those.
        for (int rover = RunLevels.WYRM; rover <= RunLevels.GECKO; rover++) {
            PlusRun run = roving(rover, 60);
            PlusRun.Foe foe = null;
            for (PlusRun.Foe each : run.foes()) if (each.kind == rover) foe = each;
            String who = "rover " + rover;
            assertTrue(who, foe.away);
            assertEquals(who, PlusRun.AWAY, run.guard(foe));
            assertEquals(who, 0, run.roused());
            assertFalse(who, run.standing());
            int spent = 0, shielded = 0, thrown = run.thrown(), quakes = run.quakes();
            boolean hung = false;
            float on = 30;
            for (int step = 0; step < 60 * 240 && !foe.flat; step++) {
                // She keeps to where it is: for the wyrm, on through the land while it is away, so that it comes again.
                if (foe.away) on = Math.min(180, on + 2.2f * STEP);
                float stand = rover == RunLevels.WYRM ? on : 56;
                int guard = run.guard(foe);
                if (guard == PlusRun.SHIELDED) shielded++;
                if (rover == RunLevels.GECKO && !foe.away && foe.y < foe.ground - 5) hung = true;
                boolean shown = rover == RunLevels.WYRM ? run.roused() >= 2 : rover == RunLevels.BLOT ? run.quakes() > quakes && run.thrown() > thrown : hung;
                if (shown && !foe.away && pressable(run, foe)) {
                    int blows = run.blows(), cracks = run.cracks();
                    touch(run, foe);
                    if (guard == PlusRun.BARE) {
                        assertEquals(who, blows + 1, run.blows());
                        spent++;
                    } else {
                        // On its shield: a piece off it, nothing to it.
                        assertEquals(who, blows, run.blows());
                        assertEquals(who, cracks + 1, run.cracks());
                    }
                } else {
                    // Out of its way: in the air over where she keeps.
                    run.place(stand, GROUND - 9);
                    run.step(STEP, false, false, false);
                }
            }
            assertTrue(who + " still up after " + run.blows() + " blows, roused " + run.roused(), foe.flat);
            assertEquals(who, PlusRun.health(rover), run.blows());
            assertEquals(who, PlusRun.health(rover), spent);
            assertTrue(who + " shielded for " + shielded, shielded >= PlusRun.strength(rover));
            assertTrue(who, run.roused() >= 1);
            assertEquals(who, 0, run.hurts());
            if (rover == RunLevels.WYRM) {
                // It strafed: things fell from it. And it came more than once.
                assertTrue(who, run.thrown() > thrown + 3);
                assertTrue(who + " roused " + run.roused(), run.roused() >= 2);
            } else if (rover == RunLevels.BLOT) {
                // It leapt and shook the ground, and spat; and it came apart, a slime for every blow.
                assertTrue(who, run.quakes() > quakes);
                assertTrue(who, run.thrown() > thrown);
                assertEquals(who, PlusRun.health(rover), run.splits());
                int slimes = 0;
                for (PlusRun.Foe each : run.foes()) if (each.kind == RunLevels.SLIME && each.spawned) slimes++;
                assertTrue(who + " slimes " + slimes, slimes >= 2);
            } else {
                assertTrue(who, hung);
            }
        }

        // Without the notes, standing where it does what it does costs her; and a touch while it is shielded is turned aside and costs it nothing.
        PlusRun bare = roving(RunLevels.BLOT, 60);
        PlusRun.Foe blot = null;
        for (PlusRun.Foe each : bare.foes()) if (each.kind == RunLevels.BLOT) blot = each;
        for (int step = 0; step < 60 * 30 && bare.hurts() == 0; step++) {
            bare.bare();
            bare.place(56, GROUND);
            bare.step(STEP, false, false, false);
        }
        assertEquals(1, bare.hurts());
        assertEquals(PlusRun.health(RunLevels.BLOT), blot.left);

        // The wyrm not beaten is driven off: after its third coming it does not come again, and the land is hers to finish.
        PlusRun off = roving(RunLevels.WYRM, 60);
        PlusRun.Foe wyrm = null;
        for (PlusRun.Foe each : off.foes()) if (each.kind == RunLevels.WYRM) wyrm = each;
        float along = 30;
        for (int step = 0; step < 60 * 300 && off.state() == PlusRun.PLAYING; step++) {
            // (She goes on only while it is away, and keeps out of its way while it is there.)
            if (wyrm.away) along = Math.min(199.5f, along + 1.6f * STEP);
            off.place(wyrm.away ? along : along - 3, GROUND - 9);
            off.step(STEP, false, false, false);
        }
        assertEquals(3, off.roused());
        assertTrue(wyrm.away && !wyrm.flat);
    }

    @Test public void whoeverSheFightsShutsOffAColourAndASlimeDrinksFireAndAFullStopGroundsHer() {
        // A third of what one of those who wait can take gone, she changes, and as she does she shuts off the colour that has longest to go.
        PlusRun run = shutIn(RunLevels.GUARD_LAND, true);
        run.lend(PlusRun.YELLOW);
        hold(run, 3, false, false, false);
        run.lend(PlusRun.GREEN);
        PlusRun.Foe foe = waiting(run);
        int shuts = run.shuts();
        for (int step = 0; step < 60 * 300 && run.phases() == 0; step++) {
            if (run.on(PlusRun.GREEN) < 5) run.lend(PlusRun.GREEN);
            if (run.on(PlusRun.YELLOW) < 2) run.lend(PlusRun.YELLOW);
            if (pressable(run, foe)) touch(run, foe);
            else park(run);
        }
        assertEquals(1, run.phases());
        assertEquals(shuts + 1, run.shuts());
        assertEquals(PlusRun.GREEN, run.lastShut());
        assertFalse(run.has(PlusRun.GREEN));
        assertTrue(run.has(PlusRun.YELLOW));
        assertTrue(run.untilCall(PlusRun.GREEN) > 9);

        // A slime drinks fire: it is not beaten by it, but grows, and takes two blows after; water still deals with one.
        for (int form : new int[]{PlusRun.RED, PlusRun.BLUE}) {
            RunLevels.Level level = flat(60);
            level.foes.add(new int[]{RunLevels.SLIME, 14, FLOOR - 1});
            PlusRun wet = new PlusRun(level);
            wet.lend(form);
            wet.place(8, GROUND);
            hold(wet, .1f, false, true, false);
            PlusRun.Foe slime = wet.foes().get(0);
            for (int step = 0; step < 240 && wet.flattened() == 0 && wet.drunk() == 0; step++) wet.step(STEP, false, false, false, step % 30 == 0);
            assertEquals("form " + form, form == PlusRun.RED ? 1 : 0, wet.drunk());
            assertEquals("form " + form, form == PlusRun.RED ? 0 : 1, wet.flattened());
            if (form == PlusRun.RED) {
                assertTrue(slime.size > 1.4f);
                assertEquals(2 * PlusRun.BLOW, slime.left);
                assertFalse(slime.flat);
            }
        }

        // A dragon's full stop that lands on her in the air is the end of her flight.
        RunLevels.Level sky = flat(60);
        sky.foes.add(new int[]{RunLevels.DRAGON, 20, FLOOR - 1});
        PlusRun flier = new PlusRun(sky);
        flier.lend(PlusRun.GREEN);
        PlusRun.Foe dragon = flier.foes().get(0);
        for (int step = 0; step < 60 * 30 && flier.grounded() == 0; step++) {
            if (flier.on(PlusRun.GREEN) < 3) flier.lend(PlusRun.GREEN);
            boolean struck = false;
            for (PlusRun.Shot shot : flier.shots()) {
                if (shot.kind != PlusRun.STOP || shot.hers) continue;
                flier.place(shot.x - PlusRun.WIDE / 2, shot.y - PlusRun.TALL / 2);
                struck = true;
            }
            if (!struck) flier.place(dragon.x - 4, GROUND - 3);
            flier.bare();
            flier.step(STEP, false, false, true);
        }
        assertEquals(1, flier.grounded());
        assertFalse(flier.has(PlusRun.GREEN));
        assertEquals(1, flier.hurts());
    }

    /** A flat land with one of those that fly over it, its feet so far over the ground. */
    private static PlusRun flown(int kind, int column, int high) {
        RunLevels.Level level = flat(80);
        level.foes.add(new int[]{kind, column, FLOOR - high - 1});
        return new PlusRun(level);
    }

    /** She comes down onto something from just over it. Returns whether it was beaten by that. */
    private static boolean stomp(PlusRun run, PlusRun.Foe foe) {
        int hurts = run.hurts();
        run.bare();
        run.place(foe.x + foe.wide() / 2 - PlusRun.WIDE / 2, foe.y - PlusRun.TALL - .4f);
        for (int step = 0; step < 30 && !foe.flat && run.hurts() == hurts && run.y() + PlusRun.TALL < foe.y + foe.tall(); step++) run.step(STEP, false, false, false);
        return foe.flat;
    }

    @Test public void thoseThatFlyAreInTheLandsFromTheTwentyFirstOnAndMoreOfThemLater() {
        int[] first = new int[RunLevels.RIME + 1];
        int before = 0, last = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int here = 0, lines = 0, was = -9;
            java.util.List<Integer> kinds = new java.util.ArrayList<>();
            int more = 0;
            for (int[] foe : level.foes) {
                if (!RunLevels.flier(foe[0])) continue;
                if (RunLevels.crowd(foe)) {
                    more++;
                    assertTrue("land " + number, number >= RunLevels.FLIERS_FROM && RunLevels.flierFrom(foe[0]) <= number);
                    continue;
                }
                here++;
                if (first[foe[0]] == 0) first[foe[0]] = number;
                // (Four tittles in a line are one of them.)
                if (foe[0] != RunLevels.MITE || foe[1] != was + 1) {
                    lines++;
                    kinds.add(foe[0]);
                }
                was = foe[0] == RunLevels.MITE ? foe[1] : -9;
                // In open air, over the land itself, short of its end; and at a height something reaches.
                assertEquals("land " + number, RunLevels.AIR, level.land[foe[2]][foe[1]]);
                assertTrue("land " + number, foe[1] >= 14 && foe[1] < level.gate - 8 && foe[2] >= 2);
                int under = RunLevels.top(level, foe[1]);
                if (under > 0) assertTrue("land " + number + " at " + foe[1], under - foe[2] - 1 <= 5 && under - foe[2] - 1 >= 2);
            }
            if (number < RunLevels.FLIERS_FROM) {
                assertEquals("land " + number, 0, here + more);
                continue;
            }
            assertTrue("land " + number + " more " + more, more <= RunLevels.fliersIn(number) * RunLevels.flocksOf(number) * 4);
            assertEquals(2 + (RunLevels.levelOf(number) - 1) / 5, RunLevels.flocksOf(number));
            // As many as the land is to have, give or take what there was no room for; and never fewer than two.
            assertTrue("land " + number + " has " + lines + " of " + RunLevels.fliersIn(number), lines >= Math.max(2, RunLevels.fliersIn(number) / 2) && lines <= RunLevels.fliersIn(number));
            // The land a kind is first met in has three of it before anything else that flies.
            for (int kind = RunLevels.DRIFTER; kind <= RunLevels.RIME; kind++) {
                if (RunLevels.flierFrom(kind) != number) continue;
                assertTrue("land " + number + " " + kinds, kinds.size() >= 3 && kinds.get(0) == kind && kinds.get(1) == kind && kinds.get(2) == kind);
            }
            if (number == RunLevels.FLIERS_FROM) before = lines;
            last = lines;
        }
        for (int kind = RunLevels.DRIFTER; kind <= RunLevels.RIME; kind++) assertEquals("kind " + kind, RunLevels.flierFrom(kind), first[kind]);
        assertEquals(21, first[RunLevels.DRIFTER]);
        assertTrue("before " + before + " last " + last, before >= 3 && last >= before + 3);
        // And the lands from there on are longer than the count of the lands before would have made them.
        assertTrue(RunLevels.level(79).gate > RunLevels.level(31).gate + 100);
    }

    @Test public void eachOfThoseThatFlyDoesWhatItDoesAndIsBeatenAsItShouldBe() {
        // A comma moth goes to and fro at a height: a touch from the side costs her, coming down on it deals with it, and so does a touch in brown.
        PlusRun run = flown(RunLevels.DRIFTER, 20, 2);
        PlusRun.Foe moth = run.foes().get(0);
        float least = moth.x, most = moth.x, lowest = moth.y;
        for (int step = 0; step < 60 * 12; step++) {
            run.step(STEP, false, false, false);
            least = Math.min(least, moth.x);
            most = Math.max(most, moth.x);
            lowest = Math.max(lowest, moth.y + moth.tall());
        }
        assertTrue(most - least > 6 && most - least < 8);
        // (She can walk under it.)
        assertTrue(lowest < GROUND - .05f);
        assertEquals(0, run.hurts());
        touch(run, moth);
        assertEquals(1, run.hurts());
        assertFalse(moth.flat);
        run.bare();
        assertTrue(stomp(run, moth));
        assertEquals(1, run.fliers());
        assertEquals(1, run.hurts());
        run = flown(RunLevels.DRIFTER, 20, 2);
        run.lend(PlusRun.BROWN);
        touch(run, run.foes().get(0));
        assertTrue(run.foes().get(0).flat);
        assertEquals(0, run.hurts());

        // A caret swallow hangs high; when she comes under, it marks her, and dives through where she stood and up the other side.
        run = flown(RunLevels.SWOOPER, 30, 5);
        PlusRun.Foe swallow = run.foes().get(0);
        hold(run, 2, false, false, false);
        assertEquals(0, swallow.mode);
        assertEquals(0, run.dives());
        run.place(27, GROUND);
        float high = swallow.y;
        boolean marked = false;
        for (int step = 0; step < 60 * 4 && swallow.mode != 2; step++) {
            run.step(STEP, false, false, false);
            marked |= swallow.mode == 1;
        }
        assertTrue(marked);
        assertEquals(2, swallow.mode);
        assertEquals(1, run.dives());
        // Standing where she was marked, she is hit; having moved off, she is not.
        float deepest = 0;
        for (int step = 0; step < 60 * 2 && swallow.mode == 2 && run.hurts() == 0; step++) {
            run.step(STEP, false, false, false);
            deepest = Math.max(deepest, swallow.y + swallow.tall());
        }
        assertEquals(1, run.hurts());
        assertTrue(deepest > GROUND);
        run = flown(RunLevels.SWOOPER, 30, 5);
        swallow = run.foes().get(0);
        run.place(27, GROUND);
        for (int step = 0; step < 60 * 4 && swallow.mode != 2; step++) run.step(STEP, false, false, false);
        run.place(21, GROUND);
        for (int step = 0; step < 60 * 3 && swallow.mode != 0; step++) run.step(STEP, false, false, false);
        assertEquals(0, run.hurts());
        assertEquals(0, swallow.mode);
        assertEquals(high, swallow.y, .4f);
        // Come down on in its dive, or after, it is dealt with.
        assertTrue(stomp(run, swallow));

        // An ellipsis cloud flies over and lets full stops fall straight down while she is under its way; in brown one costs half a heart, and in
        // the air it is the end of a flight in green.
        run = flown(RunLevels.BOMBER, 30, 4);
        PlusRun.Foe cloud = run.foes().get(0);
        hold(run, 3, false, false, false);
        assertTrue(run.shots().isEmpty());
        run.place(28, GROUND);
        run.lend(PlusRun.BROWN);
        int thrown = run.thrown(), halves = run.halves();
        boolean puffed = false;
        for (int step = 0; step < 60 * 20 && run.hurts() == 0; step++) {
            if (run.on(PlusRun.BROWN) < 3) run.lend(PlusRun.BROWN);
            puffed |= cloud.mode == 1;
            for (PlusRun.Shot shot : run.shots()) {
                assertEquals(PlusRun.STOP, shot.kind);
                assertEquals(0, shot.across, 0);
                assertTrue(shot.down > 0);
                // (She steps under the first that falls.)
                if (shot.y > GROUND - 1.5f) run.place(shot.x - PlusRun.WIDE / 2, GROUND);
            }
            run.step(STEP, false, false, false);
        }
        assertTrue(puffed && run.thrown() > thrown);
        assertEquals(1, run.hurts());
        assertEquals(halves - 1, run.halves());
        run = flown(RunLevels.BOMBER, 30, 4);
        run.lend(PlusRun.GREEN);
        for (int step = 0; step < 60 * 20 && run.grounded() == 0; step++) {
            if (run.on(PlusRun.GREEN) < 3) run.lend(PlusRun.GREEN);
            run.bare();
            boolean under = false;
            for (PlusRun.Shot shot : run.shots()) {
                run.place(shot.x - PlusRun.WIDE / 2, shot.y - PlusRun.TALL / 2);
                under = true;
            }
            if (!under) run.place(28, GROUND - 2);
            run.step(STEP, false, false, false);
        }
        assertEquals(1, run.grounded());
        assertFalse(run.has(PlusRun.GREEN));

        // An asterisk hovers, takes aim, and fires at where she is; it backs off from her, so far and no further; and in purple what it fires goes back.
        run = flown(RunLevels.SHOOTER, 30, 3);
        PlusRun.Foe star = run.foes().get(0);
        run.place(26, GROUND);
        boolean aimed = false;
        for (int step = 0; step < 60 * 6 && run.shots().isEmpty(); step++) {
            run.step(STEP, false, false, false);
            aimed |= star.mode == 1;
        }
        assertTrue(aimed);
        assertEquals(1, run.shots().size());
        PlusRun.Shot fired = run.shots().get(0);
        assertEquals(PlusRun.DOT, fired.kind);
        // (At her: to the left and down.)
        assertTrue(fired.across < 0 && fired.down > 0 && !fired.hers);
        run.lend(PlusRun.PURPLE);
        for (int step = 0; step < 60 * 3 && !fired.hers && run.hurts() == 0; step++) run.step(STEP, false, false, false);
        assertTrue(fired.hers);
        assertEquals(0, run.hurts());
        float home = star.home;
        for (int step = 0; step < 60 * 8; step++) {
            run.place(star.x - 2, GROUND);
            run.step(STEP, false, false, false);
            run.shots().clear();
        }
        assertTrue(star.x > home + 4.5f && star.x <= home + 5.05f);
        // From higher ground, or with a jump and a swing, it is dealt with as anything is.
        assertTrue(stomp(run, star));

        // Tittles, four in a line: landed on one after another without touching the ground, each is worth one plus more than the last.
        RunLevels.Level line = flat(80);
        for (int each = 0; each < 4; each++) line.foes.add(new int[]{RunLevels.MITE, 30 + each, FLOOR - 3});
        run = new PlusRun(line);
        int bonus = run.bonus();
        for (PlusRun.Foe tittle : run.foes()) assertTrue(stomp(run, tittle));
        assertEquals(4, run.combo());
        assertEquals(bonus + 1 + 2 + 3, run.bonus());
        assertEquals(4, run.fliers());

        // A candle wisp comes after her, slowly; nothing she swings or lands on it with puts it out, and it burns whoever lands on it; water does,
        // and steam, and a touch in brown; and it can be outrun: it goes no further than eight squares from where it began, and then goes back.
        run = flown(RunLevels.WISP, 30, 3);
        PlusRun.Foe wisp = run.foes().get(0);
        run.place(25, GROUND);
        float apart = Math.abs(wisp.x - run.x());
        hold(run, 2, false, false, false);
        assertTrue(Math.abs(wisp.x - run.x()) < apart - 1.5f);
        assertEquals(0, run.hurts());
        run.arm(PlusRun.SWORD);
        int fizzled = run.fizzled();
        for (int step = 0; step < 60 * 6 && run.fizzled() == fizzled && run.hurts() == 0; step++) {
            run.place(wisp.x - 1.1f, wisp.y + wisp.tall() - PlusRun.TALL);
            run.step(STEP, false, true, false, step % 20 == 0);
        }
        assertTrue(run.fizzled() > fizzled);
        assertEquals(RunLevels.WISP, run.fizzledAt());
        assertFalse(wisp.flat);
        assertFalse(stomp(run, wisp));
        assertEquals(1, run.hurts());
        run.place(60, GROUND);
        hold(run, 12, false, false, false);
        assertEquals(wisp.home, wisp.x, .1f);
        for (int form : new int[]{PlusRun.BLUE, PlusRun.RED}) {
            run = flown(RunLevels.WISP, 30, 0);
            wisp = run.foes().get(0);
            run.lend(form);
            run.place(27.5f, GROUND);
            hold(run, .05f, false, true, false);
            for (int step = 0; step < 90 && !wisp.flat && run.hurts() == 0; step++) run.step(STEP, false, false, false, step % 30 == 0);
            assertEquals("form " + form, form == PlusRun.BLUE, wisp.flat);
        }
        run = flown(RunLevels.WISP, 30, 3);
        run.lend(PlusRun.BROWN);
        touch(run, run.foes().get(0));
        assertTrue(run.foes().get(0).flat);

        // A rime flake goes round and round where it began; only fire melts it.
        for (int form : new int[]{PlusRun.RED, PlusRun.BLUE}) {
            run = flown(RunLevels.RIME, 30, 1);
            PlusRun.Foe flake = run.foes().get(0);
            float round = 0;
            for (int step = 0; step < 60 * 6; step++) {
                run.step(STEP, false, false, false);
                round = Math.max(round, Math.abs(flake.x - flake.home));
            }
            assertEquals(2, round, .1f);
            run.lend(form);
            for (int step = 0; step < 60 * 8 && !flake.flat && run.fizzled() == 0; step++) {
                run.place(flake.x - 2.5f, flake.y + flake.tall() / 2 - PlusRun.TALL / 2);
                run.step(STEP, false, true, false, step % 30 == 0);
            }
            assertEquals("form " + form, form == PlusRun.RED, flake.flat);
            assertEquals("form " + form, form == PlusRun.RED ? 0 : 1, run.fizzled() > 0 ? 1 : 0);
        }
    }

    @Test public void aRibbonAddsItsColourToWhatSheWearsAndNeverTakesTheirPlace() {
        RunLevels.Level level = flat(60);
        level.items.add(new int[]{PlusRun.FORM + PlusRun.GREEN, 20, FLOOR - 1, 0});
        level.items.add(new int[]{PlusRun.FORM + PlusRun.PURPLE, 30, FLOOR - 1, 0});
        level.items.add(new int[]{PlusRun.FORM + PlusRun.BLACK, 40, FLOOR - 1, 0});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.colours = 1 << PlusRun.RED - 1 | 1 << PlusRun.BLUE - 1;
        PlusRun run = new PlusRun(level, perks);
        assertTrue(run.call(PlusRun.RED));
        hold(run, 2, false, false, false);
        // Red on, a green ribbon picked up: both are on, red with the time it had, green with a ribbon's.
        run.place(19.8f, GROUND);
        hold(run, .1f, false, false, false);
        assertTrue(run.has(PlusRun.RED) && run.has(PlusRun.GREEN));
        assertEquals(6.9f, run.on(PlusRun.RED), .1f);
        assertTrue(run.on(PlusRun.GREEN) > 13.5f);
        // A ribbon does not count against how many of her own she may wear: with red and a ribbon on, blue can still be called on.
        assertTrue(run.call(PlusRun.BLUE));
        assertEquals(3, Integer.bitCount(run.mix()));
        // And with as many of her own on as she can hold, another ribbon is still worn, not wasted.
        run.place(29.8f, GROUND);
        hold(run, .1f, false, false, false);
        assertEquals(4, Integer.bitCount(run.mix()));
        assertTrue(run.has(PlusRun.PURPLE) && run.has(PlusRun.RED) && run.has(PlusRun.BLUE) && run.has(PlusRun.GREEN));
        // Each goes in its own time: red first, the ribbons last.
        hold(run, 7, false, false, false);
        assertFalse(run.has(PlusRun.RED));
        assertTrue(run.has(PlusRun.BLUE) && run.has(PlusRun.GREEN) && run.has(PlusRun.PURPLE));
        // A black ribbon is black put on her: everything she wore is gone, each to its own wait. It is no colour of hers: she can call on
        // nothing under it, for twice as long as a ribbon lasts.
        run.place(39.8f, GROUND);
        hold(run, .1f, false, false, false);
        assertEquals(0, run.mix());
        assertFalse(run.coloured());
        assertEquals(PlusRun.BLACK, run.form());
        assertTrue(run.cursed() > PlusRun.BLACKENED - .5f);
        assertTrue(run.untilCall(PlusRun.BLUE) > 9.5f);
        assertTrue(run.untilCall(PlusRun.RED) > 0 && run.untilCall(PlusRun.RED) < run.untilCall(PlusRun.BLUE));
        hold(run, 7.2f, false, false, false);
        assertEquals(PlusRun.BLACK, run.form());
        assertFalse(run.callable(PlusRun.RED));
        hold(run, PlusRun.BLACKENED - 7, false, false, false);
        assertEquals(0, run.form());
        assertTrue(run.callable(PlusRun.RED));
        // A life lost puts an end to everything she wears.
        RunLevels.Level holed = flat(60);
        hole(holed, 16, 18);
        PlusRun.Perks frail = new PlusRun.Perks();
        frail.hearts = 1;
        frail.colours = perks.colours;
        PlusRun fallen = new PlusRun(holed, frail);
        assertEquals(3, fallen.callAll(3));
        fallen.place(17, 6);
        hold(fallen, 2, false, false, false);
        assertEquals(1, fallen.livesLost());
        assertEquals(0, fallen.mix());
        assertTrue(fallen.untilCall(PlusRun.RED) > 8 && fallen.untilCall(PlusRun.BLUE) > 8);
    }

    @Test public void mixesHaveNamesAndPowersOfTheirOwnAndAllSevenAreWhite() {
        // Steam, red and blue: one cloud in place of fire and water, which goes on through one sign to the next; and a jump more in the air.
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.TIMES, 10, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.TIMES, 12, FLOOR - 1});
        PlusRun.Perks all = new PlusRun.Perks();
        all.colours = (1 << PlusRun.BLACK) - 1;
        all.together = 3;
        PlusRun run = new PlusRun(level, all);
        hold(run, .2f, false, false, false);
        assertEquals(3, run.callAll(1 | 2));
        assertEquals(1 << PlusRun.STEAMS, run.mixed());
        assertEquals(PlusRun.STEAMS, run.lastMix());
        assertEquals(1, run.mixings());
        run.place(5, GROUND);
        hold(run, .1f, false, true, false);
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.shots().size());
        assertEquals(PlusRun.STEAM, run.shots().get(0).kind);
        hold(run, 1.2f, false, false, false);
        assertEquals(2, run.flattened());
        int jumps = run.flaps();
        run.step(STEP, false, false, true);
        hold(run, .2f, false, false, false);
        run.step(STEP, false, false, true);
        assertEquals(jumps + 1, run.flaps());
        // A third colour: the prism, through which whatever lands on her lands by half.
        assertTrue(run.call(PlusRun.GREEN));
        assertTrue((run.mixed() & 1 << PlusRun.PRISM) != 0);
        assertTrue((run.mixed() & 1 << PlusRun.TIDE) != 0);
        assertEquals(PlusRun.PRISM, run.lastMix());

        // The comet, red and yellow: at a run she goes through whatever she meets, and it costs her nothing; at a walk she does not.
        RunLevels.Level row = flat(80);
        row.foes.add(new int[]{RunLevels.TIMES, 30, FLOOR - 1, 3});
        PlusRun comet = new PlusRun(row, all);
        hold(comet, .2f, false, false, false);
        assertEquals(1 | 16, comet.callAll(1 | 16));
        assertEquals(1 << PlusRun.COMET, comet.mixed());
        comet.place(20, GROUND);
        for (int step = 0; step < 200 && comet.x() < 34; step++) comet.step(STEP, false, true, false);
        assertEquals(1, comet.flattened());
        assertEquals(0, comet.hurts());

        // The ember, red and brown: what her touch knocks over goes off burning into what is beyond it.
        RunLevels.Level two = flat(80);
        two.foes.add(new int[]{RunLevels.TIMES, 24, FLOOR - 1});
        two.foes.add(new int[]{RunLevels.TIMES, 29, FLOOR - 1});
        PlusRun ember = new PlusRun(two, all);
        hold(ember, .2f, false, false, false);
        assertEquals(1 | 4, ember.callAll(1 | 4));
        assertEquals(1 << PlusRun.EMBER, ember.mixed());
        ember.place(21, GROUND);
        for (int step = 0; step < 120 && ember.flattened() == 0; step++) ember.step(STEP, false, true, false);
        assertEquals(1, ember.flattened());
        float stood = ember.x();
        hold(ember, .6f, false, false, false);
        assertEquals(2, ember.flattened());
        assertTrue(ember.x() < stood + 1.5f);

        // The mirror, brown and purple: what is thrown at her goes back, and on through a row of them.
        RunLevels.Level throwers = flat(80);
        throwers.foes.add(new int[]{RunLevels.THROWER, 30, FLOOR - 1});
        throwers.foes.add(new int[]{RunLevels.THROWER, 32, FLOOR - 1});
        PlusRun mirror = new PlusRun(throwers, all);
        hold(mirror, .2f, false, false, false);
        assertEquals(4 | 32, mirror.callAll(4 | 32));
        assertEquals(1 << PlusRun.MIRROR, mirror.mixed());
        mirror.place(26, GROUND);
        for (int step = 0; step < 900 && mirror.flattened() < 2; step++) {
            if (mirror.on(PlusRun.PURPLE) < 1) mirror.lend(PlusRun.PURPLE);
            if (mirror.on(PlusRun.BROWN) < 1) mirror.lend(PlusRun.BROWN);
            mirror.step(STEP, false, false, false);
        }
        assertEquals(2, mirror.flattened());
        assertEquals(0, mirror.hurts());
        assertTrue(mirror.turned() >= 1);

        // The gale, green and yellow: she rises faster than green alone lifts her. And the tide, blue and green: let go of in the air, she comes down slowly.
        float[] rose = new float[2];
        for (int which = 0; which < 2; which++) {
            PlusRun flier = new PlusRun(flat(60), all);
            hold(flier, .2f, false, false, false);
            flier.callAll(which == 0 ? 8 : 8 | 16);
            float from = flier.y();
            hold(flier, 1.5f, false, false, true);
            rose[which] = from - flier.y();
        }
        assertTrue("green " + rose[0] + " gale " + rose[1], rose[1] > rose[0] + 2);
        PlusRun tide = new PlusRun(flat(60), all);
        hold(tide, .2f, false, false, false);
        assertEquals(2 | 8, tide.callAll(2 | 8));
        assertEquals(1 << PlusRun.TIDE, tide.mixed());
        hold(tide, 1.5f, false, false, true);
        float high = tide.y();
        hold(tide, .5f, false, false, false);
        assertTrue("fell " + (tide.y() - high), tide.y() - high < 1.5f);
        // And its water goes both ways.
        tide.step(STEP, false, false, false, true);
        int water = 0;
        for (PlusRun.Shot shot : tide.shots()) if (shot.kind == PlusRun.WATER) water++;
        assertEquals(2, water);

        // How many she may wear at once is what she has been given to keep: two, three, and at the last all of them; and all six are white:
        // a white with a time to it, as each of the six has. Black is not one of them.
        PlusRun.Perks few = new PlusRun.Perks();
        few.colours = (1 << PlusRun.BLACK) - 1;
        PlusRun two2 = new PlusRun(flat(60), few);
        assertEquals(2, two2.together());
        assertEquals(2, Integer.bitCount(two2.callAll(127)));
        assertFalse(two2.white());
        few.together = 5;
        assertEquals(5, Integer.bitCount(new PlusRun(flat(60), few).callAll(127)));
        few.together = 7;
        PlusRun white = new PlusRun(flat(60), few);
        assertEquals(0, white.whitened());
        assertEquals(63, white.callAll(127));
        assertTrue(white.white());
        assertEquals(PlusRun.WHITE, white.form());
        assertEquals(1, white.whitened());
        assertTrue((white.mixed() & 1 << PlusRun.WHITEN) != 0);
        assertEquals(PlusRun.WHITEN, white.lastMix());
        for (int colour = PlusRun.RED; colour <= PlusRun.PURPLE; colour++) assertTrue(white.has(colour));
        assertFalse(white.has(PlusRun.BLACK));
        assertTrue(white.formLeft() > 8 && white.formLeft() <= 9);
        hold(white, 9.2f, false, false, false);
        assertFalse(white.white());
        assertEquals(0, white.mix());
        assertEquals(1, white.whitened());
        // A white ribbon, which no place gives now, would be white too, for as long as it lasted.
        PlusRun lent = new PlusRun(flat(60), new PlusRun.Perks());
        lent.lend(PlusRun.WHITE);
        assertTrue(lent.white());
        assertEquals(PlusRun.WHITE, lent.form());
        assertEquals(63, lent.mix());
        assertEquals(1, lent.whitened());
        assertTrue(lent.formLeft() > 11);
        hold(lent, 12.2f, false, false, false);
        assertFalse(lent.white());
        assertEquals(0, lent.mix());

    }

    @Test public void eachOfThoseWhoWaitGivesSomethingToKeep() {
        // Whoever waits at the end of a land has something to give that is kept: the seven colours, one each, black last; and Zero his ring.
        boolean[] won = new boolean[PlusRun.WHITE + 1];
        int last = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            int prize = RunLevels.prizeOf(number);
            // (Lucy has nothing to hand over: what is won in the last land is the white that is truly hers, and it is won in the moment.)
            assertEquals("land " + number, RunLevels.bossOf(number) >= 0 && number != RunLevels.TOTAL, prize != RunLevels.NOTHING);
            if (prize > 0) {
                assertFalse("land " + number, won[prize]);
                won[prize] = true;
                last = prize;
            }
        }
        for (int colour = PlusRun.RED; colour <= PlusRun.BLACK; colour++) assertTrue("no one gives " + colour, won[colour]);
        assertFalse(won[PlusRun.WHITE]);
        assertEquals(PlusRun.BLACK, last);
        assertEquals(RunLevels.RING, RunLevels.prizeOf(RunLevels.COUNT));
    }

    @Test public void theRangeHangsTargetsAndTheOldBookSleepsUnderSomeCellars() {
        // The range: an ink gun that does not run dry, and three targets hung at a time for twelve seconds.
        int[] which = new int[1];
        RunLevels.Level with = null;
        for (int number = 1; with == null; number++) {
            RunLevels.Level level = RunLevels.level(number);
            for (int index = 0; index < level.rooms.size(); index++) {
                if (level.rooms.get(index)[0] == RunLevels.RANGE) {
                    with = level;
                    which[0] = index;
                }
            }
        }
        int[] one = with.rooms.get(which[0]);
        with.weapons.add(new int[]{PlusRun.SWORD, one[1], one[2] - 1});
        PlusRun run = new PlusRun(with);
        run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertEquals(PlusRun.SWORD, run.weapon());
        int bonus = run.bonus();
        assertTrue(run.enter());
        assertEquals(PlusRun.GUN, run.weapon());
        assertTrue(run.sealed());
        assertEquals(12, run.gameLeft(), .01f);
        int hit = 0;
        for (int step = 0; step < 60 * 13 && run.gameLeft() > 0; step++) {
            PlusRun.Foe target = null;
            int hung = 0;
            for (PlusRun.Foe foe : run.foes()) {
                if (foe.kind != RunLevels.TARGET || foe.flat) continue;
                hung++;
                target = foe;
                // A target is no one: it neither moves nor hurts.
                assertEquals(foe.home, foe.x, 0);
                assertTrue(foe.x > one[3] && foe.x < one[4]);
            }
            assertTrue(hung <= 3);
            // Level with one of them and facing it, she fires.
            if (target != null) {
                boolean right = target.x > run.x();
                run.place(right ? Math.max(one[3] + 1, target.x - 3) : Math.min(one[4] - 1, target.x + 3), target.y + target.tall() / 2 - PlusRun.TALL * .45f);
                run.step(STEP, !right, right, false, false);
                run.step(STEP, false, false, false, true);
            } else {
                run.step(STEP, false, false, false);
            }
            hit = run.caught();
        }
        assertTrue("hit " + hit, hit >= PlusRun.ENOUGH);
        assertEquals(0, run.hurts());
        assertFalse(run.sealed());
        // (And whatever fell from any creature her ink caught on the way, picked up.)
        assertEquals(bonus + hit * 2 + 10 + run.dropPluses(), run.bonus());
        // What she carried is in her hands again, and the targets are gone.
        assertEquals(PlusRun.SWORD, run.weapon());
        for (PlusRun.Foe foe : run.foes()) assertTrue(foe.kind != RunLevels.TARGET || foe.flat);

        // The Old Book: under the last wave of one cellar in two. He is fought as those who wait are, but he is no one's gatekeeper.
        RunLevels.Level cellar = null;
        int den = -1;
        for (int number = 1; cellar == null; number++) {
            RunLevels.Level level = RunLevels.level(number);
            if (RunLevels.bossOf(number) >= 0) continue;
            for (int index = 0; index < level.rooms.size(); index++) {
                if (level.rooms.get(index)[10] != 0) {
                    cellar = level;
                    den = index;
                }
            }
        }
        one = cellar.rooms.get(den);
        cellar.items.add(new int[]{PlusRun.NOTES, one[1], one[2] - 1, 0});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.lasting = 1000;
        run = new PlusRun(cellar, perks);
        run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        bonus = run.bonus();
        assertTrue(run.enter());
        for (int wave = 1; wave < PlusRun.WAVES; wave++) {
            arrived(run);
            dealt(run);
            hold(run, PlusRun.BREATH + .1f, false, false, false);
        }
        assertEquals(PlusRun.WAVES, run.wave());
        arrived(run);
        // The rest of the last wave is dealt with, for the purposes of this: he is what is left.
        thinned(run);
        for (PlusRun.Foe foe : run.foes()) if (foe.spawned && foe.kind != RunLevels.OLD_BOOK) foe.flat = true;
        assertEquals(1, run.woken());
        PlusRun.Foe book = null;
        for (PlusRun.Foe foe : run.foes()) if (foe.kind == RunLevels.OLD_BOOK) book = foe;
        assertTrue(book.spawned);
        assertEquals(PlusRun.health(RunLevels.OLD_BOOK), book.left);
        assertFalse(run.standing());
        assertFalse(run.locked());
        int did = 0, changes = 0;
        for (int step = 0; step < 60 * 300 && !book.flat; step++) {
            assertTrue("at " + book.x, run.absent(book) || book.x >= one[3] + 1 - .01f && book.x + book.wide() <= one[4] + .01f);
            int wanted = 0;
            for (int thing : PlusRun.does(book.kind, run.phase(book))) wanted |= 1 << thing;
            if (pressable(run, book) && (book.did & wanted) == wanted) {
                int phases = run.phases();
                run.place(book.x + book.wide() / 2 - PlusRun.WIDE / 2, book.y + .1f);
                run.step(STEP, false, false, false);
                if (run.phases() > phases) changes++;
            } else {
                run.place(Math.max(one[3] + 2, book.x - 7), one[11] - 9);
                run.step(STEP, false, false, false);
            }
            did = book.did;
        }
        assertTrue("left " + book.left + " did " + book.did + " step " + book.step + " mode " + book.mode + " enter " + book.enter + " at " + book.x + " her " + run.x()
                + " wave " + run.wave() + " phase " + run.wavePhase() + " state " + run.state() + " room " + run.room(), book.flat);
        assertEquals(2, changes);
        assertEquals(3, Integer.bitCount(did));
        run.step(STEP, false, false, false);
        assertEquals(0, run.wave());
        assertFalse(run.sealed());
        assertTrue(run.ranked() >= 0);
        // (And the pluses that fell from the Old Book, those she was close enough to draw in.)
        assertEquals(bonus + PlusRun.RANK_PAYS[run.ranked()] + 20 + run.dropPluses(), run.bonus());
        assertEquals(PlusRun.PLAYING, run.state());
    }

    @Test public void aColourGivenHerIsHersToCallOnAndOneLentIsNot() {
        // A colour given her there and then is hers to call on; one lent her is not.
        PlusRun given = new PlusRun(flat(20), new PlusRun.Perks());
        given.gift(PlusRun.RED);
        assertEquals(PlusRun.RED, given.form());
        hold(given, 9.5f, false, false, false);
        assertEquals(0, given.form());
        assertFalse(given.call(PlusRun.RED));
        hold(given, 10, false, false, false);
        assertTrue(given.call(PlusRun.RED));
        PlusRun lent = new PlusRun(flat(20), new PlusRun.Perks());
        lent.lend(PlusRun.GREEN);
        hold(lent, 15, false, false, false);
        assertFalse(lent.call(PlusRun.GREEN));
        // What doing what is asked pays, by what was asked.
        assertEquals(RunLevels.PURSE, RunLevels.questPrize(RunLevels.CAKES));
        assertEquals(RunLevels.SHOTS, RunLevels.questPrize(RunLevels.CELLAR));
        assertEquals(RunLevels.LASTING, RunLevels.questPrize(RunLevels.LETTER));
        assertEquals(RunLevels.QUICKER, RunLevels.questPrize(RunLevels.TIME));
        assertEquals(RunLevels.RIBBON, RunLevels.questPrize(RunLevels.ROOM));
    }

    @Test public void aFlagOnHighGroundBringsHerBackOntoIt() {
        RunLevels.Level level = flat(40);
        for (int column = 10; column < 20; column++) for (int row = FLOOR - 3; row < FLOOR; row++) level.land[row][column] = RunLevels.BLOCK;
        level.land[FLOOR - 4][12] = RunLevels.FLAG;
        level.flags.add(12);
        hole(level, 24, 28);
        PlusRun.Perks frail = new PlusRun.Perks();
        frail.hearts = 1;
        PlusRun run = new PlusRun(level, frail);
        run.place(11, FLOOR - 3 - PlusRun.TALL);
        while (run.hurts() == 0 && run.time() < 20) run.step(STEP, false, true, false);
        assertEquals(1, run.hurts());
        assertEquals(1, run.livesLost());
        hold(run, .3f, false, false, false);
        assertEquals(12 + (1 - PlusRun.WIDE) / 2, run.x(), .01f);
        assertEquals(FLOOR - 3 - PlusRun.TALL, run.y(), .01f);
    }

    @Test public void theLandsOfTheSecondVolumeAreLongerAndHaveWhatTheirChaptersAreMadeOf() {
        boolean[] ground = new boolean[128], signs = new boolean[RunLevels.LUCY + 1], ribbons = new boolean[PlusRun.WHITE + 1], met = new boolean[RunLevels.NURI + 1];
        int movers = 0, doors = 0, first = 0, total = 0, heights = 0, crowd = 0;
        for (int number = 1; number <= RunLevels.COUNT; number++) first += RunLevels.level(number).columns;
        for (int number = RunLevels.COUNT + 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int boss = RunLevels.bossOf(number);
            assertEquals(5 + (number - 31) / 10, level.chapter);
            // (In the third volume no one waits but at its very end.)
            assertEquals("land " + number, number <= RunLevels.SECOND && number % 5 == 0 || number == RunLevels.TOTAL, boss >= 0);
            assertEquals("land " + number, boss >= 0 ? 4 : 3, level.flags.size());
            assertEquals(boss >= 0, level.guarded);
            assertEquals(boss >= 0, level.arena >= 0);
            // By where she starts, by each flag and before whoever waits; and a book or a brush, and an ink gun or two, each before what it is good against.
            int guns = 0;
            for (int[] weapon : level.weapons) {
                assertEquals("land " + number + " column " + weapon[1], weapon[2] + 1, RunLevels.top(level, weapon[1]));
                if (weapon[0] == PlusRun.GUN) guns++;
            }
            int lying = (boss >= 0 ? 5 : 4) + 2 + number % 2;
            assertTrue("land " + number + " has " + level.weapons.size(), level.weapons.size() >= lying && level.weapons.size() <= lying + level.gate / 40);
            assertTrue("land " + number, guns >= 1);
            assertEquals(level.columns, RunLevels.level(number).columns);
            total += level.columns;
            crowd += level.foes.size();
            int friends = 0, villagers = 0, stalls = 0;
            for (int[] item : level.items) {
                if (item[0] == PlusRun.STALL) stalls++;
                else if (item[0] == PlusRun.DROPOFF) assertEquals(RunLevels.LETTER, level.quest);
                else if (item[1] >= level.main && item[3] >= RunLevels.SOMI) continue;
                else if (item[3] >= RunLevels.SOMI) villagers++;
                else if (item[3] != 0) friends++;
                met[item[3]] = true;
                if (item[0] > PlusRun.FORM && item[0] < PlusRun.CHEST) ribbons[item[0] - PlusRun.FORM] = true;
                int top = RunLevels.top(level, item[1]);
                assertTrue("land " + number + " column " + item[1], item[1] >= level.main || top >= 0 && item[2] < top);
            }
            assertEquals("land " + number, boss == RunLevels.MAWANG ? 5 : 3, friends);
            // Two villages, each with someone in it and a stall, and things to look at.
            assertEquals("land " + number, 2, villagers);
            assertEquals("land " + number, 2, stalls);
            assertTrue("land " + number, level.decor.size() >= 8);
            for (char[] row : level.land) for (char square : row) ground[square] = true;
            for (int[] foe : level.foes) if (foe[0] < signs.length) signs[foe[0]] = true;
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
        assertTrue("mean " + total / 50 + " against " + first / 30, total / 50 > first / 30 * 1.5f);
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
        for (int form = PlusRun.RED; form <= PlusRun.BLACK; form++) assertTrue("no ribbon " + form, ribbons[form]);
        assertFalse(ribbons[PlusRun.WHITE]);
        for (int who = 1; who <= RunLevels.NURI; who++) assertTrue("no one met " + who, met[who]);
    }

    // Her lines

    private static String[] line(String[][] lines, String name) {
        for (String[] one : lines) if (one[0].equals(name)) return one;
        throw new AssertionError("no line " + name);
    }

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
        assertEquals(95 + 86 + 37 + 7 + 11 + 4 + 40 + 19 + 3 + 3 + 3 + 3 + 5 + 7 + 1 + 1 + 2 + 1 + 10 + 5 + 6 + 4 + 2 + 9 + 19 + 12 + 14 + 4 + RunStory.GALLERY.length + RunStory.NEW_PET.length + RunStory.WARP.length + 80 + RunStory.LEVEL.length
                // (The two lines about sloppy copies in the lands, her line with the game's name in it, and the keys.)
                + 2 + RunStory.TITLE_LINE.length + RunStory.KEYS.length + RunStory.SKILLS.length + RunStory.MOVES.length + RunStory.SECRETS.length + RunStory.WATER.length + RunStory.DUNGEONS.length + RunStory.SMITH_QUESTS.length + RunStory.KNUCKLES.length
                + RunStory.LATER_UNIQUES.length + RunStory.BLACKENED.length + RunStory.STATUS_UNIQUES.length + RunStory.MIRROR.length + RunStory.ECHOES.length + RunStory.ECHO_ANSWERS.length + RunStory.CAST_SHADOWS.length, lines);
        assertEquals(5, RunStory.GALLERY.length);
        assertEquals(14, RunStory.REVEAL.length);
        // The clip stands where Halgakos names the great evil, and says it shorter, in her own way; there is something told before it and after.
        assertEquals("rev7", RunStory.REVEAL[RunStory.CUT_AT][0]);
        assertEquals("H", RunStory.REVEAL[RunStory.CUT_AT][2]);
        assertTrue(RunStory.REVEAL[RunStory.CUT_AT][1].contains("great evil"));
        assertEquals("So you're the great evil. The Mother Tongue " + Chatter.SLOPPY, RunStory.CUT_SAYS);
        assertTrue(RunStory.CUT_AT > 0 && RunStory.CUT_AT + 1 < RunStory.REVEAL.length);
        assertEquals(4, RunStory.READ.length);
        assertEquals("f", RunStory.READ[0][2]);
        assertEquals(12, RunStory.LUCY.length);
        assertEquals("e", RunStory.LUCY[0][2]);
        assertEquals(RunLevels.FRIENDS, RunStory.PLEDGES.length);
        assertEquals(RunLevels.FRIENDS, RunStory.FRIENDS.length);
        assertEquals(RunLevels.ROOM, RunStory.ASKS.length);
        assertEquals(RunLevels.ROOM, RunStory.ASKED.length);
        assertEquals(RunLevels.TRAMPLE + 1, RunStory.ROOMS.length);
        assertTrue(line(RunStory.ENTER, "enter14")[2].equals("Z") && line(RunStory.ENTER, "enter15")[2].equals("a"));
        assertEquals(RunLevels.NURI - RunLevels.SOMI + 1, RunStory.MEET.length);
        assertEquals(16, RunStory.ENTER.length);
        assertEquals(32, RunStory.PHASES.length);
        assertEquals(RunLevels.ALTTEUL - RunLevels.SOMI + 1, RunStory.RUMOURS.length);
        assertEquals(15, RunStory.waiting(RunLevels.OLD_BOOK));
        boolean[] theirs = new boolean[15];
        for (int kind : new int[]{RunLevels.ZERO, RunLevels.ILLYA, RunLevels.TSUBAKIEH, RunLevels.GOKHAE, RunLevels.MIRI, RunLevels.YEOBAEK, RunLevels.KING,
                RunLevels.MAWANG, RunLevels.GUARD, RunLevels.BUTLER, RunLevels.BYEORU, RunLevels.JUPAN, RunLevels.SUMBI, RunLevels.PADS, RunLevels.PARTY}) {
            assertFalse(theirs[RunStory.waiting(kind)]);
            theirs[RunStory.waiting(kind)] = true;
        }
        assertEquals(RunLevels.ALL_CHAPTERS, RunStory.CHAPTERS.length);
        assertEquals(PlusRun.WHITE, RunStory.FORMS.length);
        assertEquals(RunLevels.OLD_BOOK - RunLevels.GOKHAE + 1, RunStory.BLOWS_LATER.length);
        for (int land = RunLevels.COUNT + 1; land <= RunLevels.TOTAL; land++) {
            assertEquals("land " + land, RunLevels.bossOf(land) >= 0 && land != RunLevels.TOTAL, RunStory.outro(land) != null);
            if (land % 10 == 0 || land % 10 == 1) assertTrue("land " + land, RunStory.intro(land) != null);
        }
        assertEquals(RunStory.SPEAKERS.length(), RunStory.NAMES.length);
        assertEquals(PlusRun.GUN + 1, RunStory.ARMS.length);
        assertEquals(PlusRun.NOTES + 1, RunStory.TOOK.length);
        assertEquals(10, RunStory.SEE.length);
        // The story moves on where someone joins her and where someone waits for her.
        for (int land : new int[]{1, 3, 7, 9, 13, RunLevels.ILLYA_LAND, 19, RunLevels.TSUBAKIEH_LAND, 25, RunLevels.COUNT}) {
            // (The Guild Guard and Penchok-jipsa wait where no part of the story begins.)
            assertTrue("land " + land, RunStory.intro(land) != null);
        }
        for (int land = 1; land <= RunLevels.COUNT; land++) {
            assertEquals("land " + land, RunLevels.bossOf(land) >= 0 && land != RunLevels.TOTAL, RunStory.outro(land) != null);
        }
    }

    // The thirty lands

    private static final int WALK = 0, WAIT = 1, BACK = 2;
    /** How many steps the jump button is held in each of the jumps the player tries. */
    private static final int[] HOLDS = {1, 4, 8, 13, 22};

    /** Plays a plan out on a run: what the buttons do at each step of it. */
    private static void act(PlusRun run, int plan, int step) {
        boolean strike = fighting && strikeNow(run);
        if (plan == WALK) run.step(STEP, false, true, false, strike);
        else if (plan == WAIT) run.step(STEP, false, false, false, strike);
        else if (plan == BACK) run.step(STEP, true, false, false, strike);
        else run.step(STEP, false, true, step < HOLDS[plan - 3], strike);
    }

    /** Whether the bot fights as it goes: a tap of what it holds, five times a second, while a creature is near. And how much harder it has it (deep in a dungeon). */
    private static boolean fighting;
    private static int crossHarder;

    private static boolean strikeNow(PlusRun run) {
        if ((int) (run.time() * 10) % 2 != 0) return false;
        float middle = run.x() + PlusRun.WIDE / 2, feet = run.y() + PlusRun.TALL;
        for (PlusRun.Foe foe : run.foes()) {
            if (foe.flat || foe.unseen || RunLevels.boss(foe.kind)) continue;
            if (Math.abs(foe.x + foe.wide() / 2 - middle) < 2.6f && Math.abs(foe.y + foe.tall() - feet) < 1.6f) return true;
        }
        return false;
    }

    /**
     * A fair player: the crossing bot, striking too when a creature is near, at the level the land is for and with what she has from
     * the start, crosses a land of every chapter with all its swarms and its unique creatures in it, and keeps a life. (Who waits at
     * the end is left out, as for the other crossings: those fights have tests of their own.)
     */
    @Test public void aFairPlayerWhoFightsCrossesALandOfEveryChapterWithItsSwarmsAndUniques() {
        StringBuilder lost = new StringBuilder();
        int crossed = 0, beaten = 0;
        fighting = true;
        try {
            for (int number : new int[]{3, 9, 15, 21, 27, 35, 45, 55, 65, 75, 85, 95}) {
                RunLevels.Level level = RunLevels.level(number);
                for (int index = level.foes.size() - 1; index >= 0; index--) if (RunLevels.boss(level.foes.get(index)[0])) level.foes.remove(index);
                for (int index = level.items.size() - 1; index >= 0; index--) if (level.items.get(index)[0] >= PlusRun.PET) level.items.remove(index);
                level.guarded = false;
                PlusRun run = cross(level, number > RunLevels.COUNT ? 500 : 360);
                lost.append(" ").append(number).append(":").append(run.livesLost()).append(run.state() == PlusRun.WON ? "" : run.state() == PlusRun.PLAYING ? " (stuck at " + (int) run.x() + " of " + run.columns() + ")" : " LOST");
                // Never every life lost; and most of them crossed to the end. (One that is not was held up by the way, not beaten: the
                // bot does not fight well from a ledge, or climb past creatures standing on it.)
                if (run.state() == PlusRun.LOST) beaten++;
                if (run.state() == PlusRun.WON) crossed++;
            }
        } finally {
            fighting = false;
        }
        System.out.println("FAIR lives lost" + lost);
        assertEquals("beaten:" + lost, 0, beaten);
        assertTrue("crossed " + crossed + ":" + lost, crossed >= 8);
    }

    /**
     * A fair player in the dungeons: the same bot, fighting, crosses a middle floor of dungeons from first to last with their own creatures
     * in, and keeps a life. (Their keepers are fought in RunDungeonsTest.)
     */
    @Test public void aFairPlayerWhoFightsCrossesDungeonFloorsWithTheirOwnCreatures() {
        StringBuilder lost = new StringBuilder();
        int beaten = 0, crossed = 0;
        fighting = true;
        try {
            for (int dungeon : new int[]{0, 4, 9, 14, 19, 23}) {
                // (On the hardest of its floors that a dive lays: a teeming one, as deep as can be before the keeper's.)
                int floor = RunDungeons.floors(dungeon) - 2;
                long dive = 77;
                while (RunDungeons.kindOf(dungeon, floor, dive) != RunDungeons.TEEMING) dive++;
                RunLevels.Level level = RunLevels.dungeon(dungeon, floor, dive);
                crossHarder = RunDungeons.deeper(floor);
                for (int index = level.items.size() - 1; index >= 0; index--) if (level.items.get(index)[0] >= PlusRun.PET) level.items.remove(index);
                int own = 0;
                for (int[] foe : level.foes) if (RunUniques.is(foe[0])) own++;
                PlusRun run = cross(level, 500);
                lost.append(" ").append(dungeon).append(" (").append(level.columns).append(" wide, ").append(own).append(" of its own, ").append(run.flattened()).append(" beaten):").append(run.livesLost()).append(run.state() == PlusRun.WON ? "" : run.state() == PlusRun.PLAYING ? " (stuck at " + (int) run.x() + " of " + run.columns() + ")" : " LOST");
                if (run.state() == PlusRun.LOST) beaten++;
                if (run.state() == PlusRun.WON) crossed++;
            }
        } finally {
            fighting = false;
            crossHarder = 0;
        }
        System.out.println("DIVE lives lost" + lost);
        assertEquals("beaten:" + lost, 0, beaten);
        assertTrue("crossed " + crossed + ":" + lost, crossed >= 4);
    }

    /** Every dungeon's first floor and keeper's floor can be crossed, and every floor of three of them (without their creatures, as for a land). */
    @Test public void everyDungeonFloorCanBeCrossed() {
        StringBuilder stuck = new StringBuilder();
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon++) {
            int floors = RunDungeons.floors(dungeon);
            for (int floor = 0; floor < floors; floor++) {
                if (floor != 0 && floor != floors - 1 && dungeon % 8 != 3) continue;
                PlusRun run = cross(unguarded(RunLevels.dungeon(dungeon, floor, 31 + dungeon)), 400);
                if (run.state() != PlusRun.WON) stuck.append("\n").append(dungeon).append("/").append(floor).append(" at ").append((int) run.x()).append(" of ").append(run.columns()).append(around(run));
            }
        }
        assertEquals("", stuck.toString());
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
        // (At the level the land is for: its coloured creatures take more than a blow at the first level.)
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.level = RunLoot.recommended(level.number);
        perks.hearts += RunLoot.hearts(perks.level);
        perks.defence = RunLoot.guard(perks.level);
        perks.harder = crossHarder;
        PlusRun run = new PlusRun(level, perks);
        float furthest = run.x(), since = 0;
        while (run.state() == PlusRun.PLAYING && run.time() < seconds) {
            // Fallen into a cave under the land, it makes for the vine up out of it, and climbs it.
            int below = (int) Math.floor(run.x() + PlusRun.WIDE / 2);
            if (run.y() > RunLevels.ROWS - .5f && level.under != null && below >= 0 && below < level.under.length && level.under[below]) {
                caveOut(run, level);
                furthest = run.x();
                since = run.time();
                continue;
            }
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

    /** In a cave: across to the vine up out of it (the nearest one on, or behind), jumping what is in the way, and up it. */
    private static void caveOut(PlusRun run, RunLevels.Level level) {
        // (The vine of this cave: within the run of columns with a cave under them that she is in.)
        int at = (int) Math.floor(run.x() + PlusRun.WIDE / 2), from = at, to = at;
        while (from > 0 && level.under[from - 1]) from--;
        while (to < level.under.length - 1 && level.under[to + 1]) to++;
        int vine = -1;
        for (int[] way : level.ways) if (way[2] == 1 && way[0] >= from && way[0] <= to) vine = way[0];
        if (vine < 0) {
            run.step(STEP, false, true, false);
            return;
        }
        boolean right = vine + .5f > run.x() + PlusRun.WIDE / 2;
        float last = run.x(), stuck = 0, jumping = 0;
        for (float gone = 0; gone < 20 && Math.abs(run.x() + PlusRun.WIDE / 2 - vine - .5f) > .3f && run.state() == PlusRun.PLAYING; gone += STEP) {
            stuck = Math.abs(run.x() - last) < .001f ? stuck + STEP : 0;
            last = run.x();
            if (stuck > .15f && run.onGround()) jumping = .35f;
            run.step(STEP, !right, right, jumping > 0);
            jumping -= STEP;
        }
        for (float gone = 0; gone < 8 && run.state() == PlusRun.PLAYING && !(run.onGround() && run.y() + PlusRun.TALL < RunLevels.ROWS - .5f); gone += STEP) {
            run.step(STEP, false, false, true);
        }
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
            // (Nor those put in to make up a land's numbers: what is proved is that the land itself can be crossed.)
            // (Nor the unique creatures, which are put in after the land is made, like them: RunUniquesTest fights them.)
            if (RunLevels.boss(level.foes.get(index)[0]) || RunLevels.crowd(level.foes.get(index)) || RunLevels.uniqueSpec(level.foes.get(index))) level.foes.remove(index);
        }
        // (Nor a pet that lies waiting: the land is crossed without one.)
        for (int index = level.items.size() - 1; index >= 0; index--) if (level.items.get(index)[0] >= PlusRun.PET) level.items.remove(index);
        level.guarded = false;
        return level;
    }

    @Test public void everyLandCanBeCrossedWithoutLosingAHeart() {
        for (int number = 1; number <= RunLevels.COUNT; number++) {
            PlusRun run = cross(unguarded(RunLevels.level(number)), 240);
            assertEquals("land " + number + " stopped at " + run.x() + " of " + run.columns() + " with " + run.hearts() + " hearts" + around(run),
                    PlusRun.WON, run.state());
            // (With its swarms, from the thirteenth land on a bot that only jumps and stomps may lose a heart; never a life.)
            // (With its swarms, from the thirteenth land on a bot that only jumps and stomps may lose a heart; never a life.)
            assertTrue("land " + number + " with " + run.hearts(), run.hearts() >= run.maxHearts() - (number <= 12 ? 0 : 1) && run.livesLost() == 0);
        }
    }

    /** Laid in layers for a play, every land can still be crossed along its main way, whatever is under it and over it. */
    @Test public void everyLandInItsLayersCanStillBeCrossed() {
        for (int number = 1; number < RunLevels.TOTAL; number++) {
            for (long play : new long[]{1, 2}) {
                RunLevels.Level level = unguarded(RunLevels.layered(RunLevels.level(number), play));
                // (Nor what walks in the caves: going through one is a choice, and fighting there a matter of skill.)
                for (int index = level.foes.size() - 1; index >= 0; index--) if (level.foes.get(index)[2] >= RunLevels.ROWS) level.foes.remove(index);
                PlusRun run = cross(level, number > RunLevels.COUNT ? 400 : 240);
                assertEquals("land " + number + " play " + play + " stopped at " + run.x() + ", " + run.y() + " of " + run.columns() + " with " + run.hearts() + " hearts" + around(run),
                        PlusRun.WON, run.state());
            }
        }
    }

    @Test public void everyLandOfTheSecondVolumeCanBeCrossedWithoutLosingAHeart() {
        for (int number = RunLevels.COUNT + 1; number <= RunLevels.TOTAL; number++) {
            PlusRun run = cross(unguarded(RunLevels.level(number)), 400);
            assertEquals("land " + number + " stopped at " + run.x() + " of " + run.columns() + " with " + run.hearts() + " hearts" + around(run),
                    PlusRun.WON, run.state());
            // (Its coloured creatures can cost a bot that only jumps and stomps a heart or two now and then; never a life.)
            assertTrue("land " + number + " with " + run.hearts(), run.hearts() >= 1 && run.livesLost() == 0);
            if (run.hearts() < PlusRun.HEARTS) System.out.println("CROSSED land " + number + " with " + run.hearts() + " hearts");
        }
    }

    /** Not a check: run with RUN_FLOCK set in the environment to get, for the lands that have fliers, the number each is made from and the way its fliers are spread. */
    @Test public void findsTheNumbersAndTheFlocksOfTheLandsWithFliers() {
        if (System.getenv("RUN_FLOCK") == null) return;
        StringBuilder found = new StringBuilder();
        try {
            for (int number = RunLevels.FLIERS_FROM; number <= RunLevels.TOTAL; number++) {
                long now = number > RunLevels.COUNT ? RunLevels.LATER_SEEDS[number - RunLevels.COUNT - 1] : RunLevels.SEEDS[number - 1], chosen = -1;
                int way = -1;
                for (int seed = 0; seed <= (number > RunLevels.COUNT ? 24 : 120) && chosen < 0; seed++) {
                    long with = seed == 0 ? now : seed;
                    for (int flock = 0; flock < (seed == 0 ? 12 : 3) && chosen < 0; flock++) {
                        RunLevels.flockTry = flock;
                        PlusRun run = cross(unguarded(RunLevels.level(number, with)), number > RunLevels.COUNT ? 400 : 240);
                        if (run.state() == PlusRun.WON && run.hearts() == PlusRun.HEARTS) {
                            chosen = with;
                            way = flock;
                        }
                    }
                }
                found.append(number).append(':').append(chosen).append('/').append(way).append(chosen == now && way == 0 ? "" : " *").append("\n");
            }
        } finally {
            RunLevels.flockTry = -1;
        }
        throw new AssertionError("FLOCK:\n" + found);
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
        for (int number : new int[]{2, 4, 5, 7, 8, 9, 14, 23, 24, 25, 27, 31, 36, 44, 47, 55, 58, 66, 69, 77}) {
            RunLevels.Level level = RunLevels.level(number);
            found.append("land ").append(number).append(": arena ").append(level.arena).append(" gate ").append(level.gate);
            found.append(" quest ").append(level.quest).append(" flags ").append(level.flags);
            for (int[] item : level.items) if (item[0] == PlusRun.CAKE || item[0] == PlusRun.DROPOFF) found.append(item[0] == PlusRun.CAKE ? " cake " : " dropoff ").append(item[1]).append("/").append(item[2]);
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
            boolean[] signs = new boolean[RunLevels.PARTY + 1];
            for (int[] foe : level.foes) {
                if (foe[0] >= RunLevels.SLIME && foe[0] <= RunLevels.POGO && !signs[foe[0]]) {
                    signs[foe[0]] = true;
                    found.append(" sign").append(foe[0]).append(" ").append(foe[1]);
                }
            }
            found.append("\n");
        }
        // And how a stretch of one of them lies, with what walks in it.
        String show = System.getenv("RUN_SHOW");
        if (show != null && show.equals("where")) {
            // Where, in each land, the things worth going to look at are.
            found.setLength(0);
            for (int number = 1; number <= RunLevels.TOTAL; number++) {
                RunLevels.Level level = RunLevels.level(number);
                found.append(number).append(" gate ").append(level.gate).append(" flags ").append(level.flags);
                for (int[] room : level.rooms) if (room[0] == RunLevels.ARENA) found.append(" arena door ").append(room[1]).append(" style ").append(number / 3 % 3);
                for (int[] room : level.rooms) if (room[0] <= RunLevels.PASSAGE) found.append(room[0] == RunLevels.STASH ? " stash " : " passage ").append(room[1]).append(room[7] == 1 ? "c" : room[7] == 2 ? "u" : "").append(" look ").append(room[13]).append(" holds ").append(room[14]).append(" wide ").append(room[4] - room[3] + 1);
                for (int[] made : level.made) found.append(" ").append(java.util.Arrays.toString(made));
                if (System.getenv("RUN_SHOW").equals("places")) continue;
                for (int[] foe : level.foes) {
                    if (RunLevels.rover(foe[0])) found.append(" rover ").append(foe[0]).append("@").append(foe[1]);
                    if (RunLevels.flier(foe[0])) found.append(" f").append(foe[0]).append("@").append(foe[1]);
                }
                found.append("\n");
            }
            throw new AssertionError("WHERE:\n" + found);
        }
        if (show != null) {
            String[] parts = show.split(",");
            RunLevels.Level level = RunLevels.level(Integer.parseInt(parts[0]));
            int first = Integer.parseInt(parts[1]), last = Math.min(level.columns, Integer.parseInt(parts[2]));
            // Or, asked for by a nought, the first arena there is in it.
            if (first == 0) for (int[] room : level.rooms) if (room[0] == RunLevels.ARENA && first == 0) {
                first = room[3] - 1;
                last = room[4] + 2;
            }
            found.setLength(0);
            for (int[] mover : level.movers) if (mover[0] >= first) found.append("mover ").append(mover[0] - first).append(" ").append(java.util.Arrays.toString(mover)).append("\n");
            for (int row = 0; row < level.land.length; row++) {
                StringBuilder line = new StringBuilder();
                for (int column = first; column < last; column++) {
                    char here = level.land[row][column];
                    for (int[] foe : level.foes) if (foe[1] == column && foe[2] == row) here = (char) ('a' + foe[0]);
                    for (int[] item : level.items) if (item[1] == column && item[2] == row) here = '*';
                    line.append(here == ' ' ? '.' : here);
                }
                found.append(String.format("%2d ", row)).append(line).append("\n");
            }
        }
        throw new AssertionError("COUNT:\n" + found);
    }

    @Test public void theLandsGrowAndEachHasEnoughInIt() {
        int before = 0;
        for (int number = 1; number <= RunLevels.COUNT; number++) {
            RunLevels.Level level = RunLevels.level(number);
            boolean shaped = true, waits = RunLevels.bossOf(number) >= 0;
            assertEquals(number, level.number);
            assertEquals((number - 1) / 6, level.chapter);
            assertTrue("land " + number + " has " + level.pluses.size() + " pluses", level.pluses.size() >= 15);
            // Two flags along the way, and one more at the door of whoever waits at the end.
            assertEquals(waits ? 3 : 2, level.flags.size());
            assertEquals(RunLevels.GATE, level.land[RunLevels.top(level, level.gate) - 1][level.gate]);
            assertTrue("land " + number + " is " + level.main, level.main > 60 && level.main < (shaped ? 600 : 400));
            // Made from the number kept for it, it is the same land every time.
            assertEquals(level.columns, RunLevels.level(number).columns);
            // Something to fight with by each flag, and from the second land on by where she starts; each lies on the ground.
            // From the twenty-third there is also a book or a brush, and an ink gun, each before what it is good against.
            // (And, with the creatures there are now, one more for every forty squares or so.)
            int laid = (number == 1 ? 2 : waits ? 4 : 3) + Math.min(2, level.chapter);
            assertTrue("land " + number + " has " + level.weapons.size(), level.weapons.size() >= laid && level.weapons.size() <= laid + level.gate / 40);
            // Chikuwa waits with bread from the third land and Rune with her ink from the ninth; ice cream and the rest from the fourth.
            int bread = 0, ink = 0, loose = 0, villagers = 0, stalls = 0, inside = 0;
            for (int[] item : level.items) {
                int top = RunLevels.top(level, item[1]);
                assertTrue("land " + number + " column " + item[1], item[1] >= level.main || top >= 0 && item[2] < top);
                assertFalse(PlusRun.solid(level.land[item[2]][item[1]]));
                if (item[0] == PlusRun.STALL) stalls++;
                else if (item[0] == PlusRun.DROPOFF) assertEquals(RunLevels.LETTER, level.quest);
                else if (item[3] == 1) bread++;
                else if (item[3] == 2) ink++;
                else if (item[1] >= level.main && (item[3] >= RunLevels.SOMI || item[0] == PlusRun.SPOT)) inside++;
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
            assertTrue("land " + number + " is flat", heights.size() >= 2);
            if (number % 6 == 0 && number < RunLevels.COUNT) {
                assertTrue("land " + number + " is " + level.main + " long, after " + before, level.main > before - 30);
                before = level.main;
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

    @Test public void aClipFillsAScreenNearlyItsShapeAndIsShownWholeOnAnother() {
        // An upright clip on an upright phone a little taller than it: it fills the height, and loses a little at its sides.
        assertEquals(1960 / 1280f, RunVideo.scale(720, 1280, 864, 1960), 1e-4f);
        // On a screen of its own shape nothing is lost either way.
        assertEquals(1.5f, RunVideo.scale(720, 1280, 1080, 1920), 1e-4f);
        // One wider than tall on an upright phone is shown whole, with black above and below; and so is an upright one on a screen lying down.
        assertEquals(864 / 1280f, RunVideo.scale(1280, 720, 864, 1960), 1e-4f);
        assertEquals(864 / 1280f, RunVideo.scale(720, 1280, 1960, 864), 1e-4f);
        // Wider than tall on a screen lying down, a little wider than it: it fills.
        assertEquals(1960 / 1280f, RunVideo.scale(1280, 720, 1960, 864), 1e-4f);
    }
}
