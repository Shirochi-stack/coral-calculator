package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Her inventory: the five places, the swap between the main and the secondary weapon, the order of the grid, and what was kept before it. */
public class RunInventoryTest {
    private static final class Kept implements Unlocks.Store {
        final Map<String, Integer> numbers = new HashMap<>();
        final Map<String, String> notes = new HashMap<>();

        @Override public int tally(String key) {
            Integer value = numbers.get(key);
            return value == null ? 0 : value;
        }

        @Override public String note(String key) { return notes.get(key); }

        @Override public void keepNote(String key, String value) { notes.put(key, value); }

        Kept with(String key, int value) {
            numbers.put(key, value);
            return this;
        }

        Kept bag(int... codes) {
            List<Integer> bag = new ArrayList<>();
            for (int code : codes) bag.add(code);
            notes.put("run_bag", RunLoot.note(bag));
            return this;
        }
    }

    private static final int BLUE_SWORD = RunLoot.code(RunLoot.WEAPON, 1, 0, RunLoot.BLUE), GOLD_SWORD = RunLoot.code(RunLoot.WEAPON, 2, 0, RunLoot.GOLD),
            RED_PEN = RunLoot.code(RunLoot.WEAPON, 1, RunLoot.PEN, RunLoot.RED), WHITE_VEST = RunLoot.code(RunLoot.ARMOUR, 1, 1, RunLoot.WHITE),
            RED_COAT = RunLoot.code(RunLoot.ARMOUR, 2, 5, RunLoot.RED), WHITE_COAT = RunLoot.code(RunLoot.ARMOUR, 1, 5, RunLoot.WHITE), BLUE_RING = RunLoot.code(RunLoot.ACCESSORY, 1, 2, RunLoot.BLUE),
            GOLD_RING = RunLoot.code(RunLoot.ACCESSORY, 3, 3, RunLoot.GOLD), PURPLE_RING = RunLoot.code(RunLoot.ACCESSORY, 4, 4, RunLoot.PURPLE);

    @Test public void theCodesAreWhatTheyAreMeantToBe() {
        assertEquals(RunLoot.WEAPON, RunLoot.type(BLUE_SWORD));
        assertEquals(RunLoot.WEAPON, RunLoot.type(RED_PEN));
        assertEquals(RunLoot.ARMOUR, RunLoot.type(WHITE_VEST));
        assertEquals(RunLoot.ACCESSORY, RunLoot.type(BLUE_RING));
        assertTrue(BLUE_SWORD != 0 && BLUE_SWORD < RunInventory.PLAIN);
        assertEquals(RunInventory.PLAIN + 3, RunInventory.plainOf(3));
        assertEquals(3, RunInventory.kindOf(RunInventory.plainOf(3)));
        assertEquals(RunArms.kindOf(BLUE_SWORD), RunInventory.kindOf(BLUE_SWORD));
    }

    // What was kept before there was an inventory becomes the main weapon, and there is no secondary one

    @Test public void aSaveFromBeforeHasHerTabletBookInHandAndNothingBesides() {
        Kept kept = new Kept();
        assertEquals(RunInventory.plainOf(PlusRun.TABLET), RunInventory.main(kept));
        assertEquals(0, RunInventory.second(kept));
        assertEquals(RunInventory.MAIN, RunInventory.slotOf(kept, RunInventory.plainOf(PlusRun.TABLET)));
    }

    @Test public void theChoiceOfWhatToBeginWithBecomesTheMainWeapon() {
        Kept kept = new Kept().with("run_mains", 3).with("run_main", 1);
        assertEquals(RunInventory.plainOf(PlusRun.KEYS), RunInventory.main(kept));
        // (one she has not got does not count)
        Kept not = new Kept().with("run_mains", 1).with("run_main", 1);
        assertEquals(RunInventory.plainOf(PlusRun.TABLET), RunInventory.main(not));
    }

    @Test public void aWeaponFoundThatWasInHandBecomesTheMainWeapon() {
        Kept kept = new Kept().with("run_mains", 3).with("run_main", 1).bag(BLUE_SWORD);
        kept.notes.put("run_hand", String.valueOf(BLUE_SWORD));
        assertEquals(BLUE_SWORD, RunInventory.main(kept));
        assertEquals(BLUE_SWORD, RunInventory.hand(kept));
        // (a thing that is not a weapon is not a weapon in hand)
        kept.notes.put("run_hand", String.valueOf(WHITE_VEST));
        assertEquals(0, RunInventory.hand(kept));
        kept.notes.put("run_hand", "not a number");
        assertEquals(0, RunInventory.hand(kept));
    }

