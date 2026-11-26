package com.coral.calculator;

/**
 * Everything that is said in Plus run. Its story is the webtoon's, A Book Fell from the Sky:
 * the World's Largest Book lands on Halgakos on her day off and she wakes inside it, in
 * Lettera, where Tsubakieh's bat has knocked the meaning off everything, so that even the
 * signs of a sum walk about. Illya means to rewrite the Last Page with herself as its only
 * heroine. Halgakos runs for the Last Page, picking up the pluses her output burst into, with
 * Chikuwa and Rune waiting for her along the way.
 *
 * Each line is its name, its words, who says it and the scene it is said in. The name is
 * the name of its recording, {@code run_v_} before it; the scene is never shown, it is what
 * the voice is told about the moment, so that the line is said the way the moment calls for.
 * Halgakos is H, Chikuwa C, Rune R, Illya I, Tsubakieh T, and the Mawang, the Halgakos who
 * went alone, is M. Every line of Halgakos's ends on her catchphrase; the others have their
 * own ways of speaking. Lines are said when the thing they are about happens, and at no
 * other time.
 */
final class RunStory {
    static final String TITLE = "A Book Fell from the Sky";
    static final String[] CHAPTERS = {"Contents Village", "The Silent Archive", "Heart-Flutter Plaza", "The Index Mountains",
            "The Last Page"};
    /** The full names of those who speak, by their letter. */
    static final String SPEAKERS = "HCRITM";
    static final String[] NAMES = {"Halgakos", "Chikuwa", "Rune", "Illya", "Tsubakieh", "The Mawang"};

    private static final String PLAYER = "She is talking to the player who controls her in a platform game, whom she calls Sloppy. ";

    /** Before anything else, the first time. */
    static final String[][] PROLOGUE = {
            {"pro1", "My one day off. One ice cream. And the World's Largest Book lands on my head Sloppy~♡", "H",
                    PLAYER + "She is complaining about how her day off was ruined: flat, put-upon, disbelieving, a little dramatic."},
            {"pro2", "Welcome to Lettera, Halgakos-ssi♡ You'll translate the Last Page for me, won't you?♡", "I",
                    "A lovely, graceful idol greets the translator she has trapped inside a giant book. Sweet, warm, sincere, charming, a sing-song lilt."},
            {"pro3", "I gave the words a tap with Normal No. 2. Now everything means exactly what it says. Normally.", "T",
                    "A cheerful, unbothered girl with a baseball bat explains, perfectly pleasantly, that she broke the meaning of every word. Bright, deadpan, polite."},
            {"pro4", "She knocked the meaning off everything. Even the sums get up and walk about Sloppy~♡", "H",
                    PLAYER + "She has just seen arithmetic signs walking around on legs. Exasperated, incredulous, almost laughing at how stupid it is."},
            {"pro5", "And my output burst into pluses when I landed. Help me pick them up Sloppy~♡", "H",
                    PLAYER + "She turns to the player and asks for help without wanting to sound like she needs it: bossy, teasing, a little sheepish."},
            {"pro6", "The only way home is the Last Page. So keep up Sloppy~♡", "H",
                    PLAYER + "She sets off. Determined and smug, a challenge in her voice, playful."},
    };

