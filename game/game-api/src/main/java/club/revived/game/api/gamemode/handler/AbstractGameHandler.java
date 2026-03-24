package club.revived.game.api.gamemode.handler;

import java.util.List;
import java.util.UUID;

import club.revived.proto.v1.minigames.GameState;
import club.revived.proto.v1.minigames.Team;

public abstract class AbstractGameHandler {

  protected GameState gameState;
  protected List<UUID> participants;
  protected List<Team> teams;

  public abstract void setupGame();

  public abstract void startGame();

  public abstract void endGame();
}
