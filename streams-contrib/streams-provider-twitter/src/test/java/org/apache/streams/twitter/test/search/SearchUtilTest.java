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

package org.apache.streams.twitter.test.search;

import org.apache.streams.twitter.search.SearchUtil;
import org.apache.streams.twitter.search.ThirtyDaySearchOperator;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.testng.collections.Lists;

public class SearchUtilTest {

  @Test
  public void test1() throws Exception {
    ThirtyDaySearchOperator operator = new ThirtyDaySearchOperator()
      .withBios(Lists.newArrayList(
        "steve",
        "matthew",
        "ate",
        "suneel"
      ));
    String query = SearchUtil.toString(operator);
    Assert.assertEquals( 4, StringUtils.countMatches(query, "bio:"));
    Assert.assertTrue( query.contains("steve"));
    Assert.assertTrue( query.contains("suneel"));
  }

  @Test
  public void test2() throws Exception {
    ThirtyDaySearchOperator operator = new ThirtyDaySearchOperator()
      .withBioLocations(Lists.newArrayList(
        "New York City",
        "Austin",
        "San Francisco"
      ));
    String query = SearchUtil.toString(operator);
    Assert.assertEquals( 2, StringUtils.countMatches(query, "bio_location:\""));
    Assert.assertTrue( query.contains("New York City"));
    Assert.assertTrue( query.contains("Austin"));
    Assert.assertTrue( query.contains("San Francisco"));
  }

  @Test
  public void test3() throws Exception {
    ThirtyDaySearchOperator operator = new ThirtyDaySearchOperator()
      .withKeywords(Lists.newArrayList("rock"))
      .withAnds(Lists.newArrayList(new ThirtyDaySearchOperator()
        .withKeywords(Lists.newArrayList("roll"))));
    String query = SearchUtil.toString(operator);
    Assert.assertEquals( "( rock AND ( roll ) )", query );
  }

  @Test
  public void test4() throws Exception {
    ThirtyDaySearchOperator operator = new ThirtyDaySearchOperator()
      .withKeywords(Lists.newArrayList("a"))
      .withAnds(Lists.newArrayList(
        new ThirtyDaySearchOperator()
          .withKeywords(Lists.newArrayList("b"))
          .withOrs(Lists.newArrayList(
            new ThirtyDaySearchOperator()
              .withKeywords(Lists.newArrayList("c")))
          )
      ))
      .withOrs(Lists.newArrayList(
        new ThirtyDaySearchOperator()
          .withKeywords(Lists.newArrayList("d"))
          .withAnds(Lists.newArrayList(
            new ThirtyDaySearchOperator()
              .withKeywords(Lists.newArrayList("e"))
          )
        )
      ))
      .withNot(true);
    String query = SearchUtil.toString(operator);
    Assert.assertEquals( "- ( a AND ( b OR ( c ) ) OR ( d AND ( e ) ) )", query);
  }

}
