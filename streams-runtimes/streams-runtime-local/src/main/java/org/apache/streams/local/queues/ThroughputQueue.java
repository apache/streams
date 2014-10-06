package org.apache.streams.local.queues;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A {@link java.util.concurrent.BlockingQueue} implementation that allows the measure measurement of how
 * data flows through the queue.  Is also a {@code MBean} so the flow statistics can be viewed through
 * JMX. Registration of the bean happens whenever a constructor receives a non-null id.
 *
 * !!! Warning !!!
 * Only the necessary methods for the local streams runtime are implemented.  All other methods throw a
 * {@link sun.reflect.generics.reflectiveObjects.NotImplementedException}.
 */
public class ThroughputQueue<E> implements BlockingQueue<E>, ThroughputQueueMXBean {

    public static final String NAME_TEMPLATE = "org.apache.streams.local:type=ThroughputQueue,name=%s";

    private static final Logger LOGGER = LoggerFactory.getLogger(ThroughputQueue.class);

    private BlockingQueue<ThroughputElement<E>> underlyingQueue;
    private ReadWriteLock putCountsLock;
    private ReadWriteLock takeCountsLock;
    @GuardedBy("putCountsLock")
    private long elementsAdded;
    @GuardedBy("takeCountsLock")
    private long elementsRemoved;
    @GuardedBy("this")
    private long startTime;
    @GuardedBy("takeCountsLock")
    private long totalQueueTime;
    @GuardedBy("takeCountsLock")
    private long maxQueuedTime;
    private volatile boolean active;

    /**
     * Creates an unbounded, unregistered {@code ThroughputQueue}
     */
    public ThroughputQueue() {
        this(-1, null);
    }

    /**
     * Creates a bounded, unregistered {@code ThroughputQueue}
     * @param maxSize maximum capacity of queue, if maxSize < 1 then unbounded
     */
    public ThroughputQueue(int maxSize) {
        this(maxSize, null);
    }

    /**
     * Creates an unbounded, registered {@code ThroughputQueue}
     * @param id unique id for this queue to be registered with. if id == NULL then not registered
     */
    public ThroughputQueue(String id) {
        this(-1, id);
    }

