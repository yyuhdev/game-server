package club.revived.game.minigames.bedwars.handler;

import org.jetbrains.annotations.NotNull;

import club.revived.game.api.gamemode.handler.AbstractGameHandler;
import club.revived.game.api.gamemode.handler.IGameHandlerFactory;
import club.revived.proto.v1.minigames.GameMeta;

public final class BedwarsGameHandlerFactory implements IGameHandlerFactory {

  @Override
  public @NotNull AbstractGameHandler createHandler(
      final @NotNull GameMeta meta) {
    return new BedwarsGameHandler();
  }
}
