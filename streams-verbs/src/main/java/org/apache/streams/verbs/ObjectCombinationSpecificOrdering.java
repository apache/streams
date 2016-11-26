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
import org.apache.streams.pojo.json.ActivityObject;

import com.google.common.collect.Ordering;

/**
 * Orders ObjectCombinations from most specific to most general, in context of
 * degree of match to a specified Activity.
 */
public class ObjectCombinationSpecificOrdering extends Ordering<ObjectCombination> {

  private Activity activity;

  public ObjectCombinationSpecificOrdering(Activity activity) {
    this.activity = activity;
  }

  @Override
  public int compare(ObjectCombination left, ObjectCombination right) {
    if (matchCount(left) < matchCount(right)) {
      return 1;
    } else if ( matchCount(left) > matchCount(right)) {
      return -1;
    } else if ( !match(activity.getActor(), left.getActor()) && match(activity.getActor(), right.getActor())) {
      return 1;
    } else if ( match(activity.getActor(), left.getActor()) && !match(activity.getActor(), right.getActor())) {
      return -1;
    } else if ( !match(activity.getObject(), left.getObject()) && match(activity.getObject(), right.getObject())) {
      return 1;
    } else if ( match(activity.getObject(), left.getObject()) && !match(activity.getObject(), right.getObject())) {
      return -1;
    } else if ( !match(activity.getTarget(), left.getTarget()) && match(activity.getTarget(), right.getTarget())) {
      return 1;
    } else if ( match(activity.getTarget(), left.getTarget()) && !match(activity.getTarget(), right.getTarget())) {
      return -1;
    } else if ( !match(activity.getProvider(), left.getProvider()) && match(activity.getTarget(), right.getProvider())) {
      return 1;
    } else if ( match(activity.getProvider(), left.getProvider()) && !match(activity.getTarget(), right.getProvider())) {
      return -1;
    } else {
      return 0;
    }
  }

  /**
   * count matches between this ObjectCombination and this Activity.
   * @param objectCombination ObjectCombination
   * @return count
   */
  private int matchCount(ObjectCombination objectCombination) {
    int matchCount = 0;
    if ( match(activity.getActor(), objectCombination.getActor())) {
      matchCount++;
    }
    if ( match(activity.getObject(), objectCombination.getObject())) {
      matchCount++;
    }
    if ( match(activity.getTarget(), objectCombination.getTarget())) {
      matchCount++;
    }
    if ( match(activity.getProvider(), objectCombination.getProvider())) {
      matchCount++;
    }
    return matchCount;
  }

  /**
   * whether this ActivityObject matches the corresponding ObjectCombination pattern.
   * @param activityObject ActivityObject
   * @param pattern pattern
   * @return true or false
   */
  public boolean match(ActivityObject activityObject, String pattern) {
    return activityObject != null
        && activityObject.getObjectType() != null
        && activityObject.getObjectType().equals(pattern);
  }
}
