package club.revived.lobby.gui;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import club.revived.proto.v1.minigames.KitType;
import club.revived.shared.inventory.ClickHandler;
import club.revived.shared.inventory.config.GuiConfigLoader;
import club.revived.shared.inventory.config.GuiFactory;
import club.revived.shared.translation.TranslationKey;

public final class DuelQueueGui {

  private static final String MENU_ID = "duel-queue-menu";
  private static final String MENU_RESOURCE = "gui/duel-queue-menu.yml";

  private final @NotNull JavaPlugin plugin;
  private final @NotNull GuiFactory factory;
  private final @NotNull Set<UUID> queuedPlayers = ConcurrentHashMap.newKeySet();
  private final @NotNull Map<UUID, AtomicInteger> playerQueueTimes = new ConcurrentHashMap<>();
  private @Nullable BukkitTask timerTask;

  public DuelQueueGui(final @NotNull JavaPlugin plugin) {
    this.plugin = plugin;
    final var loader = new GuiConfigLoader();
    final var menuPath = new File(plugin.getDataFolder(), MENU_RESOURCE);

    final var result = loader.load(Path.of(menuPath.toURI()));
    if (result.isErr()) {
      plugin.getLogger().severe("Failed to load duel queue menu: " + result.unwrapErr());
    }

    this.factory = new GuiFactory(loader)
        .action("queue", (String id) -> ctx -> joinQueue(ctx.player(), id))
        .action("leave-queue", (ClickHandler) ctx -> handleLeaveQueue(ctx.player()))
        .action("close", (ClickHandler) ctx -> ctx.player().closeInventory())
        .condition("in_queue", p -> queuedPlayers.contains(p.getUniqueId()))
        .condition("not_in_queue", p -> !queuedPlayers.contains(p.getUniqueId()));

    startTimerTask();
  }

  private void startTimerTask() {
    timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      playerQueueTimes.forEach((uuid, time) -> time.incrementAndGet());
    }, 20L, 20L);
  }

  public void shutdown() {
    if (timerTask != null) {
      timerTask.cancel();
      timerTask = null;
    }
  }

  public void open(
      final @NotNull Player player,
      final int playersInQueue,
      final @Nullable KitType kitType) {
    final var playerUuid = player.getUniqueId();
    final var queueTime = playerQueueTimes.computeIfAbsent(playerUuid, k -> new AtomicInteger(0));

    final Map<String, Supplier<String>> placeholders = new HashMap<>();
    placeholders.put("players_in_queue", () -> String.valueOf(queuedPlayers.size()));
    placeholders.put("selected_kit", () -> kitType != null ? kitType.name() : "None");
    placeholders.put("player", player::getName);
    placeholders.put("queue_time", () -> formatTime(queueTime.get()));

    factory.create(MENU_ID, player, placeholders).ifPresentOrElse(gui -> {
      gui.open(player);
      gui.startUpdating(plugin);
    }, () -> {
      player.sendRichMessage(TranslationKey.of("gui.error").translate());
    });
  }

  private @NotNull String formatTime(final int seconds) {
    final var minutes = seconds / 60;
    final var secs = seconds % 60;
    return String.format("%d:%02d", minutes, secs);
  }

  private void joinQueue(final @NotNull Player player, final @NotNull String queue) {
    final var uuid = player.getUniqueId();
    queuedPlayers.add(uuid);
    playerQueueTimes.computeIfAbsent(uuid, k -> new AtomicInteger(0)).set(0);
    player.sendRichMessage("<green>You have joined the " + queue + " queue!</green>");
  }

  private void handleLeaveQueue(final @NotNull Player player) {
    final var uuid = player.getUniqueId();
    queuedPlayers.remove(uuid);
    playerQueueTimes.remove(uuid);
    player.sendRichMessage("<red>You have left the duel queue.</red>");
  }

  public boolean isQueued(final @NotNull Player player) {
    return queuedPlayers.contains(player.getUniqueId());
  }
}
