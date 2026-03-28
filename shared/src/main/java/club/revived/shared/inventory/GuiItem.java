package club.revived.shared.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import club.revived.shared.component.ColorUtils;
import club.revived.shared.translation.TranslationKey;
import net.kyori.adventure.text.Component;

public final class GuiItem {

  private final @NotNull ItemStack itemStack;
  private @Nullable ClickHandler clickHandler;
  private boolean updateOnClick;

  private GuiItem(final @NotNull ItemStack itemStack) {
    this.itemStack = itemStack;
  }

  public static @NotNull Builder builder(final @NotNull Material material) {
    return new Builder(material);
  }

  public static @NotNull Builder builder(final @NotNull ItemStack itemStack) {
    return new Builder(itemStack);
  }

  public static @NotNull GuiItem of(final @NotNull ItemStack itemStack) {
    return new GuiItem(itemStack);
  }

  public static @NotNull GuiItem empty() {
    return new GuiItem(new ItemStack(Material.AIR));
  }

  public @NotNull ItemStack itemStack() {
    return itemStack;
  }

  public @NotNull Optional<ClickHandler> clickHandler() {
    return Optional.ofNullable(clickHandler);
  }

  public @NotNull GuiItem onClick(final @Nullable ClickHandler handler) {
    this.clickHandler = handler;
    return this;
  }

  public @NotNull GuiItem updateOnClick(final boolean update) {
    this.updateOnClick = update;
    return this;
  }

  public boolean shouldUpdateOnClick() {
    return updateOnClick;
  }

  public boolean isEmpty() {
    return itemStack.getType() == Material.AIR;
  }

  public static final class Builder {

    private final @NotNull ItemStack itemStack;
    private final @NotNull Map<String, Supplier<String>> placeholders = new HashMap<>();

    private @Nullable Component name;
    private @Nullable TranslationKey nameKey;
    private @Nullable List<Component> lore;
    private @Nullable List<TranslationKey> loreKeys;
    private @Nullable ClickHandler clickHandler;
    private boolean updateOnClick;

    private Builder(final @NotNull Material material) {
      this.itemStack = new ItemStack(material);
    }

    private Builder(final @NotNull ItemStack itemStack) {
      this.itemStack = itemStack.clone();
    }

    public @NotNull Builder name(final @NotNull Component name) {
      this.name = name;
      return this;
    }

    public @NotNull Builder name(final @NotNull String miniMessage) {
      this.name = ColorUtils.parse(miniMessage);
      return this;
    }

    public @NotNull Builder nameKey(final @NotNull TranslationKey key) {
      this.nameKey = key;
      return this;
    }

    public @NotNull Builder nameKey(final @NotNull String key) {
      return nameKey(TranslationKey.of(key));
    }

    public @NotNull Builder lore(final @NotNull List<Component> lore) {
      this.lore = new ArrayList<>(lore);
      return this;
    }

    public @NotNull Builder lore(final @NotNull Component... lines) {
      this.lore = new ArrayList<>(List.of(lines));
      return this;
    }

    public @NotNull Builder loreMiniMessage(final @NotNull String... lines) {
      this.lore = new ArrayList<>();
      for (final var line : lines) {
        this.lore.add(ColorUtils.parse(line));
      }
      return this;
    }

    public @NotNull Builder loreKeys(final @NotNull List<TranslationKey> keys) {
      this.loreKeys = new ArrayList<>(keys);
      return this;
    }

    public @NotNull Builder loreKeys(final @NotNull TranslationKey... keys) {
      this.loreKeys = new ArrayList<>(List.of(keys));
      return this;
    }

    public @NotNull Builder loreKeys(final @NotNull String... keys) {
      this.loreKeys = new ArrayList<>();
      for (final var key : keys) {
        this.loreKeys.add(TranslationKey.of(key));
      }
      return this;
    }

    public @NotNull Builder amount(final int amount) {
      itemStack.setAmount(amount);
      return this;
    }

    public @NotNull Builder placeholder(final @NotNull String key, final @NotNull Supplier<String> value) {
      placeholders.put(key, value);
      return this;
    }

    public @NotNull Builder placeholder(final @NotNull String key, final @NotNull String value) {
      return placeholder(key, () -> value);
    }

    public @NotNull Builder onClick(final @NotNull ClickHandler handler) {
      this.clickHandler = handler;
      return this;
    }

    public @NotNull Builder updateOnClick(final boolean update) {
      this.updateOnClick = update;
      return this;
    }

    public @NotNull Builder meta(final @NotNull Consumer<ItemMeta> consumer) {
      itemStack.editMeta(consumer);
      return this;
    }

    public @NotNull GuiItem build() {
      itemStack.editMeta(meta -> {
        final var resolvedName = resolveName();
        if (resolvedName != null) {
          meta.displayName(resolvedName);
        }

        final var resolvedLore = resolveLore();
        if (resolvedLore != null && !resolvedLore.isEmpty()) {
          meta.lore(resolvedLore);
        }
      });

      final var item = new GuiItem(itemStack);
      item.onClick(clickHandler);
      item.updateOnClick(updateOnClick);
      return item;
    }

    private @Nullable Component resolveName() {
      if (nameKey != null) {
        final var translated = nameKey.translate();
        final var replaced = replacePlaceholders(translated);
        return ColorUtils.parse(replaced);
      }
      return name;
    }

    private @Nullable List<Component> resolveLore() {
      if (loreKeys != null && !loreKeys.isEmpty()) {
        return loreKeys.stream()
            .map(key -> {
              final var translated = key.translate();
              final var replaced = replacePlaceholders(translated);
              return ColorUtils.parse(replaced);
            })
            .toList();
      }
      return lore;
    }

    private @NotNull String replacePlaceholders(final @NotNull String text) {
      var result = text;
      for (final var entry : placeholders.entrySet()) {
        result = result.replace("{" + entry.getKey() + "}", entry.getValue().get());
      }
      return result;
    }
  }
}
