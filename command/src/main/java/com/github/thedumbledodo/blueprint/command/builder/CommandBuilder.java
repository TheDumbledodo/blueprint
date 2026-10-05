package com.github.thedumbledodo.blueprint.command.builder;

import com.github.thedumbledodo.blueprint.command.argument.SuggestionProvider;
import com.github.thedumbledodo.blueprint.command.model.Argument;
import com.github.thedumbledodo.blueprint.command.model.CommandAction;
import com.github.thedumbledodo.blueprint.command.model.CommandArgument;
import com.github.thedumbledodo.blueprint.command.model.CommandNode;
import com.github.thedumbledodo.blueprint.command.requirement.ArgumentPermission;
import com.github.thedumbledodo.blueprint.command.requirement.CommandRequirement;
import com.github.thedumbledodo.blueprint.command.requirement.PermissionRequirement;
import lombok.Getter;

import java.util.*;
import java.util.function.Supplier;

public final class CommandBuilder {

    @Getter
    private final String name;

    private final Set<String> aliases = new LinkedHashSet<>();
    private String description = "";

    private final List<CommandRequirement> requirements = new ArrayList<>();
    private final List<CommandRequirement> actionRequirements = new ArrayList<>();
    private final List<ArgumentPermission> argumentPermissions = new ArrayList<>();

    private final List<CommandArgument<?>> arguments = new ArrayList<>();
    private final Map<String, CommandBuilder> children = new LinkedHashMap<>();

    private CommandAction action;
    private Class<?> senderType;

    private CommandBuilder(String name) {
        this.name = validateName(name);
    }

    public static CommandBuilder of(String name) {
        return new CommandBuilder(name);
    }

    public CommandBuilder aliases(String... aliases) {
        for (String alias : aliases) {
            this.aliases.add(validateName(alias));
        }
        return this;
    }

    public CommandBuilder description(String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    public CommandBuilder permission(String permission) {
        return requires(PermissionRequirement.of(permission));
    }

    public CommandBuilder permission(Supplier<String> permission) {
        return requires(new PermissionRequirement(permission));
    }

    public CommandBuilder requires(CommandRequirement requirement) {
        requirements.add(Objects.requireNonNull(requirement, "requirement"));
        return this;
    }

    public CommandBuilder actionRequirement(CommandRequirement requirement) {
        actionRequirements.add(Objects.requireNonNull(requirement, "requirement"));
        return this;
    }

    public CommandBuilder argumentPermission(ArgumentPermission permission) {
        argumentPermissions.add(Objects.requireNonNull(permission, "permission"));
        return this;
    }

    public CommandBuilder sender(Class<?> senderType) {
        this.senderType = senderType;
        return this;
    }

    public <T> CommandBuilder argument(Argument<T> argument) {
        return argument(CommandArgument.required(argument));
    }

    public <T> CommandBuilder argument(Argument<T> argument, SuggestionProvider suggestions) {
        return argument(CommandArgument.required(argument).withSuggestions(suggestions));
    }

    public <T> CommandBuilder optional(Argument<T> argument) {
        return argument(CommandArgument.optional(argument, null));
    }

    public <T> CommandBuilder optional(Argument<T> argument, T defaultValue) {
        return argument(CommandArgument.optional(argument, defaultValue));
    }

    public CommandBuilder argument(CommandArgument<?> argument) {
        for (CommandArgument<?> existing : arguments) {
            if (existing.getName().equals(argument.getName())) {
                throw new IllegalStateException("Argument <" + argument.getName() + "> is declared twice in /" + name);
            }
        }

        arguments.add(argument);
        return this;
    }

    public CommandBuilder executes(CommandAction action) {
        this.action = Objects.requireNonNull(action, "action");
        return this;
    }

    public CommandBuilder subcommand(CommandBuilder child) {
        if (children.containsKey(child.getName())) {
            throw new IllegalStateException("Subcommand " + child.getName() + " is declared twice in /" + name);
        }

        children.put(child.getName(), child);
        return this;
    }

    public CommandBuilder child(String name) {
        return children.computeIfAbsent(validateName(name), CommandBuilder::new);
    }

    public boolean hasAction() {
        return action != null;
    }

    public CommandNode build() {
        validateArguments();

        final List<CommandRequirement> nodeRequirements = new ArrayList<>(requirements);
        final List<CommandRequirement> executionRequirements = new ArrayList<>();

        if (children.isEmpty()) {
            nodeRequirements.addAll(actionRequirements);

        } else {
            executionRequirements.addAll(actionRequirements);
        }

        final List<CommandNode> builtChildren = new ArrayList<>(children.size());

        for (CommandBuilder child : children.values()) {
            builtChildren.add(child.build());
        }

        return new CommandNode(name, new ArrayList<>(aliases), description,
                nodeRequirements, executionRequirements, argumentPermissions,
                arguments, action, senderType, builtChildren);
    }

    private void validateArguments() {
        if (!arguments.isEmpty() && action == null) {
            throw new IllegalStateException("/" + name + " declares arguments but has no executes(...)");
        }

        boolean optionalSeen = false;

        for (CommandArgument<?> argument : arguments) {
            if (argument.optional()) {
                optionalSeen = true;
                continue;
            }

            if (optionalSeen) {
                throw new IllegalStateException("Required argument <" + argument.getName() + "> cannot follow an optional argument in /" + name);
            }
        }
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank() || name.contains(" ")) {
            throw new IllegalArgumentException("command names cannot be empty or contain spaces: \"" + name + "\"");
        }
        return name.toLowerCase(Locale.ROOT);
    }
}
