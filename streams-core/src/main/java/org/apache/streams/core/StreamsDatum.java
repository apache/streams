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

package org.apache.streams.core;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

/**
 * Created by sblackmon on 1/2/14.
 */
public class StreamsDatum implements Serializable {

    public StreamsDatum(Object document) {
        this.document = document;
    }

    public StreamsDatum(Object document, BigInteger sequenceid) {

        this.document = document;
        this.sequenceid = sequenceid;
    }

    public StreamsDatum(Object document, DateTime timestamp) {

        this.document = document;
        this.timestamp = timestamp;
    }

    public StreamsDatum(Object document, DateTime timestamp, BigInteger sequenceid) {
        this.document = document;
        this.timestamp = timestamp;
        this.sequenceid = sequenceid;
    }

    public DateTime timestamp;

    public BigInteger sequenceid;

    public Map<String, Object> metadata;

    public Object document;

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigInteger getSequenceid() {
        return sequenceid;
    }

    public void setSequenceid(BigInteger sequenceid) {
        this.sequenceid = sequenceid;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Object getDocument() {
        return document;
    }

    public void setDocument(Object document) {
        this.document = document;
    }
}
