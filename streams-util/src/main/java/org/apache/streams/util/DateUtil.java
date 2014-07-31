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

package org.apache.streams.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/*
 *
 * If you can think of a better way, feel free to implement. This was a great class that I found that
 * solves the majority of the issue I was dealing with.
 *
 * smashew 11=13=2012
 *
 * Site:
 * http://stackoverflow.com/questions/3389348/parse-any-date-in-java
 */

public class DateUtil
{

    private static final String REGEX_ONLY_NUMBERS = "[0-9]+";

	private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>()
	{
		private static final long serialVersionUID = 1L;
		{
			put("^\\d{8}$", "yyyyMMdd");
			put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
			put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
			put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
			put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
			put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
			put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
			put("^\\d{12}$", "yyyyMMddHHmm");
			put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
			put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
			put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
			put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
			put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
			put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
			put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
			put("^\\d{14}$", "yyyyMMddHHmmss");
			put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
			put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
			put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
			put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
			put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
			put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
			put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
		}
	};

	/**
	 * Determine SimpleDateFormat pattern matching with the given date string. Returns null if format is unknown. You
	 * can simply extend DateUtil with more formats if needed.
	 *
	 * @param dateString
	 *             The date string to determine the SimpleDateFormat pattern for.
	 * @return The matching SimpleDateFormat pattern, or null if format is unknown.
	 * @see java.text.SimpleDateFormat
	 */
	public static String determineDateFormat(String dateString)
        throws ParseException
	{
		for (String regexp : DATE_FORMAT_REGEXPS.keySet())
			if (dateString.toLowerCase().matches(regexp))
				return DATE_FORMAT_REGEXPS.get(regexp);

        throw new ParseException("unable to parse date",0);
	}

	public static DateTime determineDate(String dateString)
		throws ParseException
	{
        // Trim the string just in case it is dirty.
        dateString = dateString.trim();

        // check to see if it looks like it is millis. If so, parse as millis and return.
        if(dateString.matches(REGEX_ONLY_NUMBERS))
            return new DateTime(new Date(Long.parseLong(dateString)));

        try
        {
            // try to parse the string into a java.date object, if possible.
            SimpleDateFormat dateFormat = new SimpleDateFormat(determineDateFormat(dateString));
            dateFormat.setLenient(false);
            return new DateTime(dateFormat.parse(dateString));
        }
        catch(Exception e)
        {

        }

        return new DateTime(DateTime.parse(dateString));
	}

    public static DateTime determineDateTime(String dateString)
            throws ParseException
    {
        return new DateTime(determineDate(dateString));
    }

    public static DateTime determineDateTime(String dateString, DateTimeZone theTimeZone)
            throws ParseException
    {
        DateTime beforeTimeZone = determineDateTime(dateString);
        return new DateTime(beforeTimeZone.getYear(),beforeTimeZone.getMonthOfYear(), beforeTimeZone.getDayOfMonth(), beforeTimeZone.getHourOfDay(), beforeTimeZone.getMinuteOfHour(), beforeTimeZone.getSecondOfMinute(), beforeTimeZone.getMillisOfSecond(), theTimeZone);
    }


    public static String getAliasForDate(String date, String prefix) throws ParseException {
        return getAliasesForDateRange(date, null, prefix).iterator().next();
    }

    public static String getAliasForDate(DateTime date, String prefix) throws ParseException {
        return getAliasesForDateRange(date, null, prefix).iterator().next();
    }

    public static Set<String> getAliasesForDateRange(String starDate, String endDate, String prefix)
        throws ParseException
    {
        DateTime start = null;
        DateTime end = null;
        DateTimeFormatter df = ISODateTimeFormat.dateTimeNoMillis();
        try {
            start = df.parseDateTime(starDate);
        } catch (Exception e) {
            //do nothing. try to parse with other parsers
        }
        if(start == null) {
            start = determineDateTime(starDate);
        }
        if(endDate != null) {
            try {
                end = df.parseDateTime(endDate);
            } catch (Exception e) {
                //do nothing. try to parse with other parsers
            }
            if( end == null)
                end = determineDateTime(endDate);
        }
        return getAliasesForDateRange(start, end, prefix);
    }

    public static Set<String> getAliasesForDateRange(DateTime startDate, DateTime endDate, String prefix) {
        Set<String> aliases = new HashSet<String>();
        aliases.add(prefix+"_"+getDateAbbreviation(startDate.getYear(), startDate.getMonthOfYear()));
        if(endDate == null) {
            return aliases;
        }
        while(endDate.isAfter(startDate)) {
            aliases.add(prefix+"_"+getDateAbbreviation(endDate.getYear(), endDate.getMonthOfYear()));
            endDate = endDate.minusMonths(1);
        }
        return aliases;
    }

    private static String getDateAbbreviation(int year, int month) {
        if(month > 9) {
            return Integer.toString(year)+Integer.toString(month);
        }
        else {
            return Integer.toString(year)+"0"+Integer.toString(month);
        }
    }


}
