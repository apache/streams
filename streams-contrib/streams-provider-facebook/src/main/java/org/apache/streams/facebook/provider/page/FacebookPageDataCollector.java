package org.apache.streams.facebook.provider.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import facebook4j.*;
import facebook4j.json.DataObjectFactory;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.IdConfig;
import org.apache.streams.facebook.provider.FacebookDataCollector;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Collects the page data from public Facebook pages
 */
public class FacebookPageDataCollector extends FacebookDataCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookPageDataCollector.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    public FacebookPageDataCollector(BlockingQueue<StreamsDatum> queue, FacebookConfiguration configuration) {
        super(configuration, queue);
    }

    @Override
    protected void getData(IdConfig id) throws Exception {
        Page responsePage = getPage(id.getId());
        backOff.reset();

        if(responsePage != null) {
            super.outputData(MAPPER.readValue(DataObjectFactory.getRawJSON(responsePage), org.apache.streams.facebook.Page.class), responsePage.getId());
        }
    }

    protected Page getPage(String pageId) throws Exception {
        int attempt = 0;
        while(attempt < MAX_ATTEMPTS) {
            ++attempt;
            try {
                Page page = getNextFacebookClient().getPage(pageId);
                return page;
            } catch (FacebookException fe) {
                LOGGER.error("Facebook returned an exception : {}", fe);
                LOGGER.error("Facebook returned an exception while trying to get feed for page, {} : {}", pageId, fe.getMessage());

                int errorCode = fe.getErrorCode();

                //Some sort of rate limiting
                if(errorCode == 17 || errorCode == 4 || errorCode == 341) {
                    super.backOff.backOff();
                }
            }
        }
        throw new Exception("Failed to get data from facebook after "+MAX_ATTEMPTS);
    }

    @VisibleForTesting
    protected BlockingQueue<StreamsDatum> getQueue() {
        return super.getQueue();
    }
}