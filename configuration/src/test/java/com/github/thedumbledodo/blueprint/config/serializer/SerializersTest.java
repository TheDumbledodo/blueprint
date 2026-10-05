package com.github.thedumbledodo.blueprint.config.serializer;

import com.github.thedumbledodo.blueprint.config.ConfigException;
import com.github.thedumbledodo.blueprint.config.ConfigProperties;
import com.github.thedumbledodo.blueprint.config.mapper.ConfigMapper;
import com.github.thedumbledodo.blueprint.config.model.ConfigSection;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SerializersTest {

    @Test
    void abstractTypeSerializerMatchesSubtypes() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder()
                .serializer(Animal.class, new NameSerializer("animal"))
                .build());

        final ConfigSection section = mapper.serialize(new PetConfig());

        assertEquals("animal:Rex", section.get("pet"));
    }

    @Test
    void concreteTypeSerializerDoesNotMatchSubtypes() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder()
                .serializer(Dog.class, new NameSerializer("dog"))
                .build());

        assertInstanceOf(ConfigSection.class, mapper.serialize(new PuppyConfig()).get("pet"));
    }

    @Test
    void exactSerializerWinsOverAbstractTypeSerializer() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder()
                .serializer(Animal.class, new NameSerializer("animal"))
                .serializer(Dog.class, new NameSerializer("dog"))
                .build());

        assertEquals("dog:Rex", mapper.serialize(new PetConfig()).get("pet"));
    }

    @Test
    void serializerIsUsedOnLoad() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder()
                .serializer(Dog.class, new NameSerializer("dog"))
                .build());

        final PetConfig config = new PetConfig();

        mapper.apply(Map.of("pet", "dog:Max"), config);

        assertEquals("Max", config.pet.name);
    }

    @Test
    void serializerReturningAMapBecomesASection() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder()
                .serializer(Dog.class, new MapSerializer())
                .build());

        final ConfigSection section = mapper.serialize(new PetConfig());

        assertInstanceOf(ConfigSection.class, section.get("pet"));
        assertEquals("Rex", ((ConfigSection) section.get("pet")).get("name"));
    }

    @Test
    void serializerFailuresIncludeThePath() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder()
                .serializer(Dog.class, new NameSerializer("dog"))
                .build());

        final ConfigException exception = assertThrows(ConfigException.class,
                () -> mapper.apply(Map.of("pet", "cat:Tom"), new PetConfig()));

        assertTrue(exception.getMessage().startsWith("pet: could not read Dog from \"cat:Tom\""));
    }

    @Test
    void builtInUuidRejectsGarbage() {
        final ConfigMapper mapper = new ConfigMapper(ConfigProperties.builder().build());

        final ConfigException exception = assertThrows(ConfigException.class,
                () -> mapper.apply(Map.of("owner", "not-a-uuid"), new UuidConfig()));

        assertTrue(exception.getMessage().startsWith("owner: could not read UUID"));
    }

    public abstract static class Animal {

        public String name;

        public Animal(String name) {
            this.name = name;
        }
    }

    public static class Dog extends Animal {

        public Dog(String name) {
            super(name);
        }
    }

    public static final class Puppy extends Dog {

        public Puppy() {
            super("Bit");
        }
    }

    public static final class PuppyConfig {

        public Puppy pet = new Puppy();
    }

    public static final class PetConfig {

        public Dog pet = new Dog("Rex");
    }

    public static final class UuidConfig {

        public UUID owner;
    }

    private record NameSerializer(String prefix) implements Serializer<Animal, String> {

        @Override
        public String serialize(Animal element) {
            return prefix + ":" + element.name;
        }

        @Override
        public Animal deserialize(String element) {
            if (!element.startsWith(prefix + ":")) {
                throw new IllegalArgumentException("expected prefix " + prefix);
            }
            return new Dog(element.substring(prefix.length() + 1));
        }
    }

    private static final class MapSerializer implements Serializer<Dog, Map<String, Object>> {

        @Override
        public Map<String, Object> serialize(Dog element) {
            final Map<String, Object> map = new LinkedHashMap<>();

            map.put("name", element.name);
            return map;
        }

        @Override
        public Dog deserialize(Map<String, Object> element) {
            return new Dog((String) element.get("name"));
        }
    }
}
