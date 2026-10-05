package com.github.thedumbledodo.blueprint.fixture;

import com.github.thedumbledodo.blueprint.annotation.BlueprintComponent;

@BlueprintComponent
public final class FailingComponent {

    public FailingComponent() {
        throw new IllegalStateException("database offline");
    }
}
