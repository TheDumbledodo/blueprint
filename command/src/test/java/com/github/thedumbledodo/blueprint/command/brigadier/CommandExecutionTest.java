package com.github.thedumbledodo.blueprint.command.brigadier;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.github.thedumbledodo.blueprint.command.fixture.*;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandExecutionTest {

    private TestCommandManager manager;
    private MinigameCommand command;

    private final TestPlayer player = new TestPlayer("Steve", "minigame.use", "minigame.admin");
    private final TestConsole console = new TestConsole();

    @BeforeEach
    void setUp() {
        this.manager = new TestCommandManager();
        this.command = new MinigameCommand();

        manager.registerRequirement("staff", actor -> actor.getName().startsWith("Staff"));
        manager.registerCompletion("arenas", () -> List.of("desert", "forest", "Dungeon"));
        manager.register(command);
        manager.flush();
    }

    @AfterEach
    void reset() {
        ComponentRegistry.clear();
    }

    @Test
    void runsTheMethodWithParsedArguments() throws CommandSyntaxException {
        manager.execute(player, "minigame join desert 2");

        assertEquals(List.of("join desert 2"), command.getCalls());
    }

    @Test
    void missingOptionalArgumentIsNull() throws CommandSyntaxException {
        manager.execute(player, "minigame join desert");

        assertEquals(List.of("join desert null"), command.getCalls());
    }

    @Test
    void defaultValueIsParsedThroughTheParser() throws CommandSyntaxException {
        manager.execute(player, "minigame team add red");
        manager.execute(player, "minigame team add blue 8");

        assertEquals(List.of("team add red 5", "team add blue 8"), command.getCalls());
    }

    @Test
    void missingOptionalPrimitiveBecomesZero() throws CommandSyntaxException {
        manager.execute(player, "minigame team list");

        assertEquals(List.of("team list 0"), command.getCalls());
    }

    @Test
    void rootAndSubcommandAliasesWork() throws CommandSyntaxException {
        manager.execute(player, "mg j desert");
        manager.execute(player, "minigame j forest");
        manager.execute(player, "mg");

        assertEquals(List.of("join desert null", "join forest null", "help"), command.getCalls());
    }

    @Test
    void greedyStringTakesTheRestOfTheInput() throws CommandSyntaxException {
        manager.execute(player, "minigame say hello there world");

        assertEquals(List.of("say hello there world"), command.getCalls());
    }

    @Test
    void enumArgumentsIgnoreCase() throws CommandSyntaxException {
        manager.execute(player, "minigame difficulty HaRd");

        assertEquals(List.of("difficulty HARD"), command.getCalls());
    }

    @Test
    void invalidEnumTellsTheSenderTheOptions() throws CommandSyntaxException {
        manager.execute(player, "minigame difficulty nightmare");

        assertTrue(command.getCalls().isEmpty());
        assertEquals("nightmare is not valid. Try one of: easy, normal, hard", player.lastMessage());
    }

    @Test
    void rangeIsEnforcedBeforeTheMethodRuns() {
        assertThrows(CommandSyntaxException.class, () -> manager.execute(player, "minigame join desert 9"));
        assertTrue(command.getCalls().isEmpty());
    }

    @Test
    void missingArgumentSendsInvalidSyntax() throws CommandSyntaxException {
        manager.execute(player, "minigame join");
        assertEquals("Invalid command syntax!", player.lastMessage());

        manager.execute(player, "minigame team");
        assertEquals("Invalid command syntax!", player.lastMessage());

        assertTrue(command.getCalls().isEmpty());
    }

    @Test
    void unknownSubcommandSendsUnknownCommand() throws CommandSyntaxException {
        manager.execute(player, "minigame dance");
        assertEquals("There is no command like that.", player.lastMessage());

        manager.execute(player, "minigame team kick bob");
        assertEquals("There is no command like that.", player.lastMessage());

        assertTrue(command.getCalls().isEmpty());
    }

    @Test
    void consoleGetsThePlayerOnlyMessage() throws CommandSyntaxException {
        manager.execute(console, "minigame join desert");

        assertTrue(command.getCalls().isEmpty());
        assertEquals("This can only be done as a player!", console.lastMessage());
    }

    @Test
    void customMessagesAreUsed() throws CommandSyntaxException {
        final CommandMessages messages = new CommandMessages();

        messages.setPlayerOnlyCommand("<red>Players only!");
        manager.setMessages(messages);
        manager.execute(console, "minigame join desert");

        assertEquals("Players only!", console.lastMessage());
    }

    @Test
    void commandExceptionMessageGoesToTheSender() throws CommandSyntaxException {
        final int result = manager.execute(player, "minigame deny");

        assertEquals(0, result);
        assertEquals("Nope x", player.lastMessage());
        assertTrue(manager.getErrors().isEmpty());
    }

    @Test
    void unexpectedExceptionIsReportedAndTheSenderGetsAGenericMessage() throws CommandSyntaxException {
        manager.execute(player, "minigame boom");

        assertEquals(1, manager.getErrors().size());
        assertEquals("boom", manager.getErrors().getFirst().getMessage());
        assertEquals("An error occurred while executing this command!", player.lastMessage());
    }

    @Test
    void actorParameterReceivesTheActor() throws CommandSyntaxException {
        manager.execute(console, "minigame whoami");

        assertEquals(List.of("whoami CONSOLE"), command.getCalls());
    }

    @Test
    void registeredCompletionsAreFilteredByWhatWasTyped() {
        assertEquals(List.of("desert"), manager.suggest(player, "minigame join de"));
        assertEquals(List.of("Dungeon"), manager.suggest(player, "minigame join du"));
    }

    @Test
    void enumAndLiteralCompletions() {
        assertEquals(List.of("easy", "hard", "normal"), manager.suggest(player, "minigame difficulty ").stream().sorted().toList());
        assertEquals(List.of("duo", "solo", "squad"), manager.suggest(player, "minigame mode ").stream().sorted().toList());
    }

    @Test
    void flushIsIdempotent() {
        assertTrue(manager.flush().isEmpty());
        assertEquals(1, manager.getCommands().size());
    }

    @Test
    void installRegistersBaseCommandComponents() throws CommandSyntaxException {
        final TestCommandManager manager = new TestCommandManager();
        final HelpCommand help = new HelpCommand();

        manager.install();
        ComponentRegistry.processRegisteredComponent(HelpCommand.class, help);
        manager.flush();
        manager.execute(new TestPlayer("Alex"), "guide basics");

        assertEquals(List.of("basics"), help.getCalls());
    }
}
