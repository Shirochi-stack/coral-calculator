package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.graphics.Shader;

import java.io.IOException;
import java.io.InputStream;

/**
 * The sandbox's pictures, read from the asset pack "sandbox" (assets/sandbox/*.webp: tileable textures of 256 px, her walk sheets of six
 * cells of 256 by 240 in a row, and small grids of 256 px cells). Kept apart from the rest of the app's art, which is why it is a pack.
 */
final class SandboxArt {
    static final int GRASS = 0, PATH = 1, STONE = 2, WATER = 3, FLOOR = 4, WALL = 5, ROOF = 6, TEXTURES = 7;
    private static final String[] TEXTURE_FILES = {"grass", "path", "stone", "water", "floor", "wall", "roof"};
    /** Her cells, and how many tiles one of them spans. */
    static final int HER_W = 256, HER_H = 240, HER_FEET = 232;
    static final int TREE_W = 320, TREE_H = 384, TREE_FOOT = 380;
    static final int CELL = 256, CELL_FOOT = 250;
    /** The picture of a loot icon: squares of 128, sixteen to a row (as Plus run's own sheet). */
    static final int ICON = 128, ICON_COLUMNS = 16;

    final Bitmap[] textures = new Bitmap[TEXTURES];
    final BitmapShader[] shaders = new BitmapShader[TEXTURES];
    Bitmap herDown, herUp, herSide, trees, rocks, furniture, creatures, icons;
    boolean ok;

    SandboxArt(Context context) {
        try {
            for (int index = 0; index < TEXTURES; index++) {
                textures[index] = read(context, "tex_" + TEXTURE_FILES[index]);
                shaders[index] = new BitmapShader(textures[index], Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            }
            herDown = read(context, "her_down");
            herUp = read(context, "her_up");
            herSide = read(context, "her_side");
            trees = read(context, "trees");
            rocks = read(context, "rocks");
            furniture = read(context, "furniture");
            creatures = read(context, "creatures");
            ok = true;
        } catch (IOException | RuntimeException missing) {
            ok = false;
        }
        icons = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_loot_icons);
    }

    private static Bitmap read(Context context, String name) throws IOException {
        try (InputStream in = context.getAssets().open("sandbox/" + name + ".webp")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            if (bitmap == null) throw new IOException(name);
            return bitmap;
        }
    }

    /** A texture's shader, scaled so that one picture covers {@code tiles} tiles of the world (the canvas is in tiles). */
    BitmapShader shader(int texture, float tiles) {
        Matrix matrix = new Matrix();
        matrix.setScale(tiles / CELL, tiles / CELL);
        shaders[texture].setLocalMatrix(matrix);
        return shaders[texture];
    }

    /** A second shader on the same texture, for a texture used at two scales at once. */
    BitmapShader extra(int texture, float tiles) {
        BitmapShader made = new BitmapShader(textures[texture], Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        Matrix matrix = new Matrix();
        matrix.setScale(tiles / CELL, tiles / CELL);
        made.setLocalMatrix(matrix);
        return made;
    }
}
