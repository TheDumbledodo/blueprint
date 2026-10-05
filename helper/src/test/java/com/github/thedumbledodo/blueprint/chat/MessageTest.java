package com.github.thedumbledodo.blueprint.chat;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void componentIsParsedOnceAndReused() {
        final Message message = Message.of("<green>Saved");

        assertSame(message.getComponent(), message.component());
        assertSame(message.component(), message.component());
    }

    @Test
    void resolversFillPlaceholders() {
        final Message message = Message.of("<gray>Hi <player>");

        assertEquals("§7Hi Steve", Text.translateToLegacyString(message.component(Placeholder.unparsed("player", "Steve"))));
    }

    @Test
    void nullBecomesAnEmptyMessage() {
        final Message message = new Message(null);

        assertTrue(message.isEmpty());
        assertEquals("", message.getRaw());
    }

    @Test
    void equalityAndToStringUseTheRawText() {
        assertEquals(Message.of("<red>x"), Message.of("<red>x"));
        assertEquals("<red>x", Message.of("<red>x").toString());
    }

    @Test
    void legacyUsesSectionCodes() {
        assertEquals("§cx", Message.of("<red>x").legacy());
    }
}
