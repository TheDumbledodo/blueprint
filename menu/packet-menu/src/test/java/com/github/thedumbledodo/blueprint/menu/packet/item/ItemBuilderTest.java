package com.github.thedumbledodo.blueprint.menu.packet.item;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.thedumbledodo.blueprint.chat.Text;
import com.github.thedumbledodo.blueprint.menu.packet.fixture.FakePacketEventsAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemBuilderTest {

    @BeforeAll
    static void installPacketEvents() {
        FakePacketEventsAPI.install();
    }

    private static List<String> lore(ItemStack item) {
        return item.getComponent(ComponentTypes.LORE)
                .map(ItemLore::getLines)
                .orElse(List.of())
                .stream()
                .map(Text::translateToLegacyString)
                .toList();
    }

    @Test
    void loreCanBeAppendedAfterAList() {
        final ItemStack item = ItemBuilder.of(ItemTypes.DIAMOND)
                .lore("<gray>first")
                .appendLore(Component.text("second"))
                .appendLore("<gray>third")
                .build();

        assertEquals(List.of("§7first", "second", "§7third"), lore(item));
    }

    @Test
    void clearingLoreAfterAListWorks() {
        final ItemStack item = ItemBuilder.of(ItemTypes.DIAMOND)
                .lore(List.of("a", "b"))
                .clearLore()
                .build();

        assertTrue(lore(item).isEmpty());
    }

    @Test
    void builtItemDoesNotChangeWhenTheBuilderDoes() {
        final ItemBuilder builder = ItemBuilder.of(ItemTypes.DIAMOND).lore("one");
        final ItemStack item = builder.build();

        builder.appendLore("two").amount(5);

        assertEquals(List.of("one"), lore(item));
        assertEquals(1, item.getAmount());
    }

    @Test
    void resolversFillNameAndLore() {
        final ItemBuilder template = ItemBuilder.of(ItemTypes.PAPER)
                .name("<yellow><arena>")
                .lore("<gray>Players: <players>");

        final ItemStack item = template.build(Placeholder.unparsed("arena", "Desert"), Placeholder.unparsed("players", "4"));

        assertEquals("§eDesert", Text.translateToLegacyString(item.getComponent(ComponentTypes.ITEM_NAME).orElseThrow()));
        assertEquals(List.of("§7Players: 4"), lore(item));
    }

    @Test
    void templateIsReusable() {
        final ItemBuilder template = ItemBuilder.of(ItemTypes.PAPER).name("<name>");

        final ItemStack first = template.build(Placeholder.unparsed("name", "A"));
        final ItemStack second = template.build(Placeholder.unparsed("name", "B"));

        assertEquals("A", Text.translateToLegacyString(first.getComponent(ComponentTypes.ITEM_NAME).orElseThrow()));
        assertEquals("B", Text.translateToLegacyString(second.getComponent(ComponentTypes.ITEM_NAME).orElseThrow()));
    }

    @Test
    void headWithoutTextureHasNoProfile() {
        final ItemStack head = ItemBuilder.of(ItemTypes.PLAYER_HEAD).build();

        assertTrue(head.getComponent(ComponentTypes.PROFILE).isEmpty());
    }
}
