package com.github.thedumbledodo.blueprint.menu.pagination;

import com.github.thedumbledodo.blueprint.menu.component.ExecuteComponent;
import com.github.thedumbledodo.blueprint.menu.model.AbstractMenu;
import com.github.thedumbledodo.blueprint.menu.model.Slots;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PaginationBuilder<T, I, C extends ExecuteComponent> {

    private final AbstractMenu<?, I, C> menu;
    private final Supplier<? extends List<T>> entries;
    private final List<Consumer<Pagination<T>>> listeners = new ArrayList<>();

    private Slots slots;
    private Function<T, I> icon;
    private BiConsumer<C, T> action;

    private PageButton<I> previous;
    private PageButton<I> next;

    @Internal
    public PaginationBuilder(AbstractMenu<?, I, C> menu, Supplier<? extends List<T>> entries) {
        this.menu = Objects.requireNonNull(menu, "menu");
        this.entries = Objects.requireNonNull(entries, "entries");
    }

    public PaginationBuilder<T, I, C> setSlots(Slots slots) {
        this.slots = Objects.requireNonNull(slots, "slots");
        return this;
    }

    public PaginationBuilder<T, I, C> setSlots(char symbol) {
        return setSlots(menu.getSlots(symbol));
    }

    public PaginationBuilder<T, I, C> setSlots(int... slots) {
        return setSlots(Slots.of(slots));
    }

    public PaginationBuilder<T, I, C> setIcon(Function<T, I> icon) {
        this.icon = Objects.requireNonNull(icon, "icon");
        return this;
    }

    public PaginationBuilder<T, I, C> onClick(BiConsumer<C, T> action) {
        this.action = Objects.requireNonNull(action, "action");
        return this;
    }

    public PaginationBuilder<T, I, C> onClick(Consumer<T> action) {
        Objects.requireNonNull(action, "action");
        return onClick((click, entry) -> action.accept(entry));
    }

    public PaginationBuilder<T, I, C> setPreviousPage(int slot, I item) {
        return setPreviousPage(slot, item, null);
    }

    public PaginationBuilder<T, I, C> setPreviousPage(int slot, I item, I emptyItem) {
        this.previous = new PageButton<>(slot, Objects.requireNonNull(item, "item"), emptyItem);
        return this;
    }

    public PaginationBuilder<T, I, C> setPreviousPage(char symbol, I item) {
        return setPreviousPage(firstSlot(symbol), item, null);
    }

    public PaginationBuilder<T, I, C> setPreviousPage(char symbol, I item, I emptyItem) {
        return setPreviousPage(firstSlot(symbol), item, emptyItem);
    }

    public PaginationBuilder<T, I, C> setNextPage(int slot, I item) {
        return setNextPage(slot, item, null);
    }

    public PaginationBuilder<T, I, C> setNextPage(int slot, I item, I emptyItem) {
        this.next = new PageButton<>(slot, Objects.requireNonNull(item, "item"), emptyItem);
        return this;
    }

    public PaginationBuilder<T, I, C> setNextPage(char symbol, I item) {
        return setNextPage(firstSlot(symbol), item, null);
    }

    public PaginationBuilder<T, I, C> setNextPage(char symbol, I item, I emptyItem) {
        return setNextPage(firstSlot(symbol), item, emptyItem);
    }

    public PaginationBuilder<T, I, C> onPageChange(Consumer<Pagination<T>> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        return this;
    }

    public Pagination<T> build() {
        return menu.createPagination(this);
    }

    @Internal
    public Supplier<? extends List<T>> getEntries() {
        return entries;
    }

    @Internal
    public Slots getSlots() {
        return slots;
    }

    @Internal
    public Function<T, I> getIcon() {
        return icon;
    }

    @Internal
    public BiConsumer<C, T> getClick() {
        return action;
    }

    @Internal
    public PageButton<I> getPrevious() {
        return previous;
    }

    @Internal
    public PageButton<I> getNext() {
        return next;
    }

    @Internal
    public List<Consumer<Pagination<T>>> getListeners() {
        return listeners;
    }

    private int firstSlot(char symbol) {
        return menu.getSlots(symbol).toList().getFirst();
    }

    @Internal
    public record PageButton<I>(int slot, I item, I emptyItem) {
    }
}
