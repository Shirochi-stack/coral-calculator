package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** What she wears of what she finds: accessories rare and for a class; armour leaning to one; and every effect felt in the rules. */
public class RunGearTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(1, columns);
        for (int column = 0; column < columns; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        level.gate = columns - 3;
        return level;
    }

    private static int accessory(int base, int rarity) { return RunLoot.code(RunLoot.ACCESSORY, 0, base, rarity); }

    private static PlusRun wearing(RunLevels.Level level, int main, int armour, int first, int second) {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = main;
        perks.worn = new RunLoot.Worn(armour, first, second);
        return new PlusRun(level, perks);
    }

    private static void hold(PlusRun run, float seconds, boolean right, boolean strike) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, false, right, false, strike);
    }

    /** Holds the button for a special, lets go, and holds it again, for a while. */
    private static void cycle(PlusRun run, float seconds) {
        for (float gone = 0; gone < seconds; gone += 1.2f) {
            hold(run, 1, false, true);
            hold(run, .2f, false, false);
        }
    }

    /** Taps the button so many times, a little apart. */
    private static void tap(PlusRun run, int times) {
        for (int time = 0; time < times; time++) {
            run.step(STEP, false, false, false, true);
            hold(run, .7f, false, false);
        }
    }

    // The table

    @Test public void accessoriesAreRareAndTheBetterOnesComeFromTheHardest() {
        int[] ordinary = new int[6], waits = new int[6];
        int accessories = 0, armours = 0;
        for (int roll = 0; roll < 10000; roll += 7) {
            for (int rarity = RunLoot.WHITE; rarity <= RunLoot.ROSE; rarity++) {
                int code = RunLoot.dropped(RunLoot.ORDINARY, 3, rarity, roll, roll * 31 % 10000);
                if (RunLoot.type(code) == RunLoot.ACCESSORY) {
                    accessories++;
                    ordinary[RunLoot.rarity(code)]++;
                }
                if (RunLoot.type(code) == RunLoot.ARMOUR) armours++;
                int fromBoss = RunLoot.dropped(RunLoot.WAITS, 3, rarity, roll, roll * 31 % 10000);
                if (RunLoot.type(fromBoss) == RunLoot.ACCESSORY) waits[RunLoot.rarity(fromBoss)]++;
            }
        }
        // Three in a hundred of a creature's things; never better than blue from one; never worse than purple from one who waits.
        assertTrue(accessories < armours / 10);
        for (int rarity = RunLoot.PURPLE; rarity <= RunLoot.ROSE; rarity++) assertEquals(0, ordinary[rarity]);
        assertEquals(0, waits[RunLoot.WHITE] + waits[RunLoot.BLUE]);
        assertTrue(waits[RunLoot.PURPLE] > 0);
    }

    @Test public void everyAccessoryIsForAClassOrAnyAndGrowsWithRarity() {
        assertEquals(30, RunLoot.ACCESSORIES.length);
        assertEquals(30, RunLoot.ACCESSORY_DOES.length);
        int[] forClass = new int[RunLoot.CLASSES];
        int any = 0;
        for (int base = 0; base < 30; base++) {
            int[] white = RunLoot.does(accessory(base, RunLoot.WHITE)), gold = RunLoot.does(accessory(base, RunLoot.GOLD)), red = RunLoot.does(accessory(base, RunLoot.RED));
            if (white[0] == RunLoot.ANY) any++;
            else forClass[white[0]]++;
            if (base != RunLoot.ROSE_OF_HALGAKOS) {
                // One thing at white; a second from gold; a third from red.
                assertEquals(3, white.length);
                assertEquals(5, gold.length);
                assertEquals(7, red.length);
            }
            assertTrue(RunLoot.said(white[1], white[2]).length() > 5);
            assertTrue(red[2] >= white[2]);
        }
        for (int count : forClass) assertTrue(count >= 2);
        assertTrue(any >= 8);
        // Bigger numbers: a Venom Nib poisons more often the rarer it is.
        assertTrue(RunLoot.does(accessory(17, RunLoot.RED))[2] > RunLoot.does(accessory(17, RunLoot.BLUE))[2]);
    }

    @Test public void armourLeansToAClassAndSetsAddUp() {
        int armour = RunLoot.code(RunLoot.ARMOUR, 2, 4, RunLoot.GOLD);
        RunLoot.Worn alone = new RunLoot.Worn(armour, 0, 0);
        int lean = RunLoot.leansTo(4);
        assertTrue(alone.of(RunLoot.SHRUG, RunLoot.ANY) > 0);
        assertTrue(alone.of(RunLoot.POWER, lean) > 0);
        assertEquals(0, alone.of(RunLoot.POWER, (lean + 1) % RunLoot.CLASSES));
        assertTrue(alone.of(RunLoot.REGEN, RunLoot.ANY) > 0);
        // An accessory of its chapter: a piece of shield more; both: harder blows too.
        int same = RunLoot.code(RunLoot.ACCESSORY, 2, 5, RunLoot.BLUE), other = RunLoot.code(RunLoot.ACCESSORY, 3, 5, RunLoot.BLUE);
        assertEquals(1, new RunLoot.Worn(armour, same, other).set);
        assertEquals(1, new RunLoot.Worn(armour, same, other).of(RunLoot.WARD, RunLoot.ANY));
        RunLoot.Worn both = new RunLoot.Worn(armour, same, same);
        assertEquals(2, both.set);
        assertTrue(both.of(RunLoot.POWER, RunLoot.ANY) >= 10);
        assertEquals(both.armour + "," + both.first + "," + both.second, RunLoot.note(both));
        assertEquals(RunLoot.note(both), RunLoot.note(RunLoot.worn(RunLoot.note(both))));
    }

    // Felt

    @Test public void theShieldBarTakesAHurtAndFillsAgain() {
        RunLevels.Level level = flat(60);
        level.land[FLOOR - 1][7] = RunLevels.SPIKE;
        PlusRun run = wearing(level, PlusRun.UNARMED, 0, accessory(1, RunLoot.WHITE), 0);
        assertEquals(1, run.wardMost());
        assertEquals(1, run.ward(), 0);
        for (int step = 0; step < 300 && run.wardHurts() == 0; step++) run.step(STEP, false, true, false);
        assertEquals(1, run.wardHurts());
        assertEquals(0, run.hurts());
        assertEquals(PlusRun.HEARTS, run.hearts());
        assertTrue(run.ward() < 1);
        hold(run, 1, true, false);
        hold(run, PlusRun.WARD_WAIT + PlusRun.WARD_FILL + .5f, false, false);
        assertEquals(1, run.ward(), .001f);
    }

    @Test public void lifeComesBack() {
        RunLevels.Level level = flat(60);
        level.land[FLOOR - 1][7] = RunLevels.SPIKE;
        PlusRun run = wearing(level, PlusRun.UNARMED, 0, accessory(0, RunLoot.WHITE), 0);
        for (int step = 0; step < 300 && run.hurts() == 0; step++) run.step(STEP, false, true, false);
        hold(run, 1, true, false);
        int hurt = run.halves();
        assertTrue(hurt < PlusRun.HEARTS * 2);
        hold(run, 31, false, false);
        assertTrue(run.halves() > hurt);
        assertTrue(run.regens() >= 1);
    }

    @Test public void specialsCostEnergyAndItComesBack() {
        PlusRun plain = wearing(flat(60), PlusRun.TABLET, 0, 0, 0);
        cycle(plain, 12);
        // Held all that while: a special each time there was energy for one, never more.
        int most = (int) ((PlusRun.ENERGY_MOST + PlusRun.ENERGY_BACK * 12) / PlusRun.SPECIAL_COST) + 1;
        assertTrue("plain " + plain.specialsUsed(), plain.specialsUsed() >= 2);
        assertTrue("plain " + plain.specialsUsed() + " most " + most, plain.specialsUsed() <= most);
        // An Inkwell Heart and an Hourglass, red: energy back faster, specials cheaper and sooner.
        PlusRun geared = wearing(flat(60), PlusRun.TABLET, 0, accessory(2, RunLoot.RED), accessory(9, RunLoot.RED));
        cycle(geared, 12);
        assertTrue("geared " + geared.specialsUsed() + " plain " + plain.specialsUsed(), geared.specialsUsed() > plain.specialsUsed());
    }

    @Test public void inkPoisonTakesBlowsAndWearsOff() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 6, FLOOR - 1});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        perks.harder = 12;
        // A Footnote Venom Tag at its rarest: every blow of a tome poisons.
        perks.worn = new RunLoot.Worn(0, accessory(14, RunLoot.ROSE), 0);
        PlusRun run = new PlusRun(level, perks);
        PlusRun.Foe foe = run.foes().get(0);
        int before = foe.left;
        for (int step = 0; step < 120 && run.poisons() == 0; step++) {
            run.step(STEP, false, false, false, step % 30 == 0);
            foe.x = 5;
        }
        assertEquals("left " + foe.left + " of " + before + " flat " + foe.flat, 1, run.poisons());
        assertTrue("poisoned " + foe.poisoned, foe.poisoned > 0);
        int hit = foe.left;
        assertTrue("hit " + hit + " before " + before, hit < before);
        for (float gone = 0; gone < PlusRun.POISON_LASTS + 1.5f; gone += STEP) {
            run.step(STEP, false, false, false);
            foe.x = 5;
        }
        assertTrue("still " + foe.poisoned, foe.poisoned <= 0);
        assertTrue("poison took " + (hit - foe.left), hit - foe.left >= 3);
    }

    @Test public void freezingHoldsACreatureStill() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 9, FLOOR - 1});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.CANNON;
        perks.harder = 20;
        perks.worn = new RunLoot.Worn(0, accessory(19, RunLoot.ROSE), 0);
        PlusRun run = new PlusRun(level, perks);
        PlusRun.Foe foe = run.foes().get(0);
        for (int step = 0; step < 240 && run.freezes() == 0; step++) run.step(STEP, false, false, false, step % 60 == 0);
        assertEquals(1, run.freezes());
        float at = foe.x;
        for (int step = 0; step < 30; step++) run.step(STEP, false, false, false);
        assertEquals(at, foe.x, .001f);
    }

    @Test public void greatBlowsAndThornsAndDodges() {
        // A Colophon Signet, red, and a Whetstone Earring, rose, on a katana: great blows often.
        RunLevels.Level level = flat(60);
        for (int column = 5; column < 40; column += 2) level.foes.add(new int[]{RunLevels.MINUS, column, FLOOR - 1});
        PlusRun run = wearing(level, PlusRun.KATANA, 0, accessory(28, RunLoot.RED), accessory(11, RunLoot.ROSE));
        for (int step = 0; step < 1200; step++) run.step(STEP, false, step % 90 < 60, false, step % 20 == 0);
        assertTrue(run.crits() > 0);
        // Thorns, rose: what hurts her is hurt back.
        RunLevels.Level thorny = flat(60);
        thorny.foes.add(new int[]{RunLevels.MINUS, 8, FLOOR - 1});
        PlusRun pricked = wearing(thorny, PlusRun.UNARMED, 0, accessory(8, RunLoot.ROSE), 0);
        hold(pricked, 3, true, false);
        assertTrue(pricked.thorned() > 0 || pricked.flattened() > 0);
        // A hare's foot at its rarest dodges now and then.
        RunLevels.Level spikes = flat(80);
        for (int column = 6; column < 70; column++) spikes.land[FLOOR - 1][column] = RunLevels.SPIKE;
        PlusRun slips = wearing(spikes, PlusRun.UNARMED, 0, accessory(7, RunLoot.ROSE), accessory(3, RunLoot.ROSE));
        hold(slips, 20, true, false);
        assertTrue(slips.dodges() + slips.shrugs() > 0);
    }

    @Test public void moreJumpsMorePlusesAndFasterAttacks() {
        // A Second Wind Feather: one more jump in the air.
        PlusRun feather = wearing(flat(60), PlusRun.UNARMED, 0, accessory(4, RunLoot.WHITE), 0);
        assertEquals(1, feather.gear(RunLoot.AIR));
        // A Duelist's Ribbon is for blades: felt with a katana, not with a cannon.
        PlusRun katana = wearing(flat(60), PlusRun.KATANA, 0, accessory(12, RunLoot.GOLD), 0);
        assertTrue(katana.gear(RunLoot.HASTE) > 0);
        PlusRun cannon = wearing(flat(60), PlusRun.CANNON, 0, accessory(12, RunLoot.GOLD), 0);
        assertEquals(0, cannon.gear(RunLoot.HASTE));
        // A Lucky Plus Coin, rose: creatures drop more pluses.
        RunLevels.Level level = RunLevels.level(6);
        PlusRun plain = new PlusRun(level), lucky = wearing(level, PlusRun.UNARMED, 0, accessory(5, RunLoot.ROSE), 0);
        for (PlusRun run : new PlusRun[]{plain, lucky}) {
            for (PlusRun.Foe foe : run.foes()) if (!RunLevels.boss(foe.kind) && foe.kind != RunLevels.TARGET) foe.flat = true;
            run.step(STEP, false, false, false);
        }
        assertTrue(lucky.scattered().size() > plain.scattered().size());
    }
}
