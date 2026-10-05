package com.github.thedumbledodo.blueprint.service;

import com.github.thedumbledodo.blueprint.fixture.*;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicesTest {

    @AfterEach
    void reset() {
        Services.clear();
        ComponentRegistry.clear();
    }

    @Test
    void registerByInstanceUsesTheRuntimeClass() {
        final Database database = new Database();

        Services.register(database);

        assertSame(database, Services.getService(Database.class));
    }

    @Test
    void registerByTypeAllowsLookupThroughAnInterface() {
        final Repository repository = new Repository() {
        };

        Services.register(Repository.class, repository);

        assertSame(repository, Services.getService(Repository.class));
        assertTrue(Services.isPresent(Repository.class));
    }

    @Test
    void missingServiceIsNull() {
        assertNull(Services.getService(Database.class));
        assertFalse(Services.isPresent(Database.class));
    }

    @Test
    void loadIfPresentReturnsTheRegisteredInstance() {
        final Database database = new Database();

        Services.register(Database.class, database);

        assertSame(database, Services.loadIfPresent(Database.class));
    }

    @Test
    void loadIfPresentCreatesClassesWithoutTheAnnotation() {
        final PlainHelper helper = Services.loadIfPresent(PlainHelper.class);

        assertNotNull(helper.database());
        assertSame(helper, Services.getService(PlainHelper.class));
    }

    @Test
    void loadIfPresentWrapsFailuresWithTheClassName() {
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> Services.loadIfPresent(FailingComponent.class));

        assertTrue(exception.getMessage().contains(FailingComponent.class.getName()));
    }

    @Test
    void nullInstancesAreRejectedWithTheType() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Services.register(Database.class, null));

        assertTrue(exception.getMessage().contains(Database.class.getName()));
    }

    @Test
    void clearRemovesEveryService() {
        Services.register(new Database());
        Services.clear();

        assertTrue(Services.getRegisteredServices().isEmpty());
    }
}
