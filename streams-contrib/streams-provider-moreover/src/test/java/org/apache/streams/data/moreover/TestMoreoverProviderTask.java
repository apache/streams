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
package org.apache.streams.data.moreover;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.moreover.api.Article;
import org.apache.streams.core.StreamsDatum;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Basic Unit tests for {@link org.apache.streams.data.moreover.MoreoverProviderTask}
 */
public class TestMoreoverProviderTask extends RandomizedTest{


    /**
     * Test that stop task will stop the task and is running returns true only when the task is still running.
     */
    @Test
    public void testStopTaskAndIsRunning() throws Exception {
        BlockingQueue<StreamsDatum> queue = Queues.newLinkedBlockingQueue();
        // perpetual task that doesn't receive any results but all api request appear successful
        MoreoverProviderTask task = new MoreoverProviderTask("", "", queue, "", true) {
            @Override
            protected MoreoverClient getMoreoverClient(String apiId, String apiKey, String lastSequence) {
                MoreoverClient mockClient = mock(MoreoverClient.class);
                MoreoverResult mockResult = mock(MoreoverResult.class);
                List<Article> articles = Lists.newLinkedList();
                doReturn(0).when(mockResult).numArticles(); //always empty
                doReturn(articles).when(mockResult).getArticles();
                doReturn(BigInteger.ZERO).when(mockResult).getMaxSequencedId();
                try {
                    doReturn(mockResult).when(mockClient).getNextBatch();
                    doReturn(1L).when(mockClient).getPullTime(); // so task does not sleep.
                } catch (Throwable t) {
                    //will never happen
                    throw  new RuntimeException(t);
                }
                return mockClient;
            }

        };

        ExecutorService service = Executors.newSingleThreadExecutor();
        assertFalse("Expected initial running state to be false", task.isRunning());
        service.submit(task);
        service.shutdown();
        //sleep to allow task to start
        Thread.currentThread().sleep(1000);
        assertTrue("Expected isRunning to return true", task.isRunning());
        assertFalse("Expected task to still be executing", service.isTerminated());
        task.stopTask();
        assertTrue("Expected task thread to terminate", service.awaitTermination(10, TimeUnit.SECONDS));
        assertFalse("Expected task to not be running", task.isRunning());
    }

    /**
     *
     */
    @Test
    @Repeat(iterations = 3)
    public void testPollTillClientReturnsLessThanMaxResults() throws Exception {
        final int expectedNumOfArticles = randomIntBetween(0, 10000);
        BlockingQueue<StreamsDatum> resultDatums = Queues.newLinkedBlockingQueue();
        MoreoverProviderTask task = new MoreoverProviderTask("", "", resultDatums, "", false);
        MoreoverResult mockResult = mock(MoreoverResult.class);
        final AtomicInteger currentResultCount = new AtomicInteger(0);
        Answer<List<Article>> getArticlesAnswer = new Answer<List<Article>>() {
            private int count = expectedNumOfArticles;

            @Override
            public List<Article> answer(InvocationOnMock invocationOnMock) throws Throwable {
                int toReturn;
                if(count - MoreoverClient.MAX_LIMIT <= 0) {
                    toReturn = count;
                    count = 0;
                } else {
                    toReturn = MoreoverClient.MAX_LIMIT;
                    count -= MoreoverClient.MAX_LIMIT;
                }
                Article article = new Article();
                List<Article> articles = Lists.newLinkedList();
                for(int i=0; i < toReturn; ++i) {
                    articles.add(article);
                }
                currentResultCount.set(articles.size());
                return articles;
            }
        };
        Answer<Integer> numArtilcesAnswer = new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                return currentResultCount.get();
            }
        };
        doAnswer(getArticlesAnswer).when(mockResult).getArticles();
        doAnswer(numArtilcesAnswer).when(mockResult).numArticles();
        MoreoverClient mockClient = mock(MoreoverClient.class);
        doReturn(mockResult).when(mockClient).getNextBatch();

        task.pollTillClientReturnsLessThanMaxResults(mockClient);
        assertEquals("Incorrect number of datums queued", expectedNumOfArticles, resultDatums.size());
        resultDatums.clear();
    }
}