    @Test public void theBackpackThatWasKeptIsTheKindsShown() {
        Kept kept = new Kept().with("run_pack", 1 << PlusRun.SWORD | 1 << PlusRun.GUN).with("run_mains", 3);
        int owned = RunInventory.owned(kept);
        assertTrue((owned >> PlusRun.SWORD & 1) != 0);
        assertTrue((owned >> PlusRun.GUN & 1) != 0);
        assertTrue((owned >> PlusRun.TABLET & 1) != 0);
        assertTrue((owned >> PlusRun.KEYS & 1) != 0);
        assertFalse((owned >> PlusRun.KATANA & 1) != 0);
    }

    // The places

    @Test public void theMainWeaponIsChosenAndTheOneItReplacesIsKeptNoWhere() {
        Kept kept = new Kept().with("run_pack", 1 << PlusRun.SWORD).bag(BLUE_SWORD);
        assertTrue(RunInventory.setMain(kept, BLUE_SWORD));
        assertEquals(BLUE_SWORD, RunInventory.main(kept));
        assertEquals(0, RunInventory.second(kept));
        assertFalse("it is main already", RunInventory.setMain(kept, BLUE_SWORD));
        assertTrue(RunInventory.setMain(kept, RunInventory.plainOf(PlusRun.TABLET)));
        assertEquals(RunInventory.plainOf(PlusRun.TABLET), RunInventory.main(kept));
        assertEquals("the weapon found is out of her hands", 0, RunInventory.hand(kept));
        assertFalse(RunInventory.setMain(kept, WHITE_VEST));
    }

    @Test public void aSecondaryWeaponIsPutInAndTakenOut() {
        Kept kept = new Kept().with("run_pack", 1 << PlusRun.SWORD).bag(RED_PEN);
        assertTrue(RunInventory.setSecond(kept, RED_PEN));
        assertEquals(RED_PEN, RunInventory.second(kept));
        assertEquals(RunInventory.SECOND, RunInventory.slotOf(kept, RED_PEN));
        assertFalse(RunInventory.setSecond(kept, RED_PEN));
        assertTrue(RunInventory.setSecond(kept, RunInventory.plainOf(PlusRun.SWORD)));
        assertEquals(RunInventory.plainOf(PlusRun.SWORD), RunInventory.second(kept));
        assertTrue(RunInventory.clearSecond(kept));
        assertEquals(0, RunInventory.second(kept));
        assertFalse(RunInventory.clearSecond(kept));
    }

    @Test public void theMainAndTheSecondaryChangePlacesWhenOneIsPutInTheOthers() {
        Kept kept = new Kept().with("run_pack", 1 << PlusRun.SWORD).bag(BLUE_SWORD);
        int tablet = RunInventory.plainOf(PlusRun.TABLET);
        RunInventory.setMain(kept, BLUE_SWORD);
        assertFalse("with no secondary there is nothing to change places with", RunInventory.setSecond(kept, BLUE_SWORD));
        RunInventory.setSecond(kept, tablet);
        assertEquals(BLUE_SWORD, RunInventory.main(kept));
        assertEquals(tablet, RunInventory.second(kept));
        // The secondary made the main one: the main one is the secondary now.
        assertTrue(RunInventory.setMain(kept, tablet));
        assertEquals(tablet, RunInventory.main(kept));
        assertEquals(BLUE_SWORD, RunInventory.second(kept));
        // And the same the other way.
        assertTrue(RunInventory.setSecond(kept, tablet));
        assertEquals(BLUE_SWORD, RunInventory.main(kept));
        assertEquals(tablet, RunInventory.second(kept));
    }

    @Test public void aSecondaryThatIsGoneFromTheBagOrTheBackpackIsNoSecondary() {
        Kept kept = new Kept().with("run_pack", 1 << PlusRun.SWORD).bag(RED_PEN);
        RunInventory.setSecond(kept, RED_PEN);
        kept.bag();
        assertEquals("the pen was honed or lost", 0, RunInventory.second(kept));
        kept.notes.put("run_second", String.valueOf(RunInventory.plainOf(PlusRun.KATANA)));
        assertEquals("a katana she has not got", 0, RunInventory.second(kept));
        kept.notes.put("run_second", String.valueOf(WHITE_VEST));
        assertEquals("an armour is no weapon", 0, RunInventory.second(kept));
        kept.notes.put("run_second", "x");
        assertEquals(0, RunInventory.second(kept));
    }

