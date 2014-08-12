package org.apache.streams.util;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A {@link java.util.concurrent.ConcurrentLinkedQueue} implementation that causes thread yields when data is not
 * successfully offered or polled.
 */
public class ConcurentYieldTillSuccessQueue<T> extends ConcurrentLinkedQueue<T> {

    @Override
    public T poll() {
        T item = null;
        while(!super.isEmpty() && (item = super.poll()) == null) {
            Thread.yield();;
        }
        return item;
    }

    @Override
    public boolean offer(T t) {
        while(!super.offer(t)) {
            Thread.yield();
        }
        return true;
    }
}
