/* Personal all-class nostalgia training guide. Map and mob IDs are verified
 * against this server's WZ/handbook data. PQ limits use local event scripts.
 * Reference guidance with confirmed GM-only destination teleports.
 */
var page = "home", stage = 0, buildLevel = 10;
var ludiMob = 0, pendingMap = 0;
var selectedTraining = -1;
var gearBand = 0;
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

// Otono class references: Togain's supplied transcripts, with local WZ skill caps and prerequisites.
// Royals forum references supplement gear priorities; their custom class mechanics are not imported.
var classGuide = 0;
var guideDefinitions = [{name:"Night Lord", route:"Rogue -> Assassin -> Hermit -> Night Lord",
    skills:skills, builds:builds, gear:gearBuilds,
    gearNote:"Saved Royals gear targets, not a comparison with your equipped gear. Availability and equip requirements may differ here.",
    notes:["", "Assassin: assumes a fast Maple Claw. The original level-60/70 SP typo is corrected to 3 SP per level.",
        "Hermit: Avenger 1, then Shadow Partner. Flash Jump follows Avenger 5.",
        "Fourth-job skills need their unlocks and mastery books. Venom is shown before Taunt; you may swap that order."]}];
function guideSkill(id, name) { return [id,name]; }
guideDefinitions.push({name:"Permanent Beginner", route:"Beginner - remain jobless",
    skills:{snails:guideSkill(1000,"Three Snails"), recovery:guideSkill(1001,"Recovery"), feet:guideSkill(1002,"Nimble Feet")},
    builds:[{name:"Beginner",job:0,start:1,end:200,initial:0,spPerLevel:1,spCap:6,
        path:[["feet",1],["snails",1],["feet",3],["snails",3]]}],
    notes:["Only six ordinary Beginner SP are earned, one per level from 2 through 7. No job advancement means no later job SP. This route takes Nimble Feet and Three Snails; Recovery is an alternative, not an extra six-point pool. Quest skills are separate."],
    gearNote:"All-class or Beginner-only gear. Check STR, level and availability before buying. STR supports melee damage; use enough DEX/accuracy gear to hit your chosen monsters. Attack speed and reach matter as well as weapon attack.",
    gear:[
        {min:1,max:19,label:"1~19",rows:[["Weapon","Starter sword, club or an all-class weapon you can equip. Test both speed and reach."],["Hat","Bandana; use affordable all-class gear."],["Clothes","Starter clothes; save for an all-class Bathrobe at level 20."],["Gloves","Work Gloves; weapon-attack scrolling is a later upgrade."],["Shoes","Movement speed helps a class with limited mobility."],["Accuracy","Use DEX/accuracy gear when you miss; do not chase enemies far above your level."]]},
        {min:20,max:35,label:"20~35",rows:[["Weapon","Frozen Tuna: level 20, 80 STR, 85 clean attack, very slow. Sake Bottle: level 30, 110 STR, 100 clean attack. These are optional rare Beginner-only targets."],["Alternative","An affordable all-class weapon is fine. Red Whip becomes available at 35 and favors movement speed."],["Clothes","Bathrobe for Men / Women (level 20); scroll for useful stats within your budget."],["Gloves","Affordable weapon-attack Work Gloves."],["Shoes","Squishy Shoes from KPQ if earned; otherwise speed/DEX shoes."],["Pendant","Spiegelmann's Necklace when you can obtain it."]]},
        {min:36,max:109,label:"36~109",rows:[["Weapon","Maroon Mop: level 36, 130 STR, 220 clean attack, very slow. Compare real kill speed with Sake Bottle or other all-class weapons."],["Hat","Bandana or another usable all-class hat. A Zakum Helmet is a later survival challenge, not an early requirement."],["Clothes","Stat-scrolled Bathrobe; Beginner has no class armor upgrades."],["Gloves / cape","Weapon-attack gloves, then a stat cape or affordable attack cape."],["Shoes / eye","Speed/DEX shoes; Broken Glasses if earned through LPQ."],["Pendant / shield","Silver Deputy Star if obtained. Maple Shield is useful only with a one-handed weapon."]]},
        {min:110,max:200,label:"110~200",rows:[["Weapon","Continue a strong Maroon Mop or compare faster all-class alternatives. Higher item level alone does not mean better damage."],["Hat / clothes","Earned Zakum Helmet if survivable; useful-stat all-class clothes."],["Gloves / cape / shoes","Attack-scrolled gloves, Pink Adventurer Cape and Facestompers are expensive goals, not prerequisites."],["Pendant","Mark of Naricain (level 110) if earned; keep a useful earlier pendant until then."],["Accuracy / survival","Keep enough accuracy to hit and HP to survive. Beginners have no Mastery, Hyper Body or Magic Guard of their own."],["Shield","An attack-scrolled Maple Shield helps a one-handed setup; a two-handed weapon cannot use it."]]}
    ]});
