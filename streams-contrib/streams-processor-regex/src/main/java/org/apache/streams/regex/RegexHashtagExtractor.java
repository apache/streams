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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
 * Processes the content of an {@link org.apache.streams.pojo.json.Activity} object to extract the Hashtags and add
 * them to the appropriate extensions object
 */
public class RegexHashtagExtractor implements StreamsProcessor{

    public final static String DEFAULT_PATTERN = "#\\w+";
    public final static String PATTERN_CONFIG_KEY = "HashtagPattern";
    public final static String EXTENSION_KEY = "hashtags";

    private String hashPattern;

    public String getHashPattern() {
        return hashPattern;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        if(!(entry.getDocument() instanceof Activity)) {
            return Lists.newArrayList();
        }
        if(Strings.isNullOrEmpty(hashPattern)) {
            prepare(null);
        }
        Activity activity = (Activity)entry.getDocument();
        Map<String, List<Integer>> matches = RegexUtils.extractMatches(hashPattern, activity.getContent());
        Set<String> hashtags = ensureHashtagsExtension(activity);
        for(String key : matches.keySet()) {
            hashtags.add(key.substring(1));
        }
        return Lists.newArrayList(entry);
    }

    @Override
    public void prepare(Object configurationObject) {
        if(configurationObject instanceof Map) {
            if(((Map)configurationObject).containsKey(PATTERN_CONFIG_KEY)) {
                hashPattern = (String)((Map)configurationObject).get(PATTERN_CONFIG_KEY);
            }
        } else if(configurationObject instanceof String) {
            hashPattern = (String)configurationObject;
        } else {
            hashPattern = DEFAULT_PATTERN;
        }
    }

    @Override
    public void cleanUp() {
        //NOP
    }

    protected Set<String> ensureHashtagsExtension(Activity activity) {
        Map<String, Object> extensions = ensureExtensions(activity);
        Set<String> hashtags;
        if(extensions.containsKey(EXTENSION_KEY)) {
            hashtags = Sets.newHashSet((Iterable<String>) extensions.get(EXTENSION_KEY));
        } else {
            hashtags = Sets.newHashSet();
            extensions.put(EXTENSION_KEY, hashtags);
        }
        return hashtags;
    }
}
