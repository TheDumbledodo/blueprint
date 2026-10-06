package com.github.thedumbledodo.blueprint.menu.packet.service;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.github.thedumbledodo.blueprint.menu.model.ButtonType;
import com.github.thedumbledodo.blueprint.menu.packet.PacketExecuteComponent;
import com.github.thedumbledodo.blueprint.menu.packet.PacketMenu;
import com.github.thedumbledodo.blueprint.menu.packet.cache.PlayerInventoryCache;
import com.github.thedumbledodo.blueprint.menu.packet.fixture.FakePacketEventsAPI;
import com.github.thedumbledodo.blueprint.menu.packet.fixture.QueueExecutor;
import com.github.thedumbledodo.blueprint.menu.packet.fixture.RecordingPacketSender;
import com.github.thedumbledodo.blueprint.menu.packet.fixture.TestUsers;
import com.github.thedumbledodo.blueprint.service.Services;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MenuServiceTest {

    private RecordingPacketSender sender;
    private QueueExecutor executor;
    private MenuService service;

    private User steve;
    private User alex;

    @BeforeAll
    static void installPacketEvents() {
        FakePacketEventsAPI.install();
    }

    @BeforeEach
    void setUp() {
        this.sender = new RecordingPacketSender();
        this.executor = new QueueExecutor();
        this.service = new MenuService(sender, executor, null);

        this.steve = TestUsers.user("Steve");
        this.alex = TestUsers.user("Alex");

        Services.register(MenuService.class, service);
    }

    @AfterEach
    void reset() {
        Services.clear();
    }

    private static ItemStack item(int amount) {
        return ItemStack.builder().type(ItemTypes.STONE).amount(amount).build();
    }

    private static WrapperPlayClientClickWindow click(int windowId, int slot, int button, WindowClickType type) {
        return new WrapperPlayClientClickWindow(windowId, Optional.of(0), slot, button, Optional.empty(), type, Optional.empty(), ItemStack.EMPTY);
    }

    private int windowId(User user) {
        return service.getWindowId(user.getUUID()).orElseThrow();
    }

    @Test
    void openSendsTheWindowThenItsContents() {
        final PacketMenu menu = new PacketMenu(3, Component.text("Shop"));

        menu.setItem(4, item(1));
        menu.open(steve);

        final List<Class<?>> types = sender.types(steve);
        final WrapperPlayServerOpenWindow open = sender.packets(steve, WrapperPlayServerOpenWindow.class).getFirst();
        final WrapperPlayServerWindowItems contents = sender.packets(steve, WrapperPlayServerWindowItems.class).getFirst();

        assertEquals(List.of(WrapperPlayServerOpenWindow.class, WrapperPlayServerWindowItems.class), types);
        assertEquals(1, open.getContainerId());
        assertEquals(2, open.getType());
        assertEquals(Component.text("Shop"), open.getTitle());
        assertEquals(27 + 36, contents.getItems().size());
        assertEquals(1, contents.getItems().get(4).getAmount());
        assertTrue(contents.getItems().get(5).isEmpty());
    }

    @Test
    void windowIdsCyclePerUser() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());
        final List<Integer> ids = new ArrayList<>();

        for (int i = 0; i < 101; i++) {
            menu.open(steve);
            ids.add(windowId(steve));
        }
        menu.open(alex);

        assertEquals(1, ids.getFirst());
        assertEquals(100, ids.get(99));
        assertEquals(1, ids.get(100));
        assertEquals(1, windowId(alex));
    }

    @Test
    void viewersAreTrackedOnOpenAndClose() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.open(steve);
        executor.runAll();

        assertEquals(Set.of(steve), menu.getViewers());
        assertTrue(menu.getViewerIds().contains(steve.getUUID()));

        menu.close(steve);
        executor.runAll();

        assertTrue(menu.getViewers().isEmpty());
        assertFalse(menu.isOpen());
    }

    @Test
    void setItemOnAnOpenMenuSendsOneSlotToItsViewersOnly() {
        final PacketMenu shop = new PacketMenu(1, Component.empty());
        final PacketMenu other = new PacketMenu(1, Component.empty());

        shop.open(steve);
        other.open(alex);
        sender.clear();

        shop.setItem(2, item(3));

        final List<WrapperPlayServerSetSlot> slots = sender.packets(steve, WrapperPlayServerSetSlot.class);

        assertEquals(1, slots.size());
        assertEquals(2, slots.getFirst().getSlot());
        assertEquals(windowId(steve), slots.getFirst().getWindowId());
        assertTrue(sender.packets(alex).isEmpty());
    }

    @Test
    void closedMenuSendsNothing() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.setItem(0, item(1));
        menu.update();

        assertTrue(sender.packets(steve).isEmpty());
    }

    @Test
    void plainClickResendsTheSlotAndClearsTheCursor() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.open(steve);
        sender.clear();

        assertTrue(service.handleClick(steve, click(windowId(steve), 3, 0, WindowClickType.PICKUP)));
        assertEquals(List.of(WrapperPlayServerSetSlot.class, WrapperPlayServerSetCursorItem.class), sender.types(steve));
    }

    @Test
    void olderClientsGetTheCursorClearedThroughSetSlot() {
        final User legacy = TestUsers.user("Legacy", ClientVersion.V_1_20_3);
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.open(legacy);
        sender.clear();
        service.handleClick(legacy, click(windowId(legacy), 3, 0, WindowClickType.PICKUP));

        final List<WrapperPlayServerSetSlot> slots = sender.packets(legacy, WrapperPlayServerSetSlot.class);

        assertEquals(2, slots.size());
        assertEquals(-1, slots.get(1).getWindowId());
        assertEquals(-1, slots.get(1).getSlot());
    }

    @Test
    void shiftClickResendsEverything() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.open(steve);
        sender.clear();
        service.handleClick(steve, click(windowId(steve), 3, 0, WindowClickType.QUICK_MOVE));

        assertEquals(List.of(WrapperPlayServerWindowItems.class), sender.types(steve));
    }

    @Test
    void clicksOnOtherWindowsAreNotHandled() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.open(steve);

        assertFalse(service.handleClick(steve, click(windowId(steve) + 1, 3, 0, WindowClickType.PICKUP)));
        assertFalse(service.handleClick(alex, click(1, 3, 0, WindowClickType.PICKUP)));
    }

    @Test
    void clickActionRunsOnTheExecutorWithTheClickedItem() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());
        final List<PacketExecuteComponent> clicks = new ArrayList<>();

        menu.setItem(5, item(7));
        menu.onClick(5, clicks::add);
        menu.open(steve);
        executor.runAll();

        service.handleClick(steve, click(windowId(steve), 5, 1, WindowClickType.PICKUP));

        assertTrue(clicks.isEmpty());

        executor.runAll();

        assertEquals(1, clicks.size());
        assertEquals(ButtonType.RIGHT, clicks.getFirst().type());
        assertEquals(7, clicks.getFirst().item().getAmount());
        assertSame(steve, clicks.getFirst().user());
        assertSame(menu, clicks.getFirst().menu());
    }

    @Test
    void clicksInThePlayerInventoryAreaDoNotRunActions() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.onClick(click -> fail("click outside the menu reached the menu"));
        menu.open(steve);
        executor.runAll();

        assertTrue(service.handleClick(steve, click(windowId(steve), 20, 0, WindowClickType.PICKUP)));
        executor.runAll();
    }

    @Test
    void clientCloseOfOurWindowIsHandledOnce() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());
        final List<String> closes = new ArrayList<>();

        menu.onClose(uuid -> closes.add("closed"));
        menu.open(steve);
        executor.runAll();

        assertFalse(service.handleClientClose(steve, windowId(steve) + 1));
        assertTrue(service.handleClientClose(steve, 1));
        assertFalse(service.handleClientClose(steve, 1));

        executor.runAll();

        assertEquals(List.of("closed"), closes);
        assertTrue(service.getMenu(steve).isEmpty());
    }

    @Test
    void serverWindowReplacesTheFakeMenu() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());
        final List<String> closes = new ArrayList<>();

        menu.onClose(uuid -> closes.add("closed"));
        menu.open(steve);
        executor.runAll();

        service.handleServerWindow(steve);
        service.handleServerWindow(steve);
        executor.runAll();

        assertEquals(List.of("closed"), closes);
        assertFalse(service.handleClick(steve, click(1, 0, 0, WindowClickType.PICKUP)));
    }

    @Test
    void openingClosesWhateverTheServerHasOpen() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.open(steve);

        final List<PacketWrapper<?>> received = sender.received(steve);

        assertEquals(1, received.size());
        assertEquals(0, ((WrapperPlayClientCloseWindow) received.getFirst()).getWindowId());
    }

    @Test
    void hiddenAreasBlockInventoryUpdates() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.open(steve);
        sender.clear();

        assertTrue(service.hidesInventorySlot(steve, 9));
        assertTrue(service.hidesInventorySlot(steve, 44));
        assertTrue(service.hidesInventory(steve));
        assertTrue(sender.packets(steve).isEmpty());
    }

    @Test
    void mirroredMenusOnlyBlockSlotsWithFakeItems() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        menu.setMirrorPlayerInventory(true);
        menu.setPlayerItem(27, item(5));
        menu.open(steve);

        assertTrue(service.hidesInventorySlot(steve, 36));
        assertFalse(service.hidesInventorySlot(steve, 37));
        assertTrue(service.hidesInventory(steve));

        menu.clearPlayerItems();

        assertFalse(service.hidesInventorySlot(steve, 36));
        assertFalse(service.hidesInventory(steve));
    }

    @Test
    void armorAndClosedMenusAreNotBlocked() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        assertFalse(service.hidesInventorySlot(steve, 36));
        assertFalse(service.hidesInventory(steve));

        menu.open(steve);

        assertFalse(service.hidesInventorySlot(steve, 5));
        assertFalse(service.hidesInventorySlot(steve, 45));
        assertFalse(service.hidesInventorySlot(steve, -1));
    }

    @Test
    void serverWindowsUpdateThePlayerInventoryPart() {
        final PlayerInventoryCache cache = service.getInventoryCache();
        final List<ItemStack> window = new ArrayList<>();

        for (int i = 0; i < 27 + 36; i++) {
            window.add(i < 27 ? item(64) : ItemStack.EMPTY);
        }
        window.set(27, item(1));
        window.set(27 + 35, item(2));

        cache.setContents(steve.getUUID(), List.of());
        cache.setWindowContents(steve.getUUID(), 5, window);

        assertEquals(1, cache.getMenuSlot(steve.getUUID(), 0).getAmount());
        assertEquals(2, cache.getMenuSlot(steve.getUUID(), 35).getAmount());

        cache.setWindowSlot(steve.getUUID(), 5, 27 + 4, item(7));
        cache.setWindowSlot(steve.getUUID(), 5, 3, item(9));
        cache.setWindowSlot(steve.getUUID(), 6, 27 + 5, item(9));

        assertEquals(7, cache.getMenuSlot(steve.getUUID(), 4).getAmount());
        assertTrue(cache.getMenuSlot(steve.getUUID(), 5).isEmpty());
        assertTrue(cache.getContents(steve.getUUID()).orElseThrow().stream().noneMatch(stack -> stack.getAmount() == 64));
    }

    @Test
    void playerInventoryWindowStillWorks() {
        final PlayerInventoryCache cache = service.getInventoryCache();

        cache.setWindowContents(steve.getUUID(), 0, List.of(item(1)));
        cache.setWindowSlot(steve.getUUID(), 0, 9, item(4));

        assertEquals(1, cache.getContents(steve.getUUID()).orElseThrow().getFirst().getAmount());
        assertEquals(4, cache.getMenuSlot(steve.getUUID(), 0).getAmount());
    }

    @Test
    void playerInventoryIndexesMapToWindowSlots() {
        assertEquals(36, PlayerInventoryCache.toWindowSlot(0));
        assertEquals(44, PlayerInventoryCache.toWindowSlot(8));
        assertEquals(9, PlayerInventoryCache.toWindowSlot(9));
        assertEquals(35, PlayerInventoryCache.toWindowSlot(35));
        assertEquals(8, PlayerInventoryCache.toWindowSlot(36));
        assertEquals(5, PlayerInventoryCache.toWindowSlot(39));
        assertEquals(45, PlayerInventoryCache.toWindowSlot(40));
        assertEquals(-1, PlayerInventoryCache.toWindowSlot(41));
    }

    @Test
    void deathOrRespawnClosesTheMenuWithoutPackets() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());
        final List<String> closes = new ArrayList<>();

        menu.onClose(uuid -> closes.add("closed"));
        menu.open(steve);
        executor.runAll();
        sender.clear();

        service.handleServerWindow(steve);
        executor.runAll();

        assertEquals(List.of("closed"), closes);
        assertTrue(service.getMenu(steve).isEmpty());
        assertFalse(menu.isOpen());
        assertTrue(sender.packets(steve).isEmpty());
        assertTrue(sender.received(steve).isEmpty());
    }

    @Test
    void closeSendsCloseWindowAndRestoresTheCachedInventory() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        service.getInventoryCache().setContents(steve.getUUID(), List.of(item(1)));
        menu.open(steve);
        sender.clear();
        service.closeMenu(steve);

        final List<WrapperPlayServerWindowItems> restored = sender.packets(steve, WrapperPlayServerWindowItems.class);

        assertEquals(WrapperPlayServerCloseWindow.class, sender.types(steve).getFirst());
        assertEquals(1, restored.size());
        assertEquals(0, restored.getFirst().getWindowId());
        assertEquals(46, restored.getFirst().getItems().size());
    }

    @Test
    void customRestorerReplacesTheCache() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());
        final List<User> restored = new ArrayList<>();

        service.setInventoryRestorer(restored::add);
        service.getInventoryCache().setContents(steve.getUUID(), List.of(item(1)));
        menu.open(steve);
        sender.clear();
        service.handleClientClose(steve, windowId(steve));
        executor.runAll();

        assertEquals(List.of(steve), restored);
        assertTrue(sender.packets(steve, WrapperPlayServerWindowItems.class).isEmpty());
    }

    @Test
    void playerItemsAndMirroringFillTheBottomArea() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());
        final List<ItemStack> inventory = new ArrayList<>();

        for (int i = 0; i < 46; i++) {
            inventory.add(i == 10 ? item(10) : ItemStack.EMPTY);
        }

        service.getInventoryCache().setContents(steve.getUUID(), inventory);
        menu.setMirrorPlayerInventory(true);
        menu.setPlayerItem(0, item(2));
        menu.open(steve);

        final List<ItemStack> items = sender.packets(steve, WrapperPlayServerWindowItems.class).getFirst().getItems();

        assertEquals(2, items.get(9).getAmount());
        assertEquals(10, items.get(10).getAmount());
    }

    @Test
    void openingAnotherMenuReplacesTheOpenOne() {
        final PacketMenu main = new PacketMenu(1, Component.text("Main"));
        final PacketMenu shop = new PacketMenu(1, Component.text("Shop"));

        main.open(steve);
        shop.open(steve);
        executor.runAll();

        assertSame(shop, service.getMenu(steve).orElseThrow());
        assertFalse(main.isOpen());
    }

    @Test
    void titleUpdateReopensWithTheSameWindowId() {
        final PacketMenu menu = new PacketMenu(1, Component.text("Before"));

        menu.open(steve);
        executor.runAll();

        final int id = windowId(steve);

        sender.clear();
        menu.setTitle(Component.text("After"));

        final WrapperPlayServerOpenWindow reopened = sender.packets(steve, WrapperPlayServerOpenWindow.class).getFirst();

        assertEquals(id, reopened.getContainerId());
        assertEquals(Component.text("After"), reopened.getTitle());
    }

    @Test
    void disconnectForgetsTheUser() {
        final PacketMenu menu = new PacketMenu(1, Component.empty());

        service.getInventoryCache().setContents(steve.getUUID(), List.of());
        menu.open(steve);
        executor.runAll();
        service.handleDisconnect(steve);
        executor.runAll();

        assertTrue(service.getMenu(steve).isEmpty());
        assertFalse(service.getInventoryCache().has(steve.getUUID()));
        assertFalse(menu.isOpen());
    }

    @ParameterizedTest
    @CsvSource({
            "PICKUP, 0, LEFT",
            "PICKUP, 1, RIGHT",
            "QUICK_MOVE, 0, SHIFT_LEFT",
            "QUICK_MOVE, 1, SHIFT_RIGHT",
            "SWAP, 40, SWAP_OFFHAND",
            "SWAP, 3, NUMBER_KEY",
            "CLONE, 2, MIDDLE",
            "THROW, 0, DROP",
            "THROW, 1, CONTROL_DROP",
            "PICKUP_ALL, 0, DOUBLE_CLICK",
            "QUICK_CRAFT, 0, UNKNOWN"
    })
    void clickTypesAreMapped(WindowClickType type, int button, ButtonType expected) {
        assertEquals(expected, service.getButtonType(type, button));
    }

    @Test
    void packetMenuRequiresAnInitializedService() {
        Services.clear();

        assertThrows(IllegalStateException.class, () -> new PacketMenu(1, Component.empty()).open(steve));
    }

    @Test
    void playerItemIndexIsValidated() {
        assertThrows(IllegalArgumentException.class, () -> new PacketMenu(1, Component.empty()).setPlayerItem(36, ItemStack.EMPTY));
    }
}
