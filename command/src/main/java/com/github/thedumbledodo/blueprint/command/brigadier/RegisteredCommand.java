package com.github.thedumbledodo.blueprint.command.brigadier;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.github.thedumbledodo.blueprint.command.model.CommandNode;

public record RegisteredCommand<S>(CommandNode node, LiteralCommandNode<S> literal) {

    public String getName() {
        return node.name();
    }
}
