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
    void placeholdersAreUsedWhenBuilding() {
        final ItemStack item = ItemBuilder.of(ItemTypes.PAPER)
                .name("<name>")
                .lore("<gray><price> coins")
                .placeholders(Placeholder.unparsed("name", "Ticket"), Placeholder.unparsed("price", "5"))
                .build();

        assertEquals("Ticket", Text.translateToLegacyString(item.getComponent(ComponentTypes.ITEM_NAME).orElseThrow()));
        assertEquals(List.of("§75 coins"), lore(item));
    }

    @Test
    void buildPlaceholdersWinOverStoredOnes() {
        final ItemBuilder builder = ItemBuilder.of(ItemTypes.PAPER)
                .name("<name>")
                .placeholders(Placeholder.unparsed("name", "stored"));

        assertEquals("given", Text.translateToLegacyString(builder.build(Placeholder.unparsed("name", "given"))
                .getComponent(ComponentTypes.ITEM_NAME).orElseThrow()));
        assertEquals("stored", Text.translateToLegacyString(builder.get().getComponent(ComponentTypes.ITEM_NAME).orElseThrow()));
    }

    @Test
    void glintIsOnlySentWhenSet() {
        assertTrue(ItemBuilder.of(ItemTypes.DIAMOND).build().getComponent(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE).isEmpty());
        assertEquals(true, ItemBuilder.of(ItemTypes.DIAMOND).glint(true).build().getComponent(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE).orElseThrow());
        assertTrue(ItemBuilder.of(ItemTypes.DIAMOND).glint(true).clearGlint().build().getComponent(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE).isEmpty());
    }

    @Test
    void replaceChangesPlainTextOnly() {
        final ItemStack item = ItemBuilder.of(ItemTypes.PAPER)
                .name("<yellow>{player}")
                .lore("<gray>Owner: {player}")
                .replace("{player}", "<red>Steve")
                .build();

        assertEquals("§e<red>Steve", Text.translateToLegacyString(item.getComponent(ComponentTypes.ITEM_NAME).orElseThrow()));
        assertEquals(List.of("§7Owner: <red>Steve"), lore(item));
    }

    @Test
    void headCanUseAnOwnerOrATexture() {
        final ItemStack owner = ItemBuilder.of(ItemTypes.PLAYER_HEAD).skullOwner("Notch").build();
        final ItemStack texture = ItemBuilder.of(ItemTypes.PLAYER_HEAD).headTexture("abc").build();

        assertEquals("Notch", owner.getComponent(ComponentTypes.PROFILE).orElseThrow().getName());
        assertEquals("abc", texture.getComponent(ComponentTypes.PROFILE).orElseThrow().getProperties().getFirst().getValue());
    }

    @Test
    void colorTooltipAndExtraComponents() {
        final ItemStack item = ItemBuilder.of(ItemTypes.LEATHER_CHESTPLATE)
                .color(0x43C9FA)
                .hideTooltip(true)
                .component(ComponentTypes.MAX_STACK_SIZE, 16)
                .build();

        assertEquals(0x43C9FA, item.getComponent(ComponentTypes.DYED_COLOR).orElseThrow().getRgb());
        assertTrue(item.getComponent(ComponentTypes.TOOLTIP_DISPLAY).orElseThrow().isHideTooltip());
        assertEquals(16, item.getComponent(ComponentTypes.MAX_STACK_SIZE).orElseThrow());
    }

    @Test
    void clearRemovesWhatWasSet() {
        final ItemStack item = ItemBuilder.of(ItemTypes.DIAMOND_CHESTPLATE)
                .enchantment(com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes.UNBREAKING, 1)
                .model(3)
                .color(1)
                .clearEnchantments()
                .clearModel()
                .clearColor()
                .clearTrim()
                .build();

        assertTrue(item.getComponent(ComponentTypes.ENCHANTMENTS).orElseThrow().isEmpty());
        assertTrue(item.getComponent(ComponentTypes.CUSTOM_MODEL_DATA_LISTS).isEmpty());
        assertTrue(item.getComponent(ComponentTypes.DYED_COLOR).isEmpty());
    }

    @Test
    void headWithoutTextureHasNoProfile() {
        final ItemStack head = ItemBuilder.of(ItemTypes.PLAYER_HEAD).build();

        assertTrue(head.getComponent(ComponentTypes.PROFILE).isEmpty());
    }
}
