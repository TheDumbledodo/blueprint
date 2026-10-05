package com.github.thedumbledodo.blueprint.menu.model;

import java.util.UUID;

@FunctionalInterface
public interface GuiAction {

    void execute(UUID uuid);
}
