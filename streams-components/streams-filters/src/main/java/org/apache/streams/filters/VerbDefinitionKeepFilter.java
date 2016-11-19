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

package org.apache.streams.filters;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.verbs.VerbDefinition;
import org.apache.streams.verbs.VerbDefinitionMatchUtil;
import org.apache.streams.verbs.VerbDefinitionResolver;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Checks one or more verb definitions against a stream of Activity documents, and drops any activities
 * which do not match the filter criteria.
 */
public class VerbDefinitionKeepFilter implements StreamsProcessor {

  public static final String STREAMS_ID = "VerbDefinitionKeepFilter";

  private static final Logger LOGGER = LoggerFactory.getLogger(VerbDefinitionKeepFilter.class);

  protected Set<VerbDefinition> verbDefinitionSet;
  protected VerbDefinitionResolver resolver;

  public VerbDefinitionKeepFilter() {
    // get with reflection
  }

  public VerbDefinitionKeepFilter(Set<VerbDefinition> verbDefinitionSet) {
    this();
    this.verbDefinitionSet = verbDefinitionSet;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {

    List<StreamsDatum> result = Lists.newArrayList();

    LOGGER.debug("{} filtering {}", STREAMS_ID, entry.getDocument().getClass());

    Activity activity;

    Preconditions.checkArgument(entry.getDocument() instanceof Activity);

    activity = (Activity) entry.getDocument();

    if ( VerbDefinitionMatchUtil.match(activity, this.verbDefinitionSet) == true ) {
      result.add(entry);
    }

    return result;
  }

  @Override
  public void prepare(Object configuration) {
    if ( verbDefinitionSet != null ) {
      resolver = new VerbDefinitionResolver(verbDefinitionSet);
    } else {
      resolver = new VerbDefinitionResolver();
    }
    Preconditions.checkNotNull(resolver);
  }

  @Override
  public void cleanUp() {
    // noOp
  }

}
