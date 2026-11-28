package com.coral.calculator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

/** The 2.5D sandbox test, full screen, in portrait or landscape: all of it is {@link SandboxView}. */
public final class SandboxActivity extends Activity {
    private SandboxView view;

    @SuppressWarnings("deprecation")
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        view = new SandboxView(this, this::finish);
        setContentView(view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        view.requestFocus();
        // For test runs (adb shell am start ... --es scene house|frame|tree --ei weapon 0..12 --ei rarity 0..5 --ef zoom 1 --ef x 48 --ef y 50 --ez attack true --ez fight true --ez fresh true):
        android.content.Intent intent = getIntent();
        if (intent.getBooleanExtra("fresh", false)) view.fresh();
        if (intent.hasExtra("scene") || intent.hasExtra("weapon") || intent.hasExtra("zoom") || intent.hasExtra("x") || intent.getBooleanExtra("fight", false)) {
            view.demo(intent.getStringExtra("scene"), intent.getIntExtra("weapon", -1), intent.getIntExtra("rarity", -1), intent.getFloatExtra("zoom", 0),
                    intent.getFloatExtra("x", 0), intent.getFloatExtra("y", 0), intent.getBooleanExtra("attack", false), intent.getBooleanExtra("fight", false));
        }
    }

    @Override protected void onPause() {
        view.pause();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        view.resume();
    }

    @Override public boolean onKeyDown(int code, KeyEvent event) {
        if (view.key(code, true)) return true;
        return super.onKeyDown(code, event);
    }

    @Override public boolean onKeyUp(int code, KeyEvent event) {
        if (view.key(code, false)) return true;
        return super.onKeyUp(code, event);
    }

    @Override public void onBackPressed() {
        if (!view.back()) super.onBackPressed();
    }
}
