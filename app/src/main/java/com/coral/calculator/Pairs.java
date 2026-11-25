package com.coral.calculator;

import java.util.Random;

/** Sixteen cards face down, eight pairs of a calculator's signs: turn two up to find a pair. */
final class Pairs {
    static final String[] FACES = {"+", "−", "×", "÷", "%", "√", "π", "="};
    static final String[] SPOKEN = {"plus", "minus", "times", "divide", "percent", "root", "pi", "equals"};
    static final int CARDS = FACES.length * 2;
    /** What turning a card did. */
    static final int NOTHING = 0, FIRST = 1, MATCH = 2, MISS = 3;
    private static final int NONE = -1;

    private final int[] faces = new int[CARDS];
    private final boolean[] matched = new boolean[CARDS];
    private int first = NONE, second = NONE;
    private int moves;
    private int found;

    Pairs(Random random) {
        for (int card = 0; card < CARDS; card++) faces[card] = card / 2;
        for (int card = CARDS - 1; card > 0; card--) {
            int other = random.nextInt(card + 1);
            int face = faces[card];
            faces[card] = faces[other];
            faces[other] = face;
        }
    }

    /**
     * Turns a card up. Two that do not match stay up until {@link #settle}, or until the next
     * card is turned, which turns them down first.
     */
    int flip(int card) {
        if (card < 0 || card >= CARDS || matched[card] || card == first || card == second) return NOTHING;
        if (second != NONE) settle();
        if (first == NONE) {
            first = card;
            return FIRST;
        }
        moves++;
        if (faces[first] == faces[card]) {
            matched[first] = true;
            matched[card] = true;
            first = NONE;
            found++;
            return MATCH;
        }
        second = card;
        return MISS;
    }

    /** Turns a pair that did not match face down again. */
    void settle() {
        if (second == NONE) return;
        first = NONE;
        second = NONE;
    }

    /** Whether two cards that did not match are waiting to be turned down. */
    boolean missShowing() { return second != NONE; }

    boolean isUp(int card) { return matched[card] || card == first || card == second; }

    boolean isMatched(int card) { return matched[card]; }

    int face(int card) { return faces[card]; }

    /** How many times a second card has been turned. */
    int moves() { return moves; }

    int found() { return found; }

    boolean done() { return found == FACES.length; }
}