    /** What is said before a land, the first time it is come to: the lands where the story moves on. */
    static String[][] intro(int land) {
        switch (land) {
            case 1: return new String[][]{
                    {"in01a", "Contents Village. Every road here leads to a chapter Sloppy~♡", "H",
                            PLAYER + "She looks over a village built on a book's table of contents. Quick, bright and breezy, talking fast, showing off that she knows where she is."},
                    {"in01b", "Hold the arrows to run. The round button jumps. Even you can manage that Sloppy~♡", "H",
                            PLAYER + "She explains the controls like it is beneath her: teasing, sing-song, smug, fond underneath."}};
            case 3: return new String[][]{
                    {"in03a", "Waaah! My bread turned into a brick! And my sign says fish cake!", "C",
                            "A sunny baker girl is wailing over a loaf of bread that has turned to stone, and over her own name being mistranslated. Loud comic crying, sniffling."},
                    {"in03b", "Names stay names. That's the basics. There. Fixed Sloppy~♡", "H",
                            "She has just fixed a crying baker's mistranslated name with one tap of her book. Smug, casual, showing off, kind underneath."},
                    {"in03c", "Thank you, Halgakos-ssi! I'll wait by the first flag with fresh bread!", "C",
                            "The baker, tears gone the moment she is helped, promises bread in thanks. Bright, bubbly, overjoyed, bouncing."}};
            case 7: return new String[][]{
                    {"in07a", "The Silent Archive. Nobody has opened a book here in years Sloppy~♡", "H",
                            PLAYER + "She steps into a huge, dark, empty library. Hushed, a little uneasy, lowering her voice."},
                    {"in07b", "I made it quiet. A normal library is a quiet library.", "T",
                            "The girl with the bat appears, pleased with herself for scaring every reader away from a library. Cheerful, matter-of-fact, polite."}};
            case 9: return new String[][]{
                    {"in09a", "Y-you read my letter... Then you're my first friend. Forever. Okay? Okay?♡", "R",
                            "A shy, lonely librarian who cries ink has just been read by someone for the first time in years. She stammers through tears, then turns intensely, sweetly clingy."},
                    {"in09b", "Friends go places together. So come along, crybaby Sloppy~♡", "H",
                            "She is flustered by a crying girl's sudden devotion and covers it with teasing. Awkward, warm, a little embarrassed."},
                    {"in09c", "I-I'll wait by the second flag... with my ink. It will take one hurt for you, Halgakos-nim.", "R",
                            "The librarian offers the one thing she has, her ink tears, to protect her new friend. Sniffling, timid, devoted, very soft."}};
            case 13: return new String[][]{
                    {"in13a", "Welcome to Heart-Flutter Plaza♡ I have given nine thousand, nine hundred and ninety-nine compliments. Yours will be the next♡", "I",
                            "The idol welcomes her guest to a pink plaza full of her fans. Radiant, flirtatious, perfectly polite, a performer on her own stage."},
                    {"in13b", "Careful. Her hearts turn my arrows round. Left goes right. Don't you laugh Sloppy~♡", "H",
                            PLAYER + "She warns the player about being charmed, embarrassed that it works on her. Flustered, urgent, defensive."}};
            case 18: return new String[][]{
                    {"in18a", "Halgakos-ssi, you are the most wonderful translator in the world♡ So you'll write that I am the heroine, won't you?♡", "I",
                            "The idol fires her most sincere compliment at the translator, meaning every word of it, to charm her into obeying. Sweet, breathless, coaxing."},
                    {"in18b", "C-compliments only work on me from friends. Probably. Throw something at her Sloppy~♡", "H",
                            PLAYER + "She is blushing and nearly charmed and fighting it. Flustered, stammering, then urgent as she tells the player what to do."}};
            case 19: return new String[][]{
                    {"in19a", "The Index Mountains. An index is always on the translator's side Sloppy~♡", "H",
                            PLAYER + "She stands at the foot of mountains shaped like index tabs. Confident, proud, grinning."},
                    {"in19b", "So another one of Us has come. Turn back. It is easier alone.", "M",
                            "A masked, older version of the heroine, who has lived alone in the back of the book for sixty years, speaks from far away. Cold, weary, hollow, slow."}};
            case 24: return new String[][]{
                    {"in24a", "Found you. Normally. I cut the bridge, so I will bat you across. One at a time.", "T",
                            "The girl with the bat blocks the way on a mountain top, cheerful as ever, offering to hit the heroine across a ravine as if it were a favour. Bright, deadpan."},
                    {"in24b", "Don't throw anything. She bats it straight back. Jump on her after she swings Sloppy~♡", "H",
                            PLAYER + "She gives the player quick, tense instructions before a fight. Urgent, focused, bossy."}};
            case 25: return new String[][]{
                    {"in25a", "Friends are a burden. Set them down, and I will fly you across. Alone.", "M",
                            "The older, lonely version of the heroine offers to carry her to the end if she abandons her friends. Grave, tired, tempting, quiet."},
                    {"in25b", "A burden shared weighs half. I'm not going alone Sloppy~♡", "H",
                            "She answers her own lonely future self. Quiet and firm at first, then defiant, with her friends behind her."}};
            case 30: return new String[][]{
                    {"in30a", "I have written it already♡ Heroine: Illya. Supporting cast: none♡", "I",
                            "The idol stands on the last page of the book, sure she has won. Triumphant, sweet, sing-song, a little too bright."},
                    {"in30b", "And her 'none' got up and walked. That's Zero. Nothing thrown gets through him Sloppy~♡", "H",
                            PLAYER + "She faces the final enemy, a giant zero. Grim, focused, a dry edge of humour."},
                    {"in30c", "Sixty years of notes. I could not finish them alone. Take them.", "M",
                            "The lonely older self, at last, hands over her life's work to help. Softened, hesitant, close to tears but composed."}};
            default: return null;
        }
    }

