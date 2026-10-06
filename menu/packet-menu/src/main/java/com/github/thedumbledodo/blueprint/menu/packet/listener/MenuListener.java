package com.github.thedumbledodo.blueprint.menu.packet.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPlayerInventory;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import com.github.thedumbledodo.blueprint.menu.packet.cache.PlayerInventoryCache;
import com.github.thedumbledodo.blueprint.menu.packet.service.MenuService;
import lombok.Getter;
import lombok.Setter;

public final class MenuListener implements PacketListener {

    private final MenuService menuService;

    @Getter @Setter
    private boolean trackInventories = true;

    public MenuListener(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        final User user = event.getUser();

        menuService.handleDisconnect(user);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final PacketTypeCommon packetType = event.getPacketType();
        final User user = event.getUser();

        if (packetType == PacketType.Play.Client.CLICK_WINDOW) {
            final WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);

            if (menuService.handleClick(user, packet)) {
                event.setCancelled(true);
            }
            return;
        }

        if (packetType == PacketType.Play.Client.CLOSE_WINDOW) {
            final WrapperPlayClientCloseWindow packet = new WrapperPlayClientCloseWindow(event);

            if (menuService.handleClientClose(user, packet.getWindowId())) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPacketType() instanceof PacketType.Play.Server packetType)) {
            return;
        }

        final User user = event.getUser();

        switch (packetType) {
            case OPEN_WINDOW, CLOSE_WINDOW, DEATH_COMBAT_EVENT, RESPAWN, CONFIGURATION_START -> menuService.handleServerWindow(user);

            case WINDOW_ITEMS -> {
                final WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);

                if (trackInventories) {
                    menuService.getInventoryCache().setWindowContents(user.getUUID(), packet.getWindowId(), packet.getItems());
                }

                if (packet.getWindowId() == 0 && menuService.hidesInventory(user)) {
                    event.setCancelled(true);
                }
            }

            case SET_SLOT -> {
                final WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);

                if (trackInventories) {
                    menuService.getInventoryCache().setWindowSlot(user.getUUID(), packet.getWindowId(), packet.getSlot(), packet.getItem());
                }

                if (packet.getWindowId() == 0 && menuService.hidesInventorySlot(user, packet.getSlot())) {
                    event.setCancelled(true);
                }
            }

            case SET_PLAYER_INVENTORY -> {
                final WrapperPlayServerSetPlayerInventory packet = new WrapperPlayServerSetPlayerInventory(event);
                final int slot = PlayerInventoryCache.toWindowSlot(packet.getSlot());

                if (trackInventories) {
                    menuService.getInventoryCache().setSlot(user.getUUID(), slot, packet.getStack());
                }

                if (menuService.hidesInventorySlot(user, slot)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
