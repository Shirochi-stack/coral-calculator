package com.coral.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Her skill tree: a web, not a wheel. Round its edge are thirteen regions, one for each class of weapon, each with its own start; from
 * each start routes wind out to clusters (a ring of small nodes round a notable, entered from one side), to dead-end spurs that end in a
 * deep notable or a keystone, and in to a shared core where every route meets. Between neighbouring regions, hybrid routes cross over.
 * Small nodes give a stat, or in a region a touch of its class's own; roots give more of a stat; notables do something of their own:
 * passives, always on; actives, called on from a slot; keystones that change a build and cost something for it. The tree is the same for
 * everyone: made from numbers, not chance. Node numbers are kept from here on: anything new is added after what there is.
 */
final class RunSkills {
    private RunSkills() {}

    // The stats

    static final int HP = 0, DEF = 1, INT = 2, ATK = 3, STATS = 4;
    static final String[] STAT_NAMES = {"HP", "Defence", "Intelligence", "Attack"};

    // The classes and the regions

    /** The thirteen classes of weapon, each a region of the tree. */
    static final String[] CLASSES = {"Swords", "Katana", "Greatswords", "Books", "Summoning staffs", "Knives", "Pens and pencils", "Maces", "Hammers", "Wands",
            "Ink guns", "Scythes", "Knuckles"};
    static final int CLASS_COUNT = 13;
    /** What a class's own effects are felt with, in words: the weapons in its region's keeping. */
    static final String[] CLASS_WITH = {"swords and the brush", "the katana", "greatswords", "books and the tablet-book", "summoning staffs", "knives and API keys",
            "pens", "maces", "war hammers", "wands", "ink guns and the cannon", "scythes, lances and whips", "knuckles"};
    /** The regions: the thirteen classes'; the core, where every route meets; and the thirteen hybrid borders, each between a class and the next. */
    static final int CORE = 13, HYBRID = 14, REGIONS = 27, PATHS = REGIONS;
    static final String[] PATH_NAMES = new String[REGIONS];
    static final int[] PATH_COLOURS = new int[REGIONS];
    /** Each class's colour (those of its old paths). */
    private static final int[] CLASS_COLOURS = {0xFF6FA8FF, 0xFFE0475A, 0xFFB08A50, 0xFF2FB5A8, 0xFF9A6AE8, 0xFF5FBF6A, 0xFFE89A3F, 0xFF8A8F98, 0xFFFFC433,
            0xFF4FC0E8, 0xFF3A50C8, 0xFF9A5ACA, 0xFFE0A030};

    static {
        for (int clazz = 0; clazz < CLASS_COUNT; clazz++) {
            PATH_NAMES[clazz] = CLASSES[clazz];
            PATH_COLOURS[clazz] = CLASS_COLOURS[clazz];
            int next = (clazz + 1) % CLASS_COUNT;
            PATH_NAMES[HYBRID + clazz] = CLASSES[clazz] + " and " + CLASSES[next];
            PATH_COLOURS[HYBRID + clazz] = blend(CLASS_COLOURS[clazz], CLASS_COLOURS[next]);
        }
        PATH_NAMES[CORE] = "The Inkwell";
        PATH_COLOURS[CORE] = 0xFFFFE9A8;
    }

