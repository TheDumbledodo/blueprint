package com.github.thedumbledodo.blueprint.menu.packet.item;

import com.github.retrooper.packetevents.protocol.component.ComponentType;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ArmorTrim;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemCustomModelData;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemDyeColor;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEnchantments;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile.Property;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemTooltipDisplay;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemUnbreakable;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentType;
import com.github.retrooper.packetevents.protocol.item.trimmaterial.TrimMaterial;
import com.github.retrooper.packetevents.protocol.item.trimpattern.TrimPattern;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.thedumbledodo.blueprint.chat.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;
import java.util.function.Supplier;

public class ItemBuilder implements Supplier<ItemStack> {

    private ItemType type = ItemTypes.AIR;
    private int amount = 1;

    private String name;
    private Component nameComponent;

    private final List<Object> lore = new ArrayList<>();
    private final List<TagResolver> placeholders = new ArrayList<>();
    private final Map<EnchantmentType, Integer> enchantments = new LinkedHashMap<>();
    private final Map<ComponentType<?>, Object> components = new LinkedHashMap<>();

    private boolean enchantVisibility = true;
    private Integer customModelData;

    private Boolean glint;
    private boolean unbreakable;
    private boolean hideTooltip;

    private Integer color;
    private ArmorTrim trim;

    private String headTexture;
    private String ownerName;
    private UUID ownerId;

    public static ItemBuilder of(ItemType type) {
        return new ItemBuilder().type(type);
    }

    public static ItemBuilder of(ItemType type, int amount) {
        return of(type).amount(amount);
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

    public ItemBuilder clearGlint() {
        this.glint = null;
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

    public ItemBuilder replace(String literal, String replacement) {
        final String escaped = Text.escape(replacement);

        if (name != null) {
            this.name = name.replace(literal, escaped);

        } else if (nameComponent != null) {
            this.nameComponent = replaceText(nameComponent, literal, replacement);
        }

        for (int i = 0; i < lore.size(); i++) {
            final Object line = lore.get(i);

            if (line instanceof String text) {
                lore.set(i, text.replace(literal, escaped));
                continue;
            }
            lore.set(i, replaceText((Component) line, literal, replacement));
        }
        return this;
    }

    public ItemBuilder placeholders(TagResolver... resolvers) {
        if (resolvers != null) {
            placeholders.addAll(Arrays.asList(resolvers));
        }
        return this;
    }

    public ItemBuilder clearPlaceholders() {
        placeholders.clear();
        return this;
    }

    public ItemBuilder enchantment(EnchantmentType type, int level) {
        enchantments.put(type, level);
        return this;
    }

    public ItemBuilder enchantments(Map<EnchantmentType, Integer> enchants) {
        enchantments.putAll(enchants);
        return this;
    }

    public ItemBuilder enchantments(Map<EnchantmentType, Integer> enchants, boolean visible) {
        enchantments.putAll(enchants);

        this.enchantVisibility = visible;
        return this;
    }

    public ItemBuilder hideEnchantments(boolean hide) {
        this.enchantVisibility = !hide;
        return this;
    }

    public ItemBuilder clearEnchantments() {
        enchantments.clear();
        return this;
    }

    public ItemBuilder model(int modelData) {
        this.customModelData = modelData;
        return this;
    }

    public ItemBuilder clearModel() {
        this.customModelData = null;
        return this;
    }

    public ItemBuilder color(int rgb) {
        this.color = rgb;
        return this;
    }

    public ItemBuilder clearColor() {
        this.color = null;
        return this;
    }

    public ItemBuilder trim(TrimMaterial material, TrimPattern pattern) {
        this.trim = new ArmorTrim(material, pattern);
        return this;
    }

    public ItemBuilder clearTrim() {
        this.trim = null;
        return this;
    }

    public ItemBuilder headTexture(String texture) {
        this.headTexture = texture;
        return this;
    }

    public ItemBuilder skullOwner(String playerName) {
        this.ownerName = playerName;
        return this;
    }

    public ItemBuilder skullOwner(UUID uuid) {
        this.ownerId = uuid;
        return this;
    }

    public ItemBuilder hideTooltip(boolean hide) {
        this.hideTooltip = hide;
        return this;
    }

    public <T> ItemBuilder component(ComponentType<T> type, T value) {
        components.put(Objects.requireNonNull(type, "type"), value);
        return this;
    }

    @Override
    public ItemStack get() {
        return build();
    }

    public ItemStack build() {
        return build(new TagResolver[0]);
    }

    public ItemStack build(TagResolver... resolvers) {
        final TagResolver[] allResolvers = merge(resolvers);
        final List<Component> lines = new ArrayList<>(lore.size());

        for (Object line : lore) {
            if (line instanceof Component component) {
                lines.add(component);
                continue;
            }
            lines.add(Text.translate((String) line, allResolvers));
        }

        final ItemStack.Builder builder = ItemStack.builder()
                .type(type)
                .amount(amount)
                .component(ComponentTypes.LORE, new ItemLore(lines))
                .component(ComponentTypes.ENCHANTMENTS, new ItemEnchantments(new HashMap<>(enchantments), enchantVisibility));

        final Component displayName = name != null ? Text.translate(name, allResolvers) : nameComponent;

        if (displayName != null) {
            builder.component(ComponentTypes.ITEM_NAME, displayName);
        }

        if (glint != null) {
            builder.component(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);
        }

        if (unbreakable) {
            builder.component(ComponentTypes.UNBREAKABLE_MODERN, new ItemUnbreakable());
        }

        if (customModelData != null) {
            builder.component(ComponentTypes.CUSTOM_MODEL_DATA_LISTS, new ItemCustomModelData(customModelData));
        }

        if (color != null) {
            builder.component(ComponentTypes.DYED_COLOR, new ItemDyeColor(color, true));
        }

        if (trim != null) {
            builder.component(ComponentTypes.TRIM, trim);
        }

        if (hideTooltip) {
            builder.component(ComponentTypes.TOOLTIP_DISPLAY, new ItemTooltipDisplay(true, Set.of()));
        }

        if (type == ItemTypes.PLAYER_HEAD && (headTexture != null || ownerName != null || ownerId != null)) {
            final List<Property> properties = headTexture == null ? List.of() : List.of(new Property("textures", headTexture, null));

            builder.component(ComponentTypes.PROFILE, new ItemProfile(ownerName, ownerId, properties));
        }

        for (Map.Entry<ComponentType<?>, Object> entry : components.entrySet()) {
            applyComponent(builder, entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private static <T> void applyComponent(ItemStack.Builder builder, ComponentType<T> type, Object value) {
        builder.component(type, (T) value);
    }

    private TagResolver[] merge(TagResolver... resolvers) {
        if (placeholders.isEmpty()) {
            return resolvers == null ? new TagResolver[0] : resolvers;
        }

        final List<TagResolver> all = new ArrayList<>(placeholders);

        if (resolvers != null) {
            all.addAll(Arrays.asList(resolvers));
        }
        return all.toArray(new TagResolver[0]);
    }

    private static Component replaceText(Component component, String literal, String replacement) {
        return component.replaceText(TextReplacementConfig.builder()
                .matchLiteral(literal)
                .replacement(replacement)
                .build());
    }
}
