package com.github.thedumbledodo.blueprint.fixture;

import com.github.thedumbledodo.blueprint.annotation.BlueprintComponent;

@BlueprintComponent
public final class CycleB {

    public CycleB(CycleA cycleA) {
    }
}
