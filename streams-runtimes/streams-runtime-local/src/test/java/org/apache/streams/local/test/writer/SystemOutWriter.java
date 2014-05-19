package org.apache.streams.core.test.writer;

/*
 * #%L
 * streams-runtime-local
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
import org.apache.streams.core.StreamsPersistWriter;

/**
 * Created by rebanks on 2/20/14.
 */
public class SystemOutWriter implements StreamsPersistWriter {
    @Override
    public void write(StreamsDatum entry) {
        System.out.println(entry.document);
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {
        System.out.println("Clean up called writer!");
    }
}
