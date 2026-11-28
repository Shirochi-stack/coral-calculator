package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Her skill tree: roots branching from a centre, two paths for each of the twelve classes of weapon, twenty-four in all. Along each path
 * small nodes give a point of health, defence, intelligence or attack; bigger root nodes give 3, 5, 10 or 20 of one; and at its turns,
 * notable skills: passives, some strong with a cost, and actives that change what a weapon does, or turn her energy into divine power.
 * Near their ends paths cross over to their neighbours, so that builds can mix. Points come from her levels, without end; a node can be
 * taken when one next to it is. The tree is the same for everyone: made from numbers, not chance.
 */
final class RunSkills {
    private RunSkills() {}

    // The stats

    static final int HP = 0, DEF = 1, INT = 2, ATK = 3, STATS = 4;
    static final String[] STAT_NAMES = {"HP", "Defence", "Intelligence", "Attack"};

    // The paths

    /** The twelve classes of weapon, each with two paths. */
    static final String[] CLASSES = {"Swords", "Katana", "Greatswords", "Books", "Summoning staffs", "Knives", "Pens and pencils", "Maces", "Hammers", "Wands",
            "Ink guns", "Scythes", "Knuckles"};
    /** (The knuckles' two came last: their nodes are after everyone else's, so that what was taken before is still what it was.) */
    static final int PATHS = 26, FIRST_PATHS = 24;
    static final String[] PATH_NAMES = {
            "Bladesong", "Duelist", "Iaido", "Ronin's Ink", "Colossus", "Earthsplitter", "Archivist", "Lorekeeper", "Conjurer", "Spiritbinder",
            "Shadowstep", "Venomfang", "Calligrapher", "Inkstorm", "Bonebreaker", "Warden", "Thunderforge", "Quakebringer", "Elementalist", "Divine Script",
            "Gunslinger", "Inkwell Artillery", "Reaper", "Harvest Moon", "Iron Fist", "Pressure Point"};
    /** Each path's colour, for its branch and its icon. */
    static final int[] PATH_COLOURS = {0xFF6FA8FF, 0xFF3F7FE0, 0xFFE0475A, 0xFF8A2A3A, 0xFFB08A50, 0xFF7A5A30, 0xFF2FB5A8, 0xFF1F7F78, 0xFF9A6AE8, 0xFF6A3FC0,
            0xFF4A4F60, 0xFF5FBF6A, 0xFFE89A3F, 0xFF3A50C8, 0xFFC0603A, 0xFF8A8F98, 0xFFFFC433, 0xFF9A6A3A, 0xFF4FC0E8, 0xFFFFE08A, 0xFF2A3FC8, 0xFF1B2152,
            0xFF7A3A9A, 0xFFE0D0FF, 0xFFE0A030, 0xFF3FB0E0};

    /** The class a path is for. */
    static int classOf(int path) { return path / 2; }

    /** The weapon a class is (in {@link RunArms}). */
    static int weaponOf(int clazz) { return RunArms.OF_KIND[clazz]; }

    // The notable skills

    /** Passive: always on. Active: changes what a weapon does when it is used, or what its held special does. */
    static final int PASSIVE = 0, ACTIVE = 1;

    /** What a notable skill does: its effect, its figure, a cost (an effect and its figure, or none), and whether it is active. */
    static final int E_ATK_PCT = 0, E_DEF_PCT = 1, E_HP_PCT = 2, E_INT_PCT = 3, E_LEECH = 4, E_REGEN = 5, E_CRIT = 6, E_CRITX = 7, E_HASTE = 8, E_AREA = 9,
            E_PROJECTILES = 10, E_ENERGY = 11, E_DP = 12, E_SHIELD = 13, E_POISON = 14, E_BURN = 15, E_FREEZE = 16, E_REACH = 17, E_SPECIAL_COST = 18,
            E_COMBO = 19, E_DODGE = 20, E_THORNS = 21, E_PIERCE = 22, E_STUN = 23, E_EFFECTS = 24;
    static final String[] EFFECT_WORDS = {"% attack", "% defence", "% HP", "% intelligence", "% of damage back as HP", " HP a second back", "% chance of a great blow",
            "% harder great blows", "% faster attacks", "% wider area", " more projectiles", "% faster energy", "% of energy spent becomes divine power",
            " shield pieces", "% of blows ink-poison", "% of blows burn", "% of blows freeze", "% further reach", "% cheaper specials", " more hits in a combo",
            "% chance to dodge", " blows of thorns", " more pierce", "% of blows stun"};

