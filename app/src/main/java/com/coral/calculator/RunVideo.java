package com.coral.calculator;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * A clip of video played over a game, by the phone's own player: a cutscene. It lies over
 * whatever is under it, black, with the picture in the middle and its proportions kept. A
 * picture nearly the shape of the screen fills it, and loses a little at two of its edges
 * (an upright clip on an upright phone, which is a little taller than the clip); one of
 * another shape is shown whole, as large as it will go, with black beside it (a clip wider
 * than tall on an upright phone has black above and below, and is not turned). Its sound is
 * in its file, and is heard or not as the game's sound is on or off. What is said in it may
 * be shown under it in words.
 *
 * A tap ends it, and so does the Back key. So does its own end; and so does anything going wrong, at any point,
 * which is told apart from the other two: whoever started it is told once, and only once,
 * whether it was played (to its end, or until it was tapped away) or could not be. It never
 * throws: a phone that cannot play it is one where it could not be played.
 *
 * It is laid over a view by being added to that view's parent; or, where that view is a
 * window of its own with no parent, as her games are when they are played over other apps,
 * as another such window over it, the same size and in the same place. It takes itself away
 * when it is over. Everything here happens on the thread the views live on.
 */
final class RunVideo extends FrameLayout implements TextureView.SurfaceTextureListener {
    /** Told once, when it is over: whether it was played, or could not be. */
    interface Ends {
        void ended(boolean played);
    }

    private final int resource;
    private final boolean sound;
    private final Ends ends;
    private final TextureView picture;
    private MediaPlayer player;
    private Surface surface;
    private WindowManager window;
    private boolean over, started;
    private int wide, tall;
    /** How much of the picture may be lost at two of its edges, all told, for it to fill the screen: beyond that it is shown whole. */
    private static final float LOST = .26f;

