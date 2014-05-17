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
package org.apache.streams.local.monitors;

import org.apache.streams.core.DatumStatusCountable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public class StatusCounterMonitorThread extends TimerTask
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusCounterMonitorThread.class);

    private final DatumStatusCountable task;

    public StatusCounterMonitorThread(DatumStatusCountable task) {
        this.task = task;
    }

    public void run() {
        LOGGER.info("{}: {} attempted, {} success, {} partial, {} failed, {} total",
                task.toString(),
                task.getDatumStatusCounter().getAttempted(),
                task.getDatumStatusCounter().getSuccess(),
                task.getDatumStatusCounter().getPartial(),
                task.getDatumStatusCounter().getFail(),
                task.getDatumStatusCounter().getEmitted());
    }
}
