var status = 0;
var wui = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }

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
        cm.sendSimple(
            "Welcome to the All In One Shop.\r\n\r\n" +
            "#r#L2#Search#l\r\n" +
            "#b#L0#Weapons#l\r\n" +
            "#L1050#Overalls#l\r\n" +
            "#L1082#Gloves#l\r\n" +
            "#L1002#Hats#l\r\n" +
            "#L1092#Shields#l\r\n" +
            "#L1040#Tops#l\r\n" +
            "#L1060#Bottoms#l\r\n" +
            "#L1102#Capes#l\r\n" +
            "#L1072#Shoes#l\r\n" +
            "#L1012#Face Accessories#l\r\n" +
            "#L1032#Earrings#l\r\n" +
            "#L1#Scrolls#l\r\n" +
            "#L2002#Potions#l\r\n" +
            "#L2060#Bow Arrows#l\r\n" +
            "#L2061#Crossbow Arrows#l\r\n" +
            "#L2070#Stars#l\r\n" +
            "#L2330#Bullets#l\r\n" +
            "#L4006#Rocks#l"
        );

    } else if (status == 1) {

        if (selection == 2) {
            wui = 1;
            cm.sendGetText(
                "Enter the name of the item you want to search for:\r\n\r\n" +
                "Example: Maple, Zakum, Chaos"
            );

        } else if (selection == 0) {

            cm.sendSimple(
                "#L1302#One Handed Sword#l\r\n" +
                "#L1312#One Handed Axe#l\r\n" +
                "#L1322#One Handed Blunt Weapon#l\r\n" +
                "#L1402#Two Handed Sword#l\r\n" +
                "#L1412#Two Handed Axe#l\r\n" +
                "#L1422#Two Handed Blunt Weapon#l\r\n" +
                "#L1432#Spear#l\r\n" +
                "#L1442#Polearm#l\r\n" +
                "#L1332#Dagger#l\r\n" +
                "#L1372#Wand#l\r\n" +
                "#L1382#Staff#l\r\n" +
                "#L1452#Bow#l\r\n" +
                "#L1462#Crossbow#l\r\n" +
                "#L1472#Claw#l\r\n" +
                "#L1482#Knuckle#l\r\n" +
                "#L1492#Gun#l"
            );

        } else {

            cm.openShopById(selection);
            cm.dispose();

        }

    } else if (status == 2) {

        if (wui == 1) {

            var query = cm.getText();

            cm.openShopBySearch(query);
            cm.dispose();

        } else {

            cm.openShopById(selection);
            cm.dispose();

        }
    }
}