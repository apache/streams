package org.apache.streams.twitter.test;

import org.apache.streams.pojo.json.Actor;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.serializer.util.TwitterActivityUtil;
import org.junit.Assert;
import org.junit.Test;

public class BuildTwitterActorTest {

    @Test
    public void testBuildTwitterActorImages() {
        Assert.assertTrue(matches("jpg"));
        Assert.assertTrue(matches("jpeg"));
        Assert.assertTrue(matches("png"));
        Assert.assertTrue(matches("gif"));
        Assert.assertTrue(matches("tiff"));
    }

    private boolean matches(String suffix) {
        User user = new User();

        user.setId(134L);
        user.setProfileImageUrlHttps("https://pbs.twimg.com/profile_images/1829730845/laptop_stethoscope_normal." + suffix);

        Actor actor = TwitterActivityUtil.buildActor(user);

        return actor.getImage().getUrl().equals("https://pbs.twimg.com/profile_images/1829730845/laptop_stethoscope." + suffix);
    }
}