package io.github.dumbledodo.blueprint;

import co.aikar.commands.VelocityCommandManager;
import com.github.dumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.dumbledodo.blueprint.service.Services;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.nio.file.Path;

public class Blueprint {

    public static void register(Object plugin, ProxyServer server, Path dataDirectory, Logger logger) {
        Services.register(plugin);

        Services.register(ProxyServer.class, server);
        Services.register(Logger.class, logger);

        Services.register(new VelocityCommandManager(server, plugin));

        ComponentRegistry.registerListener((type, instance) -> {

            for (Method method : type.getMethods()) {

                if (method.isAnnotationPresent(Subscribe.class)) {
                    server.getEventManager().register(plugin, instance);
                    break;
                }
            }
            return instance;
        });

        BlueprintConfiguration.builder()
                .configDirectory(dataDirectory)
                .build()
                .register(plugin);
    }
}