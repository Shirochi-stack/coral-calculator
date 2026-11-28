package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/** The named weapons: the table (every one has an icon, a grip and a valid class), what they do, their passives and looks, drops and saves. */
public class RunWeaponsTest {
    @Test public void thereAreOverAHundredAndTwentyAndNineOrTenOfEachClass() {
        assertTrue(RunWeapons.count() >= 120);
        int[] perClass = new int[RunLoot.WEAPONS.length];
        for (int index = 0; index < RunWeapons.count(); index++) perClass[RunWeaponData.KIND[index]]++;
        for (int cls = 0; cls < perClass.length; cls++) assertTrue(RunLoot.WEAPONS[cls] + " has " + perClass[cls], perClass[cls] >= 9 && perClass[cls] <= 10);
    }

    @Test public void everyWeaponHasANameAnIconAGripAndAValidClass() {
        HashSet<String> names = new HashSet<>();
        HashSet<Integer> places = new HashSet<>();
        for (int index = 0; index < RunWeapons.count(); index++) {
            String name = RunWeaponData.NAMES[index];
            assertTrue(name, name.length() > 3 && names.add(name));
            int cls = RunWeaponData.KIND[index], era = RunWeaponData.ERA[index];
            assertTrue(cls >= 0 && cls < RunLoot.WEAPONS.length);
            // (An era that is a real chapter's, never the book of addition's or the paper's.)
            assertTrue(era >= 1 && era <= 11);
            assertTrue("one weapon of a class in a chapter", places.add(era * 16 + cls));
            assertTrue(RunWeaponData.LOW[index] <= RunWeaponData.HIGH[index] && RunWeaponData.LOW[index] >= 0 && RunWeaponData.HIGH[index] <= RunLoot.ROSE);
            for (int rarity = RunWeaponData.LOW[index]; rarity <= RunWeaponData.HIGH[index]; rarity++) {
                int code = RunWeapons.code(index, rarity);
                assertTrue(RunWeapons.named(code));
                assertEquals(index, RunWeapons.indexOf(code));
                assertEquals(RunLoot.WEAPON, RunLoot.type(code));
                assertEquals(rarity, RunLoot.rarity(code));
                assertTrue(RunInventory.weapon(code));
                assertEquals("its class", RunArms.OF_KIND[cls], RunArms.kindOf(code));
                // The picture is its own, in the weapons' sheet, and the handle and the tip of it are measured.
                int icon = RunLoot.iconOf(code);
                assertEquals(RunWeapons.ICON_BASE + index, icon);
                short[] axis = RunGrips.axis(icon);
                assertTrue(name + " has no axis", axis != null);
                assertTrue(name + " axis " + java.util.Arrays.toString(axis), axis[1] >= 0 && axis[1] < 128 && axis[2] >= 0 && axis[2] < 128 && axis[3] >= 0 && axis[3] < 128 && axis[4] >= 0 && axis[4] < 128);
                assertTrue(name + " is drawn on the diagonal", axis[3] - axis[1] > 30 && axis[2] - axis[4] > 30);
                // And her hands have a grip for its class.
                assertTrue(name + " has no grip", RunGrips.of(RunArms.kindOf(code)) >= 0);
                assertEquals(name, RunLoot.name(code).replace("Rose ", ""));
            }
            // The effects it has are ones the game has.
            int[] pairs = RunWeaponData.EFFECTS[index];
            assertEquals(0, pairs.length % 2);
            for (int at = 0; at < pairs.length; at += 2) assertTrue(pairs[at] >= 0 && pairs[at] < RunLoot.EFFECTS && pairs[at + 1] > 0);
        }
    }

