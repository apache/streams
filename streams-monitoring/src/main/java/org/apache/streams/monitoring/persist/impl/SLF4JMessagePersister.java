package org.apache.streams.monitoring.persist.impl;

import org.apache.streams.monitoring.persist.MessagePersister;
import org.slf4j.Logger;

import java.util.List;

public class SLF4JMessagePersister implements MessagePersister {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SLF4JMessagePersister.class);
    private static final int SUCCESS_STATUS = 0;
    private static final int FAILURE_STATUS = -1;

    public SLF4JMessagePersister() {

    }

    @Override
    public int persistMessages(List<String> messages) {
        for(String message : messages) {
            LOGGER.info(message);
        }

        return SUCCESS_STATUS;
    }
}
