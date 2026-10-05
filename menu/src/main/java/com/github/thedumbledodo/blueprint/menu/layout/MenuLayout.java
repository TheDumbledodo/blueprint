package com.github.thedumbledodo.blueprint.menu.layout;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class MenuLayout {

    private final List<String> rows;
    private final int columns;

    private MenuLayout(List<String> rows) {
        this.rows = List.copyOf(rows);
        this.columns = rows.getFirst().length();
    }

    public static MenuLayout of(String... rows) {
        if (rows == null || rows.length == 0) {
            throw new IllegalArgumentException("a layout needs at least one row");
        }

        final int columns = rows[0] == null ? 0 : rows[0].length();

        if (columns == 0) {
            throw new IllegalArgumentException("layout rows cannot be empty");
        }

        for (int i = 0; i < rows.length; i++) {
            final int length = rows[i] == null ? 0 : rows[i].length();

            if (length != columns) {
                throw new IllegalArgumentException("layout row " + (i + 1) + " has " + length + " characters, expected " + columns);
            }
        }
        return new MenuLayout(List.of(rows));
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getSize() {
        return rows.size() * columns;
    }

    public List<Integer> getSlots(char symbol) {
        final List<Integer> slots = new ArrayList<>();

        for (int row = 0; row < rows.size(); row++) {
            final String line = rows.get(row);

            for (int column = 0; column < columns; column++) {
                if (line.charAt(column) != symbol) {
                    continue;
                }
                slots.add(row * columns + column);
            }
        }
        return slots;
    }

    public char getSymbol(int slot) {
        if (slot < 0 || slot >= getSize()) {
            throw new IllegalArgumentException("slot must be between 0 and " + (getSize() - 1));
        }
        return rows.get(slot / columns).charAt(slot % columns);
    }
}
