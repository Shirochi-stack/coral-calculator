package com.coral.calculator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Brings the companion back after the device restarts or the app is updated. */
public final class CompanionReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            Companion.get(context).sync();
        }
    }
}
