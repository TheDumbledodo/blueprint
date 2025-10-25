package io.github.dumbledodo.blueprint;

import com.github.dumbledodo.blueprint.lifecycle.ComponentRegistry;
import com.github.dumbledodo.blueprint.loader.BlueprintLoader;
import com.github.dumbledodo.blueprint.service.Services;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import io.github.dumbledodo.blueprint.config.AbstractConfig;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter @Builder
public class BlueprintConfiguration {

    @Default
    private Path configDirectory = Paths.get(".");

    @Default
    private YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().build();

    public void register(Object main) {
        Services.register(BlueprintConfiguration.class, this);

        ComponentRegistry.registerListener((type, instance) -> {

            if (instance instanceof AbstractConfig config) {

                final Path configFile = configDirectory
                        .resolve(config.getPath())
                        .resolve(config.getFileName());

                try {
                    return YamlConfigurations.update(configFile, type, properties);

                } catch (Exception throwable) {
                    throwable.printStackTrace();
                }
            }
            return instance;
        });

        BlueprintLoader.loadComponents(main.getClass());
    }
}
