package club.revived.shared.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import club.revived.shared.component.ColorUtils;
import club.revived.shared.translation.TranslationKey;
import net.kyori.adventure.text.Component;

public final class InventoryGui implements InventoryHolder {

  private static final Map<UUID, InventoryGui> OPEN_GUIS = new ConcurrentHashMap<>();
  private static final int DEFAULT_UPDATE_INTERVAL = 20;

  private final @NotNull String id;
  private final @NotNull Component title;
  private final int rows;
  private final @NotNull Map<Integer, GuiItem> items;
  private final @NotNull Map<Integer, DynamicGuiItem> dynamicItems;
  private final @NotNull Map<Integer, StatefulGuiItem> statefulItems;
  private final @NotNull Inventory inventory;

  private @Nullable Consumer<ClickContext> globalClickHandler;
  private @Nullable Consumer<Player> closeHandler;
  private @Nullable InventoryGui parent;
  private @Nullable BukkitTask updateTask;
  private boolean preventClose;
  private int updateInterval = DEFAULT_UPDATE_INTERVAL;
  private long currentTick;

  private InventoryGui(
      final @NotNull String id,
      final @NotNull Component title,
      final int rows) {
    this.id = id;
    this.title = title;
    this.rows = rows;
    this.items = new HashMap<>();
    this.dynamicItems = new HashMap<>();
    this.statefulItems = new HashMap<>();
    this.inventory = Bukkit.createInventory(this, rows * 9, title);
  }

  public static @NotNull Builder builder(final @NotNull String id) {
    return new Builder(id);
  }

  public static @NotNull Optional<InventoryGui> getOpenGui(final @NotNull Player player) {
    return Optional.ofNullable(OPEN_GUIS.get(player.getUniqueId()));
  }

  public static void closeAll() {
    OPEN_GUIS.values().forEach(gui -> {
      gui.stopUpdating();
      gui.viewers().forEach(Player::closeInventory);
    });
    OPEN_GUIS.clear();
  }

  public @NotNull String id() {
    return id;
  }

  public @NotNull Component title() {
    return title;
  }

  public int rows() {
    return rows;
  }

  public int size() {
    return rows * 9;
  }

  @Override
  public @NotNull Inventory getInventory() {
    return inventory;
  }

  public @NotNull Optional<GuiItem> item(final int slot) {
    return Optional.ofNullable(items.get(slot));
  }

  public @NotNull InventoryGui setItem(final int slot, final @NotNull GuiItem item) {
    validateSlot(slot);
    items.put(slot, item);
    inventory.setItem(slot, item.itemStack());
    return this;
  }

  public @NotNull InventoryGui setItem(final int row, final int column, final @NotNull GuiItem item) {
    return setItem(toSlot(row, column), item);
  }

  public @NotNull InventoryGui setDynamicItem(final int slot, final @NotNull DynamicGuiItem item) {
    validateSlot(slot);
    dynamicItems.put(slot, item);
    inventory.setItem(slot, item.itemStack());
    return this;
  }

  public @NotNull InventoryGui setStatefulItem(final int slot, final @NotNull StatefulGuiItem item) {
    validateSlot(slot);
    statefulItems.put(slot, item);
    inventory.setItem(slot, item.itemStack());
    return this;
  }

  public @NotNull Optional<DynamicGuiItem> dynamicItem(final int slot) {
    return Optional.ofNullable(dynamicItems.get(slot));
  }

  public @NotNull Optional<StatefulGuiItem> statefulItem(final int slot) {
    return Optional.ofNullable(statefulItems.get(slot));
  }

  public @NotNull InventoryGui removeItem(final int slot) {
    validateSlot(slot);
    items.remove(slot);
    dynamicItems.remove(slot);
    statefulItems.remove(slot);
    inventory.setItem(slot, null);
    return this;
  }

