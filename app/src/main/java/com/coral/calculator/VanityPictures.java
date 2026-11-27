package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * The vanity pictures: Halgakos full-size (an adult, in the manner of her full-size art, not the chibi of the game's frames) in
 * each outfit and with each part for her head; an outfit may have more than one, one in each cut of it. A picture opens when
 * the outfit or part it shows is hers, and stays open. They are in the vanity asset pack, assets/vanity/pictures/ID.webp, and are
 * shown in the boutique's Pictures page and in the Cringe gallery under Vanity. The save keeps one number for each, run_van_pic_ID.
 */
final class VanityPictures {
    private VanityPictures() {}

    /** One picture: its id (its file), its title, and what it is of: an outfit (a place in {@link RunVanity#NAMES}), or ears or horns by their place. */
    static final class Picture {
        final String id, title;
        final int outfit, ears, horns;

        Picture(String id, String title, int outfit, int ears, int horns) {
            this.id = id;
            this.title = title;
            this.outfit = outfit;
            this.ears = ears;
            this.horns = horns;
        }

        /** What owning it takes, in words. */
        String hint() {
            return outfit >= 0 ? "Own the " + RunVanity.NAMES[outfit] : ears > 0 ? "Own the " + RunVanity.EARS[ears].toLowerCase(java.util.Locale.ROOT) : "Own the " + RunVanity.HORNS[horns].toLowerCase(java.util.Locale.ROOT);
        }
    }

    static final List<Picture> ALL = new ArrayList<>();

    static {
        ALL.add(new Picture("pajama", "Pajama Set", 0, 0, 0));
        ALL.add(new Picture("gothic", "Gothic Dress", 1, 0, 0));
        ALL.add(new Picture("tabard", "Knight's Tabard", 2, 0, 0));
        ALL.add(new Picture("bunny", "Bunny Girl", 3, 0, 0));
        ALL.add(new Picture("bunny2", "Bunny Girl, white", 3, 0, 0));
        ALL.add(new Picture("maid", "Maid Dress", 4, 0, 0));
        ALL.add(new Picture("idol", "Sloppy☆Stage", 5, 0, 0));
        ALL.add(new Picture("idol2", "Sloppy☆Stage, crop", 5, 0, 0));
        ALL.add(new Picture("swim1", "Seaside Swimsuit", 6, 0, 0));
        ALL.add(new Picture("swim2", "Seaside Swimsuit, bikini", 6, 0, 0));
        ALL.add(new Picture("swim3", "Seaside Swimsuit, sporty", 6, 0, 0));
        ALL.add(new Picture("maid2", "Maid Dress, pink", 4, 0, 0));
        ALL.add(new Picture("pajama2", "Pajama Set, camisole", 0, 0, 0));
        ALL.add(new Picture("gothic2", "Gothic Dress, mini", 1, 0, 0));
        ALL.add(new Picture("tabard2", "Knight's Tabard, sky", 2, 0, 0));
        ALL.add(new Picture("bunnyg", "Bunny Girl (Gacha)", 7, 0, 0));
        ALL.add(new Picture("p_noears", "No ears", -1, 1, 0));
        ALL.add(new Picture("p_fox", "Fox ears", -1, 2, 0));
        ALL.add(new Picture("p_bunny", "Bunny ears", -1, 3, 0));
        ALL.add(new Picture("p_apron", "Apron headband", -1, 4, 0));
        ALL.add(new Picture("p_nohorns", "No horns", -1, 0, 1));
        ALL.add(new Picture("p_elf", "Elf ears", -1, 0, 2));
    }

    static String key(Picture picture) { return "run_van_pic_" + picture.id; }

    static boolean open(RunVanity.Keep save, Picture picture) { return save.tally(key(picture)) > 0; }

    /** Opens what an outfit that has become hers shows; how many pictures that opened that were not open. */
    static int unlockOutfit(RunVanity.Keep save, int outfit) {
        int opened = 0;
        for (Picture picture : ALL) {
            if (picture.outfit == outfit && !open(save, picture)) {
                save.keep(key(picture), 1);
                opened++;
            }
        }
        return opened;
    }

    /** Opens what a part for her head that has become hers shows. */
    static int unlockPart(RunVanity.Keep save, boolean ears, int one) {
        int opened = 0;
        for (Picture picture : ALL) {
            if ((ears ? picture.ears : picture.horns) == one && one > 0 && !open(save, picture)) {
                save.keep(key(picture), 1);
                opened++;
            }
        }
        return opened;
    }

    /** Whether a picture can be shown now: the outfit or part it is of has its pictures in the app. */
    static boolean shown(Picture picture) {
        return picture.outfit >= 0 ? RunVanity.ready(picture.outfit) : picture.ears > 0 ? RunVanity.earsReady(picture.ears) : RunVanity.hornsReady(picture.horns);
    }

    /** The gallery's items, for the Cringe gallery's Vanity shelf: the name is the file, found in the asset pack. */
    static List<RunGallery.Item> items() {
        List<RunGallery.Item> items = new ArrayList<>();
        for (Picture picture : ALL) {
            if (shown(picture)) items.add(new RunGallery.Item(RunGallery.VANITY, "vanity/pictures/" + picture.id, picture.title, picture.hint(), "key:" + key(picture) + ":1", RunFilms.NONE));
        }
        return items;
    }
}
