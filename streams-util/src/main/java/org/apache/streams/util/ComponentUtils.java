package org.apache.streams.util;

import java.util.Queue;

/**
 * Created by sblackmon on 3/31/14.
 */
public class ComponentUtils {

    public static void offerUntilSuccess(Object entry, Queue queue) {

        boolean success;
        do {
            synchronized( ComponentUtils.class ) {
                success = queue.offer(entry);
            }
            Thread.yield();
        }
        while( !success );
    }

}
