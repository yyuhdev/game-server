package club.revived.shared.inventory;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ClickHandler {

  void handle(final @NotNull ClickContext context);

  default @NotNull ClickHandler andThen(final @NotNull ClickHandler after) {
    return context -> {
      handle(context);
      after.handle(context);
    };
  }

  static @NotNull ClickHandler cancelling() {
    return ClickContext::cancel;
  }

  static @NotNull ClickHandler empty() {
    return context -> {};
  }
}
