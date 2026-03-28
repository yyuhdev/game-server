package club.revived.shared.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StatefulGuiItem {

  private static final int DEFAULT_UPDATE_INTERVAL = 1;
  private static final String DEFAULT_STATE_ID = "__default__";

  private final @NotNull List<StateEntry> states;
  private final @NotNull Supplier<GuiItem> defaultItemSupplier;
  private final @NotNull Supplier<Player> playerSupplier;
  private final int updateInterval;
  private @Nullable ClickHandler clickHandler;
  private @Nullable String lastStateId;

  private StatefulGuiItem(
      final @NotNull List<StateEntry> states,
      final @NotNull Supplier<GuiItem> defaultItemSupplier,
      final @NotNull Supplier<Player> playerSupplier,
      final int updateInterval) {
    this.states = states;
    this.defaultItemSupplier = defaultItemSupplier;
    this.playerSupplier = playerSupplier;
    this.updateInterval = updateInterval;
  }

  public static @NotNull Builder builder(
      final @NotNull Supplier<GuiItem> defaultItemSupplier,
      final @NotNull Supplier<Player> playerSupplier) {
    return new Builder(defaultItemSupplier, playerSupplier);
  }

  public int updateInterval() {
    return updateInterval;
  }

  public boolean shouldUpdate(final long tick) {
    return tick % updateInterval == 0;
  }

  public boolean hasStateChanged() {
    final var currentStateId = resolveCurrentStateId();
    if (lastStateId == null || !lastStateId.equals(currentStateId)) {
      lastStateId = currentStateId;
      return true;
    }
    return false;
  }

  private @NotNull String resolveCurrentStateId() {
    final var player = playerSupplier.get();
    for (final var state : states) {
      if (player != null && state.condition().test(player)) {
        return state.id();
      }
    }
    return DEFAULT_STATE_ID;
  }

  public @NotNull GuiItem resolve() {
    final var player = playerSupplier.get();
    for (final var state : states) {
      if (player != null && state.condition().test(player)) {
        lastStateId = state.id();
        final var item = state.itemSupplier().get();
        if (clickHandler != null) {
          item.onClick(clickHandler);
        }
        return item;
      }
    }
    lastStateId = DEFAULT_STATE_ID;
    final var defaultItem = defaultItemSupplier.get();
    if (clickHandler != null) {
      defaultItem.onClick(clickHandler);
    }
    return defaultItem;
  }

  public @NotNull ItemStack itemStack() {
    return resolve().itemStack();
  }

  public @NotNull StatefulGuiItem onClick(final @Nullable ClickHandler handler) {
    this.clickHandler = handler;
    return this;
  }

  public @NotNull List<StateEntry> states() {
    return List.copyOf(states);
  }

  public record StateEntry(
      @NotNull String id,
      @NotNull Predicate<Player> condition,
      @NotNull Supplier<GuiItem> itemSupplier) {
  }

  public static final class Builder {

    private final @NotNull Supplier<GuiItem> defaultItemSupplier;
    private final @NotNull Supplier<Player> playerSupplier;
    private final @NotNull List<StateEntry> states = new ArrayList<>();
    private int updateInterval = DEFAULT_UPDATE_INTERVAL;
    private @Nullable ClickHandler clickHandler;

    private Builder(
        final @NotNull Supplier<GuiItem> defaultItemSupplier,
        final @NotNull Supplier<Player> playerSupplier) {
      this.defaultItemSupplier = defaultItemSupplier;
      this.playerSupplier = playerSupplier;
    }

    public @NotNull Builder state(
        final @NotNull String id,
        final @NotNull Predicate<Player> condition,
        final @NotNull Supplier<GuiItem> itemSupplier) {
      states.add(new StateEntry(id, condition, itemSupplier));
      return this;
    }

    public @NotNull Builder updateInterval(final int ticks) {
      if (ticks < 1) {
        throw new IllegalArgumentException("Update interval must be at least 1 tick");
      }
      this.updateInterval = ticks;
      return this;
    }

    public @NotNull Builder onClick(final @NotNull ClickHandler handler) {
      this.clickHandler = handler;
      return this;
    }

    public @NotNull StatefulGuiItem build() {
      final var stateful = new StatefulGuiItem(
          List.copyOf(states), defaultItemSupplier, playerSupplier, updateInterval);
      stateful.onClick(clickHandler);
      return stateful;
    }
  }
}
