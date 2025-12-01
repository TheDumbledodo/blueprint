package io.github.dumbledodo.blueprint.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuType {
    GENERIC_9X1(0, 9),
    GENERIC_9X2(1, 18),
    GENERIC_9X3(2, 27),
    GENERIC_9X4(3, 36),
    GENERIC_9X5(4, 45),
    GENERIC_9X6(5, 54),

    GENERIC_3X3(6, 9),
    CRAFTER_3X3(7, 10),

    ANVIL(8, 3),
    BEACON(9, 1),
    BLAST_FURNACE(10, 3),
    BREWING_STAND(11, 4),
    CRAFTING_TABLE(12, 10),
    ENCHANTMENT_TABLE(13, 2),
    FURNACE(14, 3),
    GRINDSTONE(15, 3),
    HOPPER(16, 5),
    LECTERN(17, 0),
    LOOM(18, 4),
    VILLAGER(19, 3),
    SHULKER_BOX(20, 27),
    SMITHING_TABLE(21, 4),
    SMOKER(22, 3),
    CARTOGRAPHY_TABLE(23, 3),
    STONECUTTER(24, 2);

    private final int id;
    private final int size;

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
}