package org.apache.streams.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.util.GuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KafkaPersistWriter implements StreamsPersistWriter, Serializable, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPersistWriter.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    private KafkaConfiguration config;

    private Producer<String, String> producer;

    public KafkaPersistWriter() {
        Config config = StreamsConfigurator.config.getConfig("kafka");
        this.config = KafkaConfigurator.detectConfiguration(config);
        this.persistQueue  = new ConcurrentLinkedQueue<StreamsDatum>();
    }

    public KafkaPersistWriter(Queue<StreamsDatum> persistQueue) {
        Config config = StreamsConfigurator.config.getConfig("kafka");
        this.config = KafkaConfigurator.detectConfiguration(config);
        this.persistQueue = persistQueue;
    }

    public KafkaPersistWriter(KafkaConfiguration config) {
        this.config = config;
        this.persistQueue = new ConcurrentLinkedQueue<StreamsDatum>();
    }

    public KafkaPersistWriter(KafkaConfiguration config, Queue<StreamsDatum> persistQueue) {
        this.config = config;
        this.persistQueue = persistQueue;
    }

    @Override
    public void start() {
        Properties props = new Properties();

        props.put("metadata.broker.list", config.getBrokerlist());
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "org.apache.streams.kafka.StreamsPartitioner");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);

        producer = new Producer<String, String>(config);

        new Thread(new KafkaPersistWriterTask(this)).start();
    }

    @Override
    public void stop() {
        producer.close();
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

            String hash = GuidUtils.generateGuid(text);

            KeyedMessage<String, String> data = new KeyedMessage<String, String>(config.getTopic(), hash, text);

            producer.send(data);

        } catch (JsonProcessingException e) {
            LOGGER.warn("save: {}", e);
        }// put
    }

    @Override
    public void run() {
        start();

        // stop();
    }
}
