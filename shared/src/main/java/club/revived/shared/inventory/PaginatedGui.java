package club.revived.shared.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import club.revived.shared.component.ColorUtils;
import club.revived.shared.translation.TranslationKey;
import net.kyori.adventure.text.Component;

public final class PaginatedGui<T> {

  private final @NotNull String id;
  private final @NotNull Component title;
  private final int rows;
  private final @NotNull List<Integer> contentSlots;
  private final @NotNull List<T> items;
  private final @NotNull Function<T, GuiItem> itemMapper;

  private @Nullable GuiItem previousPageItem;
  private @Nullable GuiItem nextPageItem;
  private @Nullable GuiItem pageInfoItem;
  private int previousPageSlot = -1;
  private int nextPageSlot = -1;
  private int pageInfoSlot = -1;

  private final @NotNull List<SlotItemPair> staticItems = new ArrayList<>();
  private @Nullable Consumer<ClickContext> globalClickHandler;
  private @Nullable Consumer<Player> closeHandler;
  private @Nullable InventoryGui parent;

  private int currentPage = 0;
  private @Nullable InventoryGui currentGui;

  private PaginatedGui(
      final @NotNull String id,
      final @NotNull Component title,
      final int rows,
      final @NotNull List<Integer> contentSlots,
      final @NotNull List<T> items,
      final @NotNull Function<T, GuiItem> itemMapper) {
    this.id = id;
    this.title = title;
    this.rows = rows;
    this.contentSlots = new ArrayList<>(contentSlots);
    this.items = new ArrayList<>(items);
    this.itemMapper = itemMapper;
  }

  public static <T> @NotNull Builder<T> builder(final @NotNull String id) {
    return new Builder<>(id);
  }

  public int currentPage() {
    return currentPage;
  }

  public int totalPages() {
    if (contentSlots.isEmpty()) {
      return 1;
    }
    return Math.max(1, (int) Math.ceil((double) items.size() / contentSlots.size()));
  }

  public int itemsPerPage() {
    return contentSlots.size();
  }

  public boolean hasNextPage() {
    return currentPage < totalPages() - 1;
  }

  public boolean hasPreviousPage() {
    return currentPage > 0;
  }

  public @NotNull List<T> items() {
    return List.copyOf(items);
  }

  public @NotNull PaginatedGui<T> setItems(final @NotNull List<T> newItems) {
    items.clear();
    items.addAll(newItems);
    return this;
  }

  public @NotNull PaginatedGui<T> addItem(final @NotNull T item) {
    items.add(item);
    return this;
  }

  public @NotNull PaginatedGui<T> removeItem(final @NotNull T item) {
    items.remove(item);
    return this;
  }

  public void open(final @NotNull Player player) {
    open(player, 0);
  }

  public void open(final @NotNull Player player, final int page) {
    currentPage = Math.max(0, Math.min(page, totalPages() - 1));
    currentGui = buildPage();
    currentGui.open(player);
  }

  public void nextPage(final @NotNull Player player) {
    if (hasNextPage()) {
      open(player, currentPage + 1);
    }
  }

  public void previousPage(final @NotNull Player player) {
    if (hasPreviousPage()) {
      open(player, currentPage - 1);
    }
  }

  public void refresh(final @NotNull Player player) {
    open(player, currentPage);
  }

  public void update() {
    if (currentGui != null) {
      final var newGui = buildPage();
      currentGui.viewers().forEach(player -> {
        newGui.open(player);
      });
      currentGui = newGui;
    }
  }

  private @NotNull InventoryGui buildPage() {
    final var gui = InventoryGui.builder(id + "_page_" + currentPage)
        .title(title)
        .rows(rows)
        .build();

    if (parent != null) {
      gui.parent(parent);
    }

    for (final var pair : staticItems) {
      gui.setItem(pair.slot(), pair.item());
    }

    final var startIndex = currentPage * contentSlots.size();
    final var endIndex = Math.min(startIndex + contentSlots.size(), items.size());

    for (int i = 0; i < contentSlots.size(); i++) {
      final var slot = contentSlots.get(i);
      final var itemIndex = startIndex + i;

      if (itemIndex < endIndex) {
        final var item = items.get(itemIndex);
        gui.setItem(slot, itemMapper.apply(item));
      }
    }

    if (previousPageSlot >= 0 && previousPageItem != null && hasPreviousPage()) {
      final var navItem = GuiItem.builder(previousPageItem.itemStack())
          .onClick(ctx -> previousPage(ctx.player()))
          .build();
      gui.setItem(previousPageSlot, navItem);
    }

    if (nextPageSlot >= 0 && nextPageItem != null && hasNextPage()) {
      final var navItem = GuiItem.builder(nextPageItem.itemStack())
          .onClick(ctx -> nextPage(ctx.player()))
          .build();
      gui.setItem(nextPageSlot, navItem);
    }

    if (pageInfoSlot >= 0 && pageInfoItem != null) {
      gui.setItem(pageInfoSlot, pageInfoItem);
    }

    if (globalClickHandler != null) {
      gui.onGlobalClick(globalClickHandler);
    }

    if (closeHandler != null) {
      gui.onClose(closeHandler);
    }

    return gui;
  }

