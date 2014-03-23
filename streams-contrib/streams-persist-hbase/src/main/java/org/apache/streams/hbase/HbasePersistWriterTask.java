package org.apache.streams.hbase;

import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class HbasePersistWriterTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HbasePersistWriterTask.class);

    private HbasePersistWriter writer;

    public HbasePersistWriterTask(HbasePersistWriter writer) {
        this.writer = writer;
    }

    @Override
    public void run() {

        while(true) {
            if( writer.persistQueue.peek() != null ) {
                try {
                    StreamsDatum entry = writer.persistQueue.remove();
                    writer.write(entry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(new Random().nextInt(1));
            } catch (InterruptedException e) {}
        }

    }

}
