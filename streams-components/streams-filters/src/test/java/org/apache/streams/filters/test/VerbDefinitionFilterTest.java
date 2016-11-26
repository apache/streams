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

package org.apache.streams.filters.test;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.filters.VerbDefinitionDropFilter;
import org.apache.streams.filters.VerbDefinitionKeepFilter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.verbs.VerbDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.List;

/**
 * Tests for {$link: org.apache.streams.verbs.VerbDefinitionResolver}
 */
public class VerbDefinitionFilterTest {

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    /**
     * Test verb match filter alone
     */
    @Test
    public void testVerbMatchFilter() throws Exception {
        VerbDefinition definition = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/post.json"), VerbDefinition.class);
        VerbDefinitionKeepFilter filter = new VerbDefinitionKeepFilter(Sets.newHashSet(definition));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"notpost\"}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 0;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\"}\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 1;
    }

    /**
     * Test provider filter, if provider has wrong type it should not pass
     */
    @Test
    public void testProviderFilter() throws Exception {
        VerbDefinition definition = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/provider.json"), VerbDefinition.class);
        VerbDefinitionKeepFilter filter = new VerbDefinitionKeepFilter(Sets.newHashSet(definition));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"product\"}}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 0;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"application\"}}\n\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 1;
    }

    /**
     * Test actor filter, if actor isn't present it should not pass
     */
    @Test
    public void testActorFilter() throws Exception {
        VerbDefinition definition = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/actor.json"), VerbDefinition.class);
        VerbDefinitionKeepFilter filter = new VerbDefinitionKeepFilter(Sets.newHashSet(definition));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"page\"}}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 0;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"person\"}}}\n\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 1;

    }

    /**
     * Test object filter, if object doesn't have a type it should not pass
     */
    @Test
    public void testObjectFilter() throws Exception {
        VerbDefinition definition = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/object.json"), VerbDefinition.class);
        VerbDefinitionKeepFilter filter = new VerbDefinitionKeepFilter(Sets.newHashSet(definition));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"object\":{\"id\":\"objectId\"}}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 0;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"object\":{\"id\":\"objectId\",\"objectType\":\"task\"}}}\n\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 1;
    }

    /**
     * Test actor and object filter together
     */
    @Test
    public void testMultiFilter() throws Exception {
        VerbDefinition definition = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/follow.json"), VerbDefinition.class);
        VerbDefinitionKeepFilter filter = new VerbDefinitionKeepFilter(Sets.newHashSet(definition));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"follow\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"page\"}}}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 0;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"2\",\"verb\":\"follow\",\"object\":{\"id\":\"objectId\",\"objectType\":\"page\"}}}\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 0;
        StreamsDatum datum3 = new StreamsDatum(mapper.readValue("{\"id\":\"3\",\"verb\":\"follow\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"page\"},\"object\":{\"id\":\"objectId\",\"objectType\":\"page\"}}}\n", Activity.class));
        List<StreamsDatum> result3 = filter.process(datum3);
        assert result3.size() == 1;

    }

    /**
     * Test targetRequired
     */
    @Test
    public void testTargetRequired() throws Exception {
        VerbDefinition definition = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/targetrequired.json"), VerbDefinition.class);
        VerbDefinitionKeepFilter filter = new VerbDefinitionKeepFilter(Sets.newHashSet(definition));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"object\":{\"id\":\"objectId\",\"objectType\":\"task\"}}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 0;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"target\":{\"id\":\"targetId\",\"objectType\":\"group\"}}\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 1;
    }

