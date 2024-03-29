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

package org.apache.streams.local.builders;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.local.LocalRuntimeConfiguration;
import org.apache.streams.local.counters.StreamsTaskCounter;
import org.apache.streams.local.queues.ThroughputQueue;
import org.apache.streams.local.test.processors.PassthroughDatumCounterProcessor;
import org.apache.streams.local.test.processors.SlowProcessor;
import org.apache.streams.local.test.providers.EmptyResultSetProvider;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.apache.streams.local.test.writer.SystemOutWriter;
import org.apache.streams.util.ComponentUtils;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.google.common.util.concurrent.Uninterruptibles;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Basic Tests for the LocalStreamBuilder.
 *
 * Test are performed by redirecting system out and counting the number of lines that the SystemOutWriter prints
 * to System.out.  The SystemOutWriter also prints one line when cleanUp() is called, so this is why it tests for
 * the numDatums +1.
 *
 *
 */
public class LocalStreamBuilderTest extends RandomizedTest {
  private static final String MBEAN_ID = "test_id";
  private static final String STREAM_ID = "test_stream";
  private static long STREAM_START_TIME = (new DateTime()).getMillis();

  @After
  public void removeLocalMBeans() {
    try {
      ComponentUtils.removeAllMBeansOfDomain("org.apache.streams.local");
    } catch (Exception e) {
      //No op.  proceed to next test
    }
  }


  public void removeRegisteredMBeans(String... ids) {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    for(String id : ids) {
      try {
        mbs.unregisterMBean(new ObjectName(String.format(ThroughputQueue.NAME_TEMPLATE, id, STREAM_ID, STREAM_START_TIME)));
      } catch (MalformedObjectNameException|InstanceNotFoundException|MBeanRegistrationException e) {
        //No-op
      }
      try {
        mbs.unregisterMBean(new ObjectName((String.format(StreamsTaskCounter.NAME_TEMPLATE, id, STREAM_ID, STREAM_START_TIME))));
      } catch (MalformedObjectNameException|InstanceNotFoundException|MBeanRegistrationException e) {
        //No-op
      }
    }
  }




  @Test
  public void testStreamIdValidations() {
    StreamBuilder builder = new LocalStreamBuilder();
    builder.newReadCurrentStream("id", new NumericMessageProvider(1));
    Exception exp = null;
    try {
      builder.newReadCurrentStream("id", new NumericMessageProvider(1));
    } catch (RuntimeException e) {
      exp = e;
    }
    Assert.assertNotNull(exp);
    exp = null;
    builder.addStreamsProcessor("1", new PassthroughDatumCounterProcessor("1"), 1, "id");
    try {
      builder.addStreamsProcessor("2", new PassthroughDatumCounterProcessor("2"), 1, "id", "id2");
    } catch (RuntimeException e) {
      exp = e;
    }
    Assert.assertNotNull(exp);
    removeRegisteredMBeans("1", "2", "id");
  }

  @Test
  public void testBasicLinearStream1()  {
    linearStreamNonParallel(1, 1);
  }

  @Test
  public void testBasicLinearStream2()  {
    linearStreamNonParallel(1004, 1);
  }

  @Test
  public void testBasicLinearStream3()  {
    linearStreamNonParallel(1, 10);
  }

  @Test
  @Repeat(iterations = 3)
  public void testBasicLinearStreamRandom()  {
    int numDatums = randomIntBetween(1, 100000);
    int numProcessors = randomIntBetween(1, 10);
    linearStreamNonParallel(numDatums, numProcessors);
  }

