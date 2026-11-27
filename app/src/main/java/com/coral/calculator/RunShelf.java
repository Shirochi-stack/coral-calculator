package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The gallery: the page behind the Cringe button on Plus run's map. It lies over the game as
 * the films do, a page of its own with three shelves under one another: the films, each with
 * a button to play it and, for the two long ones, the sound to play it with; their
 * storyboards and cast sheets; and the pictures that open as the story goes on. Above the
 * films is what holds for all of them: the language Halgakos speaks in them, where there is
 * more than one, and the language written under them, or none.
 *
 * Whatever has not been opened by the story shows a lock and a word about where it opens,
 * and never itself. A picture that is open is looked at by touching it: it fills the page,
 * and can be made larger with two fingers or two taps and moved about. What is in the
 * gallery, and what opens it, is {@link RunGallery}'s; this only shows it.
 */
// (It is made by the game and by nothing else: no layout file names it.)
@android.annotation.SuppressLint("ViewConstructor")
final class RunShelf extends FrameLayout {
    /** Whoever opened the gallery: a film is played by it, over the gallery; and it makes the game's small sounds. */
    interface Host {
        void play(int film, View over);

        void tap();
    }

    private final PlaySession session;
    private final boolean everything;
    private final Host host;
    private final int ink, soft, card, page, line;
    private final float density;
    private final LinearLayout shelves;
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final List<RunGallery.Item> items;
    private final Set<String> written = new HashSet<>();
    private WindowManager window;
    private FrameLayout viewer;
    private boolean closed;

