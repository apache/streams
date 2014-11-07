package com.google.gplus.provider;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.google.api.services.plus.Plus;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.apache.streams.google.gplus.GPlusOAuthConfiguration;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link com.google.gplus.provider.AbstractGPlusProvider}
 */
public class TestAbstractGPlusProvider extends RandomizedTest{

    /**
     * Test that every collector will be run and that data queued from the collectors will be processed.
     */
    @Test
    @Repeat(iterations = 3)
    public void testDataCollectorRunsPerUser() {
        int numUsers = randomIntBetween(1, 1000);
        List<UserInfo> userList = Lists.newLinkedList();
        for(int i=0; i < numUsers; ++i) {
            userList.add(new UserInfo());
        }
        GPlusConfiguration config = new GPlusConfiguration();
        GPlusOAuthConfiguration oauth = new GPlusOAuthConfiguration();
        oauth.setAccessToken("a");
        oauth.setConsumerKey("a");
        oauth.setConsumerSecret("a");
        oauth.setAccessTokenSecret("a");
        config.setOauth(oauth);
        config.setGooglePlusUsers(userList);
        AbstractGPlusProvider provider = new AbstractGPlusProvider(config) {

            @Override
            protected Plus createPlusClient() throws IOException {
                return mock(Plus.class);
            }

            @Override
            protected Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, Plus plus, UserInfo userInfo) {
                final BlockingQueue<StreamsDatum> q = queue;
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            q.put(new StreamsDatum(null));
                        } catch (InterruptedException ie) {
                            fail("Test was interrupted");
                        }
                    }
                };
            }
        };

        try {
            provider.prepare(null);
            provider.startStream();
            int datumCount = 0;
            while(provider.isRunning()) {
                datumCount += provider.readCurrent().size();
            }
            assertEquals(numUsers, datumCount);
        } finally {
            provider.cleanUp();
        }
    }


}
