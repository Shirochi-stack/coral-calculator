package com.coral.calculator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** The selling economy: curios, what gear sells for, junk and locks, the elf merchant's stock and prices, and what is kept. */
public class RunEconomyTest {
    private static final class Kept implements Unlocks.Store {
        final Map<String, String> notes = new HashMap<>();
        final Map<String, Integer> numbers = new HashMap<>();

        @Override public int tally(String key) {
            Integer value = numbers.get(key);
            return value == null ? 0 : value;
        }

        @Override public String note(String key) { return notes.get(key); }

        @Override public void keepNote(String key, String value) { notes.put(key, value); }

        Kept bag(int... codes) {
            List<Integer> bag = new ArrayList<>();
            for (int code : codes) bag.add(code);
            notes.put("run_bag", RunLoot.note(bag));
            return this;
        }
    }

    private static final int WHITE_SWORD = RunLoot.code(RunLoot.WEAPON, 1, 0, RunLoot.WHITE), BLUE_VEST = RunLoot.code(RunLoot.ARMOUR, 1, 1, RunLoot.BLUE),
            PURPLE_RING = RunLoot.code(RunLoot.ACCESSORY, 4, 4, RunLoot.PURPLE), GOLD_SWORD = RunLoot.code(RunLoot.WEAPON, 3, 0, RunLoot.GOLD),
            RED_COAT = RunLoot.code(RunLoot.ARMOUR, 5, 5, RunLoot.RED);

    // The curios

