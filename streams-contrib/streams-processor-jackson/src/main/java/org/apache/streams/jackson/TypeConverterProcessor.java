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

package org.apache.streams.jackson;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * TypeConverterProcessor changes the JVM type while maintaining
 * the underlying document.
 */
public class TypeConverterProcessor implements StreamsProcessor {

  public static final String STREAMS_ID = "TypeConverterProcessor";

  private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverterProcessor.class);

  private List<String> formats = new ArrayList<>();

  private ObjectMapper mapper;

  private Class inClass;
  private Class outClass;


  /**
   * TypeConverterProcessor constructor.
   * @param inClass inClass
   * @param outClass outClass
   * @param mapper mapper
   */
  public TypeConverterProcessor(Class inClass, Class outClass, ObjectMapper mapper) {
    this.inClass = inClass;
    this.outClass = outClass;
    this.mapper = mapper;
  }

  /**
   * TypeConverterProcessor constructor.
   * @param inClass inClass
   * @param outClass outClass
   * @param formats formats
   */
  public TypeConverterProcessor(Class inClass, Class outClass, List<String> formats) {
    this.inClass = inClass;
    this.outClass = outClass;
    this.formats = formats;
  }

  /**
   * TypeConverterProcessor constructor.
   * @param inClass inClass
   * @param outClass outClass
   */
  public TypeConverterProcessor(Class inClass, Class outClass) {
    this.inClass = inClass;
    this.outClass = outClass;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {
    List<StreamsDatum> result = new LinkedList<>();
    Object inDoc = entry.getDocument();
    ObjectNode node = null;
    if ( inClass == String.class
          || inDoc instanceof String ) {
      try {
        node = this.mapper.readValue((String)entry.getDocument(), ObjectNode.class);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } else {
      node = this.mapper.convertValue(inDoc, ObjectNode.class);
    }

    if (node != null) {
      Object outDoc;
      try {
        if ( outClass == String.class ) {
          outDoc = this.mapper.writeValueAsString(node);
        } else {
          outDoc = this.mapper.convertValue(node, outClass);
        }
        StreamsDatum outDatum = new StreamsDatum(outDoc, entry.getId(), entry.getTimestamp(), entry.getSequenceid());
        outDatum.setMetadata(entry.getMetadata());
        result.add(outDatum);
      } catch (Throwable ex) {
        LOGGER.warn(ex.getMessage());
        LOGGER.warn(node.toString());
      }
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

  }
}
