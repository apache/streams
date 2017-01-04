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

package org.apache.streams.regex;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RegexMentionExtractorTest {

    private Activity activity;
    private Set<Map<String, Object>> mentions;

    public RegexMentionExtractorTest(String activityContent, Set<Map<String, Object>> hashtags) {
        this.activity = new Activity();
        this.activity.setContent(activityContent);
        this.mentions = hashtags;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
                {"This is the @content of a standard tweet", Stream.of(new HashMap<String, Object>() {{
                  put("displayName", "content");
                }}).collect(Collectors.toSet())},
                {"This is the content of a standard tweet", Stream.of(new HashMap<String, Object>()).collect(Collectors.toSet())},
                {"This is the @content of a standard @tweet",  Stream.of(new HashMap<String, Object>() {{
                    put("displayName", "content");
                }},new HashMap<String, Object>() {{
                    put("displayName", "tweet");
                }}).collect(Collectors.toSet())},
                {"UNIX 时间1400000000 秒…… （该睡觉了，各位夜猫子）@程序员#", Stream.of(new HashMap<String, Object>() {{
                    put("displayName", "程序员");
                }}).collect(Collectors.toSet())},
                {"This is the body of a @fbpost. It can have multiple lines of #content, as well as much more detailed and flowery @language.",
                    Stream.of(new HashMap<String, Object>() {{
                    put("displayName", "fbpost");
                }},new HashMap<String, Object>() {{
                    put("displayName", "language");
                }}).collect(Collectors.toSet())}
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExtraction() {
        StreamsDatum datum = new StreamsDatum(activity, "Test");
        List<StreamsDatum> result = new RegexMentionsExtractor().process(datum);
        assertThat(result.size(), is(equalTo(1)));
        Activity output = (Activity)result.get(0).getDocument();
        Set<String> extracted = (Set) ExtensionUtil.getInstance().ensureExtensions(output).get(RegexMentionsExtractor.EXTENSION_KEY);
        Sets.SetView<String> diff = Sets.difference(extracted, mentions);
        assertThat(diff.size(), is(equalTo(0)));
    }
}
