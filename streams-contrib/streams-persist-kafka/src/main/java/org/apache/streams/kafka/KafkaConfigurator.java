package org.apache.streams.kafka;

import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class KafkaConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaConfigurator.class);

    public static KafkaConfiguration detectConfiguration(Config kafka) {
        String brokerlist = StreamsConfigurator.config.getString("kafka.metadata.broker.list");
        String topic = StreamsConfigurator.config.getString("kafka.topic");

        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration();

        kafkaConfiguration.setBrokerlist(brokerlist);
        kafkaConfiguration.setBrokerlist(brokerlist);

        return kafkaConfiguration;
    }

}
