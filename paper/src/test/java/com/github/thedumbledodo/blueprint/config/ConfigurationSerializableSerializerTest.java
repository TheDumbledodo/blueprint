package com.github.thedumbledodo.blueprint.config;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.github.thedumbledodo.blueprint.BlueprintConfiguration;
import com.github.thedumbledodo.blueprint.config.annotation.Configuration;
import com.github.thedumbledodo.blueprint.config.json.JsonFormat;
import com.github.thedumbledodo.blueprint.config.yaml.YamlFormat;
import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.util.Vector;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationSerializableSerializerTest {

    private final ConfigurationSerializableSerializer serializer = new ConfigurationSerializableSerializer();

    @TempDir
    Path directory;

    @BeforeEach
    void setUp() {
        final ServerMock server = MockBukkit.mock();

        server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private BlueprintConfiguration configuration() {
        return BlueprintConfiguration.builder()
                .configDirectory(directory)
                .properties(ConfigProperties.builder()
                        .serializer(org.bukkit.configuration.serialization.ConfigurationSerializable.class, serializer)
                        .build())
                .format(new YamlFormat())
                .format(new JsonFormat())
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"places.yml", "places.json"})
    void vectorsAndLocationsRoundTrip(String fileName) {
        final BlueprintConfiguration configuration = configuration();
        final PlacesConfig config = configuration.load(places(fileName));

        config.offset = new Vector(1.5, 2, -3);
        config.spawn = new Location(Bukkit.getWorld("world"), 10, 64, -20, 90f, 0f);
        configuration.save(config);

        final PlacesConfig reloaded = configuration.load(places(fileName));

        assertEquals(config.offset, reloaded.offset);
        assertEquals(config.spawn, reloaded.spawn);
    }

    @Test
    void nestedSerializablesAreConvertedBothWays() {
        final FireworkEffect effect = FireworkEffect.builder()
                .with(FireworkEffect.Type.STAR)
                .withColor(Color.RED, Color.BLUE)
                .withFade(Color.WHITE)
                .build();

        final Map<?, ?> serialized = (Map<?, ?>) serializer.serialize(effect);
        final List<?> colors = (List<?>) serialized.get("colors");

        assertEquals(ConfigurationSerialization.getAlias(FireworkEffect.class), serialized.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY));
        assertInstanceOf(Map.class, colors.getFirst());
        assertEquals(effect, serializer.deserialize(serialized));
    }

    @Test
    void oldConfigLibStringsAreReadAndRewrittenAsSections() throws Exception {
        final Path file = directory.resolve("places.yml");

        Files.writeString(file, "offset: \"==: Vector\\nx: 1.0\\ny: 2.0\\nz: 3.0\\n\"\n");

        final PlacesConfig config = configuration().load(new PlacesConfig());
        final String written = Files.readString(file);

        assertEquals(new Vector(1, 2, 3), config.offset);
        assertTrue(written.contains("offset:\n  ==: Vector"));
    }

    @Test
    void sectionsWithoutTypeKeyAreRejected() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> serializer.deserialize(Map.of("x", 1.0)));

        assertTrue(exception.getMessage().contains("=="));
    }

    @Test
    void keyedValuesAreStoredByKey() {
        final Rarity epic = new Rarity(NamespacedKey.minecraft("epic"));
        final KeyedSerializer<Rarity> keyed = new KeyedSerializer<>(() -> new SingleRegistry<>(epic));

        assertEquals("minecraft:epic", keyed.serialize(epic));
        assertEquals(epic, keyed.deserialize("minecraft:epic"));
        assertEquals(epic, keyed.deserialize("EPIC"));
        assertThrows(IllegalArgumentException.class, () -> keyed.deserialize("minecraft:common"));
        assertThrows(IllegalArgumentException.class, () -> keyed.deserialize("not a key!"));
    }

    private static PlacesConfig places(String fileName) {
        return fileName.endsWith(".json") ? new JsonPlacesConfig() : new PlacesConfig();
    }

    @Configuration("places.yml")
    public static class PlacesConfig {

        public Vector offset = new Vector(0, 0, 0);
        public Location spawn = null;
    }

    @Configuration("places.json")
    public static final class JsonPlacesConfig extends PlacesConfig {
    }

    private record Rarity(NamespacedKey key) implements Keyed {

        @Override
        public @NotNull NamespacedKey getKey() {
            return key;
        }
    }

    private record SingleRegistry<T extends Keyed>(T value) implements Registry<T> {

        @Override
        public T get(@NotNull NamespacedKey key) {
            return value.getKey().equals(key) ? value : null;
        }

        @Override
        public @NotNull T getOrThrow(@NotNull NamespacedKey key) {
            final T found = get(key);

            if (found == null) {
                throw new IllegalArgumentException(key.asString());
            }
            return found;
        }

        @Override
        public @NotNull Stream<T> stream() {
            return Stream.of(value);
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return List.of(value).iterator();
        }

        @Override
        public NamespacedKey getKey(@NotNull T element) {
            return value.equals(element) ? value.getKey() : null;
        }

        @Override
        public boolean hasTag(@NotNull TagKey<T> key) {
            return false;
        }

        @Override
        public @NotNull Tag<T> getTag(@NotNull TagKey<T> key) {
            throw new NoSuchElementException(key.toString());
        }

        @Override
        public @NotNull Collection<Tag<T>> getTags() {
            return List.of();
        }

        @Override
        public @NotNull Stream<NamespacedKey> keyStream() {
            return Stream.of(value.getKey());
        }

        @Override
        public int size() {
            return 1;
        }
    }
}
