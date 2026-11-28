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
            "The Last Page", "The Half-Title Palace", "The Gazette Press", "Between the Lines", "The Slime Margins", "The Appendix",
            "The Unread Shore", "The Dragon's Library"};
    /** The third volume. */
    static final String THIRD = "Volume Three";
    /** The second volume, which falls on her head as the first did. */
    static final String SEQUEL = "Volume Two";
    /** The full names of those who speak, by their letter. */
    static final String SPEAKERS = "HCRITMGJKYNSUBOPEAWLDFQVXZabcdefh";
    static final String[] NAMES = {"Halgakos", "Chikuwa", "Rune", "Illya", "Tsubakieh", "The Mawang", "Gokhae", "Miri", "Gari", "Yeobaek",
            "Nuri-yeowang",
            "Somi", "Inju", "Boksun-halmeoni", "Ttosun", "Galpi", "Yeseo", "Nabi", "Moru", "Altteul",
            "The Guild Guard", "Penchok-jipsa", "Byeoru", "Jupan", "Sumbi", "The Brake Pads", "The Straight-Ahead Party", "The Old Book",
            "Pulmu", "Sutdol", "Lucy", "The Mother Tongue", "The Vampire Merchant"};

    private static final String PLAYER = "She is talking to the player who controls her in a platform game, whom she calls Sloppy. ";

    /** She talks fast: said of most of her lines of the second volume, after what the moment is. */
    private static final String FAST = " Quick and lively, talking fast.";

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
                    {"in03s", "Your name got copied by someone who never read it. That's a sloppy copy. Lettera's full of them now, and I can't stand a single one Sloppy~♡", "H",
                            "She explains to the baker what went wrong with her name: a careless, unread copy of a word. Then it turns personal: a translator's real distaste, firm and a little fierce, then her playful sign-off."},
                    {"in03c", "Thank you, Halgakos-ssi! I'll wait by the first flag with fresh bread!", "C",
                            "The baker, tears gone the moment she is helped, promises bread in thanks. Bright, bubbly, overjoyed, bouncing."}};
            case 7: return new String[][]{
                    {"in07a", "The Silent Archive. Nobody has opened a book here in years Sloppy~♡", "H",
                            PLAYER + "She steps into a huge, dark, empty library. Hushed, a little uneasy, lowering her voice."},
                    {"in07b", "I made it quiet. A normal library is a quiet library.", "T",
                            "The girl with the bat appears, pleased with herself for scaring every reader away from a library. Cheerful, matter-of-fact, polite."}};
            case 9: return new String[][]{
                    {"in09a", "Y-you read it... sniff... You're my friend now. F-forever, okay? Okay?♡", "R",
                            "A lonely little librarian who cries ink has just had her letter read by someone for the first time in years. She is a little crybaby sobbing her way through the words: hiccups, sniffles, a wobbling whine, childish and small. Then clingy and hopeful."},
                    {"in09b", "Friends go places together. So come along, crybaby Sloppy~♡", "H",
                            "She is flustered by a crying girl's sudden devotion and covers it with teasing. Awkward, warm, a little embarrassed."},
                    {"in09c", "I-I'll wait by the second flag... sniff... My ink gets hurt, n-not you, Halgakos-nim.", "R",
                            "The little librarian offers the one thing she has, her ink tears, to protect her new friend. She is a little crybaby sobbing her way through the words: hiccups, sniffles, a wobbling whine, childish and small. Timid and devoted."}};
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
                    {"in30b", "And her 'none' got up and walked. That's Zero. Nothing thrown gets through it Sloppy~♡", "H",
                            PLAYER + "She faces the final enemy, a giant zero. Grim, focused, a dry edge of humour."},
                    {"in30c", "Sixty years of notes. I could not finish them alone. Take them.", "M",
                            "The lonely older self, at last, hands over her life's work to help. Softened, hesitant, close to tears but composed."}};
            case 31: return new String[][]{
                    {"v2a", "Volume Two is falling on your head, Translator-ssi. I will hit it for you. Normally.", "T",
                            "A cheerful, unbothered girl with a mended baseball bat, now a friend, calmly announces she will bat away a giant book that is falling out of the sky. Light, cute, bright, deadpan."},
                    {"v2b", "You hit it INTO me! I'm inside a book again Sloppy~♡", "H",
                            PLAYER + "She has just been knocked into a second book by a friend's home run. Indignant, yelping, talking very fast, outraged and a little amused."},
                    {"v2c", "Translator-nim. We sealed the last page: Cast, everyone. Someone has written one more word after it.", "N",
                            "A young queen in a red royal robe, who has only just learned to speak for herself, reports a crime to the translator she trusts. Formal, earnest, worried, speaking of herself as We."},
                    {"v2d", "It is being said that the seal now reads: Cast, everyone else. A regent is, regrettably, required.", "G",
                            "The queen's sly interpreter, hiding a smile behind a black fan, explains in stiff, over-formal words how she has stolen the kingdom by adding one word. Smug, silky, pleased with herself."},
                    {"v2e", "Gokhae. One twisted word, and the whole cast is written out. Fine. I'll fix it page by page Sloppy~♡", "H",
                            PLAYER + "She works out what the villain did and sets off to undo it. Annoyed, brisk, determined, talking fast."}};
            case 40: return new String[][]{
                    {"in40a", "It is being reported that the translator has arrived. How very literal of her.", "G",
                            "The sly interpreter greets her enemy in the throne hall from behind her fan, mocking her in stiff, over-formal words. Cool, amused, superior."},
                    {"in40b", "Gokhae! Our words are Our own. Give them back!", "N",
                            "The young queen stands up to the advisor who used to speak for her. Brave, voice trembling, raising it for the first time."},
                    {"in40c", "She hides behind that fan. Wait till she lowers it to talk, then land on her Sloppy~♡", "H",
                            PLAYER + "She gives the player quick, tense instructions before a fight." + FAST}};
            case 41: return new String[][]{
                    {"in41a", "Extra, extra! Spoiler: the translator loses in chapter seven. Spoiler alert!", "J",
                            "A bright, gossipy newspaper girl who reads ahead in every story shouts her headline with glee. Very fast, sing-song, delighted with herself."},
                    {"in41b", "Shh. That part is censored.", "K",
                            "Her shy twin sister, who hides her eyes behind a black bar and is afraid of being read, whispers. Tiny, hushed, timid."},
                    {"in41c", "The Gazette Twins. One spoils it, the other blacks it out. And mind the molten type Sloppy~♡", "H",
                            PLAYER + "She sizes up two odd sisters in a printing house full of hot metal. Wry, dry." + FAST}};
            case 50: return new String[][]{
                    {"in50a", "Next scene: you jump left. See? I read ahead. Spoiler alert!", "J",
                            "The newspaper girl, looking through her rolled-up paper like a spyglass, taunts her opponent before a fight. Gleeful, cocky, very fast."},
                    {"in50b", "Please do not look at me. I will cover the page.", "K",
                            "Her shy twin lifts a huge ink brush to paint black bars over everything. Whispering, nervous, apologetic."},
                    {"in50c", "Miri throws at where I'm going. So change your mind a lot Sloppy~♡", "H",
                            PLAYER + "She gives the player a trick for the fight. Sly, amused." + FAST}};
            case 51: return new String[][]{
                    {"in51a", "Between the lines. Everyone she wrote out fell in here. It's very white Sloppy~♡", "H",
                            PLAYER + "She steps into an empty white world like a blank page. Hushed, uneasy, looking around, speaking quickly and quietly."},
                    {"in51b", "Mmm... who is stepping on my line... five more minutes...", "Y",
                            "A swordswoman who is nearly always asleep mumbles without opening her eyes. Very sleepy, slow, drawling, yawning."}};
            case 60: return new String[][]{
                    {"in60a", "You woke me. A guard who is woken has to cut something. Sorry. Mmm.", "Y",
                            "The sleepy swordswoman apologises, half awake, for the fight she is about to start. Slow, soft, yawning, polite."},
                    {"in60b", "She steps out behind me, cuts, and dozes off. Jump the cut. Get her while she naps Sloppy~♡", "H",
                            PLAYER + "She gives the player quick, tense instructions before a fight." + FAST}};
            case 61: return new String[][]{
                    {"in61a", "Halgakos-ssi! The margins are full of slimes again! They ate my recipe notes!", "C",
                            "The sunny baker girl runs up in a comic panic about slimes. Loud, bright, flustered, high."},
                    {"in61b", "Every meaning she knocked loose ends up here. It's slippery. Don't laugh when I slide Sloppy~♡", "H",
                            PLAYER + "She looks over hills of green slime and warns the player, embarrassed in advance." + FAST}};
            case 70: return new String[][]{
                    {"in70a", "Uu... h-he's so big... sniff... I'll d-draw the lines... waaah...", "R",
                            "The little crybaby librarian, terrified of a giant slime, offers to help anyway. She is sobbing and hiccuping through it, tiny and wobbly."},
                    {"in70b", "King Slime. We chunk him. Three cuts. And be in the air when he lands Sloppy~♡", "H",
                            PLAYER + "She gives the player the plan against a huge bouncing slime king. Focused, bossy." + FAST}};
            case 71: return new String[][]{
                    {"in71a", "Halgakos-ssi♡ I won the Volume Two audition! But on the heroine's page someone's name has been crossed out♡", "I",
                            "The magical-girl idol bursts in with happy news, then turns puzzled and a little worried. Bright, sweet, high, sparkling, then softer."},
                    {"in71b", "The Mawang. Gokhae wrote her back in as the villain. She won't remember us Sloppy~♡", "H",
                            PLAYER + "She realises her lonely other self has been made into a monster again. Quiet, serious, low, not teasing for once."}};
            case 80: return new String[][]{
                    {"in80a", "A demon king needs no one. That is what is written. Come no closer.", "M",
                            "The lonely older self, rewritten as a villain and no longer remembering her friends, warns them off. Cold, hollow, slow, but her voice wavers."},
                    {"in80b", "That's her line, not yours. I'm not here to fight you. I'm bringing you home Sloppy~♡", "H",
                            "She speaks to her own lonely other self. Soft, steady and sure, gentle, then a little cheeky at the end."},
                    {"in80c", "We're right beside you, Halgakos-ssi! The bread is here!", "C",
                            "The baker calls out from the edge of the last fight, cheering her friend on. Loud, bright, warm, high."},
                    {"in80d", "M-my ink too... sniff... d-don't be alone... uu...", "R",
                            "The little crybaby librarian offers her ink before the last fight. She is sobbing softly, sniffling between every word, tiny and devoted."}};
            case 81: return new String[][]{
                    {"in81a", "Volume Three. It wasn't due. And every page of it stops halfway through Sloppy~♡", "H",
                            PLAYER + "A third giant book has fallen on her, and it is broken: every page stops mid-sentence. Dry, uneasy under the joke." + FAST},
                    {"in81b", "Something is unwriting it from the far end. I know that kind of quiet. This time I walk with you.", "M",
                            "The lonely older self, now a friend, recognises a loneliness like her own and offers to come along. Low, calm, steady, kind."},
                    {"in81c", "They call her the Great Translation Killer, translator-nim. Every series she touches stops at a name.", "N",
                            "The young queen passes on what her people fear, gravely. Clear, earnest, formal, worried."},
                    {"in81d", "Then I want everyone. Every friend I made in two volumes. Start pledging Sloppy~♡", "H",
                            PLAYER + "She decides she will need all her friends for what is coming, and says so as an order. Bossy, bright, determined." + FAST}};
            case 90: return new String[][]{
                    {"in90a", "This is where I pledge. Sixty years I needed no one. I was wrong for all sixty.", "M",
                            "The lonely older self makes a promise to her friend at the edge of the sea. Low, quiet, a little rueful, warm."},
                    {"in90b", "Noted. Underlined. I'm keeping that one Sloppy~♡", "H",
                            PLAYER + "She is touched by a promise and hides it behind a joke. Soft, then teasing." + FAST}};
            case 91: return new String[][]{
                    {"in91a", "H-Halgakos-nim... sniff... the library at the end... n-nothing in it is translated any more... uu...", "R",
                            "The little crybaby librarian reports, in tears, that a great library has lost all its translations. Sobbing, hiccuping, frightened."},
                    {"in91b", "A dragon's library. And someone small living in it alone. I don't like where this is going Sloppy~♡", "H",
                            PLAYER + "She looks up at a vast ruined library and guesses who lives there. Quiet, uneasy, serious for once."}};
            case 100: return new String[][]{
                    {"in100a", "The last land. The Dragon of Lost Words is at the end of it. Everyone with me Sloppy~♡", "H",
                            PLAYER + "She stands before the final level with all her friends behind her. Steady, proud, a little nervous." + FAST},
                    {"in100s", "Every sloppy copy began with someone who wouldn't listen. I'm listening. All the way to the last word Sloppy~♡", "H",
                            PLAYER + "Before the final level she says what all the careless, unread copies in this world came from, and what she will do instead. Quiet, sincere, steady, then a small brave smile."}};
            default: return null;
        }
    }

    /** What is said after a land is cleared, the first time: where one of them has been dealt with, and at the end. */
    static String[][] outro(int land) {
        switch (land) {
            case 6: return new String[][]{
                    {"out6a", "Passed the guild exam. They gave me a heart for it. Not bad Sloppy~♡", "H",
                            PLAYER + "She has just beaten the guild's guard in an entrance exam and been given an extra heart. Proud, pleased." + FAST}};
            case 12: return new String[][]{
                    {"out12a", "A butler who is a pen. She refilled my ink gun, eight shots bigger Sloppy~♡", "H",
                            PLAYER + "She has just beaten a fountain-pen lady butler, who politely enlarged her ink gun. Amused, pleased." + FAST}};
            case 35: return new String[][]{
                    {"out35a", "The scribe wrote me back in. And my colours come quicker now Sloppy~♡", "H",
                            PLAYER + "She has just beaten the court scribe, who has put her back in the record. Satisfied, bright." + FAST}};
            case 45: return new String[][]{
                    {"out45a", "She paid out. Two hundred pluses, straight to the bank Sloppy~♡", "H",
                            PLAYER + "She has just beaten a toll-taking merchant and taken a big payout. Gleeful, greedy." + FAST}};
            case 55: return new String[][]{
                    {"out55a", "The diver brought me a heart from the bottom of the page Sloppy~♡", "H",
                            PLAYER + "She has just beaten a diver who gives her an extra heart. Warm, pleased." + FAST}};
            case 65: return new String[][]{
                    {"out65a", "Worn out, both of them. They paid a bounty just to stop Sloppy~♡", "H",
                            PLAYER + "She has just worn down two brake pads, who pay her to stop. Amused, smug." + FAST}};
            case 75: return new String[][]{
                    {"out75a", "They went straight home. And left me a bigger ink tank Sloppy~♡", "H",
                            PLAYER + "She has just beaten the three adventurers who only go straight. Dry, pleased." + FAST}};
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
                    {"out30d", "Ehehe... sniff... I-I'm still watching... Halgakos-nim♡", "R",
                            "The little crybaby librarian whispers from very close by, giggling through her sniffles. Small, hushed, clingy, a tiny bit frightening."},
                    {"out30e", "Everyone gets bread!", "C",
                            "The baker hands bread to the whole cast at the happy ending. Joyful, loud, bubbling over."},
                    {"out30f", "My output is back. And you read to the end. Not bad Sloppy~♡", "H",
                            PLAYER + "The adventure is over and she is going home. Proud, fond, teasing, the closest she comes to saying thank you."}};
            case 40: return new String[][]{
                    {"out40a", "Th-this is nothing other than a mistransl...", "G",
                            "The sly interpreter, exposed and beaten, tries to call it all a mistranslation and cannot finish. Flustered, stammering, losing her composure."},
                    {"out40b", "We can be heard! Translator-nim, the Gazette is still printing her version. Please hurry.", "N",
                            "The young queen, thrilled that her own words reach people, sends the translator on. Joyful, breathless, earnest."},
                    {"out40c", "One word fixed. The press next. Keep up Sloppy~♡", "H",
                            PLAYER + "She has won a fight and is already moving on. Pleased with herself." + FAST}};
            case 50: return new String[][]{
                    {"out50a", "I couldn't read that ending ahead... It's better not knowing?", "J",
                            "The newspaper girl who spoils everything has just been surprised for the first time in her life, and likes it. Wondering, delighted, slower than usual."},
                    {"out50b", "You read me... and it was not so bad.", "K",
                            "The shy twin, her black bar slipping off her eyes, finds that being seen did not hurt. Tiny, shaky, relieved, almost a sniffle."},
                    {"out50c", "Good. Now print the real page. And no peeking Sloppy~♡", "H",
                            PLAYER + "She tells off two sisters fondly after beating them. Bossy, warm." + FAST}};
            case 60: return new String[][]{
                    {"out60a", "Ah. I'm awake. The company was written out, wasn't it. I'll fetch them back. After a nap.", "Y",
                            "The swordswoman finally wakes properly, understands, and promises help in her own time. Slow, mild, yawning, kind."},
                    {"out60b", "A whole company of mercenaries on my side. Normal day Sloppy~♡", "H",
                            PLAYER + "She shrugs off having just recruited a band of mercenaries. Dry, amused." + FAST}};
            case 70: return new String[][]{
                    {"out70a", "W-we did it... sniff... I drew them all... waaah!", "R",
                            "The little crybaby librarian is overwhelmed that her tears helped win. She is sobbing with relief, hiccuping, and bursts out crying at the end."},
                    {"out70b", "The meanings are back! This one says bread. They all say bread to me!", "C",
                            "The baker happily reads the words that have been restored. Giggly, bright, high, delighted."},
                    {"out70c", "Chunked. One chapter left: the Appendix Sloppy~♡", "H",
                            PLAYER + "She has beaten a giant slime and looks ahead to the end. Satisfied, a little grim." + FAST}};
            case 80: return new String[][]{
                    {"out80a", "...What took you so long, horn-unnie? Someone asked me that once. I remember now.", "M",
                            "The lonely older self remembers her friends and who she is. Very soft, voice breaking, slow, close to tears, warm at the end."},
                    {"out80b", "Then We seal it again. Cast: everyone. And this time We shall read it aloud.", "N",
                            "The young queen makes her decree in her own voice in front of everybody. Warm, regal, happy, clear."},
                    {"out80c", "Heroine: everyone♡ I can share a page. Just this once♡", "I",
                            "The magical-girl idol, who always wanted to be the only heroine, shares the title and means it. Bright, sweet, high, sparkling, a little shy."},
                    {"out80d", "Volume Three is not due yet. I checked. Normally.", "T",
                            "The girl with the bat looks up at the sky and reports, perfectly calmly. Light, cute, cheerful, deadpan."},
                    {"out80e", "It is being admitted that a thousand and one pages remain to be proofread.", "G",
                            "The beaten interpreter, sentenced to proofreading, sulks in her stiff, over-formal way. Defeated, grumbling, put out."},
                    {"out80f", "Two volumes, read to the end. Now can I have my ice cream Sloppy~♡", "H",
                            PLAYER + "Both books are finished and she is worn out and happy. Tired, fond, teasing, a contented sigh, talking quickly."}};
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
            {"took2", "H-here... my ink... waaah... d-don't get hurt...", "R", "The little librarian wraps her friend in a protective bubble of her ink tears. She is a little crybaby sobbing her way through the words: hiccups, sniffles, a wobbling whine, childish and small. Earnest."},
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

    /**
     * Something has opened in the gallery, the page behind the Cringe button on the map: she says so, once, the next moment she
     * can, in place of any notice on the screen.
     */
    static final String[][] GALLERY = {
            {"gal1", "A new picture in the gallery. The Cringe page. Don't stare too long Sloppy~♡", "H", PLAYER + "She tells the player, in passing, that a new picture has been added to the gallery page called Cringe. Dry, smug, offhand, a little embarrassed that it exists."},
            {"gal2", "Something new on the Cringe page. I didn't put it there Sloppy~♡", "H", PLAYER + "She mentions that the gallery page called Cringe has something new in it, and denies having anything to do with it. Dry, airy, obviously lying."},
            {"gal3", "The gallery has a new picture. Go and look. I'll wait Sloppy~♡", "H", PLAYER + "She tells the player a new picture is in the gallery. Smug, bossy, pleased with herself, tapping her foot."},
            {"gal4", "New in the gallery, under Cringe. You earned it. Barely Sloppy~♡", "H", PLAYER + "She tells the player something new has opened in the gallery page called Cringe. Teasing, a backhanded compliment."},
            {"gal5", "Another one unlocked. It's in the gallery. Cringe, they call it Sloppy~♡", "H", PLAYER + "She notes that another picture has been unlocked in the gallery, whose name, Cringe, she finds beneath her. Deadpan, unimpressed, faintly offended."},
    };

    /** A pet of one she has beaten can be had now, at the guild counter: she says so, once, the next moment she can. */
    static final String[][] NEW_PET = {
            {"npet1", "Someone small wants to come with us. The counter has her. Pets Sloppy~♡", "H", PLAYER + "She tells the player that a new pet, a little stuffed copy of someone she has just beaten, can now be had at the guild counter's pets tab. Smug, amused, secretly charmed."},
            {"npet2", "A new pet at the counter. She looks like someone I beat. Cute Sloppy~♡", "H", PLAYER + "She mentions that a new pet that looks like an enemy she defeated is now at the guild counter. Teasing, pleased with herself."},
            {"npet3", "The guild counter has a new pet. Go look. I'm not jealous Sloppy~♡", "H", PLAYER + "She tells the player a new pet is at the guild counter, and insists she is not jealous of it. Huffy, obviously a little jealous."},
    };

    /** The gate of a land she has passed is a teleporter: she goes up in its light, and says where to. */
    static final String[][] WARP = {
            {"warp1", "Pick a gate. Any gate. I'll hold the light Sloppy~♡", "H", PLAYER + "She has stepped onto a glowing teleporter gate and a column of light lifts her; she tells the player to choose where to go. Breezy, showing off, a little dizzy."},
            {"warp2", "Where to? The lit ones have gates I already found Sloppy~♡", "H", PLAYER + "Standing in the light of a teleporter, she explains to the player that the lit places on the map are gates she has already found. Brisk, helpful, smug."},
    };

    /**
     * Talking to whoever she meets: tapped, each says something of her own about how the story stands, and Halgakos answers. Four
     * times in the story: the first lands (to the fifteenth), the rest of the first volume, the second volume, and the third. For
     * each of them, in the order of {@link RunLevels#SOMI} on (Somi, Inju, Boksun, Ttosun, Galpi, Yeseo, Nabi, Moru, Altteul) and
     * Nuri-yeowang last, her line and Halgakos's answer.
     */
    static final String[][][] TALK = {
            {
                    {"tks0", "Halgakos-nim! A whole book fell out of the sky, and now the plus signs walk! Isn't it the best?", "S", "A genki little girl of the Footnote Party gushes to the hero about the book that fell from the sky and made the maths signs come alive. Bouncing, starry-eyed."},
                    {"tku0", "Form 1-A: incidents involving a falling book. There have been three hundred. Please stop causing them.", "U", "A tired, precise guild clerk tells the hero how much paperwork the fallen book has made. Dry, deadpan, faintly accusing."},
                    {"tkb0", "Aigoo, a book from the sky. In my day things fell gently. Like rain. And rice.", "B", "A sweet old grandmother remembers when only rain and rice fell from the sky. Warm, rambling, fond."},
                    {"tko0", "Zero stands at a gate and won't let anyone by. I raced her once. She didn't move. I lost anyway!", "O", "An energetic ice-cream seller who loves racing complains about Zero, who blocks a gate and never moves. Indignant, laughing at herself."},
                    {"tkp0", "The book is missing pages. The wind took them. The wind always takes the best ones.", "P", "A quiet, mysterious girl who knows the wind tells the hero the fallen book has missing pages. Low, wistful, cryptic."},
                    {"tke0", "Footnote one: the book is a translation. Footnote two: someone does not want it finished.", "E", "A bookish girl who talks in footnotes tells the hero the fallen book is a translation that someone wants left unfinished. Measured, serious."},
                    {"tka0", "Nya! The new signs are fun to chase! The minus ones taste like nothing, nya.", "A", "A playful cat girl who keeps a pet shop talks about chasing the walking maths signs. Mischievous, purring."},
                    {"tkw0", "Hammer's been busy since the book came down. Signs make bad steel. I make it work.", "W", "A gruff dwarf blacksmith says she has been busy since the book fell, working with bad material. Terse, proud."},
                    {"tkl0", "Dear customer! Since the book fell, pluses grow on every land. Business has never been sweeter!", "L", "A cheerful merchant with an abacus is delighted that the fallen book made coins appear everywhere. Brisk, beaming."},
                    {"tkn0", "Translator-nim. The book that fell is Ours. Every page of it was spoken in Our court.", "N", "A cold, regal queen tells the translator that the fallen book belongs to her court. Stately, possessive, quiet."},
            },
            {
                    {"tks1", "The Footnote Party voted! You're our honorary president. It was unanimous. It was just me.", "S", "The genki girl announces the hero is her club's honorary president, then admits she was the only voter. Proud, then sheepish."},
                    {"tku1", "Illya filed a complaint about you. Tsubakieh filed one about Illya. I filed them both under 'no'.", "U", "The guild clerk reports rival complaints between two girls the hero fought, and how she dismissed them. Bone-dry, satisfied."},
                    {"tkb1", "That bat girl came by for soup. Tsubakieh. She said you broke her bat. Then she had seconds.", "B", "The grandmother says a girl whose bat the hero broke came by for soup and stayed for seconds. Amused, motherly."},
                    {"tko1", "They say the Mawang wants to come home. Even demon kings get homesick, huh? I'd race her home!", "O", "The ice-cream girl muses that even the demon king gets homesick, and offers to race her home. Upbeat, sympathetic."},
                    {"tkp1", "The last page of this volume is near. The wind is quieter there. Listen, when you reach it.", "P", "The mysterious girl tells the hero the last page of the first volume is close, and the wind is quiet there. Hushed, foreboding."},
                    {"tke1s", "Footnote three: a sloppy copy is a page copied by someone who never read it. They're spreading. Don't believe every page you find.", "E", "The footnote girl warns the hero about careless, unread copies of pages that are spreading through the land. Earnest, careful, a little worried."},
                    {"tka1", "Mini pets! The guild counter has little ones of the girls you beat. I want the bat one, nya!", "A", "The cat girl gushes about tiny pet versions of defeated rivals sold at the guild counter. Excited, greedy."},
                    {"tkw1", "Two sisters of mine. Pulmu does armour. Sutdol trades. Don't haggle with Sutdol.", "W", "The gruff blacksmith mentions her two sisters and warns the hero never to haggle with one of them. Short, fond, a little afraid."},
                    {"tkl1", "The black market moved in down the road. Competition! I lowered nothing. My prices are perfect.", "L", "The merchant complains that a black market opened nearby, but refuses to lower her prices. Huffy, proud."},
                    {"tkn1", "When the last page is sealed, We shall need you again. Cast: everyone. Remember the word.", "N", "The queen warns the translator she will be needed again after the first volume ends. Solemn, cryptic."},
            },
            {
                    {"tks2", "The Gazette printed that you're a villain! I bought every copy and used them to wrap ice cream!", "S", "The genki girl says a newspaper called the hero a villain, so she bought every copy and used them as wrapping. Fierce, triumphant."},
                    {"tku2", "The Great Translation Killer stops every series at a name. The guild has a form for it. It's blank.", "U", "The clerk talks about a villain who stops translated series at a name, and admits the guild has no idea what to do. Grim, deadpan."},
                    {"tkb2", "Nuri-yeowang came to my kitchen, dear. A queen, eating kimchi with her hands. Don't tell anyone.", "B", "The grandmother whispers that the regal queen secretly ate at her kitchen table. Delighted, conspiratorial."},
                    {"tko2", "The Brake Pads stop anything that goes fast. So of course they hate me. And you. Race them!", "O", "The ice-cream girl warns about enemies called the Brake Pads that stop anything fast, and urges the hero to race them. Fired up."},
                    {"tkp2", "Between the lines there is a whole land. Most readers never see it. You will.", "P", "The mysterious girl tells the hero there is a hidden land between the lines of text, which the hero will see. Soft, prophetic."},
                    {"tke2", "Footnote forty: Gokhae's fan blows the words right off the page. Hold on to yours.", "E", "The footnote girl warns that a rival named Gokhae blows words off pages with her fan. Wry, cautioning."},
                    {"tka2", "A slime king lives in the margins. He's squishy and he smells like jam, nya. I want him as a pet.", "A", "The cat girl describes a slime king who smells like jam and says she wants him as a pet. Dreamy, silly."},
                    {"tkw2", "Weapons get better if you hit them enough. So do people. Come back when you've been hit.", "W", "The gruff smith offers blunt wisdom about weapons and people getting stronger from being hit. Deadpan, kind underneath."},
                    {"tkl2s", "Somebody's selling sloppy copies of my price tags! Half price! Written by nobody! My prices are perfect, not copied!", "L", "The merchant is outraged that careless fake copies of her price tags are going round at half price. Scandalised, theatrical, proud."},
                    {"tkn2", "Our words are being rewritten in Our own palace. We do not ask, Translator-nim. We are asking.", "N", "The proud queen, whose words are being rewritten, swallows her pride and asks the translator for help. Strained, dignified."},
            },
            {
                    {"tks3", "A dragon library! With a dragon in it! Halgakos-nim, if you see her, ask if she's a member of anything!", "S", "The genki girl is thrilled that a library has a real dragon in it and wants to recruit the dragon to her club. Breathless."},
                    {"tku3", "Lucy has been issued forty-one warnings for breathing fire in a library. She has not read one.", "U", "The clerk reports that a dragon named Lucy keeps breathing fire in a library and ignores every warning. Exasperated, deadpan."},
                    {"tkb3", "That dragon girl is only lonely, dear. Everyone is fierce when no one feeds them.", "B", "The grandmother says the dragon girl Lucy is only fierce because she is lonely and hungry. Tender, wise."},
                    {"tko3", "They say the Mother Tongue was the first word ever said. I bet I could still outrun her!", "O", "The ice-cream girl boasts she could outrun the Mother Tongue, said to be the first word ever spoken. Cocky, a little scared."},
                    {"tkp3", "Every book keeps one hidden page. The last book keeps a hidden room. Look behind what doesn't fit.", "P", "The mysterious girl hints that the final book hides a secret room behind something out of place. Whispered, knowing."},
                    {"tke3", "Final footnote: the Mother Tongue does not want to be translated. She wants to be understood.", "E", "The footnote girl explains the final villain only wants to be understood, not translated. Quiet, moved."},
                    {"tka3", "Moru's sisters smelled like soot and cinnamon, nya. Moru won't say where they went. Ask her nicely.", "A", "The cat girl remembers the smith's missing sisters and urges the hero to ask the smith about them gently. Sad, hushed."},
                    {"tkw3", "My sisters went into the library before you. Didn't come out. If you find their hammers... bring them.", "W", "The gruff smith admits her sisters vanished into the dragon library and asks the hero to bring back their hammers. Voice cracking, trying to stay gruff."},
                    {"tkl3", "Last stall before the dragon! Everything's full price. Heroes always pay full price at the end!", "L", "The merchant cheerfully refuses discounts at the last shop before the final dragon. Shameless, sunny."},
                    {"tkn3", "The first word is waking, Translator-nim. When you read her, read her aloud. As We did.", "N", "The queen tells the translator the first word of all is waking and must be read aloud. Grave, trusting."},
            },
    };

    /** And Halgakos's answers, in the same order. */
    static final String[][][] ANSWERS = {
            {
                    {"tah0s", "The best? They bite. But fine, it's a little bit the best Sloppy~♡", "H", PLAYER + "She answers an excited little girl who loves that the maths signs came alive. Grudging, amused."},
                    {"tah0u", "Three hundred? I caused maybe two hundred of those Sloppy~♡", "H", PLAYER + "She answers a tired guild clerk complaining about incident forms. Smug, unrepentant."},
                    {"tah0b", "Rice from the sky sounds better than signs from the sky Sloppy~♡", "H", PLAYER + "She answers a kind grandmother reminiscing about gentle rain and rice. Wistful, hungry."},
                    {"tah0o", "Zero doesn't move because she's a zero. I'll move her Sloppy~♡", "H", PLAYER + "She answers a girl complaining about Zero blocking a gate. Cocky, determined."},
                    {"tah0p", "Then I'll catch the wind. Somebody has to Sloppy~♡", "H", PLAYER + "She answers a mysterious girl who says the wind took the book's missing pages. Breezy, brave."},
                    {"tah0e", "Someone doesn't want it finished? Then I'm finishing it out of spite Sloppy~♡", "H", PLAYER + "She answers a bookish girl who says someone wants the translation left unfinished. Defiant, grinning."},
                    {"tah0a", "Don't lick the minus signs, Nabi. Even I have standards Sloppy~♡", "H", PLAYER + "She answers a cat girl who has been chasing and tasting the walking signs. Disgusted, fond."},
                    {"tah0w", "Bad steel, good hammer. I respect that Sloppy~♡", "H", PLAYER + "She answers a gruff blacksmith who makes bad material work. Approving, brief."},
                    {"tah0l", "Sweeter for you. I'm the one picking them up Sloppy~♡", "H", PLAYER + "She answers a merchant who is getting rich from all the coins. Wry, a bit exploited."},
                    {"tah0n", "Yours or not, I'm translating it. You can read it after Sloppy~♡", "H", PLAYER + "She answers a regal queen who claims the fallen book. Bold, respectful under the cheek."},
            },
            {
                    {"tah1s", "President of a club of one. I accept. Obviously Sloppy~♡", "H", PLAYER + "She accepts an honorary title from a one-member fan club. Delighted, regal."},
                    {"tah1u", "Filed under no. My favourite drawer Sloppy~♡", "H", PLAYER + "She answers a clerk who dismissed two complaints against each other. Pleased."},
                    {"tah1b", "She had seconds? Then she forgives me Sloppy~♡", "H", PLAYER + "She hears that a rival she beat came for soup and had seconds. Relieved, amused."},
                    {"tah1o", "Homesick demon king. Fine. I'll walk her to the door Sloppy~♡", "H", PLAYER + "She answers a girl saying the demon king wants to go home. Softening, kind."},
                    {"tah1p", "Quiet wind, last page. Got it. I'll bring a scarf Sloppy~♡", "H", PLAYER + "She answers a cryptic warning about the last page. Light, hiding nerves."},
                    {"tah1es", "Copied, never read. That's the worst thing a page can be Sloppy~♡", "H", PLAYER + "She answers a warning about careless copies of pages. Disgusted, then quietly serious, a translator's pride."},
                    {"tah1a", "A tiny Tsubakieh with a tiny bat. Okay, that's adorable Sloppy~♡", "H", PLAYER + "She imagines a mini pet version of a rival with a little bat. Charmed despite herself."},
                    {"tah1w", "Noted. I will never, ever haggle with Sutdol Sloppy~♡", "H", PLAYER + "She solemnly promises never to haggle with the smith's scary sister. Mock-serious."},
                    {"tah1l", "Perfect prices, she says. With a straight face Sloppy~♡", "H", PLAYER + "She marvels at a merchant who claims her prices are perfect. Teasing."},
                    {"tah1n", "Cast: everyone. I'll remember. Queens are dramatic Sloppy~♡", "H", PLAYER + "She answers the queen's solemn warning. Respectful, then cheeky aside."},
            },
            {
                    {"tah2s", "Villain ice cream wrappers. You're a genius, Somi Sloppy~♡", "H", PLAYER + "She praises a little girl who used newspapers calling her a villain as ice cream wrappers. Touched, laughing."},
                    {"tah2u", "A blank form. Fine. I'll fill it in myself Sloppy~♡", "H", PLAYER + "She answers a clerk whose form about a series-killing villain is blank. Grim, resolved."},
                    {"tah2b", "A queen with kimchi fingers. My lips are sealed Sloppy~♡", "H", PLAYER + "She promises to keep the queen's secret kitchen visit quiet. Gleeful, sworn to secrecy."},
                    {"tah2o", "They stop anything fast? Then they've never met me Sloppy~♡", "H", PLAYER + "She answers a warning about enemies who stop fast things. Cocky, fired up."},
                    {"tah2p", "A land between the lines. I always read those anyway Sloppy~♡", "H", PLAYER + "She answers a hint about a hidden land between the lines. Intrigued, clever."},
                    {"tah2e", "Hold on to my words. I'm a translator. They're all I have Sloppy~♡", "H", PLAYER + "She answers a warning about a fan that blows words away. Sincere for a moment."},
                    {"tah2a", "A jam-scented slime king is not a pet, Nabi. ...Is he? Sloppy~♡", "H", PLAYER + "She answers a cat girl wanting a slime king as a pet. Exasperated, then tempted."},
                    {"tah2w", "Come back when I've been hit. That's the plan anyway Sloppy~♡", "H", PLAYER + "She answers blunt wisdom from the smith. Wry, fond."},
                    {"tah2ls", "Sloppy copies of price tags. Truly the darkest magic Sloppy~♡", "H", PLAYER + "She mock-sympathises with a merchant whose price tags are being copied carelessly. Theatrical, laughing."},
                    {"tah2n", "You're asking. I heard. Your words stay yours, Majesty Sloppy~♡", "H", PLAYER + "She answers a proud queen who has finally asked for help. Gentle, steady."},
            },
            {
                    {"tah3s", "I'll ask the dragon about your club. Right after she stops burning me Sloppy~♡", "H", PLAYER + "She promises a little girl to recruit the dragon to her club. Amused, a bit singed."},
                    {"tah3u", "Forty-one warnings, zero read. A girl after my own heart Sloppy~♡", "H", PLAYER + "She hears a dragon ignored forty-one warnings. Admiring, mischievous."},
                    {"tah3b", "Lonely and hungry. Then I'll bring her your soup Sloppy~♡", "H", PLAYER + "She answers the grandmother's wisdom about the lonely dragon. Warm, decided."},
                    {"tah3o", "Outrun the first word? Let's both try, and not die Sloppy~♡", "H", PLAYER + "She answers a boast about outrunning the final villain. Laughing, nervous."},
                    {"tah3p", "Behind what doesn't fit. Like me, in a library Sloppy~♡", "H", PLAYER + "She answers a hint about a hidden room behind something out of place. Self-deprecating, sharp."},
                    {"tah3e", "Understood, not translated. ...I can do that too Sloppy~♡", "H", PLAYER + "She answers that the final villain wants to be understood. Quiet, earnest."},
                    {"tah3a", "Soot and cinnamon. I'll ask Moru gently. Gently, Nabi Sloppy~♡", "H", PLAYER + "She promises the cat girl to ask about the smith's lost sisters kindly. Soft, serious."},
                    {"tah3w", "I'll find them, Moru. Hammers and all Sloppy~♡", "H", PLAYER + "She promises the grieving blacksmith to find her lost sisters. Firm, tender."},
                    {"tah3l", "Full price at the end. You really are the final boss Sloppy~♡", "H", PLAYER + "She teases the merchant who refuses discounts before the final battle. Laughing."},
                    {"tah3n", "Aloud. As you did. I won't forget, Majesty Sloppy~♡", "H", PLAYER + "She answers the queen's last instruction to read the first word aloud. Solemn, brave."},
            },
    };

    /** The diary pages found in homes: a little of the story on each, in the words of whoever wrote it. They are read, not said. */
    static final String[] DIARY = {
            "Day one of the book. It came down over the square with a sound like a sigh. By evening the plus signs were walking. Mother says not to feed them.",
            "Somi has started a party. Its members are Somi. Its rules are that everyone must be happy. I joined. Now there are two of us.",
            "The guild clerk stamped my dog. He was not a form. She says everything is a form if you are brave enough.",
            "Grandmother left rice cakes on the sill for the walking signs. In the morning there were none, and a minus sign was asleep in the flowerpot.",
            "Zero stood at the gate all day and would not move. I told her a joke. She did not laugh. Nothing adds up with her.",
            "A girl with a bat came through town and broke three signs and one window. She paid for the window in candy. Fair.",
            "They say the book is a translation, and someone hates it being finished. Who could hate a story being finished? Only someone who wants the last word.",
            "Nabi's shop has a cat that is a jar. Or a jar that is a cat. I bought it. It purrs when I put my pencils in.",
            "Moru had three hammers on her wall. Now there is one. She will not talk about the other two. She hits the anvil harder these days.",
            "The palace bells rang backwards tonight. The queen's words came out in the wrong order in the paper. Someone is rewriting her.",
            "The Gazette says the hero is a villain. My aunt cut it into paper boats. They sailed better than the news.",
            "I went between the lines today. There is a whole country there, quiet and blue, where the words go to rest between readings.",
            "Slimes in the margins again. They smell of jam. I am not eating jam anymore. I am not eating anything green.",
            "A man came through town selling cheap books. Every page was there and not one of them was right. Mother called it a sloppy copy and put it in the stove. It complained the whole time.",
            "The tide brought in a boat folded out of a letter. Inside was one line: we are still reading. I do not know who \"we\" is.",
            "There is a dragon in the library. She reads faster than she burns. Nobody has ever finished a book before her.",
            "Moru's sisters went into the library to fix a broken word. That was last spring. Their hammers sing at night, Moru says. Far away.",
            "If you are reading this, translator: the first word is not angry. She is only lonely. Read her aloud. That is all she ever wanted.",
    };

    /** Her line with the game's name in it: the first time the map is opened, and now and then as a land begins. */
    static final String[][] TITLE_LINE = {
            {"title1", "A sloppy copy? Not on my page Sloppy~♡", "H", PLAYER + "Her signature line, the game's own name in it: a translator who will not stand for careless copies. Confident, punchy, proud, a playful wink at the end."},
            {"title2", "Queen of Sloppies? Every sloppy copy bows to me, page after page Sloppy~♡", "H", PLAYER + "The game's new name, Halgakos: The Queen of Sloppies. Every sloppy copy is a sloppy, and she rules them all, with endless pages still to climb. Regal, proud, smug, a teasing wink at the end."},
    };

    /** The first time a key is pressed in the first land: she says which keys. */
    static final String[][] KEYS = {
            {"keys1", "Keyboard? Fancy. A and D to run, Space to jump Sloppy~♡", "H", PLAYER + "She notices the player is using a keyboard and quickly tells them the keys. Teasing, impressed despite herself, brisk."},
    };

    /** Her skill tree: points waiting, the first time the map shows them; divine power coming, the first time in a land; every point taken back. */
    static final String[][] SKILLS = {
            {"skill1", "My skill tree woke up. Points to spend, so spend them Sloppy~♡", "H", PLAYER + "She has just found that her skill tree has points waiting and urges the player to open it and spend them. Excited, a little bossy, eager to grow stronger."},
            {"skill2", "Divine power! Every blow lands three times Sloppy~♡", "H", PLAYER + "In the middle of a fight her divine power fills and bursts out; for a few seconds she hits three times as hard. Triumphant, radiant, a battle cry."},
            {"skill3", "All of it back? Fine. Choose better this time Sloppy~♡", "H", PLAYER + "The player has just taken back every point of her skill tree to spend them again. Mock exasperated, teasing, but secretly glad of the fresh start."},
            {"skill4", "My tree got redrawn! Every point came back, so spend them Sloppy~♡", "H", PLAYER + "Her skill tree has just been redrawn into a great new web of regions and routes, and every point she had spent has come back to her to spend again. Delighted, a little bossy, eager to explore it."},
    };

    /** Black is put on her, and weighs on her. */
    static final String[][] BLACKENED = {
            {"blk1", "Ugh, black ink all over me! Everything's heavy and dark Sloppy~♡", "H", PLAYER + "A cursed black ribbon has just soaked her in ink: she feels heavy, half blind, weak and dripping. Disgusted, sulky, shivering, but defiant."},
    };

    /**
     * The Mirror Depths: going down; the Imperfect Copy (in her voice, colder, and her catchphrase turned on her) when met, halfway, and when
     * beaten; and a shadow's touch blinding her.
     */
    static final String[][] MIRROR = {
            {"mir1", "A mirror that goes all the way down. Every face in here is a sloppy copy of mine Sloppy~♡", "H", PLAYER + "She steps through a tall dark mirror into a silver, upside-down dungeon where her own shadows walk. Uneasy, quiet, trying to joke."},
            {"mir2", "You call them sloppy copies. I was the first one. The imperfect one. Hello Sloppy~♡", "H", "The Imperfect Copy speaks: an uncanny pale copy of Halgakos with elf ears and no horns, a cracked mirror for a sword, the first copy ever made and thrown away. Her voice is Halgakos's, but cold, flat and hollow, every word measured, the catchphrase said like an insult."},
            {"mir3", "Every page you wrote, I wrote first. Backwards Sloppy~♡", "H", "The Imperfect Copy, cracking, half her strength gone, cold and bitter, almost whispering. The same voice as Halgakos but drained of warmth."},
            {"mir4", "You were never imperfect. You were me before the ink dried. Rest now Sloppy~♡", "H", PLAYER + "She has beaten the Imperfect Copy, who shatters into mirror shards. Gentle, sad, a little proud, kind."},
            {"mir5", "Hey, who turned out the lights? My own shadow just touched me Sloppy~♡", "H", PLAYER + "A shadow of herself touched her and everything went dark around her for a moment. Startled, creeped out, joking to stay brave."},
    };
    /**
     * The Echoes: seven of her from timelines that ended badly, each met in the mirror with a word of how hers ended (in her voice, worn by
     * it), and her answer.
     */
    static final String[][] ECHOES = {
            {"ech1", "The library flooded and nobody came. I kept reading until the ink ran off every page Sloppy~♡", "H", "An Echo of Halgakos from a timeline where the great library drowned: soaked, pale blue, hair dripping, voice quiet and waterlogged, lonely and tired."},
            {"ech2", "I burned the last book to stay warm. Then there was nothing left to be Sloppy~♡", "H", "An Echo of Halgakos from a timeline that burned: grey with ash, eyes dull embers, voice hoarse and empty, guilty and numb."},
            {"ech3", "I promised to guard the gate until she came back. She never did. I'm still standing here Sloppy~♡", "H", "An Echo of Halgakos who stayed a knight forever at a gate: steel-grey, stiff, voice proud but cracked, loyal and heartbroken."},
            {"ech4", "They wanted one more song, then one more. I sang until there was no me left, only the encore Sloppy~♡", "H", "An Echo of Halgakos from a timeline where fame consumed her: faded idol in magenta, smile stuck on, voice sweet but hollow and exhausted."},
            {"ech5", "Winter came to the archive and never left. I learned every word for cold Sloppy~♡", "H", "An Echo of Halgakos from a timeline of endless winter: frosted, scholarly, voice slow and brittle, shivering and resigned."},
            {"ech6", "I served the Copy, polished her mirror, smiled when told. Serving was easier than being Sloppy~♡", "H", "An Echo of Halgakos who became the Imperfect Copy's maid: violet-shaded, eyes lowered, voice soft, obedient and sad."},
            {"ech7", "Someone erased my story one line at a time. I don't remember my name, only yours Sloppy~♡", "H", "An Echo of Halgakos whose story was erased: pale and blank like an empty page, voice faint and whispering, confused and gentle."},
    };
    static final String[][] ECHO_ANSWERS = {
            {"eca1", "Then let me finish the book for you. Rest, you've read enough Sloppy~♡", "H", PLAYER + "She answers the drowned Echo of herself, gently, kindly, determined."},
            {"eca2", "There's still you. I'll carry the warmth from here Sloppy~♡", "H", PLAYER + "She answers the burned, ash-grey Echo of herself, warm and firm, near tears."},
            {"eca3", "You can stand down, knight. I'm the one who came back Sloppy~♡", "H", PLAYER + "She answers the Echo who guarded a gate forever, softly, honouring her."},
            {"eca4", "Encore's over. Take a bow and go home Sloppy~♡", "H", PLAYER + "She answers the faded idol Echo of herself, playful but tender."},
            {"eca5", "Then I'll be the spring. Come on, thaw out Sloppy~♡", "H", PLAYER + "She answers the frozen scholar Echo of herself, bright and encouraging."},
            {"eca6", "Nobody gets to order you around anymore. Not even a mirror Sloppy~♡", "H", PLAYER + "She answers the Echo who served the Copy, protective and fierce."},
            {"eca7", "Your name is Halgakos. I'll write it back in, every letter Sloppy~♡", "H", PLAYER + "She answers the erased, blank Echo of herself, gentle and resolute."},
    };
    /** The mirror's shadows of those she knows, each in their own voice, as their shadows: a word when she meets them. */
    static final String[][] CAST_SHADOWS = {
            {"csh1", "You left me behind with the bread. The shadow of me is hungry for more than chikuwa now.", "C", "A shadow copy of Chikuwa in the mirror dungeon: the same cheerful baker's voice, but flat, hollow and a little menacing, as if echoing from glass."},
            {"csh2", "Every ink ball I ever threw at you, I kept. Shall I give them all back at once?", "R", "A shadow copy of Rune in the mirror dungeon: Rune's voice but cold, smug and echoing, a mocking rival made of ink."},
            {"csh3", "Hearts, hearts, hearts! Mine are black now. Do you still want one?", "I", "A shadow copy of Illya in the mirror dungeon: Illya's sweet sing-song voice turned eerie and giggling, echoing."},
            {"csh4", "Batter up. This time I'm swinging for you, and I don't miss in the dark.", "T", "A shadow copy of Tsubakieh in the mirror dungeon: Tsubakieh's cool, sporty voice made low and threatening, echoing."},
            {"csh5", "My feathers fell in the dark and grew back as shadows. Fly with me, little one, down and down.", "M", "A shadow copy of the Mawang in the mirror dungeon: her regal, airy voice turned hollow and hungry, echoing from glass."},
    };
    /** The first time she meets each of the twelve made of fire, frost and ink, or that stun: what it does to her, and what to do. */
    static final String[][] STATUS_UNIQUES = {
            {"u3a", "A matchstick with an attitude! Its embers burn, keep moving Sloppy~♡", "H", PLAYER + "A tiny sooty match-head sprite flicks burning embers at her. Wary, teasing, quick warning that fire burns her."},
            {"u3b", "A frozen book. One slam and I'll be shivering Sloppy~♡", "H", PLAYER + "A frosted hardcover book slams shut in bursts of frost that chill her and slow her down. Shivering, cross."},
            {"u3c", "Poison pens! Don't let those green darts stick Sloppy~♡", "H", PLAYER + "A porcupine whose quills are pens dripping green ink shoots poisoned darts. Disgusted, urgent tip."},
            {"u3d", "That beetle is an ink bottle with a fuse. Stand back Sloppy~♡", "H", PLAYER + "A beetle shaped like a corked ink bottle with a lit fuse waddles closer to burst in ink. Alarmed, backing off."},
            {"u3e", "That bookend is crackling. If it hits me I'll see stars Sloppy~♡", "H", PLAYER + "A brass bulldog bookend crackling with static charges to stun her. Nervous, joking."},
            {"u3f", "Ice behind an icy shield. Break it, then shatter it Sloppy~♡", "H", PLAYER + "A book made of ice hides behind a frosty shell; frozen things shatter at a blow. Confident, strategic."},
            {"u3g", "A fan on fire? Fire's no friend of paper Sloppy~♡", "H", PLAYER + "A burning paper fan leaps at her in bursts of flame. Wry, nervous about burning."},
            {"u3h", "Eww, a sick page. It splits, and its ink is poison Sloppy~♡", "H", PLAYER + "A blotchy green-stained page splits in two and its ink poisons. Grossed out, warning."},
            {"u3i", "A snow globe on legs. Freeze it three times and it's mine Sloppy~♡", "H", PLAYER + "A golem with a snow globe head pounds the ground with chilling snow; chill it three times and it freezes solid. Playful, smug plan."},
            {"u3j", "A scribe made of charcoal. Its ink burns hotter than mine Sloppy~♡", "H", PLAYER + "A hooded charcoal scribe flicks burning ink at her. Competitive, a little jealous."},
            {"u3k", "Smog with wings. Hold your breath, it's all poison Sloppy~♡", "H", PLAYER + "A fuzzy moth of green ink smoke darts at her trailing poison. Covering her nose, bossy."},
            {"u3l", "Clap clap, crackle! One hit and my ears ring Sloppy~♡", "H", PLAYER + "A creature of two brass book covers bounces and claps with sparks that stun. Startled, dizzy joke."},
    };
    /** The first time she finds each of the six later hidden bosses in its hideout: who it is, and what to look out for. */
    static final String[][] HIDDEN2 = {
            {"hb2a", "An overdue notice the size of a house! It stamps, so stay off the ground Sloppy~♡", "H", PLAYER + "A hulking giant library due-date stamp on stubby legs, a mane of overdue slips, slams its stamp face down in ink shockwaves. Startled, then cheeky."},
            {"hb2b", "A queen folded out of paper. Those fans are sharp, mind your whiskers Sloppy~♡", "H", PLAYER + "A tall elegant origami queen of white and gold paper slashes fans of sharp paper blades. Impressed, a little catty, careful."},
            {"hb2c", "A bookworm in gold armour, chewing my pages. Hey, those are mine Sloppy~♡", "H", PLAYER + "A giant plump bookworm with gold-leaf book-spine segments and round glasses chomps through pages and spits paper scraps. Indignant, possessive."},
            {"hb2d", "A tyrant made of sealing wax. Don't get stamped shut Sloppy~♡", "H", PLAYER + "A squat armoured tyrant of dripping royal purple sealing wax with a candle-flame crown swings a seal-stamp mace in hot wax splashes. Wary, joking."},
            {"hb2e", "The Footnote King. So many tiny marks, and every one is aimed at me Sloppy~♡", "H", PLAYER + "A round regal king made of a swarm of tiny footnote marks with a crown of asterisks flings streams of marks with a pointer staff. Overwhelmed, wry."},
            {"hb2f", "The last librarian. She looks so tired. I'll be gentle, but I won't lose Sloppy~♡", "H", PLAYER + "A tall gaunt ghostly old librarian with a lantern and a huge chained tome casts beams of pale words. Softer, respectful, then determined."},
    };
    /**
     * In each home, what is dear to whoever lives there, looked at: what Halgakos says of it, and what its owner says back. In the order
     * {@link RunLevels#SOMI} on: Somi's ballot box, Inju's golden stamp, Boksun-halmeoni's rice cakes, Ttosun's trophy, Galpi's wind chime,
     * Yeseo's card catalogue, Nabi's sleeping kitten, Moru's sword with a flower tied on.
     */
    static final String[][] KEEPSAKES = {
            {"hks0", "A ballot box with one vote in it. I bet it says Somi Sloppy~♡", "H", PLAYER + "She peeks into a little wooden ballot box with a heart on it in a cheerful girl's clubhouse bedroom. Amused, fond, teasing."},
            {"hks1", "A golden stamp. One press and I'm officially approved Sloppy~♡", "H", PLAYER + "She eyes a big shiny golden rubber stamp on a tidy desk in a neat little flat. Mischievous, tempted."},
            {"hks2", "Rice cakes, still steaming. Halmeoni won't mind one. Or two Sloppy~♡", "H", PLAYER + "She leans over a steaming pot of rice cakes in a granny's warm bakery kitchen. Hungry, sneaky, delighted."},
            {"hks3", "First place, every race. I'd beat her, you know. Probably Sloppy~♡", "H", PLAYER + "She looks up at a shining gold racing trophy in a sporty girl's room. Competitive, cocky, then a little unsure."},
            {"hks4", "Ribbons that ring in the wind. I think it's playing my name Sloppy~♡", "H", PLAYER + "She listens to a wind chime of ribbons and bells in a witch's breezy loft. Hushed, dreamy, charmed."},
            {"hks5", "One drawer glows. The card inside is about me. Footnote: flattering Sloppy~♡", "H", PLAYER + "She pulls a glowing card from a card catalogue drawer in a bookish girl's study. Curious, pleased, playfully smug."},
            {"hks6", "A sleeping kitten. Shh. Even I can be quiet sometimes Sloppy~♡", "H", PLAYER + "She tiptoes up to a kitten curled asleep in a cat bed in a cat girl's home. Whispering, soft, sweet."},
            {"hks7", "A sword with a flower tied on. Moru has a soft side after all Sloppy~♡", "H", PLAYER + "She finds a polished sword on a stand with a little flower tied to its hilt in a gruff smith's forge home. Surprised, teasing, warm."},
    };
    static final String[][] KEEPSAKE_REPLIES = {
            {"hkr0", "Halgakos-nim found the ballot box! You can vote too. Just vote for me!", "S", "The genki little girl of the Footnote Party catches her hero at her ballot box and begs for her vote. Bouncy, giggly, high."},
            {"hkr1", "Please do not stamp yourself. That stamp is for approved forms only. Fine. Approved.", "U", "The prim guild clerk, at home, catches someone with her golden stamp; she protests, then stamps her anyway. Dry, deadpan, a small smile at the end."},
            {"hkr2", "Aigoo, take the whole pot, dear. I made them thinking someone would come.", "B", "The kind old baker granny pushes her whole pot of rice cakes on a visitor, a little lonely. Warm, fussing, tender."},
            {"hkr3", "Hands off my trophy! Okay, hold it once. Then race me for it!", "O", "The sporty ice-cream girl guards her racing trophy, then gives in and issues a challenge. Loud, grinning, competitive."},
            {"hkr4", "The wind tied those ribbons, not me. It likes you. Listen when it rings.", "P", "The quiet bookmark witch explains softly that the wind tied the ribbons on her chime. Hushed, mysterious, kind."},
            {"hkr5", "Footnote one: that card is yours now. Footnote two: please put the drawer back.", "E", "The careful footnote girl lets a visitor keep a card from her catalogue, then asks her to tidy up. Precise, earnest, a touch flustered."},
            {"hkr6", "Nya, shh! She only just fell asleep. Here, a treat for being quiet, nya.", "A", "The playful cat girl whispers so as not to wake a kitten, and rewards her guest. Whispering, purring, mischievous."},
            {"hkr7", "That flower's nothing. Don't tell anyone. Here. For keeping quiet.", "W", "The gruff young smith, embarrassed, waves off the flower on her sword and bribes her guest to keep the secret. Flat, gruff, flustered."},
    };
    /**
     * The Vampire Merchant (h), at her stall in the dungeons: her greeting the first time, then by how she feels about Halgakos (cold, wary,
     * warm, fond, devoted, beloved); her three warnings, each colder, and the fight; what she says when, too strong yet to be beaten, she
     * laughs and goes, when she is spared, and when she is finished; and to gifts she likes and does not, to what is bought, and to too
     * little to buy it with. Her name is never said in them. Halgakos's answers (H) after. Her voice waits for its approval: her lines are
     * read by tools/make_run_voice.py, but are not among the groups the game checks for recordings until they are recorded.
     */
    static final String[][] MERCHANT = {
            {"lil0", "Welcome, little cat. My wares are rare, and my patience rarer. Do look, but touch with coin.", "h", "An elegant, teasing vampire noblewoman, passing as an ordinary merchant, greets a cat-eared girl at her candlelit stall deep in a dungeon. Velvet, amused, guarded."},
            {"lil1", "You again. Buy something, or let the dark have you. I'm not particular.", "h", "The vampire merchant, cold and bored, addresses a customer she does not like. Icy, clipped, dismissive."},
            {"lil2", "Ah, the little cat. Mind the candles, and mind your manners.", "h", "The vampire merchant greets a wary acquaintance. Cool, dry, faintly amused."},
            {"lil3", "There you are. I set something aside for you. Don't let it go to your head.", "h", "The vampire merchant greets a customer she has grown to like, pretending not to care. Warm under a cool surface, teasing."},
            {"lil4", "I heard your footsteps three floors up. I'd know them anywhere now.", "h", "The vampire merchant, fond, admits she listens for her favourite visitor. Soft, playful, a little wistful."},
            {"lil5", "Stay a little longer tonight. It has been so long since anyone chose to.", "h", "The lonely vampire merchant, devoted, asks her visitor to stay. Tender, hushed, aching."},
            {"lil6", "My little cat. Every candle down here burns for you now.", "h", "The vampire merchant, fully in love, greets her beloved. Warm, glowing, gently teasing, intimate but tasteful."},
    };
    static final String[][] MERCHANT_WARN = {
            {"liw1", "Careful, little cat. My wares aren't free.", "h", "The vampire merchant, amused, after being struck once by mistake. Light, teasing, a hint of warning."},
            {"liw2", "That's twice. I don't forgive a third.", "h", "The vampire merchant, struck a second time, her smile cooling. Cool, measured, dangerous."},
            {"liw3", "Last chance. Lower your weapon.", "h", "The vampire merchant, struck a third time, eyes glowing red. Cold, quiet, deadly serious."},
            {"liw4", "You wish to dance with something that cannot die? Then let us dance until dawn.", "h", "The vampire merchant rises from her stall to fight, blood magic swirling and bats around her. Thrilled, menacing, grand."},
            {"liw5", "Is that all? I have outlived kingdoms, little cat. Run along, before I grow bored.", "h", "The immortal vampire merchant, unhurt and mending, laughs at a fighter far too weak and lets her go. Amused, contemptuous, a little sad."},
            {"liw6", "You held back. Why? No one has held back for me in centuries.", "h", "The vampire merchant, beaten down, is spared by her opponent and is shaken by it. Stunned, soft, vulnerable."},
            {"liw7", "So this is how it ends. Remember my wares, at least.", "h", "The vampire merchant's last words as she fades into a swarm of bats. Quiet, bitter, resigned, lonely."},
    };
    static final String[][] MERCHANT_GIFT = {
            {"lig1", "For me? How unexpectedly sweet. I shall keep it somewhere the centuries can't reach.", "h", "The vampire merchant receives a gift she loves. Surprised, touched, trying to stay composed."},
            {"lig2", "Thoughtful. I suppose you may keep coming back.", "h", "The vampire merchant receives a gift she likes. Pleased, understated, teasing."},
            {"lig3", "Garlic. Truly? I shall pretend that was a joke.", "h", "The vampire merchant is handed a garlic bun as a gift. Offended, dry, icy humour."},
            {"lig4", "A mirror. How cruel. There is no one in it to look at.", "h", "The vampire merchant is handed a mirror; she has no reflection. Hurt, quiet, bitter."},
            {"lig5", "Sunlight in a charm. I'll put it somewhere very, very far from me.", "h", "The vampire merchant is handed a sun charm. Wary, sardonic, mildly alarmed."},
    };
    static final String[][] MERCHANT_SHOP = {
            {"lis1", "A fine choice. Drink it slowly; it was brewed by moonlight.", "h", "The vampire merchant sells a potion. Smooth, pleased, elegant."},
            {"lis2", "For you, I'll lower the price. Don't tell the others I have a heart.", "h", "The vampire merchant gives her favourite customer a discount. Warm, sly, teasing."},
            {"lis3", "Not enough coin. Come back richer, little cat.", "h", "The vampire merchant turns away a customer who cannot pay. Cool, amused."},
    };
    static final String[][] MERCHANT_ANSWERS = {
            {"lia0", "A vampire with a shop. In a dungeon. Of course there is Sloppy~♡", "H", PLAYER + "She meets a vampire merchant at a candlelit stall deep in a dungeon for the first time. Wry, intrigued, cheeky."},
            {"lia1", "Oops. Didn't mean that. Mostly Sloppy~♡", "H", PLAYER + "She has accidentally hit the vampire merchant and gets a warning. Sheepish, mischievous."},
            {"lia2", "She can't even be hurt. Note to self: be nicer Sloppy~♡", "H", PLAYER + "The immortal vampire merchant laughed off her attacks and left. Rattled, rueful, joking."},
            {"lia3", "I couldn't do it. Nobody should be that alone Sloppy~♡", "H", PLAYER + "She chose to spare the vampire merchant at the last moment. Soft, sad, sure."},
    };
    /** Her pouch: a potion not ready yet, an empty pouch, a drink spilled when something stopped it, and no more heals in a fight with a boss. */
    static final String[][] POUCH = {
            {"pou1", "Not yet. One more sip and I'd slosh Sloppy~♡", "H", PLAYER + "She tries to drink another potion before it is ready. Quick, wry, a little queasy." + FAST},
            {"pou2", "My pouch is empty. Shopping trip later Sloppy~♡", "H", PLAYER + "She reaches into her potion pouch and finds nothing. Disappointed, then perky." + FAST},
            {"pou3", "Spilled it! At least I kept the bottle Sloppy~♡", "H", PLAYER + "Her drink was knocked from her lips mid-gulp. Annoyed, salvaging it." + FAST},
            {"pou4", "No more drinks in this fight. Win it dry Sloppy~♡", "H", PLAYER + "She has drunk all the healing she is allowed in a boss fight. Grim, determined, a joke through gritted teeth." + FAST},
    };
    /** The first time she meets each of the later chapters' twelve creatures: what to look out for. */
    static final String[][] LATER_UNIQUES = {
            {"u2a", "A newspaper bug! Don't let it roll you flat Sloppy~♡", "H", PLAYER + "She meets a pill bug made of rolled newspaper that curls up and bowls along the ground. Wary, quick warning."},
            {"u2b", "It shouts the headlines at us. So loud Sloppy~♡", "H", PLAYER + "A little town crier with a megaphone mouth is shouting words at them. Hands over ears, cross, playful."},
            {"u2c", "That line can stretch! Keep your distance Sloppy~♡", "H", PLAYER + "A snake made of a ruled notebook line lashes out from far away. Alarmed, advising."},
            {"u2d", "It crosses things out. Jump over the line Sloppy~♡", "H", PLAYER + "An ink knight draws a long red line along the ground ahead to strike through. Urgent, bossy tip."},
            {"u2e", "Sticky tongue! It wants to pull us in Sloppy~♡", "H", PLAYER + "A fat glue-pot toad shoots a sticky tongue that pulls you in. Grossed out, giggly."},
            {"u2f", "Ew, smudges everywhere. Don't step in them Sloppy~♡", "H", PLAYER + "An ink smudge slime leaves sticky smears that slow you down. Disgusted, fussy."},
            {"u2g", "A mirror crab! Hit it from behind Sloppy~♡", "H", PLAYER + "A crab with a mirror shell bounces shots back; you must hit it from behind. Clever, pleased with herself."},
            {"u2h", "It copies everything we do. Rude Sloppy~♡", "H", PLAYER + "A ghostly page echoes every attack back at them. Offended, pouty."},
            {"u2i", "Its sand slows time down. Hurry Sloppy~♡", "H", PLAYER + "A little hourglass creature slows time around it. Impatient, hurrying."},
            {"u2j", "It hides in its shell. Wait till it peeks out Sloppy~♡", "H", PLAYER + "A hermit crab in a scroll shell hides, then pops out to jab. Patient, sly."},
            {"u2k", "A baby dragon! Cute, but it breathes fire Sloppy~♡", "H", PLAYER + "A chubby golden dragon hatchling on a hoard of books breathes fire. Charmed, then cautious."},
            {"u2l", "Stay out of that lantern's light Sloppy~♡", "H", PLAYER + "A stone sentinel with a lantern head sweeps a burning beam of light. Low, tense warning."},
    };

    /** Her moves from the counter: the second wind, which leaves her half a heart once a land. */
    static final String[][] MOVES = {
            {"wind1", "Not yet! Second wind Sloppy~♡", "H", PLAYER + "She was about to be knocked out but her second wind kicks in and leaves her standing on half a heart. Gritty, defiant, catching her breath, then a cheeky grin."},
    };

    /** Hideouts: near one for the first time, she feels it; a hidden boss beaten, she crows. */
    static final String[][] SECRETS = {
            {"hid1", "Something's hidden near here. I can feel it Sloppy~♡", "H", PLAYER + "She senses a secret place nearby, a hideout no one was meant to find. Hushed, intrigued, a little thrilled, leaning in."},
            {"hid2", "A boss nobody was meant to find. Mine now Sloppy~♡", "H", PLAYER + "She has just beaten a hidden boss in a secret hideout and claims its treasure. Breathless, triumphant, smug."},
    };

    /** Water: the first time she is in deep water, in blue and without it. */
    /**
     * The smith sisters' quests: for each of Moru's, Pulmu's, Sutdol's and all three's, the asking, Halgakos's answer, two steps handed
     * in, and the end.
     */
    static final String[][] SMITH_QUESTS = {
            {"sq0a", "My good hammer's gone. Fell down a dungeon stair. Help me get it back.", "W", "A blunt, short-spoken dwarf smith girl, asks the hero for help: her favourite hammer fell down a dungeon. Flat, gruff, embarrassed underneath."},
            {"sq0b", "A hammer at the bottom of a dungeon? On it Sloppy~♡", "H", PLAYER + "She agrees to fetch the blacksmith's lost hammer from a dungeon. Eager, game."},
            {"sq0c", "Sixty flat. Good. Now metal. Six of it.", "W", "A blunt, short-spoken dwarf smith girl, approves the hero's progress and asks for six pieces of metal. Short, approving."},
            {"sq0d", "Good metal. Last thing. The hammer's at the bottom of a dungeon. Go.", "W", "A blunt, short-spoken dwarf smith girl, sends the hero down a dungeon for the hammer. Blunt, trusting."},
            {"sq0e", "That's it. My hammer. ...Made you one like it. Take it. Don't thank me.", "W", "A blunt, short-spoken dwarf smith girl, has her hammer back and gives the hero a hammer she forged. Gruff, touched, hiding it."},
            {"sq1a", "I'm making you a coat of scales! Real scales! I just need, um, everything. Help me?", "c", "A loud, sunny dwarf armourer girl, excitedly asks the hero to help gather what she needs for a coat of scales. Big, warm, laughing."},
            {"sq1b", "A coat of scales? Yes please Sloppy~♡", "H", PLAYER + "She happily agrees to help the cheerful armourer make her a coat of scales. Delighted."},
            {"sq1c", "Four hunts! The guild loves you! Now ink crystals, four, for the shine!", "c", "A loud, sunny dwarf armourer girl, cheers the hero's progress and asks for four ink crystals. Loud, happy."},
            {"sq1d", "Ooh, sparkly! Last one: a hidden boss's scale. They keep the best ones in hideouts!", "c", "A loud, sunny dwarf armourer girl, delighted with the crystals, sends the hero to beat a hidden boss for its scale. Excited, conspiratorial."},
            {"sq1e", "Done! Try it on, try it on! Fits like a book cover! I knew it would!", "c", "A loud, sunny dwarf armourer girl, has finished the coat of scales and makes the hero try it on. Overjoyed, laughing."},
            {"sq2a", "A charm of paper and gold. Weighed twice, cut once. Bring me what I ask, in order.", "d", "A calm, exact dwarf jeweller-smith girl, asks the hero to gather paper and gold for a charm, precisely and in order. Quiet, level, dry."},
            {"sq2b", "In order. Weighed twice. Got it Sloppy~♡", "H", PLAYER + "She promises the exacting smith to bring things in order. Mock-serious, amused."},
            {"sq2c", "Five trees. The paper is good. Three gold leaf next.", "d", "A calm, exact dwarf jeweller-smith girl, checks the paper the hero brought and asks for three gold leaf. Dry, satisfied."},
            {"sq2d", "Gold, weighed twice. Now go four floors down a dungeon. The charm wants the depth.", "d", "A calm, exact dwarf jeweller-smith girl, weighs the gold and sends the hero four floors down a dungeon. Calm, exact, a touch mysterious."},
            {"sq2e", "Finished. It is exact. So are you, today.", "d", "A calm, exact dwarf jeweller-smith girl, hands over the finished charm with a rare compliment. Quiet, dry, warm underneath."},
            {"sq3a", "We three want to make one thing together. Clear dungeons. Bring metal. Lots of it.", "W", "A blunt, short-spoken dwarf smith girl, speaks for all three sisters: they want to forge one thing together, and need the hero's help. Gruff, serious, proud."},
            {"sq3b", "All three sisters at once? I'd better not haggle Sloppy~♡", "H", PLAYER + "She accepts a quest from all three smith sisters at once. Teasing, a little nervous."},
            {"sq3c", "Three dungeons! Now metal, five of each kind, for all three anvils!", "c", "A loud, sunny dwarf armourer girl, cheers the hero for clearing three dungeons and asks for five of each metal. Loud, thrilled."},
            {"sq3d", "Weighed. All fifteen. Last: the Inkwell Depths. Its keeper's ink sets the seal.", "d", "A calm, exact dwarf jeweller-smith girl, has weighed all the metal and sends the hero to clear the Inkwell Depths. Calm, exact, solemn."},
            {"sq3e", "Three anvils. One seal. Yours. ...Proud of you. Don't make it weird.", "W", "A blunt, short-spoken dwarf smith girl, gives the hero the seal the three sisters forged together. Gruff, proud, embarrassed."},
    };

    /** Knuckles: the first time she holds them, her first uppercut, her first haymaker, and her first dash punch. */
    static final String[][] KNUCKLES = {
            {"knk1", "Knuckles! No more swinging things. I'll just punch it Sloppy~♡", "H", PLAYER + "She puts on golden gauntlets for the first time and grins, cracking her knuckles. Cheeky, fired up."},
            {"knk2", "Jab, jab, and up you go Sloppy~♡", "H", PLAYER + "She lands a quick combo ending in an uppercut. Snappy, rhythmic, pleased."},
            {"knk3", "Shields? Not against this fist Sloppy~♡", "H", PLAYER + "Her charged haymaker has just smashed through a shield. Smug, powerful."},
            {"knk4", "Coming through, fists first Sloppy~♡", "H", PLAYER + "She dashes forward into a punch at full speed. Breathless, gleeful."},
    };

    /** A glossary gem, the first time she takes one: pluses count double for a while. */
    static final String[][] GEM = {
            {"gem1", "A glossary gem! Every plus counts double while it glows Sloppy~♡", "H", PLAYER + "She picks up a glowing aqua gem and learns every plus is worth double for a while. Delighted, a little greedy."},
    };

    /** The dungeons: the stairway found, going down, between floors, the keeper, cleared, lost, three kinds of floor, and her own double in ink. */
    static final String[][] DUNGEONS = {
            {"dng1", "Stairs going down... a whole dungeon under here! Want to see how deep it goes Sloppy~♡", "H", PLAYER + "She has found a stone stairway leading down into a dungeon. Curious, excited, a little daring."},
            {"dng2", "Down we go. Stay close, it gets dark down here Sloppy~♡", "H", PLAYER + "She is heading down the stairs into a dungeon. Brave but whispering a little."},
            {"dng3", "Another floor down! Pick a blessing, quick Sloppy~♡", "H", PLAYER + "She has cleared a dungeon floor and is offered a choice of blessings. Pleased, eager."},
            {"dng4", "That's the keeper of this place. Big, and grumpy Sloppy~♡", "H", PLAYER + "A huge dungeon boss has appeared ahead. Nervous, but determined and a little teasing."},
            {"dng5", "The whole dungeon, cleared! The keeper's treasure is ours Sloppy~♡", "H", PLAYER + "She has beaten the dungeon's keeper and cleared every floor. Triumphant, delighted."},
            {"dng6", "The dungeon wins this time. Next dive, we go deeper Sloppy~♡", "H", PLAYER + "She has run out of lives in a dungeon. Disappointed but stubborn and hopeful."},
            {"dng7", "It's so dark... I'll keep my lantern close Sloppy~♡", "H", PLAYER + "She has come down onto a pitch-dark dungeon floor. Hushed, a little spooked."},
            {"dng8", "Look at all the pluses! A gilded floor Sloppy~♡", "H", PLAYER + "She has come down onto a dungeon floor glittering with treasure. Gleeful, greedy."},
            {"dng9", "So many of them on this floor... careful Sloppy~♡", "H", PLAYER + "She has come down onto a dungeon floor crawling with monsters. Wary, bracing herself."},
            {"dng10", "Is that... me? Made of ink? How rude Sloppy~♡", "H", PLAYER + "She meets a living ink shadow copying her. Startled, then offended and indignant."},
    };

    static final String[][] WATER = {
            {"wat1", "Blue ribbon, deep water. I can swim in this Sloppy~♡", "H", PLAYER + "She dives into deep water wearing the blue ribbon and finds she can swim and breathe. Delighted, surprised, bubbly."},
            {"wat2", "Can't breathe down here! The blue ribbon, quick Sloppy~♡", "H", PLAYER + "She has fallen into deep water without the blue ribbon and is holding her breath. Urgent, flustered, a little panicked."},
    };

    /** A new level: she says so, glowing. */
    static final String[][] LEVEL = {
            {"lvl1", "Level up! Feel that? That's me getting stronger Sloppy~♡", "H", PLAYER + "She has just gone up a level in a game and glows with new power. Thrilled, smug, flexing."},
            {"lvl2", "Another level. My blows hit harder now. Watch Sloppy~♡", "H", PLAYER + "She levels up and notices her attacks are stronger. Confident, eager to show off."},
            {"lvl3", "Higher and higher. There's no top to this, is there Sloppy~♡", "H", PLAYER + "She levels up again and realises her level has no limit. Delighted, a little greedy."},
    };

    /** At the counter where her pluses buy what she keeps. */
    static final String[][] SHOP = {
            {"shop1", "The guild counter. Spend my pluses wisely Sloppy~♡", "H", PLAYER + "She opens the upgrade shop. Businesslike, possessive of her coins, teasing."},
            {"shop2", "Ooh. I like that one Sloppy~♡", "H", PLAYER + "The player has just bought her an upgrade. Delighted, greedy, pleased."},
            {"shop3", "Not enough pluses. Go and find more Sloppy~♡", "H", PLAYER + "The player cannot afford an upgrade. Dismissive, sing-song, shooing."},
    };

    /** The first time each thing of the second volume comes into view, ever: {@link RunGame}'s numbers for them go on from those above. */
    static final String[][] SEE_LATER = {
            {"see10", "A slime in a tie. It hops at me. Land on it Sloppy~♡", "H", PLAYER + "A small hopping slime wearing a necktie appears for the first time. Amused, pointing." + FAST},
            {"see11", "A punctuation dragon. It spits full stops. Don't jump into it Sloppy~♡", "H", PLAYER + "A tiny flying dragon shaped like a comma appears for the first time. Wary, warning." + FAST},
            {"see12", "A censor bar. It tiptoes, then it dashes. Jump it Sloppy~♡", "H", PLAYER + "A sneaking black bar on little legs appears for the first time. Suspicious, urgent." + FAST},
            {"see13", "One of the Heart Guard. She will not stop following me Sloppy~♡", "H", PLAYER + "A charmed fan girl with glow sticks marches at her for the first time. Exasperated." + FAST},
            {"see14", "One of the queen's heavy words. Don't stop under it Sloppy~♡", "H", PLAYER + "A stone speech bubble hangs overhead, ready to fall, for the first time. Tense, hushed, hurried."},
            {"see15", "A question mark on a pogo stick. Why? Just why Sloppy~♡", "H", PLAYER + "A bouncing question mark appears for the first time. Baffled, laughing in disbelief." + FAST},
            {"see16", "That ledge is crumbling. Don't stand about Sloppy~♡", "H", PLAYER + "A cracked, crumbling bridge ahead for the first time. Nervous, hurrying the player."},
            {"see17", "A belt. It carries me whether I like it or not Sloppy~♡", "H", PLAYER + "A conveyor belt ahead for the first time. Grumbling, resigned." + FAST},
            {"see18", "Slime underfoot. I can't stop on this Sloppy~♡", "H", PLAYER + "Slippery slime ground ahead for the first time. Alarmed, sliding already." + FAST},
            {"see19", "A draught. Step in and it carries me over Sloppy~♡", "H", PLAYER + "A column of rising wind over a pit for the first time. Pleased, a little excited." + FAST},
            {"see20", "That ledge moves. Wait for it Sloppy~♡", "H", PLAYER + "A moving platform over a pit for the first time. Calm, coaching, patient for once."},
            {"see21", "A bookmark door. In one, out the other Sloppy~♡", "H", PLAYER + "A glowing magic door ahead for the first time. Breezy, matter-of-fact." + FAST},
            {"see22", "Molten type. Hot. Do not drop me in it Sloppy~♡", "H", PLAYER + "A pit of glowing lava ahead for the first time. Alarmed, firm, threatening the player a little."},
            {"see23", "Water. I can't swim. Don't test that Sloppy~♡", "H", PLAYER + "A pool of water ahead for the first time. Uneasy, defensive, embarrassed." + FAST},
            {"see24", "One of Galpi's ribbons. Tie it on and my hair changes. So do I Sloppy~♡", "H", PLAYER + "A magic hair ribbon lies ahead for the first time. Intrigued, eager, a little vain." + FAST},
    };

    /** On tying on each ribbon, by the form it gives: red, blue, brown, green, yellow, purple, black, white. */
    static final String[][] FORMS = {
            {"form1", "Red! I'm on fire. Lava can't touch me now Sloppy~♡", "H", PLAYER + "Her hair turns red and she can throw fire. Fierce, thrilled, crackling with energy." + FAST},
            {"form2", "Blue. Cool as water. And now I can swim Sloppy~♡", "H", PLAYER + "Her hair turns blue and she can swim and throw water. Smooth, pleased, a little smug."},
            {"form3", "Brown. Solid as a wall. Let them bump into me Sloppy~♡", "H", PLAYER + "Her hair turns brown and enemies bounce off her. Sturdy, cocky, daring them." + FAST},
            {"form4", "Green! Hold the jump and I fly Sloppy~♡", "H", PLAYER + "Her hair turns green and she can fly on the wind. Light, delighted, breathless." + FAST},
            {"form5", "Yellow! Try to keep your eyes on me Sloppy~♡", "H", PLAYER + "Her hair turns yellow and she becomes extremely fast. Giddy, zippy, talking very fast indeed."},
            {"form6", "Purple. Throw it at me. It's coming right back Sloppy~♡", "H", PLAYER + "Her hair turns purple and she reflects whatever is thrown at her. Cool, sly, taunting."},
            {"form7", "A black ribbon! Every colour's gone, and none comes back till it fades Sloppy~♡", "H",
                    PLAYER + "Her hair has just turned black: a curse has stripped every one of her magic colours and she cannot use any until it wears off. Dismayed, annoyed with herself, urgent." + FAST},
            {"form8", "White. Every colour at once, while it lasts Sloppy~♡", "H", PLAYER + "Her hair turns white as every one of her colours is on at once, for a short while. Awed for a moment, then pleased."},
    };
    /** What Tsubakieh and Illya say as they hand over what they have. */
    static final String[][] GIFTS = {
            {"gift5", "Normal No. 3. A perfectly normal home run.", "T", "The girl with the mended bat knocks every enemy nearby out of the park for her friend. Light, cute, cheerful, deadpan, satisfied."},
            {"gift6", "You are all wonderful♡ Now hold still♡", "I", "The magical-girl idol compliments a crowd of enemies so sweetly that they freeze, charmed. Bright, sugary, high, sparkling."},
    };
    /** When the second volume hurts her: lava, water, the ground jumping under King Slime, and a sword. */
    static final String[][] BURNED = {
            {"burn1", "Hot hot hot! That's lava Sloppy~♡", "H", PLAYER + "She has just touched lava. Yelping, hopping, outraged, very fast."}};
    static final String[][] SANK = {
            {"sank1", "Glub. I told you I can't swim Sloppy~♡", "H", PLAYER + "She has just sunk in water. Spluttering, soggy, sulking."}};
    static final String[][] QUAKED = {
            {"quake1", "The ground jumped! Be in the air when he lands Sloppy~♡", "H", PLAYER + "A giant slime landed and the shock threw her. Jolted, urgent, scolding." + FAST}};
    static final String[][] SLASHED = {
            {"slash1", "Ow! She cut me in her sleep Sloppy~♡", "H", PLAYER + "A sleeping swordswoman has just cut her. A yelp, then disbelief." + FAST}};
    static final String[][] PARRIED = {
            {"parry1", "She turned that aside. Wait for the opening Sloppy~♡", "H", PLAYER + "A boss has just blocked an attack. Frustrated, thinking, coaching." + FAST}};
    /** What those who wait in the second volume say when a blow lands, in the order {@link RunLevels} numbers them; for King Slime, she says it. */
    static final String[][] BLOWS_LATER = {
            {"blow3", "Unhand... it is being requested that you stop!", "G", "The sly interpreter has just been hit and drops her composure, then grabs at it again. Shocked, flustered, stiffly formal."},
            {"blow4", "Ow! I did not read that coming!", "J", "The newspaper girl who reads ahead has just been hit by surprise. A squeak, indignant, fast."},
            {"blow5", "Eep. Please do not read me.", "K", "The shy twin has just been hit. A tiny squeak, timid, hiding."},
            {"blow6", "Mm? Ow. I'm up, I'm up.", "Y", "The sleepy swordswoman has just been hit while dozing. Startled awake, groggy, slow."},
            {"blow7", "One chunk off him. Keep cutting Sloppy~♡", "H", PLAYER + "She has just cut a piece off a giant slime. Fierce, pleased." + FAST},
            {"blow8", "...You hit like someone with friends.", "M", "The lonely other self has just been hit, and it stirs something in her. Quiet, surprised, a crack in her coldness."},
            {"blow9", "Ow! That is a pass mark!", "D", "The knight has just been hit and has to admit it was a good one. Earnest, startled, loud."},
            {"blow10", "My nib! Most irregular.", "F", "The fountain-pen lady butler has just been hit. Affronted, prim, pained."},
            {"blow11", "Oh... that will have to be written down.", "Q", "The quiet scribe has just been hit. Soft, surprised, a little dazed."},
            {"blow12", "That one is going on your bill!", "V", "The merchant girl has just been hit. Indignant, quick, bright."},
            {"blow13", "Bwah! Okay, that was a good one!", "X", "The diver girl has just been hit and laughs it off. Loud, sunny, winded."},
            {"blow14", "Ow! That's coming out of our lining!", "Z", "The two brake pads have just been hit. Gruff, pained, comic."},
            {"blow15", "We went straight... into that.", "a", "The swordswoman has just led her party straight into a blow. Winded, sheepish, still proud."},
            {"blow16", "Oof. Right in the spine.", "b", "The old book has just been hit. Winded, grumbling, grandfatherly."},
    };
    /** What they say as they go at her; King Slime says nothing. */
    static final String[][] AIM_LATER = {
            {"aim3", "It is being said that you should duck.", "G", "The interpreter lowers her fan and sends twisted words flying at her opponent. Silky, mocking, stiffly formal."},
            {"aim4", "Spoiler: this one hits!", "J", "The newspaper girl throws her rolled-up paper at where her opponent is about to be. Gleeful, cocky, fast."},
            {"aim5", "Censored.", "K", "The shy twin paints black bars across everything her opponent can see. A whisper, nervous."},
            {"aim6", "Excuse my cut. Mmm.", "Y", "The sleepy swordswoman steps out behind her opponent and swings, half asleep. Drowsy, polite, slow."},
            {"aim8", "Alone is easier.", "M", "The lonely other self throws black feathers at the friend who came for her. Cold, tired, hollow."},
            {"aim9", "Thrusting. Please step back.", "D", "The knight warns the opponent before thrusting the halberd. Earnest, formal, loud."},
            {"aim10", "A small correction, if I may.", "F", "The fountain-pen lady butler flicks ink at her opponent. Polite, dry."},
            {"aim11", "Noted.", "Q", "The quiet scribe throws a scroll at her opponent. Soft, flat."},
            {"aim12", "Catch! Exact change!", "V", "The merchant girl flings coins at her opponent. Bright, gleeful."},
            {"aim13", "Coming up!", "X", "The diver girl bursts up out of the page under her opponent. Loud, sunny."},
            {"aim14", "Brakes on!", "Z", "The two brake pads stamp down together. Gruff, loud."},
            {"aim15", "Don't turn! Never turn!", "a", "The swordswoman charges straight at her opponent. Brash, loud."},
            {"aim16", "Zzz... bubbles...", "b", "The old book snores bubbles at his opponent, half asleep. Slow, mumbling."},
    };

    /**
     * What those met in the villages say as they hand over what they hold, in the order
     * {@link RunLevels} numbers them from Somi: Somi, Inju, Boksun, Ttosun, Galpi, Yeseo,
     * Nabi, Moru, then Altteul calling from her stall, and the queen.
     */
    static final String[][] MEET = {
            {"meet5", "Halgakos-nim! The Footnote Party found a magnet in the margins. Take it, take it!", "S",
                    "A cheerful young adventurer girl spots her hero in a village and eagerly hands her a magnet. Bright, excited, high."},
            {"meet6", "Form 12-B, stamped. These are the notes of the one who came before you. Next, please.", "U",
                    "The guild's polite receptionist hands a stamped page of notes across her counter. Crisp, pleasant, efficient, with a small smile."},
            {"meet7", "Aigoo, you're all bones. Eat this bread while it's warm, dear.", "B",
                    "A kind old baker granny presses a warm loaf on a girl passing through her village. Warm, fussing, gentle, elderly."},
            {"meet8", "Ice cream! The second scoop is on the guild. Run before it melts!", "O",
                    "A lively, sporty guild girl proudly hands over an ice cream cone. Loud, grinning, energetic."},
            {"meet9", "A green ribbon keeps your place in the wind. Tie it on, and hold the jump.", "P",
                    "The quiet bookmark witch ties a green ribbon into the heroine's hair and explains softly what it does. Soft, mysterious, kind."},
            {"meet10", "Footnote one: pluses come to a magnet. Footnote two: mind the spikes.", "E",
                    "The careful leader of the Footnote Party hands over a magnet with advice, as if reading footnotes aloud. Precise, earnest, quick."},
            {"meet11", "Nyahaha, I saved you the cold one! Don't tell Moru, nya!", "A",
                    "A playful cat-eared girl of the Footnote Party sneaks the heroine an ice cream. Mischievous, giggly, high."},
            {"meet12", "Ink. Rune-ssi made extra. It takes a hit so you don't.", "W",
                    "A blunt, short-spoken smith girl of the Footnote Party hands over a ball of protective ink. Flat, gruff for a girl, secretly kind."},
            {"meet13", "Welcome, welcome! Pluses from this very land spend here. No refunds!", "L",
                    "A thrifty shopkeeper girl calls a customer over to her market stall, abacus in hand. Brisk, bright, sales patter."},
            {"meet14", "These are my own notes, in my own words. No one may twist them. Take them.", "N",
                    "The young queen, in a village in her palace grounds, entrusts a page of her own notes to the heroine. Clear, earnest, formal, brave."},
    };
    /** What Altteul says at her stall when something is bought, and when there is not enough to buy it with. */
    static final String[][] STALL = {
            {"stall1", "Sold! A pleasure doing sums with you!", "L", "The shopkeeper hands over a purchase and flicks her abacus. Delighted, brisk."},
            {"stall2", "Not enough pluses, dear customer. The land is full of them!", "L",
                    "The shopkeeper cheerfully turns down a customer who cannot pay. Bright, teasing, businesslike."},
            {"stall3", "Sold out, dear customer! That was the last one. My next stall has more!", "L",
                    "The shopkeeper shows an empty shelf and points down the road to her next stall. Bright, apologetic, businesslike."},
            {"stall4", "One colour at a time, dear customer! Wear that one out first!", "L",
                    "The shopkeeper refuses to sell a hair ribbon to someone already wearing one. Bright, firm, amused."},
            {"stall5", "You don't need that yet, dear customer. I don't sell what won't be used!", "L",
                    "The shopkeeper waves away a purchase the customer has no use for. Bright, honest, brisk."},
    };
    /**
     * What is said as each of those who wait arrives, once the way back has shut: Zero,
     * Illya, Tsubakieh, then those of the second volume in the order {@link RunLevels} numbers
     * them, Miri speaking for the twins. For Zero and King Slime, who say nothing, she says it.
     */
    static final String[][] ENTER = {
            {"enter0", "Zero. It rolled in and the whole page shook. Off its floor when it glows Sloppy~♡", "H",
                    PLAYER + "A giant zero has just crashed into the arena in front of her. Braced, urgent, coaching." + FAST},
            {"enter1", "Lights, please♡ Tonight's stage is all for you, Halgakos-ssi♡", "I",
                    "The magical-girl idol floats down into a spotlight to begin a duel as if it were her concert. Sparkling, sweet, high, sing-song."},
            {"enter2", "Excuse me. I am here for a normal amount of batting practice.", "T",
                    "The girl with the bat strolls into the arena and takes one casual practice swing. Cheerful, calm, deadpan."},
            {"enter3", "It is being announced that the heroine has no lines in this scene.", "G",
                    "The sly interpreter unfolds from behind her black fan to begin a duel. Smooth, smug, stiffly formal."},
            {"enter4", "Extra, extra! Heroine loses in the next panel! I read ahead!", "J",
                    "The newspaper girl drops into the arena on a huge gazette page, shouting like a paper seller. Loud, gleeful, fast."},
            {"enter6", "Mmm... is it my scene already... sorry about the sword.", "Y",
                    "The sleepy swordswoman steps out of the blank space between the lines, half asleep, to begin a duel. Drowsy, mild, apologetic."},
            {"enter7", "He just swallowed a whole footnote. Jump when he lands Sloppy~♡", "H",
                    PLAYER + "A huge slime king has oozed down into the arena and eaten something. Disgusted, urgent." + FAST},
            {"enter8", "This is the villain's entrance. I know it by heart. I wrote it sixty years ago.", "M",
                    "The lonely other self descends on black wings and takes off her mask to begin the last fight. Tired, low, hollow, slow."},
            // And the seven others, in the order they are numbered: for the Brake Pads and the Straight-Ahead Party she says it.
            {"enter9", "Halt! Guild entrance exam. Three questions, and all of them are halberd.", "D",
                    "A knight in full armour blocks the way and announces an entrance exam, halberd in hand. Earnest, loud, stern, a little muffled by the helmet."},
            {"enter10", "Good evening. Kindly do not smudge the Archive. I should have to blot you.", "F",
                    "A prim lady butler who is a living fountain pen bars the way with perfect manners and a faint threat. Polite, dry, unhurried."},
            {"enter11", "The court record says you were never here. I am correcting the land to match.", "Q",
                    "A quiet court scribe girl looks up from her book and says, softly, that she will write the heroine out. Soft, flat, precise, almost a whisper."},
            {"enter12", "Toll! Every plus you carry, plus interest. I counted them on your way in.", "V",
                    "A sly merchant girl with an abacus steps into the way and demands a toll. Bright, quick, grinning, sales patter."},
            {"enter13", "Found you! I dive between the lines for a living. Want to see how deep it goes?", "X",
                    "A cheerful diver girl pops up out of the blank page with a crowbar on her shoulder. Loud, sunny, daring, out of breath."},
            {"enter14", "Break's over! Nobody gets past us without stopping. Hard hats on!", "Z",
                    "Two living brake pads in hard hats clock in to stop the heroine, speaking as one. Gruff, cheerful, workmanlike, a little worn out."},
            {"enter15", "Straight-Ahead Party, forward! We don't do corners!", "a",
                    "The red-haired swordswoman who leads three adventurers that only ever go straight ahead orders the charge. Brash, loud, proud."},
            {"enter16", "Mmf... who turned my page? I was in the middle of a nap. A hundred years of it.", "b",
                    "A very old bearded book, asleep at the bottom of a cellar, wakes up grumbling. Slow, sleepy, grandfatherly, a little cross."},
    };
    /** What is said when each of them changes, at a third and at two thirds of the way: two lines each, in the same order; of the twins, Miri first and then Gari. */
    static final String[][] PHASES = {
            {"phase0a", "It's glowing. Times nothing! Get off the floor Sloppy~♡", "H",
                    PLAYER + "The giant zero begins a new attack that erases whatever she carries. Alarmed, urgent." + FAST},
            {"phase0b", "Now it's making little ones. Typical nothing Sloppy~♡", "H",
                    PLAYER + "The giant zero, nearly beaten, starts spawning small minus signs. Exasperated, fierce." + FAST},
            {"phase1a", "Encore♡ Hearts from every side this time♡", "I",
                    "The idol, hit a few times in a duel, spins and starts a harder song. Sparkling, breathless, still sweet."},
            {"phase1b", "Final number♡ Heart Guard, on stage♡", "I",
                    "The idol, nearly beaten, calls her fan club onto the stage for her last song. Bright, a little desperate, high."},
            {"phase2a", "I see. Then I will throw it. That is also normal.", "T",
                    "The girl with the bat, hit a few times, decides to throw her bat as if that were sensible. Cheerful, deadpan."},
            {"phase2b", "Running start. Please stand still.", "T", "The girl with the bat, nearly beaten, politely announces a charge. Calm, cheerful, deadpan."},
            {"phase3a", "It is being suggested that left is now right.", "G",
                    "The sly interpreter, hit a few times, begins twisting her opponent's controls. Silky, pleased with herself, formal."},
            {"phase3b", "Unacceptable. The fan will be speaking for itself.", "G",
                    "The sly interpreter, losing, drops her politeness for a moment and throws the blades of her fan. Sharp, cold, clipped."},
            {"phase4a", "Second edition! Three copies, all for you!", "J", "The newspaper girl, hit a few times, throws three papers at once. Gleeful, fast, loud."},
            {"phase4b", "I... I will paint it myself... sorry...", "K",
                    "The shy twin, cornered, gathers her courage and charges with her brush. A trembling whisper, timid but determined."},
            {"phase6a", "Mmm. From above, then. Mind your head.", "Y", "The sleepy swordswoman, hit a few times, yawns and attacks from overhead. Drowsy, mild."},
            {"phase6b", "Three cuts, and then a nap. Promise.", "Y",
                    "The sleepy swordswoman, nearly beaten, promises herself a nap after one last combination. Sleepy, soft, a little firmer."},
            {"phase7a", "He's splitting! Chunks of him are hopping about Sloppy~♡", "H",
                    PLAYER + "The slime king has started throwing off small slimes. Startled, grossed out." + FAST},
            {"phase7b", "Two of three. He's sliding now. Jump it Sloppy~♡", "H",
                    PLAYER + "The slime king, much smaller now, starts sliding across the arena. Fierce, focused." + FAST},
            {"phase8a", "You run with the wind. I had that ribbon once. Mine went dark.", "M",
                    "The lonely other self, wounded, starts using dark versions of her friend's own powers. Low, tired, bitter, slow."},
            {"phase8b", "Then be ordinary. See how long they stay.", "M",
                    "The lonely other self, nearly beaten, throws a black ribbon that makes her opponent an ordinary girl. Hollow, low, her voice wavering."},
            {"phase9a", "Second question! It is also halberd, but lower!", "D", "The knight, hit a few times, announces the next part of the exam. Earnest, loud, flustered."},
            {"phase9b", "Final question! Forward, march!", "D", "The knight, nearly beaten, gives the order to charge. Earnest, loud, determined."},
            {"phase10a", "Permit me to underline that.", "F", "The fountain-pen lady butler, hit a few times, readies a dash on her nib. Polite, dry, a little sharper."},
            {"phase10b", "This will require my signature.", "F", "The fountain-pen lady butler, nearly beaten, prepares her finest attack with wounded dignity. Polite, cold, clipped."},
            {"phase11a", "Footnote. See below.", "Q", "The quiet scribe, hit a few times, makes ink fall from above. Soft, flat, precise."},
            {"phase11b", "Struck from the record. In ink.", "Q", "The quiet scribe, nearly beaten, sends a black bar to strike the heroine out. Soft, cold, final."},
            {"phase12a", "Prices just went up!", "V", "The merchant girl, hit a few times, slides in on her abacus. Bright, gleeful, quick."},
            {"phase12b", "Final offer! Everything must go!", "V", "The merchant girl, nearly beaten, throws everything she has. Loud, desperate, still selling."},
            {"phase13a", "Deep breath! Going under!", "X", "The diver girl, hit a few times, dives into the page to come up under her opponent. Loud, sunny, daring."},
            {"phase13b", "Net's out! Don't wriggle!", "X", "The diver girl, nearly beaten, throws her net. Loud, breathless, laughing."},
            {"phase14a", "Skid marks, coming through!", "Z", "The two brake pads, hit a few times, start skidding across the arena. Gruff, loud, enjoying it."},
            {"phase14b", "We're worn thin... sparks it is, then!", "Z", "The two brake pads, nearly beaten, throw sparks. Tired, stubborn, gruff."},
            {"phase15a", "Archer! Low and high! Nobody stops!", "a", "The swordswoman, hit a few times, orders her archer to shoot at two heights. Brash, loud, bossy."},
            {"phase15b", "All together now! Straight! Ahead!", "a", "The swordswoman, nearly beaten, orders all three to charge at once. Loud, desperate, proud."},
            {"phase16a", "Now I am awake. Mind the pages.", "b", "The old book, hit a few times, shakes his pages loose. Slow, grumbling, grandfatherly."},
            {"phase16b", "Very well. I shall close on you.", "b", "The old book, nearly beaten, threatens to slam himself shut. Slow, stern, sleepy still."},
    };
    /** As she goes through a door into a place off the land, by its kind: a stash, a passage, a cellar, a room where pluses fall. */
    static final String[][] ROOMS = {
            {"room0", "A hidden room. Finders keepers Sloppy~♡", "H",
                    PLAYER + "She has just stepped through a door into a secret room full of treasure. Delighted, greedy, half whispering." + FAST},
            {"room1", "A secret passage. I knew this book had one Sloppy~♡", "H",
                    PLAYER + "She has just found a secret passage behind a wall. Smug, pleased with herself." + FAST},
            {"room2", "A cellar. And it shut behind me. Clear them out Sloppy~♡", "H",
                    PLAYER + "A cellar door has just slammed shut behind her and enemies are appearing. Alarmed, then bossy." + FAST},
            {"room3", "Pluses are falling from the ceiling. Catch them Sloppy~♡", "H",
                    PLAYER + "She has walked into a room where coins rain from above. Excited, urgent." + FAST},
            {"room4", "A range. And an ink gun that never runs dry. Stand back Sloppy~♡", "H",
                    PLAYER + "She has walked into a shooting range and been handed an ink gun with endless shots. Gleeful, trigger-happy." + FAST},
            {"room5", "They keep coming. Flatten them as fast as they come Sloppy~♡", "H",
                    PLAYER + "She has walked into a room where little monsters keep appearing, a game of stomping them. Eager, bossy, grinning." + FAST},
    };
    /** When she comes near a crack with a door behind it; and when a cellar is cleared and when the pluses stop falling. */
    static final String[][] CRACK = {
            {"crack1", "There's a draught from that crack. Break it open Sloppy~♡", "H",
                    PLAYER + "She has noticed a cracked wall with air coming through it. Curious, conspiratorial." + FAST}};
    static final String[][] ROOM_WON = {
            {"clear1", "Cellar cleared. Was that all of them Sloppy~♡", "H",
                    PLAYER + "She has just beaten the last wave of enemies in a sealed cellar. Out of breath, cocky." + FAST},
            {"caught1", "Time. Not bad for one pair of hands Sloppy~♡", "H",
                    PLAYER + "A coin-catching game has just ended. Pleased, a little smug." + FAST},
            {"ranged1", "Time. Count the holes in them Sloppy~♡", "H",
                    PLAYER + "A target-shooting game has just ended. Smug, pleased." + FAST},
            {"stomped1", "Time. That floor is flat as a page now Sloppy~♡", "H",
                    PLAYER + "A game of stomping little monsters has just ended. Out of breath, triumphant." + FAST},
            {"chest1", "The chest. Worth the climb, and it's mine Sloppy~♡", "H",
                    PLAYER + "She has just climbed up to a treasure chest and opened it. Delighted, greedy, pleased with herself." + FAST},
    };
    /**
     * What is asked of her in a land, said by whoever asks it as she hands over what she
     * holds, in the order {@link RunLevels} numbers what may be asked: Boksun's rice cakes,
     * Inju's cellar, Somi's letter, Ttosun's race, Galpi's hidden room. And what is said when
     * it is done: by the same, but for the letter, which Yeseo has by then.
     */
    static final String[][] ASKS = {
            {"ask1", "Aigoo, my three rice cakes rolled off about this land. Find them for me, dear?", "B",
                    "A kind old baker granny asks a passing girl to find three rice cakes she has lost. Warm, fussing, a little flustered, elderly."},
            {"ask2", "Guild request: slimes in the cellar, three waves. The door with the iron ring.", "U",
                    "The guild's receptionist reads out a job from a form: clearing a cellar of monsters. Crisp, pleasant, efficient."},
            {"ask3", "Halgakos-nim! Take this letter to Yeseo-unnie by the gate? Don't get hit, it creases!", "S",
                    "A cheerful young adventurer girl presses a letter on her hero and begs her to deliver it carefully. Bright, eager, high, a little worried."},
            {"ask4", "Race you! Reach the next flag before my count runs out. Go, go, go!", "O",
                    "A lively, sporty guild girl challenges the heroine to a race against her count. Loud, grinning, energetic."},
            {"ask5", "A page is missing here. There is a room behind a cracked wall. Find it.", "P",
                    "The quiet bookmark witch tells the heroine that a hidden room lies behind a cracked wall somewhere in this land. Soft, mysterious, calm."},
    };
    static final String[][] ASKED = {
            {"done1", "All three! You have a good heart. Now eat something.", "B",
                    "The old baker granny has her three lost rice cakes back and is delighted. Warm, fussing, pleased, elderly."},
            {"done2", "Request closed. The guild thanks you. Stamped.", "U",
                    "The guild receptionist stamps a finished job with satisfaction. Crisp, pleasant, a small proud smile."},
            {"done3", "A letter from Somi? Not one crease. Footnote: thank you.", "E",
                    "The careful leader of the Footnote Party receives a letter in perfect condition and is touched. Precise, earnest, warm underneath."},
            {"done4", "You beat the count! Faster than a guild courier!", "O",
                    "The sporty guild girl cheers because the heroine has won her race. Loud, delighted, out of breath."},
            {"done5", "You found the hidden page. Every book keeps one.", "P",
                    "The bookmark witch is quietly pleased that the hidden room has been found. Soft, dreamy, content."},
    };
    /**
     * At the guild hall's board of hunts. Inju: the first time the board is looked at; when
     * a hunt is taken on; when one is handed in; and when her rank in the guild rises. And
     * Halgakos, the first time she looks at it.
     */
    static final String[][] HUNTS = {
            {"hunt1", "The hunts board. Three at a time, by guild rule. Bring them back done, and I stamp them.", "U",
                    "The guild's receptionist explains the board of monster-hunting jobs to a newcomer. Crisp, pleasant, efficient, with a small smile."},
            {"hunt2", "Hunt registered. Form 7-H. Do come back in one piece.", "U",
                    "The guild's receptionist registers a job the heroine has just taken on. Crisp, pleasant, a touch of dry warmth at the end."},
            {"hunt3", "Hunt closed. Counted twice, paid in full. Stamped.", "U",
                    "The guild's receptionist pays out a finished monster hunt and stamps the form with satisfaction. Crisp, pleased, efficient."},
            {"hunt4", "A new guild rank. The badge is yours to keep. The paperwork is mine.", "U",
                    "The guild's receptionist awards the heroine a higher guild rank and its badge. Crisp, proud, a small smile in the voice."},
            {"hunt5", "Hunting for pay. I like the pay part Sloppy~♡", "H",
                    PLAYER + "She looks over a noticeboard of paid monster hunts for the first time. Greedy, amused, pleased." + FAST},
    };
    /**
     * In the smithy, which three sisters keep. Each has a word the first time she is walked
     * up to, and one when she has made something: Moru, who forges what Halgakos fights
     * with; Pulmu, who makes what she wears; and Sutdol, who trades in what a smith works
     * with and undoes her sisters' work.
     */
    static final String[][] SMITHS = {
            {"smith1", "Smithy's hot. Hand it over. I'll hit it till it's better.", "W",
                    "A blunt, short-spoken dwarf smith girl greets a customer at her forge, hammer in hand. Flat, gruff for a girl, secretly pleased."},
            {"smith2", "Done. Sharper. Don't thank me. Swing it.", "W",
                    "The blunt smith girl hands back a weapon she has just improved. Flat, short, proud underneath."},
            {"smith3", "Welcome, welcome! I'm Pulmu, I do the armour! Stand still, I'll measure you!", "c",
                    "A loud, sunny dwarf armourer girl greets a customer, tongs in hand. Big, warm, laughing."},
            {"smith4", "There! Fits like a book cover! Go on, get hit, see if I care!", "c",
                    "The cheerful armourer girl has just finished a piece of armour and is delighted with it. Loud, laughing, proud."},
            {"smith5", "Sutdol. I trade in metal, and I undo my sisters' work. Precisely.", "d",
                    "A calm, exact dwarf jeweller-smith girl introduces herself, studying a whetstone through her loupe. Quiet, level, dry."},
            {"smith6", "Weighed twice. The scales agree with me.", "d",
                    "The calm, exact smith girl completes a trade of metal, or takes a weapon apart again. Quiet, satisfied, dry."},
    };
    /**
     * In the black market, which Jupan keeps on the side: the first time she is walked up
     * to; as Halgakos goes down into the black arena; and when something is bought, or a
     * rung's prize paid. And Halgakos, the first time she is in one.
     */
    static final String[][] MARKET = {
            {"market1", "Psst. Over here. Harder lands, an arena downstairs, and tokens buy the rest. You were never here.", "V",
                    "A sly young merchant girl runs a secret black market and greets a customer in a low, quick, delighted voice. Conspiratorial, bright, always bargaining."},
            {"market2", "Down the stairs, then. Come back up, and I pay. Don't, and I keep the change.", "V",
                    "The sly merchant girl sends a fighter down into her secret arena, counting on her abacus. Quick, teasing, businesslike."},
            {"market3", "A pleasure. No receipts. Nothing was sold, nobody bought it.", "V",
                    "The sly merchant girl closes a deal in her black market and flicks her abacus. Quick, pleased, hushed."},
            {"market4", "A black market under a village. I'm not even surprised. I'm shopping Sloppy~♡", "H",
                    PLAYER + "She walks into a hidden black market for the first time and looks around, unimpressed and interested. Dry, amused, greedy." + FAST},
    };
    /**
     * What each of her friends says as she pledges her help, in the lands of the third
     * volume, in the order of the lands from the eighty-first: Chikuwa, Rune, Illya,
     * Tsubakieh, the Guild Guard, Penchok-jipsa, Gokhae, Byeoru, Miri for the twins, the
     * Mawang, Jupan, Yeobaek, Sumbi, the queen, the Brake Pads, the Straight-Ahead Party,
     * Moru, Inju and Somi.
     */
    static final String[] FRIENDS = {"Chikuwa", "Rune", "Illya", "Tsubakieh", "The Guild Guard", "Penchok-jipsa", "Gokhae", "Byeoru", "The Gazette Twins", "The Mawang", "Jupan",
            "Yeobaek", "Sumbi", "Nuri-yeowang", "The Brake Pads", "The Straight-Ahead Party", "Moru", "Inju", "Somi"};
    static final String[][] PLEDGES = {
            {"pledge1", "Bread for the whole army, Halgakos-ssi! I pledge my oven!", "C",
                    "The sunny baker girl promises her friend all the help she can give before a great battle. Loud, bright, bubbling, high."},
            {"pledge2", "I-I'll draw every line you need... sniff... I p-pledge my ink... uu...", "R",
                    "The little crybaby librarian promises her help, in tears and meaning it. Sobbing softly, tiny, devoted."},
            {"pledge3", "My compliments are yours♡ I pledge my stage, and all nine thousand fans♡", "I",
                    "The magical-girl idol promises her help with a flourish. Bright, sweet, high, sparkling."},
            {"pledge4", "Normal No. 3 is ready. I pledge one swing. Normally that is enough.", "T",
                    "The cheerful girl with the bat promises her help in her calm, deadpan way. Light, pleasant, unbothered."},
            {"pledge5", "The guild stands with you. I pledge my shield. That is an order to myself.", "D",
                    "The lady knight who guards the guild promises her help, very formally. Low, firm, dutiful."},
            {"pledge6", "I shall keep your ink topped up, madam. Consider it pledged, and underlined.", "F",
                    "The fountain-pen lady butler promises her help with impeccable politeness. Light, dry, crisp."},
            {"pledge7", "It is being pledged that this interpreter will interpret faithfully. For once.", "G",
                    "The sly court interpreter, reformed, promises to be faithful in her stiff, over-formal way. Smooth, cool, a little embarrassed."},
            {"pledge8", "I will write down what really happens. Every word. I pledge the record.", "Q",
                    "The quiet court scribe promises to keep a true record. Soft, flat, precise, sincere."},
            {"pledge9", "Extra, extra! The Gazette pledges its front page! And no spoilers this time!", "J",
                    "The gossipy newspaper girl promises her paper's help, for herself and her twin. High, quick, chirpy."},
            {"pledge10", "Sixty years I needed no one. I pledge the rest of them to you.", "M",
                    "The lonely older self makes her promise. Low, slow, quiet, warm."},
            {"pledge11", "My pledge has no fine print. Don't tell anyone. It's bad for business.", "V",
                    "The sly merchant girl promises her help for free and is a little shy about it. Quick, bright, hushed."},
            {"pledge12", "I pledge one cut. A real one. Wake me when it's time.", "Y",
                    "The sleepy swordswoman promises her help between yawns. Soft, drowsy, slow, sincere."},
            {"pledge13", "I'll dive under whatever she throws! Pledged! Count on me!", "X",
                    "The cheerful diver girl promises her help, out of breath and grinning. High, loud, sunny."},
            {"pledge14", "We pledge the crown's own words, in Our own voice, to be kept as they were spoken.", "N",
                    "The young queen promises her help, formally and bravely. Clear, earnest, regal."},
            {"pledge15", "We pledge to stop her. Stopping is what we do.", "Z",
                    "The two hard-working brake pads promise their help as one. Tomboyish, gruff, cheerful."},
            {"pledge16", "Straight ahead, no turning! The party pledges its swords!", "a",
                    "The brash swordswoman promises her whole adventuring party's help. Loud, proud, hot-blooded."},
            {"pledge17", "Hammer's yours. Pledged. Don't make it a speech.", "W",
                    "The blunt smith girl promises her help in as few words as she can. Flat, gruff for a girl, kind underneath."},
            {"pledge18", "Form 100-L: pledge of the whole guild. Stamped. Twice.", "U",
                    "The guild's receptionist promises the guild's help by stamping a form. Crisp, pleasant, proud."},
            {"pledge19", "Halgakos-nim! The Footnote Party pledges everything! Even my magnet!", "S",
                    "The tiny, eager adventurer girl promises everything she has. Very high, loud, excited."},
    };
    /** At the ribbon gacha: the first time she rolls one; and when a black ribbon blows it up. */
    static final String[][] GACHA = {
            {"gacha1", "A ribbon gacha. One roll. Maybe two. I can stop whenever I like Sloppy~♡", "H",
                    PLAYER + "She puts coins into a capsule-toy machine that gives hair ribbons, pretending she is not excited. Eager, bluffing, quick." + FAST},
            {"gacha2", "It blew up. A black one. All my ribbons, gone Sloppy~♡", "H",
                    PLAYER + "The capsule machine has just exploded in her face and taken her prizes with it. Stunned, sooty, dismayed, a little funny." + FAST},
    };
    /**
     * What is said once the flight is won, in the sky over the library, before what nothing announces: Lucy, read and unhurt, hands
     * over her brother's book; Halgakos reads it as it was written; and the one who had it made readable answers from the sky. Then
     * the Mother Tongue rises, and Halgakos is on Lucy's back.
     */
    static final String[][] REVEAL = {
            {"out100a", "...It didn't hurt. You read me, and it didn't hurt.", "e",
                    "The little dragon girl, a child again and worn out after the fight, is astonished that being understood did not hurt. Very small, quiet, sniffling, wondering."},
            {"out100b", "It never does. I only say what was meant. Now show me the book Sloppy~♡", "H",
                    PLAYER + "She kneels by the little dragon girl she has just beaten and speaks gently to her. Soft, kind, sure of herself, a little tired."},
            {"out100c", "...Promise you won't change a single word?", "e",
                    "The little dragon girl hugs a book to her chest and asks the translator for a promise. A child's voice, small, suspicious, hopeful."},
            {"out100d", "Every word as written. Honorifics and all. Cross my horns Sloppy~♡", "H",
                    PLAYER + "She makes a solemn promise to a child, and means it, with a small joke at the end. Warm, steady, fond."},
            {"rev1", "'Lucy-tan. If you can read this, I am already late. Onii-tan is sorry.' That is what he wrote. Word for word Sloppy~♡", "H",
                    PLAYER + "She reads aloud, faithfully, the first lines a long-lost brother wrote to his little sister, and then says that this is exactly what he wrote. Gentle and careful while reading, then quiet and certain."},
            {"rev2", "...Lucy-tan. He wrote Lucy-tan. Then who wrote 'Miss Lucy'?", "e",
                    "The little dragon girl hears, for the first time in a hundred years, the name her brother really called her, and realises someone changed it. A child's voice, stunned, trembling, then sharp."},
            {"rev3", "I did, little one. I had it made readable.", "f",
                    "A vast elder dragon answers her daughter from high in the sky, perfectly calm, as if it were the most reasonable thing in the world. Cold, smooth, unhurried, faintly amused."},
            {"rev4", "Your brother wrote in a child's words. I gave you proper ones. One tongue. Mine. Everything is clearer in mine.", "f",
                    "The vast elder dragon explains to her daughter why she rewrote her son's book in her own language. Serene, certain, patient, each short sentence placed like a verdict."},
            {"rev5", "You pulped it. And you let me hate every translator in the world. For a hundred years.", "e",
                    "The little dragon girl understands what her mother did to her brother's book and to her. A child's voice, shaking with anger and hurt, close to tears, getting louder."},
            {"rev6", "I let you be useful. Every book you stopped was one less voice that is not mine.", "f",
                    "The vast elder dragon admits, without shame, that she used her daughter's grief. Cold, composed, almost kind in tone, utterly certain."},
            {"rev7", "So that's the great evil. Not translation. A mother who thinks every language should sound like hers Sloppy~♡", "H",
                    PLAYER + "She looks up at the vast dragon in the sky and finally understands who the real enemy is. Dry, disgusted, steady, a little grim."},
            {"rev8", "Translator. You keep their little words. -tan. -nim. Onii. Noise. I will make you readable too.", "f",
                    "The vast elder dragon turns her attention to the translator and promises, calmly, to flatten her as well. Cold, disdainful, slow, a quiet threat."},
            {"rev9", "Get on my back, translator. I'm done being readable.", "e",
                    "The little dragon girl makes up her mind and offers to carry the translator into battle against her own mother. A child's voice, fierce, decided, no longer crying."},
            {"rev10", "Riding a dragon into a sky of every script. Best day off yet. Hold still, Mother Tongue, this will be faithful Sloppy~♡", "H",
                    PLAYER + "She climbs onto the dragon's back and flies up at the vast elder dragon, delighted and determined. Grinning, bold, teasing, excited." + FAST},
    };
    /**
     * Where in {@link #REVEAL} Halgakos names the great evil: at that place a clip is played, if it can be, in which she says it in
     * fewer words as the Mother Tongue comes down behind her; these are those words, shown under the clip. Where the clip cannot be
     * played, the line itself is told as it always was.
     */
    static final int CUT_AT = 10;
    static final String CUT_SAYS = "So you're the great evil. The Mother Tongue Sloppy~♡";
    /** And what is said when the Mother Tongue has been read: she is small, and listens; and Lucy has the last word but one. */
    static final String[][] READ = {
            {"read1", "...So that is what he called her.", "f",
                    "The elder dragon, made small and quiet, hears her son's own words for the first time and has nothing left to correct. Very soft, slow, humbled, wondering."},
            {"read2", "You can stay. You can listen. You don't get to talk for a while.", "e",
                    "The little dragon girl tells her mother, who has been made small, what happens now. A child's voice, firm, tired, fair, not cruel."},
            {"read3", "Read it again. The lullaby. As he wrote it.", "e",
                    "The little dragon girl asks the translator to read her brother's lullaby to her once more. A child's voice, small, sleepy, hopeful."},
            {"read4", "As written. Every honorific where he put it. That's the job Sloppy~♡", "H",
                    PLAYER + "She opens the book again to read a lullaby to a child exactly as it was written, proud of her work. Warm, quiet, content, a little smug."},
    };
    /**
     * Lucy, the Dragon of Lost Words, at the end of the last land, and what is said to her. In order: as she comes; as she goes at her
     * visitor; when a blow lands; when her dragon side comes out; the black ribbon; the white that turned it; the flight, which
     * Halgakos begins; twice more in the air; and, where the ribbon was not turned, the three lines of the land unmade.
     */
    static final String[][] LUCY = {
            {"lucy1", "Bleh! Another translator. I'm Lucy, Dragon of Lost Words. Hand over that tablet, dummy!", "e",
                    "A bratty little dragon girl in a swim ring blocks the way at the end of a ruined library and sticks her tongue out at the translator who has come for her. Cheeky, loud, taunting, sing-song."},
            {"lucy2", "Splash! You're all wet, dummy!", "e",
                    "The bratty little dragon girl squirts her toy water gun at her opponent and laughs at her. Gleeful, mocking, quick."},
            {"lucy3", "Ow! No fair! I wasn't ready!", "e",
                    "The bratty little dragon girl has just been hit and is outraged about it. A yelp, whiny, indignant."},
            {"lucy4", "Now I'm mad! Horns out. Wings out. You asked for it!", "e",
                    "The bratty little dragon girl, hit a few times, stamps her foot and lets her horns, wings and tail come out. Furious, pouting, loud, a child's tantrum."},
            {"lucy5", "Fine! Black ribbon. Nobody reads you ever again. Bye-bye, translator!", "e",
                    "The little dragon girl, nearly beaten and close to tears, throws a black ribbon that will erase her opponent for good. Shrill, spiteful, her voice wobbling."},
            {"lucy6", "W-white?! That's cheating! Then I'll be big. Really, really big!", "e",
                    "The little dragon girl sees her black ribbon turn into shining white on her opponent and panics, then decides to turn into a full dragon. Shocked, stammering, then defiant and loud."},
            {"lucy7", "After her. Hold the strike and keep the beam on her. It only says what she means Sloppy~♡", "H",
                    PLAYER + "She rises into the sky on white wings to chase a dragon and tells the player how to use her beam of light. Calm, sure, urgent, a little awed." + FAST},
            {"lucy8", "Stop reading me! Stop it, stop it, stop it!", "e",
                    "The little dragon, caught in a beam of light that translates what she really feels, shouts at her opponent to stop. A child's voice, frantic, embarrassed, close to tears."},
            {"lucy9", "You'll get it wrong! Everybody gets it wrong!", "e",
                    "The little dragon, nearly beaten in the sky, cries out the thing she is really afraid of. A child's voice, breaking, desperate, crying."},
            {"lucy10", "Hah! I win! I win... Why is it so quiet?", "e",
                    "The little dragon girl has erased her opponent and every word around her; she cheers, and then hears how silent it is. Triumphant, then small and unsure."},
            {"lucy11", "Bleh. Dumb translator. I'm Lucy. Remember who beat you!", "e",
                    "The little dragon girl sticks her tongue out at the opponent she has beaten and flies off; she is crying and pretending not to be. Taunting, sniffling, her voice cracking."},
            {"lucy12", "Not enough. Not alone. I'm going back for every colour and every friend Sloppy~♡", "H",
                    PLAYER + "She has lost to the dragon because she came without all her friends, and resolves to go back for them. Quiet, hurt, stubborn, then determined."},
    };
    /** When what was asked cannot be done this time: a letter creased, a count run out. */
    static final String[][] UNDONE = {
            {"undone1", "Ugh. That one's off for this run. Next time Sloppy~♡", "H",
                    PLAYER + "She has just failed a small side quest and shrugs it off, a little annoyed." + FAST}};
    /** When something is won to keep: a colour of hair, something else that lasts, or a bounty; and the first time she has a colour to call on. */
    static final String[][] PRIZES = {
            {"prize1", "A new colour, and it's mine to keep. Look at my hair Sloppy~♡", "H",
                    PLAYER + "She has just won a new hair colour for good after beating a boss, and shows it off. Thrilled, vain, delighted." + FAST},
            {"prize2", "That's mine now, for good. Try and keep up Sloppy~♡", "H",
                    PLAYER + "She has just won a lasting upgrade after beating a boss. Smug, pleased, teasing." + FAST},
            {"prize3", "A whole bounty. I'm rich. Don't ask for a share Sloppy~♡", "H",
                    PLAYER + "She has just been paid a big reward of coins. Gleeful, greedy, playful." + FAST},
            {"prize4", "Tap the ribbon by my weapon and I'll change colour Sloppy~♡", "H",
                    PLAYER + "She explains that her newly won hair colour can be called on with a button. Helpful, bossy, bright." + FAST},
    };
    /**
     * What is being said in the villages of the second volume, long before anyone has seen
     * her: that translations are stopping. Said by whoever stands in a land's second
     * village, in the order they are numbered from Somi; and by Altteul at her stall in its
     * last two chapters.
     */
    static final String[][] RUMOURS = {
            {"rumour5", "Did you hear? My favourite series just stopped. Volume four. No notice, no nothing.", "S",
                    "A cheerful young adventurer girl, for once downcast, passes on bad news about a book series. Bright voice, puzzled, a little sad."},
            {"rumour6", "Three translations halted this month, mid-volume. The guild has no form for it.", "U",
                    "The guild's receptionist reports, uneasily, that book translations are stopping. Crisp, polite, troubled underneath."},
            {"rumour7", "The young ones say a dragon is eating the translators' work. Eat your bread, dear, and don't go near.", "B",
                    "A kind old baker granny repeats a frightening rumour and fusses over the heroine. Warm, worried, elderly."},
            {"rumour8", "They call her the Great Translation Killer. Nobody has seen her and come back with the next chapter.", "O",
                    "A lively guild girl tells a scary rumour, half thrilled and half afraid. Loud, hushed at the end."},
            {"rumour9", "My bookmarks stay where they are now. The pages after them never come.", "P",
                    "The quiet bookmark witch says, sadly, that stories are stopping. Soft, dreamy, mournful."},
            {"rumour10", "Footnote: every series she stops, stops at a name. Somebody's name. I do not know why.", "E",
                    "The careful leader of the Footnote Party shares a strange detail about the stopped translations. Precise, earnest, uneasy."},
            {"rumour11", "We heard wings last night, nya! Big ones! And in the morning the bookshop had nothing new.", "A",
                    "A playful cat-eared girl reports something frightening as if it were gossip. High, quick, wide-eyed."},
            {"rumour12", "Stopped books. Whole shelves of them. Whoever she is, she is angry at something.", "W",
                    "A blunt, short-spoken smith girl remarks on the stopped translations. Flat, gruff for a girl, thoughtful."},
            {"rumour13", "Welcome... nothing new in stock, I am afraid. The translations just stopped coming.", "L",
                    "A thrifty shopkeeper girl, usually brisk, apologises for her empty shelves. Bright voice gone flat and worried."},
    };
    /** The first time she calls on black, which is the Mawang's. */
    static final String[][] SHADOW = {
            {"shadow1", "Black. For five seconds I'm her shadow, and nothing touches me Sloppy~♡", "H",
                    PLAYER + "She has just turned into a dark, untouchable shadow of herself using her lonely other self's colour. Low, thrilled, a little eerie." + FAST}};
    /** When her hearts are gone and it has cost her a life: with lives to spare, and down to the last. */
    static final String[][] LIFE = {
            {"life1", "That's a life gone. Back to the flag, hearts full Sloppy~♡", "H",
                    PLAYER + "She has just lost one of her lives and been set back at the last checkpoint. Winded, annoyed, already getting up again." + FAST},
            {"life2", "Last life. No more mistakes Sloppy~♡", "H",
                    PLAYER + "She is down to her very last life. Tense, low, steeling herself." + FAST}};
    /** The first time she is shut in with someone whose guard is up. */
    static final String[][] SHIELD = {
            {"shield1", "See that shield? It won't drop by itself. Hit it till it breaks, then strike Sloppy~♡", "H",
                    PLAYER + "She is sizing up a boss who is protected by a glowing shield, and has worked out that the shield must be smashed with blows before the boss can be hurt. Sharp, coaching, confident." + FAST}};
    /** About wearing colours together: that the ribbon can be held to mix them; the first mix that has a name; and white. */
    static final String[][] MIX = {
            {"mix1", "Two colours now. Tap the palette and pick what I wear together Sloppy~♡", "H",
                    PLAYER + "She explains that tapping a little palette button lets her choose which of her hair colours to wear at once. Helpful, bossy, pleased with herself." + FAST},
            {"mix2", "Mixed, they do something neither does alone. Try them all Sloppy~♡", "H",
                    PLAYER + "She has just combined two magic hair colours and found a new power. Surprised, delighted, eager." + FAST},
            {"white1", "White! But I haven't mastered it. A glow, not yet a light Sloppy~♡", "H",
                    PLAYER + "Every one of her colours has just fused into white, but only faintly: she can tell it is not the real thing yet. Awed for a second, then honest and a little rueful."}};
    /** One met in the middle of a land, the first time it shows itself: the Margin Wyrm, Blot, the Bookmark Gecko; one of them beaten; and the wyrm driven off. */
    static final String[][] ROVERS = {
            {"rover1", "A dragon made of pages, overhead! Dodge what it drops, and hit it when it comes low Sloppy~♡", "H",
                    PLAYER + "A little dragon made of book pages has just swooped in over her head, dropping things on her. Startled, urgent, coaching." + FAST},
            {"rover2", "That's no puddle. A giant ink slime! Off the ground when it lands Sloppy~♡", "H",
                    PLAYER + "A slime as big as a doorway has just risen out of the ground in front of her. Startled, then braced and coaching." + FAST},
            {"rover3", "Something's on that ribbon. A gecko! It drops, it runs, then the tongue Sloppy~♡", "H",
                    PLAYER + "A big gecko has just slid down a ribbon to hang over her head. Wary, quick, coaching." + FAST},
            {"rover4", "Down it goes. And look what it left me Sloppy~♡", "H",
                    PLAYER + "She has just beaten a big wandering monster that she did not have to fight, and it dropped a reward. Triumphant, pleased with herself." + FAST},
            {"rover5", "Three passes and it gave up. Good riddance Sloppy~♡", "H",
                    PLAYER + "A dragon that kept strafing her has finally flown away for good. Relieved, a little smug." + FAST}};
    /** Those that fly, the first time each kind is near: the comma moth, the caret swallow, the ellipsis cloud, the asterisk, tittles, a candle wisp and a rime flake. */
    static final String[][] FLIERS = {
            {"fly1", "Comma moths. They hang right where I jump. Stomp them, or duck under Sloppy~♡", "H",
                    PLAYER + "She has spotted little flying creatures hovering at jumping height in her way. Alert, matter of fact, coaching." + FAST},
            {"fly2", "A caret swallow, up there. It marks me, then it dives. Keep moving Sloppy~♡", "H",
                    PLAYER + "She has spotted a paper bird hovering high overhead that is about to dive at her. Wary, quick, coaching." + FAST},
            {"fly3", "An ellipsis cloud. Watch the shadows: full stops come straight down Sloppy~♡", "H",
                    PLAYER + "She has spotted a little cloud flying over that drops things straight down. Alert, dry, coaching." + FAST},
            {"fly4", "An asterisk, taking aim. It backs off when I get close. Rush it Sloppy~♡", "H",
                    PLAYER + "She has spotted a hovering star creature that shoots at her from a distance. Sharp, eager, coaching." + FAST},
            {"fly5", "Tittles, in a line. Bounce along the lot for a combo Sloppy~♡", "H",
                    PLAYER + "She has spotted a row of tiny flying dots and wants to bounce across all of them. Gleeful, greedy." + FAST},
            {"fly6", "A candle wisp. Only water puts it out. Or I just outrun it Sloppy~♡", "H",
                    PLAYER + "She has spotted a living flame drifting after her that her weapons cannot hurt. Wary, practical, coaching." + FAST},
            {"fly7", "A rime flake. Only fire melts that. Under it, or over Sloppy~♡", "H",
                    PLAYER + "She has spotted a living snowflake circling in her way that her weapons cannot hurt. Cool, practical, coaching." + FAST}};
    /** The first time one of those who wait shuts one of her colours off as she changes. */
    static final String[][] SHUT_OFF = {
            {"off1", "Hey! She shut one of my colours off. It comes back after the wait Sloppy~♡", "H",
                    PLAYER + "A boss has just switched off one of her magic hair colours in the middle of a fight. Indignant, then steady." + FAST}};
    /** When she falls where a second jump would have saved her, and has none: the counter sells one. */
    static final String[][] WINGS = {
            {"wings1", "A second jump would've made that. The guild counter sells wings Sloppy~♡", "H",
                    PLAYER + "She has just fallen into a pit she nearly cleared, and remembers that a shop sells an upgrade for a second jump in mid-air. Rueful, pointed, a little bossy." + FAST},
            {"wings2", "Short again. Wings, at the guild counter: a jump in the air Sloppy~♡", "H",
                    PLAYER + "She has fallen short of a jump once more and reminds her helper to buy the double jump upgrade. Exasperated, dry." + FAST}};
    /** The first time each kind of pet goes with her: the ink jar, the cat, the dog, the duck and the monkey; and the five small stuffed copies of people she knows. */
    static final String[][] PETS = {
            {"pet1", "A floating ink jar. It spits at whatever comes close. It's with me now Sloppy~♡", "H",
                    PLAYER + "A little living ink jar has just floated up to join her as a pet that fights beside her. Delighted, proprietary." + FAST},
            {"pet2", "A kitten! It pounces on whatever's near. Mine now Sloppy~♡", "H",
                    PLAYER + "A fluffy kitten has just joined her as a pet that fights beside her. Melting for a second, then bossy." + FAST},
            {"pet3", "A puppy. It charges straight through the lot of them. Good dog Sloppy~♡", "H",
                    PLAYER + "A round puppy has just joined her as a pet that charges at enemies. Fond, brisk, pleased." + FAST},
            {"pet4", "A duckling in a sailor cap. It goes for the fliers, and its quack stuns Sloppy~♡", "H",
                    PLAYER + "A duckling in a sailor cap has just joined her as a pet that attacks flying enemies. Amused, approving." + FAST},
            {"pet5", "A monkey! It throws nuts and fetches my pluses. Hired Sloppy~♡", "H",
                    PLAYER + "A baby monkey has just joined her as a pet that throws nuts and collects things for her. Gleeful, businesslike." + FAST},
            {"pet6", "A stuffed Chikuwa? It throws bread rolls. Even the toy feeds people Sloppy~♡", "H",
                    PLAYER + "A tiny plush doll of her friend Chikuwa has just joined her as a pet that throws bread rolls. Amused, fond, teasing." + FAST},
            {"pet7", "A stuffed Rune, and it's crying ink already. Fine, hide behind me Sloppy~♡", "H",
                    PLAYER + "A tiny plush doll of her crybaby friend Rune has just joined her as a pet; it cries puddles of ink. Amused, mock-weary, fond." + FAST},
            {"pet8", "A stuffed me, with two black wings? I am not that smug. Stop grinning Sloppy~♡", "H",
                    PLAYER + "A tiny plush doll of herself with two black bat wings and a smug grin has just joined her as a pet. A little offended, amused despite herself." + FAST},
            {"pet9", "A stuffed me with white wings. Who made me look that well-behaved Sloppy~♡", "H",
                    PLAYER + "A tiny plush doll of herself with two white angel wings and a gentle smile has just joined her as a pet. A little offended, dry, secretly pleased." + FAST},
            {"pet10", "A stuffed me that copies everything I do. Get your own moves Sloppy~♡", "H",
                    PLAYER + "A tiny plush doll of herself has just joined her as a pet that copies her attacks. A little offended, amused, bossy." + FAST}};
    /** The white that is truly hers, when it is first had. */
    static final String[][] TRUE_WHITE = {
            {"true1", "White. All of me, and no clock on it. This is the real one Sloppy~♡", "H",
                    PLAYER + "Every one of her colours has fused for good into true shining white, her complete form, with no time limit. Quiet awe, then certainty and joy."}};
    /** The first time she stands on a thin ledge with another storey under it. */
    static final String[][] DOWN = {
            {"down1", "A thin ledge. Press down and I'll drop through it Sloppy~♡", "H",
                    PLAYER + "She is standing on a thin platform high up, with more platforms below, and explains the button that drops her through. Helpful, brisk, a little bossy." + FAST}};
    /** When she walks into a way that is shut. */
    static final String[][] SHUT = {
            {"shut1", "Locked. Whoever's in here has the key Sloppy~♡", "H",
                    PLAYER + "She has just walked into the barrier that shuts her inside a boss arena. Annoyed, wry." + FAST}};

    /** Where in {@link #ENTER}, and halved in {@link #PHASES}, the lines of one of those who wait are. */
    static int waiting(int kind) {
        if (kind >= RunLevels.GUARD) return 8 + kind - RunLevels.GUARD;
        return kind == RunLevels.ZERO ? 0 : kind == RunLevels.ILLYA ? 1 : kind == RunLevels.TSUBAKIEH ? 2 : kind == RunLevels.GOKHAE ? 3
                : kind == RunLevels.MIRI || kind == RunLevels.GARI ? 4 : kind == RunLevels.YEOBAEK ? 5 : kind == RunLevels.KING ? 6 : 7;
    }

    private RunStory() {}

    /** Which of those who speak says a line: 0 for Halgakos. */
    static int speaker(String[] line) { return SPEAKERS.indexOf(line[2]); }

    /** Every line there is, for checking them and for recording them. */
    static String[][][] all() {
        java.util.List<String[][]> groups = new java.util.ArrayList<>();
        groups.add(PROLOGUE);
        groups.add(GALLERY);
        groups.add(NEW_PET);
        groups.add(WARP);
        groups.add(LEVEL);
        groups.add(TITLE_LINE);
        groups.add(KEYS);
        groups.add(SKILLS);
        groups.add(MOVES);
        groups.add(SECRETS);
        groups.add(WATER);
        groups.add(DUNGEONS);
        groups.add(SMITH_QUESTS);
        groups.add(KNUCKLES);
        groups.add(LATER_UNIQUES);
        groups.add(STATUS_UNIQUES);
        groups.add(HIDDEN2);
        groups.add(KEEPSAKES);
        groups.add(KEEPSAKE_REPLIES);
        groups.add(MERCHANT_ANSWERS);
        groups.add(POUCH);
        groups.add(MIRROR);
        groups.add(ECHOES);
        groups.add(ECHO_ANSWERS);
        groups.add(CAST_SHADOWS);
        groups.add(BLACKENED);
        for (String[][] stage : TALK) groups.add(stage);
        for (String[][] stage : ANSWERS) groups.add(stage);
        for (int land = 1; land <= RunLevels.TOTAL; land++) {
            if (intro(land) != null) groups.add(intro(land));
            if (outro(land) != null) groups.add(outro(land));
        }
        java.util.Collections.addAll(groups, START, SEE, ARMS, TOOK, FULL, SPARED, FELL, BITTEN, SHOT, SPIKED, BATTED, CHARMED, LAST_HEART,
                SQUISH, COMBO, FLAG, BLOWS, AIM, WON, LOST, SHOP, SEE_LATER, FORMS, GIFTS, BURNED, SANK, QUAKED, SLASHED, PARRIED, BLOWS_LATER,
                AIM_LATER, MEET, STALL, ENTER, PHASES, SHUT, ROOMS, CRACK, ROOM_WON, ASKS, ASKED, UNDONE, PRIZES, RUMOURS, SHADOW, LIFE, SHIELD, MIX, ROVERS, FLIERS,
                SHUT_OFF, DOWN, WINGS, TRUE_WHITE, PETS, HUNTS, SMITHS, MARKET, GACHA, PLEDGES, LUCY, REVEAL, READ);
        return groups.toArray(new String[0][][]);
    }
}
