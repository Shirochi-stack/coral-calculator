package com.coral.calculator;

import java.util.List;

/**
 * Weapons and creatures for the sandbox, with no Android in them: the weapon picker's table, read from the real weapons of Plus run
 * ({@link RunLoot#WEAPONS}, {@link RunArms}), how a blow is shaped in the world, and what a blow does to a creature.
 */
final class SandboxCombat {
    /** How a weapon is used in the world: a swing in an arc, a shot, a lob that bursts where it lands, a slam that rings out, a fan of shots. */
    static final int ARC = 0, SHOT = 1, LOB = 2, RING = 3, FAN = 4;
    /** Which theme the picker's weapons are of (the first that is not paper, which is the weak one). */
    static final int THEME = 1;

    /** One weapon of the picker. All lengths are in tiles, all times in seconds. */
    static final class Weapon {
        int index, kind, code, rarity, shape, shots;
        String name, does;
        /** Damage of one blow; seconds before the next; how far it reaches (a swing's length, a shot's flight, a lob's landing); how fast it flies; how wide a swing is in half-angle, or a burst in radius. */
        float damage, rest, reach, speed, spread, burst, knock;
        boolean through, both;
        int colour;
    }

    private SandboxCombat() {}

    static int weapons() { return RunLoot.WEAPONS.length; }

    /** Weapon number {@code index} of {@link RunLoot#WEAPONS}, as it is at a rarity (white to the rose). */
    static Weapon weapon(int index, int rarity) {
        Weapon w = new Weapon();
        w.index = index;
        w.rarity = Math.max(0, Math.min(RunLoot.ROSE, rarity));
        w.code = RunLoot.code(RunLoot.WEAPON, THEME, index, w.rarity);
        w.kind = RunArms.kindOf(w.code);
        w.name = RunLoot.WEAPONS[index];
        w.does = RunArms.does(w.kind);
        int blows = 1 + Math.max(0, RunArms.itemBlows(w.code));
        w.damage = 7 * blows;
        w.rest = RunArms.rest(w.kind, 0) * RunArms.itemRest(w.code);
        float far = RunArms.reach(w.kind, 0) * RunArms.itemReach(w.code);
        w.colour = 0xFFE7C85A;
        w.shots = 1;
        w.knock = 3;
        w.speed = 0;
        int way = RunArms.way(w.kind);
        if (way == RunArms.SWUNG) {
            w.shape = ARC;
            // (A square of Plus run is about a body's width; in this world her reach is a tile and a bit more for every square.)
            w.reach = .9f + far * .9f;
            w.spread = (float) Math.toRadians(Math.max(32, Math.min(95, 30 + RunArms.area(w.kind, 0) * 22)));
            w.knock = w.kind == RunArms.MACE ? 6 : w.kind == RunArms.HAMMER || w.kind == RunArms.GREATSWORD ? 5 : 3;
            if (w.kind == RunArms.KATANA) w.spread = (float) Math.toRadians(26);
            if (w.kind == RunArms.SCYTHE) w.both = true;
            if (w.kind == RunArms.HAMMER) {
                w.shape = RING;
                w.burst = 2.3f;
                w.reach = 1.3f;
            }
            if (w.kind == RunArms.KNUCKLES) w.damage *= .8f;
        } else if (way == RunArms.LOBBED) {
            w.shape = LOB;
            w.reach = Math.min(9, far * .9f + 1);
            w.speed = Math.max(4, RunArms.speed(w.kind) * .6f);
            w.burst = Math.max(1.5f, RunArms.burst(w.kind, 0)) * .9f;
            w.damage *= 1.1f;
        } else {
            w.shape = w.kind == RunArms.STAFF ? FAN : SHOT;
            w.reach = Math.min(11, far * .75f + 1.5f);
            w.speed = Math.max(7, RunArms.speed(w.kind) * .6f);
            if (w.kind == RunArms.STAFF) {
                w.shots = 3;
                w.spread = .28f;
                w.colour = 0xFFB59BFF;
            }
            if (w.kind == RunArms.WAND) w.colour = new int[]{0xFFFF7A3C, 0xFF7AD7FF, 0xFFFFE94A, 0xFF3A3F5C}[RunArms.element(w.code)];
            if (w.kind == RunArms.GUN) {
                w.spread = .05f;
                w.colour = 0xFF40445A;
                w.damage *= .6f;
            }
            w.through = w.kind == RunArms.PEN && w.rarity >= RunLoot.GOLD;
            w.knock = 1.5f;
        }
        return w;
    }

