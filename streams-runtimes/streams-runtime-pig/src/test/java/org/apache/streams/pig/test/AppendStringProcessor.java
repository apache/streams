/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.pig.test;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to Test Pig processor wrapper with arguments to prepare method
 */
public class AppendStringProcessor implements StreamsProcessor {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AppendStringProcessor.class);

    String append;

    public AppendStringProcessor() {
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        List<StreamsDatum> resultSet;
        resultSet = new LinkedList<StreamsDatum>();
        String value = (String) entry.getDocument()+ new String(append);
        resultSet.add(new StreamsDatum(value));
        return resultSet;
    }

    @Override
    public void prepare(Object configurationObject) {
        append = ((String[]) configurationObject)[0];
    }

    @Override
    public void cleanUp() {
        LOGGER.info("Processor clean up");
    }
}
