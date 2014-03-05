package org.apache.streams.hdfs;

import org.apache.hadoop.fs.FileStatus;
import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class WebHdfsPersistReaderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsPersistReaderTask.class);

    private WebHdfsPersistReader reader;

    public WebHdfsPersistReaderTask(WebHdfsPersistReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {

        for( FileStatus fileStatus : reader.status ) {
            BufferedReader bufferedReader;

            if( fileStatus.isFile() && !fileStatus.getPath().getName().endsWith("_SUCCESS")) {
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(reader.client.open(fileStatus.getPath())));

                    String line = "";
                    do{
                        try {
                            line = bufferedReader.readLine();
                            if( line != null ) {
                                String[] fields = line.split(Character.toString(reader.DELIMITER));
                                reader.persistQueue.offer(new StreamsDatum(fields[3]));
                            }
                        } catch (Exception e) {
                            LOGGER.warn("Failed processing " + line);
                        }
                    } while( line != null );
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

    }

}
