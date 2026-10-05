package com.github.thedumbledodo.blueprint.menu.packet;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.thedumbledodo.blueprint.menu.model.MenuType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PacketMenuTypesTest {

    @Test
    void modernClientsUseTheCurrentRegistry() {
        assertEquals(5, PacketMenuTypes.getId(MenuType.GENERIC_9X6, ClientVersion.V_1_21_4));
        assertEquals(7, PacketMenuTypes.getId(MenuType.CRAFTER_3X3, ClientVersion.V_1_21_4));
        assertEquals(16, PacketMenuTypes.getId(MenuType.HOPPER, ClientVersion.V_1_21_4));
        assertEquals(24, PacketMenuTypes.getId(MenuType.STONECUTTER, ClientVersion.V_1_20_3));
    }

    @Test
    void clientsBeforeTheCrafterShiftDown() {
        assertEquals(6, PacketMenuTypes.getId(MenuType.GENERIC_3X3, ClientVersion.V_1_20));
        assertEquals(15, PacketMenuTypes.getId(MenuType.HOPPER, ClientVersion.V_1_20));
        assertEquals(23, PacketMenuTypes.getId(MenuType.STONECUTTER, ClientVersion.V_1_19_4));
    }

    @Test
    void crafterNeedsANewClient() {
        assertThrows(IllegalArgumentException.class, () -> PacketMenuTypes.getId(MenuType.CRAFTER_3X3, ClientVersion.V_1_20));
    }

    @Test
    void everyTypeHasAnId() {
        for (MenuType type : MenuType.values()) {
            assertEquals(type.ordinal(), PacketMenuTypes.getId(type, ClientVersion.V_1_21_4));
        }
    }
}
