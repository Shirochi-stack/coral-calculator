package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Artifacts: the curios found in the lands (in chests, in hideouts and dungeons, from those who wait, and now and then from an ordinary
 * creature). Each has a picture, a flavour line, a rarity and a sell value; some do a small thing while they are carried (up to
 * {@link #POUCH} at once): pluses that come to her from further off, a pet-sized helper that fetches them, footsteps that leave flowers,
 * a better price at the elf merchant's. Most perks are the game's own effects ({@link RunLoot}'s) at small figures, so the rules that
 * already read them need no change; the rest are read from here.
 *
 * <p>What is kept: {@code run_curios} (id:count pairs), {@code run_carry} (the ids in the pouch), {@code run_locks} (entries that are
 * never sold, with {@link RunEconomy}). A curio is, as a drop or a cell of the inventory, an "entry": {@link RunLoot#ARTIFACT} and its id.
 */
final class RunArtifacts {
    private RunArtifacts() {}

    /** Where a curio's picture is among the loot icon numbers: from here up they are in the economy's own sheet ({@link RunEconomyArt}). */
    static final int ICON_BASE = 10000;
    /** How many she can carry at once, so that a hoard does not stack. */
    static final int POUCH = 3;
    /** The perks that are not one of {@link RunLoot}'s effects. */
    static final int NO_PERK = -1, SELL = 100, HAGGLE = 101, FLOWERS = 102, FETCH = 103, LUCKY = 104;
    /** The most of the shop perks that can add up. */
    static final int SELL_MOST = 20, HAGGLE_MOST = 20;

    /** Name, flavour line, rarity, sell value, perk, figure. */
    private static final Object[][] TABLE = {
            // Common
            {"Pressed-Flower Bookmark", "Still smells faintly of the page it kept.", 0, 8, FLOWERS, 0},
            {"Chewed Pencil Stub", "Proofread with someone's teeth.", 0, 5, NO_PERK, 0},
            {"Cracked Monocle", "Makes everything look slightly suspicious. It usually is.", 0, 12, RunLoot.CRIT, 2},
            {"Dog-Eared Receipt", "Proof that she once paid for something. Nobody remembers what.", 0, 6, NO_PERK, 0},
            {"Lopsided Paperclip", "Attracts mostly lint, and the odd plus.", 0, 9, RunLoot.MAGNET, 4},
            {"Sticky Note of Doom", "'Remember to' and then the rest has faded.", 0, 7, NO_PERK, 0},
            {"Faded Library Stamp", "OVERDUE, in letters that have given up.", 0, 10, NO_PERK, 0},
            {"Mismatched Button", "From a coat that no longer exists.", 0, 6, NO_PERK, 0},
            {"Pocket-Lint Pearl", "A tiny wonder, if you are very tired.", 0, 14, SELL, 3},
            {"Bent Hairpin", "It has picked one lock and one sandwich.", 0, 8, NO_PERK, 0},
            {"One-Note Tin Whistle", "The note is a complaint.", 0, 11, NO_PERK, 0},
            {"Empty Sealed Envelope", "Sealed with great confidence.", 0, 13, HAGGLE, 3},
            // Fine
            {"Brass Page-Turner", "It turns pages that do not want turning.", 1, 28, RunLoot.HASTE, 3},
            {"Lucky Thimble", "Dented by three needles and one dragon.", 1, 32, RunLoot.DODGE, 3},
            {"Moth-Eaten Bookplate", "Ex libris: someone who ran out of shelf.", 1, 24, NO_PERK, 0},
            {"Felt Bookworm", "A little fetcher. It brings back pluses, and now and then a crumb.", 1, 30, FETCH, 1},
            {"Spectacles of Small Print", "Everything becomes legible, including regret.", 1, 38, RunLoot.CRIT, 4},
            {"Penny-Plain Stamp", "Posted nowhere, delivered everywhere.", 1, 26, SELL, 4},
            {"Candle End", "It burns with a flame this tall. Always.", 1, 22, RunLoot.REGEN, 2},
            {"Marbled Endpaper Scrap", "Pretty enough to be a map of somewhere.", 1, 35, RunLoot.PLUSES, 3},
            {"Ribbon of Many Bookmarks", "Every one of them marks a different last page.", 1, 40, RunLoot.MAGNET, 8},
            {"Slightly Crushed Paper Crane", "It still thinks it can fly.", 1, 45, LUCKY, 3},
            {"Left-Handed Quill Nib", "It writes mirror-wise, and no one has complained.", 1, 29, NO_PERK, 0},
            {"Compass That Points to Snacks", "Never wrong, rarely helpful.", 1, 42, HAGGLE, 5},
            // Rare
            {"Inkwell of the First Scribe", "The ink inside has been wet for a thousand years and has opinions.", 2, 140, RunLoot.POWER, 5},
            {"Locket of Marginalia", "Notes in a hand that argues back.", 2, 110, RunLoot.LEECH, 2},
            {"Clockwork Page-Mouse", "It winds itself up when no one is reading. It fetches pluses.", 2, 120, FETCH, 2},
            {"Glass Ink Bead", "A dark little sky with one star in it.", 2, 90, RunLoot.PLUSES, 5},
            {"Lamp of Late Chapters", "It burns brightest at three in the morning.", 2, 130, RunLoot.ENERGY, 15},
            {"Stitched Silk Bookmark", "It slips out of the way of things, and so does she.", 2, 105, RunLoot.DODGE, 5},
            {"Gilded Index Tab", "A merchant will always say it is worth more.", 2, 95, SELL, 6},
            {"Archive Reading Glass", "It finds the weak line of every sentence.", 2, 150, RunLoot.CRIT, 6},
            {"Pocket Atlas of Nowhere", "Every page is the same road, and it leads here.", 2, 85, RunLoot.MAGNET, 15},
            {"Unsent Love Letter", "Tied in ribbon. Merchants are soft about these.", 2, 160, HAGGLE, 8},
            {"Snuffed Moon in a Jar", "Her footsteps leave flowers, and they glow a little.", 2, 100, FLOWERS, 0},
            {"Rattling Jar of Commas", "Shake it and everything slows down, for a breath.", 2, 75, RunLoot.HASTE, 5},
            // Prized
            {"Crown of Footnotes", "Heavy with qualifications.", 3, 300, RunLoot.POWER, 8},
            {"Phoenix-Feather Quill", "It rewrites a small hurt, now and then.", 3, 380, RunLoot.REGEN, 4},
            {"Hourglass of Borrowed Time", "The sand runs up when no one is looking.", 3, 320, RunLoot.HASTE, 8},
            {"Honest Mirror Shard", "It shows the blow before it lands.", 3, 260, RunLoot.DODGE, 8},
            {"Ledger of Small Mercies", "Every plus is entered twice, in her favour.", 3, 410, RunLoot.PLUSES, 10},
            {"Silver Bell for Lost Things", "It rings toward whatever you dropped.", 3, 340, RunLoot.MAGNET, 25},
            {"Wolf-Fang Seal", "A stamp that bites back.", 3, 230, RunLoot.LEECH, 3},
            // Mythic
            {"The Last Blank Page", "Blank on purpose. Everyone is afraid to start.", 4, 700, RunLoot.PLUSES, 15},
            {"Heart of the Library", "It beats once for every book that was never returned.", 4, 850, RunLoot.REGEN, 6},
            {"Star-Map of the Unwritten", "The stars on it are sentences.", 4, 780, RunLoot.POWER, 12},
            {"Sleeping Golden Bookworm", "Do not wake it. It is very good at finding things.", 4, 1000, LUCKY, 8},
            {"Halgakos's First Pressed Plus", "She keeps it flat in a book. It knows.", 5, 1500, RunLoot.PLUSES, 20}};

    static final int COUNT = TABLE.length;

    // What each is

    static String name(int id) { return (String) TABLE[id][0]; }
    static String flavour(int id) { return (String) TABLE[id][1]; }
    static int rarity(int id) { return (Integer) TABLE[id][2]; }
    static int value(int id) { return (Integer) TABLE[id][3]; }
    static int perk(int id) { return (Integer) TABLE[id][4]; }
    static int figure(int id) { return (Integer) TABLE[id][5]; }
    /** Which picture in the curios sheet (eight to a row, 128 px): its own place. */
    static int icon(int id) { return id; }

    /** The entry that stands for a curio in a cell of the inventory or as a drop, and back. */
    static int entry(int id) { return RunLoot.code(RunLoot.ARTIFACT, id / 64, id % 64, rarity(id)); }
    static boolean is(int entry) { return entry > 0 && RunLoot.type(entry) == RunLoot.ARTIFACT; }
    static int idOf(int entry) { return RunLoot.theme(entry) * 64 + RunLoot.base(entry); }

    /** What a perk says, in words. */
    static String perkWords(int id) {
        int perk = perk(id), figure = figure(id);
        if (perk == NO_PERK) return "No gimmick: just a curio. It sells.";
        switch (perk) {
            case SELL: return "Gimmick: merchants pay " + figure + "% more for what she sells";
            case HAGGLE: return "Gimmick: the elf merchant's prices are " + figure + "% lower";
            case FLOWERS: return "Gimmick: her footsteps leave flowers";
            case FETCH: return "Gimmick: a little helper fetches pluses from " + figure + " squares further";
            case LUCKY: return "Gimmick: " + figure + "% more of what creatures drop";
            default: return "Gimmick: " + RunLoot.said(perk, figure(id)).toLowerCase(java.util.Locale.ROOT);
        }
    }

    // What is kept

    /** How many of each she has: a count for every id. */
    static int[] counts(Unlocks.Store store) {
        int[] counts = new int[COUNT];
        String note = store.note("run_curios");
        if (note == null || note.isEmpty()) return counts;
        for (String pair : note.split(",")) {
            String[] part = pair.split(":");
            try {
                int id = Integer.parseInt(part[0].trim());
                if (id >= 0 && id < COUNT) counts[id] = Math.max(0, Integer.parseInt(part[1].trim()));
            } catch (RuntimeException ignored) {
                // (A broken pair is dropped.)
            }
        }
        return counts;
    }

    static String note(int[] counts) {
        StringBuilder out = new StringBuilder();
        for (int id = 0; id < COUNT; id++) {
            if (counts[id] <= 0) continue;
            if (out.length() > 0) out.append(',');
            out.append(id).append(':').append(counts[id]);
        }
        return out.toString();
    }

    static void add(Unlocks.Store store, int id, int number) {
        int[] counts = counts(store);
        counts[id] = Math.max(0, counts[id] + number);
        store.keepNote("run_curios", note(counts));
        // (What was carried and is no more is not carried.)
        List<Integer> carry = carried(store);
        if (carry.removeIf(one -> counts[one] <= 0)) store.keepNote("run_carry", join(carry));
    }

    static int total(Unlocks.Store store) {
        int total = 0;
        for (int count : counts(store)) total += count;
        return total;
    }

    /** The ids in her pouch, in the order they went in; only those she still has. */
    static List<Integer> carried(Unlocks.Store store) {
        List<Integer> out = new ArrayList<>();
        String note = store.note("run_carry");
        int[] counts = counts(store);
        if (note != null && !note.isEmpty()) {
            for (String one : note.split(",")) {
                try {
                    int id = Integer.parseInt(one.trim());
                    if (id >= 0 && id < COUNT && counts[id] > 0 && !out.contains(id) && out.size() < POUCH) out.add(id);
                } catch (NumberFormatException ignored) {
                    // (Dropped.)
                }
            }
        }
        return out;
    }

    private static String join(List<Integer> ids) {
        StringBuilder out = new StringBuilder();
        for (int id : ids) out.append(out.length() > 0 ? "," : "").append(id);
        return out.toString();
    }

    /** Puts a curio in her pouch, or takes it out. Whether the pouch changed: it does not when the pouch is full. */
    static boolean carry(Unlocks.Store store, int id, boolean on) {
        List<Integer> carry = carried(store);
        if (on) {
            if (carry.contains(id) || carry.size() >= POUCH || counts(store)[id] <= 0) return false;
            carry.add(id);
        } else if (!carry.remove((Integer) id)) {
            return false;
        }
        store.keepNote("run_carry", join(carry));
        refresh(store);
        return true;
    }

    // What carrying does

    /** The effects of what is carried, added up, for the rules to read: set by {@link #refresh} when she starts or changes the pouch. */
    private static final int[] AMBIENT = new int[RunLoot.EFFECTS];
    private static int sellBonus, haggle, lucky;
    private static boolean flowers;

    static void refresh(Unlocks.Store store) { refresh(carried(store)); }

    static void refresh(List<Integer> carry) {
        java.util.Arrays.fill(AMBIENT, 0);
        sellBonus = haggle = lucky = 0;
        flowers = false;
        for (int id : carry) {
            int perk = perk(id), figure = figure(id);
            if (perk == NO_PERK) continue;
            if (perk >= 0 && perk < RunLoot.EFFECTS) AMBIENT[perk] += figure;
            else if (perk == FETCH) AMBIENT[RunLoot.MAGNET] += 12 * figure;
            else if (perk == SELL) sellBonus += figure;
            else if (perk == HAGGLE) haggle += figure;
            else if (perk == LUCKY) lucky += figure;
            else if (perk == FLOWERS) flowers = true;
        }
        sellBonus = Math.min(SELL_MOST, sellBonus);
        haggle = Math.min(HAGGLE_MOST, haggle);
    }

    /** How much of a game effect what is carried gives (the rules add it to what she wears). */
    static int effect(int effect) { return effect >= 0 && effect < AMBIENT.length ? AMBIENT[effect] : 0; }
    /** The percent more she is paid when she sells; the percent off at the elf merchant's; the percent more that creatures drop. */
    static int sellBonus() { return sellBonus; }
    static int haggle() { return haggle; }
    static int lucky() { return lucky; }
    /** Whether her footsteps leave flowers. */
    static boolean flowers() { return flowers; }

    // What is found

    /** The odds in a hundred of each rarity, from a creature, a chest, and from those who wait, a hideout or a dungeon. */
    static final int CREATURE = 0, CHEST = 1, GREAT = 2;
    private static final int[][] ODDS = {{62, 28, 8, 2, 0, 0}, {46, 32, 16, 5, 1, 0}, {0, 22, 40, 28, 8, 2}};

    /** How likely a find from a creature is, in ten thousand, for an ordinary one and for a boss: rare, and a little likelier in later lands. */
    static int creatureChance(boolean boss, int land) { return boss ? 800 : 60 + Math.min(80, land) / 2; }

    /** The entry of a curio found, by a roll from 0 to 99 (rarity) and one from 0 to anything (which of that rarity). */
    static int found(int source, int rarityRoll, int pickRoll) {
        int[] odds = ODDS[source];
        int sum = 0, rarity = 0;
        for (int index = 0; index < odds.length; index++) {
            sum += odds[index];
            if (rarityRoll % 100 < sum) {
                rarity = index;
                break;
            }
        }
        return entry(pick(rarity, pickRoll));
    }

    /** Which curio of a rarity (or, if there is none, of the nearest below). */
    static int pick(int rarity, int roll) {
        for (int r = rarity; r >= 0; r--) {
            List<Integer> of = new ArrayList<>();
            for (int id = 0; id < COUNT; id++) if (rarity(id) == r) of.add(id);
            if (!of.isEmpty()) return of.get(Math.floorMod(roll, of.size()));
        }
        return 0;
    }

    /** A curio for tests and the merchant: from a seeded random. */
    static int random(int source, Random random) { return idOf(found(source, random.nextInt(100), random.nextInt(1000))); }
}