    private static int blend(int one, int other) {
        int r = ((one >> 16 & 255) + (other >> 16 & 255)) / 2, g = ((one >> 8 & 255) + (other >> 8 & 255)) / 2, b = ((one & 255) + (other & 255)) / 2;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /** The weapon a class is (in {@link RunArms}). */
    static int weaponOf(int clazz) { return RunArms.OF_KIND[clazz]; }

    /** The class whose region a weapon is in: the thirteen found by their own, and the first six by their kin; or -1 for empty hands. */
    static int classOfWeapon(int weapon) {
        switch (weapon) {
            case RunArms.SWORD: case RunArms.BRUSH: return 0;
            case RunArms.KATANA: return 1;
            case RunArms.GREATSWORD: return 2;
            case RunArms.BOOK: case RunArms.TABLET: return 3;
            case RunArms.STAFF: return 4;
            case RunArms.KNIFE: case RunArms.KEYS: return 5;
            case RunArms.PEN: return 6;
            case RunArms.MACE: return 7;
            case RunArms.HAMMER: return 8;
            case RunArms.WAND: return 9;
            case RunArms.GUN: case RunArms.CANNON: return 10;
            case RunArms.SCYTHE: case RunArms.LANCE: case RunArms.WHIP: return 11;
            case RunArms.KNUCKLES: return 12;
            default: return -1;
        }
    }

    /** The picture of a class: the emblem of its first path in the sheet. */
    static int emblemOf(int clazz) { return clazz < 12 ? clazz * 2 : 24; }

    // What the tree does

    /** Passive: always on. Active: called on from a slot. A keystone is a passive that changes a build, with a cost. */
    static final int PASSIVE = 0, ACTIVE = 1, KEYSTONE = 2;

    static final int E_ATK_PCT = 0, E_DEF_PCT = 1, E_HP_PCT = 2, E_INT_PCT = 3, E_LEECH = 4, E_REGEN = 5, E_CRIT = 6, E_CRITX = 7, E_HASTE = 8, E_AREA = 9,
            E_PROJECTILES = 10, E_ENERGY = 11, E_DP = 12, E_SHIELD = 13, E_POISON = 14, E_BURN = 15, E_FREEZE = 16, E_REACH = 17, E_SPECIAL_COST = 18,
            E_COMBO = 19, E_DODGE = 20, E_THORNS = 21, E_PIERCE = 22, E_STUN = 23, E_SHRUG = 24, E_AIR = 25, E_PLUSES = 26, E_MAGNET = 27, E_SPARK = 28,
            E_SPECIAL = 29, E_WARD_HIT = 30, E_CHAIN = 31, E_ECHO = 32, E_REFLECT = 33, E_QUAKE = 34, E_POOL = 35, E_EXECUTE = 36, E_AMBUSH = 37,
            E_SHIELD_BREAK = 38, E_KILL_HEAL = 39, E_KILL_ENERGY = 40, E_SPEED = 41, E_SWIM = 42, E_CLIMB = 43, E_FORMS = 44, E_METER = 45,
            E_SECOND_WIND = 46, E_BLOOD = 47, E_IRON = 48, E_PET = 49, E_STOMP = 50, E_RICOCHET = 51, E_CHARGE = 52, E_LAST_STAND = 53, E_FRENZY = 54,
            E_POUND_AREA = 55, E_POUND_HIT = 56, E_GLIDE = 57, E_AERIAL_TIME = 58, E_AERIAL_HIT = 59, E_SKYBORNE = 60,
            E_EFFECTS = 61;

    /**
     * Whether an effect is felt only with the weapons of the class that gives it (a class's own way of fighting), or always, whatever is in
     * her hands (what she is: her HP, her shield, how she moves, her pets, her forms).
     */
    static boolean bound(int effect) {
        switch (effect) {
            case E_HP_PCT: case E_SHIELD: case E_REGEN: case E_SECOND_WIND: case E_BLOOD: case E_IRON: case E_AIR: case E_SWIM: case E_CLIMB: case E_FORMS:
            case E_MAGNET: case E_PLUSES: case E_SHRUG: case E_DP: case E_ENERGY: case E_WARD_HIT: case E_GLIDE: case E_AERIAL_TIME: case E_SKYBORNE:
                return false;
            default:
                return true;
        }
    }

    /** What so much of an effect does, in words: a cost, under nought, says what is given up. */
    static String phrase(int effect, int figure) {
        int n = Math.abs(figure);
        boolean less = figure < 0;
        switch (effect) {
            case E_ATK_PCT: return (less ? "−" : "+") + n + "% attack";
            case E_DEF_PCT: return less ? "−" + n + "% defence (hurts cost " + n + "% more)" : "+" + n + "% defence (hurts cost " + n + "% less)";
            case E_HP_PCT: return (less ? "−" : "+") + n + "% HP";
            case E_INT_PCT: return (less ? "−" : "+") + n + "% intelligence";
            case E_LEECH: return n + "% chance a blow gives back half a heart";
            case E_REGEN: return n + " HP back every second";
            case E_CRIT: return "+" + n + "% chance of a great blow";
            case E_CRITX: return "great blows " + n + "% harder";
            case E_HASTE: return n + "% " + (less ? "slower" : "faster") + " attacks";
            case E_AREA: return n + "% " + (less ? "narrower" : "wider") + " swings and bursts";
            case E_PROJECTILES: return "every throw or shot goes out " + (n == 1 ? "twice" : (n + 1) + " times") + ", fanned";
            case E_ENERGY: return n + "% " + (less ? "slower" : "faster") + " energy";
            case E_DP: return n + "% of energy spent becomes divine power";
            case E_SHIELD: return "+" + n + " shield piece" + (n == 1 ? "" : "s");
            case E_POISON: return n + "% chance a blow ink-poisons";
            case E_BURN: return n + "% chance a blow burns";
            case E_FREEZE: return n + "% chance a blow freezes";
            case E_REACH: return n + "% " + (less ? "shorter" : "further") + " reach";
            case E_SPECIAL_COST: return "specials cost " + n + "% less energy";
            case E_COMBO: return (n * 5) + "% faster attacks";
            case E_DODGE: return n + "% chance to slip a hurt";
            case E_THORNS: return n + " blow" + (n == 1 ? "" : "s") + " of thorns on whatever hurts her";
            case E_PIERCE: return "her shots go through " + n + " more creature" + (n == 1 ? "" : "s");
            case E_STUN: return n + "% chance a blow knocks a creature off its feet for a second";
            case E_SHRUG: return n + "% chance to shrug off a hurt";
            case E_AIR: return n + " more jump" + (n == 1 ? "" : "s") + " in the air";
            case E_PLUSES: return n + "% more pluses from what she beats";
            case E_MAGNET: return "pluses fly to her from " + n + " more square" + (n == 1 ? "" : "s") + " away";
            case E_SPARK: return n + " more energy from every blow";
            case E_SPECIAL: return "specials ready " + n + "% sooner";
            case E_WARD_HIT: return n + "% chance a blow gives back a shield piece";
            case E_CHAIN: return "her shots jump on to " + n + " more creature" + (n == 1 ? "" : "s");
            case E_ECHO: return n + "% chance a swing strikes again a beat later";
            case E_REFLECT: return n >= 100 ? "her swings send what is thrown at her back" : n + "% chance a swing sends what is thrown at her back";
            case E_QUAKE: return n >= 100 ? "every blow sends out a shockwave" : n + "% chance a blow sends out a shockwave";
            case E_POOL: return n >= 100 ? "every blow leaves a pool of ink that slows creatures" : n + "% chance a blow leaves a pool of ink that slows creatures";
            case E_EXECUTE: return "blows " + n + "% harder on creatures under a third of their health";
            case E_AMBUSH: return "blows " + n + "% harder on creatures not yet hurt";
            case E_SHIELD_BREAK: return "blows " + n + "% stronger against shields" + (n >= 100 ? ", taking two pieces at a time" : "");
            case E_KILL_HEAL: return n + " HP back for every creature she beats";
            case E_KILL_ENERGY: return n + " energy back for every creature she beats";
            case E_SPEED: return n + "% " + (less ? "slower" : "faster") + " running";
            case E_SWIM: return n + "% faster swimming, and breath held " + n + "% longer";
            case E_CLIMB: return n + "% faster climbing";
            case E_FORMS: return "forms and ribbons last " + n + "% longer";
            case E_METER: return "a combo meter (each blow within two seconds of the last adds " + n + "% attack, up to ten)";
            case E_SECOND_WIND: return "once a land, a hurt that would end her leaves her " + n + " HP";
            case E_BLOOD: return "actives and specials cost HP instead of energy (one HP for every two energy, never her last " + BLOOD_KEEPS + ")";
            case E_IRON: return "her chance to slip a hurt counts as that much defence instead";
            case E_PET: return "pets act " + n + "% sooner";
            case E_STOMP: return "landing on a creature counts as " + n + " more blow" + (n == 1 ? "" : "s");
            case E_RICOCHET: return "her shots bounce off walls " + (n == 1 ? "once" : n + " times");
            case E_CHARGE: return "a blow after a second and a half at rest is " + n + "% harder";
            case E_LAST_STAND: return "+" + n + "% attack while at a third of her HP or less";
            case E_FRENZY: return n + "% faster attacks for three seconds after she beats a creature";
            case E_POUND_AREA: return "her ground pound reaches " + n + "% wider";
            case E_POUND_HIT: return "her ground pound strikes " + n + "% harder";
            case E_GLIDE: return "she glides " + n + "% slower, and so further";
            case E_AERIAL_TIME: return "her aerial strike holds her up " + n + "% longer";
            case E_AERIAL_HIT: return "her aerial strike hits " + n + "% harder";
            case E_SKYBORNE: return "she cannot attack on the ground";
            default: return "";
        }
    }

    /** HP that blood ink never takes. */
    static final int BLOOD_KEEPS = 10;

    /** The classes in a set of them, in words: "swords and the brush", or "the katana, or greatswords". */
    static String withWords(int classes) {
        List<String> names = new ArrayList<>();
        for (int clazz = 0; clazz < CLASS_COUNT; clazz++) if ((classes >> clazz & 1) != 0) names.add(CLASS_WITH[clazz]);
        if (names.isEmpty()) return "anything";
        if (names.size() == 1) return names.get(0);
        return String.join(", ", names.subList(0, names.size() - 1)) + ", or " + names.get(names.size() - 1);
    }

    /** A notable skill: its name, its kind, the classes it is for (0 for any), its effects and their figures (a cost under nought), its place in the list, and its picture. */
    static final class Notable {
        final String name;
        final int kind, classes, index, icon;
        final boolean keystone;
        final int[] effects, figures;

        Notable(String name, int kind, int classes, int index, int[] pairs) {
            this.name = name;
            this.kind = kind == ACTIVE ? ACTIVE : PASSIVE;
            this.keystone = kind == KEYSTONE;
            this.classes = classes;
            this.index = index;
            this.icon = NOTABLE_ICONS + index;
            effects = new int[pairs.length / 2];
            figures = new int[pairs.length / 2];
            for (int at = 0; at < effects.length; at++) {
                effects[at] = pairs[at * 2];
                figures[at] = pairs[at * 2 + 1];
            }
        }

        /** Whether it costs anything. */
        boolean costly() {
            for (int figure : figures) if (figure < 0) return true;
            return false;
        }

        /** How much of an effect it gives (or, under nought, takes). */
        int of(int effect) {
            int sum = 0;
            for (int at = 0; at < effects.length; at++) if (effects[at] == effect) sum += figures[at];
            return sum;
        }

        /** What it does, said plainly: with which weapons, what always, and what it costs. */
        String said() {
            List<String> withThem = new ArrayList<>(), always = new ArrayList<>(), costs = new ArrayList<>();
            for (int at = 0; at < effects.length; at++) {
                String words = phrase(effects[at], figures[at]);
                if (figures[at] < 0) costs.add(words);
                else if (classes != 0 && bound(effects[at])) withThem.add(words);
                else always.add(words);
            }
            StringBuilder said = new StringBuilder();
            if (!withThem.isEmpty()) said.append("With ").append(withWords(classes)).append(": ").append(joined(withThem));
            if (!always.isEmpty()) said.append(said.length() > 0 ? ". " : "").append(capital(joined(always)));
            if (!costs.isEmpty()) said.append(said.length() > 0 ? "; but " : "But ").append(joined(costs));
            return said.toString();
        }
    }

    private static String joined(List<String> parts) {
        if (parts.size() == 1) return parts.get(0);
        return String.join(", ", parts.subList(0, parts.size() - 1)) + " and " + parts.get(parts.size() - 1);
    }

    private static String capital(String words) { return words.isEmpty() ? words : Character.toUpperCase(words.charAt(0)) + words.substring(1); }

    /** Where the notables' own pictures begin in the sheet: after the old emblems and node pictures. */
    static final int EMBLEMS = 26, NODE_ICONS = 26, NOTABLE_ICONS = 38;
    /** The seed's picture: on the button that opens the tree. */
    static final int SEED_ICON = NODE_ICONS + 11;

    /** Every notable, in a fixed order: each class's seven, the thirteen hybrids, then the core's passives, actives and keystones. */
    static final List<Notable> NOTABLES;
    /** Where each kind begins in it, and how many of each a class has. */
    static final int PER_CLASS = 7, HYBRIDS = CLASS_COUNT * PER_CLASS, CORE_PASSIVES = HYBRIDS + CLASS_COUNT, CORE_PASSIVE_COUNT = 30,
            CORE_ACTIVES = CORE_PASSIVES + CORE_PASSIVE_COUNT, CORE_ACTIVE_COUNT = 8, CORE_KEYSTONES = CORE_ACTIVES + CORE_ACTIVE_COUNT, CORE_KEYSTONE_COUNT = 6;
    /** The moves of the air, added after: the ring round her seed (four passives and Skyborne), and the classes' own, with their classes. */
    static final int SKY = CORE_KEYSTONES + CORE_KEYSTONE_COUNT, SKY_COUNT = 5, SKY_CLASSES = SKY + SKY_COUNT;
    static final int[] SKY_CLASS = {8, 12, 9, 2, 1};
    /** A class's seven, by their places: the one by the start, the two clusters' outward, the two actives' clusters, the deep one and the keystone. */
    static final int EARLY = 0, OUTER_LEFT = 1, OUTER_RIGHT = 2, ACTIVE_LEFT = 3, ACTIVE_RIGHT = 4, DEEP = 5, CLASS_KEYSTONE = 6;

    private static final List<Notable> MAKING = new ArrayList<>();

    private static Notable n(String name, int kind, int classes, int... pairs) {
        Notable made = new Notable(name, kind, classes, MAKING.size(), pairs);
        MAKING.add(made);
        return made;
    }

    static {
        Notable[] table = {
        // (made by the table's own script)
            n("Bladedancer", PASSIVE, 1 << 0, E_SPEED, 8, E_HASTE, 12),
            n("Twin Edge", PASSIVE, 1 << 0, E_ECHO, 25),
            n("Keen Edge", PASSIVE, 1 << 0, E_CRIT, 10, E_CRITX, 30),
            n("Thousand Cuts", ACTIVE, 0, E_HASTE, 40, E_ECHO, 40),
            n("Parry Stance", ACTIVE, 0, E_DEF_PCT, 30, E_REFLECT, 100, E_THORNS, 2),
            n("Sword Saint", PASSIVE, 1 << 0, E_ATK_PCT, 30, E_ECHO, 20, E_REFLECT, 50),
            n("Duelist's Vow", KEYSTONE, 1 << 0, E_ATK_PCT, 70, E_ECHO, 30, E_AREA, -40),
            n("Wind Step", PASSIVE, 1 << 1, E_SPEED, 10, E_DODGE, 8),
            n("Iaido", PASSIVE, 1 << 1, E_CHARGE, 40),
            n("Clean Cut", PASSIVE, 1 << 1, E_EXECUTE, 35),
            n("Flash Draw", ACTIVE, 0, E_HASTE, 35, E_CRIT, 25),
            n("Moonlit Severance", ACTIVE, 0, E_EXECUTE, 60, E_REACH, 30),
            n("Ronin's Resolve", PASSIVE, 1 << 1, E_CRITX, 60, E_CHARGE, 40),
            n("One Cut", KEYSTONE, 1 << 1, E_EXECUTE, 100, E_CHARGE, 80, E_HASTE, -30),
            n("Unstoppable", PASSIVE, 1 << 2, E_HP_PCT, 10, E_SHRUG, 8),
            n("Cleave", PASSIVE, 1 << 2, E_AREA, 30),
            n("Colossal Swing", PASSIVE, 1 << 2, E_ATK_PCT, 15, E_SHIELD_BREAK, 40),
            n("Earthsplitter", ACTIVE, 0, E_QUAKE, 60, E_AREA, 40),
            n("Titan's Grip", ACTIVE, 0, E_ATK_PCT, 40, E_SHIELD_BREAK, 100),
            n("Giant's Blood", PASSIVE, 1 << 2, E_HP_PCT, 20, E_AREA, 30, E_STUN, 10),
            n("Juggernaut", KEYSTONE, 1 << 2, E_ATK_PCT, 60, E_SHRUG, 20, E_SHIELD_BREAK, 60, E_SPEED, -20),
            n("Margin Notes", PASSIVE, 1 << 3, E_INT_PCT, 20, E_ENERGY, 10),
            n("Footnotes", PASSIVE, 1 << 3, E_AREA, 25),
            n("Bookworm", PASSIVE, 1 << 3, E_PROJECTILES, 1),
            n("Library Storm", ACTIVE, 0, E_PROJECTILES, 2, E_AREA, 30),
            n("Recitation", ACTIVE, 0, E_INT_PCT, 50, E_ENERGY, 40),
            n("Living Library", PASSIVE, 1 << 3, E_AREA, 40, E_BURN, 15),
            n("Forbidden Tome", KEYSTONE, 1 << 3, E_INT_PCT, 60, E_AREA, 50, E_HP_PCT, -25),
            n("Ward Spirits", PASSIVE, 1 << 4, E_SHIELD, 1, E_WARD_HIT, 8),
            n("Spirit Bond", PASSIVE, 1 << 4, E_PET, 25, E_PIERCE, 1),
            n("Gathering Spirits", PASSIVE, 1 << 4, E_PROJECTILES, 1),
            n("Spirit Host", ACTIVE, 0, E_PROJECTILES, 3),
            n("Ancestral Call", ACTIVE, 0, E_PET, 50, E_ATK_PCT, 30),
            n("Spiritbinder", PASSIVE, 1 << 4, E_CHAIN, 1, E_PIERCE, 2),
            n("Legion", KEYSTONE, 1 << 4, E_PROJECTILES, 3, E_PET, 30, E_ATK_PCT, -25),
            n("Fan of Knives", PASSIVE, 1 << 5, E_PROJECTILES, 1),
            n("Ambush", PASSIVE, 1 << 5, E_AMBUSH, 50),
            n("Venom Edge", PASSIVE, 1 << 5, E_POISON, 15),
            n("Shadowstep", ACTIVE, 0, E_DODGE, 30, E_SPEED, 25, E_AMBUSH, 50),
            n("Knife Storm", ACTIVE, 0, E_PROJECTILES, 3, E_PIERCE, 1),
            n("Venomfang", PASSIVE, 1 << 5, E_POISON, 30, E_CHAIN, 1),
            n("Death Mark", KEYSTONE, 1 << 5, E_AMBUSH, 150, E_CRIT, 20, E_HP_PCT, -20),
            n("Flowing Script", PASSIVE, 1 << 6, E_HASTE, 12, E_ENERGY, 10),
            n("Ink Pools", PASSIVE, 1 << 6, E_POOL, 25),
            n("Calligraphy", PASSIVE, 1 << 6, E_PIERCE, 1, E_REACH, 15),
            n("Flood of Ink", ACTIVE, 0, E_POOL, 100, E_PROJECTILES, 2),
            n("Signature", ACTIVE, 0, E_CRIT, 30, E_CRITX, 50),
            n("Ink Sea", PASSIVE, 1 << 6, E_POOL, 40, E_POISON, 15),
            n("Mightier Than the Sword", KEYSTONE, 1 << 6, E_ATK_PCT, 50, E_PIERCE, 3, E_DEF_PCT, -20),
            n("Iron Will", PASSIVE, 1 << 7, E_DEF_PCT, 10, E_SHRUG, 6),
            n("Concussion", PASSIVE, 1 << 7, E_STUN, 15),
            n("Crushing Blow", PASSIVE, 1 << 7, E_SHIELD_BREAK, 50, E_ATK_PCT, 8),
            n("Warden's Rite", ACTIVE, 0, E_STUN, 40, E_DEF_PCT, 25),
            n("Ringing Smash", ACTIVE, 0, E_QUAKE, 50, E_STUN, 30),
            n("Warden", PASSIVE, 1 << 7, E_SHIELD, 2, E_THORNS, 2, E_STUN, 10),
            n("Bulwark", KEYSTONE, 1 << 7, E_DEF_PCT, 40, E_SHIELD, 2, E_STUN, 20, E_HASTE, -25),
            n("Thunderous", PASSIVE, 1 << 8, E_STUN, 12, E_AREA, 10),
            n("Aftershock", PASSIVE, 1 << 8, E_QUAKE, 20),
            n("Anvil Strike", PASSIVE, 1 << 8, E_SHIELD_BREAK, 60),
            n("Thunderforge", ACTIVE, 0, E_QUAKE, 80, E_ATK_PCT, 25),
            n("Quakebringer", ACTIVE, 0, E_AREA, 50, E_STUN, 40),
            n("Mountain's Heart", PASSIVE, 1 << 8, E_QUAKE, 30, E_SHIELD_BREAK, 60, E_HP_PCT, 10),
            n("Worldbreaker", KEYSTONE, 1 << 8, E_QUAKE, 100, E_SHIELD_BREAK, 100, E_HASTE, -35),
            n("Piercing Bolt", PASSIVE, 1 << 9, E_PIERCE, 2),
            n("Split Bolt", PASSIVE, 1 << 9, E_PROJECTILES, 1),
            n("Arc", PASSIVE, 1 << 9, E_CHAIN, 1),
            n("Elemental Flux", ACTIVE, 0, E_BURN, 30, E_FREEZE, 30, E_POISON, 30),
            n("Overload", ACTIVE, 0, E_CHAIN, 3, E_INT_PCT, 30),
            n("Conduit", PASSIVE, 1 << 9, E_CHAIN, 2, E_INT_PCT, 25),
            n("Scatter Script", KEYSTONE, 1 << 9, E_PROJECTILES, 3, E_CHAIN, 1, E_ATK_PCT, -30),
            n("Quick Reload", PASSIVE, 1 << 10, E_HASTE, 15),
            n("Ricochet", PASSIVE, 1 << 10, E_RICOCHET, 2),
            n("Charged Shot", PASSIVE, 1 << 10, E_CHARGE, 50),
            n("Bullet Hell", ACTIVE, 0, E_HASTE, 50, E_PROJECTILES, 1),
            n("Deadeye", ACTIVE, 0, E_CRIT, 40, E_PIERCE, 2),
            n("Gunslinger", PASSIVE, 1 << 10, E_RICOCHET, 2, E_CHAIN, 1, E_CRITX, 30),
            n("Artillery", KEYSTONE, 1 << 10, E_CHARGE, 150, E_AREA, 50, E_HASTE, -40),
            n("Harvest", PASSIVE, 1 << 11, E_LEECH, 8),
            n("Wide Arc", PASSIVE, 1 << 11, E_AREA, 25, E_REACH, 10),
            n("Reaper's Due", PASSIVE, 1 << 11, E_KILL_HEAL, 3),
            n("Harvest Moon", ACTIVE, 0, E_LEECH, 30, E_AREA, 40),
            n("Soul Reap", ACTIVE, 0, E_EXECUTE, 60, E_KILL_HEAL, 6),
            n("Grim Harvest", PASSIVE, 1 << 11, E_LEECH, 12, E_KILL_HEAL, 4, E_AREA, 20),
            n("Death's Embrace", KEYSTONE, 1 << 11, E_LEECH, 30, E_KILL_HEAL, 8, E_DEF_PCT, -30),
            n("Flurry", PASSIVE, 1 << 12, E_HASTE, 12, E_METER, 2),
            n("Launcher", PASSIVE, 1 << 12, E_STUN, 15),
            n("Finisher", PASSIVE, 1 << 12, E_EXECUTE, 40),
            n("Iron Fist", ACTIVE, 0, E_ATK_PCT, 50, E_SHIELD_BREAK, 80),
            n("Pressure Point", ACTIVE, 0, E_STUN, 50, E_CRIT, 30),
            n("Hundred Fists", PASSIVE, 1 << 12, E_METER, 4, E_HASTE, 15),
            n("One-Inch Punch", KEYSTONE, 1 << 12, E_EXECUTE, 100, E_STUN, 30, E_REACH, -40),
            n("Blade and Draw", PASSIVE, 1 << 0 | 1 << 1, E_ECHO, 15, E_CHARGE, 20),
            n("Heavy Draw", PASSIVE, 1 << 1 | 1 << 2, E_ATK_PCT, 15, E_AREA, 15),
            n("Weighty Tomes", PASSIVE, 1 << 2 | 1 << 3, E_SHIELD_BREAK, 40, E_AREA, 15),
            n("Grimoire of Spirits", PASSIVE, 1 << 3 | 1 << 4, E_INT_PCT, 15, E_PROJECTILES, 1),
            n("Spirit Blades", PASSIVE, 1 << 4 | 1 << 5, E_PIERCE, 1, E_AMBUSH, 30),
            n("Poison Pen", PASSIVE, 1 << 5 | 1 << 6, E_POISON, 15, E_POOL, 15),
            n("Heavy Ink", PASSIVE, 1 << 6 | 1 << 7, E_POOL, 15, E_STUN, 10),
            n("Smith's Rhythm", PASSIVE, 1 << 7 | 1 << 8, E_QUAKE, 15, E_STUN, 10),
            n("Thunderstrike", PASSIVE, 1 << 8 | 1 << 9, E_QUAKE, 15, E_CHAIN, 1),
            n("Arcane Rounds", PASSIVE, 1 << 9 | 1 << 10, E_RICOCHET, 1, E_CHAIN, 1),
            n("Reaper's Rounds", PASSIVE, 1 << 10 | 1 << 11, E_LEECH, 6, E_CHARGE, 30),
            n("Reaping Fists", PASSIVE, 1 << 11 | 1 << 12, E_LEECH, 6, E_EXECUTE, 30),
            n("Blade and Fist", PASSIVE, 1 << 12 | 1 << 0, E_ECHO, 15, E_METER, 2),
            n("Inspired Blow", PASSIVE, 0, E_CRIT, 8, E_CRITX, 25),
            n("Vampiric Ink", PASSIVE, 0, E_LEECH, 6),
            n("Second Wind", PASSIVE, 0, E_SECOND_WIND, 20),
            n("Fleet Foot", PASSIVE, 0, E_SPEED, 12),
            n("Deep Diver", PASSIVE, 0, E_SWIM, 30),
            n("Sure Grip", PASSIVE, 0, E_CLIMB, 35),
            n("Pet Whisperer", PASSIVE, 0, E_PET, 25),
            n("Ribbon Weaver", PASSIVE, 0, E_FORMS, 30),
            n("Plus Magnet", PASSIVE, 0, E_MAGNET, 2),
            n("Treasure Sense", PASSIVE, 0, E_PLUSES, 20),
            n("Featherweight", PASSIVE, 0, E_AIR, 1),
            n("Heavy Landing", PASSIVE, 0, E_STOMP, 2),
            n("Combo Meter", PASSIVE, 0, E_METER, 2),
            n("Thorn Skin", PASSIVE, 0, E_THORNS, 2),
            n("Shieldbreaker", PASSIVE, 0, E_SHIELD_BREAK, 40),
            n("Battle Trance", PASSIVE, 0, E_LAST_STAND, 40),
            n("Feeding Frenzy", PASSIVE, 0, E_FRENZY, 30),
            n("Echoing Strikes", PASSIVE, 0, E_ECHO, 12),
            n("Chain Reaction", PASSIVE, 0, E_CHAIN, 1),
            n("Iron Hide", PASSIVE, 0, E_DEF_PCT, 12),
            n("Quickstep", PASSIVE, 0, E_DODGE, 8),
            n("Mind of Ink", PASSIVE, 0, E_INT_PCT, 15, E_ENERGY, 10),
            n("Toughness", PASSIVE, 0, E_HP_PCT, 12),
            n("Precision", PASSIVE, 0, E_REACH, 15, E_CRIT, 4),
            n("Soul Siphon", PASSIVE, 0, E_KILL_ENERGY, 6),
            n("Paper Ward", PASSIVE, 0, E_SHIELD, 1, E_WARD_HIT, 6),
            n("Ink Spark", PASSIVE, 0, E_SPARK, 2),
            n("Quick Hands", PASSIVE, 0, E_SPECIAL, 20, E_SPECIAL_COST, 10),
            n("Shrug It Off", PASSIVE, 0, E_SHRUG, 8),
            n("Healing Ink", PASSIVE, 0, E_REGEN, 1),
            n("Adrenaline", ACTIVE, 0, E_SPEED, 40, E_HASTE, 30),
            n("Sanctuary", ACTIVE, 0, E_DEF_PCT, 40, E_REGEN, 3),
            n("Berserk", ACTIVE, 0, E_ATK_PCT, 60, E_DEF_PCT, -30),
            n("Divine Spark", ACTIVE, 0, E_DP, 150, E_ENERGY, 30),
            n("Hunter's Mark", ACTIVE, 0, E_CRIT, 40, E_EXECUTE, 50),
            n("Mirror Skin", ACTIVE, 0, E_REFLECT, 100, E_THORNS, 4, E_DODGE, 20),
            n("Ink Storm", ACTIVE, 0, E_PROJECTILES, 2, E_CHAIN, 2),
            n("Golden Hour", ACTIVE, 0, E_PLUSES, 100, E_MAGNET, 6),
            n("Blood Ink", KEYSTONE, 0, E_BLOOD, 1, E_HP_PCT, 15),
            n("Glass Cannon", KEYSTONE, 0, E_ATK_PCT, 50, E_CRITX, 50, E_HP_PCT, -40),
            n("Iron Reflexes", KEYSTONE, 0, E_IRON, 1, E_DEF_PCT, 10),
            n("Unbreakable", KEYSTONE, 0, E_SHIELD, 3, E_SHRUG, 10, E_SPEED, -15),
            n("Avatar of Ink", KEYSTONE, 0, E_DP, 200, E_ENERGY, -30),
            n("Perfect Form", KEYSTONE, 0, E_FORMS, 100, E_DEF_PCT, 15, E_ATK_PCT, -15),
            // (Added after: the moves of the air, in a ring round her seed, and a few of the classes' own, at the edges of their regions.)
            n("Heavy Descent", PASSIVE, 0, E_POUND_AREA, 25, E_POUND_HIT, 25),
            n("Updraft", PASSIVE, 0, E_GLIDE, 35),
            n("Hang Time", PASSIVE, 0, E_AERIAL_TIME, 30),
            n("Sky Dancer", PASSIVE, 0, E_AERIAL_HIT, 25, E_AERIAL_TIME, 15),
            n("Skyborne", KEYSTONE, 0, E_AERIAL_HIT, 100, E_AERIAL_TIME, 30, E_SKYBORNE, -1),
            n("Quake Slam", PASSIVE, 1 << 8, E_POUND_AREA, 40, E_POUND_HIT, 20),
            n("Meteor Fist", PASSIVE, 1 << 12, E_POUND_HIT, 50, E_STUN, 10),
            n("Sky Sigil", PASSIVE, 1 << 9, E_AERIAL_HIT, 40, E_AERIAL_TIME, 10),
            n("Falling Star", PASSIVE, 1 << 2, E_POUND_HIT, 35, E_POUND_AREA, 15),
            n("Swallow Dive", PASSIVE, 1 << 1, E_AERIAL_HIT, 30, E_AERIAL_TIME, 20)
        // (end of the table)
        };
        NOTABLES = Collections.unmodifiableList(new ArrayList<>(MAKING));
    }

    // The tree

    /** What a node is: a small step, a root of a stat, a notable skill, the hidden root every start hangs from, or a class's start. */
    static final int SMALL = 0, ROOT = 1, NOTABLE = 2, CENTRE = 3, START = 4;

    /**
     * A node: what it is; its region (in {@link #path}); which stat it gives and how much, or which effect and for which classes (0 for any);
     * which notable; where it is drawn; and those next to it.
     */
    static final class Node {
        final int id, type, path, stat, figure, notable, effect, classes;
        final float x, y;
        final List<Integer> next = new ArrayList<>();

        Node(int id, int type, int path, int stat, int figure, int notable, int effect, int classes, float x, float y) {
            this.id = id;
            this.type = type;
            this.path = path;
            this.stat = stat;
            this.figure = figure;
            this.notable = notable;
            this.effect = effect;
            this.classes = classes;
            this.x = x;
            this.y = y;
        }
    }

    /** How far out the tree reaches, in its own units. */
    static final float REACH = 92;

    private static List<Node> tree;

    static synchronized List<Node> tree() {
        if (tree == null) tree = Collections.unmodifiableList(new Maker().make());
        return tree;
    }

    /** What each class's small nodes give besides attack, and each class's favoured stat for its roots. */
    private static final int[][] CLASS_SMALL = {{E_HASTE, 3}, {E_CRIT, 2}, {E_AREA, 4}, {E_INT_PCT, 4}, {E_PET, 5}, {E_POISON, 3}, {E_POOL, 3}, {E_STUN, 2},
            {E_SHIELD_BREAK, 8}, {E_INT_PCT, 4}, {E_HASTE, 3}, {E_LEECH, 1}, {E_METER, 1}};
    private static final int[] CLASS_STAT = {ATK, ATK, HP, INT, INT, ATK, INT, DEF, HP, INT, ATK, HP, ATK};
    /** A small node's figure of each stat; a root's; and a jewel's, the big roots at the ends of spurs and the hearts of regions. */
    private static final int[] SMALL_FIGURES = {3, 1, 2, 2}, ROOT_FIGURES = {10, 3, 5, 5}, JEWEL_FIGURES = {25, 5, 12, 12};
    /** A class's start: so much attack with its weapons. */
    static final int START_ATTACK = 8;

    /** Lays the tree out: everything at its place, every link made, in an order that is kept from here on. */
    private static final class Maker {
        final List<Node> nodes = new ArrayList<>();
        int stats;
        /** Per class: its clusters' rings (outer left, outer right, active left, active right), and the node of the core it comes in at. */
        final int[][][] rings = new int[CLASS_COUNT][4][];
        final int[] ports = new int[CLASS_COUNT], loops = new int[CLASS_COUNT];
        static final double TURN = Math.PI * 2 / CLASS_COUNT, HALF = TURN / 2;

        List<Node> make() {
            add(CENTRE, CORE, -1, 0, -1, -1, 0, 0, 0);
            for (int clazz = 0; clazz < CLASS_COUNT; clazz++) region(clazz);
            for (int clazz = 0; clazz < CLASS_COUNT; clazz++) crossings(clazz);
            core();
            skyward();
            return nodes;
        }

        /** A number between -1 and 1, the same every time for the same two: how each region and each part of the core is made a little its own. */
        static double wobble(int one, int two) {
            long dice = one * 7919L + two * 104729L + 12345;
            dice = dice * 6364136223846793005L + 1442695040888963407L;
            dice = dice * 6364136223846793005L + 1442695040888963407L;
            return ((dice >>> 33) % 20001) / 10000.0 - 1;
        }

        int add(int type, int region, int stat, int figure, int notable, int effect, int classes, double x, double y) {
            Node node = new Node(nodes.size(), type, region, stat, figure, notable, effect, classes, (float) x, (float) y);
            nodes.add(node);
            return node.id;
        }

        void link(int one, int other) {
            nodes.get(one).next.add(other);
            nodes.get(other).next.add(one);
        }

        /** A point of a class's region, from how far out along its middle and how far to one side (to the next class's, above nought). */
        static double[] at(int clazz, double out, double side) {
            double angle = clazz * TURN - Math.PI / 2, c = Math.cos(angle), s = Math.sin(angle);
            return new double[]{out * c - side * s, out * s + side * c};
        }

        /** A point of the core, from how far out and at what turn. */
        static double[] polar(double out, double angle) {
            return new double[]{out * Math.cos(angle - Math.PI / 2), out * Math.sin(angle - Math.PI / 2)};
        }

        /** How far out the core's outer and inner rings are at a turn: not round, but scalloped, the one drawn in where the ways come in and the other out. */
        static double outerRing(double angle) { return 41 + Math.sin(3 * angle + .7) - 1.4 * Math.cos(CLASS_COUNT * angle); }

        static double innerRing(double angle) { return 27 + .8 * Math.sin(2 * angle + 2.1) + 1.6 * Math.cos(CLASS_COUNT * angle); }

        int classSmall(int clazz, double[] p) {
            int count = stats++;
            int[] flavour = CLASS_SMALL[clazz];
            return count % 2 == 0 ? add(SMALL, clazz, -1, 3, -1, E_ATK_PCT, 1 << clazz, p[0], p[1]) : add(SMALL, clazz, -1, flavour[1], -1, flavour[0], 1 << clazz, p[0], p[1]);
        }

        int statSmall(int region, double[] p) {
            int stat = stats++ % STATS;
            return add(SMALL, region, stat, SMALL_FIGURES[stat], -1, -1, 0, p[0], p[1]);
        }

        int root(int region, int stat, int[] figures, double[] p) { return add(ROOT, region, stat, figures[stat], -1, -1, 0, p[0], p[1]); }

        int notable(int region, int index, double[] p) { return add(NOTABLE, region, -1, 0, index, -1, 0, p[0], p[1]); }

        /**
         * A cluster: a ring of five small nodes round a notable, entered from one side; the notable is reached from the two far ones. Returns
         * the ring, the entry first.
         */
        int[] cluster(int region, int clazz, double[] middle, int from, int index) {
            Node entry = nodes.get(from);
            double toward = Math.atan2(entry.y - middle[1], entry.x - middle[0]), round = 2.6;
            int[] ring = new int[5];
            for (int at = 0; at < 5; at++) {
                double angle = toward + at * Math.PI * 2 / 5;
                double[] p = {middle[0] + Math.cos(angle) * round, middle[1] + Math.sin(angle) * round};
                ring[at] = clazz >= 0 ? classSmall(clazz, p) : statSmall(region, p);
                if (at > 0) link(ring[at - 1], ring[at]);
            }
            link(ring[4], ring[0]);
            link(from, ring[0]);
            int heart = notable(region, index, middle);
            link(heart, ring[2]);
            link(heart, ring[3]);
            return ring;
        }

        /** A dead-end spur from a cluster's far side, outward from its middle and turned a little: two small nodes and what it ends in. */
        void spur(int region, int clazz, int[] ring, double[] middle, int index, double turn) {
            int far = nodes.get(ring[2]).x * middle[0] + nodes.get(ring[2]).y * middle[1] > nodes.get(ring[3]).x * middle[0] + nodes.get(ring[3]).y * middle[1] ? ring[2] : ring[3];
            Node from = nodes.get(far);
            double angle = Math.atan2(from.y - middle[1], from.x - middle[0]) + turn, ax = Math.cos(angle), ay = Math.sin(angle);
            int previous = far;
            for (int step = 1; step <= 2; step++) {
                int small = classSmall(clazz, new double[]{from.x + ax * 2.6 * step, from.y + ay * 2.6 * step});
                link(previous, small);
                previous = small;
            }
            link(previous, notable(region, index, new double[]{from.x + ax * 8.2, from.y + ay * 8.2}));
        }

        /**
         * A class's region: its start; its trunk out to its jewel and a fork to two clusters, each with a dead-end spur past it; two clusters
         * of actives to the sides; and inward the notable by the start, a gate, a jewel off it, and a winding way on to the core. Each region
         * is drawn a little its own: some the other way round, clusters a little nearer or further.
         */
        void region(int clazz) {
            int first = clazz * PER_CLASS, flip = clazz % 2 == 0 ? 1 : -1;
            double[] begin = at(clazz, 64, 0);
            int start = add(START, clazz, -1, START_ATTACK, -1, E_ATK_PCT, 1 << clazz, begin[0], begin[1]);
            link(0, start);
            // Outward: a step, the region's jewel, and a fork to the two outer clusters.
            int step = classSmall(clazz, at(clazz, 66.6, 0));
            link(start, step);
            int hub = root(clazz, CLASS_STAT[clazz], JEWEL_FIGURES, at(clazz, 69.3, 0));
            link(step, hub);
            for (int side = -1; side <= 1; side += 2) {
                double out = 75.6 + 1.4 * wobble(clazz, side + 3), wide = 7.4 + .7 * wobble(clazz, side + 5);
                int one = classSmall(clazz, at(clazz, 70.6, 2.4 * side));
                link(hub, one);
                int two = classSmall(clazz, at(clazz, out - 3.4, (wide - 2.8) * side));
                link(one, two);
                double[] middle = at(clazz, out, wide * side);
                int[] ring = cluster(clazz, clazz, middle, two, first + (side * flip < 0 ? OUTER_LEFT : OUTER_RIGHT));
                rings[clazz][side < 0 ? 0 : 1] = ring;
                spur(clazz, clazz, ring, middle, first + (side * flip < 0 ? CLASS_KEYSTONE : DEEP), .3 * wobble(clazz, side + 7));
            }
            // Inward: a step and the notable by the start; to each side the clusters of actives; and through the gate, on to the core.
            int in = classSmall(clazz, at(clazz, 61.4, 0));
            link(start, in);
            int early = notable(clazz, first + EARLY, at(clazz, 58.6, 0));
            link(in, early);
            for (int side = -1; side <= 1; side += 2) {
                double out = 57 + .9 * wobble(clazz, side + 9), wide = 7.8 + .4 * wobble(clazz, side + 11);
                int one = classSmall(clazz, at(clazz, 62.4, 2.7 * side));
                link(start, one);
                int two = classSmall(clazz, at(clazz, out + 3.6, (wide - 2.8) * side));
                link(one, two);
                rings[clazz][side < 0 ? 2 : 3] = cluster(clazz, clazz, at(clazz, out, wide * side), two, first + (side * flip < 0 ? ACTIVE_LEFT : ACTIVE_RIGHT));
            }
            int gate = root(clazz, clazz % 2 == 0 ? HP : DEF, ROOT_FIGURES, at(clazz, 55.6, 0));
            link(early, gate);
            // Off the gate, a dead-end jewel, to one side or the other.
            int a = classSmall(clazz, at(clazz, 53.6, 2.5 * flip)), b = classSmall(clazz, at(clazz, 51.6, 4.6 * flip));
            link(gate, a);
            link(a, b);
            link(b, root(clazz, CLASS_STAT[(clazz + 4) % CLASS_COUNT], JEWEL_FIGURES, at(clazz, 49.2, 6.4 * flip)));
            // The way in winds a little.
            int previous = gate, count = 0;
            for (double out = 52.9; out > 44; out -= 2.7) {
                int small = classSmall(clazz, at(clazz, out, -1.1 * flip * Math.sin(count++ * 1.3 + .4)));
                link(previous, small);
                previous = small;
                if (count == 1) loops[clazz] = small;
            }
            ports[clazz] = previous;
        }

        /** The ring node of a cluster nearest the middle of the tree, of those still with room for a link (not its entry). */
        int inmost(int[] ring) {
            int best = -1;
            double least = 1e9;
            for (int at = 1; at < ring.length; at++) {
                Node node = nodes.get(ring[at]);
                double far = Math.hypot(node.x, node.y);
                if (far < least && node.next.size() <= 3) {
                    least = far;
                    best = ring[at];
                }
            }
            return best;
        }

        /** The ring node of a cluster furthest toward one side (to the next class's, above nought), of those still with room for a link. */
        int toward(int clazz, int[] ring, int side) {
            int best = -1;
            double most = -1e9;
            double angle = clazz * TURN - Math.PI / 2, sx = -Math.sin(angle), sy = Math.cos(angle);
            for (int at = 1; at < ring.length; at++) {
                Node node = nodes.get(ring[at]);
                double far = (node.x * sx + node.y * sy) * side;
                if (far > most && node.next.size() <= 3) {
                    most = far;
                    best = ring[at];
                }
            }
            return best;
        }

        /**
         * A route of small nodes from one node to another, about so far apart, bowed out to one side by so much; one in the middle may be a
         * notable. Returns the nodes on it.
         */
        List<Integer> route(int region, int from, int to, double apart, double bow, int middleNotable, int classes) {
            Node a = nodes.get(from), b = nodes.get(to);
            double length = Math.hypot(b.x - a.x, b.y - a.y), nx = -(b.y - a.y) / length, ny = (b.x - a.x) / length;
            int steps = Math.max(1, (int) Math.round((length + Math.abs(bow)) / apart));
            if (middleNotable >= 0 && steps % 2 == 1) steps++;
            List<Integer> made = new ArrayList<>();
            int previous = from;
            for (int at = 1; at < steps; at++) {
                double t = at / (double) steps, bend = bow * Math.sin(Math.PI * t);
                double[] p = {a.x + (b.x - a.x) * t + nx * bend, a.y + (b.y - a.y) * t + ny * bend};
                int node;
                if (middleNotable >= 0 && at == steps / 2) node = notable(region, middleNotable, p);
                else if (classes != 0) node = add(SMALL, region, -1, 2, -1, E_ATK_PCT, classes, p[0], p[1]);
                else node = statSmall(region, p);
                link(previous, node);
                made.add(node);
                previous = node;
            }
            link(previous, to);
            return made;
        }

        /** Between a class and the next: out at the edge, a hybrid route bowed outward with its notable; in the middle, a few steps across. */
        void crossings(int clazz) {
            int next = (clazz + 1) % CLASS_COUNT, both = 1 << clazz | 1 << next;
            int from = toward(clazz, rings[clazz][1], 1), to = toward(next, rings[next][0], -1);
            Node a = nodes.get(from), b = nodes.get(to);
            // (Bowed away from the middle of the tree.)
            double mx = (a.x + b.x) / 2, my = (a.y + b.y) / 2, nx = -(b.y - a.y), ny = b.x - a.x;
            double outward = Math.signum(nx * mx + ny * my);
            route(HYBRID + clazz, from, to, 2.6, outward * (2.2 + wobble(clazz, 13)), HYBRIDS + clazz, both);
            from = toward(clazz, rings[clazz][3], 1);
            to = toward(next, rings[next][2], -1);
            route(HYBRID + clazz, from, to, 2.5, 0, -1, 0);
        }

        /**
         * The core: where each region's route comes in, a ring round (not quite round); from the ring between each two, a cluster; on each
         * winding way in, a notable; a second ring further in; and off it, dead ends: actives, passives, keystones and jewels. A loop from some
         * regions' ways in joins the ring a little to one side, a second way in.
         */
        void core() {
            int[] outer = new int[CLASS_COUNT], inner = new int[CLASS_COUNT];
            int[][] arcs = new int[CLASS_COUNT][8];
            for (int clazz = 0; clazz < CLASS_COUNT; clazz++) {
                double angle = clazz * TURN;
                outer[clazz] = root(CORE, clazz % STATS, ROOT_FIGURES, polar(outerRing(angle), angle));
                link(ports[clazz], outer[clazz]);
            }
            int passive = CORE_PASSIVES;
            for (int clazz = 0; clazz < CLASS_COUNT; clazz++) {
                int next = (clazz + 1) % CLASS_COUNT, previous = outer[clazz];
                arcs[clazz][0] = outer[clazz];
                for (int at = 1; at <= 7; at++) {
                    double angle = (clazz + at / 8.0) * TURN;
                    int small = statSmall(CORE, polar(outerRing(angle), angle));
                    link(previous, small);
                    previous = small;
                    arcs[clazz][at] = small;
                }
                link(previous, outer[next]);
                double rim = outerRing(clazz * TURN + HALF) - 2.5, angle = clazz * TURN + HALF + .04 * wobble(clazz, 21), deep = rim - 5.1;
                int step = statSmall(CORE, polar(rim, clazz * TURN + HALF));
                link(arcs[clazz][4], step);
                cluster(CORE, -1, polar(deep, angle), step, passive++);
            }
            // Loops: from the way in of every other region, a bowed route to the ring a little to one side of where it comes in.
            for (int clazz = 1; clazz < CLASS_COUNT; clazz += 2) route(clazz, inmost(rings[clazz][3]), arcs[clazz][2], 2.6, 1.2, -1, 1 << clazz);
            for (int clazz = 0; clazz < CLASS_COUNT; clazz++) {
                double angle = clazz * TURN, top = outerRing(angle), bottom = innerRing(angle);
                double sway = .045 * (clazz % 2 == 0 ? 1 : -1);
                double span = top - bottom;
                int one = statSmall(CORE, polar(top - span * .19, angle + sway));
                link(outer[clazz], one);
                int heart = notable(CORE, passive++, polar(top - span * .42, angle + sway * 1.5));
                link(one, heart);
                int two = statSmall(CORE, polar(top - span * .64, angle + sway * .5));
                link(heart, two);
                int three = statSmall(CORE, polar(top - span * .83, angle - sway * .3));
                link(two, three);
                inner[clazz] = root(CORE, (clazz + 2) % STATS, ROOT_FIGURES, polar(bottom, angle));
                link(three, inner[clazz]);
            }
            int active = CORE_ACTIVES, keystone = CORE_KEYSTONES;
            for (int clazz = 0; clazz < CLASS_COUNT; clazz++) {
                int next = (clazz + 1) % CLASS_COUNT, previous = inner[clazz], middle = -1;
                for (int at = 1; at <= 5; at++) {
                    double angle = (clazz + at / 6.0) * TURN;
                    int small = statSmall(CORE, polar(innerRing(angle), angle));
                    link(previous, small);
                    previous = small;
                    if (at == 3) middle = small;
                }
                link(previous, inner[next]);
                // Off the ring between, inward: an active (most of them), or a passive, or one jewel.
                double half = clazz * TURN + HALF, rim = innerRing(half);
                int step = statSmall(CORE, polar(rim - 2.6, half));
                link(middle, step);
                double[] end = polar(rim - 5.6 - .8 * wobble(clazz, 23), half + .04 * wobble(clazz, 24));
                int last;
                if (clazz % 2 == 0 || clazz == 1) last = notable(CORE, active++, end);
                else if (clazz == 11) last = root(CORE, ATK, JEWEL_FIGURES, end);
                else last = notable(CORE, passive++, end);
                link(step, last);
                // Off the ring where a way comes in, inward: a keystone (every other one), or a jewel.
                double angle = clazz * TURN, ring = innerRing(angle);
                int a = statSmall(CORE, polar(ring - 2.6, angle));
                link(inner[clazz], a);
                int b = statSmall(CORE, polar(ring - 5.2, angle + .06 * wobble(clazz, 25)));
                link(a, b);
                double[] deep = polar(ring - 8.4, angle + .1 * wobble(clazz, 26));
                link(b, clazz % 2 == 0 && clazz < 12 ? notable(CORE, keystone++, deep) : root(CORE, clazz % STATS, JEWEL_FIGURES, deep));
            }
        }
        /**
         * The moves of the air, laid after everything else so that no node before them changes its number: round her seed, in the clear
         * space at the heart of the core, a ring of their four passives, each reached from the core's inner notables by a step, with Skyborne
         * on a spur off the ring; and each class's own at the edge of its region, where there is room, on a spur of two steps from one of its
         * small nodes. Every place is tried against what is already there, so that nothing lies over anything or under a link.
         */
        void skyward() {
            int before = nodes.size();
            double round = 12.5;
            int[] ring = new int[SKY_COUNT - 1];
            double[] angles = new double[SKY_COUNT - 1];
            // Each passive, as near its quarter as it can be laid with a clear way out to the core's inner ring, and that way laid with it.
            for (int at = 0; at < ring.length; at++) {
                double target = Math.PI / 4 + at * Math.PI / 2;
                for (int tries = 0; tries <= 60 && angles[at] == 0; tries++) {
                    double angle = target + (tries % 2 == 0 ? 1 : -1) * ((tries + 1) / 2) * Math.PI / 180;
                    double[] p = polar(round, angle);
                    if (!room(p, .8)) continue;
                    List<Integer> near = new ArrayList<>();
                    for (int id = 1; id < before; id++) {
                        Node node = nodes.get(id);
                        double out = Math.hypot(node.x, node.y);
                        if (node.path == CORE && out > 23 && out < 31 && node.next.size() >= 2 && !nextToEnd(id)) near.add(id);
                    }
                    near.sort((one, two) -> Double.compare(far(nodes.get(one), p[0], p[1]), far(nodes.get(two), p[0], p[1])));
                    for (int id : near) {
                        Node into = nodes.get(id);
                        double length = far(into, p[0], p[1]);
                        if (length > 18) break;
                        int steps = (int) Math.ceil(length / 3.4) - 1;
                        double[][] way = new double[steps][];
                        boolean fits = clear(p[0], p[1], into.x, into.y, id, -1);
                        for (int step = 0; step < steps && fits; step++) {
                            way[step] = new double[]{p[0] + (into.x - p[0]) * (step + 1) / (steps + 1), p[1] + (into.y - p[1]) * (step + 1) / (steps + 1)};
                            fits = room(way[step], .38);
                        }
                        if (!fits) continue;
                        angles[at] = angle;
                        ring[at] = notable(CORE, SKY + at, p);
                        int previous = ring[at];
                        for (double[] point : way) {
                            int small = statSmall(CORE, point);
                            link(previous, small);
                            previous = small;
                        }
                        link(previous, id);
                        break;
                    }
                }
            }
            // The ring itself: small nodes along it from each passive to the next.
            List<Integer> smalls = new ArrayList<>();
            for (int at = 0; at < ring.length; at++) {
                int next = (at + 1) % ring.length, previous = ring[at];
                double from = angles[at], to = angles[next];
                if (to <= from) to += Math.PI * 2;
                int count = Math.max(1, (int) Math.round((to - from) * round / 5.2) - 1);
                for (int step = 1; step <= count; step++) {
                    int small = statSmall(CORE, polar(round, from + (to - from) * step / (count + 1)));
                    smalls.add(small);
                    link(previous, small);
                    previous = small;
                }
                link(previous, ring[next]);
            }            // Skyborne, on a spur off the ring: outward from one of its small nodes where there is room, or else in toward her seed.
            boolean laid = false;
            for (double[] way : new double[][]{{3.2, 6.4}, {3.6, 7.2}, {-3, -5.8}}) {
                for (int small : smalls) {
                    if (laid) break;
                    Node from = nodes.get(small);
                    double angle = Math.atan2(from.y, from.x), out = Math.hypot(from.x, from.y);
                    double[] step = {Math.cos(angle) * (out + way[0]), Math.sin(angle) * (out + way[0])}, end = {Math.cos(angle) * (out + way[1]), Math.sin(angle) * (out + way[1])};
                    if (!room(step, .38) || !room(end, 1.05) || !clear(from.x, from.y, end[0], end[1], small, -1)) continue;
                    int one = statSmall(CORE, step);
                    link(small, one);
                    link(one, notable(CORE, SKY + SKY_COUNT - 1, end));
                    laid = true;
                }
            }
            for (int which = 0; which < SKY_CLASS.length; which++) {
                int clazz = SKY_CLASS[which];
                double best = Double.MAX_VALUE;
                double[] spot = null;
                int near = -1;
                for (double out = 46; out <= 86; out += 1) {
                    for (double side = -16; side <= 16; side += 1) {
                        double[] p = at(clazz, out, side);
                        if (Math.hypot(p[0], p[1]) > REACH - 4 || !room(p, .8)) continue;
                        for (Node node : nodes) {
                            if (node.path != clazz || node.type != SMALL || node.next.size() != 2 || nextToEnd(node.id)) continue;
                            double length = far(node, p[0], p[1]);
                            if (length < 6 || length > 9 || length >= best) continue;
                            double[] one = {node.x + (p[0] - node.x) / 3, node.y + (p[1] - node.y) / 3}, two = {node.x + (p[0] - node.x) * 2 / 3, node.y + (p[1] - node.y) * 2 / 3};
                            if (!room(one, .38) || !room(two, .38) || !clear(node.x, node.y, p[0], p[1], node.id, -1)) continue;
                            best = length;
                            spot = p;
                            near = node.id;
                        }
                    }
                }
                Node from = nodes.get(near);
                int previous = near;
                for (int step = 1; step <= 2; step++) {
                    int small = classSmall(clazz, new double[]{from.x + (spot[0] - from.x) * step / 3, from.y + (spot[1] - from.y) * step / 3});
                    link(previous, small);
                    previous = small;
                }
                link(previous, notable(clazz, SKY_CLASSES + which, spot));
            }
        }

        static double far(Node node, double x, double y) { return Math.hypot(node.x - x, node.y - y); }

        /** Whether a node is next to a dead end. */
        boolean nextToEnd(int id) {
            for (int other : nodes.get(id).next) if (nodes.get(other).next.size() == 1) return true;
            return false;
        }

        /** How big a node is drawn, as the tree draws it: what nothing may lie over. */
        static double sized(Node node) {
            if (node.type == CENTRE) return 0;
            if (node.type == START) return 1.25;
            if (node.type == NOTABLE) return keystone(node) ? 1.05 : .8;
            return node.type == ROOT ? .58 : .38;
        }

        /** Whether a node of a size has room at a point: clear of every node, and of every link. */
        boolean room(double[] p, double size) {
            for (Node node : nodes) {
                if (node.id == 0) continue;
                if (far(node, p[0], p[1]) < (size + sized(node)) * 1.35 + .35) return false;
                for (int other : node.next) {
                    if (other < node.id || other == 0) continue;
                    Node b = nodes.get(other);
                    if (gap(p[0], p[1], node.x, node.y, b.x, b.y) < size * 1.1 + .2) return false;
                }
            }
            return true;
        }

        /** Whether a link from one point to another runs clear of every node but those it joins. */
        boolean clear(double ax, double ay, double bx, double by, int one, int two) {
            for (Node node : nodes) {
                if (node.id == 0 || node.id == one || node.id == two) continue;
                if (gap(node.x, node.y, ax, ay, bx, by) < sized(node) * 1.1 + .25) return false;
            }
            return true;
        }

        static double gap(double px, double py, double ax, double ay, double bx, double by) {
            double dx = bx - ax, dy = by - ay, t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)));
            return Math.hypot(px - ax - t * dx, py - ay - t * dy);
        }
    }
    // Taken
    /** Points: one a level after the first, and one more every tenth. */
    static int pointsFor(int level) { return Math.max(0, level - 1) + Math.max(0, level - 1) / 10; }

    /** Whether a node can be taken, with these taken already: not taken, and next to one that is (the hidden root, which every start hangs from, always is). */
    static boolean canTake(java.util.BitSet taken, int id) {
        if (id <= 0 || id >= tree().size() || taken.get(id)) return false;
        for (int other : tree().get(id).next) if (other == 0 || taken.get(other)) return true;
        return false;
    }

    /** What respeccing costs, in pluses: by how many are taken. */
    static int respecCost(java.util.BitSet taken) { return 100 + taken.cardinality() * 25; }

    /** All that is taken, added up: flat stats; the effects felt whatever she holds; those felt with each class's weapons; and the actives taken. */
    static final class Built {
        final int[] stats = new int[STATS];
        final int[] effects = new int[E_EFFECTS];
        final int[][] byClass = new int[CLASS_COUNT][E_EFFECTS];
        final List<Notable> actives = new ArrayList<>();

        /** So much of an effect, for some classes (0 for any); a cost, and anything that is felt always, always. */
        void add(int effect, int figure, int classes) {
            if (figure < 0 || classes == 0 || !bound(effect)) {
                effects[effect] += figure;
                return;
            }
            for (int clazz = 0; clazz < CLASS_COUNT; clazz++) if ((classes >> clazz & 1) != 0) byClass[clazz][effect] += figure;
        }

        /** How much of an effect she has with a class's weapon in her hands (-1 for none). */
        int with(int effect, int clazz) { return effects[effect] + (clazz >= 0 && clazz < CLASS_COUNT ? byClass[clazz][effect] : 0); }
    }

    static Built built(java.util.BitSet taken) {
        Built built = new Built();
        List<Node> nodes = tree();
        for (int id = taken.nextSetBit(1); id >= 0 && id < nodes.size(); id = taken.nextSetBit(id + 1)) {
            Node node = nodes.get(id);
            if (node.type == NOTABLE) {
                Notable notable = NOTABLES.get(node.notable);
                // (An active's effect is felt only while it is called on, from a slot.)
                if (notable.kind == ACTIVE) {
                    built.actives.add(notable);
                    continue;
                }
                for (int at = 0; at < notable.effects.length; at++) built.add(notable.effects[at], notable.figures[at], notable.classes);
            } else if (node.effect >= 0) {
                built.add(node.effect, node.figure, node.classes);
            } else if (node.stat >= 0) {
                built.stats[node.stat] += node.figure;
            }
        }
        return built;
    }

    /** The notable a node is, or null. */
    static Notable notableAt(int id) {
        List<Node> nodes = tree();
        if (id <= 0 || id >= nodes.size()) return null;
        Node node = nodes.get(id);
        return node.type == NOTABLE ? NOTABLES.get(node.notable) : null;
    }

    /** Whether a node is a keystone. */
    static boolean keystone(Node node) { return node.type == NOTABLE && NOTABLES.get(node.notable).keystone; }

    /** A node in a few words: what it is, then what it gives. */
    static String title(Node node) {
        if (node.type == CENTRE) return "Her seed";
        if (node.type == START) return CLASSES[node.path] + ": start";
        if (node.type == NOTABLE) return NOTABLES.get(node.notable).name;
        if (node.effect >= 0) return capital(phrase(node.effect, node.figure));
        return "+" + node.figure + " " + STAT_NAMES[node.stat];
    }

    static String kindWords(Node node) {
        if (node.type == CENTRE) return "Where every start hangs from";
        String region = PATH_NAMES[node.path];
        if (node.type == START) return "Start · " + region;
        if (node.type == SMALL) return "Small node · " + region;
        if (node.type == ROOT) return (node.figure >= JEWEL_FIGURES[node.stat] ? "Jewel" : "Root") + " · " + region;
        Notable notable = NOTABLES.get(node.notable);
        return (notable.keystone ? "Keystone" : notable.kind == ACTIVE ? "Active" : "Passive") + " · " + region;
    }

    /** What a node does, said plainly. */
    static String does(Node node) {
        if (node.type == CENTRE) return "Every start hangs from here.";
        if (node.type == START) return "Where the " + CLASSES[node.path].toLowerCase(java.util.Locale.ROOT) + "' routes begin: any start can be taken first. With "
                + CLASS_WITH[node.path] + ": +" + node.figure + "% attack";
        if (node.type == NOTABLE) {
            Notable notable = NOTABLES.get(node.notable);
            String said = notable.said();
            return notable.kind == ACTIVE ? said + ", for " + (int) ACTIVE_LASTS + " seconds when called on from a slot (" + (int) ACTIVE_COST + " energy, then "
                    + (int) ACTIVE_REST + " seconds to rest)" : notable.keystone ? said + ". A keystone: it changes a build" : notable.classes == 0 ? said + ", always" : said;
        }
        if (node.effect >= 0) return (node.classes != 0 && bound(node.effect) ? "With " + withWords(node.classes) + ": " : "") + phrase(node.effect, node.figure);
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

    /** Every taken node still joined to the hidden root through taken nodes; any other let go. */
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

    /** The tree as it was before it was redrawn: what a save of it was kept under is let go, once, and every point is hers to spend again. */
    static final String TREE_VERSION_KEY = "run_tree_redrawn", REDRAWN_NOTE = "The tree was redrawn; your points are back to spend";

    /** Whether a save of the tree is of the tree before it was redrawn, to be let go: not read since it was, and with something taken in it. */
    static boolean fromBefore(int seen, String taken) { return seen == 0 && taken != null && !taken.isEmpty(); }

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
