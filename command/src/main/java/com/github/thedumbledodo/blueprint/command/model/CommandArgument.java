package com.github.thedumbledodo.blueprint.command.model;

import com.github.thedumbledodo.blueprint.command.argument.SuggestionProvider;

public record CommandArgument<T>(Argument<T> argument, boolean optional, T defaultValue, String defaultInput,
                                 SuggestionProvider suggestions, String completionId) {

    public static <T> CommandArgument<T> required(Argument<T> argument) {
        return new CommandArgument<>(argument, false, null, null, null, null);
    }

    public static <T> CommandArgument<T> optional(Argument<T> argument, T defaultValue) {
        return new CommandArgument<>(argument, true, defaultValue, null, null, null);
    }

    public CommandArgument<T> withSuggestions(SuggestionProvider suggestions) {
        return new CommandArgument<>(argument, optional, defaultValue, defaultInput, suggestions, completionId);
    }

    public String getName() {
        return argument.getName();
    }
}
