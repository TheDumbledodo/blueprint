package com.github.thedumbledodo.blueprint.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmallCapsTest {

    @Test
    void lettersAndDigitsAreTranslated() {
        assertEquals("ʙᴇᴅᴡᴀʀꜱ ₀₁", SmallCaps.translate("Bedwars 01"));
    }

    @Test
    void unmappedCharactersAreKept() {
        assertEquals("x-é!", SmallCaps.translate("X-é!"));
    }

    @Test
    void colonIsTranslated() {
        assertEquals("₁₂︰₃₀", SmallCaps.translate("12:30"));
    }

    @Test
    void nullAndEmptyGiveEmptyString() {
        assertEquals("", SmallCaps.translate(null));
        assertEquals("", SmallCaps.translate(""));
    }
}
