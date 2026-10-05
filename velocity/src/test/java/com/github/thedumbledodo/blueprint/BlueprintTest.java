package com.github.thedumbledodo.blueprint;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.service.Services;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlueprintTest {

    private final Object plugin = new Object();

    private ProxyServer server;
    private EventManager eventManager;
    private Logger logger;

    @BeforeEach
    void setUp() {
        this.server = mock(ProxyServer.class);
        this.eventManager = mock(EventManager.class);
        this.logger = mock(Logger.class);

        when(server.getEventManager()).thenReturn(eventManager);
    }

    @AfterEach
    void tearDown() {
        Blueprint.unregister();
    }

    @Test
    void platformObjectsAndExtrasAreRegistered() {
        final ApiClient client = new ApiClient();

        Blueprint.registerServices(plugin, server, logger, client);

        assertSame(server, Services.getService(ProxyServer.class));
        assertSame(logger, Services.getService(Logger.class));
        assertSame(plugin, Services.getService(BlueprintModule.class));
        assertSame(client, Services.getService(ApiClient.class));
    }

    @Test
    void componentsWithSubscribeMethodsAreRegisteredForEvents() {
        final JoinListener listener = new JoinListener();

        Blueprint.registerListeners(plugin, server);
        ComponentRegistry.processRegisteredComponent(JoinListener.class, listener);
        ComponentRegistry.processRegisteredComponent(ApiClient.class, new ApiClient());

        verify(eventManager).register(plugin, listener);
        verifyNoMoreInteractions(eventManager);
    }

    public static final class ApiClient {
    }

    public static final class JoinListener {

        @Subscribe
        public void onLogin(PostLoginEvent event) {
        }
    }
}
