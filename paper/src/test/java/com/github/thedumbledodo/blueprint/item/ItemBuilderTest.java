package com.github.thedumbledodo.blueprint.item;

import org.mockbukkit.mockbukkit.MockBukkit;
import com.github.thedumbledodo.blueprint.chat.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemBuilderTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static String name(ItemStack item) {
        return Text.translateToLegacyString(item.getItemMeta().displayName());
    }

    private static List<String> lore(ItemStack item) {
        final List<Component> lore = item.getItemMeta().lore();

        return lore == null ? List.of() : lore.stream().map(Text::translateToLegacyString).toList();
    }

    @Test
    void resolversFillNameAndLore() {
        final ItemStack item = ItemBuilder.from(Material.PAPER)
                .name("<yellow><arena>")
                .lore("<gray>Players: <players>")
                .build(Placeholder.unparsed("arena", "Desert"), Placeholder.unparsed("players", "4"));

        assertEquals("§eDesert", name(item));
        assertEquals(List.of("§7Players: 4"), lore(item));
    }

    @Test
    void templateCanBeBuiltManyTimes() {
        final ItemBuilder template = ItemBuilder.from(Material.PAPER).name("<name>");

        assertEquals("A", name(template.build(Placeholder.unparsed("name", "A"))));
        assertEquals("B", name(template.build(Placeholder.unparsed("name", "B"))));
    }

    @Test
    void textAndComponentLoreCanBeMixed() {
        final ItemStack item = ItemBuilder.from(Material.PAPER)
                .lore(Component.text("first"))
                .appendLore("<red>second")
                .build();

        assertEquals(List.of("first", "§csecond"), lore(item));
    }

    @Test
    void appendingKeepsLoreFromAnExistingItem() {
        final ItemStack base = new ItemStack(Material.PAPER);
        final ItemMeta meta = base.getItemMeta();

        meta.lore(List.of(Component.text("existing")));
        base.setItemMeta(meta);

        assertEquals(List.of("existing", "added"), lore(ItemBuilder.from(base).appendLore("added").build()));
    }

    @Test
    void builtItemIsNotChangedByTheBuilderLater() {
        final ItemBuilder builder = ItemBuilder.from(Material.PAPER).name("first");
        final ItemStack item = builder.build();

        builder.name("second").amount(10);

        assertEquals("first", name(item));
        assertEquals(1, item.getAmount());
    }

    @Test
    void replaceWorksOnTextAndOnExistingComponents() {
        final ItemStack base = new ItemStack(Material.PAPER);
        final ItemMeta meta = base.getItemMeta();

        meta.displayName(Component.text("Hello {player}"));
        base.setItemMeta(meta);

        assertEquals("Hello Steve", name(ItemBuilder.from(base).replace("{player}", "Steve").build()));
        assertEquals("Hi <red>x", name(ItemBuilder.from(Material.PAPER).name("Hi {p}").replace("{p}", "<red>x").build()));
    }

    @Test
    void clearLoreRemovesLore() {
        assertTrue(lore(ItemBuilder.from(Material.PAPER).lore("a", "b").clearLore().build()).isEmpty());
    }
}
