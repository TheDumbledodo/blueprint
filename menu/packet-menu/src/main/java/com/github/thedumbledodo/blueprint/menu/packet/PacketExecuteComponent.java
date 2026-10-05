package com.github.thedumbledodo.blueprint.menu.packet;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.thedumbledodo.blueprint.menu.component.ExecuteComponent;
import com.github.thedumbledodo.blueprint.menu.model.ButtonType;

import java.util.Objects;
import java.util.UUID;

public final class PacketExecuteComponent extends ExecuteComponent {

    private final User user;
    private final ItemStack item;
    private final PacketMenu menu;

    public PacketExecuteComponent(UUID uuid, User user, ButtonType buttonType, int slot, ItemStack item, PacketMenu menu) {
        super(uuid, buttonType, slot);

        this.user = Objects.requireNonNull(user, "user");
        this.item = item == null ? ItemStack.EMPTY : item;
        this.menu = Objects.requireNonNull(menu, "menu");
    }

    public User user() {
        return user;
    }

    public ItemStack item() {
        return item;
    }

    public PacketMenu menu() {
        return menu;
    }
}
