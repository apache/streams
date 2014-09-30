package org.apache.streams.local.executors;

import org.apache.streams.local.builders.LocalStreamBuilder;
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
