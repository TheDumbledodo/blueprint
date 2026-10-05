package com.github.thedumbledodo.blueprint;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.ProxyServer;
import com.github.thedumbledodo.blueprint.command.VelocityCommandManager;
import com.github.thedumbledodo.blueprint.config.json.JsonFormat;
import com.github.thedumbledodo.blueprint.config.yaml.YamlFormat;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.loader.BlueprintLoader;
import com.github.thedumbledodo.blueprint.service.Services;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.nio.file.Path;

public class Blueprint {

    public static void register(Object plugin, ProxyServer server, Path dataDirectory, Logger logger, Object... objects) {
        registerServices(plugin, server, logger, objects);

        final VelocityCommandManager commandManager = new VelocityCommandManager(server, plugin, logger);

        Services.register(VelocityCommandManager.class, commandManager);

        registerListeners(plugin, server);
        createConfiguration(dataDirectory).install();
        commandManager.install();

        BlueprintLoader.loadComponents(plugin.getClass());
        commandManager.flush();
    }

    public static void unregister() {
        ComponentRegistry.clear();
        Services.clear();
    }

    static void registerServices(Object plugin, ProxyServer server, Logger logger, Object... objects) {
        Services.register(plugin);
        Services.register(BlueprintModule.class, plugin);

        Services.register(ProxyServer.class, server);
        Services.register(Logger.class, logger);

        for (Object object : objects) {
            Services.register(object);
        }
    }

    static void registerListeners(Object plugin, ProxyServer server) {
        ComponentRegistry.registerListener((type, instance) -> {

            for (Method method : type.getMethods()) {

                if (method.isAnnotationPresent(Subscribe.class)) {
                    server.getEventManager().register(plugin, instance);
                    break;
                }
            }
            return instance;
        });
    }

    static BlueprintConfiguration createConfiguration(Path dataDirectory) {
        return BlueprintConfiguration.builder()
                .configDirectory(dataDirectory)
                .format(new YamlFormat())
                .format(new JsonFormat())
                .build();
    }
}
