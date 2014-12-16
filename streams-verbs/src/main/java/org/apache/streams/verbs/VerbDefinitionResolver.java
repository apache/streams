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

package org.apache.streams.verbs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.verbs.ObjectCombination;
import org.apache.streams.verbs.VerbDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VerbDefinitionResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerbDefinitionResolver.class);

    protected Set<VerbDefinition> verbDefinitionSet;

    public VerbDefinitionResolver() {
        // get with reflection
    }

    public VerbDefinitionResolver(Set<VerbDefinition> verbDefinitionSet) {
        this();
        this.verbDefinitionSet = verbDefinitionSet;
    }

    public List<VerbDefinition> matchingVerbDefinitions(Activity activity) {

        Set<VerbDefinition> candidates = Sets.newConcurrentHashSet(verbDefinitionSet);

        for( VerbDefinition verbDefinition : candidates ) {
            if( activity.getVerb().equals(verbDefinition.getValue())) {
                for( ObjectCombination criteria : verbDefinition.getObjects()) {
                    if( filter(activity, criteria) == false ) {
                        verbDefinition.getObjects().remove(criteria);
                    }
                }
            } else {
                candidates.remove(verbDefinition);
            }
            if( verbDefinition.getObjects().size() == 0)
                candidates.remove(verbDefinition);

        }

        return Lists.newArrayList(candidates);

    }

    public List<ObjectCombination> matchingObjectCombinations(Activity activity) {

        List<ObjectCombination> results = Lists.newArrayList();

        for( VerbDefinition verbDefinition : verbDefinitionSet ) {
            if( activity.getVerb().equals(verbDefinition.getValue())) {
                for( ObjectCombination criteria : verbDefinition.getObjects()) {
                    if( filter(activity, criteria) == true ) {
                        results.add(criteria);
                    }
                }
            }
        }

        results.sort(new ObjectCombinationSpecificOrdering(activity));

        return results;
    }

    public static boolean filter(Activity activity, ObjectCombination criteria) {

        return  filterType(activity.getActor(), criteria.getActorRequired(), criteria.getActor())
                &&
                filterType(activity.getObject(), criteria.getObjectRequired(), criteria.getObject())
                &&
                filterType(activity.getProvider(), criteria.getProviderRequired(), criteria.getProvider())
                &&
                filterType(activity.getTarget(), criteria.getTargetRequired(), criteria.getTarget())
                ;

    }

    public static boolean filterType(ActivityObject activityObject, boolean required, String pattern) {
        if (required == true && activityObject == null) return false;
        if (required == false && activityObject == null) return true;
        if (pattern.equals("*")) return true;
        else if (activityObject.getObjectType() == null) return false;
        else if (activityObject.getObjectType().equals(pattern))
            return true;
        else return false;
    }



}
