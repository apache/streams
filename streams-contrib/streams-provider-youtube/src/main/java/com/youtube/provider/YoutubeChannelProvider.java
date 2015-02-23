package com.youtube.provider;

import com.google.api.services.youtube.YouTube;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

import java.util.concurrent.BlockingQueue;

/**
 *
 */
public class YoutubeChannelProvider extends YoutubeProvider {

    @Override
    protected Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, YouTube youtube, UserInfo userInfo) {
        return new YoutubeChannelDataCollector(youtube, queue, strategy, userInfo, this.config);
    }
}
