package com.coral.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Last card: a game of getting rid of one's cards, for the player and Halgakos. Each card has
 * one of a calculator's four signs and a number from 0 to 9. A card may be put down on the
 * last one if it has the same sign or the same number. Three kinds of card do more: a skip
 * and a take two, which come in each sign, and an any sign, which goes on anything and names
 * the sign to follow. Whoever puts down their last card wins.
 *
 * A card is a number: its sign times 16, plus what is on it.
 */
final class LastCard {
    static final int SIGNS = 4;
    /** What is on a card that is not a number from 0 to 9. */
    static final int SKIP = 10, TAKE_TWO = 11, ANY = 12;
    static final int PLAYER = 0, HALGAKOS = 1;
    /** Who has won: one of the two, {@link #TIE}, or {@link #NOBODY} while the game is on. */
    static final int NOBODY = -1, TIE = 2;
    static final int NONE = -1;
    static final int HAND = 7;
    /** The sign an any-sign card is filed under until one is named for it. */
    private static final int NO_SIGN = SIGNS;

    private final List<Integer> pile = new ArrayList<>();
    private final List<Integer> discards = new ArrayList<>();
    private final List<List<Integer>> hands = new ArrayList<>();
    private int top;
    /** The sign the next card must have, unless it matches what is on the top card. */
    private int wanted;
    private int turn = PLAYER;
    private int winner = NOBODY;
    /** The card the player has just drawn and may still put down, or {@link #NONE}. */
    private int drawn = NONE;
    /** Turns in a row on which no card could be drawn or put down. */
    private int stuck;

    LastCard(Random random) {
        for (int sign = 0; sign < SIGNS; sign++) {
            for (int number = 0; number <= 9; number++) pile.add(card(sign, number));
            for (int copy = 0; copy < 2; copy++) {
                pile.add(card(sign, SKIP));
                pile.add(card(sign, TAKE_TWO));
            }
            pile.add(card(NO_SIGN, ANY));
        }
        Collections.shuffle(pile, random);
        hands.add(new ArrayList<>());
        hands.add(new ArrayList<>());
        for (int dealt = 0; dealt < HAND; dealt++) {
            hands.get(PLAYER).add(pile.remove(pile.size() - 1));
            hands.get(HALGAKOS).add(pile.remove(pile.size() - 1));
        }
        // The first card turned up is a plain number: anything else goes back under the pile.
        while (what(pile.get(pile.size() - 1)) > 9) pile.add(0, pile.remove(pile.size() - 1));
        top = pile.remove(pile.size() - 1);
        wanted = sign(top);
    }

    static int card(int sign, int what) { return sign * 16 + what; }

    static int sign(int card) { return card >> 4; }

    /** What is on a card: a number from 0 to 9, {@link #SKIP}, {@link #TAKE_TWO} or {@link #ANY}. */
    static int what(int card) { return card & 15; }

    static boolean isAny(int card) { return what(card) == ANY; }

    /** The cards a side holds, in the order they came. Not to be changed. */
    List<Integer> hand(int who) { return Collections.unmodifiableList(hands.get(who)); }

    /** The card last put down. */
    int top() { return top; }

    /** The sign the next card must have, unless it matches what is on the top card. */
    int wanted() { return wanted; }

    int turn() { return turn; }

    int winner() { return winner; }

    /** How many cards are left to draw. */
    int left() { return pile.size(); }

    /** The card the player has just drawn and may still put down, or {@link #NONE}. */
    int drawn() { return drawn; }

    /** Every card in the game, wherever it is: a check that none is lost or made. */
    int cards() { return pile.size() + discards.size() + 1 + hands.get(PLAYER).size() + hands.get(HALGAKOS).size(); }

    /** Whether a card may go down on the top one. */
    boolean fits(int card) {
        return isAny(card) || sign(card) == wanted || (!isAny(top) && what(card) == what(top));
    }

