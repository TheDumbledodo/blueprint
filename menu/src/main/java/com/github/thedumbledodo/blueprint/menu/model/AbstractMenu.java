package com.github.thedumbledodo.blueprint.menu.model;

import com.github.thedumbledodo.blueprint.menu.component.ClickCooldowns;
import com.github.thedumbledodo.blueprint.menu.component.ExecuteComponent;
import com.github.thedumbledodo.blueprint.menu.layout.MenuLayout;
import com.github.thedumbledodo.blueprint.menu.pagination.Pagination;
import com.github.thedumbledodo.blueprint.menu.pagination.PaginationBuilder;
import com.github.thedumbledodo.blueprint.menu.scheduler.MenuScheduler;
import com.github.thedumbledodo.blueprint.menu.scheduler.MenuTask;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractMenu<V, I, C extends ExecuteComponent> {

    private static final Object ANY_CLICK = new Object();

    @Getter
    private final MenuType type;

    @Getter
    private Component title;

    private final Map<Integer, I> items = new ConcurrentHashMap<>();
    private final Map<Integer, SlotAction<C>> actions = new ConcurrentHashMap<>();
    private final Set<Integer> paginatedSlots = ConcurrentHashMap.newKeySet();
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

    @Getter
    private final ClickCooldowns cooldowns = new ClickCooldowns();

    @Getter
    private MenuLayout layout;

    private Consumer<C> anyClickAction;
    private GuiAction openAction;
    private GuiAction closeAction;
    private Consumer<AbstractMenu<V, I, C>> refreshAction;

    private final boolean refreshOverridden;

    @Getter
    private Duration refreshInterval;

    private MenuTask refreshTask;


    protected AbstractMenu(MenuType type, Component title) {
        this.type = Objects.requireNonNull(type, "type");
        this.title = title == null ? Component.empty() : title;
        this.refreshOverridden = overridesRefreshItems();
    }

    protected AbstractMenu(int rows, Component title) {
        this(MenuType.of(rows), title);
    }

    public abstract void open(V viewer);

    public abstract void close(V viewer);

    protected abstract void render();

    protected abstract void render(int slot);

    protected abstract void updateTitle();

    protected abstract MenuScheduler getScheduler();

    protected void refreshItems() {
        if (refreshAction != null) {
            refreshAction.accept(this);
        }
    }

    public int getSize() {
        return type.getSize();
    }

    public int getRows() {
        return type.getRows();
    }

    public int getColumns() {
        return type.getColumns();
    }

    public Optional<I> getItem(int slot) {
        return Optional.ofNullable(items.get(slot));
    }

    public Map<Integer, I> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public boolean hasClick(int slot) {
        return actions.containsKey(slot);
    }

    public void setItem(int slot, I item) {
        checkSlot(slot);

        if (item == null) {
            items.remove(slot);

        } else {
            items.put(slot, item);
        }
        render(slot);
    }

    public void setItem(Slots slots, I item) {
        for (int slot : slots) {
            setItem(slot, item);
        }
    }

    public void setItem(char symbol, I item) {
        setItem(getSlots(symbol), item);
    }

    public void setItem(int slot, Supplier<? extends I> item) {
        setItem(slot, Objects.requireNonNull(item, "item").get());
    }

    public void setItem(Slots slots, Supplier<? extends I> item) {
        setItem(slots, Objects.requireNonNull(item, "item").get());
    }

    public void setItem(char symbol, Supplier<? extends I> item) {
        setItem(getSlots(symbol), item);
    }

    public void removeItem(int slot) {
        setItem(slot, (I) null);
    }

    public void removeItem(Slots slots) {
        setItem(slots, (I) null);
    }

    public void onClick(int slot, Consumer<C> action) {
        putClick(slot, action, action);
    }

    public void onClick(Slots slots, Consumer<C> action) {
        for (int slot : slots) {
            putClick(slot, action, action);
        }
    }

    public void onClick(char symbol, Consumer<C> action) {
        onClick(getSlots(symbol), action);
    }

    public void onClick(int slot, Runnable action) {
        onClick(slot, ignoreClick(action));
    }

    public void onClick(Slots slots, Runnable action) {
        onClick(slots, ignoreClick(action));
    }

    public void onClick(char symbol, Runnable action) {
        onClick(getSlots(symbol), ignoreClick(action));
    }

    public void removeClick(int slot) {
        putClick(slot, null, null);
    }

    public void removeClick(Slots slots) {
        for (int slot : slots) {
            removeClick(slot);
        }
    }

    public void onClick(Consumer<C> action) {
        this.anyClickAction = action;
    }

    public void onClick(Runnable action) {
        onClick(ignoreClick(action));
    }

    public void onOpen(GuiAction action) {
        this.openAction = action;
    }

    public void onClose(GuiAction action) {
        this.closeAction = action;
    }

    public void onRefresh(Consumer<AbstractMenu<V, I, C>> action) {
        this.refreshAction = action;

        if (!viewers.isEmpty()) {
            startRefresh();
        }
    }

    public void clear() {
        actions.clear();

        for (int slot = 0; slot < getSize(); slot++) {
            setItem(slot, (I) null);
        }
    }

    public void fill(I item) {
        for (int slot = 0; slot < getSize(); slot++) {
            setItem(slot, item);
        }
    }

    public void fillEmpty(I item) {
        for (int slot = 0; slot < getSize(); slot++) {
            if (items.containsKey(slot)) {
                continue;
            }
            setItem(slot, item);
        }
    }

    public void fillBorder(I item) {
        checkGrid();

        final int rows = getRows();
        final int columns = getColumns();

        for (int slot = 0; slot < getSize(); slot++) {
            final int row = slot / columns;
            final int column = slot % columns;

            if (row != 0 && row != rows - 1 && column != 0 && column != columns - 1) {
                continue;
            }
            setItem(slot, item);
        }
    }

    public void fillRow(int row, I item) {
        setItem(getRow(row), item);
    }

    public void fillColumn(int column, I item) {
        setItem(getColumn(column), item);
    }

    public Slots getRow(int row) {
        checkGrid();

        if (row < 0 || row >= getRows()) {
            throw new IllegalArgumentException("row must be between 0 and " + (getRows() - 1));
        }
        return Slots.range(row * getColumns(), row * getColumns() + getColumns() - 1);
    }

    public Slots getColumn(int column) {
        checkGrid();

        if (column < 0 || column >= getColumns()) {
            throw new IllegalArgumentException("column must be between 0 and " + (getColumns() - 1));
        }

        final List<Integer> slots = new ArrayList<>(getRows());

        for (int row = 0; row < getRows(); row++) {
            slots.add(row * getColumns() + column);
        }
        return Slots.of(slots);
    }

    public void setLayout(MenuLayout layout) {
        if (layout != null && (layout.getColumns() != getColumns() || layout.getRowCount() != getRows())) {
            throw new IllegalArgumentException("layout is " + layout.getRowCount() + "x" + layout.getColumns()
                    + " but the menu is " + getRows() + "x" + getColumns());
        }
        this.layout = layout;
    }

    public Slots getSlots(char symbol) {
        if (layout == null) {
            throw new IllegalStateException("setLayout(...) must be called before using layout symbols");
        }

        final List<Integer> slots = layout.getSlots(symbol);

        if (slots.isEmpty()) {
            throw new IllegalArgumentException("layout has no slots for '" + symbol + "'");
        }
        return Slots.of(slots);
    }

    public <T> PaginationBuilder<T, I, C> paginate(List<T> entries) {
        Objects.requireNonNull(entries, "entries");
        return paginate(() -> entries);
    }

    public <T> PaginationBuilder<T, I, C> paginate(Supplier<? extends List<T>> entries) {
        return new PaginationBuilder<>(this, entries);
    }

    @Internal
    public <T> Pagination<T> createPagination(PaginationBuilder<T, I, C> builder) {
        final Slots slots = builder.getSlots();
        final Function<T, I> icon = builder.getIcon();
        final BiConsumer<C, T> click = builder.getClick();

        if (slots == null) {
            throw new IllegalStateException("pagination needs slots(...)");
        }

        if (icon == null) {
            throw new IllegalStateException("pagination needs icon(...)");
        }

        for (int slot : slots) {
            checkSlot(slot);

            if (paginatedSlots.contains(slot)) {
                throw new IllegalArgumentException("slot " + slot + " is already used by another pagination, use setEntries(...) to change its list");
            }
        }

        final Supplier<? extends List<T>> entries = builder.getEntries();
        final Pagination<T> pagination = new Pagination<>(slots.toList(), entries::get,
                (slot, entry) -> {
                    setItem(slot, icon.apply(entry));
                    putClick(slot, click == null ? null : event -> click.accept(event, entry), click);
                },
                slot -> {
                    setItem(slot, (I) null);
                    putClick(slot, null, null);
                });

        paginatedSlots.addAll(slots.toList());
        builder.getListeners().forEach(pagination::onPageChange);

        final PaginationBuilder.PageButton<I> previous = builder.getPrevious();
        final PaginationBuilder.PageButton<I> next = builder.getNext();

        if (previous != null) {
            setPageButton(pagination, previous.slot(), previous.item(), previous.emptyItem(), Pagination::hasPrevious, Pagination::previous);
        }

        if (next != null) {
            setPageButton(pagination, next.slot(), next.item(), next.emptyItem(), Pagination::hasNext, Pagination::next);
        }
        pagination.render();
        return pagination;
    }

    private <T> void setPageButton(Pagination<T> pagination, int slot, I item, I emptyItem,
                                   Predicate<Pagination<?>> visible, Consumer<Pagination<?>> turn) {
        checkSlot(slot);

        final Consumer<C> action = click -> turn.accept(pagination);
        final Consumer<Pagination<T>> updater = current -> {
            if (visible.test(current)) {
                setItem(slot, item);
                putClick(slot, action, action);
                return;
            }

            putClick(slot, null, null);
            setItem(slot, emptyItem);
        };

        pagination.onPageChange(updater);
        updater.accept(pagination);
    }

    public void update() {
        render();
    }

    public void setTitle(Component title) {
        this.title = title == null ? Component.empty() : title;

        if (!viewers.isEmpty()) {
            updateTitle();
        }
    }

    public void setRefresh(Duration interval) {
        if (interval != null && (interval.isZero() || interval.isNegative())) {
            throw new IllegalArgumentException("refresh interval must be positive");
        }

        this.refreshInterval = interval;

        stopRefresh();

        if (!viewers.isEmpty()) {
            startRefresh();
        }
    }

    public void refresh() {
        if (hasRefreshCode()) {
            refreshItems();
        }
    }

    public Set<UUID> getViewerIds() {
        return Collections.unmodifiableSet(viewers);
    }

    public boolean isOpen() {
        return !viewers.isEmpty();
    }

    public void handleClick(C click) {
        if (anyClickAction != null) {
            click.attach(cooldowns, ANY_CLICK);
            anyClickAction.accept(click);
        }

        final SlotAction<C> action = actions.get(click.slot());

        if (action == null) {
            return;
        }

        click.attach(cooldowns, action.key());
        action.action().accept(click);
    }

    public void handleOpen(UUID uuid) {
        final boolean first = viewers.isEmpty();

        viewers.add(uuid);

        if (first) {
            startRefresh();
        }

        if (openAction != null) {
            openAction.execute(uuid);
        }
    }

    public void handleClose(UUID uuid) {
        if (!viewers.remove(uuid)) {
            return;
        }

        if (viewers.isEmpty()) {
            stopRefresh();
        }

        if (closeAction != null) {
            closeAction.execute(uuid);
        }
    }

    protected void prepareOpen() {
        refresh();
    }

    protected void checkSlot(int slot) {
        if (slot < 0 || slot >= getSize()) {
            throw new IllegalArgumentException("slot must be between 0 and " + (getSize() - 1) + " but was " + slot);
        }
    }

    private void putClick(int slot, Consumer<C> action, Object key) {
        checkSlot(slot);

        if (action == null) {
            actions.remove(slot);
            return;
        }
        actions.put(slot, new SlotAction<>(action, key == null ? action : key));
    }

    private Consumer<C> ignoreClick(Runnable action) {
        Objects.requireNonNull(action, "action");
        return click -> action.run();
    }

    private boolean hasRefreshCode() {
        return refreshAction != null || refreshOverridden;
    }

    private boolean overridesRefreshItems() {
        for (Class<?> current = getClass(); current != AbstractMenu.class; current = current.getSuperclass()) {
            try {
                current.getDeclaredMethod("refreshItems");
                return true;

            } catch (NoSuchMethodException ignored) {
            }
        }
        return false;
    }

    private void checkGrid() {
        if (!type.isGrid()) {
            throw new IllegalStateException(type + " is not a grid menu");
        }
    }

    private void startRefresh() {
        if (refreshInterval == null || refreshTask != null || !hasRefreshCode()) {
            return;
        }

        final MenuScheduler scheduler = getScheduler();

        if (scheduler == null) {
            throw new IllegalStateException("this menu has no scheduler, so it cannot refresh on a timer");
        }
        this.refreshTask = scheduler.schedule(this::refresh, refreshInterval);
    }

    private void stopRefresh() {
        if (refreshTask == null) {
            return;
        }

        refreshTask.cancel();
        this.refreshTask = null;
    }

    private record SlotAction<C>(Consumer<C> action, Object key) {
    }
}
