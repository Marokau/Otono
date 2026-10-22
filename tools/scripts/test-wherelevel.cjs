// Run from the repository root: node tools/scripts/test-wherelevel.cjs
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
let output = '', disposed = false, warpedTo = 0;
const player = {getLevel: () => 82, getJob: () => ({getId: () => 411, name: () => 'HERMIT'}),
    getSkillLevel: () => 0, getDex: () => 25, getLuk: () => 380, isGM: () => true};
const ctx = vm.createContext({cm: {getPlayer: () => player, sendSimple: text => {
    output = text;
    if (text.includes('#L900#Main menu')) {
        const header = '\r\n\r\n#k#eMenu navigation#n\r\n#b';
        assert.equal(text.split(header).length, 2, 'One black bold navigation header per submenu');
        const [content, navigation] = text.split(header);
        assert.doesNotMatch(content, /#L\d+#Back to /, 'Back links belong below the header');
        assert.match(navigation, /#L900#Main menu#l\r\n#L999#Close#l\r\n$/);
        assert.doesNotMatch(navigation, /\r\n\r\n/, 'Navigation options stay together');
    }
},
    sendGetNumber: text => output = text, sendYesNo: text => output = text,
    warp: mapId => warpedTo = mapId, dispose: () => disposed = true},
    Java: {type: () => ({spots: () => ({size: () => 0, isEmpty: () => true})})}});
vm.runInContext(fs.readFileSync('scripts/npc/wherelevel.js', 'utf8'), ctx);
const sum = obj => Object.values(obj).reduce((a, b) => a + b, 0);
for (let stage = 0; stage < ctx.builds.length; stage++) {
    const b = ctx.builds[stage];
    for (let level = b.start; level <= b.end; level++) {
        const current = ctx.totalsFor(stage, level), previous = ctx.totalsFor(stage, level - 1);
        const delta = sum(current) - sum(previous);
        assert.ok(delta >= 0 && delta <= (level === b.start ? b.initial : 3), `SP budget at ${stage}/${level}`);
        if (stage < 3 || level <= 197) assert.equal(delta, level === b.start ? b.initial : 3);
    }
}
assert.equal(sum(ctx.totalsFor(0, 30)), 61);
assert.equal(sum(ctx.totalsFor(1, 70)), 61 + 121);
assert.equal(sum(ctx.totalsFor(2, 120)), 61 + 121 + 151);
assert.equal(ctx.totalsFor(1, 60).nimble, 11);
assert.equal(ctx.totalsFor(1, 70).drain, 1);
assert.equal(ctx.totalsFor(2, 82).jump, 2);
assert.equal(ctx.totalsFor(3, 120).triple, 2);
ctx.start(); assert.match(output, /Browse all chapters/); assert.doesNotMatch(output, /stats \(AP\)|My nostalgia training|another level/);
assert.doesNotMatch(output, /Guide source/);
assert.doesNotMatch(output, /Where bots/);
assert.deepEqual([...output.matchAll(/#L(\d+)#/g)].map(m => Number(m[1])), [5,2,6,999]);
assert.match(output, /Browse all chapters#l\r\n\r\n#L2#Class Guide#l\r\n\r\n#L6#Party Quests#l\r\n\r\n#L999#Close/);
ctx.action(1, 0, 2); assert.match(output, /Class Guides/); assert.match(output, /Night Lord/);
ctx.action(1, 0, 0); assert.match(output, /Class: Night Lord/); assert.match(output, /Gear/);
ctx.action(1, 0, 700); assert.equal(ctx.page, 'classes'); ctx.action(1, 0, 0);
ctx.action(1, 0, 0); assert.match(output, /Hermit - level 82/); assert.match(output, /\+2 Flash Jump/);
ctx.action(1, 0, 3); ctx.action(1, 0, 1); assert.match(output, /Assassin - level 30/);
ctx.action(1, 0, 900); ctx.action(1, 0, 5); ctx.action(1, 0, 4); assert.match(output, /Levels 70-89/);
ctx.action(1, 0, 0); ctx.action(1, 0, 0); assert.match(output, /Lycanthrope \(Lv 80\)/);
ctx.action(1, 0, 900); ctx.action(1, 0, 2); ctx.action(1, 0, 0); ctx.action(1, 0, 1);
assert.match(output, /gear - level 70~100/); assert.match(output, /Lord Pirate Hat/);
ctx.action(1, 0, 1); assert.match(output, /Page 2 of 3/); assert.match(output, /Top\(overall\)/);
ctx.action(1, 0, 1); assert.match(output, /Page 3 of 3/); assert.match(output, /Maple Skanda \/ Red Craven/);
ctx.action(1, 0, 0); assert.match(output, /Page 2 of 3/);
ctx.action(1, 0, 2); assert.match(output, /gear ranges/);
ctx.action(1, 0, 7); assert.match(output, /180~200\+/); assert.match(output, /Ninja Toad Headband/);
ctx.action(1, 0, 3); assert.match(output, /gear - level 70~100/); assert.match(output, /Page 1 of 3/);
ctx.action(1, 0, 901); assert.match(output, /Class: Night Lord/);
ctx.action(1, 0, 0); assert.match(output, /Flash Jump: 2 \/ 0/);
assert.equal(ctx.gearBuilds.length, 8);
for (const mob of ['Zombie Mushroom (Lv 24)', 'Horny Mushroom (Lv 22)', 'Brown Teddy (Lv 30)',
    'Platoon Chronos (Lv 41)', 'Headless Horseman (Lv 101)', 'Skelegon (Lv 110)',
    'Skelosaurus (Lv 113)', 'Papulatus Clock (Lv 125)', 'Female Boss / Anego (Lv 130)',
    'Primitive Boar (Lv 57)']) {
    assert.ok(ctx.training.some(row => row[2].includes(mob)), `Missing nostalgia entry: ${mob}`);
}
for (const mob of ['Memory Monk', 'Memory Monk Trainee', 'Memory Guardian', 'Chief Memory Guardian',
    'Qualm Monk', 'Qualm Monk Trainee', 'Qualm Guardian', 'Chief Qualm Guardian',
    'Oblivion Monk', 'Oblivion Monk Trainee', 'Oblivion Guardian', 'Chief Oblivion Guardian',
    'Dodo', 'Lilynouch', 'Lyka']) {
    assert.ok(ctx.training.some(row => row[2].startsWith(mob + ' (Lv ')), `Missing Temple mob: ${mob}`);
}
assert.ok(!ctx.training.some(row => /Ulu/i.test(row[2])));
const tauromacis = ctx.training.findIndex(row => row[2].startsWith('Tauromacis (Lv 70)'));
ctx.openRegion('sanctuary'); ctx.action(1, 0, 0); ctx.action(1, 0, tauromacis);
assert.match(output, /Teleport to #m105090500#/);
ctx.action(1, 0, 1); assert.match(output, /Teleport to .*105090500/);
ctx.action(1, 0, 0); assert.equal(warpedTo, 105090500);
for (const [level, expected] of [[1,0],[30,0],[31,0],[34,0],[35,1],[51,1],[54,1],[55,2],[70,3],[100,4],[135,5],[150,6],[180,7],[201,7]]) {
    assert.equal(ctx.gearBandFor(level), expected, `Gear range at ${level}`);
}
for (const band of ctx.gearBuilds) assert.equal(band.rows.length, 18);

// Review chapter flow, every destination, optional scrollable details, and teleport permissions.
ctx.home(); ctx.action(1, 0, 5); ctx.action(1, 0, 6);
assert.match(output, /Levels 120-149/);
ctx.action(1, 0, 100);
assert.match(output, /Stay awhile/); assert.doesNotMatch(output, /The Time Sphere/);
ctx.action(1, 0, 1); assert.match(output, /Mr. Bouffon/); assert.doesNotMatch(output, /A Piece of Crack/);
ctx.action(1, 0, 2); assert.match(output, /level 60/);
assert.match(output, /A Piece of Crack/);
assert.match(output, /level 80/);
assert.match(output, /The Time Sphere/);
assert.doesNotMatch(output, /Page \d|Previous details|#L11#/);
ctx.action(1, 0, 0); assert.equal(ctx.page, 'ludiQuest');
ctx.action(1, 0, 1); assert.match(output, /220050300/);
warpedTo = 0; ctx.action(0, 0, 0); assert.equal(warpedTo, 0);
ctx.ludiMenu(); ctx.action(1, 0, 2); ctx.action(1, 0, 2);
assert.match(output, /one-person party/);
assert.match(output, /Access and summoning/);
assert.doesNotMatch(output, /Page \d|Previous details|#L11#/);
ctx.action(1, 0, 0); ctx.action(1, 0, 1); ctx.action(1, 0, 0);
assert.equal(warpedTo, 220080000, 'Boss teleport must use the approach, not the instance');
assert.equal(ctx.ludiMobs.length, 6);
for (let index = 0; index < ctx.ludiMobs.length; index++) {
    ctx.ludiMenu(); ctx.action(1, 0, 0);
    assert.equal(ctx.page, 'ludiMobs');
    assert.doesNotMatch(output, /Choose a side|More monsters|Buffy|Death Teddy|Page /);
    const row = ctx.ludiMobs[index];
    assert.ok(row[1] >= 90 && row[1] <= 120);
    assert.ok(output.includes(row[0] + ' (Lv ' + row[1] + ') - ' + row[3]));
    ctx.action(1, 0, index); assert.ok(output.includes('Map ' + row[2]));
    ctx.action(1, 0, 0); assert.equal(ctx.page, 'ludiMobs');
    ctx.action(1, 0, index); ctx.action(1, 0, 1); ctx.action(1, 0, 0);
    assert.equal(warpedTo, row[2]);
}
ctx.ludiMenu(); ctx.action(1, 0, 0); ctx.action(1, 0, 103);
assert.equal(ctx.page, 'ludi');
player.isGM = () => false; warpedTo = 0;
for (const pick of [1, 2]) {
    ctx.ludiMenu(); ctx.action(1, 0, pick);
    assert.doesNotMatch(output, /#L1#Teleport/);
    ctx.action(1, 0, 1); assert.notEqual(ctx.page, 'ludiWarpConfirm');
}
ctx.ludiMenu(); ctx.action(1, 0, 0); ctx.action(1, 0, 0);
assert.doesNotMatch(output, /Teleport/); ctx.action(1, 0, 1);
assert.notEqual(ctx.page, 'ludiWarpConfirm'); assert.equal(warpedTo, 0);
player.isGM = () => true;
ctx.ludiMenu(); ctx.action(1, 0, 2); ctx.action(1, 0, 1);
player.isGM = () => false; ctx.action(1, 0, 0); assert.equal(warpedTo, 0);
ctx.action(0, 0, 0); assert.ok(disposed);
// Every chapter boundary selects exactly one chapter, including returning at level 200.
for (let level = 1; level <= 200; level++) {
    const chapter = ctx.tourChapters[ctx.chapterFor(level)];
    assert.ok(level >= chapter[0] && level <= chapter[1]);
}
player.isGM = () => true;
const visited = new Set();
for (let chapter = 0; chapter < ctx.tourChapters.length; chapter++) {
    ctx.home(); ctx.action(1, 0, 5); ctx.action(1, 0, chapter);
    assert.equal(ctx.page, 'training');
    assert.match(output, /Regional choices/);
    assert.match(output, /\r\n\r\n#k#eOptional episodes#n\r\n#b#L100#/);
    assert.doesNotMatch(output, /Party activities|#L700#Optional episodes/);
    assert.doesNotMatch(output, /\(Lv \d+\)/, 'Chapter must not dump mobs');
    for (const optional of [false, true]) {
        const keys = ctx.tourChapters[chapter][optional ? 5 : 4];
        for (let index = 0; index < keys.length; index++) {
            ctx.tourChapter = chapter; ctx.chapterMenu();
            ctx.action(1, 0, (optional ? 100 : 0) + index);
            const key = keys[index]; visited.add(key);
            assert.equal(ctx.tourRegion, key);
            if (key === 'ludi') { ctx.action(1, 0, 3); }
            else {
                const region = ctx.tourRegions[key];
                assert.equal(ctx.page, 'tourRegion');
                assert.ok(output.includes(region.summary));
                if (region.mobs.length) {
                    ctx.action(1, 0, 0);
                    assert.equal(ctx.page, 'trainingList');
                    const offered = [...output.matchAll(/#L(\d+)#/g)].map(m => Number(m[1])).filter(id => id < 700);
                    assert.deepEqual(offered, Array.from(region.mobs));
                    for (const mob of region.mobs) {
                        ctx.trainingList(); ctx.action(1, 0, mob);
                        assert.equal(ctx.page, 'detail');
                        assert.ok(output.includes(ctx.training[mob][2]));
                        ctx.action(1, 0, 0); assert.equal(ctx.page, 'trainingList');
                        ctx.action(1, 0, mob); ctx.action(1, 0, 1);
                        warpedTo = 0; ctx.action(0, 0, 0); assert.equal(warpedTo, 0);
                        ctx.trainingList(); ctx.action(1, 0, mob); ctx.action(1, 0, 1); ctx.action(1, 0, 0);
                        assert.equal(warpedTo, ctx.training[mob][3]);
                        assert.ok(![220080001,280030000,240060200,270050100].includes(warpedTo), 'No instance shortcut');
                        player.isGM = () => false; warpedTo = 0;
                        ctx.trainingList(); ctx.action(1, 0, mob);
                        assert.doesNotMatch(output, /Teleport/);
                        ctx.action(1, 0, 1); assert.equal(warpedTo, 0);
                        player.isGM = () => true;
                    }
                    ctx.trainingList(); ctx.action(1, 0, 700); assert.equal(ctx.page, 'tourRegion');
                }
                for (let story = 0; story < region.stories.length; story++) {
                    ctx.regionMenu(); ctx.action(1, 0, 1); ctx.action(1, 0, story);
                    const entry = region.stories[story];
                    assert.ok(output.includes(entry.intro));
                    ctx.action(1, 0, 2);
                    assert.equal(ctx.page, 'tourStoryMore');
                    assert.ok(output.includes(entry.details));
                    assert.doesNotMatch(output, /Page \d+ of/);
                    ctx.action(1, 0, 0); assert.equal(ctx.page, 'tourStory');
                    if (entry.map) {
                        ctx.action(1, 0, 1); assert.equal(ctx.page, 'tourWarpConfirm');
                        warpedTo = 0; ctx.action(0, 0, 0); assert.equal(warpedTo, 0);
                        ctx.storyMenu(false); ctx.action(1, 0, 1); ctx.action(1, 0, 0); assert.equal(warpedTo, entry.map);
                        ctx.storyMenu(false); ctx.action(1, 0, 1);
                        player.isGM = () => false; warpedTo = 0;
                        ctx.action(1, 0, 0); assert.equal(warpedTo, 0);
                        ctx.storyMenu(false); assert.doesNotMatch(output, /Teleport/);
                        ctx.action(1, 0, 1); assert.equal(ctx.page, 'tourStory');
                        player.isGM = () => true;
                    }
                    ctx.storyMenu(false); ctx.action(1, 0, 0); assert.equal(ctx.page, 'tourStories');
                    ctx.action(1, 0, 700); assert.equal(ctx.page, 'tourRegion');
                }
                ctx.regionMenu(); ctx.action(1, 0, 700);
            }
            assert.equal(ctx.page, 'training');
        }
    }
}
assert.equal(visited.size, Object.keys(ctx.tourRegions).length + 1, 'Every region is reachable');
// Preserve every old monster, even where it now lives in a boss story or the special Ludi pages.
const reachable = Object.values(ctx.tourRegions).flatMap(r => r.mobs.map(i => ctx.training[i][2])).join('\n') +
    JSON.stringify(Object.values(ctx.tourRegions).flatMap(r => r.stories)) + JSON.stringify(ctx.ludiMobs) + 'Papulatus Clock';
for (const row of ctx.training) {
    const name = row[2].match(/^(.+?) \(Lv /)?.[1];
    if (name) assert.ok(reachable.includes(name), `Unreachable saved mob: ${name}`);
}
ctx.home(); ctx.action(1, 0, 6); assert.match(output, /Kerning PQ: 21-30/);
ctx.action(1, 0, 700); assert.equal(ctx.page, 'tourChapters');
ctx.action(1, 0, 0); ctx.action(1, 0, 1); assert.match(output, /Ellinia and Kerning/);
ctx.action(1, 0, 0); assert.match(output, /Slime \(Lv 6\)/); assert.match(output, /Bubbling \(Lv 15\)/);
assert.doesNotMatch(output, /Blue Snail|Spirit Viking/);
ctx.action(1, 0, 900); assert.equal(ctx.page, 'home');
disposed = false; ctx.action(1, 0, 999); assert.equal(disposed, true);
console.log('PASS: SP/gear, all eight regional chapters, every mob/story route, Back links, scrollable details, preserved destinations, and GM-only confirmed teleports.');
