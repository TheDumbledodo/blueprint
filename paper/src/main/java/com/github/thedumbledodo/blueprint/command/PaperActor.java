package com.github.thedumbledodo.blueprint.command;

import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public record PaperActor(CommandSourceStack source) implements CommandActor {

    @Override
    public String getName() {
        return getSender().getName();
    }

    @Override
    public Optional<UUID> getUniqueId() {
        return getSender() instanceof Player player ? Optional.of(player.getUniqueId()) : Optional.empty();
    }

    @Override
    public boolean isPlayer() {
        return getSender() instanceof Player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return getSender().hasPermission(permission);
    }

    @Override
    public void sendMessage(Component message) {
        getSender().sendMessage(message);
    }

    @Override
    public CommandSender getSender() {
        return source.getSender();
    }
}
