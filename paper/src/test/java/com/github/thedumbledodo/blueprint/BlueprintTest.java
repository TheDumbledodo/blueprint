package com.github.thedumbledodo.blueprint;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import org.mockbukkit.mockbukkit.ServerMock;
import com.github.thedumbledodo.blueprint.config.annotation.Configuration;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.service.Services;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintTest {

    private ServerMock server;
    private PluginMock plugin;

    @BeforeEach
    void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.createMockPlugin();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
        Blueprint.unregister();
    }

    @Test
    void extraObjectsAreRegisteredOneByOne() {
        final ApiClient client = new ApiClient();

        Blueprint.registerServices(plugin, client, "token");

        assertSame(client, Services.getService(ApiClient.class));
        assertEquals("token", Services.getService(String.class));
        assertSame(plugin, Services.getService(Plugin.class));
        assertNull(Services.getService(Object[].class));
    }

    @Test
    void listenerComponentsReceiveEvents() {
        final JoinCounter counter = new JoinCounter();

        Blueprint.registerListeners(plugin);
        ComponentRegistry.processRegisteredComponent(JoinCounter.class, counter);
        server.addPlayer();

        assertEquals(1, counter.joins);
    }

    @Test
    void configsAreWrittenToThePluginFolderWithBukkitTypes() {
        final BlueprintConfiguration configuration = Blueprint.createConfiguration(plugin);

        configuration.install();

        final SpawnConfig config = configuration.load(new SpawnConfig());
        final Path file = plugin.getDataFolder().toPath().resolve("spawn.yml");

        config.offset = new Vector(4, 5, 6);
        configuration.save(config);

        final SpawnConfig reloaded = configuration.load(new SpawnConfig());

        assertTrue(Files.exists(file));
        assertEquals(new Vector(4, 5, 6), reloaded.offset);
    }

    public static final class ApiClient {
    }

    public static final class JoinCounter implements Listener {

        private int joins;

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            joins++;
        }
    }

    @Configuration("spawn.yml")
    public static final class SpawnConfig {

        public Vector offset = new Vector(1, 2, 3);
    }
}
