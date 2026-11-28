package com.coral.calculator;

/**
 * Crafting with paper, at the smithy. Paper is cut from the paper trees in the lands: plain paper from any of them, fine parchment from
 * those of the middle chapters on, and golden pages from the rarest, late. With it, and with what a smith works with and her pluses:
 * <ul>
 * <li>a paper weapon of any of the twelve kinds is folded (white, or blue with fine paper in it): cheap, light, never as good as the real
 * thing;</li>
 * <li>a weapon found, in her hands, is honed a rarity higher, up to red;</li>
 * <li>a Halgakos Rose weapon is made of each class, from golden pages, late and dear: one for every class;</li>
 * <li>and at the very last, with a Rose of every class made, the Mother Tongue's Quill: the one weapon above all others.</li>
 * </ul>
 */
final class RunCraft {
    private RunCraft() {}

    static final int PLAIN = 0, FINE = 1, GOLDEN = 2, GRADES = 3;
    static final String[] PAPER_NAMES = {"Plain paper", "Fine parchment", "Golden pages"};

    // Trees

    /** How many blows a tree of a grade takes to come down, and how much paper it gives. */
    static int treeHolds(int grade) { return grade == GOLDEN ? 8 : grade == FINE ? 5 : 3; }

    static int treeGives(int grade) { return grade == GOLDEN ? 1 : grade == FINE ? 2 : 3; }

    /** Of a hundred trees in a land, how many are fine and how many golden: none fine before the fourth chapter, none golden before the eighth. */
    static int fineOdds(int chapter) { return chapter < 3 ? 0 : Math.min(45, 15 + (chapter - 3) * 5); }

    static int goldenOdds(int chapter) { return chapter < 7 ? 0 : Math.min(20, 6 + (chapter - 7) * 3); }

    /** A tree's grade from a roll of 0 to 99. */
    static int grade(int chapter, int roll) {
        if (roll < goldenOdds(chapter)) return GOLDEN;
        if (roll < goldenOdds(chapter) + fineOdds(chapter)) return FINE;
        return PLAIN;
    }

    // What is made

    /** What folding a paper weapon costs: plain paper, fine paper for a blue one, and pluses. */
    static final int FOLD_PLAIN = 6, FOLD_FINE = 2, FOLD_PLUSES = 80;

    /** A paper weapon of a kind (its place among the twelve), white or blue. */
    static int folded(int base, boolean blue) { return RunLoot.code(RunLoot.WEAPON, RunLoot.PAPER, base, blue ? RunLoot.BLUE : RunLoot.WHITE); }

    /** What honing a weapon found from a rarity to the next costs: fine paper, gold leaf, and pluses; and whether it can be. */
    static int honeFine(int rarity) { return 2 + rarity * 2; }

    static int honeLeaf(int rarity) { return rarity / 2; }

    static int honePluses(int rarity) { return 150 * (rarity + 1); }

    static boolean honeable(int code) {
        return code != 0 && RunLoot.type(code) == RunLoot.WEAPON && !RunLoot.paper(code) && !RunLoot.ultimate(code) && RunLoot.rarity(code) < RunLoot.RED;
    }

    /** The weapon honed. */
    static int honed(int code) { return code + 1; }

    /** What a Halgakos Rose weapon costs: golden pages, gold leaf, ink crystal and pluses, and the level she must be. */
    static final int ROSE_GOLDEN = 12, ROSE_LEAF = 4, ROSE_CRYSTAL = 4, ROSE_PLUSES = 2500, ROSE_LEVEL = 60;

    /** The Rose of a kind, as it is made: of the last chapter. */
    static int rose(int base) { return RunLoot.code(RunLoot.WEAPON, 11, base, RunLoot.ROSE); }

    /** And the Mother Tongue's Quill: every Rose made first, then golden pages, gold leaf and pluses, at a level. */
    static final int QUILL_GOLDEN = 30, QUILL_LEAF = 12, QUILL_PLUSES = 9999, QUILL_LEVEL = 90, ALL_ROSES = (1 << 13) - 1;
    /** The twelve Roses there were before the knuckles: a save with all of them made has the knuckles' Rose given it, so that none is set back. */
    static final int FIRST_ROSES = (1 << 12) - 1, KINDS = 13;

    /** What a save's Roses are, the knuckles' given where every one of the first twelve was made. */
    static int roses(int made) { return (made & FIRST_ROSES) == FIRST_ROSES ? made | 1 << RunLoot.KNUCKLES : made; }

    static boolean quillOpen(int rosesMade, int level) { return rosesMade == ALL_ROSES && level >= QUILL_LEVEL; }
}
