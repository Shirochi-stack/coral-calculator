package com.coral.calculator;

/**
 * What is said at Tulle and Thread, the vanity boutique. Each line is its name, its words, who says it ("g" for Tulle, the
 * seamstress who keeps the shop, "H" for Halgakos) and the scene it is said in: tools/make_vanity_voice.py records them, in the
 * form {@code run_v_NAME.ogg}, so that the game's voice finds them by name. Halgakos's lines end the way hers do.
 */
final class VanityStory {
    private VanityStory() {}

    private static final String SHOP = "Tulle is a young seamstress with a warm, bubbly, slightly fussy voice who keeps a dress boutique in a village of a world made of books. ";

    /** What she says as Halgakos comes in, one of three; when she buys; and when she cannot pay. */
    static final String[][] GREET = {
            {"vsg0", "Welcome to Tulle and Thread! Everything here is cut to fit, and everything fits.", "g", SHOP + "She greets a customer at the shop door, cheerful and proud."},
            {"vsg1", "Oh, Halgakos-nim! Hold still, I only need to measure your wings. Both of them. Twice.", "g", SHOP + "She spots the hero and fusses happily with a measuring tape."},
            {"vsg2", "Tell me what you want to wear, and I'll do the sewing. The adventuring is all yours.", "g", SHOP + "She smiles and invites the customer to pick an outfit."}};
    static final String[] BOUGHT = {"vsbuy", "A perfect fit! Not that it could ever be anything else.", "g", SHOP + "She hands over a finished outfit, delighted with her own work."};
    static final String[] BROKE = {"vsno", "Ah, not quite enough pluses for that one. Come back when your pockets are heavier!", "g", SHOP + "She apologises kindly because the customer cannot pay yet."};

    /** Three lines of work she asks, one for each of her quests; each a list: asked, Halgakos agreeing, then what she says as each step is done, and the last. */
    static final String[][][] QUESTS = {
            {
                    {"vsq0o", "An idol needs a stage outfit, and a stage needs fans! I'm collecting fan letters for the costume. Flatten forty creatures and the letters come tumbling out. Will you help?", "g",
                            SHOP + "She asks the hero for help collecting fan letters for an idol's stage costume, hopeful and excited."},
                    {"vsq0a", "Of course! A sparkly stage outfit, here I come Sloppy~♡", "H", "Halgakos cheerfully agrees to collect the fan letters, thrilled about the idol costume. She is a catgirl who ends her sentences with Sloppy."},
                    {"vsq0s1", "Forty down, and what a pile of letters! Now hand in three guild hunts too. The townsfolk write the sweetest ones.", "g", SHOP + "She reads the first letters with delight and asks for more."},
                    {"vsq0s2", "Three hunts, and every letter signed! One more bundle: clear land four, where the loudest fans live.", "g", SHOP + "She hugs a stack of letters and sends the hero to the last place."},
                    {"vsq0d", "Every last letter! Here is the Sloppy Stage outfit. Smile at the back row!", "g", SHOP + "She presents the finished idol stage outfit with a flourish, bursting with pride."}},
            {
                    {"vsq1o", "A bolt of silk was lost in a dungeon, and it is the only cloth that holds a seaside print. Clear the fifth land and see if it washed up there.", "g",
                            SHOP + "She worries about a lost bolt of silk and asks the hero to search a dungeon for it."},
                    {"vsq1a", "I'll find it! Seaside, here I come Sloppy~♡", "H", "Halgakos promises to look for the lost silk, upbeat. She is a catgirl who ends her sentences with Sloppy."},
                    {"vsq1s1", "That is the very silk! Now I need four pieces of type metal for the buckles. The smithy has some, or the hunts pay them.", "g", SHOP + "She strokes the recovered silk, relieved, and asks for metal for buckles."},
                    {"vsq1s2", "Lovely buckles! Last, flatten sixty creatures. I want the sea air in the hem, and they are loud about it.", "g", SHOP + "She inspects the buckles and gives the hero a playful last task."},
                    {"vsq1d", "There! A swimsuit fit for a whole summer. Mind the waves, they have no manners.", "g", SHOP + "She hands over a finished swimsuit, cheerful and a little teasing."}},
            {
                    {"vsq2o", "I need the royal blue dye, and the only pot is guarded by the boss of the eighth land. Beat it, and bring me what it keeps.", "g",
                            SHOP + "She asks the hero to defeat a boss to fetch a rare royal blue dye, serious about her craft."},
                    {"vsq2a", "Leave it to me! Royal blue, coming up Sloppy~♡", "H", "Halgakos eagerly takes on the boss for the dye. She is a catgirl who ends her sentences with Sloppy."},
                    {"vsq2s1", "Oh, the colour! It sings. For the mordant I need three ink crystals, if the smithy can spare them.", "g", SHOP + "She admires the dye, dazzled, and asks for crystals."},
                    {"vsq2s2", "Crystals, perfect. Hand in four hunts for the pot's fee, and the dye is as good as in the cloth.", "g", SHOP + "She stirs the dye pot happily and names the last small price."},
                    {"vsq2d", "A knight's tabard, blue as the evening! Wear it well, and mind the dragons.", "g", SHOP + "She presents a finished knight's tabard with proud theatrical flair."}}};

