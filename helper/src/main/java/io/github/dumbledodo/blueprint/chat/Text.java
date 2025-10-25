package io.github.dumbledodo.blueprint.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Text {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.defaults())
                    .build())
            .build();

    public static Component translate(String text) {
        if (text == null) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text);
    }

    public static Component[] translate(String... text) {
        return Arrays.stream(text).map(Text::translate).toArray(Component[]::new);
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

    public static List<String> translateToMiniMessage(List<Component> component) {
        return component.stream().map(Text::translateToMiniMessage).toList();
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