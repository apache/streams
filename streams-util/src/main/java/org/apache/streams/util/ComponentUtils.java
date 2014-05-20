package org.apache.streams.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Queue;

/**
 * Created by sblackmon on 3/31/14.
 */
public class ComponentUtils {

    public static void offerUntilSuccess(Object entry, Queue queue) {
        while(!queue.offer(entry))
            Thread.yield();
    }

    public static String pollUntilStringNotEmpty(final Queue queue) {

        String result = null;
        do {
            try {
                result = (String) queue.remove();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            Thread.yield();
        }
        while (result == null && !StringUtils.isNotEmpty(result));

        return result;
    }

}
