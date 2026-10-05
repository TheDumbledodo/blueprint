package com.github.thedumbledodo.blueprint.command.parser;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParser;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ProxyPlayerParser implements ArgumentParser<Player> {

    private final ProxyServer server;

    public ProxyPlayerParser(ProxyServer server) {
        this.server = server;
    }

    @Override
    public ArgumentType<?> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public Player parse(CommandActor actor, Object input) {
        final String name = String.valueOf(input);

        return server.getPlayer(name).orElseThrow(() ->
                new CommandException(CommandMessages::getPlayerNotFound, Placeholder.unparsed("input", name)));
    }

    @Override
    public Collection<String> suggest(CommandActor actor, String input) {
        final List<String> names = new ArrayList<>();

        for (Player player : server.getAllPlayers()) {
            names.add(player.getUsername());
        }
        return names;
    }
}
