package com.github.thedumbledodo.blueprint.command;

import com.github.thedumbledodo.blueprint.command.argument.ArgumentParser;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParsers;
import com.github.thedumbledodo.blueprint.command.argument.SuggestionProvider;
import com.github.thedumbledodo.blueprint.command.brigadier.BrigadierCompiler;
import com.github.thedumbledodo.blueprint.command.brigadier.RegisteredCommand;
import com.github.thedumbledodo.blueprint.command.builder.CommandBuilder;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.model.BaseCommand;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import com.github.thedumbledodo.blueprint.command.model.CommandNode;
import com.github.thedumbledodo.blueprint.command.parser.AnnotationParser;
import com.github.thedumbledodo.blueprint.command.requirement.CommandRequirement;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.reflect.Primitives;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class CommandManager<S> {

    @Getter
    private final Class<?> senderType;

    private final Map<Class<?>, ArgumentParser<?>> parsers = new ConcurrentHashMap<>();
    private final Map<String, SuggestionProvider> completions = new ConcurrentHashMap<>();

    private final Map<String, CommandRequirement> requirements = new ConcurrentHashMap<>();

    private final List<Object> pending = new ArrayList<>();
    private final List<RegisteredCommand<S>> commands = new ArrayList<>();

    @Getter private final BrigadierCompiler<S> compiler = new BrigadierCompiler<>(this);
    private final AnnotationParser annotationParser = new AnnotationParser(this);

    @Getter @Setter
    private CommandMessages messages = CommandMessages.DEFAULT;

    @Getter @Setter
    private boolean hideUnpermitted = true;

    protected CommandManager(Class<?> senderType) {
        this.senderType = Objects.requireNonNull(senderType, "senderType");

        parsers.putAll(ArgumentParsers.defaults());
    }

    public abstract CommandActor createActor(S source);

    public abstract void refresh();

    protected abstract void registerCommands(List<RegisteredCommand<S>> commands);

    public void install() {
        ComponentRegistry.registerListener((type, instance) -> {

            if (instance instanceof BaseCommand command) {
                register(command);
            }
            return instance;
        });
    }

    public synchronized void register(BaseCommand command) {
        pending.add(Objects.requireNonNull(command, "command"));
    }

    public synchronized void register(CommandBuilder builder) {
        pending.add(Objects.requireNonNull(builder, "builder"));
    }

    public synchronized void register(CommandNode node) {
        pending.add(Objects.requireNonNull(node, "node"));
    }

    public synchronized List<RegisteredCommand<S>> flush() {
        final List<RegisteredCommand<S>> flushed = new ArrayList<>(pending.size());

        for (Object entry : pending) {
            final CommandNode node = toNode(entry);

            flushed.add(new RegisteredCommand<>(node, compiler.compile(node)));
        }

        pending.clear();
        commands.addAll(flushed);

        if (!flushed.isEmpty()) {
            registerCommands(flushed);
        }
        return flushed;
    }

    public synchronized List<RegisteredCommand<S>> getCommands() {
        return List.copyOf(commands);
    }

    public <T> void registerParser(Class<T> type, ArgumentParser<T> parser) {
        parsers.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(parser, "parser"));
    }

    public <T> ArgumentParser<T> getParser(Class<T> type) {
        if (type == null) {
            return null;
        }

        final Class<?> wrapped = Primitives.wrap(type);
        final ArgumentParser<?> parser = parsers.get(wrapped);

        if (parser != null) {
            return (ArgumentParser<T>) parser;
        }

        if (wrapped.isEnum()) {
            return (ArgumentParser<T>) parsers.computeIfAbsent(wrapped, key -> ArgumentParsers.enumeration((Class) key));
        }
        return null;
    }

    public void registerCompletion(String id, SuggestionProvider provider) {
        completions.put(normalizeId(id), Objects.requireNonNull(provider, "provider"));
    }

    public void registerCompletion(String id, Supplier<? extends Collection<String>> supplier) {
        registerCompletion(id, SuggestionProvider.of(supplier));
    }

    public SuggestionProvider getCompletion(String id) {
        return completions.get(normalizeId(id));
    }

    public void registerRequirement(String key, CommandRequirement requirement) {
        requirements.put(normalizeId(key), Objects.requireNonNull(requirement, "requirement"));
    }

    public CommandRequirement getRequirement(String key) {
        return requirements.get(normalizeId(key));
    }

    public boolean isSenderType(Class<?> type) {
        return type == CommandActor.class || senderType.isAssignableFrom(type);
    }

    public void handleError(CommandNode node, Throwable throwable) {
        throwable.printStackTrace();
    }

    private CommandNode toNode(Object entry) {
        if (entry instanceof BaseCommand command) {
            return annotationParser.parse(command);
        }

        if (entry instanceof CommandBuilder builder) {
            return builder.build();
        }
        return (CommandNode) entry;
    }

    private static String normalizeId(String id) {
        Objects.requireNonNull(id, "id");

        if (id.startsWith("@")) {
            return id.substring(1).toLowerCase(Locale.ROOT);
        }
        return id.toLowerCase(Locale.ROOT);
    }
}
