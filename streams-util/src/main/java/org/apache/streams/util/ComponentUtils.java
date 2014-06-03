package org.apache.streams.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Queue;

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
    public static void offerUntilSuccess(Object entry, Queue queue, int waitTime) {
        while(!queue.offer(entry)) {
            Thread.yield();
            safeQuickRest(1);
        }
    }


    @SuppressWarnings("unchecked")
    public static void offerUntilSuccess(Object entry, Queue queue) {
        offerUntilSuccess(entry, queue, 2);
    }

    private static void safeQuickRest() {
        safeQuickRest(1);
    }

    private static void safeQuickRest(final int time) {
        try {
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