    /**
     * Test that wildcard verb definition matches every item
     */
    @Test
    public void testAllWildcard() throws Exception {
        VerbDefinition definition = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/post.json"), VerbDefinition.class);
        VerbDefinitionKeepFilter filter = new VerbDefinitionKeepFilter(Sets.newHashSet(definition));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"notpost\"}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 0;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\"}\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 1;
        StreamsDatum datum3 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"product\"}}\n", Activity.class));
        List<StreamsDatum> result3 = filter.process(datum3);
        assert result3.size() == 1;
        StreamsDatum datum4 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"application\"}}\n\n", Activity.class));
        List<StreamsDatum> result4 = filter.process(datum4);
        assert result4.size() == 1;
        StreamsDatum datum5 = new StreamsDatum(mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"object\":{\"id\":\"objectId\",\"objectType\":\"task\"}}\n", Activity.class));
        List<StreamsDatum> result5 = filter.process(datum5);
        assert result5.size() == 1;
        StreamsDatum datum6 = new StreamsDatum(mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"target\":{\"id\":\"targetId\",\"objectType\":\"group\"}}\n", Activity.class));
        List<StreamsDatum> result6 = filter.process(datum6);
        assert result6.size() == 1;
        StreamsDatum datum7 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"page\"}}\n", Activity.class));
        List<StreamsDatum> result7 = filter.process(datum7);
        assert result7.size() == 1;
    }

    /**
     * Test that multiple verb definitions chain properly
     */
    @Test
    public void testAllMultipleDefinition() throws Exception {
        VerbDefinition provider = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/provider.json"), VerbDefinition.class);
        VerbDefinition actor = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/actor.json"), VerbDefinition.class);
        VerbDefinition object = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/object.json"), VerbDefinition.class);
        VerbDefinition target = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/targetrequired.json"), VerbDefinition.class);
        VerbDefinition follow = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/follow.json"), VerbDefinition.class);
        VerbDefinitionKeepFilter filter = new VerbDefinitionKeepFilter(Sets.newHashSet(provider,actor,object,target,follow));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"notpost\"}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 0;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\"}\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 1;
        StreamsDatum datum3 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"product\"}}\n", Activity.class));
        List<StreamsDatum> result3 = filter.process(datum3);
        assert result3.size() == 1;
        StreamsDatum datum4 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"application\"}}\n\n", Activity.class));
        List<StreamsDatum> result4 = filter.process(datum4);
        assert result4.size() == 1;
        StreamsDatum datum5 = new StreamsDatum(mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"object\":{\"id\":\"objectId\",\"objectType\":\"task\"}}\n", Activity.class));
        List<StreamsDatum> result5 = filter.process(datum5);
        assert result5.size() == 1;
        StreamsDatum datum6 = new StreamsDatum(mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"target\":{\"id\":\"targetId\",\"objectType\":\"group\"}}\n", Activity.class));
        List<StreamsDatum> result6 = filter.process(datum6);
        assert result6.size() == 1;
        StreamsDatum datum7 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"page\"}}\n", Activity.class));
        List<StreamsDatum> result7 = filter.process(datum7);
        assert result7.size() == 1;
        StreamsDatum datum9 = new StreamsDatum(mapper.readValue("{\"id\":\"3\",\"verb\":\"follow\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"page\"},\"object\":{\"id\":\"objectId\",\"objectType\":\"page\"}}}\n", Activity.class));
        List<StreamsDatum> result9 = filter.process(datum9);
        assert result9.size() == 1;
    }

    /**
     * Test verb drop filter alone
     */
    @Test
    public void testVerbDropFilter() throws Exception {
        VerbDefinition definition = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/post.json"), VerbDefinition.class);
        VerbDefinitionDropFilter filter = new VerbDefinitionDropFilter(Sets.newHashSet(definition));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"notpost\"}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 1;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\"}\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 0;
    }

    /**
     * Test that wildcard verb definition will drop every item
     */
    @Test
    public void testDropAllWildcard() throws Exception {
        VerbDefinition definition = mapper.readValue(VerbDefinitionFilterTest.class.getResourceAsStream("/post.json"), VerbDefinition.class);
        VerbDefinitionDropFilter filter = new VerbDefinitionDropFilter(Sets.newHashSet(definition));
        filter.prepare(null);
        StreamsDatum datum1 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"notpost\"}\n", Activity.class));
        List<StreamsDatum> result1 = filter.process(datum1);
        assert result1.size() == 1;
        StreamsDatum datum2 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\"}\n", Activity.class));
        List<StreamsDatum> result2 = filter.process(datum2);
        assert result2.size() == 0;
        StreamsDatum datum3 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"product\"}}\n", Activity.class));
        List<StreamsDatum> result3 = filter.process(datum3);
        assert result3.size() == 0;
        StreamsDatum datum4 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"application\"}}\n\n", Activity.class));
        List<StreamsDatum> result4 = filter.process(datum4);
        assert result4.size() == 0;
        StreamsDatum datum5 = new StreamsDatum(mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"object\":{\"id\":\"objectId\",\"objectType\":\"task\"}}\n", Activity.class));
        List<StreamsDatum> result5 = filter.process(datum5);
        assert result5.size() == 0;
        StreamsDatum datum6 = new StreamsDatum(mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"target\":{\"id\":\"targetId\",\"objectType\":\"group\"}}\n", Activity.class));
        List<StreamsDatum> result6 = filter.process(datum6);
        assert result6.size() == 0;
        StreamsDatum datum7 = new StreamsDatum(mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"page\"}}\n", Activity.class));
        List<StreamsDatum> result7 = filter.process(datum7);
        assert result7.size() == 0;
    }

}