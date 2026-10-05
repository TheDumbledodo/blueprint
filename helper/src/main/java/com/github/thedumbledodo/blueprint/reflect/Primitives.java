package com.github.thedumbledodo.blueprint.reflect;

import lombok.experimental.UtilityClass;

import java.lang.invoke.MethodType;

@UtilityClass
public class Primitives {

    public Class<?> wrap(Class<?> type) {
        return MethodType.methodType(type).wrap().returnType();
    }
}
