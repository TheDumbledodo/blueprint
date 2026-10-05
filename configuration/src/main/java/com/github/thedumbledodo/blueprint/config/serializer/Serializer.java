package com.github.thedumbledodo.blueprint.config.serializer;

public interface Serializer<T, S> {

    S serialize(T element);

    T deserialize(S element);
}
