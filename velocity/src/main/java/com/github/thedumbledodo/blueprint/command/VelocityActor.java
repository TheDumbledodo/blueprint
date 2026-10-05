package com.github.thedumbledodo.blueprint.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public record VelocityActor(CommandSource source) implements CommandActor {

    @Override
    public String getName() {
        return source instanceof Player player ? player.getUsername() : "CONSOLE";
    }

    @Override
    public Optional<UUID> getUniqueId() {
        return source instanceof Player player ? Optional.of(player.getUniqueId()) : Optional.empty();
    }

    @Override
    public boolean isPlayer() {
        return source instanceof Player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public void sendMessage(Component message) {
        source.sendMessage(message);
    }

    @Override
    public CommandSource getSender() {
        return source;
    }
}