    /** What is said after a land is cleared, the first time: where one of them has been dealt with, and at the end. */
    static String[][] outro(int land) {
        switch (land) {
            case 18: return new String[][]{
                    {"out18a", "Oh my... Nobody has ever said no to me before♡ I will see you on the Last Page♡", "I",
                            "The idol has just lost for the first time. Shaken, a wobble in her voice, but keeping her composure and her smile."},
                    {"out18b", "Scan says: compliments received, zero. Huh. That's a little sad Sloppy~♡", "H",
                            PLAYER + "She reads a scan of her enemy and finds out nobody has ever complimented her back. Surprised, then unexpectedly gentle."}};
            case 24: return new String[][]{
                    {"out24a", "Normal No. 2 has broken. Normally. I'm sorry. Is that what a normal friend says?", "T",
                            "Her bat has snapped in two. She is mildly stunned, then sincerely sorry, asking an honest question. Deadpan turning soft."},
                    {"out24b", "Bread can always be baked again. So can friends!", "C",
                            "The baker forgives the girl who cursed her bakery, offering her bread. Warm, sunny, generous."},
                    {"out24c", "The curse is lifting. Words mean things again. About time Sloppy~♡", "H",
                            PLAYER + "The curse on the book is breaking. Relieved, pleased with herself, a happy sigh."}};
            case 30: return new String[][]{
                    {"out30a", "Whoever reads me to the end is the hero of this story. That's what the Last Page says Sloppy~♡", "H",
                            "She reads the last line of the book aloud, slowly, and it moves her more than she expected. Soft, wondering, then a small laugh."},
                    {"out30b", "I only wanted to be loved... You're complimenting me? Me?♡", "I",
                            "The defeated idol is being complimented, sincerely, for the first time in her life. Crying, overwhelmed, disbelieving, happy."},
                    {"out30c", "Cast: everyone. That seems normal.", "T",
                            "She reads the rewritten last page, which now lists everybody. Content, deadpan, quietly pleased."},
                    {"out30d", "I'm... still watching you, Halgakos-nim♡", "R",
                            "The crying librarian whispers from very close by. Sweet, hushed, devoted, a tiny bit frightening."},
                    {"out30e", "Everyone gets bread!", "C",
                            "The baker hands bread to the whole cast at the happy ending. Joyful, loud, bubbling over."},
                    {"out30f", "My output is back. And you read to the end. Not bad Sloppy~♡", "H",
                            PLAYER + "The adventure is over and she is going home. Proud, fond, teasing, the closest she comes to saying thank you."}};
            default: return null;
        }
    }

