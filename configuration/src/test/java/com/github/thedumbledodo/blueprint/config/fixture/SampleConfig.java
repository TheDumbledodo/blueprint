package com.github.thedumbledodo.blueprint.config.fixture;

import com.github.thedumbledodo.blueprint.chat.Message;
import com.github.thedumbledodo.blueprint.chat.Text;
import com.github.thedumbledodo.blueprint.config.annotation.Comment;
import com.github.thedumbledodo.blueprint.config.annotation.Configuration;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.math.BigDecimal;
import java.util.*;

@Getter
@Configuration("sample.mem")
@Comment({"Sample plugin settings.", "MiniMessage is supported."})
public class SampleConfig {

    public static String ignoredStatic = "static";

    @Comment("Sent after /minigame reload")
    public String reloadedPlugin = "<green>Reloaded.";
    public int maxPlayers = 16;
    public long cooldownMillis = 1500L;
    public boolean announce = true;
    public double multiplier = 1.5;
    public char prefix = '!';
    public Difficulty difficulty = Difficulty.NORMAL;
    public List<String> worlds = new ArrayList<>(List.of("arena_1", "arena_2"));
    public Set<Difficulty> modes = new LinkedHashSet<>(List.of(Difficulty.EASY, Difficulty.HARD));
    public int[] slots = {10, 11, 12};
    public ArenaSection arena = new ArenaSection();
    public Map<String, ArenaSection> arenas = new LinkedHashMap<>(Map.of("desert", new ArenaSection()));
    public Map<Difficulty, Integer> rewardsByDifficulty = new LinkedHashMap<>(Map.of(Difficulty.HARD, 50));
    public Reward reward = new Reward("diamond", 3);
    public UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public Message welcome = Message.of("<gray>Welcome <player>");
    public Component title = Text.translate("<gold>Title");
    public BigDecimal price = new BigDecimal("19.99");
    public String nothing = null;
    public Object anything = "raw";

    public final String version = "1";
    public transient String cache = "not saved";
}