    /** Whether the side to move may put down the card at a place in its hand. */
    boolean playable(int who, int place) {
        if (winner != NOBODY || who != turn || place < 0 || place >= hands.get(who).size()) return false;
        int card = hands.get(who).get(place);
        // Having drawn, the player may put down only the card they drew.
        if (who == PLAYER && drawn != NONE && card != drawn) return false;
        return fits(card);
    }

    /**
     * Puts down the card at a place in a side's hand.
     *
     * @param named the sign to follow, for an any-sign card; ignored for any other
     * @return whether the card could be put down
     */
    boolean play(int who, int place, int named) {
        if (!playable(who, place)) return false;
        int card = hands.get(who).remove(place);
        discards.add(top);
        top = card;
        wanted = isAny(card) ? Math.max(0, Math.min(SIGNS - 1, named)) : sign(card);
        drawn = NONE;
        stuck = 0;
        if (hands.get(who).isEmpty()) {
            winner = who;
            return true;
        }
        // A skip and a take two both cost the other side its turn.
        if (what(card) == TAKE_TWO) {
            take(1 - who);
            take(1 - who);
        }
        if (what(card) != SKIP && what(card) != TAKE_TWO) turn = 1 - who;
        return true;
    }

    /**
     * The side to move draws a card, having nothing to put down or not wishing to. If the
     * card fits, the turn stays theirs to put it down or to {@link #pass}; otherwise the turn
     * passes.
     *
     * @return the card drawn, or {@link #NONE} if there was none to draw
     */
    int draw(int who) {
        if (winner != NOBODY || who != turn || (who == PLAYER && drawn != NONE)) return NONE;
        int card = take(who);
        if (card == NONE) {
            // Nothing to draw and nothing put down: when that goes round both sides, the fewer cards win.
            if (++stuck >= 2) {
                int mine = hands.get(PLAYER).size(), hers = hands.get(HALGAKOS).size();
                winner = mine == hers ? TIE : mine < hers ? PLAYER : HALGAKOS;
            }
            turn = 1 - who;
            return NONE;
        }
        stuck = 0;
        if (fits(card)) drawn = who == PLAYER ? card : NONE;
        else turn = 1 - who;
        return card;
    }

    /** The player keeps the card they drew, and the turn passes. */
    void pass() {
        if (winner != NOBODY || turn != PLAYER || drawn == NONE) return;
        drawn = NONE;
        turn = HALGAKOS;
    }

    private int take(int who) {
        if (pile.isEmpty()) {
            // The cards put down, all but the top one, are shuffled to be drawn again.
            pile.addAll(discards);
            discards.clear();
            Collections.shuffle(pile, new Random(pile.size() * 31L + top));
        }
        if (pile.isEmpty()) return NONE;
        int card = pile.remove(pile.size() - 1);
        hands.get(who).add(card);
        return card;
    }

    /**
     * The card Halgakos would put down, as its place in her hand, or {@link #NONE} if she has
     * nothing that fits. She keeps her any-sign cards for when nothing else will do, and
     * makes the player take two or miss a turn sooner when they are close to going out.
     */
    int choice() {
        List<Integer> hand = hands.get(HALGAKOS);
        int best = NONE, bestWorth = Integer.MIN_VALUE, close = hands.get(PLAYER).size() <= 3 ? 30 : 6;
        for (int place = 0; place < hand.size(); place++) {
            int card = hand.get(place);
            if (!fits(card)) continue;
            int worth = isAny(card) ? -50 : what(card) == TAKE_TWO ? close + 4 : what(card) == SKIP ? close : 0;
            // A sign she holds a lot of is one she will be able to follow.
            if (!isAny(card)) worth += held(sign(card)) * 3;
            if (worth > bestWorth) {
                bestWorth = worth;
                best = place;
            }
        }
        return best;
    }

    /** The sign Halgakos names when she puts down an any-sign card: the one she holds most of. */
    int named() {
        int best = 0;
        for (int sign = 1; sign < SIGNS; sign++) if (held(sign) > held(best)) best = sign;
        return best;
    }

    private int held(int sign) {
        int count = 0;
        for (int card : hands.get(HALGAKOS)) if (!isAny(card) && sign(card) == sign) count++;
        return count;
    }
}
