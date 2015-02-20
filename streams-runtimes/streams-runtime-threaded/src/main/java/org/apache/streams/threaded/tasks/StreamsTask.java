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

import org.apache.streams.core.StreamsDatum;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for all task that will be used to execute instances of {@link org.apache.streams.core.StreamsOperation}
 * in local mode.
 */
public interface StreamsTask {

    public StatusCounts getCurrentStatus();

    public Collection<StreamsTask> getChildren();

    public String getId();

    public void process(StreamsDatum datum);

    public void initialize(final Map<String, StreamsTask> ctx);

    public void addOutputQueue(String id);

    public void prepare(Object configuration);

    public void cleanup();
}
