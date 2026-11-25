package com.coral.calculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The games played with Halgakos: which one is on, how it stands, what she has to say about
 * it, and the tallies kept between rounds. Her popup and the full screen both show this one
 * session, so a game carries on from either. Used on the main thread only.
 */
final class PlaySession {
    /** Told when anything on show is different, and when something has pleased or upset her. */
    interface Listener {
        void onPlayChanged();
        void onMood(boolean cheerful);
    }

    static final int MENU = 0, TIC = 1, RPS = 2, PAIRS = 3, SUMS = 4;
    /** Played all over the screen, on a {@link PlayStage}, as is a prank. */
    static final int TAG = 5, RAIN = 6, ORDER = 7, PRANK = 8;
    static final String[] NAMES = {"Play with Halgakos", "Tic-tac-toe", "Rock paper scissors", "Pairs", "Quick sums",
            "Catch Halgakos", "Number rain", "Bubble count", "Prank me"};
    /** Points that win a match of rock paper scissors. */
    static final int RPS_TARGET = 3;
    static final int SUMS_MS = 30_000;
    static final int SUMS_READY = 0, SUMS_RUNNING = 1, SUMS_OVER = 2;
    static final int NO_HAND = -1;

    /** How often, out of a hundred, she misses what the player is up to at tic-tac-toe. */
    private static final int SLIP_PERCENT = 28;
    private static final long HER_MOVE_MS = 520;
    private static final long MISS_MS = 850;
    private static final String PREFERENCES = "play";

    private static PlaySession instance;

    private final SharedPreferences preferences;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final List<Listener> listeners = new ArrayList<>();
    private final Runnable herMove = this::herMove;
    private final Runnable settlePairs = this::settlePairs;
    private final Runnable endSums = this::endSums;

    private int game = MENU;
    private String line = "What shall we play?";

    private TicTacToe tic;
    private boolean herTurn;
    private boolean playerOpens = true;

    private int myHand = NO_HAND, herHand = NO_HAND;
    private int myPoints, herPoints;
    private boolean rpsStarted;

    private Pairs pairs;

    private Sums.Question question;
    private int sumsState = SUMS_READY;
    private int sumsScore;
    private long sumsDeadline;

    static PlaySession get(Context context) {
        if (instance == null) instance = new PlaySession(context.getApplicationContext());
        return instance;
    }

