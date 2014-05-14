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
import com.google.common.collect.Maps;
import org.apache.streams.core.StreamsProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Processes the content of an {@link org.apache.streams.pojo.json.Activity} object to extract the @user mentions and add
 * them to the appropriate extensions object
 */
public class RegexMentionsExtractor extends AbstractRegexExtensionExtractor<Map<String, Object>> implements StreamsProcessor {
    public static final String DEFAULT_PATTERN = "@\\w+";
    public static final String PATTERN_CONFIG_KEY = "MentionPattern";
    public static final String EXTENSION_KEY = "user_mentions";
    public static final String DISPLAY_KEY = "displayName";

    public RegexMentionsExtractor() {
        super(PATTERN_CONFIG_KEY, EXTENSION_KEY, DEFAULT_PATTERN);
    }

    @Override
    protected Map<String, Object> prepareObject(String extracted) {
        HashMap<String, Object> mention = Maps.newHashMap();
        mention.put(DISPLAY_KEY, extracted.substring(1));
        return mention;
    }

}
