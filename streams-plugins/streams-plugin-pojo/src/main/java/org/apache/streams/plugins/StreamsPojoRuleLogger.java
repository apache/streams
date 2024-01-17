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

package org.apache.streams.plugins;

import org.jsonschema2pojo.RuleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamsPojoRuleLogger implements RuleLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamsPojoRuleLogger.class);

    @Override
    public void debug(String s) {
        LOGGER.debug(s);
    }

    @Override
    public void error(String s) {
        LOGGER.error(s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        LOGGER.error(s, throwable);
    }

    @Override
    public void info(String s) {
        LOGGER.info(s);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void trace(String s) {
        LOGGER.trace(s);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        LOGGER.warn(s, throwable);
    }

    @Override
    public void warn(String s) {
        LOGGER.warn(s);
    }
}
