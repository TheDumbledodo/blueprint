package io.github.dumbledodo.blueprint;

import co.aikar.commands.PaperCommandManager;
import com.github.dumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.dumbledodo.blueprint.service.Services;
import de.exlll.configlib.YamlConfigurationProperties;
import io.github.dumbledodo.blueprint.config.BukkitConfigurationSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Blueprint {

    public static void register(Plugin plugin, Object... objects) {
        Services.register(plugin);
        Services.register(Plugin.class, plugin);
        Services.register(objects);

        Services.register(new PaperCommandManager(plugin));

        final PluginManager pluginManager = Bukkit.getPluginManager();

        ComponentRegistry.registerListener((type, instance) -> {

            if (instance instanceof Listener listener) {
                pluginManager.registerEvents(listener, plugin);
            }
            return instance;
        });

        final BukkitConfigurationSerializable configurationSerializable = new BukkitConfigurationSerializable();
        final YamlConfigurationProperties properties = YamlConfigurationProperties
                .newBuilder()
                .addSerializer(Vector.class, configurationSerializable)
                .addSerializer(BlockVector.class, configurationSerializable)
                .addSerializer(ItemStack.class, configurationSerializable)
                .addSerializer(Color.class, configurationSerializable)
                .addSerializer(PotionEffect.class, configurationSerializable)
                .addSerializer(FireworkEffect.class, configurationSerializable)
                .addSerializer(Pattern.class, configurationSerializable)
                .addSerializer(Location.class, configurationSerializable)
                .addSerializer(AttributeModifier.class, configurationSerializable)
                .addSerializer(BoundingBox.class, configurationSerializable)
                .addSerializer(SpawnRule.class, configurationSerializable)
                .build();

        BlueprintConfiguration.builder()
                .configDirectory(plugin.getDataFolder().toPath())
                .properties(properties)
                .build()
                .register(plugin);
    }
}
