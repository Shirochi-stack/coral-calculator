package com.coral.calculator;

/** Rock, paper, scissors: each beats one of the others and loses to the third. */
final class RockPaperScissors {
    static final int ROCK = 0, PAPER = 1, SCISSORS = 2;
    static final String[] NAMES = {"Rock", "Paper", "Scissors"};
    /** How the first of two hands did against the second. */
    static final int LOSS = -1, TIE = 0, WIN = 1;

    private RockPaperScissors() {}

    static int outcome(int mine, int theirs) {
        if (mine == theirs) return TIE;
        return (mine - theirs + 3) % 3 == 1 ? WIN : LOSS;
    }

    /** Why one hand beat the other, the winner named first; null for two of the same. */
    static String reason(int one, int other) {
        if (one == other) return null;
        int winner = outcome(one, other) == WIN ? one : other;
        return winner == ROCK ? "Rock blunts scissors" : winner == PAPER ? "Paper wraps rock" : "Scissors cut paper";
    }
}
