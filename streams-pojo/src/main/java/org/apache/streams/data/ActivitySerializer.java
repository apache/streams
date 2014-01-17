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

package org.apache.streams.data;

import org.apache.streams.pojo.json.Activity;

import java.util.List;

/**
 * Serializes and deserializes Activities from a String
 */
public interface ActivitySerializer {

    /**
     * Gets the supported content type that can be deserialized/serialized
     *
     * @return A string representing the format name.  Can be an IETF MIME type or other
     */
    String serializationFormat();

    /**
     * Converts the activity to a String representation.
     *
     * @param deserialized the string
     * @return a fully populated Activity object
     */
    String serialize(Activity deserialized);

    /**
     * Converts a string into an Activity
     * @param serialized the string representation
     * @return a fully populated Activity object
     */
    Activity deserialize(String serialized);

    /**
     * Converts a string representing multiple activities into a list of Activity objects
     * @param serializedList a String representation of a List
     * @return a list of fully populated activities
     */
    List<Activity> deserializeAll(String serializedList);
}
