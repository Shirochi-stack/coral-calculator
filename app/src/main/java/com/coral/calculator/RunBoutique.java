package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tulle and Thread, the vanity boutique: a page of its own over the game, as the gallery is. Tulle keeps it, drawn as a
 * row of six pictures that change now and then (she blinks, waves, holds up a dress, stretches her tape, thinks; she never
 * moves in between). On show are the outfits, each worn by Halgakos herself, with a price in pluses and in treats, and a
 * button to wear one she has; and her quests, three lines, each step handed in here. What she says is voiced.
 */
@android.annotation.SuppressLint("ViewConstructor")
final class RunBoutique extends View {
    /** What the game lends it: its voice, and its small sounds. */
    interface Host {
        void say(String name);

        boolean speaking();

        void hush();

        void play(String effect);

        void closed();
    }

    private static final int OUTFITS = 0, QUESTS = 1;
    /** Which of her six pictures, and for how long, round and round. */
    private static final float[][] IDLE = {{0, 2.4f}, {1, .15f}, {0, 1.6f}, {2, 1.5f}, {0, 1.2f}, {1, .15f}, {0, 1.0f}, {3, 1.8f}, {0, 1.4f}, {1, .15f}, {0, 1.0f},
            {4, 1.3f}, {0, 1.2f}, {5, 1.6f}, {1, .15f}};
    private static final float[] IDLE_TOTAL = {0};
    static {
        for (float[] one : IDLE) IDLE_TOTAL[0] += one[1];
    }