  /**
   * Tests that all datums pass through each processor and that all datums reach the writer
   * @param numDatums
   * @param numProcessors
   */
  private void linearStreamNonParallel(int numDatums, int numProcessors) {
    String processorId = "proc";
    try {
      LocalRuntimeConfiguration conf = new ComponentConfigurator<>(LocalRuntimeConfiguration.class).detectConfiguration();
      StreamBuilder builder = new LocalStreamBuilder(conf.withMaxQueueCapacity(10l));
      builder.newPerpetualStream("numeric_provider", new NumericMessageProvider(numDatums));
      String connectTo;
      for(int i=0; i < numProcessors; ++i) {
        if(i == 0) {
          connectTo = "numeric_provider";
        } else {
          connectTo = processorId+(i-1);
        }
        builder.addStreamsProcessor(processorId+i, new PassthroughDatumCounterProcessor(processorId+i), 1, connectTo);
      }
      Set output = Collections.newSetFromMap(new ConcurrentHashMap<>());
      builder.addStreamsPersistWriter("writer", new DatumCounterWriter("writer"), 1, processorId+(numProcessors-1));
      builder.start();
      for(int i=0; i < numProcessors; ++i) {
        Assert.assertEquals("Processor "+i+" did not receive all of the datums", numDatums, PassthroughDatumCounterProcessor.COUNTS.get(processorId+i).get());
      }
      for(int i=0; i < numDatums; ++i) {
        Assert.assertTrue("Expected writer to have received : "+i, DatumCounterWriter.RECEIVED.get("writer").contains(i));
      }
    } finally {
      for(int i=0; i < numProcessors; ++i) {
        removeRegisteredMBeans(processorId+i, processorId+i+"-"+PassthroughDatumCounterProcessor.class.getCanonicalName());
      }
      removeRegisteredMBeans("writer", "numeric_provider");
    }
  }

  @Test
  public void testParallelLinearStream1() {
    String processorId = "proc";
    int numProcessors = randomIntBetween(1, 10);
    int numDatums = randomIntBetween(1, 300000);
    try {
      LocalRuntimeConfiguration conf = new ComponentConfigurator<>(LocalRuntimeConfiguration.class).detectConfiguration();
      StreamBuilder builder = new LocalStreamBuilder(conf.withMaxQueueCapacity(50l));
      builder.newPerpetualStream("numeric_provider", new NumericMessageProvider(numDatums));
      String connectTo;
      for(int i=0; i < numProcessors; ++i) {
        if(i == 0) {
          connectTo = "numeric_provider";
        } else {
          connectTo = processorId+(i-1);
        }
        int parallelHint = randomIntBetween(1,5);
        builder.addStreamsProcessor(processorId+i, new PassthroughDatumCounterProcessor(processorId+i), parallelHint, connectTo);
      }
      builder.addStreamsPersistWriter("writer", new DatumCounterWriter("writer"), 1, processorId+(numProcessors-1));
      builder.start();
      Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
      builder.stop();
      Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
      Assert.assertEquals(numDatums, DatumCounterWriter.RECEIVED.get("writer").size());
      for(int i=0; i < numDatums; ++i) {
        Assert.assertTrue("Expected Writer to receive datum : " + i, DatumCounterWriter.RECEIVED.get("writer").contains(i));
      }
      for(int i=0; i < numProcessors; ++i) {
        Assert.assertEquals(numDatums, PassthroughDatumCounterProcessor.COUNTS.get(processorId+i).get());
      }

    } finally {
      for(int i=0; i < numProcessors; ++i) {
        removeRegisteredMBeans(processorId+i);
      }
      removeRegisteredMBeans("writer", "numeric_provider");
    }
  }

  @Test
  public void testBasicMergeStream() {
    try {
      int numDatums1 = randomIntBetween(1, 300000);
      int numDatums2 = randomIntBetween(1, 300000);
      StreamsProcessor processor1 = new PassthroughDatumCounterProcessor("proc1");
      StreamsProcessor processor2 = new PassthroughDatumCounterProcessor("proc2");
      StreamBuilder builder = new LocalStreamBuilder();
      builder.newPerpetualStream("sp1", new NumericMessageProvider(numDatums1))
          .newPerpetualStream("sp2", new NumericMessageProvider(numDatums2))
          .addStreamsProcessor("proc1", processor1, 1, "sp1")
          .addStreamsProcessor("proc2", processor2, 1, "sp2")
          .addStreamsPersistWriter("writer1", new DatumCounterWriter("writer"), 1, "proc1", "proc2");
      builder.start();
      Assert.assertEquals(numDatums1, PassthroughDatumCounterProcessor.COUNTS.get("proc1").get());
      Assert.assertEquals(numDatums2, PassthroughDatumCounterProcessor.COUNTS.get("proc2").get());
      Assert.assertEquals(numDatums1+numDatums2, DatumCounterWriter.COUNTS.get("writer").get());
    } finally {
      String procClass = "-"+PassthroughDatumCounterProcessor.class.getCanonicalName();
      String writerClass = "-"+DatumCounterWriter.class.getCanonicalName();
      removeRegisteredMBeans("proc1", "proc2", "writer1", "sp1", "sp2");
    }
  }

