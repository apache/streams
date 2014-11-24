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

package org.apache.streams.datasift.util;

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
 * Created by sblackmon on 3/27/14.
 *
 * Depracated: Use StreamsJacksonMapper instead
 */
@Deprecated()
public class StreamsDatasiftMapper extends StreamsJacksonMapper {

    public static final String DATASIFT_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";

    public static final DateTimeFormatter DATASIFT_FORMATTER = DateTimeFormat.forPattern(DATASIFT_FORMAT);

    public static final Long getMillis(String dateTime) {

        // this function is for pig which doesn't handle exceptions well
        try {
            Long result = DATASIFT_FORMATTER.parseMillis(dateTime);
            return result;
        } catch( Exception e ) {
            return null;
        }

    }

    private static final StreamsDatasiftMapper INSTANCE = new StreamsDatasiftMapper();

    public static StreamsDatasiftMapper getInstance(){
        return INSTANCE;
    }

    public StreamsDatasiftMapper() {
        super();
        registerModule(new SimpleModule()
        {
            {
                addDeserializer(DateTime.class, new StdDeserializer<DateTime>(DateTime.class) {
                    @Override
                    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException, JsonProcessingException {
                        DateTime result = null;
                        try {
                            result = DATASIFT_FORMATTER.parseDateTime(jpar.getValueAsString());
                        } catch (Exception e) {}
                        if (result == null) {
                            try {
                                result = RFC3339Utils.getInstance().parseToUTC(jpar.getValueAsString());
                            } catch (Exception e) {}
                        }
                        return result;
                    }
                });
            }
        });

    }

}
