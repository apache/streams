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

package org.apache.streams.data.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and formats dates to Joda Time {@link org.joda.time.DateTime} and to RFC3339 compatible Strings
 */
public class RFC3339Utils {

  private static final RFC3339Utils INSTANCE = new RFC3339Utils();

  public static RFC3339Utils getInstance() {
    return INSTANCE;
  }

  private static final String BASE = "^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}";
  private static final String TZ = "[+-][0-9]{2}:?[0-9]{2}$";
  private static final String SUB_SECOND = "\\.([0-9]*)";
  private static final String UTC = "Z$";


  private static final Pattern MILLIS = Pattern.compile("^[0-9]*$");
  private static final Pattern UTC_STANDARD = Pattern.compile(BASE + UTC);
  private static final Pattern UTC_SUB_SECOND = Pattern.compile(BASE + SUB_SECOND + UTC);
  private static final Pattern LOCAL_STANDARD = Pattern.compile(BASE + TZ);
  private static final Pattern LOCAL_SUB_SECOND = Pattern.compile(BASE + SUB_SECOND + TZ);

  private static final String BASE_FMT = "yyyy-MM-dd'T'HH:mm:ss";
  public static final DateTimeFormatter UTC_STANDARD_FMT = DateTimeFormat.forPattern(BASE_FMT + "'Z'").withZoneUTC();
  public static final DateTimeFormatter UTC_SUB_SECOND_FMT = DateTimeFormat.forPattern(BASE_FMT + ".SSS'Z'").withZoneUTC();
  public static final DateTimeFormatter LOCAL_STANDARD_FMT = DateTimeFormat.forPattern(BASE_FMT + "Z").withZoneUTC();
  public static final DateTimeFormatter LOCAL_SUB_SECOND_FMT = DateTimeFormat.forPattern(BASE_FMT + ".SSSZ").withZoneUTC();

  /**
   * Contains various formats.  All formats should be of international standards when comes to the ordering of the
   * days and month.
   */
  private static final  DateTimeFormatter DEFAULT_FORMATTER;
  /**
   * Contains alternative formats that will succeed after failures from the DEFAULT_FORMATTER.
   * i.e. 4/24/2014 will throw an exception on the default formatter because it will assume international date standards
   * However, the date will parse in the ALT_FORMATTER because it contains the US format of MM/dd/yyyy.
   */
  private static final DateTimeFormatter ALT_FORMATTER;

  static {
    DateTimeParser[] parsers = new DateTimeParser[]{
        DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").getParser(),
        DateTimeFormat.forPattern("dd MMMM yyyy HH:mm:ss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd-MM-yyyy").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyy/MM/dd").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd MMM yyyy").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd MMMM yyyy").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyyMMddHHmm").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyyMMdd HHmm").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd-MM-yyyy HH:mm").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyy/MM/dd HH:mm").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd MMM yyyy HH:mm").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd MMMM yyyy HH:mm").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyyMMddHHmmss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyyMMdd HHmmss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("HH:mm:ss yyyy/MM/dd").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("HH:mm:ss MM/dd/yyyy").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("HH:mm:ss yyyy-MM-dd").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("HH:mm:ss MM-dd-yyyy").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC().getParser(),
        UTC_STANDARD_FMT.getParser(),
        UTC_SUB_SECOND_FMT.getParser(),
        LOCAL_STANDARD_FMT.getParser(),
        LOCAL_SUB_SECOND_FMT.getParser()
    };
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.append(null, parsers);
    DEFAULT_FORMATTER = builder.toFormatter().withZoneUTC();

    DateTimeParser[] altParsers = new DateTimeParser[] {
        DateTimeFormat.forPattern("MM-dd-yyyy HH:mm:ss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("MM/dd/yyyy HH:mm").withZoneUTC().getParser(),
        DateTimeFormat.forPattern("MM/dd/yyyy").withZoneUTC().getParser(),
    };
    builder = new DateTimeFormatterBuilder();
    builder.append(null, altParsers);
    ALT_FORMATTER = builder.toFormatter().withZoneUTC();
  }

  private RFC3339Utils() {}

  /**
   * parse String to DateTime
   * @param toParse DateTime as UTC String
   * @return DateTime
   */
  public static DateTime parseUTC(String toParse) {
    if (MILLIS.matcher(toParse).matches()) {
      return new DateTime(Long.valueOf(toParse), DateTimeZone.UTC);
    }
    if (UTC_STANDARD.matcher(toParse).matches()) {
      return parseUTC(UTC_STANDARD_FMT, toParse);
    }
    Matcher utc = UTC_SUB_SECOND.matcher(toParse);
    if (utc.matches()) {
      return parseUTC(getSubSecondFormat(utc.group(1), "'Z'"), toParse);
    }
    if (LOCAL_STANDARD.matcher(toParse).matches()) {
      return parseUTC(LOCAL_STANDARD_FMT, toParse);
    }
    Matcher local = LOCAL_SUB_SECOND.matcher(toParse);
    if (local.matches()) {
      return parseUTC(getSubSecondFormat(local.group(1), "Z"), toParse);
    }
    throw new IllegalArgumentException(String.format("Failed to parse date %s. Ensure format is RFC3339 Compliant", toParse));
  }

  private static DateTime parseUTC(DateTimeFormatter formatter, String toParse) {
    return formatter.parseDateTime(toParse);
  }

  /**
   * Parses arbitrarily formatted Strings representing dates or dates and times to a {@link org.joda.time.DateTime}
   * objects.  It first attempts parse with international standards, assuming the dates are either dd MM yyyy or
   * yyyy MM dd.  If that fails it will try American formats where the month precedes the days of the month.
   * @param dateString abitrarily formatted date or date and time string
   * @return {@link org.joda.time.DateTime} representation of the dateString
   */
  public static DateTime parseToUTC(String dateString) {
    if (MILLIS.matcher(dateString).find()) {
      return new DateTime(Long.parseLong(dateString));
    }
    try {
      return DEFAULT_FORMATTER.parseDateTime(dateString);
    } catch (Exception ex) {
      return ALT_FORMATTER.parseDateTime(dateString);
    }
  }

  /**
   * Formats an arbitrarily formatted into RFC3339 Specifications.
   * @param dateString date string to be formatted
   * @return RFC3339 compliant date string
   */
  public static String format(String dateString) {
    return format(parseToUTC(dateString));
  }

  public static String format(DateTime toFormat) {
    return UTC_SUB_SECOND_FMT.print(toFormat.getMillis());
  }

  public static String format(DateTime toFormat, TimeZone tz) {
    return LOCAL_SUB_SECOND_FMT.withZone(DateTimeZone.forTimeZone(tz)).print(toFormat.getMillis());
  }

  private static DateTimeFormatter getSubSecondFormat(String sub, String suffix) {
    DateTimeFormatter result;
    //Since RFC3339 allows for any number of sub-second notations, we need to flexibly support more or less than 3
    //digits; however, if it is exactly 3, just use the standards.
    if (sub.length() == 3) {
      result = suffix.equals("Z") ? LOCAL_SUB_SECOND_FMT : UTC_SUB_SECOND_FMT;
    } else {
      StringBuilder pattern = new StringBuilder();
      pattern.append(BASE_FMT);
      pattern.append(".");
      for (int i = 0; i < sub.length(); i++) {
        pattern.append("S");
      }
      pattern.append(suffix);
      result = DateTimeFormat.forPattern(pattern.toString()).withZoneUTC();
    }
    return result;
  }
}
