package club.revived.game.api.gamemode.handler;

import org.jetbrains.annotations.NotNull;

public interface IGameHandlerFactory {

  @NotNull
  AbstractGameHandler createHandler(final String id);
}
