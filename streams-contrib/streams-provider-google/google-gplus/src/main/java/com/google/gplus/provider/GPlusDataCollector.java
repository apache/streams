package com.google.gplus.provider;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.apache.streams.util.api.requests.backoff.BackOffException;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class GPlusDataCollector implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GPlusDataCollector.class);


    /**
     * Looks at the status code of the expception.  If the code indicates that the request should be retried,
     * it executes the back off strategy and returns true.
     * @param gjre
     * @param backOff
     * @return returns true if the error code of the exception indicates the request should be retried.
     */
    public boolean backoffAndIdentifyIfRetry(GoogleJsonResponseException gjre, BackOffStrategy backOff) throws BackOffException {
        boolean tryAgain = false;
        switch (gjre.getStatusCode()) {
            case 400 :
                LOGGER.warn("Bad Request  : {}",  gjre);
                break;
            case 401 :
                LOGGER.warn("Invalid Credentials : {}", gjre);
            case 403 :
                LOGGER.warn("Possible rate limit exception. Retrying. : {}", gjre.getMessage());
                backOff.backOff();
                tryAgain = true;
                break;
            case 503 :
                LOGGER.warn("Google Backend Service Error : {}", gjre);
                break;
            default:
                LOGGER.warn("Google Service returned error : {}", gjre);
                tryAgain = true;
                backOff.backOff();
                break;
        }
        return tryAgain;
    }


}
