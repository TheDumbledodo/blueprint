package com.github.thedumbledodo.blueprint.command.argument;

import com.github.thedumbledodo.blueprint.command.model.CommandActor;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@FunctionalInterface
public interface SuggestionProvider {

    SuggestionProvider NONE = (actor, input) -> List.of();

    Collection<String> suggest(CommandActor actor, String input);

    static SuggestionProvider of(Supplier<? extends Collection<String>> supplier) {
        return (actor, input) -> supplier.get();
    }

    static SuggestionProvider of(String... values) {
        final List<String> list = List.of(values);

        return (actor, input) -> list;
    }
}
