package com.coral.calculator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

public class SandboxTest {
    // The map

    @Test public void theMapIsTheSameForTheSameSeedInAnyOrder() {
        SandboxWorld one = new SandboxWorld(42), two = new SandboxWorld(42), other = new SandboxWorld(43);
        one.ensureAll();
        // (Two makes its chunks in the opposite order, as a player wandering the other way would.)
        for (int y = SandboxWorld.SIZE - 1; y >= 0; y -= SandboxWorld.CHUNK) for (int x = SandboxWorld.SIZE - 1; x >= 0; x -= SandboxWorld.CHUNK) two.ensure(x, y);
        other.ensureAll();
        assertArrayEquals(one.ground, two.ground);
        assertArrayEquals(one.natural, two.natural);
        assertFalse(java.util.Arrays.equals(one.ground, other.ground));
    }

    @Test public void theMapIsBigAndMadeOfEverythingAskedFor() {
        assertTrue(SandboxWorld.SIZE >= 64);
        for (long seed : new long[]{20260, 1, 777}) {
            SandboxWorld world = new SandboxWorld(seed);
            int[] census = world.census();
            for (int ground = 0; ground < 4; ground++) assertTrue("seed " + seed + " ground " + ground, census[ground] > 40);
            assertTrue(world.count(SandboxWorld.TREE) > 150);
            assertTrue(world.count(SandboxWorld.ROCK) > 30);
        }
    }

    @Test public void whereSheStartsIsClearAndOnLand() {
        SandboxWorld world = new SandboxWorld(20260);
        for (int dy = -3; dy <= 3; dy++) {
            for (int dx = -3; dx <= 3; dx++) {
                assertFalse(world.solid(SandboxWorld.SPAWN + dx, SandboxWorld.SPAWN + dy));
                assertEquals(SandboxWorld.GRASS, world.groundOf(SandboxWorld.SPAWN + dx, SandboxWorld.SPAWN + dy));
            }
        }
    }

    // Depth

    @Test public void whatIsLowerOnTheScreenIsDrawnLast() {
        SandboxDepth.Item[] items = new SandboxDepth.Item[5];
        for (int i = 0; i < items.length; i++) items[i] = new SandboxDepth.Item();
        items[0].set(5, 10, SandboxDepth.STANDING, 6, 0); // her, in front of the tree
        items[1].set(5, 9, SandboxDepth.STANDING, 0, 1); // the tree's foot
        items[2].set(5, 8, SandboxDepth.STANDING, 6, 2); // another of her, behind it
        items[3].set(5, 9, SandboxDepth.ROOF, 2, 3); // a roof at the tree's depth goes over it
        items[4].set(0, 0, SandboxDepth.FLYING, 7, 4); // a shot is over everything
        SandboxDepth.sort(items, items.length);
        assertEquals(2, items[0].index);
        assertEquals(1, items[1].index);
        assertEquals(3, items[2].index);
        assertEquals(0, items[3].index);
        assertEquals(4, items[4].index);
    }

    // Building

    @Test public void buildingIsOnlyWhereItMakesSense() {
        SandboxWorld world = new SandboxWorld(20260);
        int x = SandboxWorld.SPAWN, y = SandboxWorld.SPAWN;
        assertFalse("a roof needs something under it", world.canPlace(SandboxWorld.ROOF, x, y));
        assertFalse("furniture needs a floor", world.canPlace(SandboxWorld.B_BED, x, y));
        assertTrue(world.place(SandboxWorld.FLOOR, x, y));
        assertFalse("one floor to a tile", world.canPlace(SandboxWorld.FLOOR, x, y));
        assertTrue(world.canPlace(SandboxWorld.B_BED, x, y));
        assertTrue(world.canPlace(SandboxWorld.ROOF, x, y));
        assertTrue(world.place(SandboxWorld.B_BED, x, y));
        assertFalse(world.canPlace(SandboxWorld.B_WALL, x, y));
        assertTrue(world.solid(x, y));
        assertFalse("out of the map", world.canPlace(SandboxWorld.FLOOR, -1, 5));
        // Not on water, a tree or a rock.
        int[] water = find(world, SandboxWorld.WATER, SandboxWorld.NONE), tree = findNatural(world, SandboxWorld.TREE);
        assertFalse(world.canPlace(SandboxWorld.FLOOR, water[0], water[1]));
        assertFalse(world.canPlace(SandboxWorld.B_WALL, tree[0], tree[1]));
    }

