package org.apache.streams.builders.threaded;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class ThreadingController {

    private final ThreadPoolExecutor threadPoolExecutor;
    private final Condition writersDepleted = new SimpleCondition();
    private final Condition providersDepleted = new SimpleCondition();
    private final Condition itemPoppedCondition = new SimpleCondition();

    public ThreadingController(int numThreads) {
        numThreads = numThreads == 0 ? 4 : numThreads;

        this.threadPoolExecutor = new ThreadPoolExecutor(numThreads,
                numThreads,
                0l,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(numThreads, false),
                new WaitUntilAvailableExecutionHandler(this.itemPoppedCondition));
    }

    public Condition getItemPoppedCondition() {
        return itemPoppedCondition;
    }

    public Condition getProvidersDepleted() {
        return this.providersDepleted;
    }

    public Condition getWritersDepleted() {
        return this.writersDepleted;
    }

    public void execute(Runnable command) {
        this.threadPoolExecutor.execute(command);
    }

}
