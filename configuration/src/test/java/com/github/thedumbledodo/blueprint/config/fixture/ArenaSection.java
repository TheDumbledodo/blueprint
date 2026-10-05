package com.github.thedumbledodo.blueprint.config.fixture;

import com.github.thedumbledodo.blueprint.config.annotation.Comment;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class ArenaSection {

    @Comment("Seconds before the game starts")
    public int countdown = 10;
    public List<String> worlds = new ArrayList<>(List.of("arena_1"));
}
