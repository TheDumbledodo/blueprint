package com.github.thedumbledodo.blueprint.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.github.thedumbledodo.blueprint.chat.Text;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.fixture.Difficulty;
import com.github.thedumbledodo.blueprint.command.fixture.TestActor;
import com.github.thedumbledodo.blueprint.command.fixture.TestCommandManager;
import com.github.thedumbledodo.blueprint.command.fixture.TestPlayer;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentParsersTest {

    private final CommandActor actor = new TestActor(new TestPlayer("Steve"));

    @Test
    void uuidParsesValidInput() {
        final UUID uuid = UUID.randomUUID();

        assertEquals(uuid, ArgumentParsers.uuid().parse(actor, uuid.toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-uuid", "1234", "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz"})
    void uuidRejectsGarbageWithTheInput(String input) {
        final CommandException exception = assertThrows(CommandException.class, () -> ArgumentParsers.uuid().parse(actor, input));
        final String rendered = Text.translateToLegacyString(exception.render(new CommandMessages())).replaceAll("§.", "");

        assertEquals("Invalid value " + input + ".", rendered);
    }

    @ParameterizedTest
    @CsvSource({"easy, EASY", "HARD, HARD", "Normal, NORMAL"})
    void enumParsingIgnoresCase(String input, Difficulty expected) {
        assertEquals(expected, ArgumentParsers.enumeration(Difficulty.class).parse(actor, input));
    }

    @Test
    void enumSuggestsLowerCaseNames() {
        assertEquals(List.of("easy", "normal", "hard"), ArgumentParsers.enumeration(Difficulty.class).suggest(actor, ""));
    }

    @ParameterizedTest
    @CsvSource({"1, true", "4, true", "0, false", "5, false", "abc, false"})
    void integerRangeIsValidatedByTheNativeType(String input, boolean valid) {
        final StringReader reader = new StringReader(input);

        if (valid) {
            assertDoesNotThrow(() -> ArgumentParsers.integer(1, 4).getNativeType().parse(reader));
            return;
        }
        assertThrows(CommandSyntaxException.class, () -> ArgumentParsers.integer(1, 4).getNativeType().parse(reader));
    }

    @Test
    void greedyStringReadsTheRest() throws CommandSyntaxException {
        assertEquals("hello there", ArgumentParsers.greedyString().getNativeType().parse(new StringReader("hello there")));
    }

    @Test
    void managerResolvesPrimitivesEnumsAndCustomParsers() {
        final TestCommandManager manager = new TestCommandManager();
        final ArgumentParser<StringBuilder> custom = ArgumentParsers.simple(ArgumentParsers.word().getNativeType(), input -> new StringBuilder((String) input));

        manager.registerParser(StringBuilder.class, custom);

        assertSame(manager.getParser(int.class), manager.getParser(Integer.class));
        assertSame(manager.getParser(Difficulty.class), manager.getParser(Difficulty.class));
        assertSame(custom, manager.getParser(StringBuilder.class));
        assertNull(manager.getParser(Thread.class));
    }
}
