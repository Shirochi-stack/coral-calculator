package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.Random;

public class MoreGamesTest {
    private static final float STEP = 1 / 60f;
    private static final float GROUND = PlusRun.FLOOR - PlusRun.TALL;

    private static void hold(PlusRun run, float seconds, boolean left, boolean right, boolean jump) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, left, right, jump);
    }

    // Plus run

    @Test public void sheStandsOnTheGroundWhereSheStarts() {
        PlusRun run = new PlusRun();
        hold(run, 1, false, false, false);
        assertTrue(run.onGround());
        assertEquals(GROUND, run.y(), .01f);
        assertEquals(2, run.x(), .01f);
        assertEquals(PlusRun.HEARTS, run.hearts());
    }

    @Test public void sheRunsTheWayTheArrowPoints() {
        PlusRun run = new PlusRun();
        hold(run, 1, false, true, false);
        assertTrue(run.x() > 6);
        assertFalse(run.facingLeft());
        hold(run, .5f, true, false, false);
        assertTrue(run.facingLeft());
    }

    @Test public void aWallStopsHer() {
        PlusRun run = new PlusRun();
        hold(run, 4, false, true, false);
        assertTrue(run.x() <= 12 - PlusRun.WIDE);
        assertTrue(run.x() > 11);
        assertEquals(GROUND, run.y(), .01f);
    }

    @Test public void aJumpGoesUpAndComesDown() {
        PlusRun run = new PlusRun();
        hold(run, .2f, false, false, false);
        float highest = run.y();
        for (int step = 0; step < 30; step++) {
            run.step(STEP, false, false, true);
            highest = Math.min(highest, run.y());
        }
        assertTrue("rose " + (GROUND - highest), GROUND - highest > 2.4f && GROUND - highest < 3.2f);
        hold(run, 1, false, false, false);
        assertTrue(run.onGround());
        assertEquals(GROUND, run.y(), .01f);
    }

    @Test public void lettingGoEarlyJumpsLower() {
        PlusRun run = new PlusRun();
        hold(run, .2f, false, false, false);
        run.step(STEP, false, false, true);
        float highest = run.y();
        for (int step = 0; step < 40; step++) {
            run.step(STEP, false, false, false);
            highest = Math.min(highest, run.y());
        }
        assertTrue(GROUND - highest < 1.5f);
    }

    @Test public void aPlusIsCollectedByTouchingIt() {
        PlusRun run = new PlusRun();
        int before = run.pluses().size();
        run.place(5, 10 - PlusRun.TALL + 1);
        hold(run, .3f, false, false, false);
        assertEquals(1, run.collected());
        assertEquals(before - 1, run.pluses().size());
    }

    @Test public void fallingIntoAGapCostsAHeartAndStartsHerAgain() {
        PlusRun run = new PlusRun();
        run.place(17, 6);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
        assertEquals(1, run.hurts());
        assertEquals(2, run.x(), .01f);
        assertTrue(run.safe() || run.onGround());
    }

    @Test public void theFlagIsWhereSheComesBackTo() {
        PlusRun run = new PlusRun();
        run.place(47.1f, GROUND);
        hold(run, .3f, false, false, false);
        run.place(52.2f, 6);
        hold(run, 3, false, false, false);
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
        assertEquals(47, (int) run.x());
    }

    @Test public void landingOnAMinusFlattensIt() {
        PlusRun run = new PlusRun();
        run.place(75.9f, 9);
        hold(run, 1, false, false, false);
        assertEquals(1, run.flattened());
        assertEquals(PlusRun.HEARTS, run.hearts());
    }

    @Test public void walkingIntoAMinusCostsAHeart() {
        PlusRun run = new PlusRun();
        run.place(74, GROUND);
        for (int step = 0; step < 120 && run.hurts() == 0; step++) run.step(STEP, false, true, false);
        assertEquals(1, run.hurts());
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
        assertEquals(0, run.flattened());
    }

    @Test public void threeHurtsEndTheRun() {
        PlusRun run = new PlusRun();
        for (int fall = 0; fall < PlusRun.HEARTS; fall++) {
            run.place(17, 6);
            hold(run, 3, false, false, false);
        }
        assertEquals(PlusRun.LOST, run.state());
        assertEquals(0, run.hearts());
    }

    @Test public void theGateEndsTheRun() {
        PlusRun run = new PlusRun();
        run.place(110.5f, GROUND);
        hold(run, 2, false, true, false);
        assertEquals(PlusRun.WON, run.state());
    }

    /**
     * A player with no skill at all: it runs right, and jumps when something is in the way,
     * when the ground ends, or when a minus is close. If it can reach the gate, the land can
     * be crossed.
     */
    @Test public void theLandCanBeCrossed() {
        PlusRun run = new PlusRun();
        float before = run.y();
        boolean jump = false;
        for (int step = 0; step < 60 * 180 && run.state() == PlusRun.PLAYING; step++) {
            boolean rising = run.y() < before - .0001f;
            before = run.y();
            boolean want = false;
            if (run.onGround()) {
                int feet = (int) Math.floor(run.y() + PlusRun.TALL + .01f), top = (int) Math.floor(run.y());
                int next = (int) Math.floor(run.x() + PlusRun.WIDE + .35f);
                for (int row = top; row < feet; row++) want |= PlusRun.solid(run.at(next, row));
                want |= !PlusRun.solid(run.at((int) Math.floor(run.x() + PlusRun.WIDE + .25f), feet));
                for (PlusRun.Minus minus : run.minuses()) {
                    float gap = minus.x - (run.x() + PlusRun.WIDE);
                    want |= !minus.flat && gap > -.2f && gap < 1.7f && Math.abs(minus.y - run.y()) < 2.5f;
                }
            }
            // The button is held while she rises, and let go as she falls, so it can be pressed again on landing.
            jump = run.onGround() ? want && !jump : rising;
            run.step(STEP, false, true, jump);
        }
        assertEquals("stopped at " + run.x() + " with " + run.hearts() + " hearts", PlusRun.WON, run.state());
        assertTrue(run.time() < 120);
    }

    @Test public void everyPlusCanBeReachedFromSomewhereSolid() {
        // A plus is within a jump of ground or a ledge beneath it: no more than three squares above something to stand on.
        PlusRun run = new PlusRun();
        for (int[] plus : run.pluses()) {
            boolean near = false;
            for (int across = -3; across <= 3 && !near; across++) {
                for (int down = 1; down <= 4 && !near; down++) near = PlusRun.solid(run.at(plus[0] + across, plus[1] + down));
            }
            assertTrue("plus at " + plus[0] + "," + plus[1], near);
        }
        assertTrue(run.total() >= 40);
    }

    // Last card

    @Test public void theCardsAreDealtSevenEach() {
        LastCard game = new LastCard(new Random(1));
        assertEquals(LastCard.HAND, game.hand(LastCard.PLAYER).size());
        assertEquals(LastCard.HAND, game.hand(LastCard.HALGAKOS).size());
        assertEquals(60, game.cards());
        assertTrue(LastCard.what(game.top()) <= 9);
        assertEquals(LastCard.sign(game.top()), game.wanted());
        assertEquals(LastCard.PLAYER, game.turn());
        assertEquals(LastCard.NOBODY, game.winner());
    }

    @Test public void onlyACardThatFitsGoesDown() {
        for (int seed = 0; seed < 50; seed++) {
            LastCard game = new LastCard(new Random(seed));
            List<Integer> hand = game.hand(LastCard.PLAYER);
            for (int place = 0; place < hand.size(); place++) {
                int card = hand.get(place);
                boolean fits = LastCard.isAny(card) || LastCard.sign(card) == LastCard.sign(game.top())
                        || LastCard.what(card) == LastCard.what(game.top());
                assertEquals(fits, game.playable(LastCard.PLAYER, place));
            }
            assertFalse(game.playable(LastCard.HALGAKOS, 0));
        }
    }

    /** Finds a deal in which the player holds a card of a kind that fits, and plays it. */
    private static LastCard afterPlaying(int kind) {
        for (int seed = 0; seed < 5000; seed++) {
            LastCard game = new LastCard(new Random(seed));
            List<Integer> hand = game.hand(LastCard.PLAYER);
            for (int place = 0; place < hand.size(); place++) {
                boolean right = kind <= 9 ? LastCard.what(hand.get(place)) <= 9 : LastCard.what(hand.get(place)) == kind;
                if (right && game.playable(LastCard.PLAYER, place)) {
                    assertTrue(game.play(LastCard.PLAYER, place, 2));
                    return game;
                }
            }
        }
        throw new AssertionError("no deal found");
    }

    @Test public void aNumberPassesTheTurn() {
        LastCard game = afterPlaying(0);
        assertEquals(LastCard.HALGAKOS, game.turn());
        assertEquals(LastCard.HAND - 1, game.hand(LastCard.PLAYER).size());
        assertEquals(60, game.cards());
    }

    @Test public void aSkipKeepsTheTurn() {
        LastCard game = afterPlaying(LastCard.SKIP);
        assertEquals(LastCard.PLAYER, game.turn());
        assertEquals(LastCard.HAND, game.hand(LastCard.HALGAKOS).size());
    }

    @Test public void aTakeTwoGivesTheOtherSideTwoCardsAndKeepsTheTurn() {
        LastCard game = afterPlaying(LastCard.TAKE_TWO);
        assertEquals(LastCard.PLAYER, game.turn());
        assertEquals(LastCard.HAND + 2, game.hand(LastCard.HALGAKOS).size());
        assertEquals(60, game.cards());
    }

    @Test public void anAnySignNamesTheSignToFollow() {
        LastCard game = afterPlaying(LastCard.ANY);
        assertEquals(2, game.wanted());
        assertEquals(LastCard.HALGAKOS, game.turn());
        assertTrue(game.fits(LastCard.card(2, 5)));
        assertFalse(game.fits(LastCard.card(1, 5)));
    }

    @Test public void drawingACardThatDoesNotFitPassesTheTurn() {
        boolean passed = false, kept = false;
        for (int seed = 0; seed < 400 && !(passed && kept); seed++) {
            LastCard game = new LastCard(new Random(seed));
            int card = game.draw(LastCard.PLAYER);
            assertNotEquals(LastCard.NONE, card);
            assertEquals(LastCard.HAND + 1, game.hand(LastCard.PLAYER).size());
            if (game.fits(card)) {
                // It may be put down, or kept: nothing else may be played, and no second card drawn.
                assertEquals(LastCard.PLAYER, game.turn());
                assertEquals(card, game.drawn());
                assertEquals(LastCard.NONE, game.draw(LastCard.PLAYER));
                game.pass();
                assertEquals(LastCard.HALGAKOS, game.turn());
                kept = true;
            } else {
                assertEquals(LastCard.HALGAKOS, game.turn());
                passed = true;
            }
        }
        assertTrue(passed && kept);
    }

    /** Halgakos plays both hands, by her own lights, until someone is out of cards. */
    @Test public void everyGameComesToAnEndWithNoCardLost() {
        for (int seed = 0; seed < 300; seed++) {
            LastCard game = new LastCard(new Random(seed));
            Random random = new Random(seed);
            int turns = 0;
            while (game.winner() == LastCard.NOBODY && turns++ < 4000) {
                int who = game.turn();
                List<Integer> hand = game.hand(who);
                int place = LastCard.NONE;
                if (who == LastCard.HALGAKOS) {
                    place = game.choice();
                } else {
                    for (int index = 0; index < hand.size() && place == LastCard.NONE; index++) {
                        if (game.playable(who, index)) place = index;
                    }
                }
                if (place != LastCard.NONE) {
                    assertTrue(game.play(who, place, who == LastCard.HALGAKOS ? game.named() : random.nextInt(LastCard.SIGNS)));
                } else if (who == LastCard.PLAYER && game.drawn() != LastCard.NONE) {
                    game.pass();
                } else {
                    game.draw(who);
                }
                assertEquals(60, game.cards());
            }
            assertNotEquals("seed " + seed, LastCard.NOBODY, game.winner());
            if (game.winner() != LastCard.TIE) assertTrue(game.hand(game.winner()).isEmpty() || game.left() == 0);
        }
    }

    @Test public void sheKeepsHerAnySignCardsForLast() {
        for (int seed = 0; seed < 300; seed++) {
            LastCard game = new LastCard(new Random(seed));
            // Give her the turn by the player drawing a card that does not fit.
            int card = game.draw(LastCard.PLAYER);
            if (game.fits(card)) game.pass();
            if (game.turn() != LastCard.HALGAKOS) continue;
            int place = game.choice();
            if (place == LastCard.NONE) continue;
            int chosen = game.hand(LastCard.HALGAKOS).get(place);
            assertTrue(game.fits(chosen));
            if (LastCard.isAny(chosen)) {
                for (int other : game.hand(LastCard.HALGAKOS)) assertTrue(LastCard.isAny(other) || !game.fits(other));
            }
        }
    }
}
