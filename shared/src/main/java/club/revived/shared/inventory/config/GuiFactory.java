package club.revived.shared.inventory.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.destroystokyo.paper.profile.ProfileProperty;

import club.revived.shared.component.ColorUtils;
import club.revived.shared.inventory.ClickHandler;
import club.revived.shared.inventory.DynamicGuiItem;
import club.revived.shared.inventory.GuiItem;
import club.revived.shared.inventory.InventoryGui;
import club.revived.shared.inventory.StatefulGuiItem;
import club.revived.shared.translation.TranslationKey;
import net.kyori.adventure.text.Component;

public final class GuiFactory {

  private final @NotNull GuiConfigLoader loader;
  private final @NotNull Map<String, Function<String, ClickHandler>> actionHandlers = new HashMap<>();
  private final @NotNull Map<String, Supplier<String>> globalPlaceholders = new HashMap<>();
  private final @NotNull Map<String, Predicate<Player>> conditionProviders = new HashMap<>();
  private @Nullable Locale locale;

  public GuiFactory(final @NotNull GuiConfigLoader loader) {
    this.loader = loader;
  }

  public @NotNull GuiFactory action(
      final @NotNull String action,
      final @NotNull Function<String, ClickHandler> handler) {
    actionHandlers.put(action, handler);
    return this;
  }

  public @NotNull GuiFactory action(
      final @NotNull String action,
      final @NotNull ClickHandler handler) {
    return action(action, ignored -> handler);
  }

  public @NotNull GuiFactory placeholder(
      final @NotNull String key,
      final @NotNull Supplier<String> value) {
    globalPlaceholders.put(key, value);
    return this;
  }

  public @NotNull GuiFactory condition(
      final @NotNull String id,
      final @NotNull Predicate<Player> predicate) {
    conditionProviders.put(id, predicate);
    return this;
  }

  public @NotNull GuiFactory locale(final @Nullable Locale locale) {
    this.locale = locale;
    return this;
  }

  public @NotNull Optional<InventoryGui> create(
      final @NotNull String id,
      final @NotNull Player player) {
    return loader.cached(id).map(config -> fromConfig(config, player, Map.of()));
  }

  public @NotNull Optional<InventoryGui> create(
      final @NotNull String id,
      final @NotNull Player player,
      final @NotNull Map<String, Supplier<String>> placeholders) {
    return loader.cached(id).map(config -> fromConfig(config, player, placeholders));
  }

  public @NotNull InventoryGui fromConfig(
      final @NotNull GuiConfig config,
      final @NotNull Player player) {
    return fromConfig(config, player, Map.of());
  }

  public @NotNull InventoryGui fromConfig(
      final @NotNull GuiConfig config,
      final @NotNull Player player,
      final @NotNull Map<String, Supplier<String>> placeholders) {
    final var allPlaceholders = new HashMap<>(globalPlaceholders);
    allPlaceholders.putAll(placeholders);

    final var title = resolveTitle(config, allPlaceholders);

    final var gui = InventoryGui.builder(config.id())
        .title(title)
        .rows(config.rows())
        .preventClose(config.preventClose())
        .build();

    applyLayout(gui, config, player, allPlaceholders);

    return gui;
  }

  private @NotNull Component resolveTitle(
      final @NotNull GuiConfig config,
      final @NotNull Map<String, Supplier<String>> placeholders) {
    if (config.titleKey() != null) {
      final var key = TranslationKey.of(config.titleKey());
      final var translated = locale != null ? key.translate(locale) : key.translate();
      final var replaced = replacePlaceholders(translated, placeholders);
      return ColorUtils.parse(replaced);
    }

    final var replaced = replacePlaceholders(config.title(), placeholders);
    return ColorUtils.parse(replaced);
  }

