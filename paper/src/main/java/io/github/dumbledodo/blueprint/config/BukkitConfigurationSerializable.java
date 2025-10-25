package io.github.dumbledodo.blueprint.config;

import de.exlll.configlib.Serializer;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class BukkitConfigurationSerializable implements Serializer<ConfigurationSerializable, String> {

    private final Yaml yaml;

    public BukkitConfigurationSerializable() {
        final DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(FlowStyle.BLOCK);

        this.yaml = new Yaml(new YamlConstructor(), new YamlRepresenter(), options);
    }

    @Override
    public String serialize(ConfigurationSerializable element) {
        return yaml.dump(element);
    }

    @Override
    public ConfigurationSerializable deserialize(String element) {
        return yaml.load(element);
    }
}