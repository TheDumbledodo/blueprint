package com.github.thedumbledodo.blueprint.config.yaml;

import com.github.thedumbledodo.blueprint.config.annotation.Comment;
import com.github.thedumbledodo.blueprint.config.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration("messages.yml")
@Comment("Messages sent by the plugin, MiniMessage is supported.")
public final class MessagesConfig {

    @Comment("Sent after /minigame reload")
    public String reloadedPlugin = "<#43C9FA>Successfully reloaded plugin!";
    public String noPermission = "<#FF0000>You do not have permission.";
    public String longLine = "<gray>This is a very long line of MiniMessage text that goes on and on well past eighty characters <white>to check folding";
    public String looksLikeBoolean = "yes";
    public String looksLikeNumber = "123";
    public String nothing = null;
    public ArenaSection arena = new ArenaSection();
    public List<ArenaSection> arenas = new ArrayList<>(List.of(new ArenaSection()));
    public Map<String, Integer> scores = new LinkedHashMap<>(Map.of("steve", 3));

    public static final class ArenaSection {

        @Comment({"Seconds before the game starts", "", "Counted down in chat"})
        public int countdown = 10;
        public List<String> worlds = new ArrayList<>(List.of("arena_1", "arena 2"));
        public double ratio = 0.5;
        public boolean enabled = true;
    }
}