    private static int[] find(SandboxWorld world, byte ground, byte natural) {
        world.ensureAll();
        for (int y = 0; y < SandboxWorld.SIZE; y++) for (int x = 0; x < SandboxWorld.SIZE; x++) if (world.ground[SandboxWorld.at(x, y)] == ground && world.natural[SandboxWorld.at(x, y)] == natural) return new int[]{x, y};
        throw new AssertionError("none found");
    }

    private static int[] findNatural(SandboxWorld world, byte what) {
        world.ensureAll();
        for (int y = 0; y < SandboxWorld.SIZE; y++) for (int x = 0; x < SandboxWorld.SIZE; x++) if (world.natural[SandboxWorld.at(x, y)] == what) return new int[]{x, y};
        throw new AssertionError("none found");
    }

    @Test public void wallsJoinTheirNeighboursAndCorners() {
        SandboxWorld world = new SandboxWorld(20260);
        int x = SandboxWorld.SPAWN, y = SandboxWorld.SPAWN;
        world.place(SandboxWorld.B_WALL, x, y);
        assertEquals(0, world.wallMask(x, y));
        world.place(SandboxWorld.B_WALL, x + 1, y);
        assertEquals(SandboxWorld.E, world.wallMask(x, y));
        assertEquals(SandboxWorld.W, world.wallMask(x + 1, y));
        world.place(SandboxWorld.B_WALL, x, y + 1);
        assertEquals(SandboxWorld.E | SandboxWorld.S, world.wallMask(x, y));
        world.place(SandboxWorld.B_WALL, x - 1, y);
        world.place(SandboxWorld.B_WALL, x, y - 1);
        assertEquals(SandboxWorld.N | SandboxWorld.E | SandboxWorld.S | SandboxWorld.W, world.wallMask(x, y));
        // A door joins as a wall does, and when the wall beside it goes the mask follows.
        world.place(SandboxWorld.B_DOOR, x + 2, y);
        assertEquals(SandboxWorld.W, world.wallMask(x + 2, y));
        assertTrue(world.doorAcross(x + 2, y));
        world.demolish(x + 1, y, new int[3]);
        assertEquals(0, world.wallMask(x + 2, y) & SandboxWorld.W);
        assertEquals(SandboxWorld.N | SandboxWorld.S | SandboxWorld.W, world.wallMask(x, y));
    }

    @Test public void doorsAreOpenOnlyToThoseNear() {
        SandboxWorld world = new SandboxWorld(20260);
        int x = SandboxWorld.SPAWN, y = SandboxWorld.SPAWN;
        world.place(SandboxWorld.B_DOOR, x, y);
        assertTrue(world.solid(x, y));
        world.updateDoors(new float[]{x + .5f}, new float[]{y + 1.4f}, 1);
        assertFalse(world.solid(x, y));
        world.updateDoors(new float[]{x + 6}, new float[]{y + 6}, 1);
        assertTrue(world.solid(x, y));
    }

    @Test public void demolishingRefundsEverythingIncludingAChestsContents() {
        SandboxWorld world = new SandboxWorld(20260);
        int x = SandboxWorld.SPAWN, y = SandboxWorld.SPAWN;
        world.place(SandboxWorld.FLOOR, x, y);
        world.place(SandboxWorld.B_CHEST, x, y);
        world.chest(x, y)[SandboxWorld.PAPER] = 5;
        int[] refund = new int[3];
        assertEquals(SandboxWorld.B_CHEST, world.demolish(x, y, refund));
        assertEquals(SandboxWorld.cost(SandboxWorld.B_CHEST)[SandboxWorld.WOOD], refund[SandboxWorld.WOOD]);
        assertEquals(5, refund[SandboxWorld.PAPER]);
        assertNull(world.chest(x, y));
        refund = new int[3];
        assertEquals(SandboxWorld.FLOOR, world.demolish(x, y, refund));
        assertEquals(1, refund[SandboxWorld.WOOD]);
        assertEquals(-1, world.demolish(x, y, refund));
        assertTrue(SandboxWorld.affords(new int[]{9, 9, 9}, SandboxWorld.B_BED));
        assertFalse(SandboxWorld.affords(new int[]{1, 9, 9}, SandboxWorld.B_BED));
    }