  public @NotNull InventoryGui fill(final @NotNull GuiItem item) {
    for (int i = 0; i < size(); i++) {
      if (!items.containsKey(i)) {
        setItem(i, item);
      }
    }
    return this;
  }

  public @NotNull InventoryGui fillBorder(final @NotNull GuiItem item) {
    for (int i = 0; i < 9; i++) {
      setItem(i, item);
      setItem(size() - 9 + i, item);
    }
    for (int i = 1; i < rows - 1; i++) {
      setItem(i * 9, item);
      setItem(i * 9 + 8, item);
    }
    return this;
  }

  public @NotNull InventoryGui fillRow(final int row, final @NotNull GuiItem item) {
    for (int i = 0; i < 9; i++) {
      setItem(toSlot(row, i), item);
    }
    return this;
  }

  public @NotNull InventoryGui fillColumn(final int column, final @NotNull GuiItem item) {
    for (int i = 0; i < rows; i++) {
      setItem(toSlot(i, column), item);
    }
    return this;
  }

  public @NotNull InventoryGui clear() {
    items.clear();
    dynamicItems.clear();
    statefulItems.clear();
    inventory.clear();
    return this;
  }

  public @NotNull InventoryGui onGlobalClick(final @Nullable Consumer<ClickContext> handler) {
    this.globalClickHandler = handler;
    return this;
  }

  public @NotNull InventoryGui onClose(final @Nullable Consumer<Player> handler) {
    this.closeHandler = handler;
    return this;
  }

  public @NotNull InventoryGui parent(final @Nullable InventoryGui parent) {
    this.parent = parent;
    return this;
  }

  public @NotNull Optional<InventoryGui> parent() {
    return Optional.ofNullable(parent);
  }

  public @NotNull InventoryGui preventClose(final boolean prevent) {
    this.preventClose = prevent;
    return this;
  }

  public boolean preventsClose() {
    return preventClose;
  }

  public @NotNull InventoryGui updateInterval(final int ticks) {
    if (ticks < 1) {
      throw new IllegalArgumentException("Update interval must be at least 1 tick");
    }
    this.updateInterval = ticks;
    return this;
  }

  public int updateInterval() {
    return updateInterval;
  }

  public void open(final @NotNull Player player) {
    OPEN_GUIS.put(player.getUniqueId(), this);
    player.openInventory(inventory);
  }

  public void startUpdating(final @NotNull JavaPlugin plugin) {
    if (updateTask != null) {
      return;
    }
    updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
  }

  public void stopUpdating() {
    if (updateTask != null) {
      updateTask.cancel();
      updateTask = null;
    }
    currentTick = 0;
  }

  public boolean isUpdating() {
    return updateTask != null;
  }

  private void tick() {
    currentTick++;

    final var viewers = viewers();
    if (viewers.isEmpty()) {
      return;
    }

    var needsUpdate = false;

    for (final var entry : dynamicItems.entrySet()) {
      if (entry.getValue().shouldUpdate(currentTick)) {
        inventory.setItem(entry.getKey(), entry.getValue().itemStack());
        needsUpdate = true;
      }
    }

    for (final var entry : statefulItems.entrySet()) {
      final var item = entry.getValue();
      if (item.shouldUpdate(currentTick) || item.hasStateChanged()) {
        inventory.setItem(entry.getKey(), item.itemStack());
        needsUpdate = true;
      }
    }

    if (needsUpdate) {
      for (final var viewer : viewers) {
        viewer.updateInventory();
      }
    }
  }

  public void update() {
    items.forEach((slot, item) -> inventory.setItem(slot, item.itemStack()));
    dynamicItems.forEach((slot, item) -> inventory.setItem(slot, item.itemStack()));
    statefulItems.forEach((slot, item) -> inventory.setItem(slot, item.itemStack()));
  }

  public void update(final int slot) {
    item(slot).ifPresent(item -> inventory.setItem(slot, item.itemStack()));
  }

