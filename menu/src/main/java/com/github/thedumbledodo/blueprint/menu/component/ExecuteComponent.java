package com.github.thedumbledodo.blueprint.menu.component;

import com.github.thedumbledodo.blueprint.menu.model.ButtonType;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public abstract class ExecuteComponent {

    private final UUID uuid;
    private final ButtonType buttonType;
    private final int slot;

    private ClickCooldowns cooldowns;
    private Object cooldownKey;

    protected ExecuteComponent(UUID uuid, ButtonType buttonType, int slot) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.buttonType = Objects.requireNonNull(buttonType, "buttonType");
        this.slot = slot;
    }

    public UUID uuid() {
        return uuid;
    }

    public ButtonType buttonType() {
        return buttonType;
    }

    public int slot() {
        return slot;
    }

    public boolean cooldown(Duration duration) {
        if (cooldowns == null) {
            throw new IllegalStateException("cooldown(...) can only be used inside a click action");
        }
        return cooldowns.tryStart(uuid, cooldownKey, duration);
    }

    public Duration remainingCooldown() {
        if (cooldowns == null) {
            return Duration.ZERO;
        }
        return cooldowns.getRemaining(uuid, cooldownKey);
    }

    @ApiStatus.Internal
    public void attach(ClickCooldowns cooldowns, Object cooldownKey) {
        this.cooldowns = cooldowns;
        this.cooldownKey = cooldownKey;
    }
}
