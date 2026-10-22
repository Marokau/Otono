/* Personal all-class nostalgia training guide. Map and mob IDs are verified
 * against this server's WZ/handbook data. PQ limits use local event scripts.
 * Reference guidance with confirmed GM-only destination teleports.
 */
var page = "home", stage = 0, buildLevel = 10;
var ludiMob = 0, pendingMap = 0;
var selectedTraining = -1;
var gearBand = 0, gearPage = 0;
// Gear targets transcribed from the owner-supplied reference, not local item guarantees.
var gearBuilds = [
    {min: 10, max: 30, label: "10~30", rows: [
        ["Hat","Bamboo Hat / Green Bandana"],
        ["Eye acc","/"],
        ["Face acc","/"],
        ["Earrings","/"],
        ["Pendant","/"],
        ["Belt","/"],
        ["Ring 1","/"],
        ["Ring 2","/"],
        ["Ring 3","/"],
        ["Ring 4","/"],
        ["Top(overall)","10+ DEX Bathrobe / 10+ LUK Bathrobe"],
        ["Bottom","/"],
        ["Throwing Star","Subi / Icicle / Tobi / Steely / Ilbi"],
        ["Weapon","Garnier / Steel Titans"],
        ["Gloves","/"],
        ["Cape","/"],
        ["Shoes","Gomushin Shoe / Whitebottom Boots / Black Snowshoes / Squishy Shoes (KPQ)"],
        ["Medal","/"]
    ]},
    {min: 35, max: 50, label: "35~50", rows: [
        ["Hat","Bamboo Hat / Green Bandana"],
        ["Eye acc","N/A or Broken Glasses"],
        ["Face acc","/"],
        ["Earrings","4~10 LUK Single earring"],
        ["Pendant","Spiegelmann's Necklace"],
        ["Belt","/"],
        ["Ring 1","/"],
        ["Ring 2","/"],
        ["Ring 3","/"],
        ["Ring 4","/"],
        ["Top(overall)","10+ LUK bathrobe"],
        ["Bottom","/"],
        ["Throwing Star","Subi / Icicle / Tobi / Steely / Ilbi"],
        ["Weapon","Maple claw / Maple Kandayo"],
        ["Gloves","6~10 Atk Work Glove | Green Mitten"],
        ["Cape","/"],
        ["Shoes","Whitebottom Boots / Black Snowshoes / Squishy Shoes (KPQ)"],
        ["Medal","/"]
    ]},
    {min: 55, max: 70, label: "55~70", rows: [
        ["Hat","Bamboo Hat / Green Bandana / Zakum Helmet"],
        ["Eye acc","N/A or Broken Glasses"],
        ["Face acc","~5 DEX Rat Mouth / 5+ DEX Maple Leaf / 5+ LUK Maple Leaf (self scroll with DEX later on)"],
        ["Earrings","6~10 LUK Earrings"],
        ["Pendant","Spiegelmann's Necklace / Chaos Spiegelmann's Necklace"],
        ["Belt","/"],
        ["Ring 1","Moonstone | Star Gem Wedding Ring"],
        ["Ring 2","/"],
        ["Ring 3","/"],
        ["Ring 4","/"],
        ["Top(overall)","10+ LUK bathrobe"],
        ["Bottom","/"],
        ["Throwing Star","Steely / Ilbi"],
        ["Weapon","Maple Kandayo / Shinobi Bracer / Maple Skanda"],
        ["Gloves","6~10 Atk Work Glove | Green Mitten"],
        ["Cape","8~15 LUK Cape"],
        ["Shoes","Whitebottom Boots / Black Snowshoes / Squishy Shoes (KPQ)"],
        ["Medal","/"]
    ]},
    {min: 70, max: 100, label: "70~100", rows: [
        ["Hat","Lord Pirate Hat / ~30+ DEX Zakum Helmet / ~30+ DEX Scarlion or Targa Helmet"],
        ["Eye acc","Broken Glasses / 2 DEX Spectrum Goggles"],
        ["Face acc","~5 DEX Rat Mouth / 5+ DEX Maple Leaf / 5+ LUK Maple Leaf (self scroll with DEX later on)"],
        ["Earrings","6~10 LUK Earrings / 10 DEX Earrings"],
        ["Pendant","Spiegelmann's Necklace / Chaos Spiegelmann's Necklace / Deputy Star"],
        ["Belt","4 DEX White Belt"],
        ["Ring 1","Moonstone | Star Gem Wedding Ring"],
        ["Ring 2","/"],
        ["Ring 3","/"],
        ["Ring 4","/"],
        ["Top(overall)","10+ LUK bathrobe / 10 DEX 16+ LUK Brown Studded Top or Light Mantis | Blue Mystique"],
        ["Bottom","16~20 DEX / 6~9 LUK Pirate Pants or Pirate Skirt"],
        ["Throwing Star","Steely / Ilbi / Hwabi"],
        ["Weapon","Maple Skanda / Red Craven"],
        ["Gloves","10~12 Atk Thief Glove | Work Glove | Green Mitten"],
        ["Cape","8~15 LUK Cape / 10+ DEX Cape"],
        ["Shoes","Black Snowshoes / Squishy Shoes (KPQ) / Yellow Snowshoes / ~10 DEX Dark Moss Boots or Red Mystique Shoes or Red | Dark Pirate Boots"],
        ["Medal","Veteran Medal"]
    ]},
    {min: 100, max: 135, label: "100~135", rows: [
        ["Hat","~30+ DEX Zakum Helmet / ~30+ DEX Scarlion or Targa Helmet"],
        ["Eye acc","Broken Glasses / 2 DEX Spectrum Goggles"],
        ["Face acc","~5 DEX Rat Mouth / 5+ DEX Maple Leaf / 5+ LUK Maple Leaf (self scroll with DEX later on)"],
        ["Earrings","10 DEX Earrings / 10 DEX 4+ LUK Ifia Earrings / 12+ DEX 2+ LUK Glittering Altaire Earrings"],
        ["Pendant","Deputy Star / Horntail Pendant"],
        ["Belt","4 DEX White Belt"],
        ["Ring 1","Moonstone | Star Gem Wedding Ring"],
        ["Ring 2","Ephenia Ring"],
        ["Ring 3","/"],
        ["Ring 4","/"],
        ["Top(overall)","10 DEX 16+ LUK Brown Studded Top or Light Mantis | Blue Mystique"],
        ["Bottom","16~20 DEX / 6~9 LUK Pirate Pants or Pirate Skirt"],
        ["Throwing Star","Hwabi"],
        ["Weapon","71~76 Atk Red Craven"],
        ["Gloves","10~12 Atk Thief Glove | Work Glove | Green Mitten"],
        ["Cape","3 Atk 6~10 DEX Blackfist Cape or Pink Gaia Cape"],
        ["Shoes","~10 DEX Dark Moss Boots or Red Mystique Shoes or Red | Dark Pirate Boots"],
        ["Medal","Master Adventurer Medal / Ellin Forest Medal"]
    ]},
    {min: 135, max: 150, label: "135~150", rows: [
        ["Hat","~30+ DEX Zakum Helmet / ~30+ DEX Scarlion or Targa Helmet"],
        ["Eye acc","Broken Glasses / 2 DEX Spectrum Goggles"],
        ["Face acc","~5 DEX Rat Mouth / 5+ DEX Maple Leaf / 5+ LUK Maple Leaf (self scroll with DEX later on)"],
        ["Earrings","10+ DEX Earrings / 10+ DEX 4+ LUK Ifia Earrings / 12+ DEX 2+ LUK Element Pierce or Glittering Altaire Earrings"],
        ["Pendant","Deputy Star / Horntail Pendant / Mark of Naricain"],
        ["Belt","4+ DEX White / 9~11 DEX Black Belt"],
        ["Ring 1","Moonstone | Star Gem Wedding Ring"],
        ["Ring 2","Ephenia Ring"],
        ["Ring 3","3 DEX 3 LUK Almighty Ring"],
        ["Ring 4","/"],
        ["Top(overall)","10 DEX 16+ LUK Brown Studded Top or Light Mantis | Blue Mystique"],
        ["Bottom","16~20 DEX / 6~9 LUK Pirate Pants or Pirate Skirt"],
        ["Throwing Star","Hwabi"],
        ["Weapon","71~76 Atk Red Craven"],
        ["Gloves","10~12 Atk Thief Glove | Work Glove | Green Mitten"],
        ["Cape","3 Atk 6~10 DEX Blackfist Cape or Pink Gaia Cape"],
        ["Shoes","~10 DEX Dark Moss Boots or Red Mystique Shoes or Red | Dark Pirate Boots / 6+ Atk Facestompers"],
        ["Medal","Master Adventurer Medal / Ellin Forest Medal"]
    ]},
    {min: 150, max: 180, label: "150~180", rows: [
        ["Hat","35+ DEX Zakum Helmet / Scarlion or Targa Helmet"],
        ["Eye acc","Broken Glasses / 2 DEX Spectrum Goggles"],
        ["Face acc","5+ DEX Rat Mouth / 5~10+ DEX Maple Leaf / 5+ LUK Maple Leaf (self scroll with 5+ DEX later on)"],
        ["Earrings","10+ DEX Earrings / 10+ DEX 4+ LUK Ifia Earrings / 12+ DEX 2+ LUK Element Pierce or Glittering Altaire Earrings"],
        ["Pendant","Horntail Pendant / 7~8 Atk Mark of Naricain"],
        ["Belt","9~11 DEX Black Belt"],
        ["Ring 1","Moonstone | Star Gem Wedding Ring"],
        ["Ring 2","Ephenia Ring"],
        ["Ring 3","7~8+ DEX 7~8 LUK Almighty Ring"],
        ["Ring 4","/"],
        ["Top(overall)","10 DEX 16+ LUK Brown Studded Top or Light Mantis | Blue Mystique"],
        ["Bottom","20 DEX / 8~9 LUK Pirate Pants or Pirate Skirt"],
        ["Throwing Star","Hwabi / Crystal Ilbi"],
        ["Weapon","~76 Atk Red Craven"],
        ["Gloves","15+ Atk Storm Caster Gloves | Brown Work Glove | Green Mitten"],
        ["Cape","8+ Atk Blackfist Cape or Pink Gaia Cape"],
        ["Shoes","6+ Atk Facestompers | Red Sock | Von Leon Boots"],
        ["Medal","Master Adventurer Medal / Ellin Forest Medal / Master or Legendary Collector Medal"]
    ]},
    {min: 180, max: 200, label: "180~200+", rows: [
        ["Hat","35+ DEX Zakum Helmet / Scarlion or Targa Helmet"],
        ["Eye acc","Broken Glasses / 2+ DEX Spectrum Goggles / Ninja Toad Headband"],
        ["Face acc","5+ DEX Rat Mouth / 5~10+ DEX Maple Leaf / 5+ LUK Maple Leaf (self scroll with 5+ DEX later on)"],
        ["Earrings","10+ DEX 4+ LUK Ifia Earrings / 12+ DEX 2+ LUK Element Pierce or Glittering Altaire Earrings"],
        ["Pendant","Horntail Pendant / 8 Atk Mark of Naricain"],
        ["Belt","9~11 DEX Black Belt"],
        ["Ring 1","Moonstone | Star Gem Wedding Ring"],
        ["Ring 2","Ephenia Ring"],
        ["Ring 3","7~8+ DEX 7~8 LUK Almighty Ring"],
        ["Ring 4","Ifia Ring"],
        ["Top(overall)","10 DEX 18+ LUK Brown Studded Top or Light Mantis | Blue Mystique"],
        ["Bottom","20 DEX / 8~9 LUK Pirate Pants or Pirate Skirt"],
        ["Throwing Star","Hwabi / Crystal Ilbi / Balanced Fury"],
        ["Weapon","76+ Atk Red Craven or Perfect Atk Red Craven"],
        ["Gloves","16+ Atk Storm Caster Gloves | Brown Work Glove | Green Mitten"],
        ["Cape","10+ Atk Blackfist Cape or Pink Gaia Cape"],
        ["Shoes","7+ Atk Facestompers | Red Sock | Von Leon Boots"],
        ["Medal","Master Adventurer Medal / Ellin Forest Medal / Legendary Collector Medal / Legendary Thief Medal"]
    ]}
];
var skills = {
    nimble: [4000000, "Nimble Body"], eyes: [4000001, "Keen Eyes"],
    disorder: [4001002, "Disorder"], dark: [4001003, "Dark Sight"], lucky: [4001344, "Lucky Seven"],
    mastery: [4100000, "Claw Mastery"], critical: [4100001, "Critical Throw"],
    endure: [4100002, "Endure"], booster: [4101003, "Claw Booster"], haste: [4101004, "Haste"], drain: [4101005, "Drain"],
    alchemist: [4110000, "Alchemist"], meso: [4111001, "Meso Up"], partner: [4111002, "Shadow Partner"],
    web: [4111003, "Shadow Web"], shadowMeso: [4111004, "Shadow Meso"], avenger: [4111005, "Avenger"], jump: [4111006, "Flash Jump"],
    warrior: [4121000, "Maple Warrior"], shifter: [4120002, "Shadow Shifter"], taunt: [4121003, "Taunt"],
    ambush: [4121004, "Ninja Ambush"], venom: [4120005, "Venomous Star"], stars: [4121006, "Shadow Stars"],
    triple: [4121007, "Triple Throw"], storm: [4121008, "Ninja Storm"], will: [4121009, "Hero's Will"]
};
// Ordered cumulative targets. Spending the legal per-level budget corrects the source's
// four-SP level 60 and two-SP level 70 typo, without changing its final Assassin build.
var builds = [
    {name: "Rogue", job: 400, start: 10, end: 30, initial: 1, path: [
        ["lucky",1],["nimble",3],["eyes",8],["lucky",20],["disorder",3],["dark",20],["nimble",10]]},
    {name: "Assassin", job: 410, start: 30, end: 70, initial: 1, path: [
        ["mastery",3],["critical",30],["mastery",5],["booster",6],["haste",20],
        ["mastery",20],["booster",20],["nimble",20],["endure",20],["drain",1]]},
    {name: "Hermit", job: 411, start: 70, end: 120, initial: 1, path: [
        ["avenger",1],["partner",30],["avenger",5],["jump",20],["avenger",30],
        ["alchemist",20],["web",20],["meso",20],["shadowMeso",1],["drain",11]]},
    {name: "Night Lord", job: 412, start: 120, end: 200, initial: 3, path: [
        ["stars",1],["triple",30],["warrior",10],["shifter",30],["stars",30],
        ["will",5],["storm",30],["venom",30],["taunt",30],["warrior",20],["ambush",30]]}
];
// min, max, title, map (0 = activity), explanation. This is a nostalgia route,
// not an EXP/hour ranking. Mob levels and locations use this server's data.
var training = [
    [1,20,"Beginner quests",0,"Complete Maple Island and early Victoria Island quests."],
    [1,20,"Blue Snail (Lv 2) - Henesys Hunting Ground I",104040000,"Victoria Road: Henesys Hunting Ground I."],
    [1,20,"Shroom (Lv 2) - Henesys Hunting Ground I",104040000,"Victoria Road: Henesys Hunting Ground I."],
    [1,20,"Red Snail (Lv 4) - Henesys Hunting Ground I",104040000,"Victoria Road: Henesys Hunting Ground I."],
    [1,20,"Stump (Lv 7) - Henesys Hunting Ground I",104040000,"Victoria Road: Henesys Hunting Ground I."],
    [1,20,"Slime (Lv 6) - South Ellinia",100040000,"Victoria Road: The Forest South of Ellinia."],
    [15,20,"Bubbling (Lv 15) - Kerning subway",103000101,"Kerning City Subway: Line 1 <Area 1>."],
    [21,30,"Kerning Party Quest",0,"Local requirement: levels 21-30. Form a party and talk to Lakelis in Kerning City."],
    [21,30,"Zombie Mushroom (Lv 24) - Ant Tunnel I",105050000,"Dungeon: Ant Tunnel I, reached through Sleepywood."],
    [21,30,"Horny Mushroom (Lv 22) - Ant Tunnel I",105050000,"Dungeon: Ant Tunnel I, reached through Sleepywood."],
    [21,30,"Wild Boar (Lv 25) - Wild Boar Land",101040001,"Hidden Street: Land of Wild Boar."],
    [21,30,"Fire Boar (Lv 32) - Burnt Land",106000101,"Warning Street: The Burnt Land I. A preview of the next chapter."],
    [30,50,"Fire Boar (Lv 32) - Burnt Land",106000101,"Warning Street: The Burnt Land I."],
    [30,50,"Cold Eye (Lv 40) - Sleepy Dungeon III",105040303,"Dungeon: Sleepy Dungeon III."],
    [30,50,"Copper Drake (Lv 45) - Drake Hunting Ground",105080000,"Dungeon: Drake Hunting Ground."],
    [30,50,"Brown Teddy (Lv 30) - Terrace Hall",220010500,"Ludibrium: Terrace Hall."],
    [30,50,"Platoon Chronos (Lv 41) - Path of Time <1>",220040000,"Ludibrium: The Path of Time <1>."],
    [50,70,"Jr. Yeti (Lv 50) - Icy Path",211010000,"El Nath: Watch Out for Icy Path I."],
    [50,70,"Coolie Zombie (Lv 57) - Forest of Dead Trees",211041100,"El Nath: Forest of Dead Trees I; the same mob also fills II-IV."],
    [50,70,"Wild Kargo (Lv 62) - Wild Cargo's Area",105090301,"Dungeon: Wild Cargo's Area."],
    [70,90,"Pepe (Lv 60) - The Crown-Flyer",211040001,"Hidden Street: The Crown-Flyer."],
    [70,90,"Yeti (Lv 65) - Ice Valley II",211040200,"El Nath: Ice Valley II."],
    [70,90,"Yeti and Pepe (Lv 78) - Ice Valley II",211040200,"El Nath: Ice Valley II."],
    [70,90,"Dark Yeti and Pepe (Lv 82) - Dangerous Cliff",211040700,"El Nath: Dangerous Cliff."],
    [70,90,"Lycanthrope (Lv 80) - Wolf Territory III",211040800,"El Nath: Wolf Territory III; also found in IV and V."],
    [70,90,"Tauromacis (Lv 70) - Sanctuary Entrance",105090500,"Dungeon: Sanctuary Entrance I; also found deeper in the Sanctuary."],
    [70,90,"Taurospear (Lv 75) - Sanctuary Entrance III",105090700,"Dungeon: Sanctuary Entrance III and IV."],
    [70,90,"Wolf Spider (Lv 80) - Wolf Spider Cavern",600020300,"MesoGears: Wolf Spider Cavern."],
    [90,120,"Spirit Viking (Lv 93) - Warped Path of Time <4>",220060300,"Ludibrium: Warped Path of Time <4>."],
    [90,120,"Gigantic Spirit Viking (Lv 98) - Twisted Time",220060301,"Hidden Street: Twisted Time."],
    [90,120,"Squid (Lv 94) - Dangerous Sea Gorge",230040200,"Aqua Road: Dangerous Sea Gorge I and II."],
    [90,120,"Risell Squid (Lv 97) - Dangerous Sea Gorge",230040200,"Aqua Road: Dangerous Sea Gorge I and II."],
    [90,120,"Bombing Fish House (Lv 90) - Deep Sea Gorge",230040000,"Aqua Road: Deep Sea Gorge I and II. Destroy it to release Gobies."],
    [90,120,"Goby (Lv 85) - Deep Sea Gorge",230040000,"Aqua Road: Deep Sea Gorge I and II. Gobies are released by destroyed Bombing Fish Houses."],
    [90,120,"Dreamy Ghost / Hime (Lv 100)",800020130,"Zipangu: Encounter with the Buddha."],
    [90,120,"Gatekeeper (Lv 108) - Warped Passage",220060400,"Ludibrium: Warped Passage. A harder excursion."],
    [90,120,"Thanatos (Lv 108) - Forgotten Passage",220070400,"Ludibrium: Forgotten Passage. A harder excursion."],
    [85,110,"Headless Horseman (Lv 101)",610010005,"Phantom Forest: Forgotten Path. An area boss that may not always be available; it also appears on several other Phantom Forest maps."],
    [110,135,"Temple of Time quest progression",270010100,"Follow the local Temple of Time quest chain through Memory, Regret, and Oblivion. Its individual mobs are listed separately."],
    [110,120,"Memory Monk (Lv 91) - Memory Lane 1",270010100,"Temple of Time: Memory Lane 1."],
    [110,120,"Memory Monk Trainee (Lv 94) - Memory Lane 2",270010200,"Temple of Time: Memory Lane 2."],
    [110,120,"Memory Guardian (Lv 98) - Memory Lane 3",270010300,"Temple of Time: Memory Lane 3 and 4."],
    [110,120,"Chief Memory Guardian (Lv 101) - Memory Lane 4",270010400,"Temple of Time: Memory Lane 4 and 5."],
    [110,125,"Qualm Monk (Lv 106) - Road of Regrets 1",270020100,"Temple of Time: Road of Regrets 1."],
    [110,125,"Qualm Monk Trainee (Lv 109) - Road of Regrets 2",270020200,"Temple of Time: Road of Regrets 2."],
    [110,125,"Qualm Guardian (Lv 113) - Road of Regrets 3",270020300,"Temple of Time: Road of Regrets 3 and 4."],
    [110,125,"Chief Qualm Guardian (Lv 116) - Road of Regrets 4",270020400,"Temple of Time: Road of Regrets 4 and 5."],
    [110,125,"Dodo (Lv 121) - Memory Lane 5",270010500,"Temple of Time area boss at Memory Lane 5."],
    [120,135,"Oblivion Monk (Lv 121) - Road to Oblivion 1",270030100,"Temple of Time: Road to Oblivion 1."],
    [120,135,"Oblivion Monk Trainee (Lv 124) - Road to Oblivion 2",270030200,"Temple of Time: Road to Oblivion 2."],
    [120,135,"Oblivion Guardian (Lv 128) - Road to Oblivion 3",270030300,"Temple of Time: Road to Oblivion 3 and 4."],
    [120,135,"Chief Oblivion Guardian (Lv 131) - Road to Oblivion 4",270030400,"Temple of Time: Road to Oblivion 4 and 5."],
    [120,135,"Lilynouch (Lv 131) - Road of Regrets 5",270020500,"Temple of Time area boss at Road of Regrets 5."],
    [120,135,"Lyka (Lv 141) - Road to Oblivion 5",270030500,"Temple of Time area boss at Road to Oblivion 5."],
    [110,135,"Skelegon (Lv 110) - Dragon Nest Left Behind",240040511,"Leafre: The Dragon Nest Left Behind. A classic party-training map; Holy Symbol helps when available."],
    [110,135,"Skelosaurus (Lv 113) - Dragon Nest Left Behind",240040511,"Leafre: The Dragon Nest Left Behind. A classic party-training map; Holy Symbol helps when available."],
    [120,200,"Voodoo (Lv 60) - Heartstopper farming",682000501,"Haunted House: Sophilia's Bedroom. A farming revisit rather than level-appropriate training."],
    [120,200,"Headless Horseman (Lv 101)",610010005,"Phantom Forest: Forgotten Path and other Phantom Forest maps. Availability varies."],
    [120,200,"Bigfoot (Lv 110) - Phantom Forest",610010005,"Phantom Forest: Forgotten Path and Twisted Paths. A boss hunt, not a regular spawn grind."],
    [120,200,"Papulatus Clock (Lv 125)",220080001,"Ludibrium: Origin of Clocktower. Complete the required quest access before fighting it."],
    [120,200,"Female Boss / Anego (Lv 130)",801040003,"Zipangu: Parlor. A boss hunt; availability and survivability vary."],
    [120,200,"Primitive Boar (Lv 57) - Ellin Forest",300010400,"Ellin Forest: Boulder Mountain Entrance. Explore the local story while visiting. Later returns are nostalgia visits rather than high-level training."],
    [120,200,"Zakum (Lv 140)",280030000,"El Nath: Zakum's Altar. Expedition boss progression."],
    [120,200,"Horntail (Lv 160)",240060200,"Leafre: Horntail's Cave. Expedition boss progression."],
    [120,200,"Pink Bean (Lv 180)",270050100,"Temple of Time: Twilight of Gods. Late endgame goal."],
    [90,119,"Red Wyvern (Lv 97)",240040310,"Explore the dragon maps of Leafre. Return whenever this region feels like home."],
    [90,119,"Blue Wyvern (Lv 101)",240040210,"Explore the dragon maps of Leafre. Return whenever this region feels like home."],
    [90,119,"Dark Wyvern (Lv 103)",240040200,"Explore the dragon maps of Leafre. Return whenever this region feels like home."],
    [90,119,"Jr. Newtie (Lv 105)",240040520,"Explore the dragon maps of Leafre. Return whenever this region feels like home."],
    [90,119,"Nest Golem (Lv 110)",240040600,"Explore the dragon maps of Leafre. Return whenever this region feels like home."]
];

