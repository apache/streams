package org.apache.streams.instagram.provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.apache.streams.instagram.InstagramUserInformationConfiguration;
import org.jinstagram.Instagram;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link org.apache.streams.instagram.provider.InstagramRecentMediaCollector}
 */
public class InstagramRecentMediaCollectorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramRecentMediaCollectorTest.class);

    private int expectedDataCount = 0;
    private long randomSeed = System.currentTimeMillis();
    private Random rand = new Random(randomSeed);
    private Map<Pagination, MediaFeed> pageMap = Maps.newHashMap();

    @Test
    public void testHandleInstagramException1() throws InstagramException {
        InstagramException ie = mock(InstagramException.class);
        when(ie.getRemainingLimitStatus()).thenReturn(1);
        final String message = "Test Message";
        when(ie.getMessage()).thenReturn(message);
        InstagramRecentMediaCollector collector = new InstagramRecentMediaCollector(new ConcurrentLinkedQueue<MediaFeedData>(), new InstagramUserInformationConfiguration());
        try {
            collector.handleInstagramException(ie, 1);
            fail("Expected RuntimeException to be thrown");
        } catch (InstagramException rte) {
//            assertTrue(rte.getMessage().contains("Mock for InstagramException"));
            assertEquals(message, rte.getMessage());
        }
    }

    @Test
    public void testHandleInstagramException2() throws InstagramException{
        InstagramException ie = mock(InstagramException.class);
        when(ie.getRemainingLimitStatus()).thenReturn(0);
        InstagramRecentMediaCollector collector = new InstagramRecentMediaCollector(new ConcurrentLinkedQueue<MediaFeedData>(), new InstagramUserInformationConfiguration());
        long startTime = System.currentTimeMillis();
        collector.handleInstagramException(ie, 1);
        long endTime = System.currentTimeMillis();
        LOGGER.debug("Slept for {} ms", startTime - endTime);
        assertTrue(endTime - startTime >= 4000); //allow for 1 sec of error
        startTime = System.currentTimeMillis();
        collector.handleInstagramException(ie, 2);
        endTime = System.currentTimeMillis();
        LOGGER.debug("Slept for {} ms", startTime - endTime);
        assertTrue(endTime - startTime >= 24000); //allow for 1 sec of error
    }

    @Test
    public void testGetUserIds() {
        InstagramUserInformationConfiguration config = new InstagramUserInformationConfiguration();
        List<String> userIds = Lists.newLinkedList();
        userIds.add("1");
        userIds.add("2");
        userIds.add("3");
        userIds.add("4");
        userIds.add("abcdefg");
        config.setUserIds(userIds);
        InstagramRecentMediaCollector collector = new InstagramRecentMediaCollector(new ConcurrentLinkedQueue<MediaFeedData>(), config);

        Set<Long> expected = Sets.newHashSet();
        expected.add(1L);
        expected.add(2L);
        expected.add(3L);
        expected.add(4L);

        assertEquals(expected, collector.getUserIds());
    }

    @Test
    public void testRun() {
        Queue<MediaFeedData> data = Queues.newConcurrentLinkedQueue();
        InstagramUserInformationConfiguration config = new InstagramUserInformationConfiguration();
        List<String> userIds = Lists.newLinkedList();
        userIds.add("1");
        userIds.add("2");
        userIds.add("3");
        userIds.add("4");
        config.setUserIds(userIds);
        InstagramRecentMediaCollector collector = new InstagramRecentMediaCollector(data, config);
        collector.setInstagramClient(createMockInstagramClient());
        collector.run();
        LOGGER.debug("Random seed == {}", randomSeed);
        assertEquals("Random Seed == " + randomSeed, this.expectedDataCount, data.size());
    }

    private Instagram createMockInstagramClient() {
        final Instagram instagramClient = mock(Instagram.class);
        try {
            final InstagramException mockException = mock(InstagramException.class);
            when(mockException.getRemainingLimitStatus()).thenReturn(-1);
            when(mockException.getMessage()).thenReturn("MockInstagramException message");
            when(instagramClient.getRecentMediaFeed(any(Long.class))).thenAnswer(new Answer<MediaFeed>() {
                @Override
                public MediaFeed answer(InvocationOnMock invocationOnMock) throws Throwable {
                    long param = (Long) invocationOnMock.getArguments()[0];
                    if (param == 2L) {
                        throw mockException;
                    } else {
                        return createRandomMockMediaFeed();
                    }
                }
            });
            when(instagramClient.getRecentMediaNextPage(any(Pagination.class))).thenAnswer(new Answer<MediaFeed>() {
                @Override
                public MediaFeed answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return createRandomMockMediaFeed();
                }
            });
        } catch (InstagramException ie) {
            fail("Failed to create mock instagram client.");
        }
        return instagramClient;
    }

    private MediaFeed createRandomMockMediaFeed() throws InstagramException {
        MediaFeed feed = mock(MediaFeed.class);
        when(feed.getData()).thenReturn(createData(this.rand.nextInt(100)));
        Pagination pagination = mock(Pagination.class);
        if(this.rand.nextInt(2) == 0) {
            when(pagination.hasNextPage()).thenReturn(true);
        } else {
            when(pagination.hasNextPage()).thenReturn(false);
        }
        when(feed.getPagination()).thenReturn(pagination);
        return feed;
    }

    private List<MediaFeedData> createData(int size) {
        List<MediaFeedData> data = Lists.newLinkedList();
        for(int i=0; i < size; ++i) {
            data.add(mock(MediaFeedData.class));
        }
        this.expectedDataCount += size;
        return data;
    }

}
