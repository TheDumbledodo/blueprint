package com.github.thedumbledodo.blueprint.menu.bukkit;

import com.github.thedumbledodo.blueprint.menu.bukkit.listener.BukkitMenuListener;
import com.github.thedumbledodo.blueprint.menu.scheduler.MenuScheduler;
import com.github.thedumbledodo.blueprint.service.Services;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

@NoArgsConstructor
public final class BlueprintBukkitMenu {

    private static Plugin plugin;

    public static BukkitMenuListener init(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        BlueprintBukkitMenu.plugin = plugin;

        BukkitMenuListener listener = Services.getService(BukkitMenuListener.class);

        if (listener == null) {
            listener = new BukkitMenuListener(plugin);

            Bukkit.getPluginManager().registerEvents(listener, plugin);
            Services.register(BukkitMenuListener.class, listener);
        }
        return listener;
    }

    public static MenuScheduler getScheduler() {
        if (plugin == null) {
            throw new IllegalStateException("Call BlueprintBukkitMenu.init(plugin) before using menu refresh");
        }

        return (task, interval) -> {
            final long ticks = Math.max(1, interval.toMillis() / 50);
            final BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, ticks, ticks);

            return bukkitTask::cancel;
        };
    }
}