function totalsFor(which, level) {
    var totals = {};
    for (var s = 0; s <= which; s++) {
        var b = builds[s];
        var budget = s < which ? b.initial + 3 * (b.end - b.start)
            : (level < b.start ? 0 : b.initial + 3 * (Math.min(level, b.end) - b.start));
        for (var i = 0; i < b.path.length && budget > 0; i++) {
            var step = b.path[i], old = totals[step[0]] || 0;
            var amount = Math.min(budget, step[1] - old);
            totals[step[0]] = old + amount;
            budget -= amount;
        }
    }
    return totals;
}

function link(id, label) {
    return "#L" + id + "#" + label + "#l\r\n";
}
function show(text, navigation) {
    var classBack = page === "build" || page === "stages" || page === "gear" || page === "gearBands";
    // The deployed GraalJS runtime lacks its regex language; use string operations.
    while (text.slice(-2) === "\r\n" || text.slice(-2) === "#b") text = text.slice(0, -2);
    cm.sendSimple(text + "\r\n\r\n#k#eMenu navigation#n\r\n#b" +
        (navigation || "") + (classBack ? link(901, "Back to Class: Night Lord") : "") +
        link(900, "Main menu") + link(999, "Close"));
}
function start() { home(); }
function home() {
    page = "home";
    cm.sendSimple("#eTraining & build guide#n\r\nLevel " + cm.getPlayer().getLevel() +
        " - " + cm.getPlayer().getJob().name() + "\r\n\r\n#b" +
        link(5,"Browse all leveling chapters") + "\r\n" +
        link(2,"Class Guides") + "\r\n" +
        link(6,"Party Quests & Challenges") + "\r\n" + link(7,"Bosses & Expeditions") + "\r\n" + link(999,"Close"));
}
function classGuidesMenu() {
    page = "classes";
    show("#eClass Guides#n\r\n\r\n#b" + link(0,"Night Lord"));
}
function classMenu() {
    page = "class";
    show("#eClass: Night Lord#n\r\nRogue -> Assassin -> Hermit -> Night Lord\r\n\r\n#b" +
        link(0, "Skill points (SP) - build targets / my points") + link(1, "Gear - recommendations for my level"),
        link(700,"Back to Class Guides"));
}
function gearBandFor(level) {
    var selected = 0;
    for (var i = 0; i < gearBuilds.length; i++) {
        if (level >= gearBuilds[i].min) selected = i;
    }
    return selected;
}
function gearMenu() {
    page = "gear";
    var b = gearBuilds[gearBand], first = gearPage * 6;
    var text = "#eNight Lord gear - level " + b.label + "#n\r\n" +
        "Your level: " + cm.getPlayer().getLevel() + " | Page " + (gearPage + 1) + " of " + Math.ceil(b.rows.length / 6) +
        "\r\nSaved Royals gear targets, not a comparison with your equipped gear. Availability and equip requirements may differ here.\r\n\r\n";
    for (var i = first; i < Math.min(first + 6, b.rows.length); i++) {
        text += "#e" + b.rows[i][0] + ":#n\r\n" +
            (b.rows[i][1] === "/" ? "No recommendation in the source." : b.rows[i][1]) + "\r\n\r\n";
    }
    text += "#b";
    if (gearPage > 0) text += link(0, "Previous slots");
    if (first + 6 < b.rows.length) text += link(1, "More slots");
    text += link(2, "Choose level range") + link(3, "Return to my level's gear");
    show(text);
}
function gearBandsMenu() {
    page = "gearBands";
    var text = "#eNight Lord gear ranges#n\r\nFor levels 31-34 and 51-54, the previous range is shown. At shared boundaries, the newer range is shown.\r\n\r\n#b";
    for (var i = 0; i < gearBuilds.length; i++) text += link(i, "Level " + gearBuilds[i].label);
    show(text);
}
// Regional tour data: local destinations and verified story starts from the review draft.
var tourChapter = 0, tourRegion = "henesys", tourStory = 0;
var tourRegions = {
    "henesys": {
        title: "Maple Island and Henesys",
        summary: "Begin with Maple Island quests, then settle into Henesys Hunting Ground I. Enjoy the familiar music and your first job adventure.",
        mobs: [1, 2, 3, 4],
        stories: [{"title": "Visit Maya", "intro": "Stop by Maya's house and see why she needs help.", "details": "Maya of Henesys starts at level 15 with Maya. Let her conversations guide the next steps.", "map": 100000001}]
    },
    "ellinia": {
        title: "Ellinia and Kerning",
        summary: "Explore Ellinia's forests, then head beneath Kerning City into the subway.",
        mobs: [5, 6],
        stories: []
    },
    "towns": {
        title: "Small town stories",
        summary: "Take a short detour when you want an errand beyond your training map.",
        mobs: [],
        stories: [{"title": "A small job in Perion", "intro": "Blackbull needs materials for his house. A small errand can be a reason to visit another town.", "details": "Fixing Blackbull's House starts at level 10 with Blackbull in Perion. Follow his collection list.", "map": 102000000},
            {"title": "Visit Maya", "intro": "Stop by Maya's house and see why she needs help.", "details": "Maya of Henesys starts at level 15 with Maya. Let her conversations guide the next steps.", "map": 100000001}]
    },
    "ant": {
        title: "Sleepywood - Ant Tunnel",
        summary: "Head into your first dungeon and spend some time among the mushrooms.",
        mobs: [9, 8],
        stories: [{"title": "Jane and the Wild Boar", "intro": "Visit Jane in Lith Harbor before your hunting trip. Her errand connects the boars and Ant Tunnel mushrooms.", "details": "Available at level 25. Jane asks for 100 Wild Boar Teeth and 80 Horny Mushroom Caps.", "map": 104000000}]
    },
    "boars": {
        title: "Perion - Wild Boar Land",
        summary: "Trade the dungeon for open terrain and a hunting trip around Perion.",
        mobs: [10],
        stories: [{"title": "Jane and the Wild Boar", "intro": "Visit Jane in Lith Harbor before your hunting trip. Her errand connects the boars and Ant Tunnel mushrooms.", "details": "Available at level 25. Jane asks for 100 Wild Boar Teeth and 80 Horny Mushroom Caps.", "map": 104000000},
            {"title": "A small job in Perion", "intro": "Blackbull needs materials for his house. A small errand can be a reason to visit another town.", "details": "Fixing Blackbull's House starts at level 10 with Blackbull in Perion. Follow his collection list.", "map": 102000000}]
    },
    "victoria": {
        title: "Victoria - Burnt Land and the dungeon",
        summary: "Choose the warm hills or explore farther beneath Sleepywood. Stay wherever the training feels good.",
        mobs: [11, 13, 14],
        stories: []
    },
    "toytown": {
        title: "Ludibrium - Teddies and Chronos",
        summary: "Make toy town your next home. Start on the terraces, then explore the first paths below the Clocktower.",
        mobs: [15, 16],
        stories: []
    },
    "mushroom": {
        title: "Mushroom Kingdom",
        summary: "An invitation after second job leads to trouble in the mushroom kingdom.",
        mobs: [],
        stories: [{"title": "The Endangered Mushking Empire", "intro": "Try this early after second job, before high rates carry you past the invitation.", "details": "The class-specific invitations use levels 30-38. Follow your matching invitation to the Head Security Officer at Mushroom Forest Field. The entrance is near the west side of Henesys. Later availability depends on the quest requirements.", "map": 106020000}]
    },
    "square": {
        title: "Kerning Square",
        summary: "Follow a subway lead into Blake's ambitions and a different kind of town story.",
        mobs: [],
        stories: [{"title": "Blake's Dream", "intro": "Speak to Jake in the subway to begin.", "details": "Blake's Dream starts at levels 35-50 with Jake at Kerning subway (103000100). It sends you to Blake at Kerning Square Station (103000310). Continue with My dream is to... and the follow-up errands.", "map": 103000100}]
    },
    "omega": {
        title: "Omega Sector",
        summary: "Take a science-fiction excursion beyond Ludibrium.",
        mobs: [],
        stories: [{"title": "Explore Omega Sector", "intro": "Meet the local NPCs and see which requests catch your interest.", "details": "This is an optional regional visit; it does not need to replace your main training stay.", "map": 221000000}]
    },
    "ariant": {
        title: "Ariant",
        summary: "Visit a desert town and learn about its people.",
        mobs: [],
        stories: [{"title": "Learning the Culture of Ariant", "intro": "Meet Byron and begin with a small introduction to the town.", "details": "Learning the Culture of Ariant starts at level 15 with Byron in The Town of Ariant.", "map": 260000200}]
    },
    "snow": {
        title: "El Nath - Snowfields",
        summary: "Travel through Orbis into the snow. Take village errands with you as you explore.",
        mobs: [17, 18, 20, 21],
        stories: [{"title": "Missing Staff Sergeant Charlie", "intro": "Master Sergeant Fox has a lead worth following.", "details": "Missing Staff Sergeant Charlie starts at level 40 with Master Sergeant Fox in El Nath. Follow his directions.", "map": 211000000}]
    },
    "kargo": {
        title: "Sleepywood - Wild Kargo",
        summary: "Return to the dungeon for a quieter alternative to the snowfields.",
        mobs: [19],
        stories: []
    },
    "korean": {
        title: "Korean Folk Town",
        summary: "Take a break from grinding to explore village tales.",
        mobs: [],
        stories: [{"title": "Preparing to Eliminate Goblins", "intro": "Speak to Chumji when you want to follow the village's goblin story.", "details": "Preparing to Eliminate Goblins starts at level 60 with Chumji in Korean Folk Town. Follow his preparation requests.", "map": 222000000}]
    },
    "mulung": {
        title: "Mu Lung and Herb Town",
        summary: "Enjoy the scenery and village errands at your own pace.",
        mobs: [],
        stories: [{"title": "Mu Lung", "intro": "Explore the town and speak with its residents.", "details": "A change of setting during your middle levels. Choose local errands that interest you.", "map": 250000000},
            {"title": "Herb Town", "intro": "Continue your village tour in Herb Town.", "details": "This is an optional exploration stop; party activities stay in their separate menu.", "map": 251000000}]
    },
    "magatia": {
        title: "Magatia",
        summary: "Investigate the alchemy societies and the people between them.",
        mobs: [],
        stories: [{"title": "A Deal with the Broker", "intro": "Han the Broker can introduce you to the town's intrigue.", "details": "A Deal with the Broker starts at level 30 with Han the Broker in Magatia and leads toward the alchemy-society tests.", "map": 261000000}]
    },
    "ellin": {
        title: "Ellin Forest",
        summary: "Explore the older forest and the camp's stories. Its party quest can wait.",
        mobs: [61],
        stories: [{"title": "Visit Altaire Camp", "intro": "Speak with the camp residents and follow the stories that interest you.", "details": "Explore from Altaire Camp. Primitive Boars are level 57, so later visits are for the story and scenery rather than a new high-level training stop.", "map": 300000000}]
    },
    "haunted": {
        title: "Haunted House",
        summary: "Explore an atmospheric detour or return later to farm Heartstoppers.",
        mobs: [56],
        stories: []
    },
    "deepsnow": {
        title: "El Nath - Deeper snowfields",
        summary: "Continue into the wilderness after third job. Stay for the atmosphere and tougher monsters.",
        mobs: [22, 24, 23],
        stories: []
    },
    "sanctuary": {
        title: "Sleepywood - The Sanctuary",
        summary: "Follow the dungeon deeper toward its ancient guardians.",
        mobs: [25, 26],
        stories: []
    },
    "nlc": {
        title: "NLC - Wolf Spider Cavern",
        summary: "Try a different underground training stop beneath New Leaf City.",
        mobs: [27],
        stories: []
    },
    "neo": {
        title: "Neo City",
        summary: "Investigate a suspicious wanderer and let the time gate become a discovery.",
        mobs: [],
        stories: [{"title": "The Suspicious Wanderer", "intro": "Begin with Han the Broker in Magatia and follow the trail toward Tera Forest.", "details": "At level 70, start The Suspicious Wanderer with Han the Broker. Follow The Wanderer's Whereabouts 1 and 2 through Syras and Isa the Station Guide. Meet Andy at Tera Forest Time Gate (240070000), reached from Minar Forest East Border (240010900). Complete Andy the Time Traveler, then Nex the Time Guard. Return after Guardian Nex's test to unlock Year 2021. Later destinations open through further progression.", "map": 261000000}]
    },
    "goby": {
        title: "Aqua Road - Underwater preview",
        summary: "Try underwater movement before choosing your next long stay.",
        mobs: [33],
        stories: []
    },
    "aqua": {
        title: "Aqua Road - Explore the Deep Sea",
        summary: "Explore the gorge when underwater movement sounds enjoyable. Save it for another character if you prefer the Clocktower.",
        mobs: [32, 30, 31],
        stories: []
    },
    "hime": {
        title: "Zipangu - Himes",
        summary: "An optional flat-map training visit. It does not have to become your main route.",
        mobs: [34],
        stories: []
    },
    "leafre": {
        title: "Leafre - Dragons and nests",
        summary: "Explore the dragon maps and return after fourth job whenever you want to stay longer.",
        mobs: [65, 66, 67, 68, 54, 69, 55],
        stories: []
    },
    "memory": {
        title: "Temple of Time - Memory and Regret",
        summary: "Preview a longer story journey. The lanes unlock through quests, not simply by reaching the monsters' levels.",
        mobs: [39, 40, 41, 42, 43, 44, 45, 46],
        stories: [{"title": "The next door in time", "intro": "Follow the Temple Keeper's story a lane at a time. A new door is a reason to explore, not a deadline to leave your favorite map.", "details": "Talk to Corba in Leafre Station (240000110), become a dragon, and cross the flight map to the Temple. At Three Doors, meet the Temple Keeper. Path to the Past starts at level 90, the first Memory hunt at 91, Regret at 106, and Oblivion at 121. Each stage still requires the preceding quests. Follow the 999-monster hunts and connecting errands in order. The Lyka quest starts at 135; the Pink Bean defeat quest starts at 140 after its prerequisites. Raid entry and readiness are separate.", "map": 270000000}]
    },
    "oblivion": {
        title: "Temple of Time - Oblivion",
        summary: "Continue the Temple story when you want a longer objective. Earlier lanes remain in the previous chapter.",
        mobs: [48, 49, 50, 51],
        stories: [{"title": "The next door in time", "intro": "Follow the Temple Keeper's story a lane at a time. A new door is a reason to explore, not a deadline to leave your favorite map.", "details": "Talk to Corba in Leafre Station (240000110), become a dragon, and cross the flight map to the Temple. At Three Doors, meet the Temple Keeper. Path to the Past starts at level 90, the first Memory hunt at 91, Regret at 106, and Oblivion at 121. Each stage still requires the preceding quests. Follow the 999-monster hunts and connecting errands in order. The Lyka quest starts at 135; the Pink Bean defeat quest starts at 140 after its prerequisites. Raid entry and readiness are separate.", "map": 270000000}]
    },
    "templeboss": {
        title: "Temple of Time - Story bosses",
        summary: "These are milestones in the Keeper's sequence, not ordinary grind targets.",
        mobs: [47, 52, 53],
        stories: [{"title": "The next door in time", "intro": "Follow the Temple Keeper's story a lane at a time. A new door is a reason to explore, not a deadline to leave your favorite map.", "details": "Talk to Corba in Leafre Station (240000110), become a dragon, and cross the flight map to the Temple. At Three Doors, meet the Temple Keeper. Path to the Past starts at level 90, the first Memory hunt at 91, Regret at 106, and Oblivion at 121. Each stage still requires the preceding quests. Follow the 999-monster hunts and connecting errands in order. The Lyka quest starts at 135; the Pink Bean defeat quest starts at 140 after its prerequisites. Raid entry and readiness are separate.", "map": 270000000}]
    },
    "forest": {
        title: "Phantom Forest - Boss revisits",
        summary: "Explore the woods outside NLC, then return for a hunt when you feel ready. Bosses may not be present.",
        mobs: [37, 58],
        stories: []
    },
    "showa": {
        title: "Showa - Anego",
        summary: "A field-boss excursion for when you want a different challenge.",
        mobs: [60],
        stories: []
    },
    "expedition": {
        title: "Expedition goals - For later",
        summary: "Keep these as long-term goals while enjoying your solo journey. Their local battle events declare six-player minimums.",
        mobs: [],
        stories: [{"title": "Zakum (Lv 140)", "intro": "An expedition goal beneath El Nath. Begin with the preparation trials.", "details": "Adobis asks for approval from the Chief's Residence Council before the trials: Unknown Dead Mine, Breath of Lava, and Eyes of Fire. Save the battle for group play. The encounter is at Zakum's Altar (280030000); use normal event entry.", "map": 0},
            {"title": "Horntail (Lv 160)", "intro": "Leafre has a deeper story beyond its training maps. Ask Chief Tatamo about Moira.", "details": "Moira's Whereabouts starts at level 80 with Chief Tatamo in Leafre (240000000), sending you to Moira at Cave of Life Entrance (240040700). Continue her transformation and entry requests. The encounter is in Horntail's Cave (240060200); preparation is not a guarantee of raid admission.", "map": 240000000},
            {"title": "Pink Bean (Lv 180)", "intro": "Keep this as a long-term reason to finish the Temple's story.", "details": "Continue the Temple Keeper's later collection requests. Forgotten Twilight (270050000) has the expedition recruiter. The encounter is in Twilight of Gods (270050100). Quest requirements, raid admission and combat readiness are separate checks.", "map": 270050000}]
    },
    "singapore": {
        title: "Singapore",
        summary: "Take a separate regional trip when you want to leave your main route.",
        mobs: [],
        stories: [{"title": "Visit the CBD", "intro": "Explore the town and follow the local requests that interest you.", "details": "This optional trip does not require adding another regular training destination.", "map": 540000000}]
    },
    "malaysia": {
        title: "Malaysia",
        summary: "Explore another town and park region for a change of scenery.",
        mobs: [],
        stories: [{"title": "Visit Trend Zone Metropolis", "intro": "Start with the town and discover its surroundings.", "details": "This is an exploration visit. Boss access guidance is not included yet.", "map": 550000000}]
    }
};
var tourChapters = [
    [1,20,"Your first adventures","Take your time with the familiar towns. Follow early quests and enjoy the small discoveries.",["henesys","ellinia"],["towns"]],
    [21,29,"Into the Ant Tunnel","Settle into Sleepywood or alternate with the open terrain around Perion.",["ant","boars"],["towns"]],
    [30,49,"A dungeon or a toy town stay","Choose a main stay. Check the optional stories early: Mushroom Kingdom invitations use levels 30-38, and Kerning Square starts at 35-50.",["victoria","toytown"],["mushroom","square","omega","ariant"]],
    [50,69,"Snowfields and village stories","Make El Nath your next home, or return to Sleepywood. Take one village detour when you want a change.",["snow","kargo"],["korean","mulung","magatia","ellin","haunted"]],
    [70,89,"Third job and the wilderness","Return from advancement to places that feel like home, or follow a new story branch.",["deepsnow","sanctuary"],["nlc","neo","goby","singapore"]],
    [90,119,"Choose your deep dungeon stay","Stay awhile in Ludibrium or explore deep Aqua Road. You do not need to rotate through every map.",["ludi","aqua"],["hime","leafre","memory","forest"]],
    [120,149,"Fourth job and the Temple journey","Enjoy fourth job, return to Leafre, and continue the Temple story at your own pace.",["leafre","oblivion"],["ludi","templeboss","forest","showa","expedition"]],
    [150,200,"Finish the stories you chose","Continue the maps you enjoy. Finish chosen stories and pick a boss goal when it sounds fun.",["oblivion","leafre"],["ludi","templeboss","forest","showa","expedition","ellin","haunted","malaysia"]]
];
var selectedParty = 0;
var showExpeditions = false;
var partyCategory = "Party Quests";
// Entrance/recruit maps and limits from the corresponding local event scripts.
var tourParties = [
    ["Henesys PQ",10,255,3,6,100000200,1012112,"Work together to feed Growlie and complete the Moon Bunny party quest. Speak with the entrance NPC in Henesys Park to begin."],
    ["Kerning PQ",21,30,3,4,103000000,9020000,"Answer Cloto's coupon questions, solve the platform puzzles, and defeat King Slime. Your party leader collects passes and asks Cloto to check each stage."],
    ["Carnival PQ",30,50,2,6,980000000,2042000,"Two opposing parties compete to earn Carnival Points by defeating monsters. Spend available CP on summons, skills and guardians; total earned CP determines the result. Field entry minimums vary. Speak to Spiegelmann here to assemble the dedicated 4-vs-4 bot teams, then start the match through the BotPQ Menu.","players per team"],
    ["Ludibrium PQ",35,50,5,6,221024500,2040034,"Explore the tower's puzzle and combat stages, collect passes, and face Alishar. Bring a team that can handle the special skill rooms and coordinate the numbered boxes."],
    ["Ellin Forest PQ",44,55,4,6,300030100,2133000,"Gather an explorer party and speak with the entrance NPC to investigate the forest. Only Explorer jobs are eligible in this local event."],
    ["Orbis PQ",51,70,5,6,200080101,2013000,"Work through the Tower of Goddess rooms to restore Minerva's statue. Collect the required pieces, coordinate the puzzles, and defeat the boss."],
    ["Ludibrium Maze PQ",51,70,3,6,220000000,9103001,"Navigate the maze's connected rooms, defeat monsters for coupons, and regroup to turn them in for rewards.",null,[315,-705]],
    ["Carnival PQ 2",51,70,2,6,980030000,2042005,"A second Monster Carnival for levels 51-70. Form two opposing parties and speak to Spiegelmann for the normal field challenge. Speak to Spiegelmann here to assemble dedicated 4-vs-4 bot teams.","players per team"],
    ["Lord Pirate PQ",55,100,3,6,251010404,2094000,"Gather a party at the pirate ship entrance and follow the NPC's directions through the combat stages to Lord Pirate."],
    ["Magatia PQ",71,85,4,4,261000011,2112004,"Help Romeo and Juliet through the laboratory's combat, research and portal puzzles. A Teleport mage and Dark Sight thief help with the specialist rooms. Protect the captive during the final boss fight. This destination is Romeo's Zenumist entrance; Juliet offers the other side."],
    ["Ariant Coliseum",20,30,2,7,980010000,2101014,"Compete to capture scorpions and collect jewels in the Ariant arena. Register through the lobby NPC and follow the arena's entry and scoring rules."],
    ["Amoria PQ",40,255,6,6,670010100,9201048,"Complete the Amorian Challenge with six players. Follow Amos's entry requirements; marriage and couple composition can affect eligibility or rewards on this server."],
    ["Guild PQ",1,255,6,30,101030104,9040000,"Gather your guild at the Sharenian excavation entrance. Coordinate the puzzles, specialist roles and Ergoth battle. Guild and role requirements still apply."],
    ["Crimsonwood Keep PQ",90,255,6,30,610030020,9201113,"Register a multi-class expedition for Crimsonwood Keep's trials and bosses. Coordinate the class-specific objectives; follow the entrance NPC's expedition requirements."],
    ["MV's Lair PQ",140,255,4,6,674030100,9220018,"Gather a high-level party to explore MV's Lair and its dangerous encounters. Follow the entrance NPC's stage instructions and ordinary reward path."],
    ["Horntail Trial PQ",120,255,6,6,240050000,2083001,"Complete the six-player Horntail trial through the cave's normal stages. This is separate from the Horntail expedition listed on the Expeditions page."],
    ["Boss Rush PQ",1,255,1,6,970030000,9000037,"Challenge the local Boss Rush sequence alone or with a party. Speak to the coordinator for the available challenge and its timer."],
    ["Cafe PQ",21,120,3,6,193000000,1052013,"Choose among the six Cafe PQ variants at this entrance. Defeat the assigned monsters, collect the requested coupons and let the leader turn them in."],
    ["Holiday PQ: 21-30",21,30,3,6,889100000,9105004,"A Maplemas party challenge for this level bracket. Follow the coordinator's requirements; seasonal context and normal entry conditions apply."],
    ["Holiday PQ: 31-40",31,40,3,6,889100010,9105004,"The level 31-40 Maplemas party challenge. This teleport reaches the matching bracket's coordinator."],
    ["Holiday PQ: 41-50",41,50,3,6,889100020,9105004,"The level 41-50 Maplemas party challenge. This teleport reaches the matching bracket's coordinator."],
    ["Rescue Gaga",12,255,1,1,922240200,9001105,"A solo rescue challenge. Speak to the coordinator to enter and follow the event's movement and rescue objectives."],
    ["Mu Lung Dojo",25,255,1,6,925020001,2091005,"Challenge Mu Gong's Dojo alone or with a party. Speak to the Dojo NPC for the tutorial, stages, points and belt rewards. Normal party-level rules still apply."],
    ["El Nath Defense",80,255,1,4,211000001,2020008,"A quest-linked defense challenge, rather than a standard repeatable PQ. Talk to Tylus about the associated skill quest and protect the objective."],
    ["Delli Skill Trial",120,255,1,2,925010200,2095000,"A quest-linked skill challenge. Speak to the entrance NPC and satisfy the required class and quest conditions before entering."],
    ["Elemental Skill Trial",100,255,2,2,220050300,2041023,"A two-player, quest-linked Elemental Thanatos challenge. Both players must satisfy the local class and quest eligibility checks."]
];

