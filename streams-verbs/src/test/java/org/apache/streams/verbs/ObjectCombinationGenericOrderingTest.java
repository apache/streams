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

import org.junit.Test;

/**
 * Orders ObjectCombinations from most specific to most general, without regard
 * for degree of match to any specific Activity.
 */
public class ObjectCombinationGenericOrderingTest  {

  @Test
  public void compareWildcardCountTest() {
    ObjectCombination combination1 = new ObjectCombination();
    ObjectCombination combination2 = new ObjectCombination().withActor("actor");
    assert (new ObjectCombinationGenericOrdering()).compare(combination1, combination2) > 0;
    ObjectCombination combination3 = new ObjectCombination();
    ObjectCombination combination4 = new ObjectCombination().withProvider("provider");
    assert (new ObjectCombinationGenericOrdering()).compare(combination3, combination4) > 0;
  }


}
