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

package org.apache.streams.verbs;

import org.apache.streams.pojo.json.Activity;

import java.util.Set;

/**
 * Check whether an activity matches one or several VerbDefinition.
 */
public class VerbDefinitionMatchUtil {

  /**
   * whether this Activity matches any of a Set of VerbDefinitions.
   * @param activity Activity
   * @param verbDefinitionSet Set of VerbDefinition
   * @return true or false
   */
  public static boolean match(Activity activity, Set<VerbDefinition> verbDefinitionSet) {

    for ( VerbDefinition verbDefinition : verbDefinitionSet) {
      if ( match( activity, verbDefinition )) {
        return true;
      }
    }
    return false;

  }

  /**
   * whether this Activity matches this VerbDefinition.
   * @param activity Activity
   * @param verbDefinition VerbDefinition
   * @return true or false
   */
  public static boolean match(Activity activity, VerbDefinition verbDefinition) {

    if ( verbDefinition.getValue() != null
          && verbDefinition.getValue().equals(activity.getVerb())) {
      for (ObjectCombination objectCombination : verbDefinition.getObjects()) {
        if (VerbDefinitionResolver.filter(activity, objectCombination) == true) {
          return true;
        }
      }
    }
    return false;
  }

}
