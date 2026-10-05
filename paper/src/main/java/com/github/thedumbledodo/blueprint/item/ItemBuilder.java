package com.github.thedumbledodo.blueprint.item;

import com.github.thedumbledodo.blueprint.chat.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    private String name;
    private List<Object> lore;

    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    public ItemBuilder(ItemStack item) {
        this.item = Objects.requireNonNull(item, "item").clone();
        this.meta = item.getItemMeta();
    }

    public static ItemBuilder from(Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    public static ItemBuilder from(ItemStack item) {
        return new ItemBuilder(item);
    }

    public static ItemBuilder from(Material material) {
        return from(material, 1);
    }

    public ItemBuilder type(Material material) {
        this.item.setType(Objects.requireNonNull(material, "material"));
        this.meta = item.getItemMeta();
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder name(Component component) {
        this.name = null;
        meta.displayName(component);
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(lines == null ? List.of() : Arrays.asList(lines));
    }

    public ItemBuilder lore(Component... lines) {
        this.lore = new ArrayList<>();

        if (lines == null) {
            return this;
        }
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        this.lore = new ArrayList<>();

        if (lines == null) {
            return this;
        }
        lore.addAll(lines);
        return this;
    }

    public ItemBuilder appendLore(String... lines) {
        if (lines == null || lines.length == 0) {
            return this;
        }
        managedLore().addAll(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder appendLore(Component... lines) {
        if (lines == null || lines.length == 0) {
            return this;
        }

        final List<Object> current = managedLore();

        for (Component line : lines) {
            if (line == null) {
                continue;
            }
            current.add(line);
        }
        return this;
    }

    public ItemBuilder clearLore() {
        this.lore = new ArrayList<>();
        return this;
    }

    public ItemBuilder replace(String literal, String replacement) {
        final String escaped = Text.escape(replacement);

        if (name != null) {
            this.name = name.replace(literal, escaped);

        } else if (meta.hasDisplayName() && meta.displayName() != null) {
            meta.displayName(replaceText(meta.displayName(), literal, replacement));
        }

        final List<Object> current = managedLore();

        for (int i = 0; i < current.size(); i++) {
            final Object line = current.get(i);

            if (line instanceof String text) {
                current.set(i, text.replace(literal, escaped));
                continue;
            }
            current.set(i, replaceText((Component) line, literal, replacement));
        }
        return this;
    }

    public ItemBuilder editMeta(Consumer<ItemMeta> consumer) {
        Objects.requireNonNull(consumer, "consumer").accept(meta);
        return this;
    }

    public ItemBuilder editItem(Consumer<ItemStack> consumer) {
        final ItemStack edited = build();

        Objects.requireNonNull(consumer, "consumer").accept(edited);

        this.item = edited;
        this.meta = edited.getItemMeta();

        this.name = null;
        this.lore = null;
        return this;
    }

    public <T, Z> ItemBuilder data(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        meta.getPersistentDataContainer().set(key, type, value);
        return this;
    }

    public ItemBuilder removeData(NamespacedKey key) {
        meta.getPersistentDataContainer().remove(key);
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment) {
        return enchantment(enchantment, 1);
    }

    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder enchantments(Map<Enchantment, Integer> enchantments) {
        enchantments.forEach(this::enchantment);
        return this;
    }

    public ItemBuilder clearEnchantments() {
        meta.removeEnchantments();
        return this;
    }

    public ItemBuilder flags(ItemFlag... itemFlag) {
        meta.addItemFlags(itemFlag);
        return this;
    }

    public ItemBuilder clearFlags() {
        meta.removeItemFlags(meta.getItemFlags().toArray(new ItemFlag[0]));
        return this;
    }

    public ItemBuilder modelData(int customModelData) {
        return modelData(Integer.valueOf(customModelData));
    }

    public ItemBuilder modelData(Integer customModelData) {
        meta.setCustomModelData(customModelData);
        return this;
    }

    public ItemBuilder clearModelData() {
        return modelData(null);
    }

    public ItemBuilder glow(boolean glow) {
        meta.setEnchantmentGlintOverride(glow);
        return this;
    }

    public ItemBuilder clearGlow() {
        meta.setEnchantmentGlintOverride(null);
        return this;
    }

    public ItemBuilder effect(PotionEffectType potionEffectType) {
        effect(potionEffectType, 10);
        return this;
    }

    public ItemBuilder effect(PotionEffectType potionEffectType, int duration) {
        effect(potionEffectType, duration, 1);
        return this;
    }

    public ItemBuilder effect(PotionEffectType potionEffectType, int duration, int amplifier) {
        effect(potionEffectType, duration, amplifier, true);
        return this;
    }

    public ItemBuilder effect(PotionEffectType potionEffectType, int duration, int amplifier, boolean ambient) {
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.addCustomEffect(new PotionEffect(potionEffectType, duration, amplifier, ambient), true);
        }
        return this;
    }

    public ItemBuilder effect(PotionType potionType) {
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.setBasePotionType(potionType);
        }
        return this;
    }

    public ItemBuilder removePotionEffect(PotionEffectType potionEffectType) {
        if (meta instanceof PotionMeta potionMeta) {

            if (!potionMeta.hasCustomEffect(potionEffectType)) {
                return this;
            }
            potionMeta.removeCustomEffect(potionEffectType);
        }
        return this;
    }

    public ItemBuilder removePotionEffect(List<PotionEffectType> potionEffectTypes) {
        if (meta instanceof PotionMeta potionMeta) {

            for (PotionEffectType potionEffectType : potionEffectTypes) {
                if (!potionMeta.hasCustomEffect(potionEffectType)) {
                    continue;
                }
                removePotionEffect(potionEffectType);
            }
        }
        return this;
    }

    public ItemBuilder color(Color color) {
        if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
            leatherArmorMeta.setColor(color);
        }
        return this;
    }

    public ItemBuilder potionColor(Color color) {
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.setColor(color);
        }
        return this;
    }

    public ItemBuilder spawnEgg(EntityType entityType) {
        if (meta instanceof SpawnEggMeta spawnEggMeta) {
            spawnEggMeta.setCustomSpawnedType(entityType);
        }
        return this;
    }

    public ItemBuilder skullOwner(OfflinePlayer player) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    public ItemBuilder skullOwner(String playerName) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwner(playerName);
        }
        return this;
    }

    public ItemBuilder fireworkPower(int power) {
        if (meta instanceof FireworkMeta fireworkMeta) {
            fireworkMeta.setPower(power);
        }
        return this;
    }

    public ItemBuilder fireworkEffect(FireworkEffect effect) {
        if (meta instanceof FireworkMeta fireworkMeta) {
            fireworkMeta.addEffect(effect);
        }
        return this;
    }

    public ItemBuilder chargedProjectiles(ItemStack... projectiles) {
        if (meta instanceof CrossbowMeta crossbowMeta) {
            crossbowMeta.setChargedProjectiles(Arrays.asList(projectiles));
        }
        return this;
    }

    public ItemBuilder trim(TrimMaterial material, TrimPattern pattern) {
        if (meta instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(new ArmorTrim(material, pattern));
        }
        return this;
    }

    public ItemBuilder clearTrim() {
        if (meta instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(null);
        }
        return this;
    }

    public ItemBuilder contents(ItemStack... items) {
        if (meta instanceof BlockStateMeta stateMeta && stateMeta.getBlockState() instanceof ShulkerBox shulker) {
            shulker.getInventory().addItem(items);

            stateMeta.setBlockState(shulker);
        }
        return this;
    }

    @SafeVarargs
    public final ItemBuilder contents(List<ItemStack>... lists) {
        final List<ItemStack> merged = new ArrayList<>();

        for (List<ItemStack> list : lists) {
            if (list == null || list.isEmpty()) {
                continue;
            }
            merged.addAll(list);
        }
        return contents(merged.toArray(new ItemStack[0]));
    }

    public ItemBuilder contents(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return this;
        }
        return contents(items.toArray(new ItemStack[0]));
    }

    public ItemStack build() {
        return build(new TagResolver[0]);
    }

    public ItemStack build(TagResolver... resolvers) {
        final ItemStack result = item.clone();

        if (meta == null) {
            return result;
        }

        final ItemMeta copy = meta.clone();

        if (name != null) {
            copy.displayName(Text.translate(name, resolvers));
        }

        if (lore != null) {
            copy.lore(lore.isEmpty() ? null : translateLore(resolvers));
        }

        result.setItemMeta(copy);
        return result;
    }

    private List<Component> translateLore(TagResolver... resolvers) {
        final List<Component> lines = new ArrayList<>(lore.size());

        for (Object line : lore) {
            if (line instanceof Component component) {
                lines.add(component);
                continue;
            }
            lines.add(Text.translate((String) line, resolvers));
        }
        return lines;
    }

    private List<Object> managedLore() {
        if (lore == null) {
            this.lore = new ArrayList<>(currentLore());
        }
        return lore;
    }

    private static Component replaceText(Component component, String literal, String replacement) {
        return component.replaceText(TextReplacementConfig.builder()
                .matchLiteral(literal)
                .replacement(replacement)
                .build());
    }

    private List<Component> currentLore() {
        final List<Component> existing = meta.lore();

        return existing == null ? new ArrayList<>() : new ArrayList<>(existing);
    }
}
