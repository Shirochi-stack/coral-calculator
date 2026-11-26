package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.Random;

public class MoreGamesTest {
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
