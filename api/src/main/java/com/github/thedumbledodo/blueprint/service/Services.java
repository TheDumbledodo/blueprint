package com.github.thedumbledodo.blueprint.service;

import com.github.thedumbledodo.blueprint.loader.BlueprintLoader;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Services {

    private static final Map<Class<?>, Object> REGISTERED_SERVICES = new ConcurrentHashMap<>();

    public static void register(Class<?> clazz, Object instance) {
        Objects.requireNonNull(clazz, "clazz");

        if (instance == null) {
            throw new IllegalArgumentException("Cannot register a null instance for " + clazz.getName());
        }
        REGISTERED_SERVICES.put(clazz, instance);
    }

    public static <T> void register(T instance) {
        register(instance.getClass(), instance);
    }

    public static <T> T getService(Class<T> clazz) {
        return (T) REGISTERED_SERVICES.get(clazz);
    }

    public static <T> boolean isPresent(Class<T> clazz) {
        return REGISTERED_SERVICES.containsKey(clazz);
    }

    public static <T> T loadIfPresent(Class<T> clazz) {
        final T instance = getService(clazz);

        if (instance != null) {
            return instance;
        }

        try {
            return BlueprintLoader.createComponentInstance(clazz);

        } catch (Exception exception) {
            throw new RuntimeException("Failed to load component: " + clazz.getName(), exception);
        }
    }

    public static Map<Class<?>, Object> getRegisteredServices() {
        return REGISTERED_SERVICES;
    }

    public static void clear() {
        REGISTERED_SERVICES.clear();
    }
}
