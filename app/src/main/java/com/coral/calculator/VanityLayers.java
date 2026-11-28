package com.coral.calculator;

import java.util.HashMap;
import java.util.Map;

/**
 * Where her frames are, on every sheet she is drawn on in Plus run, and where on each frame the parts of her outfit are
 * (the anchors). The table of anchors is {@code assets/vanity_anchors.txt}, made by tools/make_vanity_layers.py: keep the
 * sheets here in step with SHEETS there.
 */
final class VanityLayers {
    private VanityLayers() {}

    /** Name, columns, rows, cell width, cell height. */
    static final Object[][] SHEETS = {
            {"pet", 8, 9, 173, 202}, {"jump", 6, 1, 256, 208}, {"lose", 6, 1, 288, 224}, {"party", 6, 1, 173, 202},
            {"white", 12, 1, 240, 212}, {"back", 6, 1, 204, 236},
            {"arm0", 8, 1, 304, 248}, {"arm1", 8, 1, 304, 248}, {"arm2", 8, 1, 304, 248}, {"arm3", 8, 1, 304, 248},
            {"arm4", 8, 1, 304, 248}, {"arm5", 8, 1, 304, 248}, {"arm6", 12, 1, 500, 248}, {"arm7", 12, 1, 336, 248},
            {"arm8", 14, 1, 400, 248}, {"arm9", 12, 1, 380, 248}, {"arm10", 12, 1, 340, 248}, {"arm11", 12, 1, 500, 248},
            {"arm12", 12, 1, 500, 248}, {"arm13", 12, 1, 500, 248}, {"arm14", 12, 1, 500, 248}, {"arm15", 12, 1, 500, 248}, {"arm16", 12, 1, 500, 248}, {"arm17", 12, 1, 500, 248},
            // Her frames with nothing in her hands, for the weapons found: by the number of the kind of weapon (book 2, greatsword 12, gun 5, hammer 15, katana 8, knife 1, mace 14, pen 0, scythe 17, staff 13, sword 4, wand 16).
            {"grip2", 8, 1, 304, 248}, {"grip12", 12, 1, 500, 248}, {"grip5", 8, 1, 304, 248}, {"grip15", 12, 1, 500, 248}, {"grip8", 14, 1, 400, 248}, {"grip1", 8, 1, 304, 248},
            {"grip14", 12, 1, 500, 248}, {"grip0", 8, 1, 304, 248}, {"grip17", 12, 1, 500, 248}, {"grip13", 12, 1, 500, 248}, {"grip4", 8, 1, 304, 248}, {"grip16", 12, 1, 500, 248},
            // Her moves: flips, air dash, ground pound, roll, cling, glide.
            {"moves", 8, 1, 256, 208},
            // Her swimming: two strokes, a dive, an ink shot.
            {"swim", 4, 1, 256, 208}};

    static int[] geometry(String sheet) {
        for (Object[] one : SHEETS) {
            if (one[0].equals(sheet)) return new int[]{(Integer) one[1], (Integer) one[2], (Integer) one[3], (Integer) one[4]};
        }
        return null;
    }

    static int frames(String sheet) {
        int[] g = geometry(sheet);
        return g == null ? 0 : g[0] * g[1];
    }

    /** Anchors of every frame of every sheet; a frame with no outfit has none. */
    static final class Anchors {
        final Map<String, float[][]> of = new HashMap<>();

        float[] get(String sheet, int frame) {
            float[][] all = of.get(sheet);
            return all == null || frame < 0 || frame >= all.length ? null : all[frame];
        }

        /** How many frames of a sheet have an outfit. */
        int count(String sheet) {
            float[][] all = of.get(sheet);
            int n = 0;
            if (all != null) for (float[] one : all) if (one != null) n++;
            return n;
        }
    }

    /** Reads the table: one line per frame, "sheet frame none" or "sheet frame" and twelve numbers. */
    static Anchors parse(String text) {
        Anchors anchors = new Anchors();
        for (Object[] one : SHEETS) anchors.of.put((String) one[0], new float[frames((String) one[0])][]);
        for (String line : text.split("\n")) {
            String[] part = line.trim().split(" ");
            if (part.length < 3) continue;
            float[][] all = anchors.of.get(part[0]);
            int frame = Integer.parseInt(part[1]);
            if (all == null || frame < 0 || frame >= all.length || part[2].equals("none")) continue;
            float[] numbers = new float[12];
            for (int i = 0; i < 12 && i + 2 < part.length; i++) numbers[i] = Float.parseFloat(part[i + 2]);
            all[frame] = numbers;
        }
        return anchors;
    }
}
