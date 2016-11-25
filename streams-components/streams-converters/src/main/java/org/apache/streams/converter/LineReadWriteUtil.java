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

package org.apache.streams.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LineReadWriteUtil converts Datums to/from character array appropriate for writing to
 * file systems.
 */
public class LineReadWriteUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverterUtil.class);

  private static Map<LineReadWriteConfiguration, LineReadWriteUtil> INSTANCE_MAP = Maps.newConcurrentMap();

  private static final List<String> DEFAULT_FIELDS = Lists.newArrayList("ID", "SEQ", "TS", "META", "DOC");

  private List<String> fields;
  private String fieldDelimiter = "\t";
  private String lineDelimiter = "\n";
  private String encoding = "UTF-8";

  private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private LineReadWriteUtil() {
  }

  private LineReadWriteUtil(LineReadWriteConfiguration configuration) {
    this.fields = configuration.getFields();
    this.fieldDelimiter = configuration.getFieldDelimiter();
    this.lineDelimiter = configuration.getLineDelimiter();
    this.encoding = configuration.getEncoding();
  }

  public static LineReadWriteUtil getInstance() {
    return getInstance(new LineReadWriteConfiguration());
  }

  /**
   * getInstance.
   * @param configuration
   * @return result
   */
  public static LineReadWriteUtil getInstance(LineReadWriteConfiguration configuration) {
    if ( INSTANCE_MAP.containsKey(configuration)
        &&
        INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      INSTANCE_MAP.put(configuration, new LineReadWriteUtil(configuration));
      return INSTANCE_MAP.get(configuration);
    }
  }

  /**
   * processLine
   * @param line
   * @return result
   */
  public StreamsDatum processLine(String line) {

    List<String> expectedFields = fields;
    if ( line.endsWith(lineDelimiter)) {
      line = trimLineDelimiter(line);
    }
    String[] parsedFields = line.split(fieldDelimiter);

    if ( parsedFields.length == 0) {
      return null;
    }

    String id = null;
    DateTime ts = null;
    BigInteger seq = null;
    Map<String, Object> metadata = null;
    String json = null;

    if ( expectedFields.contains( FieldConstants.DOC )
        && parsedFields.length > expectedFields.indexOf(FieldConstants.DOC)) {
      json = parsedFields[expectedFields.indexOf(FieldConstants.DOC)];
    }

    if ( expectedFields.contains( FieldConstants.ID )
        && parsedFields.length > expectedFields.indexOf(FieldConstants.ID)) {
      id = parsedFields[expectedFields.indexOf(FieldConstants.ID)];
    }
    if ( expectedFields.contains( FieldConstants.SEQ )
        && parsedFields.length > expectedFields.indexOf(FieldConstants.SEQ)) {
      try {
        seq = new BigInteger(parsedFields[expectedFields.indexOf(FieldConstants.SEQ)]);
      } catch ( NumberFormatException nfe ) {
        LOGGER.warn("invalid sequence number {}", nfe);
      }
    }
    if ( expectedFields.contains( FieldConstants.TS )
        && parsedFields.length > expectedFields.indexOf(FieldConstants.TS)) {
      ts = parseTs(parsedFields[expectedFields.indexOf(FieldConstants.TS)]);
    }
    if ( expectedFields.contains( FieldConstants.META )
        && parsedFields.length > expectedFields.indexOf(FieldConstants.META)) {
      metadata = parseMap(parsedFields[expectedFields.indexOf(FieldConstants.META)]);
    }

    StreamsDatum datum = new StreamsDatum(json);
    datum.setId(id);
    datum.setTimestamp(ts);
    datum.setMetadata(metadata);
    datum.setSequenceid(seq);
    return datum;

  }

  /**
   * convertResultToString
   * @param entry
   * @return result
   */
  public String convertResultToString(StreamsDatum entry) {
    String metadataJson = null;
    try {
      metadataJson = MAPPER.writeValueAsString(entry.getMetadata());
    } catch (JsonProcessingException ex) {
      LOGGER.warn("Error converting metadata to a string", ex);
    }

    String documentJson = null;
    try {
      if ( entry.getDocument() instanceof String ) {
        documentJson = (String) entry.getDocument();
      } else {
        documentJson = MAPPER.writeValueAsString(entry.getDocument());
      }
    } catch (JsonProcessingException ex) {
      LOGGER.warn("Error converting document to string", ex);
    }

    if (Strings.isNullOrEmpty(documentJson)) {
      return null;
    } else {
      StringBuilder stringBuilder = new StringBuilder();
      Iterator<String> fields = this.fields.iterator();
      List<String> fielddata = Lists.newArrayList();
      Joiner joiner = Joiner.on(fieldDelimiter).useForNull("");
      while( fields.hasNext() ) {
        String field = fields.next();
        if ( field.equals(FieldConstants.DOC) ) {
          fielddata.add(documentJson);
        } else if ( field.equals(FieldConstants.ID) ) {
          fielddata.add(entry.getId());
        } else if ( field.equals(FieldConstants.SEQ) ) {
          if (entry.getSequenceid() != null) {
            fielddata.add(entry.getSequenceid().toString());
          } else {
            fielddata.add("null");
          }
        } else if ( field.equals(FieldConstants.TS) ) {
          if (entry.getTimestamp() != null) {
            fielddata.add(entry.getTimestamp().toString());
          } else {
            fielddata.add(DateTime.now().toString());
          }
        } else if ( field.equals(FieldConstants.META) ) {
          fielddata.add(metadataJson);
        } else if ( entry.getMetadata().containsKey(field)) {
          fielddata.add(entry.getMetadata().get(field).toString());
        } else {
          fielddata.add(null);
        }
      }
      joiner.appendTo(stringBuilder, fielddata);
      return stringBuilder.toString();
    }
  }

  /**
   * parseTs
   * @param field
   * @return
   */
  public DateTime parseTs(String field) {

    DateTime timestamp = null;
    try {
      long longts = Long.parseLong(field);
      timestamp = new DateTime(longts);
    } catch ( Exception e1 ) {
      try {
        timestamp = DateTime.parse(field);
      } catch ( Exception e2 ) {
        try {
          timestamp = MAPPER.readValue(field, DateTime.class);
        } catch ( Exception e3 ) {
          LOGGER.warn("Could not parse timestamp:{} ", field);
        }
      }
    }

    return timestamp;
  }

  /**
   * parseMap
   * @param field
   * @return result
   */
  public Map<String, Object> parseMap(String field) {

    Map<String, Object> metadata = null;

    try {
      JsonNode jsonNode = MAPPER.readValue(field, JsonNode.class);
      metadata = MAPPER.convertValue(jsonNode, Map.class);
    } catch (Exception ex) {
      LOGGER.warn("failed in parseMap: " + ex.getMessage());
    }
    return metadata;
  }

  private String trimLineDelimiter(String str) {
    if ( !Strings.isNullOrEmpty(str)) {
      if (str.endsWith(lineDelimiter)) {
        return str.substring(0, str.length() - 1);
      }
    }
    return str;
  }
}