guideDefinitions.push({name:"Dark Knight", route:"Warrior -> Spearman -> Dragon Knight -> Dark Knight",
    skills:{hpRecovery:[1000000,"Improved HP Recovery"],hpIncrease:[1000001,"Improved MaxHP Increase"],endure:[1000002,"Endure"],power:[1001004,"Power Strike"],slash:[1001005,"Slash Blast"],
        spearMastery:[1300000,"Spear Mastery"],poleMastery:[1300001,"Pole Arm Mastery"],spearBooster:[1301004,"Spear Booster"],poleBooster:[1301005,"Pole Arm Booster"],iron:[1301006,"Iron Will"],hyper:[1301007,"Hyper Body"],
        resistance:[1310000,"Elemental Resistance"],crusher:[1311001,"Spear Crusher"],fury:[1311004,"Dragon Fury: Pole Arm"],sacrifice:[1311005,"Sacrifice"],roar:[1311006,"Dragon Roar"],crash:[1311007,"Power Crash"],blood:[1311008,"Dragon Blood"],
        warrior:[1321000,"Maple Warrior"],rush:[1321003,"Rush"],stance:[1321002,"Power Stance"],beholder:[1321007,"Beholder"],achilles:[1320005,"Achilles"],berserk:[1320006,"Berserk"],aura:[1320008,"Aura of the Beholder"],hex:[1320009,"Hex of the Beholder"],will:[1321010,"Hero's Will"],magnet:[1321001,"Monster Magnet"]},
    builds:[
        {name:"Warrior",job:100,start:10,end:30,initial:1,path:[["power",1],["hpRecovery",5],["hpIncrease",10],["slash",20],["power",20],["endure",5],["hpRecovery",6]]},
        {name:"Spearman",job:130,start:30,end:70,initial:1,path:[["spearMastery",5],["spearBooster",6],["spearMastery",20],["iron",3],["hyper",30],["poleMastery",20],["poleBooster",20],["spearBooster",20],["iron",11]]},
        {name:"Dragon Knight",job:131,start:70,end:120,initial:1,path:[["crusher",30],["fury",30],["sacrifice",3],["roar",30],["blood",20],["crash",20],["resistance",18]]},
        {name:"Dark Knight",job:132,start:120,end:200,initial:3,path:[["rush",1],["beholder",10],["berserk",30],["stance",30],["warrior",20],["achilles",30],["will",5],["hex",25],["aura",25],["rush",30],["warrior",30],["magnet",28]]}
    ],
    notes:["Max Improved MaxHP Increase early: its level-up HP benefit is not retroactive. Power Strike 1 unlocks Slash Blast.",
        "Hybrid spear/pole-arm route: skip Final Attack so both weapons receive Mastery and Booster. Use a spear first, then add a pole arm for third-job Fury.",
        "Spear Crusher is the main damage skill; Pole Arm Fury handles broad groups. Crusher 16 temporarily changes target count: you may save SP at 15 and spend eleven together to reach 26. This reference shows continuous spending. Dragon Blood costs HP.",
        "Unlock skills and mastery books normally. Max-level Berserk here doubles damage at or below 50% HP. Plan potions around survival first; big percentage heals can turn it off. Power Crash removes a physical-defense buff; it is not a blanket Damage Reflect cancel."],
    gearNote:"Prioritize enough accuracy to hit, then STR and weapon attack. These are gradual targets, not required purchases. Hybrid builds use a spear for Crusher and a pole arm for Fury.",
    gear:[
        {min:10,max:29,label:"10~29",rows:[["Weapon","An affordable level-appropriate warrior weapon. Power Strike and Slash Blast do not require a spear yet."],["Hat","Bamboo Hat or Bandana; favor accuracy/DEX where needed."],["Clothes","Warrior armor you can equip; Bathrobe is an option from level 20."],["Gloves","Accuracy gloves when missing; weapon-attack gloves when hits are reliable."],["Shoes","Affordable speed/DEX shoes; Squishy Shoes if earned."],["Stats","STR for damage. Add DEX only as needed after checking accuracy gear and enemy level."]]},
        {min:30,max:69,label:"30~69",rows:[["Weapon","A spear matching your Mastery; Maple Impaler from level 43. Add a pole arm as its Mastery and Booster are learned."],["Pole arm","Maple Karstan from level 64 for the hybrid route."],["Hat / eye","Bandana or Bamboo Hat; Broken Glasses if earned. No need to start with a boss helmet."],["Clothes","DEX/accuracy-scrolled Bathrobe or useful warrior armor."],["Gloves / cape","Affordable attack gloves once accuracy is sufficient; useful STR/DEX cape."],["Pendant / shoes","Spiegelmann's Necklace; useful speed/DEX shoes."]]},
        {min:70,max:119,label:"70~119",rows:[["Spear","Sky Ski from level 85: fast spear option. Compare attack speed and scrolled attack with your existing spear."],["Pole arm","Maple Karstan, then Purple Surfboard from level 95 for Fury."],["Hat / eye","Earned Zakum Helmet and Broken Glasses are upgrades when accessible."],["Clothes","STR-scrolled warrior armor once accuracy is covered; retain DEX gear when needed."],["Gloves / cape / shoes","Attack gloves, useful-stat cape and speed/DEX shoes; attack accessories are costly later goals."],["Pendant","Silver Deputy Star; Mark of Naricain from level 110 if earned."]]},
        {min:120,max:200,label:"120~200",rows:[["Weapon","A well-scrolled Sky Ski; retain a useful pole arm for Fury. Compare total attack and speed rather than item level alone."],["Hat / clothes","Earned boss helmet and STR-focused warrior armor; keep enough accuracy."],["Gloves","Attack-scrolled Work Gloves or Stormcaster Gloves."],["Cape / shoes","Attack cape and Facestompers as budget permits; useful earlier gear remains viable."],["Pendant","Horntail Necklace or Mark of Naricain; compare actual rolled stats."],["Survival","Hyper Body helps the party. Choose HP and resistance gear for the encounter without blindly sacrificing all damage."]]}
    ]});
