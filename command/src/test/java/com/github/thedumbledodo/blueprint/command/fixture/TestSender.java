package com.github.thedumbledodo.blueprint.command.fixture;

import com.github.thedumbledodo.blueprint.chat.Text;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class TestSender {

    private final String name;
    private final Set<String> permissions = new HashSet<>();
    private final List<String> messages = new ArrayList<>();

    public TestSender(String name, String... permissions) {
        this.name = name;
        this.permissions.addAll(List.of(permissions));
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || permissions.contains("*");
    }

    public void sendMessage(Component message) {
        messages.add(Text.translateToLegacyString(message).replaceAll("§.", ""));
    }

    public String lastMessage() {
        return messages.isEmpty() ? null : messages.getLast();
    }
}
