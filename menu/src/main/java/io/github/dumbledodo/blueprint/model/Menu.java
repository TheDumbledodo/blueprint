package io.github.dumbledodo.blueprint.model;

import com.github.dumbledodo.blueprint.service.Services;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.dumbledodo.blueprint.service.MenuService;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter @Setter
public abstract class Menu {

    private final int id;
    private final Component name;

    private final MenuType type;
    private final Integer size;

    private Map<Integer, Button> buttons;

    private volatile WrapperPlayServerOpenWindow menuPacket;
    private volatile WrapperPlayServerWindowItems contentPacket;

    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

    private GuiAction closeAction;

    private int lastEmptySlot;

    private Menu(MenuType type, Integer size, Component name) {
        this.id = Services.getService(MenuService.class).generateWindowId();
        this.name = name;

        this.type = type;
        this.size = (size == null ? type.getSize() : size) + 36;

        this.buttons = new ConcurrentHashMap<>(this.size);
        this.menuPacket = new WrapperPlayServerOpenWindow(id, type.getId(), name);

        final ArrayList<ItemStack> items = new ArrayList<>(this.size);

        for (int i = 0; i < this.size; i++) {
            items.add(null);
        }
        this.contentPacket = new WrapperPlayServerWindowItems(id, 0, items, null);
        this.lastEmptySlot = computeLastEmptySlot();
    }

    public Menu(MenuType type, Component name) {
        this(type, null, name);
    }

    public Menu(int rows, Component name) {
        this(MenuType.of(rows), rows * 9, name);
    }

    public Set<User> getViewers() {
        final ProtocolManager protocolManager = PacketEvents.getAPI().getProtocolManager();

        return viewers.stream()
                .map(protocolManager::getChannel)
                .map(protocolManager::getUser)
                .collect(Collectors.toSet());
    }

    public List<ItemStack> getItems() {
        return contentPacket.getItems();
    }

    public ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    private int computeLastEmptySlot() {
        final List<ItemStack> items = getItems();

        for (int i = items.size() - 1; i >= 0; i--) {
            final ItemStack itemStack = items.get(i);

            if (itemStack != null) {
                continue;
            }
            return i;
        }
        return -1;
    }

    public void setItem(int slot, ItemStack item) {
        getItems().set(slot, item);
        setLastEmptySlot(computeLastEmptySlot());
    }

    public void setButton(int slot, Button button) {
        buttons.put(slot, button);
    }

    public void setItems(List<ItemStack> items) {
        contentPacket.setItems(items);

        setLastEmptySlot(computeLastEmptySlot());
    }

    public void addViewer(UUID uuid) {
        viewers.add(uuid);
    }

    public void removeViewer(UUID uuid) {
        viewers.remove(uuid);
    }

    public Optional<GuiAction> getCloseAction() {
        return Optional.ofNullable(closeAction);
    }
}