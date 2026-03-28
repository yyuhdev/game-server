package club.revived.services.queue;

import java.util.logging.Logger;

import club.revived.celery.Celery;
import club.revived.services.queue.task.QueueTask;

public class QueueService {
  private static final Logger LOGGER = Logger.getLogger(QueueService.class.getName());
  private QueueTask queueTask;

  public QueueService() {
    LOGGER.info("Initializing Queue Service components...");
  }

  public void start() {
    LOGGER.info("Setting up Celery...");

    Celery.builder()
        .dragonfly()
        .influx()
        .mongo()
        .nats()
        .build();

    LOGGER.info("Starting Queue Service components...");

    this.queueTask = new QueueTask();
    this.queueTask.start();

    LOGGER.info("Queue Service is now running");
  }

  public void stop() {
    LOGGER.info("Stopping Queue Service components...");

    LOGGER.info("Queue Service stopped");
  }
}
