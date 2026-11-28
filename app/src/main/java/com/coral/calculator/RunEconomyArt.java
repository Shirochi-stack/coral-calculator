package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.io.IOException;
import java.io.InputStream;

/**
 * The pictures of the economy and the new weapons, kept in the runart asset pack and not in the base module: the curios' sheet (eight
 * squares of 128 to a row) and the new weapons' (sixteen to a row). A "loot icon" number from {@link RunArtifacts#ICON_BASE} up is one of
 * these, not one of {@code run_loot_icons}'s.
 */
final class RunEconomyArt {
    private RunEconomyArt() {}

    static final int SQUARE = 128, CURIO_COLUMNS = 8, WEAPON_COLUMNS = 16;
    private static Bitmap curios, weapons;
    private static boolean curiosTried, weaponsTried;

    /** Whether an icon number is one of these. */
    static boolean extra(int index) { return index >= RunArtifacts.ICON_BASE; }

    /** The sheet an icon is in, or null where it is missing (or the number is an ordinary one). */
    static Bitmap sheet(Context context, int index) {
        if (index >= RunWeapons.ICON_BASE) {
            if (weapons == null && !weaponsTried) {
                weaponsTried = true;
                weapons = read(context, "runart/weapon_icons.webp");
            }
            return weapons;
        }
        if (index >= RunArtifacts.ICON_BASE) {
            if (curios == null && !curiosTried) {
                curiosTried = true;
                curios = read(context, "runart/curio_icons.webp");
            }
            return curios;
        }
        return null;
    }

    /** Where an icon is in its sheet. */
    static void source(int index, Rect into) {
        int columns, place;
        if (index >= RunWeapons.ICON_BASE) {
            columns = WEAPON_COLUMNS;
            place = index - RunWeapons.ICON_BASE;
        } else {
            columns = CURIO_COLUMNS;
            place = index - RunArtifacts.ICON_BASE;
        }
        int column = place % columns, row = place / columns;
        into.set(column * SQUARE, row * SQUARE, (column + 1) * SQUARE, (row + 1) * SQUARE);
    }

    private static Bitmap read(Context context, String path) {
        try (InputStream in = context.getAssets().open(path)) {
            return BitmapFactory.decodeStream(in);
        } catch (IOException missing) {
            return null;
        }
    }
}
