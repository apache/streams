package org.apache.streams.instagram.provider;

import com.google.common.collect.Sets;
import org.apache.streams.instagram.InstagramUserInformationConfiguration;
import org.jinstagram.Instagram;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Set;

/**
 *
 */
public class InstagramRecentMediaCollector implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramRecentMediaCollector.class);
    protected static final int MAX_ATTEMPTS = 5;
    protected static final int SLEEP_SECS = 5; //5 seconds

    protected Queue dataQueue; //exposed for testing
    private InstagramUserInformationConfiguration config;
    private Instagram instagramClient;
    private volatile boolean isCompleted;


    public InstagramRecentMediaCollector(Queue<MediaFeedData> queue, InstagramUserInformationConfiguration config) {
        this.dataQueue = queue;
        this.config = config;
        this.instagramClient = new Instagram(this.config.getClientId());
        this.isCompleted = false;
    }

    protected void setInstagramClient(Instagram instagramClient) {
        this.instagramClient = instagramClient;
    }

    protected Set<Long> getUserIds() {
        Set<Long> userIds = Sets.newHashSet();
        for(String id : config.getUserIds()) {
            try {
                userIds.add(Long.parseLong(id));
            } catch (NumberFormatException nfe) {
                LOGGER.error("Failed to parse user id, {}, to a long : {}", id, nfe.getMessage());
            }
        }
        return userIds;
    }

    protected void handleInstagramException(InstagramException instaExec, int attempt) throws InstagramException {
        LOGGER.debug("RemainingApiLimitStatus: {}", instaExec.getRemainingLimitStatus());
        if(instaExec.getRemainingLimitStatus() == 0) { //rate limit exception
            long sleepTime = Math.round(Math.pow(SLEEP_SECS, attempt)) * 1000;
            try {
                LOGGER.debug("Encountered rate limit exception, sleeping for {} ms", sleepTime);
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } else {
            LOGGER.error("Instagram returned an excetpion to the user media request : {}", instaExec.getMessage());
            throw instaExec;
        }
    }

    private void getUserMedia(Long userId) {
        MediaFeed feed = null;
        int attempts = 0;
        int count = 0;
        do {
            ++attempts;
            try {
                feed = this.instagramClient.getRecentMediaFeed(userId);
                queueData(feed, userId);
                count += feed.getData().size();
                while(feed != null && feed.getPagination() != null && feed.getPagination().hasNextPage()) {
                    feed = this.instagramClient.getRecentMediaNextPage(feed.getPagination());
                    queueData(feed, userId);
                    count += feed.getData().size();
                }
            } catch (InstagramException ie) {
                try {
                    handleInstagramException(ie, attempts);
                } catch (InstagramException ie2) { //not a rate limit exception, ignore user
                    attempts = MAX_ATTEMPTS;
                }
            }
        } while(feed == null && attempts < MAX_ATTEMPTS);
        LOGGER.debug("For user, {}, received {} MediaFeedData", userId, count);
    }

    private void queueData(MediaFeed userFeed, Long userId) {
        if(userFeed == null) {
            LOGGER.error("User id, {}, returned a NULL media feed from instagram.", userId);
        } else {
            for(MediaFeedData data : userFeed.getData()) {
                synchronized (this.dataQueue) {
                    while(!this.dataQueue.offer(data)) {
                        Thread.yield();
                    }
                }
            }
        }
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }

    @Override
    public void run() {
        for(Long userId : getUserIds()) {
            getUserMedia(userId);
        }
        this.isCompleted = true;
    }
}
