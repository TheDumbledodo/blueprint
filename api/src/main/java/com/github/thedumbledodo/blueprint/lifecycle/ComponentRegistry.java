package com.github.thedumbledodo.blueprint.lifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ComponentRegistry {

    private static final List<ComponentListener> LISTENERS = new CopyOnWriteArrayList<>();

    public static void registerListener(ComponentListener listener) {
        LISTENERS.add(listener);
    }

    public static Object processRegisteredComponent(Class<?> type, Object instance) {
        Object result = instance;

        for (ComponentListener listener : LISTENERS) {
            final Object modified = listener.onComponentRegistered(type, result);

            if (modified == null) {
                continue;
            }
            result = modified;
        }
        return result;
    }

    public static void clear() {
        LISTENERS.clear();
    }
}
