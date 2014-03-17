package org.apache.streams.hdfs;

import com.google.common.base.Strings;
import org.apache.hadoop.fs.FileStatus;
import org.apache.streams.core.StreamsDatum;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
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
                            if( !Strings.isNullOrEmpty(line) ) {
                                String[] fields = line.split(Character.toString(reader.DELIMITER));
                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(new Long(fields[2]));
                                StreamsDatum entry = new StreamsDatum(fields[3], fields[0], new DateTime(cal.getTime()));
                                reader.persistQueue.offer(entry);
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
