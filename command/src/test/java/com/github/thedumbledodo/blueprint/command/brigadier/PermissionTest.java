package com.github.thedumbledodo.blueprint.command.brigadier;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.github.thedumbledodo.blueprint.command.builder.CommandBuilder;
import com.github.thedumbledodo.blueprint.command.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PermissionTest {

    private TestCommandManager manager;
    private MinigameCommand command;

    @BeforeEach
    void setUp() {
        this.manager = new TestCommandManager();
        this.command = new MinigameCommand();

        manager.registerRequirement("staff", actor -> actor.getName().startsWith("Staff"));
        manager.registerCompletion("arenas", () -> List.of("desert"));
        manager.register(command);
        manager.register(new HelpCommand());
        manager.flush();
    }

    @Test
    void classPermissionHidesTheWholeCommand() {
        final TestPlayer stranger = new TestPlayer("Stranger");

        assertFalse(manager.canSee(stranger, "minigame"));
        assertThrows(CommandSyntaxException.class, () -> manager.execute(stranger, "minigame"));
    }

    @Test
    void methodPermissionHidesOnlyThatSubcommand() {
        final TestPlayer player = new TestPlayer("Steve", "minigame.use");
        final List<String> visible = manager.visibleChildren(player, "minigame");

        assertFalse(visible.contains("reload"));
        assertTrue(visible.contains("join"));
        assertThrows(CommandSyntaxException.class, () -> manager.execute(player, "minigame reload"));
    }

    @Test
    void defaultMethodPermissionDoesNotHideSubcommands() throws CommandSyntaxException {
        final TestPlayer player = new TestPlayer("Steve");

        assertTrue(manager.canSee(player, "guide"));

        manager.execute(player, "guide basics");
        manager.execute(player, "guide");

        assertEquals("No permission to execute this command.", player.lastMessage());
    }

    @Test
    void argumentPermissionUsesTheTypedValue() throws CommandSyntaxException {
        final TestPlayer allowed = new TestPlayer("Steve", "minigame.use", "minigame.join.desert");
        final TestPlayer denied = new TestPlayer("Alex", "minigame.use", "minigame.join.forest");

        manager.execute(allowed, "minigame arena join Desert");
        manager.execute(denied, "minigame arena join desert");

        assertEquals(List.of("arena join Desert"), command.getCalls());
        assertEquals("No permission to execute this command.", denied.lastMessage());
        assertTrue(manager.visibleChildren(denied, "minigame").contains("arena"));
    }

    @Test
    void requirementHidesTheNode() {
        assertTrue(manager.visibleChildren(new TestPlayer("StaffBob", "minigame.use"), "minigame").contains("staff"));
        assertFalse(manager.visibleChildren(new TestPlayer("Bob", "minigame.use"), "minigame").contains("staff"));
    }

    @Test
    void builderPermissionSupplierIsLive() throws CommandSyntaxException {
        final AtomicReference<String> permission = new AtomicReference<>("spawn.use");
        final List<String> calls = new ArrayList<>();
        final TestPlayer player = new TestPlayer("Steve", "spawn.use");

        manager.register(CommandBuilder.of("spawn")
                .permission(permission::get)
                .executes(context -> calls.add("spawn")));
        manager.flush();

        manager.execute(player, "spawn");
        permission.set("spawn.vip");

        assertFalse(manager.canSee(player, "spawn"));
        assertEquals(List.of("spawn"), calls);
    }

    @Test
    void visibleNodesSendTheNoPermissionMessageWhenNotHidden() throws CommandSyntaxException {
        final TestCommandManager manager = new TestCommandManager();
        final List<String> calls = new ArrayList<>();
        final TestPlayer player = new TestPlayer("Steve");

        manager.setHideUnpermitted(false);
        manager.register(CommandBuilder.of("secret")
                .permission("secret.use")
                .executes(context -> calls.add("secret")));
        manager.flush();

        assertTrue(manager.canSee(player, "secret"));

        manager.execute(player, "secret");

        assertTrue(calls.isEmpty());
        assertEquals("No permission to execute this command.", player.lastMessage());
    }
}
