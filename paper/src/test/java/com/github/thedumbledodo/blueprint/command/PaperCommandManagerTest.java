package com.github.thedumbledodo.blueprint.command;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.github.thedumbledodo.blueprint.command.builder.CommandBuilder;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandRegistrationFlag;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PaperCommandManagerTest {

    private ServerMock server;
    private PaperCommandManager manager;

    @BeforeEach
    void setUp() {
        this.server = MockBukkit.mock();

        final PluginMock plugin = MockBukkit.createMockPlugin();
        this.manager = new PaperCommandManager(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static CommandActor actor(CommandSender sender) {
        return new PaperActor(new FakeSource(sender));
    }

    @Test
    void platformParsersAreRegistered() {
        assertNotNull(manager.getParser(Player.class));
        assertNotNull(manager.getParser(OfflinePlayer.class));
        assertNotNull(manager.getParser(World.class));
    }

    @Test
    void playerParserFindsOnlinePlayers() {
        final PlayerMock steve = server.addPlayer("Steve");

        assertSame(steve, manager.getParser(Player.class).parse(actor(server.getConsoleSender()), "Steve"));
        assertTrue(manager.getParser(Player.class).suggest(actor(server.getConsoleSender()), "").contains("Steve"));
    }

    @Test
    void unknownPlayerGivesTheConfiguredMessage() {
        final CommandException exception = assertThrows(CommandException.class,
                () -> manager.getParser(Player.class).parse(actor(server.getConsoleSender()), "Ghost"));

        assertEquals("Player not found!", com.github.thedumbledodo.blueprint.chat.Text
                .translateToLegacyString(exception.render(manager.getMessages())).replaceAll("§.", ""));
    }

    @Test
    void worldParserUsesLoadedWorlds() {
        server.addSimpleWorld("arena");

        assertEquals("arena", manager.getParser(World.class).parse(actor(server.getConsoleSender()), "arena").getName());
        assertThrows(CommandException.class, () -> manager.getParser(World.class).parse(actor(server.getConsoleSender()), "missing"));
    }

    @Test
    void actorWrapsTheSender() {
        final PlayerMock steve = server.addPlayer("Steve");
        final CommandActor actor = actor(steve);

        assertEquals("Steve", actor.getName());
        assertTrue(actor.isPlayer());
        assertEquals(Optional.of(steve.getUniqueId()), actor.getUniqueId());
        assertFalse(actor(server.getConsoleSender()).isPlayer());
    }

    @Test
    void registerAllPassesNameDescriptionAndAliases() {
        final RecordingCommands registrar = new RecordingCommands();

        manager.register(CommandBuilder.of("spawn")
                .aliases("hub")
                .description("Teleport to spawn")
                .executes(context -> {
                }));
        manager.flush();
        manager.flush();
        manager.registerAll(registrar);

        assertEquals(1, registrar.registered.size());
        assertEquals("spawn", registrar.registered.getFirst().node().getLiteral());
        assertEquals("Teleport to spawn", registrar.registered.getFirst().description());
        assertEquals(List.of("hub"), List.copyOf(registrar.registered.getFirst().aliases()));
    }

    private record FakeSource(CommandSender sender) implements CommandSourceStack {

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public CommandSender getSender() {
            return sender;
        }

        @Override
        public Entity getExecutor() {
            return sender instanceof Entity entity ? entity : null;
        }

        @Override
        public Player getPlayerOrThrow() {
            if (sender instanceof Player player) {
                return player;
            }
            throw new IllegalStateException("the sender is not a player");
        }

        @Override
        public Entity getEntityOrThrow() {
            if (sender instanceof Entity entity) {
                return entity;
            }
            throw new IllegalStateException("the sender is not an entity");
        }

        @Override
        public CommandSourceStack withLocation(Location location) {
            return this;
        }

        @Override
        public CommandSourceStack withExecutor(Entity executor) {
            return this;
        }
    }

    private record Registration(LiteralCommandNode<CommandSourceStack> node, String description,
                                Collection<String> aliases) {
    }

    private static final class RecordingCommands implements Commands {

        private final List<Registration> registered = new ArrayList<>();

        @Override
        public CommandDispatcher<CommandSourceStack> getDispatcher() {
            return new CommandDispatcher<>();
        }

        @Override
        public Set<String> register(LiteralCommandNode<CommandSourceStack> node, String description, Collection<String> aliases) {
            registered.add(new Registration(node, description, aliases));
            return Set.of(node.getLiteral());
        }

        @Override
        public Set<String> register(PluginMeta meta, LiteralCommandNode<CommandSourceStack> node, String description, Collection<String> aliases) {
            return register(node, description, aliases);
        }

        @Override
        public Set<String> registerWithFlags(PluginMeta meta, LiteralCommandNode<CommandSourceStack> node, String description, Collection<String> aliases, Set<CommandRegistrationFlag> flags) {
            return register(node, description, aliases);
        }

        @Override
        public Set<String> register(String label, String description, Collection<String> aliases, BasicCommand command) {
            return Set.of(label);
        }

        @Override
        public Set<String> register(PluginMeta meta, String label, String description, Collection<String> aliases, BasicCommand command) {
            return Set.of(label);
        }
    }
}
