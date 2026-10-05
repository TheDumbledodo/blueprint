package com.github.thedumbledodo.blueprint.config.yaml;

import com.github.thedumbledodo.blueprint.BlueprintConfiguration;
import com.github.thedumbledodo.blueprint.config.ConfigException;
import com.github.thedumbledodo.blueprint.config.ConfigProperties;
import com.github.thedumbledodo.blueprint.config.mapper.ConfigMapper;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.service.Services;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlFormatTest {

    private final YamlFormat format = new YamlFormat();
    private final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder().build());

    @TempDir
    Path directory;

    @AfterEach
    void reset() {
        Services.clear();
        ComponentRegistry.clear();
    }

    @Test
    void outputMatchesTheGoldenFile() throws IOException {
        assertEquals(golden(), format.write(mapper.serialize(new MessagesConfig())));
    }

    @Test
    void readingTheOutputGivesBackTheSameValues() {
        final MessagesConfig source = new MessagesConfig();

        source.reloadedPlugin = "<red>Changed";
        source.arena.countdown = 3;
        source.scores.put("alex", 9);

        final MessagesConfig target = new MessagesConfig();

        mapper.apply(format.read(format.write(mapper.serialize(source))), target);

        assertEquals("<red>Changed", target.reloadedPlugin);
        assertEquals(3, target.arena.countdown);
        assertEquals(Map.of("steve", 3, "alex", 9), target.scores);
        assertEquals("yes", target.looksLikeBoolean);
        assertEquals("123", target.looksLikeNumber);
        assertEquals(target.longLine, source.longLine);
    }

    @Test
    void longListsStayOneItemPerLine() {
        final MessagesConfig config = new MessagesConfig();

        config.arena.worlds = new ArrayList<>(List.of(
                "a_very_long_world_name_number_one",
                "a_very_long_world_name_number_two"));

        final String written = format.write(mapper.serialize(config));

        assertTrue(written.contains("  worlds:\n  - a_very_long_world_name_number_one\n  - a_very_long_world_name_number_two\n"));
        final Map<?, ?> arena = (Map<?, ?>) format.read(written).get("arena");

        assertEquals(config.arena.worlds, arena.get("worlds"));
    }

    @Test
    void emptyDocumentsAreEmptySections() {
        assertTrue(format.read("").isEmpty());
        assertTrue(format.read("   \n").isEmpty());
        assertTrue(format.read("~").isEmpty());
        assertTrue(format.read("# only a comment\n").isEmpty());
    }

    @Test
    void topLevelListIsRejected() {
        final ConfigException exception = assertThrows(ConfigException.class, () -> format.read("- a\n- b\n"));

        assertTrue(exception.getMessage().startsWith("expected a section at the top of the file"));
    }

    @Test
    void invalidYamlIsReported() {
        final ConfigException exception = assertThrows(ConfigException.class, () -> format.read("a: [unclosed\n"));

        assertTrue(exception.getMessage().startsWith("invalid YAML"));
    }

    @Test
    void javaTypeTagsAreNotConstructed() {
        assertThrows(ConfigException.class, () -> format.read("a: !!javax.script.ScriptEngineManager []\n"));
    }

    @Test
    void nonTextKeysBecomeText() {
        final Map<String, Object> values = format.read("1: one\ntrue: yes\nnested:\n  2: two\n");

        assertEquals("one", values.get("1"));
        assertEquals(Map.of("2", "two"), values.get("nested"));
    }

    @Test
    void handWrittenFileIsLoadedAndCommentsAreRefreshed() throws IOException {
        final Path file = directory.resolve("messages.yml");

        Files.writeString(file, "# my own note\nreloadedPlugin: <yellow>Done\narena:\n  countdown: 30\n");

        final BlueprintConfiguration configuration = BlueprintConfiguration.builder()
                .configDirectory(directory)
                .format(format)
                .build();

        final MessagesConfig config = configuration.load(new MessagesConfig());
        final String written = Files.readString(file, StandardCharsets.UTF_8);

        assertEquals("<yellow>Done", config.reloadedPlugin);
        assertEquals(30, config.arena.countdown);
        assertEquals(List.of("arena_1", "arena 2"), config.arena.worlds);
        assertFalse(written.contains("my own note"));
        assertTrue(written.contains("# Sent after /minigame reload\nreloadedPlugin: <yellow>Done"));
    }

    private String golden() throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("messages.yml")) {
            assertNotNull(stream, "missing golden file " + "messages.yml");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }
}
