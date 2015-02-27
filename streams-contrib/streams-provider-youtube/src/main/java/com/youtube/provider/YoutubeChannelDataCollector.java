package com.youtube.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.youtube.pojo.YoutubeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 *
 */
public class YoutubeChannelDataCollector extends YoutubeDataCollector{

    private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeChannelDataCollector.class);
    private static final String CONTENT = "snippet,contentDetails,statistics,topicDetails";
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();
    private static final int MAX_ATTEMPTS= 5;

    private YouTube youTube;
    private BlockingQueue<StreamsDatum> queue;
    private BackOffStrategy strategy;
    private UserInfo userInfo;
    private YoutubeConfiguration youtubeConfig;

    public YoutubeChannelDataCollector(YouTube youTube, BlockingQueue<StreamsDatum> queue, BackOffStrategy strategy, UserInfo userInfo, YoutubeConfiguration youtubeConfig) {
        this.youTube = youTube;
        this.queue = queue;
        this.strategy = strategy;
        this.userInfo = userInfo;
        this.youtubeConfig = youtubeConfig;
    }

    @Override
    public void run() {
        try {
            int attempt = 0;
             YouTube.Channels.List channelLists = this.youTube.channels().list(CONTENT).setId(this.userInfo.getUserId()).setKey(this.youtubeConfig.getApiKey());
            boolean tryAgain = false;
            do {
                try {
                    List<Channel> channels = channelLists.execute().getItems();
                    for (Channel channel : channels) {
                        this.queue.put(new StreamsDatum(MAPPER.writeValueAsString(channel), channel.getId()));
                    }
                    if (StringUtils.isEmpty(channelLists.getPageToken())) {
                        channelLists = null;
                    } else {
                        channelLists = this.youTube.channels().list(CONTENT).setId(this.userInfo.getUserId()).setOauthToken(this.youtubeConfig.getApiKey())
                                .setPageToken(channelLists.getPageToken());
                    }
                } catch (GoogleJsonResponseException gjre) {
                    LOGGER.warn("GoogleJsonResposneException caught : {}", gjre);
                    tryAgain = backoffAndIdentifyIfRetry(gjre, this.strategy);
                    ++attempt;
                } catch (Throwable t) {
                    LOGGER.warn("Unable to get channel info for id : {}", this.userInfo.getUserId());
                    LOGGER.warn("Excpection thrown while trying to get channel info : {}", t);
                }
            } while((tryAgain && attempt < MAX_ATTEMPTS) || channelLists != null);

        } catch (Throwable t) {
            LOGGER.warn(t.getMessage());
        }
    }


}
