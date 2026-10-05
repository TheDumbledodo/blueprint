package com.github.thedumbledodo.blueprint.menu.builder;

import com.github.thedumbledodo.blueprint.chat.Text;
import com.github.thedumbledodo.blueprint.menu.component.ExecuteComponent;
import com.github.thedumbledodo.blueprint.menu.layout.MenuLayout;
import com.github.thedumbledodo.blueprint.menu.model.AbstractMenu;
import com.github.thedumbledodo.blueprint.menu.model.GuiAction;
import com.github.thedumbledodo.blueprint.menu.model.MenuType;
import com.github.thedumbledodo.blueprint.menu.model.Slots;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class MenuBuilder<M extends AbstractMenu<?, I, C>, I, C extends ExecuteComponent> {

    private final BiFunction<MenuType, Component, M> factory;
    private final List<Consumer<M>> steps = new ArrayList<>();

    private MenuType type = MenuType.of(3);
    private Component title = Component.empty();

    public MenuBuilder(BiFunction<MenuType, Component, M> factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public MenuBuilder<M, I, C> rows(int rows) {
        return type(MenuType.of(rows));
    }

    public MenuBuilder<M, I, C> type(MenuType type) {
        this.type = Objects.requireNonNull(type, "type");
        return this;
    }

    public MenuBuilder<M, I, C> title(String miniMessage) {
        return title(Text.translate(miniMessage));
    }

    public MenuBuilder<M, I, C> title(Component title) {
        this.title = title;
        return this;
    }

    public MenuBuilder<M, I, C> layout(String... rows) {
        return apply(menu -> menu.setLayout(MenuLayout.of(rows)));
    }

    public MenuBuilder<M, I, C> item(int slot, I item) {
        return apply(menu -> menu.setItem(slot, item));
    }

    public MenuBuilder<M, I, C> item(Slots slots, I item) {
        return apply(menu -> menu.setItem(slots, item));
    }

    public MenuBuilder<M, I, C> item(char symbol, I item) {
        return apply(menu -> menu.setItem(symbol, item));
    }

    public MenuBuilder<M, I, C> onClick(int slot, Consumer<C> action) {
        return apply(menu -> menu.onClick(slot, action));
    }

    public MenuBuilder<M, I, C> onClick(Slots slots, Consumer<C> action) {
        return apply(menu -> menu.onClick(slots, action));
    }

    public MenuBuilder<M, I, C> onClick(char symbol, Consumer<C> action) {
        return apply(menu -> menu.onClick(symbol, action));
    }

    public MenuBuilder<M, I, C> onAnyClick(Consumer<C> action) {
        return apply(menu -> menu.onAnyClick(action));
    }

    public MenuBuilder<M, I, C> fill(I item) {
        return apply(menu -> menu.fill(item));
    }

    public MenuBuilder<M, I, C> fillEmpty(I item) {
        return apply(menu -> menu.fillEmpty(item));
    }

    public MenuBuilder<M, I, C> fillBorder(I item) {
        return apply(menu -> menu.fillBorder(item));
    }

    public MenuBuilder<M, I, C> onOpen(GuiAction action) {
        return apply(menu -> menu.onOpen(action));
    }

    public MenuBuilder<M, I, C> onClose(GuiAction action) {
        return apply(menu -> menu.onClose(action));
    }

    public MenuBuilder<M, I, C> onRefresh(Consumer<M> action) {
        Objects.requireNonNull(action, "action");
        return apply(menu -> menu.onRefresh(ignored -> action.accept(menu)));
    }

    public MenuBuilder<M, I, C> refresh(Duration interval) {
        return apply(menu -> menu.setRefresh(interval));
    }

    public MenuBuilder<M, I, C> apply(Consumer<M> step) {
        steps.add(Objects.requireNonNull(step, "step"));
        return this;
    }

    public M build() {
        final M menu = factory.apply(type, title);

        menu.setItems(() -> {
            for (Consumer<M> step : steps) {
                step.accept(menu);
            }
        });
        return menu;
    }
}
