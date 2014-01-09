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

import org.apache.rave.model.ActivityStreamsObject;
import org.apache.rave.portal.model.impl.ActivityStreamsEntryImpl;
import org.apache.rave.portal.model.impl.ActivityStreamsObjectImpl;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.Date;

public class CassandraActivityStreamsEntry extends ActivityStreamsEntryImpl implements Comparable<CassandraActivityStreamsEntry>{

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject object;

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject target;

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject actor;

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject provider;

    public int compareTo(CassandraActivityStreamsEntry entry){
        return (this.getPublished()).compareTo(entry.getPublished());
    }
}
