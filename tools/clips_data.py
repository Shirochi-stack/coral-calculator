"""
The clips of the story: what each is, which model makes it, where it plays, its storyboard and its lines in four languages.
Used by tools/make_clips.py. Everyone who speaks has an existing voice (tools/make_run_voice.py): H Halgakos, C Chikuwa, R Rune,
I Illya, T Tsubakieh, M the Mawang (Halgakos sixty years on), f the Mother Tongue.
Lines: (who, second it begins on in the clip, English, Japanese, Korean, Chinese). Spoken lines only: no songs, no lyrics.
Most clips are combat; every clip is a different scene; all are 16:9 at 1920x1080.
A clip of model "magi" has a "gen" (a 30-second generation of three shots) and "cut" (from, to), in the generation's own seconds.
"""

SHEETS = {
    "H": ("opening/sheets/halgakos.png", "Halgakos"),
    "C": ("opening/sheets/chikuwa.png", "Chikuwa"),
    "R": ("opening/sheets/rune.png", "Rune"),
    "I": ("opening/sheets/illya.png", "Illya"),
    "T": ("opening/sheets/tsubakieh.png", "Tsubakieh"),
    "W": ("mother-rises/ref_halgakos_white.png", "the Mawang (Halgakos sixty years on: long white hair, dark horns, one white wing and one dark wing)"),
    "F": ("mother-rises/ref_mother.png", "the Mother Tongue, a vast elder dragon with wings of pages"),
}

STYLE = ("Hand-drawn 2D anime in the clean webtoon style of the reference sheets: crisp line art, soft cel shading, flat pastel colours, dynamic "
         "action animation, no blood, no gore, ink and paper effects only. Not 3D, not photorealistic. Characters look exactly as on their sheets. "
         "One consistent background. Family-friendly. No subtitles, no captions, no on-screen text, no logos, no watermark. No music; sound effects only.")

_ = None


def C(id, model, secs, cast, place, shots, scene, sfx, lines, **extra):
    return dict(id=id, model=model, secs=secs, cast=cast, place=place, shots=shots, scene=scene, sfx=sfx, lines=lines, **extra)


