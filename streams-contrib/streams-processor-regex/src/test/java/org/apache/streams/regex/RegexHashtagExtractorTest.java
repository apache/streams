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


import com.google.common.collect.Sets;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.json.Activity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RegexHashtagExtractorTest {

    private Activity activity;
    private Set<String> hashtags;

    public RegexHashtagExtractorTest(String activityContent, Set<String> hashtags) {
        this.activity = new Activity();
        this.activity.setContent(activityContent);
        this.hashtags = hashtags;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
                {"This is the #content of a standard tweet", Sets.newHashSet("content")},
                {"This is the content of a standard tweet", Sets.newHashSet()},
                {"This is the #content of a standard #tweet", Sets.newHashSet("content", "tweet")},
                {"UNIX 时间1400000000 秒…… （该睡觉了，各位夜猫子）#程序员#", Sets.newHashSet("程序员")},
                {"This is the body of a #fbpost. It can have multiple lines of #content, as well as much more detailed and flowery #language.", Sets.newHashSet("content", "fbpost", "language")}
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExtraction() {
        StreamsDatum datum = new StreamsDatum(activity, "Test");
        List<StreamsDatum> result = new RegexHashtagExtractor().process(datum);
        assertThat(result.size(), is(equalTo(1)));
        Activity output = (Activity)result.get(0).getDocument();
        Set<String> extracted = (Set) ensureExtensions(output).get(RegexHashtagExtractor.EXTENSION_KEY);
        Sets.SetView<String> diff = Sets.difference(extracted, hashtags);
        assertThat(diff.size(), is(equalTo(0)));
    }
}
