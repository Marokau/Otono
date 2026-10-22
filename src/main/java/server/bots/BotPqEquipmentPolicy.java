package server.bots;

import client.Character;
import client.inventory.WeaponType;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Dedicated PQ crews use their class/build weapons; population preferences are unchanged. */
final class BotPqEquipmentPolicy {
    private static final Set<Character> CREW = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private BotPqEquipmentPolicy() {}
    static void register(Character bot) { CREW.add(bot); BotPqDialogue.register(bot); }
    static boolean allows(Character bot, WeaponType type) {
        return !CREW.contains(bot) || BotEquipManager.isPreferredWeapon(bot, type);
    }
}
