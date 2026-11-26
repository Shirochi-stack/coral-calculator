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
        // It does not slow her, and what she comes down on is still dealt with.
        run.place(30, GROUND);
        plain.place(30, GROUND);
        hold(run, 1, false, true, false);
        hold(plain, 1, false, true, false);
        assertEquals(plain.x(), run.x(), .01f);
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
            for (int step = 0; step < 240 && stomper.flattened() == 0 && stomper.hurts() == 0 && big.left == 2; step++) {
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

    @Test public void everyLandHasFiveTimesTheCreaturesItWasLaidWithAndTheFirstThreeTwice() {
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int laid = 0, more = 0, packs = 0, was = -9, wasRow = -9;
            java.util.Set<Integer> kinds = new java.util.TreeSet<>();
            for (int[] foe : level.foes) {
                if (foe[1] >= level.main || RunLevels.boss(foe[0]) || RunLevels.flier(foe[0]) || foe[0] == RunLevels.TARGET) continue;
                if (!RunLevels.crowd(foe)) {
                    laid++;
                    kinds.add(foe[0]);
                }
            }
            for (int[] foe : level.foes) {
                if (!RunLevels.crowd(foe) || RunLevels.flier(foe[0])) continue;
                more++;
                // Of a kind the land has already; standing on its ground or on a ledge, with room over it; clear of the start, of flags and of where the land ends.
                assertTrue("land " + number + " kind " + foe[0], kinds.contains(foe[0]) || foe[0] == RunLevels.MINUS);
                char under = level.land[foe[2] + 1][foe[1]];
                assertTrue("land " + number + " at " + foe[1], under == RunLevels.BLOCK || under == RunLevels.LEDGE);
                assertEquals("land " + number + " at " + foe[1], RunLevels.AIR, level.land[foe[2]][foe[1]]);
                assertTrue("land " + number + " at " + foe[1], foe[1] >= 18 && foe[1] < level.gate - 5);
                for (int flag : level.flags) assertTrue("land " + number + " at " + foe[1] + " by flag " + flag, Math.abs(flag - foe[1]) >= 6);
                for (int[] thing : level.decor) assertTrue("land " + number + " at " + foe[1] + " in a village", Math.abs(thing[1] - foe[1]) >= 10);
                if (foe[1] != was + 1 || foe[2] != wasRow) packs++;
                was = foe[1];
                wasRow = foe[2];
            }
            int times = RunLevels.crowdOf(number);
            assertEquals("land " + number, number <= 3 ? 2 : 5, times);
            // Five times what it was laid with, or twice: give or take a tenth, where there was not room for all of them.
            // (Where someone waits at the end the land is short, and there is room for fewer: twice over at the least.)
            assertTrue("land " + number + " laid " + laid + " more " + more, more <= laid * (times - 1)
                    && more >= (RunLevels.bossOf(number) >= 0 ? Math.min(laid, laid * (times - 1)) : laid * (times - 1) * 6 / 10));
            // In packs, spread along the land: not all in one place.
            if (more >= 8) assertTrue("land " + number + " packs " + packs + " of " + more, packs >= more / 4 && packs <= more);
        }
        // One of them keeps still while she is far off, and is about its business when she is near.
        RunLevels.Level level = flat(120);
        level.foes.add(new int[]{RunLevels.MINUS, 90, FLOOR - 1, 1, 1});
        level.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1, 1, 1});
        PlusRun run = new PlusRun(level);
        PlusRun.Foe far = run.foes().get(0), near = run.foes().get(1);
        float farAt = far.x, nearAt = near.x;
        hold(run, 2, false, false, false);
        assertEquals(farAt, far.x, 0);
        assertTrue(Math.abs(near.x - nearAt) > 1);
        run.place(80, GROUND);
        hold(run, 1, false, false, false);
        assertTrue(Math.abs(far.x - farAt) > .5f);
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
        touch(run, guard);
        touch(run, guard);
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
        PlusRun late = shutIn(RunLevels.GUARD_LAND, true);
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
        assertEquals(PlusRun.strength(RunLevels.GUARD) + 2, Math.round(last.shieldMost));
        assertTrue(late.opens(last) < 2 && late.forms(last) < 1.5f);
    }

    @Test public void aPetWaitsInEveryLandFromTheThirdAndKeepsBesideHerForTheWholeOfIt() {
        // Where they wait: none in the first two lands; from the third one in every land, on its ground, past its first flag; no two lands running the same kind; and every kind.
        java.util.Set<Integer> kinds = new java.util.TreeSet<>();
        int last = -1;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int waiting = 0;
            for (int[] item : level.items) {
                if (item[0] < PlusRun.PET || item[0] >= PlusRun.PET + PlusRun.PETS) continue;
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
        assertEquals(PlusRun.PETS, kinds.size());

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
        for (int step = 0; step < 60 * 4 && tough.left == 3; step++) {
            tough.x = tough.home;
            run.step(STEP, false, false, false);
        }
        assertEquals(2, tough.left);
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
        assertEquals(9, run.foes().get(0).left);
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
            for (int step = 0; step < 60 * 30; step++) {
                fight.place(guard.x - 1.05f, floorOf(fight) - PlusRun.TALL - 1.8f);
                fight.shots().removeIf(shot -> !shot.hers);
                fight.step(STEP, false, false, false);
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
            assertTrue("land " + land + " chipped " + chipped, chipped >= run.shatters() * 2);
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
            assertEquals("land " + land, PlusRun.HEARTS - 1, run.hearts());
            assertEquals("land " + land, PlusRun.LIVES, run.lives());
            assertTrue("land " + land, run.locked());
            assertTrue("land " + land, run.x() > level.arena);
            assertFalse("land " + land, foe.away);
            assertEquals("land " + land, left, foe.left);
            assertEquals("land " + land, 1, run.phase(foe));
            assertEquals(1, run.entrances());
            // Twice more, and her hearts are gone: that is a life.
            while (run.livesLost() == 0 && run.hurts() < 9) touched(run);
            assertEquals("land " + land, PlusRun.HEARTS, run.hurts());
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
        // It is a way of its own, three screens long: spikes to cross on ledges, signs walking in it, something worth having up out of the
        // way, and a chest by its far door.
        RunLevels.Level level = run.level();
        assertTrue(one[4] - one[3] >= 40);
        int spikes = 0, walkers = 0, ribbons = 0, chests = 0;
        for (int column = one[3]; column <= one[4]; column++) if (level.land[FLOOR - 1][column] == RunLevels.SPIKE) spikes++;
        for (int[] foe : level.foes) if (foe[1] >= one[3] && foe[1] <= one[4]) walkers++;
        for (int[] item : level.items) {
            if (item[1] < one[3] || item[1] > one[4]) continue;
            if (item[0] > PlusRun.FORM && item[0] < PlusRun.CHEST) ribbons++;
            if (item[0] == PlusRun.CHEST) chests++;
        }
        assertEquals(level.chapter >= 1 ? 15 : 0, spikes);
        assertEquals(level.chapter >= 2 ? 3 : 2, walkers);
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
                        if (foe.size > 1.5f && foe.left == 6) {
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
        assertEquals(PlusRun.RANK_A, PlusRun.rank(PlusRun.PAR + 1, 0, 0));
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
        boolean[] kinds = new boolean[RunLevels.TRAMPLE + 1];
        int raised = 0, passages = 0, dens = 0;
        int hidden = 0;
        assertTrue(RunLevels.level(1).rooms.isEmpty());
        for (int number = RunLevels.PLACES_FROM; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            // A door in its village from the second land; from the second chapter, in two lands of three, another by its first crowd.
            assertTrue("land " + number + " has " + level.rooms.size(), level.rooms.size() >= (level.chapter >= 1 && number % 3 != 1 || level.chapter >= 2 ? 2 : 1));
            assertTrue(level.main > level.gate && level.main < level.columns);
            for (int[] one : level.rooms) {
                kinds[one[0]] = true;
                if (one[7] != 0) hidden++;
                // Its door is in the land proper, on the ground; the place itself is beyond the end of it.
                assertTrue("land " + number, one[1] < level.gate && one[3] >= level.main && one[4] < level.columns - 1);
                assertEquals("land " + number, RunLevels.ENTRY, level.land[one[2] - 1][one[1]]);
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
                    assertEquals(RunLevels.AIR, level.land[FLOOR - RunLevels.ROOM_TALL][column]);
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
            // They are places to move in: a stash and the rooms for games twenty squares and more across, a cellar wider, a passage twice that.
            for (int[] one : level.rooms) {
                int wide = one[4] - one[3] + 1;
                assertTrue("land " + number + " kind " + one[0] + " is " + wide, wide >= (one[0] == RunLevels.PASSAGE ? 44 : one[0] == RunLevels.ARENA ? 100 : 30));
                // A stash has a chest, which is not on its floor; a passage has one too.
                int chests = 0;
                for (int[] item : level.items) {
                    if (item[0] != PlusRun.CHEST || item[1] < one[3] || item[1] > one[4]) continue;
                    chests++;
                    if (one[0] == RunLevels.STASH) assertTrue("land " + number, item[2] <= FLOOR - 9);
                }
                assertEquals("land " + number, one[0] == RunLevels.STASH || one[0] == RunLevels.PASSAGE || one[0] == RunLevels.ARENA ? 1 : 0, chests);
            }
            // No sign of the land is in them but those that walk in its passages, and nothing of the land's to fight with.
            for (int[] foe : level.foes) {
                boolean passing = false;
                for (int[] one : level.rooms) passing |= one[0] == RunLevels.PASSAGE && foe[1] >= one[3] && foe[1] <= one[4];
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
        assertEquals(10, met[RunLevels.WYRM]);
        assertEquals(10, met[RunLevels.BLOT]);
        assertEquals(10, met[RunLevels.GECKO]);
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
                assertEquals(2, slime.left);
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
            assertTrue("land " + number + " more " + more, more <= RunLevels.fliersIn(number) * 2 * 4);
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
            assertEquals("land " + number, RunLevels.bossOf(number) >= 0, prize != RunLevels.NOTHING);
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
        assertEquals(bonus + hit * 2 + 10, run.bonus());
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
        assertEquals(bonus + PlusRun.RANK_PAYS[run.ranked()] + 20, run.bonus());
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
        boolean[] ground = new boolean[128], signs = new boolean[RunLevels.RIME + 1], ribbons = new boolean[PlusRun.WHITE + 1], met = new boolean[RunLevels.NURI + 1];
        int movers = 0, doors = 0, first = 0, total = 0, heights = 0, crowd = 0;
        for (int number = 1; number <= RunLevels.COUNT; number++) first += RunLevels.level(number).columns;
        for (int number = RunLevels.COUNT + 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int boss = RunLevels.bossOf(number);
            assertEquals(5 + (number - 31) / 10, level.chapter);
            assertEquals("land " + number, number % 5 == 0, boss >= 0);
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
        assertEquals(95 + 86 + 37 + 7 + 11 + 4 + 40 + 19 + 3 + 3 + 3 + 3 + 5 + 7 + 1 + 1 + 2 + 1 + 5, lines);
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
            assertEquals("land " + land, RunLevels.bossOf(land) >= 0, RunStory.outro(land) != null);
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
            // (Nor those put in to make up a land's numbers: what is proved is that the land itself can be crossed.)
            if (RunLevels.boss(level.foes.get(index)[0]) || RunLevels.crowd(level.foes.get(index))) level.foes.remove(index);
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
            assertTrue("land " + number + " is " + level.columns, level.columns > 60 && level.columns < (shaped ? 600 : 400));
            // Made from the number kept for it, it is the same land every time.
            assertEquals(level.columns, RunLevels.level(number).columns);
            // Something to fight with by each flag, and from the second land on by where she starts; each lies on the ground.
            // From the twenty-third there is also a book or a brush, and an ink gun, each before what it is good against.
            assertEquals("land " + number, (number == 1 ? 2 : waits ? 4 : 3) + Math.min(2, level.chapter), level.weapons.size());
            // Chikuwa waits with bread from the third land and Rune with her ink from the ninth; ice cream and the rest from the fourth.
            int bread = 0, ink = 0, loose = 0, villagers = 0, stalls = 0;
            for (int[] item : level.items) {
                int top = RunLevels.top(level, item[1]);
                assertTrue("land " + number + " column " + item[1], item[1] >= level.main || top >= 0 && item[2] < top);
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
            assertTrue("land " + number + " is flat", heights.size() >= 2);
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
