package club.revived.game.minigames.duels;

import club.revived.game.api.gamemode.Gamemode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class DuelsGamemode extends Gamemode {

  public DuelsGamemode(final @NotNull Plugin plugin) {
    super("duels", "Duels", plugin);
  }

  @Override
  protected void onEnable() {
    getLogger().info("Duels gamemode ready to pop off");
  }

  @Override
  protected void onDisable() {
    getLogger().info("Duels gamemode dipping out");
  }
}
