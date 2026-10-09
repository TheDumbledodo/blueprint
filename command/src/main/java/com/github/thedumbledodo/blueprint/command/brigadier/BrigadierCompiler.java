package com.github.thedumbledodo.blueprint.command.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.github.thedumbledodo.blueprint.chat.Text;
import com.github.thedumbledodo.blueprint.command.CommandManager;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParser;
import com.github.thedumbledodo.blueprint.command.argument.SuggestionProvider;
import com.github.thedumbledodo.blueprint.command.context.ExecutionContext;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import com.github.thedumbledodo.blueprint.command.model.CommandArgument;
import com.github.thedumbledodo.blueprint.command.model.CommandNode;
import com.github.thedumbledodo.blueprint.command.requirement.ArgumentPermission;
import com.github.thedumbledodo.blueprint.command.requirement.CommandRequirement;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class BrigadierCompiler<S> {

    private final CommandManager<S> manager;

    public BrigadierCompiler(CommandManager<S> manager) {
        this.manager = manager;
    }

    public LiteralCommandNode<S> compile(CommandNode node) {
        return compile(node, node.name());
    }

    public LiteralCommandNode<S> compile(CommandNode node, String literalName) {
        return compileLiteral(node, literalName, List.of(), "/" + literalName).build();
    }

    private LiteralArgumentBuilder<S> compileLiteral(CommandNode node, String literalName, List<CommandRequirement> inherited, String path) {
        final List<CommandRequirement> requirements = new ArrayList<>(inherited);
        final LiteralArgumentBuilder<S> literal = LiteralArgumentBuilder.literal(literalName);

        requirements.addAll(node.requirements());

        if (manager.isHideUnpermitted() && !node.requirements().isEmpty()) {
            final List<CommandRequirement> own = node.requirements();

            literal.requires(source -> CommandRequirement.testAll(own, manager.createActor(source)));
        }

        for (CommandNode child : node.children()) {
            literal.then(compileLiteral(child, child.name(), requirements, path + " " + child.name()));

            for (String alias : child.aliases()) {
                literal.then(compileLiteral(child, alias, requirements, path + " " + alias));
            }
        }

        final Command<S> invalidSyntax = context -> fail(requirements, CommandMessages::getInvalidSyntax, context);

        if (!node.children().isEmpty() && node.arguments().isEmpty() && node.getChild("subcommand").isEmpty()) {
            literal.then(RequiredArgumentBuilder.<S, String>argument("subcommand", StringArgumentType.greedyString())
                    .executes(context -> fail(requirements, CommandMessages::getUnknownCommand, context)));
        }

        if (!node.isExecutable()) {
            return literal.executes(invalidSyntax);
        }

        final List<CommandArgument<?>> arguments = node.arguments();
        final List<ArgumentParser<?>> parsers = resolveParsers(node, path);
        final Command<S> command = context -> execute(node, requirements, parsers, context);

        literal.executes(arguments.isEmpty() || arguments.getFirst().optional() ? command : invalidSyntax);

        RequiredArgumentBuilder<S, ?> next = null;

        for (int i = arguments.size() - 1; i >= 0; i--) {
            final CommandArgument<?> argument = arguments.get(i);
            final RequiredArgumentBuilder<S, ?> builder = argument(argument.getName(), parsers.get(i).getNativeType());
            final SuggestionProvider suggestions = resolveSuggestions(argument, parsers.get(i), path);

            if (suggestions != null) {
                builder.suggests((context, suggestionsBuilder) -> suggest(suggestions, context, suggestionsBuilder));
            }

            final boolean last = i == arguments.size() - 1;

            builder.executes(last || arguments.get(i + 1).optional() ? command : invalidSyntax);

            if (next != null) {
                builder.then(next);
            }
            next = builder;
        }

        if (next != null) {
            literal.then(next);
        }
        return literal;
    }

    private int execute(CommandNode node, List<CommandRequirement> requirements, List<ArgumentParser<?>> parsers, CommandContext<S> context) {
        final CommandActor actor = manager.createActor(context.getSource());
        final CommandMessages messages = manager.getMessages();

        try {
            if (!manager.isHideUnpermitted() && !CommandRequirement.testAll(requirements, actor)) {
                throw new CommandException(CommandMessages::getNoPermission);
            }

            if (!CommandRequirement.testAll(node.executionRequirements(), actor)) {
                throw new CommandException(CommandMessages::getNoPermission);
            }

            final Class<?> senderType = node.senderType();

            if (senderType != null && senderType != CommandActor.class && !senderType.isInstance(actor.getSender())) {
                throw new CommandException(CommandMessages.getWrongSender(actor));
            }

            final Map<String, Object> values = new LinkedHashMap<>();
            final Map<String, String> inputs = readInputs(context);
            final List<CommandArgument<?>> arguments = node.arguments();

            for (int i = 0; i < arguments.size(); i++) {
                final CommandArgument<?> argument = arguments.get(i);
                final ArgumentParser<?> parser = parsers.get(i);
                final Object input = getArgument(context, argument.getName());

                if (input == null) {
                    values.put(argument.getName(), defaultValue(argument, parser, actor));
                    continue;
                }
                values.put(argument.getName(), parser.parse(actor, input));
            }

            final ExecutionContext execution = new ExecutionContext(actor, messages, values, inputs);

            for (ArgumentPermission permission : node.argumentPermissions()) {
                execution.checkPermission(permission.resolve(execution));
            }

            node.action().execute(execution);
            return Command.SINGLE_SUCCESS;

        } catch (CommandException exception) {
            actor.sendMessage(exception.render(messages));
            return 0;

        } catch (RuntimeException exception) {
            manager.handleError(node, exception);
            actor.sendMessage(Text.translate(messages.getCommandError()));
            return 0;
        }
    }

    private int fail(List<CommandRequirement> requirements, Function<CommandMessages, String> message, CommandContext<S> context) {
        final CommandActor actor = manager.createActor(context.getSource());
        final CommandMessages messages = manager.getMessages();
        final String text = CommandRequirement.testAll(requirements, actor) ? message.apply(messages) : messages.getNoPermission();

        actor.sendMessage(Text.translate(text));
        return 0;
    }

    private Object defaultValue(CommandArgument<?> argument, ArgumentParser<?> parser, CommandActor actor) {
        if (argument.defaultValue() != null) {
            return argument.defaultValue();
        }

        final String defaultInput = argument.defaultInput();

        if (defaultInput == null || defaultInput.isEmpty()) {
            return null;
        }

        try {
            return parser.parse(actor, parser.getNativeType().parse(new StringReader(defaultInput)));

        } catch (CommandSyntaxException exception) {
            throw new IllegalStateException("Default value \"" + defaultInput + "\" is not valid for <" + argument.getName() + ">", exception);
        }
    }

    private Map<String, String> readInputs(CommandContext<S> context) {
        final Map<String, String> inputs = new LinkedHashMap<>();
        final String input = context.getInput();

        for (ParsedCommandNode<S> parsed : context.getNodes()) {
            if (!(parsed.getNode() instanceof ArgumentCommandNode<?, ?> argumentNode)) {
                continue;
            }
            inputs.put(argumentNode.getName(), parsed.getRange().get(input));
        }
        return inputs;
    }

    private Object getArgument(CommandContext<S> context, String name) {
        try {
            return context.getArgument(name, Object.class);

        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private List<ArgumentParser<?>> resolveParsers(CommandNode node, String path) {
        final List<ArgumentParser<?>> parsers = new ArrayList<>(node.arguments().size());

        for (CommandArgument<?> argument : node.arguments()) {
            ArgumentParser<?> parser = argument.argument().getParser();

            if (parser == null) {
                parser = manager.getParser(argument.argument().getType());
            }

            if (parser == null) {
                throw new IllegalStateException("No parser registered for " + argument.argument().getType().getName()
                        + " (argument <" + argument.getName() + "> in " + path + "). Register one with registerParser(...)");
            }
            parsers.add(parser);
        }
        return parsers;
    }

    private SuggestionProvider resolveSuggestions(CommandArgument<?> argument, ArgumentParser<?> parser, String path) {
        if (argument.suggestions() != null) {
            return argument.suggestions();
        }

        final String completionId = argument.completionId();

        if (completionId != null) {
            final SuggestionProvider provider = manager.getCompletion(completionId);

            if (provider == null) {
                throw new IllegalStateException("Unknown completion @" + completionId + " used in " + path + ". Register it with registerCompletion(...)");
            }
            return provider;
        }

        if (!overridesSuggest(parser)) {
            return null;
        }
        return parser::suggest;
    }

    private CompletableFuture<Suggestions> suggest(SuggestionProvider provider, CommandContext<S> context, SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining();
        final String lower = remaining.toLowerCase(Locale.ROOT);

        try {
            final CommandActor actor = manager.createActor(context.getSource());

            for (String suggestion : provider.suggest(actor, remaining)) {
                if (suggestion.toLowerCase(Locale.ROOT).startsWith(lower)) {
                    builder.suggest(suggestion);
                }
            }

        } catch (RuntimeException exception) {
            manager.handleError(null, exception);
        }
        return builder.buildFuture();
    }

    private static boolean overridesSuggest(ArgumentParser<?> parser) {
        try {
            return parser.getClass().getMethod("suggest", CommandActor.class, String.class).getDeclaringClass() != ArgumentParser.class;

        } catch (NoSuchMethodException exception) {
            return false;
        }
    }

    private static <S, T> RequiredArgumentBuilder<S, T> argument(String name, ArgumentType<?> type) {
        return RequiredArgumentBuilder.argument(name, (ArgumentType<T>) type);
    }
}
