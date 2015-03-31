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
package org.apache.streams.s3;

import com.google.common.base.Strings;
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;

public class S3PersistReaderTask implements Runnable {

    private boolean useTimestampAsPublished = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(S3PersistReaderTask.class);

    private S3PersistReader reader;

    public S3PersistReaderTask(S3PersistReader reader) {
        this.reader = reader;

        setTimestampAsPublished(this.reader.getS3ReaderConfiguration());
    }

    private void setTimestampAsPublished(S3ReaderConfiguration config) {
        if(config.getUseTimestampAsPublished() != null && config.getUseTimestampAsPublished()) {
            this.useTimestampAsPublished = config.getUseTimestampAsPublished();
        }
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

                        DateTime publishedDate = getPublishedDate(fields);

                        StreamsDatum entry = null;

                        if(publishedDate != null) {
                            entry = new StreamsDatum(fields[3], fields[0], publishedDate);
                        } else {
                            new StreamsDatum(fields[3], fields[0]);
                        }

                        ComponentUtils.offerUntilSuccess(entry, reader.persistQueue);
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

    DateTime getPublishedDate(String[] fields) {
        DateTime dateTime = null;

        if(useTimestampAsPublished && fields.length >= 2 && fields[1] != null && !fields[1].equals("")) {
            try {
                dateTime = new DateTime(fields[1]);
            } catch (Exception e) {
                LOGGER.error("Exception while trying to parse datetime from S3 File: {}", e);
            }
        }

        return dateTime;
    }

    private static void closeSafely(String file, Closeable closeable) {
        try {
            closeable.close();
        } catch(Exception e) {
            LOGGER.error("There was an issue closing file: {}", file);
        }
    }
}