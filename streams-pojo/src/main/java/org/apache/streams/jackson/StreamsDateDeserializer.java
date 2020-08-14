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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * StreamsDateDeserializer is a supporting class for
 * @see {@link org.apache.streams.jackson.StreamsJacksonMapper}
 *
 * <p></p>
 * Converting date-time strings other than RFC3339 to java.util.Date objects requires
 * additional formats to be provided when instantiating StreamsJacksonMapper.
 */
public class StreamsDateDeserializer extends StdDeserializer<Date> implements Serializable {

  private List<DateTimeFormatter> formatters = new ArrayList<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsDateDeserializer.class);

  protected StreamsDateDeserializer(Class<Date> dateClass) {
    super(dateClass);
  }

  protected StreamsDateDeserializer(Class<Date> dateClass, List<String> formats) {
    super(dateClass);
    for ( String format : formats ) {
      try {
        formatters.add(DateTimeFormatter.ofPattern(format));
      } catch (Exception ex) {
        LOGGER.warn("Exception parsing format " + format);
      }
    }
  }

  /**
   * Applies each additional format in turn, until it can provide a non-null DateTime
   */
  @Override
  public Date deserialize(JsonParser jpar, DeserializationContext context) throws IOException {

    Date result = Date.from(LocalDateTime.parse(jpar.getValueAsString(), DateTimeFormatter.ISO_INSTANT).atZone(ZoneOffset.UTC).toInstant());
    Iterator<DateTimeFormatter> iterator = formatters.iterator();
    while ( result == null && iterator.hasNext()) {
      DateTimeFormatter formatter = iterator.next();
      result = Date.from(LocalDateTime.parse(jpar.getValueAsString(), formatter).atZone(ZoneOffset.UTC).toInstant());
    }
    return result;
  }
}
