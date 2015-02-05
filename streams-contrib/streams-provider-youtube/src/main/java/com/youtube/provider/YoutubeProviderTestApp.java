package com.youtube.provider;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.youtube.pojo.YoutubeConfiguration;

import java.util.List;
import java.util.Map;

/**
 * Created by rdouglas on 2/4/15.
 */
public class YoutubeProviderTestApp {
    public static void main(String[] args) {
        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 1000 * 60 * 5);
        LocalStreamBuilder builder = new LocalStreamBuilder(5000, streamConfig);

        YoutubeConfiguration youtubeConfiguration = new YoutubeConfiguration();
        UserInfo user = new UserInfo();
        user.setUserId("UCNENOn2nmwguQYkejKhJGPQ");

        List<UserInfo> users = Lists.newArrayList();
        users.add(user);

        youtubeConfiguration.setYoutubeUsers(users);
        youtubeConfiguration.setApiKey("AIzaSyCQjyvWifeuFqWLqIl7zD5yyfU5suAb8uU");
        builder.newPerpetualStream("GPlusUserActivityProvider", new YoutubeUserActivityProvider(youtubeConfiguration));
        builder.start();
    }
}