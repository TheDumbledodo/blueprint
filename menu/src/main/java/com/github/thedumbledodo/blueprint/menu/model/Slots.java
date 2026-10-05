package com.github.thedumbledodo.blueprint.menu.model;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Slots implements Iterable<Integer> {

    private final List<Integer> slots;

    private Slots(Collection<Integer> slots) {
        this.slots = List.copyOf(new LinkedHashSet<>(slots));
    }

    public static Slots of(int... slots) {
        final List<Integer> list = new ArrayList<>(slots.length);

        for (int slot : slots) {
            list.add(slot);
        }
        return new Slots(list);
    }

    public static Slots of(Collection<Integer> slots) {
        return new Slots(Objects.requireNonNull(slots, "slots"));
    }

    public static Slots range(int from, int to) {
        if (from > to) {
            throw new IllegalArgumentException("range start " + from + " is after its end " + to);
        }

        final List<Integer> list = new ArrayList<>(to - from + 1);

        for (int slot = from; slot <= to; slot++) {
            list.add(slot);
        }
        return new Slots(list);
    }

    public List<Integer> toList() {
        return slots;
    }

    public int size() {
        return slots.size();
    }

    public boolean isEmpty() {
        return slots.isEmpty();
    }

    public boolean contains(int slot) {
        return slots.contains(slot);
    }

    @Override
    public @NotNull Iterator<Integer> iterator() {
        return slots.iterator();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Slots that && slots.equals(that.slots);
    }

    @Override
    public int hashCode() {
        return slots.hashCode();
    }

    @Override
    public String toString() {
        return slots.toString();
    }
}
