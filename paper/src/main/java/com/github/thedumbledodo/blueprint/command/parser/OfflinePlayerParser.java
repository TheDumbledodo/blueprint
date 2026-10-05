package com.github.thedumbledodo.blueprint.command.parser;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParser;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class OfflinePlayerParser implements ArgumentParser<OfflinePlayer> {

    @Override
    public ArgumentType<?> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public OfflinePlayer parse(CommandActor actor, Object input) {
        final String name = String.valueOf(input);
        final OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);

        if (player == null) {
            throw new CommandException(CommandMessages::getPlayerNotFound, Placeholder.unparsed("input", name));
        }
        return player;
    }

    @Override
    public Collection<String> suggest(CommandActor actor, String input) {
        final List<String> names = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }
}
