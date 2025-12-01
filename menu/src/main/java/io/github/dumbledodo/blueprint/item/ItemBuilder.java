package io.github.dumbledodo.blueprint.item;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ArmorTrim;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEnchantments;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile.Property;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentType;
import com.github.retrooper.packetevents.protocol.item.trimmaterial.TrimMaterial;
import com.github.retrooper.packetevents.protocol.item.trimpattern.TrimPattern;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import io.github.dumbledodo.blueprint.chat.Text;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.function.Function;

public class ItemBuilder {

    private ItemType type = ItemTypes.AIR;
    private Component displayName = null;

    private List<Component> lore = new ArrayList<>();
    private final Map<EnchantmentType, Integer> enchantments = new HashMap<>();

    private int amount = 1;
    private boolean enchantVisibility = true;

    private Integer customModelData = null;

    private boolean glint = false;
    private boolean unbreakable = false;

    private ArmorTrim trim;
    private String headTexture;

    public static ItemBuilder of(ItemType type) {
        return new ItemBuilder().type(type);
    }

    public ItemBuilder type(ItemType type) {
        this.type = type;
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
        this.displayName = Text.translate(text);
        return this;
    }

    public ItemBuilder name(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            clearLore();
            return this;
        }
        this.lore = lines.stream().map(Text::translate).toList();
        return this;
    }

    public ItemBuilder lore(Component line) {
        this.lore.add(line);
        return this;
    }

    public ItemBuilder clearLore() {
        this.lore.clear();
        return this;
    }

    public ItemBuilder placeholder(Function<String, String> replacer) {
        if (displayName != null) {
            final String mini = Text.translateToMiniMessage(displayName);

            this.displayName = Text.translate(replacer.apply(mini));
        }

        if (lore != null) {
            final List<String> mini = lore.stream().map(Text::translateToMiniMessage).toList();

            this.lore = mini.stream().map(replacer)
                    .map(Text::translate)
                    .toList();
        }
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
        final ItemStack.Builder builder = ItemStack.builder()
                .type(type)
                .amount(amount)
                .component(ComponentTypes.LORE, new ItemLore(lore))
                .component(ComponentTypes.ENCHANTMENTS, new ItemEnchantments(enchantments, enchantVisibility))
                .component(ComponentTypes.UNBREAKABLE, unbreakable)
                .component(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);

        if (displayName != null) {
            builder.component(ComponentTypes.ITEM_NAME, displayName);
        }

        if (customModelData != null) {
            builder.component(ComponentTypes.CUSTOM_MODEL_DATA, customModelData);
        }

        if (trim != null) {
            builder.component(ComponentTypes.TRIM, trim);
        }

        if (type == ItemTypes.PLAYER_HEAD) {
            final Property property = new Property("textures", headTexture, null);

            builder.component(ComponentTypes.PROFILE, new ItemProfile(null, null, List.of(property)));
        }
        return builder.build();
    }
}