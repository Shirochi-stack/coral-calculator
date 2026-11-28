package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/** The Vampire Merchant: her affinity and hearts, her prices, gifts, her three warnings, where her stall stands, and the fight with her. */
public class RunLilithTest {
    private static final float STEP = 1 / 60f;

    static final class Kept implements RunLilith.Store {
        final Map<String, Integer> kept = new HashMap<>();

        @Override public int get(String key) { return kept.getOrDefault(key, 0); }

        @Override public void put(String key, int value) { kept.put(key, value); }
    }

    @Test public void herAffinityStaysBetweenNoughtAndAHundredAndShowsAsHearts() {
        Kept store = new Kept();
        assertEquals(0, RunLilith.befriend(store, -20));
        assertEquals(100, RunLilith.befriend(store, 250));
        assertEquals(10, RunLilith.halfHearts(100));
        assertEquals(5, RunLilith.halfHearts(55));
        assertEquals(RunLilith.COLD, RunLilith.tier(0));
        assertEquals(RunLilith.WARM, RunLilith.tier(45));
        assertEquals(RunLilith.DEVOTED, RunLilith.tier(99));
        assertEquals(RunLilith.BELOVED, RunLilith.tier(100));
    }

    @Test public void herPricesFallAsSheLikesHalgakosAndRiseForEveryGrudge() {
        int base = 100;
        assertEquals(100, RunLilith.price(base, 0, 0));
        assertTrue(RunLilith.price(base, 50, 0) < RunLilith.price(base, 20, 0));
        assertEquals(60, RunLilith.price(base, 100, 0));
        assertTrue(RunLilith.price(base, 50, 1) > RunLilith.price(base, 50, 0));
        assertTrue(RunLilith.price(1, 100, 0) >= 1);
    }

    @Test public void aGiftSheLikesPleasesHerLessEachTimeAndOneSheDoesNotStingsEveryTime() {
        Kept store = new Kept();
        RunLilith.befriend(store, 50);
        int first = RunLilith.give(store, 0), second = RunLilith.give(store, 0);
        assertTrue(first > 0 && second > 0 && second < first);
        // (Never less than a quarter of it, however often.)
        for (int time = 0; time < 4; time++) assertTrue(RunLilith.give(store, 0) >= 2);
        int before = RunLilith.affinity(store);
        assertEquals(RunLilith.GIFT_WORTH[5], RunLilith.give(store, 5));
        assertEquals(RunLilith.GIFT_WORTH[5], RunLilith.give(store, 5));
        assertEquals(before + 2 * RunLilith.GIFT_WORTH[5], RunLilith.affinity(store));
        for (int gift = 0; gift < RunLilith.GIFT_KINDS; gift++) assertEquals(gift < 5, RunLilith.likes(gift));
    }

    @Test public void threeWarningsEachColderThenTheFightAndACalmForgetsThem() {
        Kept store = new Kept();
        RunLilith.befriend(store, 60);
        RunLilith.Temper temper = new RunLilith.Temper();
        assertEquals(RunLilith.Temper.AMUSED, temper.struck(store, 10));
        assertEquals(58, RunLilith.affinity(store));
        assertEquals(RunLilith.Temper.COOLER, temper.struck(store, 12));
        assertEquals(RunLilith.Temper.COLD_FACE, temper.struck(store, 14));
        assertEquals(50, RunLilith.affinity(store));
        assertEquals(RunLilith.Temper.COLD_FACE, temper.face(15));
        // A calm, and she has forgotten them: the next blow amuses her again.
        assertEquals(RunLilith.Temper.CALM_FACE, temper.face(14 + RunLilith.Temper.CALM + 1));
        assertEquals(RunLilith.Temper.AMUSED, temper.struck(store, 14 + RunLilith.Temper.CALM + 1));
        // Three more without a calm, and only the one after the last warning starts the fight.
        float now = 50;
        assertEquals(RunLilith.Temper.COOLER, temper.struck(store, now));
        assertEquals(RunLilith.Temper.COLD_FACE, temper.struck(store, now + 1));
        int before = RunLilith.affinity(store);
        assertEquals(RunLilith.Temper.FIGHT, temper.struck(store, now + 2));
        assertEquals(before - RunLilith.Temper.FIGHT_COST, RunLilith.affinity(store));
        assertEquals(1, store.get(RunLilith.GRUDGE));
        assertEquals(RunLilith.Temper.FIGHT, temper.struck(store, now + 3));
    }

