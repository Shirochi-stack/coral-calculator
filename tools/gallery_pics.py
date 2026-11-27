"""
The 100 gallery pictures of Plus run that open as the story goes on.

    python tools/gallery_pics.py jobs             writes the three shards of jobs for the art script (gen.py, gpt-6-astra) under WORK
    python tools/gallery_pics.py sheets           a contact sheet for each twenty pictures that exist
    python tools/gallery_pics.py webp             converts the pictures to WebP (quality 80, 1280 wide) into app/src/main/assets/gallery and writes pictures.tsv

Each picture is a row: name, title, the word shown while it is locked, the rule that opens it (see RunGallery), who is in it, and the scene.
The rules: "open:N" (land N has been reached), "land:N" (land N has been won), "ending:1" the bad ending (the land unmade),
"ending:2" the true one, "ending:4" the Mother Tongue risen. At least five belong to each of the bad and the true ending, and none is shared.
"""
import json
import pathlib
import sys

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
WORK = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\gallery-art")
DIST = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes")
OUT = ROOT / "app" / "src" / "main" / "assets" / "gallery"
FACES = ROOT / "assets" / "plus-run" / "faces"
REFS = {"H": DIST / "opening/sheets/halgakos.png", "C": DIST / "opening/sheets/chikuwa.png", "R": DIST / "opening/sheets/rune.png",
        "I": DIST / "opening/sheets/illya.png", "T": DIST / "opening/sheets/tsubakieh.png", "W": DIST / "mother-rises/ref_halgakos_white.png",
        "F": DIST / "mother-rises/ref_mother.png", "G": FACES / "gokhae.png", "N": FACES / "nuri.png", "J": FACES / "miri.png",
        "K": FACES / "gari.png", "Y": FACES / "yeobaek.png"}
STYLE = ("A 2D anime illustration in exactly the clean webtoon style of the reference pictures: crisp line art, soft cel shading, rich colour, "
         "a full background, characters on model from their reference sheets. Wide 3:2 picture. No text, no speech bubbles, no logos, no watermark. "
         "Family-friendly: ink and paper instead of gore, no one sexualised.")

