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
import org.apache.streams.pojo.json.Activity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RegexUrlExtractorTest {

    private Activity activity;
    private Set<String> links;

    public RegexUrlExtractorTest(String activityContent, Set<String> links) {
        this.activity = new Activity();
        this.activity.setContent(activityContent);
        this.links = links;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
                {"This is the http://t.co/foo of a standard tweet", Stream.of("http://t.co/foo").collect(Collectors.toSet())},
                {"This is the https://t.co/foo of a standard tweet", Stream.of("https://t.co/foo").collect(Collectors.toSet())},
                {"This is the https://t.co/foo of a standard tweet https://t.co/foo", Stream.of("https://t.co/foo").collect(Collectors.toSet())},
                {"This is the http://amd.com/test of a standard tweet", Stream.of("http://amd.com/test").collect(Collectors.toSet())},
                {"This is the content of a standard tweet", new HashSet<>()},
                {"This is the http://www.google.com/articles/awesome?with=query&params=true of a standard @tweet",
                    Stream.of("http://www.google.com/articles/awesome?with=query&params=true").collect(Collectors.toSet())}
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExtraction() {
        StreamsDatum datum = new StreamsDatum(activity, "Test");
        List<StreamsDatum> result = new RegexUrlExtractor().process(datum);
        assertThat(result.size(), is(equalTo(1)));
        Activity output = (Activity)result.get(0).getDocument();
        Set<String> extracted = new HashSet<>(output.getLinks());
        Set<String> diff = links.stream().filter((x) -> !extracted.contains(x)).collect(Collectors.toSet());
        assertThat(diff.size(), is(equalTo(0)));
    }
}
