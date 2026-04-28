package io.github.dumbledodo.blueprint;

import com.github.dumbledodo.blueprint.service.Services;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import io.github.dumbledodo.blueprint.listener.MenuListener;
import io.github.dumbledodo.blueprint.service.MenuService;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class BlueprintMenu {

    public static MenuService init() {
        PacketEventsAPI<?> packetEventsAPI = Services.getService(PacketEventsAPI.class);

        if (packetEventsAPI == null) {
            packetEventsAPI = PacketEvents.getAPI();
        }
        return init(packetEventsAPI);
    }

    public static MenuService init(PacketEventsAPI<?> packetEventsAPI) {
        if (packetEventsAPI == null) {
            throw new IllegalStateException("PacketEvents API is not available. Initialize PacketEvents before initializing Blueprint menu.");
        }
        Services.register(PacketEventsAPI.class, packetEventsAPI);

        MenuService menuService = Services.getService(MenuService.class);

        if (menuService == null) {
            menuService = new MenuService();

            Services.register(MenuService.class, menuService);
        }

        if (!Services.isPresent(MenuListener.class)) {
            Services.register(MenuListener.class, new MenuListener(menuService, packetEventsAPI));
        }
        return menuService;
    }
}
