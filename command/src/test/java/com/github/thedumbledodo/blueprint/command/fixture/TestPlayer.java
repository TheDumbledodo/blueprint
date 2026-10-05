package com.github.thedumbledodo.blueprint.command.fixture;

import lombok.Getter;

import java.util.UUID;

@Getter
public class TestPlayer extends TestSender {

    private final UUID uniqueId = UUID.randomUUID();

    public TestPlayer(String name, String... permissions) {
        super(name, permissions);
    }
}
