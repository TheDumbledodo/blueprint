package com.github.thedumbledodo.blueprint.config;

import com.github.thedumbledodo.blueprint.config.mapper.NameFormatter;
import com.github.thedumbledodo.blueprint.config.serializer.Serializer;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Getter @Builder(toBuilder = true)
public final class ConfigProperties {

    @Default
    private final boolean inputNulls = true;

    @Default
    private final boolean outputNulls = true;

    @Default
    private final NameFormatter nameFormatter = NameFormatter.IDENTITY;

    @Singular
    private final Map<Class<?>, Serializer<?, ?>> serializers;
}