  private void applyLayout(
      final @NotNull InventoryGui gui,
      final @NotNull GuiConfig config,
      final @NotNull Player player,
      final @NotNull Map<String, Supplier<String>> placeholders) {
    for (final var mapping : config.slots()) {
      final var itemConfig = config.items().get(mapping.itemRef());
      if (itemConfig == null) {
        continue;
      }

      final var handler = resolveActionHandler(mapping.action());
      final var slots = resolveSlots(mapping, config.rows());

      if (mapping.isStateful()) {
        final var interval = mapping.updateInterval() != null ? mapping.updateInterval() : 1;
        applyStatefulItem(gui, slots, itemConfig, config, player, placeholders, handler, mapping.states(), interval);
      } else if (mapping.isDynamic()) {
        applyDynamicItem(gui, slots, itemConfig, placeholders, handler, mapping.updateInterval());
      } else {
        for (final var slot : slots) {
          final var item = createItem(itemConfig, placeholders, handler);
          gui.setItem(slot, item);
        }
      }
    }
  }

  private void applyDynamicItem(
      final @NotNull InventoryGui gui,
      final @NotNull List<Integer> slots,
      final @NotNull ItemConfig itemConfig,
      final @NotNull Map<String, Supplier<String>> placeholders,
      final @Nullable ClickHandler handler,
      final int updateInterval) {
    for (final var slot : slots) {
      final var dynamicItem = DynamicGuiItem.of(
          () -> createItem(itemConfig, placeholders, handler),
          updateInterval);
      if (handler != null) {
        dynamicItem.onClick(handler);
      }
      gui.setDynamicItem(slot, dynamicItem);
    }
  }

  private void applyStatefulItem(
      final @NotNull InventoryGui gui,
      final @NotNull List<Integer> slots,
      final @NotNull ItemConfig defaultItemConfig,
      final @NotNull GuiConfig guiConfig,
      final @NotNull Player player,
      final @NotNull Map<String, Supplier<String>> placeholders,
      final @Nullable ClickHandler handler,
      final @NotNull List<StateConfig> states,
      final int updateInterval) {
    for (final var slot : slots) {
      final var builder = StatefulGuiItem.builder(
          () -> createItem(defaultItemConfig, placeholders, handler),
          () -> player)
          .updateInterval(updateInterval);

      for (final var state : states) {
        final var condition = conditionProviders.getOrDefault(state.condition(), p -> false);
        builder.state(state.id(), condition, () -> createItem(state.item(), placeholders, handler));
      }

      if (handler != null) {
        builder.onClick(handler);
      }

      gui.setStatefulItem(slot, builder.build());
    }
  }

  private @NotNull List<Integer> resolveSlots(final @NotNull SlotMapping mapping, final int rows) {
    if (mapping.isSingle()) {
      return List.of(mapping.slot());
    }

    if (mapping.isMulti()) {
      return mapping.slots();
    }

    if (mapping.isPattern()) {
      return parsePattern(mapping.pattern(), rows);
    }

    return List.of();
  }

  private @NotNull List<Integer> parsePattern(final @NotNull String pattern, final int rows) {
    final var slots = new ArrayList<Integer>();
    final var lines = pattern.split("\n");
    final var size = rows * 9;

    for (int row = 0; row < Math.min(lines.length, rows); row++) {
      final var line = lines[row];
      for (int col = 0; col < Math.min(line.length(), 9); col++) {
        if (line.charAt(col) == 'X') {
          final var slot = row * 9 + col;
          if (slot < size) {
            slots.add(slot);
          }
        }
      }
    }

    return slots;
  }

  private @Nullable ClickHandler resolveActionHandler(final @Nullable String action) {
    if (action == null || action.isBlank()) {
      return null;
    }

    final var parts = action.split(":", 2);
    final var actionType = parts[0];
    final var actionArg = parts.length > 1 ? parts[1] : "";

    final var handlerFactory = actionHandlers.get(actionType);
    if (handlerFactory != null) {
      return handlerFactory.apply(actionArg);
    }

    return null;
  }

