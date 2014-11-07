package com.google.gplus.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.gplus.serializer.util.GPlusPersonDeserializer;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Collects user profile information for a specific GPlus user
 */
public  class GPlusUserDataCollector implements Runnable{

    private static final Logger LOGGER = LoggerFactory.getLogger(GPlusUserDataCollector.class);
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();
    private static final int MAX_ATTEMPTS = 5;

    static { //set up Mapper for Person objects
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Person.class, new GPlusPersonDeserializer());
        MAPPER.registerModule(simpleModule);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private BackOffStrategy backOffStrategy;
    private Plus gPlus;
    private BlockingQueue<StreamsDatum> datumQueue;
    private UserInfo userInfo;


    public GPlusUserDataCollector(Plus gPlus, BackOffStrategy backOffStrategy, BlockingQueue<StreamsDatum> datumQueue, UserInfo userInfo) {
        this.gPlus = gPlus;
        this.backOffStrategy = backOffStrategy;
        this.datumQueue = datumQueue;
        this.userInfo = userInfo;
    }

    protected void queueUserHistory() {
        try {
            boolean tryAgain = false;
            int attempts = 0;
            com.google.api.services.plus.model.Person person = null;
            do {
                try {
                    person = this.gPlus.people().get(userInfo.getUserId()).execute();
                    this.backOffStrategy.reset();
                    tryAgain = person == null;
                } catch (GoogleJsonResponseException gjre) {
                    switch (gjre.getStatusCode()) {
                        case 400 :
                            LOGGER.warn("Bad Request for user={} : {}", userInfo.getUserId(), gjre);
                            tryAgain = false;
                            break;
                        case 401 :
                            LOGGER.warn("Invalid Credentials : {}", gjre);
                            tryAgain = false;
                        case 403 :
                            LOGGER.warn("Possible rate limit exception. Retrying. : {}", gjre.getMessage());
                            this.backOffStrategy.backOff();
                            tryAgain = true;
                            break;
                        case 503 :
                            LOGGER.warn("Google Backend Service Error : {}", gjre);
                            tryAgain = false;
                            break;
                        default:
                            LOGGER.warn("Google Service returned error : {}", gjre);
                            tryAgain = true;
                            this.backOffStrategy.backOff();
                            break;
                    }
                }
                ++attempts;
            } while(tryAgain && attempts < MAX_ATTEMPTS);
            this.datumQueue.put(new StreamsDatum(MAPPER.writeValueAsString(person), person.getId()));
        } catch (Throwable t) {
            LOGGER.warn("Unable to pull user data for user={} : {}", userInfo.getUserId(), t);
            if(t instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        queueUserHistory();
    }



}
