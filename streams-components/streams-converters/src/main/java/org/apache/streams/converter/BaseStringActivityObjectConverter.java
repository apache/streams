/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.streams.converter;

import org.apache.streams.data.ActivityObjectConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.ActivityObject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Ensures generic ObjectNode representation of an Activity can be converted to Activity.
 *
 * <p/>
 * BaseObjectNodeActivityConverter is included by default in all
 * @see {@link ActivityConverterProcessor}
 *
 */
public class BaseStringActivityObjectConverter implements ActivityObjectConverter<String> {

  public static Class requiredClass = String.class;

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  @Override
  public Class requiredClass() {
    return requiredClass;
  }

  @Override
  public String serializationFormat() {
    return null;
  }

  @Override
  public String fromActivityObject(ActivityObject deserialized) throws ActivityConversionException {
    try {
      return mapper.writeValueAsString(deserialized);
    } catch (Exception ex) {
      throw new ActivityConversionException();
    }
  }

  @Override
  public ActivityObject toActivityObject(String serialized) throws ActivityConversionException {
    try {
      return mapper.readValue(serialized, ActivityObject.class);
    } catch (Exception ex) {
      throw new ActivityConversionException();
    }
  }

}
