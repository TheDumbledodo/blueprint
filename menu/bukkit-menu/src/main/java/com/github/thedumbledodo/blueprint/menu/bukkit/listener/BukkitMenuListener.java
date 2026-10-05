package com.github.thedumbledodo.blueprint.menu.bukkit.listener;

import com.github.thedumbledodo.blueprint.menu.bukkit.BukkitExecuteComponent;
import com.github.thedumbledodo.blueprint.menu.bukkit.BukkitMenu;
import com.github.thedumbledodo.blueprint.menu.model.ButtonType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public final class BukkitMenuListener implements Listener {

    private final Plugin plugin;

    public BukkitMenuListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        final Inventory top = event.getView().getTopInventory();

        if (!(top.getHolder(false) instanceof BukkitMenu menu)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        final int rawSlot = event.getRawSlot();
        final InventoryAction action = event.getAction();
        final boolean topClick = rawSlot >= 0 && rawSlot < top.getSize();

        if (!topClick) {
            if (event.isShiftClick() || action == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
            }
            return;
        }

        if (!menu.isInteractive(rawSlot) || action == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }

        final ItemStack item = menu.getItem(rawSlot).orElseGet(() -> top.getItem(rawSlot));
        final ButtonType buttonType = toButtonType(event.getClick());

        menu.handleClick(new BukkitExecuteComponent(player.getUniqueId(), player, buttonType, rawSlot, item, menu, event));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        final Inventory top = event.getView().getTopInventory();

        if (!(top.getHolder(false) instanceof BukkitMenu menu)) {
            return;
        }

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= top.getSize() || menu.isInteractive(rawSlot)) {
                continue;
            }

            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof BukkitMenu menu)) {
            return;
        }
        menu.handleOpen(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof BukkitMenu menu)) {
            return;
        }
        menu.handleClose(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() != plugin) {
            return;
        }

        for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
            if (BukkitMenu.getOpenMenu(player).isEmpty()) {
                continue;
            }
            player.closeInventory();
        }
    }

    public static ButtonType toButtonType(ClickType clickType) {

        return switch (clickType) {
            case LEFT -> ButtonType.LEFT;
            case SHIFT_LEFT -> ButtonType.SHIFT_LEFT;
            case RIGHT -> ButtonType.RIGHT;
            case SHIFT_RIGHT -> ButtonType.SHIFT_RIGHT;
            case MIDDLE -> ButtonType.MIDDLE;
            case NUMBER_KEY -> ButtonType.NUMBER_KEY;
            case DOUBLE_CLICK -> ButtonType.DOUBLE_CLICK;
            case SWAP_OFFHAND -> ButtonType.SWAP_OFFHAND;
            case DROP -> ButtonType.DROP;
            case CONTROL_DROP -> ButtonType.CONTROL_DROP;

            default -> ButtonType.UNKNOWN;
        };
    }
}
