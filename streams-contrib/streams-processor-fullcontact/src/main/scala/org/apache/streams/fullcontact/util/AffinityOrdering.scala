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

package org.apache.streams.fullcontact.util

import org.apache.juneau.ObjectMap

object AffinityOrdering extends Ordering[ObjectMap] {
  def sortByAffinityString(s1: String, s2: String) : Int = {
    if(s1 == null && s2 == null) return 0;
    if(s1 == null ) return -1;
    if(s2 == null ) return 1;
    if(s1.isEmpty && s2.isEmpty) return 0;
    if(s1 == s2) return 0;
    if(s1.isEmpty ) return -1;
    if(s2.isEmpty ) return 1;
    return s1.takeRight(1).compareToIgnoreCase(s2.takeRight(1))
  }
  def sortByAffinity(o1: ObjectMap, o2: ObjectMap) : Int = {
    return sortByAffinityString(o1.getString("affinity", ""), o2.getString("affinity", ""))
  }
  def compare(a:ObjectMap, b:ObjectMap) = sortByAffinity(a,b)
}
