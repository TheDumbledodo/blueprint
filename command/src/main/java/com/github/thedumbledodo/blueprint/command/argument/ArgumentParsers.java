package com.github.thedumbledodo.blueprint.command.argument;

import com.mojang.brigadier.arguments.*;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.*;
import java.util.function.Function;

@UtilityClass
public class ArgumentParsers {

    public ArgumentParser<String> word() {
        return simple(StringArgumentType.word(), String.class::cast);
    }

    public ArgumentParser<String> quotedString() {
        return simple(StringArgumentType.string(), String.class::cast);
    }

    public ArgumentParser<String> greedyString() {
        return simple(StringArgumentType.greedyString(), String.class::cast);
    }

    public ArgumentParser<Integer> integer() {
        return simple(IntegerArgumentType.integer(), Integer.class::cast);
    }

    public ArgumentParser<Integer> integer(int min, int max) {
        return simple(IntegerArgumentType.integer(min, max), Integer.class::cast);
    }

    public ArgumentParser<Long> longs() {
        return simple(LongArgumentType.longArg(), Long.class::cast);
    }

    public ArgumentParser<Long> longs(long min, long max) {
        return simple(LongArgumentType.longArg(min, max), Long.class::cast);
    }

    public ArgumentParser<Double> doubles() {
        return simple(DoubleArgumentType.doubleArg(), Double.class::cast);
    }

    public ArgumentParser<Double> doubles(double min, double max) {
        return simple(DoubleArgumentType.doubleArg(min, max), Double.class::cast);
    }

    public ArgumentParser<Float> floats() {
        return simple(FloatArgumentType.floatArg(), Float.class::cast);
    }

    public ArgumentParser<Float> floats(float min, float max) {
        return simple(FloatArgumentType.floatArg(min, max), Float.class::cast);
    }

    public ArgumentParser<Boolean> bool() {
        return simple(BoolArgumentType.bool(), Boolean.class::cast);
    }

    public ArgumentParser<UUID> uuid() {
        return new ArgumentParser<>() {

            @Override
            public ArgumentType<?> getNativeType() {
                return StringArgumentType.word();
            }

            @Override
            public UUID parse(CommandActor actor, Object input) {
                try {
                    return UUID.fromString((String) input);

                } catch (IllegalArgumentException exception) {
                    throw new CommandException(CommandMessages::getInvalidArgument, Placeholder.unparsed("input", String.valueOf(input)));
                }
            }
        };
    }

    public <E extends Enum<E>> ArgumentParser<E> enumeration(Class<E> type) {
        final E[] constants = type.getEnumConstants();
        final List<String> names = new ArrayList<>(constants.length);

        for (E constant : constants) {
            names.add(constant.name().toLowerCase(Locale.ROOT));
        }

        final String joined = String.join(", ", names);

        return new ArgumentParser<>() {

            @Override
            public ArgumentType<?> getNativeType() {
                return StringArgumentType.word();
            }

            @Override
            public E parse(CommandActor actor, Object input) {
                final String text = String.valueOf(input);

                for (E constant : constants) {
                    if (constant.name().equalsIgnoreCase(text)) {
                        return constant;
                    }
                }
                throw new CommandException(CommandMessages::getInvalidChoice,
                        Placeholder.unparsed("input", text),
                        Placeholder.unparsed("values", joined));
            }

            @Override
            public Collection<String> suggest(CommandActor actor, String input) {
                return names;
            }
        };
    }

    public <T> ArgumentParser<T> simple(ArgumentType<?> nativeType, Function<Object, T> converter) {
        return new ArgumentParser<>() {

            @Override
            public ArgumentType<?> getNativeType() {
                return nativeType;
            }

            @Override
            public T parse(CommandActor actor, Object input) {
                return converter.apply(input);
            }
        };
    }

    public Map<Class<?>, ArgumentParser<?>> defaults() {
        final Map<Class<?>, ArgumentParser<?>> parsers = new LinkedHashMap<>();

        parsers.put(String.class, word());
        parsers.put(Integer.class, integer());
        parsers.put(Long.class, longs());
        parsers.put(Double.class, doubles());
        parsers.put(Float.class, floats());
        parsers.put(Boolean.class, bool());
        parsers.put(UUID.class, uuid());

        return parsers;
    }
}