    @Test public void aRoofFadesAsOneOverAllOfItsJoinedTiles() {
        SandboxWorld world = new SandboxWorld(20260);
        int x = SandboxWorld.SPAWN, y = SandboxWorld.SPAWN;
        for (int i = 0; i < 3; i++) {
            world.place(SandboxWorld.FLOOR, x + i, y);
            world.place(SandboxWorld.ROOF, x + i, y);
        }
        world.place(SandboxWorld.FLOOR, x + 5, y);
        world.place(SandboxWorld.ROOF, x + 5, y);
        BitSet over = world.roofOver(x + 1, y);
        assertEquals(3, over.cardinality());
        assertTrue(over.get(SandboxWorld.at(x + 2, y)));
        assertFalse(over.get(SandboxWorld.at(x + 5, y)));
        assertEquals(0, world.roofOver(x + 4, y).cardinality());
    }

    // Gathering

    @Test public void treesAndRocksGiveTheirThingsAfterEnoughBlows() {
        SandboxWorld world = new SandboxWorld(20260);
        int[] tree = findNatural(world, SandboxWorld.TREE), rock = findNatural(world, SandboxWorld.ROCK);
        Random random = new Random(1);
        assertNull(world.hurtNatural(tree[0], tree[1], 7, random));
        assertTrue(world.hurtPart(tree[0], tree[1]) > 0);
        int[] gave = null;
        for (int i = 0; i < 6 && gave == null; i++) gave = world.hurtNatural(tree[0], tree[1], 7, random);
        assertNotNull(gave);
        assertTrue(gave[SandboxWorld.WOOD] >= 2);
        assertEquals(SandboxWorld.NONE, world.naturalOf(tree[0], tree[1]));
        gave = world.hurtNatural(rock[0], rock[1], 999, random);
        assertTrue(gave[SandboxWorld.STONE_ITEM] >= 2);
    }

    // Saving

    @Test public void whatWasBuiltAndGatheredComesBack() {
        SandboxWorld world = new SandboxWorld(9);
        int x = SandboxWorld.SPAWN, y = SandboxWorld.SPAWN;
        for (int i = 0; i < 4; i++) {
            world.place(SandboxWorld.FLOOR, x + i, y);
            world.place(SandboxWorld.B_WALL, x + i, y - 1);
        }
        world.place(SandboxWorld.B_DOOR, x + 4, y - 1);
        world.place(SandboxWorld.ROOF, x, y);
        world.place(SandboxWorld.B_CHEST, x + 1, y);
        world.place(SandboxWorld.B_LAMP, x + 2, y);
        world.toggleLamp(x + 2, y);
        world.chest(x + 1, y)[SandboxWorld.STONE_ITEM] = 7;
        int[] tree = findNatural(world, SandboxWorld.TREE);
        world.hurtNatural(tree[0], tree[1], 999, new Random(3));
        String saved = world.save();
        SandboxWorld back = SandboxWorld.load(saved);
        assertNotNull(back);
        back.ensureAll();
        world.ensureAll();
        assertArrayEquals(world.floor, back.floor);
        assertArrayEquals(world.wall, back.wall);
        assertArrayEquals(world.roof, back.roof);
        assertArrayEquals(world.furniture, back.furniture);
        assertArrayEquals(world.natural, back.natural);
        assertEquals(7, back.chest(x + 1, y)[SandboxWorld.STONE_ITEM]);
        assertFalse(back.lit(x + 2, y));
        assertEquals(saved, back.save());
        assertNull(SandboxWorld.load("garbage"));
        assertNull(SandboxWorld.load(null));
    }

