package com.github.thedumbledodo.blueprint.math;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RandomUtilTest {

    @Test
    void nextIntIncludesBothBounds() {
        final Set<Integer> seen = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            final int value = RandomUtil.nextInt(1, 3);

            assertTrue(value >= 1 && value <= 3);
            seen.add(value);
        }
        assertEquals(Set.of(1, 2, 3), seen);
    }

    @Test
    void nextIntWorksAtTheIntegerLimits() {
        assertEquals(Integer.MAX_VALUE, RandomUtil.nextInt(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, RandomUtil.nextInt(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    void reversedBoundsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> RandomUtil.nextInt(5, 1));
        assertThrows(IllegalArgumentException.class, () -> RandomUtil.nextDouble(5, 1));
    }

    @Test
    void nanBoundsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> RandomUtil.nextDouble(Double.NaN, 1));
    }

    @Test
    void equalDoubleBoundsReturnTheBound() {
        assertEquals(2.5, RandomUtil.nextDouble(2.5, 2.5));
    }

    @Test
    void nextDoubleStaysInRange() {
        for (int i = 0; i < 1000; i++) {
            final double value = RandomUtil.nextDouble(-1, 1);

            assertTrue(value >= -1 && value < 1);
        }
    }

    @Test
    void emptyAndNullCollectionsGiveNull() {
        assertNull(RandomUtil.getRandomElement(List.of()));
        assertNull(RandomUtil.getRandomElement(Set.of()));
        assertNull(RandomUtil.getRandomElement(new String[0]));
        assertNull(RandomUtil.getRandomElement((List<String>) null));
        assertTrue(RandomUtil.getRandomElementOptional((Set<String>) null).isEmpty());
    }

    @Test
    void randomElementComesFromTheInput() {
        final List<String> maps = List.of("desert", "forest", "ocean");

        for (int i = 0; i < 100; i++) {
            assertTrue(maps.contains(RandomUtil.getRandomElement(maps)));
            assertTrue(maps.contains(RandomUtil.getRandomElement(Set.copyOf(maps))));
        }
    }

    @Test
    void chanceEdgeCases() {
        assertFalse(RandomUtil.hasChance(0));
        assertFalse(RandomUtil.hasChance(-1));
        assertFalse(RandomUtil.hasChance(Double.NaN));
        assertTrue(RandomUtil.hasChance(1));
        assertTrue(RandomUtil.hasChance(2));
    }
}
