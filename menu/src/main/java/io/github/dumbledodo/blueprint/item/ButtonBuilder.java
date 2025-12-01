package io.github.dumbledodo.blueprint.item;

import io.github.dumbledodo.blueprint.component.CooldownComponent;
import io.github.dumbledodo.blueprint.component.ExecuteComponent;
import io.github.dumbledodo.blueprint.model.Button;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter @Setter
public class ButtonBuilder {

    private Consumer<ExecuteComponent> click;
    private CooldownComponent cooldown = new CooldownComponent(0);

    public Button build() {
        return new Button(click, cooldown);
    }
}