    /** A notable skill: its name, whether active, its effect and figure, and its cost (-1 for none) and figure. */
    static final class Notable {
        final String name;
        final int kind, effect, figure, cost, costFigure;
        /** The path it is on, and its place along it (the twelfth, 11, the keystone at its end). */
        int path = -1, index = -1;

        Notable(String name, int kind, int effect, int figure, int cost, int costFigure) {
            this.name = name;
            this.kind = kind;
            this.effect = effect;
            this.figure = figure;
            this.cost = cost;
            this.costFigure = costFigure;
        }

        String said() {
            String good = (figure > 0 ? "+" : "") + figure + EFFECT_WORDS[effect];
            return cost < 0 ? good : good + ", but " + costFigure + EFFECT_WORDS[cost];
        }
    }

    /** The twelve notables of each path, built from the path's own leanings: two each of six themes, alternately passive and active. */
    static List<Notable> notables(int path) {
        synchronized (MADE) {
            if (MADE[path] == null) MADE[path] = make(path);
            return MADE[path];
        }
    }

    private static final List<?>[] MADE_LISTS = new List<?>[PATHS];
    @SuppressWarnings("unchecked")
    private static final List<Notable>[] MADE = (List<Notable>[]) MADE_LISTS;

    private static List<Notable> make(int path) {
        int clazz = classOf(path), side = path % 2;
        String[] names = NOTABLE_NAMES[path];
        int[][] themes = side == 0 ? new int[][]{{E_ATK_PCT, 12}, {E_HASTE, 10}, {E_CRIT, 8}, {E_AREA, 20}, {E_COMBO, 1}, {E_REACH, 15}}
                : new int[][]{{E_LEECH, 4}, {E_SHIELD, 1}, {E_DEF_PCT, 12}, {E_PROJECTILES, 1}, {E_DP, 20}, {E_REGEN, 1}};
        // A class's own twist on the second of its themes.
        int[] twist = {E_CRITX, E_COMBO, E_STUN, E_INT_PCT, E_PROJECTILES, E_POISON, E_PIERCE, E_STUN, E_AREA, E_FREEZE, E_HASTE, E_LEECH, E_COMBO};
        List<Notable> list = new ArrayList<>();
        for (int index = 0; index < 12; index++) {
            int[] theme = themes[index % 6];
            int effect = index == 7 ? twist[clazz] : theme[0];
            int figure = theme[1] * (1 + index / 6);
            boolean active = index % 2 == 1;
            // Every third passive is strong, with a cost: twice as much, and something given up for it.
            int cost = -1, costFigure = 0;
            if (!active && index % 3 == 0) {
                figure *= 2;
                cost = effect == E_DEF_PCT || effect == E_SHIELD ? E_ATK_PCT : effect == E_LEECH ? E_ENERGY : E_DEF_PCT;
                costFigure = -Math.max(10, figure / 2);
            }
            Notable made = new Notable(names[index], active ? ACTIVE : PASSIVE, effect, figure, cost, costFigure);
            made.path = path;
            made.index = index;
            list.add(made);
        }
        return java.util.Collections.unmodifiableList(list);
    }

    private static final String[][] NOTABLE_NAMES = new String[PATHS][12];

    static {
        String[] first = {"Opening", "Rising", "Turning", "Burning", "Silent", "Bright", "Deep", "Endless", "Crimson", "Final", "Hidden", "True"};
        for (int path = 0; path < PATHS; path++) {
            String word = PATH_NAMES[path];
            for (int index = 0; index < 12; index++) NOTABLE_NAMES[path][index] = first[(index + path) % first.length] + " " + word + (index % 2 == 1 ? " Art" : "");
        }
    }

    // The tree

    /** What a node is: a small step of a stat, a root of a stat, a notable skill, or the centre. */
    static final int SMALL = 0, ROOT = 1, NOTABLE = 2, CENTRE = 3;
    /** How many of each a path has along it: its nodes, and every how many a root; the roots' figures in turn. */
    static final int PATH_LENGTH = 44;
    static final int[] ROOT_FIGURES = {3, 5, 10, 20};

