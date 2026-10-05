package com.github.thedumbledodo.blueprint;

import com.github.thedumbledodo.blueprint.command.PaperCommandManager;
import com.github.thedumbledodo.blueprint.config.ConfigProperties;
import com.github.thedumbledodo.blueprint.config.ConfigurationSerializableSerializer;
import com.github.thedumbledodo.blueprint.config.KeyedSerializer;
import com.github.thedumbledodo.blueprint.config.json.JsonFormat;
import com.github.thedumbledodo.blueprint.config.yaml.YamlFormat;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.loader.BlueprintLoader;
import com.github.thedumbledodo.blueprint.service.Services;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffectType;

public class Blueprint {

    public static void register(Plugin plugin, Object... objects) {
        registerServices(plugin, objects);

        final PaperCommandManager commandManager = new PaperCommandManager(plugin);

        Services.register(PaperCommandManager.class, commandManager);

        registerListeners(plugin);
        createConfiguration(plugin).install();
        commandManager.install();

        BlueprintLoader.loadComponents(plugin.getClass());
        commandManager.flush();
    }

    public static void unregister() {
        ComponentRegistry.clear();
        Services.clear();
    }

    static void registerServices(Plugin plugin, Object... objects) {
        Services.register(plugin);

        Services.register(Plugin.class, plugin);
        Services.register(BlueprintModule.class, plugin);

        for (Object object : objects) {
            Services.register(object);
        }
    }

    static void registerListeners(Plugin plugin) {
        final PluginManager pluginManager = Bukkit.getPluginManager();

        ComponentRegistry.registerListener((type, instance) -> {

            if (instance instanceof Listener listener) {
                pluginManager.registerEvents(listener, plugin);
            }
            return instance;
        });
    }

    static BlueprintConfiguration createConfiguration(Plugin plugin) {
        final ConfigProperties properties = ConfigProperties.builder()
                .serializer(ConfigurationSerializable.class, new ConfigurationSerializableSerializer())
                .serializer(Enchantment.class, new KeyedSerializer<>(() -> Registry.ENCHANTMENT))
                .serializer(PotionEffectType.class, new KeyedSerializer<>(() -> Registry.EFFECT))
                .serializer(Sound.class, new KeyedSerializer<>(() -> Registry.SOUNDS))
                .build();

        return BlueprintConfiguration.builder()
                .configDirectory(plugin.getDataFolder().toPath())
                .properties(properties)
                .format(new YamlFormat())
                .format(new JsonFormat())
                .build();
    }
}
