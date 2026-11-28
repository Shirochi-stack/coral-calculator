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
 * A film is a longer one, of another make: its picture is a file without sound, and its sound
 * a file of its own, so that one picture can be had with several sounds. The two are made
 * ready apart and begun together, when both can begin; while they play the sound is kept to
 * the picture, and set back to it if it is ever more than a tenth of a second away. A film
 * whose sound cannot be played is shown without it. What is heard in a film may be written
 * under it line by line, each line at its own time: in the black under the picture where
 * there is black enough, and if not, low on the picture, clear of what the picture itself
 * has written on it. And a film is shown whole unless next to nothing would be lost by its
 * filling the screen: it has names written at its edge, and five people side by side, which
 * a clip of one face has not.
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
    /** A film's sound, a file of its own (or none); what is written under it, line by line (or nothing); and where that is shown. */
    private final int voiceResource;
    private final boolean film;
    private final java.util.List<RunFilms.Line> lines;
    private TextView caption;
    /** How large what is written under it is when it fits (the size is taken down for a line that would be more than three lines, so that none runs off the screen). */
    private float captionSize;
    private MediaPlayer voice;
    private boolean pictureReady, voiceReady, voiceGone;
    /** How many times it has looked at itself; how long its sound takes to go on after being set to a moment, as far as that has been seen; and whether it was set at the last look. */
    private int ticks, lead;
    private boolean set;
    private final Runnable ticker = this::tick;
    private MediaPlayer player;
    private Surface surface;
    private WindowManager window;
    private boolean over, started;
    private int wide, tall;
    /** How much of the picture may be lost at two of its edges, all told, for it to fill the screen: beyond that it is shown whole. */
    private static final float LOST = .26f;
    /** And how much of a film's: next to nothing. */
    static final float FILM_LOST = .08f;

    /** A clip from the raw resources, with its sound or without, and words to show under it or none. */
    RunVideo(Context context, int resource, boolean sound, String words, Ends ends) {
        this(context, resource, sound, words, false, 0, null, ends);
    }

    private RunVideo(Context context, int resource, boolean sound, String words, boolean film, int voiceResource, java.util.List<RunFilms.Line> lines, Ends ends) {
        super(context);
        this.resource = resource;
        this.sound = sound;
        this.ends = ends;
        this.film = film;
        this.voiceResource = voiceResource;
        this.lines = lines;
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
        if (lines != null && !lines.isEmpty()) {
            // What is heard, written under it: put where it belongs once the picture's size is known.
            caption = new TextView(context);
            caption.setTextColor(Color.WHITE);
            caption.setGravity(Gravity.CENTER);
            caption.setMaxLines(3);
            caption.setShadowLayer(5, 0, 1, Color.BLACK);
            int pad = Math.round(10 * context.getResources().getDisplayMetrics().density);
            caption.setPadding(pad, pad / 3, pad, pad / 3);
            caption.setBackgroundColor(0x73000000);
            caption.setVisibility(INVISIBLE);
            addView(caption, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
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
        return laid(under, ends, () -> new RunVideo(under.getContext(), resource, sound, words, ends));
    }

    /** Lays a film over a view: its picture, its sound (0 for none), and what is written under it (or nothing). */
    static RunVideo film(android.view.View under, int picture, int voice, java.util.List<RunFilms.Line> lines, Ends ends) {
        return laid(under, ends, () -> new RunVideo(under.getContext(), picture, false, null, true, voice, lines, ends));
    }

    private interface Maker {
        RunVideo made();
    }

    private static RunVideo laid(android.view.View under, Ends ends, Maker maker) {
        try {
            RunVideo video = maker.made();
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
            try (AssetFileDescriptor file = FilmFiles.open(getContext(), resource)) {
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
                if (film) {
                    pictureReady = true;
                    begin();
                    return;
                }
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
            return;
        }
        if (film && voiceResource != 0) readyVoice();
    }

    /** A film's sound is made ready beside its picture. If it cannot be, the film is shown without it. */
    private void readyVoice() {
        try {
            voice = new MediaPlayer();
            voice.setAudioAttributes(new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_GAME)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC).build());
            try (AssetFileDescriptor file = FilmFiles.open(getContext(), voiceResource)) {
                voice.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            }
            voice.setOnPreparedListener(which -> {
                voiceReady = true;
                begin();
            });
            voice.setOnErrorListener((which, what, extra) -> {
                silent("the sound's error " + what + " " + extra);
                return true;
            });
            voice.prepareAsync();
        } catch (Exception failed) {
            silent("the sound could not be opened: " + failed);
        }
    }

    /** A film's sound is given up, and the film goes on, or begins, without it. */
    private void silent(String why) {
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "film without its sound: " + why);
        voiceGone = true;
        MediaPlayer old = voice;
        voice = null;
        try {
            if (old != null) old.release();
        } catch (RuntimeException ignored) {
            // Already gone.
        }
        begin();
    }

    /** A film begins when its picture can, and its sound can or never will: both at once. */
    private void begin() {
        if (over || started || !pictureReady || voice != null && !voiceReady) return;
        started = true;
        try {
            player.start();
            if (voice != null) voice.start();
        } catch (RuntimeException failed) {
            finish(false, "could not be begun: " + failed);
            return;
        }
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "film begins" + (voice != null ? ", with its sound" : ", silent") + (caption != null ? ", " + lines.size() + " lines under it" : ""));
        postDelayed(ticker, TICK);
    }

    /** How often a film looks at itself, in thousandths of a second; and how far its sound may be from its picture before it is set back. */
    private static final int TICK = 100, APART = 100;

    /** While a film plays: what is written under it is kept to the moment, and now and then its sound is kept to its picture. */
    private void tick() {
        if (over || player == null) return;
        try {
            int now = player.getCurrentPosition();
            if (caption != null) {
                String words = RunFilms.at(lines, now / 1000f);
                if (words == null) {
                    caption.setVisibility(INVISIBLE);
                } else {
                    if (!words.contentEquals(caption.getText())) {
                        fitCaption(words);
                        caption.setText(words);
                    }
                    caption.setVisibility(VISIBLE);
                }
            }
            // (Its sound is looked at twice a second while it is new, when it is most likely to be off, and once a second after.)
            if (voice != null && ++ticks % (ticks < 40 ? 5 : 10) == 0 && voice.isPlaying()) {
                int apart = voice.getCurrentPosition() - player.getCurrentPosition();
                // A sound set to a moment takes a while to go on from it, and is behind by that much: how much is learned from what
                // is seen just after it was set, and allowed for the next time.
                if (set && apart < 0 && apart > -500) lead += -apart;
                set = false;
                boolean far = Math.abs(apart) > APART;
                if (BuildConfig.DEBUG && (far || ticks == 5 || ticks == 40 || ticks % 300 == 0)) {
                    android.util.Log.i("RunFinale", "film at " + now / 1000 + " s: its sound " + apart + " ms from its picture" + (far ? ", set to it" + (lead > 0 ? " and " + lead + " ms on" : "") : ""));
                }
                if (far) {
                    int to = Math.max(0, player.getCurrentPosition() + lead);
                    voice.seekTo(to, MediaPlayer.SEEK_CLOSEST);
                    set = true;
                }
            }
        } catch (RuntimeException gone) {
            // A player that has just been let go: the film is ending.
        }
        postDelayed(ticker, TICK);
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
    static float scale(int wide, int tall, int width, int height) { return scale(wide, tall, width, height, LOST); }

    /** The same, for a picture of which no more than so much may be lost. */
    static float scale(int wide, int tall, int width, int height, float lost) {
        float across = width / (float) wide, down = height / (float) tall;
        float whole = Math.min(across, down), filling = Math.max(across, down);
        return 1 - whole / filling <= lost ? filling : whole;
    }

    /** The picture in the middle of the view with its proportions kept, filling it or whole. */
    private void fit() {
        int width = picture.getWidth(), height = picture.getHeight();
        if (wide <= 0 || tall <= 0 || width <= 0 || height <= 0) return;
        float scale = scale(wide, tall, width, height, film ? FILM_LOST : LOST);
        Matrix matrix = new Matrix();
        // (The view stretches the picture to itself: that is undone, and then it is made the size it should be.)
        matrix.setScale(wide * scale / width, tall * scale / height, width / 2f, height / 2f);
        picture.setTransform(matrix);
        if (caption != null) place(scale, width, height);
    }

    /**
     * Where what is written under a film goes: in the black under the picture, close to it, if there is black enough for two
     * lines; if not, on the picture, as low as the picture's own writing allows, and never in the strip at the foot of the screen
     * that belongs to the phone.
     */
    /** The size of what is written, taken down by steps (to six tenths of itself) until it is no more than three lines wide as it is wrapped, by word or by character. */
    private void fitCaption(String words) {
        if (captionSize <= 0 || caption.getMaxWidth() <= 0 || caption.getMaxWidth() == Integer.MAX_VALUE) return;
        int room = caption.getMaxWidth() - caption.getPaddingLeft() - caption.getPaddingRight();
        float size = captionSize;
        android.text.TextPaint paint = new android.text.TextPaint(caption.getPaint());
        while (true) {
            paint.setTextSize(size);
            int count = android.text.StaticLayout.Builder.obtain(words, 0, words.length(), paint, Math.max(1, room)).build().getLineCount();
            if (count <= 3 || size <= captionSize * .6f) break;
            size *= .93f;
        }
        caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    private void place(float scale, int width, int height) {
        float density = getResources().getDisplayMetrics().density, scaled = getResources().getDisplayMetrics().scaledDensity;
        float shownWide = wide * scale, shownTall = tall * scale, top = (height - shownTall) / 2, bottom = top + shownTall;
        boolean tallPicture = tall > wide;
        float size = RunFilms.size(Math.min(shownWide, width), tallPicture, 14 * scaled);
        captionSize = size;
        caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        caption.setMaxWidth(Math.round(Math.min(shownWide, width) - 24 * density));
        LayoutParams where = (LayoutParams) caption.getLayoutParams();
        float under = height - bottom;
        if (under >= size * 3.4f + 24 * density) {
            where.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            where.topMargin = Math.round(bottom + size * .5f);
            where.bottomMargin = 0;
        } else {
            where.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            where.topMargin = 0;
            where.bottomMargin = Math.round(Math.max(28 * density, height - (top + shownTall * RunFilms.foot(tallPicture))));
        }
        caption.setLayoutParams(where);
    }

    /**
     * It is put away in order: the player is stopped and let go, then what it drew on, and only then, on the next turn of the
     * loop and not from inside whatever tap or news of the player this was called from, the view itself is taken off the screen.
     */
    private void finish(boolean played, String why) {
        if (over) return;
        over = true;
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "clip over: " + why + (played ? "" : " (not played)"));
        removeCallbacks(ticker);
        MediaPlayer sounding = voice;
        voice = null;
        try {
            if (sounding != null) {
                sounding.setOnPreparedListener(null);
                sounding.setOnErrorListener(null);
                sounding.release();
            }
        } catch (RuntimeException ignored) {
            // It is being put away.
        }
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
