package com.github.thedumbledodo.blueprint.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;

import java.util.Collection;
import java.util.List;

public interface ArgumentParser<T> {

    ArgumentType<?> getNativeType();

    T parse(CommandActor actor, Object input);

    default Collection<String> suggest(CommandActor actor, String input) {
        return List.of();
    }
}
