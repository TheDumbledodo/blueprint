package io.github.dumbledodo.blueprint.model;

import io.github.dumbledodo.blueprint.component.CooldownComponent;
import io.github.dumbledodo.blueprint.component.ExecuteComponent;
import lombok.Getter;

import java.util.function.Consumer;

@Getter
public final class Button {

    private final Consumer<ExecuteComponent> execute;
    private final CooldownComponent cooldown;

    public Button(Consumer<ExecuteComponent> execute, CooldownComponent cooldown) {
        this.execute = execute;
        this.cooldown = cooldown;
    }

    public Button(Consumer<ExecuteComponent> execute) {
        this(execute, null);
    }
}