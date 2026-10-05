package com.github.thedumbledodo.blueprint.config.mapper;

@FunctionalInterface
public interface NameFormatter {

    NameFormatter IDENTITY = name -> name;
    NameFormatter LOWER_KEBAB = name -> separate(name, '-');
    NameFormatter LOWER_UNDERSCORE = name -> separate(name, '_');

    String format(String name);

    private static String separate(String name, char separator) {
        final StringBuilder builder = new StringBuilder(name.length() + 4);

        for (int i = 0; i < name.length(); i++) {
            final char character = name.charAt(i);

            if (Character.isUpperCase(character) && i > 0) {
                builder.append(separator);
            }
            builder.append(Character.toLowerCase(character));
        }
        return builder.toString();
    }
}
