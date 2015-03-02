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
package org.apache.streams.threaded.builders;

import org.apache.streams.core.*;
import org.apache.streams.threaded.controller.ThreadingController;
import org.apache.streams.threaded.tasks.StatusCounts;
import org.apache.streams.threaded.tasks.StreamsPersistWriterTask;
import org.apache.streams.threaded.tasks.StreamsProcessorTask;
import org.apache.streams.threaded.tasks.StreamsProviderTask;
import org.apache.streams.threaded.tasks.StreamsTask;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;

/**
 * {@link ThreadedStreamBuilder} implementation to run a data processing stream in a single
 * JVM across many threads.  Depending on your data stream, the JVM heap may need to be set to a high value. Default
 * implementation uses unbound {@link java.util.concurrent.ConcurrentLinkedQueue} to connect stream components.
 */
public class ThreadedStreamBuilder implements StreamBuilder {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ThreadedStreamBuilder.class);
    public static final String DEFAULT_STREAM_IDENTIFIER = "Unknown_Stream";
    public static final String STREAM_IDENTIFIER_KEY = "streamsID";

    private final List<StreamsGraphElement> graphElements = new ArrayList<>();

    public static final String TIMEOUT_KEY = "TIMEOUT";
    private static final List<ThreadedStreamBuilder> CURRENTLY_EXECUTING = Collections.synchronizedList(new ArrayList<ThreadedStreamBuilder>());

    private ExecutorService providerExecutor;
    private final ThreadingController threadingController;
    private final Queue<StreamsDatum> queue;
    private final Map<String, StreamComponent> providers;
    private final Map<String, StreamComponent> components;
    private final Map<String, Object> streamConfig;
    private final Map<String, StreamsTask> tasks = new LinkedHashMap<>();
    private final Collection<StreamBuilderEventHandler> eventHandlers = new ArrayList<>();

    public ThreadedStreamBuilder() {
        this(new ArrayBlockingQueue<StreamsDatum>(2000), null, ThreadingController.getInstance());
    }

    public ThreadedStreamBuilder(ThreadingController threadingController) {
        this(new ArrayBlockingQueue<StreamsDatum>(2000), null, threadingController);
    }

    public ThreadedStreamBuilder(Queue<StreamsDatum> queue) {
        this(queue, null, ThreadingController.getInstance());
    }

    public ThreadedStreamBuilder(Queue<StreamsDatum> queue, ThreadingController threadingController) {
        this(queue, null, threadingController);
    }

    public ThreadedStreamBuilder(Map<String, Object> streamConfig) {
        this(new ArrayBlockingQueue<StreamsDatum>(50), streamConfig, ThreadingController.getInstance());
    }

    public ThreadedStreamBuilder(Queue<StreamsDatum> queue, Map<String, Object> streamConfig, ThreadingController threadingController) {
        this.queue = queue;
        this.providers = new LinkedHashMap<String, StreamComponent>();
        this.components = new LinkedHashMap<String, StreamComponent>();
        this.streamConfig = streamConfig;
        this.threadingController = threadingController;
    }

    public List<StreamsGraphElement> getGraphElements() {
        if(this.graphElements.size() == 0) {
            buildGraphElements();
        }

        return this.graphElements;
    }

    public static List<ThreadedStreamBuilder> getCurrentlyExecuting() {
        synchronized (ThreadedStreamBuilder.class) {
            return Collections.unmodifiableList(CURRENTLY_EXECUTING);
        }
    }

    private void buildGraphElements() {
        this.graphElements.clear();
        for(StreamsTask p : this.tasks.values()) {
            if(p instanceof StreamsProviderTask) {
                appendAndFollow(p, this.graphElements);
            }
        }
    }

    private void appendAndFollow(StreamsTask t, List<StreamsGraphElement> elems) {
        for(StreamsTask c : t.getChildren()) {
            String type;

            if(t.getClass().equals(StreamsProviderTask.class)) {
                type = "provider";
            } else if(t.getClass().equals(StreamsProcessorTask.class)) {
                type = "processor";
            } else if(t.getClass().equals(StreamsPersistWriterTask.class)) {
                type = "writer";
            } else {
                type = "unknown";
            }

            elems.add(new StreamsGraphElement(t.getId(), c.getId(), type, 0));
            appendAndFollow(c, elems);
        }
    }

    @Override
    public ThreadedStreamBuilder newPerpetualStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(this.threadingController, id, provider, true));
        return this;
    }

    @Override
    public ThreadedStreamBuilder newReadCurrentStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(this.threadingController, id, provider, false));
        return this;
    }

    @Override
    public ThreadedStreamBuilder newReadNewStream(String id, StreamsProvider provider, BigInteger sequence) {
        validateId(id);
        this.providers.put(id, new StreamComponent(this.threadingController, id, provider, sequence));
        return this;
    }

    @Override
    public ThreadedStreamBuilder newReadRangeStream(String id, StreamsProvider provider, DateTime start, DateTime end) {
        validateId(id);
        this.providers.put(id, new StreamComponent(this.threadingController, id, provider, start, end));
        return this;
    }

    public ThreadedStreamBuilder addStreamsProcessor(String id, StreamsProcessor processor, String... inBoundIds) {
        addStreamsProcessor(id, processor, 1, inBoundIds);
        return this;
    }

    @Override
    public ThreadedStreamBuilder addStreamsProcessor(String id, StreamsProcessor processor, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(this.threadingController, id, processor, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        return this;
    }

    public ThreadedStreamBuilder addStreamsPersistWriter(String id, StreamsPersistWriter writer, String... inBoundIds) {
        return addStreamsPersistWriter(id, writer, 1, inBoundIds);
    }

    @Override
    public ThreadedStreamBuilder addStreamsPersistWriter(String id, StreamsPersistWriter writer, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(this.threadingController, id, writer, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        return this;
    }

    public ThreadedStreamBuilder addEventHandler(StreamBuilderEventHandler eventHandler) {
        this.eventHandlers.add(eventHandler);
        return this;
    }

    public ThreadedStreamBuilder removeEventHandler(StreamBuilderEventHandler eventHandler) {
        if(this.eventHandlers.contains(eventHandler)) {
            this.eventHandlers.remove(eventHandler);
        }
        return this;
    }

    public final Map<String, StatusCounts> getUpdateCounts() {
        final Map<String, StatusCounts> updateMap = new HashMap<String, StatusCounts>();

        for (final String k : tasks.keySet()) {
            updateMap.put(k, tasks.get(k).getCurrentStatus());
        }
        return updateMap;
    }

    /**
     * Runs the data stream in the this JVM and blocks till completion.
     */
    @Override
    public void start() {

        this.tasks.clear();

        // Let the threading controller know that we will need this until we call release.
        this.getThreadingController().lock();

        // create a new timer to update the tasks
        Timer timer = new Timer(true);

        this.providerExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("Apache Streams - Provider[" + DateTime.now().toString() + "]");
                t.setPriority(Math.max(1,(int)(Thread.currentThread().getPriority() * .5)));
                return t;
            }
        });

        createTasks();

        // if anyone would like to listen in to progress events
        // let them do that
        final TimerTask updateTask = getUpdateCounts() == null || getUpdateCounts().keySet().size() == 0 ? null : new TimerTask() {
            public void run() {

                final Map<String, StatusCounts> updateMap = getUpdateCounts();

                for(String k : updateMap.keySet()) {
                    for (StreamsGraphElement g : getGraphElements()) {
                        if (g.getTarget().equals(k)) {
                            g.setValue((int) updateMap.get(k).getWorking());
                        }
                    }
                }

                updateEventHandlers(updateMap);
            }
        };

        try {
            synchronized (ThreadedStreamBuilder.class) {
                CURRENTLY_EXECUTING.add(this);
            }

            if(updateTask != null) {
                timer.schedule(updateTask, 0, 1500);
            }

            for(StreamsTask t : this.tasks.values()) {
                t.prepare(this.streamConfig);
            }

            // Starting all the tasks
            for(StreamsTask task : this.tasks.values()) {
                if (task instanceof Runnable) {
                    providerExecutor.execute((Runnable) task);
                }
            }

            Condition condition = null;
            while((condition = getOffendingLock()) != null) {
                condition.await();
            }

            for(StreamsTask t : this.tasks.values()) {
                t.cleanup();
            }

            for(final String k : tasks.keySet()) {
                final StatusCounts counts = tasks.get(k).getCurrentStatus();
                LOGGER.debug("Finishing: {} - Working[{}] Success[{}] Failed[{}] TimeSpent[{}]", k,
                        counts.getWorking(), counts.getSuccess(), counts.getFailed(), counts.getAverageTimeSpent());
            }
            updateEventHandlers(getUpdateCounts());

            // Call the shutdown procedure.
            shutdown();

        } catch (Throwable e) {
            // No Operation
            try {
                shutdown();
            }
            catch (Throwable omgE) {
                LOGGER.error("Unexpected Error: {}", omgE);
            }
        } finally {

            // cancel the timer
            timer.cancel();

            synchronized (CURRENTLY_EXECUTING) {
                CURRENTLY_EXECUTING.remove(this);
            }

            // Call the release on the threaded builder
            this.getThreadingController().release();

            // kill the updateTask
            if(updateTask != null) {
                updateTask.cancel();
            }

        }
    }

    private void updateEventHandlers(Map<String, StatusCounts> updateMap) {
        if (eventHandlers.size() > 0) {
            for (final StreamBuilderEventHandler eventHandler : eventHandlers) {
                try {
                    try {
                        eventHandler.update(updateMap, getGraphElements());
                    } catch(Throwable e) {
                        LOGGER.error("Exception while trying to update event handler: {}", e);
                    }
                }
                catch(Throwable e) {
                    LOGGER.error("Exception while trying to update event handler: {}", e);
                }
            }
        }
    }

    private Condition getOffendingLock() {
        for(StreamsTask t : this.tasks.values()) {
            if(t instanceof StreamsProviderTask) {
                if (((StreamsProviderTask) t).isRunning()) {
                    return ((StreamsProviderTask) t).getLock();
                }
            }
        }
        return null;
    }

    private void shutdownExecutor(ExecutorService executorService) throws InterruptedException {
        executorService.shutdown();
        if(!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
            executorService.shutdownNow();
            if(!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                LOGGER.debug("Unable to shutdown Provider Executor service");
            }
        }
    }

    protected void shutdown() throws InterruptedException {
        LOGGER.debug("Shutting down...");
        shutdownExecutor(this.providerExecutor);
        LOGGER.debug("Shut down");
    }

    protected void createTasks() {
        for (StreamComponent prov : this.providers.values()) {
            this.tasks.put(prov.getId(), prov.createConnectedTask(this.streamConfig));
        }

        for (StreamComponent comp : this.components.values()) {
            this.tasks.put(comp.getId(), comp.createConnectedTask(this.streamConfig));
        }

        for(StreamsTask t : this.tasks.values()) {
            t.initialize(this.tasks);
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
        return (Queue<StreamsDatum>) toReturn;
    }

    protected int getTimeout() {
        return streamConfig != null && streamConfig.containsKey(TIMEOUT_KEY) ? (Integer) streamConfig.get(TIMEOUT_KEY) : 3000;
    }

    public ThreadingController getThreadingController() {
        return threadingController;
    }
}