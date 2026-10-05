package com.github.thedumbledodo.blueprint.menu.packet.fixture;

import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;

import java.util.UUID;

public final class TestUsers {

    public static User user(String name) {
        return user(name, ClientVersion.V_1_21_4);
    }

    public static User user(String name, ClientVersion version) {
        return new User(new Object(), ConnectionState.PLAY, version, new UserProfile(UUID.randomUUID(), name));
    }
}
