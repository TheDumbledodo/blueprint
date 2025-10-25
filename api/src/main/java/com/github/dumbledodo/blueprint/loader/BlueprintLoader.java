package com.github.dumbledodo.blueprint.loader;

import com.github.dumbledodo.blueprint.annotation.Component;
import com.github.dumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.dumbledodo.blueprint.service.Services;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlueprintLoader {

    public static void loadComponents(Class<?> main) {
        for (Class<?> clazz : findComponentClasses(main)) {
            try {
                createComponentInstance(clazz);

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static List<Class<?>> findComponentClasses(Class<?> main) {
        final String basePackage = main.getPackage().getName();

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(basePackage)
                .scan()) {

            return scanResult.getAllClasses().stream()
                    .map(ClassInfo::loadClass)
                    .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                    .collect(Collectors.toList());
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

            } else if (paramType.isAnnotationPresent(Component.class)) {
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