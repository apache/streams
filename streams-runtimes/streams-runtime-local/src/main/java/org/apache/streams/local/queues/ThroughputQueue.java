/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.streams.local.queues;

import org.apache.streams.local.builders.LocalStreamBuilder;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * A {@link java.util.concurrent.BlockingQueue} implementation that allows the measure measurement of how
 * data flows through the queue.  Is also a {@code MBean} so the flow statistics can be viewed through
 * JMX. Registration of the bean happens whenever a constructor receives a non-null id.
 * <p></p>
 * !!! Warning !!!
 * Only the necessary methods for the local streams runtime are implemented.  All other methods throw a
 * {@link org.apache.commons.lang.NotImplementedException}.
 */
public class ThroughputQueue<E> implements BlockingQueue<E>, ThroughputQueueMXBean {

  public static final String NAME_TEMPLATE = "org.apache.streams.local:type=ThroughputQueue,name=%s,identifier=%s,startedAt=%s";

  private static final Logger LOGGER = LoggerFactory.getLogger(ThroughputQueue.class);

  private BlockingQueue<ThroughputElement<E>> underlyingQueue;
  private AtomicLong elementsAdded;
  private AtomicLong elementsRemoved;
  private AtomicLong startTime;
  private AtomicLong totalQueueTime;
  private long maxQueuedTime;
  private volatile boolean active;
  private ReadWriteLock maxQueueTimeLock;

  /**
   * Creates an unbounded, unregistered {@code ThroughputQueue}
   */
  public ThroughputQueue() {
    this(-1, null, LocalStreamBuilder.DEFAULT_STREAM_IDENTIFIER, -1);
  }

  /**
   *
   * @param streamIdentifier
   * @param startedAt
   */
  public ThroughputQueue(String streamIdentifier, long startedAt) {
    this(-1, null, streamIdentifier, startedAt);
  }

  /**
   * Creates a bounded, unregistered {@code ThroughputQueue}
   *
   * @param maxSize maximum capacity of queue, if maxSize < 1 then unbounded
   */
  public ThroughputQueue(int maxSize) {
    this(maxSize, null, LocalStreamBuilder.DEFAULT_STREAM_IDENTIFIER, -1);
  }

  /**
   *
   * @param maxSize
   * @param streamIdentifier
   * @param startedAt
   */
  public ThroughputQueue(int maxSize, String streamIdentifier, long startedAt) {
    this(maxSize, null, streamIdentifier, startedAt);
  }

  /**
   * Creates an unbounded, registered {@code ThroughputQueue}
   *
   * @param id unique id for this queue to be registered with. if id == NULL then not registered
   */
  public ThroughputQueue(String id) {
    this(-1, id, LocalStreamBuilder.DEFAULT_STREAM_IDENTIFIER, -1);
  }

  /**
   *
   * @param id
   * @param streamIdentifier
   * @param startedAt
   */
  public ThroughputQueue(String id, String streamIdentifier, long startedAt) {
    this(-1, id, streamIdentifier, startedAt);
  }

  /**
   *
   * @param maxSize
   * @param id
   */
  public ThroughputQueue(int maxSize, String id) {
    this(maxSize, id, LocalStreamBuilder.DEFAULT_STREAM_IDENTIFIER, -1);

  }

  /**
   * Creates a bounded, registered {@code ThroughputQueue}
   *
   * @param maxSize maximum capacity of queue, if maxSize < 1 then unbounded
   * @param id      unique id for this queue to be registered with. if id == NULL then not registered
   */
  public ThroughputQueue(int maxSize, String id, String streamIdentifier, long startedAt) {
    if (maxSize < 1) {
      this.underlyingQueue = new LinkedBlockingQueue<>();
    } else {
      this.underlyingQueue = new LinkedBlockingQueue<>(maxSize);
    }
    this.elementsAdded = new AtomicLong(0);
    this.elementsRemoved = new AtomicLong(0);
    this.startTime = new AtomicLong(-1);
    this.active = false;
    this.maxQueuedTime = 0;
    this.maxQueueTimeLock = new ReentrantReadWriteLock();
    this.totalQueueTime = new AtomicLong(0);
    if (id != null) {
      try {
        ObjectName name = new ObjectName(String.format(NAME_TEMPLATE, id, streamIdentifier, startedAt));
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(this, name);
      } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
        LOGGER.error("Failed to register MXBean : {}", e);
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public boolean add(E e) {
    if (this.underlyingQueue.add(new ThroughputElement<E>(e))) {
      internalAddElement();
      return true;
    }
    return false;
  }

  @Override
  public boolean offer(E e) {
    if (this.underlyingQueue.offer(new ThroughputElement<E>(e))) {
      internalAddElement();
      return true;
    }
    return false;
  }

  @Override
  public void put(E e) throws InterruptedException {
    this.underlyingQueue.put(new ThroughputElement<E>(e));
    internalAddElement();
  }

  @Override
  public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
    if (this.underlyingQueue.offer(new ThroughputElement<E>(e), timeout, unit)) {
      internalAddElement();
      return true;
    }
    return false;
  }

  @Override
  public E take() throws InterruptedException {
    ThroughputElement<E> e = this.underlyingQueue.take();
    internalRemoveElement(e);
    return e.getElement();
  }

  @Override
  public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    ThroughputElement<E> e = this.underlyingQueue.poll(timeout, unit);
    if(e != null) {
      internalRemoveElement(e);
      return e.getElement();
    }
    return null;
  }

