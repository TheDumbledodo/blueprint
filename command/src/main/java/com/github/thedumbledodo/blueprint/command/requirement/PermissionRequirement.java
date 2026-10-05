package com.github.thedumbledodo.blueprint.command.requirement;

import com.github.thedumbledodo.blueprint.command.model.CommandActor;

import java.util.Objects;
import java.util.function.Supplier;

public record PermissionRequirement(Supplier<String> permission) implements CommandRequirement {

    public PermissionRequirement(Supplier<String> permission) {
        this.permission = Objects.requireNonNull(permission, "permission");
    }

    public static PermissionRequirement of(String permission) {
        return new PermissionRequirement(() -> permission);
    }

    @Override
    public boolean test(CommandActor actor) {
        final String value = permission.get();

        if (value == null || value.isBlank()) {
            return true;
        }
        return actor.hasPermission(value);
    }
}
