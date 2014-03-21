package org.apache.streams.console;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsolePersistReader implements StreamsPersistReader {

    private final static String STREAMS_ID = "ConsolePersistReader";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePersistReader.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    public ConsolePersistReader() {
        this.persistQueue = new ConcurrentLinkedQueue<StreamsDatum>();
    }

    public ConsolePersistReader(Queue<StreamsDatum> persistQueue) {
        this.persistQueue = persistQueue;
    }

    public void prepare(Object o) {

    }

    public void cleanUp() {

    }

    @Override
    public void startStream() {
        // no op
    }

    @Override
    public StreamsResultSet readAll() {
        return readCurrent();
    }

    @Override
    public StreamsResultSet readCurrent() {

        LOGGER.info("{} readCurrent", STREAMS_ID);

        Scanner sc = new Scanner(System.in);

        while( sc.hasNextLine() ) {

            persistQueue.offer(new StreamsDatum(sc.nextLine()));

        }

        LOGGER.info("Providing {} docs", persistQueue.size());

        StreamsResultSet result =  new StreamsResultSet(persistQueue);

        LOGGER.info("{} Exiting", STREAMS_ID);

        return result;

    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return readCurrent();
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return readCurrent();
    }
}
