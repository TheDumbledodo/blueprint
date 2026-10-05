package com.github.thedumbledodo.blueprint.menu.model;

import com.github.thedumbledodo.blueprint.menu.TestClick;
import com.github.thedumbledodo.blueprint.menu.TestMenu;
import com.github.thedumbledodo.blueprint.menu.layout.MenuLayout;
import com.github.thedumbledodo.blueprint.menu.pagination.Pagination;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class AbstractMenuTest {

    private final UUID viewer = UUID.randomUUID();

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void fillBorderTouchesOnlyBorderSlots(int rows) {
        final TestMenu menu = new TestMenu(rows);

        menu.fillBorder("border");

        final int expected = rows <= 2 ? rows * 9 : 18 + (rows - 2) * 2;

        assertEquals(expected, menu.getItems().size());

        for (int slot : menu.getItems().keySet()) {
            final int row = slot / 9;
            final int column = slot % 9;

            assertTrue(row == 0 || row == rows - 1 || column == 0 || column == 8, "slot " + slot + " is not a border slot");
        }
    }

    @Test
    void fillEmptyNeverOverwrites() {
        final TestMenu menu = new TestMenu(1);

        menu.setItem(4, "button");
        menu.fillEmpty("glass");

        assertEquals("button", menu.getItem(4).orElseThrow());
        assertEquals(9, menu.getItems().size());
    }

    @Test
    void fillRowAndColumn() {
        final TestMenu menu = new TestMenu(3);

        menu.fillRow(1, "row");
        menu.fillColumn(0, "column");

        assertEquals("column", menu.getItem(9).orElseThrow());
        assertEquals("row", menu.getItem(13).orElseThrow());
        assertEquals("column", menu.getItem(18).orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> menu.fillRow(3, "x"));
    }

    @Test
    void gridHelpersRejectNonGridMenus() {
        assertThrows(IllegalStateException.class, () -> new TestMenu(MenuType.CRAFTER_3X3).fillBorder("x"));
    }

    @Test
    void setItemRendersOnlyThatSlot() {
        final TestMenu menu = new TestMenu(1);

        menu.setItem(3, "item");

        assertEquals(List.of(3), menu.getRenderedSlots());
        assertEquals(0, menu.getUpdates());
    }

    @Test
    void setItemsSendsOnlyTheChangedSlotsOnceAtTheEnd() {
        final TestMenu menu = new TestMenu(2);

        menu.setItems(() -> {
            menu.setItem(0, "a");
            menu.setItem(5, "b");
            menu.setItem(0, "c");
        });

        assertEquals(List.of(List.of(0, 5)), menu.getRenderBatches());
        assertEquals("c", menu.getItem(0).orElseThrow());
    }

    @Test
    void setItemsWithoutChangesSendsNothing() {
        final TestMenu menu = new TestMenu(1);

        menu.setItems(() -> {
        });

        assertTrue(menu.getRenderBatches().isEmpty());
        assertTrue(menu.getRenderedSlots().isEmpty());
    }

    @Test
    void slotsOutsideTheMenuAreRejected() {
        final TestMenu menu = new TestMenu(1);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> menu.setItem(9, "x"));

        assertEquals("slot must be between 0 and 8 but was 9", exception.getMessage());
        assertThrows(IllegalArgumentException.class, () -> menu.setItem(-1, "x"));
        assertThrows(IllegalArgumentException.class, () -> menu.onClick(9, click -> {
        }));
    }

    @Test
    void oneActionCanRunOnManySlots() {
        final TestMenu menu = new TestMenu(1);
        final List<Integer> clicked = new ArrayList<>();

        menu.onClick(Slots.of(1, 2, 4, 5), click -> clicked.add(click.slot()));

        menu.handleClick(TestClick.left(viewer, 1));
        menu.handleClick(TestClick.left(viewer, 3));
        menu.handleClick(TestClick.left(viewer, 5));

        assertEquals(List.of(1, 5), clicked);
    }

    @Test
    void itemsAndActionsAreIndependent() {
        final TestMenu menu = new TestMenu(1);
        final AtomicInteger clicks = new AtomicInteger();

        menu.onClick(0, click -> clicks.incrementAndGet());
        menu.setItem(0, "before");
        menu.setItem(0, "after");
        menu.removeItem(0);
        menu.handleClick(TestClick.left(viewer, 0));

        assertEquals(1, clicks.get());
        assertTrue(menu.getItem(0).isEmpty());

        menu.setItem(0, "item");
        menu.removeClick(0);
        menu.handleClick(TestClick.left(viewer, 0));

        assertEquals(1, clicks.get());
        assertEquals("item", menu.getItem(0).orElseThrow());
        assertFalse(menu.hasClick(0));
    }

    @Test
    void anyClickRunsBeforeTheSlotAction() {
        final TestMenu menu = new TestMenu(1);
        final List<String> calls = new ArrayList<>();

        menu.onAnyClick(click -> calls.add("any " + click.slot()));
        menu.onClick(2, click -> calls.add("slot"));

        menu.handleClick(TestClick.left(viewer, 2));
        menu.handleClick(TestClick.left(viewer, 5));

        assertEquals(List.of("any 2", "slot", "any 5"), calls);
    }

    @Test
    void changesMadeInAClickAreSentRightAway() {
        final TestMenu menu = new TestMenu(1);

        menu.onClick(0, click -> {
            menu.setItem(1, "a");
            menu.setItem(2, "b");
        });
        menu.handleClick(TestClick.left(viewer, 0));

        assertEquals(List.of(1, 2), menu.getRenderedSlots());
        assertTrue(menu.getRenderBatches().isEmpty());
    }

    @Test
    void updateWithChangesSendsTheWholeWindowOnce() {
        final TestMenu menu = new TestMenu(2);

        menu.update(() -> {
            menu.fillBorder("glass");
            menu.setItem(4, "center");
        });

        assertEquals(1, menu.getUpdates());
        assertTrue(menu.getRenderedSlots().isEmpty());
        assertEquals("center", menu.getItem(4).orElseThrow());
    }

    @Test
    void refreshRunsEveryTimeItIsCalled() {
        final TestMenu menu = new TestMenu(1);
        final AtomicInteger runs = new AtomicInteger();

        menu.onRefresh(current -> runs.incrementAndGet());
        menu.refresh();
        menu.refresh();

        assertEquals(2, runs.get());
    }

    @Test
    void cooldownIsSharedByTheSameActionAndTracksTimeLeft() {
        final TestMenu menu = new TestMenu(1);
        final List<String> calls = new ArrayList<>();
        final List<Duration> remaining = new ArrayList<>();

        menu.onClick(Slots.of(0, 1), click -> {
            if (!click.cooldown(Duration.ofHours(1))) {
                remaining.add(click.remainingCooldown());
                return;
            }
            calls.add("claimed " + click.slot());
        });
        menu.onClick(2, click -> {
            if (click.cooldown(Duration.ofHours(1))) {
                calls.add("other");
            }
        });

        menu.handleClick(TestClick.left(viewer, 0));
        menu.handleClick(TestClick.left(viewer, 1));
        menu.handleClick(TestClick.left(viewer, 2));
        menu.handleClick(TestClick.left(UUID.randomUUID(), 1));

        assertEquals(List.of("claimed 0", "other", "claimed 1"), calls);
        assertEquals(1, remaining.size());
        assertTrue(remaining.getFirst().compareTo(Duration.ofMinutes(59)) > 0);
    }

    @Test
    void cooldownOutsideAClickActionFails() {
        assertThrows(IllegalStateException.class, () -> TestClick.left(viewer, 0).cooldown(Duration.ofSeconds(1)));
        assertEquals(Duration.ZERO, TestClick.left(viewer, 0).remainingCooldown());
    }

    @Test
    void clearRemovesItemsAndActions() {
        final TestMenu menu = new TestMenu(1);

        menu.setItem(0, "item");
        menu.onClick(0, click -> fail("cleared action ran"));
        menu.clear();
        menu.handleClick(TestClick.left(viewer, 0));

        assertTrue(menu.getItems().isEmpty());
    }

    @Test
    void layoutSymbolsSetItemsAndActions() {
        final TestMenu menu = new TestMenu(2);
        final AtomicInteger clicks = new AtomicInteger();

        menu.setLayout(MenuLayout.of(
                "#########",
                "#   x   #"));
        menu.setItem('#', "glass");
        menu.onClick('x', click -> clicks.incrementAndGet());
        menu.handleClick(TestClick.left(viewer, 13));

        assertEquals(11, menu.getItems().size());
        assertEquals(1, clicks.get());
    }

    @Test
    void navigationHidesAtTheEdgesAndTurnsPages() {
        final TestMenu menu = new TestMenu(2);

        menu.setLayout(MenuLayout.of(
                "xxxxxxxxx",
                "###<#>###"
        ));

        final Pagination<String> pagination = menu.paginate('x', IntStream.range(0, 20)
                .mapToObj(i -> "item" + i)
                .toList(), entry -> entry, null);

        menu.setPreviousPage(pagination, '<', "previous");
        menu.setNextPage(pagination, '>', "next");

        assertTrue(menu.getItem(12).isEmpty());
        assertFalse(menu.hasClick(12));
        assertEquals("next", menu.getItem(14).orElseThrow());

        menu.handleClick(TestClick.left(viewer, 14));

        assertEquals(2, pagination.getPage());
        assertEquals("previous", menu.getItem(12).orElseThrow());
        assertEquals("item9", menu.getItem(0).orElseThrow());

        menu.handleClick(TestClick.left(viewer, 14));

        assertEquals(3, pagination.getPage());
        assertTrue(menu.getItem(14).isEmpty());
        assertFalse(menu.hasClick(14));
    }

    @Test
    void paginatedEntriesGetTheirOwnClick() {
        final TestMenu menu = new TestMenu(2);
        final List<String> bought = new ArrayList<>();
        final Pagination<String> pagination = menu.paginate(Slots.of(0, 1, 2), List.of("a", "b", "c", "d"),
                entry -> "icon " + entry, (click, entry) -> bought.add(entry));

        menu.setPreviousPage(pagination, 9, "previous", "none");
        menu.setNextPage(pagination, 17, "next");

        assertEquals("icon a", menu.getItem(0).orElseThrow());
        assertEquals("none", menu.getItem(9).orElseThrow());
        assertFalse(menu.hasClick(9));

        menu.handleClick(TestClick.left(viewer, 1));
        menu.handleClick(TestClick.left(viewer, 17));
        menu.handleClick(TestClick.left(viewer, 0));
        menu.handleClick(TestClick.left(viewer, 1));

        assertEquals(List.of("b", "d"), bought);
        assertEquals("previous", menu.getItem(9).orElseThrow());
        assertTrue(menu.getItem(17).isEmpty());
        assertFalse(menu.hasClick(1));
    }

    @Test
    void severalPaginationsTurnIndependently() {
        final TestMenu menu = new TestMenu(2);
        final Pagination<String> first = menu.paginate(Slots.range(0, 1), List.of("a", "b", "c"), entry -> entry, null);
        final Pagination<String> second = menu.paginate(Slots.range(9, 10), List.of("x", "y", "z"), entry -> entry, null);

        menu.setNextPage(first, 8, "next");
        menu.setNextPage(second, 17, "next");
        menu.handleClick(TestClick.left(viewer, 17));

        assertEquals(1, first.getPage());
        assertEquals(2, second.getPage());
        assertEquals("a", menu.getItem(0).orElseThrow());
        assertEquals("z", menu.getItem(9).orElseThrow());
    }

    @Test
    void paginationsCannotShareSlots() {
        final TestMenu menu = new TestMenu(2);

        menu.paginate(Slots.range(0, 4), List.of("a"), entry -> entry, null);

        assertThrows(IllegalArgumentException.class, () -> menu.paginate(Slots.range(4, 8), List.of("b"), entry -> entry, null));
    }

    @Test
    void titleIsPushedOnlyWhileOpen() {
        final TestMenu menu = new TestMenu(1);

        menu.setTitle(Component.text("Closed"));
        menu.open(viewer);
        menu.setTitle(Component.text("Open"));

        assertEquals(1, menu.getTitleUpdates());
        assertEquals(Component.text("Open"), menu.getTitle());
    }

    @Test
    void viewersAndHooksAreTracked() {
        final TestMenu menu = new TestMenu(1);
        final List<String> calls = new ArrayList<>();

        menu.onOpen(uuid -> calls.add("open"));
        menu.onClose(uuid -> calls.add("close"));

        menu.open(viewer);
        assertTrue(menu.getViewerIds().contains(viewer));

        menu.close(viewer);
        menu.close(viewer);

        assertFalse(menu.isOpen());
        assertEquals(List.of("open", "close"), calls);
    }

    @Test
    void refreshRunsOnOpenAndOnSchedule() {
        final TestMenu menu = new TestMenu(1);
        final AtomicInteger counter = new AtomicInteger();

        menu.onRefresh(current -> current.setItem(0, "count " + counter.incrementAndGet()));
        menu.setRefresh(Duration.ofSeconds(1));

        assertTrue(menu.getScheduledTasks().isEmpty());

        menu.open(viewer);
        assertEquals("count 1", menu.getItem(0).orElseThrow());

        menu.runScheduledTasks();
        assertEquals("count 2", menu.getItem(0).orElseThrow());

        menu.close(viewer);
        assertEquals(1, menu.getCancelledTasks());
    }

    @Test
    void refreshWithoutCodeDoesNothing() {
        final TestMenu menu = new TestMenu(1);

        menu.setRefresh(Duration.ofSeconds(1));
        menu.open(viewer);
        menu.refresh();

        assertTrue(menu.getScheduledTasks().isEmpty());
        assertTrue(menu.getRenderBatches().isEmpty());
    }

    @Test
    void refreshThatSetsNothingSendsNothing() {
        final TestMenu menu = new TestMenu(1);
        final AtomicInteger runs = new AtomicInteger();

        menu.onRefresh(current -> runs.incrementAndGet());
        menu.refresh();

        assertEquals(1, runs.get());
        assertTrue(menu.getRenderBatches().isEmpty());
    }

    @Test
    void overriddenRefreshItemsCountsAsRefreshCode() {
        final AtomicInteger runs = new AtomicInteger();
        final TestMenu menu = new TestMenu(1) {

            @Override
            protected void refreshItems() {
                setItem(0, "run " + runs.incrementAndGet());
            }
        };

        menu.open(viewer);

        assertEquals("run 1", menu.getItem(0).orElseThrow());
    }

    @Test
    void refreshIntervalMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new TestMenu(1).setRefresh(Duration.ZERO));
    }
}