    private PlaySession(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    void addListener(Listener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    void removeListener(Listener listener) { listeners.remove(listener); }

    private void changed() {
        for (Listener listener : new ArrayList<>(listeners)) listener.onPlayChanged();
    }

    private void mood(boolean cheerful) {
        for (Listener listener : new ArrayList<>(listeners)) listener.onMood(cheerful);
    }

    /**
     * What Halgakos says. Every line of hers is chosen here, from what has just happened in
     * the game, which is where another source of her words would take over.
     */
    private void say(String... lines) {
        line = lines[random.nextInt(lines.length)];
    }

    int game() { return game; }

    String line() { return line; }

    /** A tally kept between rounds, such as {@code tic_won} or {@code sums_best}; 0 if none. */
    int tally(String key) { return preferences.getInt(key, 0); }

    private void count(String key) {
        preferences.edit().putInt(key, tally(key) + 1).apply();
    }

    /**
     * Keeps a result if it beats the one kept under this name, or is the first.
     *
     * @param lowerWins whether the result is a time, where less is better, and not a score
     * @return whether it was kept
     */
    boolean keepBest(String key, int value, boolean lowerWins) {
        int had = tally(key);
        if (value <= 0 || (had != 0 && (lowerWins ? value >= had : value <= had))) return false;
        preferences.edit().putInt(key, value).apply();
        return true;
    }

    /** The menu is to be shown again with its tallies as they now stand, after a game elsewhere. */
    void refresh() { changed(); }

    /** Goes to a game, carrying on with the round that was left there, or back to the menu. */
    void open(int which) {
        game = which;
        switch (which) {
            case TIC:
                if (tic == null) startTic();
                else if (tic.result() != TicTacToe.EMPTY) say("Another round?", "Shall we go again?");
                else say(herTurn ? "It's my turn. Let me think…" : "Where were we? It's your turn.");
                break;
            case RPS:
                if (!rpsStarted) startRps();
                else if (rpsOver()) say("Another match?", "Best of the next one?");
                else say("Still " + myPoints + " to " + herPoints + ". Your throw!");
                break;
            case PAIRS:
                if (pairs == null) startPairs();
                else if (pairs.done()) say("Shall I shuffle them again?");
                else say("Where were we? " + (Pairs.FACES.length - pairs.found()) + " pairs to go.");
                break;
            case SUMS:
                if (sumsState == SUMS_READY) startSums();
                else if (sumsState == SUMS_OVER) say("Another thirty seconds?", "Think you can beat that?");
                else say("The clock is still running!");
                break;
            default:
                say("What shall we play?", "Pick a game!", "I'm ready when you are.", "Don't worry. Games are my speciality.");
                break;
        }
        changed();
    }

    // Tic-tac-toe

    TicTacToe tic() { return tic; }

    boolean herTurn() { return herTurn; }

    private void startTic() {
        handler.removeCallbacks(herMove);
        tic = new TicTacToe();
        herTurn = !playerOpens;
        playerOpens = !playerOpens;
        if (herTurn) {
            say("I'll start this one.", "My turn to go first!");
            handler.postDelayed(herMove, HER_MOVE_MS);
        } else {
            say("You start. Crosses are yours.", "You go first!");
        }
    }

    void ticAgain() {
        startTic();
        changed();
    }

    void ticTap(int cell) {
        if (tic == null || herTurn || !tic.place(cell, TicTacToe.PLAYER)) return;
        if (tic.result() != TicTacToe.EMPTY) {
            finishTic();
        } else {
            herTurn = true;
            say("Hmm, let me think…", "Let me see…", "Ooh. One moment…");
            handler.postDelayed(herMove, HER_MOVE_MS);
        }
        changed();
    }

    private void herMove() {
        if (tic == null || !herTurn) return;
        herTurn = false;
        tic.place(tic.choose(random, SLIP_PERCENT), TicTacToe.HALGAKOS);
        if (tic.result() != TicTacToe.EMPTY) finishTic();
        else say("Your turn!", "There. Your move.", "Over to you.");
        changed();
    }

    private void finishTic() {
        herTurn = false;
        switch (tic.result()) {
            case TicTacToe.PLAYER:
                count("tic_won");
                // Losing is when her catchphrase comes out.
                say(Chatter.CATCHPHRASE + " You win.", "Three in a row?! " + Chatter.CATCHPHRASE,
                        "You win! I didn't see that coming.", "Hmph. Well played. That one is yours.");
                mood(false);
                break;
            case TicTacToe.HALGAKOS:
                count("tic_lost");
                say("Three in a row. I win!", "That's my game!", "Got you! Again?");
                mood(true);
                break;
            default:
                count("tic_drawn");
                say("A draw. We're evenly matched.", "Nobody wins that one.", "All full. Call it a draw?");
                break;
        }
    }

    // Rock paper scissors

    int myHand() { return myHand; }
    int herHand() { return herHand; }
    int myPoints() { return myPoints; }
    int herPoints() { return herPoints; }
    boolean rpsOver() { return myPoints >= RPS_TARGET || herPoints >= RPS_TARGET; }

    private void startRps() {
        rpsStarted = true;
        myHand = NO_HAND;
        herHand = NO_HAND;
        myPoints = 0;
        herPoints = 0;
        say("First to three. Ready?", "Rock, paper, scissors. First to three!");
    }

    void rpsAgain() {
        startRps();
        changed();
    }

    void rpsThrow(int hand) {
        if (rpsOver()) return;
        myHand = hand;
        herHand = random.nextInt(3);
        int outcome = RockPaperScissors.outcome(myHand, herHand);
        String reason = RockPaperScissors.reason(myHand, herHand);
        if (outcome == RockPaperScissors.WIN) myPoints++;
        else if (outcome == RockPaperScissors.LOSS) herPoints++;
        if (myPoints >= RPS_TARGET) {
            count("rps_won");
            line = reason + ". " + Chatter.orCatchphrase(random, "The match is yours!", "Hmph. You win this one.");
            mood(false);
        } else if (herPoints >= RPS_TARGET) {
            count("rps_lost");
            line = reason + ". I win the match!";
            mood(true);
        } else if (outcome == RockPaperScissors.WIN) {
            line = reason + ". Your point!";
        } else if (outcome == RockPaperScissors.LOSS) {
            line = reason + ". My point!";
        } else {
            say("Snap! " + RockPaperScissors.NAMES[hand] + " for us both.", "The same! Throw again.");
        }
        changed();
    }

    // Pairs

    Pairs pairs() { return pairs; }

    private void startPairs() {
        handler.removeCallbacks(settlePairs);
        pairs = new Pairs(random);
        say("Find the eight pairs.", "Eight pairs are hiding. Turn two over!");
    }

    void pairsAgain() {
        startPairs();
        changed();
    }

    void pairsTap(int card) {
        if (pairs == null) return;
        int turned = pairs.flip(card);
        if (turned == Pairs.NOTHING) return;
        handler.removeCallbacks(settlePairs);
        if (turned == Pairs.MATCH && pairs.done()) {
            int best = tally("pairs_best"), moves = pairs.moves();
            boolean record = best == 0 || moves < best;
            if (record) preferences.edit().putInt("pairs_best", moves).apply();
            line = "All eight in " + moves + " turns!" + (record && best != 0 ? " That's a new best." : "");
            mood(true);
        } else if (turned == Pairs.MATCH) {
            say("A pair!", "That's one!", "Nicely remembered.", "Yes! They match.");
        } else if (turned == Pairs.MISS) {
            say("Not a pair. Remember them!", "No match. Where was the other?", "Close! Try again.");
            handler.postDelayed(settlePairs, MISS_MS);
        }
        changed();
    }

    private void settlePairs() {
        if (pairs == null || !pairs.missShowing()) return;
        pairs.settle();
        changed();
    }

    // Quick sums

    int sumsState() { return sumsState; }
    int sumsScore() { return sumsScore; }
    Sums.Question question() { return question; }

    /** Milliseconds left on the clock while a round is running. */
    long sumsLeft() {
        return sumsState == SUMS_RUNNING ? Math.max(0, sumsDeadline - SystemClock.elapsedRealtime()) : 0;
    }

    private void startSums() {
        handler.removeCallbacks(endSums);
        sumsState = SUMS_READY;
        sumsScore = 0;
        question = null;
        say("Thirty seconds. How many can you do?", "Sums against the clock! Ready?");
    }

    void sumsAgain() {
        startSums();
        sumsBegin();
    }

    void sumsBegin() {
        handler.removeCallbacks(endSums);
        sumsState = SUMS_RUNNING;
        sumsScore = 0;
        sumsDeadline = SystemClock.elapsedRealtime() + SUMS_MS;
        question = Sums.next(random);
        say("Go!", "Off you go!");
        handler.postDelayed(endSums, SUMS_MS);
        changed();
    }

    void sumsAnswer(int choice) {
        if (sumsState != SUMS_RUNNING || question == null) return;
        if (sumsLeft() == 0) {
            endSums();
            return;
        }
        if (question.choices[choice] == question.answer) {
            sumsScore++;
            say("Right!", "Yes!", "Correct!", "That's it!", "Quick!");
        } else {
            // Her catchphrase, turned on the player for once.
            line = question.text + " is " + question.answer + "." + (random.nextInt(3) == 0 ? " Where did your output go?!" : "");
        }
        question = Sums.next(random);
        changed();
    }

    private void endSums() {
        if (sumsState != SUMS_RUNNING) return;
        handler.removeCallbacks(endSums);
        sumsState = SUMS_OVER;
        int best = tally("sums_best");
        boolean record = sumsScore > best;
        if (record) preferences.edit().putInt("sums_best", sumsScore).apply();
        line = "Time! " + sumsScore + " right." + (record && best != 0 ? " That's a new best!"
                : sumsScore == 0 ? " Where did your output go?!" : "");
        if (sumsScore > 0) mood(true);
        changed();
    }
}
