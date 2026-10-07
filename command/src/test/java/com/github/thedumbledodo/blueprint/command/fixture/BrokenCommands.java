package com.github.thedumbledodo.blueprint.command.fixture;

import com.github.thedumbledodo.blueprint.command.annotation.*;
import com.github.thedumbledodo.blueprint.command.model.BaseCommand;

public final class BrokenCommands {

    public static final class NoAlias extends BaseCommand {

        @Default
        public void run(TestSender sender) {
        }
    }

    @Command("broken")
    public static final class OptionalBeforeRequired extends BaseCommand {

        @Subcommand("run")
        public void run(TestSender sender, @OptionalArg String first, String second) {
        }
    }

    @Command("broken")
    public static final class DuplicatePath extends BaseCommand {

        @Subcommand("run")
        public void first(TestSender sender) {
        }

        @Subcommand("run")
        public void second(TestSender sender) {
        }
    }

    @Command("broken")
    public static final class UnknownCompletion extends BaseCommand {

        @Subcommand("run")
        @CommandCompletion("@missing")
        public void run(TestSender sender, String value) {
        }
    }

    @Command("broken")
    public static final class UnknownRequirement extends BaseCommand {

        @Subcommand("run")
        @Requires("missing")
        public void run(TestSender sender) {
        }
    }

    @Command("broken")
    public static final class JoinNumber extends BaseCommand {

        @Subcommand("run")
        public void run(TestSender sender, @Join int value) {
        }
    }

    @Command("broken")
    public static final class UnknownParser extends BaseCommand {

        @Subcommand("run")
        public void run(TestSender sender, Thread thread) {
        }
    }
}
