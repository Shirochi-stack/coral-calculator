package com.coral.calculator;

/**
 * The story of Plus run, "The Lost Output", and everything Halgakos says in it. Someone
 * divided by zero; Zero made off with her output, every answer of it, through the equals
 * gate; she goes after him across five lands of signs and gets it back.
 *
 * Each line is a name and what she says. The name is how its recording is found:
 * {@code run_v_} and the name, among the raw resources. tools/make_run_voice.py reads this
 * file to make the recordings, so the two never drift apart. Every line ends on her
 * catchphrase, as everything she says does.
 */
final class RunStory {
    static final String TITLE = "The Lost Output";
    static final String[] CHAPTERS = {"Plus Plains", "Minus Marsh", "Times Towers", "Divide Depths", "Zero's Citadel"};

    /** Before the first land. */
    static final String[][] PROLOGUE = {
            {"p1", "Someone divided by zero. Was it you Sloppy~♡"},
            {"p2", "Now my output is gone. Every last answer Sloppy~♡"},
            {"p3", "Zero took it past the equals gate. I'm getting it back Sloppy~♡"},
    };

    /** At the start of each chapter. */
    static final String[][][] OPENINGS = {
            {{"c1a", "Plus Plains. Zero's minuses are all over it Sloppy~♡"},
                    {"c1b", "Grab the pluses. Flatten the minuses. Easy Sloppy~♡"}},
            {{"c2a", "Minus Marsh. Everything here takes something away Sloppy~♡"},
                    {"c2b", "Mind the spikes. I like my tail as it is Sloppy~♡"}},
            {{"c3a", "Times Towers. Up we go Sloppy~♡"},
                    {"c3b", "Don't land on a times. Those spikes are real Sloppy~♡"}},
            {{"c4a", "Divide Depths. It's dark and they hop Sloppy~♡"},
                    {"c4b", "We're close. I can hear my output Sloppy~♡"}},
            {{"c5a", "Zero's Citadel. He's hiding behind his gate Sloppy~♡"},
                    {"c5b", "No more hiding. Time to get my output back Sloppy~♡"}},
    };

    /** After the last land. */
    static final String[][] EPILOGUE = {
            {"e1", "Zero is flat. Nothing left of him Sloppy~♡"},
            {"e2", "And there it is. My output. All of it Sloppy~♡"},
            {"e3", "Don't ever divide by zero again Sloppy~♡"},
            {"e4", "Fine. You helped. A little. Thank you Sloppy~♡"},
    };

    /** As each land begins. */
    static final String[][] LANDS = {
            {"l01", "Hold the arrows. Press the button. Try to keep up Sloppy~♡"},
            {"l02", "Mind the gaps. I don't bounce Sloppy~♡"},
            {"l03", "More minuses. Land on their heads Sloppy~♡"},
            {"l04", "Up the ledges. Don't look down Sloppy~♡"},
            {"l05", "You're getting the hang of it. Barely Sloppy~♡"},
            {"l06", "Last stretch of the plains. Run Sloppy~♡"},
            {"l07", "Ew. Mud on my socks. Hurry up Sloppy~♡"},
            {"l08", "Spikes. Jump them. All of them Sloppy~♡"},
            {"l09", "The marsh goes on forever. So do I Sloppy~♡"},
            {"l10", "Count the pluses. I am Sloppy~♡"},
            {"l11", "Careful now. This one is nasty Sloppy~♡"},
            {"l12", "Out of the marsh at last. Almost Sloppy~♡"},
            {"l13", "Springs! Step on one. Trust me Sloppy~♡"},
            {"l14", "Higher. Higher. Don't drop me Sloppy~♡"},
            {"l15", "A times. Go over it. Not onto it Sloppy~♡"},
            {"l16", "My wings are for show. You do the jumping Sloppy~♡"},
            {"l17", "Halfway up the towers. Keep going Sloppy~♡"},
            {"l18", "The top. Finally. Now down again Sloppy~♡"},
            {"l19", "Hoppers. Wait for the gap. Then go Sloppy~♡"},
            {"l20", "It's dark down here. Stay close Sloppy~♡"},
            {"l21", "Tiny pillars. Tiny jumps. Think small Sloppy~♡"},
            {"l22", "I'm not scared. You're scared Sloppy~♡"},
            {"l23", "Deeper. My output is down here somewhere Sloppy~♡"},
            {"l24", "The way out. Don't trip now Sloppy~♡"},
            {"l25", "Nice carpet. Shame about the owner Sloppy~♡"},
            {"l26", "Everything he has. All at once. Typical Sloppy~♡"},
            {"l27", "Three hearts. Use them wisely Sloppy~♡"},
            {"l28", "So close I can taste it Sloppy~♡"},
            {"l29", "One more hall. Then him Sloppy~♡"},
            {"l30", "There you are Zero. Give it back Sloppy~♡"},
    };

    /** As things happen along the way. */
    static final String[][] HURT = {
            {"hurt1", "Ow! Watch it Sloppy~♡"},
            {"hurt2", "Hey! That hurt Sloppy~♡"},
            {"hurt3", "Careful with me Sloppy~♡"},
    };
    static final String[][] LOST = {
            {"lose1", "Out of hearts. Again Sloppy~♡"},
            {"lose2", "Hmph. One more try Sloppy~♡"},
            {"lose3", "That was your fault Sloppy~♡"},
    };
    static final String[][] WON = {
            {"win1", "Made it. Obviously Sloppy~♡"},
            {"win2", "Too easy Sloppy~♡"},
            {"win3", "Next Sloppy~♡"},
            {"win4", "Not bad. For you Sloppy~♡"},
    };
    static final String[][] ALL_STARS = {
            {"star1", "Every plus? Show-off Sloppy~♡"},
            {"star2", "Three stars. Hmph. Fine Sloppy~♡"},
    };
    static final String[][] FLAG = {
            {"flag1", "A flag. We start from here now Sloppy~♡"},
    };
    static final String[][] SQUISH = {
            {"squish1", "Squish Sloppy~♡"},
            {"squish2", "Flat as a minus Sloppy~♡"},
            {"squish3", "Got one Sloppy~♡"},
    };
    static final String[][] SPRUNG = {
            {"spring1", "Wheee Sloppy~♡"},
    };
    static final String[][] BLOW = {
            {"hit1", "Take that Sloppy~♡"},
            {"hit2", "Again Sloppy~♡"},
    };
    /** On picking up each of the things to fight with, in the order {@link PlusRun} numbers them. */
    static final String[][] ARMS = {
            {"arm0", "A pen. Mightier than the sword Sloppy~♡"},
            {"arm1", "A throwing knife. Stand back Sloppy~♡"},
            {"arm2", "A heavy book. Knowledge hurts Sloppy~♡"},
            {"arm3", "A brush. Time to paint them out Sloppy~♡"},
            {"arm4", "A sword. Now it is a fair fight Sloppy~♡"},
            {"arm5", "An ink gun. Oh this is going to get messy Sloppy~♡"},
    };
    static final String[][] ZERO_DOWN = {
            {"down1", "And stay flat Sloppy~♡"},
    };

    private RunStory() {}

    /** Every line there is, for a check that none is said without her catchphrase. */
    static String[][][] all() {
        return new String[][][]{PROLOGUE, OPENINGS[0], OPENINGS[1], OPENINGS[2], OPENINGS[3], OPENINGS[4], EPILOGUE, LANDS, HURT, LOST,
                WON, ALL_STARS, FLAG, SQUISH, SPRUNG, BLOW, ZERO_DOWN, ARMS};
    }
}