    private final PlaySession session;
    private final RunVanity.Keep save;
    private final Host host;
    private final boolean dark;
    private final float density;
    private final int ink, soft, card, page;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG), text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF(), closeBox = new RectF(), bubble = new RectF();
    private final Bitmap keeper, jump;
    private final VanityArt art;
    private final Random random = new Random();
    private final ArrayDeque<String[]> queue = new ArrayDeque<>();
    private final List<Object[]> hits = new ArrayList<>();
    private final RectF[] tabs = {new RectF(), new RectF()};
    private String[] said;
    private int tab = OUTFITS;
    private float scroll, top, bottom, grabY, grabScroll, content;
    private boolean dragged, closed;
    private long start = System.nanoTime();
    private WindowManager window;

    private RunBoutique(Context context, PlaySession session, boolean dark, int[] safe, Host host) {
        super(context);
        this.session = session;
        save = RunVanity.on(session);
        this.host = host;
        this.dark = dark;
        density = context.getResources().getDisplayMetrics().density;
        ink = dark ? 0xFFECECEC : 0xFF3A2E33;
        soft = dark ? 0xFFA9A0A5 : 0xFF84767C;
        card = dark ? 0xFF2A2428 : 0xFFFFFFFF;
        page = dark ? 0xFF1B1719 : 0xFFFFF3F4;
        keeper = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_vanity_npc);
        jump = BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_jump);
        art = new VanityArt(context);
        setPadding(safe[0], safe[1], safe[2], safe[3]);
        setClickable(true);
        String[] greeting = VanityStory.GREET[random.nextInt(VanityStory.GREET.length)];
        speak(greeting);
    }

    /** Lays the boutique over a view; null if there is nowhere to lay it. */
    static RunBoutique over(View under, PlaySession session, boolean dark, int[] safe, Host host) {
        try {
            RunBoutique shop = new RunBoutique(under.getContext(), session, dark, safe, host);
            if (under.getParent() instanceof ViewGroup) {
                shop.setZ(under.getZ() + 1);
                ((ViewGroup) under.getParent()).addView(shop, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else if (under.getLayoutParams() instanceof WindowManager.LayoutParams) {
                WindowManager.LayoutParams like = new WindowManager.LayoutParams();
                like.copyFrom((WindowManager.LayoutParams) under.getLayoutParams());
                like.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                like.setTitle("Tulle and Thread");
                WindowManager manager = (WindowManager) under.getContext().getSystemService(Context.WINDOW_SERVICE);
                manager.addView(shop, like);
                shop.window = manager;
            } else {
                return null;
            }
            return shop;
        } catch (RuntimeException failed) {
            return null;
        }
    }

    boolean closed() { return closed; }

    void close() {
        if (closed) return;
        closed = true;
        host.hush();
        try {
            if (window != null) window.removeView(this);
            else if (getParent() instanceof ViewGroup) ((ViewGroup) getParent()).removeView(this);
        } catch (RuntimeException gone) {
            // Already gone.
        }
        host.closed();
    }

    private float dp(float value) { return value * density; }

    private void speak(String[] line) {
        if (line == null) return;
        queue.add(line);
    }

    // The way it looks

    @Override protected void onDraw(Canvas canvas) {
        if (closed) return;
        float w = getWidth(), h = getHeight();
        paint.setShader(new LinearGradient(0, 0, 0, h, dark ? 0xFF2A2024 : 0xFFFFE7EC, page, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        // A line is said when the one before it has been.
        if (!queue.isEmpty() && !host.speaking()) {
            said = queue.poll();
            host.say(said[0]);
        }
        float left = getPaddingLeft() + dp(14), right = w - getPaddingRight() - dp(14), y = getPaddingTop() + dp(10);
        // Her name and the way out.
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(22));
        canvas.drawText("Tulle & Thread", left, y + dp(24), text);
        text.setTypeface(Typeface.DEFAULT);
        text.setTextSize(dp(12.5f));
        text.setColor(soft);
        canvas.drawText("+ " + save.tally("run_bank") + "   ·   " + save.tally("run_treats") + " treats", left, y + dp(44), text);
        closeBox.set(right - dp(44), y, right, y + dp(44));
        paint.setColor(card);
        canvas.drawCircle(closeBox.centerX(), closeBox.centerY(), dp(22), paint);
        text.setColor(ink);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(dp(20));
        canvas.drawText("✕", closeBox.centerX(), closeBox.centerY() + dp(7), text);
        y += dp(56);
        // Tulle herself, and what she says.
        float tall = dp(176), wide = tall * 224 / 288;
        float time = ((System.nanoTime() - start) / 1e9f) % IDLE_TOTAL[0], at = 0;
        int frame = 0;
        for (float[] one : IDLE) {
            at += one[1];
            if (time < at) {
                frame = (int) one[0];
                break;
            }
        }
        box.set(left, y, left + wide, y + tall);
        canvas.drawBitmap(keeper, new android.graphics.Rect(frame * 224, 0, frame * 224 + 224, 288), box, paint);
        bubble.set(left + wide + dp(8), y + dp(8), right, y + tall - dp(34));
        paint.setColor(card);
        canvas.drawRoundRect(bubble, dp(16), dp(16), paint);
        text.setTextAlign(Paint.Align.LEFT);
        text.setTextSize(dp(14));
        text.setColor(ink);
        if (said != null) {
            text.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText(said[2].equals("H") ? "Halgakos" : "Tulle", bubble.left + dp(12), bubble.top + dp(22), text);
            text.setTypeface(Typeface.DEFAULT);
            text.setColor(soft);
            float line = bubble.top + dp(42);
            for (String part : wrap(said[1], bubble.width() - dp(24))) {
                if (line > bubble.bottom - dp(6)) break;
                canvas.drawText(part, bubble.left + dp(12), line, text);
                line += dp(18);
            }
        }
        y += tall + dp(6);
        // The two tabs.
        float each = (right - left - dp(8)) / 2;
        String[] names = {"Outfits", "Quests"};
        for (int index = 0; index < 2; index++) {
            tabs[index].set(left + index * (each + dp(8)), y, left + index * (each + dp(8)) + each, y + dp(36));
            paint.setColor(index == tab ? CalculatorLayout.CORAL : card);
            canvas.drawRoundRect(tabs[index], dp(18), dp(18), paint);
            text.setColor(index == tab ? Color.WHITE : ink);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(dp(14));
            canvas.drawText(names[index], tabs[index].centerX(), tabs[index].centerY() + dp(5), text);
        }
        top = y + dp(44);
        bottom = h - getPaddingBottom() - dp(6);
        hits.clear();
        canvas.save();
        canvas.clipRect(0, top, w, bottom);
        float at2 = top - scroll;
        content = tab == OUTFITS ? drawOutfits(canvas, left, right, at2) : drawQuests(canvas, left, right, at2);
        canvas.restore();
        postInvalidateOnAnimation();
    }

    private List<String> wrap(String words, float room) {
        List<String> lines = new ArrayList<>();
        StringBuilder now = new StringBuilder();
        for (String word : words.split(" ")) {
            String tryOn = now.length() == 0 ? word : now + " " + word;
            if (text.measureText(tryOn) > room && now.length() > 0) {
                lines.add(now.toString());
                now = new StringBuilder(word);
            } else {
                now = new StringBuilder(tryOn);
            }
        }
        if (now.length() > 0) lines.add(now.toString());
        return lines;
    }

    private void pill(Canvas canvas, RectF where, String label, boolean bright, boolean on) {
        paint.setColor(on ? CalculatorLayout.CORAL : bright ? CalculatorLayout.CORAL : dark ? 0xFF44292E : 0xFFFCE9EB);
        canvas.drawRoundRect(where, where.height() / 2, where.height() / 2, paint);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextSize(dp(13));
        text.setColor(on || bright ? Color.WHITE : CalculatorLayout.CORAL);
        canvas.drawText(label, where.centerX(), where.centerY() + dp(4.5f), text);
        text.setTypeface(Typeface.DEFAULT);
    }

    private float drawOutfits(Canvas canvas, float left, float right, float y) {
        float start = y;
        int mode = RunVanity.mode(save), chosen = RunVanity.outfit(save);
        float each = (right - left - dp(12)) / 3;
        for (int one = 0; one < 3; one++) {
            box.set(left + one * (each + dp(6)), y, left + one * (each + dp(6)) + each, y + dp(32));
            paint.setColor(one == mode ? CalculatorLayout.CORAL : card);
            canvas.drawRoundRect(box, dp(16), dp(16), paint);
            text.setColor(one == mode ? Color.WHITE : ink);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(dp(13));
            canvas.drawText(RunVanity.MODE_NAMES[one], box.centerX(), box.centerY() + dp(4.5f), text);
            hits.add(new Object[]{new RectF(box), "mode", one});
        }
        y += dp(42);
        for (int outfit = 0; outfit < RunVanity.count(); outfit++) {
            if (!RunVanity.ready(outfit)) continue;
            boolean hers = RunVanity.owns(save, outfit), on = chosen == outfit && mode == RunVanity.VANITY;
            RectF at = new RectF(left, y, right, y + dp(96));
            y += dp(104);
            if (at.bottom < top - dp(4) || at.top > bottom + dp(4)) continue;
            paint.setColor(card);
            canvas.drawRoundRect(at, dp(16), dp(16), paint);
            // Her in it: from the sheet of her in the air.
            Bitmap sheet = art.sheet("jump", jump, RunVanity.STYLES[outfit]);
            float tall = dp(84), wide = tall * 256 / 208;
            box.set(at.left + dp(8), at.top + dp(6), at.left + dp(8) + wide, at.top + dp(6) + tall);
            canvas.drawBitmap(sheet, new android.graphics.Rect(256, 0, 512, 208), box, paint);
            float words = at.left + dp(16) + wide, room = at.right - words - dp(110);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(dp(15));
            text.setColor(ink);
            canvas.drawText(RunVanity.NAMES[outfit], words, at.top + dp(24), text);
            text.setTypeface(Typeface.DEFAULT);
            text.setTextSize(dp(11));
            text.setColor(soft);
            int row = 0;
            for (String part : wrap(RunVanity.DOES[outfit], room)) {
                if (row++ > 2) break;
                canvas.drawText(part, words, at.top + dp(40) + row * dp(13), text);
            }
            // What to do with it.
            int quest = RunVanity.QUEST[outfit];
            if (hers) {
                RectF wear = new RectF(at.right - dp(104), at.bottom - dp(34), at.right - dp(10), at.bottom - dp(8));
                pill(canvas, wear, on ? "Wearing" : "Wear", on, on);
                hits.add(new Object[]{wear, "wear", outfit});
            } else if (quest >= 0) {
                RectF ask = new RectF(at.right - dp(104), at.bottom - dp(34), at.right - dp(10), at.bottom - dp(8));
                pill(canvas, ask, "Quest", false, false);
                hits.add(new Object[]{ask, "quest", quest});
            } else {
                RectF plus = new RectF(at.right - dp(104), at.bottom - dp(62), at.right - dp(10), at.bottom - dp(36));
                RectF treat = new RectF(at.right - dp(104), at.bottom - dp(32), at.right - dp(10), at.bottom - dp(6));
                pill(canvas, plus, "+ " + RunVanity.PRICES[outfit], save.tally("run_bank") >= RunVanity.PRICES[outfit], false);
                pill(canvas, treat, RunVanity.TREATS[outfit] + " treats", save.tally("run_treats") >= RunVanity.TREATS[outfit], false);
                hits.add(new Object[]{plus, "buy", outfit});
                hits.add(new Object[]{treat, "treats", outfit});
            }
        }
        return y - start + dp(10);
    }

    private float drawQuests(Canvas canvas, float left, float right, float y) {
        float start = y;
        for (int quest = 0; quest < VanityQuests.count(); quest++) {
            RectF at = new RectF(left, y, right, y + dp(130));
            y += dp(138);
            if (at.bottom < top - dp(4) || at.top > bottom + dp(4)) continue;
            paint.setColor(card);
            canvas.drawRoundRect(at, dp(16), dp(16), paint);
            int state = VanityQuests.state(save, quest);
            boolean open = VanityQuests.open(quest), finished = state > VanityQuests.STEPS[quest].length;
            text.setTextAlign(Paint.Align.LEFT);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(dp(15));
            text.setColor(ink);
            canvas.drawText(VanityQuests.NAMES[quest], at.left + dp(14), at.top + dp(24), text);
            text.setTypeface(Typeface.DEFAULT);
            text.setTextSize(dp(12));
            text.setColor(soft);
            canvas.drawText("Reward: " + RunVanity.NAMES[VanityQuests.REWARD[quest]], at.left + dp(14), at.top + dp(42), text);
            // Its steps, a dot for each, the one she is on in words.
            for (int step = 0; step < VanityQuests.STEPS[quest].length; step++) {
                paint.setColor(step + 1 < state || finished ? CalculatorLayout.CORAL : dark ? 0xFF4A4045 : 0xFFE6D9DD);
                canvas.drawCircle(at.left + dp(20) + step * dp(16), at.top + dp(60), dp(5), paint);
            }
            RectF act = new RectF(at.right - dp(124), at.bottom - dp(40), at.right - dp(12), at.bottom - dp(10));
            if (!open) {
                text.setColor(soft);
                canvas.drawText("Tulle is still sewing the reward.", at.left + dp(14), at.top + dp(84), text);
            } else if (finished) {
                text.setColor(soft);
                canvas.drawText("Done: the outfit is hers.", at.left + dp(14), at.top + dp(84), text);
            } else if (state == 0) {
                text.setColor(soft);
                canvas.drawText("Ask Tulle what she needs.", at.left + dp(14), at.top + dp(84), text);
                pill(canvas, act, "Hear her out", true, false);
                hits.add(new Object[]{act, "take", quest});
            } else {
                int[] now = VanityQuests.progress(save, quest);
                VanityQuests.Step step = VanityQuests.STEPS[quest][state - 1];
                text.setColor(ink);
                canvas.drawText(step.ask, at.left + dp(14), at.top + dp(84), text);
                text.setColor(soft);
                canvas.drawText(step.kind == VanityQuests.LAND ? (now[0] >= 1 ? "Cleared" : "Not yet") : now[0] + " of " + now[1], at.left + dp(14), at.top + dp(102), text);
                boolean ready = now[0] >= now[1];
                pill(canvas, act, step.kind == VanityQuests.GIVE ? "Hand over" : "Hand in", ready, false);
                hits.add(new Object[]{act, "hand", quest});
            }
        }
        return y - start + dp(10);
    }

    // The way it is touched

    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                grabY = event.getY();
                grabScroll = scroll;
                dragged = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getY() - grabY) > dp(8)) dragged = true;
                if (dragged) scroll = Math.max(0, Math.min(Math.max(0, content - (bottom - top)), grabScroll + grabY - event.getY()));
                return true;
            case MotionEvent.ACTION_UP:
                if (!dragged) tap(event.getX(), event.getY());
                return true;
            default:
                return true;
        }
    }

    private void tap(float x, float y) {
        if (closeBox.contains(x, y)) {
            host.play("tap");
            close();
            return;
        }
        for (int index = 0; index < 2; index++) {
            if (tabs[index].contains(x, y)) {
                tab = index;
                scroll = 0;
                host.play("page");
                return;
            }
        }
        if (y < top || y > bottom) return;
        for (Object[] hit : hits) {
            if (!((RectF) hit[0]).contains(x, y)) continue;
            act((String) hit[1], (Integer) hit[2]);
            return;
        }
    }

    private void act(String what, int number) {
        switch (what) {
            case "mode":
                RunVanity.setMode(save, number);
                host.play("tap");
                break;
            case "wear":
                RunVanity.choose(save, RunVanity.outfit(save) == number && RunVanity.mode(save) == RunVanity.VANITY ? -1 : number);
                RunVanity.setMode(save, RunVanity.outfit(save) >= 0 ? RunVanity.VANITY : RunVanity.DEFAULT);
                host.play("star");
                break;
            case "buy":
            case "treats":
                if (what.equals("buy") ? RunVanity.buy(save, number) : RunVanity.buyWithTreats(save, number)) {
                    RunVanity.setMode(save, RunVanity.VANITY);
                    host.play("buy");
                    speak(VanityStory.BOUGHT_FOR[number] != null ? VanityStory.BOUGHT_FOR[number] : VanityStory.BOUGHT);
                    speak(VanityStory.HAL_BOUGHT[random.nextInt(VanityStory.HAL_BOUGHT.length)]);
                } else {
                    host.play("no");
                    speak(VanityStory.BROKE);
                }
                break;
            case "quest":
                tab = QUESTS;
                scroll = 0;
                host.play("page");
                break;
            case "take":
                if (VanityQuests.take(save, number)) {
                    host.play("tap");
                    speak(VanityQuests.line(number, 0));
                    speak(VanityQuests.line(number, 1));
                }
                break;
            case "hand": {
                int state = VanityQuests.state(save, number);
                int result = VanityQuests.hand(save, number);
                if (result == VanityQuests.NOTHING) {
                    host.play("no");
                } else {
                    host.play(result == VanityQuests.FINISHED ? "unlock" : "buy");
                    speak(VanityQuests.line(number, result == VanityQuests.FINISHED ? 4 : 1 + state));
                }
                break;
            }
            default:
                break;
        }
    }
}
