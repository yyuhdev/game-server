package club.revived.game.minigames.bedwars;

import club.revived.game.api.gamemode.Gamemode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class BedwarsGamemode extends Gamemode {

  public BedwarsGamemode(final @NotNull Plugin plugin) {
    super("bedwars", "BedWars", plugin);
  }

  @Override
  protected void onEnable() {
    getLogger().info("BedWars gamemode is locked in fr!");
  }

  @Override
  protected void onDisable() {
    getLogger().info("BedWars gamemode shutting down, no cap");
  }
}
