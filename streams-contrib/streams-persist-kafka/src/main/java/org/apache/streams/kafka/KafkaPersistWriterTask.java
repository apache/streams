package org.apache.streams.kafka;

import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class KafkaPersistWriterTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPersistWriterTask.class);

    private KafkaPersistWriter writer;

    public KafkaPersistWriterTask(KafkaPersistWriter writer) {
        this.writer = writer;
    }

    @Override
    public void run() {

        while(true) {
            try {
                StreamsDatum entry = writer.persistQueue.remove();
                writer.write(entry);
                Thread.sleep(new Random().nextInt(100));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
