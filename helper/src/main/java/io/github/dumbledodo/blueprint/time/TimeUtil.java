package io.github.dumbledodo.blueprint.time;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static final long FULL_DAY_IN_SECONDS = TimeUnit.DAYS.toSeconds(1);

    public static long getLocalTimeInSeconds() {
        return LocalTime.now().toSecondOfDay();
    }

    public static long getLocalTimeInSeconds(ZoneId id) {
        return LocalTime.now(id).toSecondOfDay();
    }

    public static long getRemainingSecondsUntil(long current, long seconds) {
        return (FULL_DAY_IN_SECONDS - current + seconds) % FULL_DAY_IN_SECONDS;
    }

    public static long getRemainingSecondsUntil(long seconds) {
        return getRemainingSecondsUntil(getLocalTimeInSeconds(), seconds);
    }

    public static long getRemainingSecondsUntil(ZoneId id, long seconds) {
        return getRemainingSecondsUntil(getLocalTimeInSeconds(id), seconds);
    }

    public static String toString(long duration) {
        return toString(duration, 1);
    }

    public static String toString(Duration duration) {
        return toString(duration, 1);
    }

    public static String toString(long duration, boolean shortVersion) {
        return toString(duration, shortVersion ? 1 : Integer.MAX_VALUE);
    }

    public static String toString(Duration duration, boolean shortVersion) {
        return toString(duration, shortVersion ? 1 : Integer.MAX_VALUE);
    }

    public static String toString(long duration, boolean shortVersion, Collection<TimeUnits> ignoreUnits) {
        return toString(duration, shortVersion ? 1 : Integer.MAX_VALUE, ignoreUnits);
    }

    public static String toString(Duration duration, boolean shortVersion, Collection<TimeUnits> ignoreUnits) {
        return toString(duration, shortVersion ? 1 : Integer.MAX_VALUE, ignoreUnits);
    }

    public static String toString(long duration, int maxUnits) {
        return toString(duration, maxUnits, List.of());
    }

    public static String toString(Duration duration, int maxUnits) {
        if (duration == null) {
            return "0s";
        }
        return toString(duration.getSeconds(), maxUnits);
    }

    public static String toString(long duration, int maxUnits, Collection<TimeUnits> ignoreUnits) {
        if (maxUnits <= 0) {
            throw new IllegalArgumentException("maxUnits must be greater than 0");
        }

        final StringBuilder result = new StringBuilder();
        final Collection<TimeUnits> ignoredUnits = ignoreUnits == null ? List.of() : ignoreUnits;

        long remaining = Math.abs(duration);
        int units = 0;

        for (TimeUnits current : TimeUnits.values()) {
            if (ignoredUnits.contains(current)) {
                continue;
            }

            final long temp = remaining / current.getSeconds();

            if (temp <= 0) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(" ");
            }

            result.append(temp)
                    .append(" ")
                    .append(current.getName())
                    .append(temp != 1 ? "s" : "");

            units++;
            if (units >= maxUnits) {
                break;
            }
            remaining -= temp * current.getSeconds();
        }

        if ("".contentEquals(result)) {
            return "0s";
        }
        return duration < 0 ? "-" + result : result.toString();
    }

    public static String toString(Duration duration, int maxUnits, Collection<TimeUnits> ignoreUnits) {
        if (duration == null) {
            return "0s";
        }
        return toString(duration.getSeconds(), maxUnits, ignoreUnits);
    }

    @Getter
    @AllArgsConstructor
    public enum TimeUnits {
        YEARS(TimeUnit.DAYS.toSeconds(365), "year"),
        MONTHS(TimeUnit.DAYS.toSeconds(30), "month"),
        WEEKS(TimeUnit.DAYS.toSeconds(7), "week"),
        DAYS(TimeUnit.DAYS.toSeconds(1), "day"),
        HOURS(TimeUnit.HOURS.toSeconds(1), "hour"),
        MINUTES(TimeUnit.MINUTES.toSeconds(1), "minute"),
        SECONDS(TimeUnit.SECONDS.toSeconds(1), "second");

        private final long seconds;
        private final String name;
    }
}
