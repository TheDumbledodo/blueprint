package com.github.thedumbledodo.blueprint.config.serializer;

import com.github.thedumbledodo.blueprint.chat.Message;
import com.github.thedumbledodo.blueprint.chat.Text;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;

public final class Serializers {

    public static final Serializer<UUID, Object> UUID = new Serializer<>() {

        @Override
        public Object serialize(UUID element) {
            return element.toString();
        }

        @Override
        public UUID deserialize(Object element) {
            return java.util.UUID.fromString(String.valueOf(element).trim());
        }
    };

    public static final Serializer<Message, Object> MESSAGE = new Serializer<>() {

        @Override
        public Object serialize(Message element) {
            return element.getRaw();
        }

        @Override
        public Message deserialize(Object element) {
            return new Message(String.valueOf(element));
        }
    };

    public static final Serializer<Component, Object> COMPONENT = new Serializer<>() {

        @Override
        public Object serialize(Component element) {
            return Text.translateToMiniMessage(element);
        }

        @Override
        public Component deserialize(Object element) {
            return Text.translate(String.valueOf(element));
        }
    };

    private static final Map<Class<?>, Serializer<?, ?>> DEFAULTS = Map.of(
            java.util.UUID.class, UUID,
            Message.class, MESSAGE,
            Component.class, COMPONENT
    );

    public static Map<Class<?>, Serializer<?, ?>> defaults() {
        return DEFAULTS;
    }
}