# name, title, locked hint, rule, cast, scene
P = [
    # --- the first volume ---
    ("pre_01_ice_cream", "One day off", "At the very start", "open:1", "H", "Halgakos on a park bench in the sun, eyes closed, happily licking a mint ice cream cone; the shadow of a gigantic book is just starting to fall over her."),
    ("pre_02_book_lands", "The book lands", "At the very start", "open:1", "H", "The World's Largest Book has landed on a park bench in a cloud of dust; only Halgakos's hand with a squashed mint ice cream sticks out from under its cover."),
    ("pre_03_lettera", "Welcome to Lettera", "At the very start", "open:1", "H", "Halgakos wakes up lying on the page of a giant book, looking up at a sky of drifting letters and a floating table of contents, dazed."),
    ("pre_04_walking_signs", "Signs with legs", "At the very start", "open:1", "H", "Halgakos stares in disbelief as plus, minus, times and divide signs walk around a green page-hill on tiny legs."),
    ("pre_05_pluses", "Her output burst", "At the very start", "open:1", "H", "Halgakos kneeling to pick up glowing gold plus signs that lie scattered on the grass like coins, a few still floating down from the sky."),
    ("l1_01_contents_village", "Contents Village", "Reach land 1", "open:1", "H", "Halgakos walking down a charming village street where every house has a chapter number sign, a book-shaped mill in the background."),
    ("l1_02_first_squish", "Squish", "Win land 1", "land:1", "H", "Halgakos stomping a minus sign flat as a page, mid-air, with a smug grin; paper puffs around."),
    ("l1_03_first_flag", "The first flag", "Win land 1", "land:1", "H", "Halgakos touching a small checkpoint flag at sunset on a grassy hill, proud, the village behind."),
    ("l3_01_brick_bread", "Bread turned brick", "Reach land 3", "open:3", "HC", "Chikuwa wailing comically over a loaf of bread turned to grey stone, her shop sign reading FISH CAKE above; Halgakos watches, amused."),
    ("l3_02_names_stay_names", "Names stay names", "Win land 3", "land:3", "HC", "Halgakos taps her open book and the stone loaf turns into warm golden bread and the sign is mended; Chikuwa beams."),
    ("l3_03_fresh_bread", "Fresh bread by the flag", "Win land 3", "land:3", "C", "Chikuwa waiting by a flag with a basket of fresh bread, steam rising, a happy wave."),
    ("l6_01_guild_exam", "The guild exam", "Win land 6", "land:6", "H", "Halgakos facing a huge armoured guild guard at a guild door with a banner, ready to fight, determined."),
    ("l6_02_heart_prize", "A heart for passing", "Win land 6", "land:6", "H", "Halgakos holding up a glowing heart-shaped gem prize proudly in front of the guild hall."),
    ("l7_01_silent_archive", "The Silent Archive", "Reach land 7", "open:7", "H", "Halgakos stepping carefully into a huge dim silent library with towering shelves and floating dust motes, whispering."),
    ("l7_02_quiet_library", "A quiet library", "Reach land 7", "open:7", "HT", "Tsubakieh proudly standing in an empty library with her bat on her shoulder; Halgakos facing her, unimpressed, arms folded."),
    ("l9_01_first_letter", "A letter read", "Reach land 9", "open:9", "HR", "Rune clutching a letter to her chest, sobbing ink drops, looking up at Halgakos who holds the letter, read; a dim library."),
    ("l9_02_friends_forever", "Friends. Forever.", "Win land 9", "land:9", "HR", "Rune clinging to Halgakos's sleeve in tears, Halgakos flustered and teasing; a warm lamp-lit library nook."),
    ("l9_03_ink_shield", "Ink wall", "Win land 9", "land:9", "R", "Rune sweeping her hands to raise a great wave-shaped wall of ink that swallows enemies, tears in her eyes, determined."),
    ("l12_01_pen_butler", "The fountain-pen butler", "Win land 12", "land:12", "H", "A prim fountain-pen lady butler bowing politely and refilling Halgakos's big ink gun in a tidy study."),
    ("l13_01_heart_plaza", "Heart-Flutter Plaza", "Reach land 13", "open:13", "HI", "Illya the magical-girl idol on a small stage in a pink plaza full of floating hearts; Halgakos at the plaza entrance, wary."),
    ("l13_02_arrows_turned", "Her hearts turn arrows", "Reach land 13", "open:13", "HI", "Halgakos dodging a storm of pink hearts while her thrown arrow spins around to point back at her; Illya laughing sweetly."),
    ("l18_01_compliment", "Nine thousand compliments", "Reach land 18", "open:18", "HI", "Illya clasping her hands and blowing a heart toward Halgakos as she gives a sincere compliment; Halgakos blushing, nearly charmed."),
    ("l18_02_nobody_said_no", "Nobody said no", "Win land 18", "land:18", "HI", "Illya sitting shaken on the stage floor, smile wobbling, as Halgakos holds up a small scan screen reading zero."),
    ("l18_03_a_little_sad", "A little sad", "Win land 18", "land:18", "HI", "Halgakos kneeling beside Illya and gently offering a hand, hearts fading around them at dusk."),
    ("l19_01_index_mountains", "The Index Mountains", "Reach land 19", "open:19", "H", "Halgakos at the foot of mountains shaped like index tabs in a pastel sky, hands on hips, confident."),
    ("l19_02_another_us", "Another of us", "Reach land 19", "open:19", "HW", "In the far distance on a ledge, the white-haired Mawang watches coldly while Halgakos looks up at her from below, uneasy."),
    ("l24_01_cut_bridge", "The cut bridge", "Reach land 24", "open:24", "HT", "Tsubakieh on a mountain top over a ravine pointing at a cut rope bridge, deadpan with her bat; Halgakos horrified."),
    ("l24_02_bat_duel", "Batting practice", "Reach land 24", "open:24", "HT", "Halgakos leaping over a huge bat swing from Tsubakieh and landing lightly on the bat itself, smirking."),
    ("l24_03_bat_broken", "Normal No. 2 breaks", "Win land 24", "land:24", "HT", "Tsubakieh holding her snapped bat halves, stunned then sincerely sorry; Halgakos watching with a small smile."),
    ("l24_04_bread_again", "Friends can be baked again", "Win land 24", "land:24", "HCT", "Chikuwa offering Tsubakieh a warm loaf of bread with a bright smile, Halgakos sighing in relief, the sky brightening."),
    ("l25_01_burden", "A burden shared", "Reach land 25", "open:25", "HW", "The Mawang hovering on a lonely ledge in a grey sky holding out a hand; Halgakos shaking her head firmly."),
    ("l25_02_ink_sea", "The sea of ink", "Reach land 25", "open:25", "HW", "A wide dark sea of ink under a grey sky with drifting pages; Halgakos and the Mawang clash far above in a shock of pages."),
    ("l30_01_last_page", "The Last Page", "Reach land 30", "open:30", "HI", "Illya on a vast white stage writing triumphantly on the Last Page; Halgakos approaching from the edge."),
    ("l30_02_zero", "The Zero", "Reach land 30", "open:30", "HI", "A giant glowing zero rising from the white page behind Illya, Halgakos looking up at it grimly."),
    ("l30_03_sixty_years", "Sixty years of notes", "Reach land 30", "open:30", "HW", "The white-haired Mawang handing Halgakos a thick bundle of old handwritten notes, softened, close to tears."),
    ("l30_04_zero_shatters", "The Zero shatters", "Win land 30", "land:30", "HIT", "The giant zero bursting into a rain of glowing letters; Illya on her knees, Halgakos landing lightly, Tsubakieh nodding."),
    ("l30_05_cast_everyone", "Cast: everyone", "Win land 30", "land:30", "HIT", "The rewritten last page glowing with the words CAST EVERYONE as a soft light; Tsubakieh reading it, deadpan, pleased."),
    ("l30_06_loved", "You're complimenting me?", "Win land 30", "land:30", "HI", "Illya crying happily as Halgakos sincerely compliments her, both sitting on the white page in warm light."),
    ("l30_07_rune_watching", "Still watching", "Win land 30", "land:30", "HR", "Rune whispering very close behind Halgakos with a giggle and a tiny bit creepy smile, Halgakos startled."),
    ("l30_08_bread_for_all", "Everyone gets bread", "Win land 30", "land:30", "HC", "Chikuwa handing bread to the whole cast on the last page, joyful and bubbling."),
    # --- the second volume ---
    ("v2_01_volume_two_falls", "Volume Two falls", "Reach land 31", "open:31", "HT", "A giant second book falling from the sky onto a green field as Tsubakieh, with a mended bat, readies a swing; Halgakos shouting."),
    ("v2_02_home_run", "Home run", "Reach land 31", "open:31", "HT", "Tsubakieh's perfect home run sending the giant book spinning back, smacking down on Halgakos who yelps."),
    ("v2_03_inside_again", "Inside a book again", "Reach land 31", "open:31", "H", "Halgakos popping her head indignantly out of the pages of a second book, dust and paper everywhere."),
    ("v2_04_the_queen", "The queen speaks", "Reach land 31", "open:31", "HN", "A young queen in a red royal robe earnestly reporting a crime to Halgakos in a bright throne hall."),
    ("l35_01_scribe", "Back in the record", "Win land 35", "land:35", "H", "Halgakos watching a young court scribe write her name back into a great ledger with a quill, satisfied."),
    ("l40_01_gokhae", "How very literal of her", "Reach land 40", "open:40", "HG", "Gokhae the sly court interpreter hiding behind a black fan in the throne hall, mocking Halgakos."),
    ("l40_02_our_words", "Our words are Our own", "Reach land 40", "open:40", "HN", "The young queen standing up bravely to her advisor, voice trembling, in the throne hall."),
    ("l40_03_fan_down", "Fan lowered", "Reach land 40", "open:40", "HG", "Halgakos leaping toward Gokhae as she lowers her black fan to speak, a throne hall in motion."),
    ("l40_04_mistranslation", "A mistranslation...", "Win land 40", "land:40", "G", "Gokhae flustered and stammering, her fan dropped, beaten, her composure gone."),
    ("l40_05_we_can_be_heard", "We can be heard", "Win land 40", "land:40", "N", "The young queen joyfully breathless, radiant, as her own words are read out to a crowd."),
    ("l41_01_gazette", "The Gazette Press", "Reach land 41", "open:41", "HJK", "Miri shouting a headline in a printing house full of hot metal type while her shy twin Gari hides behind a black bar; Halgakos wry."),
    ("l45_01_toll", "The toll payout", "Win land 45", "land:45", "H", "Halgakos gleefully scooping a heap of gold plus signs from a defeated toll-taking merchant at a toll gate."),
    ("l50_01_spoiler", "Spoiler alert", "Reach land 50", "open:50", "HJ", "Miri looking through her rolled-up paper like a spyglass, taunting, cocky; Halgakos wary."),
    ("l50_02_black_bar", "Cover the page", "Reach land 50", "open:50", "HK", "Gari lifting a huge ink brush to paint black bars across the scene, whispering and nervous."),
    ("l50_03_not_so_bad", "Not so bad", "Win land 50", "land:50", "JK", "Gari's black bar slipping off her eyes, relieved; Miri wondering and delighted beside her at an ending she did not predict."),
    ("l51_01_between_lines", "Between the lines", "Reach land 51", "open:51", "H", "Halgakos stepping into an empty white world like a blank page, looking around uneasily."),
    ("l51_02_sleeping_sword", "A swordswoman asleep", "Reach land 51", "open:51", "HY", "Yeobaek the sleepy swordswoman dozing upright on her sword in a white void, mumbling; Halgakos peeking at her."),
    ("l60_01_woken", "A guard who is woken", "Reach land 60", "open:60", "HY", "Yeobaek half awake, yawning and apologising as she draws her sword; Halgakos bracing."),
    ("l60_02_cut_and_nap", "Cut and nap", "Reach land 60", "open:60", "HY", "Yeobaek stepping out behind Halgakos, one clean slash, then dozing off again standing up."),
    ("l60_03_company_returns", "The company returns", "Win land 60", "land:60", "HY", "A band of mercenaries marching back into the white void behind a smiling, drowsy Yeobaek; Halgakos shrugging, amused."),
    ("l61_01_slime_margins", "The Slime Margins", "Reach land 61", "open:61", "HC", "Chikuwa running in a comic panic over hills of green slime that have eaten her recipe notes; Halgakos pulling a face."),
    ("l65_01_brake_pads", "Worn out", "Win land 65", "land:65", "H", "Two tired little brake pads paying Halgakos a bounty just to make her stop, a bag of pluses changing hands."),
    ("l70_01_king_slime", "King Slime", "Reach land 70", "open:70", "HR", "A giant glossy green slime king with a tiny gold crown dropping from the sky; Rune quaking behind Halgakos."),
    ("l70_02_three_cuts", "Three cuts", "Reach land 70", "open:70", "H", "Halgakos slashing King Slime three times in the air with a sword, each cut a flash, the slime splitting into chunks."),
    ("l70_03_bread_cannon", "Bread cannon", "Win land 70", "land:70", "HC", "Chikuwa firing a bread cannon at slime chunks that turn into warm bread rolls raining down; Halgakos catching one."),
    ("l70_04_i_drew_them", "I drew them all", "Win land 70", "land:70", "R", "Rune sobbing with relief and laughter, ink lines glowing around her feet after the fight."),
    ("l71_01_audition", "The audition", "Reach land 71", "open:71", "HI", "Illya bursting into a bright room with a gold audition certificate, sparkling; Halgakos pleased."),
    ("l71_02_crossed_out", "A name crossed out", "Reach land 71", "open:71", "HI", "Halgakos and Illya looking at the heroine's page where a name has been crossed out in black ink, both worried."),
    ("l80_01_demon_king", "A demon king needs no one", "Reach land 80", "open:80", "HW", "The Mawang hovering over a dark plain, cold and hollow, warning them off with a raised hand; Halgakos stands facing her."),
    ("l80_02_not_her_line", "That's her line", "Reach land 80", "open:80", "HW", "Halgakos blocking the Mawang's dark wing with her open book and answering with her white wing, pages blasting outward."),
    ("l80_03_cheering", "The bread is here", "Reach land 80", "open:80", "HWCR", "At the edge of the fight, Chikuwa waving a loaf and Rune tearfully offering ink, both cheering Halgakos on as she faces the Mawang."),
    ("l80_04_what_took_so_long", "What took you so long", "Win land 80", "land:80", "HW", "The Mawang kneeling, her cold mask cracking, tears in her eyes, while Halgakos kneels beside her with a gentle smile, evening light."),
    ("l80_05_we_seal_it", "Cast: everyone", "Win land 80", "land:80", "HNIT", "The young queen decreeing the seal aloud in front of Halgakos, Illya and Tsubakieh in the throne hall, warm and regal."),
    ("l80_06_share_a_page", "Heroine: everyone", "Win land 80", "land:80", "HI", "Illya sharing the title of heroine on a page, shy and sparkling, with Halgakos grinning."),
    ("l80_07_proofreading", "A thousand and one pages", "Win land 80", "land:80", "HG", "Gokhae at a vast desk piled with stacks of pages, sentenced to proofreading, sulking; Halgakos smirking nearby."),
    ("l80_08_ice_cream_wish", "Now, my ice cream", "Win land 80", "land:80", "H", "Halgakos stretching, tired and happy on the grass beside a closed book, one finger raised, asking for her ice cream."),
    # --- the third volume ---
    ("v3_01_volume_three", "Volume Three", "Reach land 81", "open:81", "H", "A third giant book fallen on a white land, broken: every page stops mid-sentence; Halgakos standing before it with her hand on her hip."),
    ("v3_02_walk_with_you", "This time I walk with you", "Reach land 81", "open:81", "HW", "The white-haired Mawang offering to come along, standing beside Halgakos on a hill overlooking a broken white land."),
    ("v3_03_killer", "The Great Translation Killer", "Reach land 81", "open:81", "HN", "The young queen gravely passing on the fear of her people, Halgakos listening with her arms folded."),
    ("v3_04_unwriting_wave", "The unwriting wave", "Reach land 81", "open:81", "HW", "A dark wave of unwriting ink rolling in from the far edge of a broken white land toward Halgakos and the Mawang."),
    ("v3_05_back_to_back", "Back to back", "Win land 81", "land:81", "HW", "Halgakos and the Mawang back to back with wings open as one, cutting the dark wave in two with a spiral of white and black light."),
    ("l90_01_pledge", "The pledge", "Reach land 90", "open:90", "HW", "At the edge of a quiet sea at dusk, the Mawang making a quiet, rueful, warm promise to Halgakos."),
    ("l90_02_ice_cream_revenge", "Ice cream revenge", "Reach land 90", "open:90", "H", "An ink wave splashing Halgakos's mint ice cream cone on a shore; she glares and blasts the sea with a beam from her book, pouting."),
    ("l91_01_dragon_library", "The Dragon's Library", "Reach land 91", "open:91", "HR", "Halgakos looking up at a vast ruined library with Rune, tearful, beside her reporting that nothing in it is translated any more."),
    ("l91_02_someone_small", "Someone small lives here", "Reach land 91", "open:91", "H", "Halgakos peering at a tiny lit window high in a ruined library tower, uneasy, serious."),
    ("l100_01_last_land", "The last land", "Reach land 100", "open:100", "HCRIT", "Halgakos with her book raised and Chikuwa, Rune, Illya and Tsubakieh lined up behind her on a vast dark page-plain."),
    ("l100_02_everyone_together", "Everyone, together", "Reach land 100", "open:100", "HCRIT", "All five fighting a giant wave of ink at once: hearts, bread, ink, bat swing, book beam."),
    ("l100_03_dawn", "Dawn on the page", "Win land 100", "land:100", "HCRIT", "The last ink wave breaking into light as dawn rises over the page; Halgakos smirks at the viewer while her friends cheer."),
    # --- the Mother Tongue risen ---
    ("mt_01_rises", "The Mother Tongue rises", "In the last land", "ending:4", "HF", "The vast ivory Mother Tongue dragon with wings of pages rising over a ruined library; Halgakos looking up, steadying her book."),
    ("mt_02_page_storm", "Page storm", "In the last land", "ending:4", "HF", "The Mother Tongue spreading her wings and blasting a storm of blank pages down at Halgakos, who raises her book as a shield."),
    ("mt_03_ink_fire", "Ink fire", "In the last land", "ending:4", "HF", "Halgakos's torrent of ink fire clashing with the dragon's breath of pages in a fireball of ink and paper."),
    ("mt_04_wings_torn", "Wings of pages", "In the last land", "ending:4", "HF", "The dragon's page-wings tearing into floating pages as she lowers her crowned head, golden light in the library."),
    # --- the bad ending: the land unmade ---
    ("bad_01_white", "Everything unwritten", "The land unmade", "ending:1", "H", "Halgakos alone on an empty white page with no ground and no sky, a melted ice cream cone in her hand, her book closed, looking down."),
    ("bad_02_empty_friends", "Where are they?", "The land unmade", "ending:1", "HCR", "Halgakos turning in the white void, looking for friends; faint pale outlines of Chikuwa and Rune fading like erased pencil sketches."),
    ("bad_03_mawang_far", "Far away, watching", "The land unmade", "ending:1", "HW", "Far behind Halgakos, tiny against the white, the white-haired Mawang watches from a distance; Halgakos does not see her."),
    ("bad_04_book_shut", "The book shuts", "The land unmade", "ending:1", "H", "Halgakos shutting her green book softly, quiet and sad, white paper drifting like snow around her."),
    ("bad_05_last_sum", "The last sum", "The land unmade", "ending:1", "H", "A single gold plus sign glowing in Halgakos's hands in the white void, the only thing left on the page."),
    ("bad_06_unwritten_plaza", "An unwritten plaza", "The land unmade", "ending:1", "I", "Illya alone on her little stage in an empty plaza with faded hearts, nobody in the seats, a lonely smile."),
    # --- the true ending ---
    ("true_01_everyone", "Everyone, on the last page", "The true ending", "ending:2", "HCRIT", "Halgakos, Chikuwa, Rune, Illya and Tsubakieh standing together on a sunlit white page in warm golden light."),
    ("true_02_ice_cream", "My mint ice cream", "The true ending", "ending:2", "HC", "Halgakos finally getting a big mint ice cream cone from a smiling Chikuwa on the sunlit page, blissful."),
    ("true_03_going_home", "Going home", "The true ending", "ending:2", "H", "Halgakos stepping through a glowing door made of a book's cover toward a bright ordinary park bench, looking back with a smug wave."),
    ("true_04_volume_three_not_due", "Not due yet", "The true ending", "ending:2", "T", "Tsubakieh calmly looking up at the sky with her bat on her shoulder, checking for another book, deadpan."),
    ("true_05_heroine_everyone", "Heroine: everyone", "The true ending", "ending:2", "HI", "Illya and Halgakos sharing one page, writing on it together with a big pen, laughing."),
    ("true_06_friends_picnic", "A picnic of friends", "The true ending", "ending:2", "HCRT", "Halgakos, Chikuwa, Rune and Tsubakieh sharing bread, tea and ice cream on a picnic blanket in a sunlit meadow of pages."),
    ("true_07_bat_mended", "A mended bat", "The true ending", "ending:2", "HT", "Tsubakieh proudly showing Halgakos her mended bat tied with a ribbon, both smiling."),
    ("true_08_read_to_the_end", "Read to the end", "The true ending", "ending:2", "H", "Halgakos closing the last page of the giant book with a satisfied smile at sunset, golden letters drifting up like fireflies."),
]


