package org.apache.streams.datasift.provider;

import com.datasift.client.DataSiftClient;
import com.datasift.client.DataSiftConfig;
import com.datasift.client.core.Stream;
import com.datasift.client.stream.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.*;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sblackmon on 12/10/13.
 */
public class DatasiftStreamProvider implements StreamsProvider, DatumStatusCountable {

    public final static String STREAMS_ID = "DatasiftStreamProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftStreamProvider.class);

    protected DatasiftConfiguration config = null;

    protected DataSiftClient client;

    public DatasiftConfiguration getConfig() {
        return config;
    }

    public void setConfig(DatasiftConfiguration config) {
        this.config = config;
    }

    protected BlockingQueue inQueue = new LinkedBlockingQueue<Interaction>(1000);

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    public BlockingQueue<Object> getInQueue() {
        return inQueue;
    }

    protected ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(100, 100));

    private DatumStatusCounter countersCurrent = new DatumStatusCounter();
    private DatumStatusCounter countersTotal = new DatumStatusCounter();

    private ObjectMapper mapper;

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public DatasiftStreamProvider() {
        Config datasiftConfig = StreamsConfigurator.config.getConfig("datasift");
        this.config = DatasiftStreamConfigurator.detectConfiguration(datasiftConfig);
    }

    public DatasiftStreamProvider(DatasiftConfiguration config) {
        this.config = config;
    }

    @Override
    public void startStream() {

        Preconditions.checkNotNull(config);

        Preconditions.checkNotNull(config.getStreamHash());

        Preconditions.checkNotNull(config.getStreamHash().get(0));

        for( String hash : config.getStreamHash()) {

            client.liveStream().subscribe(new Subscription(Stream.fromString(hash)));

        }

        for( int i = 0; i < ((config.getStreamHash().size() / 5) + 1); i++ )
            executor.submit(new DatasiftEventProcessor(inQueue, providerQueue, Activity.class));

    }

    public void stop() {

        for( String hash : config.getStreamHash()) {

            client.liveStream().subscribe(new Subscription(Stream.fromString(hash)));

        }
    }

    public Queue<StreamsDatum> getProviderQueue() {
        return this.providerQueue;
    }

    @Override
    public StreamsResultSet readCurrent() {

        StreamsResultSet current;

        synchronized( DatasiftStreamProvider.class ) {
            current = new StreamsResultSet(Queues.newConcurrentLinkedQueue(providerQueue));
            current.setCounter(new DatumStatusCounter());
            current.getCounter().add(countersCurrent);
            countersTotal.add(countersCurrent);
            countersCurrent = new DatumStatusCounter();
            providerQueue.clear();
        }

        return current;

    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

    @Override
    public void prepare(Object configurationObject) {

        Preconditions.checkNotNull(config);

        String apiKey = config.getApiKey();
        String userName = config.getUserName();

        DataSiftConfig config = new DataSiftConfig(userName, apiKey);

        mapper = StreamsJacksonMapper.getInstance();

        client = new DataSiftClient(config);

        client.liveStream().onError(new ErrorHandler());

        //handle delete message
        client.liveStream().onStreamEvent(new DeleteHandler());

    }

    @Override
    public void cleanUp() {
        stop();
    }

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        return countersTotal;
    }

    public class Subscription extends StreamSubscription {
        AtomicLong count = new AtomicLong();

        public Subscription(Stream stream) {
            super(stream);
        }

        public void onDataSiftLogMessage(DataSiftMessage di) {
            //di.isWarning() is also available
            System.out.println((di.isError() ? "Error" : di.isInfo() ? "Info" : "Warning") + ":\n" + di);
        }

        public void onMessage(Interaction i) {

            LOGGER.debug("Processing:\n" + i);

            String json;
            try {
                json = mapper.writeValueAsString(i);
                inQueue.offer(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
    }

    public class DeleteHandler extends StreamEventListener {
        public void onDelete(DeletedInteraction di) {
            //go off and delete the interaction if you have it stored. This is a strict requirement!
            LOGGER.info("DELETED:\n " + di);
        }
    }

    public class ErrorHandler extends ErrorListener {
        public void exceptionCaught(Throwable t) {
            LOGGER.warn(t.getMessage());
            //do something useful...
        }
    }

}
