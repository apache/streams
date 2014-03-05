package org.apache.streams.core.tasks;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.test.processors.PassthroughDatumCounterProcessor;
import org.apache.streams.core.test.providers.NumericMessageProvider;
import static org.junit.Assert.*;

import org.apache.streams.core.test.writer.DatumCounterWriter;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by rebanks on 2/18/14.
 */
public class BasicTasksTest {



    @Test
    public void testProviderTask() {
        int numMessages = 100;
        NumericMessageProvider provider = new NumericMessageProvider(numMessages);
        StreamsProviderTask task = new StreamsProviderTask(provider, false);
        Queue<StreamsDatum> outQueue = new ConcurrentLinkedQueue<StreamsDatum>();
        task.addOutputQueue(outQueue);
        Queue<StreamsDatum> inQueue = createInputQueue(numMessages);
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
                //Ignore
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
            fail("Test Interupted.");
        };
    }

    @Test
    public void testProcessorTask() {
        int numMessages = 100;
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor();
        StreamsProcessorTask task = new StreamsProcessorTask(processor);
        Queue<StreamsDatum> outQueue = new ConcurrentLinkedQueue<StreamsDatum>();
        Queue<StreamsDatum> inQueue = createInputQueue(numMessages);
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
        task.stopTask();
        assertEquals(numMessages, processor.getMessageCount());
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
    public void testWriterTask() {
        int numMessages = 100;
        DatumCounterWriter writer = new DatumCounterWriter();
        StreamsPersistWriterTask task = new StreamsPersistWriterTask(writer);
        Queue<StreamsDatum> outQueue = new ConcurrentLinkedQueue<StreamsDatum>();
        Queue<StreamsDatum> inQueue = createInputQueue(numMessages);

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
        assertEquals(numMessages, writer.getDatumsCounted());
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
    public void testMergeTask() {
        int numMessages = 100;
        int incoming = 5;
        StreamsMergeTask task = new StreamsMergeTask();
        Queue<StreamsDatum> outQueue = new ConcurrentLinkedQueue<StreamsDatum>();
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
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor();
        StreamsProcessorTask task = new StreamsProcessorTask(processor);
        Queue<StreamsDatum> outQueue1 = new ConcurrentLinkedQueue<StreamsDatum>();
        Queue<StreamsDatum> outQueue2 = new ConcurrentLinkedQueue<StreamsDatum>();
        Queue<StreamsDatum> inQueue = createInputQueue(numMessages);
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
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor();
        StreamsProcessorTask task = new StreamsProcessorTask(processor);
        Queue<StreamsDatum> outQueue1 = new ConcurrentLinkedQueue<StreamsDatum>();
        Queue<StreamsDatum> outQueue2 = new ConcurrentLinkedQueue<StreamsDatum>();
        Queue<StreamsDatum> inQueue = createInputQueue(numMessages);
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

    private Queue<StreamsDatum> createInputQueue(int numDatums) {
        Queue<StreamsDatum> queue = new ConcurrentLinkedQueue<StreamsDatum>();
        for(int i=0; i < numDatums; ++i) {
            queue.add(new StreamsDatum(i));
        }
        return queue;
    }


}
