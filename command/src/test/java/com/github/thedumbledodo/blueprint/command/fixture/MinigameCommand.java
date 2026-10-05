package com.github.thedumbledodo.blueprint.command.fixture;

import com.github.thedumbledodo.blueprint.command.annotation.*;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.model.BaseCommand;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.List;

@Getter
@CommandAlias("minigame|mg")
@CommandPermission("minigame.use")
@Description("Minigame commands")
public final class MinigameCommand extends BaseCommand {

    private final List<String> calls = new ArrayList<>();

    @Default
    public void help(TestSender sender) {
        calls.add("help");
    }

    @Subcommand("join|j")
    @CommandCompletion("@arenas")
    public void join(TestPlayer player, String arena, @Optional @Range(min = 1, max = 4) Integer team) {
        calls.add("join " + arena + " " + team);
    }

    @Subcommand("reload")
    @CommandPermission("minigame.reload")
    public void reload(TestSender sender) {
        calls.add("reload");
    }

    @Subcommand("team add")
    public void teamAdd(TestSender sender, String name, @Default("5") int size) {
        calls.add("team add " + name + " " + size);
    }

    @Subcommand("team list")
    public void teamList(TestSender sender, @Optional int page) {
        calls.add("team list " + page);
    }

    @Subcommand("say")
    public void say(TestSender sender, @Join String message) {
        calls.add("say " + message);
    }

    @Subcommand("difficulty")
    public void difficulty(TestSender sender, Difficulty difficulty) {
        calls.add("difficulty " + difficulty);
    }

    @Subcommand("admin")
    @CommandPermission("minigame.admin")
    public void admin(TestSender sender) {
        calls.add("admin");
    }

    @Subcommand("arena join")
    @CommandPermission("minigame.join.{arena}")
    public void arenaJoin(TestPlayer player, @Name("arena") String arenaName) {
        calls.add("arena join " + arenaName);
    }

    @Subcommand("staff")
    @Requires("staff")
    public void staff(TestSender sender) {
        calls.add("staff");
    }

    @Subcommand("mode")
    @CommandCompletion("solo|duo|squad")
    public void mode(TestSender sender, String mode) {
        calls.add("mode " + mode);
    }

    @Subcommand("boom")
    public void boom(TestSender sender) {
        throw new IllegalStateException("boom");
    }

    @Subcommand("deny")
    public void deny(TestSender sender) {
        throw new CommandException("<red>Nope <value>", Placeholder.unparsed("value", "x"));
    }

    @Subcommand("whoami")
    public void whoami(com.github.thedumbledodo.blueprint.command.model.CommandActor actor) {
        calls.add("whoami " + actor.getName());
    }
}
