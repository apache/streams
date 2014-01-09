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
public abstract class StreamsDatum {

    public DateTime timestamp;

    public BigInteger sequenceid;

    public Map<String, Object> metadata;

    public Serializable document;

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

    public Serializable getDocument() {
        return document;
    }

    public void setDocument(Serializable document) {
        this.document = document;
    }
}
