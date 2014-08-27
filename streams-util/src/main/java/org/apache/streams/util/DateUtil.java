package org.apache.streams.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
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
            put("^[a-z]{3}\\s[a-z]{4,}\\s\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\-{1}\\d{4}\\s\\d{4}$", "EEE MMMM dd HH:mm:ss Z yyyy");
            put("^\\d{2}\\s[a-z]{3,}\\s\\d{4}\\s\\d{2}:\\d{2}:\\d{2}\\s[a-z]{3}$", "dd MMM yyyy HH:mm:ss ZZZZ");               // 06 Nov 2013 00:10:16 EST
            put("^\\w{3}\\s\\w{3,5}\\s\\d{1,2}\\s\\d{2}\\:\\d{2}:\\d{2}\\s\\+\\d{4}\\s\\d{4}$", "EEE MMM dd HH:mm:ss Z yyyy");  // Thu Apr 12 12:28:55 +0000 2012
		}
	};


    /**
     * Determine SimpleDateFormat pattern matching with the given date string. Returns null if format is unknown. You
     * can simply extend DateUtil with more formats if needed.
     *
     * @param dateString The date string to determine the SimpleDateFormat pattern for.
     * @return The matching SimpleDateFormat pattern, or null if format is unknown.
     * @see SimpleDateFormat
     */
    public static String determineDateFormat(String dateString) {
        for (String regexp : DATE_FORMAT_REGEXPS.keySet())
            if (dateString.toLowerCase().matches(regexp))
                return DATE_FORMAT_REGEXPS.get(regexp);
        return null;
    }

    public static Date determineDate(String dateString) throws ParseException {
        try {
            // Trim the string just in case it is dirty.
            dateString = dateString.trim();

            // check to see if it looks like it is millis. If so, parse as millis and return.
            if (dateString.matches(REGEX_ONLY_NUMBERS))
                return new Date(Long.parseLong(dateString));

            // try to parse the string into a java.date object, if possible.
            String stringFormat = determineDateFormat(dateString);
            if (stringFormat != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(stringFormat);
                dateFormat.setLenient(false);

                return dateFormat.parse(dateString);
            } else {
                return new DateTime(DateTime.parse(dateString)).toDate();
            }
        }
        catch(Throwable e) {
            throw new ParseException("Unable to parse", 0);
        }
    }

    public static DateTime determineDateTime(String dateString) throws ParseException {
        return new DateTime(determineDate(dateString));
    }

    public static DateTime determineJodaDateTime(String dateString) throws ParseException {
        try {
            dateString = dateString.trim();
            String formatString = determineDateFormat(dateString);
            if (formatString != null) {
                // EST is non-deterministic, :. it must be converted to something deterministic
                if(dateString.contains(" EST"))
                    dateString = dateString.replace(" EST", " America/New_York");
                if(formatString.contains("Z"))
                    return DateTimeFormat.forPattern(formatString).withOffsetParsed().parseDateTime(dateString);
                else
                    return DateTimeFormat.forPattern(formatString).parseDateTime(dateString);
            } else {
                return DateTime.parse(dateString);
            }
        }
        catch(Throwable e) {
            e.printStackTrace();
            throw new ParseException("Unable to parse: " + dateString, 0);
        }
    }

    public static DateTime determineDateTime(String dateString, DateTimeZone theTimeZone) throws ParseException {
        DateTime beforeTimeZone = determineDateTime(dateString);
        return new DateTime(beforeTimeZone.getYear(), beforeTimeZone.getMonthOfYear(), beforeTimeZone.getDayOfMonth(), beforeTimeZone.getHourOfDay(), beforeTimeZone.getMinuteOfHour(), beforeTimeZone.getSecondOfMinute(), beforeTimeZone.getMillisOfSecond(), theTimeZone);
    }

    public static String getAliasForDate(String date, String prefix) throws ParseException {
        return getAliasesForDateRange(date, null, prefix).iterator().next();
    }

    public static String getAliasForDate(DateTime date, String prefix) throws ParseException {
        return getAliasesForDateRange(date, null, prefix).iterator().next();
    }

    public static Set<String> getAliasesForDateRange(String starDate, String endDate, String prefix) throws ParseException {
        DateTime start = null;
        DateTime end = null;
        DateTimeFormatter df = ISODateTimeFormat.dateTimeNoMillis();
        try {
            start = df.parseDateTime(starDate);
        } catch (Exception e) {
            //do nothing. try to parse with other parsers
        }
        if (start == null) {
            start = determineDateTime(starDate);
        }
        if (endDate != null) {
            try {
                end = df.parseDateTime(endDate);
            } catch (Exception e) {
                //do nothing. try to parse with other parsers
            }
            if (end == null)
                end = determineDateTime(endDate);
        }
        return getAliasesForDateRange(start, end, prefix);
    }

    public static Set<String> getAliasesForDateRange(DateTime startDate, DateTime endDate, String prefix) {
        Set<String> aliases = new HashSet<String>();
        aliases.add(prefix + "_" + getDateAbbreviation(startDate.getYear(), startDate.getMonthOfYear()));
        if (endDate == null) {
            return aliases;
        }
        while (endDate.isAfter(startDate)) {
            aliases.add(prefix + "_" + getDateAbbreviation(endDate.getYear(), endDate.getMonthOfYear()));
            endDate = endDate.minusMonths(1);
        }
        return aliases;
    }

    private static String getDateAbbreviation(int year, int month) {
        if (month > 9) {
            return Integer.toString(year) + Integer.toString(month);
        } else {
            return Integer.toString(year) + "0" + Integer.toString(month);
        }
    }
}
