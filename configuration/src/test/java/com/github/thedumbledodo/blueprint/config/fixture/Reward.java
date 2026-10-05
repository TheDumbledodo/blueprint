package com.github.thedumbledodo.blueprint.config.fixture;

import com.github.thedumbledodo.blueprint.config.annotation.Comment;

public record Reward(@Comment("Item to give") String item, int amount) {
}
