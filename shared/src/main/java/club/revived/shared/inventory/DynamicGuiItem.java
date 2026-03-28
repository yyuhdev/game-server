package club.revived.shared.inventory;

import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DynamicGuiItem {

  private final @NotNull Supplier<GuiItem> itemSupplier;
  private final int updateInterval;
  private @Nullable ClickHandler clickHandler;

  private DynamicGuiItem(
      final @NotNull Supplier<GuiItem> itemSupplier,
      final int updateInterval) {
    this.itemSupplier = itemSupplier;
    this.updateInterval = updateInterval;
  }

  public static @NotNull DynamicGuiItem of(
      final @NotNull Supplier<GuiItem> itemSupplier,
      final int updateInterval) {
    if (updateInterval < 1) {
      throw new IllegalArgumentException("Update interval must be at least 1 tick");
    }
    return new DynamicGuiItem(itemSupplier, updateInterval);
  }

  public static @NotNull DynamicGuiItem of(
      final @NotNull Supplier<ItemStack> itemSupplier) {
    return new DynamicGuiItem(() -> GuiItem.of(itemSupplier.get()), 1);
  }

  public int updateInterval() {
    return updateInterval;
  }

  public boolean shouldUpdate(final long tick) {
    return tick % updateInterval == 0;
  }

  public @NotNull GuiItem resolve() {
    final var item = itemSupplier.get();
    if (clickHandler != null) {
      item.onClick(clickHandler);
    }
    return item;
  }

  public @NotNull ItemStack itemStack() {
    return resolve().itemStack();
  }

  public @NotNull DynamicGuiItem onClick(final @Nullable ClickHandler handler) {
    this.clickHandler = handler;
    return this;
  }

  public @NotNull Supplier<GuiItem> supplier() {
    return itemSupplier;
  }
}
