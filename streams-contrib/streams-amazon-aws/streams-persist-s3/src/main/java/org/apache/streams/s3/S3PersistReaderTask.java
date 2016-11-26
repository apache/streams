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

import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.util.ComponentUtils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;

/**
 * S3PersistReaderTask reads documents from s3 on behalf of
 * @see org.apache.streams.s3.S3PersistReader
 */
public class S3PersistReaderTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3PersistReaderTask.class);

  private S3PersistReader reader;

  public S3PersistReaderTask(S3PersistReader reader) {
    this.reader = reader;
  }

  @Override
  public void run() {

    for (String file : reader.getFiles()) {

      // Create our buffered reader
      S3ObjectInputStreamWrapper is = new S3ObjectInputStreamWrapper(reader.getAmazonS3Client().getObject(reader.getBucketName(), file));
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
      LOGGER.info("Reading: {} ", file);

      String line;
      try {
        while ((line = bufferedReader.readLine()) != null) {
          if ( !Strings.isNullOrEmpty(line) ) {
            reader.countersCurrent.incrementAttempt();
            StreamsDatum entry = reader.lineReaderUtil.processLine(line);
            ComponentUtils.offerUntilSuccess(entry, reader.persistQueue);
            reader.countersCurrent.incrementStatus(DatumStatus.SUCCESS);
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        LOGGER.warn(ex.getMessage());
        reader.countersCurrent.incrementStatus(DatumStatus.FAIL);
      }

      LOGGER.info("Completed:  " + file);

      try {
        closeSafely(file, is);
      } catch (Exception ex) {
        LOGGER.error(ex.getMessage());
      }
    }
  }

  private static void closeSafely(String file, Closeable closeable) {
    try {
      closeable.close();
    } catch (Exception ex) {
      LOGGER.error("There was an issue closing file: {}", file);
    }
  }
}
