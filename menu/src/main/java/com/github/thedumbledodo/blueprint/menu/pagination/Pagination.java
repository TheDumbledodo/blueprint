package com.github.thedumbledodo.blueprint.menu.pagination;

import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class Pagination<T> {

    @Getter private final List<Integer> slots;

    private final BiConsumer<Integer, T> renderer;
    private final IntConsumer clearer;
    private final Consumer<Runnable> batcher;

    private final List<Consumer<Pagination<T>>> listeners = new CopyOnWriteArrayList<>();

    private Supplier<List<T>> entries;
    @Getter private int page = 1;

    public Pagination(List<Integer> slots, Supplier<List<T>> entries, BiConsumer<Integer, T> renderer, IntConsumer clearer, Consumer<Runnable> batcher) {
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("pagination needs at least one slot");
        }

        this.slots = List.copyOf(slots);

        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.clearer = Objects.requireNonNull(clearer, "clearer");
        this.batcher = Objects.requireNonNull(batcher, "batcher");

        this.entries = Objects.requireNonNull(entries, "entries");
    }

    public int getMaxPage() {
        final int size = getEntries().size();

        return Math.max(1, (size + slots.size() - 1) / slots.size());
    }

    public int getPageSize() {
        return slots.size();
    }

    public List<T> getEntries() {
        final List<T> list = entries.get();

        return list == null ? List.of() : list;
    }

    public List<T> getPageEntries() {
        final List<T> list = getEntries();
        final int from = Math.min(list.size(), (page - 1) * slots.size());
        final int to = Math.min(list.size(), from + slots.size());

        return list.subList(from, to);
    }

    public boolean hasNext() {
        return page < getMaxPage();
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public void next() {
        if (!hasNext()) {
            return;
        }
        setPage(page + 1);
    }

    public void previous() {
        if (!hasPrevious()) {
            return;
        }
        setPage(page - 1);
    }

    public void setPage(int page) {
        this.page = page;
        render();
    }

    public void setEntries(List<T> entries) {
        setEntries(() -> entries);
    }

    public void setEntries(Supplier<List<T>> entries) {
        this.entries = Objects.requireNonNull(entries, "entries");
        render();
    }

    public void onPageChange(Consumer<Pagination<T>> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void render() {
        this.page = Math.max(1, Math.min(page, getMaxPage()));

        final List<T> pageEntries = getPageEntries();

        batcher.accept(() -> {
            for (int i = 0; i < slots.size(); i++) {
                final int slot = slots.get(i);

                if (i < pageEntries.size()) {
                    renderer.accept(slot, pageEntries.get(i));
                    continue;
                }
                clearer.accept(slot);
            }

            for (Consumer<Pagination<T>> listener : listeners) {
                listener.accept(this);
            }
        });
    }
}
