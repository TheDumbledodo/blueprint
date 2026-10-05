package com.github.thedumbledodo.blueprint.loader;

import com.github.thedumbledodo.blueprint.annotation.BlueprintComponent;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.service.Services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BlueprintLoader {

    private static final ThreadLocal<Set<Class<?>>> CREATING = ThreadLocal.withInitial(LinkedHashSet::new);

    public static void loadComponents(Class<?> main) {
        for (Class<?> clazz : BlueprintScanner.scan(main, BlueprintLoader::isComponent)) {
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

        final Set<Class<?>> creating = CREATING.get();

        if (!creating.add(clazz)) {
            throw new IllegalStateException("Circular dependency: " + describeCycle(creating, clazz));
        }

        try {
            return instantiate(clazz);

        } finally {
            creating.remove(clazz);
        }
    }

    private static <T> T instantiate(Class<T> clazz) throws Exception {
        final Constructor<?> constructor = getComponentConstructor(clazz);
        final List<Object> paramList = new ArrayList<>();

        for (Class<?> paramType : constructor.getParameterTypes()) {
            final Object serviceInstance = Services.getService(paramType);

            if (serviceInstance != null) {
                paramList.add(serviceInstance);

            } else if (isComponent(paramType)) {
                paramList.add(createComponentInstance(paramType));

            } else {
                throw new IllegalStateException("No component found for required type: " + paramType.getName() + " in " + clazz.getName());
            }
        }

        final T instance;

        try {
            instance = (T) constructor.newInstance(paramList.toArray());

        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw exception;
        }

        final T result = (T) ComponentRegistry.processRegisteredComponent(clazz, instance);

        Services.register(clazz, result);
        return result;
    }

    public static boolean isComponent(Class<?> clazz) {
        if (clazz.isAnnotation()) {
            return false;
        }

        if (clazz.isAnnotationPresent(BlueprintComponent.class)) {
            return true;
        }

        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(BlueprintComponent.class)) {
                return true;
            }
        }
        return false;
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

    private static String describeCycle(Set<Class<?>> creating, Class<?> clazz) {
        final StringBuilder builder = new StringBuilder();
        boolean inCycle = false;

        for (Class<?> current : creating) {
            if (current == clazz) {
                inCycle = true;
            }

            if (!inCycle) {
                continue;
            }
            builder.append(current.getSimpleName()).append(" -> ");
        }
        return builder.append(clazz.getSimpleName()).toString();
    }
}
