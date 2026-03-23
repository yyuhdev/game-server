package club.revived.game.server;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import club.revived.game.minigames.bedwars.BedwarsGamemode;
import club.revived.game.minigames.duels.DuelsGamemode;
import club.revived.game.minigames.skywars.SkywarsGamemode;
import club.revived.game.minigames.spleef.SpleefGamemode;
import club.revived.game.server.gamemode.GamemodeRegistry;

public final class GameServer extends JavaPlugin {

  @Override
  public void onLoad() {
  }

  @Override
  public void onEnable() {
    final var gameModeRegistry = new GamemodeRegistry();

    final var gamemodes = List.of(
        new BedwarsGamemode(this),
        new DuelsGamemode(this),
        new SpleefGamemode(this),
        new SkywarsGamemode(this));

    gamemodes.forEach(gamemode -> {
      gameModeRegistry.register(gamemode);
    });

    gameModeRegistry.enableAll();
  }

  @Override
  public void onDisable() {
  }
}
