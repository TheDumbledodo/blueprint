package com.github.thedumbledodo.blueprint.command.model;

import com.github.thedumbledodo.blueprint.command.requirement.ArgumentPermission;
import com.github.thedumbledodo.blueprint.command.requirement.CommandRequirement;

import java.util.List;
import java.util.Optional;

public record CommandNode(String name, List<String> aliases, String description, List<CommandRequirement> requirements,
                          List<CommandRequirement> executionRequirements, List<ArgumentPermission> argumentPermissions,
                          List<CommandArgument<?>> arguments, CommandAction action, Class<?> senderType,
                          List<CommandNode> children) {

    public CommandNode(String name, List<String> aliases, String description,
                       List<CommandRequirement> requirements,
                       List<CommandRequirement> executionRequirements,
                       List<ArgumentPermission> argumentPermissions,
                       List<CommandArgument<?>> arguments,
                       CommandAction action,
                       Class<?> senderType,
                       List<CommandNode> children) {
        this.name = name;
        this.aliases = List.copyOf(aliases);
        this.description = description;

        this.requirements = List.copyOf(requirements);
        this.executionRequirements = List.copyOf(executionRequirements);
        this.argumentPermissions = List.copyOf(argumentPermissions);

        this.arguments = List.copyOf(arguments);
        this.action = action;
        this.senderType = senderType;

        this.children = List.copyOf(children);
    }

    public boolean isExecutable() {
        return action != null;
    }

    public Optional<CommandNode> getChild(String name) {
        for (CommandNode child : children) {
            if (child.name().equals(name) || child.aliases().contains(name)) {
                return Optional.of(child);
            }
        }
        return Optional.empty();
    }
}
