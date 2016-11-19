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

import org.apache.streams.core.StreamsDatum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;

/**
 * KafkaPersistReaderTask reads documents from kafka on behalf of
 * @see org.apache.streams.kafka.KafkaPersistReader
 */
public class KafkaPersistReaderTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPersistReaderTask.class);

  private KafkaPersistReader reader;
  private KafkaStream<String,String> stream;

  public KafkaPersistReaderTask(KafkaPersistReader reader, KafkaStream<String,String> stream) {
    this.reader = reader;
    this.stream = stream;
  }

  @Override
  public void run() {

    MessageAndMetadata<String,String> item;
    while (true) {

      ConsumerIterator<String, String> it = stream.iterator();
      while (it.hasNext()) {
        item = it.next();
        reader.persistQueue.add(new StreamsDatum(item.message()));
      }
      try {
        Thread.sleep(new Random().nextInt(100));
      } catch (InterruptedException interrupt) {
        LOGGER.trace("Interrupt", interrupt);
      }
    }

  }

}
