package club.revived.game.server.handler;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import club.revived.game.api.gamemode.handler.AbstractGameHandler;
import club.revived.game.api.gamemode.handler.IGameHandlerFactory;
import club.revived.game.minigames.bedwars.handler.BedwarsGameHandlerFactory;
import club.revived.shared.result.Result;

public final class GameHandlerRegistry {

  private final Map<String, IGameHandlerFactory> factories = new HashMap<>();

  public GameHandlerRegistry() {
    this.factories.put("bedwars", new BedwarsGameHandlerFactory());
  }

  @NotNull
  public Result<AbstractGameHandler, String> handler(final @NotNull String id) {
    if (!factories.containsKey(id)) {
      return Result.err("Factory with id '" + id + "' is not registered.");
    }

    final var factory = factories.get(id);
    final var handler = factory.createHandler(id);

    return Result.ok(handler);
  }

}
