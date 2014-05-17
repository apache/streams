package org.apache.streams.local.tasks;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A rejection handler that just pauses execution and let's
 * the item wait until a free spot opens up for it to go into
 */
public class WaitUntilAvailableExecutionHandler implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // Wait until the pool is free for another item
        while(executor.getMaximumPoolSize() == executor.getQueue().size()) {
            safeSleep();
        }
        executor.submit(r);
    }

    public static void safeSleep() {
        Thread.yield();
        try {
            // wait one tenth of a millisecond
            Thread.sleep(0, (1000000 / 10));
            Thread.yield();
        }
        catch(Exception e) {
            // no operation
        }
        Thread.yield();
    }
}