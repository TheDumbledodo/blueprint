package io.github.dumbledodo.blueprint.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class Text {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static Component translate(String text) {
        if (text == null) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text);
    }

    public static String translateToLegacyString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(Text.translate(input));
    }

    public static Component[] translate(String... inputs) {
        final List<Component> list = new ArrayList<>();

        for (String input : inputs) {
            list.add(translate(input));
        }
        return list.toArray(new Component[0]);
    }

    public static List<Component> translate(List<String> list) {
        final List<Component> toReturn = new ArrayList<>();
        for (String text : list) {
            toReturn.add(translate(text));
        }
        return toReturn;
    }

    public static String translateToMiniMessage(Component component) {
        return MINI_MESSAGE.serialize(component);
    }

    public static List<String> translateToMiniMessage(List<Component> components) {
        final List<String> list = new ArrayList<>();

        for (Component component : components) {
            list.add(translateToMiniMessage(component));
        }
        return list;
    }

    public static String capitalize(String text) {
        final StringBuilder builder = new StringBuilder();
        final String[] words = text.split(" +");

        for (String word : words) {
            builder.append(word.substring(0, 1).toUpperCase());
            builder.append(word.substring(1).toLowerCase());
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}