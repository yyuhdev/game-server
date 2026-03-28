package club.revived.game.api.gamemode;

import club.revived.proto.v1.minigames.GameMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameTimer {

  private final Plugin plugin;
  private final int durationSeconds;
  private final Runnable onTimerEnd;

  private @Nullable BukkitTask timerTask;
  private int remainingSeconds;
  private boolean running;
  private boolean paused;

  public GameTimer(
      final @NotNull Plugin plugin,
      final @NotNull GameMeta meta,
      final @NotNull Runnable onTimerEnd) {
    this.plugin = plugin;
    this.durationSeconds = meta.getMaxDurationSeconds();
    this.remainingSeconds = this.durationSeconds;
    this.onTimerEnd = onTimerEnd;
    this.running = false;
    this.paused = false;
  }

  public void start() {
    if (running) {
      return;
    }

    running = true;
    paused = false;

    timerTask = new BukkitRunnable() {
      @Override
      public void run() {
        if (paused) {
          return;
        }

        if (remainingSeconds <= 0) {
          stop();
          onTimerEnd.run();
          return;
        }

        remainingSeconds--;
      }
    }.runTaskTimer(plugin, 0L, 20L);
  }

  public void stop() {
    running = false;
    paused = false;

    if (timerTask != null) {
      timerTask.cancel();
      timerTask = null;
    }
  }

  public void pause() {
    if (running && !paused) {
      paused = true;
    }
  }

  public void resume() {
    if (running && paused) {
      paused = false;
    }
  }

  public void reset() {
    remainingSeconds = durationSeconds;
  }

  public void addTime(final int seconds) {
    remainingSeconds = Math.max(0, remainingSeconds + seconds);
  }

  public void setRemainingTime(final int seconds) {
    remainingSeconds = Math.max(0, seconds);
  }

  public int getRemainingSeconds() {
    return remainingSeconds;
  }

  public int getDurationSeconds() {
    return durationSeconds;
  }

  public boolean isRunning() {
    return running;
  }

  public boolean isPaused() {
    return paused;
  }

  public int getElapsedSeconds() {
    return durationSeconds - remainingSeconds;
  }

  @NotNull
  public String getFormattedTime() {
    int minutes = remainingSeconds / 60;
    int seconds = remainingSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }
}
