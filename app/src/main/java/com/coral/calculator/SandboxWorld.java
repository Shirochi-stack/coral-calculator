package com.coral.calculator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * The rules and the map of the sandbox test (no Android in here, so that it can be tested): a tile map of {@link #SIZE} by {@link #SIZE}
 * tiles in chunks of {@link #CHUNK}, made from a seed, and the house a player builds on it in four layers: floor, wall (and door), roof
 * and furniture. Nothing is drawn here.
 */
final class SandboxWorld {
    static final int SIZE = 96, CHUNK = 16, CHUNKS = SIZE / CHUNK;
    /** The ground of a tile. */
    static final byte GRASS = 0, PATH = 1, STONE = 2, WATER = 3;
    /** What grows or lies on a tile of its own. */
    static final byte NONE = 0, TREE = 1, ROCK = 2, BUSH = 3, TUFT = 4;
    /** What the wall layer holds. */
    static final byte NO_WALL = 0, WALL = 1, DOOR = 2;
    /** What the furniture layer holds. */
    static final byte NO_FURNITURE = 0, BED = 1, TABLE = 2, CHEST = 3, LAMP = 4;
    /** What can be built: the order of the build bar. */
    static final int FLOOR = 0, B_WALL = 1, B_DOOR = 2, ROOF = 3, B_BED = 4, B_TABLE = 5, B_CHEST = 6, B_LAMP = 7, DEMOLISH = 8, KINDS = 9;
    static final String[] KIND_NAMES = {"Floor", "Wall", "Door", "Roof", "Bed", "Table", "Chest", "Lamp", "Demolish"};
    /** What is gathered. */
    static final int WOOD = 0, PAPER = 1, STONE_ITEM = 2, ITEMS = 3;
    static final String[] ITEM_NAMES = {"Wood", "Paper", "Stone"};
    /** Sides of a wall: which neighbours it joins. */
    static final int N = 1, E = 2, S = 4, W = 8;
    /** Where she starts, and how far around it is kept clear. */
    static final int SPAWN = SIZE / 2, CLEAR = 6;
    /** How much it takes to build each kind (wood, paper, stone). */
    private static final int[][] COST = {{1, 0, 0}, {2, 0, 0}, {3, 0, 0}, {1, 1, 0}, {4, 3, 0}, {3, 0, 0}, {4, 0, 2}, {1, 1, 1}, {0, 0, 0}};

    final long seed;
    final byte[] ground = new byte[SIZE * SIZE], natural = new byte[SIZE * SIZE], variant = new byte[SIZE * SIZE];
    final byte[] floor = new byte[SIZE * SIZE], wall = new byte[SIZE * SIZE], roof = new byte[SIZE * SIZE], furniture = new byte[SIZE * SIZE];
    final boolean[] doorOpen = new boolean[SIZE * SIZE];
    private final boolean[] made = new boolean[CHUNKS * CHUNKS];
    /** The blows a tree or a rock has taken. */
    private final HashMap<Integer, Integer> hurt = new HashMap<>();
    /** What each chest holds. */
    private final HashMap<Integer, int[]> chests = new HashMap<>();
    /** The lamps that are put out. */
    private final HashSet<Integer> lampsOut = new HashSet<>();

    SandboxWorld(long seed) { this.seed = seed; }

    // The map

    static int at(int x, int y) { return y * SIZE + x; }
    static boolean inside(int x, int y) { return x >= 0 && y >= 0 && x < SIZE && y < SIZE; }

    /** Makes the chunk a tile is in, if it has not been made: every chunk depends on the seed alone, so in whatever order they are made the map is the same. */
    void ensure(int x, int y) {
        if (!inside(x, y)) return;
        int chunk = (y / CHUNK) * CHUNKS + x / CHUNK;
        if (made[chunk]) return;
        made[chunk] = true;
        for (int ty = y / CHUNK * CHUNK; ty < y / CHUNK * CHUNK + CHUNK; ty++) {
            for (int tx = x / CHUNK * CHUNK; tx < x / CHUNK * CHUNK + CHUNK; tx++) {
                int tile = at(tx, ty);
                ground[tile] = groundAt(seed, tx, ty);
                natural[tile] = naturalAt(seed, tx, ty, ground[tile]);
                variant[tile] = (byte) (hash(tx, ty, seed + 7) * 3);
            }
        }
    }

    void ensureAll() { for (int y = 0; y < SIZE; y += CHUNK) for (int x = 0; x < SIZE; x += CHUNK) ensure(x, y); }

    byte groundOf(int x, int y) {
        if (!inside(x, y)) return WATER;
        ensure(x, y);
        return ground[at(x, y)];
    }

    byte naturalOf(int x, int y) {
        if (!inside(x, y)) return NONE;
        ensure(x, y);
        return natural[at(x, y)];
    }

    int variantOf(int x, int y) { return inside(x, y) ? variant[at(x, y)] : 0; }

    static float hash(int x, int y, long seed) {
        long h = x * 374761393L + y * 668265263L + seed * 2246822519L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        h ^= h >>> 16;
        h *= 0x9E3779B97F4A7C15L;
        h ^= h >>> 29;
        h *= 0xBF58476D1CE4E5B9L;
        return ((h >>> 38) & 0xFFFFFF) / (float) 0x1000000;
    }

    private static float smooth(float t) { return t * t * (3 - 2 * t); }

    static float noise(float x, float y, long seed) {
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        float fx = smooth(x - ix), fy = smooth(y - iy);
        float a = hash(ix, iy, seed), b = hash(ix + 1, iy, seed), c = hash(ix, iy + 1, seed), d = hash(ix + 1, iy + 1, seed);
        return (a + (b - a) * fx) + ((c + (d - c) * fx) - (a + (b - a) * fx)) * fy;
    }

    static float fbm(float x, float y, long seed) {
        return (noise(x, y, seed) * .57f + noise(x * 2.1f, y * 2.1f, seed + 11) * .29f + noise(x * 4.3f, y * 4.3f, seed + 23) * .14f);
    }

    private static boolean onPath(long seed, int x, int y) {
        float phase = (seed % 97) * .1f;
        // One that winds across, one down the other way, and a lower one: a few paths that meet near where she starts.
        if (Math.abs(y - (SPAWN + 6 * Math.sin(x * .09f + phase))) < 1.1f) return true;
        if (Math.abs(x - (SPAWN - 14 + 9 * Math.sin(y * .07f + phase * 1.7f))) < 1) return true;
        return x > 20 && Math.abs(y - (SIZE - 14 + 5 * Math.sin(x * .12f + phase * .6f))) < .9f;
    }

    static byte groundAt(long seed, int x, int y) {
        float dx = x - SPAWN, dy = y - SPAWN;
        float near = (float) Math.sqrt(dx * dx + dy * dy);
        if (near < CLEAR + 2) return GRASS;
        if (onPath(seed, x, y)) return PATH;
        float height = fbm(x / 17f, y / 17f, seed);
        if (height < .39f) return WATER;
        if (height > .62f) return STONE;
        return GRASS;
    }

    static byte naturalAt(long seed, int x, int y, byte ground) {
        float dx = x - SPAWN, dy = y - SPAWN;
        if (dx * dx + dy * dy < CLEAR * CLEAR) return NONE;
        float roll = hash(x, y, seed + 3);
        if (ground == STONE) return roll < .16f ? ROCK : roll < .19f ? BUSH : NONE;
        if (ground != GRASS) return NONE;
        float wet = fbm(x / 11f + 100, y / 11f + 100, seed + 1);
        float forest = Math.max(0, Math.min(1, (wet - .42f) / .18f));
        if (roll < .02f + .46f * forest) return TREE;
        if (roll < .03f + .5f * forest) return ROCK;
        if (roll < .06f + .5f * forest) return BUSH;
        return roll < .16f + .5f * forest ? TUFT : NONE;
    }

    // What stands where

    boolean solid(int x, int y) {
        if (!inside(x, y)) return true;
        ensure(x, y);
        int tile = at(x, y);
        if (ground[tile] == WATER && floor[tile] == 0 || natural[tile] == TREE || natural[tile] == ROCK) return true;
        if (wall[tile] == WALL || wall[tile] == DOOR && !doorOpen[tile]) return true;
        return furniture[tile] != NO_FURNITURE;
    }

    /** Which sides of a wall join another wall or a door: what decides how it is drawn. */
    int wallMask(int x, int y) {
        int mask = 0;
        if (hasWall(x, y - 1)) mask |= N;
        if (hasWall(x + 1, y)) mask |= E;
        if (hasWall(x, y + 1)) mask |= S;
        if (hasWall(x - 1, y)) mask |= W;
        return mask;
    }

    boolean hasWall(int x, int y) { return inside(x, y) && wall[at(x, y)] != NO_WALL; }

    /** Whether a door runs along its row (walls to its east or west) rather than its column. */
    boolean doorAcross(int x, int y) {
        int mask = wallMask(x, y);
        return (mask & (E | W)) != 0 || (mask & (N | S)) == 0;
    }

    // Building

    static int[] cost(int kind) { return COST[kind]; }

    static boolean affords(int[] items, int kind) {
        for (int item = 0; item < ITEMS; item++) if (items[item] < COST[kind][item]) return false;
        return true;
    }

    /** Whether a kind may be built (or, for demolish, whether there is anything to take down) on a tile. Not whether it can be paid for. */
    boolean canPlace(int kind, int x, int y) {
        if (!inside(x, y)) return false;
        ensure(x, y);
        int tile = at(x, y);
        if (kind == DEMOLISH) return furniture[tile] != 0 || wall[tile] != 0 || roof[tile] != 0 || floor[tile] != 0;
        if (ground[tile] == WATER || natural[tile] == TREE || natural[tile] == ROCK) return false;
        switch (kind) {
            case FLOOR: return floor[tile] == 0;
            case B_WALL: case B_DOOR: return wall[tile] == 0 && furniture[tile] == 0;
            case ROOF: return roof[tile] == 0 && (floor[tile] != 0 || wall[tile] != 0);
            default: return furniture[tile] == 0 && wall[tile] == 0 && floor[tile] != 0;
        }
    }

    /** Builds it where it can be built; clears a bush or tuft that was in the way. */
    boolean place(int kind, int x, int y) {
        if (kind == DEMOLISH || !canPlace(kind, x, y)) return false;
        int tile = at(x, y);
        if (natural[tile] == BUSH || natural[tile] == TUFT) natural[tile] = NONE;
        switch (kind) {
            case FLOOR: floor[tile] = 1; break;
            case B_WALL: wall[tile] = WALL; break;
            case B_DOOR: wall[tile] = DOOR; doorOpen[tile] = false; break;
            case ROOF: roof[tile] = 1; break;
            default:
                furniture[tile] = (byte) (kind - B_BED + BED);
                if (furniture[tile] == CHEST) chests.put(tile, new int[ITEMS]);
        }
        return true;
    }

    /** Takes down the topmost thing on a tile (furniture, then wall or door, then roof, then floor) and adds what it cost back to refund, with a chest's contents. Returns what it was, or -1. */
    int demolish(int x, int y, int[] refund) {
        if (!canPlace(DEMOLISH, x, y)) return -1;
        int tile = at(x, y), kind;
        if (furniture[tile] != 0) {
            kind = furniture[tile] - BED + B_BED;
            int[] held = chests.remove(tile);
            if (held != null) for (int item = 0; item < ITEMS; item++) refund[item] += held[item];
            lampsOut.remove(tile);
            furniture[tile] = 0;
        } else if (wall[tile] != 0) {
            kind = wall[tile] == DOOR ? B_DOOR : B_WALL;
            wall[tile] = 0;
            doorOpen[tile] = false;
        } else if (roof[tile] != 0) {
            kind = ROOF;
            roof[tile] = 0;
        } else {
            kind = FLOOR;
            floor[tile] = 0;
        }
        for (int item = 0; item < ITEMS; item++) refund[item] += COST[kind][item];
        return kind;
    }

    /** The tiles of the roof that covers a tile, all of it that is joined: what fades when she is under it. Empty where there is no roof. */
    BitSet roofOver(int x, int y) {
        BitSet region = new BitSet(SIZE * SIZE);
        if (!inside(x, y) || roof[at(x, y)] == 0) return region;
        ArrayList<Integer> open = new ArrayList<>();
        open.add(at(x, y));
        region.set(at(x, y));
        while (!open.isEmpty()) {
            int tile = open.remove(open.size() - 1), tx = tile % SIZE, ty = tile / SIZE;
            int[][] around = {{tx + 1, ty}, {tx - 1, ty}, {tx, ty + 1}, {tx, ty - 1}};
            for (int[] next : around) {
                if (!inside(next[0], next[1]) || roof[at(next[0], next[1])] == 0 || region.get(at(next[0], next[1]))) continue;
                region.set(at(next[0], next[1]));
                open.add(at(next[0], next[1]));
            }
        }
        return region;
    }

    // Gathering

    /** How many blows a tree or rock takes before it is gone. */
    static int toughness(byte what) { return what == TREE ? 30 : what == ROCK ? 40 : 1; }

    /**
     * Hurts a tree or rock; when it is gone, clears it and says what it gave in items (wood, paper, stone). Returns null while it still stands.
     * (A bush or tuft is cleared at the first blow and gives a scrap of paper.)
     */
    int[] hurtNatural(int x, int y, float damage, java.util.Random random) {
        byte what = naturalOf(x, y);
        if (what == NONE) return null;
        int tile = at(x, y);
        int total = (hurt.containsKey(tile) ? hurt.get(tile) : 0) + Math.max(1, Math.round(damage));
        if (total < toughness(what)) {
            hurt.put(tile, total);
            return null;
        }
        hurt.remove(tile);
        natural[tile] = NONE;
        int[] gave = new int[ITEMS];
        if (what == TREE) {
            gave[WOOD] = 2 + random.nextInt(2);
            gave[PAPER] = random.nextInt(3);
        } else if (what == ROCK) {
            gave[STONE_ITEM] = 2 + random.nextInt(3);
        } else {
            gave[PAPER] = 1;
        }
        return gave;
    }

    /** How hurt a tree or rock is, from none to nearly gone, as a part. */
    float hurtPart(int x, int y) {
        Integer blows = hurt.get(at(x, y));
        return blows == null ? 0 : Math.min(1, blows / (float) toughness(naturalOf(x, y)));
    }

    // Chests and lamps

    int[] chest(int x, int y) { return inside(x, y) ? chests.get(at(x, y)) : null; }
    boolean lit(int x, int y) { return inside(x, y) && furniture[at(x, y)] == LAMP && !lampsOut.contains(at(x, y)); }
    void toggleLamp(int x, int y) {
        if (!inside(x, y) || furniture[at(x, y)] != LAMP) return;
        if (!lampsOut.remove(at(x, y))) lampsOut.add(at(x, y));
    }

    /** The doors open to whoever is near: a door is open when something stands within a tile and a half of it. */
    void updateDoors(float[] xs, float[] ys, int count) {
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) if (wall[at(x, y)] == DOOR) doorOpen[at(x, y)] = false;
        for (int index = 0; index < count; index++) {
            int fromX = (int) Math.floor(xs[index] - 1.6f), toX = (int) Math.floor(xs[index] + 1.6f);
            int fromY = (int) Math.floor(ys[index] - 1.6f), toY = (int) Math.floor(ys[index] + 1.6f);
            for (int y = Math.max(0, fromY); y <= Math.min(SIZE - 1, toY); y++) {
                for (int x = Math.max(0, fromX); x <= Math.min(SIZE - 1, toX); x++) {
                    if (wall[at(x, y)] != DOOR) continue;
                    float dx = xs[index] - (x + .5f), dy = ys[index] - (y + .5f);
                    if (dx * dx + dy * dy < 1.5f * 1.5f) doorOpen[at(x, y)] = true;
                }
            }
        }
    }

    // Saving

    /** Everything built or gathered, as text: the seed, then one entry for each. */
    String save() {
        StringBuilder out = new StringBuilder("S1;").append(seed);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int tile = at(x, y);
                if (floor[tile] != 0) out.append(";F,").append(x).append(',').append(y);
                if (wall[tile] != 0) out.append(";W,").append(x).append(',').append(y).append(',').append(wall[tile]);
                if (roof[tile] != 0) out.append(";R,").append(x).append(',').append(y);
                if (furniture[tile] != 0) out.append(";U,").append(x).append(',').append(y).append(',').append(furniture[tile]);
                int[] held = chests.get(tile);
                if (held != null) out.append(";C,").append(x).append(',').append(y).append(',').append(held[0]).append(',').append(held[1]).append(',').append(held[2]);
                if (lampsOut.contains(tile)) out.append(";L,").append(x).append(',').append(y);
                if (made[(y / CHUNK) * CHUNKS + x / CHUNK] && natural[tile] != naturalAt(seed, x, y, ground[tile])) out.append(";X,").append(x).append(',').append(y);
            }
        }
        return out.toString();
    }

    /** The world a save was made from; null if it is not one. */
    static SandboxWorld load(String text) {
        if (text == null) return null;
        try {
            String[] parts = text.split(";");
            if (parts.length < 2 || !parts[0].equals("S1")) return null;
            SandboxWorld world = new SandboxWorld(Long.parseLong(parts[1]));
            for (int index = 2; index < parts.length; index++) {
                String[] f = parts[index].split(",");
                int x = Integer.parseInt(f[1]), y = Integer.parseInt(f[2]);
                if (!inside(x, y)) continue;
                world.ensure(x, y);
                int tile = at(x, y);
                switch (f[0]) {
                    case "F": world.floor[tile] = 1; break;
                    case "W": world.wall[tile] = Byte.parseByte(f[3]); break;
                    case "R": world.roof[tile] = 1; break;
                    case "U": world.furniture[tile] = Byte.parseByte(f[3]); break;
                    case "C": world.chests.put(tile, new int[]{Integer.parseInt(f[3]), Integer.parseInt(f[4]), Integer.parseInt(f[5])}); break;
                    case "L": world.lampsOut.add(tile); break;
                    case "X": world.natural[tile] = NONE; break;
                    default: break;
                }
            }
            return world;
        } catch (RuntimeException broken) {
            return null;
        }
    }

    /** How many tiles of each kind the world has, for a look at what a seed made: grass, path, stone, water. */
    int[] census() {
        ensureAll();
        int[] counts = new int[4];
        for (byte tile : ground) counts[tile]++;
        return counts;
    }

    int count(byte what) {
        ensureAll();
        int total = 0;
        for (byte tile : natural) if (tile == what) total++;
        return total;
    }

    List<int[]> built() {
        ArrayList<int[]> all = new ArrayList<>();
        for (int tile = 0; tile < SIZE * SIZE; tile++) if (floor[tile] != 0 || wall[tile] != 0 || roof[tile] != 0 || furniture[tile] != 0) all.add(new int[]{tile % SIZE, tile / SIZE});
        return all;
    }
}
