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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatumCollectorWriter implements StreamsPersistWriter {

    private final List<StreamsDatum> datums = Collections.synchronizedList(new ArrayList<StreamsDatum>());
    private boolean cleanupCalled = false;
    private boolean prepareCalled = false;

    public boolean wasCleanupCalled() { return this.cleanupCalled; }
    public boolean wasPrepeareCalled() { return this.prepareCalled; }

    public List<StreamsDatum> getDatums() { return this.datums; }

    @Override
    public void write(StreamsDatum entry) {
        this.datums.add(entry);
    }

    @Override
    public void prepare(Object configurationObject) {
        this.prepareCalled = true;
    }

    @Override
    public void cleanUp() {
        this.cleanupCalled = true;
    }
}