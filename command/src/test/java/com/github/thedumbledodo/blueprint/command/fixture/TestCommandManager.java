package com.github.thedumbledodo.blueprint.command.fixture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import com.github.thedumbledodo.blueprint.command.CommandManager;
import com.github.thedumbledodo.blueprint.command.brigadier.RegisteredCommand;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class TestCommandManager extends CommandManager<TestSender> {

    private final CommandDispatcher<TestSender> dispatcher = new CommandDispatcher<>();
    private final List<Throwable> errors = new ArrayList<>();

    private int refreshes;

    public TestCommandManager() {
        super(TestSender.class);
    }

    @Override
    public CommandActor createActor(TestSender source) {
        return new TestActor(source);
    }

    @Override
    public void refresh() {
        refreshes++;
    }

    @Override
    protected void registerCommands(List<RegisteredCommand<TestSender>> commands) {
        for (RegisteredCommand<TestSender> command : commands) {
            dispatcher.getRoot().addChild(command.literal());

            for (String alias : command.node().aliases()) {
                dispatcher.getRoot().addChild(getCompiler().compile(command.node(), alias));
            }
        }
    }

    @Override
    public void handleError(com.github.thedumbledodo.blueprint.command.model.CommandNode node, Throwable throwable) {
        errors.add(throwable);
    }

    public int execute(TestSender sender, String input) throws CommandSyntaxException {
        return dispatcher.execute(input, sender);
    }

    public List<String> suggest(TestSender sender, String input) {
        final ParseResults<TestSender> parse = dispatcher.parse(input, sender);

        return dispatcher.getCompletionSuggestions(parse).join().getList().stream()
                .map(Suggestion::getText)
                .toList();
    }

    public boolean canSee(TestSender sender, String literal) {
        final CommandNode<TestSender> node = dispatcher.getRoot().getChild(literal);

        return node != null && node.canUse(sender);
    }

    public List<String> visibleChildren(TestSender sender, String literal) {
        final CommandNode<TestSender> node = dispatcher.getRoot().getChild(literal);
        final List<String> names = new ArrayList<>();

        for (CommandNode<TestSender> child : node.getChildren()) {
            if (child.canUse(sender)) {
                names.add(child.getName());
            }
        }
        return names;
    }
}