  private @NotNull GuiItem createItem(
      final @NotNull ItemConfig config,
      final @NotNull Map<String, Supplier<String>> placeholders,
      final @Nullable ClickHandler handler) {
    final var allPlaceholders = new HashMap<>(globalPlaceholders);
    allPlaceholders.putAll(placeholders);
    config.placeholders().forEach((k, v) -> allPlaceholders.put(k, () -> v));

    final var material = Material.matchMaterial(config.material());
    if (material == null) {
      return GuiItem.empty();
    }

    final var item = new ItemStack(material, config.amount());

    item.editMeta(meta -> {
      final var displayName = resolveName(config, allPlaceholders);
      if (displayName != null) {
        meta.displayName(displayName);
      }

      final var loreLines = resolveLore(config, allPlaceholders);
      if (!loreLines.isEmpty()) {
        meta.lore(loreLines);
      }

      if (config.customModelData() != null) {
        meta.setCustomModelData(config.customModelData());
      }

      if (config.flags() != null) {
        for (final var flag : config.flags()) {
          try {
            meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
          } catch (final IllegalArgumentException ignored) {
          }
        }
      }

      if (config.glowing()) {
        meta.setEnchantmentGlintOverride(true);
      }

      if (meta instanceof final SkullMeta skullMeta) {
        applySkullMeta(skullMeta, config);
      }
    });

    final var guiItem = GuiItem.of(item);
    if (handler != null) {
      guiItem.onClick(handler);
    }
    return guiItem;
  }

  private void applySkullMeta(final @NotNull SkullMeta meta, final @NotNull ItemConfig config) {
    if (config.skullOwner() != null) {
      meta.setOwningPlayer(Bukkit.getOfflinePlayer(config.skullOwner()));
    } else if (config.skullTexture() != null) {
      final var profile = Bukkit.createProfile(UUID.randomUUID());
      profile.setProperty(new ProfileProperty("textures", config.skullTexture()));
      meta.setPlayerProfile(profile);
    }
  }

  private @Nullable Component resolveName(
      final @NotNull ItemConfig config,
      final @NotNull Map<String, Supplier<String>> placeholders) {
    if (config.nameKey() != null) {
      final var key = TranslationKey.of(config.nameKey());
      final var translated = locale != null ? key.translate(locale) : key.translate();
      final var replaced = replacePlaceholders(translated, placeholders);
      return ColorUtils.parse(replaced);
    }

    if (config.name() != null) {
      final var replaced = replacePlaceholders(config.name(), placeholders);
      return ColorUtils.parse(replaced);
    }

    return null;
  }

  private @NotNull List<Component> resolveLore(
      final @NotNull ItemConfig config,
      final @NotNull Map<String, Supplier<String>> placeholders) {
    if (config.loreKey() != null) {
      final var key = TranslationKey.of(config.loreKey());
      final var translated = locale != null ? key.translateList(locale) : key.translateList();
      return translated.stream()
          .map(line -> {
            final var replaced = replacePlaceholders(line, placeholders);
            return ColorUtils.parse(replaced);
          })
          .toList();
    }

    if (config.loreKeys() != null && !config.loreKeys().isEmpty()) {
      return config.loreKeys().stream()
          .map(keyStr -> {
            final var key = TranslationKey.of(keyStr);
            final var translated = locale != null ? key.translate(locale) : key.translate();
            final var replaced = replacePlaceholders(translated, placeholders);
            return ColorUtils.parse(replaced);
          })
          .toList();
    }

    if (config.lore() != null && !config.lore().isEmpty()) {
      return config.lore().stream()
          .map(line -> {
            final var replaced = replacePlaceholders(line, placeholders);
            return ColorUtils.parse(replaced);
          })
          .toList();
    }

    return List.of();
  }

  private @NotNull String replacePlaceholders(
      final @NotNull String text,
      final @NotNull Map<String, Supplier<String>> placeholders) {
    var result = text;
    for (final var entry : placeholders.entrySet()) {
      result = result.replace("{" + entry.getKey() + "}", entry.getValue().get());
    }
    return result;
  }
}