var tourExpeditions = [
    ["Zakum",50,255,6,30,211042400,2030013,"Complete your own Zakum prerequisites and speak to Adobis to register the expedition. Enter normally and use an Eye of Fire when the party is ready. A dedicated bot crew can be assembled here."],
    ["Horntail",100,255,6,30,240050400,2083004,"Register the Horntail expedition through the cave's NPC. Meet the normal quest and entry requirements, then tackle the heads and main body."],
    ["Pink Bean",120,255,6,30,270050000,2141001,"Prepare the Temple of Time prerequisites and required entry items. Register through the NPC and complete the statue phases before Pink Bean."],
    ["Balrog",50,255,6,30,105100100,1061014,"Register the normal Balrog expedition at the temple entrance. This guide does not offer the unreferenced Easy Balrog event or bypass its normal entry conditions."],
    ["Scarlion and Targa",100,255,6,30,551030100,9270047,"Register the Malaysia expedition through the entrance NPC. Bring the required quest progress and summoning items for Scarlion and Targa."],
    ["Showa Boss",100,255,3,30,801040004,9120201,"Register the Showa boss expedition through the local NPC. Follow the normal entry requirements and encounter progression."],
    ["Papulatus",1,255,1,6,220080000,0,"Enter the Clocktower boss encounter through the normal portal with your party. Complete the required quest preparations and bring the summoning piece. The local event's broad level range does not bypass quest requirements."],
    ["Captain Latanica",1,255,1,6,541010060,0,"Enter through the Ghost Ship's normal boss portal with an eligible party. Follow the local quest and access requirements; this is an instanced boss battle rather than an expedition registration NPC."]
];
var companionActivities = {"Kerning PQ":true,"Ludibrium PQ":true,"Carnival PQ":true,"Carnival PQ 2":true,"Ellin Forest PQ":true,"Orbis PQ":true,"Ludibrium Maze PQ":true,"Magatia PQ":true,"Zakum":true};
function chapterFor(level) {
    for (var i = 0; i < tourChapters.length; i++) if (level <= tourChapters[i][1]) return i;
    return tourChapters.length - 1;
}
function chapterMenu() {
    page = "training";
    var chapter = tourChapters[tourChapter];
    var text = "#eLevels " + chapter[0] + "-" + chapter[1] + " - " + chapter[2] + "#n\r\n\r\n" + chapter[3] + "\r\n\r\n#eRegional choices#n\r\n#b";
    for (var i = 0; i < chapter[4].length; i++) text += link(i, regionTitle(chapter[4][i]));
    text += "\r\n#k#eOptional episodes#n\r\n#b";
    for (var i = 0; i < chapter[5].length; i++) text += link(100 + i, regionTitle(chapter[5][i]));
    show(text, link(701,"Browse all chapters / revisit favorites"));
}
function regionTitle(key) {
    return key === "ludi" ? "Ludibrium - Deeper into the Clocktower" : tourRegions[key].title;
}
function chapterList() {
    page = "tourChapters";
    var text = "#eBrowse all chapters#n\r\nChoose a region to revisit, or preview a future adventure.\r\n\r\n#b";
    for (var i = 0; i < tourChapters.length; i++) {
        var c = tourChapters[i];
        text += link(i,"Levels " + c[0] + "-" + c[1] + " - " + c[2]);
    }
    show(text, link(700,"Back to chapter"));
}
function openRegion(key) {
    tourRegion = key;
    if (key === "ludi") ludiMenu(); else regionMenu();
}
function tourRegionBack() {
    chapterMenu();
}
function regionMenu() {
    page = "tourRegion";
    var r = tourRegions[tourRegion];
    var text = "#e" + r.title + "#n\r\n\r\n" + r.summary + "\r\n\r\n#b";
    if (r.mobs.length) text += link(0,"Monsters & training maps");
    if (r.stories.length) text += link(1,tourRegion === "expedition" ? "Boss goals & preparations" : "Quests & discoveries");
    show(text, link(700,"Back to chapter"));
}
function trainingList() {
    page = "trainingList";
    var r = tourRegions[tourRegion];
    var text = "#e" + r.title + " - Monsters & training maps#n\r\nChoose a monster to view its map and travel option.\r\n\r\n#b";
    for (var i = 0; i < r.mobs.length; i++) text += link(r.mobs[i],training[r.mobs[i]][2]);
    show(text, link(700,"Back to " + r.title));
}
function storyList() {
    page = "tourStories";
    var r = tourRegions[tourRegion];
    var text = "#e" + r.title + " - Discoveries#n\r\nFollow a story when you want a change of pace.\r\n\r\n#b";
    for (var i = 0; i < r.stories.length; i++) text += link(i,r.stories[i].title);
    show(text, link(700,"Back to region"));
}
function storyMenu(details) {
    page = details ? "tourStoryMore" : "tourStory";
    var story = tourRegions[tourRegion].stories[tourStory];
    var text = "#e" + story.title + "#n\r\n\r\n" + (details ? story.details : story.intro) + "\r\n\r\n";
    if (story.map) text += "#b#m" + story.map + "##k - Map " + story.map + "\r\n\r\n";
    text += "#b";
    if (!details) text += link(2,"More details");
    if (story.map && cm.getPlayer().isGM()) text += link(1,"Teleport to #m" + story.map + "#");
    show(text, link(0,details ? "Back to introduction" : "Back to discoveries") + link(700,"Back to region"));
}
function partyFolders() {
    page = "tourPartyFolders";
    show("#eParty Quests & Challenges#n\r\nChoose an activity for its summary and entrance teleport.\r\n\r\n#b" +
        link(0,"Party Quests") + link(1,"Challenges") + link(2,"PVP PQs"));
}
function activityCategory(name) {
    var pvp = {"Carnival PQ":true,"Carnival PQ 2":true,"Ariant Coliseum":true};
    var challenges = {"Boss Rush PQ":true,"Rescue Gaga":true,"Mu Lung Dojo":true,
        "El Nath Defense":true,"Delli Skill Trial":true,"Elemental Skill Trial":true};
    return pvp[name] ? "PVP PQs" : challenges[name] ? "Challenges" : "Party Quests";
}
function partyMenu(expeditions, category) {
    showExpeditions = expeditions === true;
    if (!showExpeditions) partyCategory = category || "Party Quests";
    page = "tourParties";
    var rows = showExpeditions ? tourExpeditions : tourParties;
    var text = "#e" + (showExpeditions ? "Bosses & Expeditions" : partyCategory) + "#n\r\nChoose an activity for its summary and entrance teleport.\r\n\r\n#b";
    for (var i = 0; i < rows.length; i++) {
        var pq = rows[i];
        if (!showExpeditions && activityCategory(pq[0]) !== partyCategory) continue;
        text += link(i, pq[0] + ": Levels " + pq[1] + "-" + pq[2] + ", " +
            pq[3] + (pq[3] === pq[4] ? "" : "-" + pq[4]) + (pq[8] === "players per team" ? "/team" : " players"));
    }
    show(text, showExpeditions ? "" : link(700,"Back to Party Quests & Challenges"));
}

