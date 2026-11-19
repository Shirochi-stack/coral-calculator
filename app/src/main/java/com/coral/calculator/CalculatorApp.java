package com.coral.calculator;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

public final class CalculatorApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        // However the process came to start, the companion returns if she is switched on. The
        // short wait lets the calculator open first when it is what started the process, so
        // she does not flash up over it.
        new Handler(Looper.getMainLooper()).postDelayed(() -> Companion.get(this).sync(), 400);
    }
}
