package club.revived.game.api.gamemode.handler;

import module java.base;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import club.revived.proto.v1.minigames.GameState;
import club.revived.proto.v1.minigames.Team;

public abstract class AbstractGameHandler {

  protected GameState gameState;
  protected List<UUID> participants;
  protected List<Team> teams;

  public abstract void setupGame();

  public abstract void startGame();

  public abstract void endGame();

  public abstract Runnable matchTimeout();

  @NotNull
  protected List<Player> players() {
    return this.participants.stream()
        .map(Bukkit::getPlayer)
        .map(Objects::requireNonNull)
        .toList();
  }
}