    /**
     * Creates a bounded, registered {@code ThroughputQueue}
     * @param maxSize maximum capacity of queue, if maxSize < 1 then unbounded
     * @param id unique id for this queue to be registered with. if id == NULL then not registered
     */
    public ThroughputQueue(int maxSize, String id) {
        if(maxSize < 1) {
            this.underlyingQueue = new LinkedBlockingQueue<>();
        } else {
            this.underlyingQueue = new LinkedBlockingQueue<>(maxSize);
        }
        this.elementsAdded = 0;
        this.elementsRemoved = 0;
        this.startTime = -1;
        this.putCountsLock = new ReentrantReadWriteLock();
        this.takeCountsLock = new ReentrantReadWriteLock();
        this.active = false;
        this.maxQueuedTime = -1;
        if(id != null) {
            try {
                ObjectName name = new ObjectName(String.format(NAME_TEMPLATE, id));
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.registerMBean(this, name);
            } catch (MalformedObjectNameException|InstanceAlreadyExistsException|MBeanRegistrationException|NotCompliantMBeanException e) {
                LOGGER.error("Failed to register MXBean : {}", e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean add(E e) {
        throw new NotImplementedException();
    }

    @Override
    public boolean offer(E e) {
        throw new NotImplementedException();
    }

    @Override
    public void put(E e) throws InterruptedException {
        this.underlyingQueue.put(new ThroughputElement<E>(e));
        try {
            this.putCountsLock.writeLock().lockInterruptibly();
            ++this.elementsAdded;
        } finally {
            this.putCountsLock.writeLock().unlock();
        }
        synchronized (this) {
            if (!this.active) {
                this.startTime = System.currentTimeMillis();
                this.active = true;
            }
        }

    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if(this.underlyingQueue.offer(new ThroughputElement<E>(e), timeout, unit)) {
            try {
                this.putCountsLock.writeLock().lockInterruptibly();
                ++this.elementsAdded;
            } finally {
                this.putCountsLock.writeLock().unlock();
            }
            synchronized (this) {
                if (!this.active) {
                    this.startTime = System.currentTimeMillis();
                    this.active = true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public E take() throws InterruptedException {
        ThroughputElement<E> e = this.underlyingQueue.take();
        try {
            this.takeCountsLock.writeLock().lockInterruptibly();
            ++this.elementsRemoved;
            Long queueTime = e.getWaited();
            this.totalQueueTime += queueTime;
            if(this.maxQueuedTime < queueTime) {
                this.maxQueuedTime = queueTime;
            }
        } finally {
            this.takeCountsLock.writeLock().unlock();
        }
        return e.getElement();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new NotImplementedException();
    }

    @Override
    public int remainingCapacity() {
        throw new NotImplementedException();
    }

    @Override
    public boolean remove(Object o) {
        throw new NotImplementedException();
    }

    @Override
    public boolean contains(Object o) {
        throw new NotImplementedException();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        throw new NotImplementedException();
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new NotImplementedException();
    }

    @Override
    public E remove() {
        throw new NotImplementedException();
    }

    @Override
    public E poll() {
        throw new NotImplementedException();
    }

    @Override
    public E element() {
        throw new NotImplementedException();
    }

    @Override
    public E peek() {
        throw new NotImplementedException();
    }

    @Override
    public int size() {
        return this.underlyingQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.underlyingQueue.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        throw new NotImplementedException();
    }

    @Override
    public Object[] toArray() {
        throw new NotImplementedException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new NotImplementedException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new NotImplementedException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new NotImplementedException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new NotImplementedException();
    }

    @Override
    public void clear() {
        throw new NotImplementedException();
    }

    @Override
    public long getCurrentSize() {
        long size = -1;
        try {
            this.putCountsLock.readLock().lock();
            try {
                this.takeCountsLock.readLock().lock();
                size = this.elementsAdded - this.elementsRemoved;
            } finally {
                this.takeCountsLock.readLock().unlock();
            }
        } finally {
            this.putCountsLock.readLock().unlock();
        }
        return size;
    }

    @Override
    public double getAvgWait() {
        double avg = -1.0;
        try {
            this.takeCountsLock.readLock().lock();
            avg = (double) this.totalQueueTime / (double) this.elementsRemoved;
        } finally {
            this.takeCountsLock.readLock().unlock();
        }
        return avg;
    }

    @Override
    public long getMaxWait() {
        ThroughputElement<E> e = this.underlyingQueue.peek();
        long max = -1;
        try {
            this.takeCountsLock.readLock().lock();
            if (e != null && e.getWaited() > this.maxQueuedTime) {
                max = e.getWaited();
            } else {
                max = this.maxQueuedTime;
            }
        } finally {
            this.takeCountsLock.readLock().unlock();
        }
        return max;
    }

    @Override
    public long getRemoved() {
        long num = -1;
        try {
            this.takeCountsLock.readLock().lock();
            num = this.elementsRemoved;
        } finally {
            this.takeCountsLock.readLock().unlock();
        }
        return num;
    }

    @Override
    public long getAdded() {
        long num = -1;
        try {
            this.putCountsLock.readLock().lock();
            num = this.elementsAdded;
        } finally {
            this.putCountsLock.readLock().unlock();
        }
        return num;
    }

    @Override
    public double getThroughput() {
        double tp = -1.0;
        synchronized (this) {
            try {
                this.takeCountsLock.readLock().lock();
                tp = this.elementsRemoved / ((System.currentTimeMillis() - this.startTime) / 1000.0);
            } finally {
                this.takeCountsLock.readLock().unlock();
            }
        }
        return tp;
    }


    /**
     * Element wrapper to measure time waiting on the queue
     * @param <E>
     */
    private class ThroughputElement<E> {
        
        private long queuedTime;
        private E element;

        protected ThroughputElement(E element) {
            this.element = element;
            this.queuedTime = System.currentTimeMillis();
        }

        /**
         * Get the time this element has been waiting on the queue.
         * current time - time element was queued
         * @return time this element has been waiting on the queue in milliseconds
         */
        public long getWaited() {
            return System.currentTimeMillis() - this.queuedTime;
        }

        /**
         * Get the queued element
         * @return the element
         */
        public E getElement() {
            return this.element;
        }
    }
}
