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

import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.ActivityObject;

import java.io.Serializable;

/**
 * Converts non-ActivityObject documents to ActivityObjects and back.
 *
 * <p></p>
 * Each converter may return zero or one alternative representations.
 */
public interface ActivityObjectConverter<T> extends Serializable {

  /**
   * What class does this ActivityConverter require?
   *
   * @return The class the ActivityConverter requires.  Should always return the templated class.
   */
  Class requiredClass();

  /**
   * Gets the supported content type that can be deserialized/serialized.
   *
   * @return A string representing the format name.  Can be an IETF MIME type or other
   */
  String serializationFormat();

  /**
   * Converts the activity to a POJO representation.
   *
   * @param deserialized the string
   * @return a fully populated Activity object
   */
  T fromActivityObject(ActivityObject deserialized) throws ActivityConversionException;

  /**
   * Converts a POJO into an ActivityObject.
   * @param serialized the string representation
   * @return a fully populated Activity object
   */
  ActivityObject toActivityObject(T serialized) throws ActivityConversionException;

}
