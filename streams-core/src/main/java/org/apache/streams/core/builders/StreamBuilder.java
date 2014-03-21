package org.apache.streams.core.builders;

import org.apache.streams.core.*;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Queue;

/**
 * Interface for building data streams.
 *
 * <pre>
 *     StreamBuilder builder = ...
 *     builder.newReadCurrentStream(. . .)
 *            .addStreamsProcessor(. . .)
 *            ...
 *            .addStreamsPersistWriter(. . .)
 *     builder.run();
 * </pre>
 *
 */
public interface StreamBuilder {


    /**
     * Add a {@link org.apache.streams.core.StreamsProcessor} to the data processing stream.
     * @param processorId unique id for this processor - must be unique across the entire stream
     * @param processor the processor to execute
     * @param numTasks the number of instances of this processor to run concurrently
     * @param connectToIds the ids of the {@link org.apache.streams.core.StreamsOperation} that this process will
     *                     receive data from.
     * @return this
     */
    public StreamBuilder addStreamsProcessor(String processorId, StreamsProcessor processor, int numTasks, String... connectToIds);

    /**
     * Add a {@link org.apache.streams.core.StreamsPersistWriter} to the data processing stream.
     * @param persistWriterId unique id for this processor - must be unique across the entire stream
     * @param writer the writer to execute
     * @param numTasks the number of instances of this writer to run concurrently
     * @param connectToIds the ids of the {@link org.apache.streams.core.StreamsOperation} that this process will
     *                     receive data from.
     * @return this
     */
    public StreamBuilder addStreamsPersistWriter(String persistWriterId, StreamsPersistWriter writer, int numTasks, String... connectToIds);

    /**
     * Add a {@link org.apache.streams.core.StreamsProvider} to the data processing stream.  The provider will execute
     * {@link org.apache.streams.core.StreamsProvider:readCurrent()} to produce data.
     * @param streamId unique if for this provider - must be unique across the entire stream.
     * @param provider provider to execute
     * @return this
     */
    public StreamBuilder newPerpetualStream(String streamId, StreamsProvider provider);

    /**
     * Add a {@link org.apache.streams.core.StreamsProvider} to the data processing stream.  The provider will execute
     * {@link org.apache.streams.core.StreamsProvider:readCurrent()} to produce data.
     * @param streamId unique if for this provider - must be unique across the entire stream.
     * @param provider provider to execute
     * @return this
     */
    public StreamBuilder newReadCurrentStream(String streamId, StreamsProvider provider);

    /**
     * Add a {@link org.apache.streams.core.StreamsProvider} to the data processing stream.  The provider will execute
     * {@link org.apache.streams.core.StreamsProvider:readNext(BigInteger)} to produce data.
     * @param streamId unique if for this provider - must be unique across the entire stream.
     * @param provider provider to execute
     * @param sequence sequence to pass to {@link org.apache.streams.core.StreamsProvider:readNext(BigInteger)} method
     * @return this
     */
    public StreamBuilder newReadNewStream(String streamId, StreamsProvider provider, BigInteger sequence);

    /**
     * Add a {@link org.apache.streams.core.StreamsProvider} to the data processing stream.  The provider will execute
     * {@link org.apache.streams.core.StreamsProvider:readRange(DateTime, DateTime)} to produce data. Whether the start
     * and end dates are inclusive or exclusive is up to the implementation.
     * @param streamId unique if for this provider - must be unique across the entire stream.
     * @param provider provider to execute
     * @param start start date
     * @param end end date
     * @return this
     */
    public StreamBuilder newReadRangeStream(String streamId, StreamsProvider provider, DateTime start, DateTime end);

    /**
     * Builds the stream, and starts it or submits it based on implementation.
     */
    public void start();

    /**
     * Stops the streams processing.  No guarantee on a smooth shutdown. Optional method, may not be implemented in
     * all cases.
     */
    public void stop();









}
