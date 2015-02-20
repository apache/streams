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
package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This writer does exactly what you'd expect, nothing.
 */
public class DoNothingWriter implements StreamsPersistWriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(DoNothingWriter.class);
    private boolean cleanupCalled = false;
    private boolean prepareCalled = false;

    public boolean wasCleanupCalled() { return this.cleanupCalled; }
    public boolean wasPrepeareCalled() { return this.prepareCalled; }

    public void write(StreamsDatum entry) {
    }

    public void prepare(Object configurationObject) {
        this.prepareCalled = true;
    }

    public void cleanUp() {
        this.cleanupCalled = true;
    }
}