    @Test public void theRarerOnesHitHarderAndDoMore() {
        int range = indexOf("Tidewrit Cutlass");
        int blue = RunWeapons.code(range, RunLoot.PURPLE), gold = RunWeapons.code(range, RunLoot.GOLD), rose = RunWeapons.code(range, RunLoot.ROSE);
        assertTrue(RunArms.itemBlows(gold) > RunArms.itemBlows(blue));
        assertTrue(RunArms.itemBlows(rose) > RunArms.itemBlows(gold));
        assertTrue(RunArms.itemReach(rose) > RunArms.itemReach(blue));
        // A burning sword's effect is burning, and gets stronger with rarity (and nothing else it does not do).
        int flame = indexOf("Candle-Snuffer Sword");
        int low = RunWeapons.code(flame, RunLoot.PURPLE), high = RunWeapons.code(flame, RunLoot.RED);
        assertTrue(RunWeapons.effect(low, RunLoot.BURN) > 0);
        assertTrue(RunWeapons.effect(high, RunLoot.BURN) > RunWeapons.effect(low, RunLoot.BURN));
        assertEquals(0, RunWeapons.effect(low, RunLoot.FREEZE));
        assertEquals(0, RunWeapons.effect(RunLoot.code(RunLoot.WEAPON, 1, 0, RunLoot.GOLD), RunLoot.BURN));
        assertFalse(RunWeapons.effectWords(low).isEmpty());
        // A wand of fire throws fire, and a wand of frost frost.
        assertEquals(RunArms.FIRE, RunArms.element(RunWeapons.code(indexOf("Lantern Wand"), RunLoot.BLUE)));
        assertEquals(RunArms.ICE, RunArms.element(RunWeapons.code(indexOf("Icicle Wand"), RunLoot.PURPLE)));
    }

    private static int indexOf(String name) {
        for (int index = 0; index < RunWeapons.count(); index++) if (RunWeaponData.NAMES[index].equals(name)) return index;
        throw new AssertionError(name);
    }

    @Test public void gearIsMoreThanStatSticksFromGoldUp() {
        int withPassive = 0, goldUp = 0, status = 0;
        for (int index = 0; index < RunWeapons.count(); index++) {
            for (int rarity = Math.max(RunLoot.GOLD, RunWeaponData.LOW[index]); rarity <= RunWeaponData.HIGH[index]; rarity++) {
                goldUp++;
                int code = RunWeapons.code(index, rarity);
                // Every one of gold or better has a passive that changes how she plays, and a look.
                assertTrue(RunWeaponData.NAMES[index], RunWeaponPassives.passive(code) != RunWeaponPassives.NONE);
                assertFalse(RunWeaponPassives.describe(code).isEmpty());
                assertTrue(RunWeaponFx.of(code).colour != 0);
                withPassive++;
            }
            // Below gold: stats, and some of the gimmicks, but no passive.
            if (RunWeaponData.LOW[index] < RunLoot.GOLD) assertEquals(RunWeaponPassives.NONE, RunWeaponPassives.passive(RunWeapons.code(index, RunLoot.PURPLE)));
            for (int at = 0; at < RunWeaponData.EFFECTS[index].length; at += 2) {
                int effect = RunWeaponData.EFFECTS[index][at];
                if (effect == RunLoot.BURN || effect == RunLoot.FREEZE || effect == RunLoot.POISON) status++;
            }
        }
        assertEquals(goldUp, withPassive);
        assertTrue("status weapons: " + status, status >= 40);
        // The passives are those of what the weapon is made of.
        assertEquals(RunWeaponPassives.BURST, RunWeaponPassives.passive(RunWeapons.code(indexOf("Dragonscript Longsword"), RunLoot.GOLD)));
        assertEquals(RunWeaponPassives.SHATTER, RunWeaponPassives.passive(RunWeapons.code(indexOf("Frostbound Folio Sword"), RunLoot.RED)));
        assertEquals(RunWeaponPassives.ARC, RunWeaponPassives.passive(RunWeapons.code(indexOf("Slimeguard Sabre"), RunLoot.RED)));
        assertEquals(RunWeaponPassives.NONE, RunWeaponPassives.passive(RunLoot.code(RunLoot.WEAPON, 5, 0, RunLoot.ROSE)));
    }

