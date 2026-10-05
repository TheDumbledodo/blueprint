package com.github.thedumbledodo.blueprint.loader;

import com.github.thedumbledodo.blueprint.fixture.*;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.service.Services;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintLoaderTest {

    @AfterEach
    void reset() {
        Services.clear();
        ComponentRegistry.clear();
    }

    @Test
    void registeredServiceIsInjectedInsteadOfCreated() throws Exception {
        final Database database = new Database();

        Services.register(Database.class, database);

        final UserService userService = BlueprintLoader.createComponentInstance(UserService.class);

        assertSame(database, userService.database());
    }

    @Test
    void sharedDependencyIsCreatedOnce() throws Exception {
        final ShopService shopService = BlueprintLoader.createComponentInstance(ShopService.class);

        assertSame(shopService.database(), shopService.userService().database());
        assertSame(shopService.database(), Services.getService(Database.class));
    }

    @Test
    void createdComponentIsRegisteredAndReused() throws Exception {
        final UserService first = BlueprintLoader.createComponentInstance(UserService.class);
        final UserService second = BlueprintLoader.createComponentInstance(UserService.class);

        assertSame(first, second);
        assertSame(first, Services.getService(UserService.class));
    }

    @Test
    void missingDependencyNamesTheTypeAndTheComponent() {
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> BlueprintLoader.createComponentInstance(NeedsApiClient.class));

        assertTrue(exception.getMessage().contains(ApiClient.class.getName()));
        assertTrue(exception.getMessage().contains(NeedsApiClient.class.getName()));
    }

    @Test
    void circularDependencyIsReportedInsteadOfOverflowing() {
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> BlueprintLoader.createComponentInstance(CycleA.class));

        assertEquals("Circular dependency: CycleA -> CycleB -> CycleA", exception.getMessage());
        assertFalse(Services.isPresent(CycleA.class));
    }

    @Test
    void failedCycleDoesNotPoisonLaterLoads() throws Exception {
        assertThrows(IllegalStateException.class, () -> BlueprintLoader.createComponentInstance(CycleA.class));

        assertNotNull(BlueprintLoader.createComponentInstance(UserService.class));
    }

    @Test
    void constructorExceptionIsThrownUnwrapped() {
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> BlueprintLoader.createComponentInstance(FailingComponent.class));

        assertEquals("database offline", exception.getMessage());
    }

    @Test
    void listenerCanReplaceTheRegisteredInstance() throws Exception {
        final Database replacement = new Database();

        ComponentRegistry.registerListener((type, instance) -> type == Database.class ? replacement : instance);

        final UserService userService = BlueprintLoader.createComponentInstance(UserService.class);

        assertSame(replacement, userService.database());
        assertSame(replacement, Services.getService(Database.class));
    }

    @Test
    void interfaceIsRejected() {
        assertThrows(IllegalStateException.class, () -> BlueprintLoader.createComponentInstance(Repository.class));
    }

    @Test
    void loadComponentsKeepsGoingAfterAFailure() {
        BlueprintLoader.loadComponents(Database.class);

        assertTrue(Services.isPresent(ShopService.class));
        assertTrue(Services.isPresent(UserService.class));
        assertFalse(Services.isPresent(FailingComponent.class));
        assertFalse(Services.isPresent(CycleA.class));
    }
}
