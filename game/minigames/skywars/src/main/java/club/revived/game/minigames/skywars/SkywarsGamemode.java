package club.revived.game.minigames.skywars;

import club.revived.game.api.gamemode.Gamemode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class SkywarsGamemode extends Gamemode {

  public SkywarsGamemode(final @NotNull Plugin plugin) {
    super("skywars", "SkyWars", plugin);
  }

  @Override
  protected void onEnable() {
    getLogger().info("SkyWars gamemode bout to hit different!");
  }

  @Override
  protected void onDisable() {
    getLogger().info("SkyWars gamemode peace out");
  }
}
