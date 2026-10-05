package com.github.thedumbledodo.blueprint.config;

import com.github.thedumbledodo.blueprint.config.serializer.Serializer;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

public final class ConfigurationSerializableSerializer implements Serializer<ConfigurationSerializable, Object> {

    @Override
    public Object serialize(ConfigurationSerializable element) {
        return serializeValue(element);
    }

    @Override
    public ConfigurationSerializable deserialize(Object element) {
        final Object value = element instanceof String legacy ? loadLegacy(legacy) : deserializeValue(element);

        if (!(value instanceof ConfigurationSerializable serializable)) {
            throw new IllegalArgumentException("expected a section with a \"" + ConfigurationSerialization.SERIALIZED_TYPE_KEY + "\" type key");
        }
        return serializable;
    }

    private Object serializeValue(Object value) {
        if (value instanceof ConfigurationSerializable serializable) {
            final Map<String, Object> map = new LinkedHashMap<>();

            map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));

            for (Map.Entry<String, Object> entry : serializable.serialize().entrySet()) {
                map.put(entry.getKey(), serializeValue(entry.getValue()));
            }
            return map;
        }

        if (value instanceof Map<?, ?> map) {
            final Map<String, Object> result = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), serializeValue(entry.getValue()));
            }
            return result;
        }

        if (value instanceof Collection<?> collection) {
            final List<Object> list = new ArrayList<>(collection.size());

            for (Object element : collection) {
                list.add(serializeValue(element));
            }
            return list;
        }
        return value;
    }

    private Object deserializeValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            final Map<String, Object> converted = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                converted.put(String.valueOf(entry.getKey()), deserializeValue(entry.getValue()));
            }

            if (converted.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                return ConfigurationSerialization.deserializeObject(converted);
            }
            return converted;
        }

        if (value instanceof Collection<?> collection) {
            final List<Object> list = new ArrayList<>(collection.size());

            for (Object element : collection) {
                list.add(deserializeValue(element));
            }
            return list;
        }
        return value;
    }

    private Object loadLegacy(String legacy) {
        final DumperOptions options = new DumperOptions();

        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        return new Yaml(new YamlConstructor(new LoaderOptions()), new YamlRepresenter(options), options).load(legacy);
    }
}