  public @NotNull java.util.List<Player> viewers() {
    return inventory.getViewers().stream()
        .filter(viewer -> viewer instanceof Player)
        .map(viewer -> (Player) viewer)
        .toList();
  }

  void handleClick(final @NotNull ClickContext context) {
    context.cancel();

    if (globalClickHandler != null) {
      globalClickHandler.accept(context);
    }

    final var slot = context.slot();

    dynamicItem(slot).ifPresent(item -> {
      final var handler = item.resolve().clickHandler();
      handler.ifPresent(h -> h.handle(context));
    });

    statefulItem(slot).ifPresent(item -> {
      final var handler = item.resolve().clickHandler();
      handler.ifPresent(h -> h.handle(context));
    });

    item(slot).ifPresent(item -> item.clickHandler().ifPresent(handler -> {
      handler.handle(context);
      if (item.shouldUpdateOnClick()) {
        update(slot);
      }
    }));
  }

  void handleClose(final @NotNull Player player) {
    OPEN_GUIS.remove(player.getUniqueId());

    if (viewers().isEmpty()) {
      stopUpdating();
    }

    if (closeHandler != null) {
      closeHandler.accept(player);
    }
  }

  private void validateSlot(final int slot) {
    if (slot < 0 || slot >= size()) {
      throw new IllegalArgumentException("Slot " + slot + " out of bounds [0, " + size() + ")");
    }
  }

  private int toSlot(final int row, final int column) {
    if (row < 0 || row >= rows) {
      throw new IllegalArgumentException("Row " + row + " out of bounds [0, " + rows + ")");
    }
    if (column < 0 || column >= 9) {
      throw new IllegalArgumentException("Column " + column + " out of bounds [0, 9)");
    }
    return row * 9 + column;
  }

  public static final class Builder {

    private final @NotNull String id;
    private @Nullable Component title;
    private @Nullable TranslationKey titleKey;
    private int rows = 3;
    private @Nullable Consumer<ClickContext> globalClickHandler;
    private @Nullable Consumer<Player> closeHandler;
    private @Nullable InventoryGui parent;
    private boolean preventClose;

    private Builder(final @NotNull String id) {
      this.id = id;
    }

    public @NotNull Builder title(final @NotNull Component title) {
      this.title = title;
      return this;
    }

    public @NotNull Builder title(final @NotNull String miniMessage) {
      this.title = ColorUtils.parse(miniMessage);
      return this;
    }

    public @NotNull Builder titleKey(final @NotNull TranslationKey key) {
      this.titleKey = key;
      return this;
    }

    public @NotNull Builder titleKey(final @NotNull String key) {
      return titleKey(TranslationKey.of(key));
    }

    public @NotNull Builder rows(final int rows) {
      if (rows < 1 || rows > 6) {
        throw new IllegalArgumentException("Rows must be between 1 and 6");
      }
      this.rows = rows;
      return this;
    }

    public @NotNull Builder onGlobalClick(final @NotNull Consumer<ClickContext> handler) {
      this.globalClickHandler = handler;
      return this;
    }

    public @NotNull Builder onClose(final @NotNull Consumer<Player> handler) {
      this.closeHandler = handler;
      return this;
    }

    public @NotNull Builder parent(final @NotNull InventoryGui parent) {
      this.parent = parent;
      return this;
    }

    public @NotNull Builder preventClose(final boolean prevent) {
      this.preventClose = prevent;
      return this;
    }

    public @NotNull InventoryGui build() {
      final var resolvedTitle = resolveTitle();
      final var gui = new InventoryGui(id, resolvedTitle, rows);
      gui.onGlobalClick(globalClickHandler);
      gui.onClose(closeHandler);
      gui.parent(parent);
      gui.preventClose(preventClose);
      return gui;
    }

    private @NotNull Component resolveTitle() {
      if (titleKey != null) {
        return ColorUtils.parse(titleKey.translate());
      }

      if (title != null) {
        return title;
      }

      return Component.text(id);
    }
  }
}
