package com.google.gplus.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;
import com.google.gplus.serializer.util.GPlusActivityDeserializer;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.api.requests.backoff.BackOffException;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Collects the public activities of a GPlus user. Has ability to filter by date ranges.
 */
public class GPlusUserActivityCollector extends GPlusDataCollector {

    /**
     * Key for all public activities
     * https://developers.google.com/+/api/latest/activities/list
     */
    private static final String PUBLIC_COLLECTION = "public";
    /**
     * Max results allowed per request
     * https://developers.google.com/+/api/latest/activities/list
     */
    private static final long MAX_RESULTS = 100;
    private static final int MAX_ATTEMPTS = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(GPlusUserActivityCollector.class);
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    static { //set up mapper for Google Activity Object
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Activity.class, new GPlusActivityDeserializer());
        MAPPER.registerModule(simpleModule);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private BlockingQueue<StreamsDatum> datumQueue;
    private BackOffStrategy backOff;
    private Plus gPlus;
    private UserInfo userInfo;

    public GPlusUserActivityCollector(Plus gPlus, BlockingQueue<StreamsDatum> datumQueue, BackOffStrategy backOff, UserInfo userInfo) {
        this.gPlus = gPlus;
        this.datumQueue = datumQueue;
        this.backOff = backOff;
        this.userInfo = userInfo;
    }

    @Override
    public void run() {
        collectActivityData();
    }

    protected void collectActivityData() {
        try {
            ActivityFeed feed = null;
            boolean tryAgain = false;
            int attempt = 0;
            DateTime afterDate = userInfo.getAfterDate();
            DateTime beforeDate = userInfo.getBeforeDate();
            do {
                try {
                    if(feed == null) {
                        feed = this.gPlus.activities().list(this.userInfo.getUserId(), PUBLIC_COLLECTION).setMaxResults(MAX_RESULTS).execute();
                    } else {
                        feed = this.gPlus.activities().list(this.userInfo.getUserId(), PUBLIC_COLLECTION).setMaxResults(MAX_RESULTS).setPageToken(feed.getNextPageToken()).execute();
                    }
                    this.backOff.reset(); //successful pull reset api.
                    for(com.google.api.services.plus.model.Activity activity : feed.getItems()) {
                        DateTime published = new DateTime(activity.getPublished().getValue());
                        if(        (afterDate == null && beforeDate == null)
                                || (beforeDate == null && afterDate.isBefore(published))
                                || (afterDate == null && beforeDate.isAfter(published))
                                || ((afterDate != null && beforeDate != null) && (afterDate.isBefore(published) && beforeDate.isAfter(published)))) {
                            this.datumQueue.put(new StreamsDatum(MAPPER.writeValueAsString(activity), activity.getId()));
                        } else if(afterDate != null && afterDate.isAfter(published)) {
                            feed.setNextPageToken(null); // do not fetch next page
                            break;
                        }
                    }
                } catch (GoogleJsonResponseException gjre) {
                    tryAgain = backoffAndIdentifyIfRetry(gjre, this.backOff);
                    ++attempt;
                }
            } while((tryAgain || (feed != null && feed.getNextPageToken() != null)) && attempt < MAX_ATTEMPTS);
        } catch (Throwable t) {
            if(t instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            t.printStackTrace();
            LOGGER.warn("Unable to pull Activities for user={} : {}",this.userInfo.getUserId(), t);
        }
    }



}
