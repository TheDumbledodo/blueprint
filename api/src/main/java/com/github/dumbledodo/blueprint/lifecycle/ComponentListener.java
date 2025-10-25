package com.github.dumbledodo.blueprint.lifecycle;

public interface ComponentListener {

    Object onComponentRegistered(Class<?> type, Object instance);
}
