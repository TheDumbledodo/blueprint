package com.github.thedumbledodo.blueprint.menu.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlotsTest {

    @Test
    void ofKeepsOrderAndDropsDuplicates() {
        assertEquals(List.of(4, 1, 2), Slots.of(4, 1, 4, 2).toList());
    }

    @Test
    void rangeIncludesBothEnds() {
        assertEquals(List.of(9, 10, 11), Slots.range(9, 11).toList());
        assertThrows(IllegalArgumentException.class, () -> Slots.range(5, 2));
    }
}