CLIPS = [
    # ===== Kling 4.0 on fal: 8 s each, about $1.88 each =====
    C("01-minus-squish", "kling", 8, "H", "Land 1: the first time a walking minus sign is met (a short comic beat).",
      ["Green page-hill. A minus sign with legs strolls up.", "She leaps and stomps its head: SQUISH, flat as a page."],
      "A green hill of book pages. A walking minus sign with tiny legs strolls toward Halgakos. She points, leaps high and lands on its head with a squish, flattening it like paper, then smirks at the viewer.",
      "a cartoon squish, small footsteps, a pop",
      [("H", 0.8, "A minus sign with legs. Land on its head Sloppy~♡", "足の生えたマイナス記号。頭を踏んじゃえ、スロッピー♡", "다리 달린 마이너스 기호. 머리를 밟아 줘, 슬로피♡", "长腿的减号。踩它的头，Sloppy♡")]),
    C("02-chikuwa-bread-barrage", "kling", 8, "HC", "Land 3: after Chikuwa is helped, at the first stone-bread golems.",
      ["Stone-bread golems rise in the village square.", "Chikuwa hurls hot loaves like cannonballs; the golems crumble."],
      "A cute village square. Three golems made of stone loaves lumber toward Chikuwa and Halgakos. Chikuwa grabs a basket and hurls hot loaves like cannonballs, each hit puffing a golem into crumbs, while Halgakos cheers behind her with a smug grin.",
      "thuds, crumbling stone, whoosh of thrown loaves",
      [("C", 1.0, "Everyone gets bread! Hot and fast!", "みんなにパンを！ 熱々を、どんどん！", "모두에게 빵을! 따끈따끈하게, 팍팍!", "人人有面包！热腾腾的，来啦！")]),
    C("03-heavy-book-throw", "kling", 8, "H", "Land 7: first time she picks up the heavy book (The Silent Archive).",
      ["Dim archive, a times sign with spikes charges.", "She hefts a huge book and slams it over its head."],
      "A dim, dusty library. A spiky times sign charges at Halgakos. She lifts a heavy book with a grunt, spins and slams it down on the sign; a burst of paper and dust.",
      "a thump, a dusty burst, paper flutter",
      [("H", 0.8, "A heavy book. Knowledge hurts Sloppy~♡", "分厚い本。知識は痛いのよ、スロッピー♡", "묵직한 책. 지식은 아프다고, 슬로피♡", "厚厚的书。知识是会疼的，Sloppy♡")]),
    C("04-rune-ink-shield", "kling", 8, "HR", "Land 9: Rune's first fight beside her (The Silent Archive, the librarian's ink).",
      ["Ink wisps lunge at Halgakos.", "Rune, sobbing, sweeps a great wall of ink across and blocks them."],
      "A huge library. Dark ink wisps lunge at Halgakos. Rune, tears streaming, sweeps her hands and a great wall of ink rises like a wave and swallows the wisps; Halgakos looks back, impressed.",
      "an ink splash, small sniffles, a rushing swish",
      [("R", 1.0, "M-my ink gets hurt, n-not you, Halgakos-nim.", "わ、私のインクが傷つくの。あ、あなたじゃなくて、ハルガコスさま。", "내, 내 잉크가 다치는 거야. 당, 당신이 아니라, 할가코스 님.", "受、受伤的是我的墨水。不、不是你，哈尔加科斯大人。")]),
    C("05-illya-hearts-arrows", "kling", 8, "HI", "Land 13/18: Heart-Flutter Plaza, Illya's hearts turn the arrows round.",
      ["Illya fires a storm of hearts.", "Halgakos dodges, flustered; her thrown arrow turns round."],
      "A pink plaza of hearts. Illya blows a storm of glowing hearts at Halgakos, who dodges and rolls, blushing, fires a pen-arrow, and the hearts spin it around so it points back at her; she ducks, outraged.",
      "heart pops, sparkle chimes, a whoosh",
      [("H", 3.0, "Careful. Her hearts turn my arrows round Sloppy~♡", "気をつけて。あの子のハートは矢を向きを変えるの、スロッピー♡", "조심해. 저 애 하트는 화살 방향을 돌려, 슬로피♡", "小心。她的爱心会把我的箭掉头，Sloppy♡")]),
    C("06-illya-final-blow", "kling", 8, "HI", "Land 18: the finishing blow on Illya.",
      ["Illya's heart-shield cracks.", "Halgakos's giant pen strikes through; hearts scatter."],
      "Illya raises a glowing heart-shaped shield. Halgakos takes a giant pen like a lance, jumps, and thrusts it through the shield; hearts shatter into petals as Illya falls back, stunned, sparkling.",
      "glass-like crack, petals rustling, a soft thud",
      [("I", 4.5, "Oh my... Nobody has ever said no to me before♡", "あら……私に「いや」って言った人、初めて♡", "어머… 저한테 싫다고 한 사람은 처음이에요♡", "哎呀……从来没有人对我说过不呢♡")]),
    C("07-bat-duel", "kling", 8, "HT", "Land 24: the boss fight with Tsubakieh starts (the mountain top).",
      ["Tsubakieh swings; the bridge breaks.", "Halgakos leaps over the swing and lands on her bat."],
      "A mountain top over a ravine. Tsubakieh swings her bat in a huge arc, deadpan, cutting the bridge. Halgakos jumps over the swing, lands lightly on the bat itself and points at her with a smirk.",
      "a heavy bat whoosh, a rope snap, wind",
      [("T", 0.8, "Found you. Normally. I will bat you across.", "見つけた。普通に。打って向こうへ飛ばします。", "찾았다. 평범하게. 쳐서 건네 드릴게요.", "找到你了。很正常。我把你打过去。"),
       ("H", 5.5, "Jump on her after she swings Sloppy~♡", "振ったあとに飛び乗るのよ、スロッピー♡", "휘두른 다음에 뛰어올라, 슬로피♡", "等她挥完棒就跳上去，Sloppy♡")]),
    C("08-bat-breaks", "kling", 8, "HT", "Land 24: the last hit; Normal No. 2 breaks.",
      ["Halgakos's sword meets the bat.", "The bat snaps in two; Tsubakieh stares at the halves."],
      "Halgakos clashes her sword with Tsubakieh's bat in a burst of sparks, the bat snaps in two with a crack, and Tsubakieh stares at the halves, stunned, then bows slightly.",
      "a sword clash, a wooden snap, wind dying",
      [("T", 4.0, "Normal No. 2 has broken. Normally. I'm sorry.", "ノーマル二号が折れました。普通に。ごめんなさい。", "노멀 2호가 부러졌어요. 평범하게. 미안해요.", "二号普通棒断了。很正常。对不起。")]),
    C("09-ink-gun-slimes", "kling", 8, "H", "Land 61: the Slime Margins (Volume Two), the ink gun against slimes.",
      ["Slimes pour over a hill.", "She fires the ink gun: ink bursts splat them away."],
      "Hills of green slime. A pack of slimes bounces toward Halgakos. She grins and fires a toy-like ink gun in rapid bursts; ink splatters pop the slimes into harmless puddles; she stands in a spray of ink, delighted.",
      "ink pops, wet splats, a bouncing sound",
      [("H", 0.8, "An ink gun. Oh, this is going to get messy Sloppy~♡", "インクガン。あら、これは散らかるわよ、スロッピー♡", "잉크 총. 어머, 이거 엉망이 되겠는데, 슬로피♡", "墨水枪。哎呀，这下要弄得一团糟了，Sloppy♡")]),
    C("10-katana-vs-knives", "kling", 8, "H", "Land 60: the Straight-Ahead Party's arena, katana against thrown knives.",
      ["Knives fly at her.", "She draws a katana and slices every one of them out of the air."],
      "A wide white-and-gold arena. A volley of throwing knives flies at Halgakos. She draws a katana and slices each one out of the air in a blur, sparks flying, and then points it forward with a smirk.",
      "blade swishes, metal pings, a sword sheath",
      [("H", 5.0, "A sword. Now it's a fair fight Sloppy~♡", "剣。これでやっと対等ね、スロッピー♡", "검이다. 이제야 공평한 싸움이네, 슬로피♡", "剑。这下才是公平的战斗，Sloppy♡")]),
    C("11-chikuwa-rune-team", "kling", 8, "CR", "Land 70: against King Slime, Chikuwa and Rune together.",
      ["King Slime lunges.", "Rune draws a chain of ink; Chikuwa smashes through with a rolling pin."],
      "A giant green slime king lunges. Rune, weeping, draws lines of ink that bind it, and Chikuwa leaps in with a huge rolling pin and smashes a chunk off it; the pair high-five.",
      "wet slap, ink swish, a heavy thwack, a small cheer",
      [("R", 0.8, "I'll d-draw the lines... waaah...", "わ、私が線を引くよ……うわぁん……", "내, 내가 선을 그을게… 으앙…", "我、我来画线……呜哇……"), ("C", 4.8, "I'll bake him flat!", "ぺったんこに焼いちゃうよ！", "납작하게 구워 줄게요!", "我把它烤扁！")]),
    C("12-pen-vs-dividing-sign", "kling", 8, "H", "Land 4: the first divide sign (a hopping enemy), a pen duel.",
      ["A divide sign hops in bounces.", "She plants the pen like a vaulting pole and drives it into it from above."],
      "A divide sign hops in big bounces across a green hill. Halgakos plants a giant pen like a vaulting pole, springs over it and drives the pen down onto it; a burst of dots flies out.",
      "boing sounds, a pop, a thump",
      [("H", 1.0, "A pen. Mightier than the sword Sloppy~♡", "ペンは剣よりも強し、スロッピー♡", "펜은 칼보다 강하다, 슬로피♡", "笔比剑更强，Sloppy♡")]),
    # ===== Seedance 2.5 on BytePlus: 8 s each, about $4.60 each =====
    C("13-mawang-clash", "seedance", 8, "HW", "Land 25: the Mawang and Halgakos clash over the sea of ink.",
      ["The Mawang dives; Halgakos blocks with her book.", "A shockwave of pages blows outward."],
      "A grey sky of drifting pages over a sea of ink. The Mawang, long white hair, dark horns, one white and one dark wing, dives hand-first at Halgakos, who blocks with an open green book; a shockwave of pages blows outward. They speak, one at a time.",
      "a shockwave boom, wingbeats, a page storm",
      [("M", 0.5, "Friends are a burden.", "友達は重荷。", "친구는 짐이야.", "朋友是负担。"), ("H", 4.0, "A burden shared weighs half Sloppy~♡", "分け合えば半分の重さよ、スロッピー♡", "나누면 절반만 무거워, 슬로피♡", "分担就只有一半重，Sloppy♡")]),
    C("14-mother-tongue-storm", "seedance", 8, "HF", "The Dragon's Library: the Mother Tongue's page storm, the first attack.",
      ["The dragon spreads her wings.", "A storm of blank pages blasts at Halgakos, who raises her book."],
      "A vast ruined library. The Mother Tongue, a vast ivory dragon with wings of pages, spreads her wings and blasts a storm of blank pages down at Halgakos, who raises her open book as a shield, skidding back. They speak, one at a time.",
      "a deep rumble, a roaring page storm, wind",
      [("F", 0.4, "Every tongue will sound like mine.", "すべての言葉は、私の声になるのです。", "모든 말은 내 목소리처럼 들리게 될 것이다.", "所有语言都将听起来像我的。"), ("H", 4.2, "Just a mother who won't let go Sloppy~♡", "ただ手を離せない母親ね、スロッピー♡", "그냥 손을 못 놓는 엄마일 뿐이야, 슬로피♡", "只是不肯放手的母亲，Sloppy♡")]),
    C("15-volume-two-home-run", "seedance", 8, "HT", "Land 31: Volume Two, Tsubakieh hits the falling book.",
      ["A giant book falls.", "Tsubakieh's home run sends it spinning; it smacks into Halgakos."],
      "A green field. A giant book falls from the sky. Tsubakieh steps up and swings, a perfect home run; the book spins away and smacks into Halgakos, who vanishes into its pages with a yelp. They speak, one at a time.",
      "a huge bat crack, a whoosh, a thump",
      [("T", 0.6, "I will hit it for you. Normally.", "私が打ちます。普通に。", "제가 쳐 드릴게요. 평범하게.", "我来帮你打。很正常。"), ("H", 4.4, "You hit it INTO me Sloppy~♡", "私に向かって打ったでしょ、スロッピー♡", "나한테 쳤잖아, 슬로피♡", "你把它打到我身上了，Sloppy♡")]),
    C("16-mawang-ally-rescue", "seedance", 8, "HW", "Land 81 (Volume Three): the Mawang saves Halgakos, a team-up.",
      ["A dark wave of unwritten ink rushes at Halgakos.", "The Mawang swoops in and cuts it with her wing."],
      "A broken white land. A wave of dark ink rushes at Halgakos. The white-haired Mawang swoops in, cuts it apart with one dark wing and lands beside her, back to back. They speak, one at a time.",
      "a whooshing ink wave, a wing cut, wind",
      [("M", 1.0, "This time I walk with you.", "今度は、私も一緒に行く。", "이번엔 나도 같이 걸어가.", "这次我和你一起走。"), ("H", 4.6, "Back to back. Don't slow me Sloppy~♡", "背中合わせ。遅れないでね、スロッピー♡", "등 맞대자. 늦지 마, 슬로피♡", "背靠背。别拖慢我，Sloppy♡")]),
    # ===== MAGI-2 on Higgsfield: six 30-second generations, each cut into three clips of about 10 s =====
    # G1: the Last Page, the Zero
    C("17-zero-rises", "magi", 10, "HI", "Land 30: the final boss, the Zero appears.", ["Illya writes 'none'.", "A giant zero rises."],
      "The last page, a vast white stage. Illya writes with a pen, triumphant, and a giant glowing zero rises up from the page behind her.", "a pen scratching, a rising hum",
      [("I", 0.8, "Heroine: Illya. Supporting cast: none♡", "ヒロイン：イリヤ。脇役：なし♡", "주인공: 일리야. 조연: 없음♡", "女主角：伊莉雅。配角：无♡")], gen="G1", cut=(0, 10)),
    C("18-zero-book-beam", "magi", 10, "HC", "Land 30: Halgakos fires a beam through the Zero.", ["Halgakos aims her open book.", "A beam of light blasts the Zero's edge; it cracks."],
      "Halgakos opens a green book and fires a blazing beam of golden letters at the giant zero; the zero's edge cracks and sparks. Chikuwa cheers beside her.", "a roaring beam, a crack",
      [("H", 1.0, "Nothing thrown gets through it. So I'll read it to death Sloppy~♡", "投げたものは通らない。なら、読み潰してあげる、スロッピー♡", "던진 건 안 통해. 그럼 읽어서 부숴 줄게, 슬로피♡", "扔的东西打不穿。那我就把它读碎，Sloppy♡")], gen="G1", cut=(10, 20)),
    C("19-zero-shatters", "magi", 10, "HIT", "Land 30: the Zero shatters.", ["The Zero cracks.", "It bursts into a rain of letters; Tsubakieh nods."],
      "The giant zero cracks along its edge and bursts into a rain of glowing letters as Halgakos lands lightly, Illya falls to her knees, and Tsubakieh nods deadpan.", "a deep crack, a burst, soft chimes",
      [("T", 6.0, "Cast: everyone. That seems normal.", "配役：みんな。普通ですね。", "출연: 모두. 평범하네요.", "角色：所有人。这很正常。")], gen="G1", cut=(20, 30)),
    # G2: the Mawang
    C("20-mawang-intro", "magi", 10, "HW", "Land 80: the Mawang's boss intro.", ["The Mawang hovers.", "She raises a hand and the ground splits."],
      "A dark plain under a cracked sky. The Mawang, white-haired, hovers with her wings out, cold, and raises a hand; the ground splits and ink flows. Halgakos stands facing her.", "a low wind, a ground crack",
      [("M", 0.8, "A demon king needs no one. Come no closer.", "魔王は誰も必要としない。近づかないで。", "마왕은 아무도 필요 없어. 다가오지 마.", "魔王不需要任何人。不要靠近。")], gen="G2", cut=(0, 10)),
    C("21-mawang-wing-clash", "magi", 10, "HW", "Land 80: Halgakos meets the Mawang's wing.", ["Her dark wing sweeps.", "Halgakos blocks with the book and answers."],
      "The Mawang sweeps her dark wing; Halgakos blocks with the open book, and with her white wing opens a gust back. Pages blast outward as they clash, equal.", "a wing whoosh, a clash",
      [("H", 3.0, "That's her line, not yours. I'm bringing you home Sloppy~♡", "それは彼女のセリフよ。連れて帰りに来たの、スロッピー♡", "그건 걔 대사야. 널 집에 데려가러 왔어, 슬로피♡", "那是她的台词。我是来带你回家的，Sloppy♡")], gen="G2", cut=(10, 20)),
    C("22-mawang-finish", "magi", 10, "HCRW", "Land 80: the final blow and the Mawang remembers.", ["Chikuwa and Rune cheer.", "A last strike; the mask cracks and the Mawang remembers."],
      "A last blow from Halgakos's book cracks the Mawang's cold mask; Chikuwa waves a loaf, Rune offers her ink, and the Mawang's eyes soften; golden light rises.", "a crack of light, a soft chime",
      [("M", 5.0, "...What took you so long, horn-unnie?", "……どうして遅かったの、ツノおねえちゃん。", "…왜 이렇게 늦었어, 뿔 언니?", "……你怎么这么久才来，长角姐姐？")], gen="G2", cut=(20, 30)),
    # G3: the Mother Tongue
    C("23-mother-tongue-rises", "magi", 10, "HF", "The Dragon's Library: the Mother Tongue's second phase rises.", ["The pages burst.", "The dragon rises over Halgakos."],
      "The Mother Tongue rises, wings of pages unfolding across the sky over a ruined library; Halgakos looks up, steadying her book.", "a rumble, pages whirling",
      [("H", 1.0, "So you're the great evil. The Mother Tongue Sloppy~♡", "あなたが大悪ね。母なる言葉、スロッピー♡", "네가 대악이구나. 어머니의 언어, 슬로피♡", "原来你就是大恶。母语，Sloppy♡")], gen="G3", cut=(0, 10)),
    C("24-mother-tongue-ink-fire", "magi", 10, "HF", "The Dragon's Library: ink fire against the Mother Tongue.", ["The dragon breathes pages.", "Halgakos answers with a torrent of ink fire."],
      "The dragon breathes a blast of pages; Halgakos answers with a torrent of ink fire from her book; the two clash in a fireball of ink and paper.", "a roaring fire, a boom",
      [("F", 0.8, "Be quiet and be mine.", "静かに、私のものになりなさい。", "조용히 하고 내 것이 되어라.", "安静下来，成为我的吧。")], gen="G3", cut=(10, 20)),
    C("25-mother-tongue-team-finish", "magi", 10, "HCRIT", "The Dragon's Library: the team's finishing blow.", ["All five strike together.", "The dragon's wings tear into blank pages."],
      "All five strike together: Chikuwa's bread, Rune's ink, Illya's hearts, Tsubakieh's bat and Halgakos's book beam; the Mother Tongue's wings tear into floating pages and she lowers her head.", "a combined boom, a long chime",
      [("H", 1.0, "Everyone, together Sloppy~♡", "みんな、一緒に、スロッピー♡", "모두 같이, 슬로피♡", "大家一起，Sloppy♡")], gen="G3", cut=(20, 30)),
    # G4: King Slime
    C("26-king-slime-intro", "magi", 10, "HRC", "Land 70: King Slime lands.", ["The ground wobbles.", "King Slime drops in; Rune quakes."],
      "The slime margins. The ground wobbles and a giant slime king drops from the sky with a splash; Rune quakes behind Halgakos.", "a huge splash, wobble sounds",
      [("R", 2.0, "Uu... h-he's so big... sniff...", "うぅ……お、大きい……ぐすっ……", "으으… 너, 너무 커… 훌쩍…", "呜……好、好大……呜……")], gen="G4", cut=(0, 10)),
    C("27-king-slime-three-cuts", "magi", 10, "H", "Land 70: three cuts on King Slime.", ["She slices once, twice, three times in the air.", "King Slime splits in chunks."],
      "Halgakos leaps and slashes King Slime three times in the air with a sword, each cut flashing; he splits into wobbling chunks.", "blade swishes, wet splits",
      [("H", 1.0, "King Slime. We chunk him. Three cuts Sloppy~♡", "キングスライム。三回で切り分ける、スロッピー♡", "킹 슬라임. 세 번 베면 끝이야, 슬로피♡", "史莱姆王。三刀切块，Sloppy♡")], gen="G4", cut=(10, 20)),
    C("28-king-slime-bread", "magi", 10, "HC", "Land 70: Chikuwa's bread cannon finishes him (comic seasoning).", ["Chikuwa fires bread.", "The chunks turn into bread rolls."],
      "Chikuwa fires a bread cannon at the slime chunks and each one turns into a warm bread roll that rains down; Halgakos catches one and bites it.", "a cannon thump, pops, cheer",
      [("C", 1.5, "They all say bread to me!", "全部、パンって読めるよ！", "전부 빵이라고 읽혀요!", "它们在我看来都是面包！")], gen="G4", cut=(20, 30)),
    # G5: Volume Three, unwriting
    C("29-volume-three-unwriting", "magi", 10, "HW", "Land 81 (Volume Three): the unwriting wave.", ["The far end of the page vanishes.", "An unwriting wave rolls toward them."],
      "A broken white land. The far edge of the page vanishes and a dark wave of unwriting ink rolls toward Halgakos and the Mawang.", "a hollow rumble, wind",
      [("H", 1.0, "Something is unwriting it from the far end Sloppy~♡", "遠くの端から、何かが消している、スロッピー♡", "먼 끝에서 뭔가 이 책을 지우고 있어, 슬로피♡", "有什么东西在从远处把它擦掉，Sloppy♡")], gen="G5", cut=(0, 10)),
    C("30-mawang-halgakos-duo", "magi", 10, "HW", "Land 81: Halgakos and the Mawang fight together.", ["Their wings open as one.", "A beam of white and black light cuts the wave."],
      "Halgakos and the Mawang stand back to back, their white and dark wings open as one, and fire a spiral beam of white and black light that cuts the dark wave in two.", "a roaring beam, an ink wave",
      [("M", 1.0, "I know that kind of quiet.", "あの静けさなら知ってる。", "그런 고요함은 나도 알아.", "那种安静我很熟悉。")], gen="G5", cut=(10, 20)),
    C("31-ice-cream-revenge", "magi", 10, "H", "Land 90: a comic beat, the sea of ink melts her ice cream.", ["A wave splashes her cone.", "She glares and sends a revenge beam at the sea."],
      "At the edge of a shore of ink. An ink wave splashes onto Halgakos's mint ice cream cone; she freezes, glares, and blasts a revenge beam from her book at the sea with a pout.", "a splash, a beam, a pout",
      [("H", 4.0, "That was my mint ice cream. You'll pay Sloppy~♡", "それは私のミントアイスよ。覚悟しなさい、スロッピー♡", "그건 내 민트 아이스크림이었어. 각오해, 슬로피♡", "那是我的薄荷冰淇淋。你要付出代价，Sloppy♡")], gen="G5", cut=(20, 30)),
    # G6: the last stand
    C("32-last-stand-gather", "magi", 10, "HCRIT", "Land 100: the last land, everyone gathers.", ["Everyone appears one by one.", "They line up behind Halgakos."],
      "A vast dark library of ink. Chikuwa, Rune, Illya and Tsubakieh appear one by one and line up behind Halgakos, who raises her book.", "footsteps, a rising hum",
      [("H", 1.0, "The last land. Everyone with me Sloppy~♡", "最後の地。みんな、私と一緒に、スロッピー♡", "마지막 땅이야. 모두 나와 함께, 슬로피♡", "最后的地方。大家跟我来，Sloppy♡")], gen="G6", cut=(0, 10)),
    C("33-last-stand-specials", "magi", 10, "HCRIT", "Land 100: all five use their special attacks.", ["Hearts, bread, ink, bat, book beam in one frame.", "A wave of ink breaks."],
      "Illya's hearts, Chikuwa's bread, Rune's ink, Tsubakieh's bat swing and Halgakos's book beam strike one after another and a giant ink wave breaks.", "a chain of booms, chimes",
      [("T", 2.0, "Normal No. 3. Swinging.", "ノーマル三号。振ります。", "노멀 3호. 휘두릅니다.", "三号普通棒。挥棒。")], gen="G6", cut=(10, 20)),
    C("34-last-stand-dawn", "magi", 10, "HCRIT", "Land 100: the last wave breaks and dawn rises on the page.", ["The wave breaks.", "Dawn light on the page; Halgakos smirks."],
      "The last giant wave of ink breaks into light; dawn rises over the page; Halgakos smirks at the viewer as her friends cheer.", "a long soft chime, a breeze, a cheer",
      [("H", 3.0, "Read to the end. Not bad Sloppy~♡", "最後まで読んだ。悪くないでしょ、スロッピー♡", "끝까지 읽었어. 나쁘지 않지, 슬로피♡", "读到了最后。不错吧，Sloppy♡")], gen="G6", cut=(20, 30)),
]

