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
    /** A touch anywhere else closes the mini calculator. */
    static final String CLOSE_OUTSIDE = "close_outside";
    /** Now and then she says something, in a bubble over her head. */
    static final String CHATTER = "chatter";
    /** Left idle, she plays a prank over whatever is on screen every so often, without being asked. */
    static final String PRANKS = "pranks";
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
    boolean closeOutside() { return preferences.getBoolean(CLOSE_OUTSIDE, true); }
    boolean chatter() { return preferences.getBoolean(CHATTER, true); }
    boolean pranks() { return preferences.getBoolean(PRANKS, true); }
    int size() { return Math.max(SMALL, Math.min(LARGE, preferences.getInt(SIZE, MEDIUM))); }
    float x() { return clamp(preferences.getFloat(X, 1f)); }
    float y() { return clamp(preferences.getFloat(Y, .58f)); }
    boolean offered() { return preferences.getBoolean(OFFERED, false); }

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