    @Test public void everyCurioHasANamePictureFlavourAndAFairValue() {
        HashSet<String> names = new HashSet<>();
        int[] lowest = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE}, highest = new int[6];
        for (int id = 0; id < RunArtifacts.COUNT; id++) {
            assertTrue(names.add(RunArtifacts.name(id)));
            assertTrue(RunArtifacts.flavour(id).length() > 10);
            assertTrue(RunArtifacts.value(id) > 0);
            int rarity = RunArtifacts.rarity(id);
            lowest[rarity] = Math.min(lowest[rarity], RunArtifacts.value(id));
            highest[rarity] = Math.max(highest[rarity], RunArtifacts.value(id));
            // The entry that stands for it reads back as it, and is not gear.
            int entry = RunArtifacts.entry(id);
            assertTrue(RunArtifacts.is(entry));
            assertEquals(id, RunArtifacts.idOf(entry));
            assertEquals(rarity, RunLoot.rarity(entry));
            assertEquals(RunArtifacts.ICON_BASE + id, RunLoot.iconOf(entry));
            assertEquals(RunArtifacts.name(id), RunLoot.name(entry));
        }
        assertEquals(48, RunArtifacts.COUNT);
        // The rarer, the more: each rarity's cheapest is above the last one's dearest (the top two have one or four each).
        for (int rarity = 1; rarity < 6; rarity++) assertTrue("rarity " + rarity, lowest[rarity] > highest[rarity - 1]);
        // There are some of every rarity from common to the rose, and enough gimmicks.
        int gimmicks = 0;
        for (int id = 0; id < RunArtifacts.COUNT; id++) if (RunArtifacts.perk(id) != RunArtifacts.NO_PERK) gimmicks++;
        assertTrue(gimmicks >= 30);
        for (int rarity = 0; rarity < 6; rarity++) assertTrue(highest[rarity] > 0);
    }

    @Test public void findsComeInTheirRaritiesAndNothingCommonFromTheGreat() {
        Random random = new Random(7);
        int[] seen = new int[6];
        for (int count = 0; count < 4000; count++) seen[RunLoot.rarity(RunArtifacts.found(RunArtifacts.GREAT, random.nextInt(100), random.nextInt(1000)))]++;
        assertEquals(0, seen[0]);
        assertTrue(seen[2] > seen[1] && seen[3] > 600);
        int[] chest = new int[6];
        for (int count = 0; count < 4000; count++) chest[RunLoot.rarity(RunArtifacts.found(RunArtifacts.CHEST, random.nextInt(100), random.nextInt(1000)))]++;
        assertTrue(chest[0] > chest[1] && chest[1] > chest[2] && chest[2] > chest[3]);
    }

    @Test public void theCuriosAreKeptCarriedAndLimited() {
        Kept kept = new Kept();
        RunArtifacts.add(kept, 4, 2);
        RunArtifacts.add(kept, 24, 1);
        assertEquals(2, RunArtifacts.counts(kept)[4]);
        assertEquals(3, RunArtifacts.total(kept));
        assertTrue(RunArtifacts.carry(kept, 4, true));
        assertTrue(RunArtifacts.carry(kept, 24, true));
        assertFalse("not one she does not have", RunArtifacts.carry(kept, 30, true));
        RunArtifacts.add(kept, 6, 1);
        RunArtifacts.add(kept, 40, 1);
        assertTrue(RunArtifacts.carry(kept, 6, true));
        assertFalse("a pouch of three", RunArtifacts.carry(kept, 40, true));
        assertEquals(RunArtifacts.POUCH, RunArtifacts.carried(kept).size());
        // What she carries does what it says: Inkwell of the First Scribe's power, the paperclip's magnet, the Stitched Silk's dodge...
        assertEquals(RunArtifacts.figure(24), RunArtifacts.effect(RunLoot.POWER));
        assertEquals(RunArtifacts.figure(4), RunArtifacts.effect(RunLoot.MAGNET));
        // A curio sold away is out of the pouch.
        RunArtifacts.add(kept, 24, -1);
        assertFalse(RunArtifacts.carried(kept).contains(24));
        assertTrue(RunArtifacts.carry(kept, 40, true));
        RunArtifacts.refresh(new ArrayList<Integer>());
        assertEquals(0, RunArtifacts.effect(RunLoot.POWER));
    }

    @Test public void theShopPerksAreCappedSoThereIsNoProfitInBuyingAndSellingBack() {
        // Every sell and haggle curio at once.
        List<Integer> every = new ArrayList<>();
        for (int id = 0; id < RunArtifacts.COUNT; id++) if (RunArtifacts.perk(id) == RunArtifacts.SELL || RunArtifacts.perk(id) == RunArtifacts.HAGGLE) every.add(id);
        RunArtifacts.refresh(every);
        // (Together they come to less than the caps, which only keep any future ones in check.)
        int sells = 0, haggles = 0;
        for (int id : every) {
            if (RunArtifacts.perk(id) == RunArtifacts.SELL) sells += RunArtifacts.figure(id);
            else haggles += RunArtifacts.figure(id);
        }
        assertEquals(Math.min(RunArtifacts.SELL_MOST, sells), RunArtifacts.sellBonus());
        assertEquals(Math.min(RunArtifacts.HAGGLE_MOST, haggles), RunArtifacts.haggle());
        for (int id = 0; id < RunArtifacts.COUNT; id++) {
            int buy = RunEconomy.buyPrice(id, RunArtifacts.haggle()), sell = RunEconomy.price(RunArtifacts.entry(id), RunArtifacts.sellBonus());
            assertTrue(RunArtifacts.name(id) + ": buys " + buy + ", sells " + sell, buy > sell * 1.3f);
        }
        RunArtifacts.refresh(new ArrayList<Integer>());
        assertEquals(0, RunArtifacts.sellBonus());
    }

    // Selling

    @Test public void gearSellsForLessThanTheCounterAsksAndMoreTheRarerItIs() {
        int white = RunEconomy.value(WHITE_SWORD), blue = RunEconomy.value(BLUE_VEST), purple = RunEconomy.value(PURPLE_RING), gold = RunEconomy.value(GOLD_SWORD),
                red = RunEconomy.value(RED_COAT);
        assertTrue(white < blue && blue < purple && purple < gold && gold < red);
        assertTrue("junk is a few pluses", white <= 10 && blue <= 20);
        // The cheapest thing at the counter is 70: no single ordinary find pays for it, so selling is a seasoning.
        assertTrue(purple < 70);
        assertEquals(0, RunEconomy.value(RunInventory.plainOf(PlusRun.SWORD)));
        assertEquals(0, RunEconomy.value(RunLoot.QUILL));
        // Paper is worth half.
        assertTrue(RunEconomy.value(RunLoot.code(RunLoot.WEAPON, RunLoot.PAPER, 0, RunLoot.WHITE)) < white);
        // A bonus pays more, but never more than the cap.
        assertEquals(Math.round(gold * 1.2f), RunEconomy.price(GOLD_SWORD, 20));
        assertEquals(RunEconomy.price(GOLD_SWORD, 20), RunEconomy.price(GOLD_SWORD, 90));
    }

    @Test public void whatIsWornHeldOrLockedIsNotSold() {
        Kept kept = new Kept().bag(WHITE_SWORD, WHITE_SWORD, BLUE_VEST, PURPLE_RING);
        kept.notes.put("run_worn", RunLoot.note(new RunLoot.Worn(BLUE_VEST, 0, 0)));
        assertFalse("worn: its only copy stays", RunEconomy.sellable(kept, BLUE_VEST));
        assertTrue(RunEconomy.sellable(kept, WHITE_SWORD));
        assertFalse("her own backpack weapons never", RunEconomy.sellable(kept, RunInventory.plainOf(PlusRun.TABLET)));
        assertFalse("what she has not", RunEconomy.sellable(kept, GOLD_SWORD));
        RunEconomy.lock(kept, WHITE_SWORD, true);
        assertTrue(RunEconomy.locked(kept, WHITE_SWORD));
        assertFalse(RunEconomy.sellable(kept, WHITE_SWORD));
        assertEquals(0, RunEconomy.sell(kept, WHITE_SWORD, 0));
        RunEconomy.lock(kept, WHITE_SWORD, false);
        assertTrue(RunEconomy.sellable(kept, WHITE_SWORD));
        // Selling takes one from the bag and pays for it.
        int pay = RunEconomy.sell(kept, WHITE_SWORD, 0);
        assertEquals(RunEconomy.value(WHITE_SWORD), pay);
        assertEquals(1, RunInventory.count(RunLoot.bag(kept.note("run_bag")), WHITE_SWORD));
        assertEquals(3, RunLoot.bag(kept.note("run_bag")).size());
    }

    @Test public void sellingAllJunkTakesOnlyJunk() {
        Kept kept = new Kept().bag(WHITE_SWORD, WHITE_SWORD, BLUE_VEST, PURPLE_RING, GOLD_SWORD, RED_COAT);
        kept.notes.put("run_worn", RunLoot.note(new RunLoot.Worn(0, PURPLE_RING, 0)));
        RunArtifacts.add(kept, 1, 3);   // common pencil: junk
        RunArtifacts.add(kept, 24, 1);  // rare inkwell: not junk
        RunEconomy.lock(kept, BLUE_VEST, true);
        int[] promised = RunEconomy.junkTotal(kept, 0);
        int[] done = RunEconomy.sellJunk(kept, 0);
        assertEquals(promised[0], done[0]);
        assertEquals(promised[1], done[1]);
        // The two white swords and the three pencils (not the locked blue vest, the worn ring, gold, red or the rare curio).
        assertEquals(5, done[1]);
        assertEquals(2 * RunEconomy.value(WHITE_SWORD) + 3 * RunArtifacts.value(1), done[0]);
        List<Integer> left = RunLoot.bag(kept.note("run_bag"));
        assertTrue(left.contains(BLUE_VEST) && left.contains(PURPLE_RING) && left.contains(GOLD_SWORD) && left.contains(RED_COAT));
        assertFalse(left.contains(WHITE_SWORD));
        assertEquals(0, RunArtifacts.counts(kept)[1]);
        assertEquals(1, RunArtifacts.counts(kept)[24]);
        assertEquals(0, RunEconomy.sellJunk(kept, 0)[1]);
    }

    @Test public void aCarriedCurioIsNotSoldAndTheSpareIs() {
        Kept kept = new Kept();
        RunArtifacts.add(kept, 8, 2);
        assertTrue(RunArtifacts.carry(kept, 8, true));
        assertTrue("the spare", RunEconomy.sellable(kept, RunArtifacts.entry(8)));
        assertEquals(RunArtifacts.value(8), RunEconomy.sell(kept, RunArtifacts.entry(8), 0));
        assertFalse("the one in the pouch stays", RunEconomy.sellable(kept, RunArtifacts.entry(8)));
        RunArtifacts.carry(kept, 8, false);
        assertTrue(RunEconomy.sellable(kept, RunArtifacts.entry(8)));
    }

    // The elf merchant

    @Test public void herStockIsTheSameForADayAndDifferentOnAnother() {
        int[] one = RunEconomy.stock(20000, 3), same = RunEconomy.stock(20000, 3), next = RunEconomy.stock(20001, 3);
        assertArrayEquals(one, same);
        assertFalse(java.util.Arrays.equals(one, next));
        assertEquals(RunEconomy.STOCK + RunEconomy.RARE_STOCK, one.length);
        HashSet<Integer> distinct = new HashSet<>();
        for (int day = 0; day < 200; day++) {
            int[] stock = RunEconomy.stock(day, 1);
            distinct.clear();
            for (int index = 0; index < stock.length; index++) {
                assertTrue(distinct.add(stock[index]));
                // The last two are the rare gimmicks.
                if (index >= RunEconomy.STOCK) assertTrue(RunArtifacts.rarity(stock[index]) >= 3 && RunArtifacts.perk(stock[index]) != RunArtifacts.NO_PERK);
                else assertTrue(RunArtifacts.rarity(stock[index]) <= 2);
            }
        }
    }

    // Balance

    @Test public void aFairPlayerSellsAboutAFifthOfAWhatALandGaveAndNeverMore() {
        // What a land gives her in pluses is in the order of a hundred and a half: forty creatures of two to five pluses, a boss, some chests.
        // What she can expect to sell of what it drops (curios by their chances, junk gear by its):
        int land = 30;
        double landIncome = 40 * 3.2 + 40 + 3 * 10;
        double curios = 40 * RunArtifacts.creatureChance(false, land) / 10000.0 * average(RunArtifacts.CREATURE) + RunArtifacts.creatureChance(true, land) / 10000.0 * average(RunArtifacts.GREAT)
                + 3 * (.12 * average(RunArtifacts.CHEST));
        // Gear: about one white-or-blue find a land (the chance of one is in RunLoot.gearChance), selling for the junk price.
        double gear = 1 * (RunEconomy.value(WHITE_SWORD) * .6 + RunEconomy.value(BLUE_VEST) * .4);
        double sold = curios + gear;
        assertTrue("sold " + sold + " of " + landIncome, sold > landIncome * .05 && sold < landIncome * .35);
        // And a rare gimmick at the merchant costs several lands' selling, so saving up for it is a plan, not an accident.
        int[] stock = RunEconomy.stock(5, 1);
        for (int index = RunEconomy.STOCK; index < stock.length; index++) assertTrue(RunEconomy.buyPrice(stock[index], 0) > sold * 2);
    }

    private static double average(int source) {
        double sum = 0, weight = 0;
        for (int rarity = 0; rarity < 6; rarity++) {
            int chance = new int[][]{{62, 28, 8, 2, 0, 0}, {46, 32, 16, 5, 1, 0}, {0, 22, 40, 28, 8, 2}}[source][rarity];
            double value = 0;
            int count = 0;
            for (int id = 0; id < RunArtifacts.COUNT; id++) if (RunArtifacts.rarity(id) == rarity) {
                value += RunArtifacts.value(id);
                count++;
            }
            sum += chance * (count == 0 ? 0 : value / count);
            weight += chance;
        }
        return sum / weight;
    }

    // What is kept

    @Test public void whatIsKeptReadsBackAndSurvivesAMangledNote() {
        Kept kept = new Kept();
        RunArtifacts.add(kept, 3, 2);
        RunArtifacts.add(kept, 47, 1);
        assertEquals("3:2,47:1", kept.note("run_curios"));
        RunEconomy.lock(kept, RunArtifacts.entry(47), true);
        RunEconomy.lock(kept, GOLD_SWORD, true);
        assertEquals(2, RunEconomy.locks(kept).size());
        assertEquals(RunArtifacts.entry(47), (int) RunEconomy.locks(kept).get(0));
        kept.notes.put("run_curios", "3:2,zzz,99:4,7:-3,5:1");
        int[] counts = RunArtifacts.counts(kept);
        assertEquals(2, counts[3]);
        assertEquals(1, counts[5]);
        assertEquals(0, counts[7]);
        kept.notes.put("run_locks", "x,12,,4");
        assertEquals(2, RunEconomy.locks(kept).size());
        kept.notes.put("run_carry", "5,5,5,6,7,x");
        assertTrue(RunArtifacts.carried(kept).size() <= RunArtifacts.POUCH);
        assertNotEquals(RunArtifacts.entry(3), RunArtifacts.entry(4));
    }
}
