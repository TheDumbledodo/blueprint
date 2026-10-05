package com.github.thedumbledodo.blueprint.menu.scheduler;

import java.time.Duration;

@FunctionalInterface
public interface MenuScheduler {

    MenuTask schedule(Runnable task, Duration interval);
}
