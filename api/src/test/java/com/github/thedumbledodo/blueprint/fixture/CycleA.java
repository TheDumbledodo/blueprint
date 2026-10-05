package com.github.thedumbledodo.blueprint.fixture;

import com.github.thedumbledodo.blueprint.annotation.BlueprintComponent;

@BlueprintComponent
public final class CycleA {

    public CycleA(CycleB cycleB) {
    }
}
