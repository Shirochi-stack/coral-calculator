package com.coral.calculator;

/**
 * Who fights in Page brawl, and what each of them says. They are the five of the webtoon
 * A Book Fell from the Sky: Halgakos the translator, Chikuwa the baker, Tsubakieh who is
 * completely normal, Rune the crying librarian, and Illya the idol.
 *
 * Every line has a name, and under that name a recording among the raw resources,
 * {@code fight_v_} before it, made by tools/make_fight_voice.py. The first letter of the name
 * says whose line it is, and so in whose voice it was recorded.
 */
final class FightCast {
    static final String GAME = "Page brawl";
    static final String[] NAMES = {"Halgakos", "Chikuwa", "Tsubakieh", "Rune", "Illya"};
    /** The letter each one's lines begin with, and the name of her pictures. */
    static final String[] LETTERS = {"h", "c", "t", "r", "i"};
    /** How each fights, in a word or two, and what she fights with. */
    static final String[] STYLES = {"Mid range", "Close and heavy", "Close range", "Long range", "All range, no reach"};
    static final String[] WEAPONS = {
            "Glossarion and a ring of API keys",
            "Brick bread, a baguette and bread rolls",
            "Normal No. 2, a white baseball bat",
            "Ink tears and a paper knife",
            "Heart beams and pink magic",
    };
    /** What each one's four moves are called: the quick one, the heavy one, the special and the ultimate. */
    static final String[][] MOVES = {
            {"Key ring", "Key thrust", "QA scan", "Chunked translation"},
            {"Baguette swat", "Brick bread smash", "Bread roll", "Brick bread drop"},
            {"Bat poke", "Full swing", "Boomerang bat", "Normal home run"},
            {"Ink flick", "Paper knife", "Ink tear", "Flood of tears"},
            {"Heart pop", "Pink magic", "Heart beam", "Grand compliment"},
    };
    /** A word on how to play each, for the screen where one is picked. */
    static final String[] TIPS = {
            "Keys up close, the book from the middle. Her scan does not fly far.",
            "Slow, sturdy, and hits hardest. Get close; throw bread on the way in.",
            "Fastest on her feet. The bat comes back when thrown, and she hits softer without it.",
            "Keep away and cry at them. Every third tear is a knife.",
            "Everything she has is thrown. Up close she can only push them off.",
    };
    /** The colour each is known by: her hair, for her name and her bars. */
    static final int[] COLOURS = {0xFFE88A96, 0xFFC98A4B, 0xFF8E6C9C, 0xFF6FB6E8, 0xFFFFA36C};

    /** What a line is for; a fighter's lines are in this order. */
    static final int PICK = 0, INTRO = 1, INTRO_TOO = 2, REPLY = 3, SPECIAL = 4, ULTIMATE = 5, HIT = 6, HIT_TOO = 7, WIN = 8, LOSE = 9;

    /**
     * Their lines. The first of each is her catchphrase, said when she is picked. Halgakos
     * ends every line on hers, as she does everywhere; and what she cries when she loses is
     * the cry she keeps for when her output has really gone.
     */
    static final String[][][] LINES = {
            {
                    {"hpick", "I've got plenty of keys. Next key Sloppy~♡"},
                    {"hintro1", "Translation is my specialty. Watch and learn Sloppy~♡"},
                    {"hintro2", "It's my day off. Make this quick Sloppy~♡"},
                    {"hreply", "Ehh~? You want to fight me? Cute Sloppy~♡"},
                    {"hspecial", "QA scan Sloppy~♡"},
                    {"hult", "Names stay names. Translation complete Sloppy~♡"},
                    {"hhit1", "Ow! Rude Sloppy~♡"},
                    {"hhit2", "Tch. That key gave out Sloppy~♡"},
                    {"hwin", "Next key. Next challenger Sloppy~♡"},
                    {"hlose", "Where did my output go Sloppy~♡"},
            },
            {
                    {"cpick", "Over here! I've got bread."},
                    {"cintro1", "I'm not fish cake! I'm bread."},
                    {"cintro2", "Let's settle this before the dough rises."},
                    {"creply", "A fight? But I only brought forty loaves."},
                    {"cspecial", "Fresh from the oven!"},
                    {"cult", "Brick bread! Even a hammer can't crack it!"},
                    {"chit1", "Waaah! My bread!"},
                    {"chit2", "Ow ow ow!"},
                    {"cwin", "Bread can always be baked again. Want some?"},
                    {"close", "My bread... my bread turned into a brick... waaah!"},
            },
            {
                    {"tpick", "Caught you. Normally."},
                    {"tintro1", "This is Normal No. 2. No. 1 broke while I was hunting a dragon."},
                    {"tintro2", "Let's have a perfectly normal fight."},
                    {"treply", "Found you. Normally."},
                    {"tspecial", "A normal throw."},
                    {"tult", "A completely, absolutely normal home run."},
                    {"thit1", "Ah. That hurt a normal amount."},
                    {"thit2", "How abnormal."},
                    {"twin", "A perfectly normal victory."},
                    {"tlose", "Normal No. 2 has... broken normally."},
            },
            {
                    {"rpick", "The only one allowed to compliment Halgakos-nim... is me♡"},
                    {"rintro1", "P-please don't hurt me... sniff... or I'll have to write it down."},
                    {"rintro2", "U-um... I'll do my best... sniff."},
                    {"rreply", "Y-you came all this way... to see me? Forever, then. Yes? Yes?♡"},
                    {"rspecial", "Uwaaah... m-my tears!"},
                    {"rult", "S-stay with me... forever... okay? Okay?♡"},
                    {"rhit1", "Waah... sniff..."},
                    {"rhit2", "T-that hurts..."},
                    {"rwin", "I-I won...? Sniff... I'm writing it in my diary..."},
                    {"rlose", "Uwaaah... I lost... sniff... d-don't leave me..."},
            },
            {
                    {"ipick", "Just as planned♡"},
                    {"iintro1", "You're the most wonderful opponent in the world♡ So you'll lose for me, won't you?♡"},
                    {"iintro2", "Nine thousand, nine hundred and ninety-nine compliments. Here comes one more♡"},
                    {"ireply", "Oh my... how frightening♡"},
                    {"ispecial", "Heart beam♡"},
                    {"iult", "Heroine: Illya♡ Supporting cast: none♡"},
                    {"ihit1", "Kyaa!"},
                    {"ihit2", "H-how rude♡"},
                    {"iwin", "Thank you for the lovely fight♡ You were truly wonderful♡"},
                    {"ilose", "I just... wanted to be loved..."},
            },
    };

    private FightCast() {}

    static String[] line(int fighter, int which) { return LINES[fighter][which]; }
}