    static Weapon[] all(int rarity) {
        Weapon[] list = new Weapon[weapons()];
        for (int index = 0; index < list.length; index++) list[index] = weapon(index, rarity);
        return list;
    }

    /** A line of figures for the picker. */
    static String stats(Weapon w) {
        String how = w.shape == ARC ? "swing" : w.shape == RING ? "slam" : w.shape == LOB ? "lob" : w.shape == FAN ? "fan of " + w.shots : "shot";
        return how + "  damage " + Math.round(w.damage) + "  reach " + String.format(java.util.Locale.US, "%.1f", w.reach) + "  rest " + String.format(java.util.Locale.US, "%.2fs", w.rest);
    }

    // Creatures

    static final int SLIME = 0, BAT = 1, WOLF = 2, DUMMY = 3;
    static final String[] FOE_NAMES = {"Ink blot", "Paper bat", "Ink wolf", "Training dummy"};
    private static final float[] HEALTH = {42, 28, 70, 220};
    private static final float[] PACE = {1.1f, 2.6f, 2.9f, 0};
    private static final float[] BITE = {4, 3, 6, 0};

    /** A creature (or a dummy) in the world. */
    static final class Foe {
        int kind;
        float x, y, vx, vy, health, max, flash, wander, bite, born, regrow, face = 1;
        float sinceHit = 99;
        boolean dead;
        Foe(int kind, float x, float y) {
            this.kind = kind;
            this.x = x;
            this.y = y;
            max = health = HEALTH[kind];
        }
        float pace() { return PACE[kind]; }
        float damage() { return BITE[kind]; }
        float radius() { return kind == WOLF ? .45f : kind == DUMMY ? .4f : .35f; }
    }

    /**
     * A blow on a creature: takes the damage off its health, throws it away along (dx, dy), makes it flash. A dummy cannot die: it stands
     * back up with its health full once it would have. Returns what it took.
     */
    static float hit(Foe foe, float damage, float dx, float dy, float knock) {
        if (foe.dead) return 0;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
        }
        foe.health -= damage;
        foe.flash = .22f;
        foe.sinceHit = 0;
        if (foe.kind != DUMMY) {
            foe.vx += dx * knock * 2.4f;
            foe.vy += dy * knock * 2.4f;
        } else {
            foe.vx += dx * knock * .5f;
            foe.vy += dy * knock * .5f;
        }
        if (foe.health <= 0) {
            if (foe.kind == DUMMY) foe.health = foe.max;
            else {
                foe.health = 0;
                foe.dead = true;
            }
        }
        return damage;
    }

    /** Whether a point is inside the arc of a swing from (x, y) facing {@code angle}: within {@code reach} and {@code spread} either side of the facing. */
    static boolean inArc(float x, float y, float angle, float reach, float spread, float px, float py, float radius) {
        float dx = px - x, dy = py - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > reach + radius) return false;
        if (distance < radius + .3f) return true;
        float off = Math.abs(wrap((float) Math.atan2(dy, dx) - angle));
        float widen = (float) Math.asin(Math.min(1, radius / distance));
        return off <= spread + widen;
    }

    static float wrap(float angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    /** Whether a swing (or a slam) from (x, y) facing {@code angle} reaches a point with a radius. */
    static boolean reaches(Weapon w, float x, float y, float angle, float px, float py, float radius) {
        if (w.shape == RING) return Math.hypot(px - (x + Math.cos(angle) * w.reach), py - (y + Math.sin(angle) * w.reach)) < w.burst + radius;
        return inArc(x, y, angle, w.reach, w.spread, px, py, radius) || w.both && inArc(x, y, angle + (float) Math.PI, w.reach, w.spread, px, py, radius);
    }

    /** Applies a swing to every creature in its arc (and behind, for a weapon that goes both ways); returns how many were hit. */
    static int swing(Weapon w, float x, float y, float angle, List<Foe> foes) {
        int hits = 0;
        for (Foe foe : foes) {
            if (foe.dead || !reaches(w, x, y, angle, foe.x, foe.y, foe.radius())) continue;
            hit(foe, w.damage, foe.x - x, foe.y - y, w.knock);
            hits++;
        }
        return hits;
    }
}
