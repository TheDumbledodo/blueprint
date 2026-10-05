package com.github.thedumbledodo.blueprint.chat;

public class SmallCaps {

    private static final String FROM = "qwertyuiopasdfghjklzcvbnm0123456789:";
    private static final String TO = "ǫᴡᴇʀᴛʏᴜɪᴏᴘᴀꜱᴅꜰɢʜᴊᴋʟᴢᴄᴠʙɴᴍ₀₁₂₃₄₅₆₇₈₉︰";

    private static final char[] TABLE = createTable();

    public static String translate(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        final char[] characters = input.toLowerCase().toCharArray();

        for (int i = 0; i < characters.length; i++) {
            final char character = characters[i];

            if (character >= TABLE.length) {
                continue;
            }
            characters[i] = TABLE[character];
        }
        return new String(characters);
    }

    private static char[] createTable() {
        final char[] table = new char[128];

        for (int i = 0; i < table.length; i++) {
            table[i] = (char) i;
        }

        for (int i = 0; i < FROM.length(); i++) {
            table[FROM.charAt(i)] = TO.charAt(i);
        }
        return table;
    }
}