function partyDetail() {
    page = "tourParty";
    var pq = (showExpeditions ? tourExpeditions : tourParties)[selectedParty];
    var text = "#e" + pq[0] + "#n\r\nLevels " + pq[1] + "-" + pq[2] + ", " +
        pq[3] + (pq[3] === pq[4] ? "" : "-" + pq[4]) + " " + (pq[8] || "players") + "\r\n\r\n" +
        pq[7] + "\r\n\r\n" + (companionActivities[pq[0]] ? "Dedicated bot crew available." : "Dedicated bot crew not implemented yet.") +
        "\r\n\r\n#b" + (pq[6] ? "#p" + pq[6] + "##k - " : "") + "#m" + pq[5] + "#\r\n\r\n" +
        "Enter through the normal NPC or portal. The usual entry requirements still apply.\r\n\r\n#b";
    if (cm.getPlayer().isGM()) text += link(1,"Teleport to " + pq[0]);
    show(text, link(0,showExpeditions ? "Back to Expeditions" : "Back to " + partyCategory));
}
function tourAction(selection) {
    var c = tourChapters[tourChapter];
    if (page === "training") {
        if (selection === 701) chapterList();
        else if (selection >= 100 && selection < 100 + c[5].length) openRegion(c[5][selection - 100]);
        else if (selection >= 0 && selection < c[4].length) openRegion(c[4][selection]);
    } else if (page === "tourChapters") {
        if (selection === 700) chapterMenu();
        else if (selection >= 0 && selection < tourChapters.length) { tourChapter = selection; chapterMenu(); }
    } else if (page === "tourRegion") {
        if (selection === 700) tourRegionBack();
        else if (selection === 0 && tourRegions[tourRegion].mobs.length) trainingList();
        else if (selection === 1 && tourRegions[tourRegion].stories.length) storyList();
    } else if (page === "tourStories") {
        if (selection === 700) regionMenu();
        else if (selection >= 0 && selection < tourRegions[tourRegion].stories.length) { tourStory = selection; storyMenu(false); }
    } else if (page === "tourStory" || page === "tourStoryMore") {
        if (selection === 700) regionMenu();
        else if (selection === 0) { if (page === "tourStoryMore") storyMenu(false); else storyList(); }
        else if (selection === 2 && page === "tourStory") storyMenu(true);
        else if (selection === 1 && cm.getPlayer().isGM()) {
            pendingMap = tourRegions[tourRegion].stories[tourStory].map;
            if (pendingMap) { page = "tourWarpConfirm"; cm.sendYesNo("Teleport to #b#m" + pendingMap + "##k now?"); }
        }
    } else if (page === "tourPartyFolders") {
        if (selection >= 0 && selection < 3) partyMenu(false, ["Party Quests","Challenges","PVP PQs"][selection]);
    } else if (page === "tourParties") {
        if (selection === 700) { if (showExpeditions) home(); else partyFolders(); }
        else if (selection >= 0 && selection < (showExpeditions ? tourExpeditions : tourParties).length) { selectedParty = selection; partyDetail(); }
    } else if (page === "tourParty") {
        if (selection === 0) partyMenu(showExpeditions, partyCategory);
        else if (selection === 1 && cm.getPlayer().isGM()) {
            var destination = (showExpeditions ? tourExpeditions : tourParties)[selectedParty];
            pendingMap = destination[5];
            page = "tourPartyWarpConfirm";
            cm.sendYesNo("Teleport to " + destination[0] + " at #b#m" + pendingMap + "##k now?");
        }
    }
}

