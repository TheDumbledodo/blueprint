package com.github.thedumbledodo.blueprint.menu.layout;

import com.github.thedumbledodo.blueprint.menu.TestMenu;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuLayoutTest {

    private final MenuLayout layout = MenuLayout.of(
            "#########",
            "#xxxxxxx#",
            "###<#>###"
    );

    @Test
    void slotsAreFoundPerSymbol() {
        assertEquals(List.of(10, 11, 12, 13, 14, 15, 16), layout.getSlots('x'));
        assertEquals(List.of(21), layout.getSlots('<'));
        assertEquals(List.of(23), layout.getSlots('>'));
        assertEquals(18, layout.getSlots('#').size());
    }

    @Test
    void symbolLookupBySlot() {
        assertEquals('<', layout.getSymbol(21));
        assertEquals('x', layout.getSymbol(10));
        assertThrows(IllegalArgumentException.class, () -> layout.getSymbol(27));
    }

    @Test
    void sizeAndRows() {
        assertEquals(3, layout.getRowCount());
        assertEquals(9, layout.getColumns());
        assertEquals(27, layout.getSize());
    }

    @Test
    void rowsMustHaveTheSameLength() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MenuLayout.of("#########", "####"));

        assertEquals("layout row 2 has 4 characters, expected 9", exception.getMessage());
    }

    @Test
    void emptyLayoutsAreRejected() {
        assertThrows(IllegalArgumentException.class, MenuLayout::of);
        assertThrows(IllegalArgumentException.class, () -> MenuLayout.of(""));
    }

    @Test
    void menuRejectsALayoutOfTheWrongSize() {
        final TestMenu menu = new TestMenu(2);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> menu.setLayout(layout));

        assertEquals("layout is 3x9 but the menu is 2x9", exception.getMessage());
    }

    @Test
    void unknownSymbolAndMissingLayoutAreReported() {
        final TestMenu menu = new TestMenu(3);

        assertThrows(IllegalStateException.class, () -> menu.getSlots('x'));

        menu.setLayout(layout);

        assertThrows(IllegalArgumentException.class, () -> menu.getSlots('?'));
    }
}
