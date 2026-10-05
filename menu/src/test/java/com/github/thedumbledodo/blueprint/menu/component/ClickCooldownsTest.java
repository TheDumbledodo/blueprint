package com.github.thedumbledodo.blueprint.menu.component;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class ClickCooldownsTest {

    private final AtomicLong clock = new AtomicLong(1_000);
    private final ClickCooldowns cooldowns = new ClickCooldowns(clock::get);

    private final UUID steve = UUID.randomUUID();
    private final UUID alex = UUID.randomUUID();
    private final Object action = new Object();

    @Test
    void playersHaveIndependentCooldowns() {
        assertTrue(cooldowns.tryStart(steve, action, Duration.ofSeconds(2)));
        assertTrue(cooldowns.tryStart(alex, action, Duration.ofSeconds(2)));
        assertFalse(cooldowns.tryStart(steve, action, Duration.ofSeconds(2)));
    }

    @Test
    void actionsHaveIndependentCooldowns() {
        assertTrue(cooldowns.tryStart(steve, action, Duration.ofSeconds(2)));
        assertTrue(cooldowns.tryStart(steve, new Object(), Duration.ofSeconds(2)));
    }

    @Test
    void cooldownEndsExactlyAtItsDuration() {
        assertTrue(cooldowns.tryStart(steve, action, Duration.ofSeconds(2)));

        clock.addAndGet(1_999);
        assertFalse(cooldowns.tryStart(steve, action, Duration.ofSeconds(2)));

        clock.addAndGet(1);
        assertTrue(cooldowns.tryStart(steve, action, Duration.ofSeconds(2)));
    }

    @Test
    void remainingTimeCountsDown() {
        cooldowns.tryStart(steve, action, Duration.ofSeconds(2));
        clock.addAndGet(500);

        assertEquals(Duration.ofMillis(1_500), cooldowns.getRemaining(steve, action));
        assertEquals(Duration.ZERO, cooldowns.getRemaining(alex, action));
    }

    @Test
    void resetClearsOnlyThatPlayer() {
        cooldowns.tryStart(steve, action, Duration.ofHours(1));
        cooldowns.tryStart(alex, action, Duration.ofHours(1));
        cooldowns.reset(steve);

        assertTrue(cooldowns.tryStart(steve, action, Duration.ofHours(1)));
        assertFalse(cooldowns.tryStart(alex, action, Duration.ofHours(1)));
    }

    @Test
    void zeroDurationNeverBlocks() {
        assertTrue(cooldowns.tryStart(steve, action, Duration.ZERO));
        assertTrue(cooldowns.tryStart(steve, action, Duration.ZERO));
    }
}
