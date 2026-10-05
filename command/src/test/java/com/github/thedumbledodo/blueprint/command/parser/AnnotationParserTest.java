package com.github.thedumbledodo.blueprint.command.parser;

import com.github.thedumbledodo.blueprint.command.fixture.*;
import com.github.thedumbledodo.blueprint.command.model.CommandArgument;
import com.github.thedumbledodo.blueprint.command.model.CommandNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationParserTest {

    private final TestCommandManager manager = new TestCommandManager();
    private final AnnotationParser parser = new AnnotationParser(manager);

    private CommandNode parseMinigame() {
        manager.registerRequirement("staff", actor -> true);
        return parser.parse(new MinigameCommand());
    }

    @Test
    void rootNameAliasesAndDescriptionComeFromTheClass() {
        final CommandNode root = parseMinigame();

        assertEquals("minigame", root.name());
        assertEquals(List.of("mg"), root.aliases());
        assertEquals("Minigame commands", root.description());
        assertTrue(root.isExecutable());
        assertEquals(1, root.requirements().size());
    }

    @Test
    void subcommandPathsAndAliasesBuildNestedNodes() {
        final CommandNode root = parseMinigame();
        final CommandNode team = root.getChild("team").orElseThrow();

        assertFalse(team.isExecutable());
        assertTrue(team.getChild("add").isPresent());
        assertTrue(team.getChild("list").isPresent());
        assertEquals(List.of("j"), root.getChild("join").orElseThrow().aliases());
        assertSame(root.getChild("join").orElseThrow(), root.getChild("j").orElseThrow());
    }

    @Test
    void senderParameterIsNotAnArgument() {
        final CommandNode join = parseMinigame().getChild("join").orElseThrow();
        final List<String> names = join.arguments().stream().map(CommandArgument::getName).toList();

        assertEquals(List.of("arena", "team"), names);
        assertEquals(TestPlayer.class, join.senderType());
    }

    @Test
    void baseSenderTypeDoesNotRestrictTheSender() {
        assertNull(parseMinigame().getChild("reload").orElseThrow().senderType());
        assertNull(parseMinigame().getChild("whoami").orElseThrow().senderType());
    }

    @Test
    void optionalAndDefaultParametersAreOptionalArguments() {
        final CommandNode root = parseMinigame();
        final CommandArgument<?> team = root.getChild("join").orElseThrow().arguments().get(1);
        final CommandArgument<?> size = root.getChild("team").orElseThrow().getChild("add").orElseThrow().arguments().get(1);

        assertTrue(team.optional());
        assertTrue(size.optional());
        assertEquals("5", size.defaultInput());
    }

    @Test
    void nameAnnotationOverridesTheParameterName() {
        final CommandNode arenaJoin = parseMinigame().getChild("arena").orElseThrow().getChild("join").orElseThrow();

        assertEquals("arena", arenaJoin.arguments().getFirst().getName());
        assertEquals(1, arenaJoin.argumentPermissions().size());
    }

    @Test
    void completionTokensAreAssignedByPosition() {
        final CommandNode root = parseMinigame();

        assertEquals("arenas", root.getChild("join").orElseThrow().arguments().getFirst().completionId());
        assertNotNull(root.getChild("mode").orElseThrow().arguments().getFirst().suggestions());
    }

    @Test
    void methodPermissionOnALeafHidesTheLeaf() {
        final CommandNode reload = parseMinigame().getChild("reload").orElseThrow();

        assertEquals(1, reload.requirements().size());
        assertTrue(reload.executionRequirements().isEmpty());
    }

    @Test
    void defaultMethodPermissionOnlyGuardsTheDefaultAction() {
        final CommandNode root = parser.parse(new HelpCommand());

        assertTrue(root.requirements().isEmpty());
        assertEquals(1, root.executionRequirements().size());
    }

    @Test
    void missingCommandAliasFails() {
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parser.parse(new BrokenCommands.NoAlias()));

        assertTrue(exception.getMessage().contains("@CommandAlias"));
    }

    @Test
    void requiredAfterOptionalFails() {
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parser.parse(new BrokenCommands.OptionalBeforeRequired()));

        assertEquals("Required argument <second> cannot follow an optional argument in /run", exception.getMessage());
    }

    @Test
    void twoMethodsOnOnePathFail() {
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parser.parse(new BrokenCommands.DuplicatePath()));

        assertTrue(exception.getMessage().contains("/broken run"));
    }

    @Test
    void unknownRequirementFails() {
        assertThrows(IllegalStateException.class, () -> parser.parse(new BrokenCommands.UnknownRequirement()));
    }

    @Test
    void joinOnANumberFails() {
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parser.parse(new BrokenCommands.JoinNumber()));

        assertTrue(exception.getMessage().startsWith("@Join only works on String parameters"));
    }

    @Test
    void unknownCompletionFailsWhenRegistered() {
        manager.register(new BrokenCommands.UnknownCompletion());

        final IllegalStateException exception = assertThrows(IllegalStateException.class, manager::flush);

        assertTrue(exception.getMessage().contains("@missing"));
    }

    @Test
    void unknownParserFailsWithTheArgumentName() {
        manager.register(new BrokenCommands.UnknownParser());

        final IllegalStateException exception = assertThrows(IllegalStateException.class, manager::flush);

        assertTrue(exception.getMessage().contains("java.lang.Thread"));
        assertTrue(exception.getMessage().contains("<thread>"));
    }
}
