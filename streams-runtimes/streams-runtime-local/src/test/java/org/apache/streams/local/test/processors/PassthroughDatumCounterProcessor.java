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

package org.apache.streams.core.test.processors;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.*;

/**
 * Created by rebanks on 2/18/14.
 */
public class PassthroughDatumCounterProcessor implements StreamsProcessor {

    public static Set<Integer> claimedNumber = new HashSet<Integer>();
    public static final Random rand = new Random();
    public static Set<Integer> sawData = new HashSet<Integer>();

    private int count = 0;
    private int id;

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        ++this.count;
        List<StreamsDatum> result = new LinkedList<StreamsDatum>();
        result.add(entry);
        synchronized (sawData) {
            sawData.add(this.id);
        }
        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        synchronized (claimedNumber) {
            this.id = rand.nextInt();
            while(!claimedNumber.add(this.id)) {
                this.id = rand.nextInt();
            }
        }
    }

    @Override
    public void cleanUp() {

    }

    public int getMessageCount() {
        return this.count;
    }
}
