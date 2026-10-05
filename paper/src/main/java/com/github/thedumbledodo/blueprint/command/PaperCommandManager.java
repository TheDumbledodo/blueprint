package com.github.thedumbledodo.blueprint.command;

import com.github.thedumbledodo.blueprint.command.brigadier.RegisteredCommand;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import com.github.thedumbledodo.blueprint.command.model.CommandNode;
import com.github.thedumbledodo.blueprint.command.parser.OfflinePlayerParser;
import com.github.thedumbledodo.blueprint.command.parser.PlayerParser;
import com.github.thedumbledodo.blueprint.command.parser.WorldParser;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.logging.Level;

public final class PaperCommandManager extends CommandManager<CommandSourceStack> {

    private final Plugin plugin;

    public PaperCommandManager(Plugin plugin) {
        super(CommandSender.class);
        this.plugin = plugin;

        registerParser(Player.class, new PlayerParser());
        registerParser(OfflinePlayer.class, new OfflinePlayerParser());
        registerParser(World.class, new WorldParser());
    }

    @Override
    public void install() {
        super.install();

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> registerAll(event.registrar()));
    }

    public void registerAll(Commands registrar) {
        for (RegisteredCommand<CommandSourceStack> command : getCommands()) {
            final CommandNode node = command.node();

            registrar.register(command.literal(), node.description(), node.aliases());
        }
    }

    @Override
    public CommandActor createActor(CommandSourceStack source) {
        return new PaperActor(source);
    }

    @Override
    public void refresh() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    @Override
    protected void registerCommands(List<RegisteredCommand<CommandSourceStack>> commands) {
    }

    @Override
    public void handleError(CommandNode node, Throwable throwable) {
        final String name = node == null ? "suggestions" : "/" + node.name();

        plugin.getLogger().log(Level.SEVERE, "Error while running " + name, throwable);
    }
}
