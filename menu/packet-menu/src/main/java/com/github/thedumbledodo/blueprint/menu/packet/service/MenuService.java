package com.github.thedumbledodo.blueprint.menu.packet.service;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.github.thedumbledodo.blueprint.menu.model.ButtonType;
import com.github.thedumbledodo.blueprint.menu.packet.PacketExecuteComponent;
import com.github.thedumbledodo.blueprint.menu.packet.PacketMenu;
import com.github.thedumbledodo.blueprint.menu.packet.PacketMenuTypes;
import com.github.thedumbledodo.blueprint.menu.packet.cache.PlayerInventoryCache;
import com.github.thedumbledodo.blueprint.menu.packet.sender.PacketSender;
import com.github.thedumbledodo.blueprint.menu.scheduler.MenuScheduler;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public final class MenuService {

    public static final int PLAYER_INVENTORY_SLOTS = 36;

    private static final int MAX_WINDOW_ID = 100;

    private final PacketSender sender;

    @Getter
    private final Executor executor;

    @Getter
    private final MenuScheduler scheduler;

    @Getter
    private final PlayerInventoryCache inventoryCache = new PlayerInventoryCache();

    private final Map<UUID, OpenMenu> openMenus = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> windowIds = new ConcurrentHashMap<>();

    @Setter
    private Consumer<User> inventoryRestorer;

    public MenuService(PacketSender sender, Executor executor, MenuScheduler scheduler) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.scheduler = scheduler;
    }

    public void openMenu(User user, PacketMenu menu) {
        final UUID uuid = user.getUUID();
        final OpenMenu previous = openMenus.remove(uuid);

        if (previous != null) {
            executor.execute(() -> previous.menu().handleClose(uuid));
        }

        final OpenMenu open = new OpenMenu(menu, nextWindowId(uuid), user);

        openMenus.put(uuid, open);

        sendOpenWindow(open);
        sendContents(open);

        executor.execute(() -> menu.handleOpen(uuid));
    }

    public void closeMenu(User user) {
        final OpenMenu open = openMenus.remove(user.getUUID());

        if (open == null) {
            return;
        }

        sender.send(user, new WrapperPlayServerCloseWindow(open.windowId()));
        finishClose(open);
    }

    public Optional<PacketMenu> getMenu(User user) {
        return getMenu(user.getUUID());
    }

    public Optional<PacketMenu> getMenu(UUID uuid) {
        final OpenMenu open = openMenus.get(uuid);

        return open == null ? Optional.empty() : Optional.of(open.menu());
    }

    public OptionalInt getWindowId(UUID uuid) {
        final OpenMenu open = openMenus.get(uuid);

        return open == null ? OptionalInt.empty() : OptionalInt.of(open.windowId());
    }

    public Set<User> getViewers(PacketMenu menu) {
        final Set<User> viewers = new HashSet<>();

        for (OpenMenu open : openMenus.values()) {
            if (open.menu() != menu) {
                continue;
            }
            viewers.add(open.user());
        }
        return viewers;
    }

    public void sendSlot(PacketMenu menu, int slot) {
        for (OpenMenu open : openMenus.values()) {
            if (open.menu() != menu) {
                continue;
            }
            sendSlot(open, slot);
        }
    }

    public void sendContents(PacketMenu menu) {
        for (OpenMenu open : openMenus.values()) {
            if (open.menu() != menu) {
                continue;
            }
            sendContents(open);
        }
    }

    public void sendTitle(PacketMenu menu) {
        for (OpenMenu open : openMenus.values()) {
            if (open.menu() != menu) {
                continue;
            }

            sendOpenWindow(open);
            sendContents(open);
        }
    }

    public boolean handleClick(User user, WrapperPlayClientClickWindow packet) {
        final UUID uuid = user.getUUID();
        final OpenMenu open = openMenus.get(uuid);

        if (open == null || packet.getWindowId() != open.windowId()) {
            return false;
        }

        final PacketMenu menu = open.menu();
        final int slot = packet.getSlot();
        final ButtonType buttonType = getButtonType(packet.getWindowClickType(), packet.getButton());

        resync(open, slot, buttonType);

        if (slot < 0 || slot >= menu.getSize()) {
            return true;
        }

        final ItemStack item = menu.getItem(slot).orElse(ItemStack.EMPTY);
        final PacketExecuteComponent click = new PacketExecuteComponent(uuid, user, buttonType, slot, item, menu);

        executor.execute(() -> menu.handleClick(click));
        return true;
    }

    public boolean handleClientClose(User user, int windowId) {
        final UUID uuid = user.getUUID();
        final OpenMenu open = openMenus.get(uuid);

        if (open == null || open.windowId() != windowId) {
            return false;
        }

        openMenus.remove(uuid, open);
        finishClose(open);
        return true;
    }

    public void handleServerWindow(User user) {
        final OpenMenu open = openMenus.remove(user.getUUID());

        if (open == null) {
            return;
        }

        executor.execute(() -> open.menu().handleClose(user.getUUID()));
    }

    public void handleDisconnect(User user) {
        final UUID uuid = user.getUUID();
        final OpenMenu open = openMenus.remove(uuid);

        inventoryCache.remove(uuid);
        windowIds.remove(uuid);

        if (open == null) {
            return;
        }
        executor.execute(() -> open.menu().handleClose(uuid));
    }

    public ButtonType getButtonType(WindowClickType clickType, int button) {

        return switch (clickType) {
            case PICKUP -> button == 0 ? ButtonType.LEFT : ButtonType.RIGHT;
            case QUICK_MOVE -> button == 0 ? ButtonType.SHIFT_LEFT : ButtonType.SHIFT_RIGHT;

            case SWAP -> {
                if (button == 40) {
                    yield ButtonType.SWAP_OFFHAND;
                }

                if (button >= 0 && button <= 8) {
                    yield ButtonType.NUMBER_KEY;
                }
                yield ButtonType.LEFT;
            }
            case CLONE -> ButtonType.MIDDLE;
            case THROW -> button == 0 ? ButtonType.DROP : ButtonType.CONTROL_DROP;
            case PICKUP_ALL -> ButtonType.DOUBLE_CLICK;

            default -> ButtonType.UNKNOWN;
        };
    }

    private void finishClose(OpenMenu open) {
        final User user = open.user();

        restoreInventory(user);
        executor.execute(() -> open.menu().handleClose(user.getUUID()));
    }

    private void restoreInventory(User user) {
        if (inventoryRestorer != null) {
            executor.execute(() -> inventoryRestorer.accept(user));
            return;
        }

        inventoryCache.getContents(user.getUUID()).ifPresent(contents ->
                sender.send(user, new WrapperPlayServerWindowItems(0, 0, contents, ItemStack.EMPTY)));
    }

    private int nextWindowId(UUID uuid) {
        return windowIds.merge(uuid, 1, (current, ignored) -> current % MAX_WINDOW_ID + 1);
    }

    private void resync(OpenMenu open, int slot, ButtonType buttonType) {
        final int totalSlots = open.menu().getSize() + PLAYER_INVENTORY_SLOTS;
        final boolean singleSlot = buttonType.isSimpleClick() || buttonType == ButtonType.DROP || buttonType == ButtonType.CONTROL_DROP;

        if (singleSlot && slot >= 0 && slot < totalSlots) {
            sendSlot(open, slot);
            clearCursor(open.user());
            return;
        }

        if (singleSlot) {
            clearCursor(open.user());
            return;
        }
        sendContents(open);
    }

    private void sendOpenWindow(OpenMenu open) {
        final PacketMenu menu = open.menu();
        final int type = PacketMenuTypes.getId(menu.getType(), open.user().getClientVersion());

        sender.send(open.user(), new WrapperPlayServerOpenWindow(open.windowId(), type, menu.getTitle()));
    }

    private void sendSlot(OpenMenu open, int slot) {
        sender.send(open.user(), new WrapperPlayServerSetSlot(open.windowId(), 0, slot, itemAt(open, slot)));
    }

    private void sendContents(OpenMenu open) {
        final PacketMenu menu = open.menu();
        final int size = menu.getSize();
        final List<ItemStack> items = new ArrayList<>(size + PLAYER_INVENTORY_SLOTS);

        for (int slot = 0; slot < size + PLAYER_INVENTORY_SLOTS; slot++) {
            items.add(itemAt(open, slot));
        }
        sender.send(open.user(), new WrapperPlayServerWindowItems(open.windowId(), 0, items, ItemStack.EMPTY));
    }

    private ItemStack itemAt(OpenMenu open, int slot) {
        final PacketMenu menu = open.menu();

        if (slot < menu.getSize()) {
            return menu.getItem(slot).orElse(ItemStack.EMPTY);
        }

        final int index = slot - menu.getSize();
        final Optional<ItemStack> playerItem = menu.getPlayerItem(index);

        if (playerItem.isPresent()) {
            return playerItem.get();
        }

        if (menu.isMirrorPlayerInventory()) {
            return inventoryCache.getMenuSlot(open.user().getUUID(), index);
        }
        return ItemStack.EMPTY;
    }

    private void clearCursor(User user) {
        final ClientVersion version = user.getClientVersion();

        if (version != null && version.isNewerThanOrEquals(ClientVersion.V_1_21_2)) {
            sender.send(user, new WrapperPlayServerSetCursorItem(ItemStack.EMPTY));
            return;
        }
        sender.send(user, new WrapperPlayServerSetSlot(-1, 0, -1, ItemStack.EMPTY));
    }

    private record OpenMenu(PacketMenu menu, int windowId, User user) {
    }
}
