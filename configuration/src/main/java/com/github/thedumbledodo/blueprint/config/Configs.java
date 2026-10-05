package com.github.thedumbledodo.blueprint.config;

import com.github.thedumbledodo.blueprint.BlueprintConfiguration;
import com.github.thedumbledodo.blueprint.service.Services;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Configs {

    public <T> T reload(T config) {
        return getConfiguration().load(config);
    }

    public void save(Object config) {
        getConfiguration().save(config);
    }

    private BlueprintConfiguration getConfiguration() {
        final BlueprintConfiguration configuration = Services.getService(BlueprintConfiguration.class);

        if (configuration == null) {
            throw new IllegalStateException("BlueprintConfiguration is not installed. Call Blueprint.register(...) before using configs.");
        }
        return configuration;
    }
}