  @Override
  public int remainingCapacity() {
    return this.underlyingQueue.remainingCapacity();
  }

  @Override
  public boolean remove(Object o) {
    try {
      return this.underlyingQueue.remove(new ThroughputElement<E>((E) o));
    } catch (ClassCastException cce) {
      return false;
    }
  }

  @Override
  public boolean contains(Object o) {
    try {
      return this.underlyingQueue.contains(new ThroughputElement<E>((E) o));
    } catch (ClassCastException cce) {
      return false;
    }
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
    ThroughputElement<E> e = this.underlyingQueue.remove();
    if(e != null) {
      internalRemoveElement(e);
      return e.getElement();
    }
    return null;
  }

  @Override
  public E poll() {
    ThroughputElement<E> e = this.underlyingQueue.poll();
    if(e != null) {
      internalRemoveElement(e);
      return e.getElement();
    }
    return null;
  }

  @Override
  public E element() {
    throw new NotImplementedException();
  }

  @Override
  public E peek() {
    ThroughputElement<E> e = this.underlyingQueue.peek();
    if( e != null) {
      return e.getElement();
    }
    return null;
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
    return this.elementsAdded.get() - this.elementsRemoved.get();
  }

  /**
   * If elements have been removed from the queue or no elements have been added, it returns the average wait time
   * in milliseconds. If elements have been added, but none have been removed, it returns the time waited by the first
   * element in the queue.
   *
   * @return the average wait time in milliseconds
   */
  @Override
  public double getAvgWait() {
    if (this.elementsRemoved.get() == 0) {
      if (this.getCurrentSize() > 0) {
        return this.underlyingQueue.peek().getWaited();
      } else {
        return 0.0;
      }
    } else {
      return (double) this.totalQueueTime.get() / (double) this.elementsRemoved.get();
    }
  }

  @Override
  public long getMaxWait() {
    ThroughputElement<E> e = this.underlyingQueue.peek();
    long max = -1;
    try {
      this.maxQueueTimeLock.readLock().lock();
      if (e != null && e.getWaited() > this.maxQueuedTime) {
        max = e.getWaited();
      } else {
        max = this.maxQueuedTime;
      }
    } finally {
      this.maxQueueTimeLock.readLock().unlock();
    }
    return max;
  }

  @Override
  public long getRemoved() {
    return this.elementsRemoved.get();
  }

  @Override
  public long getAdded() {
    return this.elementsAdded.get();
  }

  @Override
  public double getThroughput() {
    if (active) {
      return this.elementsRemoved.get() / ((System.currentTimeMillis() - this.startTime.get()) / 1000.0);
    }
    return 0.0;
  }

  /**
   * Handles updating the stats whenever elements are added to the queue
   */
  private void internalAddElement() {
    this.elementsAdded.incrementAndGet();
    synchronized (this) {
      if (!this.active) {
        this.startTime.set(System.currentTimeMillis());
        this.active = true;
      }
    }
  }

  /**
   * Handle updating the stats whenever elements are removed from the queue
   * @param e Element removed
   */
  private void internalRemoveElement(ThroughputElement<E> e) {
    if(e != null) {
      this.elementsRemoved.incrementAndGet();
      Long queueTime = e.getWaited();
      this.totalQueueTime.addAndGet(queueTime);
      boolean unlocked = false;
      try {
        this.maxQueueTimeLock.readLock().lock();
        if (this.maxQueuedTime < queueTime) {
          this.maxQueueTimeLock.readLock().unlock();
          unlocked = true;
          try {
            this.maxQueueTimeLock.writeLock().lock();
            this.maxQueuedTime = queueTime;
          } finally {
            this.maxQueueTimeLock.writeLock().unlock();
          }
        }
      } finally {
        if (!unlocked)
          this.maxQueueTimeLock.readLock().unlock();
      }
    }
  }


  /**
   * Element wrapper to measure time waiting on the queue
   *
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
     *
     * @return time this element has been waiting on the queue in milliseconds
     */
    public long getWaited() {
      return System.currentTimeMillis() - this.queuedTime;
    }

    /**
     * Get the queued element
     *
     * @return the element
     */
    public E getElement() {
      return this.element;
    }


    /**
     * Measures equality by the element and ignores the queued time
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
      if(obj instanceof ThroughputElement && obj != null) {
        ThroughputElement that = (ThroughputElement) obj;
        if(that.getElement() == null && this.getElement() == null) {
          return true;
        } else if(that.getElement() != null) {
          return that.getElement().equals(this.getElement());
        } else {
          return false;
        }
      }
      return false;
    }
  }
}
