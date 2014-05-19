package org.apache.streams.data.util;

/*
 * #%L
 * streams-provider-moreover
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.streams.pojo.json.Activity;

import static java.util.regex.Pattern.matches;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class MoreoverTestUtil {

    public static void test(Activity activity) {
        assertThat(activity, is(not(nullValue())));
        assertThat(activity.getActor(), is(not(nullValue())));
        assertThat(activity.getObject(), is(not(nullValue())));
        if(activity.getObject().getId() != null) {
            assertThat(matches("id:.*:[a-z]*s:[a-zA-Z0-9]*", activity.getObject().getId()), is(true));
        }
        assertThat(activity.getObject().getObjectType(), is(not(nullValue())));
        System.out.println(activity.getPublished());
    }
}
