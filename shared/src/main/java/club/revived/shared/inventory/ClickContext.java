package club.revived.shared.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public record ClickContext(
    @NotNull Player player,
    @NotNull ClickAction action,
    int slot,
    int hotbarButton,
    @NotNull InventoryClickEvent event
) {

  public static @NotNull ClickContext from(final @NotNull InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof final Player player)) {
      throw new IllegalArgumentException("Click event must be from a player");
    }
    return new ClickContext(
        player,
        ClickAction.from(event.getClick()),
        event.getSlot(),
        event.getHotbarButton(),
        event
    );
  }

  public void cancel() {
    event.setCancelled(true);
  }

  public boolean isLeftClick() {
    return action == ClickAction.LEFT || action == ClickAction.SHIFT_LEFT;
  }

  public boolean isRightClick() {
    return action == ClickAction.RIGHT || action == ClickAction.SHIFT_RIGHT;
  }

  public boolean isShiftClick() {
    return action == ClickAction.SHIFT_LEFT || action == ClickAction.SHIFT_RIGHT;
  }
}
