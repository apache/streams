package org.apache.streams.data.util;


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
