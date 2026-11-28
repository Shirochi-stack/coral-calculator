package com.coral.calculator;

import java.util.Arrays;
import java.util.Comparator;

/**
 * The order the sandbox draws things in: what is further up the screen (a smaller y, further from the viewer) first, so that whatever is nearer
 * is drawn over it. Halgakos is behind a tree when her feet are above its foot, and in front when they are below.
 */
final class SandboxDepth {
    /** What kind of thing, for those at the same depth: the ground's marks first, then the things that stand, then what flies and floats. */
    static final int DECAL = 0, STANDING = 1, ROOF = 2, FLYING = 3, LABEL = 4;

    /** One thing to draw: where its foot is (the y that decides), what kind it is, and what it is (for the drawer). */
    static final class Item {
        float x, y;
        int layer, kind, index;
        Item set(float x, float y, int layer, int kind, int index) {
            this.x = x;
            this.y = y;
            this.layer = layer;
            this.kind = kind;
            this.index = index;
            return this;
        }
    }

    static final Comparator<Item> ORDER = (a, b) -> {
        if (a.layer >= FLYING || b.layer >= FLYING) {
            if (a.layer != b.layer) return Integer.compare(a.layer, b.layer);
        }
        int byY = Float.compare(a.y, b.y);
        if (byY != 0) return byY;
        if (a.layer != b.layer) return Integer.compare(a.layer, b.layer);
        int byX = Float.compare(a.x, b.x);
        return byX != 0 ? byX : Integer.compare(a.index, b.index);
    };

    private SandboxDepth() {}

    static void sort(Item[] items, int count) { Arrays.sort(items, 0, count, ORDER); }
}
