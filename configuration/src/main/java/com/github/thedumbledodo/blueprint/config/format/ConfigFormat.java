package com.github.thedumbledodo.blueprint.config.format;

import com.github.thedumbledodo.blueprint.config.model.ConfigSection;

import java.util.List;
import java.util.Map;

public interface ConfigFormat {

    List<String> getExtensions();

    Map<String, Object> read(String content);

    String write(ConfigSection section);
}