guideDefinitions.push({name:"Bowmaster",route:"Bowman -> Hunter -> Ranger -> Bowmaster",
    skills:{blessing:[3000000,"The Blessing of Amazon"],critical:[3000001,"Critical Shot"],eye:[3000002,"The Eye of Amazon"],focus:[3001003,"Focus"],blow:[3001004,"Arrow Blow"],double:[3001005,"Double Shot"],mastery:[3100000,"Bow Mastery"],booster:[3101002,"Bow Booster"],soul:[3101004,"Soul Arrow: Bow"],bomb:[3101005,"Arrow Bomb: Bow"],knock:[3101003,"Power Knock-Back"],thrust:[3110000,"Thrust"],puppet:[3111002,"Puppet"],inferno:[3111003,"Inferno"],hawk:[3111005,"Silver Hawk"],strafe:[3111006,"Strafe"],mortal:[3110001,"Mortal Blow"],rain:[3111004,"Arrow Rain"],warrior:[3121000,"Maple Warrior"],eyes:[3121002,"Sharp Eyes"],hurricane:[3121004,"Hurricane"],hamstring:[3121007,"Hamstring"],phoenix:[3121006,"Phoenix"],will:[3121009,"Hero's Will"],expert:[3120005,"Bow Expert"],concentrate:[3121008,"Concentrate"],breath:[3121003,"Dragon's Breath"]},
    builds:[
        {name:"Bowman",job:300,start:10,end:30,initial:1,path:[["blow",1],["blessing",3],["eye",8],["critical",20],["double",20],["focus",9]]},
        {name:"Hunter",job:310,start:30,end:70,initial:1,path:[["bomb",1],["mastery",19],["booster",6],["soul",2],["bomb",30],["booster",20],["soul",20],["mastery",20],["knock",20],["blessing",14]]},
        {name:"Ranger",job:311,start:70,end:120,initial:1,path:[["strafe",1],["mortal",5],["rain",30],["strafe",30],["puppet",5],["hawk",30],["puppet",20],["inferno",30],["thrust",6]]},
        {name:"Bowmaster",job:312,start:120,end:200,initial:3,path:[["hurricane",1],["eyes",30],["expert",30],["hurricane",30],["warrior",20],["phoenix",30],["concentrate",30],["will",5],["breath",30],["warrior",30],["hamstring",28]]}
    ],
    notes:["Arrow Blow 1 unlocks Double Shot; Blessing 3 unlocks range and Focus. Critical Shot and range are early priorities.",
        "Skip Final Attack in this reference; it does not carry into your main third/fourth-job attacks. Keep arrows until Soul Arrow is usable. Booster 5 is required before Soul Arrow.",
        "Arrow Rain first favors mob training; move Strafe earlier if you prefer single targets. Mortal Blow 5 unlocks Rain; Puppet 5 unlocks Hawk. Learn Puppet placement rather than relying on raw HP.",
        "Unlock fourth-job skills and books normally. Sharp Eyes supports the whole party; Hurricane is your sustained single-target attack. Bow Expert needs Bow Mastery 20; Phoenix needs Silver Hawk 15. Carry arrows and check their bonuses when using Soul Arrow."],
    gearNote:"DEX is your damage stat; add only enough STR, including equipment, for the bow you choose. Maple bows ease early STR requirements. These are affordable-to-aspirational targets, not a mandatory shopping list.",
    gear:[
        {min:10,max:34,label:"10~34",rows:[["Bow","War Bow at 10; upgrade to affordable bows as their level/STR requirements allow. Battle Bow is available from level 25."],["Ammo","Carry arrows; Soul Arrow is a second-job skill."],["Hat / clothes","Bandana or bowman hat; DEX-focused clothes you can equip."],["Gloves","Work Gloves; attack scrolling is a later upgrade."],["Shoes / cape","Affordable speed/DEX shoes; useful-stat cape when available."],["Stats","DEX for damage. Check your next bow's STR requirement before adding AP."]]},
        {min:35,max:69,label:"35~69",rows:[["Bow","Maple Bow from 35; Maple Kandiva Bow from 64. Both avoid a STR requirement."],["Ammo","Carry arrows; use Soul Arrow after its Booster prerequisite is met."],["Hat / eye","Bandana or bowman hat; Broken Glasses if earned. Boss helmets are optional later upgrades."],["Clothes","DEX gear now; STR-scrolled overall later can reduce base STR needed for endgame bows."],["Gloves / cape","Attack-scrolled Work Gloves and an affordable DEX/STR cape."],["Pendant / shoes","Spiegelmann's Necklace; Squishy Shoes or useful-stat shoes."]]},
        {min:70,max:119,label:"70~119",rows:[["Bow","Keep a good Maple Kandiva Bow until you can equip a worthwhile Nisrock (100, 105 STR) or Dragon Shiner Bow (110, 115 STR)."],["Hat / eye","Earned Zakum Helmet; Broken Glasses or useful DEX eye accessory."],["Clothes","Useful DEX/STR bowman overall; count gear STR before adding AP."],["Gloves","Attack-scrolled gloves; upgrade within your budget."],["Cape / shoes","DEX/STR cape and shoes; compare attack accessories when affordable."],["Pendant","Silver Deputy Star; Mark of Naricain from 110 if earned."]]},
        {min:120,max:200,label:"120~200",rows:[["Bow","Well-scrolled Nisrock or Dragon Shiner Bow. Later bows must justify their stat requirements and cost."],["Hat / clothes","Earned boss helmet; STR/DEX overall to meet bow requirements with less base STR."],["Gloves","Attack-scrolled Work Gloves or Stormcaster Gloves."],["Cape / shoes","Attack cape and Facestompers are expensive upgrades; useful stat gear is fine while saving."],["Pendant","Horntail Necklace or Mark of Naricain; compare actual stats and the STR you need."],["Ammo / survival","Keep arrows stocked. Maintain enough HP to survive the boss, including the possibility of Dispel removing Hyper Body."]]}
    ]});

