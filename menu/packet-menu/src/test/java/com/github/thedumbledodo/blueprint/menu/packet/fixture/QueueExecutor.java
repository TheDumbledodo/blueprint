package com.github.thedumbledodo.blueprint.menu.packet.fixture;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public final class QueueExecutor implements Executor {

    private final List<Runnable> tasks = new ArrayList<>();

    @Override
    public void execute(@NotNull Runnable command) {
        tasks.add(command);
    }

    public int size() {
        return tasks.size();
    }

    public void runAll() {
        while (!tasks.isEmpty()) {
            tasks.removeFirst().run();
        }
    }
}
