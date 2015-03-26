package org.apache.streams.instagram.serializer.util;

import org.apache.streams.pojo.json.Activity;
import org.jinstagram.entity.common.Comments;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class InstagramActivityUtilTest {

    /**
     * Ensures that posts with empty comments do not throw NPEs
     */
    @Test
    public void addExtensionsTest() {
        MediaFeedData data = new MediaFeedData();
        Activity activity = new Activity();

        Comments comments = new Comments();
        comments.setComments(null);

        data.setComments(comments);

        InstagramActivityUtil.addInstagramExtensions(activity, data);

        assertTrue(activity != null);
    }

    /**
     * Ensures that we are adding the raw Instagram object into the activity's extensions
     */
    @Test
    public void addInstagramExtensionTest() {
        MediaFeedData data = new MediaFeedData();
        Activity activity = new Activity();

        InstagramActivityUtil.addInstagramExtensions(activity, data);

        assertTrue(((Map<String, Object>)activity.getAdditionalProperties().get("extensions")).containsKey("instagram"));
    }
}
