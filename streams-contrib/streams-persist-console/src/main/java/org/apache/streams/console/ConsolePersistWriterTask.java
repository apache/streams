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

package org.apache.streams.console;

import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class ConsolePersistWriterTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePersistWriterTask.class);

    private ConsolePersistWriter writer;

    public ConsolePersistWriterTask(ConsolePersistWriter writer) {
        this.writer = writer;
    }

    @Override
    public void run() {
        while(true) {
            if( writer.persistQueue.peek() != null ) {
                try {
                    StreamsDatum entry = writer.persistQueue.remove();
                    writer.write(entry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(new Random().nextInt(100));
            } catch (InterruptedException e) {}
        }
    }

}
