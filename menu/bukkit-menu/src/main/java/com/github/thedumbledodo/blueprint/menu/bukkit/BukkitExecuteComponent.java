package com.github.thedumbledodo.blueprint.menu.bukkit;

import com.github.thedumbledodo.blueprint.menu.component.ExecuteComponent;
import com.github.thedumbledodo.blueprint.menu.model.ButtonType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;

public final class BukkitExecuteComponent extends ExecuteComponent {

    private final Player player;
    private final ItemStack item;
    private final BukkitMenu menu;
    private final InventoryClickEvent event;

    public BukkitExecuteComponent(UUID uuid, Player player, ButtonType buttonType, int slot, ItemStack item, BukkitMenu menu, InventoryClickEvent event) {
        super(uuid, buttonType, slot);

        this.player = Objects.requireNonNull(player, "player");
        this.item = item;
        this.menu = Objects.requireNonNull(menu, "menu");
        this.event = event;
    }

    public Player player() {
        return player;
    }

    public ItemStack item() {
        return item;
    }

    public BukkitMenu menu() {
        return menu;
    }

    public InventoryClickEvent event() {
        return event;
    }
}
