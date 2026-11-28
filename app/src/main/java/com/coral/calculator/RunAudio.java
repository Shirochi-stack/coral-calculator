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
 * {@code run_v_} before it, so a sound that is not there is simply not played. Lines recorded
 * since the base module grew near its limit are in the voices asset pack instead, under
 * {@code voices/}, and are played from there.
 */
final class RunAudio {
    private static final String[] EFFECTS = {"jump", "plus", "squish", "hurt", "spring", "flag", "win", "lose", "star", "tap",
            "blow", "fall", "page", "throw", "slash", "shot", "pick", "clang", "buff", "bread", "pop",
            "charm", "combo", "buy", "no", "flap", "bat", "heart", "form", "door", "quake",
            "crumble", "censor", "fire", "splash", "parry", "unlock"};

    private final Context context;
    private final SoundPool pool;
    private final Map<String, Integer> loaded = new HashMap<>();
    private MediaPlayer voice;
    private boolean on;
    /** The lines in the voices pack, by name, listed the first time one is looked for. */
    private java.util.Set<String> packed;
    static final String PACK_DIR = "voices";

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
    boolean has(String name) { return resource("run_v_" + name) != 0 || packed().contains("run_v_" + name + ".ogg"); }

    private java.util.Set<String> packed() {
        if (packed == null) {
            packed = new java.util.HashSet<>();
            try {
                String[] names = context.getAssets().list(PACK_DIR);
                if (names != null) java.util.Collections.addAll(packed, names);
            } catch (java.io.IOException ignored) {
                // No pack: every line is among the raw resources.
            }
        }
        return packed;
    }

    /** A line from the voices pack, ready to start; or null. */
    private MediaPlayer fromPack(String name) {
        String file = "run_v_" + name + ".ogg";
        if (!packed().contains(file)) return null;
        MediaPlayer player = new MediaPlayer();
        try {
            try (android.content.res.AssetFileDescriptor in = context.getAssets().openFd(PACK_DIR + "/" + file)) {
                player.setDataSource(in.getFileDescriptor(), in.getStartOffset(), in.getLength());
            } catch (java.io.FileNotFoundException compressed) {
                // (Kept compressed in the pack, it cannot be read in place: it is copied out once, and played from the copy.)
                java.io.File copy = new java.io.File(context.getCacheDir(), file);
                if (!copy.isFile()) {
                    try (java.io.InputStream in = context.getAssets().open(PACK_DIR + "/" + file); java.io.OutputStream out = new java.io.FileOutputStream(copy)) {
                        byte[] buffer = new byte[16384];
                        for (int read; (read = in.read(buffer)) > 0; ) out.write(buffer, 0, read);
                    }
                }
                player.setDataSource(copy.getPath());
            }
            player.prepare();
            return player;
        } catch (java.io.IOException | RuntimeException unplayable) {
            player.release();
            return null;
        }
    }

    /** Has her say a line, in place of whatever she was saying. */
    void say(String name) {
        if (!on) return;
        int resource = resource("run_v_" + name);
        if (resource == 0 && !packed().contains("run_v_" + name + ".ogg")) return;
        hush();
        try {
            voice = resource != 0 ? MediaPlayer.create(context, resource) : fromPack(name);
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
