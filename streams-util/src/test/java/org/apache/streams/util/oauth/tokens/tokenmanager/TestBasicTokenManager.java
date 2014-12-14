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

package org.apache.streams.util.oauth.tokens.tokenmanager;

import org.apache.streams.util.oauth.tokens.AbstractOauthToken;
import org.apache.streams.util.oauth.tokens.tokenmanager.impl.BasicTokenManger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Unit tests for BasticTokenManager
 */
public class TestBasicTokenManager {

    /**
     * Simple token for testing purposes
     */
    private class TestToken extends AbstractOauthToken {

        private String s;

        public TestToken(String s) {
            this.s = s;
        }

        @Override
        protected boolean internalEquals(Object o) {
            if(!(o instanceof TestToken))
                return false;
            TestToken that = (TestToken) o;
            return this.s.equals(that.s);
        }
    }

    @Test
    public void testNoArgConstructor() {
        try {
            BasicTokenManger manager = new BasicTokenManger<TestToken>();
            assertEquals(0, manager.numAvailableTokens());
        } catch (Throwable t) {
            fail("Constructors threw error: "+t.getMessage());
        }
    }

    @Test
    public void testCollectionConstructor() {
        List<TestToken> tokens = new LinkedList<TestToken>();
        try {
            BasicTokenManger manager1 = new BasicTokenManger<TestToken>(tokens);
            tokens.add(new TestToken("a"));
            tokens.add(new TestToken("b"));
            assertEquals(0, manager1.numAvailableTokens());
            BasicTokenManger manager2 = new BasicTokenManger<TestToken>(tokens);
            assertEquals(2, manager2.numAvailableTokens());
            assertEquals(0, manager1.numAvailableTokens());
        } catch (Throwable t) {
            fail("Constructors threw error: "+t.getMessage());
        }
    }

    @Test
    public void testAddTokenToPool() {
        BasicTokenManger<TestToken> manager = new BasicTokenManger<TestToken>();
        assertTrue(manager.addTokenToPool(new TestToken("a")));
        assertEquals(1, manager.numAvailableTokens());
        assertFalse(manager.addTokenToPool(new TestToken("a")));
        assertEquals(1, manager.numAvailableTokens());
        assertTrue(manager.addTokenToPool(new TestToken("b")));
        assertEquals(2, manager.numAvailableTokens());
    }

    @Test
    public void testAddAllTokensToPool() {
        BasicTokenManger<TestToken> manager = new BasicTokenManger<TestToken>();
        List<TestToken> tokens = new ArrayList<TestToken>();
        tokens.add(new TestToken("a"));
        tokens.add(new TestToken("b"));
        tokens.add(new TestToken("c"));
        assertTrue(manager.addAllTokensToPool(tokens));
        assertEquals(3, manager.numAvailableTokens());
        assertFalse(manager.addAllTokensToPool(tokens));
        assertEquals(3, manager.numAvailableTokens());
        tokens.add(new TestToken("d"));
        assertTrue(manager.addAllTokensToPool(tokens));
        assertEquals(4, manager.numAvailableTokens());
    }

    @Test
    public void testGetNextAvailableToken() {
        BasicTokenManger manager = new BasicTokenManger<TestToken>();
        assertNull(manager.getNextAvailableToken());
        TestToken tokenA = new TestToken("a");
        assertTrue(manager.addTokenToPool(tokenA));
        assertEquals(tokenA, manager.getNextAvailableToken());
        assertEquals(tokenA, manager.getNextAvailableToken());
        assertEquals(tokenA, manager.getNextAvailableToken());

        TestToken tokenB = new TestToken("b");
        TestToken tokenC = new TestToken("c");
        assertTrue(manager.addTokenToPool(tokenB));
        assertTrue(manager.addTokenToPool(tokenC));
        assertEquals(tokenA, manager.getNextAvailableToken());
        assertEquals(tokenB, manager.getNextAvailableToken());
        assertEquals(tokenC, manager.getNextAvailableToken());
        assertEquals(tokenA, manager.getNextAvailableToken());
        assertEquals(tokenB, manager.getNextAvailableToken());
        assertEquals(tokenC, manager.getNextAvailableToken());
    }

    @Test
    public void testMultiThreadSafety() {
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);
        BasicTokenManger<TestToken> manager = new BasicTokenManger<TestToken>();
        for(int i=0; i < numThreads; ++i) {
            assertTrue(manager.addTokenToPool(new TestToken(String.valueOf(i))));
        }
        for(int i=0; i < numThreads; ++i) {
            executor.submit(new TestThread(manager, startLatch, finishLatch, numThreads));
        }
        try {
            Thread.sleep(2000); //sleep for 2 seconds so other threads can initialize
            startLatch.countDown();
            finishLatch.await();
            assertTrue("No errors were thrown during thead safe check", true);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            fail("Error occured durring thread safe test : "+t.getMessage());
        }
    }

    /**
     * Test class for thread safe check.
     */
    private class TestThread implements Runnable {

        private BasicTokenManger<TestToken> manager;
        private CountDownLatch startLatch;
        private CountDownLatch finishedLatch;
        private int availableTokens;

        public TestThread(BasicTokenManger<TestToken> manager, CountDownLatch startLatch, CountDownLatch finishedLatch, int availableTokens) {
            this.manager = manager;
            this.startLatch = startLatch;
            this.finishedLatch = finishedLatch;
            this.availableTokens = availableTokens;
        }

        @Override
        public void run() {
            try {
                this.startLatch.await();
                for(int i=0; i < 1000; ++i) {
                    assertNotNull(this.manager.getNextAvailableToken());
                    assertEquals(this.availableTokens, this.manager.numAvailableTokens());
                }
                this.finishedLatch.countDown();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Throwable t) {
                fail("Threw error in multithread test : "+t.getMessage());
            }
        }
    }

}