    @Test public void oneFoundWeaponIsInTwoPlacesOnlyWithTwoOfIt() {
        Kept kept = new Kept().bag(BLUE_SWORD);
        RunInventory.setMain(kept, BLUE_SWORD);
        kept.notes.put("run_second", String.valueOf(BLUE_SWORD));
        assertEquals(0, RunInventory.second(kept));
        kept.bag(BLUE_SWORD, BLUE_SWORD);
        assertEquals(BLUE_SWORD, RunInventory.second(kept));
    }

    @Test public void whatSheWearsGoesInTheRightPlace() {
        RunLoot.Worn none = RunLoot.NOTHING;
        RunLoot.Worn armoured = RunInventory.equip(none, WHITE_VEST);
        assertEquals(WHITE_VEST, armoured.armour);
        assertEquals(RED_COAT, RunInventory.equip(armoured, RED_COAT).armour);
        RunLoot.Worn one = RunInventory.equip(armoured, BLUE_RING);
        assertEquals(BLUE_RING, one.first);
        RunLoot.Worn two = RunInventory.equip(one, GOLD_RING);
        assertEquals(GOLD_RING, two.second);
        // With both full, the less rare of the two goes.
        RunLoot.Worn three = RunInventory.equip(two, PURPLE_RING);
        assertEquals(PURPLE_RING, three.first);
        assertEquals(GOLD_RING, three.second);
        // In a place that is asked for.
        assertEquals(BLUE_RING, RunInventory.equipAt(armoured, BLUE_RING, RunInventory.LAST).second);
        assertEquals(BLUE_RING, RunInventory.equipAt(two, BLUE_RING, RunInventory.FIRST).first);
        // Taking off.
        RunLoot.Worn off = RunInventory.unequip(three, RunInventory.ARMOUR);
        assertEquals(0, off.armour);
        assertEquals(PURPLE_RING, off.first);
        assertEquals(0, RunInventory.unequip(three, RunInventory.LAST).second);
        assertEquals(0, RunInventory.unequip(three, RunInventory.FIRST).first);
        // A weapon is not worn.
        assertEquals(none.armour, RunInventory.equip(none, BLUE_SWORD).armour);
    }

    @Test public void aThingIsWornNoMoreOftenThanSheHasIt() {
        List<Integer> bag = new ArrayList<>();
        bag.add(BLUE_RING);
        RunLoot.Worn none = RunLoot.NOTHING;
        assertTrue(RunInventory.canWear(none, bag, BLUE_RING));
        assertFalse(RunInventory.canWear(RunInventory.equip(none, BLUE_RING), bag, BLUE_RING));
        bag.add(BLUE_RING);
        assertTrue(RunInventory.canWear(RunInventory.equip(none, BLUE_RING), bag, BLUE_RING));
        assertFalse("a weapon is never worn", RunInventory.canWear(none, bag, BLUE_SWORD));
    }

    @Test public void theSlotsOfWhatIsWornAreReadBack() {
        Kept kept = new Kept().bag(WHITE_VEST, BLUE_RING, GOLD_RING);
        kept.notes.put("run_worn", RunLoot.note(new RunLoot.Worn(WHITE_VEST, BLUE_RING, GOLD_RING)));
        assertEquals(WHITE_VEST, RunInventory.inSlot(kept, RunInventory.ARMOUR));
        assertEquals(BLUE_RING, RunInventory.inSlot(kept, RunInventory.FIRST));
        assertEquals(GOLD_RING, RunInventory.inSlot(kept, RunInventory.LAST));
        assertEquals(RunInventory.ARMOUR, RunInventory.slotOf(kept, WHITE_VEST));
        assertEquals(RunInventory.LAST, RunInventory.slotOf(kept, GOLD_RING));
        assertEquals(-1, RunInventory.slotOf(kept, RED_COAT));
        assertEquals(-1, RunInventory.slotOf(kept, 0));
        assertEquals(5, RunInventory.SLOTS);
    }

    // The swap button

