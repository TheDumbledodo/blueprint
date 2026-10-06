package com.github.thedumbledodo.blueprint.menu.bukkit;

import com.github.thedumbledodo.blueprint.chat.Text;
import com.github.thedumbledodo.blueprint.menu.builder.MenuBuilder;
import com.github.thedumbledodo.blueprint.menu.model.AbstractMenu;
import com.github.thedumbledodo.blueprint.menu.model.MenuType;
import com.github.thedumbledodo.blueprint.menu.scheduler.MenuScheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitMenu extends AbstractMenu<Player, ItemStack, BukkitExecuteComponent> implements InventoryHolder {

    private final Set<Integer> interactiveSlots = ConcurrentHashMap.newKeySet();
    private Inventory inventory;

    public BukkitMenu(MenuType type, Component title) {
        super(type, title);
    }

    public BukkitMenu(int rows, Component title) {
        super(rows, title);
    }

    public static MenuBuilder<BukkitMenu, ItemStack, BukkitExecuteComponent> builder() {
        return new MenuBuilder<>(BukkitMenu::new);
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            this.inventory = createInventory();
            fillInventory();
        }
        return inventory;
    }

    @Override
    public void open(Player player) {
        prepareOpen();
        player.openInventory(getInventory());
    }

    @Override
    public void close(Player player) {
        if (inventory == null || player.getOpenInventory().getTopInventory() != inventory) {
            return;
        }
        player.closeInventory();
    }

    @Override
    protected void render() {
        if (inventory == null) {
            return;
        }
        fillInventory();
    }

    @Override
    protected void render(int slot) {
        if (inventory == null || (isInteractive(slot) && getItem(slot).isEmpty())) {
            return;
        }
        inventory.setItem(slot, getItem(slot).orElse(null));
    }

    @Override
    protected void updateTitle() {
        if (inventory == null) {
            return;
        }

        final String title = Text.translateToLegacyString(getTitle());

        for (HumanEntity viewer : new ArrayList<>(inventory.getViewers())) {
            final InventoryView view = viewer.getOpenInventory();

            if (view.getTopInventory() != inventory) {
                continue;
            }
            view.setTitle(title);
        }
    }

    @Override
    protected MenuScheduler getScheduler() {
        return BlueprintBukkitMenu.getScheduler();
    }

    public List<Player> getViewers() {
        if (inventory == null) {
            return List.of();
        }

        final List<Player> players = new ArrayList<>();

        for (HumanEntity viewer : inventory.getViewers()) {
            if (viewer instanceof Player player) {
                players.add(player);
            }
        }
        return players;
    }

    public void setInteractive(int slot, boolean interactive) {
        checkSlot(slot);

        if (interactive) {
            interactiveSlots.add(slot);
            return;
        }
        interactiveSlots.remove(slot);
    }

    public boolean isInteractive(int slot) {
        return interactiveSlots.contains(slot);
    }

    private Inventory createInventory() {
        final InventoryType inventoryType = toInventoryType(getType());

        if (inventoryType == InventoryType.CHEST) {
            return Bukkit.createInventory(this, getSize(), getTitle());
        }
        return Bukkit.createInventory(this, inventoryType, getTitle());
    }

    private void fillInventory() {
        final ItemStack[] contents = new ItemStack[inventory.getSize()];

        for (int slot = 0; slot < contents.length && slot < getSize(); slot++) {
            if (isInteractive(slot) && getItem(slot).isEmpty()) {
                contents[slot] = inventory.getItem(slot);
                continue;
            }
            contents[slot] = getItem(slot).orElse(null);
        }
        inventory.setContents(contents);
    }

    public static Optional<BukkitMenu> getOpenMenu(HumanEntity player) {
        final Inventory top = player.getOpenInventory().getTopInventory();

        if (!(top.getHolder(false) instanceof BukkitMenu menu)) {
            return Optional.empty();
        }
        return Optional.of(menu);
    }

    static InventoryType toInventoryType(MenuType type) {
        return switch (type) {
            case GENERIC_9X1, GENERIC_9X2, GENERIC_9X3, GENERIC_9X4, GENERIC_9X5, GENERIC_9X6 -> InventoryType.CHEST;
            case GENERIC_3X3 -> InventoryType.DISPENSER;
            case CRAFTER_3X3 -> InventoryType.CRAFTER;
            case ANVIL -> InventoryType.ANVIL;
            case BEACON -> InventoryType.BEACON;
            case BLAST_FURNACE -> InventoryType.BLAST_FURNACE;
            case BREWING_STAND -> InventoryType.BREWING;
            case CRAFTING_TABLE -> InventoryType.WORKBENCH;
            case ENCHANTMENT_TABLE -> InventoryType.ENCHANTING;
            case FURNACE -> InventoryType.FURNACE;
            case GRINDSTONE -> InventoryType.GRINDSTONE;
            case HOPPER -> InventoryType.HOPPER;
            case LECTERN -> InventoryType.LECTERN;
            case LOOM -> InventoryType.LOOM;
            case VILLAGER -> InventoryType.MERCHANT;
            case SHULKER_BOX -> InventoryType.SHULKER_BOX;
            case SMITHING_TABLE -> InventoryType.SMITHING;
            case SMOKER -> InventoryType.SMOKER;
            case CARTOGRAPHY_TABLE -> InventoryType.CARTOGRAPHY;
            case STONECUTTER -> InventoryType.STONECUTTER;
        };
    }
}
