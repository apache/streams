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

package com.google.gplus.provider;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;
import com.google.common.collect.Lists;
import com.google.gplus.serializer.util.GPlusActivityDeserializer;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.ConstantTimeBackOffStrategy;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link com.google.gplus.provider.GPlusUserActivityCollector}
 */
public class TestGPlusUserActivityCollector extends RandomizedTest {


    private static final String ACTIVITY_TEMPLATE = "{ \"kind\": \"plus#activity\", \"etag\": \"\\\"Vea_b94Y77GDGgRK7gFNPnolKQw/v1-6aVSBGT4qiStMoz7f2_AN2fM\\\"\", \"title\": \"\", \"published\": \"%s\", \"updated\": \"2014-10-27T06:26:33.927Z\", \"id\": \"z13twrlznpvtzz52w22mdt1y0k3of1djw04\", \"url\": \"https://plus.google.com/116771159471120611293/posts/GR7CGR8N5VL\", \"actor\": { \"id\": \"116771159471120611293\", \"displayName\": \"displayName\", \"url\": \"https://plus.google.com/116771159471120611293\", \"image\": { \"url\": \"https://lh6.googleusercontent.com/-C0fiZBxdvw0/AAAAAAAAAAI/AAAAAAAAJ5k/K4pgR3_-_ms/photo.jpg?sz=50\" } }, \"verb\": \"share\", \"object\": { \"objectType\": \"activity\", \"id\": \"z13zgvtiurjgfti1v234iflghvq2c1dge04\", \"actor\": { \"id\": \"104954254300557350002\", \"displayName\": \"displayName\", \"url\": \"https://plus.google.com/104954254300557350002\", \"image\": { \"url\": \"https://lh4.googleusercontent.com/-SO1scj4p2LA/AAAAAAAAAAI/AAAAAAAAI-s/efA9LBVe144/photo.jpg?sz=50\" } }, \"content\": \"\", \"url\": \"https://plus.google.com/104954254300557350002/posts/AwewXhtn7ws\", \"replies\": { \"totalItems\": 0, \"selfLink\": \"https://content.googleapis.com/plus/v1/activities/z13twrlznpvtzz52w22mdt1y0k3of1djw04/comments\" }, \"plusoners\": { \"totalItems\": 9, \"selfLink\": \"https://content.googleapis.com/plus/v1/activities/z13twrlznpvtzz52w22mdt1y0k3of1djw04/people/plusoners\" }, \"resharers\": { \"totalItems\": 0, \"selfLink\": \"https://content.googleapis.com/plus/v1/activities/z13twrlznpvtzz52w22mdt1y0k3of1djw04/people/resharers\" }, \"attachments\": [ { \"objectType\": \"photo\", \"id\": \"104954254300557350002.6074732746360957410\", \"content\": \"26/10/2014 - 1\", \"url\": \"https://plus.google.com/photos/104954254300557350002/albums/6074732747132702225/6074732746360957410\", \"image\": { \"url\": \"https://lh4.googleusercontent.com/-oO3fnARlDm0/VE3JP1xHKeI/AAAAAAAAeCY/-X2jzc6HruA/w506-h750/2014%2B-%2B1\", \"type\": \"image/jpeg\" }, \"fullImage\": { \"url\": \"https://lh4.googleusercontent.com/-oO3fnARlDm0/VE3JP1xHKeI/AAAAAAAAeCY/-X2jzc6HruA/w600-h1141/2014%2B-%2B1\", \"type\": \"image/jpeg\", \"height\": 1141, \"width\": 600 } } ] }, \"annotation\": \"Truth 😜\", \"provider\": { \"title\": \"Reshared Post\" }, \"access\": { \"kind\": \"plus#acl\", \"description\": \"Public\", \"items\": [ { \"type\": \"public\" } ] } }";
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();
    private static final String IN_RANGE_IDENTIFIER = "data in range";


    static {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Activity.class, new GPlusActivityDeserializer());
        MAPPER.registerModule(simpleModule);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Creates a randomized activity and randomized date range.
     * The activity feed is separated into three chunks,
     * |. . . data too recent to be in date range . . .||. . . data in date range. . .||. . . data too old to be in date range|
     * [index 0, ............................................................................................., index length-1]
     * Inside of those chunks data has no order, but the list is ordered by those three chunks.
     *
     * The test will check to see if the num of data in the date range make onto the output queue.
     */
    @Test
    @Repeat(iterations = 3)
    public void testWithBeforeAndAfterDates() throws InterruptedException {
        //initialize counts assuming no date ranges will be used
        int numActivities = randomIntBetween(0, 1000);
        int numActivitiesInDateRange = numActivities;
        int numberOutOfRange = 0;
        int numBerforeRange = 0;
        int numAfterRange = 0;
        //determine if date ranges will be used
        DateTime beforeDate = null;
        DateTime afterDate = null;
        if(randomInt() % 2 == 0) {
            beforeDate = DateTime.now().minusDays(randomIntBetween(1,5));
        }
        if(randomInt() % 2 == 0) {
            if(beforeDate == null) {
                afterDate = DateTime.now().minusDays(randomIntBetween(1, 10));
            } else {
                afterDate = beforeDate.minusDays(randomIntBetween(1, 10));
            }
        }
        //update counts if date ranges are going to be used.
        if(beforeDate != null || afterDate != null ) { //assign amount to be in range
            numActivitiesInDateRange = randomIntBetween(0, numActivities);
            numberOutOfRange = numActivities - numActivitiesInDateRange;
        }
        if(beforeDate == null && afterDate != null) { //assign all out of range to be before the start of the range
            numBerforeRange = numberOutOfRange;
        } else if(beforeDate != null && afterDate == null) { //assign all out of range to be after the start of the range
            numAfterRange = numberOutOfRange;
        } else if(beforeDate != null && afterDate != null) { //assign half before range and half after the range
            numAfterRange = (numberOutOfRange / 2) + (numberOutOfRange % 2);
            numBerforeRange = numberOutOfRange / 2;
        }

        Plus plus = createMockPlus(numBerforeRange, numAfterRange, numActivitiesInDateRange, afterDate, beforeDate);
        BackOffStrategy strategy = new ConstantTimeBackOffStrategy(1);
        BlockingQueue<StreamsDatum> datums = new LinkedBlockingQueue<>();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId("A");
        userInfo.setAfterDate(afterDate);
        userInfo.setBeforeDate(beforeDate);
        GPlusUserActivityCollector collector = new GPlusUserActivityCollector(plus, datums, strategy, userInfo);
        collector.run();

        assertEquals(numActivitiesInDateRange, datums.size());
        while(!datums.isEmpty()) {
            StreamsDatum datum = datums.take();
            assertNotNull(datum);
            assertNotNull(datum.getDocument());
            assertTrue(datum.getDocument() instanceof String);
            assertTrue(((String)datum.getDocument()).contains(IN_RANGE_IDENTIFIER)); //only in range documents are on the out going queue.
        }
    }


