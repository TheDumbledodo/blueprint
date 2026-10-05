package com.github.thedumbledodo.blueprint.menu.component;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class ClickCooldowns {

    private final LongSupplier clock;
    private final Map<UUID, Map<Object, Long>> expireTimes = new ConcurrentHashMap<>();

    public ClickCooldowns() {
        this(System::currentTimeMillis);
    }

    public ClickCooldowns(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public boolean tryStart(UUID uuid, Object key, Duration duration) {
        Objects.requireNonNull(duration, "duration");

        final long now = clock.getAsLong();
        final Map<Object, Long> times = expireTimes.computeIfAbsent(uuid, ignored -> new ConcurrentHashMap<>());
        final Long expireTime = times.get(key);

        if (expireTime != null && now < expireTime) {
            return false;
        }

        times.put(key, now + duration.toMillis());
        return true;
    }

    public Duration getRemaining(UUID uuid, Object key) {
        final Map<Object, Long> times = expireTimes.get(uuid);
        final Long expireTime = times == null ? null : times.get(key);

        if (expireTime == null) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(Math.max(0, expireTime - clock.getAsLong()));
    }

    public void reset(UUID uuid) {
        expireTimes.remove(uuid);
    }

    public void resetAll() {
        expireTimes.clear();
    }
}
