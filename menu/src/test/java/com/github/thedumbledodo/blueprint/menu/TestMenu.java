package com.github.thedumbledodo.blueprint.menu;

import com.github.thedumbledodo.blueprint.menu.model.AbstractMenu;
import com.github.thedumbledodo.blueprint.menu.model.MenuType;
import com.github.thedumbledodo.blueprint.menu.scheduler.MenuScheduler;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class TestMenu extends AbstractMenu<UUID, String, TestClick> {

    private final List<Integer> renderedSlots = new ArrayList<>();
    private final List<Runnable> scheduledTasks = new ArrayList<>();

    private int updates;
    private int titleUpdates;
    private int cancelledTasks;

    public TestMenu(int rows) {
        super(rows, Component.text("Test"));
    }

    public TestMenu(MenuType type) {
        super(type, Component.text("Test"));
    }

    @Override
    public void open(UUID viewer) {
        prepareOpen();
        handleOpen(viewer);
    }

    @Override
    public void close(UUID viewer) {
        handleClose(viewer);
    }

    @Override
    protected void render() {
        updates++;
    }

    @Override
    protected void render(int slot) {
        renderedSlots.add(slot);
    }

    @Override
    protected void updateTitle() {
        titleUpdates++;
    }

    @Override
    protected MenuScheduler getScheduler() {
        return (task, interval) -> {
            scheduledTasks.add(task);
            return () -> cancelledTasks++;
        };
    }

    public void runScheduledTasks() {
        for (Runnable task : List.copyOf(scheduledTasks)) {
            task.run();
        }
    }

    public void resetCounters() {
        renderedSlots.clear();
        updates = 0;
    }

    public Duration interval() {
        return getRefreshInterval();
    }
}