  @Test
  public void testBasicBranch() {
    try {
      int numDatums = randomIntBetween(1, 300000);
      LocalRuntimeConfiguration conf = new ComponentConfigurator<>(LocalRuntimeConfiguration.class).detectConfiguration();
      StreamBuilder builder = new LocalStreamBuilder(conf.withMaxQueueCapacity(50l));
      builder.newPerpetualStream("prov1", new NumericMessageProvider(numDatums))
          .addStreamsProcessor("proc1", new PassthroughDatumCounterProcessor("proc1"), 1, "prov1")
          .addStreamsProcessor("proc2", new PassthroughDatumCounterProcessor("proc2"), 1, "prov1")
          .addStreamsPersistWriter("w1", new DatumCounterWriter("writer"), 1, "proc1", "proc2");
      builder.start();
      Assert.assertEquals(numDatums, PassthroughDatumCounterProcessor.COUNTS.get("proc1").get());
      Assert.assertEquals(numDatums, PassthroughDatumCounterProcessor.COUNTS.get("proc2").get());
      Assert.assertEquals(numDatums*2, DatumCounterWriter.COUNTS.get("writer").get());
    } finally {
      String provClass = "-"+NumericMessageProvider.class.getCanonicalName();
      String procClass = "-"+PassthroughDatumCounterProcessor.class.getCanonicalName();
      String writerClass = "-"+DatumCounterWriter.class.getCanonicalName();
      removeRegisteredMBeans("prov1", "proc1", "proc2", "w1");
    }
  }

  @Test
  public void testSlowProcessorBranch() {
    try {
      int numDatums = 30;
      LocalRuntimeConfiguration conf = new ComponentConfigurator<>(LocalRuntimeConfiguration.class).detectConfiguration();
      StreamBuilder builder = new LocalStreamBuilder(conf.withTaskTimeoutMs(2000l));
      builder.newPerpetualStream("prov1", new NumericMessageProvider(numDatums))
          .addStreamsProcessor("proc1", new SlowProcessor(), 1, "prov1")
          .addStreamsPersistWriter("w1", new DatumCounterWriter("writer"), 1, "proc1");
      builder.start();
      Assert.assertEquals(numDatums, DatumCounterWriter.COUNTS.get("writer").get());
    } finally {
      String provClass = "-"+NumericMessageProvider.class.getCanonicalName();
      String procClass = "-"+PassthroughDatumCounterProcessor.class.getCanonicalName();
      String writerClass = "-"+DatumCounterWriter.class.getCanonicalName();
      removeRegisteredMBeans("prov1", "proc1", "w1");
    }
  }

  @Test
  public void testConfiguredProviderTimeout() {
    try {
      int timeout = 10000;
      long start = System.currentTimeMillis();
      LocalRuntimeConfiguration conf = new ComponentConfigurator<>(LocalRuntimeConfiguration.class).detectConfiguration();
      StreamBuilder builder = new LocalStreamBuilder((LocalRuntimeConfiguration)conf
        .withMaxQueueCapacity(-1l)
        .withProviderTimeoutMs((long)timeout)
      );
      builder.newPerpetualStream("prov1", new EmptyResultSetProvider())
          .addStreamsProcessor("proc1", new PassthroughDatumCounterProcessor("proc1"), 1, "prov1")
          .addStreamsProcessor("proc2", new PassthroughDatumCounterProcessor("proc2"), 1, "proc1")
          .addStreamsPersistWriter("w1", new SystemOutWriter(), 1, "proc1");
      builder.start();
      long end = System.currentTimeMillis();
      //We care mostly that it doesn't terminate too early.  With thread shutdowns, etc, the actual time is indeterminate.  Just make sure there is an upper bound
      Assert.assertThat((int) (end - start), is(allOf(greaterThanOrEqualTo(timeout), lessThanOrEqualTo(4 * timeout))));
    } finally {
      String provClass = "-"+NumericMessageProvider.class.getCanonicalName();
      String procClass = "-"+PassthroughDatumCounterProcessor.class.getCanonicalName();
      String writerClass = "-"+DatumCounterWriter.class.getCanonicalName();
      removeRegisteredMBeans("prov1", "proc1", "proc2", "w1");
    }
  }

