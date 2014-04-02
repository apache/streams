package org.apache.streams.util;

import org.apache.commons.lang3.StringUtils;

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

    public static String pollUntilStringNotEmpty(Queue queue) {

        String result = null;
        do {
            synchronized( ComponentUtils.class ) {
                try {
                    result = (String) queue.remove();
                } catch( Exception e ) {}
            }
            Thread.yield();
        }
        while( result == null && !StringUtils.isNotEmpty(result) );

        return result;
    }

}
