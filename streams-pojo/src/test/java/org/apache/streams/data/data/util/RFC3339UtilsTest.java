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


import org.joda.time.DateTime;
import org.junit.Test;

import static org.apache.streams.data.util.RFC3339Utils.parseUTC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        System.out.println(parsed.getMillis());
    }
}
