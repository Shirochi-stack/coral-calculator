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
final class PlaySession implements Unlocks.Store {
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
    /** Chess is played on the card and minesweeper all over the screen; they are numbered on from her pranks. */
    static final int CHESS = 23, MINES = 24;
    /** Last card is played on the card too, and Plus run all over the screen. */
    static final int LAST_CARD = 25, RUN = 26;
    /** Page brawl, a fight between two of the webtoon's five, all over the screen; numbered on from the last of her pranks. */
    static final int FIGHT = 31;
    static final int NO_SQUARE = -1;
    /** How many moves ahead she looks at chess, counting each side's. */
    private static final int CHESS_AHEAD = 3;

    /** What a game is called. */
    static String name(int game) {
        return game == CHESS ? "Chess" : game == MINES ? "Minesweeper" : game == LAST_CARD ? "Last card"
                : game == RUN ? "Plus run" : game == FIGHT ? FightCast.GAME : NAMES[game];
    }

    /** Whether a game is played on the card, and not all over the screen. */
    static boolean onCard(int game) {
        return game == TIC || game == RPS || game == PAIRS || game == SUMS || game == CHESS || game == LAST_CARD;
    }
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
    private final CompanionPrefs names;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final List<Listener> listeners = new ArrayList<>();
    private final Runnable herMove = this::herMove;
    private final Runnable settlePairs = this::settlePairs;
    private final Runnable endSums = this::endSums;

    private int game = MENU;
    private String line = "Play with me Sloppy~♡";

    private TicTacToe tic;
    /** She had the first move of this round; and how many rounds running the player has won, and has drawn. */
    private boolean sheOpened;
    private int ticWins, ticDraws;
    private boolean herTurn;
    private boolean playerOpens = true;

    private int myHand = NO_HAND, herHand = NO_HAND;
    private int myPoints, herPoints;
    private boolean rpsStarted;
    /** How many throws running have been the same for both; and whether she has led this match by two to nothing. */
    private int rpsTies;
    private boolean rpsBehind;

    private Pairs pairs;
    /** How many pairs running have been found with no miss between them. */
    private int pairsRun;

    private Sums.Question question;
    private int sumsState = SUMS_READY;
    private int sumsScore, sumsWrong;
    private long sumsDeadline;

    private Chess chess;
    private boolean chessHerTurn;
    private int chessPicked = NO_SQUARE;
    /** How many moves the player has made in this game. */
    private int chessMoves;
    /** Which game of chess this is: a move she thought of for an earlier one is dropped. */
    private int chessGame;

    private LastCard cards;
    /** The place in the player's hand of an any-sign card that is waiting for its sign to be named, or none. */
    private int cardsNaming = LastCard.NONE;
    /** The most cards the player has held this hand, and what the last card they put down was. */
    private int cardsMost, cardsLast;
    private final Runnable herCard = this::herCard;

    static PlaySession get(Context context) {
        if (instance == null) instance = new PlaySession(context.getApplicationContext());
        return instance;
    }

