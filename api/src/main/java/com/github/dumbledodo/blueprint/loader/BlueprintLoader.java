package com.github.dumbledodo.blueprint.loader;

import com.github.dumbledodo.blueprint.annotation.BlueprintComponent;
import com.github.dumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.dumbledodo.blueprint.service.Services;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class BlueprintLoader {

    public static void loadComponents(Class<?> main) {
        for (Class<?> clazz : BlueprintScanner.scan(main, clazz -> clazz.isAnnotationPresent(BlueprintComponent.class))) {
            try {
                createComponentInstance(clazz);

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static <T> T createComponentInstance(Class<T> clazz) throws Exception {
        final T service = Services.getService(clazz);

        if (service != null) {
            return service;
        }

        final Constructor<?> constructor = getComponentConstructor(clazz);

        if (constructor == null) {
            return null;
        }

        final List<Object> paramList = new ArrayList<>();

        for (Class<?> paramType : constructor.getParameterTypes()) {
            final Object serviceInstance = Services.getService(paramType);

            if (serviceInstance != null) {
                paramList.add(serviceInstance);

            } else if (paramType.isAnnotationPresent(BlueprintComponent.class)) {
                final Object paramInstance = createComponentInstance((Class<T>) paramType);

                if (paramInstance == null) {
                    continue;
                }
                paramList.add(paramInstance);

            } else {
                throw new Exception("No component found for required type: " + paramType.getName() + " in " + constructor.getDeclaringClass().getName());
            }
        }

        final T instance = (T) constructor.newInstance(paramList.toArray());
        final T result = (T) ComponentRegistry.processRegisteredComponent(clazz, instance);

        Services.register(clazz, result);

        return result;
    }

    private static Constructor<?> getComponentConstructor(Class<?> clazz) {
        if (clazz.isInterface()) {
            throw new IllegalStateException("Cannot create a component from an interface: " + clazz.getName());
        }

        final Constructor<?>[] constructors = clazz.getConstructors();

        if (constructors.length == 0) {
            throw new IllegalStateException("No public constructors found for class: " + clazz.getName());
        }
        return constructors[0];
    }
}