    private Plus createMockPlus(final int numBefore, final int numAfter, final int numInRange, final DateTime after, final DateTime before) {
        Plus plus = mock(Plus.class);
        final Plus.Activities activities = createMockPlusActivities(numBefore, numAfter, numInRange, after, before);
        doAnswer(new Answer() {
            @Override
            public Plus.Activities answer(InvocationOnMock invocationOnMock) throws Throwable {
                return activities;
            }
        }).when(plus).activities();
        return plus;
    }

    private Plus.Activities createMockPlusActivities(final int numBefore, final int numAfter, final int numInRange, final DateTime after, final DateTime before) {
        Plus.Activities activities = mock(Plus.Activities.class);
        try {
            Plus.Activities.List list = createMockPlusActivitiesList(numBefore, numAfter, numInRange, after, before);
            when(activities.list(anyString(), anyString())).thenReturn(list);
        } catch (IOException ioe) {
            fail("Should not have thrown exception while creating mock. : "+ioe.getMessage());
        }
        return activities;
    }

    private Plus.Activities.List createMockPlusActivitiesList(final int numBefore, final int numAfter, final int numInRange, final DateTime after, final DateTime before) {
        Plus.Activities.List list = mock(Plus.Activities.List.class);
        when(list.setMaxResults(anyLong())).thenReturn(list);
        when(list.setPageToken(anyString())).thenReturn(list);
        ActivityFeedAnswer answer = new ActivityFeedAnswer(numBefore, numAfter, numInRange, after, before);
        try {
            doAnswer(answer).when(list).execute();
        } catch (IOException ioe) {
            fail("Should not have thrown exception while creating mock. : "+ioe.getMessage());
        }
        return list;
    }


    private static ActivityFeed createMockActivityFeed(int numBefore, int numAfter, int numInRange,  DateTime after, DateTime before, boolean page) {
        ActivityFeed feed = new ActivityFeed();
        List<Activity> list = Lists.newLinkedList();
        for(int i=0; i < numAfter; ++i) {
            DateTime published = before.plus(randomIntBetween(0, Integer.MAX_VALUE));
            Activity activity = createActivityWithPublishedDate(published);
            list.add(activity);
        }
        for(int i=0; i < numInRange; ++i) {
            DateTime published = null;
            if((before == null && after == null) || before == null) {
                published = DateTime.now(); // no date range or end time date range so just make the time now.
            } else if(after == null) {
                published = before.minusMillis(randomIntBetween(1, Integer.MAX_VALUE)); //no beginning to range
            } else { // has to be in range
                long range = before.getMillis() - after.getMillis();
                published = after.plus(range / 2); //in the middle
            }
            Activity activity = createActivityWithPublishedDate(published);
            activity.setTitle(IN_RANGE_IDENTIFIER);
            list.add(activity);
        }
        for(int i=0; i < numBefore; ++i) {
            DateTime published = after.minusMillis(randomIntBetween(1, Integer.MAX_VALUE));
            Activity activity = createActivityWithPublishedDate(published);
            list.add(activity);
        }
        if(page) {
            feed.setNextPageToken("A");
        } else {
            feed.setNextPageToken(null);
        }
        feed.setItems(list);
        return feed;
    }

    private static Activity createActivityWithPublishedDate(DateTime dateTime) {
        Activity activity = new Activity();
        activity.setPublished(new com.google.api.client.util.DateTime(dateTime.getMillis()));
        activity.setId("a");
        return activity;
    }

    private static class ActivityFeedAnswer implements Answer<ActivityFeed> {
        private int afterCount = 0;
        private int beforeCount = 0;
        private int inCount = 0;
        private int maxBatch = 100;

        private int numAfter;
        private int numInRange;
        private int numBefore;
        private DateTime after;
        private DateTime before;

        private ActivityFeedAnswer(int numBefore, int numAfter, int numInRange, DateTime after, DateTime before) {
            this.numBefore = numBefore;
            this.numAfter = numAfter;
            this.numInRange = numInRange;
            this.after = after;
            this.before = before;
        }




        @Override
        public ActivityFeed answer(InvocationOnMock invocationOnMock) throws Throwable {
            int totalCount = 0;
            int batchAfter = 0;
            int batchBefore = 0;
            int batchIn = 0;
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

            return createMockActivityFeed(batchBefore, batchAfter, batchIn, after, before, numAfter != afterCount || inCount != numInRange || beforeCount != numBefore);
        }
    }




}
