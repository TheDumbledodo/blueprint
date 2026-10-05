package com.github.thedumbledodo.blueprint.menu.packet.sender;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

@FunctionalInterface
public interface PacketSender {

    PacketSender SILENT = User::sendPacketSilently;

    void send(User user, PacketWrapper<?> packet);
}
