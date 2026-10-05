package com.github.thedumbledodo.blueprint.command.parser;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParser;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ServerParser implements ArgumentParser<RegisteredServer> {

    private final ProxyServer server;

    public ServerParser(ProxyServer server) {
        this.server = server;
    }

    @Override
    public ArgumentType<?> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public RegisteredServer parse(CommandActor actor, Object input) {
        final String name = String.valueOf(input);

        return server.getServer(name).orElseThrow(() ->
                new CommandException(CommandMessages::getNotFound, Placeholder.unparsed("input", name)));
    }

    @Override
    public Collection<String> suggest(CommandActor actor, String input) {
        final List<String> names = new ArrayList<>();

        for (RegisteredServer registered : server.getAllServers()) {
            names.add(registered.getServerInfo().getName());
        }
        return names;
    }
}
