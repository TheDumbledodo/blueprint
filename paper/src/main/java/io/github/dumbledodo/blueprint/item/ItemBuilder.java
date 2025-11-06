package io.github.dumbledodo.blueprint.item;

import io.github.dumbledodo.blueprint.chat.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
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
        this.item.setType(material);
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
        meta.displayName(Text.translate(name));
        return this;
    }

    public ItemBuilder name(Component component) {
        meta.displayName(component);
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(List<String> miniLines) {
        if (miniLines == null || miniLines.isEmpty()) {
            meta.lore(null);
            return this;
        }
        meta.lore(miniLines.stream().map(Text::translate).toList());
        return this;
    }

    public ItemBuilder clearLore() {
        meta.lore(null);
        return this;
    }

    public ItemBuilder placeholder(Function<String, String> replacer) {
        if (meta.hasDisplayName()) {
            final Component displayName = meta.displayName();

            if (displayName == null) {
                return this;
            }
            final String mini = Text.translateToMiniMessage(displayName);

            meta.displayName(Text.translate(replacer.apply(mini)));
        }

        if (meta.hasLore()) {
            final List<Component> lore = meta.lore();

            if (lore == null) {
                return this;
            }
            final List<String> mini = lore.stream().map(Text::translateToMiniMessage).toList();

            meta.lore(mini.stream().map(replacer).map(Text::translate).toList());
        }
        return this;
    }

    public ItemBuilder editMeta(Consumer<ItemMeta> consumer) {
        consumer.accept(meta);
        return this;
    }

    public ItemBuilder editItem(Consumer<ItemStack> consumer) {
        consumer.accept(item);
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
        meta.getItemFlags().forEach(item::removeItemFlags);
        return this;
    }

    public ItemBuilder modelData(int customModelData) {
        meta.setCustomModelData(customModelData);
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

    public ItemBuilder contents(ItemStack... items) {
        if (meta instanceof BlockStateMeta stateMeta && stateMeta.getBlockState() instanceof ShulkerBox shulker) {
            shulker.getInventory().setContents(items);

            stateMeta.setBlockState(shulker);
        }
        return this;
    }

    public ItemBuilder contents(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return this;
        }
        return contents(items.toArray(new ItemStack[0]));
    }

    public ItemStack build() {
        final ItemStack result = item.clone();
        result.setItemMeta(meta.clone());

        return result;
    }
}