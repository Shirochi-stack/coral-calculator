package com.coral.calculator;

import android.content.Context;
import android.content.SharedPreferences;

/** What the user chose for the Halgakos companion, and where she was last left. */
final class CompanionPrefs {
    static final int SMALL = 0;
    static final int MEDIUM = 1;
    static final int LARGE = 2;

    static final String ENABLED = "enabled";
    /** On a foldable, stay away while the device is folded shut. */
    static final String UNFOLDED_ONLY = "unfolded_only";
    static final String HIDE_IN_APP = "hide_in_app";
    static final String SNAP = "snap";
    static final String FADE = "fade";
    /** How long she is left alone before she fades, within these limits. */
    static final int FADE_SHORTEST_MS = 500;
    static final int FADE_LONGEST_MS = 10000;
    private static final String FADE_AFTER = "fade_after_ms";
    /** The pink-haired Halgakos who moves, rather than the still one that spares the battery. */
    static final String ANIMATED = "animated";
    /** A tap opens the mini calculator and a hold the menu, rather than the other way round. */
    static final String TAP_CALCULATOR = "tap_calculator";
    /** Where a tap opens the mini calculator, it takes two taps close together, so a stray touch does not. */
    static final String DOUBLE_TAP = "double_tap";
    /** The tap that would open the mini calculator opens her games in its place. */
    static final String TAP_GAMES = "tap_games";
    /** The app, opened from its icon, starts on the games and not on the calculator. */
    static final String START_GAMES = "start_games";
    /** A touch anywhere else closes the mini calculator. */
    static final String CLOSE_OUTSIDE = "close_outside";
    /** Now and then she says something, in a bubble over her head. */
    static final String CHATTER = "chatter";
    /** Left idle, she plays a prank over whatever is on screen every so often, without being asked. */
    static final String PRANKS = "pranks";
    /** Plus run plays its sounds and her voice. */
    static final String GAME_SOUND = "game_sound";
    /** The user's birthday, the month and the day as one number (the 14th of March is 314), or 0 if she has not been told. */
    static final String BIRTHDAY = "birthday";
    /** The last day she celebrated it on, the year and the day of the year as one number. */
    private static final String PARTIED = "partied";
    /** The colour of the animated Halgakos's hair: 0 for her own pink, or one of the colours won in Plus run, as {@link Hair} numbers them. */
    static final String HAIR = "hair";
    /** The colour of the user's own mixing, as red, green and blue; and whether she glows in the colour she wears. */
    static final String HAIR_CUSTOM = "hair_custom";
    static final String HAIR_GLOW = "hair_glow";
    /** What she calls the user, in place of "Sloppy". */
    static final String NICKNAME = "nickname";
    static final int NICKNAME_LENGTH = 20;
    static final String SIZE = "size";
    /** Position as a share of the room she has to move in, so it survives a change of screen. */
    static final String X = "x";
    static final String Y = "y";
    /** The permission to display over other apps was offered when the calculator first opened. */
    private static final String OFFERED = "permission_offered";

    private final SharedPreferences preferences;

    CompanionPrefs(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences("companion", Context.MODE_PRIVATE);
    }

    boolean enabled() { return preferences.getBoolean(ENABLED, true); }
    boolean unfoldedOnly() { return preferences.getBoolean(UNFOLDED_ONLY, false); }
    boolean hideInApp() { return preferences.getBoolean(HIDE_IN_APP, true); }
    boolean snap() { return preferences.getBoolean(SNAP, false); }
    boolean fade() { return preferences.getBoolean(FADE, true); }
    int fadeAfterMs() {
        return Math.max(FADE_SHORTEST_MS, Math.min(FADE_LONGEST_MS, preferences.getInt(FADE_AFTER, 1500)));
    }
    boolean animated() { return preferences.getBoolean(ANIMATED, true); }
    boolean tapCalculator() { return preferences.getBoolean(TAP_CALCULATOR, true); }
    boolean doubleTap() { return preferences.getBoolean(DOUBLE_TAP, true); }
    boolean tapGames() { return preferences.getBoolean(TAP_GAMES, true); }
    boolean startGames() { return preferences.getBoolean(START_GAMES, true); }
    boolean closeOutside() { return preferences.getBoolean(CLOSE_OUTSIDE, true); }
    boolean chatter() { return preferences.getBoolean(CHATTER, true); }
    boolean pranks() { return preferences.getBoolean(PRANKS, true); }
    boolean gameSound() { return preferences.getBoolean(GAME_SOUND, true); }
    int size() { return Math.max(SMALL, Math.min(LARGE, preferences.getInt(SIZE, MEDIUM))); }
    float x() { return clamp(preferences.getFloat(X, 1f)); }
    float y() { return clamp(preferences.getFloat(Y, .58f)); }
    boolean offered() { return preferences.getBoolean(OFFERED, false); }

    int hair() { return preferences.getInt(HAIR, Hair.PINK); }

    void setHair(int colour) { preferences.edit().putInt(HAIR, colour).apply(); }

    int hairCustom() { return preferences.getInt(HAIR_CUSTOM, 0xF4A3BE) & 0xFFFFFF; }

    void setHairCustom(int colour) { preferences.edit().putInt(HAIR_CUSTOM, colour & 0xFFFFFF).apply(); }

    boolean hairGlow() { return preferences.getBoolean(HAIR_GLOW, false); }

    int birthday() { return preferences.getInt(BIRTHDAY, 0); }

    void setBirthday(int monthAndDay) { preferences.edit().putInt(BIRTHDAY, monthAndDay).apply(); }

    /** Whether today is the user's birthday. One born on the 29th of February has it on the 28th in a year without one. */
    boolean birthdayToday() { return birthdayOn(birthday(), java.util.Calendar.getInstance()); }

    static boolean birthdayOn(int monthAndDay, java.util.Calendar day) {
        if (monthAndDay <= 0) return false;
        int month = day.get(java.util.Calendar.MONTH) + 1, date = day.get(java.util.Calendar.DAY_OF_MONTH);
        if (monthAndDay == 229 && day.getActualMaximum(java.util.Calendar.DAY_OF_YEAR) < 366) return month == 2 && date == 28;
        return monthAndDay == month * 100 + date;
    }

    private static int today() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        return now.get(java.util.Calendar.YEAR) * 1000 + now.get(java.util.Calendar.DAY_OF_YEAR);
    }

    /** Whether she has already celebrated today; and remembers that she now has. */
    boolean partiedToday() { return preferences.getInt(PARTIED, 0) == today(); }

    void setPartied() { preferences.edit().putInt(PARTIED, today()).apply(); }

    /** What she calls the user: "Sloppy" unless they gave her another name. */
    String nickname() { return Chatter.nickname(preferences.getString(NICKNAME, null)); }

    void setNickname(String name) { preferences.edit().putString(NICKNAME, Chatter.nickname(name)).apply(); }

    /** A line of hers, with the user called what they asked to be called. */
    String named(String line) { return Chatter.named(line, nickname()); }

    void set(String key, boolean value) { preferences.edit().putBoolean(key, value).apply(); }
    void setSize(int size) { preferences.edit().putInt(SIZE, size).apply(); }
    void setFadeAfterMs(int milliseconds) { preferences.edit().putInt(FADE_AFTER, milliseconds).apply(); }
    void setPosition(float x, float y) { preferences.edit().putFloat(X, clamp(x)).putFloat(Y, clamp(y)).apply(); }
    void setOffered() { preferences.edit().putBoolean(OFFERED, true).apply(); }

    private static float clamp(float value) { return Float.isNaN(value) ? 0 : Math.max(0, Math.min(1, value)); }
}
