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
package org.apache.streams.facebook.provider.page;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import facebook4j.Page;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.FacebookOAuthConfiguration;
import org.apache.streams.facebook.IdConfig;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests For {@link org.apache.streams.facebook.provider.FacebookProvider}
 */
public class TestFacebookPageProvider {
    final int DEFAULT_WAIT_PERIOD = 5000; //5 seconds in milliseconds

    @Test
    public void testPageProviderCollector() {
        FacebookPageProvider facebookPageProvider = new FacebookPageProvider(new FacebookConfiguration());
        assertEquals(facebookPageProvider.getDataCollector().getClass(), FacebookPageDataCollector.class);
    }

    @Test
    public void testPageProviderEmpty() throws Exception {
        Map<IdConfig, Page> pageMap = createPageMap();

        testPageProvider(pageMap);
    }

    @Test
    public void testPageProviderSingleDocument() throws Exception {
        Map<IdConfig, Page> pageMap = createPageMap("fake_id_1");

        testPageProvider(pageMap);
    }

    @Test
    public void testPageProviderMultipleDocuments() throws Exception {
        Map<IdConfig, Page> pageMap = createPageMap("fake_id_1", "fake_id_2", "fake_id_3", "fake_id_4", "fake_id_5");

        testPageProvider(pageMap);
    }

    /**
     * Given a map of Mocked IdConfig->Page combinations, run the FacebookPageProvider end to end
     * and assert that correct behavior is occurring
     *
     * @param pageMap
     * @throws Exception
     */
    private void testPageProvider(Map<IdConfig, Page> pageMap) throws Exception {
        FacebookConfiguration facebookConfiguration = createFacebookConfiguration(pageMap);

        FacebookPageProvider facebookPageProvider = spy(new FacebookPageProvider(facebookConfiguration));
        facebookPageProvider.prepare(null);

        FacebookPageDataCollector dataCollector = createDataCollector(facebookPageProvider.getQueue(), facebookConfiguration, pageMap);

        doReturn(dataCollector).when(facebookPageProvider).getDataCollector();

        assertFalse(dataCollector.isComplete());

        facebookPageProvider.startStream();

        Thread.sleep(DEFAULT_WAIT_PERIOD);
        StreamsResultSet streamsDatums = facebookPageProvider.readCurrent();

        assertEquals(pageMap.size(), streamsDatums.size());
        assertTrue(containsExpectedDocuments(pageMap, streamsDatums));

        assertTrue(dataCollector.isComplete());
    }

    /**
     * Iterates through both the pageMap and returned datums from the provider
     * and determines whether or not all expected documents are present
     *
     * @param pageMap
     * @param streamsDatums
     * @return
     */
    private boolean containsExpectedDocuments(Map<IdConfig, Page> pageMap, StreamsResultSet streamsDatums) {
        boolean containsAll = true;

        List<String> pageMapIds = Lists.newArrayList();
        for(IdConfig pageMapId : pageMap.keySet()) {
            pageMapIds.add(pageMapId.getId());
        }

        for(StreamsDatum datum : streamsDatums.getQueue()) {
            if(!pageMapIds.contains(datum.getId())) {
                containsAll = false;
                break;
            }

            pageMapIds.remove(datum.getId());
        }

        return containsAll;
    }

    /**
     * Creates a Mocked FacebookConfiguration using the IDs declared in the passed
     * in pageMap
     *
     * @param pageMap
     * @return
     */
    private FacebookConfiguration createFacebookConfiguration(Map<IdConfig, Page> pageMap) {
        FacebookConfiguration facebookConfiguration = new FacebookConfiguration();
        facebookConfiguration.setIds(Sets.newHashSet(pageMap.keySet()));
        facebookConfiguration.setOauth(new FacebookOAuthConfiguration());

        return facebookConfiguration;
    }

    /**
     * Given an indeterminately sized list of IDs, create a Map containing
     * Mocked Pages to be used in the tests
     *
     * @param ids
     * @return
     */
    private Map<IdConfig, Page> createPageMap(String... ids) {
        Map<IdConfig, Page> map = Maps.newHashMap();

        for(String id : ids) {
            IdConfig idConfig = new IdConfig()
                    .withBeforeDate(new DateTime())
                    .withId(id);
            map.put(idConfig, new TestPage(id, id + " Name"));
        }

        return map;
    }

    /**
     * Given a BlockingQueue, Mocked FacebookConfiguration and Map containing IdConfig objects and Mocked Pages,
     * create a FacebookPageDataCollector which outputs those pages to the passed in Queue
     *
     * @param queue
     * @param facebookConfiguration
     * @param pages
     * @return
     */
    private FacebookPageDataCollector createDataCollector(BlockingQueue<StreamsDatum> queue, FacebookConfiguration facebookConfiguration, Map<IdConfig, Page> pages) {
        final Map<IdConfig, Page> finalPages = pages;

        FacebookPageDataCollector dataCollector = new FacebookPageDataCollector(queue, facebookConfiguration) {
            @Override
            protected void getData(IdConfig id) throws Exception {
                super.outputData(finalPages.get(id), finalPages.get(id).getId());
            }
        };

        return dataCollector;
    }
}