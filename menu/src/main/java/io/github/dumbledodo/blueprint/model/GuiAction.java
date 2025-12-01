package io.github.dumbledodo.blueprint.model;

import java.util.UUID;

@FunctionalInterface
public interface GuiAction {

    void execute(UUID uuid);
}