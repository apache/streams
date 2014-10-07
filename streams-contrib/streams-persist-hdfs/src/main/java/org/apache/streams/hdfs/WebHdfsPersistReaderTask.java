/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
                            // Temporarily disabling timestamp reads to make reader and writer compatible
                            // This capability will be restore in PR for STREAMS-169
                            //StreamsDatum entry = new StreamsDatum(fields[3], fields[0], new DateTime(Long.parseLong(fields[2])));
                            StreamsDatum entry = new StreamsDatum(fields[3], fields[0]);
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
