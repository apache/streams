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
package org.apache.streams.data.data.util;


import org.apache.streams.data.util.RFC3339Utils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.TimeZone;

import static org.apache.streams.data.util.RFC3339Utils.format;
import static org.apache.streams.data.util.RFC3339Utils.parseUTC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RFC3339UtilsTest {

    @Test
    public void validUTC() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00Z");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(12)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
    }

    @Test
    public void validUTCSubSecond() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00.7Z");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(12)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
        assertThat(parsed.millisOfSecond().get(), is(equalTo(700)));
    }

    @Test
    public void validUTCSubSecondMultiDigit() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00.7343Z");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(12)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
        assertThat(parsed.millisOfSecond().get(), is(equalTo(734)));
    }

    @Test
    public void validEST() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00-05:00");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(17)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
    }

    @Test
    public void validESTSubSecond() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00.7-05:00");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(17)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
        assertThat(parsed.millisOfSecond().get(), is(equalTo(700)));
    }

    @Test
    public void validESTSubSecondMultiDigit() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00.7343-05:00");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(17)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
        assertThat(parsed.millisOfSecond().get(), is(equalTo(734)));
    }

    @Test
    public void validESTNoSeparator() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00-0500");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(17)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
    }

    @Test
    public void validESTSubSecondNoSeparator() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00.7-0500");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(17)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
        assertThat(parsed.millisOfSecond().get(), is(equalTo(700)));
    }

    @Test
    public void validESTSubSecondMultiDigitNoSeparator() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00.7343-0500");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(17)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
        assertThat(parsed.millisOfSecond().get(), is(equalTo(734)));
    }

    @Test
    public void validCET() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00+01:00");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(11)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
    }

    @Test
    public void validCETSubSecond() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00.7+01:00");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(11)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
        assertThat(parsed.millisOfSecond().get(), is(equalTo(700)));
    }

    @Test
    public void validCETSubSecondMultidigit() {
        DateTime parsed = parseUTC("2014-12-25T12:00:00.7343+01:00");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(11)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
        assertThat(parsed.millisOfSecond().get(), is(equalTo(734)));
    }

    @Test
    public void validLong() {
        DateTime parsed = parseUTC("1419505200734");
        assertThat(parsed.minuteOfHour().get(), is(equalTo(0)));
        assertThat(parsed.hourOfDay().get(), is(equalTo(11)));
        assertThat(parsed.dayOfMonth().get(), is(equalTo(25)));
        assertThat(parsed.monthOfYear().get(), is(equalTo(12)));
        assertThat(parsed.millisOfSecond().get(), is(equalTo(734)));
    }

    @Test
    public void validFormatUTC() {
        DateTime parsed = new DateTime(1419505200734L);
        assertThat(format(parsed), is(equalTo("2014-12-25T11:00:00.734Z")));
    }

    @Test
    public void validFormat() {
        TimeZone cet = TimeZone.getTimeZone("CET");
        DateTime parsed = new DateTime(1419505200734L);
        assertThat(format(parsed, cet), is(equalTo("2014-12-25T12:00:00.734+0100")));
    }

    @Test
    public void testParseVariousDateFormats() {
        String date = "Thu April 24 04:43:10 -0500 2014";
        DateTime expected = new DateTime(2014, 4, 24, 9, 43, 10, DateTimeZone.forOffsetHours(0));
        testHelper(expected, date);
        date = "2014/04/24 04:43:10";
        expected = new DateTime(2014, 4, 24, 4, 43, 10, DateTimeZone.forOffsetHours(0));
        testHelper(expected, date);
        date = "2014-04-24T04:43:10Z";
        testHelper(expected, date);
        date = "04:43:10 2014/04/24";
        testHelper(expected, date);
        date = "4/24/2014 04:43:10";
        testHelper(expected, date);
        date = "04:43:10 4/24/2014";
        testHelper(expected, date);
        date = "04:43:10 2014-04-24";
        testHelper(expected, date);
        date = "4-24-2014 04:43:10";
        testHelper(expected, date);
        date = "04:43:10 4-24-2014";
        testHelper(expected, date);
        expected = new DateTime(2014, 4, 24, 0, 0, 0, DateTimeZone.forOffsetHours(0));
        date = "24-4-2014";
        testHelper(expected, date);
        date = "2014-4-24";
        testHelper(expected, date);
        date = "2014/4/24";
        testHelper(expected, date);
        date = "2014/4/24 fesdfs";
        try {
            RFC3339Utils.parseToUTC(date);
            fail("Should not have been able to parse : "+date);
        } catch (Exception e) {
        }
    }

    private void testHelper(DateTime expected, String dateString) {
        DateTime parsedDate = RFC3339Utils.parseToUTC(dateString);
        assertEquals("Failed to parse : "+dateString, expected, parsedDate);
        String rfc3339String = RFC3339Utils.format(dateString);
        String parsedRfc3339String = RFC3339Utils.format(parsedDate);
        assertEquals("Parsed String should be equal.", parsedRfc3339String, rfc3339String);
        DateTime convertedBack = RFC3339Utils.parseToUTC(parsedRfc3339String);
        assertEquals(expected, convertedBack);
    }
}
