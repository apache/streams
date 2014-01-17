package org.apache.streams.console;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsolePersistWriter implements StreamsPersistWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePersistWriter.class);

    protected volatile Queue<StreamsDatum> persistQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    private ObjectMapper mapper = new ObjectMapper();

    public ConsolePersistWriter(Queue<StreamsDatum> persistQueue) {
        this.persistQueue = persistQueue;
    }

    @Override
    public void start() {
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
