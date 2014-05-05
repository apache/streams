package org.apache.streams.s3;

import com.google.common.base.Strings;
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;

public class S3PersistReaderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3PersistReaderTask.class);

    private S3PersistReader reader;

    public S3PersistReaderTask(S3PersistReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {

        for(String file : reader.getFiles()) {

            // Create our buffered reader
            S3ObjectInputStreamWrapper is = new S3ObjectInputStreamWrapper(reader.getAmazonS3Client().getObject(reader.getBucketName(), file));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            LOGGER.info("Reading: {} ", file);

            String line = "";
            try {
                while((line = bufferedReader.readLine()) != null) {
                    if( !Strings.isNullOrEmpty(line) ) {
                        reader.countersCurrent.incrementAttempt();
                        String[] fields = line.split(Character.toString(reader.DELIMITER));
                        StreamsDatum entry = new StreamsDatum(fields[3], fields[0]);
                        write( entry );
                        reader.countersCurrent.incrementStatus(DatumStatus.SUCCESS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(e.getMessage());
                reader.countersCurrent.incrementStatus(DatumStatus.FAIL);
            }

            LOGGER.info("Completed:  " + file);

            try {
                closeSafely(file, is);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private static void closeSafely(String file, Closeable closeable) {
        try {
            closeable.close();
        } catch(Exception e) {
            LOGGER.error("There was an issue closing file: {}", file);
        }
    }


    private void write( StreamsDatum entry ) {
        boolean success;
        do {
            synchronized( S3PersistReader.class ) {
                success = reader.persistQueue.offer(entry);
            }
            Thread.yield();
        }
        while( !success );
    }

}
