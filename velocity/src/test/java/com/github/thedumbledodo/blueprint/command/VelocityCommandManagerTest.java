package com.github.thedumbledodo.blueprint.command;

import com.mojang.brigadier.CommandDispatcher;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.github.thedumbledodo.blueprint.command.builder.CommandBuilder;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.model.Argument;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VelocityCommandManagerTest {

    private ProxyServer server;
    private CommandManager commandManager;
    private CommandMeta.Builder metaBuilder;
    private CommandMeta meta;

    private VelocityCommandManager manager;

    @BeforeEach
    void setUp() {
        this.server = mock(ProxyServer.class);
        this.commandManager = mock(CommandManager.class);
        this.metaBuilder = mock(CommandMeta.Builder.class);
        this.meta = mock(CommandMeta.class);

        when(server.getCommandManager()).thenReturn(commandManager);
        when(commandManager.metaBuilder(any(BrigadierCommand.class))).thenReturn(metaBuilder);
        when(metaBuilder.aliases(any(String[].class))).thenReturn(metaBuilder);
        when(metaBuilder.plugin(any())).thenReturn(metaBuilder);
        when(metaBuilder.build()).thenReturn(meta);

        this.manager = new VelocityCommandManager(server, new Object(), mock(Logger.class));
    }

    private static Player player(UUID uuid) {
        final Player player = mock(Player.class);

        when(player.getUsername()).thenReturn("Steve");
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.hasPermission(anyString())).thenReturn(true);
        return player;
    }

    @Test
    void rootsAreRegisteredWithTheirAliases() {
        manager.register(CommandBuilder.of("queue")
                .aliases("q")
                .executes(context -> {
                }));
        manager.flush();
        manager.flush();

        verify(metaBuilder).aliases("q");
        verify(commandManager, times(1)).register(eq(meta), any(BrigadierCommand.class));
    }

    @Test
    void compiledCommandsRunAgainstVelocitySources() throws Exception {
        final Argument<String> server = Argument.of("server", String.class);
        final List<String> calls = new ArrayList<>();
        final Player player = player(UUID.randomUUID());

        manager.register(CommandBuilder.of("send")
                .argument(server)
                .executes(context -> calls.add(context.getSender(Player.class).getUsername() + " -> " + context.get(server))));
        manager.flush();

        final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

        dispatcher.getRoot().addChild(manager.getCommands().getFirst().literal());
        dispatcher.execute("send lobby", player);

        assertEquals(List.of("Steve -> lobby"), calls);
    }

    @Test
    void playerOnlyMessageReachesTheConsole() throws Exception {
        final CommandSource console = mock(CommandSource.class);

        when(console.hasPermission(anyString())).thenReturn(true);

        manager.register(CommandBuilder.of("hub")
                .sender(Player.class)
                .executes(context -> fail("console ran a player command")));
        manager.flush();

        final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

        dispatcher.getRoot().addChild(manager.getCommands().getFirst().literal());
        dispatcher.execute("hub", console);

        verify(console).sendMessage(any(Component.class));
    }

    @Test
    void playerParserUsesTheProxy() {
        final Player steve = player(UUID.randomUUID());
        final CommandActor actor = new VelocityActor(steve);

        when(server.getPlayer("Steve")).thenReturn(Optional.of(steve));
        when(server.getPlayer("Ghost")).thenReturn(Optional.empty());
        when(server.getAllPlayers()).thenReturn(List.of(steve));

        assertSame(steve, manager.getParser(Player.class).parse(actor, "Steve"));
        assertEquals(List.of("Steve"), List.copyOf(manager.getParser(Player.class).suggest(actor, "")));
        assertThrows(CommandException.class, () -> manager.getParser(Player.class).parse(actor, "Ghost"));
    }

    @Test
    void actorDescribesPlayersAndConsole() {
        final UUID uuid = UUID.randomUUID();
        final CommandActor player = new VelocityActor(player(uuid));
        final CommandActor console = new VelocityActor(mock(CommandSource.class));

        assertEquals("Steve", player.getName());
        assertEquals(Optional.of(uuid), player.getUniqueId());
        assertTrue(player.isPlayer());
        assertEquals("CONSOLE", console.getName());
        assertFalse(console.isPlayer());
    }
}
