package com.github.thedumbledodo.blueprint.config.model;

import lombok.Getter;

import java.util.*;

@Getter
public final class ConfigSection {

    private final Map<String, Object> values = new LinkedHashMap<>();
    private final Map<String, List<String>> comments = new LinkedHashMap<>();

    private List<String> header = List.of();

    public static ConfigSection fromMap(Map<?, ?> map) {
        final ConfigSection section = new ConfigSection();

        if (map == null) {
            return section;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            section.set(String.valueOf(entry.getKey()), fromValue(entry.getValue()));
        }
        return section;
    }

    public void set(String key, Object value) {
        values.put(key, value);
    }

    public void set(String key, Object value, List<String> comments) {
        set(key, value);

        if (comments == null || comments.isEmpty()) {
            return;
        }
        this.comments.put(key, List.copyOf(comments));
    }

    public Object get(String key) {
        return values.get(key);
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(values.keySet());
    }

    public List<String> getComments(String key) {
        return comments.getOrDefault(key, List.of());
    }

    public void setHeader(List<String> header) {
        this.header = header == null ? List.of() : List.copyOf(header);
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            map.put(entry.getKey(), toPlain(entry.getValue()));
        }
        return map;
    }

    public static Object fromValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return fromMap(map);
        }

        if (value instanceof Collection<?> collection) {
            final List<Object> list = new ArrayList<>(collection.size());

            for (Object element : collection) {
                list.add(fromValue(element));
            }
            return list;
        }
        return value;
    }

    public static Object toPlain(Object value) {
        if (value instanceof ConfigSection section) {
            return section.toMap();
        }

        if (value instanceof Map<?, ?> map) {
            final Map<String, Object> plain = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                plain.put(String.valueOf(entry.getKey()), toPlain(entry.getValue()));
            }
            return plain;
        }

        if (value instanceof Collection<?> collection) {
            final List<Object> list = new ArrayList<>(collection.size());

            for (Object element : collection) {
                list.add(toPlain(element));
            }
            return list;
        }
        return value;
    }
}
