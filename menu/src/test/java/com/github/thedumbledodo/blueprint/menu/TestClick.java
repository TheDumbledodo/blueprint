package com.github.thedumbledodo.blueprint.menu;

import com.github.thedumbledodo.blueprint.menu.component.ExecuteComponent;
import com.github.thedumbledodo.blueprint.menu.model.ButtonType;

import java.util.UUID;

public final class TestClick extends ExecuteComponent {

    public TestClick(UUID uuid, ButtonType buttonType, int slot) {
        super(uuid, buttonType, slot);
    }

    public static TestClick left(UUID uuid, int slot) {
        return new TestClick(uuid, ButtonType.LEFT, slot);
    }
}
