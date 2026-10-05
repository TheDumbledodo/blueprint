package com.github.thedumbledodo.blueprint.lifecycle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComponentRegistryTest {

    @AfterEach
    void reset() {
        ComponentRegistry.clear();
    }

    @Test
    void listenersRunInRegistrationOrderAndSeeEachOthersResult() {
        final List<String> calls = new ArrayList<>();

        ComponentRegistry.registerListener((type, instance) -> {
            calls.add("first:" + instance);
            return instance + "-first";
        });

        ComponentRegistry.registerListener((type, instance) -> {
            calls.add("second:" + instance);
            return instance + "-second";
        });

        final Object result = ComponentRegistry.processRegisteredComponent(String.class, "value");

        assertEquals(List.of("first:value", "second:value-first"), calls);
        assertEquals("value-first-second", result);
    }

    @Test
    void nullResultKeepsThePreviousInstance() {
        ComponentRegistry.registerListener((type, instance) -> null);

        assertEquals("value", ComponentRegistry.processRegisteredComponent(String.class, "value"));
    }

    @Test
    void clearRemovesListeners() {
        ComponentRegistry.registerListener((type, instance) -> "changed");
        ComponentRegistry.clear();

        assertEquals("value", ComponentRegistry.processRegisteredComponent(String.class, "value"));
    }
}
