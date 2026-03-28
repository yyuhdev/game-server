package club.revived.services.queue;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import club.revived.shared.result.Result;

public class Main {
  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
  private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

  public static void main(final @NotNull String[] args) {
    LOGGER.info("Starting Queue Service...");

    final var service = new QueueService();
    service.start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      LOGGER.info("Shutting down Queue Service...");
      service.stop();
      shutdownLatch.countDown();
    }));

    LOGGER.info("Queue Service started successfully");

    Result.of(() -> {
      shutdownLatch.await();

      return null;
    }).ifErr(err -> {
      Thread.currentThread().interrupt();
    });
  }
}
