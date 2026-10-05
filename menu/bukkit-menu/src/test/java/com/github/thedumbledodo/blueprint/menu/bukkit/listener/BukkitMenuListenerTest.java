package com.github.thedumbledodo.blueprint.menu.bukkit.listener;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation;
import com.github.thedumbledodo.blueprint.menu.bukkit.BlueprintBukkitMenu;
import com.github.thedumbledodo.blueprint.menu.bukkit.BukkitMenu;
import com.github.thedumbledodo.blueprint.menu.model.ButtonType;
import com.github.thedumbledodo.blueprint.service.Services;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BukkitMenuListenerTest {

    private ServerMock server;
    private PluginMock plugin;
    private PlayerMock player;
    private PlayerSimulation simulation;

    @BeforeEach
    void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.createMockPlugin();
        this.player = server.addPlayer();
        this.simulation = new PlayerSimulation(player);

        BlueprintBukkitMenu.init(plugin);
    }

    @AfterEach
    void tearDown() {
        try {
            if (player != null) {
                showPlayerInventory();
            }

        } finally {
            MockBukkit.unmock();
            Services.clear();
        }
    }

    // MockBukkit leaves a closed view with a null top inventory, which a real server never does.
    // Disabling the plugin looks at every player's open view, so give them a real one first.
    private void showPlayerInventory() {
        player.openInventory(player.getInventory());
    }

    private BukkitMenu openMenu() {
        final BukkitMenu menu = new BukkitMenu(1, Component.text("Test"));

        menu.open(player);
        return menu;
    }

    @Test
    void clickInTheMenuIsCancelledAndRunsTheButtonOnce() {
        final BukkitMenu menu = new BukkitMenu(1, Component.text("Test"));
        final AtomicInteger clicks = new AtomicInteger();

        menu.setItem(2, new ItemStack(Material.DIAMOND));
        menu.onClick(2, click -> clicks.incrementAndGet());
        menu.open(player);

        final InventoryClickEvent event = simulation.simulateInventoryClick(player.getOpenInventory(), ClickType.LEFT, 2);

        assertTrue(event.isCancelled());
        assertEquals(1, clicks.get());
    }

    @Test
    void clickComponentCarriesTheClickDetails() {
        final BukkitMenu menu = new BukkitMenu(1, Component.text("Test"));
        final List<ButtonType> types = new ArrayList<>();

        menu.setItem(4, new ItemStack(Material.EMERALD));
        menu.onClick(4, click -> {
            types.add(click.buttonType());

            assertSame(player, click.player());
            assertEquals(Material.EMERALD, click.item().getType());
            assertSame(menu, click.menu());
        });
        menu.open(player);

        simulation.simulateInventoryClick(player.getOpenInventory(), ClickType.SHIFT_RIGHT, 4);

        assertEquals(List.of(ButtonType.SHIFT_RIGHT), types);
    }

    @Test
    void playerInventoryClicksAreAllowedButShiftClicksAreNot() {
        openMenu();

        final InventoryClickEvent normal = simulation.simulateInventoryClick(player.getOpenInventory(), ClickType.LEFT, 15);
        final InventoryClickEvent shift = simulation.simulateInventoryClick(player.getOpenInventory(), ClickType.SHIFT_LEFT, 15);

        assertFalse(normal.isCancelled());
        assertTrue(shift.isCancelled());
    }

    @Test
    void interactiveSlotsAreNotCancelled() {
        final BukkitMenu menu = new BukkitMenu(1, Component.text("Deposit"));

        menu.setInteractive(0, true);
        menu.open(player);

        assertFalse(simulation.simulateInventoryClick(player.getOpenInventory(), ClickType.LEFT, 0).isCancelled());
        assertTrue(simulation.simulateInventoryClick(player.getOpenInventory(), ClickType.LEFT, 1).isCancelled());
    }

    @Test
    void dragIntoTheMenuIsCancelled() {
        openMenu();

        final InventoryDragEvent event = new InventoryDragEvent(player.getOpenInventory(), null, new ItemStack(Material.STONE), false,
                Map.of(3, new ItemStack(Material.STONE)));

        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void openAndCloseHooksRun() {
        final BukkitMenu menu = new BukkitMenu(1, Component.text("Test"));
        final List<String> calls = new ArrayList<>();

        menu.onOpen(uuid -> calls.add("open"));
        menu.onClose(uuid -> calls.add("close"));
        menu.open(player);
        player.closeInventory();

        assertEquals(List.of("open", "close"), calls);
        assertFalse(menu.isOpen());
    }

    @Test
    void setItemUpdatesTheOpenInventory() {
        final BukkitMenu menu = openMenu();

        menu.setItem(3, new ItemStack(Material.GOLD_INGOT));

        assertEquals(Material.GOLD_INGOT, Objects.requireNonNull(menu.getInventory().getItem(3)).getType());
    }

    @Test
    void disablingThePluginClosesOpenMenus() {
        final BukkitMenu menu = openMenu();

        server.getPluginManager().disablePlugin(plugin);

        assertNotSame(menu.getInventory(), player.getOpenInventory().getTopInventory());
    }

    @Test
    void normalInventoriesAreLeftAlone() {
        final Inventory chest = server.createInventory(null, 9);

        player.openInventory(chest);

        assertFalse(simulation.simulateInventoryClick(player.getOpenInventory(), ClickType.LEFT, 0).isCancelled());
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT, LEFT",
            "SHIFT_RIGHT, SHIFT_RIGHT",
            "NUMBER_KEY, NUMBER_KEY",
            "SWAP_OFFHAND, SWAP_OFFHAND",
            "CREATIVE, UNKNOWN",
            "WINDOW_BORDER_LEFT, UNKNOWN"
    })
    void clickTypesAreMapped(ClickType clickType, ButtonType expected) {
        assertEquals(expected, BukkitMenuListener.toButtonType(clickType));
    }
}