    /** A clip from the raw resources, with its sound or without, and words to show under it or none. */
    RunVideo(Context context, int resource, boolean sound, String words, Ends ends) {
        super(context);
        this.resource = resource;
        this.sound = sound;
        this.ends = ends;
        setBackgroundColor(Color.BLACK);
        setClickable(true);
        picture = new TextureView(context);
        picture.setSurfaceTextureListener(this);
        addView(picture, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        if (words != null && !words.isEmpty()) {
            TextView said = new TextView(context);
            said.setText(words);
            said.setTextColor(Color.WHITE);
            said.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            said.setGravity(Gravity.CENTER);
            said.setShadowLayer(6, 0, 1, Color.BLACK);
            int pad = Math.round(16 * context.getResources().getDisplayMetrics().density);
            said.setPadding(pad, pad / 2, pad, pad / 2);
            said.setBackgroundColor(0x66000000);
            LayoutParams under = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
            under.bottomMargin = pad * 3;
            addView(said, under);
        }
        // A tap is enough of it.
        setOnClickListener(view -> finish(true, "tapped"));
        // And one that has not begun within a few seconds is not going to.
        postDelayed(() -> {
            if (!started) finish(false, "never began");
        }, 5000);
    }

    /** Lays a clip over a view, if that view has somewhere for it to be laid; if not, it could not be played. */
    static RunVideo over(android.view.View under, int resource, boolean sound, String words, Ends ends) {
        try {
            RunVideo video = new RunVideo(under.getContext(), resource, sound, words, ends);
            if (under.getParent() instanceof ViewGroup) {
                // (Over it in height as well as in order: a view that is raised is drawn, and touched, above one added after it that is not.)
                video.setZ(under.getZ() + 1);
                ((ViewGroup) under.getParent()).addView(video, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else if (under.getLayoutParams() instanceof WindowManager.LayoutParams) {
                // A window of its own: the clip is one more, like it, and drawn by the phone's graphics, which a picture that moves needs.
                WindowManager.LayoutParams like = new WindowManager.LayoutParams();
                like.copyFrom((WindowManager.LayoutParams) under.getLayoutParams());
                like.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                like.setTitle("Halgakos's cutscene");
                WindowManager manager = (WindowManager) under.getContext().getSystemService(Context.WINDOW_SERVICE);
                manager.addView(video, like);
                video.window = manager;
            } else {
                throw new IllegalStateException("nowhere to lay it");
            }
            return video;
        } catch (RuntimeException failed) {
            ends.ended(false);
            return null;
        }
    }

    /** It is ended from outside, as a tap would end it: when the game is put away, say. */
    void skip() { finish(true, "put away"); }

    @Override public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        if (over) return;
        try {
            player = new MediaPlayer();
            try (AssetFileDescriptor file = getResources().openRawResourceFd(resource)) {
                player.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            }
            surface = new Surface(texture);
            player.setSurface(surface);
            player.setVolume(sound ? 1 : 0, sound ? 1 : 0);
            player.setOnVideoSizeChangedListener((which, videoWide, videoTall) -> {
                wide = videoWide;
                tall = videoTall;
                fit();
            });
            player.setOnPreparedListener(which -> {
                if (over) return;
                started = true;
                which.start();
                if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "clip begins");
            });
            player.setOnCompletionListener(which -> finish(true, "its end"));
            player.setOnErrorListener((which, what, extra) -> {
                finish(started, "the player's error " + what + " " + extra);
                return true;
            });
            player.prepareAsync();
        } catch (Exception failed) {
            finish(false, "could not be opened: " + failed);
        }
    }

    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) { fit(); }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        finish(started, "its picture was taken away");
        return true;
    }

    @Override public void onSurfaceTextureUpdated(SurfaceTexture texture) {}

    @Override public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        // (Where it is a window of its own, the keys come to it: Back is a tap.)
        if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK || event.getKeyCode() == android.view.KeyEvent.KEYCODE_ESCAPE) {
            if (event.getAction() == android.view.KeyEvent.ACTION_UP) finish(true, "Back");
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /** The scale at which a picture is shown in a space: filling it, if little is lost by that; if not, whole. */
    static float scale(int wide, int tall, int width, int height) {
        float across = width / (float) wide, down = height / (float) tall;
        float whole = Math.min(across, down), filling = Math.max(across, down);
        return 1 - whole / filling <= LOST ? filling : whole;
    }

    /** The picture in the middle of the view with its proportions kept, filling it or whole. */
    private void fit() {
        int width = picture.getWidth(), height = picture.getHeight();
        if (wide <= 0 || tall <= 0 || width <= 0 || height <= 0) return;
        float scale = scale(wide, tall, width, height);
        Matrix matrix = new Matrix();
        // (The view stretches the picture to itself: that is undone, and then it is made the size it should be.)
        matrix.setScale(wide * scale / width, tall * scale / height, width / 2f, height / 2f);
        picture.setTransform(matrix);
    }

    /**
     * It is put away in order: the player is stopped and let go, then what it drew on, and only then, on the next turn of the
     * loop and not from inside whatever tap or news of the player this was called from, the view itself is taken off the screen.
     */
    private void finish(boolean played, String why) {
        if (over) return;
        over = true;
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "clip over: " + why + (played ? "" : " (not played)"));
        MediaPlayer old = player;
        player = null;
        try {
            if (old != null) {
                old.setOnPreparedListener(null);
                old.setOnVideoSizeChangedListener(null);
                old.setOnCompletionListener(null);
                old.setOnErrorListener(null);
                if (started) old.stop();
            }
        } catch (RuntimeException ignored) {
            // It is being put away: whatever state it was in, it is gone.
        }
        try {
            if (old != null) old.release();
        } catch (RuntimeException ignored) {
            // The same.
        }
        try {
            if (surface != null) surface.release();
        } catch (RuntimeException ignored) {
            // The same.
        }
        surface = null;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            try {
                if (window != null) window.removeView(this);
                else if (getParent() instanceof ViewGroup) ((ViewGroup) getParent()).removeView(this);
            } catch (RuntimeException ignored) {
                // Already gone.
            }
        });
        ends.ended(played);
    }
}
