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

import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;

import java.util.Collection;

/**
 * Processes the content of an {@link org.apache.streams.pojo.json.Activity} object to extract the URLs and add
 * them to the appropriate extensions object
 */
public class RegexUrlExtractor extends AbstractRegexExtensionExtractor<String> implements StreamsProcessor {

    //Temporarily copied from streams-processor-urls so as not to force a dependency on that provider.  This should
    //be moved to a common utility package
    public final static String DEFAULT_PATTERN =
            "(?:(?:https?|ftp)://)" +
                    "(?:\\S+(?::\\S*)?@)?" +
                    "(?:" +
                    "(?!(?:10|127)(?:\\.\\d{1,3}){3})" +
                    "(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})" +
                    "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})" +
                    "(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])" +
                    "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}" +
                    "(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))" +
                    "|" +
                    "(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)" +
                    "(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*" +
                    "(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))" +
                    ")" +
                    "(?::\\d{2,5})?" +
                    "(?:/[^\\s]*)?";

    public final static String PATTERN_CONFIG_KEY = "URLPattern";

    public RegexUrlExtractor() {
        super(PATTERN_CONFIG_KEY, null, DEFAULT_PATTERN);
    }

    @Override
    protected String prepareObject(String extracted) {
        return extracted;
    }

    @Override
    protected Collection<String> ensureTargetObject(Activity activity) {
        return activity.getLinks();
    }
}
