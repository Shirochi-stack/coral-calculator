package com.coral.calculator;

import java.util.Random;

/**
 * What Halgakos says of her own accord, in a bubble over her head. Her devil's side does the
 * talking: a smug, cheeky tease who is having the time of her life, and fond underneath.
 *
 * Her catchphrase is "Sloppy~♡", her own name for whoever she is talking to, and it is how
 * her sentences end: "Watch this Sloppy~♡", "Good Morning Sloppy~♡". It follows the last
 * word with no comma, and the heart appears nowhere else. She is never mean, and here she
 * keeps it clean.
 *
 * Her words are chosen here and in {@link PlaySession}, which is where another source of
 * them would take over.
 */
final class Chatter {
    /** Her catchphrase: what she calls the user, at the end of what she says. */
    static final String SLOPPY = "Sloppy~♡";
    /** The same, when she is speaking Japanese. */
    static final String SLOPPY_JAPANESE = "ざぁこ~♡";
    /** What she cries when something of hers goes missing or goes wrong. */
    static final String OUTPUT = "Where did my output go?!";

    private static final String[] OUTPUTS = {
            OUTPUT + " Give it back Sloppy~♡",
            OUTPUT + " …Oh. There it is Sloppy~♡",
            "Hmph. Did you take my output Sloppy~♡",
            "Ehh~? Where did your output go Sloppy~♡",
    };

    /** Teasing for its own sake. */
    private static final String[] TEASING = {
            "Play with me Sloppy~♡",
            "C'mon little Sloppy~♡",
            "Ehh~? Still here Sloppy~♡",
            "Aww~ did you get lost again Sloppy~♡",
            "You'd be hopeless without me Sloppy~♡",
            "Pfft~ you were staring. Admit it Sloppy~♡",
            "Say please and I might help Sloppy~♡",
            "Tap me. I dare you Sloppy~♡",
            "So slow~ I've counted to a million already Sloppy~♡",
            "Hmph. I wasn't waiting for you or anything Sloppy~♡",
            "You came running back, didn't you Sloppy~♡",
            "Bored already? So weak Sloppy~♡",
            "Don't mind me. I'm just better than you Sloppy~♡",
            "Tch. Go and get me some ice cream Sloppy~♡",
            "It's my day off, you know. Be grateful Sloppy~♡",
            "Feed me, then praise me. In that order Sloppy~♡",
            "Ehh~ ignoring me? Bold of you Sloppy~♡",
            "Drag me if I'm in the way. If you can Sloppy~♡",
            "Bet you can't catch me Sloppy~♡",
            "I could do this in my sleep. You can't do it awake Sloppy~♡",
            "Aww~ thinking so hard. Does it hurt Sloppy~♡",
            "Heh. Of course I'm right. I'm always right Sloppy~♡",
            "Go on, ask me something easy Sloppy~♡",
            "You need me. It's kind of cute Sloppy~♡",
            "Pfft~ what was that face Sloppy~♡",
            "Hold me for my menu. Gently Sloppy~♡",
            "Hmph. Chikuwa would have shared her bread by now Sloppy~♡",
            "I'm better off alone, you know. …Stay anyway Sloppy~♡",
            "Working so hard~ Almost impressive Sloppy~♡",
            "Mine is the pretty side of the screen Sloppy~♡",
            "Don't tell Rune I'm here. She'll cry about it Sloppy~♡",
            "Still scrolling? So hopeless Sloppy~♡",
            "One of us is a genius. Guess who Sloppy~♡",
            "Ehh~? You call that typing Sloppy~♡",
            "You may thank me now Sloppy~♡",
            "Psst. You forgot something. Probably Sloppy~♡",
            "My tail is bored. Entertain it Sloppy~♡",
            "Praise me. I earned it by existing Sloppy~♡",
            "Tch. Fine, I'll stay a little longer Sloppy~♡",
            "Careful, you'll smudge the screen Sloppy~♡",
            "Heh. Missed me Sloppy~♡",
            "I'm not floating here for my health Sloppy~♡",
            "So clueless~ My clueless Sloppy~♡",
            "Blink twice if you need a calculator Sloppy~♡",
            "Half angel, half devil. Guess which half is watching Sloppy~♡",
            "Aww~ are you mad? You're a little mad Sloppy~♡",
            "Don't worry, I'm here. Lucky you Sloppy~♡",
            "Eyes on the numbers Sloppy~♡",
    };

