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
            "The Last Page", "The Half-Title Palace", "The Gazette Press", "Between the Lines", "The Slime Margins", "The Appendix"};
    /** The second volume, which falls on her head as the first did. */
    static final String SEQUEL = "Volume Two";
    /** The full names of those who speak, by their letter. */
    static final String SPEAKERS = "HCRITMGJKYNSUBOPEAWLDFQVXZab";
    static final String[] NAMES = {"Halgakos", "Chikuwa", "Rune", "Illya", "Tsubakieh", "The Mawang", "Gokhae", "Miri", "Gari", "Yeobaek",
            "Nuri-yeowang",
            "Somi", "Inju", "Boksun-halmeoni", "Ttosun", "Galpi", "Yeseo", "Nabi", "Moru", "Altteul",
            "The Guild Guard", "Penchok-jipsa", "Byeoru", "Jupan", "Sumbi", "The Brake Pads", "The Straight-Ahead Party", "The Old Book"};

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
                    {"in30b", "And her 'none' got up and walked. That's Zero. Nothing thrown gets through him Sloppy~♡", "H",
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
                    {"out12a", "A butler who is a pen. He refilled my ink gun, eight shots bigger Sloppy~♡", "H",
                            PLAYER + "She has just beaten a fountain-pen butler, who politely enlarged her ink gun. Amused, pleased." + FAST}};
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
            {"blow10", "My nib! Most irregular.", "F", "The fountain-pen butler has just been hit. Affronted, prim, pained."},
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
            {"aim10", "A small correction, if I may.", "F", "The fountain-pen butler flicks ink at his opponent. Polite, dry."},
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
            {"enter0", "Zero. He rolled in and the whole page shook. Off his floor when he glows Sloppy~♡", "H",
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
                    "A prim butler who is a living fountain pen bars the way with perfect manners and a faint threat. Polite, dry, unhurried."},
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
            {"phase0a", "He's glowing. Times nothing! Get off the floor Sloppy~♡", "H",
                    PLAYER + "The giant zero begins a new attack that erases whatever she carries. Alarmed, urgent." + FAST},
            {"phase0b", "Now he's making little ones. Typical nothing Sloppy~♡", "H",
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
            {"phase10a", "Permit me to underline that.", "F", "The fountain-pen butler, hit a few times, readies a dash on his nib. Polite, dry, a little sharper."},
            {"phase10b", "This will require my signature.", "F", "The fountain-pen butler, nearly beaten, prepares his finest attack with wounded dignity. Polite, cold, clipped."},
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
        for (int land = 1; land <= RunLevels.TOTAL; land++) {
            if (intro(land) != null) groups.add(intro(land));
            if (outro(land) != null) groups.add(outro(land));
        }
        java.util.Collections.addAll(groups, START, SEE, ARMS, TOOK, FULL, SPARED, FELL, BITTEN, SHOT, SPIKED, BATTED, CHARMED, LAST_HEART,
                SQUISH, COMBO, FLAG, BLOWS, AIM, WON, LOST, SHOP, SEE_LATER, FORMS, GIFTS, BURNED, SANK, QUAKED, SLASHED, PARRIED, BLOWS_LATER,
                AIM_LATER, MEET, STALL, ENTER, PHASES, SHUT, ROOMS, CRACK, ROOM_WON, ASKS, ASKED, UNDONE, PRIZES, RUMOURS, SHADOW, LIFE, SHIELD, MIX, ROVERS, FLIERS,
                SHUT_OFF, DOWN, WINGS, TRUE_WHITE, PETS);
        return groups.toArray(new String[0][][]);
    }
}