    /** At the start of a land that has nothing of its own to say. */
    static final String[][] START = {
            {"go1", "Next page. The gate is at the far end Sloppy~♡", "H", PLAYER + "A new level begins. Brisk, ready, a little bossy."},
            {"go2", "Stretch your thumbs. Here we go Sloppy~♡", "H", PLAYER + "A new level begins. Playful, teasing, energetic."},
            {"go3", "Pluses first. Gate after. Simple Sloppy~♡", "H", PLAYER + "A new level begins. Smug, matter-of-fact, sing-song."},
            {"go4", "Another page. Try to keep up Sloppy~♡", "H", PLAYER + "A new level begins. Confident, challenging, grinning."},
    };

    /** The first time each thing comes into view, ever. In the order of {@link RunGame}'s numbers for them. */
    static final String[][] SEE = {
            {"see0", "A minus sign with legs. Land on its head Sloppy~♡", "H", PLAYER + "A small walking enemy appears for the first time. Amused, pointing it out, instructing."},
            {"see1", "A times sign. Spikes on every arm. Don't land on that one Sloppy~♡", "H", PLAYER + "A spiky enemy appears for the first time. Wary, warning, firm."},
            {"see2", "A divide sign. It hops. Go under it or come down on it Sloppy~♡", "H", PLAYER + "A hopping enemy appears for the first time. Thinking aloud, then advising."},
            {"see3", "That one throws knives. Jump them Sloppy~♡", "H", PLAYER + "An enemy that throws knives appears for the first time. Alarmed, quick warning."},
            {"see4", "That one shoots its dots. Keep low Sloppy~♡", "H", PLAYER + "An enemy that shoots appears for the first time. Tense, quick warning."},
            {"see5", "Spikes. Jump them. Obviously Sloppy~♡", "H", PLAYER + "Spikes on the ground ahead for the first time. Dry, sarcastic, teasing."},
            {"see6", "A spring. Step on it and hold on Sloppy~♡", "H", PLAYER + "A bounce spring ahead for the first time. Pleased, a little excited."},
            {"see7", "A flag. Fall after this and I come back here. Not that I plan to fall Sloppy~♡", "H", PLAYER + "A checkpoint flag ahead for the first time. Explaining, then proud and defensive."},
            {"see8", "Something to fight with. Go and get it Sloppy~♡", "H", PLAYER + "A weapon lying on the ground ahead for the first time. Eager, greedy, mischievous."},
            {"see9", "There's the gate. Run for it Sloppy~♡", "H", PLAYER + "The exit gate comes into view for the first time. Excited, urging."},
    };

    /** On picking up each of the things to fight with, in the order {@link PlusRun} numbers them. */
    static final String[][] ARMS = {
            {"arm0", "A pen. Mightier than the sword Sloppy~♡", "H", PLAYER + "She picks up an oversized fountain pen to throw. Smug, pleased with her own joke."},
            {"arm1", "A throwing knife. Stand well back Sloppy~♡", "H", PLAYER + "She picks up a throwing knife. Mischievous, a mock-dangerous purr."},
            {"arm2", "A heavy book. Knowledge hurts Sloppy~♡", "H", PLAYER + "She picks up a thick book to lob at enemies. Gleeful, grunting a little at the weight."},
            {"arm3", "A giant brush. Time to paint them out Sloppy~♡", "H", PLAYER + "She picks up a huge paint brush. Delighted, playful."},
            {"arm4", "A sword. Now it's a fair fight Sloppy~♡", "H", PLAYER + "She picks up a sword. Confident, heroic, enjoying herself."},
            {"arm5", "An ink gun. Oh, this is going to get messy Sloppy~♡", "H", PLAYER + "She picks up a toy ink blaster. Thrilled, wicked giggle."},
    };

