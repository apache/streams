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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.test.processors.PassthroughDatumCounterProcessor;
import org.apache.streams.local.test.processors.SlowProcessor;
import org.apache.streams.local.test.providers.EmptyResultSetProvider;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.writer.SystemOutWriter;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

/**
 * Basic Tests for the LocalStreamBuilder.
 *
 * Test are performed by redirecting system out and counting the number of lines that the SystemOutWriter prints
 * to System.out.  The SystemOutWriter also prints one line when cleanUp() is called, so this is why it tests for
 * the numDatums +1.
 *
 *
 */
public class LocalStreamBuilderTest {

    ByteArrayOutputStream out;

    @Before
    public void setSystemOut() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
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
        assertNotNull(exp);
        exp = null;
        builder.addStreamsProcessor("1", new PassthroughDatumCounterProcessor(), 1, "id");
        try {
            builder.addStreamsProcessor("2", new PassthroughDatumCounterProcessor(), 1, "id", "id2");
        } catch (RuntimeException e) {
            exp = e;
        }
        assertNotNull(exp);
    }

    @Test
    public void testBasicLinearStream1()  {
        int numDatums = 1;
        StreamBuilder builder = new LocalStreamBuilder();
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor();
        SystemOutWriter writer = new SystemOutWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertThat(count, greaterThan(numDatums)); // using > because number of lines in system.out is non-deterministic
    }

    @Test
    public void testBasicLinearStream2()  {
        int numDatums = 100;
        StreamBuilder builder = new LocalStreamBuilder();
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor();
        SystemOutWriter writer = new SystemOutWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertThat(count, greaterThan(numDatums)); // using > because number of lines in system.out is non-deterministic
    }

    @Test
    public void testParallelLinearStream1() {
        int numDatums = 1000;
        int parallelHint = 20;
        PassthroughDatumCounterProcessor.sawData = new HashSet<Integer>();
        PassthroughDatumCounterProcessor.claimedNumber = new HashSet<Integer>();
        StreamBuilder builder = new LocalStreamBuilder();
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor();
        SystemOutWriter writer = new SystemOutWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, parallelHint, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertThat(count, greaterThan(numDatums)); // using > because number of lines in system.out is non-deterministic
        assertEquals(parallelHint, PassthroughDatumCounterProcessor.claimedNumber.size()); //test 40 were initialized
        assertTrue(PassthroughDatumCounterProcessor.sawData.size() > 1 && PassthroughDatumCounterProcessor.sawData.size() <= parallelHint); //test more than one processor got data
    }

    @Test
    public void testBasicMergeStream() {
        int numDatums1 = 1;
        int numDatums2 = 100;
        PassthroughDatumCounterProcessor processor1 = new PassthroughDatumCounterProcessor();
        PassthroughDatumCounterProcessor processor2 = new PassthroughDatumCounterProcessor();
        SystemOutWriter writer = new SystemOutWriter();
        StreamBuilder builder = new LocalStreamBuilder();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums1))
                .newReadCurrentStream("sp2", new NumericMessageProvider(numDatums2))
                .addStreamsProcessor("proc1", processor1, 1, "sp1")
                .addStreamsProcessor("proc2", processor2, 1, "sp2")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1", "proc2");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while (scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertThat(count, greaterThan(numDatums1 + numDatums2)); // using > because number of lines in system.out is non-deterministic
    }

    @Test
    public void testBasicBranch() {
        int numDatums = 100;
        StreamBuilder builder = new LocalStreamBuilder();
        builder.newReadCurrentStream("prov1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", new PassthroughDatumCounterProcessor(), 1, "prov1")
                .addStreamsProcessor("proc2", new PassthroughDatumCounterProcessor(), 1, "prov1")
                .addStreamsPersistWriter("w1", new SystemOutWriter(), 1, "proc1", "proc2");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertThat(count, greaterThan(numDatums * 2)); // using > because number of lines in system.out is non-deterministic

    }

    @Test
    public void testSlowProcessorBranch() {
        int numDatums = 30;
        int timeout = 2000;
        Map<String, Object> config = Maps.newHashMap();
        config.put(LocalStreamBuilder.TIMEOUT_KEY, timeout);
        StreamBuilder builder = new LocalStreamBuilder(config);
        builder.newReadCurrentStream("prov1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", new SlowProcessor(), 1, "prov1")
                .addStreamsPersistWriter("w1", new SystemOutWriter(), 1, "proc1");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertThat(count, greaterThan(numDatums)); // using > because number of lines in system.out is non-deterministic

    }

    @Test
    public void testConfiguredProviderTimeout() {
        Map<String, Object> config = Maps.newHashMap();
        int timeout = 10000;
        config.put(LocalStreamBuilder.TIMEOUT_KEY, timeout);
        long start = System.currentTimeMillis();
        StreamBuilder builder = new LocalStreamBuilder(-1, config);
        builder.newPerpetualStream("prov1", new EmptyResultSetProvider())
                .addStreamsProcessor("proc1", new PassthroughDatumCounterProcessor(), 1, "prov1")
                .addStreamsProcessor("proc2", new PassthroughDatumCounterProcessor(), 1, "proc1")
                .addStreamsPersistWriter("w1", new SystemOutWriter(), 1, "proc1");
        builder.start();
        long end = System.currentTimeMillis();
        //We care mostly that it doesn't terminate too early.  With thread shutdowns, etc, the actual time is indeterminate.  Just make sure there is an upper bound
        assertThat((int)(end - start), is(allOf(greaterThanOrEqualTo(timeout), lessThanOrEqualTo(4 * timeout))));
    }

    @Test
    public void ensureShutdownWithBlockedQueue() throws InterruptedException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        int before = Thread.activeCount();
        final StreamBuilder builder = new LocalStreamBuilder(1);
        builder.newPerpetualStream("prov1", new NumericMessageProvider(30))
                .addStreamsProcessor("proc1", new SlowProcessor(), 1, "prov1")
                .addStreamsPersistWriter("w1", new SystemOutWriter(), 1, "proc1");
        service.submit(new Runnable(){
            @Override
            public void run() {
                builder.start();
            }
        });
        //Let streams spin up threads and start to process
        Thread.sleep(500);
        builder.stop();
        service.shutdownNow();
        service.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertThat(Thread.activeCount(), is(equalTo(before)));
    }
}
