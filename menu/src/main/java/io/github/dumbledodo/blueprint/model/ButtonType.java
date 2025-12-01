package io.github.dumbledodo.blueprint.model;

public enum ButtonType {
    LEFT,
    SHIFT_LEFT,
    RIGHT,
    SHIFT_RIGHT,
    MIDDLE,
    NUMBER_KEY,
    DOUBLE_CLICK,
    SWAP_OFFHAND,
    DROP,
    CONTROL_DROP,
    UNKNOWN;

    public boolean isShiftClick() {
        return this == SHIFT_LEFT || this == SHIFT_RIGHT;
    }
}