    /** On picking up what helps, in the order {@link PlusRun} numbers it; bread and ink are handed over by a friend, who speaks. */
    static final String[][] TOOK = {
            {"took0", "Fresh from the oven! One heart back!", "C", "The baker hands her friend a warm bread roll in the middle of an adventure. Bright, proud, cheerful."},
            {"took1", "Ice cream! Finally. Now watch me run Sloppy~♡", "H", PLAYER + "She finds the ice cream she has wanted all day and gets a burst of speed. Overjoyed, giddy, fast."},
            {"took2", "T-take my ink... it will be hurt instead of you.", "R", "The crying librarian wraps her friend in a protective bubble of her ink tears. Sniffling, shy, earnest."},
            {"took3", "A magnet. Come here, pluses Sloppy~♡", "H", PLAYER + "She picks up a magnet that pulls collectible plus signs to her. Smug, beckoning, sing-song."},
            {"took4", "Her notes. Nothing can touch me now Sloppy~♡", "H", PLAYER + "She picks up glowing notes that make her invincible for a few seconds. Bold, exhilarated."},
    };
    /** When bread comes with all her hearts full, and when the ink takes a hurt for her. */
    static final String[][] FULL = {
            {"full1", "You're not hurt at all! Have it anyway!", "C", "The baker gives bread to a friend who does not need healing. Laughing, generous."}};
    static final String[][] SPARED = {
            {"spared1", "The ink took that one. Thanks, crybaby Sloppy~♡", "H", "A protective bubble has just burst, saving her from a hit. Startled, then relieved and fond."}};

    /** When she is hurt, by what hurt her. */
    static final String[][] FELL = {
            {"fell1", "Hey! I fell! Mind the gaps Sloppy~♡", "H", PLAYER + "She has just fallen down a hole because of the player. Indignant, yelping, scolding."},
            {"fell2", "Do I look like I can fly? Don't answer that Sloppy~♡", "H", PLAYER + "She has just fallen down a hole, and she does have wings. Sarcastic, huffy, embarrassed."},
    };
    static final String[][] BITTEN = {
            {"bit1", "Ow! It bumped me. Watch where I'm going Sloppy~♡", "H", PLAYER + "She has just walked into an enemy and been hurt. A yelp of pain, then scolding."},
            {"bit2", "That hurt! Be careful with me Sloppy~♡", "H", PLAYER + "She has just been hurt by an enemy. Pained, pouting, complaining."},
    };
    static final String[][] SHOT = {
            {"shot1", "Ow! I said jump the knives Sloppy~♡", "H", PLAYER + "She has just been hit by a thrown knife. A sharp yelp, then exasperated."},
            {"shot2", "It got me. Duck next time Sloppy~♡", "H", PLAYER + "She has just been hit by a projectile. Wincing, annoyed."},
    };
    static final String[][] SPIKED = {
            {"spike1", "Spikes! Right in the socks Sloppy~♡", "H", PLAYER + "She has just stepped on spikes. A pained squeal, hopping, outraged."}};
    static final String[][] BATTED = {
            {"bat1", "Ow! Not the bat. Get behind her Sloppy~♡", "H", PLAYER + "She has just been hit by a baseball bat. Dazed, pained, urgent."}};
    static final String[][] CHARMED = {
            {"charm1", "Wh-what? My legs. Everything's backwards Sloppy~♡", "H", PLAYER + "She has just been charmed by a heart and her controls are reversed. Dizzy, blushing, flustered, giggly against her will."}};
    static final String[][] LAST_HEART = {
            {"last1", "One heart left. Don't you dare Sloppy~♡", "H", PLAYER + "She is down to her last life. Nervous, threatening, tense whisper."}};

