package io.github.dumbledodo.blueprint.component;

import com.github.retrooper.packetevents.protocol.player.User;
import lombok.Getter;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@Getter
public final class CooldownComponent {

    private final long delayMillis;

    private final BiConsumer<User, Long> onCooldown;
    private final TimeUnit unit;

    private long expireTime = 0L;

    public CooldownComponent(long delay) {
        this(delay, TimeUnit.SECONDS);
    }

    public CooldownComponent(long delay, TimeUnit unit) {
        this(delay, unit, null);
    }

    public CooldownComponent(long delay, BiConsumer<User, Long> onCooldown) {
        this(delay, TimeUnit.SECONDS, onCooldown);
    }

    public CooldownComponent(long delay, TimeUnit unit, BiConsumer<User, Long> onCooldown) {
        this.delayMillis = unit.toMillis(delay);
        this.onCooldown = onCooldown;
        this.unit = unit;
    }

    public void reset() {
        expireTime = System.currentTimeMillis() + delayMillis;
    }

    public boolean test(User user) {
        final long now = System.currentTimeMillis();

        if (System.currentTimeMillis() >= expireTime) {
            reset();
            return true;
        }

        if (onCooldown != null) {
            final long remaining  = expireTime - now;

            onCooldown.accept(user, Math.max(0, remaining));
        }
        return false;
    }
}