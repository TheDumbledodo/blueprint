package com.github.thedumbledodo.blueprint.command;

import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.github.thedumbledodo.blueprint.command.brigadier.RegisteredCommand;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import com.github.thedumbledodo.blueprint.command.model.CommandNode;
import com.github.thedumbledodo.blueprint.command.parser.ProxyPlayerParser;
import com.github.thedumbledodo.blueprint.command.parser.ServerParser;
import org.slf4j.Logger;

import java.util.List;

public final class VelocityCommandManager extends CommandManager<CommandSource> {

    private final ProxyServer server;
    private final Object plugin;
    private final Logger logger;

    public VelocityCommandManager(ProxyServer server, Object plugin, Logger logger) {
        super(CommandSource.class);

        this.server = server;
        this.plugin = plugin;
        this.logger = logger;

        registerParser(Player.class, new ProxyPlayerParser(server));
        registerParser(RegisteredServer.class, new ServerParser(server));
    }

    @Override
    public CommandActor createActor(CommandSource source) {
        return new VelocityActor(source);
    }

    @Override
    public void refresh() {
    }

    @Override
    protected void registerCommands(List<RegisteredCommand<CommandSource>> commands) {
        final com.velocitypowered.api.command.CommandManager commandManager = server.getCommandManager();

        for (RegisteredCommand<CommandSource> command : commands) {
            final BrigadierCommand brigadierCommand = new BrigadierCommand(command.literal());
            final CommandMeta meta = commandManager.metaBuilder(brigadierCommand)
                    .aliases(command.node().aliases().toArray(new String[0]))
                    .plugin(plugin)
                    .build();

            commandManager.register(meta, brigadierCommand);
        }
    }

    @Override
    public void handleError(CommandNode node, Throwable throwable) {
        final String name = node == null ? "suggestions" : "/" + node.name();

        if (logger == null) {
            throwable.printStackTrace();
            return;
        }
        logger.error("Error while running {}", name, throwable);
    }
}
