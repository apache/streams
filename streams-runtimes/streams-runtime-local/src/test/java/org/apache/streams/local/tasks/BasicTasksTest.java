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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.counters.DatumStatusCounter;
import org.apache.streams.local.counters.StreamsTaskCounter;
import org.apache.streams.local.queues.ThroughputQueue;
import org.apache.streams.local.test.processors.PassthroughDatumCounterProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.apache.streams.util.ComponentUtils;
import org.junit.After;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Queue;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 *
 */
public class BasicTasksTest {


    private static final String MBEAN_ID = "test_bean";
    @After
    public void removeLocalMBeans() {
        try {
            ComponentUtils.removeAllMBeansOfDomain("org.apache.streams.local");
        } catch (Exception e) {
            //No op.  proceed to next test
        }
    }

    @Test
    public void testProviderTask() {
        int numMessages = 100;
        NumericMessageProvider provider = new NumericMessageProvider(numMessages);
        StreamsProviderTask task = new StreamsProviderTask(provider, false);
        BlockingQueue<StreamsDatum> outQueue = new LinkedBlockingQueue<>();
        task.addOutputQueue(outQueue);
        //Test that adding input queues to providers is not valid
        BlockingQueue<StreamsDatum> inQueue = createInputQueue(numMessages);
        Exception exp = null;
        try {
            task.addInputQueue(inQueue);
        } catch (UnsupportedOperationException uoe) {
            exp = uoe;
        }
        assertNotNull(exp);

        ExecutorService service = Executors.newFixedThreadPool(1);
        service.submit(task);
        int attempts = 0;
        while(outQueue.size() != numMessages) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ++attempts;
            if(attempts == 10) {
                fail("Provider task failed to output "+numMessages+" in a timely fashion.");
            }
        }
        service.shutdown();
        try {
            if(!service.awaitTermination(5, TimeUnit.SECONDS)){
                service.shutdownNow();
                fail("Service did not terminate.");
            }
            assertTrue("Task should have completed running in aloted time.", service.isTerminated());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testProcessorTask() {
        int numMessages = 100;
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor("");
        StreamsProcessorTask task = new StreamsProcessorTask(processor);
        StreamsTaskCounter counter = new StreamsTaskCounter(MBEAN_ID);
        task.setStreamsTaskCounter(counter);
        BlockingQueue<StreamsDatum> outQueue = new LinkedBlockingQueue<>();
        BlockingQueue<StreamsDatum> inQueue = createInputQueue(numMessages);
        task.addOutputQueue(outQueue);
        task.addInputQueue(inQueue);
        assertEquals(numMessages, task.getInputQueues().get(0).size());
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.submit(task);
        int attempts = 0;
        while(inQueue.size() != 0 && outQueue.size() != numMessages) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //Ignore
            }
            ++attempts;
            if(attempts == 10) {
                fail("Processor task failed to output "+numMessages+" in a timely fashion.");
            }
        }
        task.stopTask();;
        service.shutdown();
        try {
            if(!service.awaitTermination(5, TimeUnit.SECONDS)){
                service.shutdownNow();
                fail("Service did not terminate.");
            }
            assertTrue("Task should have completed running in aloted time.", service.isTerminated());
        } catch (InterruptedException e) {
            fail("Test Interupted.");
        }
        assertEquals(numMessages, processor.getMessageCount());
        assertEquals(numMessages, counter.getNumReceived());
        assertEquals(numMessages, counter.getNumEmitted());
        assertEquals(0, counter.getNumUnhandledErrors());
        assertEquals(0.0, counter.getErrorRate(), 0.0);
    }

    @Test
    public void testWriterTask() {
        int numMessages = 100;
        DatumCounterWriter writer = new DatumCounterWriter("");
        StreamsPersistWriterTask task = new StreamsPersistWriterTask(writer);
        StreamsTaskCounter counter = new StreamsTaskCounter(MBEAN_ID);
        task.setStreamsTaskCounter(counter);
        BlockingQueue<StreamsDatum> outQueue = new LinkedBlockingQueue<>();
        BlockingQueue<StreamsDatum> inQueue = createInputQueue(numMessages);

        Exception exp = null;
        try {
            task.addOutputQueue(outQueue);
        } catch (UnsupportedOperationException uoe) {
            exp = uoe;
        }
        assertNotNull(exp);
        task.addInputQueue(inQueue);
        assertEquals(numMessages, task.getInputQueues().get(0).size());
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.submit(task);
        int attempts = 0;
        while(inQueue.size() != 0 ) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //Ignore
            }
            ++attempts;
            if(attempts == 10) {
                fail("Processor task failed to output "+numMessages+" in a timely fashion.");
            }
        }
        task.stopTask();
        service.shutdown();
        try {
            if(!service.awaitTermination(5, TimeUnit.SECONDS)){
                service.shutdownNow();
                fail("Service did not terminate.");
            }
            assertTrue("Task should have completed running in aloted time.", service.isTerminated());
        } catch (InterruptedException e) {
            fail("Test Interupted.");
        }
        assertEquals(numMessages, writer.getDatumsCounted());
        assertEquals(numMessages, counter.getNumReceived());
        assertEquals(0, counter.getNumEmitted());
        assertEquals(0, counter.getNumUnhandledErrors());
        assertEquals(0.0, counter.getErrorRate(), 0.0);
    }

    @Test
    public void testMergeTask() {
        int numMessages = 100;
        int incoming = 5;
        StreamsMergeTask task = new StreamsMergeTask();
        BlockingQueue<StreamsDatum> outQueue = new LinkedBlockingQueue<>();
        task.addOutputQueue(outQueue);
        for(int i=0; i < incoming; ++i) {
            task.addInputQueue(createInputQueue(numMessages));
        }
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.submit(task);
        int attempts = 0;
        while(outQueue.size() != incoming * numMessages ) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //Ignore
            }
            ++attempts;
            if(attempts == 10) {
                assertEquals("Processor task failed to output " + (numMessages * incoming) + " in a timely fashion.", (numMessages * incoming), outQueue.size());
            }
        }
        task.stopTask();
        service.shutdown();
        try {
            if(!service.awaitTermination(5, TimeUnit.SECONDS)){
                service.shutdownNow();
                fail("Service did not terminate.");
            }
            assertTrue("Task should have completed running in aloted time.", service.isTerminated());
        } catch (InterruptedException e) {
            fail("Test Interupted.");
        }
    }

    @Test
    public void testBranching() {
        int numMessages = 100;
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor("");
        StreamsProcessorTask task = new StreamsProcessorTask(processor);
        BlockingQueue<StreamsDatum> outQueue1 = new LinkedBlockingQueue<>();
        BlockingQueue<StreamsDatum> outQueue2 = new LinkedBlockingQueue<>();
        BlockingQueue<StreamsDatum> inQueue = createInputQueue(numMessages);
        task.addOutputQueue(outQueue1);
        task.addOutputQueue(outQueue2);
        task.addInputQueue(inQueue);
        assertEquals(numMessages, task.getInputQueues().get(0).size());
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.submit(task);
        int attempts = 0;
        while(inQueue.size() != 0 ) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //Ignore
            }
            ++attempts;
            if(attempts == 10) {
                assertEquals("Processor task failed to output "+(numMessages)+" in a timely fashion.", 0, inQueue.size());
            }
        }
        task.stopTask();

        service.shutdown();
        try {
            if(!service.awaitTermination(5, TimeUnit.SECONDS)){
                service.shutdownNow();
                fail("Service did not terminate.");
            }
            assertTrue("Task should have completed running in aloted time.", service.isTerminated());
        } catch (InterruptedException e) {
            fail("Test Interupted.");
        }
        assertEquals(numMessages, processor.getMessageCount());
        assertEquals(numMessages, outQueue1.size());
        assertEquals(numMessages, outQueue2.size());
    }

    @Test
    public void testBranchingSerialization() {
        int numMessages = 1;
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor("");
        StreamsProcessorTask task = new StreamsProcessorTask(processor);
        BlockingQueue<StreamsDatum> outQueue1 = new LinkedBlockingQueue<>();
        BlockingQueue<StreamsDatum> outQueue2 = new LinkedBlockingQueue<>();
        BlockingQueue<StreamsDatum> inQueue = createInputQueue(numMessages);
        task.addOutputQueue(outQueue1);
        task.addOutputQueue(outQueue2);
        task.addInputQueue(inQueue);
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.submit(task);
        int attempts = 0;
        while(inQueue.size() != 0 ) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //Ignore
            }
            ++attempts;
            if(attempts == 10) {
                assertEquals("Processor task failed to output "+(numMessages)+" in a timely fashion.", 0, inQueue.size());
            }
        }
        task.stopTask();

        service.shutdown();
        try {
            if(!service.awaitTermination(5, TimeUnit.SECONDS)){
                service.shutdownNow();
                fail("Service did not terminate.");
            }
            assertTrue("Task should have completed running in aloted time.", service.isTerminated());
        } catch (InterruptedException e) {
            fail("Test Interupted.");
        }
        assertEquals(numMessages, processor.getMessageCount());
        assertEquals(numMessages, outQueue1.size());
        assertEquals(numMessages, outQueue2.size());
        StreamsDatum datum1 = outQueue1.poll();
        StreamsDatum datum2 = outQueue2.poll();
        assertNotNull(datum1);
        assertEquals(datum1, datum2);
        datum1.setDocument("a");
        assertNotEquals(datum1, datum2);
    }

    private BlockingQueue<StreamsDatum> createInputQueue(int numDatums) {
        BlockingQueue<StreamsDatum> queue = new LinkedBlockingQueue<>();
        for(int i=0; i < numDatums; ++i) {
            queue.add(new StreamsDatum(i));
        }
        return queue;
    }


}
