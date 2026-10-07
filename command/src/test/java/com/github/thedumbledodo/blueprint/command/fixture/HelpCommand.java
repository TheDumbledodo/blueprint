package com.github.thedumbledodo.blueprint.command.fixture;

import com.github.thedumbledodo.blueprint.command.annotation.Command;
import com.github.thedumbledodo.blueprint.command.annotation.CommandPermission;
import com.github.thedumbledodo.blueprint.command.annotation.Default;
import com.github.thedumbledodo.blueprint.command.annotation.Subcommand;
import com.github.thedumbledodo.blueprint.command.model.BaseCommand;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Command("guide")
public final class HelpCommand extends BaseCommand {

    private final List<String> calls = new ArrayList<>();

    @Default
    @CommandPermission("guide.full")
    public void full(TestSender sender) {
        calls.add("full");
    }

    @Subcommand("basics")
    public void basics(TestSender sender) {
        calls.add("basics");
    }
}
