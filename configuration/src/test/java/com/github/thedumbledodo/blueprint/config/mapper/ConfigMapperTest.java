package com.github.thedumbledodo.blueprint.config.mapper;

import com.github.thedumbledodo.blueprint.chat.Message;
import com.github.thedumbledodo.blueprint.chat.Text;
import com.github.thedumbledodo.blueprint.config.ConfigException;
import com.github.thedumbledodo.blueprint.config.ConfigProperties;
import com.github.thedumbledodo.blueprint.config.fixture.*;
import com.github.thedumbledodo.blueprint.config.model.ConfigSection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMapperTest {

    private final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder().build());

    @Test
    void fieldsAreWrittenInDeclarationOrderWithCommentsAndHeader() {
        final ConfigSection section = mapper.serialize(new SampleConfig());
        final List<String> keys = new ArrayList<>(section.getKeys());

        assertEquals("reloadedPlugin", keys.get(0));
        assertEquals("maxPlayers", keys.get(1));
        assertEquals(List.of("Sent after /minigame reload"), section.getComments("reloadedPlugin"));
        assertEquals(List.of("Sample plugin settings.", "MiniMessage is supported."), section.getHeader());
        assertEquals(List.of("Seconds before the game starts"), ((ConfigSection) section.get("arena")).getComments("countdown"));
        assertEquals(List.of("Item to give"), ((ConfigSection) section.get("reward")).getComments("item"));
    }

    @Test
    void staticAndTransientFieldsAreSkipped() {
        final Set<String> keys = mapper.serialize(new SampleConfig()).getKeys();

        assertFalse(keys.contains("ignoredStatic"));
        assertFalse(keys.contains("cache"));
        assertTrue(keys.contains("version"));
    }

    @Test
    void roundTripKeepsEveryType() {
        final SampleConfig source = new SampleConfig();

        source.reloadedPlugin = "<red>Changed";
        source.maxPlayers = 32;
        source.cooldownMillis = 9_000_000_000L;
        source.announce = false;
        source.multiplier = 0.25;
        source.prefix = '#';
        source.difficulty = Difficulty.HARD;
        source.worlds = new ArrayList<>(List.of("lobby"));
        source.modes = new LinkedHashSet<>(List.of(Difficulty.NORMAL));
        source.slots = new int[]{1, 2};
        source.arena.countdown = 3;
        source.arenas.put("forest", new ArenaSection());
        source.rewardsByDifficulty.put(Difficulty.EASY, 5);
        source.reward = new Reward("emerald", 7);
        source.owner = UUID.fromString("00000000-0000-0000-0000-000000000002");
        source.welcome = Message.of("<green>Hello");
        source.title = Text.translate("<aqua>New");
        source.price = new BigDecimal("0.10");
        source.nothing = "set";
        source.anything = List.of("a", "b");

        final SampleConfig target = new SampleConfig();

        mapper.apply(mapper.serialize(source).toMap(), target);

        assertEquals("<red>Changed", target.reloadedPlugin);
        assertEquals(32, target.maxPlayers);
        assertEquals(9_000_000_000L, target.cooldownMillis);
        assertFalse(target.announce);
        assertEquals(0.25, target.multiplier);
        assertEquals('#', target.prefix);
        assertEquals(Difficulty.HARD, target.difficulty);
        assertEquals(List.of("lobby"), target.worlds);
        assertEquals(Set.of(Difficulty.NORMAL), target.modes);
        assertArrayEquals(new int[]{1, 2}, target.slots);
        assertEquals(3, target.arena.countdown);
        assertEquals(Set.of("desert", "forest"), target.arenas.keySet());
        assertEquals(Map.of(Difficulty.HARD, 50, Difficulty.EASY, 5), target.rewardsByDifficulty);
        assertEquals(new Reward("emerald", 7), target.reward);
        assertEquals(source.owner, target.owner);
        assertEquals(source.welcome, target.welcome);
        assertEquals(source.title, target.title);
        assertEquals(new BigDecimal("0.10"), target.price);
        assertEquals("set", target.nothing);
        assertEquals(List.of("a", "b"), target.anything);
    }

    @Test
    void bigDecimalIsStoredAsTextToKeepPrecision() {
        assertEquals("19.99", mapper.serialize(new SampleConfig()).get("price"));
    }

    @Test
    void missingKeysKeepTheirDefaults() {
        final SampleConfig config = new SampleConfig();

        mapper.apply(Map.of("arena", Map.of("countdown", 5)), config);

        assertEquals(5, config.arena.countdown);
        assertEquals(List.of("arena_1"), config.arena.worlds);
        assertEquals(16, config.maxPlayers);
    }

    @Test
    void nestedSectionIsUpdatedInPlace() {
        final SampleConfig config = new SampleConfig();
        final ArenaSection arena = config.arena;

        mapper.apply(Map.of("arena", Map.of("countdown", 5)), config);

        assertSame(arena, config.arena);
    }

    @Test
    void unknownKeysAreIgnored() {
        final SampleConfig config = new SampleConfig();

        mapper.apply(Map.of("doesNotExist", 1, "maxPlayers", 20), config);

        assertEquals(20, config.maxPlayers);
    }

    @Test
    void finalFieldsAreNeverSet() {
        final SampleConfig config = new SampleConfig();

        mapper.apply(Map.of("version", "2"), config);

        assertEquals("1", config.version);
    }

    @Test
    void missingRecordComponentKeepsTheCurrentValue() {
        final SampleConfig config = new SampleConfig();

        mapper.apply(Map.of("reward", Map.of("amount", 9)), config);

        assertEquals(new Reward("diamond", 9), config.reward);
    }

    @Test
    void enumsAreReadCaseInsensitively() {
        final SampleConfig config = new SampleConfig();

        mapper.apply(Map.of("difficulty", "hard", "modes", List.of("easy")), config);

        assertEquals(Difficulty.HARD, config.difficulty);
        assertEquals(Set.of(Difficulty.EASY), config.modes);
    }

    @Test
    void unknownEnumListsTheValidValues() {
        final ConfigException exception = assertThrows(ConfigException.class,
                () -> mapper.apply(Map.of("difficulty", "nightmare"), new SampleConfig()));

        assertEquals("difficulty: expected one of [EASY, NORMAL, HARD] but got \"nightmare\"", exception.getMessage());
    }

    @Test
    void wholeNumberDoublesAreAcceptedForInts() {
        final SampleConfig config = new SampleConfig();

        mapper.apply(Map.of("maxPlayers", 24.0, "multiplier", 2), config);

        assertEquals(24, config.maxPlayers);
        assertEquals(2.0, config.multiplier);
    }

    @Test
    void numbersInQuotesAreAccepted() {
        final SampleConfig config = new SampleConfig();

        mapper.apply(Map.of("maxPlayers", "12", "announce", "FALSE"), config);

        assertEquals(12, config.maxPlayers);
        assertFalse(config.announce);
    }

    @Test
    void fractionsAreRejectedForWholeNumbers() {
        final ConfigException exception = assertThrows(ConfigException.class,
                () -> mapper.apply(Map.of("maxPlayers", 16.5), new SampleConfig()));

        assertEquals("maxPlayers: expected a whole number but got 16.5", exception.getMessage());
    }

    @Test
    void outOfRangeNumbersAreRejected() {
        final ConfigException exception = assertThrows(ConfigException.class,
                () -> mapper.apply(Map.of("maxPlayers", 3_000_000_000L), new SampleConfig()));

        assertTrue(exception.getMessage().startsWith("maxPlayers: 3000000000 is out of range"));
    }

    @Test
    void wrongTypeErrorHasTheNestedPath() {
        final ConfigException exception = assertThrows(ConfigException.class,
                () -> mapper.apply(Map.of("arena", Map.of("countdown", "ten")), new SampleConfig()));

        assertEquals("arena.countdown: expected a number but got \"ten\"", exception.getMessage());
    }

    @Test
    void listElementErrorsHaveTheIndex() {
        final ConfigException exception = assertThrows(ConfigException.class,
                () -> mapper.apply(Map.of("worlds", List.of("ok", Map.of("a", 1))), new SampleConfig()));

        assertEquals("worlds[1]: expected text but got a section", exception.getMessage());
    }

    @Test
    void sectionExpectedButTextGiven() {
        final ConfigException exception = assertThrows(ConfigException.class,
                () -> mapper.apply(Map.of("arena", "flat"), new SampleConfig()));

        assertEquals("arena: expected a section but got \"flat\"", exception.getMessage());
    }

    @Test
    void outputNullsOffSkipsNullValues() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder().outputNulls(false).build());

        assertFalse(mapper.serialize(new SampleConfig()).contains("nothing"));
        assertTrue(this.mapper.serialize(new SampleConfig()).contains("nothing"));
    }

    @Test
    void inputNullsOffKeepsDefaults() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder().inputNulls(false).build());
        final SampleConfig config = new SampleConfig();
        final Map<String, Object> values = new HashMap<>();

        values.put("reloadedPlugin", null);
        mapper.apply(values, config);

        assertEquals("<green>Reloaded.", config.reloadedPlugin);
    }

    @Test
    void inputNullsOnSetsNull() {
        final SampleConfig config = new SampleConfig();
        final Map<String, Object> values = new HashMap<>();

        values.put("reloadedPlugin", null);
        values.put("maxPlayers", null);
        mapper.apply(values, config);

        assertNull(config.reloadedPlugin);
        assertEquals(16, config.maxPlayers);
    }

    @Test
    void nameFormatterChangesKeys() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder().nameFormatter(NameFormatter.LOWER_KEBAB).build());
        final SampleConfig config = new SampleConfig();

        assertTrue(mapper.serialize(config).contains("max-players"));

        mapper.apply(Map.of("max-players", 40), config);

        assertEquals(40, config.maxPlayers);
    }

    @Test
    void nameFormatters() {
        assertEquals("max-players", NameFormatter.LOWER_KEBAB.format("maxPlayers"));
        assertEquals("max_players", NameFormatter.LOWER_UNDERSCORE.format("maxPlayers"));
        assertEquals("maxPlayers", NameFormatter.IDENTITY.format("maxPlayers"));
    }

    @Test
    void inheritedFieldsComeFirst() {
        final List<String> keys = new ArrayList<>(mapper.serialize(new ChildConfig()).getKeys());

        assertEquals("reloadedPlugin", keys.getFirst());
        assertEquals("childOnly", keys.getLast());
    }

    @Test
    void sectionWithoutNoArgConstructorExplainsTheFix() {
        final ConfigException exception = assertThrows(ConfigException.class,
                () -> mapper.apply(Map.of("holder", Map.of("value", "x")), new NoDefaultConstructorConfig()));

        assertTrue(exception.getMessage().contains("needs a no-arg constructor or a registered serializer"));
    }

    @Test
    void jdkTypesWithoutSerializerAreRejectedWithTheirName() {
        final ConfigException exception = assertThrows(ConfigException.class, () -> mapper.serialize(new DateConfig()));

        assertEquals("date: no serializer registered for java.time.LocalDate", exception.getMessage());
    }

    public static final class ChildConfig extends SampleConfig {

        public String childOnly = "child";
    }

    public static final class Holder {

        public String value;

        public Holder(String value) {
            this.value = value;
        }
    }

    public static final class NoDefaultConstructorConfig {

        public Holder holder;
    }

    public static final class DateConfig {

        public LocalDate date = LocalDate.of(2026, 1, 1);
    }
}
