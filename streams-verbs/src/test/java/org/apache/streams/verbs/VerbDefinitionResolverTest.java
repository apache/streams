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

package org.apache.streams.verbs;

import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests for {$link: org.apache.streams.verbs.VerbDefinitionResolver}.
 */
public class VerbDefinitionResolverTest {

  ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  /**
   * Test of matchingVerbDefinitions.
   */
  @Test
  public void testMatchingVerbDefinitions() throws Exception {
    VerbDefinition definition = mapper.readValue(VerbDefinitionResolverTest.class.getResourceAsStream("/post.json"), VerbDefinition.class);
    VerbDefinitionResolver resolver = new VerbDefinitionResolver(Stream.of(definition).collect(Collectors.toSet()));
    Activity activity0 = mapper.readValue("{\"id\":\"1\",\"verb\":\"notpost\"}\n", Activity.class);
    Set<VerbDefinition> result0 = resolver.matchingVerbDefinitions(activity0);
    assert result0.size() == 0;
    Activity activity1 = mapper.readValue("{\"id\":\"1\",\"verb\":\"post\"}\n", Activity.class);
    Set<VerbDefinition> result1 = resolver.matchingVerbDefinitions(activity1);
    assert result1.size() == 1;
    assert result1.contains(definition);
    Activity activity2 = mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"product\"}}\n", Activity.class);
    Set<VerbDefinition> result2 = resolver.matchingVerbDefinitions(activity2);
    assert result2.size() == 1;
    assert result2.contains(definition);
    Activity activity3 = mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"application\"}}\n\n", Activity.class);
    Set<VerbDefinition> result3 = resolver.matchingVerbDefinitions(activity3);
    assert result3.size() == 1;
    assert result3.contains(definition);
    Activity activity4 = mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"object\":{\"id\":\"objectId\",\"objectType\":\"task\"}}\n", Activity.class);
    Set<VerbDefinition> result4 = resolver.matchingVerbDefinitions(activity4);
    assert result4.size() == 1;
    assert result4.contains(definition);
    Activity activity5 = mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"target\":{\"id\":\"targetId\",\"objectType\":\"group\"}}\n", Activity.class);
    Set<VerbDefinition> result5 = resolver.matchingVerbDefinitions(activity5);
    assert result5.size() == 1;
    assert result5.contains(definition);
    Activity activity6 = mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"page\"}}\n", Activity.class);
    Set<VerbDefinition> result6 = resolver.matchingVerbDefinitions(activity6);
    assert result6.size() == 1;
    assert result6.contains(definition);
  }

  /**
   * Test of matchingObjectCombinations.
   */
  @Test
  public void testMatchingObjectCombinations() throws Exception {
    VerbDefinition provider = mapper.readValue(VerbDefinitionResolverTest.class.getResourceAsStream("/provider.json"), VerbDefinition.class);
    VerbDefinition actor = mapper.readValue(VerbDefinitionResolverTest.class.getResourceAsStream("/actor.json"), VerbDefinition.class);
    VerbDefinition object = mapper.readValue(VerbDefinitionResolverTest.class.getResourceAsStream("/object.json"), VerbDefinition.class);
    VerbDefinition post = mapper.readValue(VerbDefinitionResolverTest.class.getResourceAsStream("/post.json"), VerbDefinition.class);
    VerbDefinition follow = mapper.readValue(VerbDefinitionResolverTest.class.getResourceAsStream("/follow.json"), VerbDefinition.class);
    VerbDefinitionResolver resolver = new VerbDefinitionResolver(Stream.of(provider, actor, object, post, follow).collect(Collectors.toSet()));
    Activity activity0 = mapper.readValue("{\"id\":\"1\",\"verb\":\"notpost\"}\n", Activity.class);
    List<ObjectCombination> result0 = resolver.matchingObjectCombinations(activity0);
    assert result0.size() == 0;
    Activity activity1 = mapper.readValue("{\"id\":\"1\",\"verb\":\"post\"}\n", Activity.class);
    List<ObjectCombination> result1 = resolver.matchingObjectCombinations(activity1);
    assert result1.size() == 4;
    Activity activity2 = mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"product\"}}\n", Activity.class);
    List<ObjectCombination> result2 = resolver.matchingObjectCombinations(activity2);
    assert result2.size() == 3;
    Activity activity3 = mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"provider\":{\"id\":\"providerId\",\"objectType\":\"application\"}}\n", Activity.class);
    List<ObjectCombination> result3 = resolver.matchingObjectCombinations(activity3);
    assert result3.size() == 4;
    assert provider.getObjects().get(0).equals(result3.get(0));
    Activity activity4 = mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"object\":{\"id\":\"objectId\",\"objectType\":\"task\"}}\n", Activity.class);
    List<ObjectCombination> result4 = resolver.matchingObjectCombinations(activity4);
    assert result4.size() == 4;
    assert object.getObjects().get(0).equals(result4.get(0));
    Activity activity5 = mapper.readValue("{\"id\":\"id\",\"verb\":\"post\",\"target\":{\"id\":\"targetId\",\"objectType\":\"group\"}}\n", Activity.class);
    List<ObjectCombination> result5 = resolver.matchingObjectCombinations(activity5);
    assert result5.size() == 4;
    Activity activity6 = mapper.readValue("{\"id\":\"1\",\"verb\":\"post\",\"actor\":{\"id\":\"actorId\",\"objectType\":\"person\"}}\n", Activity.class);
    List<ObjectCombination> result6 = resolver.matchingObjectCombinations(activity6);
    assert result6.size() == 4;
    assert actor.getObjects().get(0).equals(result6.get(0));
  }
}