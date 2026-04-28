package io.github.dumbledodo.blueprint.math;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class RandomUtil {

    public int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }
        return (int) ThreadLocalRandom.current().nextLong(min, (long) max + 1);
    }

    public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public double nextDouble(double min, double max) {
        if (Double.isNaN(min) || Double.isNaN(max)) {
            throw new IllegalArgumentException("min and max must be numbers");
        }

        if (min > max) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }

        if (Double.compare(min, max) == 0) {
            return min;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public <T> T getRandomElement(List<T> list) {
        return getRandomElementOptional(list).orElse(null);
    }

    public <T> T getRandomElement(Set<T> set) {
        return getRandomElementOptional(set).orElse(null);
    }

    public <T> T getRandomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }

    public <T> Optional<T> getRandomElementOptional(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(ThreadLocalRandom.current().nextInt(list.size())));
    }

    public <T> Optional<T> getRandomElementOptional(Set<T> set) {
        if (set == null || set.isEmpty()) {
            return Optional.empty();
        }
        return set.stream()
                .skip(ThreadLocalRandom.current().nextInt(set.size()))
                .findFirst();
    }

    public <T> Optional<T> getRandomElementOptional(T[] array) {
        if (array == null || array.length == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(array[ThreadLocalRandom.current().nextInt(array.length)]);
    }

    public boolean hasChance(double probability) {
        return !Double.isNaN(probability) && !(probability <= 0) && (probability >= 1 || nextDouble() < probability);
    }
}
