/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.util.ComponentUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Tests the StreamsProviderTask.
 */
public class StreamsProviderTaskTest {

    protected StreamsProvider mockProvider;
    protected ExecutorService pool;

    @Before
    public void setup() {
        mockProvider = mock(StreamsProvider.class);
        pool = Executors.newFixedThreadPool(1);
    }

    @Test
    public void runPerpetual() {
        StreamsProviderTask task = new StreamsProviderTask(mockProvider, true);
        when(mockProvider.isRunning()).thenReturn(true);
        when(mockProvider.readCurrent()).thenReturn(new StreamsResultSet(new LinkedBlockingQueue<StreamsDatum>()));
        task.setTimeout(500);
        task.setSleepTime(10);
        task.run();
        //Setting this to at least 2 means that it was correctly set to perpetual mode
        verify(mockProvider, atLeast(2)).readCurrent();
        verify(mockProvider, atMost(1)).prepare(null);
    }

    @Test
    public void flushes() {
        Queue<StreamsDatum> out = new LinkedBlockingQueue<>();
        StreamsProviderTask task = new StreamsProviderTask(mockProvider, true);
        when(mockProvider.isRunning()).thenReturn(true);
        when(mockProvider.readCurrent()).thenReturn(new StreamsResultSet(getQueue(3)));
        task.setTimeout(100);
        task.setSleepTime(10);
        task.getOutputQueues().add(out);
        task.run();
        assertThat(out.size(), is(equalTo(3)));
    }

    protected Queue<StreamsDatum> getQueue(int numElems) {
        Queue<StreamsDatum> results = new LinkedBlockingQueue<>();
        for(int i=0; i<numElems; i++) {
            results.add(new StreamsDatum(Math.random()));
        }
        return results;
    }

    @Test
    public void runNonPerpetual() {
        StreamsProviderTask task = new StreamsProviderTask(mockProvider, false);
        when(mockProvider.isRunning()).thenReturn(true);
        when(mockProvider.readCurrent()).thenReturn(new StreamsResultSet(new LinkedBlockingQueue<StreamsDatum>()));
        task.setTimeout(500);
        task.setSleepTime(10);
        task.run();
        //In read current mode, this should only be called 1 time
        verify(mockProvider, atLeast(1)).readCurrent();
        verify(mockProvider, atMost(1)).prepare(null);
    }

    @Test
    public void stoppable() throws InterruptedException {
        StreamsProviderTask task = new StreamsProviderTask(mockProvider, true);
        when(mockProvider.isRunning()).thenReturn(true);
        when(mockProvider.readCurrent()).thenReturn(new StreamsResultSet(new LinkedBlockingQueue<StreamsDatum>()));
        task.setTimeout(-1);
        task.setSleepTime(10);
        Future<?> taskResult = pool.submit(task);

        //After a few milliseconds, tell the task that it is to stop and wait until it says it isn't or a timeout happens
        int count = 0;
        do {
            Thread.sleep(100);
            if(count == 0) {
                task.stopTask();
            }
        } while(++count < 10 && !taskResult.isDone());
        verifyNotRunning(task, taskResult);

    }

    @Test
    public void earlyException() throws InterruptedException {
        StreamsProviderTask task = new StreamsProviderTask(mockProvider, true);
        when(mockProvider.isRunning()).thenReturn(true);
        doThrow(new RuntimeException()).when(mockProvider).prepare(null);
        task.setTimeout(-1);
        task.setSleepTime(10);
        Future<?> taskResult = pool.submit(task);
        int count = 0;
        while(++count < 10 && !taskResult.isDone()) {
            Thread.sleep(100);
        }
        verifyNotRunning(task, taskResult);
    }

    protected void verifyNotRunning(StreamsProviderTask task, Future<?> taskResult) {
        //Make sure the task is reporting that it is complete and that the run method returned
        if(taskResult.isDone()) {
            assertThat(task.isRunning(), is(false));
        } else {
            ComponentUtils.shutdownExecutor(pool, 0, 10);
            fail();
        }
    }
}
