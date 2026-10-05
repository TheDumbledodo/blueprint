package com.github.thedumbledodo.blueprint.command.parser;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParser;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class WorldParser implements ArgumentParser<World> {

    @Override
    public ArgumentType<?> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public World parse(CommandActor actor, Object input) {
        final String name = String.valueOf(input);
        final World world = Bukkit.getWorld(name);

        if (world == null) {
            throw new CommandException(CommandMessages::getNotFound, Placeholder.unparsed("input", name));
        }
        return world;
    }

    @Override
    public Collection<String> suggest(CommandActor actor, String input) {
        final List<String> names = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            names.add(world.getName());
        }
        return names;
    }
}
