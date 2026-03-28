package club.revived.services.queue.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

public final class QueueTask {

  private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

  private static final Logger LOGGER = Logger.getLogger(QueueTask.class.getSimpleName());

  public QueueTask() {
  }

  @NotNull
  public ScheduledFuture<?> start() {
    return this.service.scheduleAtFixedRate(() -> {
      LOGGER.info("test");
    }, 0, 1, TimeUnit.SECONDS);
  }

}