DROP = {"l6_02_heart_prize", "l65_01_brake_pads", "l45_01_toll", "l35_01_scribe", "v3_03_killer", "l50_03_not_so_bad"}
P = [p for p in P if p[0] not in DROP]


def refs_for(cast):
    return [str(REFS[k]) for k in cast if k in REFS][:5]


def jobs():
    WORK.mkdir(parents=True, exist_ok=True)
    (WORK / "refs").mkdir(exist_ok=True)
    shards = [[], [], []]
    for i, (name, title, hint, rule, cast, scene) in enumerate(P):
        small = []
        for r in refs_for(cast):
            src = pathlib.Path(r)
            dst = WORK / "refs" / (src.parent.name + "_" + src.name)
            if not dst.exists():
                im = Image.open(src).convert("RGB")
                im.thumbnail((1024, 1024))
                im.save(dst)
            small.append(str(dst))
        shards[i % 3].append({"name": name, "size": "1536x1024", "refs": small,
                              "prompt": STYLE + " The reference pictures are the character sheets of those in the scene; they look exactly as on them. Scene: " + scene})
    for k, s in enumerate(shards):
        (WORK / f"jobs_{k}.json").write_text(json.dumps(s, indent=1, ensure_ascii=False), encoding="utf-8")
    print(len(P), "pictures in three shards under", WORK)


