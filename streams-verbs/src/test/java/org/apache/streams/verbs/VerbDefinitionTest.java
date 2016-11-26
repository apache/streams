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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

/**
 * Unit tests for VerbDefinition and utils.
 */
public class VerbDefinitionTest {

  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Test read verb definition from json.
   */
  @Test
  public void testReadVerbDefinitionJson() throws Exception {

    VerbDefinition definition = mapper.readValue(VerbDefinitionTest.class.getResourceAsStream("/do.json"), VerbDefinition.class);

    assert definition != null;
    assert definition.getObjectType().equals("verb");
    assert definition.getObjects().size() == 1;
    assert definition.getObjects().get(0).getActor().equals("*");
    assert definition.getObjects().get(0).getObject().equals("*");
    assert definition.getObjects().get(0).getTarget().equals("*");
    assert definition.getObjects().get(0).getProvider().equals("*");
    assert definition.getObjects().get(0).getTemplates().getAdditionalProperties().size() == 1;
  }

  /**
   * Test verb definition defaults are set.
   */
  @Test
  public void testObjectCombinationDefaults() throws Exception {

    ObjectCombination combination = new ObjectCombination();

    assert combination.getActor().equals("*");
    assert combination.getObject().equals("*");
    assert combination.getTarget().equals("*");
    assert combination.getProvider().equals("*");
    assert !combination.getTargetRequired();

  }

}