    /** When things go well. */
    static final String[][] SQUISH = {
            {"squish1", "Squish Sloppy~♡", "H", PLAYER + "She has just jumped on an enemy and flattened it. Short, gleeful, sing-song."},
            {"squish2", "Flat as a page Sloppy~♡", "H", PLAYER + "She has just flattened an enemy. Smug, satisfied."},
    };
    static final String[][] COMBO = {
            {"combo1", "Three in a row! Did you see that Sloppy~♡", "H", PLAYER + "She has just bounced on three enemies without touching the ground. Thrilled, breathless, showing off."}};
    static final String[][] FLAG = {
            {"flag1", "Flag. Good. I'm not walking all that again Sloppy~♡", "H", PLAYER + "She has just passed a checkpoint. Relieved, a little out of breath."}};
    static final String[][] BLOWS = {
            {"blow0", "Take that, nothing Sloppy~♡", "H", PLAYER + "She has just landed a hit on the final boss, a giant zero. Fierce, triumphant."},
            {"blow1", "Kyaa! That is not how you treat an idol♡", "I", "The idol has just been hit. A squeal, shocked, flustered, indignant but still polite."},
            {"blow2", "Ow. That was a normal amount of pain.", "T", "The girl with the bat has just been hit on the head. Mildly surprised, deadpan."},
    };
    /** What Illya and Tsubakieh say as they go at her. */
    static final String[][] AIM = {
            {"aim1", "Look this way♡", "I", "The idol fires a heart beam at her opponent in a fight. Playful, sing-song, flirtatious."},
            {"aim2", "Batter up. Normally.", "T", "She raises her baseball bat to swing at her opponent. Cheerful, calm, deadpan."},
    };

    /** At the end of a land: by how many stars, and out of hearts. */
    static final String[][] WON = {
            {"won1", "We made it. Barely. Pick up more pluses next time Sloppy~♡", "H", PLAYER + "The level is finished but with few plus signs collected. Relieved, grudging, nagging."},
            {"won2", "Not bad. For you Sloppy~♡", "H", PLAYER + "The level is finished fairly well. Pleased, teasing, a backhanded compliment."},
            {"won3", "Every plus. Fine. I'm impressed Sloppy~♡", "H", PLAYER + "The level is finished perfectly. Genuinely impressed and trying to hide it, warm."},
    };
    static final String[][] LOST = {
            {"lose1", "I'm not crying. You're crying Sloppy~♡", "H", PLAYER + "She has lost all her lives and is sitting on the ground in tears. Sniffling, sulking, denying it."},
            {"lose2", "Out of hearts. Carry me next time Sloppy~♡", "H", PLAYER + "She has lost all her lives. Pouting, tired, whiny."},
            {"lose3", "Hmph. One more try. I'm not done Sloppy~♡", "H", PLAYER + "She has lost all her lives but refuses to give up. Huffy, stubborn, determined."},
    };

    /** At the counter where her pluses buy what she keeps. */
    static final String[][] SHOP = {
            {"shop1", "The guild counter. Spend my pluses wisely Sloppy~♡", "H", PLAYER + "She opens the upgrade shop. Businesslike, possessive of her coins, teasing."},
            {"shop2", "Ooh. I like that one Sloppy~♡", "H", PLAYER + "The player has just bought her an upgrade. Delighted, greedy, pleased."},
            {"shop3", "Not enough pluses. Go and find more Sloppy~♡", "H", PLAYER + "The player cannot afford an upgrade. Dismissive, sing-song, shooing."},
    };

    private RunStory() {}

    /** Which of those who speak says a line: 0 for Halgakos. */
    static int speaker(String[] line) { return SPEAKERS.indexOf(line[2]); }

    /** Every line there is, for checking them and for recording them. */
    static String[][][] all() {
        java.util.List<String[][]> groups = new java.util.ArrayList<>();
        groups.add(PROLOGUE);
        for (int land = 1; land <= RunLevels.COUNT; land++) {
            if (intro(land) != null) groups.add(intro(land));
            if (outro(land) != null) groups.add(outro(land));
        }
        java.util.Collections.addAll(groups, START, SEE, ARMS, TOOK, FULL, SPARED, FELL, BITTEN, SHOT, SPIKED, BATTED, CHARMED, LAST_HEART,
                SQUISH, COMBO, FLAG, BLOWS, AIM, WON, LOST, SHOP);
        return groups.toArray(new String[0][][]);
    }
}
