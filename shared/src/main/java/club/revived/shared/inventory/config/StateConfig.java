package club.revived.shared.inventory.config;

import org.jetbrains.annotations.NotNull;

public record StateConfig(
    @NotNull String id,
    @NotNull String condition,
    @NotNull ItemConfig item
) {

  public static @NotNull StateConfig of(
      final @NotNull String id,
      final @NotNull String condition,
      final @NotNull ItemConfig item) {
    return new StateConfig(id, condition, item);
  }
}
