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

package org.apache.streams.juneau;

import org.apache.commons.lang3.StringUtils;
import org.apache.juneau.BeanSession;
import org.apache.juneau.ClassMeta;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.transform.StringSwap;
import org.apache.streams.data.util.RFC3339Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Transforms {@link Date} to {@link String Strings}.
 */
public class DateSwap extends StringSwap<Date> {

  DateTimeFormatter dateTimeFormatter;

  /**
   * Constructor.
   */
  public DateSwap() {
    dateTimeFormatter = DateTimeFormatter.ISO_INSTANT;
  }

  @Override /* PojoSwap */
  public String swap(BeanSession session, Date o) {
    DateTimeFormatter dateTimeFormatter = this.dateTimeFormatter;
    if( StringUtils.isNotBlank(session.getProperty("format", String.class, RFC3339Utils.UTC_STANDARD_FMT.toString()))) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(session.getProperty("format", String.class, RFC3339Utils.UTC_STANDARD_FMT.toString()));
    }
    return ZonedDateTime.ofInstant(o.toInstant(), ZoneOffset.UTC).format(dateTimeFormatter);
  }

  @Override /* PojoSwap */
  public Date unswap(BeanSession session, String s, ClassMeta<?> hint) {
    DateTimeFormatter dateTimeFormatter = this.dateTimeFormatter;
    if( StringUtils.isNotBlank(session.getProperty("format", String.class, RFC3339Utils.UTC_STANDARD_FMT.toString()))) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(session.getProperty("format", String.class, RFC3339Utils.UTC_STANDARD_FMT.toString()));
    }
    return Date.from(LocalDateTime.parse(s, dateTimeFormatter).atZone(ZoneOffset.UTC).toInstant());
  }

}
