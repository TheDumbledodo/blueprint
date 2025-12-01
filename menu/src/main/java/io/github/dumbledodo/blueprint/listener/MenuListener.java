package io.github.dumbledodo.blueprint.listener;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import io.github.dumbledodo.blueprint.service.MenuService;

import java.util.UUID;

public final class MenuListener implements PacketListener {

    private final MenuService menuService;

    public MenuListener(MenuService menuService, PacketEventsAPI<?> packetEventsAPI) {
        this.menuService = menuService;

        packetEventsAPI.getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        menuService.onCloseMenu(event.getUser());
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final PacketTypeCommon packetType = event.getPacketType();
        final User user = event.getUser();

        if (packetType == PacketType.Play.Client.CLOSE_WINDOW) {
            menuService.onCloseMenu(user);
            return;
        }

        if (packetType == PacketType.Play.Client.CLICK_WINDOW) {
            final UUID uuid = user.getUUID();

            menuService.getMenu(uuid).ifPresent(menu -> {
                final WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);

                event.setCancelled(true);
                menuService.handleClickMenu(user, menu, packet);
            });
        }
    }
}