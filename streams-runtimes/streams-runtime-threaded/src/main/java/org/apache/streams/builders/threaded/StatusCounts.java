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
package org.apache.streams.builders.threaded;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.streams.jackson.StreamsJacksonMapper;

import java.io.Serializable;

public class StatusCounts implements Serializable {

    private long queue;
    private long working;
    private long success;
    private long failed;

    public long getQueue()      { return queue; }
    public long getWorking()    { return working; }
    public long getSuccess()    { return success; }
    public long getFailed()     { return failed; }

    StatusCounts(long queue, long working, long success, long failed) {
        this.queue = queue;
        this.working = working;
        this.success = success;
        this.failed = failed;
    }

    public String toString() {
        try {
            return StreamsJacksonMapper.getInstance().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return this.queue + " " + this.working + " " + this.success + " " + this.failed;
        }
    }

}
