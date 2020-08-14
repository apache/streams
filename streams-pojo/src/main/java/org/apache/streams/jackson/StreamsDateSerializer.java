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

package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.Serializable;

/**
 * StreamsDateSerializer is a supporting class for
 * @link org.apache.streams.jackson.StreamsJacksonMapper.
 */
public class StreamsDateSerializer extends StdSerializer<Date> implements Serializable {

  protected StreamsDateSerializer(Class<Date> dateTimeClass) {
    super(dateTimeClass);
  }

  @Override
  public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeString(ZonedDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
  }
}
