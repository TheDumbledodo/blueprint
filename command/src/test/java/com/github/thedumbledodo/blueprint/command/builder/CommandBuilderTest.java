package com.github.thedumbledodo.blueprint.command.builder;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.github.thedumbledodo.blueprint.command.argument.ArgumentParsers;
import com.github.thedumbledodo.blueprint.command.argument.SuggestionProvider;
import com.github.thedumbledodo.blueprint.command.fixture.Difficulty;
import com.github.thedumbledodo.blueprint.command.fixture.TestCommandManager;
import com.github.thedumbledodo.blueprint.command.fixture.TestConsole;
import com.github.thedumbledodo.blueprint.command.fixture.TestPlayer;
import com.github.thedumbledodo.blueprint.command.model.Argument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandBuilderTest {

    private static final Argument<String> TARGET = Argument.of("target", String.class);
    private static final Argument<Integer> AMOUNT = Argument.of("amount", ArgumentParsers.integer(1, 64));
    private static final Argument<Difficulty> DIFFICULTY = Argument.of("difficulty", Difficulty.class);

    private final List<String> calls = new ArrayList<>();
    private final TestCommandManager manager = new TestCommandManager();
    private final TestPlayer player = new TestPlayer("Steve");

    @BeforeEach
    void setUp() {
        manager.register(CommandBuilder.of("Give")
                .aliases("g")
                .description("Give items")
                .executes(context -> calls.add("give help"))
                .subcommand(CommandBuilder.of("item")
                        .argument(TARGET, SuggestionProvider.of("Steve", "Alex"))
                        .optional(AMOUNT, 1)
                        .executes(context -> calls.add("item " + context.get(TARGET) + " " + context.get(AMOUNT))))
                .subcommand(CommandBuilder.of("mode")
                        .sender(TestPlayer.class)
                        .optional(DIFFICULTY)
                        .executes(context -> calls.add("mode " + context.getOptional(DIFFICULTY).orElse(Difficulty.NORMAL)))));
        manager.flush();
    }

    @Test
    void typedArgumentsReachTheAction() throws CommandSyntaxException {
        manager.execute(player, "give item Alex 32");

        assertEquals(List.of("item Alex 32"), calls);
    }

    @Test
    void optionalArgumentUsesItsDefault() throws CommandSyntaxException {
        manager.execute(player, "give item Alex");

        assertEquals(List.of("item Alex 1"), calls);
    }

    @Test
    void namesAreLowerCasedAndAliasesWork() throws CommandSyntaxException {
        manager.execute(player, "g");
        manager.execute(player, "give");

        assertEquals(List.of("give help", "give help"), calls);
    }

    @Test
    void senderTypeIsEnforced() throws CommandSyntaxException {
        final TestConsole console = new TestConsole();

        manager.execute(console, "give mode hard");
        manager.execute(player, "give mode hard");
        manager.execute(player, "give mode");

        assertEquals(List.of("mode HARD", "mode NORMAL"), calls);
        assertEquals("This can only be done as a player!", console.lastMessage());
    }

    @Test
    void customSuggestionsAreUsed() {
        assertEquals(List.of("Alex"), manager.suggest(player, "give item a"));
    }

    @Test
    void argumentsWithoutAnActionFail() {
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> CommandBuilder.of("broken").argument(TARGET).build());

        assertEquals("/broken declares arguments but has no executes(...)", exception.getMessage());
    }

    @Test
    void duplicateArgumentAndSubcommandNamesFail() {
        assertThrows(IllegalStateException.class, () -> CommandBuilder.of("broken").argument(TARGET).argument(TARGET));
        assertThrows(IllegalStateException.class, () -> CommandBuilder.of("broken")
                .subcommand(CommandBuilder.of("a"))
                .subcommand(CommandBuilder.of("a")));
    }

    @Test
    void namesWithSpacesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> CommandBuilder.of("two words"));
        assertThrows(IllegalArgumentException.class, () -> CommandBuilder.of(""));
    }
}
