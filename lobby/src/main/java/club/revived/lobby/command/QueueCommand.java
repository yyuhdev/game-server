package club.revived.lobby.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import club.revived.lobby.gui.DuelQueueGui;
import club.revived.proto.v1.minigames.KitType;

public final class QueueCommand implements CommandExecutor {

  private final DuelQueueGui gui;

  public QueueCommand(final JavaPlugin plugin) {
    this.gui = new DuelQueueGui(plugin);
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
      @NotNull String @NotNull [] args) {
    this.gui.open((Player) sender, 67, KitType.KIT_TYPE_AXE);
    return false;
  }
}
