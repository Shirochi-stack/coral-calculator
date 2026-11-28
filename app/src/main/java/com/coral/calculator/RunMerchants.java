package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Aelwyn Pennyfeather's wagon: a page of its own over the game, as Tulle's boutique is. Aelwyn, the elf merchant, goes through a loop of eight
 * pictures (she smiles, blinks, waves, shows off an hourglass, counts coins, polishes a monocle, thinks of a price, holds out her hand) and speaks,
 * voiced. Two tabs: Buy, her stock of the day (six curios and two rare gimmicks, different every day and in every village), and Sell, where
 * what she is paid for goes (junk at once, the rest in the pack, opened for selling). The rules are {@link RunEconomy}.
 */
@android.annotation.SuppressLint("ViewConstructor")
final class RunMerchants extends View {
    /** What the game lends it: its voice, its small sounds, and the pack opened to sell. */
    interface Host {
        void say(String name);

        boolean speaking();

        void hush();

        void play(String effect);

        void closed();
    }

    private static final int BUY = 0, SELL = 1;
    /** Which of her eight pictures, and for how long, round and round: a real loop of gestures, not a breath. */
    private static final float[][] IDLE = {{0, 2.2f}, {1, .15f}, {0, 1.4f}, {2, 1.5f}, {0, 1.0f}, {1, .15f}, {0, .8f}, {3, 1.8f}, {0, 1.2f}, {4, 1.7f}, {1, .15f}, {0, 1.0f},
            {5, 1.9f}, {0, 1.1f}, {6, 1.6f}, {1, .15f}, {0, .9f}, {7, 1.7f}};
    private static final float[] IDLE_TOTAL = {0};
    static {
        for (float[] one : IDLE) IDLE_TOTAL[0] += one[1];
    }