    private PlaySession(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        names = new CompanionPrefs(context);
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

    /** What she is saying, with the user called by the name they chose. */
    String line() { return names.named(line); }

    /** A tally kept between rounds, such as {@code tic_won} or {@code sums_best}; 0 if none. */
    @Override public int tally(String key) { return preferences.getInt(key, 0); }

    /** One more of something that is counted. */
    void count(String key) { add(key, 1); }

    void add(String key, int more) {
        if (more > 0) preferences.edit().putInt(key, tally(key) + more).apply();
    }

    /** Words kept between rounds, such as which of her lines have been unlocked; null if none. */
    @Override public String note(String key) { return preferences.getString(key, null); }

    @Override public void keepNote(String key, String value) { preferences.edit().putString(key, value).apply(); }

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

    /** Keeps a number under a name, whatever was kept there before. */
    void keep(String key, int value) { preferences.edit().putInt(key, value).apply(); }

    /** The menu is to be shown again with its tallies as they now stand, after a game elsewhere. */
    void refresh() { changed(); }

    /** Goes to a game, carrying on with the round that was left there, or back to the menu. */
    void open(int which) {
        game = which;
        switch (which) {
            case TIC:
                if (tic == null) startTic();
                else if (tic.result() != TicTacToe.EMPTY) say("Again? You like losing Sloppy~♡", "Another round? Brave of you Sloppy~♡");
                else say(herTurn ? "My turn. Watch and learn Sloppy~♡" : "Still your turn Sloppy~♡");
                break;
            case RPS:
                if (!rpsStarted) startRps();
                else if (rpsOver()) say("Another match? You don't learn Sloppy~♡", "Again Sloppy~♡");
                else say("Still " + myPoints + " to " + herPoints + ". Throw Sloppy~♡");
                break;
            case PAIRS:
                if (pairs == null) startPairs();
                else if (pairs.done()) say("Shall I shuffle? You'll forget them anyway Sloppy~♡");
                else say((Pairs.FACES.length - pairs.found()) + " pairs to go. Think Sloppy~♡");
                break;
            case SUMS:
                if (sumsState == SUMS_READY) startSums();
                else if (sumsState == SUMS_OVER) say("Think you can beat that? Cute Sloppy~♡", "Another thirty seconds Sloppy~♡");
                else say("The clock is still running Sloppy~♡");
                break;
            case CHESS:
                if (chess == null) startChess();
                else if (chess.result() != Chess.ONGOING) say("Another game? You like losing Sloppy~♡", "Set them up again Sloppy~♡");
                else say(chessHerTurn ? "Shh. I'm thinking Sloppy~♡" : "Still your move Sloppy~♡");
                break;
            case LAST_CARD:
                if (cards == null) startCards();
                else if (cards.winner() != LastCard.NOBODY) say("Shall I deal again? You'll lose again Sloppy~♡", "Another hand Sloppy~♡");
                else say(cards.turn() == LastCard.HALGAKOS ? "My turn. Hands off Sloppy~♡" : "Still your turn Sloppy~♡");
                break;
            default:
                say("What will you lose at today Sloppy~♡", "Pick one. I'm good at all of them Sloppy~♡", "Play with me Sloppy~♡",
                        "Don't worry. Games are my speciality Sloppy~♡", "Can't decide? So hopeless Sloppy~♡");
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
        sheOpened = herTurn;
        playerOpens = !playerOpens;
        if (herTurn) {
            say("I'll start. Try to keep up Sloppy~♡", "Me first. Obviously Sloppy~♡");
            handler.postDelayed(herMove, HER_MOVE_MS);
        } else {
            say("You start. Crosses are yours Sloppy~♡", "You first. You'll need it Sloppy~♡");
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
            say("Hmm~ let me think Sloppy~♡", "Ehh~? There Sloppy~♡", "Pfft~ interesting choice Sloppy~♡");
            handler.postDelayed(herMove, HER_MOVE_MS);
        }
        changed();
    }

    private void herMove() {
        if (tic == null || !herTurn) return;
        herTurn = false;
        tic.place(tic.choose(random, SLIP_PERCENT), TicTacToe.HALGAKOS);
        if (tic.result() != TicTacToe.EMPTY) finishTic();
        else say("Your move Sloppy~♡", "There. Top that Sloppy~♡", "Take your time Sloppy~♡");
        changed();
    }

    private void finishTic() {
        herTurn = false;
        switch (tic.result()) {
            case TicTacToe.PLAYER:
                count("tic_won");
                ticDraws = 0;
                keepBest("tic_row", ++ticWins, false);
                if (sheOpened) count("tic_second");
                say("Three in a row?! No way Sloppy~♡", "You win. Don't get used to it Sloppy~♡",
                        "Tch. Beginner's luck Sloppy~♡", "Hmph. I let you have that one Sloppy~♡");
                mood(false);
                break;
            case TicTacToe.HALGAKOS:
                count("tic_lost");
                ticWins = 0;
                ticDraws = 0;
                say("Three in a row. Too easy Sloppy~♡", "I win. Obviously Sloppy~♡", "Got you! Again Sloppy~♡");
                mood(true);
                break;
            default:
                count("tic_drawn");
                ticWins = 0;
                keepBest("tic_draw_row", ++ticDraws, false);
                say("A draw? Not bad for a Sloppy~♡", "Nobody wins. I'll allow it Sloppy~♡", "Tch. A draw Sloppy~♡");
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
        rpsTies = 0;
        rpsBehind = false;
        say("First to three. Ready Sloppy~♡", "Don't cry when you lose Sloppy~♡");
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
        if (outcome == RockPaperScissors.WIN || outcome == RockPaperScissors.LOSS) rpsTies = 0;
        else keepBest("rps_ties", ++rpsTies, false);
        if (herPoints == 2 && myPoints == 0) rpsBehind = true;
        if (myPoints >= RPS_TARGET) {
            count("rps_won");
            if (herPoints == 0) count("rps_sweep");
            if (rpsBehind) count("rps_back");
            line = reason + ". " + (random.nextBoolean() ? "Tch. The match is yours Sloppy~♡" : "Hmph. You win this one Sloppy~♡");
            mood(false);
        } else if (herPoints >= RPS_TARGET) {
            count("rps_lost");
            line = reason + ". I win Sloppy~♡";
            mood(true);
        } else if (outcome == RockPaperScissors.WIN) {
            line = reason + ". Tch. Your point Sloppy~♡";
        } else if (outcome == RockPaperScissors.LOSS) {
            line = reason + ". My point Sloppy~♡";
        } else {
            say("Snap! " + RockPaperScissors.NAMES[hand] + " for us both Sloppy~♡", "Copying me Sloppy~♡");
        }
        changed();
    }

    // Pairs

    Pairs pairs() { return pairs; }

    private void startPairs() {
        handler.removeCallbacks(settlePairs);
        pairs = new Pairs(random);
        pairsRun = 0;
        say("Find the eight pairs. If you can Sloppy~♡", "Eight pairs. Try to remember Sloppy~♡");
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
        if (turned == Pairs.MATCH) {
            keepBest("pairs_run", ++pairsRun, false);
            if (pairs.moves() == 1) count("pairs_first");
            if (pairs.done()) count("pairs_done");
        } else if (turned == Pairs.MISS) {
            pairsRun = 0;
            count("pairs_miss");
        }
        if (turned == Pairs.MATCH && pairs.done()) {
            int best = tally("pairs_best"), moves = pairs.moves();
            boolean record = best == 0 || moves < best;
            if (record) preferences.edit().putInt("pairs_best", moves).apply();
            line = "All eight in " + moves + " turns. " + (record && best != 0 ? "A new best! Not bad Sloppy~♡" : "Took you long enough Sloppy~♡");
            mood(true);
        } else if (turned == Pairs.MATCH) {
            say("A pair. Lucky Sloppy~♡", "Ehh~ you remembered Sloppy~♡", "Hmph. Fine, that's one Sloppy~♡", "Not bad Sloppy~♡");
        } else if (turned == Pairs.MISS) {
            say("Wrong~ Try again Sloppy~♡", "Pfft~ not even close Sloppy~♡", "No match. Where was it Sloppy~♡");
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
        say("Thirty seconds. Scared Sloppy~♡", "Sums against the clock. Don't panic Sloppy~♡");
    }

    void sumsAgain() {
        startSums();
        sumsBegin();
    }

    void sumsBegin() {
        handler.removeCallbacks(endSums);
        sumsState = SUMS_RUNNING;
        sumsScore = 0;
        sumsWrong = 0;
        sumsDeadline = SystemClock.elapsedRealtime() + SUMS_MS;
        question = Sums.next(random);
        say("Go Sloppy~♡", "Off you go Sloppy~♡");
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
            count("sums_right");
            say("Hmph. Right Sloppy~♡", "Correct Sloppy~♡", "A lucky guess Sloppy~♡", "Not bad Sloppy~♡", "Keep up Sloppy~♡");
        } else {
            sumsWrong++;
            count("sums_wrong");
            line = question.text + " is " + question.answer + " Sloppy~♡";
        }
        question = Sums.next(random);
        changed();
    }

    private void endSums() {
        if (sumsState != SUMS_RUNNING) return;
        handler.removeCallbacks(endSums);
        sumsState = SUMS_OVER;
        count("sums_played");
        if (sumsScore == 0) count("sums_zero");
        if (sumsWrong == 0 && sumsScore >= 8) count("sums_clean");
        int best = tally("sums_best");
        boolean record = sumsScore > best;
        if (record) preferences.edit().putInt("sums_best", sumsScore).apply();
        line = "Time! " + sumsScore + " right." + (record && best != 0 ? " A new best! Hmph Sloppy~♡"
                : sumsScore == 0 ? " Where did your output go Sloppy~♡" : sumsScore < 5 ? " So slow Sloppy~♡" : " Not bad Sloppy~♡");
        if (sumsScore > 0) mood(true);
        changed();
    }

    // Last card

    LastCard cards() { return cards; }

    /** Whether the player has put down an any-sign card and has yet to name the sign that follows. */
    boolean cardsNaming() { return cardsNaming != LastCard.NONE; }

    private void startCards() {
        handler.removeCallbacks(herCard);
        cards = new LastCard(random);
        cardsNaming = LastCard.NONE;
        cardsMost = LastCard.HAND;
        cardsLast = LastCard.NONE;
        say("Seven cards each. Match the sign or the number Sloppy~♡", "First one out of cards wins. So, me Sloppy~♡");
    }

    void cardsAgain() {
        startCards();
        changed();
    }

    /** A touch on a card in the player's hand: it goes down if it fits. */
    void cardsTap(int place) {
        if (cards == null || cards.winner() != LastCard.NOBODY || !cards.playable(LastCard.PLAYER, place)) return;
        cardsNaming = LastCard.NONE;
        if (LastCard.isAny(cards.hand(LastCard.PLAYER).get(place))) {
            cardsNaming = place;
            say("Any sign. Which will it be Sloppy~♡");
            changed();
            return;
        }
        int what = LastCard.what(cards.hand(LastCard.PLAYER).get(place));
        cards.play(LastCard.PLAYER, place, 0);
        afterPlayerCard(what);
    }

    /** The player names the sign to follow their any-sign card. */
    void cardsName(int sign) {
        if (cards == null || cardsNaming == LastCard.NONE) return;
        int place = cardsNaming;
        cardsNaming = LastCard.NONE;
        if (cards.play(LastCard.PLAYER, place, sign)) afterPlayerCard(LastCard.ANY);
        else changed();
    }

    private void afterPlayerCard(int what) {
        cardsLast = what;
        if (what == LastCard.TAKE_TWO) count("cards_twos");
        else if (what == LastCard.SKIP) count("cards_skips");
        if (cards.winner() != LastCard.NOBODY) {
            finishCards();
        } else if (what == LastCard.TAKE_TWO) {
            say("Two more for me?! Rude Sloppy~♡", "Tch. Fine. Two cards Sloppy~♡");
        } else if (what == LastCard.SKIP) {
            say("Hey! I wanted that turn Sloppy~♡", "Skipping me? Bold Sloppy~♡");
        } else if (cards.hand(LastCard.PLAYER).size() == 1) {
            say("One card left?! Not so fast Sloppy~♡");
        } else {
            say("Hmm~ let me see Sloppy~♡", "Ehh~? That one Sloppy~♡", "My turn Sloppy~♡");
        }
        herCardSoon();
        changed();
    }

    /** A touch on the pile: the player draws a card, or keeps the one just drawn and lets the turn go. */
    void cardsDraw() {
        if (cards == null || cards.winner() != LastCard.NOBODY || cards.turn() != LastCard.PLAYER || cardsNaming()) return;
        if (cards.drawn() != LastCard.NONE) {
            cards.pass();
            say("Keeping it? My turn then Sloppy~♡");
        } else {
            int card = cards.draw(LastCard.PLAYER);
            if (card != LastCard.NONE) count("cards_drew");
            cardsMost = Math.max(cardsMost, cards.hand(LastCard.PLAYER).size());
            if (cards.winner() != LastCard.NOBODY) finishCards();
            else if (card == LastCard.NONE) say("No cards left to draw. My turn Sloppy~♡");
            else if (cards.drawn() != LastCard.NONE) say("That one fits. Put it down or keep it Sloppy~♡");
            else say("Pfft~ nothing useful? My turn Sloppy~♡", "Aww~ no luck. My turn Sloppy~♡");
        }
        herCardSoon();
        changed();
    }

    private void herCardSoon() {
        handler.removeCallbacks(herCard);
        if (cards != null && cards.winner() == LastCard.NOBODY && cards.turn() == LastCard.HALGAKOS) {
            handler.postDelayed(herCard, HER_MOVE_MS + 260);
        }
    }

    private void herCard() {
        if (cards == null || cards.winner() != LastCard.NOBODY || cards.turn() != LastCard.HALGAKOS) return;
        int place = cards.choice();
        if (place == LastCard.NONE) {
            int card = cards.draw(LastCard.HALGAKOS);
            if (cards.winner() != LastCard.NOBODY) finishCards();
            else if (card == LastCard.NONE) say("Nothing to draw. Your turn Sloppy~♡");
            else if (cards.turn() == LastCard.HALGAKOS) say("Ooh. This one will do Sloppy~♡");
            else say("Tch. Nothing. Your turn Sloppy~♡", "I'm drawing. Don't look Sloppy~♡");
        } else {
            int what = LastCard.what(cards.hand(LastCard.HALGAKOS).get(place));
            cards.play(LastCard.HALGAKOS, place, cards.named());
            cardsMost = Math.max(cardsMost, cards.hand(LastCard.PLAYER).size());
            if (cards.winner() != LastCard.NOBODY) finishCards();
            else if (cards.hand(LastCard.HALGAKOS).size() == 1) say("Last card Sloppy~♡");
            else if (what == LastCard.TAKE_TWO) say("Take two. You're welcome Sloppy~♡", "Two more for you Sloppy~♡");
            else if (what == LastCard.SKIP) say("Skip! I go again Sloppy~♡", "You miss a turn Sloppy~♡");
            else if (what == LastCard.ANY) say("Any sign. I pick this one Sloppy~♡");
            else say("Your turn Sloppy~♡", "There. Top that Sloppy~♡", "Match that Sloppy~♡");
        }
        herCardSoon();
        changed();
    }

    private void finishCards() {
        handler.removeCallbacks(herCard);
        cardsNaming = LastCard.NONE;
        if (cards.winner() == LastCard.PLAYER) {
            count("cards_won");
            if (cardsMost >= 10) count("cards_full");
            if (cardsLast == LastCard.ANY) count("cards_any");
            if (cards.hand(LastCard.HALGAKOS).size() >= 6) count("cards_sweep");
            say("Out of cards?! No way Sloppy~♡", "You win. Don't get used to it Sloppy~♡", "Hmph. Lucky deal Sloppy~♡");
            mood(false);
        } else if (cards.winner() == LastCard.HALGAKOS) {
            count("cards_lost");
            say("No cards left. I win Sloppy~♡", "All gone. Too easy Sloppy~♡", "Out first. Obviously Sloppy~♡");
            mood(true);
        } else {
            say("Nobody could move. Call it even Sloppy~♡");
        }
    }

    // Chess: the player has white and moves first; she has black

    Chess chess() { return chess; }

    boolean chessHerTurn() { return chessHerTurn; }

    /** The square of the piece the player has picked up, or {@link #NO_SQUARE}. */
    int chessPicked() { return chessPicked; }

    private void startChess() {
        chessGame++;
        chess = new Chess();
        chessHerTurn = false;
        chessPicked = NO_SQUARE;
        chessMoves = 0;
        say("You're white. You'll need the head start Sloppy~♡", "Chess? Bold of you Sloppy~♡", "White moves first. Go on Sloppy~♡");
    }

    void chessAgain() {
        startChess();
        changed();
    }

    /** A touch on a square: it picks up one of the player's pieces, or moves the one picked up there. */
    void chessTap(int square) {
        if (chess == null || chessHerTurn || chess.result() != Chess.ONGOING) return;
        int move = chessPicked == NO_SQUARE ? Chess.NO_MOVE : chess.move(chessPicked, square);
        if (move == Chess.NO_MOVE) {
            chessPicked = chess.at(square) > 0 && square != chessPicked ? square : NO_SQUARE;
            changed();
            return;
        }
        boolean took = chess.at(square) != 0, pawn = chess.at(chessPicked) == Chess.PAWN;
        chess.play(move);
        chessPicked = NO_SQUARE;
        chessMoves++;
        if (took) count("chess_took");
        if (pawn && chess.at(square) > Chess.PAWN) count("chess_promoted");
        if (chess.result() == Chess.ONGOING && chess.inCheck()) count("chess_checks");
        if (chess.result() != Chess.ONGOING) {
            finishChess();
        } else {
            chessHerTurn = true;
            if (chess.inCheck()) say("Check? How rude Sloppy~♡", "Eek. Check Sloppy~♡");
            else if (took) say("Hey! I was using that Sloppy~♡", "Tch. Give that back Sloppy~♡");
            else say("Hmm~ let me think Sloppy~♡", "Ehh~? That move Sloppy~♡", "Pfft~ interesting choice Sloppy~♡");
            think();
        }
        changed();
    }

    /** She thinks about her move away from the main thread, on a copy of the game, and then plays it. */
    private void think() {
        int game = chessGame;
        Chess copy = chess.copy();
        long began = SystemClock.uptimeMillis();
        new Thread(() -> {
            int move = copy.choose(new Random(), CHESS_AHEAD);
            handler.postDelayed(() -> herChessMove(game, move), Math.max(0, HER_MOVE_MS - (SystemClock.uptimeMillis() - began)));
        }, "halgakos-chess").start();
    }

    private void herChessMove(int game, int move) {
        if (game != chessGame || chess == null || !chessHerTurn || move == Chess.NO_MOVE) return;
        chessHerTurn = false;
        boolean took = chess.at(Chess.to(move)) != 0;
        chess.play(move);
        if (chess.result() != Chess.ONGOING) finishChess();
        else if (chess.inCheck()) say("Check Sloppy~♡", "Check. Run along Sloppy~♡");
        else if (took) say("Yoink. Mine now Sloppy~♡", "Thanks for that one Sloppy~♡");
        else say("Your move Sloppy~♡", "There. Top that Sloppy~♡", "Take your time Sloppy~♡");
        changed();
    }

    private void finishChess() {
        chessHerTurn = false;
        if (chessMoves >= 60) count("chess_long");
        switch (chess.result()) {
            case Chess.WHITE_WINS:
                count("chess_won");
                if (chessMoves <= 25) count("chess_quick");
                boolean queen = false;
                for (int square = 0; square < 64; square++) queen |= chess.at(square) == Chess.QUEEN;
                if (!queen) count("chess_queenless");
                say("Checkmate?! No way Sloppy~♡", "You win. Don't get used to it Sloppy~♡", "Hmph. I let you have that one Sloppy~♡");
                mood(false);
                break;
            case Chess.BLACK_WINS:
                count("chess_lost");
                say("Checkmate. Too easy Sloppy~♡", "Checkmate. Obviously Sloppy~♡", "Got your king! Again Sloppy~♡");
                mood(true);
                break;
            default:
                count("chess_drawn");
                say("A draw? Not bad for a Sloppy~♡", "Nobody wins. I'll allow it Sloppy~♡", "Tch. A draw Sloppy~♡");
                break;
        }
    }
}
