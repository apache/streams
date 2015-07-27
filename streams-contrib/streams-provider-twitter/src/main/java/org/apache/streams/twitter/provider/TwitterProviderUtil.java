package org.apache.streams.twitter.provider;

import org.apache.streams.twitter.TwitterUserInformationConfiguration;

/**
 * Created by sblackmon on 7/26/15.
 */
public class TwitterProviderUtil {

    public static String baseUrl(TwitterUserInformationConfiguration config) {

        String baseUrl = new StringBuilder()
                .append(config.getProtocol())
                .append("://")
                .append(config.getHost())
                .append(":")
                .append(config.getPort())
                .append("/")
                .append(config.getVersion())
                .append("/")
                .toString();

        return baseUrl;
    }
}
