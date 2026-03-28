package club.revived.shared.inventory.config;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SlotMapping(
    @NotNull String itemRef,
    @Nullable Integer slot,
    @Nullable List<Integer> slots,
    @Nullable String pattern,
    @Nullable String action,
    @Nullable Integer updateInterval,
    @Nullable List<StateConfig> states
) {

  public static @NotNull SlotMapping single(final @NotNull String itemRef, final int slot) {
    return new SlotMapping(itemRef, slot, null, null, null, null, null);
  }

  public static @NotNull SlotMapping single(
      final @NotNull String itemRef,
      final int slot,
      final @Nullable String action
  ) {
    return new SlotMapping(itemRef, slot, null, null, action, null, null);
  }

  public static @NotNull SlotMapping multi(final @NotNull String itemRef, final @NotNull List<Integer> slots) {
    return new SlotMapping(itemRef, null, slots, null, null, null, null);
  }

  public static @NotNull SlotMapping pattern(final @NotNull String itemRef, final @NotNull String pattern) {
    return new SlotMapping(itemRef, null, null, pattern, null, null, null);
  }

  public boolean isSingle() {
    return slot != null;
  }

  public boolean isMulti() {
    return slots != null && !slots.isEmpty();
  }

  public boolean isPattern() {
    return pattern != null;
  }

  public boolean isDynamic() {
    return updateInterval != null && updateInterval > 0;
  }

  public boolean isStateful() {
    return states != null && !states.isEmpty();
  }
}
