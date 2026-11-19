package com.coral.calculator;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

/**
 * Holds nothing itself: the companion's windows belong to {@link Companion}. Being started
 * and sticky, it has Android start the process again, and with it the companion, should the
 * process be removed while she is switched on.
 */
public final class CompanionService extends Service {
    @Override public void onCreate() {
        super.onCreate();
        Companion.get(this).sync();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Companion.get(this).screenChanged();
    }

    @Override public void onDestroy() {
        Companion.get(this).serviceStopped();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
