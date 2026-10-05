package com.github.thedumbledodo.blueprint.command.parser;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParser;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PlayerParser implements ArgumentParser<Player> {

    @Override
    public ArgumentType<?> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public Player parse(CommandActor actor, Object input) {
        final String name = String.valueOf(input);
        final Player player = Bukkit.getPlayerExact(name);

        if (player == null || !canSee(actor, player)) {
            throw new CommandException(CommandMessages::getPlayerNotFound, Placeholder.unparsed("input", name));
        }
        return player;
    }

    @Override
    public Collection<String> suggest(CommandActor actor, String input) {
        final List<String> names = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!canSee(actor, player)) {
                continue;
            }
            names.add(player.getName());
        }
        return names;
    }

    private boolean canSee(CommandActor actor, Player target) {
        if (!(actor.getSender() instanceof Player viewer)) {
            return true;
        }
        return viewer.canSee(target);
    }
}