  @Ignore
  @Test
  public void ensureShutdownWithBlockedQueue() throws InterruptedException {
    try {
      ExecutorService service = Executors.newSingleThreadExecutor();
      int before = Thread.activeCount();
      final StreamBuilder builder = new LocalStreamBuilder();
      builder.newPerpetualStream("prov1", new NumericMessageProvider(30))
          .addStreamsProcessor("proc1", new SlowProcessor(), 1, "prov1")
          .addStreamsPersistWriter("w1", new SystemOutWriter(), 1, "proc1");
      service.submit(builder::start);
      //Let streams spin up threads and start to process
      Thread.sleep(500);
      builder.stop();
      service.shutdownNow();
      service.awaitTermination(30000, TimeUnit.MILLISECONDS);
      Assert.assertThat(Thread.activeCount(), is(equalTo(before)));
    } finally {
      String provClass = "-"+NumericMessageProvider.class.getCanonicalName();
      String procClass = "-"+PassthroughDatumCounterProcessor.class.getCanonicalName();
      String writerClass = "-"+DatumCounterWriter.class.getCanonicalName();
      removeRegisteredMBeans("prov1", "proc1", "w1");
    }
  }

  @Before
  private void clearCounters() {
    PassthroughDatumCounterProcessor.COUNTS.clear();
    PassthroughDatumCounterProcessor.CLAIMED_ID.clear();
    PassthroughDatumCounterProcessor.SEEN_DATA.clear();
    DatumCounterWriter.COUNTS.clear();
    DatumCounterWriter.CLAIMED_ID.clear();
    DatumCounterWriter.SEEN_DATA.clear();
    DatumCounterWriter.RECEIVED.clear();
  }


  /**
   * Creates {@link org.apache.streams.core.StreamsProcessor} that passes any StreamsDatum it gets as an
   * input and counts the number of items it processes.
   * @param counter
   * @return
   */
  private StreamsProcessor createPassThroughProcessor(final AtomicInteger counter) {
    StreamsProcessor processor = mock(StreamsProcessor.class);
    when(processor.process(any(StreamsDatum.class))).thenAnswer(new Answer<List<StreamsDatum>>() {
      @Override
      public List<StreamsDatum> answer(InvocationOnMock invocationOnMock) throws Throwable {
        List<StreamsDatum> datum = new LinkedList<>();
        if(counter != null) {
          counter.incrementAndGet();
        }
        datum.add((StreamsDatum) invocationOnMock.getArguments()[0] );
        return datum;
      }
    });
    return processor;
  }

  private StreamsPersistWriter createSetCollectingWriter(final Set collector) {
    return createSetCollectingWriter(collector, null);
  }

  /**
   * Creates a StreamsPersistWriter that adds every datums document to a set
   * @param collector
   * @return
   */
  private StreamsPersistWriter createSetCollectingWriter(final Set collector, final AtomicInteger counter) {
    StreamsPersistWriter writer = mock(StreamsPersistWriter.class);
    doAnswer(invocationOnMock -> {
      if(counter != null) {
        counter.incrementAndGet();
      }
      collector.add(((StreamsDatum)invocationOnMock.getArguments()[0]).getDocument());
      return null;
    }).when(writer).write(any(StreamsDatum.class));
    return writer;
  }
}
