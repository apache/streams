package org.apache.streams.s3;

import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class S3PersistWriterTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3PersistWriterTask.class);

    private S3PersistWriter writer;

    public S3PersistWriterTask(S3PersistWriter writer) {
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
