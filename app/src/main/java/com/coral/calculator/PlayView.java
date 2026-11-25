package com.coral.calculator;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The small games played with Halgakos. The companion opens this where the mini calculator
 * opens, at the same size, and the app shows it larger on a screen of its own. Both show the
 * one {@link PlaySession}, so a game carries on from either.
 */
final class PlayView extends LinearLayout implements PlaySession.Listener {
    interface Host {
        /** The arrow in the corner: carry on in the app, on a full screen. */
        void onOpenApp();
        void onClose();
        /** Something in a game has pleased Halgakos, or upset her. */
        void onMood(boolean cheerful);
        /** A game that is played all over the screen was chosen, or a prank asked for. */
        void onStage(int game);
    }

    /** Size at full scale, in dp: the mini calculator's, whose place this takes. */
    static final int WIDTH_DP = MiniCalculatorView.WIDTH_DP;
    static final int HEIGHT_DP = MiniCalculatorView.HEIGHT_DP;
    private static final int SIDE = 12, TOP = 8, HEADER = 32, HEADER_BUTTON = 46, SPEECH = 40, GAP = 6, FOOTER = 36;
    private static final int NOT_SHOWN = -1;
    private static final long TICK_MS = 100;
    private static final String[] HANDS = {CompanionIcon.ROCK, CompanionIcon.PAPER, CompanionIcon.SCISSORS};
    /** The menu's tiles, in the order of the games in {@link PlaySession}. */
    private static final String[] TILE_ICONS = {CompanionIcon.GRID, CompanionIcon.SCISSORS, CompanionIcon.CARDS,
            CompanionIcon.SUMS, CompanionIcon.TARGET, CompanionIcon.RAIN, CompanionIcon.BUBBLES, CompanionIcon.SPARK};

    private final PlaySession session;
    private final float unit;
    private final boolean dark;
    private final boolean haptics;
    private final Host host;
    private final int ink, soft, muted, key, tint;
    private final TextView title;
    private final View back;
    private final TextView speech;
    private final FrameLayout body;
    private final Runnable tick = this::tick;
    /** Brings the game on show up to date; each game sets its own when it is laid out. */
    private Runnable updater = () -> {};
    /** Moves the clock of quick sums on, while a round of it is on show. */
    private Runnable clock = () -> {};
    private int shown = NOT_SHOWN;

