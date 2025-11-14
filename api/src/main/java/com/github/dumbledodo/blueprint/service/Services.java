package com.github.dumbledodo.blueprint.service;

import com.github.dumbledodo.blueprint.annotation.BlueprintComponent;
import com.github.dumbledodo.blueprint.loader.BlueprintLoader;

import java.util.HashMap;
import java.util.Map;

public final class Services {

    private static final Map<Class<?>, Object> REGISTERED_SERVICES = new HashMap<>();

    public static void register(Class<?> clazz, Object instance) {
        REGISTERED_SERVICES.put(clazz, instance);
    }

    public static <T> void register(T instance) {
        register(instance.getClass(), instance);
    }

    public static <T> T getService(Class<T> clazz) {
        return (T) REGISTERED_SERVICES.get(clazz);
    }

    public static <T> T loadIfPresent(Class<T> clazz) {
        final T instance = getService(clazz);

        if (instance != null || !clazz.isAnnotationPresent(BlueprintComponent.class)) {
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
}
