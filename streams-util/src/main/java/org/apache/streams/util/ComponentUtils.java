package org.apache.streams.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Queue;

public class ComponentUtils {

    @SuppressWarnings("unchecked")
    public static void offerUntilSuccess(Object entry, Queue queue) {
        while(!queue.offer(entry)) {
            Thread.yield();
            safeQuickRest(1);
        }
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
