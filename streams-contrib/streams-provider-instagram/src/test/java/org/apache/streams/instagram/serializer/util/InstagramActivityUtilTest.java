package org.apache.streams.instagram.serializer.util;

import org.apache.streams.pojo.json.Activity;
import org.jinstagram.entity.common.Comments;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.junit.Test;

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

        assert(activity != null);
    }
}
