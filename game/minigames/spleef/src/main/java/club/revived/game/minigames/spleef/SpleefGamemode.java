package club.revived.game.minigames.spleef;

import club.revived.game.api.gamemode.Gamemode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class SpleefGamemode extends Gamemode {

  public SpleefGamemode(final @NotNull Plugin plugin) {
    super("spleef", "Spleef", plugin);
  }

  @Override
  protected void onEnable() {
    getLogger().info("Spleef gamemode bout to go crazy");
  }

  @Override
  protected void onDisable() {
    getLogger().info("Spleef gamemode out here");
  }
}
