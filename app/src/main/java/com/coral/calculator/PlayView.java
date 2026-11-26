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

import java.util.List;

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
    /** The menu's tiles, two to a row under Plus run, which has the top of the menu to itself: the games they open, and their pictures. */
    private static final int[] TILE_GAMES = {PlaySession.TIC, PlaySession.RPS, PlaySession.PAIRS, PlaySession.SUMS,
            PlaySession.CHESS, PlaySession.LAST_CARD, PlaySession.MINES, PlaySession.TAG, PlaySession.RAIN,
            PlaySession.ORDER, PlaySession.PRANK};
    private static final String[] TILE_ICONS = {CompanionIcon.GRID, CompanionIcon.SCISSORS, CompanionIcon.CARDS,
            CompanionIcon.SUMS, CompanionIcon.PAWN, CompanionIcon.FAN, CompanionIcon.MINE,
            CompanionIcon.TARGET, CompanionIcon.RAIN, CompanionIcon.BUBBLES, CompanionIcon.SPARK};
    /** The four signs of Last card, as they are written and the colour each is drawn in. */
    private static final String[] SIGNS = {"+", "−", "×", "÷"};
    private static final int[] SIGN_COLOURS = {0xFFFF393F, 0xFF1E9AA6, 0xFF8257D8, 0xFFE0921A};
    /** The chess pieces from pawn to king, as the solid figures of the chess symbols, kept from turning into emoji. */
    private static final String[] CHESSMEN = {"", "♟︎", "♞︎", "♝︎", "♜︎", "♛︎", "♚︎"};
    private static final String[] CHESSMEN_NAMES = {"", "pawn", "knight", "bishop", "rook", "queen", "king"};

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
                    : game == PlaySession.PAIRS ? pairs() : game == PlaySession.SUMS ? sums()
                    : game == PlaySession.CHESS ? chess() : game == PlaySession.LAST_CARD ? lastCard() : menu();
            body.addView(content, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            title.setText(PlaySession.name(game));
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

    /**
     * Plus run across the top, as the game she recommends, and eleven tiles under it: six
     * games played on this card, four all over the screen, and her pranks in a row of their own.
     */
    private View menu() {
        LinearLayout grid = new LinearLayout(getContext());
        grid.setOrientation(VERTICAL);
        LinearLayout featured = new LinearLayout(getContext());
        featured.setGravity(Gravity.CENTER_VERTICAL);
        featured.setPadding(px(11), 0, px(9), 0);
        featured.setBackground(tile(CalculatorLayout.CORAL, 13));
        featured.addView(new CompanionIcon(getContext(), CompanionIcon.LEAP, Color.WHITE, 1f), new LayoutParams(px(22), px(22)));
        LinearLayout featuredWords = column();
        TextView featuredName = text(PlaySession.name(PlaySession.RUN), 13, Color.WHITE, true);
        featuredName.setMaxLines(1);
        featuredWords.addView(featuredName, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView featuredNote = text("", 9.5f, 0xFFFFE9EA, false);
        featuredNote.setMaxLines(1);
        featuredNote.setEllipsize(TextUtils.TruncateAt.END);
        featuredWords.addView(featuredNote, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LayoutParams featuredWordsParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        featuredWordsParams.leftMargin = px(9);
        featured.addView(featuredWords, featuredWordsParams);
        TextView badge = text("Recommended", 8.5f, CalculatorLayout.CORAL, true);
        badge.setMaxLines(1);
        badge.setPadding(px(7), px(2.5f), px(7), px(2.5f));
        badge.setBackground(round(Color.WHITE, 9));
        featured.addView(badge, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        featured.setFocusable(true);
        tap(featured, () -> host.onStage(PlaySession.RUN));
        grid.addView(featured, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.25f));
        // Page brawl under it, recommended as well.
        TextView fightNote = text("", 9.5f, 0xFFFFE9EA, false);
        LayoutParams fightParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.25f);
        fightParams.topMargin = px(4);
        grid.addView(recommended(CompanionIcon.CLASH, PlaySession.FIGHT, fightNote), fightParams);
        TextView[] notes = new TextView[TILE_ICONS.length];
        for (int row = 0; row < (TILE_ICONS.length + 1) / 2; row++) {
            LinearLayout line = new LinearLayout(getContext());
            // The last tile, with no other to stand beside, has its row to itself.
            for (int column = 0; column < 2 && row * 2 + column < TILE_ICONS.length; column++) {
                int index = row * 2 + column, game = TILE_GAMES[index];
                // Its picture to the left, its name over a line about it to the right.
                LinearLayout tile = new LinearLayout(getContext());
                tile.setGravity(Gravity.CENTER_VERTICAL);
                tile.setPadding(px(9), 0, px(6), 0);
                tile.setBackground(tile(key, 13));
                tile.addView(new CompanionIcon(getContext(), TILE_ICONS[index], CalculatorLayout.CORAL, 1f),
                        new LayoutParams(px(18), px(18)));
                LinearLayout words = column();
                TextView name = text(PlaySession.name(game), 11.5f, ink, true);
                name.setMaxLines(1);
                // The longest name gives up a little of its size to stay on one line.
                name.setAutoSizeTextTypeUniformWithConfiguration(px(8), px(11.5f), Math.max(1, px(.5f)),
                        TypedValue.COMPLEX_UNIT_PX);
                name.setGravity(Gravity.CENTER_VERTICAL);
                words.addView(name, new LayoutParams(LayoutParams.MATCH_PARENT, px(15)));
                notes[index] = text("", 9.5f, muted, false);
                notes[index].setMaxLines(1);
                notes[index].setEllipsize(TextUtils.TruncateAt.END);
                LayoutParams noteParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                noteParams.topMargin = px(1);
                words.addView(notes[index], noteParams);
                LayoutParams wordsParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
                wordsParams.leftMargin = px(7);
                tile.addView(words, wordsParams);
                tile.setFocusable(true);
                tap(tile, () -> {
                    if (PlaySession.onCard(game)) session.open(game); else host.onStage(game);
                });
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
                if (column > 0) params.leftMargin = px(GAP);
                line.addView(tile, params);
            }
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
            params.topMargin = px(4);
            grid.addView(line, params);
        }
        updater = () -> {
            fightNote.setText(note(PlaySession.FIGHT));
            ((View) fightNote.getParent().getParent()).setContentDescription(PlaySession.name(PlaySession.FIGHT) + ", recommended. " + fightNote.getText());
            featuredNote.setText(note(PlaySession.RUN));
            featured.setContentDescription(PlaySession.name(PlaySession.RUN) + ", recommended. " + featuredNote.getText());
            for (int index = 0; index < notes.length; index++) {
                notes[index].setText(note(TILE_GAMES[index]));
                ((View) notes[index].getParent().getParent()).setContentDescription(
                        PlaySession.name(TILE_GAMES[index]) + ". " + notes[index].getText());
            }
        };
        return grid;
    }

    /** A game she recommends, across the whole card: its picture, its name over a line about it, and the word for it. */
    private View recommended(String icon, int game, TextView note) {
        LinearLayout banner = new LinearLayout(getContext());
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setPadding(px(11), 0, px(9), 0);
        banner.setBackground(tile(CalculatorLayout.CORAL, 13));
        banner.addView(new CompanionIcon(getContext(), icon, Color.WHITE, 1f), new LayoutParams(px(22), px(22)));
        LinearLayout words = column();
        TextView name = text(PlaySession.name(game), 13, Color.WHITE, true);
        name.setMaxLines(1);
        words.addView(name, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        note.setMaxLines(1);
        note.setEllipsize(TextUtils.TruncateAt.END);
        words.addView(note, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LayoutParams wordsParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        wordsParams.leftMargin = px(9);
        banner.addView(words, wordsParams);
        TextView badge = text("Recommended", 8.5f, CalculatorLayout.CORAL, true);
        badge.setMaxLines(1);
        badge.setPadding(px(7), px(2.5f), px(7), px(2.5f));
        badge.setBackground(round(Color.WHITE, 9));
        banner.addView(badge, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        banner.setFocusable(true);
        tap(banner, () -> host.onStage(game));
        return banner;
    }

    /** The line under a game's name on its tile: how the player has done at it, or where it is played. */
    private String note(int game) {
        switch (game) {
            case PlaySession.FIGHT: {
                // How many fights have been won, and with how many of the five the ladder has been topped.
                int cleared = 0;
                for (int fighter = 0; fighter < Fight.FIGHTERS; fighter++) if (session.tally("fight_cleared_" + fighter) > 0) cleared++;
                int won = session.tally("fight_won");
                return won == 0 ? "Five fighters · a ladder" : "Won " + won + " · Topped with " + cleared + " of " + Fight.FIGHTERS;
            }
            case PlaySession.TIC: return record(session.tally("tic_won"), session.tally("tic_lost"), session.tally("tic_drawn"));
            case PlaySession.RPS: return record(session.tally("rps_won"), session.tally("rps_lost"), 0);
            case PlaySession.CHESS: return record(session.tally("chess_won"), session.tally("chess_lost"), session.tally("chess_drawn"));
            case PlaySession.LAST_CARD: return record(session.tally("cards_won"), session.tally("cards_lost"), 0);
            case PlaySession.RUN: {
                // How far through its thirty lands the player has got, and the stars won on the way.
                int stars = 0;
                for (int land = 1; land <= RunLevels.COUNT; land++) stars += session.tally("run_stars_" + land);
                int open = Math.max(1, Math.min(RunLevels.COUNT, session.tally("run_open")));
                return stars == 0 ? RunStory.TITLE + " · " + RunLevels.COUNT + " lands"
                        : "Land " + open + " of " + RunLevels.COUNT + " · " + stars + (stars == 1 ? " star" : " stars");
            }
            case PlaySession.PAIRS: return best("pairs_best", " turns", "On this card");
            case PlaySession.SUMS: return best("sums_best", " right", "On this card");
            case PlaySession.TAG: return best("tag_best", " caught", "Whole screen");
            case PlaySession.RAIN: return best("rain_best", " caught", "Whole screen");
            case PlaySession.ORDER: {
                int tenths = session.tally("order_best");
                return tenths == 0 ? "Whole screen" : "Best: " + PlayStage.seconds(tenths) + "s";
            }
            case PlaySession.MINES: {
                int seconds = session.tally("mines_best");
                return seconds == 0 ? "Whole screen" : "Best: " + PlayStage.clock(seconds);
            }
            default: return "Whole screen";
        }
    }

    private static String record(int won, int lost, int drawn) {
        return won + lost + drawn == 0 ? "On this card" : "Won " + won + " · Lost " + lost;
    }

    // Last card

    private View lastCard() {
        LinearLayout root = column();
        CardTable table = new CardTable(getContext());
        root.addView(table, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        TextView note = footnote();
        note.setMaxLines(1);
        TextView again = button("");
        tap(again, session::cardsAgain);
        LinearLayout footer = new LinearLayout(getContext());
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.addView(note, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        footer.addView(again, new LayoutParams(LayoutParams.WRAP_CONTENT, px(26)));
        LayoutParams footerParams = new LayoutParams(LayoutParams.MATCH_PARENT, px(28));
        footerParams.topMargin = px(4);
        root.addView(footer, footerParams);
        updater = () -> {
            LastCard game = session.cards();
            if (game == null) return;
            boolean over = game.winner() != LastCard.NOBODY;
            table.invalidate();
            table.setContentDescription("Last card. You hold " + game.hand(LastCard.PLAYER).size() + " cards, Halgakos "
                    + game.hand(LastCard.HALGAKOS).size() + ". " + (over ? "The game is over."
                    : game.turn() == LastCard.PLAYER ? "Your turn." : "Her turn."));
            note.setText("Won " + session.tally("cards_won") + " · Lost " + session.tally("cards_lost"));
            style(again, over ? "Play again" : "Deal again", over);
        };
        return root;
    }

    /**
     * The table of Last card: her cards face down along the top, the pile to draw from and
     * the card last put down in the middle, and the player's hand along the bottom. A touch
     * on a card of the player's that fits puts it down; a touch on the pile draws.
     */
    private final class CardTable extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint words = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF box = new RectF(), pile = new RectF();
        private final RectF[] choices = {new RectF(), new RectF(), new RectF(), new RectF()};
        /** Where each card of the player's hand was last drawn: its left edge. */
        private float[] lefts = {};
        private float handTop, cardWide, cardTall;

        CardTable(Context context) {
            super(context);
            words.setTextAlign(Paint.Align.CENTER);
            words.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            setFocusable(true);
        }

        @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            lefts = new float[64];
        }

        @Override protected void onDraw(Canvas canvas) {
            LastCard game = session.cards();
            if (game == null || lefts.length == 0) return;
            float width = getWidth(), height = getHeight();
            boolean mine = game.turn() == LastCard.PLAYER && game.winner() == LastCard.NOBODY;

            // Her hand: backs, overlapping, with how many she holds.
            float small = px(22), smallTall = px(31);
            int hers = game.hand(LastCard.HALGAKOS).size();
            float spread = hers <= 1 ? 0 : Math.min(small * .55f, (width * .62f - small) / (hers - 1));
            float start = (width - (small + spread * (hers - 1))) / 2;
            for (int card = 0; card < hers; card++) back(canvas, start + spread * card, 0, small, smallTall);
            words.setColor(muted);
            words.setTextSize(px(10.5f));
            canvas.drawText(hers == 1 ? "Halgakos: last card" : "Halgakos: " + hers + " cards", width / 2, smallTall + px(13), words);

            // The middle: the pile to draw from, and the card last put down.
            cardWide = px(44);
            cardTall = px(62);
            float middle = smallTall + px(22) + (height - smallTall - px(22) - cardTall - px(18) - cardTall) / 2;
            pile.set(width / 2 - cardWide - px(9), middle, width / 2 - px(9), middle + cardTall);
            back(canvas, pile.left, pile.top, cardWide, cardTall);
            words.setColor(Color.WHITE);
            words.setTextSize(px(10));
            canvas.drawText(mine && game.drawn() != LastCard.NONE ? "Keep" : "Draw", pile.centerX(), pile.centerY() + px(3.5f), words);
            face(canvas, game.top(), width / 2 + px(9), middle, cardWide, cardTall, 1);
            // The sign to follow, when the card last put down has none of its own.
            if (LastCard.isAny(game.top())) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(SIGN_COLOURS[game.wanted()]);
                float x = width / 2 + px(9) + cardWide + px(16), y = middle + cardTall / 2;
                canvas.drawCircle(x, y, px(11), paint);
                words.setColor(Color.WHITE);
                words.setTextSize(px(15));
                canvas.drawText(SIGNS[game.wanted()], x, y + px(5.2f), words);
            }

            // The player's hand, or the four signs to choose from after an any-sign card.
            handTop = height - cardTall - px(2);
            if (session.cardsNaming()) {
                float each = (width - px(8) * 3) / 4;
                for (int sign = 0; sign < LastCard.SIGNS; sign++) {
                    choices[sign].set((each + px(8)) * sign, handTop + px(8), (each + px(8)) * sign + each, handTop + cardTall - px(8));
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(SIGN_COLOURS[sign]);
                    canvas.drawRoundRect(choices[sign], px(10), px(10), paint);
                    words.setColor(Color.WHITE);
                    words.setTextSize(px(24));
                    canvas.drawText(SIGNS[sign], choices[sign].centerX(), choices[sign].centerY() + px(8.5f), words);
                }
                return;
            }
            List<Integer> hand = game.hand(LastCard.PLAYER);
            int held = Math.min(hand.size(), lefts.length);
            float step = held <= 1 ? 0 : Math.min(cardWide + px(3), (width - cardWide) / (held - 1));
            float from = (width - (cardWide + step * (held - 1))) / 2;
            for (int place = 0; place < held; place++) {
                boolean fits = mine && game.playable(LastCard.PLAYER, place);
                lefts[place] = from + step * place;
                // A card that may go down stands a little proud of the rest.
                face(canvas, hand.get(place), lefts[place], handTop - (fits ? px(7) : 0), cardWide, cardTall, fits || !mine ? 1 : .45f);
            }
        }

        /** The back of a card. */
        private void back(Canvas canvas, float left, float top, float wide, float tall) {
            box.set(left, top, left + wide, top + tall);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(CalculatorLayout.CORAL);
            canvas.drawRoundRect(box, wide * .16f, wide * .16f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1, wide * .05f));
            paint.setColor(dark ? 0xFF1D2023 : Color.WHITE);
            canvas.drawRoundRect(box, wide * .16f, wide * .16f, paint);
        }

        /** The face of a card: its sign in a corner, and its number or what it does in the middle. */
        private void face(Canvas canvas, int card, float left, float top, float wide, float tall, float strength) {
            int alpha = Math.round(255 * strength);
            boolean any = LastCard.isAny(card);
            int colour = any ? ink : SIGN_COLOURS[LastCard.sign(card)], what = LastCard.what(card);
            box.set(left, top, left + wide, top + tall);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF2F3438 : Color.WHITE);
            canvas.drawRoundRect(box, wide * .16f, wide * .16f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1.5f, wide * .06f));
            paint.setColor(any ? muted : colour);
            paint.setAlpha(alpha);
            canvas.drawRoundRect(box, wide * .16f, wide * .16f, paint);
            paint.setAlpha(255);
            if (any) {
                // All four signs, one to a corner of the middle.
                words.setTextSize(wide * .36f);
                for (int sign = 0; sign < LastCard.SIGNS; sign++) {
                    words.setColor(SIGN_COLOURS[sign]);
                    words.setAlpha(alpha);
                    canvas.drawText(SIGNS[sign], left + wide * (sign % 2 == 0 ? .32f : .68f), top + tall * (sign < 2 ? .42f : .72f), words);
                }
                words.setAlpha(255);
                return;
            }
            words.setColor(colour);
            words.setAlpha(alpha);
            words.setTextSize(wide * .3f);
            canvas.drawText(SIGNS[LastCard.sign(card)], left + wide * .22f, top + wide * .34f, words);
            if (what <= 9) {
                words.setTextSize(wide * .62f);
                canvas.drawText(String.valueOf(what), left + wide / 2, top + tall * .72f, words);
            } else {
                // What it does to the other side, in words, over how many.
                words.setTextSize(wide * .22f);
                canvas.drawText(what == LastCard.SKIP ? "skip" : "take", left + wide / 2, top + tall * .52f, words);
                words.setTextSize(wide * .4f);
                canvas.drawText(what == LastCard.SKIP ? "→" : "2", left + wide / 2, top + tall * .84f, words);
            }
            words.setAlpha(255);
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent event) {
            LastCard game = session.cards();
            if (event.getActionMasked() != android.view.MotionEvent.ACTION_UP || game == null) return true;
            float x = event.getX(), y = event.getY();
            if (session.cardsNaming()) {
                for (int sign = 0; sign < LastCard.SIGNS; sign++) {
                    if (choices[sign].contains(x, y)) {
                        touched();
                        session.cardsName(sign);
                        return true;
                    }
                }
            }
            if (pile.contains(x, y)) {
                touched();
                session.cardsDraw();
            } else if (y >= handTop - px(8) && !session.cardsNaming()) {
                // The card on top where the finger came down: the last one that begins to its left.
                int held = Math.min(game.hand(LastCard.PLAYER).size(), lefts.length);
                for (int place = held - 1; place >= 0; place--) {
                    if (x >= lefts[place] && x <= lefts[place] + cardWide) {
                        touched();
                        session.cardsTap(place);
                        break;
                    }
                }
            }
            return true;
        }

        private void touched() {
            if (haptics) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            performClick();
        }

        @Override public boolean performClick() { return super.performClick(); }
    }

    // Chess

    private View chess() {
        LinearLayout root = column();
        ChessBoard board = new ChessBoard(getContext());
        FrameLayout stage = new FrameLayout(getContext());
        stage.addView(board, new FrameLayout.LayoutParams(px(224), px(224), Gravity.CENTER));
        root.addView(stage, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        // A lower row than the other games have: the board needs the height.
        TextView note = footnote();
        note.setMaxLines(1);
        TextView again = button("");
        tap(again, session::chessAgain);
        LinearLayout footer = new LinearLayout(getContext());
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.addView(note, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        footer.addView(again, new LayoutParams(LayoutParams.WRAP_CONTENT, px(26)));
        LayoutParams footerParams = new LayoutParams(LayoutParams.MATCH_PARENT, px(28));
        footerParams.topMargin = px(4);
        root.addView(footer, footerParams);
        updater = () -> {
            Chess game = session.chess();
            if (game == null) return;
            boolean over = game.result() != Chess.ONGOING;
            board.refresh();
            board.setContentDescription("Chess board. You have white. " + (over ? "The game is over."
                    : session.chessHerTurn() ? "Halgakos is thinking." : "Your move."));
            note.setText("Won " + session.tally("chess_won") + " · Lost " + session.tally("chess_lost")
                    + " · Drawn " + session.tally("chess_drawn"));
            style(again, over ? "Play again" : "Restart", over);
        };
        return root;
    }

    /** The chess board: white at the bottom, the player's. A touch picks a piece up, and another puts it down. */
    private final class ChessBoard extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint figure = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int light = dark ? 0xFF4B5157 : 0xFFF6ECEA, deep = dark ? 0xFF7A4149 : 0xFFEFA5AB;
        /** The squares the piece in hand may go to. */
        private int[] goals = {};

        /** Brings the board up to date with the game. */
        void refresh() {
            Chess game = session.chess();
            int picked = session.chessPicked();
            goals = game == null || picked == PlaySession.NO_SQUARE ? new int[0] : game.goals(picked);
            invalidate();
        }

        ChessBoard(Context context) {
            super(context);
            figure.setTextAlign(Paint.Align.CENTER);
            figure.setStrokeJoin(Paint.Join.ROUND);
            setBackground(round(light, 8));
            setClipToOutline(true);
            setFocusable(true);
        }

        @Override protected void onDraw(Canvas canvas) {
            Chess game = session.chess();
            if (game == null) return;
            float side = getWidth() / 8f;
            int picked = session.chessPicked(), last = game.lastMove();
            int checked = game.inCheck() ? game.king() : PlaySession.NO_SQUARE;
            figure.setTextSize(side * .8f);
            float baseline = side / 2 - (figure.ascent() + figure.descent()) / 2;
            for (int square = 0; square < 64; square++) {
                float left = (square & 7) * side, top = (square >> 3) * side;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(((square >> 3) + (square & 7)) % 2 == 0 ? light : deep);
                canvas.drawRect(left, top, left + side, top + side, paint);
                // The move just made, the piece in hand, and a king in check are each lit.
                int wash = square == checked ? 0x99FF2D38 : square == picked ? 0x80FFD24A
                        : last != Chess.NO_MOVE && (square == Chess.from(last) || square == Chess.to(last)) ? 0x55FFD24A : 0;
                if (wash != 0) {
                    paint.setColor(wash);
                    canvas.drawRect(left, top, left + side, top + side, paint);
                }
                int piece = game.at(square);
                if (piece != 0) {
                    // One figure for both sides: white's filled white with a dark edge, black's dark with a light one.
                    String man = CHESSMEN[Math.abs(piece)];
                    figure.setStyle(Paint.Style.STROKE);
                    figure.setStrokeWidth(side * (piece > 0 ? .09f : .05f));
                    figure.setColor(piece > 0 ? 0xFF2B2628 : 0xFFF4F0F0);
                    canvas.drawText(man, left + side / 2, top + baseline, figure);
                    figure.setStyle(Paint.Style.FILL);
                    figure.setColor(piece > 0 ? Color.WHITE : 0xFF2B2628);
                    canvas.drawText(man, left + side / 2, top + baseline, figure);
                }
            }
            // Where the piece in hand may go: a dot on an empty square, a ring round a piece it would take.
            for (int goal : goals) {
                float x = (goal & 7) * side + side / 2, y = (goal >> 3) * side + side / 2;
                paint.setColor(0xCCFF393F);
                if (game.at(goal) == 0) {
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x, y, side * .15f, paint);
                } else {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(side * .09f);
                    canvas.drawCircle(x, y, side * .42f, paint);
                }
            }
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent event) {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP && getWidth() > 0) {
                int file = (int) (event.getX() * 8 / getWidth()), row = (int) (event.getY() * 8 / getHeight());
                if (file >= 0 && file < 8 && row >= 0 && row < 8) {
                    if (haptics) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    performClick();
                    Chess game = session.chess();
                    int piece = game == null ? 0 : game.at(row * 8 + file);
                    announceForAccessibility((char) ('a' + file) + String.valueOf(8 - row)
                            + (piece == 0 ? "" : (piece > 0 ? ", white " : ", black ") + CHESSMEN_NAMES[Math.abs(piece)]));
                    session.chessTap(row * 8 + file);
                }
            }
            return true;
        }

        @Override public boolean performClick() { return super.performClick(); }
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
