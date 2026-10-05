package com.github.thedumbledodo.blueprint.config;

import com.github.thedumbledodo.blueprint.BlueprintConfiguration;
import com.github.thedumbledodo.blueprint.config.annotation.Configuration;
import com.github.thedumbledodo.blueprint.config.fixture.InMemoryFormat;
import com.github.thedumbledodo.blueprint.config.fixture.SampleConfig;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.loader.BlueprintLoader;
import com.github.thedumbledodo.blueprint.service.Services;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoadTest {

    @TempDir
    Path directory;

    private InMemoryFormat format;
    private BlueprintConfiguration configuration;

    @BeforeEach
    void setUp() {
        this.format = new InMemoryFormat();
        this.configuration = BlueprintConfiguration.builder()
                .configDirectory(directory)
                .format(format)
                .build();

        configuration.install();
    }

    @AfterEach
    void reset() {
        Services.clear();
        ComponentRegistry.clear();
    }

    @Test
    void firstLoadWritesTheDefaults() throws Exception {
        configuration.load(new SampleConfig());

        final Map<String, Object> values = InMemoryFormat.readFile(directory.resolve("sample.mem"));

        assertEquals(16, values.get("maxPlayers"));
        assertEquals("<green>Reloaded.", values.get("reloadedPlugin"));
    }

    @Test
    void existingValuesAreLoaded() throws Exception {
        InMemoryFormat.writeFile(directory.resolve("sample.mem"), Map.of("maxPlayers", 32));

        final SampleConfig config = configuration.load(new SampleConfig());

        assertEquals(32, config.maxPlayers);
    }

    @Test
    void missingKeysAreAddedAndUnknownKeysAreDropped() throws Exception {
        final Path file = directory.resolve("sample.mem");

        InMemoryFormat.writeFile(file, Map.of("maxPlayers", 32, "removedSetting", true));
        configuration.load(new SampleConfig());

        final Map<String, Object> values = InMemoryFormat.readFile(file);

        assertEquals(32, values.get("maxPlayers"));
        assertTrue(values.containsKey("reloadedPlugin"));
        assertFalse(values.containsKey("removedSetting"));
    }

    @Test
    void deletedKeyResetsToTheDefaultOnReload() throws Exception {
        final Path file = directory.resolve("sample.mem");

        InMemoryFormat.writeFile(file, Map.of("maxPlayers", 32));

        final SampleConfig config = configuration.load(new SampleConfig());

        InMemoryFormat.editFile(file, values -> values.remove("maxPlayers"));
        Configs.reload(config);

        assertEquals(16, config.maxPlayers);
    }

    @Test
    void reloadPicksUpFileChanges() throws Exception {
        final Path file = directory.resolve("sample.mem");
        final SampleConfig config = configuration.load(new SampleConfig());

        InMemoryFormat.editFile(file, values -> values.put("maxPlayers", 64));
        Configs.reload(config);

        assertEquals(64, config.maxPlayers);
    }

    @Test
    void reloadKeepsTheSameInstanceEverywhere() throws Exception {
        final SampleConfig config = configuration.load(new SampleConfig());

        Services.register(SampleConfig.class, config);
        InMemoryFormat.editFile(directory.resolve("sample.mem"), values -> values.put("maxPlayers", 8));
        Configs.reload(config);

        assertSame(config, Services.getService(SampleConfig.class));
        assertEquals(8, Services.getService(SampleConfig.class).maxPlayers);
    }

    @Test
    void finalFieldIsRewrittenFromCode() throws Exception {
        final Path file = directory.resolve("sample.mem");

        InMemoryFormat.writeFile(file, Map.of("version", "2"));

        final SampleConfig config = configuration.load(new SampleConfig());

        assertEquals("1", config.version);
        assertEquals("1", InMemoryFormat.readFile(file).get("version"));
    }

    @Test
    void brokenFileIsReportedAndLeftUntouched() throws Exception {
        final Path file = directory.resolve("sample.mem");

        Files.writeString(file, "not valid content", StandardCharsets.UTF_8);

        final ConfigException exception = assertThrows(ConfigException.class, () -> configuration.load(new SampleConfig()));

        assertTrue(exception.getMessage().startsWith("sample.mem -> "));
        assertEquals("not valid content", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void typeErrorNamesTheFileAndKey() throws Exception {
        final Path file = directory.resolve("sample.mem");

        InMemoryFormat.writeFile(file, Map.of("maxPlayers", "ten"));

        final ConfigException exception = assertThrows(ConfigException.class, () -> configuration.load(new SampleConfig()));

        assertEquals("sample.mem -> maxPlayers: expected a number but got \"ten\"", exception.getMessage());
        assertEquals("ten", InMemoryFormat.readFile(file).get("maxPlayers"));
    }

    @Test
    void saveWritesRuntimeChanges() throws Exception {
        final SampleConfig config = configuration.load(new SampleConfig());

        config.maxPlayers = 99;
        Configs.save(config);

        assertEquals(99, InMemoryFormat.readFile(directory.resolve("sample.mem")).get("maxPlayers"));
    }

    @Test
    void failedWriteLeavesTheOldFileIntact() throws Exception {
        final SampleConfig config = configuration.load(new SampleConfig());
        final Path file = directory.resolve("sample.mem");
        final String before = Files.readString(file);

        format.setFailWrites(true);
        config.maxPlayers = 1;

        assertThrows(ConfigException.class, () -> Configs.save(config));
        assertEquals(before, Files.readString(file));
        assertFalse(Files.exists(directory.resolve("sample.mem.tmp")));
    }

    @Test
    void configInASubdirectory() {
        configuration.load(new ArenaConfig());

        assertTrue(Files.exists(directory.resolve("arenas").resolve("arena.mem")));
    }

    @Test
    void unknownExtensionFailsWithTheRegisteredOnes() {
        final ConfigException exception = assertThrows(ConfigException.class,
                () -> configuration.load(new TomlConfig()));

        assertTrue(exception.getMessage().contains("settings.toml"));
        assertTrue(exception.getMessage().contains("mem"));
    }

    @Test
    void componentConfigsAreLoadedWhenCreated() throws Exception {
        InMemoryFormat.writeFile(directory.resolve("component.mem"), Map.of("maxPlayers", 5));

        final ComponentConfig config = BlueprintLoader.createComponentInstance(ComponentConfig.class);

        assertEquals(5, config.maxPlayers);
    }

    @Test
    void reloadWithoutInstalledConfigurationExplainsWhy() {
        Services.clear();

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> Configs.reload(new SampleConfig()));

        assertTrue(exception.getMessage().contains("Blueprint.register"));
    }

    @Test
    void configWithoutTheAnnotationIsRejected() {
        final ConfigException exception = assertThrows(ConfigException.class, () -> configuration.load(new Object()));

        assertTrue(exception.getMessage().contains("@Configuration"));
    }

    @Configuration("component.mem")
    public static final class ComponentConfig extends SampleConfig {
    }

    @Configuration(value = "arena.mem", path = "arenas")
    public static final class ArenaConfig extends SampleConfig {
    }

    @Configuration("settings.toml")
    public static final class TomlConfig extends SampleConfig {
    }
}
