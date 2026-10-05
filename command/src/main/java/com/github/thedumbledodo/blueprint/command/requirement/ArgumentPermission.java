package com.github.thedumbledodo.blueprint.command.requirement;

import com.github.thedumbledodo.blueprint.command.context.ExecutionContext;

@FunctionalInterface
public interface ArgumentPermission {

    String resolve(ExecutionContext context);
}
