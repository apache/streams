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
package org.apache.streams.threaded.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.streams.jackson.StreamsJacksonMapper;

import java.io.Serializable;

public class StatusCounts implements Serializable {

    private String id;
    private String type;
    private long working;
    private long success;
    private long failed;
    private long timeSpentSuccess;
    private long timeSpentFailure;

    public String getId()                   { return id; }
    public String getType()                 { return type; }
    public long getWorking()                { return this.working; }
    public long getSuccess()                { return this.success; }
    public long getFailed()                 { return this.failed; }
    public long getTimeSpentSuccess()       { return this.timeSpentSuccess; }
    public long getTimeSpentFailure()       { return this.timeSpentFailure; }

    public double getAverageSuccessTime()   { return this.success == 0 ? 0 : (double)this.timeSpentSuccess / (double)this.success; }
    public double getAverageFailureTime()   { return this.failed == 0 ? 0 : (double)this.timeSpentFailure / (double)this.failed; }
    public double getAverageTimeSpent()     { return (this.success + this.failed == 0) ? 0 : ((double)this.timeSpentSuccess + (double)this.timeSpentFailure) / ((double)this.success + (double)this.failed); }

    StatusCounts(String id, String type, long working, long success, long failed, long timeSpentSuccess, long timeSpentFailure) {
        this.id = id;
        this.type = type;
        this.working = working;
        this.success = success;
        this.failed = failed;
        this.timeSpentSuccess = timeSpentSuccess;
        this.timeSpentFailure = timeSpentFailure;
    }

    public String toString() {
        try {
            return StreamsJacksonMapper.getInstance().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return this.working + " " + this.success + " " + this.failed + " " + this.timeSpentSuccess + " " ;
        }
    }


}
