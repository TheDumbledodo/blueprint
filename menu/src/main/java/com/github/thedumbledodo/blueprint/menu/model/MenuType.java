package com.github.thedumbledodo.blueprint.menu.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuType {
    GENERIC_9X1(9, 9),
    GENERIC_9X2(18, 9),
    GENERIC_9X3(27, 9),
    GENERIC_9X4(36, 9),
    GENERIC_9X5(45, 9),
    GENERIC_9X6(54, 9),

    GENERIC_3X3(9, 3),
    CRAFTER_3X3(10, 3),

    ANVIL(3, 3),
    BEACON(1, 1),
    BLAST_FURNACE(3, 3),
    BREWING_STAND(5, 5),
    CRAFTING_TABLE(10, 10),
    ENCHANTMENT_TABLE(2, 2),
    FURNACE(3, 3),
    GRINDSTONE(3, 3),
    HOPPER(5, 5),
    LECTERN(1, 1),
    LOOM(4, 4),
    VILLAGER(3, 3),
    SHULKER_BOX(27, 9),
    SMITHING_TABLE(4, 4),
    SMOKER(3, 3),
    CARTOGRAPHY_TABLE(3, 3),
    STONECUTTER(2, 2);

    private final int size;
    private final int columns;

    public static MenuType of(int rows) {
        return switch (rows) {
            case 1 -> MenuType.GENERIC_9X1;
            case 2 -> MenuType.GENERIC_9X2;
            case 3 -> MenuType.GENERIC_9X3;
            case 4 -> MenuType.GENERIC_9X4;
            case 5 -> MenuType.GENERIC_9X5;

            default -> MenuType.GENERIC_9X6;
        };
    }

    public int getRows() {
        return size / columns;
    }

    public boolean isGrid() {
        return columns > 1 && size % columns == 0;
    }
}
