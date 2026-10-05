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
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
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
        final PacketTypeCommon packetType = event.getPacketType();
        final User user = event.getUser();

        if (packetType == PacketType.Play.Server.OPEN_WINDOW || packetType == PacketType.Play.Server.CLOSE_WINDOW) {
            menuService.handleServerWindow(user);
            return;
        }

        if (!trackInventories) {
            return;
        }

        if (packetType == PacketType.Play.Server.WINDOW_ITEMS) {
            final WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);

            if (packet.getWindowId() == 0) {
                menuService.getInventoryCache().setContents(user.getUUID(), packet.getItems());
            }
            return;
        }

        if (packetType == PacketType.Play.Server.SET_SLOT) {
            final WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);

            if (packet.getWindowId() == 0) {
                menuService.getInventoryCache().setSlot(user.getUUID(), packet.getSlot(), packet.getItem());
            }
        }
    }
}
