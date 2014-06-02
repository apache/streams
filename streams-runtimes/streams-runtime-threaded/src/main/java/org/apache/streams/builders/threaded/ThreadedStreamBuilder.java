/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.streams.builders.threaded;

import org.apache.streams.core.*;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

/**
 * {@link ThreadedStreamBuilder} implementation to run a data processing stream in a single
 * JVM across many threads.  Depending on your data stream, the JVM heap may need to be set to a high value. Default
 * implementation uses unbound {@link java.util.concurrent.ConcurrentLinkedQueue} to connect stream components.
 */
public class ThreadedStreamBuilder implements StreamBuilder {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ThreadedStreamBuilder.class);

    public static final String TIMEOUT_KEY = "TIMEOUT";
    private final Queue<StreamsDatum> queue;
    private final Map<String, StreamComponent> providers;
    private final Map<String, StreamComponent> components;
    private final Map<String, Object> streamConfig;
    private final Map<String, BaseStreamsTask> tasks = new LinkedHashMap<String, BaseStreamsTask>();
    private final Collection<StreamBuilderEventHandler> eventHandlers = new ArrayList<StreamBuilderEventHandler>();

    private Thread shutDownHandler;

    /**
     *
     */
    public ThreadedStreamBuilder() {
        this(new ArrayBlockingQueue<StreamsDatum>(50), null);
    }

    /**
     * @param streamConfig
     */
    public ThreadedStreamBuilder(Map<String, Object> streamConfig) {
        this(new ArrayBlockingQueue<StreamsDatum>(50), streamConfig);
    }

    /**
     * @param queueType
     */
    public ThreadedStreamBuilder(Queue<StreamsDatum> queueType) {
        this(queueType, null);
    }

    /**
     * @param queueType
     * @param streamConfig
     */
    public ThreadedStreamBuilder(Queue<StreamsDatum> queueType, Map<String, Object> streamConfig) {
        this.queue = queueType;
        this.providers = new LinkedHashMap<String, StreamComponent>();
        this.components = new LinkedHashMap<String, StreamComponent>();
        this.streamConfig = streamConfig;
    }

    @Override
    public StreamBuilder newPerpetualStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, true));
        return this;
    }

    @Override
    public StreamBuilder newReadCurrentStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, false));
        return this;
    }

    @Override
    public StreamBuilder newReadNewStream(String id, StreamsProvider provider, BigInteger sequence) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, sequence));
        return this;
    }

    @Override
    public StreamBuilder newReadRangeStream(String id, StreamsProvider provider, DateTime start, DateTime end) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, start, end));
        return this;
    }

    @Override
    public StreamBuilder addStreamsProcessor(String id, StreamsProcessor processor, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(id, processor, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        return this;
    }

    @Override
    public StreamBuilder addStreamsPersistWriter(String id, StreamsPersistWriter writer, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(id, writer, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        return this;
    }


    private ExecutorService executor;

    public void addEventHandler(StreamBuilderEventHandler eventHandler) {
        this.eventHandlers.add(eventHandler);
    }

    public void removeEventHandler(StreamBuilderEventHandler eventHandler) {
        if(this.eventHandlers.contains(eventHandler))
            this.eventHandlers.remove(eventHandler);
    }

    /**
     * Runs the data stream in the this JVM and blocks till completion.
     */
    @Override
    public synchronized void start() {

        final Timer timer = new Timer(true);

        if (this.shutDownHandler != null) {
            String message = "The stream builder has already been started and has not been successfully shutdown. Nothing will execute.";
            LOGGER.warn(message);
            throw new RuntimeException(message);
        }

        // Notice, we are making a reference to 'self' we need to remove this handler
        // once we are completed ot ensure we don't hold onto this object reference
        final ThreadedStreamBuilder self = this;
        this.shutDownHandler = new Thread() {
            @Override
            public void run() {
                LOGGER.debug("Shutdown hook received.  Beginning shutdown");
                self.stop();
            }
        };

        Runtime.getRuntime().addShutdownHook(shutDownHandler);

        this.tasks.clear();
        createTasks();
        this.executor = Executors.newFixedThreadPool(tasks.size());

        try {
            LOGGER.debug("----------------------------------- Starting LocalStream Builder -----------------------------------");

            // Starting all the tasks
            for(StreamsTask task : this.tasks.values())
                this.executor.execute(task);


            // if anyone would like to listen in to progress events
            // let them do that
            TimerTask updateTask = new TimerTask() {
                public void run() {
                    if(eventHandlers.size() > 0) {
                        final Map<String, StatusCounts> updateMap = new HashMap<String, StatusCounts>();
                        for(final String k : tasks.keySet())
                            updateMap.put(k, tasks.get(k).getCurrentStatus());
                        for (final StreamBuilderEventHandler eventHandler : eventHandlers) {
                            new Thread(new Runnable() {
                                public void run() {
                                    eventHandler.update(updateMap);
                                }
                            }).start();
                        }
                    }
                }
            };

            timer.schedule(updateTask, 0, 1000);

            // keep going until we are done..
            // because we are using queues in between each of these items we can
            // reach edge cases where a datum is trapped between getting recognized
            // so we require 5 instances (ms) of the system telling us to quit before we
            // finally exit to prevent any lost items.
            int foundReasonToQuit = 0;

            while (foundReasonToQuit < 5) {
                boolean isRunning = false;

                // check to see if it is running, if it is, then set the flag and break.
                for (BaseStreamsTask task : this.tasks.values()) {
                    if(task.isRunning()) {
                        isRunning = true;
                        break;
                    }
                }

                if (isRunning) {
                    foundReasonToQuit = 0;
                    safeQuickRest(1);
                }
                else {
                    foundReasonToQuit++;
                    safeQuickRest(1);
                }
            }

            LOGGER.debug("Components are no longer running, we can turn it off");
            shutdown();

            for(final String k : tasks.keySet()) {
                StatusCounts counts = tasks.get(k).getCurrentStatus();
                LOGGER.debug("Finishing: {} - Queue[{}] Working[{}] Success[{}] Failed[{}] ", k,
                        counts.getQueue(), counts.getWorking(), counts.getSuccess(), counts.getFailed());
            }



        } catch (Throwable e) {
            // No Operation
            try {
                shutdown();
            }
            catch (Throwable omgE) {
                LOGGER.error("Unexpected Error: {}", omgE);
            }
        } finally {
            if (!Runtime.getRuntime().removeShutdownHook(this.shutDownHandler))
                LOGGER.warn("We should have removed the shutdown handler...");

            this.shutDownHandler = null;

            // Kill the timer
            timer.cancel();
        }
    }

    private void safeQuickRest(final int millis) {
        try {
            Thread.yield();
            Thread.sleep(millis);
        } catch(Throwable e) {
            // No Operation
        }
    }

    private void shutdownExecutor() {
        // make sure that
        try {
            if (!this.executor.isShutdown()) {
                // tell the executor to shutdown.
                this.executor.shutdown();

                if (!this.executor.awaitTermination(5, TimeUnit.MINUTES))
                    this.executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            this.executor.shutdownNow();
            throw new RuntimeException(ie);
        }
    }

    protected void shutdown() throws InterruptedException {
        LOGGER.debug("Shutting down...");
        //give the stream 30secs to try to shutdown gracefully, then force shutdown otherwise
        for(BaseStreamsTask task : this.tasks.values())
            task.stopTask();

        shutdownExecutor();
    }

    protected void createTasks() {
        for (StreamComponent prov : this.providers.values()) {
            BaseStreamsTask task = prov.createConnectedTask(getTimeout());
            task.setStreamConfig(this.streamConfig);
            this.tasks.put(prov.getId(), task);
        }

        for (StreamComponent comp : this.components.values()) {
            BaseStreamsTask task = comp.createConnectedTask(getTimeout());
            task.setStreamConfig(this.streamConfig);
            this.tasks.put(comp.getId(), task);
        }
    }

    public void stop() {
        try {
            shutdown();
        } catch (Exception e) {
            LOGGER.warn("Forcing Shutdown: There was an error stopping: {}", e.getMessage());
        }
    }

    private void connectToOtherComponents(String[] connectToIds, StreamComponent toBeConnected) {
        for (String id : connectToIds) {
            StreamComponent upStream;
            if (this.providers.containsKey(id)) {
                upStream = this.providers.get(id);
            } else if (this.components.containsKey(id)) {
                upStream = this.components.get(id);
            } else {
                throw new InvalidStreamException("Cannot connect to id, " + id + ", because id does not exist.");
            }
            upStream.addOutBoundQueue(toBeConnected, toBeConnected.getInBoundQueue());
            toBeConnected.addInboundQueue(upStream);
        }
    }

    private void validateId(String id) {
        if (this.providers.containsKey(id) || this.components.containsKey(id)) {
            throw new InvalidStreamException("Duplicate id. " + id + " is already assigned to another component");
        }
    }

    @SuppressWarnings("unchecked")
    private Queue<StreamsDatum> cloneQueue() {
        Object toReturn = SerializationUtil.cloneBySerialization(this.queue);
        if(toReturn instanceof Queue)
            return (Queue<StreamsDatum>)toReturn;
        else
            throw new RuntimeException("Unable to clone the queue");
    }

    protected int getTimeout() {
        return streamConfig != null && streamConfig.containsKey(TIMEOUT_KEY) ? (Integer) streamConfig.get(TIMEOUT_KEY) : 3000;
    }

}
