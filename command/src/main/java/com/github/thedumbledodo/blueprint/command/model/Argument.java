package com.github.thedumbledodo.blueprint.command.model;

import com.github.thedumbledodo.blueprint.command.argument.ArgumentParser;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class Argument<T> {

    private final String name;

    private final Class<T> type;
    private final ArgumentParser<T> parser;

    private Argument(String name, Class<T> type, ArgumentParser<T> parser) {
        this.name = Objects.requireNonNull(name, "name");

        this.type = type;
        this.parser = parser;
    }

    public static <T> Argument<T> of(String name, Class<T> type) {
        return new Argument<>(name, Objects.requireNonNull(type, "type"), null);
    }

    public static <T> Argument<T> of(String name, ArgumentParser<T> parser) {
        return new Argument<>(name, null, Objects.requireNonNull(parser, "parser"));
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }
}