    @Test public void thePassivesDoWhatTheySay() {
        Random dice = new Random(5);
        int echo = RunWeapons.code(indexOf("Proofreader's Saber"), RunLoot.GOLD);
        RunWeaponPassives.State state = new RunWeaponPassives.State();
        int extras = 0;
        for (int blow = 1; blow <= 12; blow++) extras += state.hit(echo, 10, 1, 0, 0, dice, 10) > 0 ? 1 : 0;
        assertEquals("gold echoes every fourth blow", 3, extras);
        assertEquals(RunWeaponPassives.ECHO, state.lastProc);
        // Red echoes more often than gold.
        RunWeaponPassives.State red = new RunWeaponPassives.State();
        int redExtras = 0;
        for (int blow = 1; blow <= 12; blow++) redExtras += red.hit(RunWeapons.code(indexOf("Proofreader's Saber"), RunLoot.RED), 10, 1, 0, 0, dice, 10) > 0 ? 1 : 0;
        assertTrue(redExtras > extras);
        // Shatter: only on one nearly beaten.
        int frost = RunWeapons.code(indexOf("Frostbound Folio Sword"), RunLoot.GOLD);
        RunWeaponPassives.State cold = new RunWeaponPassives.State();
        assertEquals(0, cold.hit(frost, 10, .9f, 0, 0, dice, 10));
        assertTrue(cold.hit(frost, 10, .2f, 0, 0, dice, 10) > 0);
        // Rhythm builds on blows close together and is lost when they stop.
        int quick = RunWeapons.code(indexOf("Rosewater Pistol"), RunLoot.GOLD);
        assertEquals(RunWeaponPassives.RHYTHM, RunWeaponPassives.passive(quick));
        RunWeaponPassives.State rhythm = new RunWeaponPassives.State();
        int most = 0;
        for (int blow = 0; blow < 5; blow++) most = Math.max(most, rhythm.hit(quick, 10, 1, 0, 0, dice, 10));
        assertTrue(most > 0);
        rhythm.tick(2);
        assertEquals(0, rhythm.rhythm);
        // Burst: the sixth blow bursts, for gold; the fourth, for the rose.
        int fire = RunWeapons.code(indexOf("Dragonscript Longsword"), RunLoot.GOLD);
        RunWeaponPassives.State burst = new RunWeaponPassives.State();
        int bursts = 0;
        for (int blow = 1; blow <= 12; blow++) {
            burst.hit(fire, 10, 1, 0, 0, dice, 10);
            if (burst.burst) bursts++;
        }
        assertEquals(2, bursts);
        // Arc: a share of blows jump on, more for the rose.
        int sap = RunWeapons.code(indexOf("Slimeguard Sabre"), RunLoot.GOLD) ;
        RunWeaponPassives.State arc = new RunWeaponPassives.State();
        int jumps = 0;
        for (int blow = 0; blow < 400; blow++) {
            arc.hit(sap, 10, 1, 0, 0, dice, 10);
            if (arc.arc) jumps++;
        }
        assertTrue("jumps " + jumps, jumps > 90 && jumps < 190);
        // Rush: a kill quickens her for a while.
        int drain = RunWeapons.code(indexOf("Heartharvest Scythe"), RunLoot.GOLD);
        RunWeaponPassives.State rush = new RunWeaponPassives.State();
        assertEquals(0, rush.haste(drain));
        rush.kill(drain, 0, 0);
        assertEquals(RunWeaponPassives.RUSH, RunWeaponPassives.passive(drain));
        assertTrue(rush.haste(drain) > 0);
        rush.tick(10);
        assertEquals(0, rush.haste(drain));
        // And red armour answers a hurt with a nova (not every time); plain armour does not.
        int coat = RunLoot.code(RunLoot.ARMOUR, 4, 3, RunLoot.RED), vest = RunLoot.code(RunLoot.ARMOUR, 4, 3, RunLoot.BLUE);
        RunWeaponPassives.State hurt = new RunWeaponPassives.State();
        int novas = 0, plain = 0;
        for (int blow = 0; blow < 6; blow++) {
            novas += hurt.hurt(coat, 0, 0) ? 1 : 0;
            plain += hurt.hurt(vest, 0, 0) ? 1 : 0;
        }
        assertEquals(2, novas);
        assertEquals(0, plain);
    }

