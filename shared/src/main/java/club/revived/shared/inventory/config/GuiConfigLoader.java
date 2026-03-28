package club.revived.shared.inventory.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import club.revived.shared.result.Result;

public final class GuiConfigLoader {

  private final @NotNull Map<String, GuiConfig> cache = new ConcurrentHashMap<>();

  public @NotNull Result<GuiConfig, String> load(final @NotNull Path path) {
    if (!Files.exists(path)) {
      return Result.err("File not found: " + path);
    }

    try {
      final var loader = YamlConfigurationLoader.builder()
          .path(path)
          .build();

      final var root = loader.load();
      final var config = parseGuiConfig(root);
      cache.put(config.id(), config);
      return Result.ok(config);
    } catch (final IOException e) {
      return Result.err("Failed to load config: " + e.getMessage());
    }
  }

  public @NotNull CompletableFuture<Result<GuiConfig, String>> loadAsync(final @NotNull Path path) {
    return CompletableFuture.supplyAsync(() -> load(path));
  }

  public @NotNull Result<Map<String, GuiConfig>, String> loadDirectory(final @NotNull Path directory) {
    if (!Files.isDirectory(directory)) {
      return Result.err("Not a directory: " + directory);
    }

    final var configs = new HashMap<String, GuiConfig>();

    try (final var stream = Files.list(directory)) {
      final var files = stream
          .filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
          .toList();

      for (final var file : files) {
        final var result = load(file);
        if (result.isErr()) {
          return Result.err(result.unwrapErr());
        }
        final var config = result.unwrap();
        configs.put(config.id(), config);
      }

      return Result.ok(configs);
    } catch (final IOException e) {
      return Result.err("Failed to list directory: " + e.getMessage());
    }
  }

  public @NotNull java.util.Optional<GuiConfig> cached(final @NotNull String id) {
    return java.util.Optional.ofNullable(cache.get(id));
  }

  public @NotNull Map<String, GuiConfig> allCached() {
    return Map.copyOf(cache);
  }

  public void clearCache() {
    cache.clear();
  }

  private @NotNull GuiConfig parseGuiConfig(final @NotNull ConfigurationNode root) {
    final var id = root.node("id").getString("default");
    final var title = root.node("title").getString("");
    final var titleKey = root.node("title-key").getString();
    final var rows = root.node("rows").getInt(3);
    final var preventClose = root.node("prevent-close").getBoolean(false);

    final var items = parseItems(root.node("items"));
    final var slots = parseSlots(root.node("layout"));

    return GuiConfig.builder(id)
        .title(title)
        .titleKey(titleKey)
        .rows(rows)
        .preventClose(preventClose)
        .items(items)
        .slots(slots)
        .build();
  }

  private @NotNull Map<String, ItemConfig> parseItems(final @NotNull ConfigurationNode node) {
    final var items = new HashMap<String, ItemConfig>();

    if (node.isNull() || node.empty()) {
      return items;
    }

    for (final var entry : node.childrenMap().entrySet()) {
      final var key = entry.getKey().toString();
      final var itemNode = entry.getValue();
      items.put(key, parseItemConfig(itemNode));
    }

    return items;
  }

  private @NotNull ItemConfig parseItemConfig(final @NotNull ConfigurationNode node) {
    final var material = node.node("material").getString("STONE");
    final var amount = node.node("amount").getInt(1);
    final var name = node.node("name").getString();
    final var nameKey = node.node("name-key").getString();
    final var lore = getStringList(node.node("lore"));
    final var loreKey = node.node("lore-key").getString();
    final var loreKeys = getStringList(node.node("lore-keys"));
    final var customModelData = node.node("custom-model-data").getInt(0);
    final var skullOwner = node.node("skull-owner").getString();
    final var skullTexture = node.node("skull-texture").getString();
    final var glowing = node.node("glowing").getBoolean(false);
    final var flags = getStringList(node.node("flags"));
    final var placeholders = getStringMap(node.node("placeholders"));

    return ItemConfig.builder(material)
        .amount(amount)
        .name(name)
        .nameKey(nameKey)
        .lore(lore)
        .loreKey(loreKey)
        .loreKeys(loreKeys)
        .customModelData(customModelData == 0 ? null : customModelData)
        .skullOwner(skullOwner)
        .skullTexture(skullTexture)
        .glowing(glowing)
        .flags(flags)
        .placeholders(placeholders)
        .build();
  }

  private @NotNull List<SlotMapping> parseSlots(final @NotNull ConfigurationNode node) {
    final var slots = new ArrayList<SlotMapping>();

    if (node.isNull() || node.empty()) {
      return slots;
    }

    if (node.isList()) {
      for (final var child : node.childrenList()) {
        slots.add(parseSlotMapping(child));
      }
    }

    return slots;
  }

  private @NotNull SlotMapping parseSlotMapping(final @NotNull ConfigurationNode node) {
    final var itemRef = node.node("item").getString("default");
    final var action = node.node("action").getString();
    final var updateIntervalNode = node.node("update-interval");
    final var statesNode = node.node("states");

    final Integer updateInterval = updateIntervalNode.isNull() ? null : updateIntervalNode.getInt();
    final List<StateConfig> states = parseStates(statesNode);

    final var slotNode = node.node("slot");
    final var slotsNode = node.node("slots");
    final var patternNode = node.node("pattern");

    if (!slotNode.isNull() && !slotNode.empty()) {
      return new SlotMapping(itemRef, slotNode.getInt(), null, null, action, updateInterval, states);
    }

    if (!slotsNode.isNull() && !slotsNode.empty()) {
      final var slotList = new ArrayList<Integer>();
      for (final var child : slotsNode.childrenList()) {
        slotList.add(child.getInt());
      }
      return new SlotMapping(itemRef, null, slotList, null, action, updateInterval, states);
    }

    if (!patternNode.isNull() && !patternNode.empty()) {
      return new SlotMapping(itemRef, null, null, patternNode.getString(), action, updateInterval, states);
    }

    return new SlotMapping(itemRef, 0, null, null, action, updateInterval, states);
  }

  private @NotNull List<StateConfig> parseStates(final @NotNull ConfigurationNode node) {
    final var states = new ArrayList<StateConfig>();

    if (node.isNull() || node.empty()) {
      return states;
    }

    for (final var child : node.childrenList()) {
      final var id = child.node("id").getString("default");
      final var condition = child.node("condition").getString("");
      final var itemNode = child.node("item");

      if (!itemNode.isNull() && !itemNode.empty()) {
        final var itemConfig = parseItemConfig(itemNode);
        states.add(StateConfig.of(id, condition, itemConfig));
      }
    }

    return states;
  }

  private @NotNull List<String> getStringList(final @NotNull ConfigurationNode node) {
    if (node.isNull() || node.empty()) {
      return List.of();
    }

    final var list = new ArrayList<String>();
    for (final var child : node.childrenList()) {
      final var value = child.getString();
      if (value != null) {
        list.add(value);
      }
    }
    return list;
  }

  private @NotNull Map<String, String> getStringMap(final @NotNull ConfigurationNode node) {
    if (node.isNull() || node.empty()) {
      return Map.of();
    }

    final var map = new HashMap<String, String>();
    for (final var entry : node.childrenMap().entrySet()) {
      final var key = entry.getKey().toString();
      final var value = entry.getValue().getString();
      if (value != null) {
        map.put(key, value);
      }
    }
    return map;
  }
}
