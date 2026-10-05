package com.github.thedumbledodo.blueprint.menu.packet;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.thedumbledodo.blueprint.menu.model.MenuType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PacketMenuTypes {

    public int getId(MenuType type, ClientVersion version) {
        final int id = getModernId(type);

        if (version == null || !version.isOlderThan(ClientVersion.V_1_20_3)) {
            return id;
        }

        if (type == MenuType.CRAFTER_3X3) {
            throw new IllegalArgumentException("CRAFTER_3X3 menus need a 1.20.3 or newer client");
        }
        return id > 7 ? id - 1 : id;
    }

    private int getModernId(MenuType type) {
        return switch (type) {
            case GENERIC_9X1 -> 0;
            case GENERIC_9X2 -> 1;
            case GENERIC_9X3 -> 2;
            case GENERIC_9X4 -> 3;
            case GENERIC_9X5 -> 4;
            case GENERIC_9X6 -> 5;
            case GENERIC_3X3 -> 6;
            case CRAFTER_3X3 -> 7;
            case ANVIL -> 8;
            case BEACON -> 9;
            case BLAST_FURNACE -> 10;
            case BREWING_STAND -> 11;
            case CRAFTING_TABLE -> 12;
            case ENCHANTMENT_TABLE -> 13;
            case FURNACE -> 14;
            case GRINDSTONE -> 15;
            case HOPPER -> 16;
            case LECTERN -> 17;
            case LOOM -> 18;
            case VILLAGER -> 19;
            case SHULKER_BOX -> 20;
            case SMITHING_TABLE -> 21;
            case SMOKER -> 22;
            case CARTOGRAPHY_TABLE -> 23;
            case STONECUTTER -> 24;
        };
    }
}