    /**
     * @param unit pixels per dp of the design: smaller than the screen's own where room is
     *             short, larger on a full screen
     * @param full on a screen of its own in the app, where there is no app to open
     */
    PlayView(Context context, float unit, boolean dark, boolean haptics, boolean full, Host host) {
        super(context);
        this.unit = unit;
        this.dark = dark;
        this.haptics = haptics;
        this.host = host;
        session = PlaySession.get(context);
        ink = dark ? 0xFFF3F3F3 : 0xFF292B2A;
        soft = dark ? 0xFFEBECEC : 0xFF565A57;
        muted = dark ? 0xFF9CA2A6 : 0xFF8B8E8C;
        key = dark ? 0xFF2A2E31 : 0xFFF3F3F3;
        tint = dark ? 0xFF44292E : 0xFFFCE9EB;
        setOrientation(VERTICAL);
        setLayoutDirection(LAYOUT_DIRECTION_LTR);
        setPadding(px(SIDE), px(TOP), px(SIDE), px(SIDE));
        GradientDrawable card = new GradientDrawable();
        card.setCornerRadius(px(22));
        card.setColor(dark ? 0xFF1D2023 : Color.WHITE);
        card.setStroke(Math.max(1, px(.75f)), dark ? 0xFF34383C : 0xFFE4E4E4);
        setBackground(card);
        setClipToOutline(true);
        // Touches on the card itself must not fall through to the app underneath.
        setClickable(true);

        LinearLayout header = new LinearLayout(context);
        header.setGravity(Gravity.CENTER_VERTICAL);
        back = headerButton(CompanionIcon.BACK, "Back to the games", () -> session.open(PlaySession.MENU));
        header.addView(back, new LayoutParams(px(34), px(HEADER)));
        title = text("", 13, muted, true);
        title.setMaxLines(1);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(px(4), 0, 0, 0);
        // The longest name gives up a little of its size to fit between the buttons.
        title.setAutoSizeTextTypeUniformWithConfiguration(px(9), px(13), Math.max(1, px(.5f)), TypedValue.COMPLEX_UNIT_PX);
        header.addView(title, new LayoutParams(0, px(18), 1));
        if (!full) {
            header.addView(headerButton(CompanionIcon.OPEN, "Play on the full screen", host::onOpenApp),
                    new LayoutParams(px(HEADER_BUTTON), px(HEADER)));
        }
        LayoutParams closeParams = new LayoutParams(px(HEADER_BUTTON), px(HEADER));
        closeParams.leftMargin = px(GAP);
        header.addView(headerButton(CompanionIcon.CLOSE, "Close the games", host::onClose), closeParams);
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, px(HEADER)));

        // What Halgakos has to say about the game, read out as it changes.
        speech = text("", 12.5f, soft, false);
        speech.setMaxLines(2);
        speech.setEllipsize(TextUtils.TruncateAt.END);
        speech.setGravity(Gravity.CENTER_VERTICAL);
        speech.setPadding(px(11), 0, px(11), 0);
        speech.setBackground(round(key, 12));
        speech.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        LayoutParams speechParams = new LayoutParams(LayoutParams.MATCH_PARENT, px(SPEECH));
        speechParams.topMargin = px(2);
        addView(speech, speechParams);

        body = new FrameLayout(context);
        LayoutParams bodyParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
        bodyParams.topMargin = px(8);
        addView(body, bodyParams);
        render();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        session.addListener(this);
        render();
    }

    @Override protected void onDetachedFromWindow() {
        session.removeListener(this);
        removeCallbacks(tick);
        super.onDetachedFromWindow();
    }

    @Override public void onPlayChanged() { render(); }

    @Override public void onMood(boolean cheerful) { host.onMood(cheerful); }

    /** Lays out the game that is on if it is not the one on show, and brings it up to date. */
    private void render() {
        int game = session.game();
        if (game != shown) {
            shown = game;
            clock = () -> {};
            body.removeAllViews();
            View content = game == PlaySession.TIC ? ticTacToe() : game == PlaySession.RPS ? rockPaperScissors()
                    : game == PlaySession.PAIRS ? pairs() : game == PlaySession.SUMS ? sums() : menu();
            body.addView(content, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            title.setText(PlaySession.NAMES[game]);
            back.setVisibility(game == PlaySession.MENU ? GONE : VISIBLE);
        }
        speech.setText(session.line());
        speech.setContentDescription("Halgakos says: " + session.line());
        updater.run();
        removeCallbacks(tick);
        tick();
    }

    private void tick() {
        if (shown != PlaySession.SUMS || session.sumsState() != PlaySession.SUMS_RUNNING || !isAttachedToWindow()) return;
        clock.run();
        postDelayed(tick, TICK_MS);
    }

    // The menu of games

    /** Eight tiles: four games played on this card, three all over the screen, and her pranks. */
    private View menu() {
        LinearLayout grid = new LinearLayout(getContext());
        grid.setOrientation(VERTICAL);
        TextView[] notes = new TextView[TILE_ICONS.length];
        for (int row = 0; row < TILE_ICONS.length / 2; row++) {
            LinearLayout line = new LinearLayout(getContext());
            for (int column = 0; column < 2; column++) {
                int index = row * 2 + column, game = index + 1;
                LinearLayout tile = new LinearLayout(getContext());
                tile.setOrientation(VERTICAL);
                tile.setGravity(Gravity.CENTER);
                tile.setPadding(px(4), 0, px(4), 0);
                tile.setBackground(tile(key, 14));
                tile.addView(new CompanionIcon(getContext(), TILE_ICONS[index], CalculatorLayout.CORAL, 1f),
                        new LayoutParams(px(21), px(21)));
                TextView name = text(PlaySession.NAMES[game], 11.5f, ink, true);
                name.setGravity(Gravity.CENTER);
                name.setMaxLines(1);
                // The longest name gives up a little of its size to stay on one line.
                name.setAutoSizeTextTypeUniformWithConfiguration(px(8), px(11.5f), Math.max(1, px(.5f)),
                        TypedValue.COMPLEX_UNIT_PX);
                LayoutParams nameParams = new LayoutParams(LayoutParams.MATCH_PARENT, px(15));
                nameParams.topMargin = px(3);
                tile.addView(name, nameParams);
                notes[index] = text("", 9.5f, muted, false);
                notes[index].setGravity(Gravity.CENTER);
                notes[index].setMaxLines(1);
                LayoutParams noteParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                noteParams.topMargin = px(1);
                tile.addView(notes[index], noteParams);
                tile.setFocusable(true);
                tap(tile, () -> {
                    if (game >= PlaySession.TAG) host.onStage(game); else session.open(game);
                });
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
                if (column > 0) params.leftMargin = px(GAP);
                line.addView(tile, params);
            }
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
            if (row > 0) params.topMargin = px(GAP);
            grid.addView(line, params);
        }
        updater = () -> {
            notes[0].setText(record(session.tally("tic_won"), session.tally("tic_lost"), session.tally("tic_drawn")));
            notes[1].setText(record(session.tally("rps_won"), session.tally("rps_lost"), 0));
            notes[2].setText(best("pairs_best", " turns", "On this card"));
            notes[3].setText(best("sums_best", " right", "On this card"));
            notes[4].setText(best("tag_best", " caught", "Whole screen"));
            notes[5].setText(best("rain_best", " caught", "Whole screen"));
            int tenths = session.tally("order_best");
            notes[6].setText(tenths == 0 ? "Whole screen" : "Best: " + PlayStage.seconds(tenths) + "s");
            notes[7].setText("Whole screen");
            for (int index = 0; index < notes.length; index++) {
                ((View) notes[index].getParent()).setContentDescription(
                        PlaySession.NAMES[index + 1] + ". " + notes[index].getText());
            }
        };
        return grid;
    }

    private static String record(int won, int lost, int drawn) {
        return won + lost + drawn == 0 ? "On this card" : "Won " + won + " · Lost " + lost;
    }

    private String best(String key, String unit, String unplayed) {
        int value = session.tally(key);
        return value == 0 ? unplayed : "Best: " + value + unit;
    }

    // Tic-tac-toe

    private View ticTacToe() {
        LinearLayout root = column();
        LinearLayout board = new LinearLayout(getContext());
        board.setOrientation(VERTICAL);
        CompanionIcon[] cells = new CompanionIcon[9];
        boolean[] lit = new boolean[9];
        for (int row = 0; row < 3; row++) {
            LinearLayout line = new LinearLayout(getContext());
            for (int column = 0; column < 3; column++) {
                int cell = row * 3 + column;
                cells[cell] = new CompanionIcon(getContext(), CompanionIcon.NONE, ink, .46f);
                cells[cell].setBackground(tile(key, 14));
                cells[cell].setFocusable(true);
                tap(cells[cell], () -> session.ticTap(cell));
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
                if (column > 0) params.leftMargin = px(GAP);
                line.addView(cells[cell], params);
            }
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
            if (row > 0) params.topMargin = px(GAP);
            board.addView(line, params);
        }
        FrameLayout stage = new FrameLayout(getContext());
        stage.addView(board, new FrameLayout.LayoutParams(px(206), px(206), Gravity.CENTER));
        root.addView(stage, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        TextView note = footnote();
        TextView again = button("");
        tap(again, session::ticAgain);
        root.addView(footer(note, again));
        updater = () -> {
            TicTacToe game = session.tic();
            if (game == null) return;
            boolean over = game.result() != TicTacToe.EMPTY;
            int[] won = game.winningLine();
            for (int cell = 0; cell < cells.length; cell++) {
                int mark = game.at(cell);
                cells[cell].set(mark == TicTacToe.PLAYER ? CompanionIcon.CLOSE : mark == TicTacToe.HALGAKOS
                        ? CompanionIcon.NOUGHT : CompanionIcon.NONE, mark == TicTacToe.HALGAKOS ? CalculatorLayout.CORAL : ink);
                boolean light = won != null && (won[0] == cell || won[1] == cell || won[2] == cell);
                if (light != lit[cell]) {
                    lit[cell] = light;
                    cells[cell].setBackground(tile(light ? tint : key, 14));
                }
                cells[cell].setClickable(mark == TicTacToe.EMPTY && !over && !session.herTurn());
                cells[cell].setContentDescription("Row " + (cell / 3 + 1) + ", column " + (cell % 3 + 1) + ": "
                        + (mark == TicTacToe.PLAYER ? "your cross" : mark == TicTacToe.HALGAKOS ? "Halgakos's nought" : "empty"));
            }
            note.setText("Won " + session.tally("tic_won") + " · Lost " + session.tally("tic_lost")
                    + " · Drawn " + session.tally("tic_drawn"));
            style(again, over ? "Play again" : "Restart", over);
        };
        return root;
    }

    // Rock paper scissors

    private View rockPaperScissors() {
        LinearLayout root = column();
        LinearLayout arena = new LinearLayout(getContext());
        arena.setGravity(Gravity.CENTER_VERTICAL);
        CompanionIcon mine = new CompanionIcon(getContext(), CompanionIcon.NONE, ink, .52f);
        CompanionIcon hers = new CompanionIcon(getContext(), CompanionIcon.NONE, CalculatorLayout.CORAL, .52f);
        arena.addView(side("You", mine), new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        LinearLayout middle = new LinearLayout(getContext());
        middle.setOrientation(VERTICAL);
        middle.setGravity(Gravity.CENTER);
        TextView score = centred(text("", 24, ink, true));
        middle.addView(score);
        middle.addView(centred(text("First to " + PlaySession.RPS_TARGET, 10.5f, muted, false)));
        arena.addView(middle, new LayoutParams(px(72), LayoutParams.WRAP_CONTENT));
        arena.addView(side("Halgakos", hers), new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        root.addView(arena, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));

        LinearLayout hands = new LinearLayout(getContext());
        View[] throwers = new View[3];
        for (int hand = 0; hand < 3; hand++) {
            LinearLayout thrower = new LinearLayout(getContext());
            thrower.setOrientation(VERTICAL);
            thrower.setGravity(Gravity.CENTER);
            thrower.setBackground(tile(key, 14));
            thrower.addView(new CompanionIcon(getContext(), HANDS[hand], CalculatorLayout.CORAL, 1f),
                    new LayoutParams(px(26), px(26)));
            TextView label = text(RockPaperScissors.NAMES[hand], 10.5f, soft, true);
            LayoutParams labelParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = px(2);
            thrower.addView(label, labelParams);
            thrower.setContentDescription("Throw " + RockPaperScissors.NAMES[hand].toLowerCase(java.util.Locale.ROOT));
            thrower.setFocusable(true);
            int thrown = hand;
            tap(thrower, () -> session.rpsThrow(thrown));
            LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
            if (hand > 0) params.leftMargin = px(GAP);
            hands.addView(thrower, params);
            throwers[hand] = thrower;
        }
        root.addView(hands, new LayoutParams(LayoutParams.MATCH_PARENT, px(58)));
        TextView note = footnote();
        TextView again = button("");
        tap(again, session::rpsAgain);
        root.addView(footer(note, again));
        int[] winner = {RockPaperScissors.TIE};
        updater = () -> {
            int my = session.myHand(), her = session.herHand();
            boolean over = session.rpsOver();
            mine.set(my == PlaySession.NO_HAND ? CompanionIcon.NONE : HANDS[my], ink);
            hers.set(her == PlaySession.NO_HAND ? CompanionIcon.NONE : HANDS[her], CalculatorLayout.CORAL);
            mine.setContentDescription(my == PlaySession.NO_HAND ? "You have not thrown" : "You threw " + RockPaperScissors.NAMES[my]);
            hers.setContentDescription(her == PlaySession.NO_HAND ? "Halgakos has not thrown"
                    : "Halgakos threw " + RockPaperScissors.NAMES[her]);
            // The hand that took the point is lit.
            int outcome = my == PlaySession.NO_HAND ? RockPaperScissors.TIE : RockPaperScissors.outcome(my, her);
            if (outcome != winner[0]) {
                winner[0] = outcome;
                mine.setBackground(round(outcome == RockPaperScissors.WIN ? tint : key, 100));
                hers.setBackground(round(outcome == RockPaperScissors.LOSS ? tint : key, 100));
            }
            score.setText(session.myPoints() + " – " + session.herPoints());
            score.setContentDescription("You " + session.myPoints() + ", Halgakos " + session.herPoints());
            for (View thrower : throwers) {
                thrower.setClickable(!over);
                thrower.setAlpha(over ? .4f : 1);
            }
            note.setText("Matches: won " + session.tally("rps_won") + " · lost " + session.tally("rps_lost"));
            style(again, over ? "Play again" : "Restart", over);
        };
        return root;
    }

    /** One player's place at rock paper scissors: a name over the hand they threw. */
    private View side(String name, CompanionIcon hand) {
        LinearLayout side = new LinearLayout(getContext());
        side.setOrientation(VERTICAL);
        side.setGravity(Gravity.CENTER_HORIZONTAL);
        side.addView(centred(text(name, 11.5f, muted, true)));
        hand.setBackground(round(key, 100));
        hand.setFocusable(true);
        LayoutParams params = new LayoutParams(px(66), px(66));
        params.topMargin = px(7);
        side.addView(hand, params);
        return side;
    }

    // Pairs

    private View pairs() {
        LinearLayout root = column();
        LinearLayout grid = new LinearLayout(getContext());
        grid.setOrientation(VERTICAL);
        TextView[] cards = new TextView[Pairs.CARDS];
        int[] look = new int[Pairs.CARDS];
        for (int row = 0; row < 4; row++) {
            LinearLayout line = new LinearLayout(getContext());
            for (int column = 0; column < 4; column++) {
                int card = row * 4 + column;
                cards[card] = text("", 21, ink, true);
                cards[card].setGravity(Gravity.CENTER);
                cards[card].setFocusable(true);
                look[card] = NOT_SHOWN;
                tap(cards[card], () -> session.pairsTap(card));
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
                if (column > 0) params.leftMargin = px(GAP);
                line.addView(cards[card], params);
            }
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
            if (row > 0) params.topMargin = px(GAP);
            grid.addView(line, params);
        }
        root.addView(grid, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        TextView note = footnote();
        TextView again = button("");
        tap(again, session::pairsAgain);
        root.addView(footer(note, again));
        updater = () -> {
            Pairs game = session.pairs();
            if (game == null) return;
            for (int card = 0; card < cards.length; card++) {
                // Face down, face up, or found: the card is only redrawn when that changes.
                int state = game.isMatched(card) ? 2 : game.isUp(card) ? 1 : 0;
                if (state != look[card]) {
                    look[card] = state;
                    cards[card].setBackground(tile(state == 0 ? tint : key, 12));
                    cards[card].setText(state == 0 ? "" : Pairs.FACES[game.face(card)]);
                    cards[card].setTextColor(state == 2 ? CalculatorLayout.CORAL : ink);
                    cards[card].setAlpha(state == 2 ? .5f : 1);
                    cards[card].setClickable(state == 0);
                    cards[card].setContentDescription("Card " + (card + 1) + ": " + (state == 0 ? "face down"
                            : Pairs.SPOKEN[game.face(card)] + (state == 2 ? ", found" : "")));
                }
            }
            int best = session.tally("pairs_best");
            note.setText("Turns " + game.moves() + (best == 0 ? "" : " · Best " + best));
            style(again, game.done() ? "Play again" : "Restart", game.done());
        };
        return root;
    }

    // Quick sums

    private View sums() {
        FrameLayout root = new FrameLayout(getContext());

        // Before a round, and after one: a number, what it counts, and a way to begin.
        LinearLayout rest = new LinearLayout(getContext());
        rest.setOrientation(VERTICAL);
        rest.setGravity(Gravity.CENTER);
        TextView figure = centred(text("", 46, CalculatorLayout.CORAL, true));
        rest.addView(figure);
        TextView caption = centred(text("", 13, soft, false));
        rest.addView(caption);
        TextView best = centred(text("", 11.5f, muted, false));
        LayoutParams bestParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        bestParams.topMargin = px(4);
        rest.addView(best, bestParams);
        TextView begin = button("");
        begin.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(14.5f));
        LayoutParams beginParams = new LayoutParams(px(150), px(40));
        beginParams.topMargin = px(16);
        rest.addView(begin, beginParams);
        tap(begin, () -> {
            if (session.sumsState() == PlaySession.SUMS_OVER) session.sumsAgain(); else session.sumsBegin();
        });
        root.addView(rest, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout round = column();
        LinearLayout top = new LinearLayout(getContext());
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView score = text("", 12.5f, CalculatorLayout.CORAL, true);
        top.addView(score, new LayoutParams(px(52), LayoutParams.WRAP_CONTENT));
        Meter meter = new Meter(getContext());
        top.addView(meter, new LayoutParams(0, px(6), 1));
        TextView time = text("", 12.5f, ink, true);
        time.setGravity(Gravity.END);
        top.addView(time, new LayoutParams(px(34), LayoutParams.WRAP_CONTENT));
        round.addView(top, new LayoutParams(LayoutParams.MATCH_PARENT, px(22)));
        TextView sum = text("", 38, ink, true);
        sum.setGravity(Gravity.CENTER);
        sum.setMaxLines(1);
        round.addView(sum, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        TextView[] answers = new TextView[Sums.CHOICES];
        for (int row = 0; row < 2; row++) {
            LinearLayout line = new LinearLayout(getContext());
            for (int column = 0; column < 2; column++) {
                int choice = row * 2 + column;
                answers[choice] = text("", 20, ink, true);
                answers[choice].setGravity(Gravity.CENTER);
                answers[choice].setBackground(CalculatorLayout.pill(key));
                answers[choice].setFocusable(true);
                tap(answers[choice], () -> session.sumsAnswer(choice));
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
                if (column > 0) params.leftMargin = px(GAP);
                line.addView(answers[choice], params);
            }
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, px(48));
            params.topMargin = px(GAP);
            round.addView(line, params);
        }
        root.addView(round, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        clock = () -> {
            long left = session.sumsLeft();
            meter.set(left / (float) PlaySession.SUMS_MS);
            time.setText(String.valueOf((left + 999) / 1000));
        };
        updater = () -> {
            int state = session.sumsState();
            boolean running = state == PlaySession.SUMS_RUNNING;
            rest.setVisibility(running ? GONE : VISIBLE);
            round.setVisibility(running ? VISIBLE : GONE);
            int record = session.tally("sums_best");
            if (running) {
                Sums.Question question = session.question();
                sum.setText(question.text);
                sum.setContentDescription(question.spoken);
                for (int choice = 0; choice < answers.length; choice++) {
                    answers[choice].setText(String.valueOf(question.choices[choice]));
                }
                score.setText(session.sumsScore() + " right");
                clock.run();
            } else if (state == PlaySession.SUMS_OVER) {
                figure.setText(String.valueOf(session.sumsScore()));
                caption.setText("right in " + PlaySession.SUMS_MS / 1000 + " seconds");
                best.setText(record == 0 ? "" : "Best: " + record);
                style(begin, "Play again", true);
            } else {
                figure.setText(String.valueOf(PlaySession.SUMS_MS / 1000));
                caption.setText("seconds of sums");
                best.setText(record == 0 ? "Pick the right answer, as many times as you can" : "Best: " + record + " right");
                style(begin, "Start", true);
            }
        };
        return root;
    }

    /** How much of the clock is left at quick sums. */
    private final class Meter extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bar = new RectF();
        private float left = 1;

        Meter(Context context) { super(context); }

        void set(float fraction) {
            left = Math.max(0, Math.min(1, fraction));
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            float radius = getHeight() / 2f;
            bar.set(0, 0, getWidth(), getHeight());
            paint.setColor(key);
            canvas.drawRoundRect(bar, radius, radius, paint);
            bar.right = Math.max(getHeight(), getWidth() * left);
            paint.setColor(CalculatorLayout.CORAL);
            if (left > 0) canvas.drawRoundRect(bar, radius, radius, paint);
        }
    }

    // What the games are built from

    private int px(float dp) { return Math.round(dp * unit); }

    private LinearLayout column() {
        LinearLayout column = new LinearLayout(getContext());
        column.setOrientation(VERTICAL);
        return column;
    }

    private TextView text(String value, float size, int color, boolean medium) {
        TextView view = new TextView(getContext());
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(size));
        view.setTextColor(color);
        view.setIncludeFontPadding(false);
        view.setTypeface(Typeface.create(medium ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        return view;
    }

    /** For a line that fills the width of a column, which would otherwise start at its left. */
    private static TextView centred(TextView view) {
        view.setGravity(Gravity.CENTER);
        return view;
    }

    /** The line under a game: its tallies. */
    private TextView footnote() {
        TextView note = text("", 11.5f, muted, false);
        note.setMaxLines(2);
        return note;
    }

    /** The row under a game: its tallies, and the button that starts it again. */
    private View footer(TextView note, TextView again) {
        LinearLayout footer = new LinearLayout(getContext());
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.addView(note, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, px(30));
        params.leftMargin = px(8);
        footer.addView(again, params);
        LayoutParams footerParams = new LayoutParams(LayoutParams.MATCH_PARENT, px(FOOTER));
        footerParams.topMargin = px(8);
        footer.setLayoutParams(footerParams);
        return footer;
    }

    private TextView button(String label) {
        TextView button = new TextView(getContext()) {
            @Override public CharSequence getAccessibilityClassName() { return Button.class.getName(); }
        };
        button.setText(label);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(12.5f));
        button.setIncludeFontPadding(false);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setGravity(Gravity.CENTER);
        button.setPadding(px(14), 0, px(14), 0);
        button.setFocusable(true);
        return button;
    }

    /** A button stands out once its round is over, and keeps back while one is being played. */
    private void style(TextView button, String label, boolean standsOut) {
        Object now = label + standsOut;
        if (now.equals(button.getTag())) return;
        button.setTag(now);
        button.setText(label);
        button.setTextColor(standsOut ? Color.WHITE : CalculatorLayout.CORAL);
        button.setBackground(CalculatorLayout.pill(standsOut ? CalculatorLayout.CORAL : tint));
    }

    private View headerButton(String kind, String description, Runnable action) {
        CompanionIcon button = new CompanionIcon(getContext(), kind, dark ? 0xFFC9CDCF : 0xFF5F6360, .44f);
        button.setContentDescription(description);
        button.setFocusable(true);
        button.setBackground(CalculatorLayout.pill(Color.TRANSPARENT));
        tap(button, action);
        return button;
    }

    private void tap(View view, Runnable action) {
        view.setOnClickListener(v -> {
            if (haptics) v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            action.run();
        });
    }

    /** A rounded shape that shows a touch, for the keys, cells and cards of the games. */
    private RippleDrawable tile(int color, float radius) {
        GradientDrawable mask = new GradientDrawable();
        mask.setCornerRadius(px(radius));
        mask.setColor(Color.WHITE);
        return new RippleDrawable(ColorStateList.valueOf(0x20FF393F), round(color, radius), mask);
    }

    private GradientDrawable round(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(px(radius));
        shape.setColor(color);
        return shape;
    }
}
