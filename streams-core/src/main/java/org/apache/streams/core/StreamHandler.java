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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 1/6/14.
 */
public class StreamHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamHandler.class);

    private volatile StreamState state;

    public void setState(StreamState state) {
        this.state = state;
    }

    public StreamState getState() {
        return this.state;
    }
}
