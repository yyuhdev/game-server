package club.revived.shared.inventory;

import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

public enum ClickAction {
  LEFT,
  RIGHT,
  SHIFT_LEFT,
  SHIFT_RIGHT,
  MIDDLE,
  DROP,
  CTRL_DROP,
  DOUBLE_CLICK,
  NUMBER_KEY,
  UNKNOWN;

  public static @NotNull ClickAction from(final @NotNull ClickType clickType) {
    return switch (clickType) {
      case LEFT -> LEFT;
      case RIGHT -> RIGHT;
      case SHIFT_LEFT -> SHIFT_LEFT;
      case SHIFT_RIGHT -> SHIFT_RIGHT;
      case MIDDLE -> MIDDLE;
      case DROP -> DROP;
      case CONTROL_DROP, SWAP_OFFHAND -> CTRL_DROP;
      case DOUBLE_CLICK -> DOUBLE_CLICK;
      case NUMBER_KEY -> NUMBER_KEY;
      default -> UNKNOWN;
    };
  }
}
