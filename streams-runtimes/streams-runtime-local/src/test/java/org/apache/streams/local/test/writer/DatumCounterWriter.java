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

/**
 * Created by rebanks on 2/18/14.
 */
public class DatumCounterWriter implements StreamsPersistWriter{

    private int counter = 0;

    @Override
    public void write(StreamsDatum entry) {
        ++this.counter;
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {
        System.out.println("clean up called");
    }

    public int getDatumsCounted() {
        return this.counter;
    }
}
