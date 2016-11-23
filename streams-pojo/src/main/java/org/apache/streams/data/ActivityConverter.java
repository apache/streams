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
import org.apache.streams.pojo.json.Activity;

import java.io.Serializable;
import java.util.List;

/**
 * Converts non-Activity documents to Activities and back.
 *
 * <p/>
 * Each converter may one, several, or zero activities.
 *
 * <p/>
 * The recommended approach for deriving multiple activities from a source document is:
 *
 * <p/>
 *   1) Return one activity for each occurance of a verb, from the same ActivityConverter, if the activities are of like type.
 *
 * <p/>
 *      For example, BlogShareConverter would convert a blog containing two shares into two Activities with verb: share
 *
 * <p/>
 *   2) Create multiple ActivityConverters, if the activities are not of like type.
 *
 * <p/>
 *      For example, a blog post that is both a post and a share should be transformed by two seperate Converters, individually
 *      or simultaneously applied.
 */
public interface ActivityConverter<T> extends Serializable {

  /**
   * What class does this ActivityConverter require?
   *
   * @return The class the ActivityConverter requires.  Should always return the templated class.
   */
  Class requiredClass();

  /**
   * Gets the supported content type that can be deserialized/serialized
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
  T fromActivity(Activity deserialized) throws ActivityConversionException;

  /**
   * Converts multiple Activities into a list of source documents.
   * @param list a typed List of documents
   * @return a list of source documents
   */
  List<T> fromActivityList(List<Activity> list) throws ActivityConversionException;

  /**
   * Converts a POJO into one or more Activities.
   * @param serialized the string representation
   * @return a fully populated Activity object
   */
  List<Activity> toActivityList(T serialized) throws ActivityConversionException;

  /**
   * Converts multiple documents into a list of Activity objects.
   * @param list a typed List of documents
   * @return a list of fully populated activities
   */
  List<Activity> toActivityList(List<T> list) throws ActivityConversionException;
}
