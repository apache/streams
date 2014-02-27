package org.apache.streams.console;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.tasks.StreamsPersistWriterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsolePersistWriter implements StreamsPersistWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePersistWriter.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    public ConsolePersistWriter() {
        this.persistQueue = new ConcurrentLinkedQueue<StreamsDatum>();
    }

    public ConsolePersistWriter(Queue<StreamsDatum> persistQueue) {
        this.persistQueue = persistQueue;
    }

    public void prepare(Object o) {
        Preconditions.checkNotNull(persistQueue);
    }

    public void cleanUp() {

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
