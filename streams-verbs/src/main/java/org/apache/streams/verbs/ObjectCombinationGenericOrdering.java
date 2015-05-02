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

import com.google.common.collect.Ordering;

/**
 * Orders ObjectCombinations from most specific to most general, without regard
 * for degree of match to any specific Activity.
 */
public class ObjectCombinationGenericOrdering extends Ordering<ObjectCombination> {

    public ObjectCombinationGenericOrdering() {}

    @Override
    public int compare(ObjectCombination left, ObjectCombination right) {
        if( wildcardCount(left) < wildcardCount(right))
            return -1;
        if( wildcardCount(left) > wildcardCount(right))
            return 1;
        if( !wildcard(left.getActor()) && wildcard(right.getActor()))
            return -1;
        if( wildcard(left.getActor()) && !wildcard(right.getActor()))
            return 1;
        if( !wildcard(left.getObject()) && wildcard(right.getObject()))
            return -1;
        if( wildcard(left.getObject()) && !wildcard(right.getObject()))
            return 1;
        if( !wildcard(left.getTarget()) && wildcard(right.getTarget()))
            return -1;
        if( wildcard(left.getTarget()) && !wildcard(right.getTarget()))
            return 1;
        if( !wildcard(left.getProvider()) && wildcard(right.getProvider()))
            return -1;
        if( wildcard(left.getProvider()) && !wildcard(right.getProvider()))
            return 1;
        return 0;
    }

    public int wildcardCount(ObjectCombination objectCombination) {
        int wildcardCount = 0;
        if( wildcard(objectCombination.getActor()))
            wildcardCount++;
        if( wildcard(objectCombination.getObject()))
            wildcardCount++;
        if( wildcard(objectCombination.getTarget()))
            wildcardCount++;
        if( wildcard(objectCombination.getProvider()))
            wildcardCount++;
        return wildcardCount;
    }

    public boolean wildcard(String pattern) {
        if( pattern.equals("*")) return true;
        else return false;
    }
}
