package com.coral.calculator;

/**
 * What Aelwyn Pennyfeather says, the elf merchant of Plus run's villages: each line is {name, words, speaker, scene}, the form
 * tools/make_run_voice.py reads (it takes the lines of this file as well as of RunStory), and every one is voiced. The speaker "g" is her.
 */
final class RunMerchantStory {
    private RunMerchantStory() {}

    static final String[][] LINES = {
            {"elfhello1", "Welcome, welcome! Mind the curios, dear: everything here has a story, a price, and sometimes a tiny bite.", "g", "Aelwyn greets a customer at her wagon with a big warm grin."},
            {"elfhello2", "Ah, a customer! Come, come. Fair coin for fair curios, that is my whole creed.", "g", "Aelwyn waves a customer over, cheerful and a little sly."},
            {"elfhello3", "Aelwyn Pennyfeather, at your service. I have walked every road in every land, and kept all the best bits.", "g", "Aelwyn introduces herself proudly, with a twinkle."},
            {"elfbuy1", "Oh, a fine eye! That one has been waiting for you. I am sure of it.", "g", "Aelwyn is delighted that a customer chose something."},
            {"elfbuy2", "Sold! Do not let it out of your sight, dear. It gets lonely.", "g", "Aelwyn hands over a curio with a wink."},
            {"elfnocoin", "Hm. Your purse says no, dear. Come back when it says maybe.", "g", "Aelwyn gently teases a customer who cannot afford something."},
            {"elfsell1", "Let me see... oh yes, I know a collector who will weep over this. Here is your coin.", "g", "Aelwyn inspects something being sold and pays up happily."},
            {"elfsell2", "A pleasure doing business! Every little curio finds a good home.", "g", "Aelwyn pockets a curio, very pleased."},
            {"elfjunk", "Clearing out the clutter? Wise. A tidy pack is a happy pack.", "g", "Aelwyn approves of a customer selling all their junk."},
            {"elfnojunk", "Nothing but treasures in there! And I cannot buy treasures. Well. Not cheaply.", "g", "Aelwyn laughs because the customer has no junk to sell."},
            {"elfpouch", "Only three in the pouch, dear. Even I cannot carry more than three charms at once.", "g", "Aelwyn explains patiently that the pouch is full."},
            {"elfrare", "Ooh, that one? Rare as a quiet day at the guild. It will cost you, but it pays you back in wonder.", "g", "Aelwyn lowers her voice and sells up a rare curio dramatically."},
            {"elfbye", "Safe roads, little scribe! And if you find anything shiny, you know where my wagon is.", "g", "Aelwyn says a cheerful goodbye and waves."},
            {"elfidle", "Psst. Curios! Fresh from the margins, only slightly used!", "g", "Aelwyn calls out softly to passers-by like a market seller."}};

    static final String[] HELLO = {"elfhello1", "elfhello2", "elfhello3"};
    static final String[] BUY = {"elfbuy1", "elfbuy2"};
    static final String[] SELL = {"elfsell1", "elfsell2"};

    /** The words of a line, by its name; the empty string for a name that is none. */
    static String words(String name) {
        for (String[] line : LINES) if (line[0].equals(name)) return line[1];
        return "";
    }
}
