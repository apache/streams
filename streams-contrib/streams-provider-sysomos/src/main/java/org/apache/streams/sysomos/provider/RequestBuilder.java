/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.sysomos.provider;

import com.sysomos.xml.BeatApi;
import org.joda.time.DateTime;

import java.net.URL;

/**
 * Simplifying abstraction that aids in building a request to the Sysomos API in a chained fashion
 */
public interface RequestBuilder {
    /**
     * Sets the date after which documents should be returned from Sysomos
     * @param afterDate the {@link org.joda.time.DateTime} instance representing the after date
     *
     * @return The RequestBuilder for continued Chaining
     */
    RequestBuilder setAddedAfterDate(DateTime afterDate);

    /**
     * Sets the date before which documents should be returned from Sysomos
     * @param beforeDate the {@link org.joda.time.DateTime} instance representing the before date
     *
     * @return The RequestBuilder for continued Chaining
     */
    RequestBuilder setAddedBeforeDate(DateTime beforeDate);

    /**
     * Sets the size of the expected response
     * @param size the number of documents
     *
     * @return The RequestBuilder for continued Chaining
     */
    RequestBuilder setReturnSetSize(long size);

    /**
     * Sets the starting offset for the number of documents given the other parameters
     * @param offset the starting offset
     *
     * @return The RequestBuilder for continued Chaining
     */
    RequestBuilder setOffset(long offset);

    /**
     * Sets the Sysomos Heartbeat ID
     * @param hid Heartbeat ID
     *
     * @return The RequestBuilder for continued Chaining
     */
    RequestBuilder setHeartBeatId(int hid);

    /**
     *
     * Sets the Sysomos Heartbeat ID as a String
     * @param hid Heartbeat ID string
     *
     * @return The RequestBuilder for continued Chaining
     */
    RequestBuilder setHeartBeatId(String hid);

    /**
     * Returns the full url need to execute a request.
     *
     * Example:
     * http://api.sysomos.com/dev/v1/heartbeat/content?apiKey=YOUR
     * -APIKEY&hid=YOUR-HEARTBEAT-ID&offset=0&size=10&
     * addedAfter=2010-10-15T13:00:00Z&addedBefore=2010-10-18T13:00:00Z
     *
     * @return the URL to use when making requests of Sysomos Heartbeat
     */
    URL getRequestUrl();

    /**
     * Executes the request to the Sysomos Heartbeat API and returns a valid response
     */
    BeatApi.BeatResponse execute();
}
