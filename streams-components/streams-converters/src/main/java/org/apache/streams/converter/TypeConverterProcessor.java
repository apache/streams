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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * TypeConverterProcessor converts between String json and jackson-compatible POJO objects.
 *
 * <p></p>
 * Activity is one supported jackson-compatible POJO, so JSON String and objects with structual similarities
 *   to Activity can be converted to Activity objects.
 *
 * <p></p>
 * However, conversion to Activity should probably use {@link org.apache.streams.converter.ActivityConverterProcessor}
 *
 */
public class TypeConverterProcessor implements StreamsProcessor, Serializable {

  public static final String STREAMS_ID = "TypeConverterProcessor";

  private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverterProcessor.class);

  private List<String> formats = new ArrayList<>();

  protected ObjectMapper mapper;

  protected Class outClass;

  public TypeConverterProcessor(Class outClass) {
    this.outClass = outClass;
  }

  public TypeConverterProcessor(Class outClass, List<String> formats) {
    this(outClass);
    this.formats = formats;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {

    List<StreamsDatum> result = new LinkedList<>();
    Object inDoc = entry.getDocument();

    Object outDoc = TypeConverterUtil.getInstance().convert(inDoc, outClass, mapper);

    if ( outDoc != null ) {
      entry.setDocument(outDoc);
      result.add(entry);
    }

    return result;
  }

  @Override
  public void prepare(Object configurationObject) {
    if ( formats.size() > 0 ) {
      this.mapper = StreamsJacksonMapper.getInstance(formats);
    } else {
      this.mapper = StreamsJacksonMapper.getInstance();
    }
  }

  @Override
  public void cleanUp() {
    this.mapper = null;
  }

}
