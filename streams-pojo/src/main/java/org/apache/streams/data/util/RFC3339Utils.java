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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and formats Joda Time {@link org.joda.time.DateTime} dates to and from RFC3339 compatible Strings
 */
public class RFC3339Utils {
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
    private static final DateTimeFormatter UTC_STANDARD_FMT = DateTimeFormat.forPattern(BASE_FMT + "'Z'").withZoneUTC();
    private static final DateTimeFormatter LOCAL_STANDARD_FMT = DateTimeFormat.forPattern(BASE_FMT + "Z").withZoneUTC();


    private RFC3339Utils() {}

    public static DateTime parseUTC(String toParse) {
        if(MILLIS.matcher(toParse).matches()) {
            return new DateTime(Long.valueOf(toParse));
        }
        if(UTC_STANDARD.matcher(toParse).matches()) {
            return parseUTC(UTC_STANDARD_FMT, toParse);
        }
        Matcher utc = UTC_SUB_SECOND.matcher(toParse);
        if(utc.matches()) {
            return parseUTC(getSubSecondFormat(utc.group(1), "'Z'"), toParse);
        }
        if(LOCAL_STANDARD.matcher(toParse).matches()) {
            return parseUTC(LOCAL_STANDARD_FMT, toParse);
        }
        Matcher local = LOCAL_SUB_SECOND.matcher(toParse);
        if(local.matches()) {
            return parseUTC(getSubSecondFormat(local.group(1), "Z"), toParse);
        }
        throw new IllegalArgumentException(String.format("Failed to parse date %s. Ensure format is RFC3339 Compliant", toParse));
    }

    public static String format(DateTime toFormat) {
        return UTC_STANDARD_FMT.print(toFormat.getMillis());
    }

    public static String format(DateTime toFormat, Locale locale) {
        return LOCAL_STANDARD_FMT.withLocale(locale).print(toFormat.getMillis());
    }

    private static DateTime parseUTC(DateTimeFormatter formatter, String toParse) {
        return formatter.parseDateTime(toParse);
    }

    private static DateTimeFormatter getSubSecondFormat(String sub, String suffix) {
        StringBuilder pattern = new StringBuilder();
        pattern.append(BASE_FMT);
        pattern.append(".");
        for(int i=0; i<sub.length(); i++) {
            pattern.append("S");
        }
        pattern.append(suffix);
        return DateTimeFormat.forPattern(pattern.toString()).withZoneUTC();
    }
}
