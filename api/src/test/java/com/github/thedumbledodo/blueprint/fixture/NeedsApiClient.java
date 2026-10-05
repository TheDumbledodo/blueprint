package com.github.thedumbledodo.blueprint.fixture;

import com.github.thedumbledodo.blueprint.annotation.BlueprintComponent;

@BlueprintComponent
public record NeedsApiClient(ApiClient apiClient) {

}
