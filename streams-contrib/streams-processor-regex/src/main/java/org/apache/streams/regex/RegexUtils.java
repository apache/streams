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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides utilities for extracting matches from content
 */
public class RegexUtils {

    private static final Map<String, Pattern> patternCache = Maps.newConcurrentMap();
    private final static Logger LOGGER = LoggerFactory.getLogger(RegexUtils.class);
    final static long REGEX_MATCH_THREAD_KEEP_ALIVE_DEFAULT_TIMEOUT = 1000L; //1 second
    private static final int MAX_THREADS = 64; //Must be multiple of two

    private static ForkJoinPool threadPool = new ForkJoinPool(MAX_THREADS);

    private RegexUtils() {}

    /**
     * Extracts matches of the given pattern in the content and returns them as a list.
     * @param pattern the pattern for the substring to match.  For example, [0-9]* matches 911 in Emergency number is 911.
     * @param content the complete content to find matches in.
     * @return a non-null list of matches.
     */
    public static Map<String, List<Integer>> extractMatches(String pattern, String content) {
        return getMatches(pattern, content, -1);
    }

    /**
     * Extracts matches of the given pattern that are bounded by separation characters and returns them as a list.
     * @param pattern the pattern for the substring to match.  For example, [0-9]* matches 911 in Emergency number is 911.
     * @param content the complete content to find matches in.
     * @return a non-null list of matches.
     */
    public static Map<String, List<Integer>> extractWordMatches(String pattern, String content) {
        pattern = "(^|\\s)(" + pattern + ")([\\s!\\.;,?]|$)";
        return getMatches(pattern, content, 2);
    }

    protected static Map<String, List<Integer>> getMatches(String pattern, String content, int capture) {
        try {
            //Since certain regex patterns can be susceptible to catastrophic backtracking
            //We need to be able to isolate this operation in a separate thread and kill
            //it if it ends up taking too long
            RegexRunnable regexRunnable = new RegexRunnable(pattern, content, capture);

            ForkJoinTask<Map<String, List<Integer>>> task = threadPool.submit(regexRunnable);

            Map<String, List<Integer>> matches = Maps.newHashMap();

            try {
                matches = task.get(REGEX_MATCH_THREAD_KEEP_ALIVE_DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                LOGGER.error("Timed out while trying to find matches for content: {} and pattern: {} Possible catastrophic backtracking!: {}", content, pattern, e);
            } finally {
                if(!task.isDone()) {
                    task.cancel(true);
                }
            }

            return matches;
        } catch (Exception e) {
            LOGGER.error("Throwable process {}", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Pattern getPattern(String pattern) {
        Pattern p;
        if (patternCache.containsKey(pattern)) {
            p = patternCache.get(pattern);
        } else {
            p = Pattern.compile(pattern);
            patternCache.put(pattern, p);
        }
        return p;
    }

    static class RegexRunnable implements Callable<Map<String, List<Integer>>> {
        private String pattern;
        private InterruptableCharSequence content;
        private int capture;

        private Map<String, List<Integer>> matches = Maps.newHashMap();

        public RegexRunnable(String pattern, String content, int capture) {
            this.pattern = pattern;
            this.content = new InterruptableCharSequence(content);
            this.capture = capture;
        }

        @Override
        public Map<String, List<Integer>> call() throws Exception {
            if(content != null) {
                Matcher m = getPattern(pattern).matcher(content);
                while (m.find()) {
                    String group = capture > 0 ? m.group(capture) : m.group();
                    if (group != null && !group.equals("")) {
                        List<Integer> indices;
                        if (matches.containsKey(group)) {
                            indices = matches.get(group);
                        } else {
                            indices = Lists.newArrayList();
                            matches.put(group, indices);
                        }
                        indices.add(m.start());
                    }
                }
            }

            return matches;
        }
    }
}