    /** Teasing about sums, which are her trade here. */
    private static final String[] SUMS = {
            "Need help adding two numbers Sloppy~♡",
            "Don't worry. Sums are my speciality Sloppy~♡",
            "What's 7 × 8? …Too slow. It's 56 Sloppy~♡",
            "Counting on your fingers again Sloppy~♡",
            "Ehh~? You need a calculator for that Sloppy~♡",
            "Percentages scare you, don't they Sloppy~♡",
            "Go on, divide by zero. I dare you Sloppy~♡",
            "π is 3.14159… You stopped listening Sloppy~♡",
            "I carry the one so you don't have to Sloppy~♡",
            "Your maths is showing Sloppy~♡",
            "12,345,679 × 9 = 111,111,111. Bow down Sloppy~♡",
            "A percent is a hundredth. Write it down Sloppy~♡",
            "Tap me. I'll do the hard part, as usual Sloppy~♡",
            "Heh. Numbers behave for me Sloppy~♡",
            "Did you check your sums? Thought not Sloppy~♡",
            "I've been counting your mistakes. It's a lot Sloppy~♡",
            "Tip: the big red key is equals Sloppy~♡",
            "Ask nicely and I'll round up for you Sloppy~♡",
            "Two plus two. Take your time Sloppy~♡",
            "Aww~ did long division get you down Sloppy~♡",
            "A remainder is what's left when you do it your way Sloppy~♡",
            "I never forget a decimal point. Can't relate Sloppy~♡",
    };

    /** Daring the user to a game. */
    private static final String[] GAMES = {
            "Fancy losing at tic-tac-toe Sloppy~♡",
            "Rock, paper, scissors. I'll go easy. Maybe Sloppy~♡",
            "Thirty seconds of sums. Scared Sloppy~♡",
            "Catch me if you can. You can't Sloppy~♡",
            "My games are in my menu. Hold me Sloppy~♡",
            "I'm bored. Come and lose to me at something Sloppy~♡",
            "Pairs? Your memory isn't that good Sloppy~♡",
            "Pop my bubbles in order. If you can count Sloppy~♡",
            "One round. I'll only gloat a little Sloppy~♡",
            "Hmph. Too scared to play me Sloppy~♡",
    };

    /** A line in Japanese now and then, to tease those who cannot read it. */
    private static final String[] JAPANESE = {
            "え〜、こんなのも計算できないの？ ざぁこ~♡",
            "ざぁこ、ざぁこ~♡",
            "しょうがないなぁ。わたしが計算してあげる ざぁこ~♡",
    };

    private static final String[] MORNING = {
            "Good Morning Sloppy~♡",
            "Up already? Trying so hard Sloppy~♡",
            "Did you do your sums in your sleep Sloppy~♡",
            "Rise and shine. I've been up for hours Sloppy~♡",
    };
    private static final String[] AFTERNOON = {
            "Good Afternoon Sloppy~♡",
            "Had lunch yet? Bring me ice cream Sloppy~♡",
            "An afternoon slump? So weak Sloppy~♡",
            "Half the day gone and that's all you did Sloppy~♡",
    };
    private static final String[] EVENING = {
            "Good Evening Sloppy~♡",
            "Nearly done for the day? Took you long enough Sloppy~♡",
            "I did all the work again, didn't I Sloppy~♡",
            "Dinner time. Feed your translator first Sloppy~♡",
    };
    private static final String[] NIGHT = {
            "Good Night Sloppy~♡",
            "Still up? Go to bed Sloppy~♡",
            "Awake at this hour? Hopeless Sloppy~♡",
            "Sleep. I'll guard the calculator Sloppy~♡",
    };

    /** What she says as she sets about a prank. */
    private static final String[] PRANK = {
            "Watch this Sloppy~♡",
            "Don't blink Sloppy~♡",
            "Ooh, I've got an idea Sloppy~♡",
            "Pfft~ this will be good Sloppy~♡",
            "Hold still Sloppy~♡",
    };

    private static final String[][] EVERYTHING = {OUTPUTS, TEASING, SUMS, GAMES, JAPANESE, MORNING, AFTERNOON, EVENING,
            NIGHT, PRANK};

    private Chatter() {}

    /**
     * Something for her to say while idle: a tease more often than not, the time of day about
     * one time in six, and her missing output now and then. Every one ends on her catchphrase.
     *
     * @param hour the hour of the day, 0 to 23
     * @param last what she said last, which she does not say twice running; may be null
     */
    static String idle(Random random, int hour, String last) {
        for (int tries = 0; ; tries++) {
            int roll = random.nextInt(100);
            String[] lines = roll < 8 ? OUTPUTS : roll < 24 ? (hour >= 5 && hour < 12 ? MORNING
                    : hour >= 12 && hour < 18 ? AFTERNOON : hour >= 18 && hour < 23 ? EVENING : NIGHT)
                    : roll < 62 ? TEASING : roll < 82 ? SUMS : roll < 96 ? GAMES : JAPANESE;
            String line = lines[random.nextInt(lines.length)];
            if (!line.equals(last) || tries > 8) return line;
        }
    }

    /** What she says as she sets about a prank. */
    static String prank(Random random) { return PRANK[random.nextInt(PRANK.length)]; }

    /** One of a few lines, or her cry about her output in their place about half the time. */
    static String orOutput(Random random, String... lines) {
        return random.nextBoolean() ? OUTPUT : lines[random.nextInt(lines.length)];
    }

    /** Every line she has of her own accord. */
    static String[] all() {
        int count = 0;
        for (String[] lines : EVERYTHING) count += lines.length;
        String[] all = new String[count];
        int next = 0;
        for (String[] lines : EVERYTHING) {
            for (String line : lines) all[next++] = line;
        }
        return all;
    }
}
