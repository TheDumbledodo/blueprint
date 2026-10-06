package com.github.thedumbledodo.blueprint.menu.pagination;

import com.github.thedumbledodo.blueprint.menu.TestMenu;
import com.github.thedumbledodo.blueprint.menu.layout.MenuLayout;
import com.github.thedumbledodo.blueprint.menu.model.Slots;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PaginationTest {

    private static final Slots SLOTS = Slots.of(0, 1, 2, 3);

    private final TestMenu menu = new TestMenu(1);

    private Pagination<String> paginate(List<String> entries) {
        return menu.paginate(entries)
                .setSlots(SLOTS)
                .setIcon(entry -> entry)
                .build();
    }

    private static List<String> entries(int count) {
        return IntStream.range(0, count).mapToObj(i -> "item" + i).toList();
    }

    @Test
    void pageCountIncludesThePartialLastPage() {
        final Pagination<String> pagination = paginate(entries(10));

        assertEquals(3, pagination.getMaxPage());

        pagination.setPage(3);

        assertEquals(List.of("item8", "item9"), pagination.getPageEntries());
        assertEquals("item8", menu.getItem(0).orElseThrow());
        assertTrue(menu.getItem(2).isEmpty());
        assertTrue(menu.getItem(3).isEmpty());
    }

    @Test
    void emptyListHasOnePageAndNoItems() {
        final Pagination<String> pagination = paginate(List.of());

        assertEquals(1, pagination.getMaxPage());
        assertFalse(pagination.hasNext());
        assertFalse(pagination.hasPrevious());
        assertTrue(menu.getItems().isEmpty());
    }

    @Test
    void exactMultipleDoesNotAddAnEmptyPage() {
        assertEquals(2, paginate(entries(8)).getMaxPage());
    }

    @Test
    void nextAndPreviousStopAtTheEdges() {
        final Pagination<String> pagination = paginate(entries(6));

        pagination.previous();
        assertEquals(1, pagination.getPage());

        pagination.next();
        pagination.next();
        assertEquals(2, pagination.getPage());
        assertEquals("item4", menu.getItem(0).orElseThrow());
    }

    @Test
    void setPageClampsToTheValidRange() {
        final Pagination<String> pagination = paginate(entries(6));

        pagination.setPage(99);
        assertEquals(2, pagination.getPage());

        pagination.setPage(-3);
        assertEquals(1, pagination.getPage());
    }

    @Test
    void shrinkingTheEntriesClampsThePage() {
        final Pagination<String> pagination = paginate(entries(10));

        pagination.setPage(3);
        pagination.setEntries(entries(3));

        assertEquals(1, pagination.getPage());
        assertEquals("item0", menu.getItem(0).orElseThrow());
        assertTrue(menu.getItem(3).isEmpty());
    }

    @Test
    void supplierIsReadOnEveryRender() {
        final List<String> live = new ArrayList<>(entries(2));
        final Pagination<String> pagination = menu.paginate(() -> live).setSlots(SLOTS).setIcon(entry -> entry).build();

        live.add("late");
        pagination.render();

        assertEquals("late", menu.getItem(2).orElseThrow());
    }

    @Test
    void turningAPageRendersItsSlots() {
        final Pagination<String> pagination = paginate(entries(10));

        menu.resetCounters();
        pagination.next();

        assertEquals(List.of(0, 1, 2, 3), menu.getRenderedSlots());
    }

    @Test
    void listenersRunAfterEveryRender() {
        final Pagination<String> pagination = paginate(entries(10));
        final List<Integer> pages = new ArrayList<>();

        pagination.onPageChange(current -> pages.add(current.getPage()));
        pagination.next();
        pagination.next();

        assertEquals(List.of(2, 3), pages);
    }

    @Test
    void layoutSymbolDecidesTheSlots() {
        final TestMenu menu = new TestMenu(2);

        menu.setLayout(MenuLayout.of(
                "#xxxxxxx#",
                "###<#>###"));

        final Pagination<String> pagination = menu.paginate(entries(9)).setSlots('x').setIcon(entry -> entry).build();

        assertEquals(7, pagination.getPageSize());
        assertEquals("item0", menu.getItem(1).orElseThrow());
        assertTrue(menu.getItem(0).isEmpty());
    }

    @Test
    void paginationNeedsSlots() {
        assertThrows(IllegalArgumentException.class, () -> menu.paginate(List.<String>of()).setSlots(Slots.of()).setIcon(entry -> entry).build());
        assertThrows(IllegalStateException.class, () -> menu.paginate(List.of("a")).setIcon(entry -> entry).build());
        assertThrows(IllegalStateException.class, () -> menu.paginate(List.of("a")).setSlots(SLOTS).build());
    }
}
