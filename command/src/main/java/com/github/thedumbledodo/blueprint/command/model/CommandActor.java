package com.github.thedumbledodo.blueprint.command.model;

import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public interface CommandActor {

    String getName();

    Optional<UUID> getUniqueId();

    boolean isPlayer();

    boolean hasPermission(String permission);

    void sendMessage(Component message);

    Object getSender();
}
