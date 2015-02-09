/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.streams.datasift.provider;

import com.datasift.client.stream.ErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for exceptions from the datasift streams and resets connections on errors.
 */
public class ErrorHandler extends ErrorListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    private String streamHash;
    private DatasiftStreamProvider provider;

    public ErrorHandler(DatasiftStreamProvider provider, String streamHash) {
        this.provider = provider;
        this.streamHash = streamHash;
    }

    @Override
    public void exceptionCaught(Throwable throwable) {
        LOGGER.error("DatasiftClient received Exception : {}", throwable);
        LOGGER.info("Attempting to restart client for stream hash : {}", this.streamHash);
        this.provider.startStreamForHash(this.streamHash);
    }
}
