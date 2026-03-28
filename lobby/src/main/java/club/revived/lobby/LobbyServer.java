package club.revived.lobby;

import java.util.Locale;

import org.bukkit.plugin.java.JavaPlugin;

import club.revived.lobby.command.QueueCommand;
import club.revived.shared.inventory.InventoryGuiListener;
import club.revived.shared.result.Result;
import club.revived.shared.translation.TranslationEngine;
import club.revived.shared.translation.TranslationKey;

public final class LobbyServer extends JavaPlugin {

  @Override
  public void onLoad() {
  }

  @Override
  public void onEnable() {
    Result.of(() -> {
      return this.getDataFolder().mkdir();
    });

    getServer().getPluginManager().registerEvents(new InventoryGuiListener(), this);

    final var translationEngine = TranslationEngine.create(Locale.ENGLISH);

    translationEngine.loadAndRegisterFromClasspath("translations/en.json", Locale.ENGLISH);
    translationEngine.loadAndRegisterFromClasspath("translations/de.json", Locale.GERMAN);

    TranslationKey.setEngine(translationEngine);

    this.saveResource("gui/duel-queue-menu.yml", false);

    getCommand("test").setExecutor(new QueueCommand(this));
  }

  @Override
  public void onDisable() {
  }
}
