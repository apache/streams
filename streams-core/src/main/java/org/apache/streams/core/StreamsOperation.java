package org.apache.streams.core;

/*
 * #%L
 * streams-core
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;

/**
 *
 */
public interface StreamsOperation extends Serializable {

    /**
     * This method will be called after initialization/serialization. Initialize any non-serializable objects here.
     * @param configurationObject Any object to help intialize the operation. ie. Map, JobContext, Properties, etc. The type
     *                            will be based on where the operation is being run (ie. hadoop, storm, locally, etc.)
     */
    public void prepare(Object configurationObject);

    /**
     * No guarantee that this method will ever be called.  But upon shutdown of the stream, an attempt to call this method
     * will be made.
     * Use this method to terminate connections, etc.
     */
    public void cleanUp();
}
