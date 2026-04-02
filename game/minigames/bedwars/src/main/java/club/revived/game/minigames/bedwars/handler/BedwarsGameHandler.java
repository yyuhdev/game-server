package club.revived.game.minigames.bedwars.handler;

import club.revived.game.api.gamemode.handler.AbstractGameHandler;

public final class BedwarsGameHandler extends AbstractGameHandler {

  @Override
  public void endGame() {

  }

  @Override
  public void setupGame() {

  }

  @Override
  public void startGame() {

  }

@Override
  public Runnable matchTimeout() {
    return () -> {
      for (final var player : super.players()) {
      }
    };
  }
}
