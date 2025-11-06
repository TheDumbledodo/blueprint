package io.github.dumbledodo.blueprint.config;

import com.github.dumbledodo.blueprint.service.Services;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import io.github.dumbledodo.blueprint.BlueprintConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

@Getter
@RequiredArgsConstructor
public abstract class AbstractConfig {

    private final String fileName;
    private final String path;

    public AbstractConfig(String fileName) {
        this.fileName = fileName;
        this.path = "";
    }

    private Path getConfigFile(BlueprintConfiguration configuration) {
        return configuration.getConfigDirectory()
                .resolve(path)
                .resolve(fileName)
                .normalize();
    }

    public <T> void save() {
        try {
            final BlueprintConfiguration configuration = Services.getService(BlueprintConfiguration.class);

            final YamlConfigurationProperties properties = configuration.getProperties();
            final Path configFile = getConfigFile(configuration);

            YamlConfigurations.save(configFile, (Class<T>) getClass(), (T) this, properties);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void reload() {
        try {
            final Class<? extends AbstractConfig> clazz = getClass();

            final BlueprintConfiguration configuration = Services.getService(BlueprintConfiguration.class);

            final YamlConfigurationProperties properties = configuration.getProperties();
            final Path configFile = getConfigFile(configuration);

            final AbstractConfig config = YamlConfigurations.update(configFile, clazz, properties);

            for (Field field : config.getClass().getDeclaredFields()) {
                final int modifiers = field.getModifiers();

                if (Modifier.isTransient(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }

                field.setAccessible(true);
                field.set(this, field.get(config));
            }
            Services.register(clazz, config);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}