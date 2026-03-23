package club.revived.game.server.gamemode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import club.revived.game.api.gamemode.Gamemode;

public final class GamemodeRegistry {

  private final Map<String, Gamemode> gamemodes = new HashMap<>();

  public void register(final @NotNull Gamemode gamemode) {
    final var id = gamemode.getId();

    if (gamemodes.containsKey(id)) {
      throw new IllegalArgumentException("Gamemode with id '" + id + "' is already registered");
    }

    gamemodes.put(id, gamemode);
    gamemode.enable();
  }

  public void unregister(final @NotNull String id) {
    final var gamemode = gamemodes.remove(id);
    if (gamemode != null) {
      gamemode.disable();
    }
  }

  public void unregister(final @NotNull Gamemode gamemode) {
    unregister(gamemode.getId());
  }

  @NotNull
  public Optional<Gamemode> getGamemode(final @NotNull String id) {
    return Optional.ofNullable(gamemodes.get(id));
  }

  @NotNull
  public Collection<Gamemode> getGamemodes() {
    return gamemodes.values();
  }

  public boolean isRegistered(final @NotNull String id) {
    return gamemodes.containsKey(id);
  }

  public void enableAll() {
    gamemodes.values().forEach(Gamemode::enable);
  }

  public void disableAll() {
    gamemodes.values().forEach(Gamemode::disable);
  }
}
