package io.github.dumbledodo.blueprint.math;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Random;
import java.util.Set;

@UtilityClass
public class RandomUtil {

    private final Random RANDOM = new Random();

    public int nextInt(int min, int max) {
        return RANDOM.nextInt((max - min) + 1) + min;
    }

    public double nextDouble() {
        return RANDOM.nextDouble();
    }

    public double nextDouble(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }

    public <T> T getRandomElement(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(RANDOM.nextInt(list.size()));
    }

    public <T> T getRandomElement(Set<T> set) {
        if (set.isEmpty()) {
            return null;
        }
        return set.stream()
                .skip(RANDOM.nextInt(set.size()))
                .findFirst()
                .orElse(null);
    }

    public <T> T getRandomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[RANDOM.nextInt(array.length)];
    }

    public boolean hasChance(double probability) {
        return !(probability <= 0) && (probability >= 1 || nextDouble() < probability);
    }
}