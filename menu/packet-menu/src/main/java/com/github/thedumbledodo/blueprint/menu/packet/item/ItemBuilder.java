package com.github.thedumbledodo.blueprint.menu.packet.item;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ArmorTrim;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemCustomModelData;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEnchantments;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile.Property;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemUnbreakable;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentType;
import com.github.retrooper.packetevents.protocol.item.trimmaterial.TrimMaterial;
import com.github.retrooper.packetevents.protocol.item.trimpattern.TrimPattern;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.thedumbledodo.blueprint.chat.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;

public class ItemBuilder {

    private ItemType type = ItemTypes.AIR;
    private int amount = 1;

    private String name;
    private Component nameComponent;

    private final List<Object> lore = new ArrayList<>();
    private final Map<EnchantmentType, Integer> enchantments = new HashMap<>();

    private boolean enchantVisibility = true;
    private Integer customModelData;

    private boolean glint;
    private boolean unbreakable;

    private ArmorTrim trim;
    private String headTexture;

    public static ItemBuilder of(ItemType type) {
        return new ItemBuilder().type(type);
    }

    public ItemBuilder type(ItemType type) {
        this.type = Objects.requireNonNull(type, "type");
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder glint(boolean glint) {
        this.glint = glint;
        return this;
    }

    public ItemBuilder name(String text) {
        this.name = text;
        this.nameComponent = null;
        return this;
    }

    public ItemBuilder name(Component displayName) {
        this.nameComponent = displayName;
        this.name = null;
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(lines == null ? List.of() : Arrays.asList(lines));
    }

    public ItemBuilder lore(List<String> lines) {
        clearLore();

        if (lines == null) {
            return this;
        }
        lore.addAll(lines);
        return this;
    }

    public ItemBuilder lore(Component... lines) {
        clearLore();

        if (lines == null) {
            return this;
        }
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder appendLore(String... lines) {
        if (lines == null) {
            return this;
        }
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder appendLore(Component... lines) {
        if (lines == null) {
            return this;
        }
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder clearLore() {
        lore.clear();
        return this;
    }

    public ItemBuilder enchantment(EnchantmentType type, int level) {
        enchantments.put(type, level);
        return this;
    }

    public ItemBuilder enchantments(Map<EnchantmentType, Integer> enchants, boolean visible) {
        enchantments.putAll(enchants);

        this.enchantVisibility = visible;
        return this;
    }

    public ItemBuilder model(int modelData) {
        this.customModelData = modelData;
        return this;
    }

    public ItemBuilder trim(TrimMaterial material, TrimPattern pattern) {
        this.trim = new ArmorTrim(material, pattern);
        return this;
    }

    public ItemBuilder headTexture(String texture) {
        this.headTexture = texture;
        return this;
    }

    public ItemStack build() {
        return build(new TagResolver[0]);
    }

    public ItemStack build(TagResolver... resolvers) {
        final List<Component> lines = new ArrayList<>(lore.size());

        for (Object line : lore) {
            if (line instanceof Component component) {
                lines.add(component);
                continue;
            }
            lines.add(Text.translate((String) line, resolvers));
        }

        final ItemStack.Builder builder = ItemStack.builder()
                .type(type)
                .amount(amount)
                .component(ComponentTypes.LORE, new ItemLore(lines))
                .component(ComponentTypes.ENCHANTMENTS, new ItemEnchantments(new HashMap<>(enchantments), enchantVisibility))
                .component(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);

        final Component displayName = name != null ? Text.translate(name, resolvers) : nameComponent;

        if (displayName != null) {
            builder.component(ComponentTypes.ITEM_NAME, displayName);
        }

        if (unbreakable) {
            builder.component(ComponentTypes.UNBREAKABLE_MODERN, new ItemUnbreakable());
        }

        if (customModelData != null) {
            builder.component(ComponentTypes.CUSTOM_MODEL_DATA_LISTS, new ItemCustomModelData(customModelData));
        }

        if (trim != null) {
            builder.component(ComponentTypes.TRIM, trim);
        }

        if (type == ItemTypes.PLAYER_HEAD && headTexture != null) {
            final Property property = new Property("textures", headTexture, null);

            builder.component(ComponentTypes.PROFILE, new ItemProfile(null, null, List.of(property)));
        }
        return builder.build();
    }
}
