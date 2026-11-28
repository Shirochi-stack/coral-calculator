package com.coral.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Her inventory, which is her backpack and her bag as one: five places for what she has on her, and a grid of everything else.
 *
 * <p>The five places are the main weapon (what she begins every land with), the secondary weapon (what the swap button, and Q or Tab,
 * changes it for), an armour and two accessories. A thing is an "entry": a weapon of her backpack, which is its kind with the smith's
 * work on it ({@link #PLAIN} plus the kind), or a thing found, which is its number in {@link RunLoot}.
 *
 * <p>What is kept is what was kept before there was an inventory, so that nothing has to be moved: the weapon found that she holds is
 * {@code run_hand} and is the main weapon; with none, the main weapon is the kind in {@code run_mainkind}, or else the one chosen
 * from the first few ({@code run_main}), or her tablet-book. New is {@code run_second}: the secondary weapon, as an entry, or none.
 * What she wears is {@code run_worn} as ever.
 */
final class RunInventory {
    private RunInventory() {}

    /** A weapon of her backpack, as an entry: this and its kind. A thing found is its own number, which is far below it. */
    static final int PLAIN = 1 << 20;
    static final int MAIN = 0, SECOND = 1, ARMOUR = 2, FIRST = 3, LAST = 4, SLOTS = 5;
    /** What the grid shows. */
    static final int ALL = 0, WEAPONS = 1, ARMOURS = 2, ACCESSORIES = 3, CURIOS = 4;
    static final String[] TABS = {"All", "Weapons", "Armour", "Accessories", "Curios"};
    /** How the grid is ordered: the rarest first; by what it is (weapons by their class, and the rest); the latest found first. */
    static final int BY_RARITY = 0, BY_TYPE = 1, BY_NEW = 2;
    static final String[] SORTS = {"Rarity", "Type", "Newest"};

    static boolean plain(int entry) { return entry >= PLAIN; }
    static int plainOf(int kind) { return PLAIN + kind; }
    static boolean weapon(int entry) { return entry != 0 && (plain(entry) || RunLoot.type(entry) == RunLoot.WEAPON); }

    /** The kind of weapon an entry is. */
    static int kindOf(int entry) { return plain(entry) ? entry - PLAIN : RunArms.kindOf(entry); }

    /** A weapon of her backpack is white; the rest are what they are found as. */
    static int rarity(int entry) { return plain(entry) ? RunLoot.WHITE : RunLoot.rarity(entry); }

    /** The type of an entry: a weapon, an armour or an accessory. */
    static int type(int entry) { return plain(entry) ? RunLoot.WEAPON : RunLoot.type(entry); }

    /** The class an entry belongs to (for sorting by what it is): a weapon's class; for the rest, the class they lean to or are for; or after them all. */
    static int classOf(int entry) {
        if (weapon(entry)) {
            int cls = RunLoot.classOf(kindOf(entry));
            return cls == RunLoot.ANY ? RunLoot.CLASSES : cls;
        }
        int lean = RunLoot.type(entry) == RunLoot.ARMOUR ? RunLoot.leansTo(RunLoot.base(entry)) : RunLoot.does(entry)[0];
        return lean == RunLoot.ANY ? RunLoot.CLASSES : lean;
    }

    /** The name of the class a weapon is of, or "Any" for one that is of none. */
    static String className(int kind) {
        int cls = RunLoot.classOf(kind);
        return cls == RunLoot.ANY ? "Any" : RunLoot.CLASS_NAMES[cls];
    }

    // What is kept

    /** The weapon found that she holds, or 0. */
    static int hand(Unlocks.Store store) {
        String note = store.note("run_hand");
        try {
            int code = note == null || note.trim().isEmpty() ? 0 : Integer.parseInt(note.trim());
            return code != 0 && RunLoot.type(code) == RunLoot.WEAPON ? code : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /** What is in her backpack as bits of kinds: what she found in the lands, and the weapons that are hers to begin with. */
    static int owned(Unlocks.Store store) {
        int bits = store.tally("run_pack");
        int mains = store.tally("run_mains") | 1;
        for (int index = 0; index < RunArms.MAINS.length; index++) if ((mains >> index & 1) != 0) bits |= 1 << RunArms.MAINS[index];
        return bits;
    }

    /** The main weapon, as an entry: the one found that she holds; else the kind she chose; else the one of the first few she chose; else her tablet-book. */
    static int main(Unlocks.Store store) {
        int hand = hand(store);
        if (hand != 0) return hand;
        int kind = noteKind(store, "run_mainkind");
        if (kind >= 0) return plainOf(kind);
        int mains = store.tally("run_mains") | 1, chosen = store.tally("run_main");
        return plainOf(chosen >= 0 && chosen < RunArms.MAINS.length && (mains >> chosen & 1) != 0 ? RunArms.MAINS[chosen] : PlusRun.TABLET);
    }

    private static int noteKind(Unlocks.Store store, String key) {
        String note = store.note(key);
        try {
            int kind = note == null || note.trim().isEmpty() ? -1 : Integer.parseInt(note.trim());
            return kind >= 0 && kind < RunArms.COUNT ? kind : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    /** The secondary weapon, as an entry, or 0: one found must still be in her bag (and not also the main one, unless there are two of it); one of her backpack must be hers. */
    static int second(Unlocks.Store store) {
        String note = store.note("run_second");
        int entry;
        try {
            entry = note == null || note.trim().isEmpty() ? 0 : Integer.parseInt(note.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
        if (entry == 0) return 0;
        int main = main(store);
        if (plain(entry)) {
            int kind = entry - PLAIN;
            return kind < RunArms.COUNT && (owned(store) >> kind & 1) != 0 && entry != main ? entry : 0;
        }
        if (RunLoot.type(entry) != RunLoot.WEAPON) return 0;
        int has = count(RunLoot.bag(store.note("run_bag")), entry);
        return has >= (entry == main ? 2 : 1) ? entry : 0;
    }

    static int count(List<Integer> bag, int code) {
        int count = 0;
        for (int one : bag) if (one == code) count++;
        return count;
    }

    /** What is in a place: weapons by their entries, the rest by what is worn. */
    static int inSlot(Unlocks.Store store, int slot) {
        if (slot == MAIN) return main(store);
        if (slot == SECOND) return second(store);
        RunLoot.Worn worn = RunLoot.worn(store.note("run_worn"));
        return slot == ARMOUR ? worn.armour : slot == FIRST ? worn.first : worn.second;
    }

    /** Which place an entry is in (a weapon in the main or the secondary place, the rest in what is worn), or -1. */
    static int slotOf(Unlocks.Store store, int entry) {
        if (entry == 0) return -1;
        if (weapon(entry)) return entry == main(store) ? MAIN : entry == second(store) ? SECOND : -1;
        RunLoot.Worn worn = RunLoot.worn(store.note("run_worn"));
        return worn.armour == entry ? ARMOUR : worn.first == entry ? FIRST : worn.second == entry ? LAST : -1;
    }

    private static void writeMain(Unlocks.Store store, int entry) {
        if (plain(entry)) {
            store.keepNote("run_hand", "0");
            store.keepNote("run_mainkind", String.valueOf(entry - PLAIN));
        } else {
            store.keepNote("run_hand", String.valueOf(entry));
            store.keepNote("run_mainkind", "");
        }
    }

    /** A weapon becomes the main one. What was the secondary one and is now the main one gives the secondary place to what was main. */
    static boolean setMain(Unlocks.Store store, int entry) {
        if (!weapon(entry)) return false;
        int was = main(store), second = second(store);
        if (entry == was) return false;
        writeMain(store, entry);
        store.keepNote("run_second", entry == second ? String.valueOf(was) : second == 0 ? "" : String.valueOf(second));
        return true;
    }

    /** A weapon becomes the secondary one; with it the main one already, the two change places (when there is a secondary to take the main place). */
    static boolean setSecond(Unlocks.Store store, int entry) {
        if (!weapon(entry)) return false;
        int main = main(store), second = second(store);
        if (entry == second) return false;
        if (entry == main) {
            if (second == 0) return false;
            writeMain(store, second);
            store.keepNote("run_second", String.valueOf(main));
            return true;
        }
        store.keepNote("run_second", String.valueOf(entry));
        return true;
    }

    /** The secondary place is emptied. */
    static boolean clearSecond(Unlocks.Store store) {
        if (second(store) == 0) return false;
        store.keepNote("run_second", "");
        return true;
    }

    /** What she wears when a thing is put on: an armour takes the armour's place; an accessory the first free place, or with both full, that of the less rare of the two. */
    static RunLoot.Worn equip(RunLoot.Worn worn, int code) {
        int type = RunLoot.type(code);
        if (type == RunLoot.ARMOUR) return new RunLoot.Worn(code, worn.first, worn.second);
        if (type != RunLoot.ACCESSORY) return worn;
        boolean firstFree = worn.first == 0, secondFree = worn.second == 0;
        boolean intoFirst = firstFree || !secondFree && RunLoot.rarity(worn.first) <= RunLoot.rarity(worn.second);
        return intoFirst ? new RunLoot.Worn(worn.armour, code, worn.second) : new RunLoot.Worn(worn.armour, worn.first, code);
    }

    /** What she wears when a thing is put on in a particular accessory place (1 for the first, 2 for the second). */
    static RunLoot.Worn equipAt(RunLoot.Worn worn, int code, int slot) {
        if (RunLoot.type(code) != RunLoot.ACCESSORY) return equip(worn, code);
        return slot == LAST ? new RunLoot.Worn(worn.armour, worn.first, code) : new RunLoot.Worn(worn.armour, code, worn.second);
    }

    /** What she wears when a place is emptied. */
    static RunLoot.Worn unequip(RunLoot.Worn worn, int slot) {
        return new RunLoot.Worn(slot == ARMOUR ? 0 : worn.armour, slot == FIRST ? 0 : worn.first, slot == LAST ? 0 : worn.second);
    }

    /** Whether one more of a thing found may be put on: there is one in the bag that is not worn already, and an accessory only twice. */
    static boolean canWear(RunLoot.Worn worn, List<Integer> bag, int code) {
        int type = RunLoot.type(code);
        int wearing = (worn.armour == code ? 1 : 0) + (worn.first == code ? 1 : 0) + (worn.second == code ? 1 : 0);
        return (type == RunLoot.ARMOUR || type == RunLoot.ACCESSORY) && wearing < count(bag, code) && (type == RunLoot.ARMOUR || wearing < 2);
    }

    // The grid

    /** One cell of the grid: what it is, and where it came from (for ordering by what was found last). */
    static final class Cell {
        final int entry, found;
        Cell(int entry, int found) {
            this.entry = entry;
            this.found = found;
        }
    }

    /** What the grid shows for a tab and an order: her backpack's weapons (those in the bits) and everything in her bag. */
    static List<Cell> cells(int ownedKinds, List<Integer> bag, int tab, int sort) {
        List<Cell> cells = new ArrayList<>();
        if (tab == ALL || tab == WEAPONS) {
            for (int kind = 0; kind < RunArms.COUNT; kind++) if ((ownedKinds >> kind & 1) != 0) cells.add(new Cell(plainOf(kind), -1 - kind));
        }
        for (int index = 0; index < bag.size(); index++) {
            int code = bag.get(index), type = RunLoot.type(code);
            if (tab == ALL || type == tab - 1) cells.add(new Cell(code, index));
        }
        Collections.sort(cells, order(sort));
        return cells;
    }

    /** (Economy) The curios tab: one cell for each kind of curio she has, in an order (by rarity, or by what they are worth for "type"; newest is by number). */
    static List<Cell> curioCells(Unlocks.Store store, int sort) {
        List<Cell> cells = new ArrayList<>();
        int[] counts = RunArtifacts.counts(store);
        for (int id = 0; id < RunArtifacts.COUNT; id++) if (counts[id] > 0) cells.add(new Cell(RunArtifacts.entry(id), id));
        Collections.sort(cells, (one, other) -> {
            int a = RunArtifacts.idOf(one.entry), b = RunArtifacts.idOf(other.entry);
            if (sort == BY_NEW) return Integer.compare(b, a);
            if (sort == BY_TYPE) return Integer.compare(RunArtifacts.value(b), RunArtifacts.value(a));
            int rare = Integer.compare(RunArtifacts.rarity(b), RunArtifacts.rarity(a));
            return rare != 0 ? rare : Integer.compare(RunArtifacts.value(b), RunArtifacts.value(a));
        });
        return cells;
    }

    /** An order: by rarity, the rarest first (then by type, then the latest found); by type (weapons by class, then armour, then accessories, each rarest first); or the latest found first. */
    static Comparator<Cell> order(int sort) {
        return (one, other) -> {
            if (sort == BY_NEW) {
                int later = Integer.compare(other.found, one.found);
                return later != 0 ? later : Integer.compare(one.entry, other.entry);
            }
            int rare = Integer.compare(rarity(other.entry), rarity(one.entry));
            int kind = Integer.compare(type(one.entry), type(other.entry));
            int sameClass = Integer.compare(classOf(one.entry), classOf(other.entry));
            int later = Integer.compare(other.found, one.found);
            if (sort == BY_RARITY) {
                if (rare != 0) return rare;
                if (kind != 0) return kind;
                return sameClass != 0 ? sameClass : later;
            }
            if (kind != 0) return kind;
            if (sameClass != 0) return sameClass;
            return rare != 0 ? rare : later;
        };
    }

    /** The place in a list of cells of an entry, or -1. */
    static int indexOf(List<Cell> cells, int entry) {
        for (int index = 0; index < cells.size(); index++) if (cells.get(index).entry == entry) return index;
        return -1;
    }

    /** What a weapon's item counts as in hand: when she holds the secondary weapon and it is a find, that find; otherwise the main one's. */
    static int itemFor(PlusRun.Perks perks, int weapon) {
        if (perks.itemMain == 0 && perks.itemSecond == 0) return perks.item;
        if (perks.itemSecond != 0 && weapon >= 0 && RunArms.kindOf(perks.itemSecond) == weapon && (perks.itemMain == 0 || RunArms.kindOf(perks.itemMain) != weapon)) return perks.itemSecond;
        return perks.itemMain;
    }

    /** The weapon a swap changes this one for, with a secondary weapon: the other of the two; with neither held, the main one. Or {@code none} if there is no secondary. */
    static int swapTarget(int weapon, int main, int second, int none) {
        if (second == PlusRun.UNARMED) return none;
        return weapon == second ? main : weapon == main ? second : main;
    }
}
