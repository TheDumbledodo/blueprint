package com.github.thedumbledodo.blueprint.menu.packet.fixture;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.thedumbledodo.blueprint.menu.packet.sender.PacketSender;

import java.util.ArrayList;
import java.util.List;

public final class RecordingPacketSender implements PacketSender {

    private final List<Sent> sent = new ArrayList<>();
    private final List<Sent> received = new ArrayList<>();

    @Override
    public void send(User user, PacketWrapper<?> packet) {
        sent.add(new Sent(user, packet));
    }

    @Override
    public void receive(User user, PacketWrapper<?> packet) {
        received.add(new Sent(user, packet));
    }

    public List<PacketWrapper<?>> received(User user) {
        return received.stream()
                .filter(entry -> entry.user() == user)
                .<PacketWrapper<?>>map(Sent::packet)
                .toList();
    }

    public List<PacketWrapper<?>> packets(User user) {
        return sent.stream()
                .filter(entry -> entry.user() == user)
                .<PacketWrapper<?>>map(Sent::packet)
                .toList();
    }

    public <T extends PacketWrapper<?>> List<T> packets(User user, Class<T> type) {
        return packets(user).stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    public List<Class<?>> types(User user) {
        return packets(user).stream()
                .<Class<?>>map(Object::getClass)
                .toList();
    }

    public void clear() {
        sent.clear();
        received.clear();
    }

    private record Sent(User user, PacketWrapper<?> packet) {
    }
}
