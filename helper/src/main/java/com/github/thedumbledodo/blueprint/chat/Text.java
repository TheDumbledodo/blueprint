package com.github.thedumbledodo.blueprint.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class Text {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final MiniMessage UNTRUSTED_MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.gradient(),
                    StandardTags.rainbow(),
                    StandardTags.reset()))
            .build();

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static Component translate(String text) {
        if (text == null) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text);
    }

    public static Component translate(String text, TagResolver... resolvers) {
        if (text == null) {
            return Component.empty();
        }

        if (resolvers == null || resolvers.length == 0) {
            return translate(text);
        }
        return MINI_MESSAGE.deserialize(text, resolvers);
    }

    public static Component translateUntrusted(String text) {
        if (text == null) {
            return Component.empty();
        }
        return UNTRUSTED_MINI_MESSAGE.deserialize(text);
    }

    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        return MINI_MESSAGE.escapeTags(text);
    }

    public static String translateToLegacyString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(translate(input));
    }

    public static String translateToLegacyString(Component component) {
        if (component == null) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(component);
    }

    public static Component[] translate(String... inputs) {
        if (inputs == null || inputs.length == 0) {
            return new Component[0];
        }

        final Component[] components = new Component[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            components[i] = translate(inputs[i]);
        }
        return components;
    }

    public static List<Component> translate(List<String> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        final List<Component> toReturn = new ArrayList<>(list.size());

        for (String text : list) {
            toReturn.add(translate(text));
        }
        return toReturn;
    }

    public static List<Component> translate(List<String> list, TagResolver... resolvers) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        final List<Component> toReturn = new ArrayList<>(list.size());

        for (String text : list) {
            toReturn.add(translate(text, resolvers));
        }
        return toReturn;
    }

    public static String translateToMiniMessage(Component component) {
        if (component == null) {
            return "";
        }
        return MINI_MESSAGE.serialize(component);
    }

    public static List<String> translateToMiniMessage(List<Component> components) {
        if (components == null || components.isEmpty()) {
            return List.of();
        }

        final List<String> list = new ArrayList<>(components.size());

        for (Component component : components) {
            list.add(translateToMiniMessage(component));
        }
        return list;
    }

    public static String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();
        final String[] words = text.trim().split(" +");

        for (String word : words) {
            builder.append(word.substring(0, 1).toUpperCase());
            builder.append(word.substring(1).toLowerCase());
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}
