package club.revived.game.minigames.bedwars.handler;

import org.jetbrains.annotations.NotNull;

import club.revived.game.api.gamemode.handler.AbstractGameHandler;
import club.revived.game.api.gamemode.handler.IGameHandlerFactory;

public final class BedwarsGameHandlerFactory implements IGameHandlerFactory {

  @Override
  public @NotNull AbstractGameHandler createHandler(final @NotNull String id) {
    return new BedwarsGameHandler();
  }
}
