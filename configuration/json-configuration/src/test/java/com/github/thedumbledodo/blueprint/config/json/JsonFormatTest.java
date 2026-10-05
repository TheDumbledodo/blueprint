package com.github.thedumbledodo.blueprint.config.json;

import com.github.thedumbledodo.blueprint.BlueprintConfiguration;
import com.github.thedumbledodo.blueprint.config.ConfigException;
import com.github.thedumbledodo.blueprint.config.ConfigProperties;
import com.github.thedumbledodo.blueprint.config.mapper.ConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonFormatTest {

    private final JsonFormat format = new JsonFormat();
    private final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder().build());

    @TempDir
    Path directory;

    @Test
    void outputMatchesTheGoldenFile() throws IOException {
        assertEquals(golden(), format.write(mapper.serialize(new StatsConfig())));
    }

    @Test
    void miniMessageTagsAreNotEscaped() {
        final String json = format.write(mapper.serialize(new StatsConfig()));

        assertTrue(json.contains("<gold><bold>Stats</bold></gold>"));
        assertFalse(json.contains("\\u003c"));
    }

    @Test
    void commentsAreNotWritten() {
        assertFalse(format.write(mapper.serialize(new StatsConfig())).contains("Shown on the scoreboard"));
    }

    @Test
    void numbersComeBackAsTheirNaturalType() {
        final Map<String, Object> values = format.read("{\"small\": 1, \"large\": 9000000000, \"decimal\": 1.5, \"huge\": 99999999999999999999}");

        assertEquals(1, values.get("small"));
        assertEquals(9_000_000_000L, values.get("large"));
        assertEquals(1.5, values.get("decimal"));
        assertEquals(new java.math.BigInteger("99999999999999999999"), values.get("huge"));
    }

    @Test
    void numbersAreConvertedToTheFieldTypes() {
        final StatsConfig config = new StatsConfig();

        mapper.apply(format.read("{\"kills\": 7.0, \"playtimeMillis\": 5, \"accuracy\": 1, \"ratio\": 2}"), config);

        assertEquals(7, config.kills);
        assertEquals(5L, config.playtimeMillis);
        assertEquals(1.0f, config.accuracy);
        assertEquals(2.0, config.ratio);
    }

    @Test
    void roundTripKeepsValues() {
        final StatsConfig source = new StatsConfig();

        source.kills = 99;
        source.topPlayers.put("alex", 1);

        final StatsConfig target = new StatsConfig();

        mapper.apply(format.read(format.write(mapper.serialize(source))), target);

        assertEquals(99, target.kills);
        assertEquals(Map.of("steve", 40, "alex", 1), target.topPlayers);
        assertNull(target.nothing);
    }

    @Test
    void emptyAndNullDocumentsAreEmptySections() {
        assertTrue(format.read("").isEmpty());
        assertTrue(format.read("null").isEmpty());
    }

    @Test
    void topLevelArrayIsRejected() {
        final ConfigException exception = assertThrows(ConfigException.class, () -> format.read("[1, 2]"));

        assertTrue(exception.getMessage().startsWith("expected an object at the top of the file"));
    }

    @Test
    void invalidJsonIsReported() {
        final ConfigException exception = assertThrows(ConfigException.class, () -> format.read("{\"a\": "));

        assertTrue(exception.getMessage().startsWith("invalid JSON"));
    }

    @Test
    void configurationPicksJsonByExtension() throws IOException {
        final BlueprintConfiguration configuration = BlueprintConfiguration.builder()
                .configDirectory(directory)
                .format(format)
                .build();

        Files.writeString(directory.resolve("stats.json"), "{\"kills\": 3}");

        final StatsConfig config = configuration.load(new StatsConfig());

        assertEquals(3, config.kills);
        assertTrue(Files.readString(directory.resolve("stats.json")).contains("\"playtimeMillis\": 9000000000"));
    }

    private String golden() throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("stats.json")) {
            assertNotNull(stream, "missing golden file " + "stats.json");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }
}
