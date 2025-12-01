package io.github.dumbledodo.blueprint.component;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.dumbledodo.blueprint.model.ButtonType;

import java.util.Objects;

public record ExecuteComponent(User user, ButtonType buttonType, int slot, ItemStack itemStack) {

    public ExecuteComponent(User user, ButtonType buttonType, int slot, ItemStack itemStack) {
        this.user = Objects.requireNonNull(user, "user cannot be null");
        this.buttonType = Objects.requireNonNull(buttonType, "button type cannot be null");
        this.slot = slot;
        this.itemStack = itemStack;
    }
}