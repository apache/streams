package org.apache.streams.console;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

public class ConsolePersistWriter implements StreamsPersistWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePersistWriter.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    public ConsolePersistWriter(Queue<StreamsDatum> persistQueue) {
        this.persistQueue = persistQueue;
    }

    @Override
    public void start() {
        Preconditions.checkNotNull(persistQueue);
        new Thread(new ConsolePersistWriterTask(this)).start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void setPersistQueue(Queue<StreamsDatum> persistQueue) {
        this.persistQueue = persistQueue;
    }

    @Override
    public Queue<StreamsDatum> getPersistQueue() {
        return this.persistQueue;
    }

    @Override
    public void write(StreamsDatum entry) {

        try {

            String text = mapper.writeValueAsString(entry);

            System.out.println(text);

        } catch (JsonProcessingException e) {
            LOGGER.warn("save: {}", e);
        }

    }

}
