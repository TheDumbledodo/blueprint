package io.github.dumbledodo.blueprint.service;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.dumbledodo.blueprint.component.CooldownComponent;
import io.github.dumbledodo.blueprint.component.ExecuteComponent;
import io.github.dumbledodo.blueprint.model.Button;
import io.github.dumbledodo.blueprint.model.ButtonType;
import io.github.dumbledodo.blueprint.model.Menu;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class MenuService {

    private final AtomicInteger windowId = new AtomicInteger();
    private final Map<UUID, Menu> viewers = new ConcurrentHashMap<>();

    public void openMenu(User user, Menu menu) {
        viewers.put(user.getUUID(), menu);

        user.sendPacket(menu.getMenuPacket());
        user.sendPacket(menu.getContentPacket());
    }

    public void onCloseMenu(User user) {
        final UUID uuid = user.getUUID();
        final Optional<Menu> optionalMenu = getMenu(uuid);

        if (optionalMenu.isEmpty()) {
            return;
        }

        final Menu menu = optionalMenu.get();
        menu.getCloseAction().ifPresent(consumer -> consumer.execute(uuid));

        viewers.remove(uuid);
    }

    public int generateWindowId() {
        return windowId.incrementAndGet();
    }

    public void handleClickMenu(User user, Menu menu, WrapperPlayClientClickWindow packet) {
        final int slot = packet.getSlot();

        if (slot < 0) {
            return;
        }
        final int id = menu.getId();
        final int buttonSlot = packet.getButton();

        final ButtonType buttonType = getButtonType(packet.getWindowClickType(), buttonSlot);

        user.sendPacket(new WrapperPlayServerSetSlot(id, 0, slot, menu.getItem(slot)));
        user.sendPacket(new WrapperPlayServerSetCursorItem(ItemStack.EMPTY));

        switch (buttonType) {
            case SWAP_OFFHAND -> user.sendPacket(new WrapperPlayServerSetSlot(0, 0, 45, null));
            case NUMBER_KEY -> {
                final int hotbarSlot = menu.getSize() - (9 - buttonSlot);

                user.sendPacket(new WrapperPlayServerSetSlot(id, 0, hotbarSlot, menu.getItem(hotbarSlot)));
            }
            case SHIFT_LEFT, SHIFT_RIGHT ->
                    user.sendPacket(new WrapperPlayServerSetSlot(id, 0, menu.getLastEmptySlot(), null));
        }

        final Button button = menu.getButtons().get(slot);

        if (button == null) {
            return;
        }

        final CooldownComponent cooldown = button.getCooldown();

        if (cooldown != null && !cooldown.test(user)) {
            return;
        }
        button.getExecute().accept(new ExecuteComponent(user, buttonType, slot, null));
    }

    public void updateItem(Menu menu, ItemStack item, int slot) {
        menu.setItem(slot, item);

        final WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(menu.getId(), 0, slot, item);

        for (User user : menu.getViewers()) {
            user.sendPacket(setSlot);
        }
    }

    public void updateItems(Menu menu, Map<Integer, ItemStack> newItems) {
        newItems.forEach(menu::setItem);

        final int id = menu.getId();

        final List<ItemStack> fullContents = menu.getItems();
        final WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(id, 0, fullContents, null);

        for (User user : menu.getViewers()) {
            user.sendPacket(windowItems);
        }
    }

    public Optional<Menu> getMenu(User user) {
        return getMenu(user.getUUID());
    }

    public Optional<Menu> getMenu(UUID uuid) {
        return Optional.ofNullable(viewers.get(uuid));
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
}