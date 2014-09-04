package org.apache.streams.builders.threaded;

import com.google.common.util.concurrent.*;
import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;

public class ThreadingController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ThreadingController.class);

    private final ThreadPoolExecutor threadPoolExecutor;
    private final ThreadPoolExecutor queueShuffler;


    private final ListeningExecutorService listeningExecutorService;

    private final Condition itemPoppedCondition = new SimpleCondition();
    public final Condition conditionWorking = new SimpleCondition();
    private int numThreads;

    private final Map<StreamsTask, Boolean> workingFlags = new HashMap<StreamsTask, Boolean>();

    public ThreadingController(int numThreads) {
        this.numThreads = numThreads == 0 ? 4 : numThreads;

        this.threadPoolExecutor = new ThreadPoolExecutor(
                this.numThreads,
                this.numThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(this.numThreads));

        this.queueShuffler = new ThreadPoolExecutor(
                1,
                this.numThreads,
                0L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(this.numThreads),
                new ThreadPoolExecutor.CallerRunsPolicy());


        this.listeningExecutorService = MoreExecutors.listeningDecorator(this.threadPoolExecutor);
    }

    public Condition getConditionWorking() {
        return this.conditionWorking;
    }

    public Condition getItemPoppedCondition() {
        return itemPoppedCondition;
    }

    public boolean isWorking() {
        synchronized (this.workingFlags) {

            if (getWorkingCount() > 0) {
                return true;
            }

            for (Boolean flagWorking : this.workingFlags.values())
                if (flagWorking)
                    return true;

            return false;
        }
    }

    public void shutDown() {
        try {
            LOGGER.info("Thread Handler: Requesting to Shut Down");

            if(!this.queueShuffler.isShutdown()) {
                this.queueShuffler.shutdown();
                if(!this.queueShuffler.awaitTermination(5, TimeUnit.MINUTES))
                    this.queueShuffler.shutdownNow();
            }

            if (!this.listeningExecutorService.isShutdown()) {
                // tell the executor to shutdown.
                this.listeningExecutorService.shutdown();
                if (!this.listeningExecutorService.awaitTermination(5, TimeUnit.MINUTES))
                    this.listeningExecutorService.shutdownNow();
            }

            LOGGER.info("Thread Handler: Shut Down");
        }
        catch(InterruptedException ioe) {
            LOGGER.warn("Error shutting down worker thread handler: {}", ioe.getMessage());
        }
    }

    public void flagWorking(StreamsTask task) {
        synchronized (this.workingFlags) {
            this.workingFlags.put(task, true);
        }
    }

    public void flagNotWorking(StreamsTask task) {
        synchronized (this.workingFlags) {
            this.workingFlags.put(task, false);

            if (!this.isWorking())
                this.conditionWorking.signalAll();
        }
    }

    public int getWorkingCount() {
        return this.threadPoolExecutor.getActiveCount() + this.queueShuffler.getActiveCount();
    }

    public synchronized void execute(Runnable command, FutureCallback responseHandler) {
        synchronized (this) {
            waitForQueue();
            Futures.addCallback(this.listeningExecutorService.submit(command), responseHandler, this.queueShuffler);
        }
    }

    public synchronized void execute(Callable<List<StreamsDatum>> command, FutureCallback<List<StreamsDatum>> responseHandler) {
        synchronized (this) {
            waitForQueue();
            Futures.addCallback(this.listeningExecutorService.submit(command), responseHandler, this.queueShuffler);
        }
    }

    protected synchronized void waitForQueue() {
        if(this.threadPoolExecutor.getQueue().size() == this.numThreads) {
            while (this.threadPoolExecutor.getQueue().size() == this.numThreads) {
                try {
                    this.itemPoppedCondition.await();
                } catch (InterruptedException ioe) {
                    LOGGER.warn("Interrupted: {}", ioe.getMessage());
                }
            }
        }
    }

}
