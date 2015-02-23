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

import org.apache.streams.threaded.builders.ThreadedStreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.test.providers.PreDefinedProvider;
import org.apache.streams.local.test.writer.DatumCollectorWriter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ThreadedStreamBuilderDatumTest {

    @Test
    public void testDatums() {
        List<StreamsDatum> providerDatums = new ArrayList<StreamsDatum>();
        providerDatums.add(new StreamsDatum(1, "1"));
        providerDatums.add(new StreamsDatum(2, "2"));
        providerDatums.add(new StreamsDatum(3, "3"));
        PreDefinedProvider provider = new PreDefinedProvider(providerDatums);

        DatumCollectorWriter writer = new DatumCollectorWriter();

        new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(10))
                .newReadCurrentStream("provider", provider)
                .addStreamsPersistWriter("writer", writer, 1, "provider")
                .start();


        assertEquals(3, writer.getDatums().size());

        boolean found1 = false, found2 = false, found3 = false;

        for(StreamsDatum d : writer.getDatums()) {
            if(d == null) {
                fail("null datum, unexpected");
            } if(d.getId().equals("1")) {
                found1 = true;
            } else if(d.getId().equals("2")) {
                found2 = true;
            } else if(d.getId().equals("3")) {
                found3 = true;
            }
        }

        assertTrue(found1 && found2 && found3);

        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }

    @Test
    public void testDatumsWithSerialization2() {
        List<StreamsDatum> providerDatums = new ArrayList<StreamsDatum>();
        providerDatums.add(new StreamsDatum(new Object(), "1"));
        providerDatums.add(new StreamsDatum(new Object(), "2"));
        providerDatums.add(new StreamsDatum(new Object(), "3"));
        PreDefinedProvider provider = new PreDefinedProvider(providerDatums);

        DatumCollectorWriter writer = new DatumCollectorWriter();

        new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(10))
                .newReadCurrentStream("provider", provider)
                .addStreamsPersistWriter("writer", writer, 1, "provider")
                .start();

        assertEquals(3, writer.getDatums().size());

        boolean found1 = false, found2 = false, found3 = false;

        for(StreamsDatum d : writer.getDatums()) {
            if(d == null) {
                fail("null datum, unexpected");
            } if(d.getId().equals("1")) {
                found1 = true;
            } else if(d.getId().equals("2")) {
                found2 = true;
            } else if(d.getId().equals("3")) {
                found3 = true;
            }
        }

        assertTrue(found1 && found2 && found3);

        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());

    }

    @Test
    public void testDatumsWithSerialization1() {
        List<StreamsDatum> providerDatums = new ArrayList<StreamsDatum>();
        providerDatums.add(new StreamsDatum(new Object(), "1"));
        providerDatums.add(new StreamsDatum(new Object(), "2"));
        providerDatums.add(new StreamsDatum(new Object(), "3"));
        PreDefinedProvider provider = new PreDefinedProvider(providerDatums);

        DatumCollectorWriter writer = new DatumCollectorWriter();

        new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(10))
                .newReadCurrentStream("provider", provider)
                .addStreamsPersistWriter("writer", writer, 1, "provider")
                .start();

        boolean found1 = false, found2 = false, found3 = false;

        assertEquals(3, writer.getDatums().size());

        for(StreamsDatum d : writer.getDatums()) {
            if (d == null) {
                fail("null datum, unexpected");
            }
            if (d.getId().equals("1")) {
                found1 = true;
            } else if (d.getId().equals("2")) {
                found2 = true;
            } else if (d.getId().equals("3")) {
                found3 = true;
            }
        }

        assertTrue(found1 && found2 && found3);

        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }
}