package com.coral.calculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

/**
 * The app's ID changed in 1.2.0, so Android keeps a release from before that as a separate
 * app with the same name and icon, and with a companion of its own. This points it out.
 */
final class OldCopy {
    /** The ID of every release up to 1.1.4. Declared in the manifest so that it can be seen. */
    private static final String PACKAGE = "com.coral.calculator";

    private OldCopy() {}

    static boolean installed(Activity activity) {
        if (PACKAGE.equals(activity.getPackageName())) return false;
        try {
            activity.getPackageManager().getPackageInfo(PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException absent) {
            return false;
        }
    }

    /** Opens Android's page for the old app, which is where it is uninstalled. */
    static void open(Activity activity) {
        try {
            activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + PACKAGE)));
        } catch (RuntimeException unavailable) {
            // Left to the user: the dialog has already said what to remove.
        }
    }

    /**
     * Says so if the old app is still installed, then carries on.
     *
     * @param next run when there is nothing to say, or once the dialog has been answered
     */
    static void offer(Activity activity, Runnable next) {
        if (!installed(activity)) {
            next.run();
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle("An older copy is still installed")
                .setMessage("This is Halgakos Calculator " + BuildConfig.VERSION_NAME + ". An earlier release is "
                        + "installed beside it as a separate app with the same name, its own settings and its "
                        + "own Halgakos.\n\nUninstall the older one so that you are not looking at two. Copy "
                        + "anything you need from its history first: it does not carry over.")
                .setNegativeButton("Later", (dialog, which) -> next.run())
                .setPositiveButton("Show the old app", (dialog, which) -> open(activity))
                .setOnCancelListener(dialog -> next.run())
                .show();
    }
}
