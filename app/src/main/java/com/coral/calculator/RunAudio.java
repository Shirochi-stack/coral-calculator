package com.coral.calculator;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

/**
 * The sounds of Plus run: its effects, which may play over one another, and Halgakos's voice,
 * one line at a time. Both are found among the raw resources by name, {@code run_s_} and
 * {@code run_v_} before it, so a sound that is not there is simply not played.
 */
final class RunAudio {
    private static final String[] EFFECTS = {"jump", "plus", "squish", "hurt", "spring", "flag", "win", "lose", "star", "tap",
            "blow", "fall", "page", "throw", "slash", "shot", "pick", "clang", "buff", "bread", "pop",
            "charm", "combo", "buy", "no", "flap", "bat", "heart"};

    private final Context context;
    private final SoundPool pool;
    private final Map<String, Integer> loaded = new HashMap<>();
    private MediaPlayer voice;
    private boolean on;

    RunAudio(Context context, boolean on) {
        this.context = context.getApplicationContext();
        this.on = on;
        AudioAttributes game = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        pool = new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(game).build();
        for (String name : EFFECTS) {
            int resource = resource("run_s_" + name);
            if (resource != 0) loaded.put(name, pool.load(this.context, resource, 1));
        }
    }

    @SuppressWarnings("DiscouragedApi")
    private int resource(String name) {
        return context.getResources().getIdentifier(name, "raw", context.getPackageName());
    }

    boolean on() { return on; }

    void set(boolean on) {
        this.on = on;
        if (!on) hush();
    }

    /** Plays an effect, at a loudness from 0 to 1. */
    void play(String name, float loudness) {
        Integer sound = loaded.get(name);
        if (on && sound != null) pool.play(sound, loudness, loudness, 1, 0, 1);
    }

    void play(String name) { play(name, .7f); }

    /** Whether a line of hers has a recording. */
    boolean has(String name) { return resource("run_v_" + name) != 0; }

    /** Has her say a line, in place of whatever she was saying. */
    void say(String name) {
        if (!on) return;
        int resource = resource("run_v_" + name);
        if (resource == 0) return;
        hush();
        try {
            voice = MediaPlayer.create(context, resource);
            if (voice == null) return;
            voice.setOnCompletionListener(player -> {
                player.release();
                if (voice == player) voice = null;
            });
            voice.start();
        } catch (RuntimeException unplayable) {
            voice = null;
        }
    }

    boolean speaking() {
        try {
            return voice != null && voice.isPlaying();
        } catch (IllegalStateException released) {
            return false;
        }
    }

    /** Stops her voice. */
    void hush() {
        if (voice == null) return;
        try {
            voice.stop();
        } catch (IllegalStateException stopped) {
            // It had already come to its end.
        }
        voice.release();
        voice = null;
    }

    void close() {
        hush();
        pool.release();
    }
}
