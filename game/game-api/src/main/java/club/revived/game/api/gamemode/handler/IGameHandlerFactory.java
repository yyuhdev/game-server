package club.revived.game.api.gamemode.handler;

import org.jetbrains.annotations.NotNull;

import club.revived.proto.v1.minigames.GameMeta;

public interface IGameHandlerFactory {

  @NotNull
  AbstractGameHandler createHandler(final @NotNull GameMeta meta);
}
