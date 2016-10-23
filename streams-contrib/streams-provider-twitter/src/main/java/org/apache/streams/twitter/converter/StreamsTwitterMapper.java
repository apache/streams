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

package org.apache.streams.twitter.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.streams.data.util.RFC3339Utils;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * This class assist with handling twitter's date-time format during conversion
 *
 * Deprecated: use StreamsJacksonMapper.getInstance() with TwitterDateTimeFormat on the classpath instead
 */
@Deprecated
public class StreamsTwitterMapper extends StreamsJacksonMapper {

    public static final String TWITTER_FORMAT = "EEE MMM dd HH:mm:ss Z yyyy";

    public static final DateTimeFormatter TWITTER_FORMATTER = DateTimeFormat.forPattern(TWITTER_FORMAT);

    public static Long getMillis(String dateTime) {

        // this function is for pig which doesn't handle exceptions well
        try {
            return TWITTER_FORMATTER.parseMillis(dateTime);
        } catch( Exception e ) {
            return null;
        }

    }

    private static final StreamsTwitterMapper INSTANCE = new StreamsTwitterMapper();

    public static StreamsTwitterMapper getInstance(){
        return INSTANCE;
    }

    public StreamsTwitterMapper() {
        super();
        registerModule(new SimpleModule()
        {
            {
                addDeserializer(DateTime.class, new StdDeserializer<DateTime>(DateTime.class) {
                    @Override
                    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException, JsonProcessingException {
                        DateTime result = null;
                        try {
                            result = TWITTER_FORMATTER.parseDateTime(jpar.getValueAsString());
                        } catch( Exception ignored ) { }
                        try {
                            result = RFC3339Utils.getInstance().parseToUTC(jpar.getValueAsString());
                        } catch( Exception ignored ) { }
                        return result;
                    }
                });
            }
        });

    }

}
