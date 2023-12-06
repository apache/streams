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

package org.apache.streams.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.streams.core.StreamsDatum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Random;

/**
 * KafkaPersistReaderTask reads documents from kafka on behalf of
 * @see org.apache.streams.kafka.KafkaPersistReader
 */
public class KafkaPersistReaderTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPersistReaderTask.class);

  private KafkaPersistReader reader;

  public KafkaPersistReaderTask(KafkaPersistReader reader) {
    this.reader = reader;
  }

  @Override
  public void run() {

    ConsumerRecords<String,String> records = reader.consumer.poll(Duration.ofMillis(100));

    while (true) {

      for (ConsumerRecord<String, String> record : records) {
        reader.persistQueue.add(new StreamsDatum(record.value(), record.key()));
      }
      try {
        Thread.sleep(new Random().nextInt(100));
      } catch (InterruptedException interrupt) {
        LOGGER.trace("Interrupt", interrupt);
      }
    }

  }

}