  private record SlotItemPair(int slot, @NotNull GuiItem item) {
  }

  public static final class Builder<T> {

    private final @NotNull String id;
    private @Nullable Component title;
    private @Nullable TranslationKey titleKey;
    private int rows = 6;
    private @NotNull List<Integer> contentSlots = new ArrayList<>();
    private @NotNull List<T> items = new ArrayList<>();
    private @Nullable Function<T, GuiItem> itemMapper;

    private @Nullable GuiItem previousPageItem;
    private @Nullable GuiItem nextPageItem;
    private @Nullable GuiItem pageInfoItem;
    private int previousPageSlot = -1;
    private int nextPageSlot = -1;
    private int pageInfoSlot = -1;

    private final @NotNull List<SlotItemPair> staticItems = new ArrayList<>();
    private @Nullable Consumer<ClickContext> globalClickHandler;
    private @Nullable Consumer<Player> closeHandler;
    private @Nullable InventoryGui parent;

    private Builder(final @NotNull String id) {
      this.id = id;
    }

    public @NotNull Builder<T> title(final @NotNull Component title) {
      this.title = title;
      return this;
    }

    public @NotNull Builder<T> title(final @NotNull String miniMessage) {
      this.title = ColorUtils.parse(miniMessage);
      return this;
    }

    public @NotNull Builder<T> titleKey(final @NotNull TranslationKey key) {
      this.titleKey = key;
      return this;
    }

    public @NotNull Builder<T> titleKey(final @NotNull String key) {
      return titleKey(TranslationKey.of(key));
    }

    public @NotNull Builder<T> rows(final int rows) {
      if (rows < 1 || rows > 6) {
        throw new IllegalArgumentException("Rows must be between 1 and 6");
      }
      this.rows = rows;
      return this;
    }

    public @NotNull Builder<T> contentSlots(final @NotNull List<Integer> slots) {
      this.contentSlots = new ArrayList<>(slots);
      return this;
    }

    public @NotNull Builder<T> contentSlots(final int... slots) {
      this.contentSlots = new ArrayList<>();
      for (final var slot : slots) {
        this.contentSlots.add(slot);
      }
      return this;
    }

    public @NotNull Builder<T> contentArea(final int startRow, final int startCol, final int endRow, final int endCol) {
      this.contentSlots = new ArrayList<>();
      for (int row = startRow; row <= endRow; row++) {
        for (int col = startCol; col <= endCol; col++) {
          this.contentSlots.add(row * 9 + col);
        }
      }
      return this;
    }

    public @NotNull Builder<T> items(final @NotNull List<T> items) {
      this.items = new ArrayList<>(items);
      return this;
    }

    public @NotNull Builder<T> itemMapper(final @NotNull Function<T, GuiItem> mapper) {
      this.itemMapper = mapper;
      return this;
    }

    public @NotNull Builder<T> previousPage(final int slot, final @NotNull GuiItem item) {
      this.previousPageSlot = slot;
      this.previousPageItem = item;
      return this;
    }

    public @NotNull Builder<T> nextPage(final int slot, final @NotNull GuiItem item) {
      this.nextPageSlot = slot;
      this.nextPageItem = item;
      return this;
    }

    public @NotNull Builder<T> pageInfo(final int slot, final @NotNull GuiItem item) {
      this.pageInfoSlot = slot;
      this.pageInfoItem = item;
      return this;
    }

    public @NotNull Builder<T> staticItem(final int slot, final @NotNull GuiItem item) {
      this.staticItems.add(new SlotItemPair(slot, item));
      return this;
    }

    public @NotNull Builder<T> onGlobalClick(final @NotNull Consumer<ClickContext> handler) {
      this.globalClickHandler = handler;
      return this;
    }

    public @NotNull Builder<T> onClose(final @NotNull Consumer<Player> handler) {
      this.closeHandler = handler;
      return this;
    }

    public @NotNull Builder<T> parent(final @NotNull InventoryGui parent) {
      this.parent = parent;
      return this;
    }

    public @NotNull PaginatedGui<T> build() {
      if (itemMapper == null) {
        throw new IllegalStateException("Item mapper is required");
      }

      if (contentSlots.isEmpty()) {
        contentArea(1, 1, rows - 2, 7);
      }

      final var resolvedTitle = resolveTitle();

      final var gui = new PaginatedGui<>(id, resolvedTitle, rows, contentSlots, items, itemMapper);
      gui.previousPageItem = previousPageItem;
      gui.nextPageItem = nextPageItem;
      gui.pageInfoItem = pageInfoItem;
      gui.previousPageSlot = previousPageSlot;
      gui.nextPageSlot = nextPageSlot;
      gui.pageInfoSlot = pageInfoSlot;
      gui.staticItems.addAll(staticItems);
      gui.globalClickHandler = globalClickHandler;
      gui.closeHandler = closeHandler;
      gui.parent = parent;

      return gui;
    }

    private @NotNull Component resolveTitle() {
      if (titleKey != null) {
        return ColorUtils.parse(titleKey.translate());
      }
      if (title != null) {
        return title;
      }
      return Component.text(id);
    }
  }
}
