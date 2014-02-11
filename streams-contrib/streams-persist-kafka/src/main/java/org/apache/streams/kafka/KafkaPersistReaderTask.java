package org.apache.streams.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class KafkaPersistReaderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPersistReaderTask.class);

    private KafkaPersistReader reader;
    private KafkaStream<String,String> stream;

    public KafkaPersistReaderTask(KafkaPersistReader reader, KafkaStream<String,String> stream) {
        this.reader = reader;
        this.stream = stream;
    }



    @Override
    public void run() {

        MessageAndMetadata<String,String> item;
        while(true) {

            ConsumerIterator<String, String> it = stream.iterator();
            while (it.hasNext()) {
                item = it.next();
                reader.persistQueue.add(new StreamsDatum(item.message()));
            }
            try {
                Thread.sleep(new Random().nextInt(100));
            } catch (InterruptedException e) {}
        }

    }

}