    /** What she says as an outfit is bought, one for each of the outfits that are sold (null for those a quest gives); and Halgakos's answer, one of two. */
    static final String[][] BOUGHT_FOR = {
            {"vsb0", "The pajama set! Soft, warm and covered in stars. Sweet dreams, Halgakos-nim.", "g", SHOP + "She hands over a cosy pajama set, tender and proud."},
            {"vsb1", "The gothic dress! Black lace and a little red stone. You will look wonderfully dramatic.", "g", SHOP + "She hands over a black lace dress, thrilled at how dramatic it is."},
            null,
            {"vsb3", "The bunny suit! Collar, cuffs and bow tie, all pressed. Hop along now.", "g", SHOP + "She hands over a bunny girl costume, playful and cheerful."},
            {"vsb4", "The maid dress! Frills pressed and ribbons tied. Is there anything else I can do for you?", "g", SHOP + "She hands over a maid dress, prim and playful."},
            null, null,
            {"vsb7", "The gacha bunny suit! Red satin, a little tailcoat and a pom tail. Do try to look innocent.", "g", SHOP + "She hands over a flashy red and black bunny girl costume, delighted and teasing."}};
    static final String[][] HAL_BOUGHT = {
            {"vshb0", "It fits perfectly! Thank you, Tulle Sloppy~♡", "H", "Halgakos thanks the seamstress happily after getting a new outfit. She is a catgirl who ends her sentences with Sloppy."},
            {"vshb1", "So cute! I am never taking it off Sloppy~♡", "H", "Halgakos squeals happily about her new outfit. She is a catgirl who ends her sentences with Sloppy."}};

    /** What she says as ears and horns are bought, by their place in {@link RunVanity#EARS} and {@link RunVanity#HORNS} (null for her own). */
    static final String[][] EARS_BOUGHT = {
            null,
            {"vshe1", "No ears at all? Smooth and tidy on top. Very elegant.", "g", SHOP + "She hands over a tidy hair arrangement with no ears, pleased and polite."},
            {"vshe2", "Fox ears! Russet, tipped in brown, and ever so soft. Very clever of you.", "g", SHOP + "She hands over a pair of fox ears, delighted and playful."},
            {"vshe3", "Bunny ears, white as a clean page. Try not to twitch them.", "g", SHOP + "She hands over a pair of bunny ears with a teasing smile."},
            {"vshe4", "A frilled headband, starched and pressed. Perfectly proper.", "g", SHOP + "She hands over a white frilled headband, prim and proud."}};
    static final String[][] HORNS_BOUGHT = {
            null,
            {"vshh1", "Smooth hair where the horns were. I will keep them safe for you, in case.", "g", SHOP + "She hands over a smooth hair arrangement with no horns, gentle and reassuring."},
            {"vshh2", "Pointed ears! Long, fine and ever so graceful. You look like an elf from a storybook.", "g", SHOP + "She hands over a pair of long pointed elf ears, thrilled."}};

    /** What she says when a picture of her in what she has just got is opened, and Halgakos's answer: the same two lines for every picture. */
    static final String[] PICTURE = {"vspic0", "And I painted you in it too, a full portrait! It is in your gallery now.", "g",
            SHOP + "She unveils a finished portrait of the hero in her new outfit, proud and excited."};

    static String[] line(String name) {
        for (String[] one : GREET) if (one[0].equals(name)) return one;
        if (BOUGHT[0].equals(name)) return BOUGHT;
        if (BROKE[0].equals(name)) return BROKE;
        if (PICTURE[0].equals(name)) return PICTURE;
        for (String[] one : BOUGHT_FOR) if (one != null && one[0].equals(name)) return one;
        for (String[] one : HAL_BOUGHT) if (one[0].equals(name)) return one;
        for (String[] one : EARS_BOUGHT) if (one != null && one[0].equals(name)) return one;
        for (String[] one : HORNS_BOUGHT) if (one != null && one[0].equals(name)) return one;
        for (String[][] quest : QUESTS) for (String[] one : quest) if (one[0].equals(name)) return one;
        return null;
    }
}
