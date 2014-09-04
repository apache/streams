package org.apache.streams.builders.threaded;

import com.google.common.util.concurrent.*;
import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

public class ThreadingController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ThreadingController.class);

    public static class LockCounter {

        private final AtomicInteger lock = new AtomicInteger(0);
        private final Condition popped = new SimpleCondition();
        private final AtomicBoolean working = new AtomicBoolean(false);

        public LockCounter() {

        }

        public Condition getPopped() {
            return this.popped;
        }

        public AtomicInteger getLock() {
            return this.lock;
        }

        public AtomicBoolean isWorking() {
            return this.working;
        }
    }


    private final Map<StreamsTask, LockCounter> queueLockCounts = new HashMap<StreamsTask, LockCounter>();
    private final ThreadPoolExecutor threadPoolExecutor;
    private final ThreadPoolExecutor queueShuffler;


    private final ListeningExecutorService listeningExecutorService;

    private final Condition itemPoppedCondition = new SimpleCondition();
    public final Condition conditionWorking = new SimpleCondition();
    private int numThreads;

    public ThreadingController(int numThreads) {
        this.numThreads = numThreads == 0 ? 4 : numThreads;

        this.threadPoolExecutor = new ThreadPoolExecutor(
                this.numThreads,
                this.numThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(this.numThreads));

        this.queueShuffler = new ThreadPoolExecutor(
                this.numThreads,
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

    public Map<StreamsTask, LockCounter> getQueueLockCounts() {
        return this.queueLockCounts;
    }

    public boolean isWorking() {

        // The number of queues that are currently working
        if(getWorkingCount() > 0)
            return true;

        // the number of locks that are currently active.
        for (LockCounter lock : this.queueLockCounts.values())
            if(lock.isWorking().get())
                return true;

        return false;
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

    public void register(StreamsTask task) {
        if(!this.queueLockCounts.containsKey(task))
            this.queueLockCounts.put(task, new LockCounter());
    }

    public void flagWorking(StreamsTask task) {
        this.queueLockCounts.get(task).isWorking().set(true);
    }

    public void flagNotWorking(StreamsTask task) {
        this.queueLockCounts.get(task).isWorking().set(false);

        // exit if the lock says we are working
        for (LockCounter lock : this.queueLockCounts.values())
            if(lock.isWorking().get())
                return;

        // exit if the task says it is working
        for(StreamsTask t : this.queueLockCounts.keySet())
            if(t.getCurrentStatus().getQueue() > 0 || t.getCurrentStatus().getWorking() > 0)
                return;

        this.conditionWorking.signalAll();
    }

    public int getWorkingCount() {
        return this.threadPoolExecutor.getActiveCount() + this.queueShuffler.getActiveCount();
    }

    public void execute(Runnable command, FutureCallback responseHandler) {
        synchronized (this) {
            waitForQueue();
            Futures.addCallback(this.listeningExecutorService.submit(command), responseHandler, this.queueShuffler);
        }
    }

    public void execute(Callable<List<StreamsDatum>> command, FutureCallback<List<StreamsDatum>> responseHandler) {
        synchronized (this) {
            waitForQueue();
            Futures.addCallback(this.listeningExecutorService.submit(command), responseHandler, this.queueShuffler);
        }
    }

    protected void waitForQueue() {
        if (this.threadPoolExecutor.getQueue().size() == this.numThreads) {
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
