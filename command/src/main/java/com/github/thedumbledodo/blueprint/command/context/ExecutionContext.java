package com.github.thedumbledodo.blueprint.command.context;

import com.github.thedumbledodo.blueprint.chat.Text;
import com.github.thedumbledodo.blueprint.command.exception.CommandException;
import com.github.thedumbledodo.blueprint.command.message.CommandMessages;
import com.github.thedumbledodo.blueprint.command.model.Argument;
import com.github.thedumbledodo.blueprint.command.model.CommandActor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;
import java.util.Optional;

public final class ExecutionContext {

    @Getter
    private final CommandActor actor;

    @Getter
    private final CommandMessages messages;

    private final Map<String, Object> arguments;
    private final Map<String, String> inputs;

    public ExecutionContext(CommandActor actor, CommandMessages messages, Map<String, Object> arguments, Map<String, String> inputs) {
        this.actor = actor;
        this.messages = messages;

        this.arguments = arguments;
        this.inputs = inputs;
    }

    public Object getSender() {
        return actor.getSender();
    }

    public <S> S getSender(Class<S> type) {
        if (type == CommandActor.class) {
            return type.cast(actor);
        }

        final Object sender = actor.getSender();

        if (!type.isInstance(sender)) {
            throw new CommandException(CommandMessages.getWrongSender(actor));
        }
        return type.cast(sender);
    }

    public <T> T get(Argument<T> argument) {
        return (T) arguments.get(argument.getName());
    }

    public <T> Optional<T> getOptional(Argument<T> argument) {
        return Optional.ofNullable(get(argument));
    }

    public <T> T get(String name) {
        return (T) arguments.get(name);
    }

    public boolean has(String name) {
        return arguments.get(name) != null;
    }

    public String getInput(String name) {
        return inputs.get(name);
    }

    public void reply(String message, TagResolver... resolvers) {
        actor.sendMessage(Text.translate(message, resolvers));
    }

    public void reply(Component message) {
        actor.sendMessage(message);
    }

    public void checkPermission(String permission) {
        if (permission == null || permission.isBlank() || actor.hasPermission(permission)) {
            return;
        }
        throw new CommandException(CommandMessages::getNoPermission);
    }
}
