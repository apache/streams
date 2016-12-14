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

package org.apache.streams.amazon.kinesis;

import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;

import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.services.kinesis.model.GetShardIteratorResult;
import com.amazonaws.services.kinesis.model.Record;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KinesisPersistReaderTask reads documents from kinesis on behalf of
 * @see {@link KinesisPersistReader}.
 */
public class KinesisPersistReaderTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(KinesisPersistReaderTask.class);

  private KinesisPersistReader reader;
  private String streamName;
  private String shardId;

  private String shardIteratorId;

  private Long pollInterval = StreamsConfigurator.detectConfiguration().getBatchFrequencyMs();

  /**
   * KinesisPersistReaderTask constructor.
   */
  public KinesisPersistReaderTask(KinesisPersistReader reader, String streamName, String shardId) {
    this.reader = reader;
    this.streamName = streamName;
    this.shardId = shardId;
  }

  @Override
  public void run() {

    GetShardIteratorRequest shardIteratorRequest = new GetShardIteratorRequest()
        .withStreamName(this.streamName)
        .withShardId(shardId)
        .withShardIteratorType("TRIM_HORIZON");

    GetShardIteratorResult shardIteratorResult = reader.client.getShardIterator(shardIteratorRequest);

    shardIteratorId = shardIteratorResult.getShardIterator();

    Map<String,Object> metadata = new HashMap<>();
    metadata.put("streamName", streamName);
    metadata.put("shardId", shardId);

    while (true) {

      GetRecordsRequest recordsRequest = new GetRecordsRequest()
          .withShardIterator(shardIteratorId);

      GetRecordsResult recordsResult = reader.client.getRecords(recordsRequest);

      LOGGER.info("{} records {} millis behind {}:{}:{} ", recordsResult.getRecords().size(), recordsResult.getMillisBehindLatest(), streamName, shardId, shardIteratorId);

      shardIteratorId = recordsResult.getNextShardIterator();

      List<Record> recordList = recordsResult.getRecords();

      for (Record record : recordList) {
        try {
          byte[] byteArray = record.getData().array();
          //byte[] decoded = Base64.decode(byteArray);
          String message = new String(byteArray, Charset.forName("UTF-8"));
          reader.persistQueue.add(
              new StreamsDatum(
                  message,
                  record.getPartitionKey(),
                  new DateTime(),
                  new BigInteger(record.getSequenceNumber()),
                  metadata));
        } catch ( Exception ex ) {
          LOGGER.warn("Exception processing record {}: {}", record, ex);
        }
      }
      try {
        Thread.sleep(reader.pollInterval);
      } catch (InterruptedException ex) {
        LOGGER.trace("InterruptedException", ex);
      }
    }

  }

}