    @Test public void herStallStandsOnSomeFloorsOfEveryDiveNeverTheKeepers() {
        for (int dungeon = 0; dungeon <= RunDungeons.MIRROR; dungeon++) {
            for (long dive = 1; dive <= 6; dive++) {
                int floors = RunDungeons.floors(dungeon), with = 0;
                for (int floor = 0; floor < floors; floor++) {
                    boolean appears = RunLilith.appearsOn(dungeon, floor, dive);
                    if (floor == floors - 1) assertFalse(appears);
                    if (appears) with++;
                }
                assertTrue(with >= 1 && with < floors);
            }
        }
        // And on a floor where she appears her stall stands on level ground, past the flag.
        int seen = 0, gifts = 0;
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon += 5) {
            for (int floor = 0; floor < RunDungeons.floors(dungeon) - 1; floor++) {
                RunLevels.Level level = RunLevels.dungeon(dungeon, floor, 3);
                for (int[] item : level.items) {
                    if (item[0] == PlusRun.SPOT && item[4] == PlusRun.LILITH) {
                        seen++;
                        assertEquals(RunLevels.top(level, item[1]) - 1, item[2]);
                        assertTrue(item[1] > level.flags.get(0));
                    }
                    if (item[0] == PlusRun.GIFT) {
                        gifts++;
                        assertTrue(item[4] >= 0 && item[4] < RunLilith.GIFT_KINDS);
                    }
                }
            }
        }
        assertTrue(seen >= 3);
        assertTrue(gifts >= 1);
    }

    private static RunLevels.Level flat() {
        RunLevels.Level flat = new RunLevels.Level(40, 60);
        for (int column = 0; column < 60; column++) for (int row = RunLevels.FLOOR; row < RunLevels.ROWS; row++) flat.land[row][column] = RunLevels.BLOCK;
        flat.gate = 57;
        flat.items.add(new int[]{PlusRun.SPOT, 20, RunLevels.FLOOR - 1, 0, PlusRun.LILITH});
        flat.items.add(new int[]{PlusRun.GIFT, 10, RunLevels.FLOOR - 1, 0, 3});
        return flat;
    }

    @Test public void aBlowAtHerStallStrikesHerAndAGiftIsPickedUp() {
        PlusRun run = new PlusRun(flat(), new PlusRun.Perks());
        run.merchant(19, 22, RunLevels.FLOOR - 2, RunLevels.FLOOR);
        assertEquals(1, run.merchantBlows());
        // (No more than one blow a third of a second.)
        run.merchant(19, 22, RunLevels.FLOOR - 2, RunLevels.FLOOR);
        assertEquals(1, run.merchantBlows());
        run.merchant(40, 42, RunLevels.FLOOR - 2, RunLevels.FLOOR);
        run.place(10 + .5f - PlusRun.WIDE / 2, RunLevels.FLOOR - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertEquals(1, run.gifts());
        assertEquals(3, run.lastGift());
    }

    @Test public void sheCannotBeBeatenUntilHalgakosIsStrongEnoughAndMendsAsSheFights() {
        PlusRun run = new PlusRun(flat(), new PlusRun.Perks());
        assertTrue(run.lilithFights());
        assertFalse(run.lilithFights());
        PlusRun.Foe her = null;
        for (PlusRun.Foe foe : run.foes()) if (foe.kind == RunUniques.LILITH) her = foe;
        assertTrue(her != null && run.lilithFighting());
        for (int[] item : run.items()) assertFalse(item[0] == PlusRun.SPOT && item[4] == PlusRun.LILITH);
        // She mends.
        her.left = her.most / 2;
        int was = her.left;
        for (int step = 0; step < 60; step++) run.step(STEP, false, false, false);
        assertTrue(her.left > was);
        // A blow that would end her stops at the last of her health; too weak, she laughs.
        assertTrue(run.lilithHolds(her, her.most * 2f));
        assertEquals(Math.round(her.most * RunLilith.LAST_BLOW), her.left);
        assertTrue(run.lilithMocks());
        assertFalse(run.lilithAsks());
        run.lilithEnds(false);
        assertFalse(run.lilithFighting());
        assertFalse(run.lilithMocks());
        // Strong enough, she is asked about; finished, she is gone for good.
        PlusRun.Perks strong = new PlusRun.Perks();
        strong.level = RunLilith.MORTAL_LEVEL;
        PlusRun late = new PlusRun(flat(), strong);
        late.lilithFights();
        PlusRun.Foe she = null;
        for (PlusRun.Foe foe : late.foes()) if (foe.kind == RunUniques.LILITH) she = foe;
        assertTrue(late.lilithHolds(she, she.most * 2f));
        assertTrue(late.lilithAsks());
        late.lilithEnds(true);
        assertTrue(she.flat);
        assertFalse(late.lilithAsks());
        for (int step = 0; step < 30; step++) late.step(STEP, false, false, false);
        assertTrue(she.flat);
        assertTrue(RunLilith.mends(RunLilith.MORTAL_LEVEL) < RunLilith.mends(1));
    }

    @Test public void herArtIsInTheLilithPackAndHerWordsNeverNameHer() {
        for (String one : new String[]{"chibi", "chibi_soft", "portrait_fangs", "portrait_soft", "portrait_shy", "cg_stall", "cg_merchant"}) {
            assertTrue(one, new java.io.File("../lilith/src/main/assets/lilith/" + one + ".webp").isFile());
        }
        assertTrue(new java.io.File("../assets/plus-run/lilith/lilith_cg.png").isFile());
        assertEquals(RunStory.NAMES.length, RunStory.SPEAKERS.length());
        assertEquals("The Vampire Merchant", RunStory.NAMES[RunStory.SPEAKERS.indexOf('h')]);
        for (String[][] group : new String[][][]{RunStory.MERCHANT, RunStory.MERCHANT_WARN, RunStory.MERCHANT_GIFT, RunStory.MERCHANT_SHOP}) {
            for (String[] line : group) {
                assertEquals("h", line[2]);
                assertFalse(line[1], line[1].contains(RunLilith.NAME) || line[1].contains("Sloppy"));
            }
        }
        for (String[] line : RunStory.MERCHANT_ANSWERS) {
            assertEquals("H", line[2]);
            assertTrue(line[1], line[1].endsWith(" Sloppy~♡") && !line[1].contains(", Sloppy"));
            assertTrue(line[0], RunVoicesTest.recorded(line[0]));
        }
        // The warnings are as asked: amused, cooler, cold.
        assertEquals("Careful, little cat. My wares aren't free.", RunStory.MERCHANT_WARN[0][1]);
        assertEquals("That's twice. I don't forgive a third.", RunStory.MERCHANT_WARN[1][1]);
        assertEquals("Last chance. Lower your weapon.", RunStory.MERCHANT_WARN[2][1]);
    }
}
