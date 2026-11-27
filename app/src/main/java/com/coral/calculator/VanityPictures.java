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
            if (outfit >= 0) return "Own the " + RunVanity.NAMES[outfit];
            if (ears > 0 && horns > 0) return "Own " + RunVanity.EARS[ears].toLowerCase(java.util.Locale.ROOT) + " and " + RunVanity.HORNS[horns].toLowerCase(java.util.Locale.ROOT);
            return ears > 0 ? "Own the " + RunVanity.EARS[ears].toLowerCase(java.util.Locale.ROOT) : "Own the " + RunVanity.HORNS[horns].toLowerCase(java.util.Locale.ROOT);
        }
    }

    static final List<Picture> ALL = new ArrayList<>();

    private static void add(String id, String title, int outfit, int ears, int horns) { ALL.add(new Picture(id, title, outfit, ears, horns)); }

    static {
        // Each outfit has a picture of the same name, so they pair up: the chibi outfit in the game, the full-size one in the gallery.
        add("pajama", "Pajama Set", 0, 0, 0);
        add("gothic", "Gothic Dress", 1, 0, 0);
        add("tabard", "Knight's Tabard", 2, 0, 0);
        add("bunny", "Bunny Girl", 3, 0, 0);
        add("maid", "Maid Dress", 4, 0, 0);
        add("idol", "Sloppy\u2606Stage", 5, 0, 0);
        add("swim1", "Seaside Swimsuit", 6, 0, 0);
        add("bunnyg", "Bunny Girl (Gacha)", 7, 0, 0);
        add("bunnyi", "Bunny Idol", 8, 0, 0);
        add("bunny2", "Bunny Girl, white", 9, 0, 0);
        add("maid2", "Maid Dress, pink", 10, 0, 0);
        add("idol2", "Sloppy\u2606Stage, crop", 11, 0, 0);
        add("swim2", "Seaside Swimsuit, frilled", 12, 0, 0);
        add("swim3", "Seaside Swimsuit, sporty", 13, 0, 0);
        add("pajama2", "Pajama Set, camisole", 14, 0, 0);
        add("gothic2", "Gothic Dress, mini", 15, 0, 0);
        add("tabard2", "Knight's Tabard, sky", 16, 0, 0);
        add("mage", "Mage Robe", 17, 0, 0);
        add("yukata", "Festival Yukata", 18, 0, 0);
        add("coat", "Winter Coat", 19, 0, 0);
        add("magical", "Magical Girl", 20, 0, 0);
        add("blazer", "School Blazer", 21, 0, 0);
        // The parts for her head: a picture for each, and for the combinations that are natural looks, opened when both parts are hers.
        add("p_noears", "No ears", -1, 1, 0);
        add("p_fox", "Fox ears", -1, 2, 0);
        add("p_bunny", "Bunny ears", -1, 3, 0);
        add("p_apron", "Apron headband", -1, 4, 0);
        add("p_nohorns", "No horns", -1, 0, 1);
        add("p_elf", "Elf ears", -1, 1, 2);
        add("p_elf_cat", "Elf ears, with cat ears", -1, 0, 2);
        add("p_bunny_nohorns", "Bunny ears, no horns", -1, 3, 1);
        add("p_fox_nohorns", "Fox ears, no horns", -1, 2, 1);
        add("p_apron_nohorns", "Apron headband, no horns", -1, 4, 1);
        add("p_noears_nohorns", "No ears, no horns", -1, 1, 1);
    }

    static String key(Picture picture) { return "run_van_pic_" + picture.id; }

    static boolean open(RunVanity.Keep save, Picture picture) { return save.tally(key(picture)) > 0; }

    /** Whether everything a picture is of is hers: its outfit, and its ears and its horns. */
    static boolean earned(RunVanity.Keep save, Picture picture) {
        return (picture.outfit < 0 || RunVanity.owns(save, picture.outfit)) && (picture.ears == 0 || RunVanity.earsOwned(save, picture.ears))
                && (picture.horns == 0 || RunVanity.hornsOwned(save, picture.horns));
    }

    /** Opens every picture whose outfit and parts are hers; how many that opened that were not open. They stay open. */
    static int refresh(RunVanity.Keep save) {
        int opened = 0;
        for (Picture picture : ALL) {
            if (!open(save, picture) && earned(save, picture)) {
                save.keep(key(picture), 1);
                opened++;
            }
        }
        return opened;
    }

    static int unlockOutfit(RunVanity.Keep save, int outfit) { return refresh(save); }

    static int unlockPart(RunVanity.Keep save, boolean ears, int one) { return refresh(save); }

    /** Whether a picture can be shown now: the outfit or part it is of has its pictures in the app. */
    static boolean shown(Picture picture) {
        return (picture.outfit < 0 || RunVanity.ready(picture.outfit)) && (picture.ears == 0 || RunVanity.earsReady(picture.ears))
                && (picture.horns == 0 || RunVanity.hornsReady(picture.horns));
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