    private RunShelf(Context context, PlaySession session, boolean dark, boolean everything, int[] safe, Host host) {
        super(context);
        this.session = session;
        this.everything = everything;
        this.host = host;
        density = context.getResources().getDisplayMetrics().density;
        ink = dark ? 0xFFECECEC : 0xFF2B2B2B;
        soft = dark ? 0xFF9AA0A6 : 0xFF746E67;
        card = dark ? 0xFF24272C : 0xFFFFFFFF;
        page = dark ? 0xFF16181C : 0xFFF7F2EC;
        line = dark ? 0xFF3A3E44 : 0xFFE2DAD0;
        setBackgroundColor(page);
        setClickable(true);
        setPadding(safe[0], safe[1], safe[2], safe[3]);
        // (A picture being looked at lies over its edges too.)
        setClipToPadding(false);
        items = RunGallery.all(text(context, "gallery/pictures.tsv"));
        try {
            String[] names = context.getAssets().list("films");
            if (names != null) java.util.Collections.addAll(written, names);
        } catch (java.io.IOException none) {
            // Nothing is written under anything.
        }
        LinearLayout whole = new LinearLayout(context);
        whole.setOrientation(LinearLayout.VERTICAL);
        addView(whole, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        // Its name at the top, with her face beside it, and the way out.
        LinearLayout head = new LinearLayout(context);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(px(16), px(10), px(10), px(8));
        ImageView face = new ImageView(context);
        face.setImageResource(R.drawable.run_cringe);
        head.addView(face, new LinearLayout.LayoutParams(px(44), px(44)));
        LinearLayout named = new LinearLayout(context);
        named.setOrientation(LinearLayout.VERTICAL);
        named.setPadding(px(10), 0, 0, 0);
        named.addView(words("Cringe", 22, ink, true));
        named.addView(words("Halgakos's gallery. Locked until the story opens it.", 12.5f, soft, false));
        head.addView(named, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        TextView out = words("✕", 18, ink, true);
        out.setGravity(Gravity.CENTER);
        out.setBackground(round(card, px(22), line));
        out.setContentDescription("Close the gallery");
        out.setOnClickListener(view -> {
            host.tap();
            close();
        });
        head.addView(out, new LinearLayout.LayoutParams(px(44), px(44)));
        whole.addView(head);
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        shelves = new LinearLayout(context);
        shelves.setOrientation(LinearLayout.VERTICAL);
        shelves.setPadding(px(14), px(2), px(14), px(28));
        scroll.addView(shelves, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        whole.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        fill();
        // It has been looked into: nothing in it is new any more.
        session.keep(RunGallery.LOOKED, RunGallery.open(items, session));
    }

    /** Lays the gallery over a view, as a film is laid over it; null if there is nowhere to lay it. */
    static RunShelf over(View under, PlaySession session, boolean dark, boolean everything, int[] safe, Host host) {
        try {
            RunShelf shelf = new RunShelf(under.getContext(), session, dark, everything, safe, host);
            if (under.getParent() instanceof ViewGroup) {
                shelf.setZ(under.getZ() + 1);
                ((ViewGroup) under.getParent()).addView(shelf, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else if (under.getLayoutParams() instanceof WindowManager.LayoutParams) {
                WindowManager.LayoutParams like = new WindowManager.LayoutParams();
                like.copyFrom((WindowManager.LayoutParams) under.getLayoutParams());
                like.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                like.setTitle("Halgakos's gallery");
                WindowManager manager = (WindowManager) under.getContext().getSystemService(Context.WINDOW_SERVICE);
                manager.addView(shelf, like);
                shelf.window = manager;
            } else {
                return null;
            }
            return shelf;
        } catch (RuntimeException failed) {
            return null;
        }
    }

    /** A raw resource by its name, or 0: the films' files are looked for by name, so one that is not there is simply not offered. */
    @SuppressWarnings("DiscouragedApi")
    static int raw(Context context, String name) {
        return context.getResources().getIdentifier(name, "raw", context.getPackageName());
    }

    /** What a file among the app's own says, or null if there is no such file. */
    static String text(Context context, String path) {
        try (InputStream in = context.getAssets().open(path)) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] some = new byte[4096];
            for (int count; (count = in.read(some)) > 0; ) out.write(some, 0, count);
            return out.toString("UTF-8");
        } catch (java.io.IOException none) {
            return null;
        }
    }

    @Override public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        // (Where it is a window of its own, the keys come to it: Back is out of the picture, and then out of the gallery.)
        if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK || event.getKeyCode() == android.view.KeyEvent.KEYCODE_ESCAPE) {
            if (event.getAction() == android.view.KeyEvent.ACTION_UP) {
                host.tap();
                if (!back()) close();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /** Back: out of a picture being looked at, if one is; false if there is nothing here for it to close but the gallery. */
    boolean back() {
        if (viewer == null) return false;
        removeView(viewer);
        viewer = null;
        return true;
    }

    /** Takes the gallery away. */
    void close() {
        if (closed) return;
        closed = true;
        loader.shutdownNow();
        try {
            if (window != null) window.removeView(this);
            else if (getParent() instanceof ViewGroup) ((ViewGroup) getParent()).removeView(this);
        } catch (RuntimeException gone) {
            // Already gone.
        }
    }

    boolean closed() { return closed; }

    /** What is on the shelves is shown again: after a film was played, or something was chosen. */
    void fill() {
        shelves.removeAllViews();
        Context context = getContext();
        // The films.
        shelves.addView(heading(RunGallery.SHELVES[RunGallery.VIDEOS], null));
        View setting = settings();
        if (setting != null) shelves.addView(setting, spaced(8));
        for (RunGallery.Item item : items) {
            if (item.shelf == RunGallery.VIDEOS) shelves.addView(video(item), spaced(8));
        }
        // What was drawn for them, and then the pictures: tiles, a few to a row.
        for (int shelf = RunGallery.BOARDS; shelf <= RunGallery.PICTURES; shelf++) {
            List<RunGallery.Item> here = new ArrayList<>();
            for (RunGallery.Item item : items) if (item.shelf == shelf) here.add(item);
            int open = 0;
            for (RunGallery.Item item : here) if (open(item)) open++;
            shelves.addView(heading(RunGallery.SHELVES[shelf], here.isEmpty() ? null : open + " of " + here.size()), spaced(14));
            if (here.isEmpty()) {
                shelves.addView(words("Pictures open here as the story goes on.", 13, soft, false), spaced(6));
                continue;
            }
            int across = shelf == RunGallery.BOARDS ? 2 : 3;
            LinearLayout row = null;
            for (int index = 0; index < here.size(); index++) {
                if (index % across == 0) {
                    row = new LinearLayout(context);
                    shelves.addView(row, spaced(8));
                }
                LinearLayout.LayoutParams where = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
                if (index % across > 0) where.leftMargin = px(8);
                row.addView(tile(here.get(index), shelf == RunGallery.BOARDS ? 104 : 84), where);
            }
            // (A row that is not full keeps its tiles the size of the others.)
            for (int index = here.size(); index % across != 0; index++) {
                LinearLayout.LayoutParams where = new LinearLayout.LayoutParams(0, 1, 1);
                where.leftMargin = px(8);
                row.addView(new View(context), where);
            }
        }
    }

    private boolean open(RunGallery.Item item) { return everything || RunGallery.open(item, session); }

    // What holds for every film

    /** The languages she has been recorded in for either film: English always, and whichever others have their file. */
    private List<Integer> tongues() {
        List<Integer> found = new ArrayList<>();
        found.add(RunFilms.ENGLISH);
        for (int tongue = RunFilms.ENGLISH + 1; tongue < RunFilms.TONGUES.length; tongue++) {
            if (raw(getContext(), "run_film_prologue_h_" + RunFilms.TONGUES[tongue]) != 0 || raw(getContext(), "run_film_idol_h_" + RunFilms.TONGUES[tongue]) != 0) found.add(tongue);
        }
        return found;
    }

    /** The languages something is written in under any of the films' sounds. */
    private List<Integer> scripts() {
        List<Integer> found = new ArrayList<>();
        for (int tongue = 0; tongue < RunFilms.TONGUES.length; tongue++) {
            for (String name : written) {
                if (name.endsWith("." + RunFilms.TONGUES[tongue] + ".txt")) {
                    found.add(tongue);
                    break;
                }
            }
        }
        return found;
    }

    private View settings() {
        List<Integer> tongues = tongues(), scripts = scripts();
        if (tongues.size() < 2 && scripts.isEmpty()) return null;
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(round(card, px(14), line));
        box.setPadding(px(12), px(8), px(12), px(10));
        if (tongues.size() > 1) {
            int now = session.tally(RunFilms.TONGUE);
            if (!tongues.contains(now)) now = RunFilms.ENGLISH;
            String[] names = new String[tongues.size()];
            for (int index = 0; index < names.length; index++) names[index] = RunFilms.TONGUE_NAMES[tongues.get(index)];
            box.addView(choice("Halgakos speaks", names, tongues.indexOf(now), picked -> session.keep(RunFilms.TONGUE, tongues.get(picked))));
        }
        if (!scripts.isEmpty()) {
            int now = session.tally(RunFilms.CAPTIONS);
            String[] names = new String[scripts.size() + 1];
            names[0] = "Off";
            for (int index = 0; index < scripts.size(); index++) names[index + 1] = RunFilms.TONGUE_NAMES[scripts.get(index)];
            int at = scripts.contains(now) ? scripts.indexOf(now) + 1 : 0;
            box.addView(choice("Captions", names, at, picked -> session.keep(RunFilms.CAPTIONS, picked == 0 ? RunFilms.NO_CAPTIONS : scripts.get(picked - 1))), spaced(tongues.size() > 1 ? 6 : 0));
        }
        return box;
    }

    private interface Picked {
        void picked(int which);
    }

    /** A word and, beside it, some things of which one is chosen. */
    private View choice(String name, String[] names, int chosen, Picked picked) {
        LinearLayout row = new LinearLayout(getContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = words(name, 13, soft, false);
        row.addView(label, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        for (int index = 0; index < names.length; index++) {
            int which = index;
            LinearLayout.LayoutParams where = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            where.leftMargin = px(6);
            row.addView(chip(names[index], index == chosen, view -> {
                host.tap();
                picked.picked(which);
                fill();
            }), where);
        }
        return row;
    }

    private TextView chip(String name, boolean chosen, OnClickListener then) {
        TextView chip = words(name, 13, chosen ? Color.WHITE : ink, chosen);
        chip.setGravity(Gravity.CENTER);
        chip.setMinWidth(px(44));
        chip.setMinHeight(px(36));
        chip.setPadding(px(12), px(6), px(12), px(6));
        chip.setBackground(round(chosen ? CalculatorLayout.CORAL : page, px(18), chosen ? CalculatorLayout.CORAL : line));
        chip.setOnClickListener(then);
        return chip;
    }

    // A film

    private View video(RunGallery.Item item) {
        Context context = getContext();
        boolean open = open(item);
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(round(card, px(14), line));
        box.setPadding(px(10), px(10), px(10), px(10));
        LinearLayout top = new LinearLayout(context);
        top.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout small = new FrameLayout(context);
        small.setBackground(round(open ? Color.BLACK : line, px(8), 0));
        small.setClipToOutline(true);
        if (open) {
            ImageView picture = new ImageView(context);
            picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
            small.addView(picture, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            show(picture, item.name, 360);
        } else {
            small.addView(lock(), new LayoutParams(px(26), px(26), Gravity.CENTER));
        }
        top.addView(small, new LinearLayout.LayoutParams(px(116), px(66)));
        LinearLayout beside = new LinearLayout(context);
        beside.setOrientation(LinearLayout.VERTICAL);
        beside.setPadding(px(12), 0, px(6), 0);
        beside.addView(words(open ? item.title : "Locked", 16, open ? ink : soft, true));
        beside.addView(words(open ? item.film == RunFilms.CLIP ? "The scene from the last land" : "Ninety seconds" : item.hint, 12.5f, soft, false));
        top.addView(beside, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        if (open) {
            TextView play = words("▶  Play", 14, Color.WHITE, true);
            play.setGravity(Gravity.CENTER);
            play.setMinHeight(px(44));
            play.setPadding(px(16), px(8), px(18), px(8));
            play.setBackground(round(CalculatorLayout.CORAL, px(22), 0));
            play.setContentDescription("Play " + item.title);
            play.setOnClickListener(view -> {
                host.tap();
                host.play(item.film, this);
            });
            top.addView(play);
        }
        box.addView(top);
        if (open && item.film != RunFilms.CLIP) {
            // Its sounds, of which one is chosen: those whose file is there.
            String[] names = RunFilms.SOUND_NAMES[item.film];
            int now = session.tally(RunFilms.SOUND + item.film);
            box.addView(words("Sound", 13, soft, false), spaced(10));
            LinearLayout row = new LinearLayout(context);
            row.setGravity(Gravity.CENTER_VERTICAL);
            boolean any = false;
            for (int choice = 0; choice < names.length; choice++) {
                boolean there = false;
                for (boolean upright : new boolean[]{true, false}) {
                    if (raw(context, "run_film_" + RunFilms.sounds(item.film, choice, RunFilms.ENGLISH, upright)[0]) != 0) there = true;
                }
                if (!there) continue;
                int which = choice;
                LinearLayout.LayoutParams where = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                if (any) where.leftMargin = px(6);
                any = true;
                row.addView(chip(names[choice], choice == now, view -> {
                    host.tap();
                    session.keep(RunFilms.SOUND + item.film, which);
                    fill();
                }), where);
            }
            box.addView(row, spaced(4));
        }
        return box;
    }

    // A picture

    private View tile(RunGallery.Item item, int tall) {
        Context context = getContext();
        boolean open = open(item);
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        FrameLayout small = new FrameLayout(context);
        small.setBackground(round(open ? card : line, px(10), open ? line : 0));
        small.setClipToOutline(true);
        if (open) {
            ImageView picture = new ImageView(context);
            picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
            small.addView(picture, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            show(picture, item.name, 420);
            small.setContentDescription(item.title);
            small.setOnClickListener(view -> {
                host.tap();
                look(item);
            });
        } else {
            small.addView(lock(), new LayoutParams(px(24), px(24), Gravity.CENTER));
        }
        box.addView(small, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, px(tall)));
        TextView under = words(open ? item.title : item.hint, 12, open ? ink : soft, false);
        under.setMaxLines(2);
        under.setPadding(px(2), px(4), px(2), 0);
        box.addView(under);
        return box;
    }

    /** A picture of the gallery's, no larger than is wanted, read off the thread the views live on and shown when it has been. */
    private void show(ImageView into, String name, int longest) {
        Context context = getContext().getApplicationContext();
        try {
            loader.execute(() -> {
                Bitmap bitmap = picture(context, name, longest);
                if (bitmap != null) post(() -> {
                    if (!closed) into.setImageBitmap(bitmap);
                });
            });
        } catch (java.util.concurrent.RejectedExecutionException shut) {
            // The gallery has been closed.
        }
    }

    /** A picture of the gallery's, with its longer side no more than about so many dots (0: as it is). */
    static Bitmap picture(Context context, String name, int longest) {
        String path = "gallery/" + name + ".webp";
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (longest > 0) {
                options.inJustDecodeBounds = true;
                try (InputStream in = context.getAssets().open(path)) {
                    BitmapFactory.decodeStream(in, null, options);
                }
                int sample = 1;
                while (Math.max(options.outWidth, options.outHeight) / (sample * 2) >= longest) sample *= 2;
                options = new BitmapFactory.Options();
                options.inSampleSize = sample;
            }
            try (InputStream in = context.getAssets().open(path)) {
                return BitmapFactory.decodeStream(in, null, options);
            }
        } catch (java.io.IOException | OutOfMemoryError none) {
            return null;
        }
    }

    /** A picture fills the page, to be looked at closely. */
    private void look(RunGallery.Item item) {
        if (viewer != null) return;
        Context context = getContext();
        viewer = new FrameLayout(context);
        viewer.setBackgroundColor(Color.BLACK);
        viewer.setClickable(true);
        Zoom zoom = new Zoom(context);
        viewer.addView(zoom, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView named = words(item.title + "\nTwo fingers, or two taps, to look closer", 12.5f, Color.WHITE, false);
        named.setGravity(Gravity.CENTER);
        named.setShadowLayer(5, 0, 1, Color.BLACK);
        LayoutParams low = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        low.bottomMargin = getPaddingBottom() + px(18);
        viewer.addView(named, low);
        TextView out = words("✕", 18, Color.WHITE, true);
        out.setGravity(Gravity.CENTER);
        out.setBackground(round(0x99000000, px(22), 0));
        out.setContentDescription("Close the picture");
        out.setOnClickListener(view -> {
            host.tap();
            back();
        });
        LayoutParams corner = new LayoutParams(px(44), px(44), Gravity.TOP | Gravity.END);
        corner.topMargin = getPaddingTop() + px(10);
        corner.rightMargin = getPaddingRight() + px(10);
        viewer.addView(out, corner);
        // (Over the whole page, its edges too: the picture is the page while it is looked at.)
        LayoutParams whole = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        whole.setMargins(-getPaddingLeft(), -getPaddingTop(), -getPaddingRight(), -getPaddingBottom());
        addView(viewer, whole);
        Context app = context.getApplicationContext();
        try {
            loader.execute(() -> {
                Bitmap bitmap = picture(app, item.name, 0);
                if (bitmap != null) post(() -> zoom.show(bitmap));
            });
        } catch (java.util.concurrent.RejectedExecutionException shut) {
            // The gallery has been closed.
        }
    }

    /** A picture that can be made larger and moved about: it begins whole, in the middle; two taps bring it close, or take it back. */
    private static final class Zoom extends View {
        private final Matrix matrix = new Matrix();
        private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        private final ScaleGestureDetector pinch;
        private final GestureDetector taps;
        private Bitmap bitmap;
        private float whole = 1, scale = 1, x, y;

        Zoom(Context context) {
            super(context);
            pinch = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override public boolean onScale(ScaleGestureDetector detector) {
                    resize(scale * detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                    return true;
                }
            });
            taps = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onDown(MotionEvent event) { return true; }

                @Override public boolean onScroll(MotionEvent first, MotionEvent event, float movedX, float movedY) {
                    x -= movedX;
                    y -= movedY;
                    settle();
                    return true;
                }

                @Override public boolean onDoubleTap(MotionEvent event) {
                    resize(scale > whole * 1.2f ? whole : Math.max(whole * 2.5f, closest()), event.getX(), event.getY());
                    return true;
                }
            });
        }

        void show(Bitmap shown) {
            bitmap = shown;
            fit();
        }

        /** The scale at which the picture is as sharp as it was drawn, or a little short of filling the screen's width if that is closer. */
        private float closest() { return Math.max(1f, getWidth() / (float) Math.max(1, bitmap.getWidth())); }

        private void fit() {
            if (bitmap == null || getWidth() == 0) return;
            whole = scale = Math.min(getWidth() / (float) bitmap.getWidth(), getHeight() / (float) bitmap.getHeight());
            settle();
        }

        private void resize(float to, float aboutX, float aboutY) {
            if (bitmap == null) return;
            float next = Math.max(whole, Math.min(Math.max(whole * 6, closest() * 2), to));
            // What is under the fingers stays under them.
            x = aboutX - (aboutX - x) * next / scale;
            y = aboutY - (aboutY - y) * next / scale;
            scale = next;
            settle();
        }

        /** The picture is kept on the page: in the middle of it where it is smaller than the page, and not pulled off it where it is larger. */
        private void settle() {
            float wide = bitmap.getWidth() * scale, tall = bitmap.getHeight() * scale;
            x = wide <= getWidth() ? (getWidth() - wide) / 2 : Math.min(0, Math.max(getWidth() - wide, x));
            y = tall <= getHeight() ? (getHeight() - tall) / 2 : Math.min(0, Math.max(getHeight() - tall, y));
            invalidate();
        }

        @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) { fit(); }

        // (A tap on it does nothing by itself: it is looked at, not pressed.)
        @android.annotation.SuppressLint("ClickableViewAccessibility")
        @Override public boolean onTouchEvent(MotionEvent event) {
            pinch.onTouchEvent(event);
            if (!pinch.isInProgress()) taps.onTouchEvent(event);
            return true;
        }

        @Override protected void onDraw(Canvas canvas) {
            if (bitmap == null) return;
            matrix.setScale(scale, scale);
            matrix.postTranslate(x, y);
            canvas.drawBitmap(bitmap, matrix, paint);
        }
    }

    // What things are made of

    private int px(float dp) { return Math.round(dp * density); }

    private LinearLayout.LayoutParams spaced(float above) {
        LinearLayout.LayoutParams where = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        where.topMargin = px(above);
        return where;
    }

    private TextView words(String said, float size, int colour, boolean heavy) {
        TextView view = new TextView(getContext());
        view.setText(said);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        view.setTextColor(colour);
        if (heavy) view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    private View heading(String name, String count) {
        LinearLayout row = new LinearLayout(getContext());
        row.setGravity(Gravity.BOTTOM);
        row.addView(words(name, 17, ink, true), new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        if (count != null) row.addView(words(count, 12.5f, soft, false));
        return row;
    }

    private static Drawable round(int colour, float radius, int edge) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(colour);
        shape.setCornerRadius(radius);
        if (edge != 0) shape.setStroke(Math.max(1, Math.round(radius / 14)), edge);
        return shape;
    }

    /** A small lock, drawn: what stands in the place of anything not yet opened. */
    private View lock() {
        View view = new View(getContext());
        view.setBackground(new Drawable() {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF box = new RectF();

            @Override public void draw(Canvas canvas) {
                float wide = getBounds().width(), tall = getBounds().height();
                paint.setColor(soft);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(wide * .12f);
                box.set(wide * .28f, tall * .1f, wide * .72f, tall * .7f);
                canvas.drawArc(box, 180, 180, false, paint);
                canvas.drawLine(box.left, tall * .4f, box.left, tall * .5f, paint);
                canvas.drawLine(box.right, tall * .4f, box.right, tall * .5f, paint);
                paint.setStyle(Paint.Style.FILL);
                box.set(wide * .14f, tall * .45f, wide * .86f, tall * .95f);
                canvas.drawRoundRect(box, wide * .1f, wide * .1f, paint);
            }

            @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }

            @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }

            @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        });
        return view;
    }
}
