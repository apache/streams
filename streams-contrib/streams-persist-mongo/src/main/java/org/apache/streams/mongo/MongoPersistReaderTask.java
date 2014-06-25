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

package org.apache.streams.mongo;

import com.google.common.base.Strings;
import com.mongodb.DBObject;
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.StreamsDatum;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MongoPersistReaderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoPersistReaderTask.class);

    private MongoPersistReader reader;

    public MongoPersistReaderTask(MongoPersistReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {

        try {
            while(reader.cursor.hasNext()) {
                DBObject dbObject = reader.cursor.next();
                StreamsDatum datum = reader.prepareDatum(dbObject);
                write(datum);
            }
        } finally {
            reader.cursor.close();
        }

    }

    //The locking may appear to be counter intuitive but we really don't care if multiple threads offer to the queue
    //as it is a synchronized queue.  What we do care about is that we don't want to be offering to the current reference
    //if the queue is being replaced with a new instance
    protected void write(StreamsDatum entry) {
        boolean success;
        do {
            try {
                reader.lock.readLock().lock();
                success = reader.persistQueue.offer(entry);
                Thread.yield();
            }finally {
                reader.lock.readLock().unlock();
            }
        }
        while (!success);
    }

}
