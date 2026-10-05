package com.github.thedumbledodo.blueprint.config.json;

import com.github.thedumbledodo.blueprint.config.annotation.Comment;
import com.github.thedumbledodo.blueprint.config.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration("stats.json")
@Comment("Comments are ignored in JSON files.")
public final class StatsConfig {

    @Comment("Shown on the scoreboard")
    public String title = "<gold><bold>Stats</bold></gold>";
    public int kills = 12;
    public long playtimeMillis = 9_000_000_000L;
    public float accuracy = 0.75f;
    public double ratio = 1.5;
    public boolean visible = true;
    public String nothing = null;
    public List<String> lines = new ArrayList<>(List.of("<gray>Kills: <kills>", "<gray>Deaths: <deaths>"));
    public Map<String, Integer> topPlayers = new LinkedHashMap<>(Map.of("steve", 40));
}
