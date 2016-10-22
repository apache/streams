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

package org.apache.streams.moreover;

import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.regex.Pattern.matches;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class MoreoverTestUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(MoreoverTestUtil.class);

    public static void test(Activity activity) {
        assertThat(activity, is(not(nullValue())));
        assertThat(activity.getActor(), is(not(nullValue())));
        assertThat(activity.getObject(), is(not(nullValue())));
        if(activity.getObject().getId() != null) {
            assertThat(matches("id:.*:[a-z]*s:[a-zA-Z0-9]*", activity.getObject().getId()), is(true));
        }
        assertThat(activity.getObject().getObjectType(), is(not(nullValue())));
        LOGGER.debug(activity.getPublished().toString());
    }
}
