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

package org.apache.streams.local.tasks;

import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.DatumStatusCountable;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.util.DatumUtils;
import org.apache.streams.local.counters.StreamsTaskCounter;

import com.google.common.util.concurrent.Uninterruptibles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsPersistWriterTask extends BaseStreamsTask implements DatumStatusCountable {

  private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPersistWriterTask.class);

  private StreamsPersistWriter writer;
  private AtomicBoolean keepRunning;
  private StreamsConfiguration streamConfig;
  private BlockingQueue<StreamsDatum> inQueue;
  private AtomicBoolean isRunning;
  private AtomicBoolean blocked;
  private StreamsTaskCounter counter;

  private DatumStatusCounter statusCounter = new DatumStatusCounter();

  @Override
  public DatumStatusCounter getDatumStatusCounter() {
    return this.statusCounter;
  }


  /**
   * Default constructor.  Uses default sleep of 500ms when inbound queue is empty.
   * @param writer writer to execute in task
   */
  public StreamsPersistWriterTask(StreamsPersistWriter writer) {
    this(writer, null);
  }

  /**
   *
   * @param writer writer to execute in task
   * @param streamConfig stream config
   */
  public StreamsPersistWriterTask(StreamsPersistWriter writer, StreamsConfiguration streamConfig) {
    super(streamConfig);
    this.streamConfig = super.streamConfig;
    this.writer = writer;
    this.keepRunning = new AtomicBoolean(true);
    this.isRunning = new AtomicBoolean(true);
    this.blocked = new AtomicBoolean(false);
  }

  @Override
  public boolean isWaiting() {
    return this.inQueue.isEmpty() && this.blocked.get();
  }

  @Override
  public void setStreamConfig(StreamsConfiguration config) {
    this.streamConfig = config;
  }

  @Override
  public void addInputQueue(BlockingQueue<StreamsDatum> inputQueue) {
    this.inQueue = inputQueue;
  }

  @Override
  public boolean isRunning() {
    return this.isRunning.get();
  }

  @Override
  public void run() {
    try {
      this.writer.prepare(this.streamConfig);
      if(this.counter == null) {
        this.counter = new StreamsTaskCounter(this.writer.getClass().getName()+ UUID.randomUUID().toString(), getStreamIdentifier(), getStartedAt());
      }
      while(this.keepRunning.get()) {
        StreamsDatum datum = null;
        try {
          this.blocked.set(true);
          datum = this.inQueue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
          LOGGER.debug("Received InterruptedException. Shutting down and re-applying interrupt status.");
          this.keepRunning.set(false);
          if(!this.inQueue.isEmpty()) {
            LOGGER.error("Received InteruptedException and input queue still has data, count={}, processor={}",this.inQueue.size(), this.writer.getClass().getName());
          }
          Thread.currentThread().interrupt();
        } finally {
          this.blocked.set(false);
        }
        if(datum != null) {
          this.counter.incrementReceivedCount();
          try {
            long startTime = System.currentTimeMillis();
            this.writer.write(datum);
            this.counter.addTime(System.currentTimeMillis() - startTime);
            statusCounter.incrementStatus(DatumStatus.SUCCESS);
          } catch (Exception e) {
            LOGGER.error("Error writing to persist writer {}", this.writer.getClass().getSimpleName(), e);
            this.keepRunning.set(false); // why do we shutdown on a failed write ?
            statusCounter.incrementStatus(DatumStatus.FAIL);
            DatumUtils.addErrorToMetadata(datum, e, this.writer.getClass());
            this.counter.incrementErrorCount();
          }
        } else { //datums should never be null
          LOGGER.trace("Received null StreamsDatum @ writer : {}", this.writer.getClass().getName());
        }
      }
      Uninterruptibles.sleepUninterruptibly(streamConfig.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
    } catch(Throwable e) {
      LOGGER.error("Caught Throwable in Persist Writer {} : {}", this.writer.getClass().getSimpleName(), e);
    } finally {
      Uninterruptibles.sleepUninterruptibly(streamConfig.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
      this.writer.cleanUp();
      this.isRunning.set(false);
    }
  }

  @Override
  public void stopTask() {
    this.keepRunning.set(false);
  }


  @Override
  public void addOutputQueue(BlockingQueue<StreamsDatum> outputQueue) {
    throw new UnsupportedOperationException(this.getClass().getName()+" does not support method - setOutputQueue()");
  }

  @Override
  public List<BlockingQueue<StreamsDatum>> getInputQueues() {
    List<BlockingQueue<StreamsDatum>> queues = new LinkedList<>();
    queues.add(this.inQueue);
    return queues;
  }

  @Override
  public void setStreamsTaskCounter(StreamsTaskCounter counter) {
    this.counter = counter;
  }
}
