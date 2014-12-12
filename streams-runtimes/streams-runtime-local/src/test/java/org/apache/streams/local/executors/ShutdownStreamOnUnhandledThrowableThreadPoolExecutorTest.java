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
import org.apache.streams.util.ComponentUtils;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class ShutdownStreamOnUnhandledThrowableThreadPoolExecutorTest {


    @After
    public void removeLocalMBeans() {
        try {
            ComponentUtils.removeAllMBeansOfDomain("org.apache.streams.local");
        } catch (Exception e) {
            //No op.  proceed to next test
        }
    }

    @Test
    public void testShutDownOnException() {
        LocalStreamBuilder sb = mock(LocalStreamBuilder.class);
        final AtomicBoolean isShutdown = new AtomicBoolean(false);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                isShutdown.set(true);
                return null;
            }
        }).when(sb).stop();

        final CountDownLatch latch = new CountDownLatch(1);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                latch.countDown();
                throw new RuntimeException("Testing Throwable Handling!");
            }
        };

        ExecutorService executor = new ShutdownStreamOnUnhandleThrowableThreadPoolExecutor(1, sb);
        executor.execute(runnable);
        try {
            latch.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        executor.shutdownNow();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        assertTrue("Expected StreamBuilder shutdown to be called", isShutdown.get());
    }


    @Test
    public void testNormalExecution() {
        LocalStreamBuilder sb = mock(LocalStreamBuilder.class);
        final AtomicBoolean isShutdown = new AtomicBoolean(false);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                isShutdown.set(true);
                return null;
            }
        }).when(sb).stop();

        final CountDownLatch latch = new CountDownLatch(1);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        };

        ExecutorService executor = new ShutdownStreamOnUnhandleThrowableThreadPoolExecutor(1, sb);
        executor.execute(runnable);
        try {
            latch.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        executor.shutdownNow();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        assertFalse("Expected StreamBuilder shutdown to be called", isShutdown.get());
    }


}
