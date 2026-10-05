package com.github.thedumbledodo.blueprint.config.json;

import com.google.gson.*;
import com.github.thedumbledodo.blueprint.config.ConfigException;
import com.github.thedumbledodo.blueprint.config.format.ConfigFormat;
import com.github.thedumbledodo.blueprint.config.model.ConfigSection;

import java.math.BigDecimal;
import java.util.*;

public final class JsonFormat implements ConfigFormat {

    private static final List<String> EXTENSIONS = List.of("json");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    @Override
    public List<String> getExtensions() {
        return EXTENSIONS;
    }

    @Override
    public Map<String, Object> read(String content) {
        if (content == null || content.isBlank()) {
            return new LinkedHashMap<>();
        }

        final JsonElement element;

        try {
            element = JsonParser.parseString(content);

        } catch (JsonParseException exception) {
            throw new ConfigException("invalid JSON: " + exception.getMessage(), exception);
        }

        if (element.isJsonNull()) {
            return new LinkedHashMap<>();
        }

        if (!element.isJsonObject()) {
            throw new ConfigException("expected an object at the top of the file but got " + element);
        }
        return toMap(element.getAsJsonObject());
    }

    @Override
    public String write(ConfigSection section) {
        return GSON.toJson(section.toMap()) + "\n";
    }

    private Map<String, Object> toMap(JsonObject object) {
        final Map<String, Object> map = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            map.put(entry.getKey(), toValue(entry.getValue()));
        }
        return map;
    }

    private Object toValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonObject()) {
            return toMap(element.getAsJsonObject());
        }

        if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final List<Object> list = new ArrayList<>(array.size());

            for (JsonElement child : array) {
                list.add(toValue(child));
            }
            return list;
        }

        final JsonPrimitive primitive = element.getAsJsonPrimitive();

        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }

        if (primitive.isNumber()) {
            return toNumber(primitive.getAsString());
        }
        return primitive.getAsString();
    }

    private Number toNumber(String text) {
        if (text.contains(".") || text.contains("e") || text.contains("E")) {
            return Double.parseDouble(text);
        }

        final BigDecimal decimal = new BigDecimal(text);

        try {
            return decimal.intValueExact();

        } catch (ArithmeticException exception) {
            try {
                return decimal.longValueExact();

            } catch (ArithmeticException ignored) {
                return decimal.toBigIntegerExact();
            }
        }
    }
}
