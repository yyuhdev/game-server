package club.revived.game.server;

import java.util.List;
import java.util.Locale;

import org.bukkit.plugin.java.JavaPlugin;

import club.revived.game.minigames.bedwars.BedwarsGamemode;
import club.revived.game.minigames.duels.DuelsGamemode;
import club.revived.game.minigames.skywars.SkywarsGamemode;
import club.revived.game.server.gamemode.GamemodeRegistry;
import club.revived.shared.translation.TranslationEngine;
import club.revived.shared.translation.TranslationKey;

public final class GameServer extends JavaPlugin {

  @Override
  public void onLoad() {
  }

  @Override
  public void onEnable() {
    final var gameModeRegistry = new GamemodeRegistry();

    final var translationEngine = TranslationEngine.create(Locale.ENGLISH);

    translationEngine.loadAndRegisterFromClasspath("translations/en.json", Locale.ENGLISH);
    translationEngine.loadAndRegisterFromClasspath("translations/de.json", Locale.GERMAN);

    TranslationKey.setEngine(translationEngine);

    final var gamemodes = List.of(
        new BedwarsGamemode(this),
        new DuelsGamemode(this),
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
