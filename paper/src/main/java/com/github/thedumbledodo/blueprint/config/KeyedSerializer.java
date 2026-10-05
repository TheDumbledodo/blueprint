package com.github.thedumbledodo.blueprint.config;

import com.github.thedumbledodo.blueprint.config.serializer.Serializer;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public final class KeyedSerializer<T extends Keyed> implements Serializer<T, Object> {

    private final Supplier<Registry<@NotNull T>> registry;

    public KeyedSerializer(Supplier<Registry<@NotNull T>> registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public Object serialize(T element) {
        return element.getKey().asString();
    }

    @Override
    public T deserialize(Object element) {
        final String text = String.valueOf(element).trim();
        final NamespacedKey key = NamespacedKey.fromString(text.toLowerCase());

        if (key == null) {
            throw new IllegalArgumentException("\"" + text + "\" is not a valid key");
        }

        final T value = registry.get().get(key);

        if (value == null) {
            throw new IllegalArgumentException("nothing is registered as " + key.asString());
        }
        return value;
    }
}