    // Weapons

    @Test public void theWeaponPickerCoversEveryClassOfPlusRun() {
        assertEquals(13, SandboxCombat.weapons());
        ArrayList<Integer> kinds = new ArrayList<>();
        for (int rarity = 0; rarity <= RunLoot.ROSE; rarity++) {
            SandboxCombat.Weapon[] all = SandboxCombat.all(rarity);
            assertEquals(RunLoot.WEAPONS.length, all.length);
            for (int i = 0; i < all.length; i++) {
                SandboxCombat.Weapon w = all[i];
                assertEquals(RunLoot.WEAPONS[i], w.name);
                assertEquals(RunArms.OF_KIND[i], w.kind);
                assertTrue(w.damage > 0 && w.rest > 0 && w.reach > .5f);
                assertTrue(RunLoot.iconOf(w.code) >= 0);
                if (w.shape == SandboxCombat.SHOT || w.shape == SandboxCombat.FAN || w.shape == SandboxCombat.LOB) assertTrue(w.speed > 0);
                if (rarity == 0) kinds.add(w.kind);
            }
        }
        assertEquals(13, new java.util.HashSet<>(kinds).size());
        // Both a swung weapon and a thrown one are there to try, and a higher rarity hits harder.
        assertEquals(SandboxCombat.ARC, SandboxCombat.weapon(0, 0).shape);
        assertEquals(SandboxCombat.SHOT, SandboxCombat.weapon(6, 0).shape);
        assertTrue(SandboxCombat.weapon(0, RunLoot.ROSE).damage > SandboxCombat.weapon(0, 0).damage);
    }

    // Damage

    @Test public void aBlowTakesHealthThrowsTheCreatureAndKillsOrNot() {
        SandboxCombat.Foe slime = new SandboxCombat.Foe(SandboxCombat.SLIME, 10, 10);
        float before = slime.health;
        assertEquals(10, SandboxCombat.hit(slime, 10, 1, 0, 3), 0);
        assertEquals(before - 10, slime.health, 0);
        assertTrue(slime.vx > 0 && slime.flash > 0);
        SandboxCombat.hit(slime, 1000, 1, 0, 3);
        assertTrue(slime.dead);
        assertEquals(0, SandboxCombat.hit(slime, 5, 1, 0, 3), 0);
        SandboxCombat.Foe dummy = new SandboxCombat.Foe(SandboxCombat.DUMMY, 5, 5);
        SandboxCombat.hit(dummy, 100000, 0, 1, 3);
        assertFalse(dummy.dead);
        assertEquals(dummy.max, dummy.health, 0);
    }

    @Test public void aSwingHitsWhatIsInFrontOfHerAndNothingBehind() {
        SandboxCombat.Weapon sword = SandboxCombat.weapon(0, 1);
        ArrayList<SandboxCombat.Foe> foes = new ArrayList<>();
        SandboxCombat.Foe front = new SandboxCombat.Foe(SandboxCombat.DUMMY, 11, 10), behind = new SandboxCombat.Foe(SandboxCombat.DUMMY, 9, 10), far = new SandboxCombat.Foe(SandboxCombat.DUMMY, 20, 10);
        foes.add(front);
        foes.add(behind);
        foes.add(far);
        assertEquals(1, SandboxCombat.swing(sword, 10, 10, 0, foes));
        assertTrue(front.sinceHit == 0 && behind.sinceHit > 0 && far.sinceHit > 0);
        // A scythe goes both ways.
        SandboxCombat.Weapon scythe = SandboxCombat.weapon(11, 1);
        assertTrue(scythe.both);
        assertEquals(2, SandboxCombat.swing(scythe, 10, 10, 0, foes));
        // A slam rings out in front of her.
        SandboxCombat.Weapon hammer = SandboxCombat.weapon(8, 1);
        assertEquals(SandboxCombat.RING, hammer.shape);
    }
}