function trainingDetail() {
    if (selectedTraining < 0 || selectedTraining >= training.length) { trainingList(); return; }
    var row = training[selectedTraining];
    page = "detail";
    var actions = row[3] && cm.getPlayer().isGM() ? link(1, "Teleport to #m" + row[3] + "#") : "";
    show("#e" + row[2] + "#n\r\n" +
        (row[3] ? "#b#m" + row[3] + "##k\r\n" : "") + "\r\n" + row[4] + "\r\n\r\n#b" + actions,
        link(0, "Back to training options"));
}
// Local WZ spawn maps and levels within this chapter's 90-120 range.
var ludiMobs = [
    ["Spirit Viking",93,220060300,"Warped side"],
    ["Phantom Watch",95,220070300,"Forgotten side"],
    ["Gigantic Spirit Viking",98,220060301,"Warped side"],
    ["Grim Phantom Watch",99,220070301,"Forgotten side"],
    ["Gatekeeper",108,220060400,"Warped side"],
    ["Thanatos",108,220070400,"Forgotten side"]
];
var ludiQuestDetails = [
    "Start with Mr. Bouffon in #m220050300# (220050300). Flo is in the same map.\r\n\r\n" +
    "#e1. Protect Ludibrium#n\r\nAvailable at level 60. Complete his collection request.\r\n\r\n" +
    "#e2. The Replacement Time Energy#n\r\nContinue Mr. Bouffon's investigation.",
    "#e3. The Time Sphere#n\r\nAccept it from Mr. Bouffon, then speak with Flo.\r\n\r\n" +
    "#e4. A Piece of Crack#n\r\nComplete Flo's collection request while The Time Sphere is active. Return to finish The Time Sphere.",
    "#e5. Papulatus#n\r\nAvailable at level 80 after The Time Sphere and A Piece of Crack are complete.\r\n\r\n" +
    "Follow Mr. Bouffon's directions for the medal and summoning item. These are access levels, not a promise that the fight will be easy."
];
var ludiBossDetails = [
    "#eAccess and summoning#n\r\nMr. Bouffon directs you to obtain the Ludibrium Medal from the monsters guarding Warped Passage or Forgotten Passage.\r\n\r\n" +
    "His Papulatus quest supplies the Piece of Cracked Dimension. Follow his directions to drop it at the crack inside the boss room.",
    "#ePlaying solo#n\r\nCreate a party with yourself as leader. This server accepts a one-person party for Papulatus.\r\n\r\n" +
    "Use the normal entrance from #m220080000# after following the access quests. The fight has a 45-minute timer.\r\n\r\nTeleport takes you to the approach map, not into the boss instance."
];
function ludiMenu() {
    page = "ludi";
    show("#eLudibrium - Deeper into the Clocktower#n\r\nSuggested tour: levels 90-120, or revisit anytime.\r\n\r\n" +
        "Stay awhile, explore the deeper maps, and speak with NPCs as you go. Their quests may lead you toward a larger challenge.\r\n\r\n#b" +
        link(0,"Monsters & training maps") + link(1,"Quests & discoveries") +
        link(2,"Boss: Papulatus"), link(3,"Back to regional choices"));
}
function ludiMobList() {
    page = "ludiMobs";
    var text = "#eExplore the deep Clocktower#n\r\nMonsters for levels 90-120.\r\n\r\n#b";
    for (var i = 0; i < ludiMobs.length; i++) {
        var row = ludiMobs[i];
        text += link(i,row[0] + " (Lv " + row[1] + ") - " + row[3]);
    }
    show(text, link(103,"Back to Ludibrium"));
}
function ludiMobDetail() {
    page = "ludiMob";
    var row = ludiMobs[ludiMob];
    var desc = "Explore this stop on the way through the Clocktower. Stay as long as the training feels good.";
    if (row[0] === "Gatekeeper" || row[0] === "Thanatos") desc = "A harder excursion guarding the deepest part of the Clocktower. Return when you feel ready.";
    if (row[0] === "Spirit Viking") desc = "A place to settle into the deeper Clocktower chapter. Explore the platforms and enjoy the music. The nearby hidden map offers another Viking stop.";
    show("#e" + row[0] + " (Lv " + row[1] + ")#n\r\n#b#m" + row[2] + "##k\r\nMap " + row[2] + "\r\n\r\n" + desc + "\r\n\r\n#b" +
        (cm.getPlayer().isGM() ? link(1,"Teleport to #m" + row[2] + "#") : ""),
        link(0,"Back to monsters") + link(2,"Back to Ludibrium"));
}
function ludiStory(boss, details) {
    page = boss ? (details ? "ludiBossMore" : "ludiBoss") : (details ? "ludiQuestMore" : "ludiQuest");
    var text = "#e" + (boss ? "Papulatus" : "Quests & discoveries") + "#n\r\n\r\n";
    if (details) {
        var sections = boss ? ludiBossDetails : ludiQuestDetails;
        text += sections.join("\r\n\r\n") + "\r\n\r\n#b";
    } else {
        text += boss ? "Your exploration can lead to a confrontation with Papulatus (Papulatus Clock, Lv 125). Unlocking the room is a milestone of its own. If the fight is too much today, return after gaining skills or equipment." :
            "Something is wrong beneath the Clocktower. Visit Mr. Bouffon in Path of Time and see what help he needs. Follow the investigation while exploring; return to it whenever you feel ready.";
        var map = boss ? 220080000 : 220050300;
        text += "\r\n\r\n#b#m" + map + "##k - Map " + map + "\r\n#b";
        if (cm.getPlayer().isGM()) text += link(1,"Teleport to #m" + map + "#");
        text += link(2,"More details - " + (boss ? "access and summoning" : "reveal the quest sequence"));
    }
    show(text, (details ? link(0,"Back to introduction") : "") + link(3,"Back to Ludibrium"));
}
function ludiAction(selection) {
    if (page === "ludi") {
        if (selection === 0) ludiMobList();
        else if (selection === 1 || selection === 2) ludiStory(selection === 2,false);
        else tourRegionBack();
    } else if (page === "ludiMobs") {
        if (selection >= 0 && selection < ludiMobs.length) { ludiMob = selection; ludiMobDetail(); }
        else ludiMenu();
    } else {
        var mob = page === "ludiMob", boss = page === "ludiBoss" || page === "ludiBossMore";
        var details = page === "ludiQuestMore" || page === "ludiBossMore";
        if (selection === 1 && !details && cm.getPlayer().isGM()) {
            pendingMap = mob ? ludiMobs[ludiMob][2] : (boss ? 220080000 : 220050300);
            page = "ludiWarpConfirm";
            cm.sendYesNo("Teleport to #b#m" + pendingMap + "##k now?");
        } else if (mob) { if (selection === 0) ludiMobList(); else ludiMenu(); }
        else if (selection === 3) ludiMenu();
        else if (selection === 2 && !details) ludiStory(boss,true);
        else ludiStory(boss,false);
    }
}
function buildMenu() {
    page = "build";
    var b = builds[stage], now = totalsFor(stage, buildLevel), before = totalsFor(stage, buildLevel - 1);
    var text = "#e" + b.name + " - level " + buildLevel + "#n\r\n" +
        "Reference build; assumes on-time job advancement and the earlier stages of this build.\r\n\r\n#ePoints for this level:#n\r\n";
    var spent = 0;
    for (var key in now) {
        var delta = now[key] - (before[key] || 0);
        if (delta > 0) { text += "+" + delta + " " + skills[key][1] + " (target " + now[key] + ")\r\n"; spent += delta; }
    }
    if (!spent) text += "Core build complete; remaining SP is your choice.\r\n";
    var job = cm.getPlayer().getJob().getId();
    if (job === 400 || job === 410 || job === 411 || job === 412) {
        text += "\r\n#eBuild targets / your current points:#n\r\n";
        for (var key in now) text += skills[key][1] + ": " + now[key] + " / " + cm.getPlayer().getSkillLevel(skills[key][0]) + "\r\n";
    } else text += "\r\nThis is a claw-thief build, not a build for your current class.\r\n";
    if (stage === 1) text += "\r\nAssassin: assumes a fast Maple Claw. The original level-60/70 SP typo is corrected to 3 SP per level.\r\n";
    if (stage === 2) text += "\r\nHermit: Avenger 1, then Shadow Partner. Flash Jump follows Avenger 5.\r\n";
    if (stage === 3) text += "\r\nFourth-job skills need their unlocks and mastery books. Venom is shown before Taunt; you may swap that order.\r\n";
    text += "\r\n#b";
    if (buildLevel > b.start) text += link(0, "Previous level");
    if (buildLevel < b.end) text += link(1, "Next level");
    text += link(2, "Choose level") + link(3, "Choose job stage");
    show(text);
}
function action(mode, type, selection) {
    if (mode !== 1 || selection === 999) { cm.dispose(); return; }
    if (selection === 900) { home(); return; }
    if (selection === 901) { classMenu(); return; }
    if (page === "home") {
        if (selection === 2) classGuidesMenu();
        else if (selection === 5) { tourChapter = chapterFor(cm.getPlayer().getLevel()); chapterList(); }
        else if (selection === 6) partyFolders();
        else if (selection === 7) partyMenu(true);
        else cm.dispose();
    } else if (page === "classes") {
        if (selection === 0) classMenu();
    } else if (page === "class") {
        if (selection === 0) {
            var job = cm.getPlayer().getJob().getId();
            stage = job === 412 ? 3 : job === 411 ? 2 : job === 410 ? 1 : 0;
            buildLevel = Math.max(builds[stage].start, Math.min(builds[stage].end, cm.getPlayer().getLevel())); buildMenu();
        } else if (selection === 1) { gearBand = gearBandFor(cm.getPlayer().getLevel()); gearPage = 0; gearMenu(); }
        else if (selection === 700) classGuidesMenu();
        else cm.dispose();
    } else if (page === "gear") {
        if (selection === 0) { gearPage = Math.max(0, gearPage - 1); gearMenu(); }
        else if (selection === 1) { gearPage = Math.min(Math.ceil(gearBuilds[gearBand].rows.length / 6) - 1, gearPage + 1); gearMenu(); }
        else if (selection === 2) gearBandsMenu();
        else if (selection === 3) { gearBand = gearBandFor(cm.getPlayer().getLevel()); gearPage = 0; gearMenu(); }
        else cm.dispose();
    } else if (page === "gearBands" && selection >= 0 && selection < gearBuilds.length) {
        gearBand = selection; gearPage = 0; gearMenu();
    } else if (page === "trainingList") {
        if (selection === 700) { regionMenu(); return; }
        if (tourRegions[tourRegion].mobs.indexOf(selection) < 0) return;
        selectedTraining = selection; trainingDetail();
    } else if (page === "tourPartyWarpConfirm") {
        if (pendingMap && cm.getPlayer().isGM()) {
            var partyDestination = (showExpeditions ? tourExpeditions : tourParties)[selectedParty];
            var landing = partyDestination[9];
            if (landing) cm.getPlayer().changeMap(cm.getMap(pendingMap), new java.awt.Point(landing[0], landing[1]));
            else cm.warp(pendingMap, 0);
        }
        pendingMap = 0; cm.dispose();
    } else if (page === "tourWarpConfirm") {
        if (pendingMap && cm.getPlayer().isGM()) cm.warp(pendingMap, 0);
        pendingMap = 0; cm.dispose();
    } else if (page === "training" || page.indexOf("tour") === 0) {
        tourAction(selection);
    } else if (page === "detail") {
        if (selection === 1 && selectedTraining >= 0 && training[selectedTraining][3] && cm.getPlayer().isGM()) {
            page = "warpConfirm";
            cm.sendYesNo("Teleport to #b#m" + training[selectedTraining][3] + "##k now?");
        } else trainingList();
    } else if (page === "warpConfirm") {
        var destination = selectedTraining >= 0 ? training[selectedTraining][3] : 0;
        if (destination && cm.getPlayer().isGM()) cm.warp(destination, 0);
        cm.dispose();
    }
    else if (page === "ludiWarpConfirm") {
        if (pendingMap && cm.getPlayer().isGM()) cm.warp(pendingMap, 0);
        pendingMap = 0;
        cm.dispose();
    }
    else if (page.indexOf("ludi") === 0) ludiAction(selection);
    else if (page === "build") {
        if (selection === 0) { buildLevel = Math.max(builds[stage].start, buildLevel - 1); buildMenu(); }
        else if (selection === 1) { buildLevel = Math.min(builds[stage].end, buildLevel + 1); buildMenu(); }
        else if (selection === 2) { page = "buildNumber"; cm.sendGetNumber("View build at level:", buildLevel, builds[stage].start, builds[stage].end); }
        else if (selection === 3) { page = "stages"; show("#eChoose a job stage#n\r\n#b" + link(0,"Rogue") + link(1,"Assassin") + link(2,"Hermit") + link(3,"Night Lord")); }
        else cm.dispose();
    } else if (page === "buildNumber") {
        buildLevel = Math.max(builds[stage].start, Math.min(builds[stage].end, selection)); buildMenu();
    } else if (page === "stages" && selection >= 0 && selection < builds.length) {
        stage = selection; buildLevel = builds[stage].start; buildMenu();
    } else cm.dispose();
}