    /** A node: what it is, the path it is on (-1 for the centre), which stat or notable, its figure, where it is drawn, and those next to it. */
    static final class Node {
        final int id, type, path, stat, figure, notable;
        final float x, y;
        final List<Integer> next = new ArrayList<>();

        Node(int id, int type, int path, int stat, int figure, int notable, float x, float y) {
            this.id = id;
            this.type = type;
            this.path = path;
            this.stat = stat;
            this.figure = figure;
            this.notable = notable;
            this.x = x;
            this.y = y;
        }
    }

    private static List<Node> tree;

    /** The whole tree: built once. */
    static synchronized List<Node> tree() {
        if (tree != null) return tree;
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0, CENTRE, -1, -1, 0, -1, 0, 0));
        int[] ends = new int[PATHS];
        for (int path = 0; path < PATHS; path++) {
            double angle = path * Math.PI * 2 / PATHS;
            int previous = 0, notables = 0;
            for (int step = 1; step <= PATH_LENGTH; step++) {
                // Each path winds a little as it goes out, a root of a constellation; and splits into two near its end.
                double turn = angle + Math.sin(step * .35 + path) * .06;
                float radius = 2 + step * 1.15f, x = (float) (Math.cos(turn) * radius), y = (float) (Math.sin(turn) * radius);
                int stat = (step + path) % STATS, type, figure, notable = -1;
                if (step % 4 == 0 && notables < 11) {
                    type = NOTABLE;
                    notable = notables++;
                    figure = 0;
                } else if (step % 7 == 3) {
                    type = ROOT;
                    figure = ROOT_FIGURES[Math.min(ROOT_FIGURES.length - 1, step / 12)];
                } else {
                    type = SMALL;
                    figure = 1;
                }
                Node node = new Node(nodes.size(), type, path, stat, figure, notable, x, y);
                nodes.add(node);
                link(nodes, previous, node.id);
                previous = node.id;
                // A side branch of small nodes and a root at every eleventh step.
                if (step % 11 == 5) {
                    int from = node.id;
                    double sideTurn = angle + (step % 2 == 0 ? .28 : -.28);
                    for (int leaf = 1; leaf <= 4; leaf++) {
                        float lr = radius + leaf * .9f;
                        boolean last = leaf == 4;
                        Node side = new Node(nodes.size(), last ? ROOT : SMALL, path, (stat + leaf) % STATS, last ? ROOT_FIGURES[Math.min(3, step / 12)] : 1, -1,
                                (float) (Math.cos(sideTurn) * lr), (float) (Math.sin(sideTurn) * lr));
                        nodes.add(side);
                        link(nodes, from, side.id);
                        from = side.id;
                    }
                }
            }
            // The last notable, at the very end: its keystone.
            Node keystone = new Node(nodes.size(), NOTABLE, path, -1, 0, 11, (float) (Math.cos(angle) * (4 + PATH_LENGTH * 1.15f)),
                    (float) (Math.sin(angle) * (4 + PATH_LENGTH * 1.15f)));
            nodes.add(keystone);
            link(nodes, previous, keystone.id);
            ends[path] = previous;
        }
        // Near their ends, paths cross over to the next one round: builds can mix. (And the first twenty-four's last still crosses to the first, as it
        // always did, so that nothing taken before is cut off.)
        for (int path = 0; path < PATHS; path++) link(nodes, ends[path], ends[(path + 1) % PATHS]);
        link(nodes, ends[FIRST_PATHS - 1], ends[0]);
        tree = nodes;
        return tree;
    }

    private static void link(List<Node> nodes, int one, int other) {
        nodes.get(one).next.add(other);
        nodes.get(other).next.add(one);
    }

    // Taken

    /** Points: one a level after the first, and one more every tenth. */
    static int pointsFor(int level) { return Math.max(0, level - 1) + Math.max(0, level - 1) / 10; }

    /** Whether a node can be taken, with these taken already: not taken, and next to one that is (the centre always is). */
    static boolean canTake(java.util.BitSet taken, int id) {
        if (id <= 0 || taken.get(id)) return false;
        for (int other : tree().get(id).next) if (other == 0 || taken.get(other)) return true;
        return false;
    }

    /** What respeccing costs, in pluses: by how many are taken. */
    static int respecCost(java.util.BitSet taken) { return 100 + taken.cardinality() * 25; }

    /** All that is taken, added up: flat stats, and the notables' effects (with their costs). */
    static final class Built {
        final int[] stats = new int[STATS];
        final int[] effects = new int[E_EFFECTS];
        final List<Notable> actives = new ArrayList<>();
    }

    static Built built(java.util.BitSet taken) {
        Built built = new Built();
        List<Node> nodes = tree();
        for (int id = taken.nextSetBit(1); id >= 0 && id < nodes.size(); id = taken.nextSetBit(id + 1)) {
            Node node = nodes.get(id);
            if (node.type == SMALL || node.type == ROOT) built.stats[node.stat] += node.figure;
            else if (node.type == NOTABLE) {
                Notable notable = notables(node.path).get(node.notable);
                // (An active's effect is felt only while it is called on, from a slot.)
                if (notable.kind == ACTIVE) {
                    built.actives.add(notable);
                    continue;
                }
                built.effects[notable.effect] += notable.figure;
                if (notable.cost >= 0) built.effects[notable.cost] += notable.costFigure;
            }
        }
        return built;
    }

    /** The notable a node is, or null. */
    static Notable notableAt(int id) {
        List<Node> nodes = tree();
        if (id <= 0 || id >= nodes.size()) return null;
        Node node = nodes.get(id);
        return node.type == NOTABLE ? notables(node.path).get(node.notable) : null;
    }

    /** Whether a node is the keystone at the end of its path. */
    static boolean keystone(Node node) { return node.type == NOTABLE && node.notable == 11; }

    /** A node in a few words: what it is, then what it gives. */
    static String title(Node node) {
        if (node.type == CENTRE) return "Her seed";
        if (node.type == NOTABLE) return notables(node.path).get(node.notable).name;
        return "+" + node.figure + " " + STAT_NAMES[node.stat];
    }

    static String kindWords(Node node) {
        if (node.type == CENTRE) return "Where every path begins";
        String path = PATH_NAMES[node.path] + " · " + CLASSES[classOf(node.path)];
        if (node.type == SMALL) return "Small node · " + path;
        if (node.type == ROOT) return "Root · " + path;
        Notable notable = notables(node.path).get(node.notable);
        return (keystone(node) ? "Keystone, " : "") + (notable.kind == ACTIVE ? "active" : "passive") + " · " + path;
    }

    /** What a node does, said plainly. */
    static String does(Node node) {
        if (node.type == CENTRE) return "Every path begins here. Take a node next to it to start one.";
        if (node.type == NOTABLE) {
            Notable notable = notables(node.path).get(node.notable);
            String said = notable.said();
            return notable.kind == ACTIVE ? said + ", for " + (int) ACTIVE_LASTS + " seconds when called on from a slot (" + (int) ACTIVE_COST + " energy, then "
                    + (int) ACTIVE_REST + " seconds to rest)" : said + ", always";
        }
        return statDoes(node.stat, node.figure);
    }

    /** What so much of a stat does. */
    static String statDoes(int stat, int figure) {
        switch (stat) {
            case HP: return "+" + figure + " HP at most (a heart is " + HEART + ")";
            case DEF: return "Each hurt costs " + figure + " HP less (never under a tenth of it)";
            case INT: return "+" + figure + "% energy back, and +" + figure + "% blows with books, the tablet, staffs and wands";
            default: return "+" + figure + "% to every blow";
        }
    }

    /** How long an active lasts, what calling on it costs, and how long it rests after: the rules' own, kept here for the menu. */
    static final float ACTIVE_LASTS = 8, ACTIVE_REST = 20, ACTIVE_COST = 30;

    /** Every taken node still joined to the centre through taken nodes; any other let go (a save from before a change to the tree). */
    static java.util.BitSet whole(java.util.BitSet taken) {
        List<Node> nodes = tree();
        java.util.BitSet joined = new java.util.BitSet();
        java.util.ArrayDeque<Integer> next = new java.util.ArrayDeque<>();
        next.add(0);
        while (!next.isEmpty()) {
            int id = next.poll();
            for (int other : nodes.get(id).next) {
                if (other > 0 && other < nodes.size() && taken.get(other) && !joined.get(other)) {
                    joined.set(other);
                    next.add(other);
                }
            }
        }
        return joined;
    }

    /** Points spent. */
    static int spent(java.util.BitSet taken) { return taken.cardinality(); }

    /** The three slots for actives, as the nodes in them (0 for none). */
    static final int SLOTS = 3;

    static int[] slots(String note) {
        int[] slots = new int[SLOTS];
        if (note == null || note.isEmpty()) return slots;
        String[] parts = note.split(",");
        for (int slot = 0; slot < SLOTS && slot < parts.length; slot++) {
            try {
                slots[slot] = Integer.parseInt(parts[slot].trim());
            } catch (NumberFormatException ignored) {
                slots[slot] = 0;
            }
        }
        return slots;
    }

    static String slotsNote(int[] slots) {
        StringBuilder note = new StringBuilder();
        for (int slot = 0; slot < SLOTS; slot++) note.append(slot == 0 ? "" : ",").append(slot < slots.length ? slots[slot] : 0);
        return note.toString();
    }

    /** The actives in the slots that are taken and are actives: in slot order, a slot empty or let go left out of its place as null. */
    static List<Notable> slotted(java.util.BitSet taken, int[] slots) {
        List<Notable> list = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            int id = slot < slots.length ? slots[slot] : 0;
            Notable notable = taken.get(id) ? notableAt(id) : null;
            list.add(notable != null && notable.kind == ACTIVE ? notable : null);
        }
        return list;
    }

    /** An active newly taken goes into the first empty slot, if there is one. The slots as they now are. */
    static int[] slotIn(int[] slots, java.util.BitSet taken, int id) {
        int[] now = java.util.Arrays.copyOf(slots, SLOTS);
        Notable notable = notableAt(id);
        if (notable == null || notable.kind != ACTIVE) return now;
        List<Notable> had = slotted(taken, now);
        for (int slot = 0; slot < SLOTS; slot++) if (now[slot] == id) return now;
        for (int slot = 0; slot < SLOTS; slot++) {
            if (had.get(slot) == null) {
                now[slot] = id;
                return now;
            }
        }
        return now;
    }

    /** What her HP comes to at most: hearts of 20, the tree's HP, and its HP percent. */
    static int mostHp(int hearts, Built built) {
        int base = hearts * HEART + built.stats[HP];
        return Math.max(HEART / 2, base + base * built.effects[E_HP_PCT] / 100);
    }

    /** Her defence's percent, kept between a half more hurt and three quarters less. */
    static int defencePercent(int percent) { return Math.max(-50, Math.min(75, percent)); }

    /** Intelligence, with the tree's intelligence percent. */
    static int intelligence(Built built, int more) { return built.stats[INT] + (built.stats[INT] * (built.effects[E_INT_PCT] + more)) / 100; }

    static String note(java.util.BitSet taken) {
        return java.util.Base64.getEncoder().encodeToString(taken.toByteArray());
    }

    static java.util.BitSet taken(String note) {
        if (note == null || note.isEmpty()) return new java.util.BitSet();
        try {
            return java.util.BitSet.valueOf(java.util.Base64.getDecoder().decode(note));
        } catch (IllegalArgumentException ignored) {
            return new java.util.BitSet();
        }
    }

    // Hearts

    /** HP in a heart; and the colours a row of hearts goes through as there are more than twenty: gold, purple, orange, blue, green. */
    static final int HEART = 20, ROW = 20;
    static final int[] LAYER_COLOURS = {0xFFE0362F, 0xFFFFC433, 0xFFB463FF, 0xFFFF8A2A, 0xFF4C9BFF, 0xFF4FC06A};

    /** For heart number i of a row of twenty (from 0), with so much HP of so much: its colour layer and how full it is (0 to 1). */
    static float[] heart(int index, int hp, int most) {
        int hearts = Math.max(1, (most + HEART - 1) / HEART), layers = (hearts + ROW - 1) / ROW;
        float full = hp / (float) HEART;
        // The top layer showing at this place, and how much of this heart is filled in it.
        for (int layer = layers - 1; layer >= 0; layer--) {
            int number = layer * ROW + index;
            if (number >= hearts) continue;
            float fill = Math.max(0, Math.min(1, full - number));
            if (fill > 0 || layer == 0) return new float[]{layer, fill, 1};
        }
        return new float[]{0, 0, index < hearts ? 1 : 0};
    }
}
