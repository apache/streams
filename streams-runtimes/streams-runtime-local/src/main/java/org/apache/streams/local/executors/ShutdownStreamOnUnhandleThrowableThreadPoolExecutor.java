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

package org.apache.streams.local.executors;

import org.apache.streams.local.builders.LocalStreamBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A fixed ThreadPoolExecutor that will shutdown a stream upon a thread ending execution due to an unhandled throwable.
 * @see {@link java.util.concurrent.ThreadPoolExecutor}
 */
public class ShutdownStreamOnUnhandleThrowableThreadPoolExecutor extends ThreadPoolExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownStreamOnUnhandleThrowableThreadPoolExecutor.class);

  private LocalStreamBuilder streamBuilder;
  private volatile boolean isStoped;

  /**
   * Creates a fixed size thread pool where corePoolSize & maximumPoolSize equal numThreads with an unbounded queue.
   * @param numThreads number of threads in pool
   * @param streamBuilder streambuilder to call {@link org.apache.streams.core.StreamBuilder#stop()} on upon receiving an unhandled throwable
   */
  public ShutdownStreamOnUnhandleThrowableThreadPoolExecutor(int numThreads, LocalStreamBuilder streamBuilder) {
    super(numThreads, numThreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    this.streamBuilder = streamBuilder;
    this.isStoped = false;
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    if(t != null) {
      LOGGER.error("Runnable, {}, exited with an unhandled throwable! : {}", r.getClass(), t);
      LOGGER.error("Attempting to shut down stream.");
      synchronized (this) {
        if (!this.isStoped) {
          this.isStoped = true;
          this.streamBuilder.stop();
        }
      }
    } else {
      LOGGER.trace("Runnable, {}, finished executing.", r.getClass());
    }
  }
}
