package com.github.thedumbledodo.blueprint;

import com.github.thedumbledodo.blueprint.config.ConfigException;
import com.github.thedumbledodo.blueprint.config.ConfigProperties;
import com.github.thedumbledodo.blueprint.config.annotation.Configuration;
import com.github.thedumbledodo.blueprint.config.format.ConfigFormat;
import com.github.thedumbledodo.blueprint.config.mapper.ConfigMapper;
import com.github.thedumbledodo.blueprint.config.model.ConfigSection;
import com.github.thedumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.thedumbledodo.blueprint.service.Services;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Singular;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Getter @Builder
public class BlueprintConfiguration {

    @Default
    private Path configDirectory = Paths.get(".");

    @Default
    private ConfigProperties properties = ConfigProperties.builder().build();

    @Singular
    private List<ConfigFormat> formats;

    @Getter(AccessLevel.NONE)
    private final Map<Object, ConfigSection> defaults = Collections.synchronizedMap(new IdentityHashMap<>());

    public void install() {
        Services.register(BlueprintConfiguration.class, this);

        ComponentRegistry.registerListener((type, instance) -> {

            if (instance.getClass().isAnnotationPresent(Configuration.class)) {
                try {
                    load(instance);

                } catch (ConfigException exception) {
                    exception.printStackTrace();
                }
            }
            return instance;
        });
    }

    public <T> T load(T config) {
        final Path file = getFile(config);
        final ConfigFormat format = getFormat(config);

        final ConfigMapper mapper = new ConfigMapper(properties);
        final ConfigSection defaultSection = defaults.computeIfAbsent(config, mapper::serialize);

        Map<String, Object> values = null;

        if (Files.exists(file)) {
            values = read(file, format);
        }

        try {
            mapper.apply(defaultSection.toMap(), config);

            if (values != null) {
                mapper.apply(values, config);
            }

        } catch (ConfigException exception) {
            throw new ConfigException(file.getFileName() + " -> " + exception.getMessage(), exception);
        }

        write(file, format, mapper.serialize(config));
        return config;
    }

    public void save(Object config) {
        final ConfigMapper mapper = new ConfigMapper(properties);

        write(getFile(config), getFormat(config), mapper.serialize(config));
    }

    public Path getFile(Object config) {
        final Configuration configuration = getAnnotation(config);

        return configDirectory
                .resolve(configuration.path())
                .resolve(configuration.value())
                .normalize();
    }

    public ConfigFormat getFormat(Object config) {
        final String fileName = getAnnotation(config).value();
        final int dot = fileName.lastIndexOf('.');
        final String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);

        for (ConfigFormat format : formats) {
            if (format.getExtensions().contains(extension)) {
                return format;
            }
        }
        throw new ConfigException("No config format registered for \"" + fileName + "\". Registered extensions: " + getExtensions());
    }

    private Configuration getAnnotation(Object config) {
        final Configuration configuration = config.getClass().getAnnotation(Configuration.class);

        if (configuration == null) {
            throw new ConfigException(config.getClass().getName() + " is not annotated with @Configuration");
        }
        return configuration;
    }

    private List<String> getExtensions() {
        return formats.stream()
                .flatMap(format -> format.getExtensions().stream())
                .toList();
    }

    private Map<String, Object> read(Path file, ConfigFormat format) {
        try {
            return format.read(Files.readString(file, StandardCharsets.UTF_8));

        } catch (IOException exception) {
            throw new ConfigException("Could not read " + file, exception);

        } catch (ConfigException exception) {
            throw new ConfigException(file.getFileName() + " -> " + exception.getMessage(), exception);
        }
    }

    private void write(Path file, ConfigFormat format, ConfigSection section) {
        final String content = format.write(section);
        final Path temporary = file.resolveSibling(file.getFileName() + ".tmp");

        try {
            final Path parent = file.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            move(temporary, file);

        } catch (IOException exception) {
            throw new ConfigException("Could not write " + file, exception);
        }
    }

    private void move(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