def sheets():
    found = [(n, WORK / "raw" / (n + ".png")) for n, *_ in P if (WORK / "raw" / (n + ".png")).exists()]
    for b in range(0, len(found), 20):
        batch = found[b:b + 20]
        w, h = 384, 256
        sheet = Image.new("RGB", (w * 5, h * 4), "white")
        for i, (n, path) in enumerate(batch):
            im = Image.open(path).convert("RGB").resize((w, h))
            sheet.paste(im, ((i % 5) * w, (i // 5) * h))
        target = WORK / f"sheet_{b // 20 + 1:02d}.jpg"
        sheet.save(target, quality=82)
        print(target, len(batch))


def webp():
    OUT.mkdir(parents=True, exist_ok=True)
    rows = ["# The pictures of the gallery that open as the story goes on: a row each, with tabs between: name, title, the word shown while it is locked, and its rule (see RunGallery).",
            "# The picture itself is gallery/NAME.webp. More pictures are more rows here and nothing else."]
    for name, title, hint, rule, cast, scene in P:
        src = WORK / "raw" / (name + ".png")
        if not src.exists():
            continue
        im = Image.open(src).convert("RGB")
        im.thumbnail((1280, 1280))
        im.save(OUT / (name + ".webp"), "WEBP", quality=80, method=6)
        rows.append("\t".join([name, title, "An ending" if rule.startswith("ending:") else hint, rule]))
    (OUT / "pictures.tsv").write_text("\n".join(rows) + "\n", encoding="utf-8")
    print(len(rows) - 2, "pictures")


if __name__ == "__main__":
    {"jobs": jobs, "sheets": sheets, "webp": webp}[sys.argv[1]]()

