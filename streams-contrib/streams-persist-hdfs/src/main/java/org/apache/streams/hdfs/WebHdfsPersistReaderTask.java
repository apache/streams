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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.hadoop.fs.FileStatus;
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class WebHdfsPersistReaderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsPersistReaderTask.class);

    private WebHdfsPersistReader reader;

    public WebHdfsPersistReaderTask(WebHdfsPersistReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {

        LOGGER.info("WebHdfsPersistReaderTask: files to process");

        for( FileStatus fileStatus : reader.status ) {
            LOGGER.info("    " + fileStatus.getPath().getName());
        }

        for( FileStatus fileStatus : reader.status ) {
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            BufferedReader bufferedReader;
            if( fileStatus.isFile() && !fileStatus.getPath().getName().startsWith("_")) {
                HdfsWriterConfiguration.Compression compression = HdfsWriterConfiguration.Compression.NONE;
                if( fileStatus.getPath().getName().endsWith(".gz"))
                    compression = HdfsWriterConfiguration.Compression.GZIP;
                LOGGER.info("Started Processing: {} Encoding: {} Compression: {}", fileStatus.getPath().getName(), reader.hdfsConfiguration.getEncoding(), compression.toString());
                try {
                    inputStream = reader.client.open(fileStatus.getPath());
                    if( compression.equals(HdfsWriterConfiguration.Compression.GZIP))
                        inputStream = new GZIPInputStream(inputStream);
                    inputStreamReader = new InputStreamReader(inputStream, reader.hdfsConfiguration.getEncoding());
                    bufferedReader = new BufferedReader(inputStreamReader);
                } catch (Exception e) {
                    LOGGER.error("Exception Opening " + fileStatus.getPath(), e.getMessage());
                    return;
                }

                String line = "";
                do{
                    try {
                        line = bufferedReader.readLine();
                        if( !Strings.isNullOrEmpty(line) ) {
                            reader.countersCurrent.incrementAttempt();
                            StreamsDatum entry = reader.processLine(line);
                            if( entry != null ) {
                                reader.write(entry);
                                reader.countersCurrent.incrementStatus(DatumStatus.SUCCESS);
                            } else {
                                LOGGER.warn("processLine failed");
                                reader.countersCurrent.incrementStatus(DatumStatus.FAIL);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.warn("WebHdfsPersistReader readLine Exception: {}", e);
                        reader.countersCurrent.incrementStatus(DatumStatus.FAIL);
                    }
                } while( !Strings.isNullOrEmpty(line) );
                LOGGER.info("Finished Processing " + fileStatus.getPath().getName());
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    LOGGER.error("WebHdfsPersistReader Exception: {}", e);
                }
            }
        }

        LOGGER.info("WebHdfsPersistReaderTask Finished");

        Uninterruptibles.sleepUninterruptibly(15, TimeUnit.SECONDS);
    }

}