    @Test public void theFxChooseALookForWhatIsHeldAndWorn() {
        int gold = RunLoot.GOLD, rose = RunLoot.ROSE;
        int held = RunWeapons.code(indexOf("Candle-Snuffer Sword"), RunLoot.RED);
        assertEquals(Vfx.FIRE_BURST, RunWeaponFx.of(held).effect);
        assertEquals(Vfx.ICE_BURST, RunWeaponFx.of(RunWeapons.code(indexOf("Icicle Wand"), RunLoot.RED)).effect);
        assertEquals(RunLoot.RED, RunWeaponFx.best(held, 0, 0, 0));
        int goldRing = RunLoot.code(RunLoot.ACCESSORY, 3, 3, gold), roseCoat = RunLoot.code(RunLoot.ARMOUR, 3, 2, rose);
        assertEquals(gold, RunWeaponFx.best(0, 0, goldRing, 0));
        assertEquals(rose, RunWeaponFx.best(held, roseCoat, 0, 0));
        assertEquals(-1, RunWeaponFx.best(RunLoot.code(RunLoot.WEAPON, 1, 0, gold), 0, 0, 0));
        assertEquals(-1, RunWeaponFx.best(0, RunLoot.code(RunLoot.ARMOUR, 1, 1, RunLoot.PURPLE), 0, 0));
        assertTrue(RunWeaponFx.glow(rose, .5f) > RunWeaponFx.glow(gold, .5f));
        assertTrue(RunWeaponFx.every(rose) < RunWeaponFx.every(RunLoot.RED) && RunWeaponFx.every(RunLoot.RED) < RunWeaponFx.every(gold));
        for (int kind = 1; kind < RunWeaponPassives.KINDS; kind++) assertTrue(RunWeaponFx.procEffect(kind) < Vfx.COUNT && RunWeaponFx.procSize(kind) > 1);
    }

    @Test public void dropsAreNamedNowAndThenOfTheirClassAndChapter() {
        int named = 0, total = 0;
        Random random = new Random(11);
        HashSet<Integer> seen = new HashSet<>();
        for (int count = 0; count < 20000; count++) {
            int theme = 1 + random.nextInt(11), rarity = random.nextInt(6), base = random.nextInt(13);
            int plain = RunLoot.code(RunLoot.WEAPON, theme, base, rarity);
            int code = RunWeapons.maybe(plain, random.nextInt(1_000_000));
            total++;
            assertEquals(RunLoot.WEAPON, RunLoot.type(code));
            assertEquals(RunLoot.WEAPON, RunLoot.type(plain));
            if (RunWeapons.named(code)) {
                named++;
                int index = RunWeapons.indexOf(code);
                assertTrue(index >= 0);
                seen.add(index);
                assertEquals("its class", base, RunWeaponData.KIND[index]);
                assertTrue("rarity within what it is", RunLoot.rarity(code) >= RunWeaponData.LOW[index] && RunLoot.rarity(code) <= RunWeaponData.HIGH[index]);
                int era = RunWeaponData.ERA[index];
                assertTrue("its chapter near", era <= theme + 1 && era >= theme - 3);
            } else {
                assertEquals(plain, code);
            }
        }
        assertTrue("some of them: " + named + " of " + total, named > 1500 && named < total * .5);
        assertTrue("every one of them can drop: " + seen.size(), seen.size() >= RunWeapons.count() * .9);
        // The ordinary drop function, as the game calls it, may give one and always a weapon for a weapon roll.
        for (int count = 0; count < 2000; count++) {
            int code = RunLoot.dropped(RunLoot.ORDINARY, 1 + random.nextInt(11), random.nextInt(6), 0, random.nextInt(10000));
            assertEquals(RunLoot.WEAPON, RunLoot.type(code));
        }
    }

    @Test public void aNamedWeaponIsKeptInTheBagAndReadBackAsItself() {
        List<Integer> bag = new ArrayList<>();
        for (int index = 0; index < RunWeapons.count(); index += 7) bag.add(RunWeapons.code(index, RunWeaponData.HIGH[index]));
        List<Integer> back = RunLoot.bag(RunLoot.note(bag));
        assertEquals(bag, back);
        for (int code : back) assertTrue(RunWeapons.named(code) && RunWeapons.indexOf(code) >= 0);
        assertNotEquals(RunWeapons.code(0, RunLoot.WHITE), RunLoot.code(RunLoot.WEAPON, 1, 0, RunLoot.WHITE));
        // Sold like any gear: worth more the rarer it is.
        assertTrue(RunEconomy.value(RunWeapons.code(indexOf("Dragonscript Longsword"), RunLoot.ROSE)) > RunEconomy.value(RunWeapons.code(indexOf("Dragonscript Longsword"), RunLoot.GOLD)));
    }
}
