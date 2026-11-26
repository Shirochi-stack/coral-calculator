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

    @Test public void zeroTakesThreeLandingsAndTheGateStaysShutUntilThen() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.ZERO, 30, FLOOR - 1});
        level.guarded = true;
        level.gate = 50;
        level.land[FLOOR - 1][25] = RunLevels.BLOCK;
        level.land[FLOOR - 1][36] = RunLevels.BLOCK;
        PlusRun run = new PlusRun(level);
        assertFalse(run.gateOpen());
        // The shut gate stops her.
        run.place(48, GROUND);
        hold(run, 1, false, true, false);
        assertEquals(PlusRun.PLAYING, run.state());
        assertTrue(run.x() + PlusRun.WIDE <= 50);
        for (int blow = 1; blow <= PlusRun.ZERO_HITS; blow++) {
            // Wait for him to shake it off, then come down on him from above.
            hold(run, 1.3f, false, false, false);
            PlusRun.Foe zero = run.foes().get(0);
            run.place(zero.x + .6f, zero.y - PlusRun.TALL - .3f);
            for (int step = 0; step < 120 && run.blows() < blow; step++) run.step(STEP, false, false, false);
            assertEquals(blow, run.blows());
            run.place(48, GROUND);
        }
        assertEquals(PlusRun.HEARTS, run.hearts());
        assertTrue(run.foes().get(0).flat);
        assertTrue(run.gateOpen());
        hold(run, 1, false, true, false);
        assertEquals(PlusRun.WON, run.state());
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
        run.place(run.foes().get(0).x - 1.2f - PlusRun.WIDE, GROUND);
        run.step(STEP, false, false, false, true);
        assertTrue(run.swinging() >= 0);
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

    @Test public void nothingThrownGetsThroughZeroButASwordDoes() {
        RunLevels.Level level = armed(PlusRun.PEN);
        level.foes.add(new int[]{RunLevels.ZERO, 30, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .5f, false, true, false);
        run.place(run.foes().get(0).x - 3, GROUND);
        run.step(STEP, false, false, false, true);
        hold(run, .5f, false, false, false);
        assertEquals(0, run.blows());
        assertTrue(run.shots().isEmpty());

        level = armed(PlusRun.SWORD);
        level.foes.add(new int[]{RunLevels.ZERO, 30, FLOOR - 1});
        PlusRun other = new PlusRun(level);
        hold(other, .5f, false, true, false);
        other.place(other.foes().get(0).x - .6f - PlusRun.WIDE, GROUND);
        other.step(STEP, false, false, false, true);
        assertEquals(1, other.blows());
        assertEquals(PlusRun.HEARTS, other.hearts());
    }

    @Test public void zeroLobsABookAtHerWhenSheKeepsHerDistance() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.ZERO, 30, FLOOR - 1});
        level.land[FLOOR - 1][24] = RunLevels.BLOCK;
        level.land[FLOOR - 1][36] = RunLevels.BLOCK;
        PlusRun run = new PlusRun(level);
        run.place(22, GROUND);
        for (int step = 0; step < 600 && run.hurts() == 0; step++) run.step(STEP, false, false, false);
        assertTrue(run.thrown() >= 1);
        assertEquals(1, run.hurts());
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

    @Test public void illyasHeartsTurnTheArrowsRoundAndWhatIsThrownDealsWithHer() {
        RunLevels.Level level = armed(PlusRun.PEN);
        level.foes.add(new int[]{RunLevels.ILLYA, 30, FLOOR - 1});
        level.guarded = true;
        level.gate = 50;
        PlusRun run = new PlusRun(level);
        assertFalse(run.gateOpen());
        hold(run, .5f, false, true, false);
        run.place(22, GROUND);
        for (int step = 0; step < 600 && run.charms() == 0; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.charms());
        assertEquals(PlusRun.HEARTS, run.hearts());
        assertTrue(run.charmed() > 0);
        float before = run.x();
        hold(run, .5f, false, true, false);
        assertTrue("went " + (run.x() - before), run.x() < before - 1);
        hold(run, PlusRun.CHARM, false, false, false);
        // Three pens, each thrown when she has come down low enough to be hit; after each she is somewhere else.
        for (int blow = 1; blow <= PlusRun.ZERO_HITS; blow++) {
            for (int step = 0; step < 1200 && run.blows() < blow; step++) {
                PlusRun.Foe illya = run.foes().get(0);
                run.place(24, GROUND);
                boolean low = illya.dazed <= 0 && illya.y + illya.tall() > FLOOR - .5f;
                // Her hearts are kept out of it: this is about what is thrown at her.
                run.shots().removeIf(shot -> !shot.hers);
                run.step(STEP, illya.x < run.x(), illya.x > run.x(), false, low && step % 30 == 0);
            }
            assertEquals(blow, run.blows());
            if (blow == 1) {
                // Shaken off, she is on the other side of whoever hit her, and keeps her distance from there.
                boolean wasRight = run.foes().get(0).x > run.x();
                hold(run, 1.3f, false, false, false);
                run.shots().removeIf(shot -> !shot.hers);
                assertTrue(wasRight != run.foes().get(0).x > run.x());
                assertTrue(Math.abs(run.foes().get(0).x - run.x()) < 6);
            }
        }
        assertTrue(run.foes().get(0).flat);
        assertTrue(run.gateOpen());
    }

    @Test public void tsubakiehSwingsHerBatSendsBackWhatIsThrownAndCanBeLandedOn() {
        RunLevels.Level level = armed(PlusRun.PEN);
        level.foes.add(new int[]{RunLevels.TSUBAKIEH, 14, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .5f, false, true, false);
        // A pen comes straight back.
        run.step(STEP, false, false, false, true);
        for (int step = 0; step < 120 && run.turned() == 0; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.turned());
        assertEquals(0, run.blows());
        // Standing in front of her gets the bat.
        PlusRun other = new PlusRun(level);
        for (int step = 0; step < 900 && other.hurts() == 0; step++) other.step(STEP, false, false, false);
        assertTrue(other.swings() >= 1);
        assertEquals(1, other.hurts());
        // Coming down on her counts, three times over.
        PlusRun third = new PlusRun(level);
        for (int blow = 1; blow <= PlusRun.ZERO_HITS; blow++) {
            hold(third, 1.3f, false, false, false);
            PlusRun.Foe she = third.foes().get(0);
            third.place(she.x + .1f, she.y - PlusRun.TALL - .3f);
            for (int step = 0; step < 120 && third.blows() < blow; step++) third.step(STEP, false, false, false);
            assertEquals(blow, third.blows());
            third.place(40, GROUND);
        }
        assertTrue(third.foes().get(0).flat);
    }

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

    /** One of them on flat ground with a gate she keeps shut, and Halgakos some way before her. */
    private static PlusRun before(int who, int weapon) {
        RunLevels.Level level = weapon >= 0 ? armed(weapon) : flat(60);
        level.foes.add(new int[]{who, 30, FLOOR - 1});
        level.guarded = true;
        level.gate = 50;
        PlusRun.Perks hardy = new PlusRun.Perks();
        hardy.hearts = 9;
        PlusRun run = new PlusRun(level, hardy);
        if (weapon >= 0) hold(run, .5f, false, true, false);
        run.place(24, GROUND);
        return run;
    }

    /** Comes down on one of them, keeping what is thrown out of it, and says whether that counted. */
    private static boolean landOn(PlusRun run, PlusRun.Foe foe) {
        int blows = run.blows();
        for (int step = 0; step < 60 && run.blows() == blows; step++) {
            if (step % 20 == 0) run.place(foe.x + foe.wide() / 2 - PlusRun.WIDE / 2, foe.y - PlusRun.TALL - .3f);
            run.shots().clear();
            run.step(STEP, false, false, false);
        }
        run.place(5, GROUND);
        return run.blows() > blows;
    }

    @Test public void gokhaeCanOnlyBeHurtWhileHerFanIsDown() {
        PlusRun run = before(RunLevels.GOKHAE, PlusRun.PEN);
        PlusRun.Foe gokhae = run.foes().get(0);
        assertFalse(run.gateOpen());
        assertEquals(0, gokhae.mode);
        run.place(26, GROUND);
        run.step(STEP, false, false, false, true);
        hold(run, .6f, false, false, false);
        assertEquals(0, run.blows());
        assertTrue(run.parried() >= 1);
        assertFalse(landOn(run, gokhae));
        for (int blow = 1; blow <= PlusRun.ZERO_HITS; blow++) {
            run.place(24, GROUND);
            // Once she has shaken off the last one, and has her fan down again.
            for (int step = 0; step < 900 && (gokhae.mode != 1 || gokhae.dazed > 0); step++) {
                run.shots().clear();
                run.step(STEP, false, false, false);
            }
            assertEquals(1, gokhae.mode);
            for (int step = 0; step < 36; step++) {
                run.shots().clear();
                run.step(STEP, false, false, false);
            }
            // With it down she says her twisted things, and can be reached.
            assertTrue(run.thrown() >= blow);
            assertTrue("blow " + blow + " mode " + gokhae.mode + " dazed " + gokhae.dazed + " act " + gokhae.act + " parried " + run.parried(), landOn(run, gokhae));
        }
        assertTrue(gokhae.flat);
        assertTrue(run.gateOpen());
    }

    @Test public void theTwinsThrowAheadOfHerAndCoverWhatSheSeesAndBothMustGo() {
        // Running, what Miri throws is thrown at where she will be, not where she is.
        PlusRun alone = before(RunLevels.MIRI, -1);
        alone.foes().get(0).y -= 3;
        alone.place(14, GROUND);
        for (int step = 0; step < 200 && alone.thrown() == 0; step++) alone.step(STEP, false, true, false);
        assertEquals(1, alone.thrown());
        PlusRun.Shot scroll = alone.shots().get(0);
        float straight = (alone.x() + PlusRun.WIDE / 2 - scroll.x) / (alone.y() + PlusRun.TALL / 2 - scroll.y), thrown = scroll.across / scroll.down;
        assertTrue("thrown " + thrown + " straight " + straight, Math.abs(thrown - straight) > .05f);

        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MIRI, 34, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.GARI, 30, FLOOR - 1});
        level.guarded = true;
        level.gate = 50;
        PlusRun.Perks hardy = new PlusRun.Perks();
        hardy.hearts = 9;
        PlusRun run = new PlusRun(level, hardy);
        PlusRun.Foe miri = run.foes().get(0), gari = run.foes().get(1);
        run.place(22, GROUND);
        for (int step = 0; step < 400 && run.censors() == 0; step++) {
            run.shots().clear();
            run.place(gari.x - 6, GROUND);
            run.step(STEP, false, false, false);
        }
        assertEquals(1, run.censors());
        assertTrue(run.censored() > 2);
        assertTrue(landOn(run, miri));
        hold(run, 1.3f, false, false, false);
        assertTrue(landOn(run, miri));
        assertTrue(miri.flat);
        assertFalse(run.gateOpen());
        assertTrue(landOn(run, gari));
        hold(run, 1.3f, false, false, false);
        assertTrue(landOn(run, gari));
        assertTrue(run.gateOpen());
    }

    @Test public void yeobaekStepsOutBehindHerCutsAndDozesOff() {
        PlusRun run = before(RunLevels.YEOBAEK, -1);
        PlusRun.Foe yeobaek = run.foes().get(0);
        // Between the lines she cannot be touched or hurt.
        run.place(yeobaek.x, GROUND);
        hold(run, .5f, false, false, false);
        assertEquals(0, run.hurts());
        assertFalse(landOn(run, yeobaek));
        run.place(24, GROUND);
        for (int step = 0; step < 300 && yeobaek.mode != 1; step++) run.step(STEP, false, false, false);
        assertEquals(1, yeobaek.mode);
        assertTrue(yeobaek.x < run.x() && run.x() - yeobaek.x < 2.2f);
        for (int step = 0; step < 300 && yeobaek.mode != 3; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.swings());
        assertEquals(1, run.hurts());
        assertEquals(PlusRun.SLASHED, run.cause());
        // Dozing, she can be.
        assertTrue(landOn(run, yeobaek));
    }

    @Test public void kingSlimeShakesTheGroundAndLosesAChunkWithEveryBlow() {
        PlusRun run = before(RunLevels.KING, -1);
        PlusRun.Foe king = run.foes().get(0);
        // Standing near where he comes down, without touching him, she is thrown by it; in the air she is not.
        for (int step = 0; step < 400 && run.quakes() == 0; step++) {
            run.place(king.x - 1.6f, GROUND);
            run.step(STEP, false, false, false);
        }
        assertEquals(1, run.quakes());
        assertEquals(1, run.hurts());
        assertEquals(PlusRun.QUAKED, run.cause());
        for (int step = 0; step < 400 && run.quakes() == 1; step++) {
            run.place(king.x - 1.6f, GROUND - 1);
            run.step(STEP, false, false, false);
        }
        assertEquals(2, run.quakes());
        assertEquals(1, run.hurts());
        float wide = king.wide();
        assertTrue(landOn(run, king));
        assertTrue(king.wide() < wide * .8f);
        assertEquals(king.ground + king.tall(), FLOOR, .01f);
        hold(run, 1.3f, false, false, false);
        assertTrue(landOn(run, king));
        hold(run, 1.3f, false, false, false);
        assertTrue(landOn(run, king));
        assertTrue(king.flat);
    }

    @Test public void theMawangSendsFeathersSwoopsAndHasToCatchHerBreath() {
        PlusRun run = before(RunLevels.MAWANG, -1);
        PlusRun.Foe mawang = run.foes().get(0);
        boolean swooped = false;
        int feathers = 0;
        for (int step = 0; step < 900 && mawang.mode != 2; step++) {
            feathers = Math.max(feathers, run.shots().size());
            if (step % 3 == 0) run.shots().clear();
            run.place(24, GROUND);
            run.step(STEP, false, false, false);
            swooped |= mawang.mode == 1;
            // On the wing nothing reaches her.
            if (step == 30) assertFalse(run.open(mawang));
        }
        assertEquals(3, feathers);
        assertTrue(swooped);
        assertEquals(2, mawang.mode);
        assertTrue(landOn(run, mawang));
    }

    // The lands of the second volume

    @Test public void theLandsOfTheSecondVolumeAreLongerAndHaveWhatTheirChaptersAreMadeOf() {
        boolean[] ground = new boolean[128], signs = new boolean[20], ribbons = new boolean[PlusRun.WHITE + 1];
        int movers = 0, doors = 0, first = 0, total = 0;
        for (int number = 1; number <= RunLevels.COUNT; number++) first += RunLevels.level(number).columns;
        for (int number = RunLevels.COUNT + 1; number <= RunLevels.TOTAL; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int boss = RunLevels.bossOf(number);
            assertEquals(5 + (number - 31) / 10, level.chapter);
            assertEquals("land " + number, number % 10 == 0, boss >= 0);
            assertEquals("land " + number, boss >= 0 ? 4 : 3, level.flags.size());
            assertEquals(boss >= 0, level.guarded);
            assertEquals(boss >= 0 ? 5 : 4, level.weapons.size());
            assertEquals(level.columns, RunLevels.level(number).columns);
            total += level.columns;
            int friends = 0;
            for (int[] item : level.items) {
                if (item[3] != 0) friends++;
                if (item[0] > PlusRun.FORM) ribbons[item[0] - PlusRun.FORM] = true;
                assertEquals("land " + number + " column " + item[1], RunLevels.BLOCK, level.land[FLOOR][item[1]]);
            }
            assertEquals("land " + number, boss == RunLevels.MAWANG ? 5 : 3, friends);
            for (char[] row : level.land) for (char square : row) ground[square] = true;
            for (int[] foe : level.foes) signs[foe[0]] = true;
            movers += level.movers.size();
            doors += level.doors.size();
        }
        // About twice as long as those of the first volume, on the whole.
        assertTrue("mean " + total / 50 + " against " + first / 30, total / 50 > first / 30 * 1.8f);
        for (char square : new char[]{RunLevels.THIN, RunLevels.CRUMBLE, RunLevels.BELT_RIGHT, RunLevels.BELT_LEFT, RunLevels.SLICK, RunLevels.DRAUGHT,
                RunLevels.DOOR, RunLevels.LAVA, RunLevels.WATER}) {
            assertTrue("no " + square, ground[square]);
        }
        assertTrue(movers > 5 && doors > 5);
        for (int kind = RunLevels.THROWER; kind <= RunLevels.MAWANG; kind++) {
            if (kind != RunLevels.ILLYA && kind != RunLevels.TSUBAKIEH) assertTrue("no sign " + kind, signs[kind]);
        }
        for (int form = PlusRun.RED; form <= PlusRun.WHITE; form++) assertTrue("no ribbon " + form, ribbons[form]);
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
        assertEquals(95, lines);
        assertEquals(RunLevels.CHAPTERS, RunStory.CHAPTERS.length);
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
    private static float worth(PlusRun run, int plan, int further) {
        PlusRun trial = run.copy();
        int hurts = trial.hurts(), blows = trial.blows();
        int step = 0;
        for (; step < length(plan) && trial.state() == PlusRun.PLAYING; step++) act(trial, plan, step);
        for (; step < 200 && !trial.onGround() && trial.state() == PlusRun.PLAYING; step++) act(trial, plan, step);
        if (trial.hurts() > hurts) return -1000;
        float here = (trial.blows() - blows) * 6 + (trial.state() == PlusRun.WON ? 50 : 0) - (plan == WAIT ? .4f : plan == BACK ? 3 : 0);
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

    @Test public void theLandsGrowAndEachHasEnoughInIt() {
        int before = 0;
        for (int number = 1; number <= RunLevels.COUNT; number++) {
            RunLevels.Level level = RunLevels.level(number);
            assertEquals(number, level.number);
            assertEquals((number - 1) / 6, level.chapter);
            assertTrue("land " + number + " has " + level.pluses.size() + " pluses", level.pluses.size() >= 15);
            // Two flags along the way, and one more at the door of whoever waits at the end.
            assertEquals(RunLevels.bossOf(number) >= 0 ? 3 : 2, level.flags.size());
            assertEquals(RunLevels.GATE, level.land[FLOOR - 1][level.gate]);
            assertTrue(level.columns > 60 && level.columns < 400);
            // Made from the number kept for it, it is the same land every time.
            assertEquals(level.columns, RunLevels.level(number).columns);
            // Something to fight with by each flag, and from the second land on by where she starts; each lies on the ground.
            assertEquals("land " + number, number == 1 ? 2 : RunLevels.bossOf(number) >= 0 ? 4 : 3, level.weapons.size());
            // Chikuwa waits with bread from the third land and Rune with her ink from the ninth; ice cream and the rest from the fourth.
            int bread = 0, ink = 0, loose = 0;
            for (int[] item : level.items) {
                assertEquals("land " + number + " column " + item[1], RunLevels.BLOCK, level.land[FLOOR][item[1]]);
                assertFalse(PlusRun.solid(level.land[FLOOR - 1][item[1]]));
                if (item[3] == 1) bread++;
                else if (item[3] == 2) ink++;
                else loose++;
            }
            assertEquals("land " + number, number >= 3 ? 1 : 0, bread);
            assertEquals("land " + number, number >= 9 ? 1 : 0, ink);
            if (number >= 4) assertTrue("land " + number + " has nothing lying about to help", loose >= 1);
            assertEquals("land " + number, RunLevels.bossOf(number) >= 0, level.guarded);
            for (int[] weapon : level.weapons) {
                assertTrue(weapon[0] >= PlusRun.PEN && weapon[0] <= PlusRun.GUN);
                assertEquals("land " + number + " column " + weapon[1], RunLevels.BLOCK, level.land[FLOOR][weapon[1]]);
                assertFalse(PlusRun.solid(level.land[FLOOR - 1][weapon[1]]));
            }
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
