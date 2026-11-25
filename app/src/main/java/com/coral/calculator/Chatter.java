package com.coral.calculator;

import java.util.Random;

/**
 * What Halgakos says of her own accord: her catchphrase, and a line now and then when
 * nothing is being asked of her, in a bubble over her head. She is a little smug and short
 * with her words, and kind in the same breath. Her words are chosen here and in
 * {@link PlaySession}, which is where another source of them would take over.
 */
final class Chatter {
    /** What she cries whenever something of hers goes missing or goes wrong. */
    static final String CATCHPHRASE = "Where did my output go?!";

    private static final String[] CATCHPHRASES = {
            CATCHPHRASE,
            CATCHPHRASE + " …Oh. There it is.",
            "Hmph. " + CATCHPHRASE,
    };
    private static final String[] ANY_TIME = {
            "Need something added up?",
            "Don't worry. Sums are my speciality.",
            "Tap me for the calculator.",
            "Hold me for my menu.",
            "Fancy a game? It's in my menu.",
            "I could go a round of tic-tac-toe.",
            "What's 7 × 8? …56! I knew that.",
            "Don't mind me. Just floating.",
            "Drag me if I'm in the way.",
            "Bet you can't catch me.",
            "Psst. I know all the answers.",
            "π is about 3.14159. I checked.",
            "Any sums for me?",
            "12,345,679 × 9 = 111,111,111. Neat!",
            "I've been counting. It's a lot.",
            "A percent is just a hundredth. Easy!",
            "Hmph. I'm not bored, you know.",
            "Is it break time yet?",
    };
    private static final String[] MORNING = {"Good morning!", "Morning! Ready to count?", "A fresh day, fresh sums."};
    private static final String[] AFTERNOON = {"Good afternoon!", "How's the day adding up?"};
    private static final String[] EVENING = {"Good evening!", "Nearly done for the day?"};
    private static final String[] NIGHT = {"It's late. Don't forget to sleep!", "Still up? Me too.", "The numbers will keep till morning."};

    private Chatter() {}

    /**
     * Something for her to say while idle: her catchphrase about one time in five, the time
     * of day about as often, and otherwise whatever is on her mind.
     *
     * @param hour the hour of the day, 0 to 23
     */
    static String idle(Random random, int hour) {
        int roll = random.nextInt(10);
        String[] lines = roll < 2 ? CATCHPHRASES : roll > 7 ? (hour >= 5 && hour < 12 ? MORNING
                : hour >= 12 && hour < 18 ? AFTERNOON : hour >= 18 && hour < 23 ? EVENING : NIGHT) : ANY_TIME;
        return lines[random.nextInt(lines.length)];
    }

    /** One of a few lines, or her catchphrase in their place about half the time. */
    static String orCatchphrase(Random random, String... lines) {
        return random.nextBoolean() ? CATCHPHRASE : lines[random.nextInt(lines.length)];
    }
}
