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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.verbs.ObjectCombination;
import org.apache.streams.verbs.VerbDefinition;
import org.apache.streams.verbs.VerbDefinitionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VerbDefinitionFilter implements StreamsProcessor {

    private static final String STREAMS_ID = "LinkResolverProcessor";
    private static final Logger LOGGER = LoggerFactory.getLogger(VerbDefinitionFilter.class);

    protected Set<VerbDefinition> verbDefinitionSet;
    protected VerbDefinitionResolver resolver;

    public VerbDefinitionFilter() {
        // get with reflection
    }

    public VerbDefinitionFilter(Set<VerbDefinition> verbDefinitionSet) {
        this();
        this.verbDefinitionSet = verbDefinitionSet;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        LOGGER.debug("{} filtering {}", STREAMS_ID, entry.getDocument().getClass());

        Activity activity;

        Preconditions.checkArgument(entry.getDocument() instanceof Activity);

        activity = (Activity) entry.getDocument();

        boolean match = false;
        for( VerbDefinition verbDefinition : verbDefinitionSet)
            if( verbDefinition.getValue() != null &&
                verbDefinition.getValue().equals(activity.getVerb()))
                for (ObjectCombination objectCombination : verbDefinition.getObjects())
                    if (VerbDefinitionResolver.filter(activity, objectCombination) == true)
                        match = true;

        if( match == true )
            result.add(entry);

        return result;
    }

    @Override
    public void prepare(Object o) {
        if( verbDefinitionSet != null)
            resolver = new VerbDefinitionResolver(verbDefinitionSet);
        else resolver = new VerbDefinitionResolver();
        Preconditions.checkNotNull(resolver);
    }

    @Override
    public void cleanUp() {
        // noOp
    }

}
