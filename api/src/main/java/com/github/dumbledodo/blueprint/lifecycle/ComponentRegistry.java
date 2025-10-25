package com.github.dumbledodo.blueprint.lifecycle;

import java.util.ArrayList;
import java.util.List;

public final class ComponentRegistry {

    private static final List<ComponentListener> listeners = new ArrayList<>();

    public static void registerListener(ComponentListener listener) {
        listeners.add(listener);
    }

    public static Object processRegisteredComponent(Class<?> type, Object instance) {
        Object result = instance;

        for (ComponentListener listener : listeners) {
            final Object modified = listener.onComponentRegistered(type, result);

            if (modified == null) {
                continue;
            }
            result = modified;
        }
        return result;
    }
}