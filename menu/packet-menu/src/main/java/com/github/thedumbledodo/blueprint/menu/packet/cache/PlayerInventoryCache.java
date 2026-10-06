package com.github.thedumbledodo.blueprint.menu.packet.cache;

import com.github.retrooper.packetevents.protocol.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerInventoryCache {

    public static final int WINDOW_SIZE = 46;
    public static final int MAIN_INVENTORY_START = 9;
    public static final int MAIN_INVENTORY_SLOTS = 36;

    private final Map<UUID, ItemStack[]> inventories = new ConcurrentHashMap<>();
    private final Map<UUID, Window> windows = new ConcurrentHashMap<>();

    public void setContents(UUID uuid, List<ItemStack> items) {
        final ItemStack[] contents = new ItemStack[WINDOW_SIZE];

        for (int i = 0; i < WINDOW_SIZE; i++) {
            contents[i] = i < items.size() && items.get(i) != null ? items.get(i) : ItemStack.EMPTY;
        }
        inventories.put(uuid, contents);
    }

    public void setWindowContents(UUID uuid, int windowId, List<ItemStack> items) {
        if (windowId == 0) {
            setContents(uuid, items);
            return;
        }

        final int size = items.size() - MAIN_INVENTORY_SLOTS;

        if (size < 0) {
            return;
        }
        windows.put(uuid, new Window(windowId, size));

        for (int i = 0; i < MAIN_INVENTORY_SLOTS; i++) {
            setSlot(uuid, MAIN_INVENTORY_START + i, items.get(size + i));
        }
    }

    public void setWindowSlot(UUID uuid, int windowId, int slot, ItemStack item) {
        if (windowId == 0) {
            setSlot(uuid, slot, item);
            return;
        }

        final Window window = windows.get(uuid);

        if (window == null || window.id() != windowId || slot < window.size() || slot >= window.size() + MAIN_INVENTORY_SLOTS) {
            return;
        }
        setSlot(uuid, MAIN_INVENTORY_START + slot - window.size(), item);
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

        if (contents == null || index < 0 || index >= MAIN_INVENTORY_SLOTS) {
            return ItemStack.EMPTY;
        }
        return contents[MAIN_INVENTORY_START + index];
    }

    public static int toWindowSlot(int inventoryIndex) {
        if (inventoryIndex >= 0 && inventoryIndex < 9) {
            return 36 + inventoryIndex;
        }

        if (inventoryIndex >= 9 && inventoryIndex < 36) {
            return inventoryIndex;
        }

        if (inventoryIndex >= 36 && inventoryIndex < 40) {
            return 44 - inventoryIndex;
        }
        return inventoryIndex == 40 ? 45 : -1;
    }

    public void remove(UUID uuid) {
        inventories.remove(uuid);
        windows.remove(uuid);
    }

    private record Window(int id, int size) {
    }
}
