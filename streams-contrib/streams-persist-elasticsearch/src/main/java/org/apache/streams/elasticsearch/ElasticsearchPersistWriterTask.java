package org.apache.streams.elasticsearch;

/*
 * #%L
 * streams-persist-elasticsearch
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class ElasticsearchPersistWriterTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistWriterTask.class);

    private ElasticsearchPersistWriter writer;

    public ElasticsearchPersistWriterTask(ElasticsearchPersistWriter writer) {
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
                Thread.sleep(new Random().nextInt(1));
            } catch (InterruptedException e) {}
        }

    }

}
