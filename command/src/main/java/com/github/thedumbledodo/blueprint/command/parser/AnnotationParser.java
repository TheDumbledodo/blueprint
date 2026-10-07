package com.github.thedumbledodo.blueprint.command.parser;

import com.github.thedumbledodo.blueprint.command.CommandManager;
import com.github.thedumbledodo.blueprint.command.annotation.*;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParser;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParsers;
import com.github.thedumbledodo.blueprint.command.argument.SuggestionProvider;
import com.github.thedumbledodo.blueprint.command.builder.CommandBuilder;
import com.github.thedumbledodo.blueprint.command.context.ExecutionContext;
import com.github.thedumbledodo.blueprint.command.model.*;
import com.github.thedumbledodo.blueprint.command.requirement.CommandRequirement;
import com.github.thedumbledodo.blueprint.command.requirement.PermissionRequirement;
import com.github.thedumbledodo.blueprint.reflect.Primitives;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnnotationParser {

    private static final Pattern ARGUMENT_PLACEHOLDER = Pattern.compile("\\{([^}]+)}");

    private final CommandManager<?> manager;

    public AnnotationParser(CommandManager<?> manager) {
        this.manager = manager;
    }

    public CommandNode parse(BaseCommand command) {
        final Class<?> type = command.getClass();
        final Command annotation = type.getAnnotation(Command.class);

        if (annotation == null) {
            throw new IllegalStateException(type.getName() + " extends BaseCommand but has no @Command");
        }

        final List<String> names = splitAliases(annotation.value());
        final CommandBuilder root = CommandBuilder.of(names.getFirst());

        root.aliases(names.subList(1, names.size()).toArray(new String[0]));
        applyMetadata(root, type, type, false);

        final List<Method> methods = new ArrayList<>(List.of(type.getMethods()));

        methods.sort(Comparator.comparing(Method::getName));

        for (Method method : methods) {
            final Subcommand subcommand = method.getAnnotation(Subcommand.class);
            final boolean isDefault = method.isAnnotationPresent(Default.class);

            if (subcommand == null && !isDefault) {
                continue;
            }

            CommandBuilder target = root;
            StringBuilder path = new StringBuilder("/" + root.getName());

            if (subcommand != null) {
                for (String segment : subcommand.value().trim().split(" +")) {
                    final List<String> segmentNames = splitAliases(segment);

                    target = target.child(segmentNames.getFirst());
                    target.aliases(segmentNames.subList(1, segmentNames.size()).toArray(new String[0]));
                    path.append(" ").append(segmentNames.getFirst());
                }
            }

            if (target.hasAction()) {
                throw new IllegalStateException("Two methods in " + type.getSimpleName() + " are mapped to " + path);
            }

            applyMetadata(target, method, type, true);
            configureMethod(target, method, command, path.toString());
        }
        return root.build();
    }

    private void applyMetadata(CommandBuilder builder, AnnotatedElement element, Class<?> owner, boolean method) {
        final Description description = element.getAnnotation(Description.class);

        if (description != null) {
            builder.description(description.value());
        }

        for (CommandPermission permission : element.getAnnotationsByType(CommandPermission.class)) {
            applyPermission(builder, permission.value(), method);
        }

        final Requires requires = element.getAnnotation(Requires.class);

        if (requires == null) {
            return;
        }

        for (String key : requires.value()) {
            final CommandRequirement requirement = manager.getRequirement(key);

            if (requirement == null) {
                throw new IllegalStateException("Unknown requirement \"" + key + "\" used in " + owner.getSimpleName() + ". Register it with registerRequirement(...)");
            }

            if (method) {
                builder.actionRequirement(requirement);
                continue;
            }
            builder.requires(requirement);
        }
    }

    private void applyPermission(CommandBuilder builder, String value, boolean method) {
        if (value.contains("{")) {
            builder.argumentPermission(context -> fillArguments(value, context));
            return;
        }

        final CommandRequirement requirement = PermissionRequirement.of(value);

        if (method) {
            builder.actionRequirement(requirement);
            return;
        }
        builder.requires(requirement);
    }

    private String fillArguments(String template, ExecutionContext context) {
        final Matcher matcher = ARGUMENT_PLACEHOLDER.matcher(template);
        final StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            final String input = context.getInput(matcher.group(1));

            matcher.appendReplacement(builder, Matcher.quoteReplacement(input == null ? "" : input.toLowerCase(Locale.ROOT)));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private void configureMethod(CommandBuilder builder, Method method, BaseCommand command, String path) {
        final Parameter[] parameters = method.getParameters();

        int start = 0;
        Class<?> senderClass = null;

        if (parameters.length > 0 && manager.isSenderType(parameters[0].getType())) {
            senderClass = parameters[0].getType();
            start = 1;

            if (senderClass != manager.getSenderType() && senderClass != CommandActor.class) {
                builder.sender(senderClass);
            }
        }

        final String[] completions = getCompletions(method);
        final List<Argument<?>> arguments = new ArrayList<>();
        final Set<String> usedNames = new HashSet<>();

        for (int i = start; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final String name = uniqueName(argumentName(parameter), usedNames);
            final Argument<?> argument = createArgument(name, parameter, path);
            final int completionIndex = i - start;
            final String completion = completionIndex < completions.length ? completions[completionIndex] : null;

            builder.argument(createCommandArgument(argument, parameter, completion));
            arguments.add(argument);
        }

        final MethodHandle handle = createHandle(method, command);
        final Class<?> sender = senderClass;

        builder.executes(context -> invoke(handle, method, sender, arguments, context));
    }

    private CommandArgument<?> createCommandArgument(Argument<?> argument, Parameter parameter, String completion) {
        final Default defaultValue = parameter.getAnnotation(Default.class);
        final boolean optional = parameter.isAnnotationPresent(OptionalArg.class) || defaultValue != null;

        SuggestionProvider suggestions = null;
        String completionId = null;

        if (completion != null && !completion.isEmpty()) {
            if (completion.equalsIgnoreCase("@nothing")) {
                suggestions = SuggestionProvider.NONE;

            } else if (completion.startsWith("@")) {
                completionId = completion.substring(1);

            } else {
                suggestions = SuggestionProvider.of(completion.split("\\|"));
            }
        }

        return new CommandArgument<>((Argument<Object>) argument, optional, null,
                defaultValue == null ? null : defaultValue.value(), suggestions, completionId);
    }

    private Argument<?> createArgument(String name, Parameter parameter, String path) {
        final Class<?> type = Primitives.wrap(parameter.getType());

        if (parameter.isAnnotationPresent(Join.class)) {
            if (type != String.class) {
                throw new IllegalStateException("@Join only works on String parameters (<" + name + "> in " + path + ")");
            }
            return Argument.of(name, ArgumentParsers.greedyString());
        }

        final Range range = parameter.getAnnotation(Range.class);

        if (range == null) {
            return Argument.of(name, (Class<Object>) type);
        }

        final ArgumentParser<?> parser = rangedParser(type, range);

        if (parser == null) {
            throw new IllegalStateException("@Range only works on number parameters (<" + name + "> in " + path + ")");
        }
        return Argument.of(name, (ArgumentParser<Object>) parser);
    }

    private ArgumentParser<?> rangedParser(Class<?> type, Range range) {
        if (type == Integer.class) {
            return ArgumentParsers.integer(clampInt(range.min()), clampInt(range.max()));
        }

        if (type == Long.class) {
            return ArgumentParsers.longs(clampLong(range.min()), clampLong(range.max()));
        }

        if (type == Double.class) {
            return ArgumentParsers.doubles(range.min(), range.max());
        }

        if (type == Float.class) {
            return ArgumentParsers.floats((float) Math.max(-Float.MAX_VALUE, range.min()), (float) Math.min(Float.MAX_VALUE, range.max()));
        }
        return null;
    }

    private void invoke(MethodHandle handle, Method method, Class<?> sender, List<Argument<?>> arguments, ExecutionContext context) {
        final Class<?>[] types = method.getParameterTypes();
        final Object[] values = new Object[types.length];

        int index = 0;

        if (sender != null) {
            values[index++] = context.getSender(sender);
        }

        for (Argument<?> argument : arguments) {
            Object value = context.get(argument);

            if (value == null && types[index].isPrimitive()) {
                value = Array.get(Array.newInstance(types[index], 1), 0);
            }
            values[index++] = value;
        }

        try {
            handle.invokeWithArguments(values);

        } catch (RuntimeException | Error exception) {
            throw exception;

        } catch (Throwable throwable) {
            throw new IllegalStateException("Command method " + method.getName() + " failed", throwable);
        }
    }

    private MethodHandle createHandle(Method method, BaseCommand command) {
        try {
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method).bindTo(command);

        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot access command method " + method, exception);
        }
    }

    private String[] getCompletions(Method method) {
        final CommandCompletion completion = method.getAnnotation(CommandCompletion.class);

        if (completion == null || completion.value().isBlank()) {
            return new String[0];
        }
        return completion.value().trim().split(" +");
    }

    private static String argumentName(Parameter parameter) {
        final Name name = parameter.getAnnotation(Name.class);

        if (name != null) {
            return name.value();
        }

        if (parameter.isNamePresent()) {
            return parameter.getName();
        }
        return parameter.getType().getSimpleName().toLowerCase(Locale.ROOT);
    }

    private static String uniqueName(String name, Set<String> usedNames) {
        String candidate = name;
        int counter = 2;

        while (!usedNames.add(candidate)) {
            candidate = name + counter++;
        }
        return candidate;
    }

    private static List<String> splitAliases(String value) {
        final List<String> names = new ArrayList<>();

        for (String name : value.split("\\|")) {
            if (name.isBlank()) {
                continue;
            }
            names.add(name.trim());
        }

        if (names.isEmpty()) {
            throw new IllegalStateException("Empty command name in \"" + value + "\"");
        }
        return names;
    }

    private static int clampInt(double value) {
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, value));
    }

    private static long clampLong(double value) {
        return (long) Math.max(Long.MIN_VALUE, Math.min(Long.MAX_VALUE, value));
    }
}
