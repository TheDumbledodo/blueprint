package com.github.thedumbledodo.blueprint.command.exception;

import com.github.thedumbledodo.blueprint.chat.Text;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Objects;
import java.util.function.Function;

public class CommandException extends RuntimeException {

    private static final CommandMessages DEFAULT_MESSAGES = new CommandMessages();

    private final transient Function<CommandMessages, String> template;
    private final transient TagResolver[] resolvers;

    public CommandException(String message, TagResolver... resolvers) {
        this(messages -> message, resolvers);
    }

    public CommandException(Function<CommandMessages, String> template, TagResolver... resolvers) {
        super(null, null, false, false);

        this.template = Objects.requireNonNull(template, "template");
        this.resolvers = resolvers == null ? new TagResolver[0] : resolvers;
    }

    public Component render(CommandMessages messages) {
        return Text.translate(template.apply(messages), resolvers);
    }

    @Override
    public String getMessage() {
        return template.apply(DEFAULT_MESSAGES);
    }
}
