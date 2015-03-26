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
package org.apache.streams.local.test.processors;

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.LinkedList;
import java.util.List;

/**
 * This processor takes in an object type and enforces that every document that
 * comes through is of that type.
 */
public class ObjectTypeEnforcerProcessor implements StreamsProcessor {
    private Class enforcedClass;
    private boolean incorrectClassPresent = false;

    public ObjectTypeEnforcerProcessor(Class enforcedClass) {
        this.enforcedClass = enforcedClass;
    }

    public List<StreamsDatum> process(StreamsDatum entry) {
        if(entry != null && entry.getDocument() != null && !entry.getDocument().getClass().equals(enforcedClass)) {
            incorrectClassPresent = true;
        }

        return Lists.newArrayList(entry);
    }

    public boolean getIncorrectClassPresent() {
        return this.incorrectClassPresent;
    }

    public void prepare(Object configurationObject) {
        // no Operation
    }

    public void cleanUp() {
        // no Operation
    }
}
