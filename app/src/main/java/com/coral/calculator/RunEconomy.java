package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The selling economy of Plus run: what a thing found (gear, curios) sells for, which of it is junk, what is locked against being sold,
 * and the elf merchant's rotating stock and her prices. The rules are here and do not draw anything; the screens ask them.
 *
 * <p>How it is balanced. A land gives her, from its creatures and chests, in the order of a hundred and a half pluses (a creature
 * {@link RunLoot#plusesFor} of them, more later), and the counter's prices are {@code 70} to {@code 1500}: one to ten lands of play. What
 * she sells has to be a seasoning and never the main income, so that nobody plays to farm a merchant: junk gear is worth a few pluses
 * (white 4, blue 12), the good gear a lot less than a counter ware (purple 30, gold 80), and a rare curio is a treat. A fair player sells
 * about one fifth of what a land gave her in pluses, and the checks in the tests hold it there. Everything the merchant sells costs more
 * than she buys it for, even with every discount of the curios on one side and every bonus on the other, so there is no profit in
 * buying and selling back.
 */
final class RunEconomy {
    private RunEconomy() {}

    /** What a piece of gear sells for at each rarity (white to the rose), before its weapon or chapter markup. */
    private static final int[] GEAR = {4, 12, 30, 80, 200, 500};
    /** A merchant sells at this many times what she would pay for the same thing; and the most her discounts can take off, in percent. */
    static final float MARKUP = 2.2f;
    /** The most junk is: white and blue gear, and curios of the two lowest rarities. */
    static final int JUNK_GEAR = RunLoot.BLUE, JUNK_CURIO = 1;

    // What things are worth

    /** What an entry sells for, with no bonus: gear by its rarity, type and chapter; a curio by its table; her own backpack weapons never. */
    static int value(int entry) {
        if (entry == 0 || RunInventory.plain(entry)) return 0;
        if (RunArtifacts.is(entry)) return RunArtifacts.value(RunArtifacts.idOf(entry));
        int rarity = RunLoot.rarity(entry);
        float markup = 1 + .06f * Math.min(RunLoot.THEMES.length - 1, RunLoot.theme(entry));
        if (RunLoot.type(entry) == RunLoot.WEAPON) markup *= 1.25f;
        if (RunLoot.paper(entry)) markup *= .5f;
        if (RunLoot.ultimate(entry)) return 0;
        return Math.max(1, Math.round(GEAR[Math.max(0, Math.min(RunLoot.ROSE, rarity))] * markup));
    }

    /** What she is paid, with the bonus the curios she carries give. */
    static int price(int entry, int bonusPercent) {
        int value = value(entry);
        return value == 0 ? 0 : Math.max(1, Math.round(value * (100 + Math.min(RunArtifacts.SELL_MOST, Math.max(0, bonusPercent))) / 100f));
    }

    // What is locked

    static List<Integer> locks(Unlocks.Store store) {
        List<Integer> out = new ArrayList<>();
        String note = store.note("run_locks");
        if (note == null || note.isEmpty()) return out;
        for (String one : note.split(",")) {
            try {
                out.add(Integer.parseInt(one.trim()));
            } catch (NumberFormatException ignored) {
                // (Dropped.)
            }
        }
        return out;
    }

    static boolean locked(Unlocks.Store store, int entry) { return locks(store).contains(entry); }

    /** Locks a thing against being sold (every one of its kind), or unlocks it. */
    static void lock(Unlocks.Store store, int entry, boolean on) {
        List<Integer> locks = locks(store);
        boolean has = locks.contains(entry);
        if (on && !has) locks.add(entry);
        else if (!on && has) locks.remove((Integer) entry);
        else return;
        StringBuilder out = new StringBuilder();
        for (int one : locks) out.append(out.length() > 0 ? "," : "").append(one);
        store.keepNote("run_locks", out.toString());
    }

    // What can be sold

    /**
     * Whether a thing can be sold now. Gear in the bag that is not worn, not the main or secondary weapon, not locked; a curio she has
     * more of than she carries, and that is not locked. Her backpack's own weapons never.
     */
    static boolean sellable(Unlocks.Store store, int entry) {
        if (entry == 0 || RunInventory.plain(entry) || value(entry) == 0 || locked(store, entry)) return false;
        if (RunArtifacts.is(entry)) {
            int id = RunArtifacts.idOf(entry);
            int has = RunArtifacts.counts(store)[id];
            int carrying = RunArtifacts.carried(store).contains(id) ? 1 : 0;
            return has - carrying > 0;
        }
        List<Integer> bag = RunLoot.bag(store.note("run_bag"));
        int copies = RunInventory.count(bag, entry);
        if (copies == 0) return false;
        // (A thing she wears or holds is kept: one copy of it stays.)
        int inUse = RunInventory.slotOf(store, entry) >= 0 ? 1 : 0;
        return copies - inUse > 0;
    }

    /** Whether it is junk, for "sell all junk": sellable and plain enough to go without being asked about. */
    static boolean junk(Unlocks.Store store, int entry) {
        if (!sellable(store, entry)) return false;
        if (RunArtifacts.is(entry)) return RunArtifacts.rarity(RunArtifacts.idOf(entry)) <= JUNK_CURIO;
        return RunLoot.rarity(entry) <= JUNK_GEAR && !RunLoot.ultimate(entry) && !RunLoot.addition(entry);
    }

    /** What selling one of a thing brings; does it (takes it from the bag or the pouch) and returns the pluses, or 0 when it cannot be sold. */
    static int sell(Unlocks.Store store, int entry, int bonusPercent) {
        if (!sellable(store, entry)) return 0;
        int pay = price(entry, bonusPercent);
        if (RunArtifacts.is(entry)) {
            RunArtifacts.add(store, RunArtifacts.idOf(entry), -1);
        } else {
            List<Integer> bag = RunLoot.bag(store.note("run_bag"));
            bag.remove((Integer) entry);
            store.keepNote("run_bag", RunLoot.note(bag));
        }
        return pay;
    }

    /** How many of a thing she could sell now: its copies, less the one she wears or carries, and none of it if it is locked. */
    static int copies(Unlocks.Store store, int entry) {
        if (entry == 0 || RunInventory.plain(entry) || value(entry) == 0 || locked(store, entry)) return 0;
        if (RunArtifacts.is(entry)) {
            int id = RunArtifacts.idOf(entry);
            return Math.max(0, RunArtifacts.counts(store)[id] - (RunArtifacts.carried(store).contains(id) ? 1 : 0));
        }
        return Math.max(0, RunInventory.count(RunLoot.bag(store.note("run_bag")), entry) - (RunInventory.slotOf(store, entry) >= 0 ? 1 : 0));
    }

    /** What selling every piece of junk would bring, and how many pieces that is. */
    static int[] junkTotal(Unlocks.Store store, int bonusPercent) {
        int sum = 0, pieces = 0;
        for (int entry : sellList(store)) {
            if (!junk(store, entry)) continue;
            int n = copies(store, entry);
            sum += n * price(entry, bonusPercent);
            pieces += n;
        }
        return new int[]{sum, pieces};
    }

    /** Sells all the junk. Returns the pluses and the number of pieces sold. */
    static int[] sellJunk(Unlocks.Store store, int bonusPercent) {
        int sum = 0, pieces = 0;
        for (int entry : sellList(store)) {
            while (junk(store, entry)) {
                sum += sell(store, entry, bonusPercent);
                pieces++;
            }
        }
        return new int[]{sum, pieces};
    }

    /** Every distinct thing there is to sell: the bag's gear, then the curios she has. */
    static List<Integer> sellList(Unlocks.Store store) {
        List<Integer> out = new ArrayList<>();
        for (int code : RunLoot.bag(store.note("run_bag"))) if (!out.contains(code)) out.add(code);
        int[] counts = RunArtifacts.counts(store);
        for (int id = 0; id < RunArtifacts.COUNT; id++) if (counts[id] > 0) out.add(RunArtifacts.entry(id));
        return out;
    }

    // The elf merchant

    /** How many curios, and how many rare gimmicks besides, she has on her stall at a time. */
    static final int STOCK = 6, RARE_STOCK = 2;

    /** What it costs to buy a curio from her: more than she pays for it, less her friend's discount (the haggle of what is carried). */
    static int buyPrice(int id, int haggleBonus) {
        float discount = Math.min(RunArtifacts.HAGGLE_MOST, Math.max(0, haggleBonus)) / 100f;
        return Math.max(1, Math.round(RunArtifacts.value(id) * MARKUP * (1 - discount)));
    }

    /**
     * Her stock for a day: six curios (the lower rarities, mostly) and two rarer gimmicks, the same for the same day and her stall number,
     * different on another. Rare ones are what a good plan saves for.
     */
    static int[] stock(long day, int stall) {
        Random random = new Random(day * 7919L + stall * 104729L + 17);
        int[] out = new int[STOCK + RARE_STOCK];
        int filled = 0;
        while (filled < STOCK) {
            int rarity = random.nextInt(100) < 70 ? random.nextInt(2) : 2;
            int id = RunArtifacts.pick(rarity, random.nextInt(1000));
            if (!has(out, filled, id)) out[filled++] = id;
        }
        while (filled < STOCK + RARE_STOCK) {
            int id = RunArtifacts.pick(random.nextInt(100) < 80 ? 3 : 4, random.nextInt(1000));
            // (Rare ones all have something to do, never "just a curio".)
            if (!has(out, filled, id) && RunArtifacts.perk(id) != RunArtifacts.NO_PERK) out[filled++] = id;
        }
        return out;
    }

    private static boolean has(int[] list, int count, int id) {
        for (int index = 0; index < count; index++) if (list[index] == id) return true;
        return false;
    }
}
