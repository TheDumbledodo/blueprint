package com.github.thedumbledodo.blueprint.command.requirement;

import com.github.thedumbledodo.blueprint.command.model.CommandActor;

import java.util.List;

@FunctionalInterface
public interface CommandRequirement {

    boolean test(CommandActor actor);

    static boolean testAll(List<CommandRequirement> requirements, CommandActor actor) {
        for (CommandRequirement requirement : requirements) {
            if (requirement.test(actor)) {
                continue;
            }
            return false;
        }
        return true;
    }
}