// Transcript-based references; exact allocations are suggested Otono routes.
guideDefinitions.push({
    name: "Ice/Lightning Mage",
    route: "Magician -> Wizard (I/L) -> Mage (I/L) -> Arch Mage (I/L)",
    skills: {"recovery":[2000000,"Improved MP Recovery"],"increase":[2000001,"Improved MaxMP Increase"],"guard":[2001002,"Magic Guard"],"bolt":[2001004,"Energy Bolt"],"claw":[2001005,"Magic Claw"],"eater":[2200000,"MP Eater"],"meditation":[2201001,"Meditation"],"teleport":[2201002,"Teleport"],"slow":[2201003,"Slow"],"cold":[2201004,"Cold Beam"],"thunder":[2201005,"Thunder Bolt"],"resistance":[2210000,"Partial Resistance"],"amp":[2210001,"Element Amplification"],"strike":[2211002,"Ice Strike"],"spear":[2211003,"Thunder Spear"],"seal":[2211004,"Seal"],"booster":[2211005,"Spell Booster"],"composition":[2211006,"Element Composition"],"warrior":[2221000,"Maple Warrior"],"bang":[2221001,"Big Bang"],"reflection":[2221002,"Mana Reflection"],"infinity":[2221004,"Infinity"],"demon":[2221003,"Ice Demon"],"ifrit":[2221005,"Ifrit"],"chain":[2221006,"Chain Lightning"],"blizzard":[2221007,"Blizzard"],"will":[2221008,"Hero's Will"]},
    builds: [
        {"name":"Magician","job":200,"start":8,"end":30,"initial":1,"path":[["bolt",1],["recovery",5],["increase",10],["claw",20],["guard",20],["recovery",16]]},
        {"name":"Wizard (I/L)","job":220,"start":30,"end":70,"initial":1,"path":[["teleport",1],["cold",1],["thunder",30],["eater",3],["meditation",20],["teleport",20],["cold",30],["eater",20],["slow",1]]},
        {"name":"Mage (I/L)","job":221,"start":70,"end":120,"initial":1,"path":[["strike",1],["amp",3],["booster",11],["strike",30],["amp",30],["booster",20],["composition",30],["resistance",20],["seal",20],["spear",1]]},
        {"name":"Arch Mage (I/L)","job":222,"start":120,"end":200,"initial":3,"path":[["blizzard",1],["chain",1],["demon",5],["ifrit",1],["blizzard",10],["warrior",10],["chain",30],["blizzard",30],["infinity",30],["ifrit",30],["warrior",30],["reflection",30],["demon",30],["will",5],["bang",28]]}
    ],
    notes: ["Max Improved MaxMP Increase early; its level-up benefit is not retroactive. Magic Claw needs Energy Bolt 1. This route spends continuously; a budget-conscious player may pause Claw at 10 and save points before raising it to 20. Keep Magic Guard active when needed and enough MP to absorb damage.","Mobbing route: Teleport and Cold Beam give mobility and freeze, then Thunder Bolt handles groups. Cold Beam first is an alternative for single-target training. MP Eater 3 unlocks Meditation; Teleport 5 unlocks Slow. Check elemental resistance before choosing a spell.","Ice Strike is the main training skill. Element Amplification increases damage and MP use; Amplification 3 unlocks Spell Booster and Seal. This route gets Booster early, then builds Strike and Amplification. Keep extra MP for spells and Magic Guard.","Suggested mixed farming/combat route. Unlock Blizzard and books normally. You may keep Blizzard at 10 until books, SP and potion budget justify raising it; this route spends continuously. Chain Lightning handles smaller groups; Ice Demon 5 unlocks Ifrit. Infinity helps MP use. Train where your spells kill efficiently."],
    gearNote: "INT is the main stat. LUK is primarily an equipment requirement: compare the INT sacrificed against equipment gained. Check actual requirements and availability. Physical weapon attack is not a spell-damage upgrade.",
    gear: [
        {"min":8,"max":34,"label":"8~34","rows":[["Weapon","Wooden Wand (8, no LUK). Upgrade when the magic attack gained justifies any stat requirement."],["Hat","Affordable magician hat or all-class hat with useful INT."],["Clothes","Useful INT clothes; a Bathrobe is available from 20 for a low-LUK setup."],["Shield","Stolen Fence / Pan Lid for early defense; Maple Shield from 20 if obtainable."],["Gloves / cape","Use INT gear you can equip; save expensive scrolling for worthwhile upgrades."],["Shoes","Useful speed or INT/LUK shoes; account for any LUK requirement."],["Potions","Stock HP and MP potions. Low MP also weakens Magic Guard protection."]]},
        {"min":35,"max":69,"label":"35~69","rows":[["Weapon","Maple Staff (35), Maple Lama Staff / Nocturnal Staff (43), Maple Shine Wand / Maple Wisdom Staff (64): all have no LUK requirement."],["Hat","Useful INT hat; an earned boss helmet is a later option."],["Clothes","INT-scrolled Bathrobe or magician armor if equipment already supplies the required LUK."],["Shield","Maple Shield has ten slots. INT/magic-attack scrolling is useful but costly."],["Gloves","Glitter Gloves from 50 have no LUK requirement; compare useful INT/magic attack."],["Cape","Purple Adventurer Cape from 50 provides magic attack; compare INT-scrolled capes."],["Eye / face","Useful INT accessories when affordable; Broken Glasses if earned."],["Earrings / shoes","INT-scrolled earrings; useful-stat shoes without wasting AP solely to equip them."],["Pendant","Spiegelmann's Necklace; Silver Deputy Star later if obtained."]]},
        {"min":70,"max":129,"label":"70~129","rows":[["Weapon","Elemental Wand 3 from 70 is a no-LUK ice-focused option; compare its elemental fit with your spells."],["Hat","Earned Zakum Helmet or another useful INT hat."],["Clothes","INT-scrolled Bathrobe or better INT overall with manageable LUK requirements."],["Shield","Useful INT/magic-attack Maple Shield; count both stats when comparing upgrades."],["Gloves","Glitter Gloves or Flamekeeper Cordon (both 50, no LUK); compare rolled/scrolled stats."],["Cape","INT-scrolled Purple Adventurer Cape or another cape with better INT and magic attack."],["Earrings","INT-scrolled earrings; upgrade within your budget."],["Eye / face / shoes","Useful INT accessories and shoes; speed helps between Teleports."],["Pendant","Silver Deputy Star until a useful earned boss pendant is available."],["Survival","Magic Guard still needs HP/MP management. Dispel and low MP can make a mage vulnerable."]]},
        {"min":130,"max":200,"label":"130~200","rows":[["Weapon","Elemental Wand 7 from 130 is a no-LUK ice-focused goal. Keep a good Wand 3 until the upgrade is worthwhile."],["Hat","Earned Zakum / suitable Targa hat; compare actual INT and useful secondary stats."],["Clothes","High-INT overall; extra required LUK should earn its cost in equipment stats."],["Shield","INT/magic-attack scrolled Maple Shield or better compatible magician shield."],["Gloves","Useful INT/magic-attack gloves; physical attack gloves do not improve spell damage."],["Cape","Compare total INT and magic attack on scrolled capes."],["Earrings / eye / face","INT-focused accessories; upgrade gradually rather than chasing perfect rolls."],["Pendant","Horntail Necklace can provide substantial INT. Compare actual INT and magic attack with other pendants."],["Shoes","Useful INT shoes with achievable requirements; keep earlier gear if an upgrade costs too much INT."],["Books / potions","Budget for unlocks, books and ultimate spells. More damage is valuable when it reduces hits needed to kill."]]}
    ]
});
guideDefinitions.push({
    name: "Hero",
    route: "Warrior -> Fighter -> Crusader -> Hero",
    skills: {"hpRecovery":[1000000,"Improved HP Recovery"],"hpIncrease":[1000001,"Improved MaxHP Increase"],"endure":[1000002,"Endure"],"body":[1001003,"Iron Body"],"power":[1001004,"Power Strike"],"slash":[1001005,"Slash Blast"],"mastery":[1100000,"Sword Mastery"],"booster":[1101004,"Sword Booster"],"rage":[1101006,"Rage"],"powerGuard":[1101007,"Power Guard"],"mpRecovery":[1110000,"Improving MP Recovery"],"shieldMastery":[1110001,"Shield Mastery"],"combo":[1111002,"Combo Attack"],"panic":[1111003,"Panic : Sword"],"coma":[1111005,"Coma: Sword"],"crash":[1111007,"Armor Crash"],"shout":[1111008,"Shout"],"achilles":[1120004,"Achilles"],"guardian":[1120005,"Guardian"],"advanced":[1120003,"Advanced Combo Attack"],"warrior":[1121000,"Maple Warrior"],"magnet":[1121001,"Monster Magnet"],"stance":[1121002,"Power Stance"],"rush":[1121006,"Rush"],"brandish":[1121008,"Brandish"],"enrage":[1121010,"Enrage"],"will":[1121011,"Hero's Will"]},
    builds: [
        {"name":"Warrior","job":100,"start":10,"end":30,"initial":1,"path":[["power",1],["hpRecovery",5],["hpIncrease",10],["slash",20],["power",20],["endure",5],["hpRecovery",6]]},
        {"name":"Fighter","job":110,"start":30,"end":70,"initial":1,"path":[["mastery",5],["booster",6],["mastery",20],["rage",20],["powerGuard",30],["booster",20],["endure",8],["body",20],["hpRecovery",14]]},
        {"name":"Crusader","job":111,"start":70,"end":120,"initial":1,"path":[["combo",30],["coma",30],["panic",30],["shout",30],["crash",20],["mpRecovery",11]]},
        {"name":"Hero","job":112,"start":120,"end":200,"initial":3,"path":[["rush",1],["brandish",30],["advanced",30],["stance",30],["warrior",20],["achilles",30],["rush",30],["will",5],["enrage",30],["warrior",30],["magnet",28]]}
    ],
    notes: ["Max Improved MaxHP Increase early for its level-up benefit. Power Strike 1 unlocks Slash Blast. Accuracy gear can solve misses without committing every spare AP to DEX.","Sword route with no Final Attack. Sword Mastery 5 unlocks Booster; Rage 3 unlocks Power Guard. Later second-job points fill older utility skills. Axe builds need matching Axe Mastery and Booster instead; this is not a hybrid route.","Combo Attack is the first priority. Keep Power Strike and Slash Blast for ordinary attacks; use Panic/Coma as finishers consuming combo orbs. Coma favors groups. Shout 3 is needed for Armor Crash. Axe users need matching axe finishers.","Brandish becomes the main attack. Advanced Combo Attack needs Combo Attack 30 and its unlock. Rush 1 gives mobility. This two-handed sword route leaves Guardian for shield users, who can prioritize it over late utility. Enrage competes with attack potions; do not assume their attack bonuses add together."],
    gearNote: "STR supplies damage; use enough accuracy to hit. This route uses swords. Compare actual attack, speed, shield cost and party Speed Infusion before choosing an endgame weapon.",
    gear: [
        {"min":10,"max":34,"label":"10~34","rows":[["Weapon","Affordable warrior sword; Power Strike for single targets, Slash Blast for groups."],["Hat","Bamboo Hat or affordable accuracy/DEX hat."],["Clothes","Warrior armor or Bathrobe from 20; retain enough accuracy."],["Gloves","Accuracy gloves while missing; attack gloves when hits are reliable."],["Cape / shoes","Useful speed/DEX shoes; stat cape when available."],["Stats","STR for damage; DEX/accuracy as needed after accounting for gear and monster level."]]},
        {"min":35,"max":69,"label":"35~69","rows":[["Two-handed sword","Lionheart from 35; later Lion's Fang or Maple Soul Rohen from 64. Compare speed and accuracy as well as attack."],["One-handed alternative","Maple swords or another sword with a useful shield; still use Sword Mastery/Booster."],["Hat / eye","Accuracy/DEX hat; Broken Glasses if earned."],["Clothes","DEX/accuracy overall while needed; move toward STR once hits are dependable."],["Gloves","Affordable attack-scrolled Work Gloves."],["Cape / shoes","Useful STR/DEX cape and speed/DEX shoes."],["Pendant","Spiegelmann's Necklace or useful earned pendant."]]},
        {"min":70,"max":119,"label":"70~119","rows":[["Two-handed sword","Strong Maple Soul Rohen; Stonetooth Sword (100, 120 DEX) or Dragon Claymore (110, 340 STR) when worthwhile. Count the stat cost."],["One-handed alternative","Red Katana (standard version 70), Sparta (one-handed version 100), Dragon Carabella (110). Duplicate event variants exist; inspect the item."],["Shield","One-handed swords need a worthwhile attack shield. Maple Shield has ten slots; scrolling weapon and shield is costly."],["Hat / accessories","Earned Zakum Helmet; accuracy/DEX eye and face gear while needed."],["Clothes","STR-focused warrior overall once accuracy is covered."],["Gloves / cape / shoes","Attack gloves, useful-stat cape and shoes; attack cape/shoes are later budget goals."],["Pendant","Silver Deputy Star; Mark of Naricain from 110 if earned."]]},
        {"min":120,"max":200,"label":"120~200","rows":[["Two-handed sword","Well-scrolled Dragon Claymore, Stonetooth Sword or Timeless Nibleheim (120, 370 STR). Compare gear and access to Speed Infusion."],["One-handed alternative","Dragon Carabella plus attack-scrolled shield; Guardian requires a shield."],["Hat","Useful earned boss helmet scrolled for needed stats."],["Clothes","STR-focused armor with enough accuracy and any weapon DEX requirement."],["Gloves","Attack-scrolled Work Gloves or Stormcaster Gloves."],["Cape / shoes","Attack cape and Facestompers as budget permits."],["Eye / face / earrings","STR where available; DEX/accuracy for misses and Stonetooth requirements."],["Pendant","Horntail Necklace or Mark of Naricain; compare actual rolls and stat requirements."],["Attack buffs","Weapon multipliers and attack buffs can change the one-handed/two-handed comparison."]]}
    ]
});
guideDefinitions.push({
    name: "Bishop",
    route: "Magician -> Cleric -> Priest -> Bishop",
    skills: {"recovery":[2000000,"Improved MP Recovery"],"increase":[2000001,"Improved MaxMP Increase"],"guard":[2001002,"Magic Guard"],"bolt":[2001004,"Energy Bolt"],"claw":[2001005,"Magic Claw"],"eater":[2300000,"MP Eater"],"teleport":[2301001,"Teleport"],"heal":[2301002,"Heal"],"invincible":[2301003,"Invincible"],"bless":[2301004,"Bless"],"arrow":[2301005,"Holy Arrow"],"resistance":[2310000,"Elemental Resistance"],"dispel":[2311001,"Dispel"],"door":[2311002,"Mystic Door"],"symbol":[2311003,"Holy Symbol"],"ray":[2311004,"Shining Ray"],"doom":[2311005,"Doom"],"dragon":[2311006,"Summon Dragon"],"warrior":[2321000,"Maple Warrior"],"bang":[2321001,"Big Bang"],"reflection":[2321002,"Mana Reflection"],"infinity":[2321004,"Infinity"],"angel":[2321007,"Angel Ray"],"bahamut":[2321003,"Bahamut"],"resurrection":[2321006,"Resurrection"],"genesis":[2321008,"Genesis"],"will":[2321009,"Hero's Will"],"shield":[2321005,"Holy Shield"]},
    builds: [
        {"name":"Magician","job":200,"start":8,"end":30,"initial":1,"path":[["bolt",1],["recovery",5],["increase",10],["claw",20],["guard",20],["recovery",16]]},
        {"name":"Cleric","job":230,"start":30,"end":70,"initial":1,"path":[["teleport",1],["heal",30],["eater",20],["teleport",20],["invincible",20],["bless",20],["arrow",11]]},
        {"name":"Priest","job":231,"start":70,"end":120,"initial":1,"path":[["dispel",3],["door",1],["symbol",30],["ray",30],["dispel",20],["resistance",20],["door",20],["dragon",15],["doom",16]]},
        {"name":"Bishop","job":232,"start":120,"end":200,"initial":3,"path":[["resurrection",1],["genesis",1],["bahamut",1],["warrior",10],["genesis",10],["shield",1],["resurrection",10],["genesis",30],["warrior",20],["bahamut",30],["shield",30],["infinity",30],["angel",30],["warrior",30],["will",5],["reflection",30],["bang",18]]}
    ],
    notes: ["Max Improved MaxMP Increase early; its level-up benefit is not retroactive. Magic Claw needs Energy Bolt 1. This route spends continuously; a budget-conscious player may pause Claw at 10 and save points before raising it to 20. Keep Magic Guard active when needed and enough MP to absorb damage.","Teleport 1 for movement, then Heal for undead training and support. Heal 5 unlocks Invincible; Invincible 5 unlocks Bless. MP Eater and higher Teleport reduce potion costs. Magic Claw remains useful against monsters Heal cannot damage.","Support-first: Dispel 3 unlocks Holy Symbol and Mystic Door. Keep Holy Symbol refreshed with a party; Shining Ray adds damage. Mystic Door consumes Magic Rocks. Summon Dragon 15 is needed for Bahamut. Dispel does not remove every ailment.","Unlock Genesis and skills through quests/books normally. This route mixes farming and support; boss-focused players may move Resurrection and Holy Shield earlier. Genesis 10 can be a useful pause until more damage reduces hits per kill and potions are affordable. Beware Heal around Zombify; restore Magic Guard after Dispel."],
    gearNote: "INT is the main stat. LUK is primarily an equipment requirement: compare the INT sacrificed against equipment gained. Check actual requirements and availability. Physical weapon attack is not a spell-damage upgrade. This includes Cleric and Priest gear.",
    gear: [
        {"min":8,"max":34,"label":"8~34","rows":[["Weapon","Wooden Wand (8, no LUK). Upgrade when the magic attack gained justifies any stat requirement."],["Hat","Affordable magician hat or all-class hat with useful INT."],["Clothes","Useful INT clothes; a Bathrobe is available from 20 for a low-LUK setup."],["Shield","Stolen Fence / Pan Lid for early defense; Maple Shield from 20 if obtainable."],["Gloves / cape","Use INT gear you can equip; save expensive scrolling for worthwhile upgrades."],["Shoes","Useful speed or INT/LUK shoes; account for any LUK requirement."],["Potions","Stock HP and MP potions. Low MP also weakens Magic Guard protection."]]},
        {"min":35,"max":69,"label":"35~69","rows":[["Weapon","Maple Staff (35), Maple Lama Staff / Nocturnal Staff (43), Maple Shine Wand / Maple Wisdom Staff (64): all have no LUK requirement."],["Hat","Useful INT hat; an earned boss helmet is a later option."],["Clothes","INT-scrolled Bathrobe or magician armor if equipment already supplies the required LUK."],["Shield","Maple Shield has ten slots. INT/magic-attack scrolling is useful but costly."],["Gloves","Glitter Gloves from 50 have no LUK requirement; compare useful INT/magic attack."],["Cape","Purple Adventurer Cape from 50 provides magic attack; compare INT-scrolled capes."],["Eye / face","Useful INT accessories when affordable; Broken Glasses if earned."],["Earrings / shoes","INT-scrolled earrings; useful-stat shoes without wasting AP solely to equip them."],["Pendant","Spiegelmann's Necklace; Silver Deputy Star later if obtained."]]},
        {"min":70,"max":129,"label":"70~129","rows":[["Weapon","Maple Shine Wand / Maple Wisdom Staff remain no-LUK choices. Compare higher-magic-attack wands against LUK needed; an elemental wand is not automatically best for Holy damage."],["Hat","Earned Zakum Helmet or another useful INT hat."],["Clothes","INT-scrolled Bathrobe or better INT overall with manageable LUK requirements."],["Shield","Useful INT/magic-attack Maple Shield; count both stats when comparing upgrades."],["Gloves","Glitter Gloves or Flamekeeper Cordon (both 50, no LUK); compare rolled/scrolled stats."],["Cape","INT-scrolled Purple Adventurer Cape or another cape with better INT and magic attack."],["Earrings","INT-scrolled earrings; upgrade within your budget."],["Eye / face / shoes","Useful INT accessories and shoes; speed helps between Teleports."],["Pendant","Silver Deputy Star until a useful earned boss pendant is available."],["Survival","Magic Guard still needs HP/MP management. Dispel and low MP can make a mage vulnerable."]]},
        {"min":130,"max":200,"label":"130~200","rows":[["Weapon","Compare Maple weapons, ordinary wands and elemental options for Holy spells. Timeless Enreal Tear is level 118 with 120 LUK here; account for the INT cost before changing your build."],["Hat","Earned Zakum / suitable Targa hat; compare actual INT and useful secondary stats."],["Clothes","High-INT overall; extra required LUK should earn its cost in equipment stats."],["Shield","INT/magic-attack scrolled Maple Shield or better compatible magician shield."],["Gloves","Useful INT/magic-attack gloves; physical attack gloves do not improve spell damage."],["Cape","Compare total INT and magic attack on scrolled capes."],["Earrings / eye / face","INT-focused accessories; upgrade gradually rather than chasing perfect rolls."],["Pendant","Horntail Necklace can provide substantial INT. Compare actual INT and magic attack with other pendants."],["Shoes","Useful INT shoes with achievable requirements; keep earlier gear if an upgrade costs too much INT."],["Books / potions","Budget for unlocks, books and ultimate spells. More damage is valuable when it reduces hits needed to kill."]]}
    ]
});
guideDefinitions.push({
    name: "Corsair",
    route: "Pirate -> Gunslinger -> Outlaw -> Corsair",
    skills: {"bulletTime":[5000000,"Bullet Time"],"kick":[5001002,"Sommersault Kick"],"double":[5001003,"Double Shot"],"dash":[5001005,"Dash"],"mastery":[5200000,"Gun Mastery"],"booster":[5201003,"Gun Booster"],"invisible":[5201001,"Invisible Shot"],"grenade":[5201002,"Grenade"],"blank":[5201004,"Blank Shot"],"wings":[5201005,"Wings"],"recoil":[5201006,"Recoil Shot"],"burst":[5210000,"Burst Fire"],"octopus":[5211001,"Octopus"],"gaviota":[5211002,"Gaviota"],"flame":[5211004,"Flamethrower"],"ice":[5211005,"Ice Splitter"],"beacon":[5211006,"Homing Beacon"],"elemental":[5220001,"Elemental Boost"],"bullseye":[5220011,"Bullseye"],"octopi":[5220002,"Wrath of the Octopi"],"warrior":[5221000,"Maple Warrior"],"aerial":[5221003,"Aerial Strike"],"rapid":[5221004,"Rapid Fire"],"hypnotize":[5221009,"Hypnotize"],"ship":[5221006,"Battleship"],"cannon":[5221007,"Battleship Cannon"],"torpedo":[5221008,"Battleship Torpedo"]},
    builds: [
        {"name":"Pirate","job":500,"start":10,"end":30,"initial":1,"path":[["double",20],["dash",10],["bulletTime",20],["kick",11]]},
        {"name":"Gunslinger","job":520,"start":30,"end":70,"initial":1,"path":[["invisible",1],["mastery",5],["booster",6],["mastery",20],["wings",5],["recoil",20],["invisible",20],["booster",20],["wings",10],["blank",20],["grenade",11]]},
        {"name":"Outlaw","job":521,"start":70,"end":120,"initial":1,"path":[["burst",20],["flame",30],["ice",26],["octopus",30],["beacon",30],["gaviota",15]]},
        {"name":"Corsair","job":522,"start":120,"end":200,"initial":3,"path":[["ship",1],["cannon",30],["bullseye",20],["rapid",30],["warrior",20],["elemental",30],["octopi",20],["torpedo",30],["aerial",30],["ship",10],["warrior",30],["hypnotize",13]]}
    ],
    notes: ["Gun-pirate route: max Double Shot; Burst Fire later needs Double Shot 20. Dash adds mobility. Move Sommersault Kick earlier for close-range groups by delaying Bullet Time if desired. Carry a gun and level-appropriate bullets.","Invisible Shot 1 for mobbing, then Mastery for stable damage. Mastery 5 unlocks Booster; Wings 5 unlocks Recoil Shot. Jump, turn and Recoil for movement; Wings controls falling. Recoil moves away from the direction fired.","Single-target-first; mobbing builds can prioritize Flamethrower. Low-level Burst Fire may be weaker than Double Shot: you may save SP to reach 11 together. This route spends continuously. Ice Splitter 26 favors freeze duration; Octopus 30, Homing Beacon 30 and Gaviota 15 unlock fourth-job skills.","Bossing route: Battleship 1 unlocks Cannon/Torpedo; Burst Fire 20 unlocks Rapid Fire. Beacon 30 unlocks Bullseye; Octopus 30 unlocks Wrath of the Octopi. Use Cannon aboard ship, Rapid Fire when dismounted. Practice dismounting around dangerous attacks and manage durability. Books/unlocks still apply; late utility is flexible."],
    gearNote: "DEX supplies gun damage; STR is primarily for equipment. Maple guns allow low-STR early play. Count equipment STR before adding AP. Upgrade bullets at their actual level requirements.",
    gear: [
        {"min":10,"max":34,"label":"10~34","rows":[["Gun","Strongest affordable gun you can equip. Early damage is modest; attack gloves help."],["Bullets","Bullet (10, +10 attack); Split Bullet (30, +12). Carry several recharged stacks."],["Hat","Useful DEX hat or Bandana."],["Clothes","DEX pirate overall; check STR and DEX requirements."],["Gloves","Affordable attack-scrolled Work Gloves."],["Cape / shoes","Useful-stat cape and speed/jump shoes; Recoil and Wings add later mobility."]]},
        {"min":35,"max":69,"label":"35~69","rows":[["Gun","Maple Gun (35), Maple Storm Pistol (43), Maple Canon Shooter (64): no STR requirement."],["Bullets","Split Bullet at 30, Mighty Bullet at 50 (+14 attack). Higher tiers require their listed level."],["Hat / eye","Useful pirate hat or Bandana; Broken Glasses if earned."],["Clothes","DEX overall or Bathrobe; preserve equipment STR needed for later guns."],["Gloves","Attack-scrolled Work Gloves."],["Cape / shoes","Useful STR/DEX cape and speed/jump shoes."],["Pendant","Spiegelmann's Necklace; useful later pendant when earned."]]},
        {"min":70,"max":119,"label":"70~119","rows":[["Gun","Keep a good Maple Canon Shooter until an upgrade is worthwhile: The Peacemaker (90, 90 STR), Concerto (100, 100 STR), Dragonfire Revolver (110, 110 STR)."],["Bullets","Vital Bullet (70, +16 attack), Shiny Bullet (90, +18), Eternal Bullet (110, +20)."],["Hat","Earned Zakum Helmet can help STR requirements and DEX."],["Clothes","White Marquini (90, 90 STR/DEX) can supply STR for later guns; compare rolls and scrolling."],["Gloves","Attack-scrolled Work Gloves or stronger attack glove."],["Cape / shoes","STR/DEX gear; compare attack accessories when affordable."],["Eye / face / earrings","Useful DEX accessories; equipment STR can reduce base STR needed for guns."],["Pendant","Silver Deputy Star; Mark of Naricain from 110 if earned."]]},
        {"min":120,"max":200,"label":"120~200","rows":[["Gun","Well-scrolled Concerto / Dragonfire Revolver; Timeless Blindness (120, 120 STR) as a later goal. Compare attack gained with DEX lost to requirements."],["Bullets","Eternal Bullet: 110, +20 attack, 3,000 base stack before Mastery. Recharge enough stacks for long fights."],["Hat","Useful earned boss helmet with STR/DEX supporting your gun."],["Clothes","Strong White Marquini or other overall. An overall cannot cheaply supply the STR to equip itself if its own requirement is too high."],["Gloves","Attack-scrolled Work Gloves or Stormcaster Gloves."],["Cape / shoes","Attack cape and Facestompers as budget permits; retain useful stat gear until upgrades are worthwhile."],["Eye / face / earrings","DEX accessories and enough equipment STR to meet the gun requirement."],["Pendant","Horntail Necklace or Mark of Naricain; compare actual attack and stats."],["Boss preparation","HP/MP potions, All Cures and enough bullets. Manage ship durability and keep safe distance."]]}
    ]
});


