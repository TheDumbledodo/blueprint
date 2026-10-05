package com.github.thedumbledodo.blueprint.command.model;

import com.github.thedumbledodo.blueprint.command.context.ExecutionContext;

@FunctionalInterface
public interface CommandAction {

    void execute(ExecutionContext context);
}
