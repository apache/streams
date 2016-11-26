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
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a base implementation for extracting entities from text using regular expressions and then
 * modifying the appropriate {@link org.apache.streams.pojo.json.Activity} extensions object.
 */
public abstract class AbstractRegexExtensionExtractor<T> implements StreamsProcessor {

  private final String patternConfigKey;
  private final String extensionKey;
  private final String defaultPattern;

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegexExtensionExtractor.class);

  private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  private String pattern;

  protected AbstractRegexExtensionExtractor(String patternConfigKey, String extensionKey, String defaultPattern) {
    this.patternConfigKey = patternConfigKey;
    this.extensionKey = extensionKey;
    this.defaultPattern = defaultPattern;
  }

  public String getPattern() {
    return pattern;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {
    Activity activity;
    if (entry.getDocument() instanceof Activity) {
      activity = (Activity) entry.getDocument();
    } else if (entry.getDocument() instanceof ObjectNode) {
      activity = mapper.convertValue(entry.getDocument(), Activity.class);
    } else {
      return new ArrayList<>();
    }
    if (StringUtils.isBlank(pattern)) {
      prepare(null);
    }
    Map<String, List<Integer>> matches = RegexUtils.extractMatches(pattern, activity.getContent());
    Collection<T> entities = ensureTargetObject(activity);
    for (String key : matches.keySet()) {
      entities.add(prepareObject(key));
    }

    Set<T> set = new HashSet<>();
    set.addAll(entities);
    entities.clear();
    entities.addAll(set);

    entry.setDocument(activity);
    return Collections.singletonList(entry);
  }

  @Override
  public void prepare(Object configurationObject) {
    if (configurationObject instanceof Map) {
      if (((Map) configurationObject).containsKey(patternConfigKey)) {
        pattern = (String) ((Map) configurationObject).get(patternConfigKey);
      }
    } else if (configurationObject instanceof String) {
      pattern = (String) configurationObject;
    } else {
      pattern = defaultPattern;
    }
  }

  @Override
  public void cleanUp() {
    //NOP
  }

  /**
   * Configures the value to be persisted to the extensions object.
   * @param extracted the value extracted by the regex
   * @return an object representing the appropriate extension
   */
  protected abstract T prepareObject(String extracted);

  @SuppressWarnings("unchecked")
  protected Collection<T> ensureTargetObject(Activity activity) {
    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);
    Set<T> hashtags;

    if (extensions.containsKey(extensionKey) && extensions.get(extensionKey) != null) {
      hashtags = Sets.newHashSet((Iterable<T>) extensions.get(extensionKey));
    } else {
      hashtags = new HashSet<>();
    }

    extensions.put(extensionKey, hashtags);

    return hashtags;
  }
}
