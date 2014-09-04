package org.apache.streams.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ComponentUtils {

    /**
     * Offer until success
     * @param entry
     * The entry you want to add
     * @param queue
     * The queue you want to add
     * @param waitTime
     * The wait time you want to have
     */
    public static void offerUntilSuccess(Object entry, Queue<Object> queue, int waitTime) {
        if(queue instanceof ArrayBlockingQueue) {


            boolean notPushed = true;
            try {
                while((notPushed = !((ArrayBlockingQueue)queue).offer(entry, 1000, TimeUnit.MILLISECONDS))) {
                    safeQuickRest(1);
                }
            }
            catch(InterruptedException ioe) {
                /* no op */
            }
        }
        else {
            while (!queue.offer(entry)) {
                safeQuickRest(waitTime);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void offerUntilSuccess(Object entry, Queue queue) {
        int waitTime = 1;
        while(!queue.offer(entry)) {
            safeQuickRest(waitTime);
            waitTime = ((waitTime * 2) > 5000) ? (waitTime * 2) : waitTime;
        }
    }

    private static void safeQuickRest() {
        safeQuickRest(1);
    }

    private static void safeQuickRest(final int time) {
        try {
            Thread.yield();
            Thread.sleep(time);

        }
        catch(InterruptedException e) {
            // No Operation
        }
    }

    public static String pollUntilStringNotEmpty(final Queue queue) {

        String result = null;
        do {
            result = (String)queue.poll();
            if(result == null)
                safeQuickRest();
        }
        while (result == null || StringUtils.isEmpty(result));

        return result;
    }

}
