package com.github.thedumbledodo.blueprint.menu.packet.cache;

import com.github.retrooper.packetevents.protocol.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerInventoryCache {

    public static final int WINDOW_SIZE = 46;
    public static final int MAIN_INVENTORY_START = 9;

    private final Map<UUID, ItemStack[]> inventories = new ConcurrentHashMap<>();

    public void setContents(UUID uuid, List<ItemStack> items) {
        final ItemStack[] contents = new ItemStack[WINDOW_SIZE];

        for (int i = 0; i < WINDOW_SIZE; i++) {
            contents[i] = i < items.size() && items.get(i) != null ? items.get(i) : ItemStack.EMPTY;
        }
        inventories.put(uuid, contents);
    }

    public void setSlot(UUID uuid, int slot, ItemStack item) {
        if (slot < 0 || slot >= WINDOW_SIZE) {
            return;
        }

        final ItemStack[] contents = inventories.get(uuid);

        if (contents == null) {
            return;
        }
        contents[slot] = item == null ? ItemStack.EMPTY : item;
    }

    public boolean has(UUID uuid) {
        return inventories.containsKey(uuid);
    }

    public Optional<List<ItemStack>> getContents(UUID uuid) {
        final ItemStack[] contents = inventories.get(uuid);

        return contents == null ? Optional.empty() : Optional.of(List.of(contents));
    }

    public ItemStack getMenuSlot(UUID uuid, int index) {
        final ItemStack[] contents = inventories.get(uuid);

        if (contents == null || index < 0 || index >= 36) {
            return ItemStack.EMPTY;
        }
        return contents[MAIN_INVENTORY_START + index];
    }

    public void remove(UUID uuid) {
        inventories.remove(uuid);
    }
}
