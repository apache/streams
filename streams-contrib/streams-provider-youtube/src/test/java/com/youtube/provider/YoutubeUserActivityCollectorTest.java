/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package com.youtube.provider;

import com.google.api.client.util.Lists;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.local.queues.ThroughputQueue;
import org.apache.streams.util.api.requests.backoff.impl.ExponentialBackOffStrategy;
import org.apache.youtube.pojo.YoutubeConfiguration;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class YoutubeUserActivityCollectorTest {
    private final String USER_ID = "fake_user_id";
    private static final String IN_RANGE_IDENTIFIER = "data in range";
    private YoutubeConfiguration config;

    @Before
    public void setup() {
        this.config = new YoutubeConfiguration();
        this.config.setApiKey("API_KEY");
    }

    @Test
    public void testGetVideos() throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        YouTube youtube = buildYouTube(0, 1, 0, now, now.minus(10000));

        BlockingQueue<StreamsDatum> datumQueue = new ThroughputQueue<>();
        YoutubeUserActivityCollector collector = new YoutubeUserActivityCollector(youtube, datumQueue, new ExponentialBackOffStrategy(2), new UserInfo().withUserId(USER_ID), this.config);

        List<Video> video = collector.getVideoList("fake_video_id");

        assertNotNull(video.get(0));
    }

    @Test
    public void testGetVideosNull() throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        YouTube youtube = buildYouTube(0, 0, 0, now.plus(10000), now.minus(10000));

        BlockingQueue<StreamsDatum> datumQueue = new ThroughputQueue<>();
        YoutubeUserActivityCollector collector = new YoutubeUserActivityCollector(youtube, datumQueue, new ExponentialBackOffStrategy(2), new UserInfo().withUserId(USER_ID), this.config);

        List<Video> video = collector.getVideoList("fake_video_id");

        assertEquals(video.size(), 0);
    }

    @Test
    public void testProcessActivityFeed() throws IOException, InterruptedException {
        DateTime now = new DateTime(System.currentTimeMillis());
        YouTube youtube = buildYouTube(0, 0, 5, now.plus(3000000), now.minus(1000000));

        BlockingQueue<StreamsDatum> datumQueue = new ThroughputQueue<>();
        YoutubeUserActivityCollector collector = new YoutubeUserActivityCollector(youtube, datumQueue, new ExponentialBackOffStrategy(2), new UserInfo().withUserId(USER_ID), this.config);

        ActivityListResponse feed = buildActivityListResponse(1);

        collector.processActivityFeed(feed, new DateTime(System.currentTimeMillis()), null);

        assertEquals(collector.getDatumQueue().size(), 5);
    }

    @Test
    public void testProcessActivityFeedBefore() throws IOException, InterruptedException {
        DateTime now = new DateTime(System.currentTimeMillis());
        YouTube youtube = buildYouTube(5, 0, 0, now, now);

        BlockingQueue<StreamsDatum> datumQueue = new ThroughputQueue<>();
        YoutubeUserActivityCollector collector = new YoutubeUserActivityCollector(youtube, datumQueue, new ExponentialBackOffStrategy(2), new UserInfo().withUserId(USER_ID), this.config);

        ActivityListResponse feed = buildActivityListResponse(1);

        collector.processActivityFeed(feed, new DateTime(System.currentTimeMillis()), null);

        assertEquals(collector.getDatumQueue().size(), 0);
    }

    @Test
    public void testProcessActivityFeedAfter() throws IOException, InterruptedException {
        DateTime now = new DateTime(System.currentTimeMillis());
        YouTube youtube = buildYouTube(0, 5, 0, now, now);

        BlockingQueue<StreamsDatum> datumQueue = new ThroughputQueue<>();
        YoutubeUserActivityCollector collector = new YoutubeUserActivityCollector(youtube, datumQueue, new ExponentialBackOffStrategy(2), new UserInfo().withUserId(USER_ID), this.config);

        ActivityListResponse feed = buildActivityListResponse(1);

        collector.processActivityFeed(feed, new DateTime(now.getMillis()-100000), null);

        assertEquals(collector.getDatumQueue().size(), 5);
    }

    @Test
    public void testProcessActivityFeedMismatchCount() throws IOException, InterruptedException {
        DateTime now = new DateTime(System.currentTimeMillis());
        YouTube youtube = buildYouTube(5, 5, 5, now, now.minus(100000));

        BlockingQueue<StreamsDatum> datumQueue = new ThroughputQueue<>();
        YoutubeUserActivityCollector collector = new YoutubeUserActivityCollector(youtube, datumQueue, new ExponentialBackOffStrategy(2), new UserInfo().withUserId(USER_ID), this.config);

        ActivityListResponse feed = buildActivityListResponse(1);

        collector.processActivityFeed(feed, new DateTime(now), null);

        assertEquals(collector.getDatumQueue().size(), 5);
    }

    @Test
    public void testProcessActivityFeedMismatchCountInRange() throws IOException, InterruptedException {
        DateTime now = new DateTime(System.currentTimeMillis());
        YouTube youtube = buildYouTube(5, 5, 5, now, now.minus(100000));

        BlockingQueue<StreamsDatum> datumQueue = new ThroughputQueue<>();
        YoutubeUserActivityCollector collector = new YoutubeUserActivityCollector(youtube, datumQueue, new ExponentialBackOffStrategy(2), new UserInfo().withUserId(USER_ID), this.config);

        ActivityListResponse feed = buildActivityListResponse(1);

        collector.processActivityFeed(feed, new DateTime(now), new DateTime(now).minus(100000));

        assertEquals(collector.getDatumQueue().size(), 5);
    }

    private ActivityListResponse buildActivityListResponse(int num) {
        ActivityListResponse activityListResponse = new ActivityListResponse();
        List<Activity> items = Lists.newArrayList();

        for(int x = 0; x < num; x ++ ) {
            Activity activity = new Activity();

            ActivityContentDetails contentDetails = new ActivityContentDetails();
            ActivityContentDetailsUpload upload = new ActivityContentDetailsUpload();
            upload.setVideoId("video_id_" + x);
            contentDetails.setUpload(upload);

            activity.setId("id_" + x);
            activity.setContentDetails(contentDetails);

            items.add(activity);
        }

        activityListResponse.setItems(items);

        return activityListResponse;
    }

    private YouTube buildYouTube(int numBeforeRange, int numAfterRange, int numInRange, DateTime afterDate, DateTime beforeDate) {

        YouTube youtube = createYoutubeMock(numBeforeRange, numAfterRange, numInRange, afterDate, beforeDate);

        return youtube;
    }

    private YouTube createYoutubeMock(int numBefore, int numAfter, int numInRange,  DateTime after, DateTime before) {
        YouTube youtube = mock(YouTube.class);

        final YouTube.Videos videos = createMockVideos(numBefore, numAfter, numInRange, after, before);
        doAnswer(new Answer() {
            @Override
            public YouTube.Videos answer(InvocationOnMock invocationOnMock) throws Throwable {
                return videos;
            }
        }).when(youtube).videos();

        return youtube;
    }

    private YouTube.Videos createMockVideos(int numBefore, int numAfter, int numInRange,  DateTime after, DateTime before) {
        YouTube.Videos videos = mock(YouTube.Videos.class);

        try {
            YouTube.Videos.List list = createMockVideosList(numBefore, numAfter, numInRange, after, before);
            when(videos.list(anyString())).thenReturn(list);
        } catch (IOException e) {
            fail("Exception thrown while creating mock");
        }

        return videos;
    }

    private YouTube.Videos.List createMockVideosList(int numBefore, int numAfter, int numInRange,  DateTime after, DateTime before) {
        YouTube.Videos.List list = mock(YouTube.Videos.List.class);

        when(list.setMaxResults(anyLong())).thenReturn(list);
        when(list.setPageToken(anyString())).thenReturn(list);
        when(list.setId(anyString())).thenReturn(list);
        when(list.setKey(anyString())).thenReturn(list);

        VideoListResponseAnswer answer = new VideoListResponseAnswer(numBefore, numAfter, numInRange, after, before);
        try {
            doAnswer(answer).when(list).execute();
        } catch (IOException ioe) {
            fail("Should not have thrown exception while creating mock. : "+ioe.getMessage());
        }

        return list;
    }

    private static VideoListResponse createMockVideoListResponse(int numBefore, int numAfter, int numInRange,  DateTime after, DateTime before, boolean page) {
        VideoListResponse feed = new VideoListResponse();
        List<Video> list = com.google.common.collect.Lists.newLinkedList();

        for(int i=0; i < numAfter; ++i) {
            com.google.api.client.util.DateTime published = new com.google.api.client.util.DateTime(after.getMillis() + 1000000);
            Video video = new Video();
            video.setSnippet(new VideoSnippet());
            video.getSnippet().setPublishedAt(published);
            list.add(video);
        }
        for(int i=0; i < numInRange; ++i) {
            DateTime published = null;
            if((before == null && after == null) || before == null) {
                published = DateTime.now(); // no date range or end time date range so just make the time now.
            } else if(after == null) {
                published = before.minusMillis(100000); //no beginning to range
            } else { // has to be in range
                long range = before.getMillis() - after.getMillis();
                published = after.plus(range / 2); //in the middle
            }
            com.google.api.client.util.DateTime gPublished = new com.google.api.client.util.DateTime(published.getMillis());
            Video video = new Video();
            video.setSnippet(new VideoSnippet());
            video.getSnippet().setPublishedAt(gPublished);
            video.getSnippet().setTitle(IN_RANGE_IDENTIFIER);
            list.add(video);
        }
        for(int i=0; i < numBefore; ++i) {
            com.google.api.client.util.DateTime published = new com.google.api.client.util.DateTime((after.minusMillis(100000)).getMillis());
            Video video = new Video();
            video.setSnippet(new VideoSnippet());
            video.getSnippet().setPublishedAt(published);
            list.add(video);
        }
        if(page) {
            feed.setNextPageToken("A");
        } else {
            feed.setNextPageToken(null);
        }

        feed.setItems(list);

        return feed;
    }

    private static class VideoListResponseAnswer implements Answer<VideoListResponse> {
        private int afterCount = 0;
        private int beforeCount = 0;
        private int inCount = 0;
        private int maxBatch = 100;

        private int numAfter;
        private int numInRange;
        private int numBefore;
        private DateTime after;
        private DateTime before;

        private VideoListResponseAnswer(int numBefore, int numAfter, int numInRange, DateTime after, DateTime before) {
            this.numBefore = numBefore;
            this.numAfter = numAfter;
            this.numInRange = numInRange;
            this.after = after;
            this.before = before;
        }

        @Override
        public VideoListResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
            int totalCount = 0;
            int batchAfter = 0;
            int batchBefore = 0;
            int batchIn = 0;
            inCount = 0;
            afterCount = 0;
            beforeCount = 0;

            if(afterCount != numAfter) {
                if(numAfter - afterCount >= maxBatch) {
                    afterCount += maxBatch;
                    batchAfter += maxBatch;
                    totalCount += batchAfter;
                } else {
                    batchAfter += numAfter - afterCount;
                    totalCount += numAfter - afterCount;
                    afterCount = numAfter;
                }
            }
            if(totalCount < maxBatch && inCount != numInRange) {
                if(numInRange - inCount >= maxBatch - totalCount) {
                    inCount += maxBatch - totalCount;
                    batchIn += maxBatch - totalCount;
                    totalCount += batchIn;
                } else {
                    batchIn += numInRange - inCount;
                    totalCount += numInRange - inCount;
                    inCount = numInRange;
                }
            }
            if(totalCount < maxBatch && beforeCount != numBefore) {
                if(numBefore - batchBefore >= maxBatch - totalCount) {
                    batchBefore += maxBatch - totalCount;
                    totalCount = maxBatch;
                    beforeCount +=batchBefore;
                } else {
                    batchBefore += numBefore - beforeCount;
                    totalCount += numBefore - beforeCount;
                    beforeCount = numBefore;
                }
            }

            return createMockVideoListResponse(batchBefore, batchAfter, batchIn, after, before, numAfter != afterCount || inCount != numInRange || beforeCount != numBefore);
        }
    }
}