    private static PlusRun landWith(PlusRun.Perks perks) {
        RunLevels.Level level = new RunLevels.Level(10, 60);
        for (int c = 0; c < 60; c++) for (int r = 10; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 57;
        return new PlusRun(level, perks);
    }

    @Test public void theSwapButtonChangesTheMainWeaponForTheSecondOne() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.pack = 1 << PlusRun.SWORD | 1 << PlusRun.KNIFE;
        perks.main = PlusRun.TABLET;
        perks.second = PlusRun.SWORD;
        PlusRun run = landWith(perks);
        assertEquals(PlusRun.TABLET, run.weapon());
        assertEquals(PlusRun.SWORD, run.nextUp());
        assertTrue(run.swap());
        assertEquals(PlusRun.SWORD, run.weapon());
        assertEquals(PlusRun.TABLET, run.nextUp());
        assertTrue(run.swap());
        assertEquals(PlusRun.TABLET, run.weapon());
        // (and the knife she also has is not what a swap goes to)
        assertTrue(run.swap());
        assertTrue(run.swap());
        assertEquals(PlusRun.TABLET, run.weapon());
    }

    @Test public void withNoSecondaryTheSwapIsAsItWas() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.pack = 1 << PlusRun.SWORD;
        perks.main = PlusRun.TABLET;
        PlusRun run = landWith(perks);
        assertEquals(PlusRun.UNARMED, perks.second);
        assertEquals(PlusRun.SWORD, run.nextUp());
        assertTrue(run.swap());
        assertEquals(PlusRun.SWORD, run.weapon());
        assertEquals(PlusRun.TABLET, run.nextUp());
    }

    @Test public void theSecondaryWeaponIsHersFromTheStartOfALand() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        perks.second = PlusRun.KATANA;
        PlusRun run = landWith(perks);
        assertTrue(run.usable(PlusRun.KATANA));
        assertEquals(PlusRun.KATANA, run.nextUp());
    }

    @Test public void theFindInHandIsTheOneOfThePlaceHeldNow() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        perks.itemMain = 0;
        perks.itemSecond = BLUE_SWORD;
        perks.second = RunArms.kindOf(BLUE_SWORD);
        assertEquals(BLUE_SWORD, RunInventory.itemFor(perks, RunArms.kindOf(BLUE_SWORD)));
        // With the main weapon in hand, the find is not counted for it.
        assertEquals(0, RunInventory.itemFor(perks, PlusRun.TABLET));
        perks.itemMain = RED_PEN;
        assertEquals(RED_PEN, RunInventory.itemFor(perks, PlusRun.TABLET));
        assertEquals(BLUE_SWORD, RunInventory.itemFor(perks, RunArms.kindOf(BLUE_SWORD)));
        // Where nothing is in a place, what was set by hand stays.
        PlusRun.Perks plain = new PlusRun.Perks();
        plain.item = GOLD_SWORD;
        assertEquals(GOLD_SWORD, RunInventory.itemFor(plain, PlusRun.TABLET));
    }

    // The grid

    private static List<Integer> entries(List<RunInventory.Cell> cells) {
        List<Integer> out = new ArrayList<>();
        for (RunInventory.Cell cell : cells) out.add(cell.entry);
        return out;
    }

    @Test public void theTabsShowWhatTheyAreFor() {
        List<Integer> bag = new ArrayList<>();
        for (int code : new int[]{BLUE_SWORD, WHITE_VEST, BLUE_RING, RED_COAT}) bag.add(code);
        int owned = 1 << PlusRun.TABLET | 1 << PlusRun.SWORD;
        assertEquals(2 + 4, RunInventory.cells(owned, bag, RunInventory.ALL, RunInventory.BY_RARITY).size());
        assertEquals(2 + 1, RunInventory.cells(owned, bag, RunInventory.WEAPONS, RunInventory.BY_RARITY).size());
        List<Integer> armours = entries(RunInventory.cells(owned, bag, RunInventory.ARMOURS, RunInventory.BY_RARITY));
        assertEquals(2, armours.size());
        assertTrue(armours.contains(WHITE_VEST) && armours.contains(RED_COAT));
        List<Integer> accessories = entries(RunInventory.cells(owned, bag, RunInventory.ACCESSORIES, RunInventory.BY_RARITY));
        assertEquals(1, accessories.size());
        assertEquals(BLUE_RING, (int) accessories.get(0));
    }

    @Test public void byRarityTheRarestComesFirstAndOfOneRarityTheLatestFound() {
        List<Integer> bag = new ArrayList<>();
        for (int code : new int[]{WHITE_VEST, GOLD_RING, RED_COAT, BLUE_RING, BLUE_SWORD, PURPLE_RING}) bag.add(code);
        List<Integer> order = entries(RunInventory.cells(0, bag, RunInventory.ALL, RunInventory.BY_RARITY));
        int last = 99;
        for (int entry : order) {
            int rarity = RunInventory.rarity(entry);
            assertTrue("rarity goes down: " + order, rarity <= last);
            last = rarity;
        }
        assertEquals(RED_COAT, (int) order.get(0));
        assertEquals(WHITE_VEST, (int) order.get(order.size() - 1));
        // Of one rarity (blue: a sword and a ring), a weapon comes before an accessory.
        assertTrue(order.indexOf(BLUE_SWORD) < order.indexOf(BLUE_RING));
        // The rose is above the red.
        bag.add(RunLoot.code(RunLoot.ACCESSORY, 5, RunLoot.ROSE_OF_HALGAKOS, RunLoot.ROSE));
        assertEquals(RunLoot.ROSE, RunInventory.rarity(RunInventory.cells(0, bag, RunInventory.ALL, RunInventory.BY_RARITY).get(0).entry));
    }

    @Test public void byTypeWeaponsComeByClassThenArmourThenAccessories() {
        List<Integer> bag = new ArrayList<>();
        for (int code : new int[]{BLUE_RING, WHITE_COAT, RED_PEN, BLUE_SWORD, RED_COAT}) bag.add(code);
        int owned = 1 << PlusRun.TABLET | 1 << PlusRun.KNIFE;
        List<Integer> order = entries(RunInventory.cells(owned, bag, RunInventory.ALL, RunInventory.BY_TYPE));
        int lastType = -1, lastClass = -1;
        for (int entry : order) {
            int type = RunInventory.type(entry), cls = RunInventory.classOf(entry);
            assertTrue("type goes up: " + order, type >= lastType);
            if (type == lastType) assertTrue("class goes up within a type: " + order, cls >= lastClass);
            lastType = type;
            lastClass = cls;
        }
        // Blades (the sword), then tomes (the tablet-book), then throwing (the pen and the knife).
        assertTrue(order.indexOf(BLUE_SWORD) < order.indexOf(RunInventory.plainOf(PlusRun.TABLET)));
        assertTrue(order.indexOf(RunInventory.plainOf(PlusRun.TABLET)) < order.indexOf(RED_PEN));
        // Within a class, the rarest first.
        assertTrue(order.indexOf(RED_PEN) < order.indexOf(RunInventory.plainOf(PlusRun.KNIFE)));
        assertTrue(order.indexOf(RED_COAT) < order.indexOf(WHITE_COAT));
        assertTrue(order.indexOf(RunInventory.plainOf(PlusRun.KNIFE)) < order.indexOf(RED_COAT));
    }

    @Test public void byNewestTheLatestFoundComesFirstAndWhatIsHersFromTheStartLast() {
        List<Integer> bag = new ArrayList<>();
        for (int code : new int[]{WHITE_VEST, GOLD_RING, RED_COAT}) bag.add(code);
        List<Integer> order = entries(RunInventory.cells(1 << PlusRun.TABLET, bag, RunInventory.ALL, RunInventory.BY_NEW));
        assertEquals(RED_COAT, (int) order.get(0));
        assertEquals(GOLD_RING, (int) order.get(1));
        assertEquals(WHITE_VEST, (int) order.get(2));
        assertEquals(RunInventory.plainOf(PlusRun.TABLET), (int) order.get(3));
    }

    @Test public void whatSheHasOfAThingStaysAsManyCellsAsThereAreOfIt() {
        List<Integer> bag = new ArrayList<>();
        bag.add(BLUE_RING);
        bag.add(BLUE_RING);
        assertEquals(2, RunInventory.cells(0, bag, RunInventory.ACCESSORIES, RunInventory.BY_RARITY).size());
        assertEquals(0, RunInventory.cells(0, new ArrayList<Integer>(), RunInventory.ALL, RunInventory.BY_RARITY).size());
        assertEquals(0, RunInventory.indexOf(RunInventory.cells(0, bag, RunInventory.ALL, RunInventory.BY_RARITY), BLUE_RING));
        assertEquals(-1, RunInventory.indexOf(RunInventory.cells(0, bag, RunInventory.ALL, RunInventory.BY_RARITY), RED_COAT));
    }

    @Test public void everyClassOfWeaponHasAClassAndAnArmourOfNoClassIsLast() {
        for (int kind = 0; kind < RunArms.COUNT; kind++) {
            int cls = RunInventory.classOf(RunInventory.plainOf(kind));
            assertTrue("kind " + kind, cls >= 0 && cls <= RunLoot.CLASSES);
        }
        assertTrue(RunInventory.classOf(RunLoot.code(RunLoot.ARMOUR, 5, RunLoot.ROSEBOUND, RunLoot.ROSE)) == RunLoot.CLASSES);
    }
}
