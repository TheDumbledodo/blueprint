package io.github.dumbledodo.blueprint.chat;

import java.util.HashMap;
import java.util.Map;

public class SmallCaps {

    private static final Map<Character, Character> CHAR_MAP = new HashMap<>() {{
        put('q', 'ǫ');
        put('w', 'ᴡ');
        put('e', 'ᴇ');
        put('r', 'ʀ');
        put('t', 'ᴛ');
        put('y', 'ʏ');
        put('u', 'ᴜ');
        put('i', 'ɪ');
        put('o', 'ᴏ');
        put('p', 'ᴘ');
        put('a', 'ᴀ');
        put('s', 'ꜱ');
        put('d', 'ᴅ');
        put('f', 'ꜰ');
        put('g', 'ɢ');
        put('h', 'ʜ');
        put('j', 'ᴊ');
        put('k', 'ᴋ');
        put('l', 'ʟ');
        put('z', 'ᴢ');
        put('c', 'ᴄ');
        put('v', 'ᴠ');
        put('b', 'ʙ');
        put('n', 'ɴ');
        put('m', 'ᴍ');
        put('0', '₀');
        put('1', '₁');
        put('2', '₂');
        put('3', '₃');
        put('4', '₄');
        put('5', '₅');
        put('6', '₆');
        put('7', '₇');
        put('8', '₈');
        put('9', '₉');
        put(':', '︰');

    }};

    public static String translate(String input) {
        final StringBuilder translated = new StringBuilder();

        for (char character : input.toLowerCase().toCharArray()) {
            translated.append(CHAR_MAP.getOrDefault(character, character));
        }
        return translated.toString();
    }
}