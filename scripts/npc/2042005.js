/**
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Drago (MapleStorySA)
 2.0 - Second Version by Jayd - translated CPQ contents to English
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;
var botMenu = false;
var cpqMinLvl = 51;
var cpqMaxLvl = 70;
var cpqMinAmt = 2;
var cpqMaxAmt = 6;

function start() {
    status = -1;
    if (cm.getMapId() == 980030000 && !(cm.getPlayer().getClient() instanceof Java.type('client.BotClient'))) {
        botMenu = true;
        cm.sendSimple("Carnival 2: levels 51-70.\r\n#b#L903#Normal Carnival field selection.#l\r\n\r\n#e#kBotPQ Menu#n\r\n#b#L900#Assemble bot Carnival teams (4 vs 4).#l\r\n#L901#Start bot Carnival match.#l\r\n#L902#Dismiss my Carnival crew.#l");
        return;
    }
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (botMenu) {
        botMenu = false;
        if (mode != 1) { cm.dispose(); return; }
        const Crew = Java.type('server.bots.BotCarnivalCompanions');
        if (selection == 900) { cm.sendOk(Crew.assemble(cm.getPlayer())); cm.dispose(); return; }
        if (selection == 901) {
            var result = Crew.begin(cm.getPlayer(), cm);
            if (result != null) cm.sendOk(result);
            cm.dispose(); return;
        }
        if (selection == 902) { cm.sendOk(Crew.release(cm.getPlayer())); cm.dispose(); return; }
        status = -1;
    }
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getParty() == null) {
                status = 10;
                cm.sendOk("You need to create a party before you can participate in Monster Carnival!");
            } else if (!cm.isLeader()) {
                status = 10;
                cm.sendOk("If you want to start the battle, let the #bleader#k come and speak to me.");
            } else {
                var leaderMapid = cm.getMapId();
                var party = cm.getParty().getMembers();
                var inMap = cm.partyMembersInMap();
                var lvlOk = 0;
                var isOutMap = 0;
                for (var i = 0; i < party.size(); i++) {
                    if (party.get(i).getLevel() >= cpqMinLvl && party.get(i).getLevel() <= cpqMaxLvl) {
                        lvlOk++;

                        if (party.get(i).getPlayer().getMapId() != leaderMapid) {
                            isOutMap++;
                        }
                    }
                }

                if (party.size() < cpqMinAmt || party.size() > cpqMaxAmt) {
                    status = 10;
                    cm.sendOk("You do not have enough people in your party. You need a party with #b" + cpqMinAmt + "#k - #r" + cpqMaxAmt + "#k members and they should be on the map with you.");
                } else if (lvlOk != inMap) {
                    status = 10;
                    cm.sendOk("Make sure everyone in your party is among the correct levels (" + cpqMinLvl + "~" + cpqMaxLvl + ")!");
                } else if (isOutMap > 0) {
                    status = 10;
                    cm.sendOk("There are some of the party members that is not on the map!");
                } else {
                    if (!cm.sendCPQMapLists2()) {
                        cm.sendOk("All Monster Carnival fields are currently in use! Try again later.");
                        cm.dispose();
                    }
                }
            }
        } else if (status == 1) {
            if (cm.fieldTaken2(selection)) {
                if (cm.fieldLobbied2(selection)) {
                    cm.challengeParty2(selection);
                    cm.dispose();
                } else {
                    cm.sendOk("The room is currently full.");
                    cm.dispose();
                }
            } else {
                var party = cm.getParty().getMembers();
                const YamlConfig = Java.type('config.YamlConfig');
                if ((selection === 0 || selection === 1) && party.size() < (YamlConfig.config.server.USE_ENABLE_SOLO_EXPEDITIONS ? 1 : 2)) {
                    cm.sendOk("You need at least 2 players to participate in the battle!");
                } else if ((selection === 2) && party.size() < (YamlConfig.config.server.USE_ENABLE_SOLO_EXPEDITIONS ? 1 : 3)) {
                    cm.sendOk("You need at least 3 players to participate in the battle!");
                } else {
                    cm.cpqLobby2(selection);
                }
                cm.dispose();
            }
        } else if (status == 11) {
            cm.dispose();
        }
    }
}