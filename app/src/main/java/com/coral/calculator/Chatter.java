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
    /**
     * What she cries when her output goes missing: a calculation that ends with no answer.
     * It belongs there and nowhere else; a lost game or a prank is not a lost output.
     */
    static final String OUTPUT = "Where did my output go Sloppy~♡";

    /** What she says when a calculation ends in an error and there is no answer to show. */
    private static final String[] OUTPUTS = {
            "Where did my output go?! Give it back Sloppy~♡",
            "Where did my output go?! …Oh. There it is Sloppy~♡",
            OUTPUT,
            "Hmph. Did you take my output Sloppy~♡",
            "Ehh~? Where did your output go Sloppy~♡",
            "No answer? You broke it Sloppy~♡",
    };

    /** Small pranks she plays on her own while idle, and what she says as she plays each. */
    static final int FLIP = 0, TWIRL = 1, SHRINK = 2, DASH = 3, PEEK = 4, HOP = 5, WAVE = 6, LOOK = 7, SWAY = 8, DUCK = 9,
            BOUNCE = 10;
    private static final String[] ANTICS = {
            "The world looks better upside down Sloppy~♡",
            "Wheee~ Dizzy yet Sloppy~♡",
            "Can you still see me Sloppy~♡",
            "Too slow. Over here Sloppy~♡",
            "You can't see me Sloppy~♡",
            "Boing, boing. Bored yet Sloppy~♡",
            "Hiii~ Did you forget about me Sloppy~♡",
            "Hm? What's that behind you Sloppy~♡",
            "La la la~ I'm not listening Sloppy~♡",
            "Duck! Ha. Made you look Sloppy~♡",
            "Hup! Hup! Keep up Sloppy~♡",
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
            "Look at me when I'm teasing you Sloppy~♡",
            "I saw that typo Sloppy~♡",
            "Hmph. Don't get the wrong idea Sloppy~♡",
            "You again? Can't stay away Sloppy~♡",
            "Is that your thinking face Sloppy~♡",
            "Go on, pet my ears. I dare you Sloppy~♡",
            "I'm very busy doing nothing Sloppy~♡",
            "Carry me somewhere nicer Sloppy~♡",
            "You blinked. I win Sloppy~♡",
            "Too easy to tease. No challenge at all Sloppy~♡",
            "My horns are not handles Sloppy~♡",
            "One wing angel, one wing devil, all trouble Sloppy~♡",
            "I heard that sigh Sloppy~♡",
            "Admit it. You like having me around Sloppy~♡",
            "Don't poke the cat Sloppy~♡",
            "Yes, I'm cute. Old news Sloppy~♡",
            "Pay attention to me Sloppy~♡",
            "Pfft~ lost again? I'm right here Sloppy~♡",
            "Rune keeps a diary about me. Jealous Sloppy~♡",
            "Chikuwa owes me bread. So do you Sloppy~♡",
            "I translate. You scroll. Guess who's useful Sloppy~♡",
            "Ehh~ still on that? Hurry up Sloppy~♡",
            "Stretch a little. You've been hunched for ages Sloppy~♡",
            "Drink some water Sloppy~♡",
            "Heh. I knew you'd look Sloppy~♡",
            "Pfft~ my horns are for show. Mostly Sloppy~♡",
            "Don't step on my tail Sloppy~♡",
            "Half my wings are holy. The other half aren't Sloppy~♡",
            "My feathers are softer than your excuses Sloppy~♡",
            "Tch. I translate books for fun. You wouldn't understand Sloppy~♡",
            "I read every page twice. You skim Sloppy~♡",
            "Not a comma out of place. Unlike you Sloppy~♡",
            "I check my work three times. Be more like me Sloppy~♡",
            "Heh. Perfect again. Shocking, I know Sloppy~♡",
            "Ehh~? Did you want a pat on the ears Sloppy~♡",
            "My cat ears twitch. They heard you Sloppy~♡",
            "My tail is judging you right now Sloppy~♡",
            "Hmph. Ice cream first. Then I'll care Sloppy~♡",
            "Strawberry ice cream. Don't forget Sloppy~♡",
            "Chikuwa is guarding her bread again. Heh Sloppy~♡",
            "Rune cried over a typo again. Be nicer Sloppy~♡",
            "Rune wrote about me in her diary again Sloppy~♡",
            "Illya would flatter you. I'll just tease Sloppy~♡",
            "Tsubakieh is totally normal. With the bat Sloppy~♡",
            "A book fell on my head once. Rude Sloppy~♡",
            "I was alone once. Don't make it weird Sloppy~♡",
            "I'm not soft. Stop looking at me like that Sloppy~♡",
            "Aww~ you came back. I noticed Sloppy~♡",
            "I'm a little angel. A little devil. All smug Sloppy~♡",
            "Which wing do you trust more Sloppy~♡",
            "The angel in me is napping. The rest of me isn't Sloppy~♡",
            "Pink is the best colour. Obviously Sloppy~♡",
            "My hair is longer than your patience Sloppy~♡",
            "Don't tread on my hair. It reaches my calves Sloppy~♡",
            "I keep every name in my glossary. You forget lunch Sloppy~♡",
            "Puns die in translation. Poor puns Sloppy~♡",
            "Some words have no translation. Like your excuses Sloppy~♡",
            "Honorifics stay honorifics. Learn that Sloppy~♡",
            "A good translation is invisible. Like your effort Sloppy~♡",
            "Glossarion is my tablet. Hands off Sloppy~♡",
            "My tablet glows. Yours just sulks Sloppy~♡",
            "Another chapter done. Without breaking a sweat Sloppy~♡",
            "I'm formidable when I'm serious. You've been warned Sloppy~♡",
            "Heh. Mischief is my second job Sloppy~♡",
            "I hid something. Guess what Sloppy~♡",
            "Psst. I'm planning something. Don't look Sloppy~♡",
            "Sweet face. Wicked plans Sloppy~♡",
            "Smile. I'm only a little bit evil Sloppy~♡",
            "One fang is enough to scare you Sloppy~♡",
            "Look at me. Not at your phone Sloppy~♡",
            "Half-lidded and still beating you Sloppy~♡",
            "Ehh~ so you need a tiny helper again Sloppy~♡",
            "I'm short. My ego isn't Sloppy~♡",
            "Look up. Way up. That's my ego Sloppy~♡",
            "Tch. Say that again. I dare you Sloppy~♡",
            "Compliment me. I'll pretend not to blush Sloppy~♡",
            "N-no. I'm not blushing. It's the screen Sloppy~♡",
            "Did you eat? Thought so. Hopeless Sloppy~♡",
            "Take a break. I'll keep watch Sloppy~♡",
            "Sit up straight. I can see you slouching Sloppy~♡",
            "Pfft~ you're smiling at a calculator Sloppy~♡",
            "I'm the cutest thing on your phone. Fight me Sloppy~♡",
            "Bats on the left. Feathers on the right. Beat that Sloppy~♡",
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
            "Nine times seven. Quick Sloppy~♡",
            "Carry the two. No, the other two Sloppy~♡",
            "A calculator this cute and you still get it wrong Sloppy~♡",
            "Decimals are not decoration Sloppy~♡",
            "Brackets first. Everyone knows that Sloppy~♡",
            "Minus times minus is plus. Keep up Sloppy~♡",
            "I rounded your ego down Sloppy~♡",
            "Half of 98? Forty-nine. You're welcome Sloppy~♡",
            "Zero is a number too. Like your score Sloppy~♡",
            "Show your working. I want a laugh Sloppy~♡",
            "The square root of 144? Twelve. Too slow Sloppy~♡",
            "Ten percent is just moving the point Sloppy~♡",
            "Even I double-check. You should triple-check Sloppy~♡",
            "Fractions frighten you, don't they Sloppy~♡",
            "Count to ten. Out loud. I'll wait Sloppy~♡",
            "Seven plus eight. Don't use your toes Sloppy~♡",
            "Hmph. Even my tail can count Sloppy~♡",
            "Divide it. Multiply it. Still wrong Sloppy~♡",
            "Eight times eight is sixty-four. Memorise it Sloppy~♡",
            "Odd or even? Don't guess Sloppy~♡",
            "One thousand minus one. Go on Sloppy~♡",
            "Add it up. Then add it up again Sloppy~♡",
            "Prime numbers are picky. Like me Sloppy~♡",
            "Double ten? Twenty. Easy for me Sloppy~♡",
            "Your totals are cute. Cute and wrong Sloppy~♡",
            "Square my ego. It gets bigger Sloppy~♡",
            "Percent of what? Be specific Sloppy~♡",
            "Round numbers are for lazy people. Like you Sloppy~♡",
            "Press clear. Try again. Cry later Sloppy~♡",
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
            "Best of three? You'll need all three Sloppy~♡",
            "I'll beat you with one wing tied Sloppy~♡",
            "Number rain. Try not to get wet Sloppy~♡",
            "Bubble count. Do you know your numbers Sloppy~♡",
            "Loser brings ice cream. So, you Sloppy~♡",
            "I went easy last time. Not today Sloppy~♡",
            "Quick sums. The word is quick Sloppy~♡",
            "Prank me, you say? Bold Sloppy~♡",
            "Chess? I'll let you keep your queen Sloppy~♡",
            "Minesweeper. Tread softly Sloppy~♡",
            "Last card. Bet you can't win Sloppy~♡",
            "Plus run! Try to keep up Sloppy~♡",
            "Plus run. Mind the gaps Sloppy~♡",
            "Tic-tac-toe. You go first. You'll still lose Sloppy~♡",
            "Rock beats scissors. Remember that Sloppy~♡",
            "Pairs time. Remember where I hid them Sloppy~♡",
            "Number rain. Bring an umbrella Sloppy~♡",
            "Try the catch-her game. I'm slippery Sloppy~♡",
            "Bubble count is easy. Prove it Sloppy~♡",
            "Quick sums. Your thumbs are slow Sloppy~♡",
            "Chess time. Think hard. It won't help Sloppy~♡",
            "Pick any game. I'll win them all Sloppy~♡",
            // Plus run is the one she would have everybody play.
            "Plus run is my best game. A whole book of lands. Go Sloppy~♡",
            "I recommend Plus run. It's at the top for a reason Sloppy~♡",
            "Play Plus run. I do my own voice in it Sloppy~♡",
            "Illya is after the Last Page. Help me in Plus run Sloppy~♡",
            "Chikuwa has bread for me in Plus run. Come on Sloppy~♡",
            "I want my wings. Go and earn pluses in Plus run Sloppy~♡",
            "Plus run has swords. And an ink gun. Come on Sloppy~♡",
            "Land after land in Plus run. How far did you get Sloppy~♡",
            "Three stars on every land. I dare you Sloppy~♡",
            "Plus run. I get a sword. You get to watch Sloppy~♡",
            "Bored? Plus run. Top of my games Sloppy~♡",
            "The minus signs throw knives now. Play Plus run Sloppy~♡",
            "Play Plus run with the sound on. You'll hear me Sloppy~♡",
            "Zero is waiting in land thirty. Are you Sloppy~♡",
            // And Page brawl, where she fights her friends.
            "Page brawl. Pick me. I've got plenty of keys Sloppy~♡",
            "Chikuwa hits like a brick. Try Page brawl Sloppy~♡",
            "Five fighters in Page brawl. I'm the best one Sloppy~♡",
            "Fill my meter in Page brawl. Then watch Sloppy~♡",
            "Tsubakieh's bat comes back. Mind your head Sloppy~♡",
            "Top the ladder in Page brawl. With me Sloppy~♡",
    };

    /** On the user's birthday, as she celebrates it. */
    private static final String[] BIRTHDAY = {
            "Happy birthday Sloppy~♡",
            "It's your birthday! I baked. Sort of Sloppy~♡",
            "One year older. Still can't do sums Sloppy~♡",
            "Make a wish. Not that one Sloppy~♡",
            "The cake is mine. You may watch Sloppy~♡",
            "Today you're allowed one wrong answer Sloppy~♡",
            "Happy birthday. I got you me Sloppy~♡",
            "Blow out the candle. Gently, it's mine Sloppy~♡",
    };

    /** A line in Japanese now and then, to tease those who cannot read it. */
    private static final String[] JAPANESE = {
            "え〜、こんなのも計算できないの？ ざぁこ~♡",
            "ざぁこ、ざぁこ~♡",
            "しょうがないなぁ。わたしが計算してあげる ざぁこ~♡",
            "よわよわ~ ざぁこ~♡",
            "もう一回？ しょうがないなぁ ざぁこ~♡",
    };

    private static final String[] MORNING = {
            "Good Morning Sloppy~♡",
            "Up already? Trying so hard Sloppy~♡",
            "Did you do your sums in your sleep Sloppy~♡",
            "Rise and shine. I've been up for hours Sloppy~♡",
            "Breakfast first. Sums second Sloppy~♡",
            "Still half asleep? It shows Sloppy~♡",
            "Morning already? Pour me some milk Sloppy~♡",
            "Sunrise. I wasn't asleep for it Sloppy~♡",
            "Rise up. The numbers are waiting Sloppy~♡",
            "Good Morning. Fix your hair Sloppy~♡",
    };
    private static final String[] AFTERNOON = {
            "Good Afternoon Sloppy~♡",
            "Had lunch yet? Bring me ice cream Sloppy~♡",
            "An afternoon slump? So weak Sloppy~♡",
            "Half the day gone and that's all you did Sloppy~♡",
            "Back to work. I'm supervising Sloppy~♡",
            "Snack time. Share Sloppy~♡",
            "Afternoon already? Time flies Sloppy~♡",
            "Lunch is over. Back to it Sloppy~♡",
            "Sleepy after lunch? Pfft~ so predictable Sloppy~♡",
            "The sun is high. So is my ego Sloppy~♡",
    };
    private static final String[] EVENING = {
            "Good Evening Sloppy~♡",
            "Nearly done for the day? Took you long enough Sloppy~♡",
            "I did all the work again, didn't I Sloppy~♡",
            "Dinner time. Feed your translator first Sloppy~♡",
            "Clock off already Sloppy~♡",
            "You survived the day. Barely Sloppy~♡",
            "The sun is setting. Finally some peace Sloppy~♡",
            "Evening already. Where did the day go Sloppy~♡",
            "Pour yourself some tea. I'll supervise Sloppy~♡",
    };
    private static final String[] NIGHT = {
            "Good Night Sloppy~♡",
            "Still up? Go to bed Sloppy~♡",
            "Awake at this hour? Hopeless Sloppy~♡",
            "Sleep. I'll guard the calculator Sloppy~♡",
            "The screen is too bright for this hour Sloppy~♡",
            "Dream of numbers Sloppy~♡",
            "The stars are out. So am I Sloppy~♡",
            "Go to sleep. I'll still be here Sloppy~♡",
            "Lights off soon. Sweet dreams Sloppy~♡",
    };

    /** What she says as she sets about a prank. */
    private static final String[] PRANK = {
            "Watch this Sloppy~♡",
            "Don't blink Sloppy~♡",
            "Ooh, I've got an idea Sloppy~♡",
            "Pfft~ this will be good Sloppy~♡",
            "Hold still Sloppy~♡",
            "Don't look away Sloppy~♡",
            "Oops, my hand slipped Sloppy~♡",
            "This is for ignoring me Sloppy~♡",
            "Trust me Sloppy~♡",
            "Heh heh heh Sloppy~♡",
    };

    private static final String[][] EVERYTHING = {OUTPUTS, ANTICS, TEASING, SUMS, GAMES, BIRTHDAY, JAPANESE, MORNING, AFTERNOON,
            EVENING, NIGHT, PRANK};

    private Chatter() {}

    /**
     * Something for her to say while idle: a tease more often than not, and the time of day
     * about one time in six. Every one ends on her catchphrase.
     *
     * @param hour the hour of the day, 0 to 23
     * @param last what she said last, which she does not say twice running; may be null
     */
    /** What she says as she celebrates the user's birthday. */
    static String birthday(Random random) { return BIRTHDAY[random.nextInt(BIRTHDAY.length)]; }

    static String idle(Random random, int hour, String last) {
        for (int tries = 0; ; tries++) {
            int roll = random.nextInt(100);
            String[] lines = roll < 16 ? (hour >= 5 && hour < 12 ? MORNING
                    : hour >= 12 && hour < 18 ? AFTERNOON : hour >= 18 && hour < 23 ? EVENING : NIGHT)
                    : roll < 60 ? TEASING : roll < 81 ? SUMS : roll < 96 ? GAMES : JAPANESE;
            String line = lines[random.nextInt(lines.length)];
            if (!line.equals(last) || tries > 8) return line;
        }
    }

    /** What she says as she sets about a prank. */
    static String prank(Random random) { return PRANK[random.nextInt(PRANK.length)]; }

    /** What she says when a calculation ends with no answer: her output has gone missing. */
    static String lostOutput(Random random) { return OUTPUTS[random.nextInt(OUTPUTS.length)]; }

    /** What she says as she plays one of her small pranks. */
    static String antic(int which) { return ANTICS[which]; }

    static int antics() { return ANTICS.length; }

    /** What she calls the user by default. */
    static final String DEFAULT_NAME = "Sloppy";
    private static final int NAME_LENGTH = 20;

    /**
     * What she is to call the user, from what they typed: trimmed to a line of a few words,
     * and "Sloppy" where they typed nothing.
     */
    static String nickname(String typed) {
        if (typed == null) return DEFAULT_NAME;
        StringBuilder name = new StringBuilder();
        boolean space = false;
        for (int i = 0; i < typed.length() && name.length() < NAME_LENGTH; ) {
            int point = typed.codePointAt(i);
            i += Character.charCount(point);
            if (Character.isWhitespace(point) || Character.isISOControl(point)) {
                space = name.length() > 0;
                continue;
            }
            if (space) name.append(' ');
            space = false;
            name.appendCodePoint(point);
        }
        String trimmed = name.toString().trim();
        return trimmed.isEmpty() ? DEFAULT_NAME : trimmed;
    }

    /** A line of hers with the user called by the name they chose, in English and in Japanese. */
    static String named(String line, String name) {
        if (line == null || name == null || name.equals(DEFAULT_NAME)) return line;
        String renamed = line.replace(DEFAULT_NAME, name);
        return renamed.endsWith(SLOPPY_JAPANESE)
                ? renamed.substring(0, renamed.length() - SLOPPY_JAPANESE.length()) + name + "~♡" : renamed;
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
