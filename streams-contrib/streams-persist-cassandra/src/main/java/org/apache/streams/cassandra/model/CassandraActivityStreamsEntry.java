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

package org.apache.streams.cassandra.model;

import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

public class CassandraActivityStreamsEntry extends Activity implements Comparable<CassandraActivityStreamsEntry>{

    @JsonDeserialize(as=ActivityObject.class)
    private ActivityObject object;

    @JsonDeserialize(as=ActivityObject.class)
    private ActivityObject target;

    @JsonDeserialize(as=Actor.class)
    private Actor actor;

    @JsonDeserialize(as=String.class)
    private String provider;

    public int compareTo(CassandraActivityStreamsEntry entry){
        return (this.getPublished()).compareTo(entry.getPublished());
    }
}
