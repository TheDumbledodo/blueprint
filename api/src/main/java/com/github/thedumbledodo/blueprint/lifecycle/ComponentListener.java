package com.github.thedumbledodo.blueprint.lifecycle;

public interface ComponentListener {

    Object onComponentRegistered(Class<?> type, Object instance);
}
