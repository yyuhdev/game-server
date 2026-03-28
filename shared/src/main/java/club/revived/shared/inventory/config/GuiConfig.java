package club.revived.shared.inventory.config;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record GuiConfig(
    @NotNull String id,
    @NotNull String title,
    int rows,
    @Nullable String titleKey,
    boolean preventClose,
    @NotNull Map<String, ItemConfig> items,
    @NotNull List<SlotMapping> slots
) {

  public static @NotNull Builder builder(final @NotNull String id) {
    return new Builder(id);
  }

  public static final class Builder {

    private final @NotNull String id;
    private @NotNull String title = "";
    private int rows = 3;
    private @Nullable String titleKey;
    private boolean preventClose;
    private @NotNull Map<String, ItemConfig> items = Map.of();
    private @NotNull List<SlotMapping> slots = List.of();

    private Builder(final @NotNull String id) {
      this.id = id;
    }

    public @NotNull Builder title(final @NotNull String title) {
      this.title = title;
      return this;
    }

    public @NotNull Builder rows(final int rows) {
      this.rows = rows;
      return this;
    }

    public @NotNull Builder titleKey(final @Nullable String titleKey) {
      this.titleKey = titleKey;
      return this;
    }

    public @NotNull Builder preventClose(final boolean preventClose) {
      this.preventClose = preventClose;
      return this;
    }

    public @NotNull Builder items(final @NotNull Map<String, ItemConfig> items) {
      this.items = items;
      return this;
    }

    public @NotNull Builder slots(final @NotNull List<SlotMapping> slots) {
      this.slots = slots;
      return this;
    }

    public @NotNull GuiConfig build() {
      return new GuiConfig(id, title, rows, titleKey, preventClose, items, slots);
    }
  }
}
