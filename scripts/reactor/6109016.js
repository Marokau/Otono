function act() {
    var eim = rm.getEventInstance();
    if (eim != null) {
        eim.dropMessage(6, "The Warrior Sigil has been activated!");
        var activated = eim.incrementIntProperty("glpq4");
        if (activated == 5) { //all 5 done
            eim.dropMessage(6, "The Antellion grants you access to the next portal! Proceed!");

            eim.showClearEffect(610030400, "4pt", 2);
            eim.giveEventPlayersStageReward(4);
        }
    }
}