package org.apache.streams.hdfs;

import com.google.common.base.Strings;
import org.apache.hadoop.fs.FileStatus;
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.StreamsDatum;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
            LOGGER.info("Found " + fileStatus.getPath().getName());
            if( fileStatus.isFile() && !fileStatus.getPath().getName().startsWith("_")) {
                LOGGER.info("Started Processing " + fileStatus.getPath().getName());
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(reader.client.open(fileStatus.getPath())));
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.error(e.getMessage());
                    return;
                }

                String line = "";
                do{
                    try {
                        line = bufferedReader.readLine();
                        if( !Strings.isNullOrEmpty(line) ) {
                            reader.countersCurrent.incrementAttempt();
                            String[] fields = line.split(Character.toString(reader.DELIMITER));
                            StreamsDatum entry = new StreamsDatum(fields[3], fields[0], new DateTime(Long.parseLong(fields[2])));
                            write( entry );
                            reader.countersCurrent.incrementStatus(DatumStatus.SUCCESS);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.warn(e.getMessage());
                        reader.countersCurrent.incrementStatus(DatumStatus.FAIL);
                    }
                } while( !Strings.isNullOrEmpty(line) );
                LOGGER.info("Finished Processing " + fileStatus.getPath().getName());
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.error(e.getMessage());
                }
            }
        }

    }

    private void write( StreamsDatum entry ) {
        boolean success;
        do {
            synchronized( WebHdfsPersistReader.class ) {
                success = reader.persistQueue.offer(entry);
            }
            Thread.yield();
        }
        while( !success );
    }

}
