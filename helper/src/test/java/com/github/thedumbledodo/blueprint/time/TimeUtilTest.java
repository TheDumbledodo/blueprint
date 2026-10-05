package com.github.thedumbledodo.blueprint.time;

import com.github.thedumbledodo.blueprint.time.TimeUtil.TimeUnits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilTest {

    @ParameterizedTest
    @CsvSource({
            "3661, 1, 1 hour",
            "3661, 3, 1 hour 1 minute 1 second",
            "60, 1, 1 minute",
            "120, 1, 2 minutes",
            "-90, 2, -1 minute 30 seconds",
            "691200, 2, 1 week 1 day",
            "0, 1, 0s"
    })
    void formatsDurations(long seconds, int maxUnits, String expected) {
        assertEquals(expected, TimeUtil.toString(seconds, maxUnits));
    }

    @Test
    void weeksAreNotIgnoredByDefault() {
        assertEquals("1 week", TimeUtil.toString(691200));
    }

    @Test
    void ignoredUnitsAreSkipped() {
        assertEquals("8 days", TimeUtil.toString(691200, 2, List.of(TimeUnits.WEEKS)));
    }

    @Test
    void shortAndLongVersions() {
        assertEquals("1 hour", TimeUtil.toString(3661, true));
        assertEquals("1 hour 1 minute 1 second", TimeUtil.toString(3661, false));
    }

    @Test
    void durationOverloadsMatchSeconds() {
        assertEquals(TimeUtil.toString(3661, false), TimeUtil.toString(Duration.ofSeconds(3661), false));
        assertEquals("0s", TimeUtil.toString(null, 1));
    }

    @Test
    void maxUnitsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> TimeUtil.toString(10, 0));
    }

    @Test
    void remainingSecondsWrapAroundMidnight() {
        assertEquals(7200, TimeUtil.getRemainingSecondsUntil(82800, 3600));
        assertEquals(0, TimeUtil.getRemainingSecondsUntil(3600, 3600));
        assertEquals(1800, TimeUtil.getRemainingSecondsUntil(3600, 5400));
    }
}
