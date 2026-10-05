package com.github.thedumbledodo.blueprint.menu.packet;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.thedumbledodo.blueprint.menu.builder.MenuBuilder;
import com.github.thedumbledodo.blueprint.menu.model.AbstractMenu;
import com.github.thedumbledodo.blueprint.menu.model.MenuType;
import com.github.thedumbledodo.blueprint.menu.packet.service.MenuService;
import com.github.thedumbledodo.blueprint.menu.scheduler.MenuScheduler;
import com.github.thedumbledodo.blueprint.service.Services;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PacketMenu extends AbstractMenu<User, ItemStack, PacketExecuteComponent> {

    private final Map<Integer, ItemStack> playerItems = new ConcurrentHashMap<>();

    @Getter @Setter
    private boolean mirrorPlayerInventory;

    public PacketMenu(MenuType type, Component title) {
        super(type, title);
    }

    public PacketMenu(int rows, Component title) {
        super(rows, title);
    }

    public static MenuBuilder<PacketMenu, ItemStack, PacketExecuteComponent> builder() {
        return new MenuBuilder<>(PacketMenu::new);
    }

    @Override
    public void open(User user) {
        Objects.requireNonNull(user, "user");

        prepareOpen();
        getService().openMenu(user, this);
    }

    public void openFor(Object player) {
        final PacketEventsAPI<?> api = Services.getService(PacketEventsAPI.class);

        if (api == null) {
            throw new IllegalStateException("PacketEvents API is not available. Call BlueprintPacketMenu.init(...) first.");
        }
        open(api.getPlayerManager().getUser(player));
    }

    @Override
    public void close(User user) {
        final MenuService service = getService();

        if (service.getMenu(user).orElse(null) != this) {
            return;
        }
        service.closeMenu(user);
    }

    @Override
    public void update() {
        final MenuService service = Services.getService(MenuService.class);

        if (service == null) {
            return;
        }
        service.sendContents(this);
    }

    @Override
    protected void render(int slot) {
        final MenuService service = Services.getService(MenuService.class);

        if (service == null) {
            return;
        }
        service.sendSlot(this, slot);
    }

    @Override
    protected void updateTitle() {
        final MenuService service = Services.getService(MenuService.class);

        if (service == null) {
            return;
        }
        service.sendTitle(this);
    }

    @Override
    protected MenuScheduler getScheduler() {
        return getService().getScheduler();
    }

    public Set<User> getViewers() {
        final MenuService service = Services.getService(MenuService.class);

        return service == null ? Set.of() : service.getViewers(this);
    }

    public Optional<ItemStack> getPlayerItem(int index) {
        return Optional.ofNullable(playerItems.get(index));
    }

    public void setPlayerItem(int index, ItemStack item) {
        if (index < 0 || index >= MenuService.PLAYER_INVENTORY_SLOTS) {
            throw new IllegalArgumentException("player inventory index must be between 0 and 35 but was " + index);
        }

        if (item == null) {
            playerItems.remove(index);

        } else {
            playerItems.put(index, item);
        }

        markChanged(getSize() + index);
    }

    public void clearPlayerItems() {
        playerItems.clear();
        update();
    }

    private MenuService getService() {
        final MenuService service = Services.getService(MenuService.class);

        if (service == null) {
            throw new IllegalStateException("MenuService is not available. Call BlueprintPacketMenu.init(...) first.");
        }
        return service;
    }
}