# What each MAGI-2 generation is: its prompt scene, in three shots of ten seconds each (the clips above are cut from it).
GENS = {
    "G1": "The last page of a giant book, a vast white stage. Shot 1 (0-10 s): Illya writes with a pen, triumphant, and a giant glowing zero rises up behind her. Shot 2 (10-20 s): Halgakos opens a green book and blasts a beam of golden letters at the zero; Chikuwa cheers; its edge cracks. Shot 3 (20-30 s): the zero cracks and bursts into a rain of letters; Illya falls to her knees and Tsubakieh nods.",
    "G2": "A dark plain under a cracked sky. Shot 1 (0-10 s): the white-haired Mawang hovers with her wings out and the ground splits. Shot 2 (10-20 s): her dark wing sweeps, Halgakos blocks with the open book and answers with her white wing; pages blast outward. Shot 3 (20-30 s): a last strike cracks her cold mask; Chikuwa waves bread and Rune offers ink; the Mawang's eyes soften in golden light.",
    "G3": "A vast ruined library. Shot 1 (0-10 s): the Mother Tongue, ivory dragon with wings of pages, rises over Halgakos. Shot 2 (10-20 s): her breath of pages against Halgakos's torrent of ink fire. Shot 3 (20-30 s): Halgakos, Chikuwa, Rune, Illya and Tsubakieh strike together; the dragon's wings tear into floating pages.",
    "G4": "The slime margins, hills of green slime. Shot 1 (0-10 s): King Slime drops from the sky, Rune quakes. Shot 2 (10-20 s): Halgakos slashes him three times with a sword and he splits into chunks. Shot 3 (20-30 s): Chikuwa's bread cannon turns the chunks into bread rolls.",
    "G5": "Shot 1 (0-10 s): a broken white land; a dark unwriting wave rolls toward Halgakos and the Mawang. Shot 2 (10-20 s): back to back with open wings they cut the wave with a white and black beam. Shot 3 (20-30 s): a shore of ink; a wave splashes Halgakos's mint ice cream and she blasts the sea with a pout.",
    "G6": "A vast dark library of ink. Shot 1 (0-10 s): Chikuwa, Rune, Illya and Tsubakieh appear one by one behind Halgakos. Shot 2 (10-20 s): hearts, bread, ink, a bat swing and a book beam strike one after another. Shot 3 (20-30 s): the last ink wave breaks into light; dawn over the page; Halgakos smirks.",
}

