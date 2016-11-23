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

import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.util.SerializationUtil;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

  /**
   * return all matching VerbDefinitions for an Activity.
   * @param activity Activity
   * @return List of VerbDefinition
   */
  public List<VerbDefinition> matchingVerbDefinitions(Activity activity) {

    // ConcurrentHashSet is preferable, but it's only in guava 15+
    // spark 1.5.0 uses guava 14 so for the moment this is the workaround
    // Set<VerbDefinition> matches = Sets.newConcurrentHashSet();
    Set<VerbDefinition> matches = Collections.newSetFromMap(new ConcurrentHashMap<VerbDefinition, Boolean>());

    for ( VerbDefinition verbDefinition : verbDefinitionSet ) {
      VerbDefinition verbDefinitionCopy = SerializationUtil.cloneBySerialization(verbDefinition);
      if ( activity.getVerb().equals(verbDefinition.getValue())) {
        for ( ObjectCombination criteria : verbDefinitionCopy.getObjects()) {
          if ( filter(activity, criteria) == false ) {
            verbDefinitionCopy.getObjects().remove(criteria);
          }
        }
        if ( verbDefinitionCopy.getObjects().size() > 0) {
          matches.add(verbDefinitionCopy);
        }
      }
    }

    return Lists.newArrayList(matches);

  }

  /**
   * return all matching ObjectCombinations for an Activity.
   * @param activity Activity
   * @return List of ObjectCombination
   */
  public List<ObjectCombination> matchingObjectCombinations(Activity activity) {

    List<ObjectCombination> results = Lists.newArrayList();

    for ( VerbDefinition verbDefinition : verbDefinitionSet ) {
      if ( activity.getVerb().equals(verbDefinition.getValue())) {
        for ( ObjectCombination criteria : verbDefinition.getObjects()) {
          if ( filter(activity, criteria) == true ) {
            results.add(criteria);
          }
        }
      }
    }

    Collections.sort(results, new ObjectCombinationSpecificOrdering(activity));

    return results;
  }

  /**
   * whether this Activity matches this ObjectCombination.
   * @param activity Activity
   * @param criteria ObjectCombination
   * @return true or false
   */
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
    if (required == true && activityObject == null) {
      return false;
    } else if (required == false && activityObject == null) {
      return true;
    } else if (pattern.equals("*")) {
      return true;
    } else if (activityObject.getObjectType() == null) {
      return false;
    } else if (activityObject.getObjectType().equals(pattern)) {
      return true;
    } else {
      return false;
    }
  }



}
