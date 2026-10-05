package com.github.thedumbledodo.blueprint.menu.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.thedumbledodo.blueprint.menu.packet.listener.MenuListener;
import com.github.thedumbledodo.blueprint.menu.packet.sender.PacketSender;
import com.github.thedumbledodo.blueprint.menu.packet.service.MenuService;
import com.github.thedumbledodo.blueprint.menu.scheduler.MenuScheduler;
import com.github.thedumbledodo.blueprint.service.Services;
import lombok.NoArgsConstructor;

import java.util.concurrent.*;

@NoArgsConstructor
public final class BlueprintPacketMenu {

    private static ScheduledExecutorService refreshExecutor;

    public static MenuService init() {
        PacketEventsAPI<?> packetEventsAPI = Services.getService(PacketEventsAPI.class);

        if (packetEventsAPI == null) {
            packetEventsAPI = PacketEvents.getAPI();
        }
        return init(packetEventsAPI);
    }

    public static MenuService init(PacketEventsAPI<?> packetEventsAPI) {
        return init(packetEventsAPI, Runnable::run);
    }

    public static MenuService init(PacketEventsAPI<?> packetEventsAPI, Executor executor) {
        if (packetEventsAPI == null) {
            throw new IllegalStateException("PacketEvents API is not available. Initialize PacketEvents before initializing Blueprint menus.");
        }
        Services.register(PacketEventsAPI.class, packetEventsAPI);

        MenuService menuService = Services.getService(MenuService.class);

        if (menuService == null) {
            menuService = new MenuService(PacketSender.SILENT, executor, createScheduler(executor));

            Services.register(MenuService.class, menuService);
        }

        if (!Services.isPresent(MenuListener.class)) {
            final MenuListener listener = new MenuListener(menuService);

            packetEventsAPI.getEventManager().registerListener(listener, PacketListenerPriority.NORMAL);
            Services.register(MenuListener.class, listener);
        }
        return menuService;
    }

    private static MenuScheduler createScheduler(Executor executor) {
        return (task, interval) -> {
            final long millis = interval.toMillis();
            final ScheduledFuture<?> future = getRefreshExecutor().scheduleAtFixedRate(() -> executor.execute(task), millis, millis, TimeUnit.MILLISECONDS);

            return () -> future.cancel(false);
        };
    }

    private static synchronized ScheduledExecutorService getRefreshExecutor() {
        if (refreshExecutor == null) {
            refreshExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
                final Thread thread = new Thread(runnable, "blueprint-menu-refresh");

                thread.setDaemon(true);
                return thread;
            });
        }
        return refreshExecutor;
    }
}
