package com.github.thedumbledodo.blueprint.config.fixture;

import com.github.thedumbledodo.blueprint.config.ConfigException;
import com.github.thedumbledodo.blueprint.config.format.ConfigFormat;
import com.github.thedumbledodo.blueprint.config.model.ConfigSection;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

@Getter @Setter
public final class InMemoryFormat implements ConfigFormat {

    private boolean failWrites;
    private ConfigSection lastWritten;

    public static Map<String, Object> decode(String content) {
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(content.trim())))) {
            return (Map<String, Object>) input.readObject();

        } catch (IOException | ClassNotFoundException | IllegalArgumentException exception) {
            throw new ConfigException("Invalid in-memory content", exception);
        }
    }

    public static String encode(Map<String, Object> values) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(new LinkedHashMap<>(values));
            output.flush();
            return Base64.getEncoder().encodeToString(bytes.toByteArray());

        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    public static Map<String, Object> readFile(Path file) throws IOException {
        return decode(Files.readString(file, StandardCharsets.UTF_8));
    }

    public static void writeFile(Path file, Map<String, Object> values) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, encode(values), StandardCharsets.UTF_8);
    }

    public static void editFile(Path file, Consumer<Map<String, Object>> editor) throws IOException {
        final Map<String, Object> values = readFile(file);

        editor.accept(values);
        writeFile(file, values);
    }

    @Override
    public List<String> getExtensions() {
        return List.of("mem");
    }

    @Override
    public Map<String, Object> read(String content) {
        if (content.isBlank()) {
            return new LinkedHashMap<>();
        }
        return decode(content);
    }

    @Override
    public String write(ConfigSection section) {
        if (failWrites) {
            throw new ConfigException("write failed on purpose");
        }

        this.lastWritten = section;
        return encode(section.toMap());
    }
}