    private final PlaySession session;
    private final Host host;
    private final boolean dark;
    private final float density;
    private final int ink, soft, card, page, stall;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG), text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF(), closeBox = new RectF(), bubble = new RectF(), junkBox = new RectF(), packBox = new RectF();
    private final Rect source = new Rect();
    private final RectF[] tabs = {new RectF(), new RectF()}, cards = new RectF[RunEconomy.STOCK + RunEconomy.RARE_STOCK];
    private final ArrayDeque<String> queue = new ArrayDeque<>();
    private final Random random = new Random();
    private Bitmap elf, curios;
    private String said = "";
    private int tab = BUY, bought;
    private long start = System.nanoTime();
    private boolean closed;
    private WindowManager window;
    private final long day;
    private float shownAt;
    private String note = "";
    private float noteAge = 9;

    private RunMerchants(Context context, PlaySession session, boolean dark, int[] safe, Host host, int stall) {
        super(context);
        this.session = session;
        this.host = host;
        this.dark = dark;
        this.stall = stall;
        density = context.getResources().getDisplayMetrics().density;
        ink = dark ? 0xFFECECEC : 0xFF2F3B33;
        soft = dark ? 0xFFA3AEA8 : 0xFF6F7F76;
        card = dark ? 0xFF222B27 : 0xFFFFFFFF;
        page = dark ? 0xFF121916 : 0xFFEAF6F1;
        elf = read(context, "runart/elf_idle.webp");
        curios = read(context, "runart/curio_icons.webp");
        day = System.currentTimeMillis() / 86_400_000L;
        for (int index = 0; index < cards.length; index++) cards[index] = new RectF();
        bought = session.tally(soldKey());
        setPadding(safe[0], safe[1], safe[2], safe[3]);
        setClickable(true);
        say(RunMerchantStory.HELLO[random.nextInt(RunMerchantStory.HELLO.length)]);
    }

    private static Bitmap read(Context context, String path) {
        try (InputStream in = context.getAssets().open(path)) {
            return BitmapFactory.decodeStream(in);
        } catch (IOException missing) {
            return null;
        }
    }

    /** Lays her wagon over a view; null if there is nowhere to lay it. */
    static RunMerchants over(View under, PlaySession session, boolean dark, int[] safe, Host host, int stall) {
        try {
            RunMerchants shop = new RunMerchants(under.getContext(), session, dark, safe, host, stall);
            if (under.getParent() instanceof ViewGroup) {
                shop.setZ(under.getZ() + 1);
                ((ViewGroup) under.getParent()).addView(shop, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else if (under.getLayoutParams() instanceof WindowManager.LayoutParams) {
                WindowManager.LayoutParams like = new WindowManager.LayoutParams();
                like.copyFrom((WindowManager.LayoutParams) under.getLayoutParams());
                like.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                like.setTitle("Aelwyn's wagon");
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

    private String soldKey() { return "run_elf_sold_" + day + "_" + stall; }

    private float dp(float value) { return value * density; }

    /** Which of her eight pictures she is in at a moment (in seconds): the same round and round, in the village and in her wagon. */
    static int frameAt(float seconds) {
        float time = Math.max(0, seconds) % IDLE_TOTAL[0], at = 0;
        for (float[] one : IDLE) {
            at += one[1];
            if (time < at) return (int) one[0];
        }
        return 0;
    }

    private void say(String name) {
        queue.add(name);
    }

    private int bank() { return session.tally("run_bank"); }

    // The way it looks

    @Override protected void onDraw(Canvas canvas) {
        if (closed) return;
        float w = getWidth(), h = getHeight();
        paint.setShader(new LinearGradient(0, 0, 0, h, dark ? 0xFF1B2C26 : 0xFFD6F1E6, page, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        if (!queue.isEmpty() && !host.speaking()) {
            String name = queue.poll();
            said = RunMerchantStory.words(name);
            host.say(name);
        }
        noteAge += .016f;
        float left = getPaddingLeft() + dp(14), right = w - getPaddingRight() - dp(14), y = getPaddingTop() + dp(10);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(22));
        canvas.drawText("Aelwyn's Wagon", left, y + dp(24), text);
        text.setTypeface(Typeface.DEFAULT);
        text.setTextSize(dp(12.5f));
        text.setColor(soft);
        canvas.drawText("+ " + bank() + "   ·   curios: " + RunArtifacts.total(session) + "   ·   pouch " + RunArtifacts.carried(session).size() + "/" + RunArtifacts.POUCH, left, y + dp(44), text);
        closeBox.set(right - dp(44), y, right, y + dp(44));
        paint.setColor(card);
        canvas.drawCircle(closeBox.centerX(), closeBox.centerY(), dp(22), paint);
        text.setColor(ink);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(dp(20));
        canvas.drawText("✕", closeBox.centerX(), closeBox.centerY() + dp(7), text);
        y += dp(56);
        // Aelwyn, and what she says.
        float tall = dp(176), wide = tall * 256 / 320;
        float time = ((System.nanoTime() - start) / 1e9f), frame = frameAt(time);
        box.set(left, y, left + wide, y + tall);
        if (elf != null) {
            source.set((int) frame * 256, 0, (int) frame * 256 + 256, 320);
            canvas.drawBitmap(elf, source, box, paint);
        }
        bubble.set(left + wide + dp(8), y + dp(8), right, y + tall - dp(20));
        paint.setColor(card);
        canvas.drawRoundRect(bubble, dp(16), dp(16), paint);
        text.setTextAlign(Paint.Align.LEFT);
        text.setTextSize(dp(14));
        text.setColor(ink);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("Aelwyn", bubble.left + dp(12), bubble.top + dp(22), text);
        text.setTypeface(Typeface.DEFAULT);
        text.setColor(soft);
        float line = bubble.top + dp(42);
        for (String part : wrap(noteAge < 3 ? note : said, bubble.width() - dp(24))) {
            if (line > bubble.bottom - dp(6)) break;
            canvas.drawText(part, bubble.left + dp(12), line, text);
            line += dp(18);
        }
        y += tall + dp(6);
        // The two tabs.
        float each = (right - left - dp(8)) / 2;
        String[] names = {"Buy", "Sell"};
        for (int index = 0; index < 2; index++) {
            tabs[index].set(left + index * (each + dp(8)), y, left + index * (each + dp(8)) + each, y + dp(38));
            paint.setColor(index == tab ? CalculatorLayout.CORAL : card);
            canvas.drawRoundRect(tabs[index], dp(19), dp(19), paint);
            text.setColor(index == tab ? Color.WHITE : ink);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(dp(14));
            canvas.drawText(names[index], tabs[index].centerX(), tabs[index].centerY() + dp(5), text);
        }
        y += dp(48);
        if (tab == BUY) drawBuy(canvas, left, right, y, h - getPaddingBottom() - dp(6));
        else drawSell(canvas, left, right, y);
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

    private void drawBuy(Canvas canvas, float left, float right, float top, float bottom) {
        int[] stock = RunEconomy.stock(day, stall);
        int columns = getWidth() > getHeight() * 1.2f ? 4 : 2, rows = (stock.length + columns - 1) / columns;
        float gap = dp(8), across = (right - left - gap * (columns - 1)) / columns, tall = Math.min(dp(132), (bottom - top - gap * (rows - 1)) / rows);
        for (int index = 0; index < stock.length; index++) {
            int id = stock[index], col = index % columns, row = index / columns;
            RectF at = cards[index];
            at.set(left + col * (across + gap), top + row * (tall + gap), left + col * (across + gap) + across, top + row * (tall + gap) + tall);
            boolean gone = (bought >> index & 1) != 0, rare = index >= RunEconomy.STOCK;
            int price = RunEconomy.buyPrice(id, RunArtifacts.haggle()), rarity = RunArtifacts.rarity(id);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(card);
            canvas.drawRoundRect(at, dp(14), dp(14), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(rare ? 3 : 1.6f));
            paint.setColor(rarity == RunLoot.ROSE ? RunLoot.rose(index) : RunLoot.COLOURS[Math.max(0, Math.min(5, rarity))]);
            canvas.drawRoundRect(at, dp(14), dp(14), paint);
            paint.setStyle(Paint.Style.FILL);
            if (curios != null) {
                source.set((id % 8) * 128, (id / 8) * 128, (id % 8 + 1) * 128, (id / 8 + 1) * 128);
                float size = Math.min(dp(56), tall * .45f);
                box.set(at.left + dp(8), at.top + dp(8), at.left + dp(8) + size, at.top + dp(8) + size);
                paint.setAlpha(gone ? 90 : 255);
                canvas.drawBitmap(curios, source, box, paint);
                paint.setAlpha(255);
            }
            text.setTextAlign(Paint.Align.LEFT);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(dp(12));
            text.setColor(ink);
            float wordsAt = at.left + dp(8) + Math.min(dp(56), tall * .45f) + dp(6), room = at.right - wordsAt - dp(6);
            int lineNo = 0;
            for (String part : wrap(RunArtifacts.name(id), room)) {
                if (lineNo >= 2) break;
                canvas.drawText(part, wordsAt, at.top + dp(20) + lineNo * dp(15), text);
                lineNo++;
            }
            text.setTypeface(Typeface.DEFAULT);
            text.setTextSize(dp(10.5f));
            text.setColor(rare ? CalculatorLayout.CORAL : soft);
            lineNo = 0;
            for (String part : wrap(RunArtifacts.perkWords(id).replace("Gimmick: ", ""), at.width() - dp(16))) {
                if (lineNo >= 2) break;
                canvas.drawText(part, at.left + dp(8), at.top + Math.min(dp(56), tall * .45f) + dp(24) + lineNo * dp(13), text);
                lineNo++;
            }
            RectF buy = new RectF(at.left + dp(8), at.bottom - dp(34), at.right - dp(8), at.bottom - dp(8));
            paint.setColor(gone ? 0xFFBBC4BF : bank() >= price ? CalculatorLayout.CORAL : 0xFFB39A9A);
            canvas.drawRoundRect(buy, dp(13), dp(13), paint);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.WHITE);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(dp(13));
            canvas.drawText(gone ? "Sold" : "+ " + price, buy.centerX(), buy.centerY() + dp(5), text);
            text.setTypeface(Typeface.DEFAULT);
        }
    }

    private void drawSell(Canvas canvas, float left, float right, float top) {
        int[] junk = RunEconomy.junkTotal(session, RunArtifacts.sellBonus());
        float y = top;
        text.setTextAlign(Paint.Align.LEFT);
        text.setTypeface(Typeface.DEFAULT);
        text.setTextSize(dp(13.5f));
        text.setColor(ink);
        canvas.drawText(junk[1] == 0 ? "No junk: nothing plain enough to sell without asking." : junk[1] + (junk[1] == 1 ? " piece" : " pieces") + " of junk (white and blue gear, plain curios)", left, y + dp(16), text);
        text.setColor(soft);
        text.setTextSize(dp(12));
        canvas.drawText("Never what is worn, held, in the pouch or locked." + (RunArtifacts.sellBonus() > 0 ? "  Curios give you +" + RunArtifacts.sellBonus() + "% here." : ""), left, y + dp(36), text);
        y += dp(50);
        junkBox.set(left, y, right, y + dp(48));
        paint.setColor(junk[1] > 0 ? CalculatorLayout.CORAL : 0xFFBBC4BF);
        canvas.drawRoundRect(junkBox, dp(24), dp(24), paint);
        text.setColor(Color.WHITE);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextSize(dp(15));
        canvas.drawText(junk[1] > 0 ? "Sell all junk  + " + junk[0] : "Sell all junk", junkBox.centerX(), junkBox.centerY() + dp(5), text);
        y += dp(60);
        // (The rest is sold from her inventory, the same one the pause card opens: at this counter it has Sell buttons. No separate bag button here.)
        packBox.setEmpty();
        text.setTypeface(Typeface.DEFAULT);
        text.setTextAlign(Paint.Align.LEFT);
        text.setTextSize(dp(12.5f));
        text.setColor(soft);
        for (String part : wrap("To sell anything else, leave my wagon, pause, and open the Inventory while you stand at my counter: every piece has a Sell button there, and a lock to keep your favourites.", right - left)) {
            canvas.drawText(part, left, y + dp(4), text);
            y += dp(17);
        }
    }

    // Touch

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) tap(event.getX(), event.getY());
        return true;
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
                host.play("page");
                return;
            }
        }
        if (tab == SELL) {
            if (junkBox.contains(x, y)) {
                int[] result = RunEconomy.sellJunk(session, RunArtifacts.sellBonus());
                if (result[1] == 0) {
                    note = RunMerchantStory.words("elfnojunk");
                    noteAge = 0;
                    host.say("elfnojunk");
                } else {
                    session.keep("run_bank", bank() + result[0]);
                    RunArtifacts.refresh(session);
                    note = "Sold " + result[1] + (result[1] == 1 ? " piece" : " pieces") + " of junk for + " + result[0] + ". " + RunMerchantStory.words("elfjunk");
                    noteAge = 0;
                    host.play("unlock");
                    host.say("elfjunk");
                }
            }
            return;
        }
        int[] stock = RunEconomy.stock(day, stall);
        for (int index = 0; index < stock.length; index++) {
            if (!cards[index].contains(x, y) || (bought >> index & 1) != 0) continue;
            int id = stock[index], price = RunEconomy.buyPrice(id, RunArtifacts.haggle());
            if (bank() < price) {
                note = RunMerchantStory.words("elfnocoin");
                noteAge = 0;
                host.play("no");
                host.say("elfnocoin");
                return;
            }
            session.keep("run_bank", bank() - price);
            RunArtifacts.add(session, id, 1);
            bought |= 1 << index;
            session.keep(soldKey(), bought);
            String line = index >= RunEconomy.STOCK ? "elfrare" : RunMerchantStory.BUY[random.nextInt(RunMerchantStory.BUY.length)];
            note = "Bought " + RunArtifacts.name(id) + " for + " + price + ". " + RunMerchantStory.words(line);
            noteAge = 0;
            host.play("buy");
            host.say(line);
            return;
        }
    }
}
