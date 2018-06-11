package org.apache.streams.core.util;

import org.apache.streams.config.StreamsConfigurator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {

  public static ExecutorService newFixedThreadPoolWithQueueSize(int numThreads, int queueSize) {
    return new ThreadPoolExecutor(numThreads, numThreads,
      5000L, TimeUnit.MILLISECONDS,
      new ArrayBlockingQueue<>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
  }

  public static void shutdownAndAwaitTermination(ExecutorService pool) {
    pool.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!pool.awaitTermination(StreamsConfigurator.detectConfiguration().getProviderTimeoutMs(), TimeUnit.SECONDS)) {
        pool.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!pool.awaitTermination(StreamsConfigurator.detectConfiguration().getProviderWaitMs(), TimeUnit.SECONDS)) {
          System.err.println("Pool did not terminate");
        }
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      pool.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }
}
