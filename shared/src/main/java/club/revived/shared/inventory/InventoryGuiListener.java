package club.revived.shared.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class InventoryGuiListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInventoryClick(final @NotNull InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof final Player player)) {
      return;
    }

    if (!(event.getInventory().getHolder() instanceof final InventoryGui gui)) {
      return;
    }

    if (event.getClickedInventory() == null) {
      return;
    }

    if (!event.getClickedInventory().equals(gui.getInventory())) {
      event.setCancelled(true);
      return;
    }

    final var context = ClickContext.from(event);
    gui.handleClick(context);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInventoryDrag(final @NotNull InventoryDragEvent event) {
    if (!(event.getInventory().getHolder() instanceof InventoryGui)) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onInventoryClose(final @NotNull InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof final Player player)) {
      return;
    }

    if (!(event.getInventory().getHolder() instanceof final InventoryGui gui)) {
      return;
    }

    if (gui.preventsClose()) {
      gui.open(player);
      return;
    }

    gui.handleClose(player);
  }
}
