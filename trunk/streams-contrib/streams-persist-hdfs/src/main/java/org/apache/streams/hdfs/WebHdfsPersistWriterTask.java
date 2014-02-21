package org.apache.streams.hdfs;

import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class WebHdfsPersistWriterTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsPersistWriterTask.class);

    private WebHdfsPersistWriter writer;

    public WebHdfsPersistWriterTask(WebHdfsPersistWriter writer) {
        this.writer = writer;
    }

    @Override
    public void run() {

        while(true) {
            if( writer.getPersistQueue().peek() != null ) {
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
