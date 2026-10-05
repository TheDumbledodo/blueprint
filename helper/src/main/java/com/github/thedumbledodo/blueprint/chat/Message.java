package com.github.thedumbledodo.blueprint.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Getter
@EqualsAndHashCode(of = "raw")
public final class Message {

    private final String raw;
    private final Component component;

    public Message(String raw) {
        this.raw = raw == null ? "" : raw;
        this.component = Text.translate(this.raw);
    }

    public static Message of(String raw) {
        return new Message(raw);
    }

    public Component component(TagResolver... resolvers) {
        if (resolvers == null || resolvers.length == 0) {
            return component;
        }
        return Text.translate(raw, resolvers);
    }

    public String legacy() {
        return Text.translateToLegacyString(raw);
    }

    public boolean isEmpty() {
        return raw.isEmpty();
    }

    @Override
    public String toString() {
        return raw;
    }
}
