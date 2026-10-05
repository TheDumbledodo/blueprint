package com.github.thedumbledodo.blueprint.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextTest {

    @Test
    void nullInputsGiveEmptyValues() {
        assertEquals(Component.empty(), Text.translate((String) null));
        assertEquals(Component.empty(), Text.translate((String) null, Placeholder.unparsed("name", "Steve")));
        assertEquals("", Text.translateToLegacyString((String) null));
        assertEquals("", Text.translateToMiniMessage((Component) null));
        assertEquals(List.of(), Text.translate((List<String>) null));
        assertEquals(0, Text.translate((String[]) null).length);
        assertEquals("", Text.escape(null));
    }

    @Test
    void miniMessageIsParsed() {
        assertEquals("§cHello", Text.translateToLegacyString("<red>Hello"));
    }

    @Test
    void unparsedPlaceholderIsInsertedAsLiteralText() {
        final Component component = Text.translate("<gray>Hi <name>", Placeholder.unparsed("name", "<red>Steve"));

        assertEquals("§7Hi <red>Steve", Text.translateToLegacyString(component));
    }

    @Test
    void untrustedTextKeepsColoursButNotClickEvents() {
        final Component component = Text.translateUntrusted("<click:run_command:/op me><red>Hi");

        assertFalse(hasClickEvent(component));
        assertTrue(Text.translateToLegacyString(component).contains("§cHi"));
    }

    @Test
    void trustedTextDoesCreateClickEvents() {
        assertTrue(hasClickEvent(Text.translate("<click:run_command:/spawn>Spawn")));
    }

    @Test
    void escapedTagsStayAsText() {
        assertEquals("<red>x", Text.translateToLegacyString(Text.escape("<red>x")));
    }

    @Test
    void listTranslationKeepsOrder() {
        final List<Component> components = Text.translate(List.of("<red>a", "<green>b"));

        assertEquals("§ca", Text.translateToLegacyString(components.get(0)));
        assertEquals("§ab", Text.translateToLegacyString(components.get(1)));
    }

    @Test
    void miniMessageRoundTrip() {
        final Component component = Text.translate("<green>Saved");

        assertEquals(component, Text.translate(Text.translateToMiniMessage(component)));
    }

    @ParameterizedTest
    @CsvSource({
            "hello world, Hello World",
            "'  hello   world ', Hello World",
            "HELLO, Hello",
            "a, A"
    })
    void capitalizeHandlesOddSpacing(String input, String expected) {
        assertEquals(expected, Text.capitalize(input));
    }

    @Test
    void capitalizeOfBlankIsEmpty() {
        assertEquals("", Text.capitalize("   "));
        assertEquals("", Text.capitalize(null));
    }

    private boolean hasClickEvent(Component component) {
        if (component.clickEvent() != null) {
            return true;
        }

        for (Component child : component.children()) {
            if (hasClickEvent(child)) {
                return true;
            }
        }
        return false;
    }
}
