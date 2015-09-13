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
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LineReadWriteUtil converts Datums to/from character array appropriate for writing to
 * file systems.
 */
public class LineReadWriteUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(TypeConverterUtil.class);

    private static LineReadWriteUtil INSTANCE;

    private final static List<String> DEFAULT_FIELDS = Lists.newArrayList("ID", "SEQ", "TS", "META", "DOC");

    private List<String> fields;
    private String fieldDelimiter = "\t";
    private String lineDelimiter = "\n";

    private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    private LineReadWriteUtil() {
        this(LineReadWriteUtil.DEFAULT_FIELDS);
    }

    private LineReadWriteUtil(List<String> fields) {
        if( fields != null && fields.size() > 0) this.fields = fields;
        else this.fields = LineReadWriteUtil.DEFAULT_FIELDS;
    }

    private LineReadWriteUtil(List<String> fields, String fieldDelimiter) {
        this(fields);
        if( fieldDelimiter != null ) this.fieldDelimiter = fieldDelimiter;
    }

    private LineReadWriteUtil(List<String> fields, String fieldDelimiter, String lineDelimiter) {
        this(fields);
        if( fieldDelimiter != null ) this.fieldDelimiter = fieldDelimiter;
        if( lineDelimiter != null ) this.lineDelimiter = lineDelimiter;
    }

    public static LineReadWriteUtil getInstance(){
        if( INSTANCE == null )
            INSTANCE = new LineReadWriteUtil(LineReadWriteUtil.DEFAULT_FIELDS);
        return INSTANCE;
    }

    public static LineReadWriteUtil getInstance(List<String> fields){
        if( INSTANCE == null )
            INSTANCE = new LineReadWriteUtil(fields);
        return INSTANCE;
    }

    public static LineReadWriteUtil getInstance(List<String> fields, String fieldDelimiter){
        if( INSTANCE == null )
            INSTANCE = new LineReadWriteUtil(fields, fieldDelimiter);
        return INSTANCE;
    }

    public static LineReadWriteUtil getInstance(List<String> fields, String fieldDelimiter, String lineDelimiter){
        if( INSTANCE == null )
            INSTANCE = new LineReadWriteUtil(fields, fieldDelimiter, lineDelimiter);
        return INSTANCE;
    }

    public StreamsDatum processLine(String line) {

        List<String> expectedFields = fields;
        String[] parsedFields = line.split(fieldDelimiter);

        if( parsedFields.length == 0)
            return null;

        String id = null;
        DateTime ts = null;
        BigInteger seq = null;
        Map<String, Object> metadata = null;
        String json = null;

        if( expectedFields.contains( FieldConstants.DOC )
                && parsedFields.length > expectedFields.indexOf(FieldConstants.DOC)) {
            json = parsedFields[expectedFields.indexOf(FieldConstants.DOC)];
        }

        if( expectedFields.contains( FieldConstants.ID )
                && parsedFields.length > expectedFields.indexOf(FieldConstants.ID)) {
            id = parsedFields[expectedFields.indexOf(FieldConstants.ID)];
        }
        if( expectedFields.contains( FieldConstants.SEQ )
                && parsedFields.length > expectedFields.indexOf(FieldConstants.SEQ)) {
            seq = new BigInteger(parsedFields[expectedFields.indexOf(FieldConstants.SEQ)]);
        }
        if( expectedFields.contains( FieldConstants.TS )
                && parsedFields.length > expectedFields.indexOf(FieldConstants.TS)) {
            ts = parseTs(parsedFields[expectedFields.indexOf(FieldConstants.TS)]);
        }
        if( expectedFields.contains( FieldConstants.META )
                && parsedFields.length > expectedFields.indexOf(FieldConstants.META)) {
            metadata = parseMap(parsedFields[expectedFields.indexOf(FieldConstants.META)]);
        }

        StreamsDatum datum = new StreamsDatum(trimLineDelimiter(json));
        datum.setId(id);
        datum.setTimestamp(ts);
        datum.setMetadata(metadata);
        datum.setSequenceid(seq);
        return datum;

    }

    public String convertResultToString(StreamsDatum entry) {
        String metadataJson = null;
        try {
            metadataJson = MAPPER.writeValueAsString(entry.getMetadata());
        } catch (JsonProcessingException e) {
            LOGGER.warn("Error converting metadata to a string", e);
        }

        String documentJson = null;
        try {
            if( entry.getDocument() instanceof String )
                documentJson = (String)entry.getDocument();
            else
                documentJson = MAPPER.writeValueAsString(entry.getDocument());
        } catch (JsonProcessingException e) {
            LOGGER.warn("Error converting document to string", e);
        }

        if (Strings.isNullOrEmpty(documentJson))
            return null;
        else {
            StringBuilder stringBuilder = new StringBuilder();
            Iterator<String> fields = this.fields.iterator();
            List<String> fielddata = Lists.newArrayList();
            Joiner joiner = Joiner.on(fieldDelimiter).useForNull("");
            while( fields.hasNext() ) {
                String field = fields.next();
                if( field.equals(FieldConstants.DOC) )
                    fielddata.add(documentJson);
                else if( field.equals(FieldConstants.ID) )
                    fielddata.add(entry.getId());
                else if( field.equals(FieldConstants.SEQ) )
                    if( entry.getSequenceid() != null)
                        fielddata.add(entry.getSequenceid().toString());
                    else
                        fielddata.add("null");
                else if( field.equals(FieldConstants.TS) )
                    if( entry.getTimestamp() != null )
                        fielddata.add(entry.getTimestamp().toString());
                    else
                        fielddata.add(DateTime.now().toString());
                else if( field.equals(FieldConstants.META) )
                    fielddata.add(metadataJson);
                else if( entry.getMetadata().containsKey(field)) {
                    fielddata.add(entry.getMetadata().get(field).toString());
                } else {
                    fielddata.add(null);
                }

            }
            joiner.appendTo(stringBuilder, fielddata);
            return stringBuilder.append(lineDelimiter).toString();
        }
    }

    public DateTime parseTs(String field) {

        DateTime timestamp = null;
        try {
            long longts = Long.parseLong(field);
            timestamp = new DateTime(longts);
        } catch ( Exception e ) {
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

    public Map<String, Object> parseMap(String field) {

        Map<String, Object> metadata = null;

        try {
            JsonNode jsonNode = MAPPER.readValue(field, JsonNode.class);
            metadata = MAPPER.convertValue(jsonNode, Map.class);
        } catch (Exception e) {
            LOGGER.warn("failed in parseMap: " + e.getMessage());
        }
        return metadata;
    }

    private String trimLineDelimiter(String str) {
        if( !Strings.isNullOrEmpty(str))
            if( str.endsWith(lineDelimiter))
                return str.substring(0,str.length()-1);
        return str;
    }
}
