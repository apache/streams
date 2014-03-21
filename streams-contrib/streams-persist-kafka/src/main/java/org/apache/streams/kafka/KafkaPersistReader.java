package org.apache.streams.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.consumer.Whitelist;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.streams.kafka.KafkaConfiguration;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KafkaPersistReader implements StreamsPersistReader, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPersistReader.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    private KafkaConfiguration config;

    private ConsumerConfig consumerConfig;
    private ConsumerConnector consumerConnector;

    public List<KafkaStream<String, String>> inStreams;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public KafkaPersistReader() {
        Config config = StreamsConfigurator.config.getConfig("kafka");
        this.config = KafkaConfigurator.detectConfiguration(config);
        this.persistQueue  = new ConcurrentLinkedQueue<StreamsDatum>();
    }

    public KafkaPersistReader(Queue<StreamsDatum> persistQueue) {
        Config config = StreamsConfigurator.config.getConfig("kafka");
        this.config = KafkaConfigurator.detectConfiguration(config);
        this.persistQueue = persistQueue;
    }

    public void setConfig(KafkaConfiguration config) {
        this.config = config;
    }

    @Override
    public void startStream() {

        Properties props = new Properties();
        props.setProperty("serializer.encoding", "UTF8");

        consumerConfig = new ConsumerConfig(props);

        consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);

        Whitelist topics = new Whitelist(config.getTopic());
        VerifiableProperties vprops = new VerifiableProperties(props);

        inStreams = consumerConnector.createMessageStreamsByFilter(topics, 1, new StringDecoder(vprops), new StringDecoder(vprops));

        for (final KafkaStream stream : inStreams) {
            executor.submit(new KafkaPersistReaderTask(this, stream));
        }

    }

    @Override
    public StreamsResultSet readAll() {
        return readCurrent();
    }

    @Override
    public StreamsResultSet readCurrent() {
        return null;
    }

    @Override
    public StreamsResultSet readNew(BigInteger bigInteger) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime dateTime, DateTime dateTime2) {
        return null;
    }

    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {
        consumerConnector.shutdown();
        while( !executor.isTerminated()) {
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {}
        }
    }
}
