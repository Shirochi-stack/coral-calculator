package com.coral.calculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/** Asking for Android's permission to display over other apps, which the companion needs. */
final class CompanionPermission {
    private static final String STEPS = "Android will show its “Display over other apps” list. Choose Halgakos "
            + "Calculator there, allow it, and come back.";

    private CompanionPermission() {}

    /**
     * Explains what is about to be asked, then opens Android's list.
     *
     * @param opened run once the list has been opened
     * @param unavailable run if this device has no such setting
     */
    static void request(Activity activity, String title, String lead, Runnable opened, Runnable unavailable) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(lead + "\n\n" + STEPS + "\n\nThe calculator uses this only to draw the companion "
                        + "and her mini calculator.")
                .setNegativeButton("Not now", null)
                .setPositiveButton("Continue", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + activity.getPackageName()));
                    try {
                        activity.startActivity(intent);
                        if (opened != null) opened.run();
                    } catch (ActivityNotFoundException missing) {
                        if (unavailable != null) unavailable.run();
                    }
                })
                .show();
    }

    /**
     * The companion is switched on from the start, but cannot appear until this is allowed.
     * Offered once, the first time the calculator is opened without it.
     */
    static void offerOnce(Activity activity) {
        CompanionPrefs prefs = new CompanionPrefs(activity);
        if (!prefs.enabled() || prefs.offered() || Settings.canDrawOverlays(activity)) return;
        prefs.setOffered();
        request(activity, "Meet Halgakos", "Halgakos can float over your other apps with a mini calculator "
                + "in hand. Turn her off at any time in Settings › Halgakos companion.", null, null);
    }
}
