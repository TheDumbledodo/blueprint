package com.github.thedumbledodo.blueprint.command.fixture;

import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public record TestActor(TestSender sender) implements CommandActor {

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public Optional<UUID> getUniqueId() {
        return sender instanceof TestPlayer player ? Optional.of(player.getUniqueId()) : Optional.empty();
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof TestPlayer;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(Component message) {
        sender.sendMessage(message);
    }

    @Override
    public Object getSender() {
        return sender;
    }
}