// Transcript additions on the existing SP/gear pages.
guideDefinitions[0].gearNote += " LUK is the damage priority; equipment DEX can reduce base DEX needed for a claw. Attack gloves, capes and shoes matter alongside the claw and stars.";
guideDefinitions[0].notes[2] += " Shadow Partner consumes Summoning Rocks. Keep recharged stars and rocks stocked.";
guideDefinitions[1].gear[2].rows.push(["Face / earrings", "Sad Mask or another useful accuracy face accessory; STR earrings where available, or DEX to cover misses."]);
guideDefinitions[1].gear[3].rows.push(["Boss access", "Boss gear is a survival challenge for a low-HP Beginner; availability is not the same as earning it safely."]);
guideDefinitions[2].gear[3].rows.push(["Eye / face / earrings", "Accuracy or DEX while missing, then STR where available. Keep Hyper Body and your Berserk potion plan in mind."]);
guideDefinitions[3].gear[3].rows.push(["Later bow", "Timeless Engaw (120, 125 STR) is an expensive goal; count the STR cost and compare actual scrolled/leveled attack."]);

function selectClassGuide(index) {
    classGuide = index;
    skills = guideDefinitions[index].skills;
    builds = guideDefinitions[index].builds;
    gearBuilds = guideDefinitions[index].gear;
    stage = 0; gearBand = 0;
    classMenu();
}

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
        var rate = b.spPerLevel || 3;
        var budget = s < which ? b.initial + rate * (b.end - b.start)
            : (level < b.start ? 0 : b.initial + rate * (Math.min(level, b.end) - b.start));
        if (b.spCap !== undefined) budget = Math.min(b.spCap, budget);
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
        (navigation || "") + (classBack ? link(901, "Back to Class: " + guideDefinitions[classGuide].name) : "") +
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
    var text = "#eClass Guides#n\r\n\r\n#b";
    text += link(1, guideDefinitions[1].name);
    for (var i = 0; i < guideDefinitions.length; i++) {
        if (i !== 1) text += link(i, guideDefinitions[i].name);
    }
    show(text);
}
function classMenu() {
    page = "class";
    var guide = guideDefinitions[classGuide];
    show("#eClass: " + guide.name + "#n\r\n" + guide.route + "\r\n\r\n#b" +
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
    var b = gearBuilds[gearBand];
    var text = "#e" + guideDefinitions[classGuide].name + " gear - level " + b.label + "#n\r\n" +
        "Your level: " + cm.getPlayer().getLevel() +
        "\r\n" + guideDefinitions[classGuide].gearNote + "\r\n\r\n";
    for (var i = 0; i < b.rows.length; i++) {
        text += "#e" + b.rows[i][0] + ":#n\r\n" +
            (b.rows[i][1] === "/" ? "No recommendation in the source." : b.rows[i][1]) + "\r\n\r\n";
    }
    text += "#b";
    text += link(2, "Choose level range") + link(3, "Return to my level's gear");
    show(text);
}
function gearBandsMenu() {
    page = "gearBands";
    var text = "#e" + guideDefinitions[classGuide].name + " gear ranges#n\r\nChoose a level range. At shared boundaries, the newer range is shown.\r\n\r\n#b";
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
    ["MV's Lair PQ",140,255,4,6,674030100,9220018,"Talk to Charles to assemble a dedicated bot crew. Collect 15 Secret Letters, check with the upper-left guide, and gather at the tunnel before fighting MV. Boss loot moves on after ten seconds, followed by a ten-minute treasure room."],
    ["Horntail Trial PQ",120,255,6,6,240050000,2083001,"Complete the six-player trial with five dedicated level-180 companions. Your own cave prerequisites still apply. Lead the normal key hand-ins; assemble again at the raid NPC afterward to expand to twelve participants."],
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
    ["Horntail",100,255,6,30,240050400,2083004,"Assemble eleven dedicated level-180 companions in two parties, then register and start the normal expedition. Your own cave prerequisites still apply. Each party has a Bishop and Dark Knight. The crew waits for your portals and main-chamber crystal."],
    ["Pink Bean",120,255,6,30,270050000,2141001,"Prepare your own Temple of Time prerequisites and Marble of Chaos. The Forgotten Temple Manager can assemble twenty-three level-180 companions in four parties. Register normally and fight five statue waves before Pink Bean within the 60-minute limit. Player testing of this larger crew is still pending."],
    ["Balrog",50,255,6,30,105100100,1061014,"Register the normal Balrog expedition at the temple entrance. This guide does not offer the unreferenced Easy Balrog event or bypass its normal entry conditions."],
    ["Scarlion and Targa",100,255,6,30,551030100,9270047,"Register the Malaysia expedition through the entrance NPC. Bring the required quest progress and summoning items for Scarlion and Targa."],
    ["Showa Boss",100,255,3,30,801040004,9120201,"Register the Showa boss expedition through the local NPC. Follow the normal entry requirements and encounter progression."],
    ["Papulatus",1,255,1,6,220080000,0,"Enter the Clocktower boss encounter through the normal portal with your party. Complete the required quest preparations and bring the summoning piece. The local event's broad level range does not bypass quest requirements."],
    ["Captain Latanica",1,255,1,6,541010060,0,"Enter through the Ghost Ship's normal boss portal with an eligible party. Follow the local quest and access requirements; this is an instanced boss battle rather than an expedition registration NPC."]
];
var companionActivities = {"Kerning PQ":true,"Ludibrium PQ":true,"Carnival PQ":true,"Carnival PQ 2":true,"Ellin Forest PQ":true,"Orbis PQ":true,"Ludibrium Maze PQ":true,"Magatia PQ":true,"MV's Lair PQ":true,"Guild PQ":true,"Zakum":true,"Horntail Trial PQ":true,"Horntail":true,"Pink Bean":true};
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
        link(0,"Party Quests") + "\r\n" + link(1,"Challenges") + "\r\n" + link(2,"PVP PQs"));
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
        (classGuide === 1 ? "Reference build for a character remaining Beginner." : "Suggested SP route; assumes on-time job advancement and earlier stages of this build. Adjust optional skills to your training and available books.") + "\r\n\r\n#ePoints for this level:#n\r\n";
    var spent = 0;
    for (var key in now) {
        var delta = now[key] - (before[key] || 0);
        if (delta > 0) { text += "+" + delta + " " + skills[key][1] + " (target " + now[key] + ")\r\n"; spent += delta; }
    }
    if (!spent) text += classGuide === 1 ? "Ordinary Beginner SP is earned only at levels 2-7.\r\n" : "Core build complete; remaining SP is your choice.\r\n";
    var job = cm.getPlayer().getJob().getId();
    var matches = false;
    for (var i = 0; i < builds.length; i++) if (builds[i].job === job) matches = true;
    if (matches) {
        text += "\r\n#eBuild targets / your current points:#n\r\n";
        for (var key in now) text += skills[key][1] + ": " + now[key] + " / " + cm.getPlayer().getSkillLevel(skills[key][0]) + "\r\n";
    } else text += "\r\nThis is a reference for " + guideDefinitions[classGuide].name + ", not your current class.\r\n";
    var note = guideDefinitions[classGuide].notes[stage];
    if (note) text += "\r\n" + note + "\r\n";
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
        if (selection >= 0 && selection < guideDefinitions.length) selectClassGuide(selection);
    } else if (page === "class") {
        if (selection === 0) {
            var job = cm.getPlayer().getJob().getId();
            stage = 0;
            for (var i = 0; i < builds.length; i++) if (builds[i].job === job) stage = i;
            buildLevel = Math.max(builds[stage].start, Math.min(builds[stage].end, cm.getPlayer().getLevel())); buildMenu();
        } else if (selection === 1) { gearBand = gearBandFor(cm.getPlayer().getLevel()); gearMenu(); }
        else if (selection === 700) classGuidesMenu();
        else cm.dispose();
    } else if (page === "gear") {
        if (selection === 2) gearBandsMenu();
        else if (selection === 3) { gearBand = gearBandFor(cm.getPlayer().getLevel()); gearMenu(); }
        else cm.dispose();
    } else if (page === "gearBands" && selection >= 0 && selection < gearBuilds.length) {
        gearBand = selection; gearMenu();
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
        else if (selection === 3) {
            page = "stages"; var text = "#eChoose a job stage#n\r\n#b";
            for (var i = 0; i < builds.length; i++) text += link(i, builds[i].name);
            show(text);
        }
        else cm.dispose();
    } else if (page === "buildNumber") {
        buildLevel = Math.max(builds[stage].start, Math.min(builds[stage].end, selection)); buildMenu();
    } else if (page === "stages" && selection >= 0 && selection < builds.length) {
        stage = selection; buildLevel = builds[stage].start; buildMenu();
    } else cm.dispose();
}
