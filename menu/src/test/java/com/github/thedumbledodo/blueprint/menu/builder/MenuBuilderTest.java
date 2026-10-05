package com.github.thedumbledodo.blueprint.menu.builder;

import com.github.thedumbledodo.blueprint.menu.TestClick;
import com.github.thedumbledodo.blueprint.menu.TestMenu;
import com.github.thedumbledodo.blueprint.menu.model.MenuType;
import com.github.thedumbledodo.blueprint.menu.model.Slots;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MenuBuilderTest {

    private final UUID viewer = UUID.randomUUID();

    private MenuBuilder<TestMenu, String, TestClick> builder() {
        return new MenuBuilder<>((type, title) -> {
            final TestMenu menu = new TestMenu(type);

            menu.setTitle(title);
            return menu;
        });
    }

    @Test
    void buildsTheMenuWithEverythingApplied() {
        final List<String> calls = new ArrayList<>();

        final TestMenu menu = builder()
                .rows(2)
                .title(Component.text("Shop of Steve"))
                .fillBorder("glass")
                .item(10, "diamond")
                .onClick(Slots.of(10, 11), click -> calls.add("bought " + click.slot()))
                .onClose(uuid -> calls.add("closed"))
                .build();

        assertEquals(MenuType.of(2), menu.getType());
        assertEquals(Component.text("Shop of Steve"), menu.getTitle());
        assertEquals("glass", menu.getItem(0).orElseThrow());
        assertEquals("diamond", menu.getItem(10).orElseThrow());
        assertEquals(1, menu.getRenderBatches().size());

        menu.open(viewer);
        menu.handleClick(TestClick.left(viewer, 10));
        menu.handleClick(TestClick.left(viewer, 11));
        menu.close(viewer);

        assertEquals(List.of("bought 10", "bought 11", "closed"), calls);
    }

    @Test
    void everyBuildCreatesANewMenu() {
        final MenuBuilder<TestMenu, String, TestClick> builder = builder().item(0, "a");

        assertNotSame(builder.build(), builder.build());
    }

    @Test
    void layoutSymbolsCanBeUsed() {
        final TestMenu menu = builder()
                .rows(1)
                .layout("#   x   #")
                .item('#', "glass")
                .item('x', "center")
                .build();

        assertEquals("glass", menu.getItem(8).orElseThrow());
        assertEquals("center", menu.getItem(4).orElseThrow());
    }

    @Test
    void refreshCodeRunsWhenTheMenuOpens() {
        final TestMenu menu = builder()
                .rows(1)
                .onRefresh(current -> current.setItem(0, "fresh"))
                .build();

        assertTrue(menu.getItem(0).isEmpty());

        menu.open(viewer);

        assertEquals("fresh", menu.getItem(0).orElseThrow());
    }
}
